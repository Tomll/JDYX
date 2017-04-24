package com.jdyxtech.jindouyunxing.activity;

import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.view.Window;
/**
 * 支付成功界面
 * @author Tom
 *
 */
public class PaySuccess extends BaseActivity implements OnClickListener{
	public static PaySuccess paySuccess;
	private TextView textView3,textView7;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) { //支付状态修改成功
			}else {   //支付状态修改失败
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.pay_success);
		
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
	 * 初始化
	 */
	private void initView() {
		paySuccess = this;
		MyApp myApp = (MyApp) getApplication();
		textView7 = (TextView) findViewById(R.id.textView7);
		textView3 = (TextView) findViewById(R.id.textView3);
		if (myApp.getFree() - myApp.getActivity_free() - myApp.getIntegral_free() > 0) { //总费用 - 优惠券 - 积分抵扣 > 0
			textView3.setText(String.format("%.2f", myApp.getFree() - myApp.getActivity_free() - myApp.getIntegral_free())); //按实际支付
		}else {
			textView3.setText("0.01"); // 只需 付0.01元
		}
		if (myApp.getBizType().equals("000202")||myApp.getBizType().equals("000203")||myApp.getBizType().equals("000204")) { //如果支付的是押金，就不需要显示“立即评价”按钮了
			textView7.setVisibility(View.GONE);
		}
	}

	//点击监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back: 
			if (PayPreview.payPreview != null) { //支付预览界面如果不为空，就将其关闭
				PayPreview.payPreview.finish(); 
			}
			finish();  //关闭支付成功界面
			break;
		case R.id.textView7: //立即评价
			startActivity(new Intent(PaySuccess.this,Evaluate.class)); //跳转到评价页面
			break;
		default:
			break;
		}
	}
	
	/**
	 * 返回键
	 */
	@Override
	public void onBackPressed() {
		if (PayPreview.payPreview != null) { //支付预览界面如果不为空，就将其关闭
			PayPreview.payPreview.finish(); 
		}
		finish();  //关闭支付成功界面
	}
	
}
