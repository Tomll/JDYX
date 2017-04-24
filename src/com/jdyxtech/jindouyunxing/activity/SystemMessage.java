package com.jdyxtech.jindouyunxing.activity;

import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.fragment.CheckFailFragment;
import com.jdyxtech.jindouyunxing.fragment.CheckSuccessFragment;
import com.jdyxtech.jindouyunxing.fragment.OtherSysMsgFragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

/**
 * 消息详情展示界面，采用Fragment实现 3种类型消息的详情展示
 * @author Tom
 *
 */
public class SystemMessage extends FragmentActivity implements OnClickListener{
	private SharedPreferences sp;
	private int msgType;
	private String msgTitle,msg;
	private FragmentManager fragmentManager;
	private FragmentTransaction transaction;
	private Fragment fragment; //此fragment为：根据不同的消息类型创建的不同类型的fragment
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.system_message);
		
		initData(); //初始化相关数据
		initView(); //初始化View视图
		
	}
	/**
	 * 初始化数据的方法
	 */
	private void initData() {
		sp = getSharedPreferences("user", MODE_PRIVATE);
		Intent intent = getIntent(); //这是从SystemMessageList.java 或者 BaiDuPushResiver.java跳转过来的intent，携带有msg 
		msgType = intent.getIntExtra("msgType", -1); // 获取intent传递过来的消息类型
		msgTitle = intent.getStringExtra("msgTitle"); // 获取intent传递过来的消息标题
		msg = intent.getStringExtra("msg"); // 获取intent传递过来的消息主体
		fragmentManager = getSupportFragmentManager(); // 获取碎片管理器
		transaction = fragmentManager.beginTransaction(); // 开启碎片事务
	}
	/**
	 * 初始化View视图的方法
	 */
	private void initView() {
		// 根据msgType的类型 创建相应的ragment放到msg_linearlayout布局中去
		if (msgType == 1) { //消息类型为 “审核通过”,接下来就是支付押金了
			final MyApp myApp = (MyApp) getApplication();
			myApp.setBizType("000202"); //设置支付的费用业务类型为：押金费用
			myApp.setFree(2000); //设置支付金额为：押金2000元
			myApp.setOrderNum(sp.getString("uname", null)+System.currentTimeMillis()); //押金支付情况下的orderNum为用户手机号 + 时间戳
			fragment = new CheckSuccessFragment();
		}else if (msgType == 2) { //消息类型为 “审核未通过”,接下来重新提交注册信息
			fragment = new CheckFailFragment();
		}else if (msgType == 3) { //消息类型为 “其他系统消息”,
			fragment = new OtherSysMsgFragment();
			Bundle bundle = new Bundle();
			bundle.putString("msgTitle", msgTitle);
			bundle.putString("msg", msg);
			fragment.setArguments(bundle); //*****666用于将数据 从外面的Activity传递到里面的fragment中去
		}
		transaction.add(R.id.msg_linearlayout, fragment); //将创建的fragment添加到msg_linearlayout布局中去
		transaction.commit(); // 提交碎片事务
	}
	
	//点击监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back:
			finish();
			break;
		default:
			break;
		}
	}
		

}
