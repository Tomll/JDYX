package com.jdyxtech.jindouyunxing.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;

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

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.SpatialRelationUtil;
import com.google.gson.Gson;
import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.javabean.OpenCarDoorBean;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;
import com.jock.lib.HighLight;
import com.jock.lib.HighLight.OnClickCallback;
import com.squareup.picasso.Picasso;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 取车界面
 * @author Tom
 *
 */
public class GetCar extends BaseActivity implements OnClickListener, BDLocationListener{
	private SharedPreferences sp;
	// 定位相关（取车界面的定位功能，只需定位 无需展示地图）
	LocationClient mLocClient;
	private Double myLat, myLng,endLat,endLng;
//	// 导航相关
//	private String mSDCardPath = null;
//	public static final String ROUTE_PLAN_NODE = "routePlanNode";
//	public static final String TAG = "NaviSDkDemo";
//	private static final String APP_FOLDER_NAME = "BNSDKDemo";
	//UI相关
	private HighLight mHightLight;//高亮 引导提示1
	private ImageView carImageView,findCarImage,openCarDoorImage;
	private TextView textView1,textView2,textView3,textView4,textView5,textView6,textView13,textView15;
	private TextView service_phone;//客服电话
	private long totalTime; //距离预约出行时间 剩余的总时间 
	private int hours,minutes; //倒计时的 小时和分钟
	private ProgressDialog proDialog; //“正在打开车门” 进度对话框
    private DefaultHttpClient defaultHttpClient = MyDefaultHttpClient.getDefaultHttpClient();

	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) { //开车门成功
				proDialog.cancel(); 
				Toast.makeText(GetCar.this, "车门已经打开！", Toast.LENGTH_LONG).show();
				Intent intent = new Intent(GetCar.this,CurrentTravel.class);
				startActivity(intent);
				finish();
			}else if (msg.what == 0) {   //开车门失败
				proDialog.cancel();
				Toast.makeText(GetCar.this, "车门开启失败！", Toast.LENGTH_LONG).show();
			}else if (msg.what == 2) {  //寻车成功
			}else if (msg.what == 3) {  //寻车失败
			}else if (msg.what == 4) {  //更新倒计时
				textView13.setText(hours+"");
				textView15.setText(minutes+"");
			}else if (msg.what == 5) { //订单时间还未到，不能开启车门
				proDialog.cancel();
				Toast.makeText(GetCar.this, "订单时间还未到，不能开启车门！", Toast.LENGTH_LONG).show();
			}else if(msg.what == 6){   //网络超时 （连接超时 或 响应超时）
				if (proDialog.isShowing()) {
					proDialog.cancel(); //关闭 进度框
				}
				Toast.makeText(GetCar.this, "网络连接超时，请重试！", Toast.LENGTH_LONG).show();
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.get_car);
		
		initView();//初始化
		
	}
	
	/**
	 * 现场保护（注意：这并不是Activity的一个生命周期，只是应用在后台运行时，该Activity将要被系统回收掉的时候 会执行这个方法）
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState); //在父类BaseActivity的onSaveInstanceState()方法中，执行了 全局变量myApp的 保存工作
	}
	
	/**
	 * 初始化操作
	 */
	private void initView() {
		proDialog = createDialog();
		sp = getSharedPreferences("user", MODE_PRIVATE);
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(this);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
//		option.setCoorType("GCJ02"); // 设置坐标类型 
		option.setCoorType("bd09ll"); // 设置坐标类型 
		option.setScanSpan(10); // 设置定位时间间隔 
		mLocClient.setLocOption(option);  //设置定位选项
		mLocClient.start();  //开启定位
//		if ( initDirs() ) { //初始化存储路径
//			initNavi();  //初始化导航
//		}
		//操控按钮
		findCarImage = (ImageView) findViewById(R.id.imageView4); //“寻车” 图片按钮
		openCarDoorImage = (ImageView) findViewById(R.id.imageView3); //“开车门” 图片按钮
		//适配展示 订单信息
		final MyApp myApp  = (MyApp) getApplication();
		textView1 = (TextView) findViewById(R.id.textView2);
		textView1.setText(myApp.getSelectCarBean().getTitle()+myApp.getSelectCarBean().getParknum()+"车位");
		textView2 = (TextView) findViewById(R.id.textView5);
		textView2.setText(myApp.getSelectCarBean().getAddr());
		carImageView = (ImageView) findViewById(R.id.imageView2);
		Picasso.with(this).load(myApp.getSelectCarBean().getBig()).placeholder(R.drawable.holder).into(carImageView);
		textView3 = (TextView) findViewById(R.id.textView8);
		textView3.setText(myApp.getSelectCarBean().getBrand());
		textView4 = (TextView) findViewById(R.id.textView7);
		textView4.setText(myApp.getSelectCarBean().getModel());
		textView5 = (TextView) findViewById(R.id.textView9);
		textView5.setText(myApp.getSelectCarBean().getSeats()+"座");
		textView6 = (TextView) findViewById(R.id.textView10);
		textView6.setText(" "+myApp.getSelectCarBean().getCarli()+" ");
		//客服电话
		service_phone = (TextView) findViewById(R.id.service_phone);
		service_phone.setText(myApp.getTel());
		
//		（以前导航用，现在不用导航了，改用步行路线规划）
//		//汽车所在停车场 的坐标
//		double[] bd09_To_Gcj02 = bd09_To_Gcj02(myApp.getStrLoc().get(1),myApp.getStrLoc().get(0));
//		endLng  = bd09_To_Gcj02[0];
//		endLat  = bd09_To_Gcj02[1];
		//预约出行时间倒计时：小时和分钟文本展示控件
		textView13 = (TextView) findViewById(R.id.textView13);
		textView15 = (TextView) findViewById(R.id.textView15);
		totalTime = myApp.getLongStartTime() - System.currentTimeMillis()+60000; //距预约开始时间 - 当前时间 = 剩余的总时间(ms)
		//此线程用于更新：预约出行时间 倒计时
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(totalTime > 0){ 
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					hours = (int) Math.floor((totalTime/60000)/60);
					minutes = (int) ((totalTime/60000)%60);
					totalTime -= 1000;
					handler.sendEmptyMessage(4); //通知更新 倒计时
				}
			}
		}).start();
		// "寻车"按钮 属性旋转动画
		ObjectAnimator.ofFloat(findCarImage, "rotationY", 0.0F, 360.0F).setDuration(1500).start();
		// "开车门"按钮 属性旋转动画
		ObjectAnimator.ofFloat(openCarDoorImage, "rotationY", 0.0F, 360.0F).setDuration(1500).start(); 	
		//*****************第一次使用App 引导提示*******************
		if (sp.getBoolean("isFirstStart", true)) {
			findViewById(R.id.imageView4).postDelayed((new Runnable() {
				@Override
				public void run() {
					showTipMask();
				}
			}), 1500); // 进入页面 1.5s后，展示引导提示信息
		}
		
	}
	
	/**
	 * 向服务器 提交 “开车门” “寻车”指令 的方法
	 */
	private void operateCar(String carCommand) {
		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/clientcommand/car/command");
        //向服务器上传json格式的数据
        try {
    		final MyApp myApp  = (MyApp) getApplication();
        	JSONObject json = new JSONObject();
			json.put("token",sp.getString("token", ""));
			json.put("ordernum",myApp.getOrderNum());
			json.put("pw",carCommand);
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
            		HttpResponse httpResponse = defaultHttpClient.execute(request); 
			         if(httpResponse.getStatusLine().getStatusCode() == 200){
			        	 String result = EntityUtils.toString(httpResponse.getEntity());
			        	 Gson gson  = new Gson();
			        	 OpenCarDoorBean openCarDoor = gson.fromJson(result, OpenCarDoorBean.class);
			        	 if (200 == openCarDoor.getStatus()) {
			        		 if (openCarDoor.getPw().equals("OPENLOCK")) {
			        			handler.sendEmptyMessage(1); //开车门成功，告诉主线程中的handler进行相应的操作
							}else if (openCarDoor.getPw().equals("FINDCAR")) {
								handler.sendEmptyMessage(2); //寻车成功，告诉主线程中的handler进行相应的操作
							}
						}else if (409 == openCarDoor.getStatus()) { //订单时间还未到，不能开启车门
							handler.sendEmptyMessage(5); 
						}else{
			        		 if (openCarDoor.getPw().equals("OPENLOCK")) {
			        			handler.sendEmptyMessage(0); //开车门失败，告诉主线程中的handler进行相应的操作
							}else if (openCarDoor.getPw().equals("FINDCAR")) {
								handler.sendEmptyMessage(3); //寻车失败，告诉主线程中的handler进行相应的操作
							}
						}
			         }
				} catch (ConnectTimeoutException e) { // 捕获 ：连接超时异常
					handler.sendEmptyMessage(6); // 通知handler进行相应的处理
				} catch (SocketTimeoutException e) { // 捕获 ：响应超时异常
					handler.sendEmptyMessage(6); // 通知handler进行相应的处理
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
	 * 创建 “正在打开车门” ProgressDialog对话框的 方法
	 */
	public ProgressDialog createDialog() {
		ProgressDialog mypDialog=new ProgressDialog(this);
		mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mypDialog.setTitle("打开车门");
		mypDialog.setMessage("正在打开车门，请您耐心等候...");
		mypDialog.setIndeterminate(false);
		mypDialog.setCancelable(true);
		return mypDialog;
	}
	
	/**
	 * 弹出提示窗口（是否确认开车）
	 */
	public void popAlertDialog() {
		AlertDialog.Builder builder2 = new AlertDialog.Builder(GetCar.this);
		builder2.setMessage("确认打开车门？");
		//设置 积极按钮 
		builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				proDialog.show(); //展示“正在打开车门”对话框
				operateCar("OPENLOCK"); //向服务器确认 打开车门
			}
		});
		//设置 消极按钮 
		builder2.setNegativeButton("取消",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
		builder2.create().show();
	}
	
	/**
	 * 用户准备开车门时，判断用户 距车的实际距离 distance，当distance大于 半径radius 的时候，是不允许取车的 
	 * 原理：判断 用户定位点 是否在 以车辆为中心点，radius为半径的圆内。
	 * @param radius：圆半径，调用该方法时，可以根据需要，定制这个半径距离
	 * @param myLat：我的定位点的 纬度值
	 * @param myLng：我的定位点的 经度值
	 * @param centerLat：圆心的  纬度值
	 * @param centerLng：圆心的 经度值
	 * @return boolean：返回值
	 */
	public static boolean checkDistance(int radius,double myLat,double myLng,double centerLat,double centerLng){
//		double[] my_bd09 = GetCar.gcj02_To_Bd09(myLat, myLng); //将定位到的 我当前位置GCJ02坐标点 转换为 bd09坐标点
//		LatLng p1 = new LatLng(my_bd09[1], my_bd09[0]); //我的当前位置 bd09坐标点
		LatLng p1 = new LatLng(myLat,myLng); //我的当前位置 bd09坐标点
		LatLng pCenter = new LatLng(centerLat,centerLng); //圆心的 bd09坐标
		//判断点p1是否在 ，以pCenter为中心点，radius为半径的圆内。
		return SpatialRelationUtil.isCircleContainsPoint(pCenter, radius, p1);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.textView4:  //开始 步行导航 至 车
// 			if (BaiduNaviManager.isNaviInited()) {
//				routeplanToNavi();
//			}
//			Toast.makeText(getApplicationContext(), "正在开启导航，请稍候！", Toast.LENGTH_LONG).show();
			startActivity(new Intent(GetCar.this,GetCarWalkRoutePlan.class)); //跳转到步行路线规划界面
			break;
		case R.id.imageView4: //寻车
			operateCar("FINDCAR"); //向服务器操作 寻车
			Toast.makeText(getApplicationContext(), "正在寻车，请注意车辆发出的灯光！", Toast.LENGTH_LONG).show();
			break;
		case R.id.imageView3: //开车门
			final MyApp myApp = (MyApp) getApplication();
			//用户准备开车门时，判断用户当前 距 车的实际距离，在一定范围(1000m)内才能取车，大于规定的距离是不允许取车的
			if (checkDistance(100000,myLat, myLng,myApp.getSelectCarBean().getLoc().get(1), myApp.getSelectCarBean().getLoc().get(0))) { //用户 距 车的距离 在1000米以内
				popAlertDialog(); //展示alertDialog对话框
			}else { //距车1000米 以外，弹出安全提示框
				new AlertDialog.Builder(GetCar.this).setTitle("安全提示").setMessage("您距车辆位置还较远，为安全起见请勿提前打开车门！").create().show();
			}
			break;
		case R.id.service_phone: //客服电话
			startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ((MyApp) getApplication()).getTel())));
			break;
		case R.id.button_back: //返回
			defaultHttpClient.getConnectionManager().shutdown();
			finish();
			break;
		default:
			break;
		}
	}
	@Override
	public void onBackPressed() {
		defaultHttpClient.getConnectionManager().shutdown();
		finish();
	}
	
    /** 
     * * 百度坐标系 (BD-09) 转换成 火星坐标系 (GCJ-02) 的算法  
     */  
    public static double[] bd09_To_Gcj02(double bd_lat, double bd_lon) {  
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;  
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * Math.PI);  
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * Math.PI);  
        double gg_lon = z * Math.cos(theta);  
        double gg_lat = z * Math.sin(theta);  
        double[] gcj = new double[]{gg_lon,gg_lat};
        return gcj;  
    }  
	
    /** 
     * 火星坐标系 (GCJ-02) 转换成 百度坐标系 (BD-09) 的算法 
     */  
    public static double[] gcj02_To_Bd09(double gg_lat, double gg_lon) {  
        double x = gg_lon, y = gg_lat;  
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * Math.PI);  
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * Math.PI);  
        double bd_lon = z * Math.cos(theta) + 0.0065;  
        double bd_lat = z * Math.sin(theta) + 0.006; 
        double[] bd = new double[]{bd_lon,bd_lat};
        return bd;  
    } 
    
	/**
	 * 实现定位接口，重写定位SDK接口中的监听函数
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
	}

	/**
	 * 第一次使用APP：通过该方法 进行 功能引导提示
	 */
	private void showTipMask() {
		// 创建第一个tip （取消选定的车辆 tip）
		mHightLight = new HighLight(GetCar.this)
				// .anchor(view)// 如果是Activity上增加引导层，不需要设置anchor
				.maskColor(R.color.grey)
				.addHighLight(R.id.imageView4, R.layout.tip_get_car1, new HighLight.OnPosCallback() {
					@Override
					public void getPos(float rightMargin, float bottomMargin, RectF rectF,HighLight.MarginInfo marginInfo) {
						marginInfo.leftMargin = rectF.right ;
						marginInfo.topMargin = (float) (rectF.bottom - 2.25*rectF.height());
					}
				});
		mHightLight.show();
		
	}
	
	
