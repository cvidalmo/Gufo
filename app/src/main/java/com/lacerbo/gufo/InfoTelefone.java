package com.lacerbo.gufo;

/*
 * Created by Vidal on 23/02/2015.
 **********************************************************************/

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
//import android.net.wifi.WifiManager;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

class InfoTelefone {

    private TelephonyManager telephonyManager;
    private Integer nivelBateria = 0;
    private Boolean isPlugado = false;
    private String strData = "";
    private String strHora = "";
    //    private String macAddress = "";
//    private String ipAddress = "";
//    private Integer linkSpeed = 0;
    private String imeiSIM1 = "";
    //    private String imeiSIM2 = "";
//    private String serialSIM1 = "";
//    private String serialSIM2 = "";
    private String operatorSIM1 = "";
    //    private String operatorSIM2 = "";
//    private String phoneSIM1 = "";
//    private String phoneSIM2 = "";
//    private Boolean isSIM1Ready = false;
//    private Boolean isSIM2Ready = false;
    private String modeloFab = "";

    Integer getNivelBateria() {
        return nivelBateria;
    }

    String getData() {
        return strData;
    }

    String getHora() {
        return strHora;
    }

    Boolean getIsPlugado() {
        return isPlugado;
    }

//    public String getMacAddress() {
//        return macAddress;
//    }
//
//    public String getIpAddress() {
//        return ipAddress;
//    }
//
//    public Integer getLinkSpeed() {
//        return linkSpeed;
//    }

    String getImeiSIM1() {
        return imeiSIM1;
    }

    /*public String getImeiSIM2() {
        return imeiSIM2;
    }

    public Boolean isSIM1Ready() {
        return isSIM1Ready;
    }

    public Boolean isSIM2Ready() {
        return isSIM2Ready;
    }

    public Boolean isDualSIM() {
        return imeiSIM2 != null;
    }

    public String getSerialSIM1() {
        return serialSIM1;
    }

    public String getSerialSIM2() {
        return serialSIM2;
    }

    public String getPhoneSIM1() {
        return phoneSIM1;
    }

    public String getPhoneSIM2() {
        return phoneSIM2;
    }
*/
    String getOperatorSIM1() {
        return operatorSIM1;
    }

//    public String getOperatorSIM2() {
//        return operatorSIM2;
//    }

    String getModeloFab() {
        return modeloFab;
    }

    public void getInstance(Context context) {

        IntentFilter ifBateria = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifBateria);
        nivelBateria = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : 0;

        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent != null ? intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) : 0;
        isPlugado = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy_HHmmss");
        String currentDateAndTime = sdf.format(new Date());

        strData = currentDateAndTime.substring(0, 6);
        strHora = currentDateAndTime.substring(7, 13);

//        WifiManager mWifiManager;
//        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

//        macAddress = mWifiManager.getConnectionInfo().getMacAddress();
//        ipAddress = String.valueOf(mWifiManager.getConnectionInfo().getIpAddress());
//        linkSpeed = mWifiManager.getConnectionInfo().getLinkSpeed();

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;

        if (model.startsWith(manufacturer)) {
            modeloFab = model;
        } else {
            modeloFab = manufacturer + " " + model;
        }

        telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TO DO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        imeiSIM1 = telephonyManager.getDeviceId();
//        imeiSIM2 = null;

        try {
            imeiSIM1 = getDeviceIdBySlot("getDeviceIdGemini");
//            imeiSIM2 = getDeviceIdBySlot("getDeviceIdGemini", 1);
        } catch (GeminiMethodNotFoundException e) {
            e.printStackTrace();

            try {
                imeiSIM1 = getDeviceIdBySlot("getDeviceId");
//                imeiSIM2 = getDeviceIdBySlot("getDeviceId", 1);
            } catch (GeminiMethodNotFoundException e1) {
                //Call here for next manufacturer's predicted method name if you wish
                e1.printStackTrace();
            }
        }

        imeiSIM1 = "000000000000000" + imeiSIM1.trim();
        imeiSIM1 = imeiSIM1.substring(imeiSIM1.length()-15);

//        if (imeiSIM2 != null) {
//            imeiSIM2 = "000000000000000" + imeiSIM2.trim();
//            imeiSIM2 = imeiSIM2.substring(imeiSIM2.length() - 15);
//        }

