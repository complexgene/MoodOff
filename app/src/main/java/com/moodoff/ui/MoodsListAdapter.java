package com.moodoff.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.moodoff.R;

import java.util.ArrayList;

/**
 * Created by snaskar on 9/26/2016.
 */

public class MoodsListAdapter extends BaseExpandableListAdapter {
    private Context ctx;
    ArrayList<String> allMoodsType = new ArrayList<>();
    public MoodsListAdapter(Context ctx){
        this.ctx = ctx;
        allMoodsType.add("Romantic");
        allMoodsType.add("Sad");
    }


    @Override
    public int getChildType(int groupPosition, int childPosition) {
        return super.getChildType(groupPosition, childPosition);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return allMoodsType.size();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return allMoodsType.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String child_title = (String) getChild(groupPosition,childPosition);
        if(convertView ==  null){
            LayoutInflater layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.moodlist_parent,parent,false);
        }
        TextView parentTextView = (TextView) convertView.findViewById(R.id.selectmood);
        parentTextView.setText(child_title);

        return convertView;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return "Select Mood";
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String group_title = (String) getGroup(groupPosition);
        if(convertView ==  null){
            LayoutInflater layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.moodlist_child,parent,false);
        }
        TextView childTextView = (TextView) convertView.findViewById(R.id.eachMoood);
        childTextView.setText(group_title);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public int getGroupCount() {
        return 1;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }



}
