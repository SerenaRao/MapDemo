package com.hzcz.mapdemo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.hzcz.mapdemo.R;
import com.hzcz.mapdemo.overlay.DrivingRouteOverlay;
import com.hzcz.mapdemo.util.AMapUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * des：驾车路线
 * Created by Serena on 2017/6/28.
 */

public class DriveRouteActivity extends Activity implements View.OnClickListener, AMap.OnMapClickListener,
        AMap.OnMarkerClickListener, AMap.OnInfoWindowClickListener, AMap.InfoWindowAdapter, RouteSearch.OnRouteSearchListener {

    @BindView(R.id.fanhui)
    ImageView mFanhui;
    @BindView(R.id.drive_map)
    MapView mDriveMap;
    @BindView(R.id.drive)
    TextView mDrive;
    @BindView(R.id.detail_tv)
    TextView mDetailTv;

    private double slat;
    private double slng;
    private double elat;
    private double elng;
    private DrivePath mDrivePath;
    private DriveRouteResult mDriveRouteResult;
    private LatLonPoint mStartPoint;//起点
    private LatLonPoint mEndPoint;//终点
    private AMap aMap;
    private RouteSearch mRouteSearch;
    private ProgressDialog progDialog = null;// 搜索时进度条
    private MyLocationStyle myLocationStyle;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_drive_route);
        ButterKnife.bind(this);
        slat = getIntent().getDoubleExtra("slat", 0.00);
        slng = getIntent().getDoubleExtra("slng", 0.00);
        elat = getIntent().getDoubleExtra("elat", 0.00);
        elng = getIntent().getDoubleExtra("elng", 0.00);
        mDrivePath = getIntent().getParcelableExtra("drive_path");
        mDriveRouteResult = getIntent().getParcelableExtra("drive_result");
        mStartPoint = new LatLonPoint(slat, slng);//起点
        mEndPoint = new LatLonPoint(elat, elng);//终点

        mDriveMap.onCreate(bundle);// 此方法必须重写
        init();
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
        searchRouteResult(2, RouteSearch.DrivingDefault);
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mDriveMap.getMap();
            setUpMap();
        }
        aMap.setOnMapClickListener(this);
        aMap.setOnMarkerClickListener(this);
        aMap.setOnInfoWindowClickListener(this);
        aMap.setInfoWindowAdapter(this);

        //画起点和终点
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mStartPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mEndPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        //控制比例尺控件是否显示
        aMap.getUiSettings().setScaleControlsEnabled(true);
        //控制缩放图示是否显示
        aMap.getUiSettings().setZoomControlsEnabled(false);
    }

    @OnClick({R.id.fanhui, R.id.drive,R.id.detail_tv})
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.fanhui:
                finish();
                break;
            case R.id.drive:
                intent = new Intent(this, DriveRouteCalculateActivity.class);
                intent.putExtra("slat", slat);
                intent.putExtra("slng", slng);
                intent.putExtra("elat", elat);
                intent.putExtra("elng", elng);
                startActivity(intent);
                break;
            case R.id.detail_tv:
                intent = new Intent(this, DriveRouteDetailActivity.class);
                intent.putExtra("drive_path", mDrivePath);
                intent.putExtra("drive_result", mDriveRouteResult);
                startActivity(intent);
                break;
        }
    }

    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult(int routeType, int mode) {
        if (mStartPoint == null) {
            Toast.makeText(this, "定位中，稍后再试...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mEndPoint == null) {
            Toast.makeText(this, "终点未设置", Toast.LENGTH_SHORT).show();
        }
        showProgressDialog();
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(mStartPoint, mEndPoint);
        // 驾车路径规划
        if (routeType == 2) {
            // 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
            RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, mode, null, null, "");
            mRouteSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询
        }
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mDriveMap.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mDriveMap.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mDriveMap.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDriveMap.onDestroy();
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int errorCode) {
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (driveRouteResult != null && driveRouteResult.getPaths() != null) {
                if (driveRouteResult.getPaths().size() > 0) {
                    mDriveRouteResult = driveRouteResult;
                    final DrivePath drivePath = mDriveRouteResult.getPaths().get(0);
                    DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                            this, aMap, drivePath,
                            mDriveRouteResult.getStartPos(),
                            mDriveRouteResult.getTargetPos(), null);
                    drivingRouteOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
                    drivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
                    drivingRouteOverlay.removeFromMap();
                    drivingRouteOverlay.addToMap();
                    drivingRouteOverlay.zoomToSpan();
                } else if (driveRouteResult != null && driveRouteResult.getPaths() == null) {
                    Toast.makeText(this, "对不起，没有搜索到相关数据！", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "对不起，没有搜索到相关数据！", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "" + errorCode, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }
}
