package com.ab5y.pmpquiz.adapter;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import com.ab5y.pmpquiz.R;
import com.ab5y.pmpquiz.models.Option;

import java.util.List;

/**
 * Created by Abhay on 27/6/2016.
 */
public class OptionsAdapter extends ArrayAdapter<Option> {

    Context context;
    int resource;
    public List<Option> data = null;

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
        checkedTextView.setTag(option.id);

        return convertView;
    }
}
