package com.jdyxtech.jindouyunxing.activity;

import com.jdyxtech.jindouyunxing.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
/**
 * 我的钱包界面
 * @author Tom
 *
 */
public class MyWallet extends Activity implements OnClickListener{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_wallet);
		
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		//由于“押金” 和 “储值卡” 共用一个Activity：MyDeposit，所以这里加一个type区分
		case R.id.re_valueCard: //储值卡
			startActivity(intent.setClass(MyWallet.this,MyDeposit.class).putExtra("type", 1));
			break;
		case R.id.re_deposit: //押金
			startActivity(intent.setClass(MyWallet.this,MyDeposit.class).putExtra("type", 0));
			break;
		case R.id.re_coupon: //优惠券
			startActivity(intent.setClass(MyWallet.this,MyCoupon.class));
			break;
		case R.id.re_integral: //积分
			startActivity(intent.setClass(MyWallet.this,MyIntegral.class));
			break;
		case R.id.button_back: //“返回”按钮
			finish();
			break;
		default:
			break;
		}
	}

}
