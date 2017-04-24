package com.jdyxtech.jindouyunxing.adapter;

import java.util.List;

import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.javabean.Deposit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MyDepositListAdapter extends BaseAdapter {

	private Context context;
	private List<Deposit> list;
	
	public MyDepositListAdapter(Context context, List<Deposit> list) {
		super();
		this.context = context;
		this.list = list;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.mould_deposite_list, null);
			viewHolder = new ViewHolder();
			viewHolder.textView1 = (TextView) convertView.findViewById(R.id.textView1);
			viewHolder.textView2 = (TextView) convertView.findViewById(R.id.textView2);
			viewHolder.textView3 = (TextView) convertView.findViewById(R.id.textView3);
			convertView.setTag(viewHolder);
		}else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		//数据适配
		viewHolder.textView1.setText(list.get(position).getIntra()); //描述
		viewHolder.textView2.setText(list.get(position).getFree()+""); //金额
		viewHolder.textView3.setText(list.get(position).getCreated_at()); //时间
		return convertView;
	}

	
	class ViewHolder{
		private TextView textView1,textView2,textView3;
	}
	
}
