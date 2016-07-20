package com.ab5y.pmpquiz;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String LOG = "LOGINACTIVITY";
    private static final int REQUEST_SIGNUP = 0;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client = new OkHttpClient();

    EditText _usernameText;
    EditText _passwordText;
    Button _loginButton;
    TextView _signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this._usernameText = (EditText) findViewById(R.id.input_username);
        this._passwordText = (EditText) findViewById(R.id.input_password);
        this._loginButton = (Button) findViewById(R.id.btn_login);
        this._signupLink = (TextView) findViewById(R.id.link_signup);
        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

//        _signup link stuff
        _signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the signup activity
                Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    public void login() {
        Log.d(LOG, "Login");

        if (!validate()){
            onLoginFailed();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(
                LoginActivity.this,
                R.style.AppTheme
        );
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        _loginButton.setEnabled(false);

        String username = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();

        JSONObject loginDetails = new JSONObject();
        try {
            loginDetails.put("login", username);
            loginDetails.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new postToServer(progressDialog).execute(getString(R.string.url)+"login", loginDetails.toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                Log.e(LOG, "Hey, I made it to Login!");
                // Log the user in automatically on successful registration
                String username = data.getStringExtra("username");
                String password = data.getStringExtra("password");
                JSONObject loginDetails = new JSONObject();
                try {
                    loginDetails.put("login", username);
                    loginDetails.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ProgressDialog progressDialog = new ProgressDialog(
                        LoginActivity.this,
                        R.style.AppTheme
                );
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Authenticating...");
                progressDialog.show();

                new postToServer(progressDialog).execute(getString(R.string.url)+"login", loginDetails.toString());
            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess(String cookie) {
        Toast.makeText(LoginActivity.this, "Authenticated successfully!", Toast.LENGTH_SHORT).show();
        _loginButton.setEnabled(true);
        SharedPreferences mPreference = PreferenceManager.getDefaultSharedPreferences(this);
        mPreference.edit().putBoolean("loggedin", true).apply();
        mPreference.edit().putString("Cookie", cookie).apply();
        this.setResult(RESULT_OK);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed :(", Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
    }

    public boolean validate(){
        boolean valid = true;

        String username = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();

        if (username.isEmpty() || username.length() < 3) {
            _usernameText.setError("enter a valid username");
            valid = false;
        } else _usernameText.setError(null);

        if (password.isEmpty() || password.length() < 5) {
            _passwordText.setError("password needs to be at least 5 characters long");
            valid = false;
        } else _passwordText.setError(null);

        return valid;
    }

    final class postToServer extends AsyncTask<String, Integer, String> {

        private Exception exception;
        String header;
        private ProgressDialog progressDialog;

        public postToServer(ProgressDialog progressDialog){
            this.progressDialog = progressDialog;
        }

        @Override
        protected String doInBackground(String... params) {
            try{
                RequestBody body = RequestBody.create(JSON, params[1]); // json
                Request request = new Request.Builder()
                        .url(params[0]) //url
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                header = response.header("set-cookie");
                Log.e(LOG, "Cookie looks like: "+header);
                return response.body().string();
            } catch (Exception e) {
                this.exception = e;
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result){
            if (progressDialog != null)
                progressDialog.dismiss();
            if (result != null) {
                if (result.contains("success")) {
                    onLoginSuccess(header);
                } else {
                    onLoginFailed();
                }
            }
            else Toast.makeText(LoginActivity.this, "Oops! Something went wrong :(", Toast.LENGTH_SHORT).show();
        }
    }
}
