package com.ab5y.pmpquiz;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ab5y.pmpquiz.adapter.OptionsAdapter;
import com.ab5y.pmpquiz.listener.OnOptionsListViewItemClickListener;
import com.ab5y.pmpquiz.listener.OnShakeEventListener;
import com.ab5y.pmpquiz.listener.OnSwipeTouchListener;
import com.ab5y.pmpquiz.models.Option;
import com.ab5y.pmpquiz.models.Question;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.framed.Header;

public class QuestionActivity extends AppCompatActivity {

    private static final String LOG = "QUESTIONACTIVITY";
    private String URL;
    private static final int LOGIN_REQUEST = 1;
    private static final String startTime = GregorianCalendar.getInstance().getTime().toString();
    private OkHttpClient client = new OkHttpClient();

    // The following are used for tracking questions, options, and answers
    public Map<Integer, List<Option>> optionsMap = new HashMap<>();
    public List<Question> questions;
    public Question currQuesion;
    public Map<Integer, Integer> answers = new HashMap<>();

    // The following are used for populating and using ListView
    public OnOptionsListViewItemClickListener onListViewItemClickListener = new OnOptionsListViewItemClickListener(this);
    private OptionsAdapter adapter;

    // The following are used for shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private OnShakeEventListener onShakeEventListener;

    // Progress bar
    private ProgressBar progressBar;

    // Category id
    private int categoryID;

