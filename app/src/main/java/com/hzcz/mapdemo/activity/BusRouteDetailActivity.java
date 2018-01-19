package com.hzcz.mapdemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.hzcz.mapdemo.R;
import com.hzcz.mapdemo.adapter.BusSegmentListAdapter;
import com.hzcz.mapdemo.overlay.BusRouteOverlay;
import com.hzcz.mapdemo.util.AMapUtil;


public class BusRouteDetailActivity extends Activity implements OnMapLoadedListener,
        OnMapClickListener, InfoWindowAdapter, OnInfoWindowClickListener, OnMarkerClickListener, View.OnClickListener {
    private AMap aMap;
    private MapView mapView;
    private BusPath mBuspath;
    private BusRouteResult mBusRouteResult;
    private TextView mTitle, mTitleBusRoute, mDesBusRoute;
    private ListView mBusSegmentList;
    private BusSegmentListAdapter mBusSegmentListAdapter;
    private LinearLayout mBusMap, mBuspathview;
    private BusRouteOverlay mBusrouteOverlay;
    private ImageView mShowBusSegmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_detail);
        mapView = (MapView) findViewById(R.id.route_map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        getIntentData();
        init();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            mBuspath = intent.getParcelableExtra("bus_path");
            mBusRouteResult = intent.getParcelableExtra("bus_result");
        }
    }

    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        registerListener();

        mTitle = (TextView) findViewById(R.id.title_center);
        mTitle.setText("公交路线详情");
        mTitleBusRoute = (TextView) findViewById(R.id.firstline);
        mDesBusRoute = (TextView) findViewById(R.id.secondline);
        String dur = AMapUtil.getFriendlyTime((int) mBuspath.getDuration());
        String dis = AMapUtil.getFriendlyLength((int) mBuspath.getDistance());
        mTitleBusRoute.setText(dur + "(" + dis + ")");
        int taxiCost = (int) mBusRouteResult.getTaxiCost();
        mDesBusRoute.setText("打车约" + taxiCost + "元");
        mDesBusRoute.setVisibility(View.VISIBLE);
        mBusMap = (LinearLayout) findViewById(R.id.title_map);
        mBusMap.setOnClickListener(this);

        aMap.clear();// 清理地图上的所有覆盖物
        mBusrouteOverlay = new BusRouteOverlay(this, aMap,
                mBuspath, mBusRouteResult.getStartPos(),
                mBusRouteResult.getTargetPos());
        mBusrouteOverlay.removeFromMap();

        mShowBusSegmentList = (ImageView) findViewById(R.id.show_bus_segment_list);
        mShowBusSegmentList.setOnClickListener(this);

        mBuspathview = (LinearLayout) findViewById(R.id.bus_path);
        configureListView();
    }

    private void registerListener() {
        aMap.setOnMapLoadedListener(this);
        aMap.setOnMarkerClickListener(this);
        aMap.setOnMapClickListener(this);
        aMap.setOnInfoWindowClickListener(this);
        aMap.setInfoWindowAdapter(this);
    }

    private void configureListView() {
        mBusSegmentList = (ListView) findViewById(R.id.bus_segment_list);
        mBusSegmentListAdapter = new BusSegmentListAdapter(
                this.getApplicationContext(), mBuspath.getSteps());
        mBusSegmentList.setAdapter(mBusSegmentListAdapter);
    }

    public void onBackClick(View view) {
        this.finish();
    }

    @Override
    public void onMapLoaded() {
        if (mBusrouteOverlay != null) {
            mBusrouteOverlay.addToMap();
            mBusrouteOverlay.zoomToSpan();
        }
    }

    @Override
    public void onMapClick(LatLng arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public View getInfoContents(Marker arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public View getInfoWindow(Marker arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onMarkerClick(Marker arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_bus_segment_list:
                mBuspathview.setVisibility(View.GONE);
                mBusSegmentList.setVisibility(View.VISIBLE);
                break;
            case R.id.title_map:
                mBuspathview.setVisibility(View.VISIBLE);
                mapView.setVisibility(View.VISIBLE);
                mBusSegmentList.setVisibility(View.GONE);
                break;
        }
    }
}
