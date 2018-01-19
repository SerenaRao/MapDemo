package com.hzcz.mapdemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.hzcz.mapdemo.R;

/**
 * Created by Serena on 2017/6/27.
 */

public class MapActivity extends Activity implements AMap.OnMyLocationChangeListener, View.OnClickListener, AMap.InfoWindowAdapter {

    private Button bus;
    private Button car;
    private Button foot;
    private MapView mMapView = null;
    private AMap aMap;
    private LatLng latLng;
    private MyLocationStyle myLocationStyle;
    private MarkerOptions markerOption;
    private View infoWindow = null;
    private double slat;
    private double slng;
    private double elat;
    private double elng;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mMapView = (MapView) findViewById(R.id.map);
        bus = (Button) findViewById(R.id.bus);
        car = (Button) findViewById(R.id.car);
        foot = (Button) findViewById(R.id.foot);
        elat = getIntent().getDoubleExtra("lat", 0.00);
        elng = getIntent().getDoubleExtra("lng", 0.00);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        //初始化地图控制器对象
        if (aMap == null) {
            aMap = mMapView.getMap();
            setUpMap();
        }
        latLng = new LatLng(elat, elng);
        markerOption = new MarkerOptions().icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .position(latLng)
                .title("商家地址")
                .snippet("杭州嘉里中心")
                .draggable(true);
        //修改中心点为商家的位置(红点展示)
        aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, 13, 0, 0)));
        aMap.clear();
        aMap.setInfoWindowAdapter(this);
        aMap.addMarker(markerOption).showInfoWindow();

        bus.setOnClickListener(this);
        car.setOnClickListener(this);
        foot.setOnClickListener(this);
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        //如果要设置定位的默认状态，可以在此处进行设置
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.showMyLocation(true);
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        //定位一次，且将视角移动到地图中心点。
        aMap.setMyLocationStyle(myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW));

        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        //控制比例尺控件是否显示
        aMap.getUiSettings().setScaleControlsEnabled(true);

        //设置SDK 自带定位消息监听
        aMap.setOnMyLocationChangeListener(this);
    }

    @Override
    public void onMyLocationChange(Location location) {
        // 定位回调监听
        if (location != null) {
            Log.e("amap", "onMyLocationChange 定位成功， lat: " + location.getLatitude() + " lon: " + location.getLongitude());
            slat = location.getLatitude();
            slng = location.getLongitude();
            Bundle bundle = location.getExtras();
            if (bundle != null) {
                int errorCode = bundle.getInt(MyLocationStyle.ERROR_CODE);
                String errorInfo = bundle.getString(MyLocationStyle.ERROR_INFO);
                // 定位类型，可能为GPS WIFI等，具体可以参考官网的定位SDK介绍
                int locationType = bundle.getInt(MyLocationStyle.LOCATION_TYPE);
                /*
                errorCode
                errorInfo
                locationType
                */
                Log.e("amap", "定位信息， code: " + errorCode + " errorInfo: " + errorInfo + " locationType: " + locationType);
            } else {
                Log.e("amap", "定位信息， bundle is null ");
            }
        } else {
            Log.e("amap", "定位失败");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bus://公交出行线路
                Intent intent1 = new Intent(MapActivity.this, RouteActivity.class);
                intent1.putExtra("slat", slat);
                intent1.putExtra("slng", slng);
                intent1.putExtra("elat", elat);
                intent1.putExtra("elng", elng);
                intent1.putExtra("route", 1);
                startActivity(intent1);
                break;
            case R.id.car://驾车出行线路
                Intent intent2 = new Intent(MapActivity.this, RouteActivity.class);
                intent2.putExtra("slat", slat);
                intent2.putExtra("slng", slng);
                intent2.putExtra("elat", elat);
                intent2.putExtra("elng", elng);
                intent2.putExtra("route", 2);
                startActivity(intent2);
                break;
            case R.id.foot://步行出行线路
                Intent intent3 = new Intent(MapActivity.this, RouteActivity.class);
                intent3.putExtra("slat", slat);
                intent3.putExtra("slng", slng);
                intent3.putExtra("elat", elat);
                intent3.putExtra("elng", elng);
                intent3.putExtra("route", 3);
                startActivity(intent3);
                break;
        }
    }

    /**
     * 监听自定义infowindow窗口的infocontents事件回调
     */
    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    /**
     * 监听自定义infowindow窗口的infowindow事件回调
     */
    @Override
    public View getInfoWindow(Marker marker) {
        if (infoWindow == null) {
            infoWindow = LayoutInflater.from(this).inflate(R.layout.custom_info_window, null);
        }
        render(marker, infoWindow);
        return infoWindow;
    }

    /**
     * 自定义infowinfow窗口
     */
    public void render(Marker marker, View view) {
        //如果想修改自定义Infow中内容，请通过view找到它并修改
        String title = marker.getTitle();
        TextView titleUi = ((TextView) view.findViewById(R.id.title));
        if (title != null) {
            SpannableString titleText = new SpannableString(title);
            titleText.setSpan(new ForegroundColorSpan(Color.RED), 0,
                    titleText.length(), 0);
            titleUi.setTextSize(15);
            titleUi.setText(titleText);
        } else {
            titleUi.setText("");
        }
        String snippet = marker.getSnippet();
        TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
        if (snippet != null) {
            SpannableString snippetText = new SpannableString(snippet);
            snippetText.setSpan(new ForegroundColorSpan(Color.GREEN), 0,
                    snippetText.length(), 0);
            snippetUi.setTextSize(20);
            snippetUi.setText(snippetText);
        } else {
            snippetUi.setText("");
        }
    }
}
