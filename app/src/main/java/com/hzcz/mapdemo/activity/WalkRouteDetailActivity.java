package com.hzcz.mapdemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.hzcz.mapdemo.R;
import com.hzcz.mapdemo.adapter.WalkSegmentListAdapter;
import com.hzcz.mapdemo.util.AMapUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * des:驾车路线详情
 * Created by Serena on 2017/6/28.
 */

public class WalkRouteDetailActivity extends Activity {

    @BindView(R.id.fanhui)
    ImageView mFanhui;
    @BindView(R.id.km_tv)
    TextView mKmTv;
    @BindView(R.id.time_tv)
    TextView mTimeTv;
    @BindView(R.id.walk_segment_list)
    ListView mWalkSegmentList;

    private WalkPath mWalkPath;
    private WalkRouteResult mWalkRouteResult;
    private WalkSegmentListAdapter mWalkSegmentListAdapter;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_walk_route_detail);
        ButterKnife.bind(this);
        mWalkPath = getIntent().getParcelableExtra("walk_path");
        mWalkRouteResult = getIntent().getParcelableExtra("walk_result");

        init();
    }
    private void init() {
        String dur = AMapUtil.getFriendlyTime((int) mWalkPath.getDuration());
        String dis = AMapUtil.getFriendlyLength((int) mWalkPath.getDistance());
        mKmTv.setText("全程" + dis);
        mTimeTv.setText(dur);
        mWalkSegmentListAdapter = new WalkSegmentListAdapter(this, mWalkPath.getSteps());
        mWalkSegmentList.setAdapter(mWalkSegmentListAdapter);
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
