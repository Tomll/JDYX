package com.jdyxtech.jindouyunxing.activity;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.javabean.LoginCheck;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.Window;
/**
 * 非本机号登录，手机号、头像等身份验证界面
 * @author Tom
 *
 */
public class ConfirmIdPhoto extends Activity implements OnClickListener{
	
	private String imgPath = null;//拍摄的照片存储的路径
	private ImageView myPhotoImageView;
	private Bitmap myPhotoBitmap;
	private EditText editText1;
	private ProgressDialog proDialog;
	private DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
	
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			proDialog.cancel();
			if (msg.what == 1) { // 验证信息提交成功
				Toast.makeText(ConfirmIdPhoto.this, "信息验证成功！", Toast.LENGTH_LONG).show();
				startActivity(new Intent(ConfirmIdPhoto.this, Location.class));
				finish();
			} else { // 验证信息提交失败
				Toast.makeText(ConfirmIdPhoto.this, "信息验证失败,请重试！", Toast.LENGTH_LONG).show();
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.confirm_id_photo);

		init();
		
	}

	/**
	 * 初始化的方法
	 */
	private void init() {
		myPhotoImageView = (ImageView) findViewById(R.id.imageView1);
		editText1 = (EditText) findViewById(R.id.editText1);
		proDialog = createDialog();
	}
	
	/**
	 * 创建 “正在打开车门” ProgressDialog对话框的 方法
	 */
	public ProgressDialog createDialog() {
		ProgressDialog mypDialog = new ProgressDialog(this);
		mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mypDialog.setTitle("信息验证");
		mypDialog.setMessage("正在验证信息，请您耐心等候...");
		mypDialog.setIndeterminate(false);
		mypDialog.setCancelable(true);
		return mypDialog;
	}
	
	/**
	 * 此方法用于检查 身份证及照片 是否有误
	 */
	public boolean checkInput() {
		if (TextUtils.isEmpty(editText1.getText().toString().trim())) {
			Toast.makeText(ConfirmIdPhoto.this, "身份证号不能为空", Toast.LENGTH_SHORT).show();
			return true;
		} else if (myPhotoBitmap == null) {
			Toast.makeText(ConfirmIdPhoto.this, "照片不能为空！", Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}

	/**
	 * 向服务器提交 身份证号 和 照片 的方法
	 */
	private void loginCheck() {
		// 创建post请求对象
		final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/clientapi/client/logincheck");
		// 向服务器上传json格式的数据
		try {
			JSONObject json = new JSONObject();
			json.put("phone", getSharedPreferences("user", MODE_PRIVATE).getString("uname", null));
			json.put("drivingid", editText1.getText().toString().trim());
			json.put("identyid_cur_0", Regist.bitmapToBase64(myPhotoBitmap));
			StringEntity se = new StringEntity(json.toString(), HTTP.UTF_8);
			se.setContentEncoding(new BasicHeader(HTTP.UTF_8, "application/json"));
			request.setEntity(se);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		// 开启一个线程，进行post带参网络请求（登录）
		new Thread() {
			public void run() {
				try {
					HttpResponse httpResponse = defaultHttpClient.execute(request);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						String result = EntityUtils.toString(httpResponse.getEntity());
						Gson gson = new Gson();
						LoginCheck loginCheck = gson.fromJson(result, LoginCheck.class);
						if (200 == loginCheck.getStatus()) {
							handler.sendEmptyMessage(1); // 验证成功，告诉主线程中的handler进行相应的操作
						} else {
							handler.sendEmptyMessage(0); // 验证失败，告诉主线程中的handler进行相应的操作
						}
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	// 点击监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back: // “返回”按钮
			defaultHttpClient.getConnectionManager().shutdown();
			startActivity(new Intent(ConfirmIdPhoto.this, Login.class));
			finish();
			break;
		case R.id.textView4: // “确认信息”按钮
			if (checkInput()) { // id及照片 输入有误，直接return
				return;
			}
			loginCheck();
			proDialog.show();
			break;
		case R.id.imageView1: // “拍摄照片”
			takePicture();
			break;
		default:
			break;
		}
	}

	/**
	 *  返回键 
	 */
	@Override
	public void onBackPressed() {
		defaultHttpClient.getConnectionManager().shutdown();
		startActivity(new Intent(ConfirmIdPhoto.this, Login.class));
		finish();
	}
	
//************************************************************************************
	/**
	 * 此方法 用于拍照并存储到本地（开启定向意图：相机）
	 * 拍照方式:用户指定路径，存储拍照所得的图片，存储为原图
	 */
	public void takePicture() {
		// 设置意图行为为拍照行为，采用MediaStore，调用系统原生的相机
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);    
		// 创建一个目录存储图片
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			// 先创建一个目录
			File file = new File(Environment.getExternalStorageDirectory(), "jdyx");
			if (!file.exists()) {
				// mkdir:创建单级目录，如果创建多级目录，父目录不存在的情况下是创建不成功的。
				// mkdirs：创建多级目录，即使父目录不存在，依然能创建成功
				file.mkdirs();
			}
			// 创建一个文件
			File imgFile = new File(file, System.currentTimeMillis() + ".jpg");
			// 获取文件的绝对路径
			imgPath = imgFile.getAbsolutePath();
			// 指定路径存储了
			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imgFile));
		}
		// 启动拍照行为
		startActivityForResult(intent, 0);
	}

	// 获取位图的（拍照/相册选取） 回传结果
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && imgPath != null) {// 回传成功
			Bitmap bitmap = handleBitmap(imgPath); // 就去读取imgPath路径中的图片，进行图片二次采样
			myPhotoImageView.setImageBitmap(bitmap);
			myPhotoBitmap = bitmap;
			imgPath = null;
		}
	}

	/**
	 * 二次采样方法：拍照，根据照片存储的path获取图片，对图片进行二次采样处理
	 * @return Bitmap
	 */
	public Bitmap handleBitmap(String path) {
		// 存储缩放比例
		int sampleSize = 6;
		// 创建图片处理类的对象
		BitmapFactory.Options options = new BitmapFactory.Options();
		// 只加载图片的边缘区域，
		options.inJustDecodeBounds = true;
		// 第一采样解码里面的内容bitmap值为null
		BitmapFactory.decodeFile(path, options);
		// 获取原图的宽和高
		//int w = options.outWidth;
		//int h = options.outHeight;
		// 缩放为原图的1/sampleSize；
		options.inSampleSize = sampleSize;
		// 第二次采样解码,加载缩放之后的图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}
//***************************************************************************************
	
}
