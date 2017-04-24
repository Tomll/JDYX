package com.jdyxtech.jindouyunxing.activity;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.utils.WalkingRouteOverlay;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Toast;

/**
 * 用于展示：取车的步行路线规划（前往车辆停放位置的路线规划图）
 * @author Tom
 *
 */
public class GetCarWalkRoutePlan extends BaseActivity implements OnClickListener,BDLocationListener,OnGetRoutePlanResultListener{
	
	// 定位相关（取车界面的定位功能，只需定位 无需展示地图）
	LocationClient mLocClient;
	private Double myLat, myLng;
	//地图
	private UiSettings uiSettings;//地图ui控制器
	private ProgressDialog progressDialog;
	MapView mMapView;
	BaiduMap mBaiduMap;
	//用于步行路线规划
	private RoutePlanSearch mSearch;
	private WalkingRouteLine walkingRouteLine;
	private WalkingRouteOverlay walkingRouteOverlay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.get_car_walk_rout_plan);
		
		initView();
	}

	/**
	 * 现场保护（注意：这并不是Activity的一个生命周期，只是应用在后台运行时，该Activity将要被系统回收掉的时候 会执行这个方法）
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState); //在父类BaseActivity的onSaveInstanceState()方法中，执行了 全局变量myApp的 保存工作
	}
	
	/**
	 * 初始化view
	 */
	private void initView() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("正在查找取车路线...");
		progressDialog.show();
		//初始化地图
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(this);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(60*1000); // 设置定位时间间隔 
		mLocClient.setLocOption(option);
		mLocClient.start();
		
		//创建驾车线路规划检索实例
		mSearch = RoutePlanSearch.newInstance();
		//设置驾车线路规划检索监听者；
		mSearch.setOnGetRoutePlanResultListener(this);
		
	}
	
	/**
	 * 定位监听回调
	 */
	@Override
	public void onReceiveLocation(BDLocation location) {
		// map view 销毁后不在处理新接收的位置
		if (location == null) {
			return;
		}
		// 将获取到的 定位信息 赋值给myLat 、myLng
		myLat = location.getLatitude();
		myLng = location.getLongitude();	
		//展示定位点
		MyLocationData locData = new MyLocationData.Builder().latitude(location.getLatitude()).longitude(location.getLongitude()).build();
		mBaiduMap.setMyLocationData(locData);
		strRoutePlan(); //开始进行 步行路线规划
		
	}
	
	/**
	 * 路线规划 初始化
	 */
	private void strRoutePlan() {
		final MyApp myApp  = (MyApp) getApplication();
		LatLng myLatLng = new LatLng(myLat, myLng); //我的当前位置 bd09坐标点
		LatLng carLatLng = new LatLng(myApp.getStrLoc().get(1),myApp.getStrLoc().get(0)); //车辆所在停车场的 bd09坐标点
		//路线规划检索用的：起、终点信息；
		PlanNode stNode = PlanNode.withLocation(myLatLng);  
		PlanNode enNode = PlanNode.withLocation(carLatLng);
		//发起 步行 线路规划检索；
		mSearch.walkingSearch((new WalkingRoutePlanOption()).from(stNode).to(enNode));		
	}
	
	/**
	 * 路线规划 监听回调
	 */
	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult arg0) {
	}
	@Override
	public void onGetTransitRouteResult(TransitRouteResult arg0) {
	}
	@Override
	public void onGetWalkingRouteResult(WalkingRouteResult result) {
	    if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {  
	        //未找到结果  
            Toast.makeText(GetCarWalkRoutePlan.this, "抱歉，未获取到步行路线！", Toast.LENGTH_SHORT).show();
	        return;  
	    }  
	    if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {  
	        //起终点或途经点地址有岐义，通过以下接口获取建议查询信息  
	        //result.getSuggestAddrInfo()  
	        return;  
	    }  
	    if (result.error == SearchResult.ERRORNO.NO_ERROR) {  
	    	walkingRouteLine = result.getRouteLines().get(0);  
	        //创建步行路线规划线路覆盖物   
	    	walkingRouteOverlay = new WalkingRouteOverlay(mBaiduMap);  
	        //设置步行路线规划数据     
	    	walkingRouteOverlay.setData(walkingRouteLine);  
	    	//先清除地图上的覆盖物
	    	mBaiduMap.clear();
	        //将步行路线规划覆盖物添加到地图中  
	    	walkingRouteOverlay.addToMap();  
	    	walkingRouteOverlay.zoomToSpan();  
	    	progressDialog.dismiss();
	   } 
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 关闭搜索
		mSearch.destroy();
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
	}
	
	//点击监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back:
			finish();
			break;
		default:
			break;
		}
	}

}
