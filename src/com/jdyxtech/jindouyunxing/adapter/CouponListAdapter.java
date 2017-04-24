package com.jdyxtech.jindouyunxing.adapter;

import java.util.List;

import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.javabean.Coupon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CouponListAdapter extends BaseAdapter {
	private Context context;
	private List<Coupon> list;
	private int posi; //选中的 优惠券的在列表中的位置

	public CouponListAdapter(Context context, List<Coupon> list, int posi) {
		super();
		this.context = context;
		this.list = list;
		this.posi = posi;
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
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.mould_coupon_list, null);
			viewHolder = new ViewHolder();
			viewHolder.couponImageView = (ImageView) convertView.findViewById(R.id.imageView1);
			viewHolder.couponFree = (TextView) convertView.findViewById(R.id.textView1);
			viewHolder.couponOutTime = (TextView) convertView.findViewById(R.id.textView2);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		// 接下来进行适配
		if (position == posi) { //被选中的优惠券打上对勾，小于0表示未使用优惠券
			viewHolder.couponImageView.setImageResource(R.drawable.coupon_selected);
		}
		viewHolder.couponFree.setText(list.get(position).getFree() + " 元");
		viewHolder.couponOutTime.setText("有效期至：" + list.get(position).getOuttime());
		return convertView;
	}

	class ViewHolder {
		private ImageView couponImageView;
		private TextView couponFree, couponOutTime;
	}
	
}