    // Cookie
    private String cookie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        URL = getResources().getString(R.string.url);
        // Get Cookie
        SharedPreferences mPreference = PreferenceManager.getDefaultSharedPreferences(this);
        cookie = mPreference.getString("Cookie", null);
        if (cookie == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, LOGIN_REQUEST);
        } else setup();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_REQUEST)
            if (resultCode == RESULT_OK)
                setup();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register the Session Manager listener onResume
        if (mSensorManager != null)
            mSensorManager.registerListener(onShakeEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        // Unregister the Sensor Manager onPause
        if (mSensorManager != null)
            mSensorManager.unregisterListener(onShakeEventListener);
        super.onPause();
    }

    private void setup() {
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

        setContentView(R.layout.activity_question);

        // Initialize progressBar
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // Swipe functionality
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        assert relativeLayout != null;
        relativeLayout.setOnTouchListener(new OnSwipeTouchListener(this){
            public void onSwipeRight() {
                prevQuestion();
            }
            public void onSwipeLeft() {
                nextQuestion();
            }
        });

        // Shake functionality
        //initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        onShakeEventListener = new OnShakeEventListener();
        onShakeEventListener.setOnShakeListener(new OnShakeEventListener.OnShakeListener() {
            @Override
            public void onShake(int count) {
                onShakeHandler();
            }
        });

        Intent intent = getIntent();
        if (intent != null) {
            categoryID = intent.getIntExtra(CategoriesActivity.CATEGORYID, 0);
            getQuestions(categoryID);
        }
    }

    public void updateAnswers(){
        if((currQuesion != null)&&(onListViewItemClickListener.id >= 0)){
            answers.put(currQuesion.id, onListViewItemClickListener.id);
        }
    }

    private void nextQuestion() {
        int index = questions.indexOf(currQuesion);
        if (index < questions.size() - 1 && optionsMap.containsKey(questions.get(index + 1).id)) {
            currQuesion = questions.get(index + 1);
            displayQuestion();
            updateOptions();
        }
    }

    private void prevQuestion() {
        int index = questions.indexOf(currQuesion);
        if (index > 0 && optionsMap.containsKey(questions.get(index - 1).id)) {
            currQuesion = questions.get(index - 1);
            displayQuestion();
            updateOptions();
        }
    }

    private void displayQuestion() {
        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setText(currQuesion.text);
    }

    private void updateOptions() {
        if(optionsMap.containsKey(currQuesion.id)) {
            ListView listView = (ListView) findViewById(R.id.listView);
            if (adapter != null) {
                listView.setAdapter(null);
            }
            adapter = new OptionsAdapter(this, R.layout.option_row, optionsMap.get(currQuesion.id));
            adapter.notifyDataSetChanged();
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(onListViewItemClickListener);
        }
    }

    public void onShakeHandler(){
        new AlertDialog.Builder(this)
                .setTitle("Submit Answers")
                .setMessage("Are you sure you want to submit your answers?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            submit();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(QuestionActivity.this, "Sending...", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    public void submit() throws JSONException {
        Gson gson = new Gson();
        String answersJson = gson.toJson(answers);
        JSONObject toSend = new JSONObject();
        int userid = 1;
        toSend.put("userid", userid);
        toSend.put("categoryid", this.categoryID);
        toSend.put("starttime", this.startTime);
        toSend.put("questionid", this.currQuesion.id);
        toSend.put("answers", new JSONObject(answersJson));
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        new postToServer(progressBar, 0).execute(URL+"answers", toSend.toString());
    }

    private void getQuestions(int category_id){
        if (category_id > 0){
            new getFromServer(progressBar, 1)
                    .execute(
                            URL + "questions?category_id="+category_id
                    );
        } else {
            new getFromServer(progressBar, 1)
                    .execute(
                            URL + "questions"
                    );
        }
    }

    private void getOptions(int questionId, int numQuestions) {
        optionsMap = null;
        getFromServer getOs = new getFromServer(progressBar, numQuestions);
        getOs.execute(URL + "options?question_id="+questionId);
    }

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    final class postToServer extends AsyncTask<String, Integer, String> {

        private Exception exception;
        private final ProgressBar progressBar;
        private int numQuestions;

        public postToServer(ProgressBar progressBar, int numQuestions){
            this.progressBar = progressBar;
            this.numQuestions = numQuestions;
        }

        @Override
        protected void onPreExecute(){
            if (numQuestions > 0)
                progressBar.setMax(10*numQuestions);
        }

        @Override
        protected String doInBackground(String... params) {
            try{
                RequestBody body = RequestBody.create(JSON, params[1]); // json
                Request request = new Request.Builder()
                        .url(params[0]) //url
                        .post(body)
                        .addHeader("cookies", cookie)
                        .build();
                Response response = client.newCall(request).execute();
                publishProgress();
                return  response.body().string();
            } catch (Exception e) {
                this.exception = e;
                e.printStackTrace();
                return null;
            }
        }

        protected void onProgressUpdate(Integer... progress) {
            progressBar.incrementProgressBy(10);
        }

        @Override
        protected void onPostExecute(String result){
            if (result != null) Toast.makeText(QuestionActivity.this, "Sent successfully!", Toast.LENGTH_SHORT).show();
            else Toast.makeText(QuestionActivity.this, "Oops! Something went wrong :(", Toast.LENGTH_SHORT).show();
        }
    }

    final class getFromServer extends AsyncTask<String, Integer, String> {

        private Exception exception;
        private ProgressBar progressBar;
        private int numCalls;

        private String contentType;

        public getFromServer(ProgressBar progressBar, int numCalls) {
            this.progressBar = progressBar;
            this.numCalls = numCalls;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setMax(numCalls * 10);
        }

        @Override
        protected String doInBackground(String... params) {
            try{
                Request request = new Request.Builder()
                        .url(params[0])
                        .addHeader("cookies", cookie)
                        .build();

                Response response = client.newCall(request).execute();
                this.contentType = response.header("Content-Type");
                publishProgress();
                return response.body().string();
            } catch (IOException e) {
                this.exception = e;
                e.printStackTrace();
                return null;
            }
        }

        protected void onProgressUpdate(Integer... progress) {
            progressBar.incrementProgressBy(10);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null && this.contentType.contains("application/json;")) {
                String jsonString = result.trim()
                        .substring(result.indexOf("{"), result.lastIndexOf("}") + 1)
                        .replace("\\", "");
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    if (jsonObject.has("options")) {
                        initializeOptionContainers(jsonObject);
                    } else if (jsonObject.has("questions")) {
                        initializeQuestionList(jsonObject.getString("questions"));
                        initializeQuiz();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(progressBar.getVisibility() == View.VISIBLE)
                    if(progressBar.getProgress() == progressBar.getMax()) {
                        progressBar.setVisibility(View.INVISIBLE);
                        progressBar.setProgress(0);
                    }
            }
        }
    }

//     ThemedSpinnerAdapter.Helper methods for AsyncTasks to initialize view state

    public void initializeOptionContainers(JSONObject jsonObject) {
        JSONArray arr = null;
        try {
            arr = jsonObject.getJSONArray("options");
            List<Option> optionsList = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                optionsList.add(
                        new Option(
                                obj.getInt("id"),
                                obj.getString("option"),
                                obj.getBoolean("isCorrectAnswer"),
                                obj.getInt("question_id")
                        )
                );
            }
            if (optionsMap == null)
                optionsMap = new HashMap<>();
            optionsMap.put(optionsList.get(0).questionId, optionsList);
            // Display options if they belong to currently displayed question
            if (optionsList.get(0).questionId == currQuesion.id) {
                updateOptions();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void initializeQuestionList(String jsonString){
        try {
            JSONArray arr = new JSONArray(jsonString);
            questions = new ArrayList<Question>();
            for (int i = 0; i < arr.length(); i++){
                JSONObject obj = arr.getJSONObject(i);
                questions.add(
                        new Question(
                                obj.getInt("id"),
                                obj.getString("question")
                        )
                );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void initializeQuiz(){
        if((questions != null) && questions.size() > 0) {
            Question question = questions.get(0);
            if (question.text.length()>0)
                currQuesion = question;
            getOptions(questions.get(0).id, questions.size());
            displayQuestion();
            for (int i = 1; i < questions.size(); i++)
                getOptions(questions.get(i).id, questions.size());
        }else
            Toast.makeText(QuestionActivity.this, "No Questions Were Fetched :(", Toast.LENGTH_SHORT).show();
    }
}