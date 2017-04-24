package com.jdyxtech.jindouyunxing.activity;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;


import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.google.gson.Gson;
import com.jdyxtech.jindouyunxing.MainActivity;
import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.javabean.CarBean;
import com.jdyxtech.jindouyunxing.javabean.GetLocals;
import com.jdyxtech.jindouyunxing.javabean.Node;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;
import com.jock.lib.HighLight;
import com.jock.lib.HighLight.OnClickCallback;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupWindow.OnDismissListener;

/**
 * 地图 选车界面
 *  1对 终点（名字） 进行正向地理编码 --> 2发起驾车路线规划（用于获取行程时间）--> 3 initData 获取车场数据
 * @author Tom
 *
 */
public class CarStateMap extends BaseActivity implements OnGetGeoCoderResultListener,OnMarkerClickListener,OnMapClickListener,
									OnClickListener,OnGetRoutePlanResultListener,OnPageChangeListener{
	
	MapView mMapView;
	BaiduMap mBaiduMap;
	private UiSettings mUiSettings;
	public static CarStateMap carStateMapActivity;
	private Double staLat,staLng,endLat,endLng;
	private GeoCoder mSearch2 = null;
	private Boolean flag = false; //flag用于标志 initData()是否成功
	private Boolean flag2 = false; //flag2用于标志 是否defaultHttpClient.getConnectionManager().shutdown()
	private String dstName,city;
	private SharedPreferences sp;
	private ProgressDialog progressDialog;
	private int position=0 , i=0 ; //position:被点击的marker在list中的位置； i:viewPager的currentItem
	
	private HighLight mHightLight;//高亮 引导提示1
	private HighLight mHightLight2;//高亮 引导提示2
	private View view ;
	private PopupWindow  window;
	private ViewPager carViewPager;
	private PagerAdapter pagerAdapter;
	private List<View> viewList = new ArrayList<View>();
	private GetLocals getLocals;
	private List<CarBean> carList;
	private RoutePlanSearch mSearch; //百度路线规划（预估行程的时间用）
	private int pre_dista; //路线规划得到的 行程公里数（整型）
	private DefaultHttpClient defaultHttpClient = MyDefaultHttpClient.getDefaultHttpClient(); //获取自定义的MyDefaultHttpClient 对象
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) { //表示车场匹配成功
				addCarFromList(position); //默认第position=0 个车场为大图标
				drawCarMarkers(staLat, staLng, R.drawable.str_point); //添加用户输入的 起点图标
				if (getLocals.getLists().size()> 0) { //如果在预估距离pre_dista范围内有车 
					//***比例尺调整算法***根据 距离起点位置最远的车场（也就是列表中的最后一个）的距离值来调整地图的比例尺（目的：保证所有车场都出现在视野范围内）
					float zoom1 = setScaleBaseDis(getLocals.getLists().get(getLocals.getLists().size()-1).getLocal().getDis()); 
					// 将输入的起点设置为地图中心点 ,缩放等级为zoom1, 以动画的形式更新地图，动画时长400ms
					mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(
							new MapStatus.Builder().target(new LatLng(staLat, staLng)).zoom(zoom1).build()),400); 
					//默认获取到第position=0个车场 中的可用车辆列表 carList
					carList = getLocals.getLists().get(position).getCarlists();
					creatViews(); //创建该车场中carList.size()个车辆 对应的carList.size()个view ，存放在viewList中（用于viewPager的适配）
					showPopwindow(); //viewPager的创建和适配 在popWinndow中完成
					//*************第一次 使用app 展示功能引导提示***********************
					if (sp.getBoolean("isFirstStart", true)) { // “第一次启动APP”
						findViewById(R.id.button_car_state_switch).postDelayed(new Runnable() {
							@Override
							public void run() {
								showTipMask();
							}
						}, 1000); // 进入页面1s后，展示引导提示信息
					}
				}else { //起点终点距离过远，没有合适的续航里程的车辆
					Toast.makeText(CarStateMap.this, "没有匹配到车辆，请返回重试！", Toast.LENGTH_LONG).show();
				}
				progressDialog.cancel(); //关闭 进度框
			}else if(msg.what == 0){   //表示匹配失败
				progressDialog.cancel(); //关闭 进度框
				Toast.makeText(CarStateMap.this, "抱歉，没有匹配到车辆，请返回重试！", Toast.LENGTH_LONG).show();
			}else if(msg.what == 2){   //网络超时 （连接超时 或 响应超时）
				progressDialog.cancel(); //关闭 进度框
				Toast.makeText(CarStateMap.this, "网络连接超时，请返回重试！", Toast.LENGTH_LONG).show();
			}
		};
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.car_state_map);
		
		initView(); //初始化
