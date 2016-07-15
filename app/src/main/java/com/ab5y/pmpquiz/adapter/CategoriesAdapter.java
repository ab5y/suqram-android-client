package com.ab5y.pmpquiz.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ab5y.pmpquiz.R;
import com.ab5y.pmpquiz.models.Category;

import java.util.List;

/**
 * Created by Abhay on 7/7/2016.
 */
public class CategoriesAdapter extends ArrayAdapter<Category> {
    private Context context;
    private int resource;
    private List<Category> data = null;

    private static final String LOG = "CATEGORIESADAPTER";

    public CategoriesAdapter(Context context, int resource, List<Category> data){
        super(context, resource, data);

        this.context = context;
        this.resource = resource;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parentView) {
        ViewHolder mainViewHolder;
        if (convertView == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(resource, parentView, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.category = data.get(position);
            viewHolder.tv_category_name = (TextView) convertView.findViewById(R.id.tv_category_name);
            viewHolder.tv_category_name.setText(viewHolder.category.name);
            convertView.setTag(viewHolder);
        } else {
            mainViewHolder = (ViewHolder) convertView.getTag();
            mainViewHolder.tv_category_name.setText(mainViewHolder.category.name);
        }
        return convertView;
    }

    public class ViewHolder {
        Category category;
        TextView tv_category_name;
    }
}
