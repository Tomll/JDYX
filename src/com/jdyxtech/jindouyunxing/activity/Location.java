package com.jdyxtech.jindouyunxing.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.RectF;
import android.net.Uri;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import java.util.Date;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionResult.SuggestionInfo;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.google.gson.Gson;
import com.jdyxtech.jindouyunxing.MainActivity;
import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.javabean.GetCarBean;
import com.jdyxtech.jindouyunxing.javabean.GetUserStatus;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;
import com.jdyxtech.jindouyunxing.wheelview_lib.TimePopupWindow;
import com.jdyxtech.jindouyunxing.wheelview_lib.TimePopupWindow.OnTimeSelectListener;
import com.jdyxtech.jindouyunxing.wheelview_lib.TimePopupWindow.Type;
import com.jdyxtech.jindouyunxing.widget.CircleImageView;
import com.jock.lib.HighLight;
import com.jock.lib.HighLight.OnClickCallback;
import com.jock.lib.HighLight.OnPosCallback;
import com.squareup.picasso.Picasso;


/**
 * 结合定位SDK实现定位，用户的约车信息（起终点、时间、人数）也在该界面获取
 */
public class Location extends BaseActivity implements  OnClickListener,OnMapClickListener,DrawerListener,BDLocationListener,
					OnGetGeoCoderResultListener,OnGetSuggestionResultListener{

	public GetCarBean get_CarBean;
	private SharedPreferences sp;
	private Editor editor;
	// 定位相关
	LocationClient mLocClient;
	BitmapDescriptor mCurrentMarker;
	private Double myLat, myLng,staLat,staLng,endLat,endLng;
	private String city,orgName,dstName;
	MapView mMapView;
	BaiduMap mBaiduMap;
	// UI相关
	OnCheckedChangeListener radioButtonListener;
	Button requestLocButton;
	boolean isFirstLoc = true;// 是否首次定位
	private UiSettings mUiSettings; //地图UI控制器
	private DrawerLayout drawerLayout;
	private TimePopupWindow pwTime;
	private AutoCompleteTextView staAutoCompleteTV,endAutoCompleteTV; //自动补全 文本框
	private TextView phone_tv;//头像右边的电话号码
	private ImageView state_image,msgStateImage;//用户状态展示图、“系统消息”后的小红点 
	private CircleImageView head_imageView; //用户头像
	private Button button_me;//“我的”按钮
	private GetUserStatus getUserStatus; //账户各种状态对应的javaBean
	private ProgressDialog progressDialog;
	//百度在线查询建议 相关
	private SuggestionSearch mSuggestionSearch;
	private List<String> list; //用于保存百度在线查询建议
	private ArrayAdapter<String> arrayAdapter; //list 适配器
	//地理编码检索（目前只是用 正向地理编码：地址-->坐标）：该搜索模块 也可去掉地图模块独立使用
	private GeoCoder mSearch1 = null;
	private HighLight mHightLight;//高亮 引导提示
	private AlertDialog alertDialog; //版本升级对话框
	private Boolean flag = false,flag2 = false;//flag:起点正向编码成功与否  flag2:起点是否是地图选点
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (1 == msg.what) { //用户状态 获取成功
				setStatusImage();//记录用户状态，并根据获取到的各种用户状态，显示对应的状态图片
			}else if (0 == msg.what) { //用户状态 获取失败
				
			}
		};
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 取消标题栏
		setContentView(R.layout.location);
		
		initView(); // 初始化 操作
		
		//时间选择后，通过接口回调，将数据回调过来
	    pwTime.setOnTimeSelectListener(new OnTimeSelectListener() {
	        @Override
	        public void onTimeSelect(Date date,int peopleNum) {
	        	if (!checkStartTime(date)) { //如果 订单时间为过去时
			 	    flag2 = false;
					return; //直接 return
				}
				if ( flag == true || flag2 == true ) { //flag:起点通过地理编码（正or反）已成功选取  或 flag2:起点已经通过 地图选点 成功选取了
					Intent intent = new Intent(Location.this,CarStateMap.class);
					final MyApp myApp = (MyApp) getApplication();
					myApp.setStaLat(staLat);
					myApp.setStaLng(staLng);
					myApp.setCity(city);
					myApp.setOrgName(orgName);
					//表示终点已经地图选点了（注：已放弃在该页进行终点的地图选点了，即使操作了地图选点， 其实也只是记录了一个终点名字，在下一页进行终点的正向地理编码）
//					if (flag3 == true) { 
//						myApp.setEndLat(endLat);
//						myApp.setEndLng(endLng);
//						myApp.setFalg3(true);
//					}
					myApp.setDstName(endAutoCompleteTV.getText().toString().trim()); //记录终点名字：用于下一个界面的终点的正向地理编码
					//判断是否是一小时之后的订单
					if (date.getTime() - System.currentTimeMillis() > 3600000) {
						myApp.setMoreThanOneHour(true);//表示是超过一小时 的订单
					}else {
						myApp.setMoreThanOneHour(false);//表示是一小时之内的订单
					}
					myApp.setLongStartTime(date.getTime()); //设置 长整型的订单开始时间
					myApp.setStartTime(getTime(date)); //设置 string类型的订单开始时间
					myApp.setPeopleNum(peopleNum);
			 	    startActivity(intent);
			 	    pwTime.dismiss();
			 	    flag2 = false;
				}else if (flag==false) {
					pwTime.dismiss();
					Toast.makeText(Location.this, "未获取到位置信息，正在重新定位...", Toast.LENGTH_LONG).show();
					mLocClient.requestLocation(); // 调用LocationClient类中的requestLocation()方法，发起一次手动定位
				}
	        }
	    });
	    
	}
	
	/**
	 * 现场保护（注意：这并不是Activity的一个生命周期，只是应用在后台运行时，该Activity将要被系统回收掉的时候 会执行这个方法）
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState); //在父类BaseActivity的onSaveInstanceState()方法中，执行了 全局变量myApp的 保存工作
	}

	/**
	 * 检查预约出行时间是否 已经过时的方法
	 */
	public boolean checkStartTime(Date startDate) {
		Date currenData = new Date(System.currentTimeMillis()-60*1000);
		if(currenData.compareTo(startDate)>0){
		    //订单开始日期  早于  当前系统日期
			Toast.makeText(Location.this, "预约出行时间已过，请选择其他时间！", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}	
	/**
	 * 初始化组件的方法
	 */
	private void initView() {
		sp = getSharedPreferences("user", MODE_PRIVATE);
		editor = sp.edit();
		//progressDialog
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("正在检索起点信息...");
		//自动补全文本框
		staAutoCompleteTV =  (AutoCompleteTextView) findViewById(R.id.sta_AutoCompleteTV);
		endAutoCompleteTV  = (AutoCompleteTextView) findViewById(R.id.end_AutoCompleteTV);
		staAutoCompleteTV.setHintTextColor(Color.BLACK); //设置起点文本本框中的 文字颜色 为黑色
		staAutoCompleteTV.addTextChangedListener(watcher); //添加文本监听
		endAutoCompleteTV.addTextChangedListener(watcher); //添加文本监听
		//实例化TimePopupWindow,并设置Type
        pwTime = new TimePopupWindow(this, Type.MONTH_DAY_HOUR_MIN); //月-日-时-分
        button_me = (Button) findViewById(R.id.button_me); //首页左上角“我的”按钮
		//初始化左侧抽屉布局
		drawerLayout = (DrawerLayout) findViewById(R.id.id_drawerlayout);
		drawerLayout.setDrawerListener(this); // 给抽屉注册滑动监听
		phone_tv = (TextView) findViewById(R.id.phone_tv);
		phone_tv.setText(sp.getString("uname", null));
		state_image = (ImageView) findViewById(R.id.state_image); //用户状态图片
		msgStateImage = (ImageView) findViewById(R.id.imageView2);//“系统消息”后的小红点
		head_imageView = (CircleImageView) findViewById(R.id.head_imageView); //用户 头像

		//初始化 在线查询建议 实例
		mSuggestionSearch = SuggestionSearch.newInstance();
		mSuggestionSearch.setOnGetSuggestionResultListener(this); //注册 在线查询建议 监听
	    list = new ArrayList<String>(); //用于保存百度在线查询建议结果 的集合
	    arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list); 
	    staAutoCompleteTV.setAdapter(arrayAdapter);
	    endAutoCompleteTV.setAdapter(arrayAdapter);
	    
		//初始化地理编码搜索模块（用于正向地理编码）
		mSearch1 = GeoCoder.newInstance();
		mSearch1.setOnGetGeoCodeResultListener(this); //注册 地理编码 监听

		//初始化地图
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setOnMapClickListener(this); //注册地图点击监听
		//地图 UI 的控制
		mMapView.showScaleControl(true);//  原地图中比例尺控件  true:展示 false:删除
		mMapView.showZoomControls(false);// 原地图中的缩放控件
		//mMapView.removeViewAt(1); // 删除百度地图LoGo 
		mUiSettings = mBaiduMap.getUiSettings(); //通过mBaiduMap获取地图 UI 控制器
		mUiSettings.setRotateGesturesEnabled(false); // 关闭 旋转手势（双指旋转）
		mUiSettings.setOverlookingGesturesEnabled(false); //关闭 俯视操作手势（双指下拉）

        // 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(this);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(0); //设置定位时间间隔
		option.setIsNeedAddress(true); //是否需要地址信息 ： 是
		mLocClient.setLocOption(option);
		mLocClient.start();
		//*************第一次 使用app 展示功能引导提示**********************
		if (sp.getBoolean("isFirstStart", true)) { //“第一次启动APP”
			findViewById(R.id.relative_input).post(new Runnable() {
				@Override
				public void run() {
					showTipMask();
				}
			});
		}
		
	}

	/**
	 * 此方法根据 获取到的各种用户状态 设置对应的图片
	 */
	public void setStatusImage() {
		//记录后面要用到的信息
		final MyApp myApp = (MyApp) getApplication();
		myApp.setCheck(getUserStatus.getManger_check()); //重新提交审核信息时，需要在Regist界面判断审核状态，所以这里记录下来
		myApp.setHead_imageUrl(getUserStatus.getHeadpic()); //更改头像界面需要头像的url 所以这里记录下来
		myApp.setOrderOff(getUserStatus.getOrderoff()); //手动取消订单界面需要用到 取消订单的次数（取消提示框），所以这里记录先来
		myApp.setAdd_deposit(getUserStatus.getAdd_deposit()); //补缴押金界面需要用到，记录下来
		myApp.setTel(getUserStatus.getTel()); //客服电话 记录下来
		//账户状态图片的显示
		if (getUserStatus.getManger_check() == 0) { //审核中
			state_image.setImageResource(R.drawable.checking);
		}else if (getUserStatus.getManger_check() == 2) { //审核未通过
			state_image.setImageResource(R.drawable.no_through);
		}else if (getUserStatus.getDeposit() == 0) { //审核通过，未支付押金（非会员）
			state_image.setImageResource(R.drawable.through);
		}else if (getUserStatus.getDeposit() > 0) { //审核通过，已支付押金（会员）
			state_image.setImageResource(R.drawable.members);
			if (getUserStatus.getLock() == 1) { //会员账户被锁定
			}
		}
		//消息状态图片的显示
		if (getUserStatus.getNewmsg() > 0) {
			button_me.setBackgroundResource(R.drawable.me_message); //显示带红点的“我的”按钮
			msgStateImage.setVisibility(View.VISIBLE); //显示“系统消息”后的小红点
		}
		//头像的显示
		Picasso.with(Location.this).load(getUserStatus.getHeadpic()).placeholder(R.drawable.head_portrait).into(head_imageView); //把 从服务器获取到的用户头像 设置到head_imageView上
		
		// 有可升级版本，且upDate = true（true：可以提示升级，false：用户主动放弃升级 不再提示），就创建升级对话框
		if (!getUserStatus.getVersion().equals("") && sp.getBoolean("upDate", true)&& alertDialog == null) {
			// 版本升级对话框
			alertDialog = new AlertDialog.Builder(Location.this).setCancelable(false)
					.setMessage("发现新版本" + getUserStatus.getVersion() + "，是否升级？").setNegativeButton("否", null)
					.setNeutralButton("不再提示", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							editor.putBoolean("upDate", false);
							editor.commit();
						}
					}).setPositiveButton("是", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Uri uri = Uri.parse(getUserStatus.getDownloadurl());
							Intent it = new Intent(Intent.ACTION_VIEW, uri);
							startActivity(it);
						}
					}).create();
		}
		//有可版本升级，upDate = true（true：可以提示升级，false：用户主动放弃升级 不再提示），且alertDialog没有显示
		if (!getUserStatus.getVersion().equals("") && sp.getBoolean("upDate", true) && alertDialog !=null && !alertDialog.isShowing()) {
			alertDialog.show();
		}
	}
	
	/**
	 * 实现定位接口，重写定位SDK接口中的监听函数
	 */
	@Override
	public void onReceiveLocation(BDLocation location) {
		// map view 销毁后不在处理新接收的位置
		if (location == null || mMapView == null) {
			return;
		}
		// **设置全局经纬
		myLat = location.getLatitude();
		myLng = location.getLongitude();
		city = location.getCity();
		//展示定位信息
		MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
				// 此处设置开发者获取到的方向信息，顺时针0-360
				.direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
		mBaiduMap.setMyLocationData(locData);
		setMapCenter(location.getLatitude(), location.getLongitude()); //将定位点设置为地图中心点
	}

	/**
	 * 地图点击监听回调 
	 */
	//点击非poi 点
	@Override
	public void onMapClick(final LatLng point) {
		//隐藏popWindow
		mBaiduMap.hideInfoWindow();
	}
	//点击poi 点
	@Override
	public boolean onMapPoiClick(final MapPoi point) {
		// 创建InfoWindow展示的view
		final View view = LayoutInflater.from(this).inflate(R.layout.poplayout_map_poi, null);
		view.setBackgroundResource(R.drawable.popup); //设置白色背景
		// 定义用于显示该InfoWindow的坐标点
		LatLng pt = new LatLng(point.getPosition().latitude, point.getPosition().longitude);
		// 创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
		InfoWindow mInfoWindow = new InfoWindow(view, pt, 0);
		TextView add_tv = (TextView) view.findViewById(R.id.textView1);
		add_tv.setText(point.getName());
		// 显示InfoWindow
		mBaiduMap.showInfoWindow(mInfoWindow);
		setMapCenter(point.getPosition().latitude, point.getPosition().longitude); //将被点击的point设置为地图的中心点
		//点击inforWindow中的 “设为起点”
		view.findViewById(R.id.textView2).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				staLat = point.getPosition().latitude;
				staLng = point.getPosition().longitude;
				staAutoCompleteTV.setText(point.getName());
				orgName = point.getName();
				flag2 = true ;
				mBaiduMap.clear();
			}
		});
		//点击inforWindow中的“设为终点”
		view.findViewById(R.id.textView3).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//已放弃在该页进行终点的地图选点了，只是记录了一个终点名字，然后在下一页进行终点的正向地理编码）
