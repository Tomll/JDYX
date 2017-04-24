package com.jdyxtech.jindouyunxing.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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
import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.javabean.GetCommentMessage;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Window;
/**
 * 展示某一个 历史订单 的界面
 * @author Tom
 *
 */
public class MyTravel extends BaseActivity implements OnClickListener{
	private SharedPreferences sp;
	private TextView textView1,textView2,textView3,textView4,textView5,textView6,textView7,textView8,textView9,textView16,textView24,textView018,textView20,textView27,textView29,textView30,textView31;
	private ImageView imageView1,imageView2,imageView3,imageView4,imageView5,imageView6,imageView7,imageView8,imageView9,imageView10;
	private List<ImageView> starList = new ArrayList<ImageView>(); //存放星星图片的集合
	private RelativeLayout comment_message_re,comment_now_re;
	private GetCommentMessage getCommentMessage;
	private DefaultHttpClient defaultHttpClient = new DefaultHttpClient();

	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (1 == msg.what) { //评论信息加载成功，根据评论记录，点亮对应数量的星星
				for (int i = 0; i <= getCommentMessage.getAssess().getNeat()-1; i++) {
					starList.get(i).setImageResource(R.drawable.star_yellow);
				}
				for (int i = 5; i <= 5+getCommentMessage.getAssess().getEnjoy()-1; i++) {
					starList.get(i).setImageResource(R.drawable.star_yellow);
				}
				textView27.setText("评价信息："+getCommentMessage.getAssess().getAssess()); //评论的内容
			}else if (0 == msg.what) {
				Toast.makeText(MyTravel.this, "评论信息加载失败！", Toast.LENGTH_SHORT).show();
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_travel);
		
