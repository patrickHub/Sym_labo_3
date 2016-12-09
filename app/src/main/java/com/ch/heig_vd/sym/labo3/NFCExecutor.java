package com.ch.heig_vd.sym.labo3;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.util.Log;

import com.ch.heig_vd.sym.labo3.NFCListener;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class NFCExecutor {
    private final String TAG = this.getClass().getName();
    private NFCListener nfcListener;

    public void addListener(NFCListener nfcListener){
        this.nfcListener = nfcListener;
    }

    public void handleTag(Tag t){
        new NFCHandler().execute(t);
    }

    private class NFCHandler extends AsyncTask <Tag, Void, String[]>{

        @Override
        protected String[] doInBackground(Tag... params) {
           // Log.d(TAG, "doInBackground ");
            Tag tag = params[0];
            Ndef ndef = Ndef.get(tag);
            if(ndef == null) {
                return null;
            }
            NdefMessage ndefMessage = ndef.getCachedNdefMessage();
            NdefRecord[] ndefRecords = ndefMessage.getRecords();
            String[] infos = new String[ndefRecords.length];
            NdefRecord ndefRecord = null;
            for (int i = 0; i < ndefRecords.length; i++) {
                 ndefRecord = ndefRecords[i];
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        infos[i] = readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding Exception", e);
                    }
                }
            }

                for (String s : infos) {
                    Log.d(TAG, "doInBackground " + s);
                }
                return infos;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            nfcListener.handleNFC(strings);
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         *  source : http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            //Get the text encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            //Get the language code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }
    }

}
