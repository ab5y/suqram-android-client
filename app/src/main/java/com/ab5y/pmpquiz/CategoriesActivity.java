package com.ab5y.pmpquiz;

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

import com.ab5y.pmpquiz.adapter.CategoriesAdapter;
import com.ab5y.pmpquiz.models.Category;
import com.ab5y.pmpquiz.models.Followee;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CategoriesActivity extends AppCompatActivity {

    public List<Category> categories = null;
//    public List<Followee> followees = null;
    public List<Integer> followeesIDs = null;

    private static final String TAG = "CATEGORIESACTIVITY";
    public static final String CATEGORYID = "com.ab5y.pmpquiz.CATEGORYID";
    private static final int LOGIN_REQUEST = 1;
    private static final int GET_FOLLOWEES = 2;

    private OkHttpClient client = new OkHttpClient();
    private String cookie;

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
            setContentView(R.layout.activity_categories);
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
                setContentView(R.layout.activity_categories);
                getFollowees();
            }
        else if (requestCode == GET_FOLLOWEES)
            if (resultCode == RESULT_OK) {

            }
    }

    private void getFollowees() {
        Log.e(TAG, "GETTING ME SOME FOLLOWEES NIGGA");
        new serverTask().execute(getString(R.string.url)+"followees");
    }

    private void onGetFolloweesIDsSuccess() {
        if (followeesIDs.isEmpty()) {
            Log.e(TAG, "NO FOLLOWEES NIGGA");
            Intent intent = new Intent(this, FollowActivity.class);
            startActivityForResult(intent, GET_FOLLOWEES);
        }
        else {
            categories = new ArrayList<>();
            for (Integer followee_id : followeesIDs) {
                getCategories(followee_id);
            }
        }
    }

//    private void onGetFolloweesSuccess() {
//        if (followees.isEmpty()) {
//            Log.e(TAG, "NO FOLLOWEES NIGGA");
//            Intent intent = new Intent(this, FollowActivity.class);
//            startActivityForResult(intent, GET_FOLLOWEES);
//        } else {
//            Log.e(TAG, "SOME FOLLOWEES NIGGA "+followees.size());
//            categories = new ArrayList<>();
//            for (Followee followee : followees) {
//                getCategories(followee.getUserID());
//            }
//        }
//    }

    private void getCategories(int creatorID) {
        new serverTask().execute(getResources().getString(R.string.url)+"categories?creator_id="+creatorID);
    }

    public void displayCategories() {
        if (categories != null && categories.size() > 0) {
            ListView listView = (ListView) findViewById(R.id.lv_categories);
            if (listView != null) {
                listView.setAdapter(new CategoriesAdapter(this, R.layout.category_row, categories));
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(view.getContext(), QuestionActivity.class);
                        intent.putExtra(CATEGORYID, categories.get(position).id);
                        startActivity(intent);
                    }
                });
            }
        }
    }

    final class serverTask extends AsyncTask<String, Integer, String> {

        private Exception exception;
        private String contentType;

        @Override
        protected String doInBackground(String... params) {
            Request request = new Request.Builder()
                    .url(params[0])
                    .build();
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
            if (result != null && this.contentType.contains("application/json;")) {
                String jsonString = result.trim()
                        .substring(result.indexOf("{"), result.lastIndexOf("}") + 1)
                        .replace("\\", "");
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    if (jsonObject.has("categories")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("categories");
                        categories.add(new Category(0, "All"));
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonArrObj = jsonArray.getJSONObject(i);
                            categories.add(new Category(jsonArrObj.getInt("id"), jsonArrObj.getString("name")));
                        }
                        displayCategories();
                    } else if (jsonObject.has("followees")) {
                        followeesIDs = new ArrayList<>();
                        JSONArray jsonArray = jsonObject.getJSONArray("followees");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject followeeJson = jsonArray.getJSONObject(i);
                            followeesIDs.add(followeeJson.getInt("followee_id"));
//                            JSONObject followeeJson = jsonArray.getJSONObject(i);
//                            followees.add(
//                                    new Followee(
//                                            followeeJson.getString("name"),
//                                            followeeJson.getString("full_name"),
//                                            followeeJson.getInt("id"),
//                                            followeeJson.getInt("type_id")
//                                    )
//                            );
                        }
                        onGetFolloweesIDsSuccess();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