		init();
		
	}

	/**
	 * 现场保护（注意：这并不是Activity的一个生命周期，只是应用在后台运行时，该Activity将要被系统回收掉的时候 会执行这个方法）
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState); //在父类BaseActivity的onSaveInstanceState()方法中，执行了 全局变量myApp的 保存工作
	}
	
	/**
	 * 初始化
	 */
	private void init() {
		sp = getSharedPreferences("user", MODE_PRIVATE);
		final MyApp myApp  = (MyApp) getApplication();
		comment_message_re = (RelativeLayout) findViewById(R.id.comment_message_re); //评价信息展示 re布局
		comment_now_re = (RelativeLayout) findViewById(R.id.comment_now_re); //“立即评价” re布局
		//行程信息
		textView1 = (TextView) findViewById(R.id.textView2);
		textView1.setText(myApp.getStartTime());
		textView2 = (TextView) findViewById(R.id.textView5);
		textView2.setText(myApp.getOrgName());
		textView3 = (TextView) findViewById(R.id.textView6);
		textView3.setText(myApp.getSelectCarBean().getTitle()+myApp.getSelectCarBean().getParknum()+"车位");
		textView4 = (TextView) findViewById(R.id.textView7);
		textView4.setText(myApp.getSelectParkBean().getTitle()+myApp.getSelectParkBean().getParknum()+"车位");
		textView5 = (TextView) findViewById(R.id.textView8);
		textView5.setText(myApp.getDstName());
		//车辆信息
//		carImageView = (ImageView) findViewById(R.id.imageView8);
//		picasso.with(this).load(myApp.getSelectCarBean().getBig()).into(carImageView);
		textView6 = (TextView) findViewById(R.id.textView10); //品牌
		textView6.setText(myApp.getSelectCarBean().getBrand()); 
		textView7 = (TextView) findViewById(R.id.textView9); //型号
		textView7.setText(myApp.getSelectCarBean().getModel());
//		textView8 = (TextView) findViewById(R.id.textView11); //几座
//		textView8.setText(myApp.getSelectCarBean().getSeats()+"座");
		textView9 = (TextView) findViewById(R.id.textView12); //车牌号
		textView9.setText("（ "+myApp.getSelectCarBean().getCarli()+" ）");
		//订单信息
		textView018 = (TextView) findViewById(R.id.textView018); //行驶时长
		textView018.setText(myApp.getDisttime()+"分钟");
		textView30 = (TextView) findViewById(R.id.textView30); //“ 超时时长: ”
		textView31 = (TextView) findViewById(R.id.textView31); //超时时长
		if (myApp.getCha_time() > 0) { //超时了
			textView31.setText(myApp.getCha_time()+"分钟");
		}else {
			textView30.setVisibility(View.GONE);
			textView31.setVisibility(View.GONE);
		}
		textView20 = (TextView) findViewById(R.id.textView20);
		textView20.setText(myApp.getDist()+"公里");
		textView24 = (TextView) findViewById(R.id.textView24);  //出行费用
		textView24.setText(myApp.getFree()+"元");
		textView29 = (TextView) findViewById(R.id.textView29); //优惠抵扣
		textView29.setText(myApp.getActivity_free()+"元");
		textView16 = (TextView) findViewById(R.id.textView16); //实际支付
		if (myApp.getFree()-myApp.getActivity_free() > 0) {
			textView16.setText(String.format("%.2f",myApp.getFree()-myApp.getActivity_free())+"元");
		}else{
			textView16.setText("0.1元");
		}
		//评价信息
		imageView1 = (ImageView) findViewById(R.id.imageView9);
		starList.add(imageView1);
		imageView2 = (ImageView) findViewById(R.id.imageView10);
		starList.add(imageView2);
		imageView3 = (ImageView) findViewById(R.id.imageView11);
		starList.add(imageView3);
		imageView4 = (ImageView) findViewById(R.id.imageView12);
		starList.add(imageView4);
		imageView5 = (ImageView) findViewById(R.id.imageView13);
		starList.add(imageView5);
		imageView6 = (ImageView) findViewById(R.id.imageView14);
		starList.add(imageView6);
		imageView7 = (ImageView) findViewById(R.id.imageView15);
		starList.add(imageView7);
		imageView8 = (ImageView) findViewById(R.id.imageView16);
		starList.add(imageView8);
		imageView9 = (ImageView) findViewById(R.id.imageView17);
		starList.add(imageView9);
		imageView10 = (ImageView) findViewById(R.id.imageView18);
		starList.add(imageView10);
		textView27 = (TextView) findViewById(R.id.textView27); //评价信息
		if (myApp.getAssess() == 0) { //订单未平价
			comment_message_re.setVisibility(View.GONE); //隐藏展示评价信息的view，显示“立即评价”按钮
		}else if (myApp.getAssess() == 1 ) { //订单已评价
			comment_now_re.setVisibility(View.GONE); //隐藏“立即评价”按钮，展示评价信息view
			//从服务器 请求评价信息
			getCommentMessage();
		}
	}

	
	/**
	 *  从服务器 获取某个订单的评价信息
	 */
	public void getCommentMessage() {

		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/assess/info");
        //向服务器上传json格式的登陆数据
        try {
        	JSONObject json = new JSONObject();
			json.put("token",sp.getString("token", ""));
			json.put("order_number", ((MyApp)getApplication()).getOrderNum());
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
			        	 getCommentMessage = gson.fromJson(result, GetCommentMessage.class);
			        	 if (200 == getCommentMessage.getStatus()) { //本订单的评价信息加载成功，告诉主线程中的handler进行相应的操作
			        		 handler.sendEmptyMessage(1); 
			        	 }else{  //本订单的评价信息加载失败，告诉主线程中的handler进行相应的操作
			        		 handler.sendEmptyMessage(0); 
			        	 }
			         }else {  //已无 更多数据，告诉主线程中的handler进行相应的操作
			        	 handler.sendEmptyMessage(2); 
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
			defaultHttpClient.getConnectionManager().shutdown();
			finish();
			break;
		case R.id.textView13: //“立即评价” 调到评价页面 去评价本单
			startActivity(new Intent(MyTravel.this,Evaluate.class));
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