//				endLat = point.getPosition().latitude;
//				endLng = point.getPosition().longitude;
				endAutoCompleteTV.setText(point.getName());
				dstName = point.getName();
				mBaiduMap.clear();
			}
				
		});
		return false;
	}
	
	
	// 文本变化监视器
	private TextWatcher watcher = new TextWatcher() {
	    @Override // 文本改变中
	    public void onTextChanged(CharSequence s, int start, int before, int count) {
	        
	    } 
	    @Override // 文本改变前
	    public void beforeTextChanged(CharSequence s, int start, int count,int after) {
	        
	    }
		@Override  // 文本改变后 
		public void afterTextChanged(Editable s) {
			if (city!=null) {
				//发起在线建议查询
				mSuggestionSearch.requestSuggestion(new SuggestionSearchOption().city(city).keyword(s.toString().trim())); 
			}
		}
	};	
	
	/**
	 * 百度 在线查询建议 监听回调方法
	 */
	@Override
	public void onGetSuggestionResult(SuggestionResult res) {
	        if (res == null || res.getAllSuggestions() == null) {  
				Toast.makeText(Location.this, "未搜索到相关信息", Toast.LENGTH_LONG).show();
	            return;  //未找到相关结果 
	        }  
        list.clear();
	    List<SuggestionInfo> allSuggestions = res.getAllSuggestions();//获取在线建议检索结果 
	    //遍历集合 allSuggestions 将其中的地址key取出 存放到 list中，一遍下一步进行适配
	    for (SuggestionInfo suggestionInfo : allSuggestions) {
			list.add(suggestionInfo.key);
		}
	    arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list); 
	    staAutoCompleteTV.setAdapter(arrayAdapter);
	    endAutoCompleteTV.setAdapter(arrayAdapter);
	}
	
	/**
	 * 正向地理编码的 监听回调方法(根据起点搜索框中的文字 ---》起点地理坐标)
	 */
	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		progressDialog.cancel();
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(Location.this, "起点检索失败，请重新输入", Toast.LENGTH_LONG).show();
			flag = false;
			return;
		}
		flag = true;
		staLat = result.getLocation().latitude;
		staLng = result.getLocation().longitude;
		orgName = staAutoCompleteTV.getText().toString().trim();
		pwTime.showAtLocation(new TextView(this), Gravity.BOTTOM, 0, 0, new Date());  //编码成功，展示弹窗
		//隐藏软键盘
		InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);  
		imm.hideSoftInputFromWindow(staAutoCompleteTV.getWindowToken(), 0);
	}
	/**
	 * 反向地理编码的 监听回调方法（不输入起点的话，默认使用当前定位为点作为起点：根据定位点坐标 ---》起点地名描述）
	 */
	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		progressDialog.cancel();
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			flag = false;
			return;
		}
		flag = true;
		orgName  = result.getAddress();
		pwTime.showAtLocation(new TextView(this), Gravity.BOTTOM, 0, 0, new Date()); //编码成功，展示弹窗
		//隐藏软键盘
		InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);  
		imm.hideSoftInputFromWindow(staAutoCompleteTV.getWindowToken(), 0);
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
	 *  登出 方法
	 */
	private void loginOut() {
		editor.putString("token", ""); //删除 token
		editor.putString("pwd", ""); //删除保存的密码 
		editor.commit(); //提交操作
		Intent intent = new Intent(Location.this,Login.class);
		startActivity(intent); //跳转到 登陆页面
		finish();//关闭 当前页面
	}
	/**
	 * 弹出提示窗口（确认退出当前账号？）
	 */
	public void popAlertDialog() {
		AlertDialog.Builder builder2 = new AlertDialog.Builder(Location.this);
		builder2.setMessage("确认退出当前账号？");
		//设置 积极按钮 
		builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				loginOut(); //退出登录 逻辑
			}
		});
		//设置 消极按钮 
		builder2.setNegativeButton("取消",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		AlertDialog alertDialog = builder2.create();
		alertDialog.show();
		//获取到Dialog对应的Window的最顶层decorView，然后就可以对decorView中的 子view 进行一些自定操作了
		//View decorView = alertDialog.getWindow().getDecorView(); 
		//setViewFontSizeAndColer(decorView, 22,getResources().getColor(R.color.lightgreen));
	}
	
