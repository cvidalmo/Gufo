package com.lacerbo.gufo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Vibrator;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.text.Html;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Calendar;

class EnviarDados {

    private Propriedades mPropriedades;

    private String strServidor, strVersaoApp;
    private Integer intPorta;
    private String strMsgEnviada, strMsgRecebida = null;
    private Context mContext;
    private FalaTexto mfalaTexto;
    private Boolean booSomCoruja, booVibrar, booFalar;
    private Vibrator mVibrar;
    private MediaPlayer[] mpSomCoruja;

    public void getInstance(Context context, String strTipo, String strArgIdentif, String strArgMsg){

        mContext = context;

        mPropriedades = new Propriedades(context);

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (!(netInfo != null && netInfo.isConnected()) ) {
            return;
        }

        //strServidor = mPropriedades.pegarStringPropriedades("PI_strIPServidor");
        strServidor = mPropriedades.pegarStringPropriedades("PI_strURLServidor");
        intPorta = mPropriedades.pegarIntegerPropriedades("PI_intPortaConexao");
        strVersaoApp = mPropriedades.pegarStringPropriedades("PI_strVersaoApp");

        strMsgEnviada = pegaDados(context, strTipo, strArgIdentif, strArgMsg);

        booSomCoruja = mPropriedades.pegarBooleanPropriedades("PI_booSomCoruja");
        booVibrar = mPropriedades.pegarBooleanPropriedades("PI_booVibrar");
        booFalar = mPropriedades.pegarBooleanPropriedades("PI_booFalar");
        mfalaTexto = new FalaTexto(mContext);

        if (booVibrar) {
            mVibrar = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        }

        if (booSomCoruja) {
            mpSomCoruja = new MediaPlayer[1];
            mpSomCoruja[0] = MediaPlayer.create(mContext, R.raw.coruja);
            mpSomCoruja[0].setVolume(1.0f, 1.0f);
        }

        //Toast.makeText(mContext, strMsgEnviada, Toast.LENGTH_LONG).show();

        new Thread(new Runnable() {
            @Override
            public void run() {

                try{
                    InetAddress addr = InetAddress.getByName(strServidor);
                    DatagramPacket dpEnviar = new DatagramPacket(strMsgEnviada.getBytes("UTF-8"), strMsgEnviada.length(), addr, intPorta);
                    DatagramSocket socketCliente = new DatagramSocket();
                    byte[] characters = new byte[500];
                    DatagramPacket dpReceber = new DatagramPacket(characters, characters.length);
                    Integer  intTentativasRecebendo;

                    socketCliente.send(dpEnviar);

                    strMsgRecebida = "";
                    intTentativasRecebendo = 0;

                    while (intTentativasRecebendo < 3 && strMsgRecebida.length() < 1) {
                        intTentativasRecebendo ++;
                        Thread.sleep(1000);
                        socketCliente.receive(dpReceber);
                        strMsgRecebida = new String(dpReceber.getData());
                        strMsgRecebida = strMsgRecebida.trim();
                    }

                    strMsgRecebida = strMsgRecebida.replace("[", "");
                    strMsgRecebida = strMsgRecebida.replace("]", "");
                    strMsgRecebida = strMsgRecebida.trim();

                    if (strMsgRecebida.length() > 2) {

                        if (strMsgRecebida.substring(0, 1).equals("!")) {  //Comando
                            strMsgRecebida = strMsgRecebida.substring(1);
                            String strComando = strMsgRecebida.substring(0, strMsgRecebida.indexOf(","));
                            String strValor = strMsgRecebida.substring(strMsgRecebida.indexOf(",")+1);

                            if (strComando.toLowerCase().contains("pi_boo")) {
                                mPropriedades.salvarPropriedades(strComando, Boolean.valueOf(strValor));
                            } else if (strComando.toLowerCase().contains("pi_int")) {
                                mPropriedades.salvarPropriedades(strComando, Integer.parseInt(strValor));
                            } else if (strComando.toLowerCase().contains("pi_flo")) {
                                mPropriedades.salvarPropriedades(strComando, Float.valueOf(strValor));
                            } else if (strComando.toLowerCase().contains("pi_lon")) {
                                mPropriedades.salvarPropriedades(strComando, Long.valueOf(strValor));
                            } else {
                                mPropriedades.salvarPropriedades(strComando, strValor);
                            }

                        } else {

                            String strObservacao = "", strLink = "";

                            if (strMsgRecebida.contains("=>")) {
                                strObservacao = strMsgRecebida.substring(strMsgRecebida.indexOf("=>")+2);
                                strMsgRecebida = strMsgRecebida.substring(0, strMsgRecebida.indexOf("=>"));
                                if (strObservacao.contains("#link:")) {
                                    strLink = strObservacao.substring(strObservacao.indexOf("#link:")+6);
                                    strObservacao = strObservacao.substring(0, strObservacao.indexOf("#link:"));
                                }
                            }

                            generateNotification(mContext, strMsgRecebida.substring(1), strObservacao, strLink);

                            if (booVibrar) {
                                long[] mPattern = {0, 900,500, 100,500 };  //O zero é para começar imediatamente, os pares sao: vibrar e pausa.
                                mVibrar.vibrate(mPattern, -1);
                                Thread.sleep(2000);
                            }

                            if (booSomCoruja) {
                                mpSomCoruja[0].start();
                                Thread.sleep(2500);
                            }

                            if ((booFalar) || (strMsgRecebida.substring(0,1).equals("#"))) {
                                strMsgRecebida = strMsgRecebida.substring(1);

                                mfalaTexto.LerTexto(strMsgRecebida.replaceAll("<(.*?)>",""), true, false);

                                while (mfalaTexto.isSpeaking()) {
                                    Thread.sleep(1000);
                                }
                            }
                        }

                    }

                    socketCliente.close();

                    if (mfalaTexto != null) {
                        mfalaTexto.destroy();
                    }

                    //Foi coloca aqui no final, para dar tempo do serviço Monitor acordar em casos de adormecer em certas horas da noite.
                    //Com isso não será executao nova instância do serviço desnecessáriamente.
                    if (mPropriedades.monitorOffBooleanProprieades()) {
                        mPropriedades.salvarPropriedades("PI_lonDataHoraMonitor", Calendar.getInstance().getTimeInMillis());
                        Intent intentMonitor = new Intent(mContext, Monitor.class);
                        mContext.startService(intentMonitor);
                    }

                } catch(IOException | InterruptedException e) {
                    //
                }

            }
        }).start();

    }

