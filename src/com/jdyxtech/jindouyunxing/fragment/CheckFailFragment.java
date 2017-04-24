package com.jdyxtech.jindouyunxing.fragment;

import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.activity.Regist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class CheckFailFragment extends Fragment {

	private View view;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_check_fali, null);
		//cheeckFailFragment中的 “重新提交” 按钮，注册点击监听
		view.findViewById(R.id.textView4).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), Regist.class)); //跳转到 注册界面 重新提交信息
				getActivity().finish();//关闭当前Activity
			}
		});
		return view;
	}

}
