package com.ab5y.pmpquiz;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.ab5y.pmpquiz.adapter.FolloweesAdapter;
import com.ab5y.pmpquiz.models.Followee;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FollowActivity extends AppCompatActivity {

    private static final String TAG = "FOLLOWACTIVITY";
    private static final int LOGIN_REQUEST = 1;

    OkHttpClient client = new OkHttpClient();
    List<Followee> followeeList = null;
    ListView followeeListView = null;
    String cookie;
    private int countFill;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get Cookie
        SharedPreferences mPreference = PreferenceManager.getDefaultSharedPreferences(this);
        cookie = mPreference.getString("Cookie", null);
        if (cookie == null){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, LOGIN_REQUEST);
        } else {
            client = new OkHttpClient().newBuilder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            final Request original = chain.request();
                            final Request authorized = original.newBuilder()
                                    .addHeader("Cookie", cookie)
                                    .build();
                            return chain.proceed(authorized);
                        }
                    })
                    .build();
            setContentView(R.layout.activity_follow);
            getFollowees();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_REQUEST)
            if (resultCode == RESULT_OK) {
                SharedPreferences mPreference = PreferenceManager.getDefaultSharedPreferences(this);
                cookie = mPreference.getString("Cookie", null);
                client = new OkHttpClient().newBuilder()
                        .addInterceptor(new Interceptor() {
                            @Override
                            public Response intercept(Chain chain) throws IOException {
                                final Request original = chain.request();
                                final Request authorized = original.newBuilder()
                                        .addHeader("Cookie", cookie)
                                        .build();
                                return chain.proceed(authorized);
                            }
                        })
                        .build();
                setContentView(R.layout.activity_follow);
                getFollowees();
            }
    }

    private void getFollowees() {
        final ProgressDialog progressDialog = new ProgressDialog(FollowActivity.this, R.style.AppTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Getting you people to follow...");
        progressDialog.show();
        new serverTask(progressDialog).execute(getResources().getString(R.string.url)+"users?user_type_id="+1);
    }

    private void onGetFolloweesSuccess(){
        Log.e(TAG, "OMG WE GOT SOME FOLLOWEES! "+followeeList.size());
        if(!followeeList.isEmpty()) {
            countFill = 0;
            for (Followee followee : followeeList) {
                new serverTask(followee).execute(getResources().getString(R.string.url)+"num_categories?creator_id="+followee.getUserID());
            }
        }

    }

    private void displayList() {
        if (followeeList != null) {
            followeeListView = (ListView) this.findViewById(R.id.lv_followees);
            if (followeeListView != null){
                followeeListView.setAdapter(
                        new FolloweesAdapter(
                                this,
                                R.layout.followee_row,
                                followeeList
                        )
                );
                followeeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        Log.e(TAG, "Clicked!");
                    }
                });
            }
        }
    }

    public void onFollowed() {
        setResult(RESULT_OK);
        finish();
    }

    public void follow(int followee_id) {
//        final ProgressDialog progressDialog = new ProgressDialog(FollowActivity.this, R.style.AppTheme);
//        progressDialog.setIndeterminate(true);
//        progressDialog.setMessage("Following ...");
//        progressDialog.show();
        JSONObject toPost = new JSONObject();
        try {
            toPost.put("followee_id", followee_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new serverTask((ProgressDialog)null).execute(
                getString(R.string.url)+"followees",
                toPost.toString(),
                getString(R.string.POST)
        );
    }

    public void unfollow(int followee_id) {
        JSONObject toPost = new JSONObject();
        try {
            toPost.put("followee_id", followee_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new serverTask((ProgressDialog)null).execute(
                getString(R.string.url)+"followees",
                toPost.toString(),
                getString(R.string.DELETE)
        );
    }

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    // Async Task
    final class serverTask extends AsyncTask<String, Integer, String> {
        private Exception exception;
        private String contentType;
        private Followee toFill;
        private ProgressDialog progressDialog;

        public serverTask(ProgressDialog progressDialog) {
            this.progressDialog = progressDialog;
        }

        public serverTask(Followee followee){
            this.toFill = followee;
        }

        @Override
        protected String doInBackground(String... params) {
            Request request;
            if(params.length > 1){
                RequestBody body = RequestBody.create(JSON, params[1]); // json
                if(params[2].equals(getString(R.string.POST)))
                    request = new Request.Builder()
                            .url(params[0])
                            .post(body)
                            .build();
                else
                    request = new Request.Builder()
                            .url(params[0])
                            .delete(body)
                            .build();
            } else{
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
                    Log.e(TAG, "OMG THIS IS WHAT THEY SEND US"+ result);
                    String jsonString = result.trim()
                            .substring(result.indexOf("{"), result.lastIndexOf("}") + 1)
                            .replace("\\", "");
                    try {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        if (jsonObject.has("Teacher")) {
                            followeeList = new ArrayList<>();
                            JSONArray jsonArray = jsonObject.getJSONArray("Teacher");
                            for (int i = 0; i < jsonArray.length(); i++){
                                JSONObject followeeJson = jsonArray.getJSONObject(i);
                                followeeList.add(
                                        new Followee(
                                                followeeJson.getString("name"),
                                                followeeJson.getString("full_name"),
                                                followeeJson.getInt("id"),
                                                followeeJson.getInt("type_id")
                                        )
                                );
                            }
                            onGetFolloweesSuccess();
                        } else if(jsonObject.has("num_categories")) {
                            if (toFill != null) {
                                toFill.setNumCategoriesAuthored(jsonObject.getInt("num_categories"));
                                countFill++;
                                Log.e(TAG, "COUNT FILL HAS COUNTED "+countFill);
                                if (countFill == followeeList.size()){
                                    Log.e(TAG, "OMG WE GOT IT ALL NOW!");
                                    displayList();
                                }
                            }
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (result.equals("followed")) {
//                    onFollowed();
                        Toast.makeText(getBaseContext(), "Successfully followed!", Toast.LENGTH_SHORT).show();
                    }else if (result.equals("unfollowed")) {
//                    onFollowed();
                        Toast.makeText(getBaseContext(), "Successfully unfollowed!", Toast.LENGTH_SHORT).show();
                    }
                }
                if (progressDialog != null)
                    progressDialog.dismiss();
            }
        }
    }
}