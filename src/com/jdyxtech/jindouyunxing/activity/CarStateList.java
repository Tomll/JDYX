package com.jdyxtech.jindouyunxing.activity;

import java.util.List;

import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.adapter.CarSateListAdapter;
import com.jdyxtech.jindouyunxing.javabean.CarBean;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.view.Window;
/**
 * 车辆列表界面
 * @author Tom
 *
 */
public class CarStateList extends Activity implements OnItemClickListener,OnClickListener{
	
	private ListView carListView;
	private CarSateListAdapter carAdapter;
	public static CarStateList carStateListActivity;
	private List<CarBean> carList;
	private List<Double> strLoc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.car_state_list);
		

		initData();
		initView();
		initCtrl();
		carListView.setAdapter(carAdapter); //carListView绑定适配
		carListView.setOnItemClickListener(this); //carListView设置 item点击事件
		
	}

	private void initData() {
		carStateListActivity = this;
		Intent intent = getIntent();
		carList = (List<CarBean>) intent.getSerializableExtra("carList");		
		strLoc = (List<Double>) intent.getSerializableExtra("strLoc");

	}

	private void initView() {
		carListView = (ListView) findViewById(R.id.listView_car);
	}

	private void initCtrl() {
		carAdapter = new CarSateListAdapter(carList,strLoc, CarStateList.this, (MyApp)getApplication());
		
	}

	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		Intent intent = new Intent(this,CarDetil.class);
		CarBean carBean = carList.get(position);
		intent.putExtra("carBean", carBean);
		startActivity(intent);
	}
	
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
