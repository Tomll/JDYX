package com.jdyxtech.jindouyunxing;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

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
import com.jdyxtech.jindouyunxing.activity.ConfirmIdPhoto;
import com.jdyxtech.jindouyunxing.activity.Location;
import com.jdyxtech.jindouyunxing.activity.Login;
import com.jdyxtech.jindouyunxing.activity.Welcome;
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
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;
/**
 * 首页面（包含自动登录逻辑）
 * @author Tom
 *
 */
public class MainActivity extends Activity {
	public static SharedPreferences sp;
	public static Editor editor;
	private Timer timer = new Timer();
	private TimerTask timerTask,timerTask2;
	private String uname,pwd,localPhoneNum;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) { //直接登陆成功
				final MyApp myApp = (MyApp) getApplication();
				myApp.setPhone(uname);
				//启动 百度云推送(凡是登陆处（APP共3处） ，登录成功后 都要启动百度云推送)
				PushManager.startWork(getApplicationContext(),PushConstants.LOGIN_TYPE_API_KEY,"yPdd4GWxMd1MGiHGByNRYepD");
				//下面的if-else用于判断是否为本机号登录(无sim卡 或 sim卡号不等于本机号)
				if (localPhoneNum==null || !localPhoneNum.equals("+86" + uname)) { // 非本机号码 登录
					Toast.makeText(MainActivity.this, "使用非本机号码登录，需要上传相关验证信息！", Toast.LENGTH_LONG).show();
					startActivity(new Intent(MainActivity.this, ConfirmIdPhoto.class));//2非本机号登录:需要到信息确认界面 提交id和照片
				} else {  //本机号登录
					startActivity(new Intent(MainActivity.this, Location.class));
				}
			}else if(msg.what == 2) {   //账户被锁定
				Toast.makeText(MainActivity.this, "您的账户被锁定，请联系客服！", Toast.LENGTH_LONG).show();
				startActivity(new Intent(MainActivity.this, Login.class));
			}else {   //main界面 直接登录 失败，跳转到登陆界面
	   		 	startActivity(new Intent(MainActivity.this, Login.class));
			}
   		 	finish();
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		
		init(); //初始化
		
//		if (sp.getBoolean("isFirstStart", true)) { // 如果是第一次使用app，创建跳到欢迎页的timerTask
			timerTask = new TimerTask() {
				@Override
				public void run() {
					startActivity(new Intent(MainActivity.this, Welcome.class));
					finish();
				}
			};
			// n*1000秒后开启定时任务
			timer.schedule(timerTask, 1 * 1000);

//		} 
//		else { // 创建 自动登录or重新登录 的timerTask
//			timerTask = new TimerTask() {
//				@Override
//				public void run() {
//					loginOrReLogin();// login or reLogin（直接登录 or 重新登录）
//				}
//			};
//
//			// 先检查网络连接 ，网络没问题再开启 登录任务
//			if (NetWorkUtils.isNetworkAvailable(getApplicationContext())) {
//				// n*1000秒后开启定时任务
//				timer.schedule(timerTask, 1 * 1000);
//			} else {
//				Toast.makeText(MainActivity.this, "请检查您的网络连接！", Toast.LENGTH_LONG).show();
//			}
//		}
		
		
		
		
	}
	
	
	
	/**
	 * 初始化的方法
	 */
	private void init() {
		sp = getSharedPreferences("user", MODE_PRIVATE);
		editor = sp.edit();
		uname = sp.getString("uname", "");
		pwd = sp.getString("pwd", "");
		//获取本机号码  
		TelephonyManager phoneMgr=(TelephonyManager)this.getSystemService(this.TELEPHONY_SERVICE);  
		localPhoneNum = phoneMgr.getLine1Number();  
	}
	/**
	 * 此方法通过判断token是否为空，决定  直接登录Or 重新登陆
	 */
	public void loginOrReLogin() {
		if ("".equals(sp.getString("token", ""))) { //token为空:重新登陆
   		 	startActivity(new Intent(MainActivity.this, Login.class));
   		 	finish();
		}else { //token不空:直接登录
			login(Login.md5(Login.md5(uname+pwd+"jdyx"))); //直接登陆逻辑
		}
	}
	
	/**
	 * token不为空：直接登录 方法
	 */
	public void login(String secondMd5) {
		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/clientapi/client/login");
        //向服务器上传json格式的登陆数据
        try {
        	JSONObject json = new JSONObject();
			json.put("phone", sp.getString("uname", ""));
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
//        因为该login方法本来就在TimerTask()的run方法中执行的，所以这里就不用再开线程，跑run方法了
//        new Thread() {       
//            public void run() {     
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
						}else if(403 == loginResponse.getStatus()){ //账户被锁定
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
//             }     
//         }.start();
	}
	
}
