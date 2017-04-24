package com.jdyxtech.jindouyunxing.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.javabean.Msg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class SystemMsgListAdapter extends BaseAdapter {
	
	/**
	 * 适配器中的这个Map，用于标记 每一个checkBox的选中状态
	 */
	private static Map<Integer,Boolean> mapMsgSelectedStatus;  //记录checkBox是否选中
	private Context context;
	private List<Msg> msgList; //原始适配数据
	private boolean showCheckBox = false; //标志位：决定是否展示单选框，默认是false，不显示
	
	/**
	 * 带参构造
	 * @param context
	 * @param list
	 */
	public SystemMsgListAdapter(Context context, List<Msg> list) {
		super();
		this.context = context;
		this.msgList = list;
		this.mapMsgSelectedStatus = new HashMap<Integer, Boolean>();
		//初始化map，默认value = false
		for (int i = 0; i < msgList.size(); i++) { 
			mapMsgSelectedStatus.put(i, false); 
        } 
	}
	/**
	 * 适配器中的 MapMsgSelectedStatus的 get()方法
	 */
	public static Map<Integer, Boolean> getMapMsgSelectedStatus() {
		return mapMsgSelectedStatus;
	}
	/**
	 * 适配器中的 MapMsgSelectedStatus的 set()方法
	 */
	public static void setMapMsgSelectedStatus(Map<Integer, Boolean> mapMsgSelectedStatus) {
		SystemMsgListAdapter.mapMsgSelectedStatus = mapMsgSelectedStatus;
	}
	
	/**
	 * 调用适配器中的这个方法：显示出每条消息 前面的checkBox
	 */
	public void showCheckBox(boolean showCheckBox) {
		this.showCheckBox = showCheckBox;
		notifyDataSetChanged();
	}

	//适配器中其他必须重写的方法
	@Override
	public int getCount() {
		return msgList.size();
	}

	@Override
	public Object getItem(int position) {
		return msgList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.mould_msg_list, null);
			viewHolder = new ViewHolder();
			viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox1);
			viewHolder.msgState_imageView = (ImageView) convertView.findViewById(R.id.msgState_imageView);
			viewHolder.msgTextView = (TextView) convertView.findViewById(R.id.msg_textView);
			viewHolder.msgTime_TextView = (TextView) convertView.findViewById(R.id.msgTime_textView);
			viewHolder.imageView_enter = (ImageView) convertView.findViewById(R.id.imageView_enter);
			convertView.setTag(viewHolder); //设置标签
		}else {
			viewHolder = (ViewHolder) convertView.getTag(); //获取标签
		}

		//************以下是数据的适配****************
		// 设置checkBox的选中状态
		viewHolder.checkBox.setChecked(mapMsgSelectedStatus.get(position));
		// 设置CheckBox、箭头图片 可视与否
		if (showCheckBox) {
			viewHolder.checkBox.setVisibility(View.VISIBLE);
			viewHolder.imageView_enter.setVisibility(View.GONE);
		} else {
			viewHolder.checkBox.setVisibility(View.GONE);
			viewHolder.imageView_enter.setVisibility(View.VISIBLE);
		}
		// 消息的 展示
		if (msgList.get(position).getSeestatus() == 0) { // 0 :未读
			viewHolder.msgState_imageView.setBackgroundResource(R.drawable.have_not_read);
		} else {
			viewHolder.msgState_imageView.setBackgroundResource(R.drawable.have_read);
		}
		viewHolder.msgTextView.setText(msgList.get(position).getMsg()); // 消息主体
		viewHolder.msgTime_TextView.setText(msgList.get(position).getCreated_at()); // 消息创建时间
		return convertView;
	}
	
	
	/**
	 * 此内部类用于 存放mould中的各个控件
	 */
	public static class ViewHolder {
		public CheckBox checkBox;
		public ImageView msgState_imageView,imageView_enter;
		public TextView msgTextView,msgTime_TextView;
	}


} 
