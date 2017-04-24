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
import com.jdyxtech.jindouyunxing.adapter.MyIntegralListAdapter;
import com.jdyxtech.jindouyunxing.javabean.MyIntegrals;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 我的积分 界面
 * @author Tom
 *
 */
public class MyIntegral extends Activity implements OnClickListener{
	private SharedPreferences sp;
	private MyIntegrals myIntegral;
	private ListView integralListView;
	private TextView textView1; //积分总额
	private DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) { //积分获取成功
				textView1.setText(myIntegral.getSum()+"分\n"+"当前积分"); //当前总积分
				MyIntegralListAdapter adapter = new MyIntegralListAdapter(MyIntegral.this, myIntegral.getLists());
				integralListView.setAdapter(adapter);
			}else {   //积分获取失败
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_integral);
		
		initView();
		getIntegrals();
		
	}
	
	/**
	 * 初始化
	 */
	private void initView() {
		sp = getSharedPreferences("user", MODE_PRIVATE);
		textView1  = (TextView) findViewById(R.id.textView1);
		integralListView = (ListView) findViewById(R.id.listView1);
		
	}

	/**
	 * 从服务器加载 账户积分列表
	 */
	private void getIntegrals() {
		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/inviteapi/integral");
        //向服务器上传json格式的登陆数据
        try {
        	JSONObject json = new JSONObject();
			json.put("token",sp.getString("token", ""));
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
			        	 myIntegral = gson.fromJson(result, MyIntegrals.class);
			        	 if (200 == myIntegral.getStatus()) {
				        	 handler.sendEmptyMessage(1); //积分获取成功，告诉主线程中的handler进行相应的操作
						}else{
				        	 handler.sendEmptyMessage(0); //积分获取失败，告诉主线程中的handler进行相应的操作
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
		case R.id.textView2:
			Toast.makeText(MyIntegral.this, "积分规则", Toast.LENGTH_SHORT).show();
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
