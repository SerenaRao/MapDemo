package com.hzcz.mapdemo.activity;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.model.RouteOverlayOptions;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.hzcz.mapdemo.R;
import com.hzcz.mapdemo.util.TTSController;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * des:驾车导航
 * Created by Serena on 2017/6/29.
 */

public class DriveRouteCalculateActivity extends Activity implements AMapNaviViewListener, AMapNaviListener {

    @BindView(R.id.navi_view)
    AMapNaviView mNaviView;

    private double slat;
    private double slng;
    private double elat;
    private double elng;
    private NaviLatLng mStartPoint;//起点
    private NaviLatLng mEndPoint;//终点
    protected TTSController mTtsManager;
    protected AMapNavi mAMapNavi;
    protected final List<NaviLatLng> sList = new ArrayList<NaviLatLng>();
    protected final List<NaviLatLng> eList = new ArrayList<NaviLatLng>();
    protected List<NaviLatLng> mWayPointList;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_base_navi);
        ButterKnife.bind(this);
        slat = getIntent().getDoubleExtra("slat", 0.00);
        slng = getIntent().getDoubleExtra("slng", 0.00);
        elat = getIntent().getDoubleExtra("elat", 0.00);
        elng = getIntent().getDoubleExtra("elng", 0.00);
        mStartPoint = new NaviLatLng(slat, slng);//起点
        mEndPoint = new NaviLatLng(elat, elng);//终点

        //实例化语音引擎
        mTtsManager = TTSController.getInstance(this);
        mTtsManager.init();

        mAMapNavi = AMapNavi.getInstance(this);
        mAMapNavi.addAMapNaviListener(this);
        mAMapNavi.addAMapNaviListener(mTtsManager);

        sList.add(mStartPoint);
        eList.add(mEndPoint);

        mNaviView.onCreate(bundle);
        mNaviView.setAMapNaviViewListener(this);

        AMapNaviViewOptions options = mNaviView.getViewOptions();
        options.setTilt(0);
        //options.setLayoutVisible(false);
        //自定义图标
        options.setCarBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.car));
        options.setFourCornersBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.lane00));
        options.setStartPointBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.navi_start));
        options.setWayPointBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.navi_way));
        options.setEndPointBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.navi_end));

        //自定义路线纹理
        RouteOverlayOptions routeOverlayOptions = new RouteOverlayOptions();
        routeOverlayOptions.setArrowOnTrafficRoute(BitmapFactory.decodeResource(getResources(), R.drawable.custtexture_aolr));
        routeOverlayOptions.setNormalRoute(BitmapFactory.decodeResource(getResources(), R.drawable.custtexture));
        routeOverlayOptions.setUnknownTraffic(BitmapFactory.decodeResource(getResources(), R.drawable.custtexture_no));
        routeOverlayOptions.setSmoothTraffic(BitmapFactory.decodeResource(getResources(), R.drawable.custtexture_green));
        routeOverlayOptions.setSlowTraffic(BitmapFactory.decodeResource(getResources(), R.drawable.custtexture_slow));
        routeOverlayOptions.setJamTraffic(BitmapFactory.decodeResource(getResources(), R.drawable.custtexture_bad));
        routeOverlayOptions.setVeryJamTraffic(BitmapFactory.decodeResource(getResources(), R.drawable.custtexture_grayred));
        options.setRouteOverlayOptions(routeOverlayOptions);

        mNaviView.setViewOptions(options);
        //设置车头向上模式
        mNaviView.setNaviMode(AMapNaviView.CAR_UP_MODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNaviView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNaviView.onPause();

        //        仅仅是停止你当前在说的这句话，一会到新的路口还是会再说的
        mTtsManager.stopSpeaking();
        //
        //        停止导航之后，会触及底层stop，然后就不会再有回调了，但是讯飞当前还是没有说完的半句话还是会说完
        //        mAMapNavi.stopNavi();
    }

    @Override
    public void onInitNaviSuccess() {
        //初始化成功
        /**
         * 方法: int strategy=mAMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, multipleroute); 参数:
         *
         * @congestion 躲避拥堵
         * @avoidhightspeed 不走高速
         * @cost 避免收费
         * @hightspeed 高速优先
         * @multipleroute 多路径
         *
         *  说明: 以上参数都是boolean类型，其中multipleroute参数表示是否多条路线，如果为true则此策略会算出多条路线。
         *  注意: 不走高速与高速优先不能同时为true 高速优先与避免收费不能同时为true
         */
        int strategy = 0;
        try {
            //再次强调，最后一个参数为true时代表多路径，否则代表单路径
            strategy = mAMapNavi.strategyConvert(true, false, false, false, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAMapNavi.calculateDriveRoute(sList, eList, mWayPointList, strategy);
    }

    @Override
    public void onInitNaviFailure() {
        //初始化失败
    }

    @Override
    public void onNaviSetting() {
        //底部导航设置点击回调
    }

    @Override
    public void onNaviCancel() {
        finish();
    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }

    @Override
    public void onNaviMapMode(int i) {
        //地图的模式，锁屏或锁车
    }

    @Override
    public void onNaviTurnClick() {
        //转弯view的点击回调
    }

    @Override
    public void onNextRoadClick() {
        //下一个道路View点击回调
    }

    @Override
    public void onScanViewButtonClick() {
        //全览按钮点击回调
    }

    @Override
    public void onLockMap(boolean b) {
        //锁地图状态发生变化时回调
    }

    @Override
    public void onNaviViewLoaded() {
        Log.d("wlx", "导航页面加载成功");
        Log.d("wlx", "请不要使用AMapNaviView.getMap().setOnMapLoadedListener();会overwrite导航SDK内部画线逻辑");
    }

    @Override
    public void onStartNavi(int i) {
        //开始导航回调
    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
        //当前位置回调
    }

    @Override
    public void onGetNavigationText(int i, String s) {
        //播报类型和播报文字回调
    }

    @Override
    public void onEndEmulatorNavi() {
        //结束模拟导航
    }

    @Override
    public void onArriveDestination() {
        //到达目的地
    }

    @Override
    public void onCalculateRouteSuccess() {
        //路线计算成功
        mAMapNavi.startNavi(NaviType.GPS);
    }

    @Override
    public void onCalculateRouteFailure(int errorInfo) {
        //路线计算失败
        Toast.makeText(this, "errorInfo：" + errorInfo, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onReCalculateRouteForYaw() {
        //偏航后重新计算路线回调
    }

    @Override
    public void onReCalculateRouteForTrafficJam() {
        //拥堵后重新计算路线回调
    }

    @Override
    public void onArrivedWayPoint(int i) {
        //到达途径点
    }

    @Override
    public void onGpsOpenStatus(boolean b) {
        //GPS开关状态回调
    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {
        //导航过程中的信息更新，请看NaviInfo的具体说明
    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {
        //过时
    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {
        //显示转弯回调
    }

    @Override
    public void hideCross() {
        //隐藏转弯回调
    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {
        //显示车道信息
    }

    @Override
    public void hideLaneInfo() {
        //隐藏车道信息
    }

    @Override
    public void onCalculateMultipleRoutesSuccess(int[] ints) {
        //多路径算路成功回调
    }

    @Override
    public void notifyParallelRoad(int i) {
        if (i == 0) {
            Toast.makeText(this, "当前在主辅路过渡", Toast.LENGTH_SHORT).show();
            Log.d("wlx", "当前在主辅路过渡");
            return;
        }
        if (i == 1) {
            Toast.makeText(this, "当前在主路", Toast.LENGTH_SHORT).show();

            Log.d("wlx", "当前在主路");
            return;
        }
        if (i == 2) {
            Toast.makeText(this, "当前在辅路", Toast.LENGTH_SHORT).show();

            Log.d("wlx", "当前在辅路");
        }
    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {
        //已过时
    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {
        //已过时
    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {
        //更新交通设施信息
    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {
        //更新巡航模式的统计信息
    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {
        //更新巡航模式的拥堵信息
    }

    @Override
    public void onPlayRing(int i) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNaviView.onDestroy();
        //since 1.6.0 不再在naviview destroy的时候自动执行AMapNavi.stopNavi();请自行执行
        mAMapNavi.stopNavi();
        mAMapNavi.destroy();
        mTtsManager.destroy();
    }
}
