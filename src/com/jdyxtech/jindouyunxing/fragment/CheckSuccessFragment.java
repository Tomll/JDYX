package com.jdyxtech.jindouyunxing.fragment;

import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.activity.AboutUs;
import com.jdyxtech.jindouyunxing.activity.Pay;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class CheckSuccessFragment extends Fragment{
	private View view;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_check_success, null);
		
		//cheeckFailFragment中的 “《筋斗云行用户协议》” 按钮，注册点击监听
		view.findViewById(R.id.textView6).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), AboutUs.class)); //跳转到 用户协议 界面
			}
		});		
		//cheeckSuccessFragment中的 “立即支付” 按钮，注册点击监听
		view.findViewById(R.id.textView4).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), Pay.class)); //跳转到 支付界面
				getActivity().finish();//关闭当前Activity
			}
		});		
		return view;
	}
	

}
