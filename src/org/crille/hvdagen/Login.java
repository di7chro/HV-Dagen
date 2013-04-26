package org.crille.hvdagen;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Login extends Activity {
    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String loginURL = "http://mittkonto.hv.se/public/appfeed/login_response.php?app_key=";

    // Den här raden används för skolan RSS-ström
    //private static final String loginXML_URL = "https://mittkonto.hv.se/public/appfeed/app_rss.php?app_key=";
    private static final String loginXML_URL = "http://public.crille.org/parse.php?key=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        final EditText username = (EditText) findViewById(R.id.login_et_username);
        final EditText password = (EditText) findViewById(R.id.login_et_password);

        Button loginButton = (Button) findViewById(R.id.login_btn_login);

        loginButton.setOnClickListener(new View.OnClickListener() {

			/*
             * The button has been clicked and someone wants to login Get the
			 * information from the textboxes and create a String with theese
			 * two, whicjh is sent to the Hash-maker
			 */

            public void onClick(View v) {

                String uname = username.getText().toString();
                String pword = password.getText().toString();

                // Concaternate the username with the password
                String userAndPass = uname + pword;

                String hash;
                try {
                    // Select the appropriate Hash-function
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    digest.update(userAndPass.getBytes());

                    // Get the hash-value for the user
                    hash = bytesToHexString(digest.digest());

                    LoginData loginUser = new LoginData();

                    // Appends the hash to the URL
                    String loginString = loginURL + hash;

                    String[] urlHash = {loginString};

                    // call thread tråden and execute doinBackground in
                    // Logindata;
                    loginUser.execute(urlHash);

                    // Get "answer" from LoginData doInBackground with get()
                    ArrayList<String> arrayList = loginUser.get();

                    // Checks the string returned from loginServer. Displays
                    // info to user
                    if (arrayList.get(0).contains("Ingen anv")) {
                        showWrongLoginPopup();
                    } else {
                        // loginResult.setText("Välkommen "+arrayList.get(0));

                        // Appends the hash to the URL
                        String loginDataStr = loginXML_URL + hash;

                        // Save the loginString to preferences
                        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("LOGINSTRING", loginDataStr);
                        editor.commit();

                        // Start the MinDag activity
                        Intent goToMinDag = new Intent(getApplicationContext(), MinDag.class);
                        startActivity(goToMinDag);
                    }

                } catch (NoSuchAlgorithmException e1) {
                    e1.printStackTrace();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    /**
     * Creates a hash of the input String
     *
     * @param bytes of username+password
     * @return The hash-value
     */
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * Displays a popup-windows sayin that the credentials were wrong
     */
    private void showWrongLoginPopup() {
        AlertDialog.Builder myBuild = new AlertDialog.Builder(this);
        myBuild.setTitle(R.string.login_wrong_title);
        myBuild.setMessage(R.string.login_wrong_text);
        myBuild.setNeutralButton("OK", null);
        myBuild.show();
    }

    public class LoginData extends AsyncTask<String, Void, ArrayList<String>> {
        private ProgressDialog progressDialog;

        protected void onPreExecute() {
            Log.i("Logindata", "1");
            progressDialog = ProgressDialog.show(Login.this, "",
                    "Kollar dina uppgifter", true);
        }

        @Override
        protected void onPostExecute(ArrayList<String> s) {
            Log.i("Logindata", "3");
            progressDialog.dismiss();
        }

        @Override
        protected ArrayList<String> doInBackground(String... urlHash) {
            String answer = null;
            try {
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(urlHash[0]);
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                answer = EntityUtils.toString(httpEntity);

            } catch (Exception e) {
                e.printStackTrace();
            }

            ArrayList<String> loginList = new ArrayList<String>();
            loginList.add(answer);
            return loginList;
        }
    }
}