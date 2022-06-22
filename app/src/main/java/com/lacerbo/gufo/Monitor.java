package com.lacerbo.gufo;

import android.Manifest;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
//import android.hardware.display.DisplayManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
//import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
//import android.view.Display;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Vidal on 22/10/2015.
 ***************************************/
public class Monitor extends Service {

    private EnviarDados enviarDados;
    private Location mLocationAnterior = null;
    private MyGpsListener myGpsListener;
    private LocationManager mLocationManager;
    private float MIN_DISTANCE = 30;  //Metros
    private Integer MIN_TIME = 30 * 1000;  //MiliSegundos.
    private String mMessageID = "";

    private SensorManager mSensorManagerAccelerometer;
    private Sensor mAccelerometer;
    private MySensorListenerAccelerometer mySensorListenerAccelerometer;

    private float floatDeltaZAnt;  //floatDeltaXAnt, floatDeltaYAnt,
    private Calendar dtAnterior = Calendar.getInstance();

    private Propriedades mPropriedades;
    private Boolean booGPSProvider = false;
    private Boolean booNetworkProvider = false;

    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "gufo");
        wakeLock.acquire(10*60*1000L /*10 minutes*/);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        enviarDados = new EnviarDados();
        dtAnterior = Calendar.getInstance();

        mPropriedades = new Propriedades(getApplicationContext());
        mPropriedades.salvarPropriedades("PI_lonDataHoraMonitor", Calendar.getInstance().getTimeInMillis());

        // GPS (Localização).
        Boolean booPermissaoGPS = (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        myGpsListener = new MyGpsListener();
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        // Sensor aprocimação. Usado para saber se colocou o celular no ouvido.
        SensorManager mSensorManagerProximity = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert mSensorManagerProximity != null;
        Sensor mProximity = mSensorManagerProximity.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        MySensorListenerProximity mySensorListenerProximity = new MySensorListenerProximity();
        mSensorManagerProximity.registerListener(mySensorListenerProximity, mProximity, SensorManager.SENSOR_DELAY_NORMAL);

        // Sensor Movimento (Balanço).
        mySensorListenerAccelerometer = new MySensorListenerAccelerometer();
        mSensorManagerAccelerometer = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert mSensorManagerAccelerometer != null;
        mAccelerometer = mSensorManagerAccelerometer.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // SMSs de SAÍDA (que são enviados).
        MyObserver myObserver = new MyObserver();
        ContentResolver mContentResolver = getBaseContext().getContentResolver();
        mContentResolver.registerContentObserver(Uri.parse("content://sms/"), true, myObserver);

        if (booPermissaoGPS) {
            ativaMonitor();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void ativaMonitor() {

        final Handler mHandler = new Handler();

        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {

                MIN_DISTANCE = mPropriedades.pegarFloatPropriedades("PI_floDistancia");
                MIN_TIME = mPropriedades.pegarIntegerPropriedades("PI_intTempoTransmissao") * 1000;

                mPropriedades.salvarPropriedades("PI_lonDataHoraMonitor", Calendar.getInstance().getTimeInMillis());

                try {
                    booGPSProvider = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch (Exception ignored) {
                }
                try {
                    booNetworkProvider = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                } catch (Exception ignored) {
                }

                Location location = null;

                if (booGPSProvider) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TO DO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    myGpsListener.onLocationChanged(mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myGpsListener);
                    location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                } else if (booNetworkProvider) {
                    myGpsListener.onLocationChanged(mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myGpsListener);
                    location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                } else {
                    enviarDados.getInstance(getApplicationContext(), "C", "Comando", "GD");  //GPS DESATIVADO.
                }

                if (location != null) {
                    if ((mLocationAnterior == null) || (location.distanceTo(mLocationAnterior) > MIN_DISTANCE)) {
                        String strLatitude, strLongitude, strPrecisao, strVelocidade, strRota;
                        Float floLatitude = 0.0f;
                        Float floLongitude = 0.0f;
                        Float floVelocidade = 0.0f;
                        Float floPrecisao = 0.0f;
                        Float floRota = 0.0f;

                        try {
                            floLatitude = (float) location.getLatitude();
                            floLongitude = (float) location.getLongitude();
                            floVelocidade = location.getSpeed();
                            floPrecisao = location.getAccuracy();
                            floRota = location.getBearing();

                        } catch (Exception e) {
                            enviarDados.getInstance(getApplicationContext(), "C", "Comando", "GED");  //GPS ERRO AO PEGAR DADOS.
                            mLocationManager.removeUpdates(myGpsListener);
                        }

                        if (floLatitude < 0.0f) {
                            strLatitude = String.valueOf(((int) (floLatitude * -1000000)) + 1000000000);
                        } else {
                            strLatitude = "00" + String.valueOf(((int) (floLatitude * 1000000)) + 1000000000);
                            strLatitude = "0" + strLatitude.substring(strLatitude.length() - 9);
                        }

                        if (floLongitude < 0.0f) {
                            strLongitude = String.valueOf(((int) (floLongitude * -1000000)) + 1000000000);
                        } else {
                            strLongitude = "00" + String.valueOf(((int) (floLongitude * 1000000)) + 1000000000);
                            strLongitude = "0" + strLongitude.substring(strLongitude.length() - 9);
                        }

                        strPrecisao = "00000" + String.valueOf((int) (floPrecisao * 1));
                        strPrecisao = strPrecisao.substring(strPrecisao.length() - 4);

                        strVelocidade = "0000" + String.valueOf((int) (floVelocidade * 1));
                        strVelocidade = strVelocidade.substring(strVelocidade.length() - 3);

                        strRota = "0000" + String.valueOf((int) (floRota * 1));
                        strRota = strRota.substring(strRota.length() - 3);

                        String strEndereco = ".";

                        if (!floLatitude.equals(0.0f)) {
                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            String result = null;

                            try {
                                List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                if (addressList != null && addressList.size() > 0) {
                                    Address address = addressList.get(0);
                                    StringBuilder sb = new StringBuilder();
                                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                                        sb.append(address.getAddressLine(i)).append(", ");
                                    }
                                    //sb.append(address.getLocality()).append("\n");
                                    //sb.append(address.getPostalCode()).append("\n");
                                    sb.append(address.getCountryName()).append(".");
                                    result = sb.toString();
                                }
                            } catch (Exception e) {
                                //Log.e(TAG, "Unable connect to Geocoder", e);
                                enviarDados.getInstance(getApplicationContext(), "C", "Comando", "GEG");  //GPS ERRO AO PEGAR GEOINFORMACOES.
                                mLocationManager.removeUpdates(myGpsListener);
                            }

                            if (result != null) {
                                strEndereco = result;
                            }
                        }
                        if (mLocationAnterior == null) {
                            mLocationAnterior = location;
                        }
                        mLocationAnterior.set(location);
                        String strDados = strLatitude + strLongitude + strPrecisao + strVelocidade + strRota + "," + strEndereco;
                        enviarDados.getInstance(getApplicationContext(), "T", "Andando", strDados);
                    } else {
                        if (isScreenOn()) {
                            enviarDados.getInstance(getApplicationContext(), "C", "Comando", "PDA");  //PING DISPLAY ATIVO.
                        } else {
                            enviarDados.getInstance(getApplicationContext(), "C", "Comando", "PDM");  //PING GPS DISTANCIA MINIMA.
                        }
                    }

                    mLocationManager.removeUpdates(myGpsListener);

                }

                //Reinicia a Thread.
                mHandler.postDelayed(this, MIN_TIME);
            }
        };

        mHandler.postDelayed(mRunnable, 30 * 1000);
    }

    private class MyGpsListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }

    private class MySensorListenerProximity implements SensorEventListener {

        @Override
        public final void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Do something here if sensor accuracy changes.
        }

        @Override
        public final void onSensorChanged(SensorEvent event) {

            mSensorManagerAccelerometer.unregisterListener(mySensorListenerAccelerometer);
            float floDistSensorAprox = event.values[0];  //Detectar se o sensor de aproximação foi obstruído (tapado).
            dtAnterior = Calendar.getInstance();  //Data imicial para o limite de tempo para fazer os moviemntos.
            floatDeltaZAnt = 0.0f;  //Zera contagem do Delta Z.

            if (isScreenOn() && floDistSensorAprox < mPropriedades.pegarFloatPropriedades("PI_floMenorDistSensorAprox"))
                mPropriedades.salvarPropriedades("PI_floMenorDistSensorAprox", floDistSensorAprox);

            // Sensor de aproximação (o que faz a tela apagar em chamadas) e com a tela ligada.
            //ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            //toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
            if (floDistSensorAprox < (mPropriedades.pegarFloatPropriedades("PI_floMenorDistSensorAprox") +1) && isScreenOn() && isChamadaOff())
                mSensorManagerAccelerometer.registerListener(mySensorListenerAccelerometer, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    private class MySensorListenerAccelerometer implements SensorEventListener {

        @Override
        public final void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Do something here if sensor accuracy changes.
        }

        @Override
        public final void onSensorChanged(SensorEvent event) {
            //int teste = Math.abs( (int) event.values[0]);

            float floatDeltaZ = event.values[2];  //Movimento Z, para frente e para trás, tipo se abanando.
            float floatDeltaY = event.values[1];  //Movimento Z, cabeça pra baixo ou pra cima.

            //Deve colocar o celular de cabeça pra baixo (posição negativa entre -0,01 e -10) e se abanar durante 2 segundos.
            // -5.00 = 45° graus, -10.00 = 90° graus.
            if (floatDeltaY < mPropriedades.pegarFloatPropriedades("PI_floCabecaPraBaixo")) {
                //sensibilidade dos movimentos (balanços, chaqualios, etc.).
                if ((floatDeltaZ - floatDeltaZAnt) > mPropriedades.pegarFloatPropriedades("PI_floSensibFinaAjuda") ||
                    (floatDeltaZAnt - floatDeltaZ) > mPropriedades.pegarFloatPropriedades("PI_floSensibFinaAjuda")) {

                    floatDeltaZAnt = floatDeltaZ;

                    // Limite de 2 segundos para fazer o movimento.
                    if ( (Calendar.getInstance().getTimeInMillis()/1000) - (dtAnterior.getTimeInMillis()/1000)  > mPropriedades.pegarIntegerPropriedades("PI_intSensibAjuda")) {

                        //ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                        //toneG.startTone(ToneGenerator.TONE_CDMA_ANSWER, 300);

                        dtAnterior = Calendar.getInstance();

                        Vibrator mVibrar = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                        long[] mPattern = {0, 900,500, 100,500 };  //O zero é para começar imediatamente, os pares sao: vibrar e pausa.
                        assert mVibrar != null;
                        mVibrar.vibrate(mPattern, -1);

                        enviarDados.getInstance(getApplicationContext(), "C", "Ajuda", "");

                        mSensorManagerAccelerometer.unregisterListener(mySensorListenerAccelerometer);
                    }
                }
            } else {
                dtAnterior = Calendar.getInstance();
            }

            if ( (Calendar.getInstance().getTimeInMillis()/1000) - (dtAnterior.getTimeInMillis()/1000)  > (mPropriedades.pegarIntegerPropriedades("PI_intSensibAjuda") +1)) {
                dtAnterior = Calendar.getInstance();
            }

        }

    }

    private class MyObserver extends ContentObserver {

        MyObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            Uri uriSMSURI = Uri.parse("content://sms/");
            Cursor cur = getContentResolver().query(uriSMSURI, null, null, null, null);

            if (cur != null) {
                cur.moveToNext();

                String message_id = cur.getString(cur.getColumnIndex("_id"));
                String type = cur.getString(cur.getColumnIndex("type"));

                if (Integer.parseInt(type) > 1) {
                    if (!message_id.equals(mMessageID)) {
                        String content = cur.getString(cur.getColumnIndex("body"));
                        String smsNumber = cur.getString(cur.getColumnIndex("address"));
                        if (smsNumber != null && smsNumber.length() > 0) {
                            enviarDados.getInstance(getApplicationContext(), "S", smsNumber, content);
                        }
                    }

                    mMessageID = message_id;
                }
                cur.close();
            }
        }

    }

    private Boolean isScreenOn () {
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
//            DisplayManager dm = (DisplayManager) getApplicationContext().getSystemService(Context.DISPLAY_SERVICE);
//            boolean screenOn = false;
//            assert dm != null;
//            for (Display display : dm.getDisplays()) {
//                if (display.getState() != Display.STATE_OFF) {
//                    screenOn = true;
//                }
//            }
//            return screenOn;
//        } else {
            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            assert pm != null;
            //noinspection deprecation
            return pm.isScreenOn();
//        }
    }

    private Boolean isChamadaOff () {
        TelephonyManager tmgr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        assert tmgr != null;
        return tmgr.getCallState() == TelephonyManager.CALL_STATE_IDLE;
    }

    /*
     *  Final do Monitor.java
     **********************************************************************************************/
}