    private String pegaDados(Context context, String strTipo, String strArgIdentif, String strArgMsg) {

        // Retirar os acentos.
        String strIdentificacao = strArgIdentif.trim().toUpperCase();
        //strIdentificacao = Normalizer.normalize(strIdentificacao,Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");

        InfoTelefone mInfoTelefone = new InfoTelefone();
        mInfoTelefone.getInstance(context);

        Integer nivelBateria = mInfoTelefone.getNivelBateria();
        Boolean isPlugado = mInfoTelefone.getIsPlugado();
        String strData = mInfoTelefone.getData();
        String strHora = mInfoTelefone.getHora();
        //String macAddress = mInfoTelefone.getMacAddress();
        //String ipAddress = mInfoTelefone.getIpAddress();
        //Integer linkSpeed = mInfoTelefone.getLinkSpeed();

        String imeiSIM1 = mInfoTelefone.getImeiSIM1();
        //String imeiSIM2 = mInfoTelefone.getImeiSIM2();
        //String serialSIM1 = mInfoTelefone.getSerialSIM1();
        //String serialSIM2 = mInfoTelefone.getSerialSIM2();
        String operatorSIM1 = mInfoTelefone.getOperatorSIM1();
        //String operatorSIM2 = mInfoTelefone.getOperatorSIM2();
        //Boolean isSIM1Ready = mInfoTelefone.isSIM1Ready();
        //Boolean isSIM2Ready = mInfoTelefone.isSIM2Ready();
        String modeloFab = mInfoTelefone.getModeloFab();

        String strEndereco, strNivelBateria;

        strNivelBateria = "0000" + nivelBateria;
        strNivelBateria = strNivelBateria.substring(strNivelBateria.length()-3);

        String strRetorno = strTipo+strVersaoApp+imeiSIM1+strData+strHora;

        if (strTipo.equals("C")) {

            strEndereco = "";
            if (strArgMsg.length()>0) {
                strEndereco = "["+strArgMsg+"]";
            }

            // Latitude  Longitude Pre Ve Rota
            // ----------==========----===---
            strRetorno += "000000000000000000000000000000" +
                    strNivelBateria + (isPlugado ? "1" : "0") + "[" + strIdentificacao + "]" +
                    "[" + operatorSIM1.substring(0, 1) + ", " + modeloFab + "]" + strEndereco;

        } else if ("FRSEN".contains(strTipo)) {

            strEndereco = "";

            if ("SEN".contains(strTipo)) {
                strEndereco = trocaAcentos(strArgMsg.trim());
            }

            strEndereco = strIdentificacao + ", " + getNomeContatoUsandoNumero(strIdentificacao) + ", " + strEndereco;
            //strEndereco = Normalizer.normalize(strEndereco, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");

            switch (strTipo) {
                case "F": strIdentificacao = "CFEITA"; break;      //Chamada FEITA.
                case "R": strIdentificacao = "CRECEBIDA"; break;   //Chamada RECEBIDA.
                case "S": strIdentificacao = "SMSSAIDA"; break;    //SMS de SAIDA.
                case "E": strIdentificacao = "SMSENTRADA"; break;  //SMS de ENTRADA.
                case "N": strIdentificacao = "NOTIFICACAO"; break;  //Notificações.
            }
            // Latitude  Longitude Pre Ve Rota
            // ----------==========----===---
            strRetorno += "000000000000000000000000000000" +
                    strNivelBateria + (isPlugado ? "1" : "0") + "[" + strIdentificacao + "]" +
                    "[" + strEndereco + "]";
        } else {

            strEndereco = trocaAcentos(strArgMsg.substring(strArgMsg.indexOf(",") + 1));
            strRetorno += strArgMsg.substring(0, strArgMsg.indexOf(",")) +
                    strNivelBateria + (isPlugado ? "1" : "0") + "[" + strIdentificacao + "]" +
                    "[" + strEndereco + "]";
        }

        strRetorno = ConverteDados(strRetorno);

        return strRetorno;
    }

