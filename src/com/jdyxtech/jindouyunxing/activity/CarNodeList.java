package com.jdyxtech.jindouyunxing.activity;

import java.io.Serializable;
import java.util.List;

import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.adapter.CarNodeListAdapter;
import com.jdyxtech.jindouyunxing.javabean.CarBean;
import com.jdyxtech.jindouyunxing.javabean.Node;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
/**
 * 车辆网点列表界面
 * @author Tom
 *
 */
public class CarNodeList extends Activity implements OnClickListener,OnItemClickListener{
	public static CarNodeList carNodeListActivity;
	private ListView carNodeListView;
	private CarNodeListAdapter carNodeListAdapter;
	private List<Node> node_list;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.car_node_list);
		
		initData();
		initView();
		initCtrl();
		carNodeListView.setAdapter(carNodeListAdapter); //carNodeListView绑定适配
		carNodeListView.setOnItemClickListener(this); //carNodeListView设置 item点击事件
	}

	private void initData() {
		carNodeListActivity = this;
		Intent intent = getIntent();
		node_list = (List<Node>) intent.getSerializableExtra("node_list");		
	}

	private void initView() {
		carNodeListView = (ListView) findViewById(R.id.listView_carNode);
	}

	private void initCtrl() {
		carNodeListAdapter = new CarNodeListAdapter(node_list,CarNodeList.this, (MyApp)getApplication());
	}

	//车辆网点列表的 item项 点击事件监听
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
//		if (((MyApp)getApplication()).isMoreThanOneHour()) {  //aaaaaaaaaaaaaaaa一小时后的订单
//			//一小时之后的订单，点击item的话 不展示车辆列表
//		}else { //一小时之内的订单，点击item跳转到车辆列表
			Intent intent = new Intent(this,CarStateList.class);
			List<CarBean> carList = node_list.get(position).getCarlists();
			intent.putExtra("carList", (Serializable)carList);
			intent.putExtra("strLoc", (Serializable)node_list.get(position).getLocal().getLoc());
			startActivity(intent);
//		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back:
			finish();
			break;
		case R.id.button_car_state_switch:
			finish();
			break;
		default:
			break;
		}
	}

}
