package com.jdyxtech.jindouyunxing.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

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

import com.google.gson.Gson;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.adapter.MyDepositListAdapter;
import com.jdyxtech.jindouyunxing.javabean.MyDeposits;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 我的押金 页面
 * @author Tom
 *
 */
public class MyDeposit extends Activity implements OnClickListener {
	private SharedPreferences sp;
	private int type = -1;
	private MyDeposits myDeposits;
	private ListView depositListView;//押金 or 储值卡明细列表
	private TextView textView1; //押金 or 储值卡 剩余
	private DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) { //获取成功
				if (type == 0) {
					textView1.setText(myDeposits.getDeposit() + "元\n" + "押金余额");
				}else if (type == 1) {
					textView1.setText(myDeposits.getValue_card() + "元\n" + "余额");
				}
				MyDepositListAdapter adapter = new MyDepositListAdapter(MyDeposit.this, myDeposits.getLists());
				depositListView.setAdapter(adapter);
			}else {   //获取失败
				Toast.makeText(MyDeposit.this, "列表数据加载失败！", Toast.LENGTH_SHORT).show();
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//根据传递进来的type 决定展示哪个页面
		type = getIntent().getIntExtra("type", -1);
		if (type == 0) { // 展示“押金” 页面布局
			setContentView(R.layout.my_deposit);
		} else if(type == 1) { // 展示“储值卡” 页面布局
			setContentView(R.layout.my_value_card);
		}
		
		initView();
		getDeposits();
	}
	
	/**
	 * 初始化方法（押金界面 和 储值卡界面 可以共用）
	 */
	private void initView() {
		sp = getSharedPreferences("user", MODE_PRIVATE);
		textView1 = (TextView) findViewById(R.id.textView1);
		depositListView = (ListView) findViewById(R.id.listView1); 
	}
	
	/**
	 * 从服务器加载 账户押金扣押明细列表
	 */
	private void getDeposits() {
		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/clientapi/client/trade");
        //向服务器上传json格式的登陆数据
        try {
        	JSONObject json = new JSONObject();
			json.put("token",sp.getString("token", ""));
			json.put("type",type); //0：押金  1：储值卡
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
			        	 myDeposits = gson.fromJson(result, MyDeposits.class);
			        	 if (200 == myDeposits.getStatus()) {
				        	 handler.sendEmptyMessage(1); //押金获取成功，告诉主线程中的handler进行相应的操作
						}else{
				        	 handler.sendEmptyMessage(0); //押金获取失败，告诉主线程中的handler进行相应的操作
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
	
	
	//点击监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back:
			defaultHttpClient.getConnectionManager().shutdown();
			finish();
			break;
		case R.id.textView2: //押金规则（押金 界面的）
			Toast.makeText(MyDeposit.this, "押金规则", Toast.LENGTH_SHORT).show();
			break;
		case R.id.textView3: //押金补缴（押金 界面的）
			//由于 押金补缴 和 储值卡充值  共用一个Activity：SupplementDeposite，所以这里加一个type区分
			startActivity(new Intent(MyDeposit.this,SupplementDeposite.class).putExtra("type", 0));
			finish();
			break;
		case R.id.textView4: //账户充值（储值卡 界面的）
			//由于 押金补缴 和 储值卡充值  共用一个Activity：SupplementDeposite，所以这里加一个type区分
			startActivity(new Intent(MyDeposit.this,SupplementDeposite.class).putExtra("type", 1));
			finish();
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
