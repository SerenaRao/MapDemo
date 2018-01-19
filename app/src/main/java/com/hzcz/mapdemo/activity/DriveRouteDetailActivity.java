package com.hzcz.mapdemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.hzcz.mapdemo.R;
import com.hzcz.mapdemo.adapter.DriveSegmentListAdapter;
import com.hzcz.mapdemo.util.AMapUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * des:驾车路线详情
 * Created by Serena on 2017/6/28.
 */

public class DriveRouteDetailActivity extends Activity {

    @BindView(R.id.fanhui)
    ImageView mFanhui;
    @BindView(R.id.km_tv)
    TextView mKmTv;
    @BindView(R.id.time_tv)
    TextView mTimeTv;
    @BindView(R.id.drive_segment_list)
    ListView mDriveSegmentList;

    private DrivePath mDrivePath;
    private DriveRouteResult mDriveRouteResult;
    private DriveSegmentListAdapter mDriveSegmentListAdapter;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_drive_route_detail);
        ButterKnife.bind(this);
        mDrivePath = getIntent().getParcelableExtra("drive_path");
        mDriveRouteResult = getIntent().getParcelableExtra("drive_result");

        init();
    }
    private void init() {
        String dur = AMapUtil.getFriendlyTime((int) mDrivePath.getDuration());
        String dis = AMapUtil.getFriendlyLength((int) mDrivePath.getDistance());
        mKmTv.setText("全程" + dis);
        mTimeTv.setText(dur);
        mDriveSegmentListAdapter = new DriveSegmentListAdapter(this, mDrivePath.getSteps());
        mDriveSegmentList.setAdapter(mDriveSegmentListAdapter);
    }

    @OnClick({R.id.fanhui})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fanhui:
                finish();
                break;
        }
    }
}
