package com.lacerbo.gufo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

/**
 * Created by Vidal on 03/10/2015.
 **************************************************/
public class ChamadaRecebida extends BroadcastReceiver {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                EnviarDados mEnviaDados = new EnviarDados();
                mEnviaDados.getInstance(context, "R", number, "");
            }
        } catch (Exception e) {
            // Não faça NADA.
        }
    }

}
