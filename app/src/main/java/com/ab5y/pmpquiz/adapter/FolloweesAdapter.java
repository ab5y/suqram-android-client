package com.ab5y.pmpquiz.adapter;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.ab5y.pmpquiz.FollowActivity;
import com.ab5y.pmpquiz.R;
import com.ab5y.pmpquiz.models.Followee;

import java.util.List;
import java.util.Locale;

/**
 * Created by Abhay on 18/7/2016.
 */
public class FolloweesAdapter extends ArrayAdapter<Followee> {
    private static final String TAG = "FOLLOWEEADAPTER";
    private Context context;
    private int resource;
    private List<Followee> data = null;
    private LayoutInflater layoutInflater;

    public FolloweesAdapter(Context context, int resource, List<Followee> data) {
        super(context, resource, data);
        this.context = context;
        this.resource = resource;
        this.data = data;
        layoutInflater = LayoutInflater.from(context);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(resource, parent, false);
            holder = new ViewHolder();
            holder.usernameView = (TextView) convertView.findViewById(R.id.followee_username);
            holder.fullnameView = (TextView) convertView.findViewById(R.id.followee_fullname);
            holder.numcategoriesView = (TextView) convertView.findViewById(R.id.followee_num_categories);
            holder.followView = (CheckBox) convertView.findViewById(R.id.cb_followee_follow);
            holder.followView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!holder.followView.isChecked()) {
                        ((FollowActivity) context).follow(data.get(position).getUserID());
                        holder.followView.setButtonDrawable(R.drawable.ic_remove_circle_black_24dp);
                        holder.followView.setChecked(true);
                        data.get(position).setFollowing(true);
                    } else {
                        ((FollowActivity) context).unfollow(data.get(position).getUserID());
                        holder.followView.setButtonDrawable(R.drawable.ic_add_circle_outline_black_24dp);
                        holder.followView.setChecked(false);
                        data.get(position).setFollowing(false);
                    }
                }
            });
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.usernameView.setText(data.get(position).getUserName());
        holder.fullnameView.setText(data.get(position).getFullName());
        holder.numcategoriesView.setText(String.format("Authored %d categories", data.get(position).getNumCategoriesAuthored()));
        if (data.get(position).getFollowing()) {
            holder.followView.setButtonDrawable(R.drawable.ic_remove_circle_black_24dp);
            holder.followView.setChecked(true);
        } else {
            Log.e(TAG, "IS NOT FOLLOWING CURRENTLY");
            holder.followView.setButtonDrawable(R.drawable.ic_add_circle_outline_black_24dp);
            holder.followView.setChecked(false);
        }
        return convertView;
    }

    static class ViewHolder {
        TextView usernameView;
        TextView fullnameView;
        TextView numcategoriesView;
        CheckBox followView;
    }
}