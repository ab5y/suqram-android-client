package com.ab5y.pmpquiz;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ab5y.pmpquiz.custom.ui.ClickToSelectEditText;
import com.ab5y.pmpquiz.models.UserType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG = "REGISTRATIONACTIVITY";
    private String URL;

    EditText _fullnameText;
    EditText _usernameText;
    ClickToSelectEditText _usertypeText;
    EditText _passwordText;
    EditText _confirmpasswordText;
    Button _signupButton;
    TextView _loginLink;

    UserType selectedUserType;

    OkHttpClient client = new OkHttpClient();

    List<UserType> userTypeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        this._fullnameText = (EditText) findViewById(R.id.input_registration_fullname);
        this._usernameText = (EditText) findViewById(R.id.input_registration_username);
        this._usertypeText = (ClickToSelectEditText) findViewById(R.id.dropdown_usertype);
        this._passwordText = (EditText) findViewById(R.id.input_registration_password);
        this._confirmpasswordText = (EditText) findViewById(R.id.input_confirm_password);
        this._signupButton = (Button) findViewById(R.id.btn_signup);
        this._loginLink = (TextView) findViewById(R.id.link_login);

        URL = getResources().getString(R.string.url);

        Log.e(TAG, "Comes here");
        getUserTypes();

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration and return to the login activity
                finish();
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");
        if (!validate()) {
            onSignupFailed(null);
            return;
        }

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(RegistrationActivity.this, R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Making you cool...");
        progressDialog.show();

        String fullname = _fullnameText.getText().toString();
        String username = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();
        String confirmpassword = _confirmpasswordText.getText().toString();

        // implement registration task
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("login", username);
            jsonObject.put("password", password);
            jsonObject.put("confirmpassword", confirmpassword);
            jsonObject.put("typeid", selectedUserType.id);
            jsonObject.put("fullname", fullname);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new serverTask(progressDialog).execute(URL+"register", jsonObject.toString());

    }

    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        Intent intent = getIntent();
        intent.putExtra("username", _usernameText.getText().toString());
        intent.putExtra("password", _passwordText.getText().toString());
        setResult(RESULT_OK, intent);
        Log.e(TAG, "Success. Going back to login, bye!");
        finish();
    }

    public void onSignupFailed(String reason) {
        if (reason != null)
            Toast.makeText(getBaseContext(), "Registration failed! "+reason, Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String username = _usernameText.getText().toString();
        String password = _passwordText.getText().toString();
        String confirmpassword = _confirmpasswordText.getText().toString();
        String usertype = _usertypeText.getText().toString();

        if (username.isEmpty() || username.length() < 3) {
            _usernameText.setError("at least 3 characters long");
            valid = false;
        } else _usernameText.setError(null);

        if (usertype.isEmpty() || selectedUserType == null) {
            _usertypeText.setError("select a User Type");
            valid = false;
        } else _usertypeText.setError(null);

        if (password.isEmpty() || password.length() < 5) {
            _passwordText.setError("at least 5 characters");
            valid = false;
        } else _passwordText.setError(null);

        if (!confirmpassword.contentEquals(password)) {
            _confirmpasswordText.setError("doesn't match Password");
            valid = false;
        } else _confirmpasswordText.setError(null);

        return valid;
    }

    public void getUserTypes() {
        Log.e(TAG, "In here, too, boss");
        final ProgressDialog progressDialog = new ProgressDialog(RegistrationActivity.this, R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Getting the cool ingredients...");
        progressDialog.show();

        new serverTask(progressDialog).execute(URL+"register");

    }

    public void initliatizeUserTypeDropDown() {
        Log.e(TAG, "Initializer. Initializes");
        if (userTypeList != null){
            _usertypeText.setItems(userTypeList);
            _usertypeText.setOnItemSelectedListener(new ClickToSelectEditText.OnItemSelectedListener<UserType>() {
                @Override
                public void onItemSelectedListener(UserType item, int selectedIndex) {
                    selectedUserType = item;
                }
            });
        }
    }

    //    Async Task

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    final class serverTask extends AsyncTask<String, Integer, String> {

        private String contentType;
        private ProgressDialog progressDialog;

        public serverTask(ProgressDialog progressDialog) {
            this.progressDialog = progressDialog;
        }

        @Override
        protected String doInBackground(String... params) {
            Request request;
            if (params.length > 1) {
                RequestBody body = RequestBody.create(JSON, params[1]);
                request = new Request.Builder()
                        .url(params[0])
                        .post(body)
                        .build();
            } else {
                request = new Request.Builder()
                        .url(params[0])
                        .build();
            }
            try {
                Response response = client.newCall(request).execute();
                this.contentType = response.header("Content-Type");
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                if (this.contentType.contains("application/json;")) {
                    String jsonString = result.trim()
                            .substring(result.indexOf("{"), result.lastIndexOf("}") + 1)
                            .replace("\\", "");
                    try {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        if (jsonObject.has("user_types")) {
                            userTypeList = new ArrayList<>();
                            JSONArray jsonArray = jsonObject.getJSONArray("user_types");
                            for (int i = 0; i < jsonArray.length(); i++){
                                JSONObject usertypeJson = jsonArray.getJSONObject(i);
                                userTypeList.add(
                                        new UserType(
                                                usertypeJson.getString("name"),
                                                usertypeJson.getInt("id")
                                        )
                                );
                            }
                            Log.e(TAG, "Check this");
                            initliatizeUserTypeDropDown();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else if (result.equals("registered")) {
                    Log.e(TAG, "Comes here because result is "+result);
                    onSignupSuccess();
                } else {
                    onSignupFailed(result);
                }
            }
            if (progressDialog != null)
                progressDialog.dismiss();
        }
    }
}
