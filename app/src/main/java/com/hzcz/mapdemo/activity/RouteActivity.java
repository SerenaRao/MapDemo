package com.hzcz.mapdemo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.hzcz.mapdemo.R;
import com.hzcz.mapdemo.adapter.BusResultListAdapter;
import com.hzcz.mapdemo.util.AMapUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * des:线路的选择页面
 * Created by Serena on 2017/6/28.
 */

public class RouteActivity extends Activity implements View.OnClickListener, RouteSearch.OnRouteSearchListener, GeocodeSearch.OnGeocodeSearchListener {

    @BindView(R.id.fanhui)
    ImageView mFanhui;
    @BindView(R.id.start_location)
    TextView mStartLocation;
    @BindView(R.id.end_location)
    TextView mEndLocation;
    @BindView(R.id.route_bus)
    ImageView mRouteBus;
    @BindView(R.id.route_car)
    ImageView mRouteCar;
    @BindView(R.id.route_walk)
    ImageView mRouteWalk;
    @BindView(R.id.route_result_list)
    ListView mRouteResultList;
    @BindView(R.id.line_tv)
    TextView mLineTv;
    @BindView(R.id.time_and_km)
    TextView mTimeAndKm;
    @BindView(R.id.go_to_detail)
    Button mGoToDetail;
    @BindView(R.id.car_layout)
    RelativeLayout mCarLayout;

    private double slat;
    private double slng;
    private double elat;
    private double elng;
    private int route;
    private LatLonPoint mStartPoint;//起点
    private LatLonPoint mEndPoint;//终点
    private ProgressDialog progDialog = null;// 搜索时进度条

    private GeocodeSearch geocoderSearch;
    private RouteSearch mRouteSearch;
    private String mCurrentCityName = "杭州";
    private final int ROUTE_TYPE_BUS = 1;
    private final int ROUTE_TYPE_CAR = 2;
    private final int ROUTE_TYPE_WALK = 3;

