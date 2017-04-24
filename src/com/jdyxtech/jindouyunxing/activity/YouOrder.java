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

import com.baidu.mapapi.SDKInitializer;
import com.google.gson.Gson;
import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.javabean.OpenOrderBean;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;
import com.squareup.picasso.Picasso;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Window;
/**
 * 订单取消 或 确认 界面，订单提交成功后 跳转到该界面
 * @author Tom
 *
 */
public class YouOrder extends BaseActivity implements OnClickListener{
	private SharedPreferences sp;
	private ImageView carImageView;
	private ProgressDialog proDialog; //操作订单 进度对话框
//	private boolean flag ;
	private int orderstatus ;
	private int hours,minutes;
	private long totalTime; //距离预约出行时间 剩余的总时间 
	public static YouOrder youOrder;
	private DefaultHttpClient defaultHttpClient = MyDefaultHttpClient.getDefaultHttpClient();
	private TextView textView1,textView2,textView3,textView4,textView5,textView6,textView7,textView8,textView9,textView10,textView11,textView12,textView13,textView19,textView23;

	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) { //订单操作成功
				proDialog.cancel();
				if (orderstatus == 1) { //开启订单成功
					Toast.makeText(YouOrder.this, "开始您的行程吧！", Toast.LENGTH_LONG).show();
					Intent intent = new Intent(YouOrder.this,GetCar.class);
					startActivity(intent);
					finish();
				}else {  //取消订单成功
					Toast.makeText(YouOrder.this, "订单取消成功！", Toast.LENGTH_LONG).show();
					if (CarStateMap.carStateMapActivity!=null) {
						CarStateMap.carStateMapActivity.finish();
					}
					if (ParkStateMap.parkStateMapActivity!=null) {
						ParkStateMap.parkStateMapActivity.finish();
					}
					finish();
				}
			}else if(msg.what == 0) {   //订单操作失败
				proDialog.cancel();
				if (orderstatus == 1) {
					Toast.makeText(YouOrder.this, "行程开启失败！", Toast.LENGTH_LONG).show();
				}else {
					Toast.makeText(YouOrder.this, "订单取消失败！", Toast.LENGTH_LONG).show();
				}
			}else if (msg.what == 2) { //更新倒计时
				textView19.setText(hours+"");
				textView23.setText(minutes+"");
			}else if (msg.what == 3) { //预约出行时间 已过
				if (YouOrder.youOrder != null) {
					popAlertDialog2(); //弹出超时提示框
				}
			}else if(msg.what == 4){   //网络超时 （连接超时 或 响应超时）
				proDialog.cancel(); //关闭 进度框
				Toast.makeText(YouOrder.this, "网络连接超时，请重试！", Toast.LENGTH_LONG).show();
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(YouOrder.this); // 一定要有此 百度地图初始化操作，否则无法使用百度地图
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.your_order);
		
		initView(); // 初始化
	}
	
	/**
	 * 现场保护（注意：这并不是Activity的一个生命周期，只是应用在后台运行时，该Activity将要被系统回收掉的时候 会执行这个方法）
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState); //在父类BaseActivity的onSaveInstanceState()方法中，执行了 全局变量myApp的 保存工作
	}
	
	/**
	 * 初始化的方法
	 */
	private void initView() {
		youOrder = this;
		final MyApp myApp  = (MyApp) getApplication();
		sp = getSharedPreferences("user", MODE_PRIVATE);
		proDialog = createProgressDialog();
		//出行时间 及 倒计时
		textView1 = (TextView) findViewById(R.id.textView2); //订单开始时间TextView
		textView1.setText(myApp.getStartTime()); //
		textView19 = (TextView) findViewById(R.id.textView19); //倒计时中的小时
		textView23 = (TextView) findViewById(R.id.textView23); //倒计时中的分钟
		totalTime = myApp.getLongStartTime() - System.currentTimeMillis() + 60*1000; //订单预约开始时间 - 当前时间 = 剩余的总时间(ms)，
		//此线程用于更新 距离订单开始时间倒计时
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (totalTime >= 60 * 1000) {// 写为 >= 60s为了在最后一分钟，时间到的时候，准时弹出超时提示框（因为用户只能看到小时、分钟，看不到秒）
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					totalTime -= 1000;
					hours = (int) Math.floor((totalTime / 60000) / 60);
					minutes = (int) ((totalTime / 60000) % 60);
					handler.sendEmptyMessage(2); // 通知更新倒计时
				}
				//Activityon在Destroy之后， isFinishing()返回 true
				if (!YouOrder.this.isFinishing()) {  //YouOrder如果没有finish,那就弹出提示框。 如果已经finish了，就不弹出了
					handler.sendEmptyMessage(3); // 通知 已超时，弹出提示框
				}
			}
		}).start();
				
		textView2 = (TextView) findViewById(R.id.textView5);
		textView2.setText(myApp.getOrgName());
		textView3 = (TextView) findViewById(R.id.textView6);
		textView3.setText(myApp.getSelectCarBean().getTitle()+myApp.getSelectCarBean().getParknum()+"车位");
		textView4 = (TextView) findViewById(R.id.textView7);
		textView4.setText(myApp.getSelectParkBean().getTitle()+myApp.getSelectParkBean().getParknum()+"车位");
		textView5 = (TextView) findViewById(R.id.textView8);
		textView5.setText(myApp.getDstName());
		
		carImageView = (ImageView) findViewById(R.id.imageView8);
		Picasso.with(YouOrder.this).load(myApp.getSelectCarBean().getSmall()).into(carImageView);
		textView6 = (TextView) findViewById(R.id.textView10);
		textView6.setText(myApp.getSelectCarBean().getBrand());
		textView7 = (TextView) findViewById(R.id.textView9);
		textView7.setText(myApp.getSelectCarBean().getModel());
		textView8 = (TextView) findViewById(R.id.textView11);
		textView8.setText("舒适型");
		textView9 = (TextView) findViewById(R.id.textView12);
		textView9.setText(" "+myApp.getSelectCarBean().getCarli()+" ");
		textView10 = (TextView) findViewById(R.id.textView18);
		textView10.setText(myApp.getSelectCarBean().getSeats()+"座");
		textView11 = (TextView) findViewById(R.id.textView20);
		textView11.setText(myApp.getSelectCarBean().getRange()+"公里");
		textView12 = (TextView) findViewById(R.id.textView22);
		textView12.setText(myApp.getSelectCarBean().getNeat()+"级");
		textView13 = (TextView) findViewById(R.id.textView24);
		textView13.setText(myApp.getSelectCarBean().getHighspeed()+"公里/小时");
	}
	
	
	/**
	 * 向服务器 操作订单状态 的方法（1：开启 or 2：结束 or 3：取消）
	 */
	private void operateOrder() {
		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/inviteapi/client/putorder");
        //向服务器上传json格式的数据
        try {
    		final MyApp myApp  = (MyApp) getApplication();
        	JSONObject json = new JSONObject();
			json.put("token",sp.getString("token", ""));
			json.put("orderstatus",orderstatus);
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
			        		 handler.sendEmptyMessage(1); //操作成功，告诉主线程中的handler进行相应的操作
						}else{
				        	 handler.sendEmptyMessage(0); //操作失败，告诉主线程中的handler进行相应的操作
						}
			         }
				} catch (ConnectTimeoutException e) { // 捕获 ：连接超时异常
					handler.sendEmptyMessage(4); // 通知handler进行相应的处理
				} catch (SocketTimeoutException e) { // 捕获 ：响应超时异常
					handler.sendEmptyMessage(4); // 通知handler进行相应的处理
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
	 * 创建操作订单时的 ProgressDialog对话框的 方法
	 */
	public ProgressDialog createProgressDialog() {
		ProgressDialog mypDialog=new ProgressDialog(this);
		mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mypDialog.setTitle("操作订单");
		mypDialog.setMessage("正在执行订单操作，请稍候...");
		mypDialog.setIndeterminate(false);
		mypDialog.setCancelable(true);
		return mypDialog;
	}
	
	/**
	 * 弹出提示窗口 (订单操作（取消 或 开启） 提示对话框) 
	 */
	public void popAlertDialog(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(YouOrder.this);
		builder.setMessage(msg);
		//设置 积极按钮 
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				proDialog.show();
				operateOrder(); //向服务器提交 操作订单 的命令
			}
		});
		//设置 消极按钮 
		builder.setNegativeButton("取消",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

			}
		});
		builder.create().show();;
	}
	
	/**
	 * 弹出提示窗口 (预约订单时间 倒计时 已到)
	 */
	public void popAlertDialog2() {
		AlertDialog.Builder builder2 = new AlertDialog.Builder(YouOrder.this);
		builder2.setMessage("您的预约出行时间已过，订单为您保留20分钟，超时后系统会自动为您取消订单！");
		//设置 积极按钮 
		builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		builder2.create().show();
	}
	
	//点击监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back: //"返回"
			defaultHttpClient.getConnectionManager().shutdown();
			finish();
			break;
		case R.id.textView25: //"确认出行"
			orderstatus = 1; //表示开启订单
			popAlertDialog("确认开始行程？");
			break;
		case R.id.textView27: //"取消订单"
			orderstatus = 3; //表示手动取消订单
			popAlertDialog("当日取消订单不可超过三次，超过后当天不可再预约车辆，确定要取消吗？您还有"+(3-((MyApp)getApplication()).getOrderOff())+"次机会~");
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
