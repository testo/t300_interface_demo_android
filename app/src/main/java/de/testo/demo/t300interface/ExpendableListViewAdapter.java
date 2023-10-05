package de.testo.demo.t300interface;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ExpendableListViewAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> mTopics;
    private HashMap<String, HashMap<String, String>> mAdditionalInfoList;

    public ExpendableListViewAdapter(Context context, HashMap<String, HashMap<String, String>> additionalInfoList) {
        this.context = context;
        this.mAdditionalInfoList = additionalInfoList;
        this.mTopics = new ArrayList<>(additionalInfoList.keySet());
    }



    @Override
    public int getGroupCount() {
        return mTopics.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.mAdditionalInfoList.get(this.mTopics.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.mTopics.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String groupName = mTopics.get(groupPosition);
        HashMap<String, String> childMap = mAdditionalInfoList.get(groupName);
        if (childMap != null) {
            ArrayList<String> keys = new ArrayList<>(childMap.keySet());
            return childMap.get(keys.get(childPosition));
        }
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(int groupPosition, boolean b, View view, ViewGroup viewGroup) {
        String topicsTitle = (String) getGroup(groupPosition);

        if (view == null){
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.additional_info_list_entry_header, null);
        }
        TextView head_tv = view.findViewById(R.id.head_tv);
        head_tv.setText(topicsTitle);

        return view;
    }
    @SuppressLint("InflateParams")
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean b, View view, ViewGroup viewGroup) {
        String groupName = mTopics.get(groupPosition);
        HashMap<String, String> childMap = mAdditionalInfoList.get(groupName);

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.additional_info_list_entry_body, null);
        }

        TextView bodyTv = view.findViewById(R.id.body_tv);

        if (childMap != null) {
            ArrayList<String> keys = new ArrayList<>(childMap.keySet());
            String key = keys.get(childPosition);
            String value = childMap.get(key);

            // Display both the key and the value
            String keyValueString = key + ": " + value;
            bodyTv.setText(keyValueString);
        }

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