//		initData();  //数据初始化，放到了路线规划成功 得到预估里程 那里了
		initCtrl(); //初始化适配器

	}
	
	/**
	 * 现场保护（注意：这并不是Activity的一个生命周期，只是应用在后台运行时，该Activity将要被系统回收掉的时候 会执行这个方法）
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState); //在父类BaseActivity的onSaveInstanceState()方法中，执行了 全局变量myApp的 保存工作
	}
	
	
	/**
	 * 算法：根据距离 动态调整 地图比例尺
	 */
	public static float setScaleBaseDis(int dis) {
		float zoom;
		if (0<dis && dis<=35) {
			zoom = 20;
		}else if (35<dis && dis<=70) {
			zoom = 19;
		}else if (70<dis && dis<=175) {
			zoom = 18;
		}else if (175<dis && dis<=350) {
			zoom = 17;
		}else if (350<dis && dis<=700) {
			zoom = 16;
		}else if (700<dis && dis<=1750) {
			zoom = 15;
		}else if (1750<dis && dis<=4000) {
			zoom = 14;
		}else if (4000<dis && dis<=7000) {
			zoom = 13;
		}else if (7000<dis && dis<=17500) {
			zoom = 12;
		}else if (17500<dis && dis<=35000) {
			zoom = 11;
		}else if (35000<dis && dis<=70000) {
			zoom = 10;
		}else {
			zoom = 10;
		}
		return zoom;
	}

	/**
	 * 初始化适配器的方法（PagerAdapter）
	 */
	private void initCtrl() {
		pagerAdapter = new PagerAdapter() {
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}
			@Override
			public int getCount() {
				return carList.size();
			}
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				container.addView(viewList.get(position));
				return viewList.get(position);
			}
			@Override
			public void destroyItem(ViewGroup container, int position, Object object) {
				container.removeView(viewList.get(position));
			}
		};
	}

	/**
	 * 从服务器加载 起点 车场 数据的方法
	 */
	private void initData() {
		progressDialog.show(); //展示进度框
		//创建post请求对象
		final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/clientlbs/client/getlocals");
        //向服务器上传json格式的登陆数据
        try {
        	JSONObject json = new JSONObject();
			json.put("token",sp.getString("token", ""));
			json.put("lon", staLng);
			json.put("lat", staLat);
			json.put("type","car");
			json.put("step",6);
			json.put("pre_dista",pre_dista); //输入的 起点 到 终点的 预估里程
			StringEntity se = new StringEntity(json.toString(),HTTP.UTF_8);
			se.setContentEncoding(new BasicHeader(HTTP.UTF_8, "application/json"));
			request.setEntity(se);
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}catch (JSONException e1) {
			e1.printStackTrace();
		}
        //开启一个线程，进行post带参网络请求
		new Thread(new Runnable() {
			public void run() {
				try {
					HttpResponse httpResponse = defaultHttpClient.execute(request);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						String result = EntityUtils.toString(httpResponse.getEntity());
						Gson gson = new Gson();
						getLocals = gson.fromJson(result, GetLocals.class);
						if (200 == getLocals.getStatus()) {
							handler.sendEmptyMessage(1); // 匹配车场匹配成功，告诉主线程中的handler进行相应的操作
							flag = true; //标记 数据加载成功
						} else {
							handler.sendEmptyMessage(0); // 匹配失败，告诉主线程中的handler进行相应的操作
						}
					}
				} catch (ConnectTimeoutException e) { //捕获 ：连接超时异常
					handler.sendEmptyMessage(2); //通知handler进行相应的处理
				} catch (SocketTimeoutException e) { //捕获 ：响应超时异常
					handler.sendEmptyMessage(2); //通知handler进行相应的处理
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * 初始化组件的方法
	 */
	private void initView() {
		
		progressDialog = createDialog();
		progressDialog.show(); //展示进度框
		
		carStateMapActivity = this;//初始化 本类实例
		sp = getSharedPreferences("user", MODE_PRIVATE);
		final MyApp myApp  = (MyApp) getApplication();
		staLat  = myApp.getStaLat();
		staLng = myApp.getStaLng();
		dstName = myApp.getDstName();
		city = myApp.getCity();
		
		// 初始化地图
		mMapView = (MapView) findViewById(R.id.bmapView_book_car);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setOnMarkerClickListener(this); //注册marker点击监听
		mBaiduMap.setOnMapClickListener(this);  //注册地图点击监听
		//地图UI类控制
		mUiSettings = mBaiduMap.getUiSettings(); //通过mBaiduMap获取地图 UI 控制器
		mUiSettings.setRotateGesturesEnabled(false); // 关闭 旋转手势（双指旋转）
		mUiSettings.setOverlookingGesturesEnabled(false); //关闭 俯视操作手势（双指下拉）

		//创建驾车线路规划检索实例
		mSearch = RoutePlanSearch.newInstance();
		//设置驾车线路规划检索监听者；
		mSearch.setOnGetRoutePlanResultListener(this);
		
		//初始化地理编码搜索模块（用于正向地理编码）  改为在onResume（）发起地理编码，触发路线规划，再触发initData()
		mSearch2 = GeoCoder.newInstance();
		mSearch2.setOnGetGeoCodeResultListener(this);
		//发起  终点地址 的正向地理编码 检索
		mSearch2.geocode(new GeoCodeOption().city(city).address(dstName)); 
	}
	
	/**
	 * 用于显示从底部弹出的popupWindow（车辆展示的ViewPager）
	 */
	private void showPopwindow() {
	    // 利用layoutInflater获得View
	    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    view = inflater.inflate(R.layout.pop_car_map, null);
	    //车场 与 距离信息
	    TextView parkAddr = (TextView) view.findViewById(R.id.textView1);
	    parkAddr.setText(getLocals.getLists().get(position).getLocal().getAddr()+getLocals.getLists().get(position).getLocal().getTitle());
	    TextView dis = (TextView) view.findViewById(R.id.textView3);
	    dis.setText("（距起点"+String.format("%.2f", getLocals.getLists().get(position).getLocal().getDis()*0.001)+"公里）");
	    
	    //	    if (myApp.isMoreThanOneHour()) { //****************一小时后的订单
//			view.findViewById(R.id.linear2).setVisibility(View.GONE);
//		}else {
			//创建viewPager并绑定适配器
			carViewPager = (ViewPager) view.findViewById(R.id.carViewPager);
			carViewPager.setAdapter(pagerAdapter);
			carViewPager.setOnPageChangeListener(this);
//		}
	    
	    // 下面是两种方法得到宽度和高度 getWindow().getDecorView().getWidth()
	    window = new PopupWindow(view,WindowManager.LayoutParams.MATCH_PARENT,600);
	    // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
	    //window.setFocusable(true);  //此句代码 保证点击window以外的空白处 window消失
	    // 实例化一个ColorDrawable颜色为半透明
	    ColorDrawable dw = new ColorDrawable(0xffffffff);
	    window.setBackgroundDrawable(dw);
	    // 设置popWindow的显示和消失动画
	    window.setAnimationStyle(R.style.mypopwindow_anim_style);
	    // 在底部显示
	    window.showAtLocation(view,Gravity.BOTTOM, 0, 0);
	    
	    //window中按钮的点击监听
	    //--“预约”按钮点击监听
	    TextView book =  (TextView) view.findViewById(R.id.textView2);
	    book.setOnClickListener(new OnClickListener() {
	    	@Override
	    	public void onClick(View v) {
//	    	    if (myApp.isMoreThanOneHour()) { //aaaaaaaaaaaaaaaaaaaaa:一小时后的订单
//	    	    	//一小时之后的订单，预约的是车辆网点
//    				Toast.makeText(CarStateMap.this, "预约时间需在1小时以内！", Toast.LENGTH_SHORT).show();
//    				//startActivity(new Intent(CarStateMap.this,ParkStateMap.class));
//	    		}else { //一小时之内订单 预约的是具体的车辆
					final MyApp myApp = (MyApp) getApplication();
					myApp.setSelectCarBean(carList.get(carViewPager.getCurrentItem()));
					myApp.setStrLoc(getLocals.getLists().get(position).getLocal().getLoc()); // 后期添加：neworder的时候，提交起点停车场的坐标
					startActivity(new Intent(CarStateMap.this,ParkStateMap.class));
//	    		}
	    	}
	    });
	    //--向左的箭头“<”
	    ImageView enter_left =  (ImageView) view.findViewById(R.id.imageView2);
	    enter_left.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (i > 0) {
					i--;
					carViewPager.setCurrentItem(i);		
				}
			}
	    });
	    //--向右的箭头“>”
	    ImageView enter_right =  (ImageView) view.findViewById(R.id.imageView3);
	    enter_right.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (i < carList.size()-1) {
					i++;
					carViewPager.setCurrentItem(i);		
				}
			}
	    });
	    //popWindow消失监听方法
	    window.setOnDismissListener(new OnDismissListener() {
	      @Override
	      public void onDismiss() {
	      }
	    });
	    
	    
	  }

	/**
	 * ViewPager滑动监听回调方法
	 */
	@Override
	public void onPageScrollStateChanged(int arg0) {
	}
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}
	@Override
	public void onPageSelected(int arg0) {
		i = arg0;
	}
	
	/**
	 * 根据carList的size大小，创建size个view（用于viewPager的适配）存放在viewList中
	 */
	public void creatViews() {
		for (int i = 0; i < carList.size(); i++) {
			View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.car_view_pager, null);
			// window中的 车图，picasso网络加载
			ImageView carImageView = (ImageView) view.findViewById(R.id.imageView1);
			Picasso.with(this).load(carList.get(i).getSmall()).into(carImageView);
			// window中的车牌号
			TextView carid_tv = (TextView) view.findViewById(R.id.carid);
			carid_tv.setText(carList.get(i).getCarli());
			// window中的品牌
			TextView carBrand_tv = (TextView) view.findViewById(R.id.textView1);
			carBrand_tv.setText(carList.get(i).getBrand());
			// window中的车型
			TextView carModel_tv = (TextView) view.findViewById(R.id.textView2);
			carModel_tv.setText(carList.get(i).getModel());
			// window中的座位数
			TextView carSeats_tv = (TextView) view.findViewById(R.id.textView3);
			carSeats_tv.setText(carList.get(i).getSeats() + "座");
			// window中的单价费用
			TextView carUnitPrice_tv= (TextView) view.findViewById(R.id.textView6);
			carUnitPrice_tv.setText(carList.get(i).getDis_free()+"元/公里 "+carList.get(i).getMin_free()+"元/分钟"); 
			// window中的续航里程
			TextView carRange_tv = (TextView) view.findViewById(R.id.textView4);
			carRange_tv.setText(carList.get(i).getRange() +"公里");
			// window中的 最高时速
			TextView carHeghtSpeed = (TextView) view.findViewById(R.id.textView5);
			carHeghtSpeed.setText(carList.get(i).getHighspeed() + "公里/小时");
			//点击车辆信息，进入车辆详情界面 
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CarBean carBean = carList.get(carViewPager.getCurrentItem());
					Intent intent = new Intent(CarStateMap.this, CarDetil.class);
					intent.putExtra("carBean", carBean);
					intent.putExtra("strLoc", (Serializable) getLocals.getLists().get(position).getLocal().getLoc());
					startActivity(intent);
				}
			});
			viewList.add(view);
		}

	}
	
	/**
	 * 地图Marker点击回调方法
	 */
	@Override
	public boolean onMarkerClick(Marker marker) {
		if (marker.getPosition().latitude == staLat && marker.getPosition().longitude == staLng) {
			setMapCenter(staLat, staLng);//将起点设置为 地图中心点
		} else {
			if (window != null && window.isShowing()) {
				window.dismiss();
			}
			Bundle bundle = marker.getExtraInfo();
			position = bundle.getInt("position"); // position表示marker（车场）在getLocals.getLists()中的位置下标
			mBaiduMap.clear();// 清空地图
			addCarFromList(position); // 重新添加一遍标注点，position处的的marker用大图
			drawCarMarkers(staLat, staLng, R.drawable.str_point); //先添加marker 后添加用户输入的目的地图标，保证起点图标在最上方
			setMapCenter(marker.getPosition().latitude, marker.getPosition().longitude); // 将被点击的marker设置为地图的中心点
			// 获取到 点击第position个车场 中的可用车辆列表 carList
			carList = getLocals.getLists().get(position).getCarlists();
			viewList.clear(); // 先清空viewList（不然viewList会越添加越多）
			creatViews(); // 创建该车场中carList.size()个车辆 对应的carList.size()个view，存放在viewList中（用于viewPager的适配）
			showPopwindow(); // viewPager的创建和适配 在popWinndow中完成
		}
		return false;
	}

	/**
	 * 从getLocals.getLists()中获取车场信息，并标注在地图上
	 */
	private void addCarFromList(int position) {
		for (int i = 0; i < getLocals.getLists().size(); i++) {
			if (i == position) { // posiotion：被点击的marker ,用大图标
//					if (((MyApp)getApplication()).isMoreThanOneHour()) { //aaaaaaaaaaaaaaaaaaaa一小时后的订单
//						drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
//								getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.car_large);
//					} else {// 一小时之内的订单，车辆网点的图标 要 根据车辆网点的实际车辆数 来添加
						switch (getLocals.getLists().get(i).getLocal().getCartotal()) {
						case 0:
							drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
									getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.car_large0);
							break;
						case 1:
							drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
									getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.car_large1);
							break;
						case 2:
							drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
									getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.car_large2);
							break;
						case 3:
							drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
									getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.car_large3);
							break;
						case 4:
							drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
									getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.car_large4);
							break;
						case 5:
							drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
									getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.car_large5);
							break;
						default:
							break;
						}
