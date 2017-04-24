package com.jdyxtech.jindouyunxing.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

import com.google.gson.Gson;
import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.adapter.MyTravelListAdapter;
import com.jdyxtech.jindouyunxing.javabean.CarBean;
import com.jdyxtech.jindouyunxing.javabean.GetTravelListBean;
import com.jdyxtech.jindouyunxing.javabean.MyTravelBean;
import com.jdyxtech.jindouyunxing.javabean.ParkBean;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
/**
 * 历史订单列表界面
 * @author Tom
 *
 */
public class MyTravelList extends Activity implements OnClickListener,OnScrollListener,OnItemClickListener{
	private SharedPreferences sp;
	private ListView travel_listView;
	private MyTravelListAdapter adapter;
	private GetTravelListBean get_TravelListBean ;
	private List<MyTravelBean> list = new ArrayList<MyTravelBean>();
	private ProgressDialog proDialog; //加载订单 进度对话框
	private boolean isLast = false;
	private int start = 0;
	private DefaultHttpClient defaultHttpClient = MyDefaultHttpClient.getDefaultHttpClient();

	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (1 == msg.what) {
				list.addAll(get_TravelListBean.getLists());
				adapter.notifyDataSetChanged();
				proDialog.cancel();
//				if (start == 0) { //第一次 加载数据
//					adapter = new MyTravelListAdapter(MyTravelList.this, get_TravelListBean.getLists()); //适配器
//					travel_listView.setAdapter(adapter); //绑定适配器
//				}else { //上拉 加载下一页数据
//					adapter.setData(list); //调用 adapter中 添加新数据的方法
//				}
			}else if (0 == msg.what) {
				proDialog.cancel();
				Toast.makeText(MyTravelList.this, "订单加载失败，请重试！", Toast.LENGTH_SHORT).show();
			}else if (2 == msg.what) {
				proDialog.cancel();
				Toast.makeText(MyTravelList.this, "没有更多订单数据！", Toast.LENGTH_SHORT).show();
			}else if(msg.what == 3){   //网络超时 （连接超时 或 响应超时）
				proDialog.cancel(); 
				Toast.makeText(MyTravelList.this, "网络连接超时，请重试！", Toast.LENGTH_LONG).show();
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_travel_list);

		init(); //初始化操作： 组件 、适配器、以及绑定适配器
		getTravelList(); //请求订单列表数据 
		
