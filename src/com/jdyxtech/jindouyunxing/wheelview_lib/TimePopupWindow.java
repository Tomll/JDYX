package com.jdyxtech.jindouyunxing.wheelview_lib;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.jdyxtech.jindouyunxing.R;

/**
 * 自定义view ：时间选择器
 * 
 * @author Sai
 * 
 */
public class TimePopupWindow extends PopupWindow implements OnClickListener {

	// 四种选择模式，年月日时分，年月日，时分，月日时分
	public enum Type {
		ALL, YEAR_MONTH_DAY, HOURS_MINS, MONTH_DAY_HOUR_MIN
	}

	private View rootView; // 总的布局
	WheelTime wheelTime;
	EditText  editText;
	private int peopleNum = 1;

	private View btnSubmit, btnCancel,imageView_add,imageView_min;
//	private ImageView 
	private static final String TAG_SUBMIT = "submit";
	private static final String TAG_CANCEL = "cancel";
	private static final String TAG_ADD = "add";
	private static final String TAG_MIN = "min";

	private OnTimeSelectListener timeSelectListener;

	public TimePopupWindow(Context context, Type type) {
		super(context);
		this.setWidth(LayoutParams.FILL_PARENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setBackgroundDrawable(new BitmapDrawable());// 这样设置才能点击屏幕外dismiss窗口
		this.setOutsideTouchable(true);
		this.setAnimationStyle(R.style.timepopwindow_anim_style);

		LayoutInflater mLayoutInflater = LayoutInflater.from(context);
		rootView = mLayoutInflater.inflate(R.layout.popwindowlayout, null);
		// -----确定和取消按钮, +按钮 -按钮
		btnSubmit = rootView.findViewById(R.id.button2);
		btnSubmit.setTag(TAG_SUBMIT);
		btnCancel = rootView.findViewById(R.id.button1);
		btnCancel.setTag(TAG_CANCEL);
		imageView_add = rootView.findViewById(R.id.imageView_add); 
		imageView_add.setTag(TAG_ADD);
		imageView_min = rootView.findViewById(R.id.imageView_min); 
		imageView_min.setTag(TAG_MIN);

		btnSubmit.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		imageView_add.setOnClickListener(this);
		imageView_min.setOnClickListener(this);
		// ----时间转轮
		final View timepickerview = rootView.findViewById(R.id.timepicker);
		ScreenInfo screenInfo = new ScreenInfo((Activity) context);
		wheelTime = new WheelTime(timepickerview, type);  //设置type （年、月、日、时、分、秒：的有无）
		wheelTime.screenheight = screenInfo.getHeight();
		editText = (EditText) rootView.findViewById(R.id.editText1);
		
		//默认选中当前时间
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		wheelTime.setPicker(year, month, day, hours, minute);
		
		setContentView(rootView);
	}

	/**
	 * 设置可以选择的时间范围
	 * 
	 * @param START_YEAR
	 * @param END_YEAR
	 */
	public void setRange(int START_YEAR, int END_YEAR) {
		WheelTime.setSTART_YEAR(START_YEAR);
		WheelTime.setEND_YEAR(END_YEAR);
	}

	/**
	 * 设置选中时间
	 * @param date
	 */
	public void setTime(Date date) {
		Calendar calendar = Calendar.getInstance();
		if (date == null)
			calendar.setTimeInMillis(System.currentTimeMillis());
		else
			calendar.setTime(date);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		wheelTime.setPicker(year, month, day, hours, minute);
	}

	/**
	 * 指定选中的时间，显示选择器
	 * @param parent
	 * @param gravity
	 * @param x
	 * @param y
	 * @param date
	 */
	public void showAtLocation(View parent, int gravity, int x, int y, Date date) {
		Calendar calendar = Calendar.getInstance();
		if (date == null)
			calendar.setTimeInMillis(System.currentTimeMillis());
		else
			calendar.setTime(date);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE); 
		wheelTime.setPicker(year, month, day, hours, minute);
		update();
		super.showAtLocation(parent, gravity, x, y);
	}

	/**
	 * 设置是否循环滚动
	 * 
	 * @param cyclic
	 */
	public void setCyclic(boolean cyclic) {
		wheelTime.setCyclic(cyclic);
	}

	@Override
	public void onClick(View v) {
		String tag = (String) v.getTag();
		if (tag.equals(TAG_CANCEL)) { //pop取消 按钮
			dismiss();
			return;
		} else if (tag.equals(TAG_SUBMIT)) {  //pop 确定 按钮
			if (timeSelectListener != null) {
				try {
					Date date = WheelTime.dateFormat.parse(wheelTime.getTime());
					timeSelectListener.onTimeSelect(date,peopleNum);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			return;
		}else if (tag.equals(TAG_ADD)) { // pop 人数增加的加号 +
			if (peopleNum == 5) { //最多5人
				
			}else {
				peopleNum++;
				editText.setText(""+peopleNum);
			}

		}else if (tag.equals(TAG_MIN)) {  //pop 人数减少的减号 -
			if (peopleNum == 1) {  //最少一人
				
			}else {
				peopleNum--;
				editText.setText(""+peopleNum);
			}
		}
		
	}

	public interface OnTimeSelectListener {
		public void onTimeSelect(Date date,int peopleNum);
	}

	public void setOnTimeSelectListener(OnTimeSelectListener timeSelectListener) {
		this.timeSelectListener = timeSelectListener; //用于将 LocationDemo中new出来的OnTimeSelectListener()传递过来
		
	}

}
