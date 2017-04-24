package com.jdyxtech.jindouyunxing.activity;

import java.io.File;
import java.io.IOException;
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

import com.baidu.android.common.logging.Log;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BaiduNaviManager.NaviInitListener;
import com.baidu.navisdk.adapter.BaiduNaviManager.RoutePlanListener;
import com.google.gson.Gson;
import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.javabean.OpenCarDoorBean;
import com.jdyxtech.jindouyunxing.javabean.OpenOrderBean;
import com.jdyxtech.jindouyunxing.javabean.OpenParkLockBean;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;
import com.squareup.picasso.Picasso;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 当前行程界面
 * @author Tom
 *
 */
public class CurrentTravel extends BaseActivity implements OnClickListener, BDLocationListener {
	// 定位相关（取车界面的定位功能，只需定位 无需展示地图）
	LocationClient mLocClient;
	BitmapDescriptor mCurrentMarker;
	private Double myLat, myLng,endLat,endLng;
	// 导航相关
	private String mSDCardPath = null;
	public static final String ROUTE_PLAN_NODE = "routePlanNode";
	public static final String TAG = "NaviSDkDemo";
	private static final String APP_FOLDER_NAME = "BNSDKDemo";
	//ui
	private ImageView carImageView;
	private TextView textView1,textView2,textView3,textView4,textView5,textView6;
	private TextView service_phone;//客服电话
	
	private ProgressDialog proDialog; //开关车门 进度对话框
	private SharedPreferences sp;
	private DefaultHttpClient defaultHttpClient = MyDefaultHttpClient.getDefaultHttpClient();

	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) { //开车位锁成功
				proDialog.cancel();
				Toast.makeText(CurrentTravel.this, "车位锁正在落下!", Toast.LENGTH_LONG).show();
			} else if (msg.what == 2) { //开车门成功
				proDialog.cancel();
				Toast.makeText(CurrentTravel.this, "车门已打开！", Toast.LENGTH_LONG).show();
			} else if (msg.what == 3){  //锁车门成功
				proDialog.cancel();
				Toast.makeText(CurrentTravel.this, "车门已锁上！", Toast.LENGTH_LONG).show();
			} else if (msg.what == 4){  //关车窗成功（为关车窗预留的4）
				//proDialog.cancel();
				//Toast.makeText(CurrentTravel.this, "车窗已锁上！", Toast.LENGTH_LONG).show();
			}else if (msg.what == 5) { //修改订单状态成功
				proDialog.cancel();
				Toast.makeText(CurrentTravel.this, "还车成功，车门已锁上！", Toast.LENGTH_LONG).show();
				Intent intent = new Intent(CurrentTravel.this,PayPreview.class);
				startActivity(intent);
				finish();
			}else if (msg.what == 6) { //各种命令,操作失败的统一处理
				proDialog.cancel();
				Toast.makeText(CurrentTravel.this, "操作失败，请重试！", Toast.LENGTH_LONG).show();
			}else if(msg.what == 7){   //网络超时 （连接超时 或 响应超时）
				proDialog.cancel(); 
				Toast.makeText(CurrentTravel.this, "网络连接超时，请重试！", Toast.LENGTH_LONG).show();
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.current_travel);
		
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
		proDialog = createDialog();// 开关车门 进度对话框
		sp = getSharedPreferences("user", MODE_PRIVATE);
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(this);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("GCJ02"); // 设置坐标类型
		option.setScanSpan(0); // 设置定位时间间隔 
		mLocClient.setLocOption(option);  //设置定位选项
		mLocClient.start();  //开启定位
		if ( initDirs() ) { //初始化存储路径
			initNavi();  //初始化导航
		}
		
		//适配展示 订单信息
		final MyApp myApp  = (MyApp) getApplication();
		textView1 = (TextView) findViewById(R.id.textView2);
		textView1.setText(myApp.getSelectParkBean().getTitle()+myApp.getSelectParkBean().getParknum()+"车位");
		textView2 = (TextView) findViewById(R.id.textView5);
		textView2.setText(myApp.getSelectParkBean().getAddr());
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
		//终点车位（通过bd09 转换为 Gcj02坐标系 得到终点的国测局坐标） 因为导航要用 GCJ02坐标系
