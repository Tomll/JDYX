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

import com.jdyxtech.jindouyunxing.MainActivity;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;

import android.app.Activity;
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
import android.widget.EditText;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
/**
 * 修改手机号界面
 * @author Tom
 *
 */
public class ChangePhoneNum extends Activity implements OnClickListener{
	private CheckBox showPwd_CheckBox;
	private EditText editOldPhone,editNewPhone,editPwd;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (0 == msg.what) { // 更改手机号成功
				Toast.makeText(ChangePhoneNum.this, "修改成功！", Toast.LENGTH_SHORT).show();
				MainActivity.editor.putString("uname", editNewPhone.getText().toString().trim());
				MainActivity.editor.commit();
				finish();
			} else if (1 == msg.what) { // 新手机号已被注册
				Toast.makeText(ChangePhoneNum.this, "您输入的新手机号已被注册！", Toast.LENGTH_SHORT).show();
			} else if (2 == msg.what) { // 更改手机号失败
				Toast.makeText(ChangePhoneNum.this, "更改手机号失败，请重试！", Toast.LENGTH_SHORT).show();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.change_phone_num);
		
		init(); //初始化
		//checkBox监听
		showPwd_CheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) { // 显示
					editPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				} else { // 隐藏
					editPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				// 提交： 显示、隐藏的 设置
				editPwd.postInvalidate();
			}
		});
		
	}

	/**
	 * 初始化方法
	 */
	private void init() {
		editOldPhone = (EditText) findViewById(R.id.edit_oldPhone);
		editNewPhone = (EditText) findViewById(R.id.edit_newPhone);
		editPwd = (EditText) findViewById(R.id.edit_pwd);
		showPwd_CheckBox = (CheckBox) findViewById(R.id.checkBox1);
	}

	/**
	 * 向服务器提交 重设密码的 请求
	 */
	public void changePhone() {
		//通过正则表达式 检查输入的旧手机号
		if (!SmsVerify.isMobileNO(editOldPhone.getText().toString().trim(), ChangePhoneNum.this)) { //原手机号 匹配错误
			return ;
		}
		//通过正则表达式 检查输入的新手机号
		if (!SmsVerify.isMobileNO(editNewPhone.getText().toString().trim(), ChangePhoneNum.this)) { //新手机号 匹配错误
			return ;
		}
		// 检查输入的密码是否为空
		if (TextUtils.isEmpty(editPwd.getText().toString().trim())) { // 密码为空
			Toast.makeText(ChangePhoneNum.this, "密码不能为空", Toast.LENGTH_LONG).show();
			return;
		}
		// 如果新手机号 和 旧手机号一样，就不能提交
		if (editOldPhone.getText().toString().trim().equals(editNewPhone.getText().toString().trim())) { // 新、旧手机号相同
			Toast.makeText(ChangePhoneNum.this, "输入的新手机号和原手机号相同", Toast.LENGTH_LONG).show();
			return;
		}
		// 创建post请求对象
		final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/clientapi/edit/phone");
		// 向服务器上传json格式的登陆数据
		try {
			JSONObject json = new JSONObject();
			json.put("token", MainActivity.sp.getString("token", ""));
			json.put("oldphone", editOldPhone.getText().toString().trim());
			json.put("newphone", editNewPhone.getText().toString().trim());
			json.put("password", Login.md5(Login.md5(editOldPhone.getText().toString().trim()+editPwd.getText().toString().trim()+"jdyx")));
			json.put("newpassword", Login.md5(Login.md5(editNewPhone.getText().toString().trim()+editPwd.getText().toString().trim()+"jdyx")));
			StringEntity se = new StringEntity(json.toString(), HTTP.UTF_8);
			se.setContentEncoding(new BasicHeader(HTTP.UTF_8, "application/json"));
			request.setEntity(se);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		// 开启一个线程，进行post带参网络请求（登录）
		new Thread() {
			public void run() {
				try {
					HttpResponse httpResponse = new DefaultHttpClient().execute(request);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						String result = EntityUtils.toString(httpResponse.getEntity());
						try {
							JSONObject jsonObject = new JSONObject(result);
							if (200 == jsonObject.getInt("status")) { // 手机号修改成功，告诉主线程中的handler进行相应的操作
								handler.sendEmptyMessage(0);
							} else if (401 == jsonObject.getInt("status")) { // 新手机号已被注册，告诉主线程中的handler进行相应的操作
								handler.sendEmptyMessage(1);
							} else { // 手机号修改失败，告诉主线程中的handler进行相应的操作
								handler.sendEmptyMessage(2);
							}
						} catch (JSONException e) {
							e.printStackTrace();
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
		case R.id.textView2: //提交新手机号
			changePhone();
			break;
		default:
			break;
		}
	}

}