//	/**
//	 * 此方法用于修改 系统原生Dialog中包含的TextView的一些属性（字体颜色、字号）
//	 * @param view ：截取的 Dialog对应的 Window的 最顶层的 decorView，提取decorView中的 子View（主要指：TextView）进行一些自定义操作
//	 * @param size ：需要设置的目标字号
//	 * @param color ：需要设置的字体颜色。（另外：这里我们需要一个全局变量 j 记录我们现在操作的是哪个TextView：
//	 * 因为AlterDialog中有5个TextView：title（标题j=1）、message（内容j=2）、yes（是j=3）、neutral（中立j=4）、no（否j=5））
//	 */
//	private void setViewFontSizeAndColer(View view, int size, int color) {
//		if (view instanceof ViewGroup) {
//			ViewGroup parent = (ViewGroup) view;
//			int count = parent.getChildCount();
//			for (int i = 0; i < count; i++) {
//				setViewFontSizeAndColer(parent.getChildAt(i), size, color); //通过递归，将parent中的所有textView 提取出来，然后设置字体大小和颜色
//			}
//		} 
//        else if(view instanceof TextView ){
//        	j++; 
//            if (j == 3 ||j==5 ) {
//            	TextView textview = (TextView)view;
//            	textview.setTextSize(size);
//            	textview.setTextColor(color);
//            	j=0;
//			}
//            
//        }
//	}
	
	/**
	 *  获取当前用户各种状态 的方法
	 */
	public void getUserStatus() { 
		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/clientapi/client/status");
        //向服务器上传json格式的登陆数据
        try {
        	JSONObject json = new JSONObject();
			json.put("token",sp.getString("token", ""));
			json.put("system", "android");
			json.put("version","1.3.0");
			StringEntity se = new StringEntity(json.toString(),HTTP.UTF_8);
			se.setContentEncoding(new BasicHeader(HTTP.UTF_8, "application/json"));
			request.setEntity(se);
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}catch (JSONException e1) {
			e1.printStackTrace();
		}
        //开启一个线程，进行post带参网络请求（登录）
        new Thread() {       
			public void run() {     
            	try {  
            		HttpResponse httpResponse = new DefaultHttpClient().execute(request); 
			         if(httpResponse.getStatusLine().getStatusCode() == 200){
			        	 String result = EntityUtils.toString(httpResponse.getEntity());
			        	 Gson gson  = new Gson();
			        	 getUserStatus = gson.fromJson(result, GetUserStatus.class);
			        	 if (200 == getUserStatus.getStatus()) { //成功，告诉主线程中的handler进行相应的操作
			        		 handler.sendEmptyMessage(1); 
			        	 }else{  //失败，告诉主线程中的handler进行相应的操作
			        		 handler.sendEmptyMessage(0); 
			        	 }
			         }
		        } catch (UnsupportedEncodingException e) {  
		            e.printStackTrace();  
		        } catch (ClientProtocolException e) {  
		            e.printStackTrace();  
		        } catch (IOException e) {  
		            e.printStackTrace();  
		        }  
             }     
         }.start();
	}
	
	/**
	 * 判断 账户 各种状态的方法（审核状态、锁定状态、押金支付状态）
	 * inOrOut ：标志位，用于区分点击的是 用户审核状态图标 还是 “预约驾乘按钮”（为了使checkState方法通用）
	 */
	public boolean checkState(int inOrOut) {
		final MyApp myApp = (MyApp) getApplication();
		if (getUserStatus == null) {
			return false;
		}
		
		if (getUserStatus.getLock()==1) { //会员账户被锁定
			Toast.makeText(Location.this, "您的账户已被锁定，请联系客服！", Toast.LENGTH_LONG).show();
			return true;
		}else if (getUserStatus.getManger_check()==0) { //审核中
			Toast.makeText(Location.this, "账户审核中，暂时无法预约驾驶！", Toast.LENGTH_LONG).show();
			return true;
		}else if (getUserStatus.getManger_check()==2) { //审核未通过
			Toast.makeText(Location.this, "账户审核未通过，请重新提交注册信息！", Toast.LENGTH_LONG).show();
			//跳转到注册界面，重新提交注册信息
			startActivity(new Intent(this,Regist.class));
			return true;
		}else if (getUserStatus.getDeposit()==0) { //审核通过，未支付押金（非会员）
			Toast.makeText(Location.this, "账户审核已通过，支付押金后即可预约乘车！", Toast.LENGTH_LONG).show();
			//给myApp设置支付类型为 “押金2000元”
			myApp.setBizType("000202"); //设置支付的费用业务类型为：押金费用
			myApp.setFree(2000); //设置支付金额为：押金2000元
			myApp.setOrderNum(myApp.getPhone()+System.currentTimeMillis()); //押金支付情况下的orderNum为用户手机号 + 时间戳
			startActivity(new Intent(this,Pay.class));//跳转到 支付界面,去支付押金
			return true;
		}else if (getUserStatus.getAdd_deposit() > 0) { //押金余额不足，需要补交押金
			Toast.makeText(Location.this, "您的押金余额不足，请补缴押金！", Toast.LENGTH_LONG).show();
			myApp.setBizType("000203"); //设置支付的费用业务类型为：押金费用
			myApp.setFree(getUserStatus.getAdd_deposit()); //设置支付金额为：押金2000元
			myApp.setOrderNum(myApp.getPhone()+System.currentTimeMillis()); //押金支付情况下的orderNum为用户手机号 + 时间戳
			startActivity(new Intent(this,Pay.class));//跳转到 支付界面,去支付押金
			return true;
		}
		//inOrOut ：标志位，用于区分点击的是 用户审核状态图标 还是 “预约驾乘按钮”（为了使checkState方法通用）
		if (inOrOut == 1) { //inOrOut == 1 表示点击的是 “预约驾乘”按钮，之后来进行checkState的
			//首单的状态的判断，满足以下条件即为“还有未完成订单”
			//当：1、 订单列表不为空（isset==0无订单isset==1有订单）2、未手动取消 3、没有 超时被系统取消 4、且第一个订单未支付（0） ,此种情况表示首单未完成
			if (getUserStatus.getOrder().getIsset()== 1 && getUserStatus.getOrder().getOrderstatus()!=3 && getUserStatus.getOrder().getOrderstatus()!=4 && getUserStatus.getOrder().getPaystatus()==0) {
				Toast.makeText(Location.this, "您还有未完成订单，暂不能预约！", Toast.LENGTH_LONG).show();
				return true;
			}
			//取消订单次数的判断
			if (getUserStatus.getOrderoff() >= 3 ) {
				Toast.makeText(Location.this, "由于您一天内取消了三次订单，今天不能再预约了！", Toast.LENGTH_LONG).show();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 检查 输入的起点 终点是否为空 
	 */
	public boolean checkInput() {
		if (TextUtils.isEmpty(endAutoCompleteTV.getText().toString())) {
			Toast.makeText(Location.this, "目的地不能为空！", Toast.LENGTH_LONG).show();
			return true;
		}
		return false;
		
	}
	/**
	 * 布局中所有可点击控件的点击事件 都在该重写方法中处理
	 */
	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.button_location: // "定位"按钮
			mLocClient.requestLocation(); // 调用LocationClient类中的requestLocation()方法，发起一次手动定位
			Toast.makeText(getApplicationContext(), "正在定位...", Toast.LENGTH_SHORT).show();
			break;
		case R.id.re_exchange: // "切换起止点" 图片按钮
			String sta = staAutoCompleteTV.getText().toString().trim();
			staAutoCompleteTV.setText(endAutoCompleteTV.getText().toString().trim());
			endAutoCompleteTV.setText(sta);
			break;
		case R.id.button_me: // "我的" 按钮
			drawerLayout.openDrawer(GravityCompat.START);
			break;
		case R.id.button_bespeak: // "预约驾乘" 按钮
			// 检查账户的各种状态是否正确、检查输入的终点是否为空
			if (checkState(1) || checkInput()) { 
				return;
			}
			
			// 1 表示 起点 已经地图选点了，起点坐标、名称都已记录，就不用再对起点 发起地理编码了
			if (flag2 == true) { 
				pwTime.showAtLocation(new TextView(this), Gravity.BOTTOM, 0, 0, new Date()); //第一个参数 好像不为空就行，只要是一个 不为空的控件就行（？？），
				flag2 = false;
				//隐藏软键盘
				InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);  
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				return;
			} 
			// 2.1 没有输入起点的情况：默认使用定位点作为起点
			if (TextUtils.isEmpty(staAutoCompleteTV.getText().toString()) && myLat != null && myLng != null) {
				staLat = myLat;
				staLng = myLng;
				// 针对 定位点 发起反向地理编码 ： 坐标--》地名
				mSearch1.reverseGeoCode(new ReverseGeoCodeOption().location(new LatLng(myLat, myLng)));
				progressDialog.show();
				return;
			} 
			// 2.2 输入了起点的情况,起点输入框不为空
			if (!TextUtils.isEmpty(staAutoCompleteTV.getText().toString())){ 
				// 发起正向地理编码 ：地名--》坐标
				if (null != city) {
					mSearch1.geocode(new GeoCodeOption().city(city).address(staAutoCompleteTV.getText().toString()));
					progressDialog.show();
				} else {
					Toast.makeText(Location.this, "未获取到位置信息，正在重新定位...", Toast.LENGTH_LONG).show();
					mLocClient.requestLocation(); // 调用LocationClient类中的requestLocation()方法，发起一次手动定位
				}
				return;
			}
			break;
		//以下是侧滑布局的控件点击事件
		case R.id.head_imageView: // 点击 头像 
			intent.setClass(Location.this,MyTravelList.class);
			startActivity(new Intent(Location.this,SetHeadImage.class));
			break;
		case R.id.state_image: // 点击 用户状态 图片
			if (!checkState(2)) { //检查账户状态，不检查最后两项
				return;
			}
			break;
		case R.id.relativeLayout1: // "我的行程" 按钮
			drawerLayout.closeDrawers();
			intent.setClass(Location.this,MyTravelList.class);
			startActivity(intent);
			break;
		case R.id.textView2: // "退出登录" 按钮
			popAlertDialog(); //展示 登出 对话框
			break;
		case R.id.relativeLayout2: // "关于我们" 按钮
			drawerLayout.closeDrawers();
			intent.setClass(Location.this,AboutUs.class);
			startActivity(intent);
			break;
		case R.id.relativeLayout3: // "系统消息" 按钮
			button_me.setBackgroundResource(R.drawable.me); //显示为不带红点的“我的”按钮
			msgStateImage.setVisibility(View.INVISIBLE); //隐藏“系统消息”后的小红点
			drawerLayout.closeDrawers();
			intent.setClass(Location.this,SystemMessageList.class);
			startActivity(intent);
			break;
		case R.id.relativeLayout4_1: // "我的钱包" 按钮
			drawerLayout.closeDrawers();
			intent.setClass(Location.this,MyWallet.class);
			startActivity(intent);
			break;
		case R.id.relativeLayout6: // "更换手机号" 按钮
			drawerLayout.closeDrawers();
			intent.setClass(Location.this,ChangePhoneNum.class);
			startActivity(intent);
			break;
		case R.id.relativeLayout5: // "意见反馈" 按钮
			drawerLayout.closeDrawers();
			intent.setClass(Location.this,FeedBack.class);
			startActivity(intent);
			break;
		default:
			break;
		}
		
	}	
	/**
	 * 地图生命周期与activity 同步,释放相应的资源
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mBaiduMap.clear();
		mMapView.onPause();
	}
	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
		staAutoCompleteTV.setText("");
		endAutoCompleteTV.setText("");
		phone_tv.setText(sp.getString("uname", null)); //修改手机号之后，重新进入界面加载新的手机号
		getUserStatus(); //当activity 被唤醒，进入onResume(),要获取用户的各种状态
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
	}
	
	/**
	 * 抽屉滑动时，需要重写的4个回调方法
	 */
	@Override
	public void onDrawerClosed(View arg0) {
	}
	@Override
	public void onDrawerOpened(View arg0) {
	}
	@Override
	public void onDrawerSlide(View arg0, float arg1) {
	}
	@Override
	public void onDrawerStateChanged(int arg0) {
	}
		
	/**
	 * 格式化系统时间的方法
	 */
	private static String getTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(date);
    }	
	
	/**
	 * 连续按两次返回键 ，退出应用的方法
	 */
	long waitTime = 2000;
	long touchTime = 0;

	@Override
	public void onBackPressed() {
		if (pwTime.isShowing()) {
			pwTime.dismiss();
		} else {
			long currentTime = System.currentTimeMillis();
			if ((currentTime - touchTime) >= waitTime) {
				Toast.makeText(Location.this, "再按一次退出应用", Toast.LENGTH_SHORT).show();
				touchTime = currentTime;
			} else {
				finish();
				System.exit(0); // 正常退出程序
			}
		}
	}
	
	
	/**
	 * 第一次使用APP：通过该方法 进行 功能引导提示
	 */
	private void showTipMask(){
		// 创建第一个tip （输入tip）
		mHightLight = new HighLight(Location.this)
				// .anchor(findViewById(R.id.id_container)) //如果是Activity上增加引导层，不需要设置anchor
				.maskColor(R.color.grey)
				.addHighLight(R.id.relative_input, R.layout.tip_location1, new HighLight.OnPosCallback() {
					@Override
					public void getPos(float rightMargin, float bottomMargin, RectF rectF,HighLight.MarginInfo marginInfo) {
						marginInfo.leftMargin = rectF.width() / 2;
						marginInfo.topMargin = rectF.bottom;
					}
				});
		// show第一个tip
		mHightLight.show();
		// 给第一个tip设置点击监听
		mHightLight.setClickCallback(new OnClickCallback() {
			@Override
			public void onClick() {
				// 创建第二个tip（预约驾乘tip）
				mHightLight = new HighLight(Location.this)
						// .anchor(findViewById(R.id.id_container)) //如果是Activity上增加引导层，不需要设置anchor
						.maskColor(R.color.grey)
						.addHighLight(R.id.button_bespeak, R.layout.tip_location2, new HighLight.OnPosCallback() {
					@Override
					public void getPos(float rightMargin, float bottomMargin, RectF rectF,HighLight.MarginInfo marginInfo) {
						marginInfo.leftMargin = rectF.left + rectF.width()/4;
						marginInfo.bottomMargin = bottomMargin + rectF.height();
					}
				});
				// show第二个tip
				mHightLight.show();
			}
		});
	}

	
	
}
