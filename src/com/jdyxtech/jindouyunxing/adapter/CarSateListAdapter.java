package com.jdyxtech.jindouyunxing.adapter;

import java.util.List;

import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.activity.CarNodeList;
import com.jdyxtech.jindouyunxing.activity.CarStateList;
import com.jdyxtech.jindouyunxing.activity.ParkStateMap;
import com.jdyxtech.jindouyunxing.javabean.CarBean;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CarSateListAdapter extends BaseAdapter {

	private List<CarBean> list;
	private List<Double> strLoc;
	private Context context;
	private MyApp myApp;

	public CarSateListAdapter(List<CarBean> list, List<Double> strLoc, Context context, MyApp myApp) {
		super();
		this.list = list;
		this.strLoc = strLoc;
		this.context = context;
		this.myApp = myApp;
	}

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
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.mould_car_list, null);
			viewHolder = new ViewHolder();
			viewHolder.carImageView = (ImageView) convertView.findViewById(R.id.imageView1);
			viewHolder.carid = (TextView) convertView.findViewById(R.id.carid);
			viewHolder.carBrand = (TextView) convertView.findViewById(R.id.textView1);
			viewHolder.carModel = (TextView) convertView.findViewById(R.id.textView2);
			viewHolder.carSeatNumber = (TextView) convertView.findViewById(R.id.textView3);
			viewHolder.carUnitPrice = (TextView) convertView.findViewById(R.id.textView7);
			viewHolder.carRange = (TextView) convertView.findViewById(R.id.textView6);
			viewHolder.carHeightSpeed = (TextView) convertView.findViewById(R.id.textView4);
			convertView.findViewById(R.id.textView5).setOnClickListener(new OnClickListener() {
				// 车 预约按钮 设置点击事件
				@Override
				public void onClick(View v) {
					Intent intent  = new Intent(context, ParkStateMap.class);
					myApp.setSelectCarBean(list.get(position));
    				myApp.setStrLoc(strLoc); //后期添加：neworder的时候，提交起点停车场的坐标
					context.startActivity(intent);
					CarStateList.carStateListActivity.finish(); //车辆列表界面 关闭
					CarNodeList.carNodeListActivity.finish(); //网点列表界面 关闭
					
				}
			});
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		//数据适配
		Picasso.with(context).load(list.get(position).getSmall()).into(viewHolder.carImageView);
		viewHolder.carid.setText(list.get(position).getCarli());
		viewHolder.carBrand.setText(list.get(position).getBrand());
		viewHolder.carModel.setText(list.get(position).getModel());
		viewHolder.carSeatNumber.setText(list.get(position).getSeats()+"座");
		viewHolder.carUnitPrice.setText(list.get(position).getDis_free()+"元/公里 "+list.get(position).getDis_free()+"元/分钟");
		viewHolder.carRange.setText(list.get(position).getRange()+"公里");
		viewHolder.carHeightSpeed.setText(list.get(position).getHighspeed()+"公里/小时");
		return convertView;
	}

	class ViewHolder {
		private ImageView carImageView;
		private TextView carid,carBrand,carModel,carSeatNumber,carUnitPrice,carRange,carHeightSpeed;
		
	}

}