    private String ConverteDados(String stringDados) {

        String stringTipo = stringDados.substring(0,1);
        int intAsciiSoma = stringTipo.getBytes()[0];
        int intCaracter;
        int intConst = 0;
        String stringNumerica, stringCaracter;
        byte[] byteLetra;
        String stringLetra;
        String stringRetorno = "";

        /*
         * intTotalNumerica é o tamanho dos dados da string com números.
         * Os valores de intTotalNumera estão dividido por 2 para facilitar os cálculos.
         **********************************************************************************/
        int intTotalNumerica = 15+17;

        /*
                  1         2         3         4         5         6         7         8
        0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
         '''':'''':'''':'''':'''':'''':'''':'''':'''':'''':'''':'''':'''':'''':'''':'''':'''':'''':'''':'''':
        C0103528220638271260210151407121003880976103862323600080001691001[ACESSO][T,Sony C2304]
        -'''---------------''''''------''''''''''----------''''---'''---'
        */

        if (stringTipo.equals("C")) intConst =  9;  //CADASTRO e COMANOS DE INFORMAÇÕES
        if (stringTipo.equals("E") || stringTipo.equals("N")) intConst = 15; //SMS: E-Entrada ou S-Saída ou N-Notificações.
        if (stringTipo.equals("F")) intConst = 17; //CHAMADAS: F-Feitas ou R-Recebidas
        if (stringTipo.equals("R")) intConst = 41; //CHAMADAS: F-Feitas ou R-Recebidas
        if (stringTipo.equals("S")) intConst = 43; //SMS: E-Entrada ou S-Saída
        if (stringTipo.equals("T")) intConst = 45; //TRACK

        stringNumerica = stringDados.substring(1,intTotalNumerica*2+1);

        for (int i = 0; i < stringNumerica.length(); i += 2){

            intCaracter = Integer.parseInt(stringNumerica.substring(i, i+2));
            intCaracter = ((intAsciiSoma*2) - intCaracter) - intConst;
            byteLetra = BigInteger.valueOf(intCaracter).toByteArray();
            stringLetra = new String( byteLetra );
            stringRetorno += stringLetra;
        }

        stringNumerica = stringRetorno;

        /*
         * Se houver mais dados a serem encriptados fora os númericos.
         *************************************************************/
        if (stringDados.length() > (intTotalNumerica*2+1)) {

            stringRetorno = "";
            stringCaracter = stringDados.substring(intTotalNumerica*2+1);
            stringCaracter = stringCaracter.trim();

            for (int i = 0; i < stringCaracter.length(); i++){

                intCaracter = stringCaracter.getBytes()[i];
                if ((intCaracter%2) == 0){ intCaracter -= 16;}  //Par
                else { intCaracter -= 30;}  //Ímpar

                byteLetra = BigInteger.valueOf(intCaracter).toByteArray();
                stringLetra = new String( byteLetra );
                stringRetorno += stringLetra;
            }


            stringCaracter = stringRetorno;

            StringBuilder sbDados = new StringBuilder(stringCaracter);
            sbDados.reverse();
            stringCaracter  = sbDados.toString();

            stringRetorno = stringNumerica+stringCaracter;

        }

        if ((stringRetorno.length()%3) == 1) {stringRetorno += "n";}   // "n" após convertido fica "~'
        if ((stringRetorno.length()%3) == 2) {stringRetorno += "nn";}

        /*
         *  1234567890 abcdefghij ABCDEFGHIJ
         *  1aA 2bB 3cC 4dD 5eE 6fF 7gG 8hH 9iI 0jJ
         *  012 345 678 901 234 567 890 123 456 789
         */
        intCaracter = stringRetorno.length()/3;
        stringCaracter = "";
        for (int i=0; i<intCaracter; i++){
            stringCaracter += stringRetorno.substring(i,i+1);
            stringCaracter += stringRetorno.substring(i+intCaracter,i+intCaracter+1);
            stringCaracter += stringRetorno.substring(i+(intCaracter*2),i+(intCaracter*2)+1);
        }

        stringRetorno = stringTipo+stringCaracter.trim();

        return stringRetorno;
    }

