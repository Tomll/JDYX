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
import com.jdyxtech.jindouyunxing.javabean.ApplySmsBean;
import com.jdyxtech.jindouyunxing.javabean.SmsBean;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 短信验证界面
 * @author Tom
 *
 */
public class SmsVerify extends Activity implements OnClickListener{
	private int i = 60,type = 2;
	private TextView countTime_tv,next_tv,tip_tv;
	private EditText edit_phone,edit_code;
	private String url;
	private SmsBean smsBean;
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (2 == msg.what) {
				if (i>=1) {
					countTime_tv.setText(i+"秒");
				}else {
					countTime_tv.setClickable(true); //"获取验证码"可点击
					next_tv.setClickable(false); //将 "下一步"不可点击
					next_tv.setBackgroundResource(R.drawable.myshape2_fill_darkgrey); //"下一步"背景 从绿色 设为灰色
					countTime_tv.setText("重新获取");
					i = 60;
				}
			}else if (1 == msg.what) {
				tip_tv.setTextColor(getResources().getColor(R.color.lightgreen));
				tip_tv.setText("验证码已发送至您的手机请注意查收！");
			}else if (0 == msg.what) {
				i = 0; //倒计时 --->"重新获取"
				tip_tv.setTextColor(Color.RED);
				tip_tv.setText("验证码发送失败请重新获取！");
			}else if (3 == msg.what) {  //验证码匹配成功
				Intent intent = new Intent();
				if (1 == type) { //跳转到 找回密码
					intent.setClass(SmsVerify.this, ResetPwd.class);
					intent.putExtra("phone", edit_phone.getText().toString().trim());
					intent.putExtra("altcode", smsBean.getAltcode());
					startActivity(intent);
				}else if (0 == type) { //跳转到 注册
					intent.setClass(SmsVerify.this, Regist.class);
					intent.putExtra("phone", edit_phone.getText().toString().trim());
					startActivity(intent);
				}
				finish();
			}else if (4 == msg.what) {  //验证码匹配失败
				Toast.makeText(SmsVerify.this, "验证码错误 请重新输入", Toast.LENGTH_SHORT).show();
			}else if (5 == msg.what) { //注册时，用户已存在
				i = 0;//倒计时 --->"重新获取"
				next_tv.setClickable(false); //  "下一步" 不可点击
				next_tv.setBackgroundResource(R.drawable.myshape2_fill_darkgrey); // "下一步"背景灰色
				Toast.makeText(SmsVerify.this, "该手机号已注册，请更换手机号并重试！", Toast.LENGTH_LONG).show();
			}else if (6 == msg.what) { //找回密码，查无此账户
				i = 0;//倒计时 --->"重新获取"
				next_tv.setClickable(false); //  "下一步" 不可点击
				next_tv.setBackgroundResource(R.drawable.myshape2_fill_darkgrey); // "下一步"背景灰色
				Toast.makeText(SmsVerify.this, "查无此账户，请检查输入的手机号！", Toast.LENGTH_LONG).show();
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sms_verify);
		
