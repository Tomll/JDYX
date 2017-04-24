package com.jdyxtech.jindouyunxing.adapter;

import java.util.List;

import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.javabean.Node;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ParkNodeListAdapter extends BaseAdapter {
	private List<Node> parkNode_list;
	private Context context;
	public ParkNodeListAdapter(List<Node> parkNode_list, Context context, MyApp myApp) {
		super();
		this.parkNode_list = parkNode_list;
		this.context = context;
	}

	@Override
	public int getCount() {
		return parkNode_list.size();
	}

	@Override
	public Object getItem(int position) {
		return parkNode_list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.mould_park_node_list, null);
			viewHolder = new ViewHolder();
			viewHolder.parkNodeAddr = (TextView) convertView.findViewById(R.id.textView2);
			viewHolder.parkNodeDis = (TextView) convertView.findViewById(R.id.textView3);
			viewHolder.parkNumImageView = (ImageView) convertView.findViewById(R.id.imageView1);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		//进行适配
		//车场列表中 表示车辆个数的图标的设置
		switch (parkNode_list.get(position).getParklists().size()) {
		case 0:
			viewHolder.parkNumImageView.setBackgroundResource(R.drawable.park_small0);
			break;
		case 1:
			viewHolder.parkNumImageView.setBackgroundResource(R.drawable.park_small1);
			break;
		case 2:
			viewHolder.parkNumImageView.setBackgroundResource(R.drawable.park_small2);
			break;
		case 3:
			viewHolder.parkNumImageView.setBackgroundResource(R.drawable.park_small3);
			break;
		case 4:
			viewHolder.parkNumImageView.setBackgroundResource(R.drawable.park_small4);
			break;
		case 5:
			viewHolder.parkNumImageView.setBackgroundResource(R.drawable.park_small5);
			break;
		default:
			break;
		}
		viewHolder.parkNodeAddr.setText(parkNode_list.get(position).getLocal().getAddr()+parkNode_list.get(position).getLocal().getTitle());
//		viewHolder.parkNodeDis.setText("距离指定终点位置"+parkNode_list.get(position).getLocal().getDis()+"m");
		viewHolder.parkNodeDis.setText("距终点"+String.format("%.2f", parkNode_list.get(position).getLocal().getDis()*0.001)+"公里");
		return convertView;
	}

	class ViewHolder {
		private ImageView parkNumImageView;
		private TextView parkNodeAddr,parkNodeDis;
	}
}
