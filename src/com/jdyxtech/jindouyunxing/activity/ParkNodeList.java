package com.jdyxtech.jindouyunxing.activity;

import java.io.Serializable;
import java.util.List;

import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.adapter.ParkNodeListAdapter;
import com.jdyxtech.jindouyunxing.javabean.Node;
import com.jdyxtech.jindouyunxing.javabean.ParkBean;

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
 * 车位网点列表界面
 * @author Tom
 *
 */
public class ParkNodeList extends Activity implements OnClickListener,OnItemClickListener{
	public static ParkNodeList parkNodeListActivity;
	private ListView parkNodeListView;
	private ParkNodeListAdapter parkNodeListAdapter;
	private List<Node> node_list;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.park_node_list);
		
		initData();
		initView();
		initCtrl();
		parkNodeListView.setAdapter(parkNodeListAdapter); //parkNodeListView绑定适配
		parkNodeListView.setOnItemClickListener(this); //parkNodeListView设置 item点击事件
	}
	
	/**
	 * 初始化数据
	 */
	private void initData() {
		parkNodeListActivity = this;
		Intent intent = getIntent();
		node_list = (List<Node>) intent.getSerializableExtra("node_list");	
	}
	/**
	 * 组件
	 */
	private void initView() {
		parkNodeListView = (ListView) findViewById(R.id.listView_parkNode);
	}
	/**
	 * 适配器
	 */
	private void initCtrl() {
		parkNodeListAdapter = new ParkNodeListAdapter(node_list, ParkNodeList.this, (MyApp)getApplication());
	}

	//给nodeList注册item点击监听
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		Intent intent = new Intent(this,ParkStateList.class);
		List<ParkBean> parkList = node_list.get(position).getParklists();
		intent.putExtra("parkList", (Serializable)parkList);
		intent.putExtra("dstLoc", (Serializable)node_list.get(position).getLocal().getLoc());
		startActivity(intent);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back:
			finish();
			break;
		case R.id.button_park_state_switch:
			finish();
			break;
		default:
			break;
		}
	}

}