		init(); //初始化
		
	}

	/**
	 * 初始化的方法
	 */
	private void init() {
		Intent intent = getIntent();
		type = intent.getIntExtra("type", 2); //根据type 跳转->找回密码 or 注册
		if (1 == type) { //找回密码
			url = MyDefaultHttpClient.HOST+"/clientapi/sms/put_altermsg";
		}else if (0 == type) { //注册
			url = MyDefaultHttpClient.HOST+"/clientapi/sms/put_msg";
		}
		edit_phone = (EditText) findViewById(R.id.edit_phone);
		edit_code = (EditText) findViewById(R.id.edit_code);
		countTime_tv = (TextView) findViewById(R.id.textView1);
		tip_tv = (TextView) findViewById(R.id.textView3);
		next_tv = (TextView) findViewById(R.id.textView2);

	}
	/**
	 *  获取 验证码
	 */
	public void applyVerificationCode() {
		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/clientapi/sms/query_msg");
        //向服务器上传json格式的登陆数据
        try {
        	JSONObject json = new JSONObject();
			json.put("phone", edit_phone.getText().toString().trim());
			json.put("control", type);
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
			        	 ApplySmsBean applySmsBean = gson.fromJson(result, ApplySmsBean.class);
			        	 if (200 == applySmsBean.getStatus()) {
				        	 handler.sendEmptyMessage(1); //验证码发送成功，告诉主线程中的handler进行相应的操作
						}else if(402 == applySmsBean.getStatus()){
							handler.sendEmptyMessage(5); //用户已存在
						}else if (406 == applySmsBean.getStatus()) { //无此用户
							handler.sendEmptyMessage(6); //无此用户
						}else {
							handler.sendEmptyMessage(0); //验证码发送失败，告诉主线程中的handler进行相应的操作
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
	 * 向服务器 校验 验证码 
	 */
	public void verificationCodeCheck() {
		if (TextUtils.isEmpty(edit_code.getText().toString().trim())) {
			Toast.makeText(SmsVerify.this, "验证码不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		if (TextUtils.isEmpty(edit_phone.getText().toString().trim())) {
			Toast.makeText(SmsVerify.this, "手机号不能为空", Toast.LENGTH_SHORT).show();
			return;
		}		
		//创建post请求对象
	    final HttpPost request = new HttpPost(url);
        //向服务器上传json格式的登陆数据
        try {
        	JSONObject json = new JSONObject();
			json.put("phone", edit_phone.getText().toString().trim());
			json.put("smscode",edit_code.getText().toString().trim());
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
			        	 smsBean = gson.fromJson(result, SmsBean.class);
			        	 if (200 == smsBean.getStatus()) {
				        	 handler.sendEmptyMessage(3); //验证码匹配成功，告诉主线程中的handler进行相应的操作
						}else{
				        	 handler.sendEmptyMessage(4); //验证码匹配失败，告诉主线程中的handler进行相应的操作
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
	 * 获取验证码  + 60S 倒计时器
	 */
	public void countTime() {
		countTime_tv.setClickable(false); //"获取验证码" 不可点击
		next_tv.setClickable(true); //  "下一步" 可点击
		next_tv.setBackgroundResource(R.drawable.myshape2_fill_green); // "下一步"背景绿色
		applyVerificationCode(); //调用 获取验证码 的方法
		new Thread(new Runnable() {  //倒计时线程
			@Override
			public void run() {
				while (i >= 1) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					i--;
					handler.sendEmptyMessage(2);
				}
			}
		}).start();
	}
	
	
	/** 
	 * 正则表达式 验证手机号格式 
	 */  
	public static boolean isMobileNO(String mobiles,Context context) {  
	    /* 
	    移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188 
	    联通：130、131、132、152、155、156、185、186 
	    电信：133、153、180、189、（1349卫通） 
	    总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9 
	    */  
	    String telRegex = "[1][3578]\\d{9}";//"[1]"代表第1位为数字1，"[3578]"代表第二位可以为3、5、7、8中的一个，其中7代表虚拟运营商号段，"\\d{9}"代表后面是可以是0～9的数字，有9位。  
	    if (TextUtils.isEmpty(mobiles)){
			Toast.makeText(context, "手机号不能为空", Toast.LENGTH_SHORT).show();
	    	return false;  
	    }else if(!mobiles.matches(telRegex)){
			Toast.makeText(context, "请输入格式正确的手机号！", Toast.LENGTH_SHORT).show();
			return false;  
	    }
	    return mobiles.matches(telRegex); 
	} 
	
	//点击监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back: //“返回”按钮
			finish();
			break;
		case R.id.textView1: //“获取验证码”按钮
			if (isMobileNO(edit_phone.getText().toString().trim(), SmsVerify.this)) { // 手机号格式 匹配正确
				countTime(); // 获取验证码 + 60s计时
			}
			break;
		case R.id.textView2: //"下一步" 按钮
			verificationCodeCheck(); //验证码校验
			break;
		default:
			break;
		}
	}

}
