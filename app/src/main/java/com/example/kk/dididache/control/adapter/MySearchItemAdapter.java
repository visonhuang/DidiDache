package com.example.kk.dididache.control.adapter;

/**
 * Created by KK on 2017/8/24.
 */

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.example.kk.dididache.R;
import com.example.kk.dididache.model.Location;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.example.kk.dididache.ui.ChooseAreaActivity.LATLNG_BACK;
import static com.example.kk.dididache.ui.ChooseAreaActivity.NAME_BACK;

/**
 * Created by 小吉哥哥 on 2017/8/16.
 */

public class MySearchItemAdapter extends ArrayAdapter<String> {
    private List<Location> locationList;
    private Context mContext;

    public MySearchItemAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
    }

    public MySearchItemAdapter(@NonNull Context context, @NonNull List<String> objects, List<Location> locations) {
        super(context, R.layout.my_search_item, objects);
        locationList = locations;
        mContext = context;
    }

    public MySearchItemAdapter(@NonNull Context context) {
        super(context, R.layout.my_search_item, new ArrayList<String>());
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView;
        ViewHolder holder;
        if (convertView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.my_search_item, parent, false);
            holder = new ViewHolder(itemView);
            itemView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
            itemView = convertView;
        }
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position == 0){
                    Intent intent = new Intent();
                    intent.putExtra(NAME_BACK, "我的位置");
                    ((AppCompatActivity) mContext).setResult(RESULT_OK, intent);
                }else {
                    LatLng latLng = locationList.get(position).getLatLng();
                    Intent intent = new Intent();
                    intent.putExtra(LATLNG_BACK, latLng);
                    intent.putExtra(NAME_BACK, locationList.get(position).getName());
                    ((AppCompatActivity) mContext).setResult(RESULT_OK, intent);
                }
                ((AppCompatActivity) mContext).finish();
            }
        });
        holder.blueLine.setVisibility(position == getCount() - 1 ? View.GONE : View.GONE);
        if(position == 0){
            holder.locationName.setText("我的位置");
        }else {
            holder.locationName.setText(getItem(position));
        }
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
