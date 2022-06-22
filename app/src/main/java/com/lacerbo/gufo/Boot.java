package com.lacerbo.gufo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Created by Vidal on 15/03/2015.
 ***************************************************************/
public class Boot extends BroadcastReceiver {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(final Context context, Intent intent) {

        try {
            Propriedades mPropriedades = new Propriedades(context);
            mPropriedades.salvarPropriedades("PI_lonDataHoraMonitor", Calendar.getInstance().getTimeInMillis());
            Intent intentMonitor = new Intent(context, Monitor.class);
            context.startService(intentMonitor);

            EnviarDados mEnviaDados = new EnviarDados();
            mEnviaDados.getInstance(context, "C", "BOOT", "");
        } catch (Exception e) {
            // Não faça NADA.
        }
    }
}
