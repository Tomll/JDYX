package com.jdyxtech.jindouyunxing.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;

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
import com.jdyxtech.jindouyunxing.javabean.FinalOrderInfor;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;
import com.jock.lib.HighLight;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Window;
/**
 * 订单信息、支付信息预览界面
 * @author Tom
 *
 */
public class PayPreview extends BaseActivity implements OnClickListener{
	public static PayPreview payPreview;
	private HighLight mHightLight;//高亮 引导提示1
	private CheckBox checkBox1,checkBox2;
	private TextView textView2,textView3,textView4,textView5,textView22,textView10,textView12,textView16,textView18,textView25,textView28;
	private int posi = -1; //选中的 优惠券的在列表中的位置,-1表示未选择
	private float currentPay;//记录当前应付费用的 中间变量
	private DecimalFormat df = new DecimalFormat("0.00");
	private FinalOrderInfor finalOrderInfor;
	private SharedPreferences sp;
	private Editor editor;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) { //实际订单信息获取成功
				final MyApp myApp  = (MyApp) getApplication();
				myApp.setFree(finalOrderInfor.getFree()); //记录实际的 订单总费用
				textView10.setText(finalOrderInfor.getDisttime()+"分钟"); //实际时间
				if( finalOrderInfor.getCha_time() > 0){//订单超时：显示出超时TextView
					textView25.setVisibility(View.VISIBLE); 
					textView25.setText("订单超时："+finalOrderInfor.getCha_time()+"分钟"); //超时多少分钟
				}
				textView12.setText(String.format("%.2f", finalOrderInfor.getDist()*0.001)+"公里");//实际里程
				textView16.setText(finalOrderInfor.getFree()+"元"); //实际总的出行费用
				//优惠券栏
				if (finalOrderInfor.getActivity().getFree() > 0) { //表示有可用的优惠券
					textView18.setText("-"+finalOrderInfor.getActivity().getFree()); //通过优惠券 减了多少钱
					myApp.setActivity_id(finalOrderInfor.getActivity().getId()); //记录服务器默认选中的优惠券的id,0表示未使用
					myApp.setActivity_free(finalOrderInfor.getActivity().getFree()); //记录服务器默认选中的优惠券的金额,0表示未使用
				}else{
					textView18.setText("无可用优惠券");
					checkBox1.setClickable(false);
					findViewById(R.id.select_coupon_re).setEnabled(false);
				}
		        //积分栏
		        if (finalOrderInfor.getDeduction_free() > 0) { //表示可以使用 积分抵扣
					textView28.setText(finalOrderInfor.getIntegral_str());
					myApp.setIntegral_free(finalOrderInfor.getDeduction_free()); //记录服务器返回的积分抵扣额
				}else {
					textView28.setText("积分不足兑换");
					checkBox2.setChecked(false);
					findViewById(R.id.select_integral_re).setEnabled(false);//设置“积分”栏为：不可用状态
				}
		        currentPay = myApp.getFree(); //当前应付费用 = 无任何抵扣情况下的总费用
				if (checkBox1.isChecked()) {
					currentPay = currentPay - myApp.getActivity_free();//减去优惠券后 的当前应付费用
				}
				if (checkBox2.isChecked()) {
					currentPay = currentPay - myApp.getIntegral_free();// 减去优惠券后的当前应付费用
				}
		        //应付
		        if(currentPay > 0){ 
		        	textView22.setText("￥"+df.format(currentPay)); //按照实际支付
		        }else { //如果减去优惠券 减去积分抵扣 后的费用 <= 0,那么只需支付0.01元
		        	textView22.setText("￥0.01"); 
		        }
			}else if (msg.what == 1) { //实际订单信息获取失败
				Toast.makeText(PayPreview.this, "订单信息加载失败！", Toast.LENGTH_SHORT).show();
			}
			
			//*****************第一次使用App 引导提示*******************
			if (sp.getBoolean("isFirstStart", true)) {
				findViewById(R.id.imageView9).post(new Runnable() {
					@Override
					public void run() {
						showTipMask();
					}
				});
				editor.putBoolean("isFirstStart", false);
				editor.commit();
			}
			
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); //在父类BaseActivity的onCreate()方法中，执行了 全局变量myApp的 恢复工作（恢复现场）
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.pay_preview);
		init();
	}
	
	/**
	 * 在onResume()生命周期中，请求服务器 获取实际支付信息
	 */
	@Override
	protected void onResume() {
		super.onResume();
		//获取订单的实际信息
		getFinalOrderInfor();
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
		payPreview = this;
		sp = getSharedPreferences("user", MODE_PRIVATE);
		editor = sp.edit();
		final MyApp myApp  = (MyApp) getApplication();
		//行程信息
		textView2 = (TextView) findViewById(R.id.textView5);
		textView2.setText(myApp.getOrgName());
		textView3 = (TextView) findViewById(R.id.textView6);
		textView3.setText(myApp.getSelectCarBean().getTitle()+myApp.getSelectCarBean().getParknum()+"车位");
		textView4 = (TextView) findViewById(R.id.textView7);
		textView4.setText(myApp.getSelectParkBean().getTitle()+myApp.getSelectParkBean().getParknum()+"车位");
		textView5 = (TextView) findViewById(R.id.textView8);
		textView5.setText(myApp.getDstName());
		//展示订单实际信息的 各文本控件
		textView10 = (TextView) findViewById(R.id.textView10);
		textView12 = (TextView) findViewById(R.id.textView12);
		textView16 =(TextView) findViewById(R.id.textView16);
		textView18 = (TextView) findViewById(R.id.textView18);
		textView22 = (TextView) findViewById(R.id.textView22);
		textView25 = (TextView) findViewById(R.id.textView25);
		textView28 = (TextView) findViewById(R.id.textView28);
		checkBox1 = (CheckBox) findViewById(R.id.checkBox1);
		checkBox2 = (CheckBox) findViewById(R.id.checkBox2);
		//优惠券checkBox 注册监听
		checkBox1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					currentPay = currentPay - myApp.getActivity_free();//减去优惠券后 的当前应付费用
			        //应付
			        if(currentPay > 0){ 
			        	textView22.setText("￥"+df.format(currentPay)); //按照实际支付
			        }else { //如果减去优惠券 减去积分抵扣 后的费用 <= 0,那么只需支付0.01元
			        	textView22.setText("￥0.01"); 
			        }
				}
				if (!isChecked) {
					currentPay = currentPay + myApp.getActivity_free();
					//应付
					if (currentPay > 0) { // 减去积分抵扣后 的应付费用 > 0
						textView22.setText("￥"+df.format(currentPay)); //按照实际支付
					} else { // 如果减去优惠券 减去积分抵扣 后的费用 <= 0,那么只需支付0.01元
						textView22.setText("￥0.01");
					}
				}
			}
		});
		//积分checkBox 注册监听
		checkBox2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					currentPay = currentPay - myApp.getIntegral_free();// 减去优惠券后的当前应付费用
					// 应付
					if (currentPay > 0) {
						textView22.setText("￥" + df.format(currentPay)); // 按照实际支付
					} else { // 如果减去优惠券 减去积分抵扣 后的费用 <= 0,那么只需支付0.01元
						textView22.setText("￥0.01");
					}
				}
				if (!isChecked) {
					currentPay = currentPay + myApp.getIntegral_free();
					// 应付
					if (currentPay > 0) { // 减去积分抵扣后 的应付费用 > 0
						textView22.setText("￥" + df.format(currentPay)); // 按照实际支付
					} else { // 如果减去优惠券 减去积分抵扣 后的费用 <= 0,那么只需支付0.01元
						textView22.setText("￥0.01");
					}
				}
			}
		});
		
	}

	/**
	 * 在优惠券列表中选择好优惠券之后，回传过来的优惠券 id 及 金额
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		final MyApp myApp  = (MyApp) getApplication();
		if (requestCode == 110 && data!=null) {
	        textView18.setText("-"+data.getFloatExtra("free", 0)); //通过优惠券，减了多少钱
	        //更新 当前应付费用 currentPay 
	        if (checkBox1.isChecked()) {
	        	//更新 当前应付费用
				currentPay = currentPay + myApp.getActivity_free() - data.getFloatExtra("free", 0);
			}
	        //应付
	        if(currentPay > 0){//减去优惠券后，实际应该支付额 > 0
	        	textView22.setText("￥"+df.format(currentPay)); 
	        }else { //如果减去优惠券后 费用 <= 0,那么只需支付0.01元
	        	textView22.setText("￥0.01"); 
			}
			posi = data.getIntExtra("posi", -1);//记录选中的 优惠券的在列表中的位置,-1表示未选择
			myApp.setActivity_id(data.getIntExtra("id", 0)); //记录选中的优惠券的id,0表示未使用 
			myApp.setActivity_free(data.getFloatExtra("free", 0)); //记录选中的优惠券金额 ，0表示未使用 
		}
	}
	
	
	/**
	 * 还车成功后来到 当前支付预览界面，获取订单的各项实际信息
	 */
	private void getFinalOrderInfor() {
		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/inviteapi/client/info");
        //向服务器上传json格式的数据
        try {
    		final MyApp myApp  = (MyApp) getApplication();
        	JSONObject json = new JSONObject();
			json.put("token",sp.getString("token", ""));
			json.put("ordernum",myApp.getOrderNum());
			
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
			        	 finalOrderInfor = gson.fromJson(result, FinalOrderInfor.class);
			        	 if (200 == finalOrderInfor.getStatus()) {
				        	 handler.sendEmptyMessage(0); //操作成功，告诉主线程中的handler进行相应的操作
						}else{
				        	 handler.sendEmptyMessage(1); //操作失败，告诉主线程中的handler进行相应的操作
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
		case R.id.textView23: //点击 立即支付
			final MyApp myApp = (MyApp) getApplication();
			if (!checkBox1.isChecked()) {
				myApp.setActivity_free(0);
				myApp.setActivity_id(0);
			}
			if (!checkBox2.isChecked()) {
				myApp.setIntegral_free(0); 
			}
			myApp.setBizType("000201"); //设置支付的费用业务类型为：出行费用
			startActivity(new Intent(this,Pay.class));			
			break;
		case R.id.select_coupon_re: //点击 优惠券
			//select_coupon = true表示从支付预览界面跳转过来，来选择优惠券的；select_coupon=false表示从侧滑菜单进来，查看优惠券的（此种情况优惠券是不可以点击选择的）
			Intent intent3 = new Intent(this,MyCoupon.class).putExtra("select_coupon", true).putExtra("posi", posi);
			startActivityForResult(intent3, 110); //去优惠券列表选择某一金额的优惠券，将金额及id回传过来
			break;
		case R.id.button_back: //返回键
			finish();
			break;
		default:
			break;
		}
	}

	/**
	 * 第一次使用APP：通过该方法 进行 功能引导提示
	 */
	private void showTipMask() {
		// 创建第一个tip （取消选定的车辆 tip）
		mHightLight = new HighLight(PayPreview.this)
				//.anchor(findViewById(R.id.container))// 如果是Activity上增加引导层，不需要设置anchor
				.maskColor(R.color.grey)
				.addHighLight(R.id.imageView9, R.layout.tip_pay_perview1, new HighLight.OnPosCallback() {
					@Override
					public void getPos(float rightMargin, float bottomMargin, RectF rectF,HighLight.MarginInfo marginInfo) {
						marginInfo.rightMargin = rightMargin;
						marginInfo.bottomMargin = bottomMargin + rectF.height();
					}
				});
		mHightLight.show();
		
	}
	
}
