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
import com.jdyxtech.jindouyunxing.adapter.CouponListAdapter;
import com.jdyxtech.jindouyunxing.javabean.GetCoupon;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
/**
 * 我的 优惠券界面
 * @author Tom
 *
 */
public class MyCoupon extends Activity implements OnClickListener,OnItemClickListener{
	//select_coupon = true表示从支付预览界面跳转过来，来选择优惠券的；select_coupon=false表示从侧滑菜单进来，查看优惠券的（此种情况优惠券是不可以点击选择的）
	private boolean select_coupon = false;//默认是false，只有从支付预览界面跳过来的时候intent携带的select_coupon = true
	private int posi = -1; //选中的 优惠券的在列表中的位置,-1表示未选择，请求优惠券列表，服务器默认返回优惠券的posi = 0
	private ProgressDialog proDialog;
	private SharedPreferences sp;
	private GetCoupon getCoupon;
	private ListView couponListView;
	private CouponListAdapter couponListAdapter;
	private DefaultHttpClient defaultHttpClient = new DefaultHttpClient();

	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) { //优惠券列表加载成功
				couponListAdapter = new CouponListAdapter(MyCoupon.this, getCoupon.getLists(),posi);
				couponListView.setAdapter(couponListAdapter); 
				proDialog.cancel(); 
			}else {   //优惠券列表加载失败
				proDialog.cancel(); 
				Toast.makeText(MyCoupon.this, "无可用优惠券！", Toast.LENGTH_SHORT).show();
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_coupon);
		
		initView(); //初始化视图
		getCoupon(); //加载优惠券数据，注：绑定适配器在handler中进行
		couponListView.setOnItemClickListener(this); //注册item点击监听
		
	}

	/**
	 * 初始化视图的方法
	 */
	private void initView() {
		Intent intent = getIntent();//指的是 从支付预览页面跳转过来的intent
		select_coupon = intent.getBooleanExtra("select_coupon", false);
		posi = intent.getIntExtra("posi", posi);
		proDialog = createDialog();
		proDialog.show();
		sp = getSharedPreferences("user", MODE_PRIVATE);
		couponListView = (ListView) findViewById(R.id.listView1);
	}

	/**
	 * 从服务器获取 优惠券列表(初始化数据)
	 */
	private void getCoupon() {
		// 创建post请求对象
		final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/activity");
		// 向服务器上传json格式的数据
		try {
			JSONObject json = new JSONObject();
			json.put("token", sp.getString("token", ""));
			json.put("status", 1); //status为1表示：获取可用优惠券，status为0表示:获取全部优惠券 
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
					HttpResponse httpResponse = defaultHttpClient.execute(request);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						String result = EntityUtils.toString(httpResponse.getEntity());
						Gson gson = new Gson();
						getCoupon = gson.fromJson(result, GetCoupon.class);
						if (200 == getCoupon.getStatus()) {
							handler.sendEmptyMessage(0); // 优惠券列表加载成功，告诉主线程中的handler进行相应的操作
						} else {
							handler.sendEmptyMessage(1); //优惠券列表加载失败，告诉主线程中的handler进行相应的操作
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
	 * 创建 “正在加载优惠券” ProgressDialog对话框的 方法
	 */
	public ProgressDialog createDialog() {
		ProgressDialog mypDialog=new ProgressDialog(this);
		mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mypDialog.setTitle("加载优惠券");
		mypDialog.setMessage("正在加载优惠券，请您耐心等候...");
		mypDialog.setIndeterminate(false);
		mypDialog.setCancelable(true);
		return mypDialog;
	}

	/**
	 * couponListView(优惠券列表) 的 item点击监听回调方法
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		if (select_coupon) { // 预览界面跳过来的：优惠券是可以点击选择
			Intent intent = new Intent();
//			if (position == posi) { //取消使用优惠券：点击的 position刚好是之前选中的posi，执行：取消使用优惠券 逻辑
//				intent.putExtra("posi", -1);
//				intent.putExtra("id", 0);
//				intent.putExtra("free", 0);
//			} else { //更改优惠券
				int id = getCoupon.getLists().get(position).getId(); // 点击的优惠券id
				float free = getCoupon.getLists().get(position).getFree(); // 点击的优惠券金额
				posi = position; // 选中的 优惠券的在列表中的位置,-1表示未选择
				intent.putExtra("posi", posi);
				intent.putExtra("id", id);
				intent.putExtra("free", free);
//			}
			setResult(111, intent); // 回传结果
			finish();
		}
	}
	
	//点击监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back: 
			defaultHttpClient.getConnectionManager().shutdown();
			finish(); 
			break;
		case R.id.textView1: //优惠券 使用规则，按钮
			Toast.makeText(MyCoupon.this, "使用规则", Toast.LENGTH_SHORT).show();
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
