package com.jdyxtech.jindouyunxing.activity;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.google.gson.Gson;
import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.activity.Location;
import com.jdyxtech.jindouyunxing.javabean.LoginBean;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;
import com.jdyxtech.jindouyunxing.utils.NetWorkUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
/**
 * 登录界面
 * @author Tom
 *
 */
public class Login extends Activity implements OnClickListener {

	private String uname, pwd,localPhoneNum;
	private EditText editText_uname, editText_pwd;
	private SharedPreferences sp;
	private Editor editor;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) { //表示登陆成功
				final MyApp myApp = (MyApp) getApplication();
				myApp.setPhone(editText_uname.getText().toString().trim());
				//启动 百度云推送(凡是登陆处（APP共3处） ，登录成功后 都要启动百度云推送)
				PushManager.startWork(getApplicationContext(),PushConstants.LOGIN_TYPE_API_KEY,"yPdd4GWxMd1MGiHGByNRYepD");
				remberUname_Pwd(); // 调用 记住用户名和密码的方法
				//下面的if-else用于判断是否为本机号登录(无sim卡 或 sim卡号不等于本机号)
				if (localPhoneNum==null || !localPhoneNum.equals("+86" + uname)) { // 非本机号码 登录
					Toast.makeText(Login.this, "使用非本机号码登录，需要上传相关验证信息！", Toast.LENGTH_LONG).show();
					startActivity(new Intent(Login.this, ConfirmIdPhoto.class));//1非本机号登录:需要到信息确认界面 提交id和照片
				} else {  //本机号登录
					startActivity(new Intent(Login.this, Location.class));
				}
				finish(); 
			}else if(msg.what == 2) {   //账户被锁定
				Toast.makeText(Login.this, "您的账户被锁定，请联系客服！", Toast.LENGTH_LONG).show();
			}else { //登录失败
				Toast.makeText(Login.this, "用户名或密码错误，请重新输入！", Toast.LENGTH_LONG).show();
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		
		sp = getSharedPreferences("user", MODE_PRIVATE);
		editor = sp.edit();
		initView(); // 调用初始化控件的方法
		initData(); // 调用初始化数据的方法
	}

	/**
	 * 此方法用于初始化
	 */
	public void initView() {
		editText_uname = (EditText) findViewById(R.id.edit_umane);
		editText_pwd = (EditText) findViewById(R.id.edit_pwd);
		//获取本机号码  
		TelephonyManager phoneMgr=(TelephonyManager)this.getSystemService(this.TELEPHONY_SERVICE);  
		localPhoneNum = phoneMgr.getLine1Number();  
	}
	
	/**
	 * 登录的方法
	 */
	public void login(String secondMd5) {
		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/clientapi/client/login");
        //向服务器上传json格式的登陆数据
        try {
        	JSONObject json = new JSONObject();
			json.put("phone", editText_uname.getText().toString().trim());
			json.put("passwd", secondMd5);
			json.put("type", "android");
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
			        	 LoginBean loginResponse = gson.fromJson(result, LoginBean.class);
			        	 if (200 == loginResponse.getStatus()) {
			        		 editor.putString("token", loginResponse.getToken()); //将登陆成功后得到的token保存到sp中
			        		 editor.commit();
				        	 handler.sendEmptyMessage(1); //匹配登陆成功，告诉主线程中的handler进行相应的操作
						}else if(403 == loginResponse.getStatus()){
				        	 handler.sendEmptyMessage(2); //账户被锁定，告诉主线程中的handler进行相应的操作
						}else {
							handler.sendEmptyMessage(0); //登陆失败，告诉主线程中的handler进行相应的操作
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
	 *  此方法用于从SharedPreferences文件中初始化(用户名和密码)数据
	 */
	private void initData() {
		editText_uname.setText(sp.getString("uname", ""));// 将保存的用户名填写在editText_uname中
		editText_pwd.setText(sp.getString("pwd", ""));
	}

	/**
	 * 此方法用于记住用户名和密码
	 */
	public void remberUname_Pwd() {
		editor.putString("uname", editText_uname.getText().toString().trim());
		editor.putString("pwd", editText_pwd.getText().toString().trim());
		editor.commit();
	}

	//点击监听
	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.button_login: // 点击 登陆按钮
			// 检查输入的 登录账号及密码 格式是否正确 
			uname = editText_uname.getText().toString().trim();
			pwd = editText_pwd.getText().toString().trim();
			if (checkPhoneAndPwd(uname,pwd)){ 
				return;
			};
			if (NetWorkUtils.isNetworkAvailable(getApplicationContext())) {
				Toast.makeText(Login.this, "登录中...", Toast.LENGTH_SHORT).show();
				login(md5(md5(uname+pwd+"jdyx"))); //登陆
			}else {
				Toast.makeText(Login.this, "请检查您的网络连接！", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.textView_forgetPwd: // 跳转到 短信验证界面 ->找回密码
			intent.setClass(Login.this, SmsVerify.class);
			intent.putExtra("type", 1); //用于标记：1找回密码 or 2注册
			startActivity(intent);
			break;
		case R.id.textView_regist: // 先到 短信验证页面界面->注册
			intent.setClass(Login.this, SmsVerify.class);
			intent.putExtra("type", 0);
			startActivity(intent);
			finish();
			break;
		default:
			break;
		}
	}
	
	
	/**
	 * 此方法用于：判断登录框中  输入的手机号 1.格式是否正确 2.是否为本机号码
	 */
	public boolean checkPhoneAndPwd(String phoneNum,String pwd) {
	    /* 
	          移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188 
	          联通：130、131、132、152、155、156、185、186 
	          电信：133、153、180、189、（1349卫通） 
	          总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9 
	    */  
	    String telRegex = "[1][3578]\\d{9}";//"[1]"代表第1位为数字1，"[3578]"代表第二位可以为3、5、7、8中的一个，其中7代表虚拟运营商号段，"\\d{9}"代表后面是可以是0～9的数字，有9位。  
	    if (TextUtils.isEmpty(phoneNum)){
			Toast.makeText(Login.this, "手机号不能为空", Toast.LENGTH_SHORT).show();
	    	return true;  
	    }else if (!phoneNum.matches(telRegex)) {
			Toast.makeText(Login.this, "请输入格式正确的手机号！", Toast.LENGTH_SHORT).show();
			return true;  
		}else if (TextUtils.isEmpty(pwd)) {
			Toast.makeText(Login.this, "密码不能为空", Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}
	
	/**
	 * MD5 加密算法
	 */
	public static String md5(String string) {
	    byte[] hash;
	    try {
	        hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
	    } catch (NoSuchAlgorithmException e) {
	        throw new RuntimeException("Huh, MD5 should be supported?", e);
	    } catch (UnsupportedEncodingException e) {
	        throw new RuntimeException("Huh, UTF-8 should be supported?", e);
	    }
	    StringBuilder hex = new StringBuilder(hash.length * 2);
	    for (byte b : hash) {
	        if ((b & 0xFF) < 0x10) hex.append("0");
	        hex.append(Integer.toHexString(b & 0xFF));
	    }
	    return hex.toString();
	}
}
