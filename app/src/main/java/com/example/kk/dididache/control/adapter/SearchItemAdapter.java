package com.example.kk.dididache.control.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.kk.dididache.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 小吉哥哥 on 2017/8/16.
 */

public class SearchItemAdapter extends ArrayAdapter<String> {


    public SearchItemAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
    }

    public SearchItemAdapter(@NonNull Context context, @NonNull List<String> objects) {
        super(context, R.layout.search_item, objects);
    }

    public SearchItemAdapter(@NonNull Context context) {
        super(context, R.layout.search_item, new ArrayList<String>());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView;
        ViewHolder holder;
        if (convertView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.search_item, parent, false);
            holder = new ViewHolder(itemView);
            itemView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            itemView = convertView;
        }
        holder.blueLine.setVisibility(position == getCount() - 1 ? View.GONE : View.VISIBLE);
        holder.locationName.setText(getItem(position));
        return itemView;
    }


    class ViewHolder {
        TextView locationName;
        View blueLine;

        ViewHolder(View itemView) {
            locationName = (TextView) itemView.findViewById(R.id.suggest_location_name);
            blueLine = itemView.findViewById(R.id.blue_line);
        }
    }
}
