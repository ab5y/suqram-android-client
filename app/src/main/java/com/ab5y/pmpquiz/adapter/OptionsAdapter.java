package com.ab5y.pmpquiz.adapter;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import com.ab5y.pmpquiz.QuestionActivity;
import com.ab5y.pmpquiz.R;
import com.ab5y.pmpquiz.models.Option;

import java.util.List;

/**
 * Created by Abhay on 27/6/2016.
 */
public class OptionsAdapter extends ArrayAdapter<Option> {

    private Context context;
    private int resource;
    private List<Option> data = null;

    private static final String LOG = "OPTIONSADAPTER";

    public OptionsAdapter(Context context, int resource, List<Option> data) {
        super(context, resource, data);

        this.context = context;
        this.resource = resource;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /*
         * The convertView argument is essentially a "ScrapView" as described is Lucas post
         * http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
         * It will have a non-null value when ListView is asking you recycle the row layout.
         * So, when convertView is not null, you should simply update its contents instead of inflating a new row layout.
         */
        if(convertView == null) {
            // inflate the layout
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(resource, parent, false);
        }

        // Option based on the position
        Option option = data.get(position);

        // Get the CheckedTextView and then set the text (item text) and tag (item id) values
        CheckedTextView checkedTextView = (CheckedTextView) convertView.findViewById(R.id.checkedTextView);
        checkedTextView.setText(option.text);
        checkedTextView.setTag(option);

        // Highlight the option the user selected if the question has already been attempted
        QuestionActivity questionActivity = (QuestionActivity)context;
        if(questionActivity.answers.containsKey(questionActivity.currQuesion.id)){
            if(questionActivity.answers.get(questionActivity.currQuesion.id) == option.id){
                checkedTextView.setChecked(true);
                checkedTextView.setBackgroundColor(Color.CYAN);
            } else {
                checkedTextView.setBackgroundColor(Color.TRANSPARENT); // Color.parseColor("#fff3f3f3")
                checkedTextView.setChecked(false);
            }
        }

        return convertView;
    }
}
