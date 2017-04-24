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
import com.jdyxtech.jindouyunxing.javabean.SmsResetPwdBean;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
/**
 * 重置密码界面
 * @author Tom
 *
 */
public class ResetPwd extends Activity implements OnClickListener {
	private String phone,altcode;
	private CheckBox showPwd_CheckBox;
	private EditText edit_newPwd,edit_newPwd2;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (0 == msg.what) { //密码修改成功
				Intent intent = new Intent(ResetPwd.this,ResetPwdSuccess.class);
				startActivity(intent);
				finish();
			} else if (1 == msg.what) { //密码修改失败
				Toast.makeText(ResetPwd.this, "重设密码失败 请重试！", Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reset_pwd);

		init();
		showPwd_CheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) { //显示
					edit_newPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
					edit_newPwd2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				}else {  //隐藏
					edit_newPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
					edit_newPwd2.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				//类似于 提交
				edit_newPwd.postInvalidate();
				edit_newPwd2.postInvalidate();
			}
		});
		
	}

	/**
	 * 初始化方法
	 */
	private void init() {
		Intent intent = getIntent();
		phone = intent.getStringExtra("phone"); 
		altcode = intent.getStringExtra("altcode");

		edit_newPwd = (EditText) findViewById(R.id.edit_newPwd);
		edit_newPwd2 = (EditText) findViewById(R.id.edit_newPwd2);
		showPwd_CheckBox =  (CheckBox) findViewById(R.id.checkBox1);
	}
	/**
	 * 向服务器提交 重设密码的 请求
	 */
	public void resetPwd() {
		if (TextUtils.isEmpty(edit_newPwd.getText().toString().trim())||TextUtils.isEmpty(edit_newPwd2.getText().toString().trim())) {
			Toast.makeText(ResetPwd.this, "密码不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		if (!TextUtils.equals(edit_newPwd.getText().toString().trim(), edit_newPwd2.getText().toString().trim())) {
			Toast.makeText(ResetPwd.this, "两次输入的密码不相同", Toast.LENGTH_SHORT).show();
			return;
		}
		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/clientapi/client/useralter");
        //向服务器上传json格式的登陆数据
        try {
        	JSONObject json = new JSONObject();
			json.put("phone", phone);
			json.put("altcode",altcode);
			json.put("newpwd",edit_newPwd2.getText().toString().trim());
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
			        	 SmsResetPwdBean smsResetPwdBean= gson.fromJson(result, SmsResetPwdBean.class);
			        	 if (200 == smsResetPwdBean.getStatus()) {
				        	 handler.sendEmptyMessage(0); //验证码匹配成功，告诉主线程中的handler进行相应的操作
						}else{
				        	 handler.sendEmptyMessage(1); //验证码匹配失败，告诉主线程中的handler进行相应的操作
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
		case R.id.button_back: //返回
			finish();
			break;
		case R.id.textView2: //提交
			resetPwd();
			break;
		default:
			break;
		}
	}
}