		travel_listView.setOnScrollListener(this);//给travel_listView 注册滑动监听
		travel_listView.setOnItemClickListener(this);//给travel_listView注册 item点击监听
		
	}

	/**
	 * 相关变量初始化 及 组件初始化
	 */
	private void init() {
		sp = getSharedPreferences("user", MODE_PRIVATE);
		proDialog = createDialog(); //加载进度对话框
		proDialog.show();
		//初始化 组件 适配器，并绑定适配器
		travel_listView = (ListView) findViewById(R.id.listView1); //组件
		adapter = new MyTravelListAdapter(MyTravelList.this, list); //适配器
		travel_listView.setAdapter(adapter); //绑定适配器

	}
	/**
	 *  请求服务器 ，获取订单列表
	 */
	public void getTravelList() {

		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/inviteapi/client/listorder");
        //向服务器上传json格式的登陆数据
        try {
        	JSONObject json = new JSONObject();
			json.put("token",sp.getString("token", ""));
			json.put("step", 10); //一次加载10条
			json.put("start", start); //开始加载的位置
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
			        	 GetTravelListBean getTravelListBean = gson.fromJson(result, GetTravelListBean.class);
			        	 if (200 == getTravelListBean.getStatus()) { //成功，告诉主线程中的handler进行相应的操作
			        		 get_TravelListBean = getTravelListBean;
			        		 handler.sendEmptyMessage(1); 
			        	 }else if(405==getTravelListBean.getStatus()||406==getTravelListBean.getStatus()){
			        		 handler.sendEmptyMessage(2);  //已无 更多数据，告诉主线程中的handler进行相应的操作
			        	 }else { //失败，告诉主线程中的handler进行相应的操作
			        		 handler.sendEmptyMessage(0); 
						}
			         }
				} catch (ConnectTimeoutException e) { // 捕获 ：连接超时异常
					handler.sendEmptyMessage(3); // 通知handler进行相应的处理
				} catch (SocketTimeoutException e) { // 捕获 ：响应超时异常
					handler.sendEmptyMessage(3); // 通知handler进行相应的处理
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
	 * travel_listView 滑动时 回调的两个方法
	 * @param view
	 * @param firstVisibleItem
	 * @param visibleItemCount
	 * @param totalItemCount
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		isLast = (firstVisibleItem +visibleItemCount == totalItemCount);
	}
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (isLast&&scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			proDialog.show();
			start += 10;
			getTravelList();
		}
	}
	/**
	 * travel_listView中的item被点击时回调的方法
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		setData(position); //将item中的订单数据信息 赋值给全局变量--myApp
		Intent intent = new Intent();
		if (list.get(position).getOrderstatus() == 0) { // 0 ：预订单
			intent.setClass(MyTravelList.this, YouOrder.class);
			startActivity(intent);
			finish(); //点击某一个订单之后，关闭订单列表界面
		}else if (list.get(position).getOrderstatus() == 1) { //1 :已经开启的订单
			if (list.get(position).getOpendoor_at() == 0) {//还没有第一次开启过车门
				intent.setClass(MyTravelList.this, GetCar.class);
			}else if (list.get(position).getOpendoor_at() > 0) {//已经第一次开启车门
				intent.setClass(MyTravelList.this, CurrentTravel.class);
			} 
			startActivity(intent);
			finish(); //点击某一个订单之后，关闭订单列表界面
		}else if (list.get(position).getOrderstatus() == 2) { //2 :结束的订单
			if (list.get(position).getPaystatus() == 0 ) { //0：未支付
				intent.setClass(MyTravelList.this, PayPreview.class); //未支付：跳转到支付预览界面
			}else { //1、2、3:已支付
				intent.setClass(MyTravelList.this, MyTravel.class); //已支付：跳转到历史订单查看界面
			}
			startActivity(intent);
			finish(); //点击某一个订单之后，关闭订单列表界面
		}else if (list.get(position).getOrderstatus() == 3) { //3:手动取消的订单
			Toast.makeText(MyTravelList.this, "你已经取消了此订单，没有出行详情信息！", Toast.LENGTH_LONG).show();
		}else if (list.get(position).getOrderstatus() == 4) { //4:订单超时，系统自动取消订单
			Toast.makeText(MyTravelList.this, "由于订单超时，系统已自动取消了该订单！", Toast.LENGTH_LONG).show();
		}
	}
	/**
	 * 此方法用于:将历史订单列表中的某个item中订单数据 赋值给全局变量 --myApp
	 * @param position
	 */
	public void setData(int position) {
		final MyApp myApp  = (MyApp) getApplication();
		myApp.setOrderNum(list.get(position).getOrder_num()); //订单号
		myApp.setStartTime(list.get(position).getStarttime()); //string类型的订单开时间
		//根据String 类型的订单开始时间，转换并设置长整型的订单开始时间
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date date = format.parse(list.get(position).getStarttime());
			myApp.setLongStartTime(date.getTime()); //long类型订单开时间
		} catch (ParseException e) {
			e.printStackTrace();
		}
		myApp.setAssess(list.get(position).getAssess());//是否评论 0为未评轮 1为已评
		myApp.setOrgName(list.get(position).getOrg_name()); //起点名
		myApp.setDstName(list.get(position).getDst_name()); //终点名
		myApp.setFree(list.get(position).getFree()); //行程总费用
		myApp.setDist(list.get(position).getDist()); //行程公里数
		myApp.setDisttime(list.get(position).getDisttime()); //行程用时
		myApp.setOpendoor_at(list.get(position).getOpendoor_at()); //第一次开车门时间，0：还未打开过车门
		myApp.setActivity_free(list.get(position).getActivity_free()); //优惠券抵扣费用
		myApp.setIntegral_free(list.get(position).getDeduction_free()); //积分抵扣费用
		myApp.setCha_time(list.get(position).getCha_time()); //超时时间，0：未超时
		//车辆信息
		CarBean selectCarBean = new CarBean();
		selectCarBean.setAddr(list.get(position).getOrg().getAddr());
		selectCarBean.setTitle(list.get(position).getOrg().getTitle());
		selectCarBean.setParknum(list.get(position).getOrg().getParknum());
		selectCarBean.setBig(list.get(position).getOrg().getBig());
		selectCarBean.setSmall(list.get(position).getOrg().getSmall());
		selectCarBean.setBrand(list.get(position).getOrg().getBrand());
		selectCarBean.setModel(list.get(position).getOrg().getModel());
		selectCarBean.setSeats(list.get(position).getOrg().getSeats());
		selectCarBean.setCarli(list.get(position).getOrg().getCarli());
		selectCarBean.setRange(list.get(position).getOrg().getRange());
		selectCarBean.setHighspeed(list.get(position).getOrg().getHighspeed());
		selectCarBean.setNeat(list.get(position).getOrg().getNeat()+"");
		List<Double> loc_list = new ArrayList<Double>();
		loc_list.add(0, list.get(position).getOrg().getLoc());  //服务器返回的是 起点停车场的坐标
		loc_list.add(1, list.get(position).getOrg().getLat());
		selectCarBean.setLoc(loc_list);
		myApp.setSelectCarBean(selectCarBean);
		//终点车位信息
		ParkBean selectParkBean = new ParkBean();
		selectParkBean.setAddr(list.get(position).getDst().getAddr());
		selectParkBean.setTitle(list.get(position).getDst().getTitle());
		selectParkBean.setParknum(list.get(position).getDst().getParknum());
		selectParkBean.setDeviceNo(list.get(position).getDst().getDeviceno());
		selectParkBean.setGwid(list.get(position).getDst().getGwid());
		List<Double> loc_list2 = new ArrayList<Double>();
		loc_list2.add(0, list.get(position).getDst().getLoc()); //服务器返回的是 终点停车场的坐标
		loc_list2.add(1, list.get(position).getDst().getLat());
		selectParkBean.setLoc(loc_list2);
		myApp.setSelectParkBean(selectParkBean);
		//起终点坐标信息
		myApp.setStrLoc(loc_list);  //起点停车场的坐标
		myApp.setDstLoc(loc_list2);  //终点停车场的坐标
	}
	/**
	 * 创建 “正在加载订单列表” ProgressDialog对话框的 方法
	 */
	public ProgressDialog createDialog() {
		ProgressDialog mypDialog=new ProgressDialog(this);
		mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mypDialog.setTitle("加载订单列表");
		mypDialog.setMessage("正在加载订单列表，请您耐心等候...");
		mypDialog.setIndeterminate(false);
		mypDialog.setCancelable(true);
		return mypDialog;
	}
	// 点击监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back:
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
}
