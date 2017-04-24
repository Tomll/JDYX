package com.jdyxtech.jindouyunxing.activity;

import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.gov.pbc.tsm.client.mobile.android.bank.service.a;
/**
 * 补缴押金界面
 * @author Tom
 *
 */
public class SupplementDeposite extends BaseActivity {
	//由于 押金补缴 和 储值卡充值  共用一个Activity：SupplementDeposite，所以这里加一个type区分
	private int type; //标志位：用于区分展示哪一个界面，押金补缴 or 储值卡充值
	private TextView textView1; //押金补缴 界面，展示带补缴金额
	private EditText editText; //储值卡 界面，输入补缴金额
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//由于 押金补缴 和 储值卡充值  共用一个Activity：SupplementDeposite，所以这里加一个type区分
		type = getIntent().getIntExtra("type", -1);
		if (type == 0) {
			setContentView(R.layout.supplement_deposit);
		}else if (type == 1) {
			setContentView(R.layout.supplement_value_card);
		}
		
		
		if (type == 0) {
			final MyApp myApp = (MyApp) getApplication();
			textView1 = (TextView) findViewById(R.id.textView1);
			textView1.setText("补缴金额："+myApp.getAdd_deposit()+"元");
			//“立即支付” 按钮，点击监听
			findViewById(R.id.textView3).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					myApp.setBizType("000203"); //设置支付的费用业务类型为：押金费用
					myApp.setFree(myApp.getAdd_deposit()); //设置支付金额为：补缴押金 xxx元
					myApp.setOrderNum(myApp.getPhone()+System.currentTimeMillis()); //押金支付情况下的orderNum为用户手机号 + 时间戳
					startActivity(new Intent(SupplementDeposite.this,Pay.class));//跳转到 支付界面,去补缴押金
					finish();
				}
			});
		}else if(type == 1) {
			final MyApp myApp = (MyApp) getApplication();
			editText = (EditText) findViewById(R.id.editText1);
			//“立即支付” 按钮，点击监听
			findViewById(R.id.textView3).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (TextUtils.isEmpty(editText.getText().toString().trim())) {
						Toast.makeText(SupplementDeposite.this, "请输入充值金额", Toast.LENGTH_SHORT).show();
					}else if (Float.valueOf(editText.getText().toString().trim()) <= 0.01f) {
						Toast.makeText(SupplementDeposite.this, "充值金额需大于0.01元", Toast.LENGTH_SHORT).show();
					}else if (Float.valueOf(editText.getText().toString().trim()) > 0.01f) {
						myApp.setBizType("000204"); //设置支付的费用业务类型为：押金费用
						myApp.setFree(Float.valueOf(editText.getText().toString().trim())); //设置支付金额为：储值卡充值 xxx元
						myApp.setOrderNum(myApp.getPhone()+System.currentTimeMillis()); //押金支付情况下的orderNum为用户手机号 + 时间戳
						startActivity(new Intent(SupplementDeposite.this,Pay.class));//跳转到 支付界面,去充值储值卡
						finish();
					}
				}
			});
		}
		
		
		//“返回” 按钮，点击监听
		findViewById(R.id.button_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	}
	
	/**
	 * 现场保护（注意：这并不是Activity的一个生命周期，只是应用在后台运行时，该Activity将要被系统回收掉的时候 会执行这个方法）
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState); //在父类BaseActivity的onSaveInstanceState()方法中，执行了 全局变量myApp的 保存工作
	}

}
