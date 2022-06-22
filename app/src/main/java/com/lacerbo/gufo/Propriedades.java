package com.lacerbo.gufo;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

/**
 * Created by Vidal on 15/03/2015.
 **************************************************************************************/
class Propriedades {

    private final SharedPreferences preferences;

    public Propriedades(Context context) {
        this.preferences = context.getSharedPreferences("com.lacerbo.gufo", Context.MODE_PRIVATE);
    }

    void salvarPropriedades(String chave, Object valor){

        //deixar como "commit" grava imediatamente enquanto "apply" grava em backgound.
        if (valor.getClass().getSimpleName().toLowerCase().equals("boolean")) {
            this.preferences.edit().putBoolean(chave, (Boolean) valor).apply();
        } else if (valor.getClass().getSimpleName().toLowerCase().equals("integer")) {
            this.preferences.edit().putInt(chave, (Integer) valor).apply();
        } else if (valor.getClass().getSimpleName().toLowerCase().equals("float")) {
            this.preferences.edit().putFloat(chave, (Float) valor).apply();
        } else if (valor.getClass().getSimpleName().toLowerCase().equals("long")) {
            this.preferences.edit().putLong(chave, (Long) valor).apply();
        } else {
            this.preferences.edit().putString(chave, (String) valor).apply();
        }

    }

    String pegarStringPropriedades(String chave){

        String retornoPadrao = "";

        switch (chave) {
            case "PI_strURLServidor":
                retornoPadrao = "celular.lacerbo.com";  //URL do servidor.
            break;
            //case "PI_strIPServidor":
            //    retornoPadrao = "167.114.109.13";  //IP do servidor.
            //break;
            case "PI_strVersaoApp":
                retornoPadrao = "103";  //Versão do aplicativo.
            break;
            case "PI_strAtivarApp":
                retornoPadrao = "*007";  //Código de acesso padrão após primeira instalação.
            break;
        }

        return this.preferences.getString(chave, retornoPadrao);
    }

    Integer pegarIntegerPropriedades(String chave){
        Integer retornoPadrao = 0;

        switch (chave) {
            case "PI_intPortaConexao":
                retornoPadrao = 16141;  //Porta de conexão do servidor.
                break;
            case "PI_intTempoTransmissao":
                retornoPadrao = 30;  //Tempo de transmissão/recepção em segundos que o serviço Monitor se conecta com o servidor.
                break;
            case "PI_intSensibAjuda":
                retornoPadrao = 2;   //Quantidades de segundos que deve ser balançado o celular para disparar o pedido de ajuda.
                break;
            case "PI_intAtivarMonitor":
                retornoPadrao = 300;  //Quantidade de segundos que é considera como inativo o serviço Monitor se não atualizar a PI_lonDataHoraMonitor.
                break;
        }

        return this.preferences.getInt(chave, retornoPadrao);

    }

    Boolean pegarBooleanPropriedades(String chave){
        Boolean retornoPadrao = false;

        switch (chave) {
            case "PI_booSomCoruja":
                retornoPadrao = true;  //Usado para ativar/desativar o canto da coruja nas mensagens ou aceso ao aplicatvo.
                break;
            case "PI_booVibrar":
                retornoPadrao = true;  //Usado para ativar/desativar a vobração nas mensagens.
                break;
            case "PI_booFalar":
                retornoPadrao = false;  //Usado para ativar/desativar a fala das mensagens. É ignorado nas mensagens de AJUDA ou PROPAGANDA.
                break;
            case "PI_booCadastroSite":
                retornoPadrao = true;  //Usado para identifica se é o primeiro acesso depois de uma instalação ou reinstalação do gufo.
                break;
        }

        return this.preferences.getBoolean(chave, retornoPadrao);
    }

    Float pegarFloatPropriedades(String chave){
        Float retornoPadrao = 0.0f;
        switch (chave) {
            case "PI_floDistancia":
                retornoPadrao = 30.0f;  //Menor distância em metros entre localizações.
                break;
            case "PI_floSensibFinaAjuda":
                retornoPadrao = 4.0f;  //Sensibilidade dos movimentos para ativar o pedido de Ajuda (diferença entre os Deltas Z do acelerômetro, atual e anterior).
                break;
            case "PI_floCabecaPraBaixo":
                retornoPadrao = -8.0f;  //Valor negativo é cabeça pra baixo. (-5,00 = 45º graus, -10,00 = 90° graus.)
                break;
            case "PI_floMenorDistSensorAprox":
                retornoPadrao = 100.0f;  //Menor distância do sensor de aproximação.)
                break;
        }
        return this.preferences.getFloat(chave, retornoPadrao);
    }

    Long pegarLongPropriedades(String chave){
        Long retornoPadrao = 0L;
        switch (chave) {
            case "PI_lonDataHoraMonitor":
                retornoPadrao = 0L;  //Valor em milisegundos da última data e hora do serviço Monitor.
                break;
            case "PI_lonDataHoraMonitor2":
                retornoPadrao = 2L;  //Menor distância do sensor de aproximação.)
                break;
        }
        return this.preferences.getLong(chave, retornoPadrao);
    }

    Boolean monitorOffBooleanProprieades() {
        Boolean retorno = true;
        Long lonDataHoraMonitor = pegarLongPropriedades("PI_lonDataHoraMonitor");

        //Se o serviço Monitor está ativado a menos de 300 segundos (PI_intAtivarMonitor).
        if ( ( (Calendar.getInstance().getTimeInMillis()/1000) - (lonDataHoraMonitor/1000) < pegarIntegerPropriedades("PI_intAtivarMonitor"))) {
            retorno = false;
        }

        return retorno;
    }
}

