package com.ch.heig_vd.sym.labo3;

import android.os.AsyncTask;
import android.util.Log;


public class AuthenticationWorker {


    //les credientials
    private final String LOGIN = "john.doe";
    private final String PASSWORD = "passwordGiven";
    private final String NFC_LOGIN = "john";
    private final String NFC_PASSWORD = "passwordEntered";

    private String login, password;
    private String nfcLogin, nfcPassword;

    private AuthenticationListener listener;

    private int currentAccred;



    private static final String TAG = AuthenticationWorker.class.getName();

    public void addListener(AuthenticationListener listener){

        this.listener = listener;
    }

    public boolean authenticateViaPassword(String login, String password){
        new Authenticator().execute(login, password);
        return true;
    }

    public boolean authenticateViaNFC(String login, String password, String nfcLogin, String nfcPassword){
        new Authenticator().execute(login,password,nfcLogin,nfcPassword);
        return true;
    }


    private class Authenticator extends AsyncTask<String, Integer, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            Log.d(TAG, "doInBackground ");
            if(params.length == 2){
                login = params[0];
                password = params[1];
            }

            if(params.length == 4){
                login = params[0];
                password = params[1];
                nfcLogin = params[2];
                nfcPassword = params[3];
            }

            if( login.equals(LOGIN) && password.equals(PASSWORD)){
                currentAccred = MainActivity.MED_ACCRED_LEVEL;
                if(params.length == 4 && nfcLogin.equals(NFC_LOGIN) && nfcPassword.equals(NFC_PASSWORD)){
                    currentAccred = MainActivity.MAX_ACCRED_LEVEL;
                }
            }



            while (currentAccred >= MainActivity.MIN_ACCRED_LEVEL){
                try {
                    publishProgress(currentAccred);
                    Thread.sleep(2000,0);
                    currentAccred --;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return MainActivity.MIN_ACCRED_LEVEL;
        }

        protected void onProgressUpdate(Integer... progress){
            Log.d(TAG, "onProgressUpdate " + progress[0]);
            listener.handleAuthentification(progress[0]);

        }

        protected void onPostExecute(Integer i){
            Log.d(TAG, "onPostExecute " + i);
            listener.handleAuthentification(i);
        }
    }

}