//****************************** 以下为百度导航 （驾车）**************** 
//	/**
//	 * 初始化导航
//	 */
//	String authinfo = null;
//	private void initNavi() {
//		BaiduNaviManager.getInstance().setNativeLibraryPath(mSDCardPath + "/BaiduNaviSDK_SO");
//		BaiduNaviManager.getInstance().init(this, mSDCardPath, APP_FOLDER_NAME, new NaviInitListener() {
//			@Override
//			public void onAuthResult(int status, String msg) {
//				if (0 == status) {
//					// authinfo = "key校验成功!";
//				} else {
//					authinfo = "key校验失败, " + msg;
//				}
//				GetCar.this.runOnUiThread(new Runnable() {
//
//					@Override
//					public void run() {
//						if (null != authinfo) { // 说明KEY校验失败
//							Toast.makeText(GetCar.this, authinfo, Toast.LENGTH_LONG).show();
//						}
//					}
//				});
//
//			}
//			public void initSuccess() {
//				// Toast.makeText(LocationDemo.this, "百度导航引擎初始化成功",
//				// Toast.LENGTH_SHORT).show();
//			}
//			public void initStart() {
//				// Toast.makeText(LocationDemo.this, "百度导航引擎初始化开始",
//				// Toast.LENGTH_SHORT).show();
//			}
//			public void initFailed() {
//				Toast.makeText(GetCar.this, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
//			}
//		}, null /* mTTSCallback */);
//	}
//	
//	/**
//	 * 规划导航路线
//	 */
//	private void routeplanToNavi() {
//		BNRoutePlanNode sNode = new BNRoutePlanNode(myLng, myLat, "我的位置", null, CoordinateType.GCJ02);
//		BNRoutePlanNode eNode = new BNRoutePlanNode(endLng,endLat,"车辆位置", null,CoordinateType.GCJ02);
//
//		if (sNode != null && eNode != null) {
//			List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
//			list.add(sNode);
//			list.add(eNode);
//			BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true, new DemoRoutePlanListener(sNode));
//		}
//	}
//
//	// 内部类 路线规划监听类(实现跳转到 诱导界面)
//	public class DemoRoutePlanListener implements RoutePlanListener {
//		
//		private BNRoutePlanNode mBNRoutePlanNode = null;
//		public DemoRoutePlanListener(BNRoutePlanNode node) {
//			mBNRoutePlanNode = node;
//		}
//		@Override
//		public void onJumpToNavigator() {
//			Intent intent = new Intent(GetCar.this, BNDemoGuideActivity.class);
//			Bundle bundle = new Bundle();
//			bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
//			intent.putExtras(bundle);
//			startActivity(intent);
//		}
//		@Override
//		public void onRoutePlanFailed() {
//		}
//	}
//	
//	/**
//	 * 初始化 存储路径
//	 */
//	private boolean initDirs() {
//		mSDCardPath = getSdcardDir();
//		if (mSDCardPath == null) {
//			return false;
//		}
//		File f = new File(mSDCardPath, APP_FOLDER_NAME);
//		if (!f.exists()) {
//			try {
//				f.mkdir();
//			} catch (Exception e) {
//				e.printStackTrace();
//				return false;
//			}
//		}
//		return true;
//	}
//	/**
//	 * 获取SD卡的外部存储路径
//	 */
//	private String getSdcardDir() {
//		if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
//			return Environment.getExternalStorageDirectory().toString();
//		}
//		return null;
//	}

	
}
