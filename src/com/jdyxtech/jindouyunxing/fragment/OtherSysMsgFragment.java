package com.jdyxtech.jindouyunxing.fragment;

import com.jdyxtech.jindouyunxing.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class OtherSysMsgFragment extends Fragment {
	private String msgTitle,msg;
	private View view; 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//创建fragment的视图
		view = inflater.inflate(R.layout.fragment_other_sys_msg, null);
		//666获取到外层Activity传递进来的消息数据
		Bundle bundle = getArguments();
		msgTitle = bundle.getString("msgTitle", "");
		msg = bundle.getString("msg", "");	
		//将消息数据适配到控件上去
		TextView Title = (TextView) view.findViewById(R.id.textView1);
		Title.setText(msgTitle);
		TextView Msg = (TextView) view.findViewById(R.id.textView3);
		Msg.setText(msg);
		return view;
	}

}