    private BusRouteResult mBusRouteResult;
    private DriveRouteResult mDriveRouteResult;
    private WalkRouteResult mWalkRouteResult;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_route_base);
        ButterKnife.bind(this);
        slat = getIntent().getDoubleExtra("slat", 0.00);
        slng = getIntent().getDoubleExtra("slng", 0.00);
        elat = getIntent().getDoubleExtra("elat", 0.00);
        elng = getIntent().getDoubleExtra("elng", 0.00);
        route = getIntent().getIntExtra("route", 0);
        mStartPoint = new LatLonPoint(slat, slng);//起点
        mEndPoint = new LatLonPoint(elat, elng);//终点

        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
        //第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(mEndPoint, 200, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);// 设置异步逆地理编码请求

        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
        switch (route) {
            case ROUTE_TYPE_BUS:
                searchRouteResult(ROUTE_TYPE_BUS, RouteSearch.BusDefault);
                mRouteResultList.setVisibility(View.VISIBLE);
                mCarLayout.setVisibility(View.GONE);
                break;
            case ROUTE_TYPE_CAR:
                searchRouteResult(ROUTE_TYPE_CAR, RouteSearch.DrivingDefault);
                mRouteResultList.setVisibility(View.GONE);
                mCarLayout.setVisibility(View.VISIBLE);
                break;
            case ROUTE_TYPE_WALK:
                searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
                mRouteResultList.setVisibility(View.GONE);
                mCarLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    @OnClick({R.id.fanhui, R.id.route_bus, R.id.route_car, R.id.route_walk})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fanhui:
                finish();
                break;
            case R.id.route_bus:
                searchRouteResult(ROUTE_TYPE_BUS, RouteSearch.BusDefault);
                mRouteResultList.setVisibility(View.VISIBLE);
                mCarLayout.setVisibility(View.GONE);
                break;
            case R.id.route_car:
                searchRouteResult(ROUTE_TYPE_CAR, RouteSearch.DrivingDefault);
                mRouteResultList.setVisibility(View.GONE);
                mCarLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.route_walk:
                searchRouteResult(ROUTE_TYPE_WALK, RouteSearch.WalkDefault);
                mRouteResultList.setVisibility(View.GONE);
                mCarLayout.setVisibility(View.VISIBLE);
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
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(
                mStartPoint, mEndPoint);
        switch (routeType) {
            case ROUTE_TYPE_BUS:
                // 公交路径规划
                // 第一个参数表示路径规划的起点和终点，第二个参数表示公交查询模式，第三个参数表示公交查询城市区号，第四个参数表示是否计算夜班车，0表示不计算
                RouteSearch.BusRouteQuery busRouteQuery = new RouteSearch.BusRouteQuery(fromAndTo, mode, mCurrentCityName, 0);
                mRouteSearch.calculateBusRouteAsyn(busRouteQuery);// 异步路径规划公交模式查询
                break;
            case ROUTE_TYPE_CAR:
                // 驾车路径规划
                // 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
                RouteSearch.DriveRouteQuery driveRouteQuery = new RouteSearch.DriveRouteQuery(fromAndTo, mode, null, null, "");
                mRouteSearch.calculateDriveRouteAsyn(driveRouteQuery);// 异步路径规划驾车模式查询
                break;
            case ROUTE_TYPE_WALK:
                //步行路径规划
                RouteSearch.WalkRouteQuery walkRouteQuery = new RouteSearch.WalkRouteQuery(fromAndTo, mode);
                mRouteSearch.calculateWalkRouteAsyn(walkRouteQuery);// 异步路径规划步行模式查询
                break;
        }
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int errorCode) {
        dissmissProgressDialog();
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (busRouteResult != null && busRouteResult.getPaths() != null) {
                if (busRouteResult.getPaths().size() > 0) {
                    mBusRouteResult = busRouteResult;
                    BusResultListAdapter mBusResultListAdapter = new BusResultListAdapter(this, mBusRouteResult);
                    mRouteResultList.setAdapter(mBusResultListAdapter);
                } else if (busRouteResult != null && busRouteResult.getPaths() == null) {
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
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int errorCode) {
        dissmissProgressDialog();
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (driveRouteResult != null && driveRouteResult.getPaths() != null) {
                if (driveRouteResult.getPaths().size() > 0) {
                    mDriveRouteResult = driveRouteResult;
                    final DrivePath drivePath = mDriveRouteResult.getPaths().get(0);
                    int dis = (int) drivePath.getDistance();
                    int dur = (int) drivePath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur) + " | " + AMapUtil.getFriendlyLength(dis);
                    mTimeAndKm.setText(des);
                    List<String> driveRoadList = new ArrayList<>();
                    for (int i = 0; i < drivePath.getSteps().size(); i++) {
                        if (!"".equals(drivePath.getSteps().get(i).getRoad())) {
                            driveRoadList.add(drivePath.getSteps().get(i).getRoad());
                        }
                    }
                    String driveRoad = "";
                    if (driveRoadList.size() > 0) {
                        if (driveRoadList.size() == 1) {
                            driveRoad = driveRoadList.get(0);
                        } else {
                            driveRoad = driveRoadList.get(0) + " 和 " + driveRoadList.get(1);
                        }
                    }
                    mLineTv.setText("途径 " + driveRoad);
                    mCarLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(RouteActivity.this, DriveRouteActivity.class);
                            intent.putExtra("slat", slat);
                            intent.putExtra("slng", slng);
                            intent.putExtra("elat", elat);
                            intent.putExtra("elng", elng);
                            intent.putExtra("drive_path", drivePath);
                            intent.putExtra("drive_result", mDriveRouteResult);
                            startActivity(intent);
                        }
                    });
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
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int errorCode) {
        dissmissProgressDialog();
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
            if (walkRouteResult != null && walkRouteResult.getPaths() != null) {
                if (walkRouteResult.getPaths().size() > 0) {
                    mWalkRouteResult = walkRouteResult;
                    final WalkPath walkPath = mWalkRouteResult.getPaths().get(0);
                    int dis = (int) walkPath.getDistance();
                    int dur = (int) walkPath.getDuration();
                    String des = AMapUtil.getFriendlyTime(dur) + " | " + AMapUtil.getFriendlyLength(dis);
                    mTimeAndKm.setText(des);
                    List<String> walkRoadList = new ArrayList<>();
                    for (int i = 0; i < walkPath.getSteps().size(); i++) {
                        if (!"".equals(walkPath.getSteps().get(i).getRoad())) {
                            walkRoadList.add(walkPath.getSteps().get(i).getRoad());
                        }
                    }
                    String walkRoad = "";
                    if (walkRoadList.size() > 0) {
                        if (walkRoadList.size() == 1) {
                            walkRoad = walkRoadList.get(0);
                        } else {
                            walkRoad = walkRoadList.get(0) + " 和 " + walkRoadList.get(1);
                        }
                    }
                    mLineTv.setText("途径 " + walkRoad);
                    mCarLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(RouteActivity.this, WalkRouteActivity.class);
                            intent.putExtra("slat", slat);
                            intent.putExtra("slng", slng);
                            intent.putExtra("elat", elat);
                            intent.putExtra("elng", elng);
                            intent.putExtra("walk_path", walkPath);
                            intent.putExtra("walk_result", mWalkRouteResult);
                            startActivity(intent);
                        }
                    });
                } else if (walkRouteResult != null && walkRouteResult.getPaths() == null) {
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
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int errorCode) {

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
     * 逆地理编码回调
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
        dissmissProgressDialog();
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (regeocodeResult != null && regeocodeResult.getRegeocodeAddress() != null
                    && regeocodeResult.getRegeocodeAddress().getFormatAddress() != null) {
                List<PoiItem> pois = regeocodeResult.getRegeocodeAddress().getPois();
                String addressName = "";
                for (int i = 0; i < pois.size(); i++) {
                    if (pois.get(i).getLatLonPoint().toString().equals(mEndPoint.toString())) {
                        addressName = pois.get(i).toString();
                    }
                }
                mEndLocation.setText(addressName);
            } else {
                Toast.makeText(this, "对不起，没有搜索到相关数据！", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "" + rCode, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 地理编码查询回调
     */
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int rCode) {

    }
}
