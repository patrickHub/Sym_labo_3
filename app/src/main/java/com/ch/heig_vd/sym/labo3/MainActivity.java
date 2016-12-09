package com.ch.heig_vd.sym.labo3;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.sym.kobel.lh5.R;

public class MainActivity extends Activity {

    public static final String MIME_TEXT_PLAIN = "text/plain";

    private static final String APP_TAG = "labo3";

    private static int currentAccredLevel;

    private TextView textViewInfo;
    private EditText email;
    private EditText password;
    private TextView result;
    private TextView verySecretView;
    private TextView secretView;
    private TextView publicView;
    private Button loginButton;
    private Button scanQRButton;
    private TextView qrResult;

    private NfcAdapter nfcAdapter;
    private boolean hasNFC;
    private NFCExecutor nfcExecutor;

    static final int MAX_ACCRED_LEVEL = 10;
    static final int MED_ACCRED_LEVEL = 5;
    static final int MIN_ACCRED_LEVEL = 1;

    private AuthenticationWorker authenticationWorker;
    private String[] nfcInfos;
    private static String TAG = MainActivity.class.getName();

    /* Save instance State */
    private final String STATE_VIS_SSECRET = "stateVisSSecret";
    private final String STATE_VIS_SECRET = "stateVisSecret";
    private final String STATE_QR_TYPE = "stateQRType";
    private final String STATE_QR_CONTENT = "stateQRContent";

    /* Stuff for QR */
    private String qrContent;
    private String qrType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewInfo = (TextView) findViewById(R.id.textViewInstructions);
        result = (TextView) findViewById(R.id.textViewResult);
        email = (EditText) findViewById(R.id.editMail);
        password = (EditText) findViewById(R.id.editPWD);
        loginButton = (Button) findViewById(R.id.loginButton);
        scanQRButton = (Button) findViewById(R.id.scanQRButton);
        verySecretView = (TextView) findViewById(R.id.very_secret_view);
        secretView = (TextView) findViewById(R.id.secret_view);
        publicView = (TextView) findViewById(R.id.public_view);
        authenticationWorker = new AuthenticationWorker();
        nfcExecutor = new NFCExecutor();
        qrResult = (TextView) findViewById(R.id.qrResult);
        currentAccredLevel = 0;


        authenticationWorker.addListener(new AuthenticationListener() {
            @Override
            public void handleAuthentification(Integer level) {
                Log.d(TAG, "handleAuthentification " + level);

                if (level == MAX_ACCRED_LEVEL) {
                    verySecretView.setVisibility(View.VISIBLE);
                } else {
                    verySecretView.setVisibility(View.INVISIBLE);
                }
                if (level >= MED_ACCRED_LEVEL) {
                    secretView.setVisibility(View.VISIBLE);
                } else {
                    secretView.setVisibility(View.INVISIBLE);
                }
            }
        });

        nfcExecutor.addListener(new NFCListener() {
            @Override
            public boolean handleNFC(String[] strings) {
                if (email.getText().length() == 0 || password.getText().length() == 0){
                    Toast.makeText(getApplicationContext(), "Please enter your login and password", Toast.LENGTH_LONG);
                    return false;
                }
                authenticationWorker.authenticateViaNFC(email.getText().toString(),password.getText().toString(),strings[0],strings[1]);
                return true;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                authenticationWorker.authenticateViaPassword(email.getText().toString(), password.getText().toString());
            }
        });

        scanQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkQRCode();
            }
        });
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        /* Check if Nfc is available */
        if(nfcAdapter == null){
            Toast.makeText(this,"NFC is not available\n", Toast.LENGTH_LONG).show();
            result.setText("NFC not available, you can log into max level.");
            hasNFC = false;
        } else {
            hasNFC = true;
        }
        if(hasNFC){
            if(!nfcAdapter.isEnabled()){
                result.setText("NFC is disabled");
            } else {
                result.setText("NFC is now available");
            }
        }

        handleIntent(getIntent());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //This will add items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(reqCode, resCode, intent);
        if (scanResult != null) {
            Log.d(TAG, "onActivityResult scanned!");
            qrType = scanResult.getFormatName();
            qrContent = scanResult.getContents();
            qrResult.setText("Type of code : " + qrType + "\nContents : " + qrContent);
            result.setText(scanResult.getContents());
            Toast.makeText(this,"Code is scanned",Toast.LENGTH_LONG);
        }
        else {
            // Could not read the Code
            Log.d(TAG, "onActivityResult Scanned Failled");
            Toast.makeText(this,"Could not Scanned",Toast.LENGTH_LONG);
        }
    }

    public void checkQRCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(nfcAdapter != null){
            setupForegroundDispatch(this, nfcAdapter);
        }
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putInt(STATE_VIS_SSECRET, verySecretView.getVisibility());
        savedInstanceState.putInt(STATE_VIS_SECRET, secretView.getVisibility());
        savedInstanceState.putString(STATE_QR_CONTENT, qrContent);
        savedInstanceState.putString(STATE_QR_TYPE, qrType);


        super.onSaveInstanceState(savedInstanceState);

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        qrContent = savedInstanceState.getString(STATE_QR_CONTENT);
        qrType = savedInstanceState.getString(STATE_QR_TYPE);

        if(qrContent != null && qrType != null){
            qrResult.setText("Type of code : " + qrType + "\nContents : " + qrContent);
        }

    }

    @Override
    protected void onPause() {

        if(nfcAdapter != null) {
            stopForegroundDispatch(this, nfcAdapter);
        }

        super.onPause();
    }


    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);

        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }


        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    private void handleIntent(Intent intent){
        // TODO: handle Intent
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                nfcExecutor.handleTag(tag);
            } else {
                Log.d(TAG, "handleIntent false MIME");
            }
        }
    }

    protected void onNewIntent(Intent intent){
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */

        handleIntent(intent);
    }


}
