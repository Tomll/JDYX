package com.jdyxtech.jindouyunxing.activity;

import java.util.ArrayList;
import java.util.List;

import com.jdyxtech.jindouyunxing.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * 欢迎页
 * @author Tom
 *
 */
public class Welcome extends Activity implements OnPageChangeListener {
	private List<ImageView> list_ImageView = new ArrayList<ImageView>(); //存放 4 张欢迎图片
	private List<ImageView> list_point = new ArrayList<ImageView>(); //存放 4 个底部的圆点
	private ViewPager viewPager;
	private PagerAdapter pagerAdapter;
	private Button button1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.welcome);
		
		initData();
		initView();
		initCtrl();
		viewPager.setAdapter(pagerAdapter);//viewPager绑定适配器
		viewPager.setOnPageChangeListener(this);//viewPaget注册滑动监听
		//“立即体验”设置点击监听
		button1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	   		 	startActivity(new Intent(Welcome.this, Login.class));
	   		 	finish();
			}
		});
		
	}
	
	/**
	 * 初始化data
	 */
	private void initData() {
		//将四个欢迎页 fragment创建出来，放到list_Fragment中
		ImageView imageView1 = new ImageView(Welcome.this);
		imageView1.setScaleType(ScaleType.FIT_XY);
		imageView1.setImageResource(R.drawable.welcome1);
		list_ImageView.add(imageView1);
		ImageView imageView2 = new ImageView(Welcome.this);
		imageView2.setScaleType(ScaleType.FIT_XY);
		imageView2.setImageResource(R.drawable.welcome2);
		list_ImageView.add(imageView2);
		ImageView imageView3 = new ImageView(Welcome.this);
		imageView3.setScaleType(ScaleType.FIT_XY);
		imageView3.setImageResource(R.drawable.welcome3);
		list_ImageView.add(imageView3);
		ImageView imageView4 = new ImageView(Welcome.this);
		imageView4.setScaleType(ScaleType.FIT_XY);
		imageView4.setImageResource(R.drawable.welcome4);
		list_ImageView.add(imageView4);
	}

	/**
	 * 初始化view
	 */
	private void initView() {
		viewPager = (ViewPager) findViewById(R.id.viewPager_welcome);
		button1 = (Button) findViewById(R.id.button1); //“立即体验按钮”
		//find到底部的4个小圆点，存放进集合 list_point中
		ImageView imageView1 = (ImageView) findViewById(R.id.imageView1);
		ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
		ImageView imageView3 = (ImageView) findViewById(R.id.imageView3);
		ImageView imageView4 = (ImageView) findViewById(R.id.imageView4);
		list_point.add(imageView1);
		list_point.add(imageView2);
		list_point.add(imageView3);
		list_point.add(imageView4);
	}

	/**
	 * 初始化adapter
	 */
	private void initCtrl() {
		pagerAdapter = new PagerAdapter() {
			@Override
			public boolean isViewFromObject(View view, Object object) {
				return view == object;
			}
			@Override
			public int getCount() {
				return list_ImageView.size();
			}
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				container.addView(list_ImageView.get(position));
				return list_ImageView.get(position);
			}
			@Override
			public void destroyItem(ViewGroup container, int position, Object object) {
				container.removeView(list_ImageView.get(position));
			}
		};
	}

	//滑动状态改变 回调方法
	@Override
	public void onPageScrollStateChanged(int arg0) {
	}
	//滑动过程中 回调方法
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}
	//滑动完毕后 回调的方法
	@Override
	public void onPageSelected(int arg0) {
		setBigPointAt(arg0); //将滑到的页面数 对应的页面底部的圆点设置为大圆点
		if (arg0 == 3) { //滑到了第4页
			button1.setVisibility(View.VISIBLE); //button1可视
		}else { //前3页面
			button1.setVisibility(View.GONE); //button1不可视
		}
	}

	/**
	 * 将底部的 4 个圆点中的第 position个设置为大圆点，其余的设置为小圆点
	 */
	public void setBigPointAt(int position) {
		for (int i = 0; i < list_point.size(); i++) {
			if (i == position) {
				list_point.get(i).setImageResource(R.drawable.point_big);;
			}else {
				list_point.get(i).setImageResource(R.drawable.point_small);;
			}
		}
	}
	
	
}
