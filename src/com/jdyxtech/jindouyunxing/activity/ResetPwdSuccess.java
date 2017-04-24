package com.jdyxtech.jindouyunxing.activity;
import com.jdyxtech.jindouyunxing.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
/**
 * 重置密码成功界面
 * @author Tom
 *
 */
public class ResetPwdSuccess extends Activity implements OnClickListener{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.reset_pwd_success);
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back:
			finish();
			break;
		case R.id.textView3:
			finish();
			break;
		default:
			break;
		}
	}

}