    private String getNomeContatoUsandoNumero(String number) {

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, new String[] {BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);

        String name = "";

        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        /* Essa troca será feita já na mensagem que vem do trigger.
        name = " "+name.toLowerCase()+" ";

        name = name.replaceAll(" minha ", " sua ");
        name = name.replaceAll(" meu ", " seu ");*/
        name = trocaAcentos(name);

        name = name.trim();

        return name;
    }

    private String trocaAcentos(String texto) {
        String strRetorno = "";
        int intTamTexto = texto.length();
        int i;

        for (i = 0; i < intTamTexto; i++) {
            if ("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ !@#$%&*()_+-=,.;:".indexOf(texto.charAt(i)) > -1) {
                strRetorno += texto.charAt(i);
            } else {
                strRetorno += "<"+(int)texto.charAt(i)+">";
            }
        }

        return  strRetorno;
    }

    private void generateNotification(Context context, String message, String observacao, String link) {

        int icon = R.mipmap.ic_launcher;
        long when = System.currentTimeMillis();
        message = message.replace("ATENÇÃO! GUFO INFORMA: ", "");
        String appname = context.getResources().getString(R.string.app_name) + " informa!";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification;

        if (link.equals("")) {
            link = "http://" + mPropriedades.pegarStringPropriedades("PI_strURLServidor") + "/dados.php?";
            link += "html="+observacao.substring(observacao.indexOf("£"));
        }

        Intent resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        PendingIntent contentIntent = PendingIntent.getActivity(context, (int) when, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        if (!observacao.equals("")) {
            observacao = "<br>" + observacao.replaceAll("£(.*?)£","");
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentIntent(contentIntent)
                .setSmallIcon(icon)
                .setTicker(appname)
                .setWhen(when)
                .setAutoCancel(true)
                .setContentTitle(appname)
                .setContentText(Html.fromHtml(message + observacao))
                .setSubText(Html.fromHtml("<small><b>La Cerbo</b> - Tecnologia do futuro usada no presente.</small>"))
                .setGroup(appname)
                .setPriority(Notification.PRIORITY_MAX)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(Html.fromHtml(message + observacao)) );
                //.setStyle(new NotificationCompat.BigTextStyle().bigText(String.format(message)) );

        if (booSomCoruja) {
            builder.setSound(null);
        } else {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }

        notification = builder.build();
        assert notificationManager != null;
        notificationManager.notify((int) when, notification);

    }

}
