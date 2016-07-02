package com.ab5y.pmpquiz;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ab5y.pmpquiz.adapter.OptionsAdapter;
import com.ab5y.pmpquiz.listener.OnListViewItemClickListener;
import com.ab5y.pmpquiz.listener.OnSwipeTouchListener;
import com.ab5y.pmpquiz.models.Option;
import com.ab5y.pmpquiz.models.Question;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class QuestionActivity extends AppCompatActivity {

    private static final String LOG = "QUESTIONACTIVITY";
    private static final String URL = "http://211d9164.ngrok.io/";
    public Map<Integer, List<Option>> optionsMap = new HashMap<>();
    public List<Question> questions;
    public Question currQuesion;
    public Map<Integer, Integer> answers = new HashMap<>();
    public OnListViewItemClickListener onListViewItemClickListener = new OnListViewItemClickListener(this);
    private OptionsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

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

        getQuestions();
    }

    public void updateAnswers(){
        if((currQuesion != null)&&(onListViewItemClickListener.id >= 0)){
            answers.put(currQuesion.id, onListViewItemClickListener.id);
        }
    }

    private void getQuestions(){
        getFromServer getQs = new getFromServer();
        getQs.execute(URL + "get_questions");
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

    private void getOptions(int questionId) throws JSONException {
        optionsMap = null;
        JSONObject toPost = new JSONObject();
        toPost.put("question_id", questionId);
        postToServer getOs = new postToServer(this);
        getOs.execute(URL + "get_options", toPost.toString());
    }

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    class postToServer extends AsyncTask<String, Integer, String> {
        private Exception exception;
        OkHttpClient client = new OkHttpClient();
        Context context;

        public postToServer(Context context){
            this.context = context;
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
                return  response.body().string();
            } catch (Exception e) {
                this.exception = e;
                Log.e(LOG, e.toString());
                return null;
            }
        }

        protected void onProgressUpdate(Integer... progress) {
//            setProgressPercent(progress[0]);
        }

        @Override
        protected void onPostExecute(String result){
            if (result != null) {
                String jsonString = result.trim()
                        .substring(1, result.length() - 1)
                        .replace("\\", "");
                try {
                    JSONArray arr = new JSONArray(jsonString);
                    List<Option> optionsList = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
//                        Log.e(LOG, obj.toString());
                        optionsList.add(new Option(obj.getInt("id"), obj.getString("option"), obj.getBoolean("isCorrectAnswer"), obj.getInt("question_id")));
                    }
                    if(optionsMap == null)
                        optionsMap = new HashMap<>();
                    optionsMap.put(optionsList.get(0).questionId, optionsList);
                    if (optionsList.get(0).questionId == currQuesion.id) {
                        updateOptions();
                    }
                } catch (JSONException e) {
                    Log.e(LOG, e.toString());
                }
            }
        }
    }

    class getFromServer extends AsyncTask<String, Integer, String> {

        private Exception exception;
        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(String... params) {
            try{
                Request request = new Request.Builder()
                        .url(params[0])
                        .build();

                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                this.exception = e;
//                e.printStackTrace();
                Log.e(LOG, e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                String jsonString = result.trim()
                                        .substring(1, result.length()-1)
                                        .replace("\\", "");
                try {
                    JSONArray arr = new JSONArray(jsonString);
                    questions = new ArrayList<Question>();
                    for (int i = 0; i < arr.length(); i++){
                        JSONObject obj = arr.getJSONObject(i);
                        Log.e(LOG, obj.toString());
                        questions.add(new Question(obj.getInt("id"), obj.getString("question")));
                    }
                    if (questions != null) {
                        Question question = questions.get(0);
                        if (question.text.length()>0) {
                            currQuesion = question;
                            displayQuestion();
                        }
                    }
                } catch (JSONException e) {
                    Log.e(LOG, e.toString());
                }
                try {
                    if(questions != null)
                        for(int i = 0; i < questions.size(); i++)
                            getOptions(questions.get(i).id);
                    else
                        Log.e(LOG, "IT IS STILL FUCKING NULL, STUART");
                } catch (JSONException e) {
                    //e.printStackTrace();
                    Log.e(LOG, e.toString());
                }
            }
        }
    }
}