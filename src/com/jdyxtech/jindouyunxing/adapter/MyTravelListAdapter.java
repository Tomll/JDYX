package com.jdyxtech.jindouyunxing.adapter;

import java.util.List;

import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.javabean.MyTravelBean;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyTravelListAdapter extends BaseAdapter {

	private Context context;
	private List<MyTravelBean> list ;
	
	public MyTravelListAdapter(Context context, List<MyTravelBean> list) {
		super();
		this.context = context;
		this.list = list;
	}
	
//	public void setData(List<MyTravelBean> list1) {
//		list = list1;
//		notifyDataSetChanged();
//	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.mould_travel_list, null);
			viewHolder = new ViewHolder();
			viewHolder.orgName = (TextView) convertView.findViewById(R.id.textView1);
			viewHolder.dstName= (TextView) convertView.findViewById(R.id.textView4);
			viewHolder.startTime= (TextView) convertView.findViewById(R.id.textView2);
			viewHolder.carli= (TextView) convertView.findViewById(R.id.textView3);
			viewHolder.imageView1 = (ImageView) convertView.findViewById(R.id.imageView1);
			convertView.setTag(viewHolder);
		}else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.imageView1.setTag(list.get(position).getOrder_num());
		viewHolder.orgName.setText(list.get(position).getOrg_name());
		viewHolder.dstName.setText(list.get(position).getDst_name());
		viewHolder.startTime.setText(list.get(position).getStarttime());
		viewHolder.carli.setText(" "+list.get(position).getOrg().getCarli()+" ");
			//超时系统取消的订单 或 手动取消的订单 或 已经完成并支付的订单：payState不等于0（未支付0、支付宝支付成功1、银联支付成功2、微信支付成功3）  用灰色表示
			if (4 == list.get(position).getOrderstatus()||3 == list.get(position).getOrderstatus()|| 0 != list.get(position).getPaystatus()) { //手动取消的订单 或 成功支付的订单 都是历史订单，用灰色表示
				viewHolder.imageView1.setImageResource(R.drawable.arrow_grey);
				viewHolder.carli.setBackgroundColor(Color.parseColor("#c0c0c0")); //深灰色
			}else { //未完成（未支付）的订单用绿色表示
				viewHolder.imageView1.setImageResource(R.drawable.arrow_green);
				viewHolder.carli.setBackgroundColor(Color.parseColor("#01CF97"));//浅绿色
			}
	
		return convertView;
	}

	class ViewHolder {
		private TextView orgName,dstName,startTime,carli;
		private ImageView imageView1;
	}
	
}
