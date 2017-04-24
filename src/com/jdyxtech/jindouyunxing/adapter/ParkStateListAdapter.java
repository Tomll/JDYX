package com.jdyxtech.jindouyunxing.adapter;

import java.util.List;

import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.activity.ParkNodeList;
import com.jdyxtech.jindouyunxing.activity.ParkStateList;
import com.jdyxtech.jindouyunxing.activity.SubmitOrder;
import com.jdyxtech.jindouyunxing.javabean.ParkBean;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ParkStateListAdapter extends BaseAdapter {
	
	private List<ParkBean> parkList;
	private List<Double> dstLoc;
	private Context context;
	private MyApp myApp;

	public ParkStateListAdapter(List<ParkBean> parkList, List<Double> dstLoc, Context context, MyApp myApp) {
		super();
		this.parkList = parkList;
		this.dstLoc = dstLoc;
		this.context = context;
		this.myApp = myApp;
	}

	@Override
	public int getCount() {
		return parkList.size();
	}

	@Override
	public Object getItem(int position) {
		return parkList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.mould_park_list, null);
			viewHolder = new ViewHolder();
			viewHolder.parkName = (TextView) convertView.findViewById(R.id.textView1);
			viewHolder.parkAddress = (TextView) convertView.findViewById(R.id.textView2);
			viewHolder.parkDis = (TextView) convertView.findViewById(R.id.textView5);
			convertView.findViewById(R.id.textView4).setOnClickListener(new OnClickListener() {
				//预约车位按钮 点击事件
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context,SubmitOrder.class);
					myApp.setSelectParkBean(parkList.get(position));
					myApp.setDstLoc(dstLoc);
					context.startActivity(intent);
					ParkStateList.parkStateListActivity.finish();
					ParkNodeList.parkNodeListActivity.finish();
				}
			});
			convertView.setTag(viewHolder);
		}else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		//适配
		viewHolder.parkName.setText(parkList.get(position).getTitle()+parkList.get(position).getParknum()+"车位");
		viewHolder.parkAddress.setText(parkList.get(position).getAddr());
		viewHolder.parkDis.setText("距终点"+String.format("%.2f", parkList.get(position).getDis()*0.001)+"公里");
		return convertView;
	}
	
	class ViewHolder{
		TextView parkName,parkAddress,parkDis,tvBook;
	}

}
