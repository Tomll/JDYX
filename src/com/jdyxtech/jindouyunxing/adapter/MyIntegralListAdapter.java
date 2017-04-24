package com.jdyxtech.jindouyunxing.adapter;

import java.util.List;

import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.javabean.Integral;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MyIntegralListAdapter extends BaseAdapter {

	private Context context;
	private List<Integral> list;
	
	public MyIntegralListAdapter(Context context, List<Integral> list) {
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
			convertView = LayoutInflater.from(context).inflate(R.layout.mould_integral_list, null);
			viewHolder = new ViewHolder();
			viewHolder.textView1 = (TextView) convertView.findViewById(R.id.textView1);
			viewHolder.textView2 = (TextView) convertView.findViewById(R.id.textView2);
			viewHolder.textView3 = (TextView) convertView.findViewById(R.id.textView3);
			convertView.setTag(viewHolder);
		}else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		//数据适配
		if (list.get(position).getIntegral() >= 0) {
			viewHolder.textView1.setText("出行"+String.format("%.2f", list.get(position).getDist()*0.001)+"公里奖励"+list.get(position).getIntegral()+"积分");
			viewHolder.textView2.setText(list.get(position).getIntegral()+"");
		}else if(list.get(position).getIntegral() <0) {
			viewHolder.textView1.setText("出行"+String.format("%.2f", list.get(position).getDist()*0.001)+"公里抵扣"+list.get(position).getIntegral()+"积分");
			viewHolder.textView2.setText(list.get(position).getIntegral()+"");
		}
		viewHolder.textView3.setText(list.get(position).getPaytime());
		return convertView;
	}

	
	class ViewHolder{
		private TextView textView1,textView2,textView3;
	}
	
}
