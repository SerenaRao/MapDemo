package com.hzcz.mapdemo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.services.route.WalkStep;
import com.hzcz.mapdemo.R;
import com.hzcz.mapdemo.util.AMapUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Serena on 2017/6/29.
 */

public class WalkSegmentListAdapter extends BaseAdapter {
    private Context mContext;
    private List<WalkStep> mItemList = new ArrayList<WalkStep>();

    public WalkSegmentListAdapter(Context context, List<WalkStep> list) {
        this.mContext = context;
        mItemList.add(new WalkStep());
        for (WalkStep walkStep : list) {
            mItemList.add(walkStep);
        }
        mItemList.add(new WalkStep());
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mItemList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_bus_segment,
                    null);
            holder.driveDirIcon = (ImageView) convertView
                    .findViewById(R.id.bus_dir_icon);
            holder.driveLineName = (TextView) convertView
                    .findViewById(R.id.bus_line_name);
            holder.driveDirUp = (ImageView) convertView
                    .findViewById(R.id.bus_dir_icon_up);
            holder.driveDirDown = (ImageView) convertView
                    .findViewById(R.id.bus_dir_icon_down);
            holder.splitLine = (ImageView) convertView
                    .findViewById(R.id.bus_seg_split_line);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final WalkStep item = mItemList.get(position);
        if (position == 0) {
            holder.driveDirIcon.setImageResource(R.drawable.dir_start);
            holder.driveLineName.setText("出发");
            holder.driveDirUp.setVisibility(View.GONE);
            holder.driveDirDown.setVisibility(View.VISIBLE);
            holder.splitLine.setVisibility(View.GONE);
            return convertView;
        } else if (position == mItemList.size() - 1) {
            holder.driveDirIcon.setImageResource(R.drawable.dir_end);
            holder.driveLineName.setText("到达终点");
            holder.driveDirUp.setVisibility(View.VISIBLE);
            holder.driveDirDown.setVisibility(View.GONE);
            holder.splitLine.setVisibility(View.VISIBLE);
            return convertView;
        } else {
            String actionName = item.getAction();
            int resID = AMapUtil.getDriveActionID(actionName);
            holder.driveDirIcon.setImageResource(resID);
            holder.driveLineName.setText(item.getInstruction());
            holder.driveDirUp.setVisibility(View.VISIBLE);
            holder.driveDirDown.setVisibility(View.VISIBLE);
            holder.splitLine.setVisibility(View.VISIBLE);
            return convertView;
        }

    }

    private class ViewHolder {
        TextView driveLineName;
        ImageView driveDirIcon;
        ImageView driveDirUp;
        ImageView driveDirDown;
        ImageView splitLine;
    }
}
