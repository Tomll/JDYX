package com.jdyxtech.jindouyunxing.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
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

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.google.gson.Gson;
import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.javabean.CalculateFreeBean;
import com.jdyxtech.jindouyunxing.javabean.CarBean;
import com.jdyxtech.jindouyunxing.javabean.ParkBean;
import com.jdyxtech.jindouyunxing.javabean.SubmitOrderBean;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;
import com.squareup.picasso.Picasso;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Window;
/**
 * 提交订单界面
 * @author Tom
 *
 */
public class SubmitOrder extends BaseActivity implements OnClickListener,OnGetRoutePlanResultListener{
	
	MapView mMapView;
	BaiduMap mBaiduMap;
	private CarBean selectCarBean;
	private ParkBean selectParkBean;
	private List<Double> strLoc; //后期服务器需要添加：起点停车场的坐标
	private List<Double> dstLoc; //后期服务器需要添加：终点停车场的坐标
	private SharedPreferences sp;
	private ProgressDialog progressDialog;
	private SubmitOrderBean submit_OrderBean;
	//路线规划( 费用预估用)
	private RoutePlanSearch mSearch;
	private TextView textView12,textView13,textView14; //预估费用
	private double dist=0;//百度服务器返回的 预估距离
	private float free=0,predist=0,predisttime=0; //预估 出行费用 、距离、时间
	private int flag = 2; //预估费用 各种状态 ，默认为2预估中；1预估失败、 0预估成功
	private Boolean flag2 = false; //flag2用于标志 是否defaultHttpClient.getConnectionManager().shutdown()
	private DefaultHttpClient defaultHttpClient = MyDefaultHttpClient.getDefaultHttpClient(); 

	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) { //表示提交成功
				progressDialog.cancel(); //关闭 进度框
				Toast.makeText(SubmitOrder.this, "订单提交成功！", Toast.LENGTH_LONG).show();
				final MyApp myApp  = (MyApp) getApplication();
				myApp.setOrderNum(submit_OrderBean.getOrdernum());//记录订单编号
				myApp.setFree(free);
				myApp.setDist(predist);
				myApp.setDisttime(predisttime);
				Intent intent = new Intent(SubmitOrder.this,YouOrder.class);
				startActivity(intent);
				if (CarStateMap.carStateMapActivity!=null) {
					CarStateMap.carStateMapActivity.finish();
				}
				if (ParkStateMap.parkStateMapActivity!=null) {
					ParkStateMap.parkStateMapActivity.finish();
				}
				finish();
			}else if (msg.what == 4) { //下手太慢，车辆已经被别人预定了
				progressDialog.cancel(); 
				Toast.makeText(SubmitOrder.this, "您选的车辆已被别人预定，请重新选择车辆！", Toast.LENGTH_LONG).show();
			}else if (msg.what == 5) { //下手太慢，车位已经被别人预定了
				progressDialog.cancel(); 
				Toast.makeText(SubmitOrder.this, "您选的车位已被别人预定，请重新选择车位！", Toast.LENGTH_LONG).show();
			}else if (msg.what == 0) {  //表示提交失败
				progressDialog.cancel(); 
				Toast.makeText(SubmitOrder.this, "订单提交失败，请重新选择车辆！", Toast.LENGTH_LONG).show();
			}else if (msg.what == 2) {  //费用计算 成功
	        	textView14.setText("预估费用："+free+"元"); //展示预估费用
	        	flag = 0; //预估费用计算成功
			}else if (msg.what == 3) {  //费用计算 失败
	        	textView14.setText("--"); //预估费用
	        	flag = 1; //预估费用计算失败
			}else if(msg.what == 6){   //网络超时 （连接超时 或 响应超时）
				progressDialog.cancel(); //关闭 进度框
				Toast.makeText(SubmitOrder.this, "网络连接超时，请重试！", Toast.LENGTH_LONG).show();
			}
 
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.submit_order);

		init(); //初始化
		onBund(); //选定的车、车位信息的适配
	}
	
	/**
	 * 现场保护（注意：这并不是Activity的一个生命周期，只是应用在后台运行时，该Activity将要被系统回收掉的时候 会执行这个方法）
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState); //在父类BaseActivity的onSaveInstanceState()方法中，执行了 全局变量myApp的 保存工作
	}
	
	private void init() {
		//显示  预估信息 的文本框
		textView12 = (TextView) findViewById(R.id.textView9);
		textView13 = (TextView) findViewById(R.id.textView10);
		textView14 = (TextView) findViewById(R.id.textView11);
		
		progressDialog = createDialog();
		sp = getSharedPreferences("user", MODE_PRIVATE);
		final MyApp myApp  = (MyApp) getApplication();
		selectCarBean = myApp.getSelectCarBean();
		selectParkBean = myApp.getSelectParkBean();
		
//		//创建驾车线路规划检索实例
//		RoutePlanSearch mSearch = RoutePlanSearch.newInstance();
//		//设置驾车线路规划检索监听者；
//		mSearch.setOnGetRoutePlanResultListener(this);
//		//准备检索起、终点信息；
//		PlanNode stNode = PlanNode.withLocation(new LatLng(selectCarBean.getLoc().get(1), selectCarBean.getLoc().get(0)));  
//		PlanNode enNode = PlanNode.withLocation(new LatLng(selectParkBean.getLoc().get(1),selectParkBean.getLoc().get(0)));
//		//发起 驾车 线路规划检索；
//		mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));
	}
	//订单信息的适配
	private void onBund() {
		//选定的车 车图，picasso网络加载
		ImageView carImageView = (ImageView) findViewById(R.id.imageView1);
		Picasso.with(this).load(selectCarBean.getSmall()).into(carImageView);
		//选定的车的品牌
		TextView carBrand_tv = (TextView) findViewById(R.id.textView1);
		carBrand_tv.setText(selectCarBean.getBrand()); 
		//选定的车的车型
		TextView carModel_tv = (TextView) findViewById(R.id.textView2);
		carModel_tv.setText(selectCarBean.getModel()); 
		//选定的车的座位数
		TextView carSeats_tv = (TextView) findViewById(R.id.textView3);
		carSeats_tv.setText(selectCarBean.getSeats()+"座"); 
		//选定的车的车牌号
		TextView carid_tv = (TextView) findViewById(R.id.textView4);
		carid_tv.setText(selectCarBean.getCarli()); 
		//选定的车的车辆 续航
		TextView car_power = (TextView) findViewById(R.id.textView14);
		car_power.setText("车辆续航里程："+selectCarBean.getRange()+"公里"); 
		//选定的车辆 距 起点位置距离
		TextView car_dis = (TextView) findViewById(R.id.textView13);
		car_dis.setText("距起点"+String.format("%.2f", selectCarBean.getDis()*0.001)+"公里"); 
		
		//选定的 停车场及车位
		TextView parkName = (TextView) findViewById(R.id.textView8);
		parkName.setText(selectParkBean.getTitle()+selectParkBean.getParknum()+"车位"); 
		//选定的 停车场 距离 终点 的距离
		TextView parkDis = (TextView) findViewById(R.id.textView7);
		parkDis.setText("距终点"+String.format("%.2f", selectParkBean.getDis()*0.001)+"公里"); 
	}

	/**
	 * 向服务器 提交 预订单（orderState = 0） 的方法
	 */
	private void submitOrder() {
		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/inviteapi/client/neworder");
        //向服务器上传json格式的数据
        try {
    		final MyApp myApp  = (MyApp) getApplication();
        	JSONObject json = new JSONObject();
			json.put("token",sp.getString("token", ""));
//			json.put("phone",sp.getString("uname", null));
			json.put("free", free);
			json.put("predist", predist);
			json.put("predisttime", predisttime);
			json.put("cidenty",System.currentTimeMillis()+"");
			json.put("org_name",myApp.getOrgName());
			json.put("dst_name",myApp.getDstName());
			
			JSONObject jsonObject_org = new JSONObject();
			jsonObject_org.putOpt("power", selectCarBean.getPower());
			jsonObject_org.putOpt("card", selectCarBean.getCarli());
			jsonObject_org.putOpt("lat", myApp.getStrLoc().get(1)); //起点停车场的坐标 纬度
			jsonObject_org.putOpt("loc", myApp.getStrLoc().get(0)); //起点停车场的坐标 纬度
			json.put("org", jsonObject_org);
			
			JSONObject jsonObject_dst = new JSONObject();
			jsonObject_dst.putOpt("deviceno", selectParkBean.getDeviceNo());
			jsonObject_dst.putOpt("gwid", selectParkBean.getGwid());
			jsonObject_dst.putOpt("lat", myApp.getDstLoc().get(1)); //终点停车场的坐标 纬度
			jsonObject_dst.putOpt("loc", myApp.getDstLoc().get(0)); //终点停车场的坐标 经度
			jsonObject_dst.putOpt("title", selectParkBean.getTitle()); //停车场的名字
			json.put("dst", jsonObject_dst);
			
			json.put("dist", dist);
			json.put("starttime", myApp.getStartTime());
			json.put("peoplenum", myApp.getPeopleNum());
			StringEntity se = new StringEntity(json.toString(),HTTP.UTF_8);
			se.setContentEncoding(new BasicHeader(HTTP.UTF_8, "application/json"));
			request.setEntity(se);
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}catch (JSONException e1) {
			e1.printStackTrace();
		}
        //开启一个线程，进行post带参网络请求（登录）
        new Thread(new Runnable() {
			@Override
			public void run() {
	          	try {  
            		HttpResponse httpResponse = defaultHttpClient.execute(request); 
			         if(httpResponse.getStatusLine().getStatusCode() == 200){
			        	 String result = EntityUtils.toString(httpResponse.getEntity());
			        	 Gson gson  = new Gson();
			        	 submit_OrderBean = gson.fromJson(result, SubmitOrderBean.class);
			        	 if (200 == submit_OrderBean.getStatus()) { //订单提交成功，告诉主线程中的handler进行相应的操作
				        	 handler.sendEmptyMessage(1); 
						}else if(406 == submit_OrderBean.getStatus()){ //下手太慢，车辆或者 车位已经别人预定了
							if(submit_OrderBean.getError() == 1){ //车 已被被人预定
								handler.sendEmptyMessage(4); 
							}else if (submit_OrderBean.getError() == 2) { //车位 已被别人预定
								handler.sendEmptyMessage(5); 
							}
						}else {  //提交失败，告诉主线程中的handler进行相应的操作
							handler.sendEmptyMessage(0); 
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
		}).start(); 
		
	}
	/**
	 * 向服务器提交  进行订单 “费用计算” 
	 */
	private void calculateFree() {
		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/inviteapi/client/free");
        //向服务器上传json格式的数据
        try {
    		final MyApp myApp  = (MyApp) getApplication();
        	JSONObject json = new JSONObject();
			json.put("token",sp.getString("token", ""));
			json.put("brand",myApp.getSelectCarBean().getBrand());
			json.put("model",myApp.getSelectCarBean().getModel());
			json.put("starttime",myApp.getStartTime());
			json.put("dist",dist);
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
			        	 CalculateFreeBean calculateFreeBean = gson.fromJson(result, CalculateFreeBean.class);
			        	 if (200 == calculateFreeBean.getStatus()) {
			        		 free = calculateFreeBean.getFree();
				        	 handler.sendEmptyMessage(2); //费用计算成功，告诉主线程中的handler进行相应的操作
						}else{
				        	 handler.sendEmptyMessage(3); //费用计算失败，告诉主线程中的handler进行相应的操作
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
	 * 路线规划监听 回调方法
	 */
	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
        	//Toast.makeText(SubmitOrder.this, "抱歉，未计算出预估信息！", Toast.LENGTH_SHORT).show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
        	//flag2的作用：如果connection已经shutdown了 还去执行calculateFree()会报错：connectionManager has shutdown
        	//另外flag2可以防止：如果已经返回到前一个页面，但该页面的路线规划成功了，然后执行 textView12.setText 会报空指针错误
        	if (!flag2) {
        		dist =result.getRouteLines().get(0).getDistance();
        		calculateFree(); // 将百度预估的 里程数 提交给公司服务器 进行费用计算
        		DecimalFormat df = new DecimalFormat("0.00");
        		textView12.setText("预估时间："+df.format(result.getRouteLines().get(0).getDuration()/60)+"分钟"); //预估时间
        		textView13.setText("预估里程："+df.format(result.getRouteLines().get(0).getDistance()*0.001)+"公里"); //预估距离
        		predisttime = result.getRouteLines().get(0).getDuration()/60;
        		predist = (float) (result.getRouteLines().get(0).getDistance()*0.001);
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
	 * 创建 “正在提交订单” 的ProgressDialog对话框的 方法
	 */
	public ProgressDialog createDialog() {
		ProgressDialog mypDialog=new ProgressDialog(this);
		mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mypDialog.setTitle("提交订单");
		mypDialog.setMessage("正在提交订单，请您耐心等候...");
		mypDialog.setIndeterminate(false);
		mypDialog.setCancelable(true);
		return mypDialog;
	}
	//点击监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.del_car: //取消预约 车辆 按钮
			defaultHttpClient.getConnectionManager().shutdown();
			flag2 = true;
			ParkStateMap.parkStateMapActivity.finish();
			finish();
			break;
		case R.id.del_park: //取消预约 车位 按钮
			defaultHttpClient.getConnectionManager().shutdown();
			flag2 = true;
			finish();
			break;
		case R.id.button_back: //返回按钮
			defaultHttpClient.getConnectionManager().shutdown();
			flag2 = true;
			finish();
			break;
		case R.id.submit_order: //提交订单按钮
			if (flag == 2) { //费用预估中
				strRoutPlan();
				Toast.makeText(SubmitOrder.this, "正在预估费用，请稍候！", Toast.LENGTH_LONG).show();
			}else if (flag == 0||flag == 1) { //费用预估成功 或者 失败 ，都会提交订单
				progressDialog.show();
				submitOrder();			
			}
			break;
		default:
			break;
		}
	}
	
	
	/**
	 * 开启路线规划 的方法
	 */
	public void strRoutPlan() {
		//创建驾车线路规划检索实例
		mSearch = RoutePlanSearch.newInstance();
		//设置驾车线路规划检索监听者；
		mSearch.setOnGetRoutePlanResultListener(this);
		//准备检索起、终点信息；
		final MyApp myApp  = (MyApp) getApplication();
		PlanNode stNode = PlanNode.withLocation(new LatLng(myApp.getSelectCarBean().getLoc().get(1), myApp.getSelectCarBean().getLoc().get(0)));  
		PlanNode enNode = PlanNode.withLocation(new LatLng(myApp.getSelectParkBean().getLoc().get(1),myApp.getSelectParkBean().getLoc().get(0)));
		//发起 驾车 线路规划检索；
		mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		strRoutPlan(); //调用 开启路线规划的方法
	}
	//返回键
	@Override
	public void onBackPressed() {
		defaultHttpClient.getConnectionManager().shutdown();
		flag2 = true;
		finish();
	}

}