//		double[] bd09_To_Gcj02 = GetCar.bd09_To_Gcj02(myApp.getSelectParkBean().getLoc().get(1), myApp.getSelectParkBean().getLoc().get(0));
		//后期需要：改为终点停车位 所在的停车场的坐标了
		double[] bd09_To_Gcj02 = GetCar.bd09_To_Gcj02(myApp.getDstLoc().get(1),myApp.getDstLoc().get(0));
		endLng  = bd09_To_Gcj02[0];
		endLat  = bd09_To_Gcj02[1];
	}
	
	/**
	 * 向服务器 提交 “落下车位锁”指令 的方法
	 */
	private void openParkLock() {
		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/clientcommand/park/command");
        //向服务器上传json格式的数据
        try {
    		final MyApp myApp  = (MyApp) getApplication();
        	JSONObject json = new JSONObject();
			json.put("token",sp.getString("token", ""));
			json.put("deviceNo",myApp.getSelectParkBean().getDeviceNo());
			json.put("gwid","p0001");
			json.put("updown",1); //1：down  2:up
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
			        	 OpenParkLockBean openParkLockBean = gson.fromJson(result, OpenParkLockBean.class);
			        	 if (200 == openParkLockBean.getStatus()) {
				        	 handler.sendEmptyMessage(1); //落车位锁成功，告诉主线程中的handler进行相应的操作
						}else{
				        	 handler.sendEmptyMessage(6); //落车位锁失败，告诉主线程中的handler进行相应的操作
						}
			         }
				} catch (ConnectTimeoutException e) { // 捕获 ：连接超时异常
					handler.sendEmptyMessage(7); // 通知handler进行相应的处理
				} catch (SocketTimeoutException e) { // 捕获 ：响应超时异常
					handler.sendEmptyMessage(7); // 通知handler进行相应的处理
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
	 * 向服务器提交指令， 操作车
	 * @param carCommand ：操作汽车的命令
	 */
	private void operateCar(final String carCommand) {
		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/clientcommand/car/command");
        //向服务器上传json格式的数据
        try {
    		final MyApp myApp  = (MyApp) getApplication();
        	JSONObject json = new JSONObject();
			json.put("token",sp.getString("token", ""));
			json.put("ordernum",myApp.getOrderNum());
			json.put("pw",carCommand); //1:开门 2：中途锁门 3：还车锁门
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
			        	 if (200 == openCarDoor.getStatus()) { //操作车成功（开 和 锁），告诉主线程中的handler进行相应的操作
			        		if (carCommand.equals("OPENLOCK")) { //开车门成功
			        			handler.sendEmptyMessage(2); 
							}else if (carCommand.equals("LOCK")) { //锁车门成功
								handler.sendEmptyMessage(3); 
							}
						}else{
				        	 handler.sendEmptyMessage(6); //操作失败，告诉主线程中的handler进行相应的操作
						}
			         }
				} catch (ConnectTimeoutException e) { // 捕获 ：连接超时异常
					handler.sendEmptyMessage(7); // 通知handler进行相应的处理
				} catch (SocketTimeoutException e) { // 捕获 ：响应超时异常
					handler.sendEmptyMessage(7); // 通知handler进行相应的处理
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
	 * 向服务器 操作订单 的方法（1：开启 or 2：结束 or 3：取消）
	 * 针对这个网络请求，服务器的操作是：锁车门 并 将订单状态修改为2：结束
	 */
	private void operateOrder() {
		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/inviteapi/client/putorder");
        //向服务器上传json格式的数据
        try {
    		final MyApp myApp  = (MyApp) getApplication();
        	JSONObject json = new JSONObject();
			json.put("token",sp.getString("token", ""));
			json.put("orderstatus",2);
			json.put("ordernum",myApp.getOrderNum());
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
			        	 OpenOrderBean openOrderBean = gson.fromJson(result, OpenOrderBean.class);
			        	 if (200 == openOrderBean.getStatus()) {
				        	 handler.sendEmptyMessage(5); //操作订单成功，告诉主线程中的handler进行相应的操作
						}else{
				        	 handler.sendEmptyMessage(6); //操作订单失败，告诉主线程中的handler进行相应的操作
						}
			         }
				} catch (ConnectTimeoutException e) { // 捕获 ：连接超时异常
					handler.sendEmptyMessage(7); // 通知handler进行相应的处理
				} catch (SocketTimeoutException e) { // 捕获 ：响应超时异常
					handler.sendEmptyMessage(7); // 通知handler进行相应的处理
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
	 * 创建  ProgressDialog对话框的 方法
	 */
	public ProgressDialog createDialog() {
		ProgressDialog mypDialog=new ProgressDialog(this);
		mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mypDialog.setIndeterminate(false);
		mypDialog.setCancelable(true);
		return mypDialog;
	}

	/**
	 * 弹出警告窗口2（操作车辆时的 提示窗口）
	 * flag ：区分标志，1开车门  2中途锁车门 3还车锁车门
	 * msg：对话框提示的消息
	 */
	public void alertDialog1(final int flag,String msg) {
		AlertDialog.Builder builder2 = new AlertDialog.Builder(CurrentTravel.this);
		builder2.setCancelable(false).setMessage(msg);
		/**
		 * 添加积极的按钮 text:设置button显示的文本 listener：button的监听事件
		 */
		builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if (flag == 0) { //落下车位锁
					proDialog.setTitle("落车位锁"); 
					proDialog.setMessage("正在落下车位锁，请稍候..."); 
					proDialog.show();//展示“落车位锁”进度对话框
					openParkLock(); 
				}else if (flag == 1) { //开车门
					proDialog.setTitle("打开车门"); 
					proDialog.setMessage("正在打开车门，请稍候..."); 
					proDialog.show();//展示“打开车门”进度对话框
					operateCar("OPENLOCK");
				}else if (flag == 2) { //锁车门
					proDialog.setTitle("锁车门"); 
					proDialog.setMessage("车门正在上锁，请稍候..."); 
					proDialog.show(); //展示“锁车门”进度对话框
					operateCar("LOCK");
				}else if (flag == 3) { //还车：后台锁门 并 修改订单状态为2->结束
					proDialog.setTitle("正在还车"); 
					proDialog.setMessage("车门正在上锁，请稍候..."); 
					proDialog.show(); //展示“正在还车”（锁车门）进度对话框
					operateOrder(); 
					
				} 
			}
		});
		//添加消极按钮
		builder2.setNegativeButton("取消",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
		builder2.create().show();
	}
	/**
	 * 弹出提示窗口2（物理钥匙位置 提示窗）
	 */
	public void alertDialog2() {
		View view = LayoutInflater.from(this).inflate(R.layout.key_loc, null);
		view.setBackgroundDrawable(new BitmapDrawable());// 这样设置才能点击window以外dismiss窗口
		AlertDialog.Builder builder2 = new AlertDialog.Builder(CurrentTravel.this);
		builder2.setCancelable(true);
		builder2.setView(view);
		builder2.create().show();
	}
	
	/**
	 * 点击事件监听
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back: //返回
			defaultHttpClient.getConnectionManager().shutdown();
			finish();
			break;
		case R.id.textView4:  //驾车导航
			if (BaiduNaviManager.isNaviInited()) {
				routeplanToNavi();
			}
			Toast.makeText(getApplicationContext(), "正在开启导航，请稍候！", Toast.LENGTH_LONG).show();
			break;
		case R.id.switch_r1: //“开车门” 1 
			alertDialog1(1, "确认打开车门？");
			break;
		case R.id.switch_r2: //“锁车门” 2 （中途暂时锁车门）
			alertDialog1(2, "确认锁上车门？");
			break;
		case R.id.switch_r3: //“关车窗”的相对布局
//			Toast.makeText(getApplicationContext(), "关车窗", Toast.LENGTH_LONG).show();
			break;
		case R.id.l4: //“落车位锁” 0
			final MyApp myApp = (MyApp) getApplication();
			//当用户准备 落车锁 还车时，判断用户当前定位点 距 终点车位的实际距离，在一定范围(1000m)内才能落车位锁，大于规定的距离是不允许落车位锁的
			if (GetCar.checkDistance(100000,myLat, myLng,myApp.getSelectParkBean().getLoc().get(1), myApp.getSelectParkBean().getLoc().get(0))) { //用户 距 终点车位 的距离 在1000米以内
				alertDialog1(0, "确认落下车位锁？");//展示alertDialog对话框
			}else { //如果：距 终点车位锁 1000米 以外，就弹出安全提示框
				new AlertDialog.Builder(CurrentTravel.this).setTitle("安全提示").setMessage("您距终点位置还较远，为安全起见请勿提前落下车位锁！").create().show();
			}
			break;
		case R.id.textView12: //“还车”：3（锁车门，然后跳到支付预览界面）
			alertDialog1(3, "请您确保：\n     1、将车停入指定车位\n     2、携带好随身物品\n     3、您已经在车外");
			break;
		case R.id.l3: //(物理钥匙位置)
			alertDialog2(); //弹出 物理钥匙位置 提示窗口
			break;
		case R.id.service_phone: //客服电话
			startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ((MyApp) getApplication()).getTel())));
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

	
//****************currentTravel********以下wei百度导航*****************

	/**
	 * 通过定位监听函数,获取定位信息
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
	 * 初始化导航
	 */
	String authinfo = null;
	private void initNavi() {
		BaiduNaviManager.getInstance().setNativeLibraryPath(mSDCardPath + "/BaiduNaviSDK_SO");
		BaiduNaviManager.getInstance().init(this, mSDCardPath, APP_FOLDER_NAME, new NaviInitListener() {
			@Override
			public void onAuthResult(int status, String msg) {
				if (0 == status) {
					// authinfo = "key校验成功!";
				} else {
					authinfo = "key校验失败, " + msg;
				}
				CurrentTravel.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (null != authinfo) { // 说明KEY校验失败
							Toast.makeText(CurrentTravel.this, authinfo, Toast.LENGTH_LONG).show();
						}
					}
				});

			}
			public void initSuccess() {
				// Toast.makeText(LocationDemo.this, "百度导航引擎初始化成功",
				// Toast.LENGTH_SHORT).show();
			}
			public void initStart() {
				// Toast.makeText(LocationDemo.this, "百度导航引擎初始化开始",
				// Toast.LENGTH_SHORT).show();
			}
			public void initFailed() {
				Toast.makeText(CurrentTravel.this, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
			}
		}, null /* mTTSCallback */);
	}
	
	/**
	 * 规划导航路线
	 */
	private void routeplanToNavi() {
		BNRoutePlanNode sNode = new BNRoutePlanNode(myLng,myLat, "我的位置", null, CoordinateType.GCJ02);
		BNRoutePlanNode eNode = new BNRoutePlanNode(endLng, endLat, "终点车位", null, CoordinateType.GCJ02);

		if (sNode != null && eNode != null) {
			List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
			list.add(sNode);
			list.add(eNode);
			BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true, new DemoRoutePlanListener(sNode));
		}
	}

	// 内部类 路线规划监听类(实现跳转到 诱导界面)
	public class DemoRoutePlanListener implements RoutePlanListener {
		
		private BNRoutePlanNode mBNRoutePlanNode = null;
		public DemoRoutePlanListener(BNRoutePlanNode node) {
			mBNRoutePlanNode = node;
		}
		@Override
		public void onJumpToNavigator() {
			Intent intent = new Intent(CurrentTravel.this, BNDemoGuideActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
			intent.putExtras(bundle);
			startActivity(intent);
		}
		@Override
		public void onRoutePlanFailed() {
			// TODO Auto-generated method stub
		}
	}
	
	/**
	 * 初始化 存储路径
	 */
	private boolean initDirs() {
		mSDCardPath = getSdcardDir();
		if (mSDCardPath == null) {
			return false;
		}
		File f = new File(mSDCardPath, APP_FOLDER_NAME);
		if (!f.exists()) {
			try {
				f.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	/**
	 * 获取SD卡的外部存储路径
	 */
	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}

	
}
