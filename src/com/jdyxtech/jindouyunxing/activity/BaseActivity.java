package com.jdyxtech.jindouyunxing.activity;

import java.io.Serializable;
import java.util.List;

import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.javabean.CarBean;
import com.jdyxtech.jindouyunxing.javabean.ParkBean;

import android.app.Activity;
import android.os.Bundle;

/**
 * 	BaseActivity是个基类 主要用于执行全局变量的myApp的保存与恢复（保护现场 与 恢复现场）工作，其他的Activity继承这个基类后，在类的onSaveInstanceState()方法中，会先去执行父类中的现场保护、现场恢复方法
 *  现场保护：当其他的Activity运行在后台时，将要被系统回收的时候，会执行onSaveInstanceState()方法中的super.savedInstanceState() 这样就会来到BaseActivty执行现场保护工作
 *  恢复现场：同理在onCreate(Bundle savedInstanceState)中执行恢复工作
 * @author Tom
 *
 */
public class BaseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			
			((MyApp) getApplication()).setStaLat(savedInstanceState.getDouble("staLat"));
			((MyApp) getApplication()).setStaLng(savedInstanceState.getDouble("staLng"));
			((MyApp) getApplication()).setEndLat(savedInstanceState.getDouble("endLat"));
			((MyApp) getApplication()).setEndLng(savedInstanceState.getDouble("endLng"));
			
			((MyApp) getApplication()).setOrgName(savedInstanceState.getString("orgName"));
			((MyApp) getApplication()).setDstName(savedInstanceState.getString("dstName"));
			((MyApp) getApplication()).setStartTime(savedInstanceState.getString("startTime"));
			((MyApp) getApplication()).setSmall(savedInstanceState.getString("small"));
			((MyApp) getApplication()).setPhone(savedInstanceState.getString("phone"));
			((MyApp) getApplication()).setOrderNum(savedInstanceState.getString("orderNum"));
			((MyApp) getApplication()).setCity(savedInstanceState.getString("city"));
			((MyApp) getApplication()).setHead_imageUrl(savedInstanceState.getString("head_imageUrl"));
			((MyApp) getApplication()).setBizType(savedInstanceState.getString("bizType"));
			((MyApp) getApplication()).setTel(savedInstanceState.getString("tel"));
			
			
			((MyApp) getApplication()).setPeopleNum(savedInstanceState.getInt("peopleNum"));
			((MyApp) getApplication()).setCheck(savedInstanceState.getInt("check"));
			((MyApp) getApplication()).setOrderOff(savedInstanceState.getInt("orderOff"));
			((MyApp) getApplication()).setActivity_id(savedInstanceState.getInt("activity_id"));
			((MyApp) getApplication()).setAssess(savedInstanceState.getInt("assess"));
			((MyApp) getApplication()).setCha_time(savedInstanceState.getInt("cha_time"));
			
			((MyApp) getApplication()).setFree(savedInstanceState.getFloat("free"));
			((MyApp) getApplication()).setDist(savedInstanceState.getFloat("dist"));
			((MyApp) getApplication()).setDisttime(savedInstanceState.getFloat("disttime"));
			((MyApp) getApplication()).setActivity_free(savedInstanceState.getFloat("activity_free"));
			((MyApp) getApplication()).setActivity_free(savedInstanceState.getFloat("integral_free"));
			((MyApp) getApplication()).setAdd_deposit(savedInstanceState.getFloat("add_deposit"));

			((MyApp) getApplication()).setSelectCarBean((CarBean) savedInstanceState.getSerializable("selectCarBean"));
			((MyApp) getApplication()).setSelectParkBean((ParkBean)savedInstanceState.getSerializable("selectParkBean"));
			((MyApp) getApplication()).setStrLoc((List<Double>)savedInstanceState.getSerializable("strLoc"));
			((MyApp) getApplication()).setDstLoc((List<Double>)savedInstanceState.getSerializable("dstLoc"));

			((MyApp) getApplication()).setFalg3(savedInstanceState.getBoolean("falg3"));
			((MyApp) getApplication()).setMoreThanOneHour(savedInstanceState.getBoolean("moreThanOneHour"));
			
			((MyApp) getApplication()).setLongStartTime(savedInstanceState.getLong("longStartTime"));
			((MyApp) getApplication()).setOpendoor_at(savedInstanceState.getLong("opendoor_at"));
			
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putDouble("staLat", ((MyApp) getApplication()).getStaLat());
		outState.putDouble("staLng", ((MyApp) getApplication()).getStaLng());
		outState.putDouble("endLat", ((MyApp) getApplication()).getEndLat());
		outState.putDouble("endLng", ((MyApp) getApplication()).getEndLng());

		outState.putString("orgName", ((MyApp) getApplication()).getOrgName());
		outState.putString("dstName", ((MyApp) getApplication()).getDstName());
		outState.putString("startTime", ((MyApp) getApplication()).getStartTime());
		outState.putString("small", ((MyApp) getApplication()).getSmall());
		outState.putString("phone", ((MyApp) getApplication()).getPhone());
		outState.putString("orderNum", ((MyApp) getApplication()).getOrderNum());
		outState.putString("city", ((MyApp) getApplication()).getCity());
		outState.putString("head_imageUrl", ((MyApp) getApplication()).getHead_imageUrl());
		outState.putString("bizType", ((MyApp) getApplication()).getBizType());
		outState.putString("tel", ((MyApp) getApplication()).getTel());

		outState.putInt("peopleNum", ((MyApp) getApplication()).getPeopleNum());
		outState.putInt("check", ((MyApp) getApplication()).getCheck());
		outState.putInt("orderOff", ((MyApp) getApplication()).getOrderOff());
		outState.putInt("activity_id", ((MyApp) getApplication()).getActivity_id());
		outState.putInt("assess", ((MyApp) getApplication()).getAssess());
		outState.putInt("cha_time", ((MyApp) getApplication()).getCha_time());
		
		outState.putFloat("free", ((MyApp) getApplication()).getFree());
		outState.putFloat("dist", ((MyApp) getApplication()).getDist());
		outState.putFloat("disttime", ((MyApp) getApplication()).getDisttime());
		outState.putFloat("activity_free", ((MyApp) getApplication()).getActivity_free());
		outState.putFloat("integral_free", ((MyApp) getApplication()).getIntegral_free());
		outState.putFloat("add_deposit", ((MyApp) getApplication()).getAdd_deposit());

		outState.putSerializable("selectCarBean", ((MyApp) getApplication()).getSelectCarBean());
		outState.putSerializable("selectParkBean", ((MyApp) getApplication()).getSelectParkBean());
		outState.putSerializable("strLoc", (Serializable) ((MyApp) getApplication()).getStrLoc());
		outState.putSerializable("dstLoc", (Serializable) ((MyApp) getApplication()).getDstLoc());

		outState.putBoolean("falg3", ((MyApp) getApplication()).isFalg3());
		outState.putBoolean("moreThanOneHour", ((MyApp) getApplication()).isMoreThanOneHour());

		outState.putLong("longStartTime", ((MyApp) getApplication()).getLongStartTime());
		outState.putLong("opendoor_at", ((MyApp) getApplication()).getOpendoor_at());

	}

}
