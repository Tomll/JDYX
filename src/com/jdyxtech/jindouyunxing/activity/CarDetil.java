package com.jdyxtech.jindouyunxing.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.javabean.CarBean;
import com.jdyxtech.jindouyunxing.widget.FixedSpeedScroller;
import com.squareup.picasso.Picasso;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
/**
 * 车辆详情界面
 * @author Tom
 *
 */
public class CarDetil extends Activity implements OnClickListener,OnPageChangeListener,OnTouchListener {
	private FixedSpeedScroller mScroller;
	
	private List<ImageView> imageList = new ArrayList<ImageView>();
	private TextView textView1,textView2,textView3,textView4,textView5,textView6,textView7,textView8,textView9,textView10;
	private ViewPager viewPager;
	
	private PagerAdapter pagerAdapter;
	private CarBean carBean;
	private List<Double> strLoc;
	private int flag = 0; //标志位：用决定viewPager正向滑动 or 反向滑动，0正向 ，1反向
	private int i = 0;//记录viewPager当前的位置
	private int beforeScroll ;//记录viewPager滑动前的位置
	
	/**
	 *  用于定时切换 headView中的图片
	 */
	private Handler handler2 = new Handler() {
		public void handleMessage(android.os.Message msg) {
			viewPager.setCurrentItem(msg.what);
			// A2:控制切换过程时长 
			mScroller.setmDuration(700);  
		};
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.car_detil);

		Intent intent = getIntent();
		carBean = (CarBean) intent.getSerializableExtra("carBean");
		strLoc = (List<Double>) intent.getSerializableExtra("strLoc");

		
		initView(); //初始化组件
		initData(); //初始化数据
		initCtrl(); //初始化适配器
		onBund(); //将数据数据配到控件上去
		
		viewPager.setAdapter(pagerAdapter);
		viewPager.setOnPageChangeListener(this);//viewPager注册滑动监听
		viewPager.setOnTouchListener(this);
		
		// A1：该try语句块用于修改viewPager图片切换时长。 A1+A2达到控制viewPager图片切换时长的目的
		try {
			Field mField = ViewPager.class.getDeclaredField("mScroller");
			mField.setAccessible(true); // 获取访问权限
			mScroller = new FixedSpeedScroller(viewPager.getContext(), new AccelerateInterpolator());
			mField.set(viewPager, mScroller);
		} catch (Exception e) {
			e.printStackTrace();
		}
		  
		// 开启一个计时线程，计时切换headView中的 imageView
		new Thread(new Runnable() {
			@Override
			public void run() {
				int size = imageList.size();
				while (true) {
					//睡3.5s
					try {
						Thread.sleep(3500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//睡醒开始滑动
					if (size == 0) {

					} else {
						// 下面的两个if-else逻辑：控制viewPager正向、反向交替滑动（1->5 5->1）
						if (i == 0) { //表示 滑到了最左端
							flag = 0; //表示 接下来要正向滑动
						} else if (i == imageList.size()) { //表示滑到了最右端
							flag = 1; //表示接下来要反向滑动
							i = imageList.size() - 2;
						}
						if (flag == 0) { // 正向滑动
							beforeScroll = i; //记录滑动前的位置
							handler2.sendEmptyMessage(i % size); //滑动
							i++; //滑动后自加1
						} else if (flag == 1) { // 反向滑动
							beforeScroll = i;
							handler2.sendEmptyMessage(i % size);
							i--;
						}
					}
				}
			}
		}).start();
		
	}

	/**
	 * viewPager的滑动监听
	 */
	@Override
	public void onPageScrollStateChanged(int arg0) {
	}
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}
	@Override
	public void onPageSelected(int arg0) {
		if (arg0 > beforeScroll && flag == 1) { //表示用户手动 正向滑动，但是viewPager目前是反向滑动
			flag = 0; //以用户为准：将viewPager调整为正向滑动
		}else if (arg0 < beforeScroll && flag == 0) { //表示用户手动 反向滑动，但是viewPager目前是正向滑动
			flag = 1; //以用户为准：将viewPager调整为反向
		}
		beforeScroll = arg0;
		i = arg0;
	}
	
	/**
	 * 初始化数据的方法
	 */
	private void initData() {
		for (int i = 0; i < carBean.getImgs().size(); i++) {
			ImageView imageView = new ImageView(this);
			imageView.setScaleType(ScaleType.FIT_XY);
			imageList.add(imageView);
		}
	}

	/**
	 * 初始化组件的方法
	 */
	private void initView() {
		viewPager = (ViewPager) findViewById(R.id.carDetil_viewPager);
		textView1 = (TextView) findViewById(R.id.textView2);
		textView2 = (TextView) findViewById(R.id.textView1);
		textView3 = (TextView) findViewById(R.id.textView3);
		textView4 = (TextView) findViewById(R.id.textView4);
		textView5 = (TextView) findViewById(R.id.textView5);
		textView6 = (TextView) findViewById(R.id.textView6);
		textView7 = (TextView) findViewById(R.id.textView7);
		textView8 = (TextView) findViewById(R.id.textView8);
		textView9 = (TextView) findViewById(R.id.textView9);
		textView10 = (TextView) findViewById(R.id.textView10);
	}

	/**
	 * 初始化适配器（控制器的方法）
	 */
	private void initCtrl() {

		pagerAdapter = new PagerAdapter() {
			@Override
			public boolean isViewFromObject(View view, Object object) {
				return view == object;
			}

			@Override
			public int getCount() {
				return carBean.getImgs().size();
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				Picasso.with(getApplicationContext()).load(carBean.getImgs().get(position)).into(imageList.get(position));
				container.addView(imageList.get(position));
				return imageList.get(position);
			}

			@Override
			public void destroyItem(ViewGroup container, int position, Object object) {
				container.removeView(imageList.get(position));
			}

		};
	}

	/**
	 * 绑定适配器
	 */
	private void onBund() {
		textView1.setText(carBean.getBrand()); 
		textView2.setText(carBean.getModel());
		textView3.setText("舒适型");
		textView4.setText(" "+carBean.getCarli()+" ");
		textView5.setText(carBean.getAddr()+carBean.getTitle()+carBean.getParknum()+"车位\n距起点"+String.format("%.2f", carBean.getDis()*0.001)+"公里");
		textView6.setText(" "+carBean.getDis_free()+"元/公里 "+carBean.getDis_free()+"元/分钟");
		textView7.setText(" "+carBean.getSeats()+"座");
		textView8.setText(" "+carBean.getRange()+"公里");
		textView9.setText(" "+carBean.getNeat()+"级");
		textView10.setText(" "+carBean.getHighspeed()+"公里/小时");
	}

	/**
	 * Touch监听回调（主要用于监听ViewPager的Touch事件）
	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		
		return false;
	}
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back: // 返回按钮
			finish();
			break;
		case R.id.textView11: // 选定车辆 按钮
			Intent intent = new Intent(this, ParkStateMap.class);
			final MyApp myApp = (MyApp) getApplication();
			myApp.setSelectCarBean(carBean);
			myApp.setStrLoc(strLoc); // 后期添加：neworder的时候，提交起点停车场的坐标
			startActivity(intent);
			if (null != CarStateList.carStateListActivity) {
				CarStateList.carStateListActivity.finish();
			}
			if (null != CarNodeList.carNodeListActivity) {
				CarNodeList.carNodeListActivity.finish();
			}
			finish();
			break;
		default:
			break;
		}
	}


	
}
