package com.jdyxtech.jindouyunxing.adapter;

import java.util.List;

import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.javabean.Node;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CarNodeListAdapter extends BaseAdapter {
	private List<Node> carNode_list;
	private Context context;
	private MyApp myApp;
	
	public CarNodeListAdapter(List<Node> carNode_list, Context context, MyApp myApp) {
		super();
		this.carNode_list = carNode_list;
		this.context = context;
		this.myApp = myApp;
	}
	@Override
	public int getCount() {
		return carNode_list.size();
	}
	@Override
	public Object getItem(int position) {
		return carNode_list.get(position);
	}
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.mould_car_node_list, null);
			viewHolder = new ViewHolder();
			viewHolder.carNodeAddr = (TextView) convertView.findViewById(R.id.textView2);
			viewHolder.carNodeDis = (TextView) convertView.findViewById(R.id.textView3);
			viewHolder.carNumImageView = (ImageView) convertView.findViewById(R.id.imageView1);
			viewHolder.textView1 = (TextView) convertView.findViewById(R.id.textView1);
			//车辆网点 “预约”按钮 设置点击事件(一小时后的订单 才会有网点“预约”按钮)
//			convertView.findViewById(R.id.textView1).setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					Toast.makeText(context, "如果是1小时后的订单，显示“预约”按钮，预约该网点", Toast.LENGTH_SHORT).show();
//				}
//			});
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		// 进行适配
//		if (myApp.isMoreThanOneHour()) { // aaaaaaaaaaaaa一小时后的订单
//			viewHolder.textView1.setVisibility(View.VISIBLE); // 显示车辆网点的 “预约”按钮(默认布局是不显示的)
//			viewHolder.carNumImageView.setBackgroundResource(R.drawable.car_small); //表示车辆个数的图标用car_small
//		} else {
			// 车场列表中 表示车辆个数的图标的设置
			switch (carNode_list.get(position).getCarlists().size()) {
			case 0:
				viewHolder.carNumImageView.setBackgroundResource(R.drawable.car_small0);
				break;
			case 1:
				viewHolder.carNumImageView.setBackgroundResource(R.drawable.car_small1);
				break;
			case 2:
				viewHolder.carNumImageView.setBackgroundResource(R.drawable.car_small2);
				break;
			case 3:
				viewHolder.carNumImageView.setBackgroundResource(R.drawable.car_small3);
				break;
			case 4:
				viewHolder.carNumImageView.setBackgroundResource(R.drawable.car_small4);
				break;
			case 5:
				viewHolder.carNumImageView.setBackgroundResource(R.drawable.car_small5);
				break;
			default:
				break;
			}
			viewHolder.carNodeAddr.setText(carNode_list.get(position).getLocal().getAddr() + carNode_list.get(position).getLocal().getTitle());
			viewHolder.carNodeDis.setText("距起点"+String.format("%.2f", carNode_list.get(position).getLocal().getDis()*0.001)+"公里");
//		}
		return convertView;
	}

	class ViewHolder {
		private ImageView carNumImageView;
		private TextView carNodeAddr,carNodeDis,textView1;
	}

}
