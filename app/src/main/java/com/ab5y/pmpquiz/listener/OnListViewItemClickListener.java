package com.ab5y.pmpquiz.listener;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.ab5y.pmpquiz.QuestionActivity;
import com.ab5y.pmpquiz.R;

/**
 * Created by Abhay on 28/6/2016.
 */
public class OnListViewItemClickListener implements AdapterView.OnItemClickListener {

    public int id;
    public String text;
    QuestionActivity parentActivity;

    public OnListViewItemClickListener(QuestionActivity activity){
        parentActivity = activity;
    }

    private static final String LOG = "ITEMCLICKLISTENER";

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Context context = view.getContext();
        CheckedTextView checkedTextViewItem = ((CheckedTextView) view.findViewById(R.id.checkedTextView));
        this.text = checkedTextViewItem.getText().toString();
        this.id = (int) checkedTextViewItem.getTag();
        parentActivity.updateAnswers();
    }
}