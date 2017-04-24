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

import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 意见反馈界面
 * 
 * @author Tom
 *
 */
public class FeedBack extends Activity implements OnClickListener {
	private SharedPreferences sp;
	private EditText feedBackEditText;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (1 == msg.what) {
				Toast.makeText(FeedBack.this, "感谢您对我们提出的宝贵意见！", Toast.LENGTH_LONG).show();
				finish();
			} else if (0 == msg.what) {
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.feedback);

		sp = getSharedPreferences("user", MODE_PRIVATE);
		// 反馈意见 输入文本框
		feedBackEditText = (EditText) findViewById(R.id.editText3);
	}

	/**
	 * 上传 反馈信息 到服务器
	 */
	public void submitFeedBackMsg() {
		// 创建post请求对象
		final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST + "/suggest/add");
		// 向服务器上传json格式的登陆数据
		try {
			JSONObject json = new JSONObject();
			json.put("token", sp.getString("token", ""));
			json.put("text", feedBackEditText.getText().toString().trim());
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
						JSONObject jsonObject;
						jsonObject = new JSONObject(result);
						if (200 == jsonObject.getInt("status")) { // 成功，告诉主线程中的handler进行相应的操作
							handler.sendEmptyMessage(1);
						} else { // 失败，告诉主线程中的handler进行相应的操作
							handler.sendEmptyMessage(0);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
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

	// 点击监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back:
			finish();
			break;
		case R.id.textView4:
			// 检查 反馈信息 是否为空
			if (feedBackEditText.getText().toString().trim().equals("")) {
				Toast.makeText(FeedBack.this, "反馈信息不能为空！", Toast.LENGTH_SHORT).show();
			} else {
				submitFeedBackMsg(); // 上传 反馈意见 给服务器的逻辑
			}
			break;
		default:
			break;
		}
	}

}
