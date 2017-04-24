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
import com.jdyxtech.jindouyunxing.javabean.SubmitComment;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 订单评价页面
 * @author Tom
 */
public class Evaluate extends BaseActivity implements OnClickListener{

	private SharedPreferences sp;
	private SubmitComment submitComment;
	private ImageView imageView1,imageView2,imageView3,imageView4,imageView5,imageView6,imageView7,imageView8,imageView9,imageView10;
	private EditText editText1; //评价信息
	private TextView service_phone;//客服电话
	private List<ImageView> starList = new ArrayList<ImageView>(); //存放星星图片的集合
	private int neat = 0,satisfaction = 0; //整洁度 、满意度 对应的评分
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (1 == msg.what) {
				Toast.makeText(Evaluate.this, "评论成功！", Toast.LENGTH_SHORT).show();
				if (PaySuccess.paySuccess!=null) {
					PaySuccess.paySuccess.finish();
				}
				if (PayPreview.payPreview!=null) {
					PayPreview.payPreview.finish();
				}
				finish();
			}else if (0 == msg.what) {
				Toast.makeText(Evaluate.this, "评论提交失败，请重试！", Toast.LENGTH_SHORT).show();
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.evaluate);
		
		initView();
		
	}
	
	/**
	 * 现场保护（注意：这并不是Activity的一个生命周期，只是应用在后台运行时，该Activity将要被系统回收掉的时候 会执行这个方法）
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState); //在父类BaseActivity的onSaveInstanceState()方法中，执行了 全局变量myApp的 保存工作
	}
	
	/**
	 * 初始化view （两排10个星星 和评论信息编辑文本）的方法
	 */
	private void initView() {
		sp = getSharedPreferences("user", MODE_PRIVATE);
		editText1 = (EditText) findViewById(R.id.editText1);
		service_phone = (TextView) findViewById(R.id.service_phone);
		service_phone.setText(((MyApp) getApplication()).getTel());
		imageView1 = (ImageView) findViewById(R.id.imageView1);
		starList.add(imageView1);
		imageView2 = (ImageView) findViewById(R.id.imageView2);
		starList.add(imageView2);
		imageView3 = (ImageView) findViewById(R.id.imageView3);
		starList.add(imageView3);
		imageView4 = (ImageView) findViewById(R.id.imageView4);
		starList.add(imageView4);
		imageView5 = (ImageView) findViewById(R.id.imageView5);
		starList.add(imageView5);
		imageView6 = (ImageView) findViewById(R.id.imageView6);
		starList.add(imageView6);
		imageView7 = (ImageView) findViewById(R.id.imageView7);
		starList.add(imageView7);
		imageView8 = (ImageView) findViewById(R.id.imageView8);
		starList.add(imageView8);
		imageView9 = (ImageView) findViewById(R.id.imageView9);
		starList.add(imageView9);
		imageView10 = (ImageView) findViewById(R.id.imageView10);
		starList.add(imageView10);
	}

	/**
	 * 此方法用于：5星评价中 展示点亮的星星个数
	 */
	public void setStarNum(int i) {
		if (i <= 4) {
			//先将0~4全部设置为灰色
			for (int j = 0; j <= 4; j++) {
				starList.get(j).setImageResource(R.drawable.star_grey);
			}
			//再将第 0~i个设置为 黄色
			for (int j = 0; j <= i; j++) {
				starList.get(j).setImageResource(R.drawable.star_yellow);
			}
		} else {
			//先将5~9全部设置为灰色
			for (int j = 5; j <= 9; j++) {
				starList.get(j).setImageResource(R.drawable.star_grey);
			}
			//再将第 5~i个设置为 黄色
			for (int j = 5; j <= i; j++) {
				starList.get(j).setImageResource(R.drawable.star_yellow);
			}
		}
	}

	/**
	 * 提交评论内容 给服务器
	 */
	private void submitComment() {
		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/assess/insert");
        //向服务器上传json格式的登陆数据
        try {
        	JSONObject json = new JSONObject();
			json.put("token",sp.getString("token", ""));
			json.put("order_number", ((MyApp)getApplication()).getOrderNum());
			json.put("neat", neat);
			json.put("enjoy",satisfaction);
			json.put("assess",editText1.getText().toString());
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
			        	 submitComment = gson.fromJson(result, SubmitComment.class);
			        	 if (200 == submitComment.getStatus()) {
				        	 handler.sendEmptyMessage(1); //评论成功，告诉主线程中的handler进行相应的操作
						}else{
				        	 handler.sendEmptyMessage(0); //评论失败，告诉主线程中的handler进行相应的操作
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
		case R.id.button_back: //返回键
			finish();
			break;
		case R.id.textView4: //“提交” 按钮
			if (neat == 0 ) {
				Toast.makeText(Evaluate.this, "请您对车辆整洁度评分", Toast.LENGTH_LONG).show();
				return;
			}else if (satisfaction == 0) {
				Toast.makeText(Evaluate.this, "请您对出行满意度评分", Toast.LENGTH_LONG).show();
				return;
			}
			submitComment(); //提交评论
			break;
		case R.id.service_phone: //客服电话
			startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ((MyApp) getApplication()).getTel())));
			break;
		case R.id.imageView1: 
			neat = 1;
			setStarNum(0);
			break;
		case R.id.imageView2: 
			neat = 2;
			setStarNum(1);
			break;
		case R.id.imageView3: 
			neat = 3;
			setStarNum(2);
			break;
		case R.id.imageView4: 
			neat = 4;
			setStarNum(3);
			break;
		case R.id.imageView5: 
			neat = 5;
			setStarNum(4);
			break;
		case R.id.imageView6: 
			satisfaction = 1;
			setStarNum(5);
			break;
		case R.id.imageView7: 
			satisfaction = 2;
			setStarNum(6);
			break;
		case R.id.imageView8: 
			satisfaction = 3;
			setStarNum(7);
			break;
		case R.id.imageView9: 
			satisfaction = 4;
			setStarNum(8);
			break;
		case R.id.imageView10: 
			satisfaction = 5;
			setStarNum(9);
			break;
		default:
			break;
		}
	}
	
	
}
