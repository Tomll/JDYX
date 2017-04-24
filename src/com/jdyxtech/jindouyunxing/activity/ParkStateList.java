package com.jdyxtech.jindouyunxing.activity;

import java.util.List;

import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.adapter.ParkStateListAdapter;
import com.jdyxtech.jindouyunxing.javabean.ParkBean;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ListView;
/**
 * 车位列表界面
 * @author Tom
 *
 */
public class ParkStateList extends Activity implements OnClickListener{
	public static ParkStateList parkStateListActivity;
	private List<ParkBean> parkList;
	private List<Double> dstLoc;
	private ListView parkListView;
	private ParkStateListAdapter parkAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.park_state_list);
		
		initView();
		initData();
		initCtrl();
		parkListView.setAdapter(parkAdapter);
		
	}


	private void initView() {
		parkListView = (ListView) findViewById(R.id.listView_park);
	}

	private void initData() {
		parkStateListActivity = this;
		Intent intent = getIntent();
		parkList = (List<ParkBean>) intent.getSerializableExtra("parkList");	
		dstLoc = (List<Double>) intent.getSerializableExtra("dstLoc");
	}
	private void initCtrl() {
		parkAdapter = new ParkStateListAdapter(parkList, dstLoc, ParkStateList.this, (MyApp)getApplication());
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