//					}
				} else { //其他没被点击的 用小图标
//					if (((MyApp)getApplication()).isMoreThanOneHour()) {
//						drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
//								getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.car_small);
//					}else {
						switch (getLocals.getLists().get(i).getLocal().getCartotal()) {
						case 0:
							drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
									getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.car_small0);
							break;
						case 1:
							drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
									getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.car_small1);
							break;
						case 2:
							drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
									getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.car_small2);
							break;
						case 3:
							drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
									getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.car_small3);
							break;
						case 4:
							drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
									getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.car_small4);
							break;
						case 5:
							drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
									getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.car_small5);
							break;
						default:
							break;
						}
//					}
				}
			}
	}
	
	/**
	 * 创建 “正在匹配车辆” 的ProgressDialog对话框的 方法
	 */
	public ProgressDialog createDialog() {
		ProgressDialog mypDialog=new ProgressDialog(this);
		mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mypDialog.setTitle("匹配车辆");
		mypDialog.setMessage("正在匹配合适的车辆，请您耐心等候...");
		mypDialog.setIndeterminate(false);
		mypDialog.setCancelable(true);
		return mypDialog;
	}
	/**
	 * 地图点击监听 及 地图中POI点击监听
	 */
	@Override
	public void onMapClick(LatLng arg0) {
		if (window!=null&&window.isShowing()) {
			window.dismiss();
		}
	}
	@Override
	public boolean onMapPoiClick(MapPoi arg0) {
		if (window!=null&&window.isShowing()) {
			window.dismiss();
		}
		return false;
	}
	
	/**
	 * 正向地理编码的 监听回调方法(终点搜索用)
	 */
	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(CarStateMap.this, "终点检索失败，请重试！", Toast.LENGTH_LONG).show();
			finish(); //关闭页面，回首页
			return;
		}
		endLat = result.getLocation().latitude;
		endLng = result.getLocation().longitude;
		//正向地理编码成功后，将终点的坐标信息写入全局变量中去
		final MyApp myApp = (MyApp) getApplication(); 
		myApp.setEndLat(endLat); 
		myApp.setEndLng(endLng);
		//***终点正向地理编码成功后，发起路线规划（用于获得行程耗时,起点——>终点预估里程）
		//准备检索起、终点信息；
		PlanNode stNode = PlanNode.withLocation(new LatLng(myApp.getStaLat(),myApp.getStaLng() ));  
		PlanNode enNode = PlanNode.withLocation(new LatLng(endLat,endLng));
		//发起 驾车 线路规划检索；
		mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));
	}
	//反向地理编码
	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * 百度路线规划 监听回调方法
	 */
	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR||result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            Toast.makeText(CarStateMap.this, "检索失败，请返回重试", Toast.LENGTH_SHORT).show();
			finish(); //关闭页面，回首页
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
        	pre_dista = (int)(result.getRouteLines().get(0).getDistance()/1000); //百度路线规划结果中得出行程的公里数 
        	if (!flag2) { //connection没有shutdown，才能执行initData(),如果已经shutdown了 还去执行initData()会报错：connectionManager has shutdown
        		initData(); //得到 预估里程pre_dista之后，才初始化data,因为提交参数的时候需要 pre_dist这个值
			}
        }
	}
	@Override
	public void onGetTransitRouteResult(TransitRouteResult arg0) {
	}
	@Override
	public void onGetWalkingRouteResult(WalkingRouteResult arg0) {
	}
	
	/**
	 * 将某个点 调整为地图的中心点
	 */
	public void setMapCenter(Double lat,Double lng) {
		// 中心点
		LatLng ll = new LatLng(lat,lng);
		// 定义地图状态
		MapStatus mMapStatus = new MapStatus.Builder().target(ll).build();
		// 定义MapStatusUpdate对象，以便描述地图将要发生的状态变化
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
		// 通过setMapStatus(MapState)方法，更新地图状态
		mBaiduMap.animateMapStatus(mMapStatusUpdate,400); //以动画的形式更新地图，动画时长400ms
	}
	
	/**
	 * *此方法：在地图的指定坐标上 添加 标注点
	 */
	public void drawParkMarkers(Double lat, Double lng,int position,int drawableId) {
		// 定义Maker坐标点
		LatLng point = new LatLng(lat, lng);
		// 构建Marker图标
		BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(drawableId);
		//marker中需要添加的 附加信息：此marker在list中的position
		Bundle bundle = new Bundle();
		bundle.putInt("position", position);
		// 构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().position(point).icon(bitmap).extraInfo(bundle);
		// 在地图上添加Marker，并显示
		mBaiduMap.addOverlay(option);
	}
	
	/**
	 * *此方法：在地图的指定坐标上 添加 标注点（无position附加信息）
	 */
	public void drawCarMarkers(Double lat, Double lng,int drawableId) {
		// 定义Maker坐标点
		LatLng point = new LatLng(lat, lng);
		// 构建Marker图标
		BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(drawableId);
		// 构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
		// 在地图上添加Marker，并显示
		mBaiduMap.addOverlay(option);
	}
	
	/**
	 * 地图生命周期  与 Activity 同步
	 */
	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
	}
	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	//点击事件监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back:
			defaultHttpClient.getConnectionManager().shutdown();//关闭当前页面的 网络连接
			flag2 = true;
			finish();
			break;
		case R.id.button_car_state_switch:
			if (flag) { // flag=true表示getData()成功，预防空指针：在未加载出来列表数据时，点击列表按钮造成的崩溃
				Intent intent = new Intent(CarStateMap.this, CarNodeList.class);
				List<Node> carNode_list = getLocals.getLists();
				intent.putExtra("node_list", (Serializable) carNode_list);
				startActivity(intent);
			}else {
				Toast.makeText(CarStateMap.this, "列表数据加载中，请稍候！", Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}

	//返回键
	@Override
	public void onBackPressed() {
		defaultHttpClient.getConnectionManager().shutdown();//关闭当前页面的 网络连接
		flag2 = true;
		finish();
	}
	
	
	/**
	 * 第一次使用APP：通过该方法 进行 功能引导提示
	 */
	private void showTipMask() {
		// 创建第一个tip （切换列表tip）
		mHightLight = new HighLight(CarStateMap.this)
				// .anchor(view)// 如果是Activity上增加引导层，不需要设置anchor
				.maskColor(R.color.grey)
				.addHighLight(R.id.button_car_state_switch, R.layout.tip_carstate_map1, new HighLight.OnPosCallback() {
					@Override
					public void getPos(float rightMargin, float bottomMargin, RectF rectF,HighLight.MarginInfo marginInfo) {
						marginInfo.rightMargin = rightMargin+rectF.width()/4;
						marginInfo.topMargin = rectF.top + rectF.height()/2;
					}
				});
		
		// 创建第二个tip（查看车辆详情tip）
		mHightLight2 = new HighLight(CarStateMap.this)
				.anchor(view)// 如果是Activity上增加引导层，不需要设置anchor
				.maskColor(R.color.grey)
				.addHighLight(R.id.carViewPager, R.layout.tip_carstate_map2, new HighLight.OnPosCallback() {
					@Override
					public void getPos(float rightMargin, float bottomMargin, RectF rectF,HighLight.MarginInfo marginInfo) {
						marginInfo.leftMargin = rectF.left;
						marginInfo.bottomMargin = bottomMargin + rectF.height();
					}
				});
		//给第二个tip设置点击监听
		mHightLight2.setClickCallback(new OnClickCallback() {
			@Override
			public void onClick() {
				mHightLight.remove();
			}
		});
		
		// show第一个tip
		mHightLight.show();
		//show第二个tip
		mHightLight2.show();
	}

	
}
