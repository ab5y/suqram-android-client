package com.ab5y.pmpquiz.listener;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.ab5y.pmpquiz.QuestionActivity;
import com.ab5y.pmpquiz.R;
import com.ab5y.pmpquiz.models.Option;

import java.util.List;

/**
 * Created by Abhay on 28/6/2016.
 */
public class OnOptionsListViewItemClickListener implements AdapterView.OnItemClickListener {

    public int id;
    public String text;
    QuestionActivity parentActivity;

    public OnOptionsListViewItemClickListener(QuestionActivity activity){
        parentActivity = activity;
    }

    private static final String LOG = "ITEMCLICKLISTENER";

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CheckedTextView checkedTextViewItem = ((CheckedTextView) view.findViewById(R.id.checkedTextView));
        this.text = checkedTextViewItem.getText().toString();
        Option option = (Option) checkedTextViewItem.getTag();
        this.id = option.id;

        List<Option> optionList = parentActivity.optionsMap.get(parentActivity.currQuesion.id);
        int count = optionList.size();
        ViewParent viewParent = view.getParent();
        for (int i = 0; i < count; i++){
            CheckedTextView ctv = ((CheckedTextView) ((View)viewParent).findViewWithTag(optionList.get(i)));
            if (ctv != null){
                ctv.setChecked(false);
                ctv.setBackgroundColor(Color.parseColor("#fff3f3f3"));
            }
        }

        checkedTextViewItem.setChecked(true);
        checkedTextViewItem.setBackgroundColor(Color.CYAN);
        parentActivity.updateAnswers();
    }
}