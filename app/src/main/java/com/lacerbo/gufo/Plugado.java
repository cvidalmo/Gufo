package com.lacerbo.gufo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Vidal on 15/03/2015.
 **************************************/
public class Plugado extends BroadcastReceiver {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(final Context context, Intent intent) {
        try {
            EnviarDados mEnviaDados = new EnviarDados();
            mEnviaDados.getInstance(context, "C", "Plugado", "");
        } catch (Exception e) {
            // Não faça NADA.
        }
    }

}