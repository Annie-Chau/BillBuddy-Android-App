package com.learning.billbuddy.views.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.learning.billbuddy.R;

public class FilterSelectionAdapter extends BaseAdapter {

    private Context context;

    private String[] filterOptions = {"Filter", "Date Ascending", "Date Descending", "Reimbursement Available"};

    public FilterSelectionAdapter(Context context) {
        this.context = context;
    }


    @Override
    public int getCount() {
        return filterOptions.length;
    }

    @Override
    public Object getItem(int position) {
        return filterOptions[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.item_selection, parent, false);
        TextView txtName = rootView.findViewById(R.id.item_selection_name);
        txtName.setText(filterOptions[position]);
        return rootView;
    }
}
