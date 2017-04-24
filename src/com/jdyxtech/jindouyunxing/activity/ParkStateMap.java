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

import com.baidu.location.LocationClient;
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
import com.google.gson.Gson;
import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.javabean.CarBean;
import com.jdyxtech.jindouyunxing.javabean.GetLocals;
import com.jdyxtech.jindouyunxing.javabean.Node;
import com.jdyxtech.jindouyunxing.javabean.ParkBean;
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
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupWindow.OnDismissListener;
/**
 * 地图 选车位 界面
 * @author Tom
 *
 */
public class ParkStateMap extends BaseActivity implements OnMarkerClickListener,OnMapClickListener,OnClickListener,OnPageChangeListener {
	
	public static ParkStateMap parkStateMapActivity;
	// 地图&定位相关
	LocationClient mLocClient;
	BitmapDescriptor mCurrentMarker;
	private Double endLat, endLng;
	MapView mMapView;
	BaiduMap mBaiduMap;
	private UiSettings mUiSettings;
	private ImageView carImageView;
	private TextView tv_carBrand,tv_carModle,tv_carSeat,tv_carli,tv_power,tv_dis;

	private HighLight mHightLight;//高亮 引导提示1
	private HighLight mHightLight2;//高亮 引导提示2
	private View view; //popWindow对应的 布局 
	private GetLocals getLocals;
	private SharedPreferences sp;
	private ProgressDialog progressDialog;
	private CarBean selectCarBean;
	private int position=0 , i=0 ; //position:被点击的marker在list中的位置； i:viewPager的currentItem
	private PopupWindow  window;
	private List<View> viewList = new ArrayList<View>();
	private List<ParkBean> parkList;
	private ViewPager parkViewPager;
	private PagerAdapter pagerAdapter;
	private DefaultHttpClient defaultHttpClient = MyDefaultHttpClient.getDefaultHttpClient(); 
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1&&endLat!=null&&endLng!=null&&mMapView!=null) { //表示车场匹配成功
				addParkFromList(position); //默认第position=0（据我最近） 个车场为大图标
				drawParkMarkers(endLat, endLng, R.drawable.end_point); //添加用户输入的 起点图标
				//zoom2:根据距离动态调整地图的比例尺，让全部网点都在视野范围内
				float zoom2 = CarStateMap.setScaleBaseDis(getLocals.getLists().get(getLocals.getLists().size()-1).getLocal().getDis());
				// 将输入的起点设置为地图中心点 ,缩放等级为zoom2, 以动画的形式更新地图，动画时长400ms
				mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(
						new MapStatus.Builder().target(new LatLng(endLat, endLng)).zoom(zoom2).build()),400); 
				//默认获取到第position=0个车场 中的可用停车位列表 parkList
				parkList = getLocals.getLists().get(position).getParklists();
				creatViews(); //创建该车场中parkList.size()个车辆 对应的parkList.size()个view ，存放在viewList中（用于viewPager的适配）
				showPopwindow(); //viewPager的创建和适配 在popWinndow中完成
				//*****************第一次使用App 引导提示*******************
				if (sp.getBoolean("isFirstStart", true)) {
					findViewById(R.id.del_car).postDelayed((new Runnable() {
						@Override
						public void run() {
							showTipMask();
						}
					}), 1000);
				}
				progressDialog.cancel(); //关闭 进度框
			}else if(msg.what == 0){   			//表示匹配失败
				progressDialog.cancel(); //关闭 进度框
				Toast.makeText(ParkStateMap.this, "抱歉，没有匹配到车位，请返回重试！", Toast.LENGTH_LONG).show();
			}else if(msg.what == 2){   //网络超时 （连接超时 或 响应超时）
				progressDialog.cancel(); //关闭 进度框
				Toast.makeText(ParkStateMap.this, "网络连接超时，请返回重试！", Toast.LENGTH_LONG).show();
			}

		};
	};	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.park_state_map);
		
		progressDialog = createDialog();
		sp = getSharedPreferences("user", MODE_PRIVATE);
		
		final MyApp myApp  = (MyApp) getApplication();
		endLat  = myApp.getEndLat();
		endLng = myApp.getEndLng();
		selectCarBean = myApp.getSelectCarBean();
		
		initView();
		initData();
		initCtrl();
		onBund(); //将 (已选定车辆)数据适配到控件上去

	}
	
	/**
	 * 现场保护（注意：这并不是Activity的一个生命周期，只是应用在后台运行时，该Activity将要被系统回收掉的时候 会执行这个方法）
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState); //在父类BaseActivity的onSaveInstanceState()方法中，执行了 全局变量myApp的 保存工作
	}
	
	/**
	 * 初始化组件的方法
	 */
	private void initView() {
		parkStateMapActivity = this;
		// 初始化地图
		mMapView = (MapView) findViewById(R.id.bmapView_book_park);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setOnMarkerClickListener(this); // 注册marker点击监听
		mBaiduMap.setOnMapClickListener(this); // 注册地图点击监听
		// 地图 UI 控制
		mUiSettings = mBaiduMap.getUiSettings(); // 通过mBaiduMap获取地图 UI 控制器
		mUiSettings.setRotateGesturesEnabled(false); // 关闭 旋转手势（双指旋转）
		mUiSettings.setOverlookingGesturesEnabled(false); //关闭 俯视操作手势（双指下拉）
		
		carImageView = (ImageView) findViewById(R.id.imageView1);
		tv_carBrand = (TextView) findViewById(R.id.textView1);
		tv_carModle = (TextView) findViewById(R.id.textView2);
		tv_carSeat = (TextView) findViewById(R.id.textView3);
		tv_carli = (TextView) findViewById(R.id.textView4);
		tv_power = (TextView) findViewById(R.id.textView6);
		tv_dis = (TextView) findViewById(R.id.textView7);

	}

	/**
	 * 从服务器加载 终点 车场 数据的方法
	 */
	private void initData() {
		progressDialog.show(); //展示进度框
		// 创建post请求对象
		final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/clientlbs/client/getlocals");
		// 向服务器上传json格式的登陆数据
		try {
			JSONObject json = new JSONObject();
			json.put("token", sp.getString("token", ""));
			json.put("lon", endLng);
			json.put("lat", endLat);
			json.put("type", "park");
			json.put("step", 5);
			json.put("pre_dista",1); 
			StringEntity se = new StringEntity(json.toString(), HTTP.UTF_8);
			se.setContentEncoding(new BasicHeader(HTTP.UTF_8, "application/json"));
			request.setEntity(se);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		// 开启一个线程，进行post带参网络请求（登录）
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpResponse httpResponse = defaultHttpClient.execute(request);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						String result = EntityUtils.toString(httpResponse.getEntity());
						Gson gson = new Gson();
						getLocals = gson.fromJson(result, GetLocals.class);
						if (200 == getLocals.getStatus()) {
							handler.sendEmptyMessage(1); // 匹配车场匹配成功，告诉主线程中的handler进行相应的操作
						} else {
							handler.sendEmptyMessage(0); // 匹配失败，告诉主线程中的handler进行相应的操作
						}
					}
				} catch (ConnectTimeoutException e) { // 捕获 ：连接超时异常
					handler.sendEmptyMessage(2); // 通知handler进行相应的处理
				} catch (SocketTimeoutException e) { // 捕获 ：响应超时异常
					handler.sendEmptyMessage(2); // 通知handler进行相应的处理
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
	 * 初始化适配器的方法
	 */
	private void initCtrl() {
		pagerAdapter = new PagerAdapter() {
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}
			@Override
			public int getCount() {
				return parkList.size();
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
	
	//已选定的车辆信息的展示
	private void onBund() {
		Picasso.with(this).load(selectCarBean.getSmall()).into(carImageView);
		tv_carBrand.setText(selectCarBean.getBrand());
		tv_carModle.setText(selectCarBean.getModel());
		tv_carSeat.setText(selectCarBean.getSeats()+"座");
		tv_carli.setText(selectCarBean.getCarli());
		tv_power.setText("车辆续航里程"+selectCarBean.getRange()+"公里");
		tv_dis.setText("距起点"+String.format("%.2f", selectCarBean.getDis()*0.001)+"公里");
	}
	
	/**
	 * 地图Marker点击回调方法
	 */
	@Override
	public boolean onMarkerClick(Marker marker) {
		if (marker.getPosition().latitude == endLat && marker.getPosition().longitude == endLng) {
			setMapCenter(endLat, endLng);// 将起点设置为 地图中心点
		} else {
			if (window != null && window.isShowing()) {
				window.dismiss();
			}
			Bundle bundle = marker.getExtraInfo();
			position = bundle.getInt("position"); // position表示marker（车场）在getLocals.getLists()中的位置下标
			mBaiduMap.clear();// 清空地图
			addParkFromList(position); // 重新添加一遍标注点，position处的的marker用大图
			drawParkMarkers(endLat, endLng, R.drawable.end_point); // 先添加marker后添加用户输入的目的地图标，保证起点图标在最上方
			setMapCenter(marker.getPosition().latitude, marker.getPosition().longitude); // 将被点击的marker设置为地图的中心点
			// 获取到 点击第position个车场 中的可用车辆列表 parkList
			parkList = getLocals.getLists().get(position).getParklists();
			viewList.clear(); // 先清空viewList（不然viewList会越添加越多）
			creatViews(); // 创建该车场中parkList.size()个车辆对应的parkList.size()个view，存放在viewList中（用于viewPager的适配）
			showPopwindow(); // viewPager的创建和适配 在popWinndow中完成
		}
		return false;
	}
	
	/**
	 * 根据parkList的size大小，创建size个view（用于viewPager的适配）存放在viewList中
	 */
	public void creatViews() {
		for (int i = 0, j = parkList.size(); i < j; i++) {
			View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.park_view_pager, null);
			// window中的 停车位号
			TextView parkNum = (TextView) view.findViewById(R.id.textView1);
			parkNum.setText(parkList.get(i).getParknum() + "车位");
			viewList.add(view);
		}
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
	    dis.setText("（距终点"+String.format("%.2f", getLocals.getLists().get(position).getLocal().getDis()*0.001)+"公里）");
	    //创建viewPager并绑定适配器
	    parkViewPager = (ViewPager) view.findViewById(R.id.carViewPager);
	    parkViewPager.setAdapter(pagerAdapter);
	    parkViewPager.setOnPageChangeListener(this);
	    
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
		window.showAtLocation(view, Gravity.BOTTOM, 0, 0);
	    
	    //window中按钮的点击监听
	    //--“预约”按钮点击监听
		TextView book = (TextView) view.findViewById(R.id.textView2);
		book.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ParkStateMap.this, SubmitOrder.class);
				final MyApp myApp = (MyApp) getApplication();
				myApp.setSelectParkBean(parkList.get(parkViewPager.getCurrentItem()));
				myApp.setDstLoc(getLocals.getLists().get(position).getLocal().getLoc()); //后期添加的：终点停车场的坐标
				startActivity(intent);
			}
		});
		// --向左的箭头“<”
		ImageView enter_left = (ImageView) view.findViewById(R.id.imageView2);
		enter_left.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (i > 0) {
					i--;
					parkViewPager.setCurrentItem(i);
				}
			}
		});
		// --向右的箭头“>”
		ImageView enter_right = (ImageView) view.findViewById(R.id.imageView3);
		enter_right.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (i < parkList.size() - 1) {
					i++;
					parkViewPager.setCurrentItem(i);
				}
			}
		});
		// popWindow消失监听方法
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
	 * 第一次使用APP：通过该方法 进行 功能引导提示
	 */
	private void showTipMask() {
		// 创建第一个tip （取消选定的车辆 tip）
		mHightLight = new HighLight(ParkStateMap.this)
				// .anchor(view)// 如果是Activity上增加引导层，不需要设置anchor
				.maskColor(R.color.grey)
				.addHighLight(R.id.del_car, R.layout.tip_parkstate_map1, new HighLight.OnPosCallback() {
					@Override
					public void getPos(float rightMargin, float bottomMargin, RectF rectF,HighLight.MarginInfo marginInfo) {
						marginInfo.rightMargin = rightMargin ;
						marginInfo.topMargin = rectF.top + rectF.height();
					}
				});
		// 创建第二个tip（预约终点车位 tip）
		mHightLight2 = new HighLight(ParkStateMap.this)
				.anchor(view)// 如果是Activity上增加引导层，不需要设置anchor
				.maskColor(R.color.grey)
				.addHighLight(R.id.textView2, R.layout.tip_parkstate_map2, new HighLight.OnPosCallback() {
					@Override
					public void getPos(float rightMargin, float bottomMargin, RectF rectF,HighLight.MarginInfo marginInfo) {
						marginInfo.rightMargin = rightMargin;
						marginInfo.topMargin = rectF.top + rectF.height();
					}
				});
		//给第二个tip设置点击监听
		mHightLight2.setClickCallback(new OnClickCallback() {
			@Override
			public void onClick() {
				mHightLight.remove();
			}
		});
		
		mHightLight.show();
		mHightLight2.show();
		
	}
	
	/**
	 * 从getLocals.getLists()中获取车场信息，并标注在地图上
	 */
	private void addParkFromList(int position) {

		for (int i = 0; i < getLocals.getLists().size(); i++) {
			if (i == position) {
				switch (getLocals.getLists().get(i).getParklists().size()) {
				case 0:
					drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
							getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.park_large0);
					break;
				case 1:
					drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
							getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.park_large1);
					break;
				case 2:
					drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
							getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.park_large2);
					break;
				case 3:
					drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
							getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.park_large3);
					break;
				case 4:
					drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
							getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.park_large4);
					break;
				case 5:
					drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
							getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.park_large5);
					break;
				default:
					break;
				}
			} else {
				switch (getLocals.getLists().get(i).getParklists().size()) {
				case 0:
					drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
							getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.park_small0);
					break;
				case 1:
					drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
							getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.park_small1);
					break;
				case 2:
					drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
							getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.park_small2);
					break;
				case 3:
					drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
							getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.park_small3);
					break;
				case 4:
					drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
							getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.park_small4);
					break;
				case 5:
					drawParkMarkers(getLocals.getLists().get(i).getLocal().getLoc().get(1),
							getLocals.getLists().get(i).getLocal().getLoc().get(0), i, R.drawable.park_small5);
					break;
				default:
					break;
				}
			}
		}
	}

	/**
	 * 地图点击事件监听
	 */
	@Override
	public void onMapClick(LatLng arg0) {
		if (window != null && window.isShowing()) {
			window.dismiss();
		}
	}
	@Override
	public boolean onMapPoiClick(MapPoi arg0) {
		if (window != null && window.isShowing()) {
			window.dismiss();
		}
		return false;
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
		// 通过setMapStatus(MapState)方法，改变地图状态
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
	 * *此方法：在地图的指定坐标上 添加 标注点（无附加信息,添加终点坐标用的）
	 */
	public void drawParkMarkers(Double lat, Double lng,int drawableId) {
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
	 * 创建 “正在匹配车=车位” 的ProgressDialog对话框的 方法
	 */
	public ProgressDialog createDialog() {
		ProgressDialog mypDialog=new ProgressDialog(this);
		mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mypDialog.setTitle("匹配车位");
		mypDialog.setMessage("正在匹配合适的车位，请您耐心等候...");
		mypDialog.setIndeterminate(false);
		mypDialog.setCancelable(true);
		return mypDialog;
	}
	
	/**
	 * 地图生命周期  与 Activity 同步
	 */
	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
	}
	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}
	
	//点击监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back:
			defaultHttpClient.getConnectionManager().shutdown();
			finish();
			break;
		case R.id.del_car: //上面的 "取消预约"车辆按钮
			defaultHttpClient.getConnectionManager().shutdown();
			finish();
			break;
		case R.id.button_park_state_switch:
			if (parkList!=null) { //预防空指针：在未加载出来列表数据时，点击列表按钮 造成的崩溃
				Intent intent = new Intent(ParkStateMap.this,ParkNodeList.class);
				List<Node> parkNode_list = getLocals.getLists();
				intent.putExtra("node_list",(Serializable)parkNode_list);
				startActivity(intent);
			}else {
				Toast.makeText(ParkStateMap.this, "列表数据加载中，请稍候！", Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}
	//返回键
	@Override
	public void onBackPressed() {
		defaultHttpClient.getConnectionManager().shutdown();
		finish();
	}
	
}
