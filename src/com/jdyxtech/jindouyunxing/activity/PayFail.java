package com.jdyxtech.jindouyunxing.activity;

import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
/**
 * 支付失败界面
 * @author Tom
 *
 */
public class PayFail extends BaseActivity implements OnClickListener{
	private int type; //intent传递过来的  表示支付类型数据
	private TextView textView3,textView12;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.pay_fail);
		type = getIntent().getIntExtra("type", -1); //intent传递过来的支付类型
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
		MyApp myApp = (MyApp) getApplication();
		textView3 = (TextView) findViewById(R.id.textView3);
		textView12 = (TextView) findViewById(R.id.textView12);
		TextView textView10 = (TextView) findViewById(R.id.textView10);
		if (type == 0) { //0表示支付宝支付
			textView10.setText("支付宝支付");
		}else if (type == 1) { //1表示微信支付
			textView10.setText("微信支付");
		}else if (type == 2) { //2表示银联支付
			textView10.setText("银联支付");
		} 
		if (myApp.getFree() - myApp.getActivity_free() - myApp.getIntegral_free() > 0) { //总费用 - 优惠券 - 积分抵扣 > 0
			textView3.setText(String.format("%.2f", myApp.getFree()-myApp.getActivity_free()-myApp.getIntegral_free()));
			textView12.setText(String.format("%.2f", myApp.getFree()-myApp.getActivity_free()-myApp.getIntegral_free())); //减去优惠抵扣，实际支付的费用
		}else {
			textView3.setText("0.01"); 
			textView12.setText("0.01"); // 只需 付0.01元
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back:
			if (PayPreview.payPreview != null) { //支付预览界面如果不为空，就将其关闭
				PayPreview.payPreview.finish(); 
			}
			finish(); //关闭支付失败界面
			break;
		case R.id.textView6: //重新支付
			Intent intent = new Intent(PayFail.this,Pay.class);
			startActivity(intent);
			finish();
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
		finish();  //关闭支付失败界面
	}
}