/*
        isSIM1Ready = telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;
        isSIM2Ready = false;

        try {
            isSIM1Ready = getSIMStateBySlot("getSimStateGemini", 0);
            isSIM2Ready = getSIMStateBySlot("getSimStateGemini", 1);
        } catch (GeminiMethodNotFoundException e) {

            e.printStackTrace();

            try {
                isSIM1Ready = getSIMStateBySlot("getSimState", 0);
                isSIM2Ready = getSIMStateBySlot("getSimState", 1);
            } catch (GeminiMethodNotFoundException e1) {
                //Call here for next manufacturer's predicted method name if you wish
                e1.printStackTrace();
            }
        }

        serialSIM1 = telephonyManager.getSimSerialNumber();
        serialSIM2 = null;

        try {
            serialSIM1 = getNumberPhoneBySlot("getSimSerialNumberGemini", 0);
            serialSIM2 = getNumberPhoneBySlot("getSimSerialNumberGemini", 1);
        } catch (GeminiMethodNotFoundException e) {
            e.printStackTrace();

            try {
                serialSIM1 = getNumberPhoneBySlot("getSimSerialNumber", 0);
                serialSIM2 = getNumberPhoneBySlot("getSimSerialNumber", 1);
            } catch (GeminiMethodNotFoundException e1) {
                //Call here for next manufacturer's predicted method name if you wish
                e1.printStackTrace();
            }
        }
*/

        operatorSIM1 = telephonyManager.getSimOperatorName();
//        operatorSIM2 = null;

        try {
            operatorSIM1 = getNumberPhoneBySlot("getSimOperatorNameGemini");
//            operatorSIM2 = getNumberPhoneBySlot("getSimOperatorNameGemini", 1);
        } catch (GeminiMethodNotFoundException e) {
            e.printStackTrace();

            try {
                operatorSIM1 = getNumberPhoneBySlot("getSimOperatorName");
//                operatorSIM2 = getNumberPhoneBySlot("getSimOperatorName", 1);
            } catch (GeminiMethodNotFoundException e1) {
                //Call here for next manufacturer's predicted method name if you wish
                e1.printStackTrace();
            }
        }


        if (operatorSIM1 != null) {
            operatorSIM1 = operatorSIM1.toUpperCase();
        }

//        if (operatorSIM2 != null) {
//            operatorSIM2 = operatorSIM2.toUpperCase();
//        } // */

/*
        phoneSIM1 = telephonyManager.getLine1Number();
        phoneSIM2 = null;

        try {
            phoneSIM1 = getNumberPhoneBySlot("getLine1NumberGemini", 0);
            phoneSIM2 = getNumberPhoneBySlot("getLine1NumberGemini", 1);
        } catch (GeminiMethodNotFoundException e) {
            e.printStackTrace();

            try {
                phoneSIM1 = getNumberPhoneBySlot("getLine1Number", 0);
                phoneSIM2 = getNumberPhoneBySlot("getLine1Number", 1);
            } catch (GeminiMethodNotFoundException e1) {
                //Call here for next manufacturer's predicted method name if you wish
                e1.printStackTrace();
            }
        }
*/

    }

    private String getDeviceIdBySlot(String predictedMethodName) throws GeminiMethodNotFoundException {

        String imei = null;

        try{

            Class<?> telephonyClass = Class.forName(telephonyManager.getClass().getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = 0;
            Object ob_phone = getSimID.invoke(telephonyManager, obParameter);

            if(ob_phone != null){
                imei = ob_phone.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return imei;
    }

/*
    private boolean getSIMStateBySlot(String predictedMethodName, int slotID) throws GeminiMethodNotFoundException {

        boolean isReady = false;

        try{

            Class<?> telephonyClass = Class.forName(telephonyManager.getClass().getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimStateGemini = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimStateGemini.invoke(telephonyManager, obParameter);

            if(ob_phone != null){
                int simState = Integer.parseInt(ob_phone.toString());
                if(simState == TelephonyManager.SIM_STATE_READY){
                    isReady = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return isReady;
    }
*/

    private String getNumberPhoneBySlot(String predictedMethodName) throws GeminiMethodNotFoundException {

        String phoneNumber = null;

        try{

            Class<?> telephonyClass = Class.forName(telephonyManager.getClass().getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimNumber = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = 0;
            Object ob_phone = getSimNumber.invoke(telephonyManager, obParameter);

            if(ob_phone != null){
                phoneNumber = ob_phone.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return phoneNumber;
    }

    private static class GeminiMethodNotFoundException extends Exception {

        private static final long serialVersionUID = -996812356902545308L;

        GeminiMethodNotFoundException(String info) {
            super(info);
        }
    }

    /* Tabela de de funções e suas descrições: **************************************
     *
     *  getPhoneCount() - Retorna o número de CHIPs disponíveis no telefone.
     *
     *********************************************************************************/
}
