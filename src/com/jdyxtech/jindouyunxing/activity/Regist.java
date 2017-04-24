package com.jdyxtech.jindouyunxing.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.google.gson.Gson;
import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.javabean.LoginBean;
import com.jdyxtech.jindouyunxing.javabean.RegistBean;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;
import com.jdyxtech.jindouyunxing.utils.NetWorkUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupWindow.OnDismissListener;

/**
 * 用户注册界面，将用户的注册信息提交到服务器
 * @author Tom
 */
public class Regist extends Activity implements OnClickListener {
	
	private EditText editText_name, editText_DriveNum,editText_Pwd,editText_Pwd2;
	private RadioGroup sex_radio_group;
	private String name, sex,phone, drivingid,passwd;
	private ImageView id_imageView1, id_imageView2,driving_licence_imageView1,driving_licence_imageView2;
	private String imgPath = null;
	private Bitmap second_bitmap,identyid_img_0,identyid_img_1,drivingid_img_0,drivingid_img_1;
	private PopupWindow window;
	private int requestCode,checkState;
	private SharedPreferences sp;
	private Editor editor;
	private ContentResolver cr;//内容解析者
	private ProgressDialog proDialog; //注册 进度对话框
	private AlertDialog.Builder alertDialog;//注册成功 提示对话框
	private boolean flag = true; //标志手机号已存在
	private DefaultHttpClient defaultHttpClient = new DefaultHttpClient();

	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) { //表示注册成功
				proDialog.cancel(); //关闭 进度框
				alertDialog.show(); //展示 注册成功提示框
			}else if(msg.what == 0){ //表示注册失败(账户已存在)
				proDialog.cancel(); //关闭 进度框
				alertDialog.setTitle("注册失败");
				alertDialog.setMessage("该账户已存在，请更换手机号并重试！");
				flag = false; //flag设置为false（手机号已存在）: 这样 点击确定后 无法进行下一步
				alertDialog.show();
			}else if (msg.what ==  2) { //注册失败
				proDialog.cancel(); //关闭 进度框
				Toast.makeText(Regist.this, "抱歉，注册失败，请重试！", Toast.LENGTH_LONG).show();
			}else if (msg.what == 3) { //登陆成功
				final MyApp myApp = (MyApp) getApplication();
				myApp.setPhone(phone);
				//启动 百度云推送(凡是登陆处（APP共3处） ，登录成功后 都要启动百度云推送)
				PushManager.startWork(getApplicationContext(),PushConstants.LOGIN_TYPE_API_KEY,"yPdd4GWxMd1MGiHGByNRYepD");
				remberUname_Pwd(); // 调用 记住用户名和密码的方法
       		 	startActivity(new Intent(Regist.this, Location.class));
       		 	finish();
			}
		};
	};	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.regist);
		
		// 初始化控件
		initView();

		// “注册”按钮的点击事件监听
		findViewById(R.id.button_submit).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (checkRegistInfo()) {  //检查输入的注册信息 格式正确之后，才开始上传信息
					proDialog.show(); //展示 上传进度对话框
					if (sex_radio_group.getCheckedRadioButtonId() == R.id.rb_A) {
						sex = "男";
					}else {
						sex = "女";
					}
					name = editText_name.getText().toString().trim();
					drivingid = editText_DriveNum.getText().toString().trim();
					passwd = editText_Pwd2.getText().toString().trim();
					//创建post请求对象
				    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/clientapi/client/register");
			        //向服务器上传json格式的数据
		            try {
		            	JSONObject json = new JSONObject();
						json.put("name", name);
						json.put("sex", sex);
						json.put("drivingid", drivingid);
						if (checkState==2) { //此情况表示是：审核未通过，来重新提交信息，所以电话号码是已知的，pwd也不用在设置了
							json.put("phone", sp.getString("uname", null));
							json.put("passwd", ""); //如果 服务器接收到的 密码是空的，直接认定为这是来重新提交审核信息的
						}else { //表示新用户注册
							json.put("phone", phone);
							json.put("passwd", passwd);
						}
						json.put("identyid_img_0", bitmapToBase64(identyid_img_0));
						json.put("identyid_img_1", bitmapToBase64(identyid_img_1));
						json.put("drivingid_img_0", bitmapToBase64(drivingid_img_0));
						json.put("drivingid_img_1", bitmapToBase64(drivingid_img_1));
						StringEntity se = new StringEntity(json.toString(),HTTP.UTF_8);
						se.setContentEncoding(new BasicHeader(HTTP.UTF_8, "application/json"));
						request.setEntity(se);
					}catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}catch (JSONException e1) {
						e1.printStackTrace();
					}
		            //开启一个线程，进行post带参网络请求
	                new Thread() {       
                        public void run() {     
                        	try {  
                        		HttpResponse httpResponse = defaultHttpClient.execute(request); 
        				         if(httpResponse.getStatusLine().getStatusCode() == 200){
        				        	 String result = EntityUtils.toString(httpResponse.getEntity());
        				        	 Gson gson  = new Gson();
        				        	 RegistBean registResponse = gson.fromJson(result, RegistBean.class);
        				        	 if (200 == registResponse.getStatus()) {
        					        	 handler.sendEmptyMessage(1); //注册成功，告诉主线程中的handler进行相应的操作
        							}else if(402 == registResponse.getStatus() ){
        					        	 handler.sendEmptyMessage(0); //账户已存在，注册失败，告诉主线程中的handler进行相应的操作
        							}else {
        								handler.sendEmptyMessage(2); //注册失败，告诉主线程中的handler进行相应的操作
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
			}
		});
	
	}

	/**
	 * 此方法用于：初始化注册界面中 控件
	 */
	private void initView() {
		sp = getSharedPreferences("user", MODE_PRIVATE);
		editor = sp.edit();
		phone = getIntent().getStringExtra("phone");
		sex_radio_group = (RadioGroup) findViewById(R.id.sex_radio_group);
		editText_name = (EditText) findViewById(R.id.editText_regist_uname);
		editText_DriveNum = (EditText) findViewById(R.id.editText_regist_DriveNum);
		editText_Pwd = (EditText) findViewById(R.id.editText_regist_pwd);
		editText_Pwd2 = (EditText) findViewById(R.id.editText_regist_pwd2);
		id_imageView1 = (ImageView) findViewById(R.id.id_imageView1);
		id_imageView2 = (ImageView) findViewById(R.id.id_imageView2);
		driving_licence_imageView1 = (ImageView) findViewById(R.id.driving_licence_imageView1);
		driving_licence_imageView2 = (ImageView) findViewById(R.id.driving_licence_imageView2);
		proDialog = createDialog();
		alertDialog = creatSuccessAlertDialog();
		
		//以下代码逻辑：如果是 重新提交信息，那么需要隐藏 两个密码输入框
		TextView title = (TextView) findViewById(R.id.title);
		View relative_pwd1 = findViewById(R.id.relative_pwd1);
		View relative_pwd2 = findViewById(R.id.relative_pwd2);
		View tv4 = findViewById(R.id.tv4);
		View tv3 = findViewById(R.id.tv3);
		final MyApp myApp = (MyApp) getApplication();
		checkState = myApp.getCheck(); //账户的审核状态
		if (checkState == 2) { //审核 没有通过，来重新提交注册信息
			title.setText("重新提交");
			relative_pwd1.setVisibility(View.GONE);
			relative_pwd2.setVisibility(View.GONE);
			tv3.setVisibility(View.GONE);
			tv4.setVisibility(View.GONE);
		}
		
	}
	/**
	 * 检查 注册内容的格式是否正确的 方法
	 * @return
	 */
	private boolean checkRegistInfo() {
		if (TextUtils.isEmpty(editText_name.getText())) {
			Toast.makeText(Regist.this, "用户名不能为空", Toast.LENGTH_LONG).show();
			return false;
		}else if (TextUtils.isEmpty(editText_DriveNum.getText())) {
			Toast.makeText(Regist.this, "驾驶证号码不能为空", Toast.LENGTH_LONG).show();
			return false;
		}
		
		if (checkState == 2) { //如果是审核未通过，来重新提交信息的情况，就不用再判断密码项了（因为页面上根本不展示密码项）
			
		}else if (TextUtils.isEmpty(editText_Pwd.getText())||TextUtils.isEmpty(editText_Pwd2.getText())) {
			Toast.makeText(Regist.this, "密码不能为空", Toast.LENGTH_LONG).show();
			return false;
		}else if (!(editText_Pwd.getText().toString().equals(editText_Pwd2.getText().toString()))) {
			Toast.makeText(Regist.this, "两次输入的密码不相同", Toast.LENGTH_LONG).show();
			return false;
		}
		
		if (identyid_img_0==null||identyid_img_1==null||drivingid_img_0==null||drivingid_img_1==null) {
			Toast.makeText(Regist.this, "证件照不能为空！", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}
	/**
	 * 创建 “正在注册” ProgressDialog对话框的 方法
	 */
	public ProgressDialog createDialog() {
		ProgressDialog mypDialog=new ProgressDialog(this);
		mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mypDialog.setTitle("正在提交");
		mypDialog.setMessage("信息上传中，请您耐心等候...");
		mypDialog.setIndeterminate(false);
		mypDialog.setCancelable(true);
		return mypDialog;
	}
	/**
	 * “信息提交，待审核” 提示框 
	 */
	public AlertDialog.Builder creatSuccessAlertDialog() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(Regist.this);
		alertDialog.setMessage("您的信息已提交！1小时内完成审核，请稍后查看审核结果~");
		alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (checkState == 2) { //(审核未通过)来重新提交注册信息的情况，提交之后就可以关闭了
					finish(); 
				}else if(flag) { //flag=true:账户不重复；flag=false:账户已存在
					//注册成功之后 ，点击“确定”登录
					if (NetWorkUtils.isNetworkAvailable(getApplicationContext())) {
						String loginString = phone+passwd+"jdyx";
						String first_md5 = Login.md5(loginString);
						String second_md5 = Login.md5(first_md5);
						login(second_md5); //登陆
					}else {
						startActivity(new Intent(Regist.this,Login.class));
						Toast.makeText(Regist.this, "请检查您的网络连接！", Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		return alertDialog;
	}
	
	/**
	 * 注册成功后，直接登录的方法
	 */
	public void login(final String second_md5) {
		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/clientapi/client/login");
        //向服务器上传json格式的登陆数据
        try {
        	JSONObject json = new JSONObject();
			json.put("phone", phone);
			json.put("passwd", second_md5);
			json.put("type", "android");
			json.put("version","1.3.0");
			StringEntity se = new StringEntity(json.toString(),HTTP.UTF_8);
			se.setContentEncoding(new BasicHeader(HTTP.UTF_8, "application/json"));
			request.setEntity(se);
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}catch (JSONException e1) {
			e1.printStackTrace();
		}
        //开启一个线程，进行post带参网络请求（登录）
        new Thread() {       
            public void run() {     
            	try {  
            		HttpResponse httpResponse = new DefaultHttpClient().execute(request); 
			         if(httpResponse.getStatusLine().getStatusCode() == 200){
			        	 String result = EntityUtils.toString(httpResponse.getEntity());
			        	 Gson gson  = new Gson();
			        	 LoginBean loginResponse = gson.fromJson(result, LoginBean.class);
			        	 if (200 == loginResponse.getStatus()) {
			        		 editor.putString("token", loginResponse.getToken()); //将登陆成功后得到的token保存到sp中
			        		 editor.commit();
				        	 handler.sendEmptyMessage(3); //匹配登陆成功，告诉主线程中的handler进行相应的操作
						}else{
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
	
	/**
	 * 此方法用于记住用户名和密码
	 */
	public void remberUname_Pwd() {
		editor.putString("uname", phone);
		editor.putString("pwd", passwd);
		editor.commit();
	}
	
//**********************
	/**
	 * 此方法 用于拍照并存储到本地（开启定向意图：相机）
	 */
	public void takePicture() {
		// 拍照方式2:用户指定路径存储拍照所得的图片，存储为原图
		Intent intent = new Intent();
		// 设置意图行为为拍照行为
		intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
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
		startActivityForResult(intent, requestCode);
	}
	
	/**
	 * 此方法从相册中选取图片（开启定向意图：相册）
	 */
	public void getPicFromAubum() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);  
        intent.addCategory(Intent.CATEGORY_OPENABLE);  
        intent.setType("image/*");  
        startActivityForResult(intent, requestCode); 
	}

	// 获取位图的（拍照/相册选取） 回传结果
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {// 回传成功
			if (imgPath!=null) {  //通过拍照获取的图片
				Bitmap bitmap = handleBitmap(imgPath); //就去读取imgPath路径中的图片，进行图片二次采样
				//下面的代码主要作用是把图片转一个角度，也可以放大缩小等（先不使用此代码）
//				Matrix m = new Matrix();
//				int width = bitmap.getWidth();
//				int height = bitmap.getHeight();
//				m.setRotate(90); // 旋转angle度
//				second_bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);// 重新生成图片
				second_bitmap = bitmap;
				imgPath = null; //完成后 再将imagePath置空，便于下次使用
			}else {  //从相册中获取图片
				Bitmap bitmap2 =null;
				cr= this.getContentResolver(); //获取到内容解析者
		        Uri uri = data.getData();  //定向意图回传的相册中图片的uri(非文件存储路径)
		        bitmap2 = handleBitmap2(uri,cr); //调用图片二次采样的方法	
		        second_bitmap = bitmap2;
			}

			if (requestCode == 101) {
				id_imageView1.setImageBitmap(second_bitmap);
				identyid_img_0 = second_bitmap;
			} else if (requestCode == 102) {
				id_imageView2.setImageBitmap(second_bitmap);
				identyid_img_1 = second_bitmap;
			}else if (requestCode == 201) {
				driving_licence_imageView1.setImageBitmap(second_bitmap);
				drivingid_img_0 = second_bitmap;
			}else if (requestCode == 202) {
				driving_licence_imageView2.setImageBitmap(second_bitmap);
				drivingid_img_1 = second_bitmap;
			}
		}
	}

	/**
	 * 采样方法1：拍照：根据图片存储path获取图片，进行图片二次采样处理的方法
	 * @return Bitmap
	 */
	public static Bitmap handleBitmap(String path) {
		// 存储缩放比例
		int sampleSize = 6;
		// 创建图片处理类的对象
		BitmapFactory.Options options = new BitmapFactory.Options();
		// 只加载图片的边缘区域，
		options.inJustDecodeBounds = true;
		// 第一采样解码里面的内容bitmap值为null
		BitmapFactory.decodeFile(path, options);
//		// 获取原图的宽和高
//		int w = options.outWidth;
//		int h = options.outHeight;
		// 判断图片的宽和高是不是在你指定的范围内，如果不是就计算缩放比例进行缩小，相反就显示原图
		// 此处判断条件这里是假设，以后要根据自己需求写判断条件
//		if (w > 200) {
//			sampleSize = 3;
//		}
		// 缩放为原图的1/sampleSize；
		options.inSampleSize = sampleSize;
		// 第二次采样解码,加载缩放之后的图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}
	/**
	 * 采样方法2：由ContentResolver解析uri中的图片，进行二次采样处理的方法
	 * @return Bitmap
	 */
	public static Bitmap handleBitmap2(Uri uri,ContentResolver cr) {
		// 存储缩放比例
		int sampleSize = 6;
		// 创建图片处理类的对象
		BitmapFactory.Options options = new BitmapFactory.Options();
		// 只加载图片的边缘区域，
		options.inJustDecodeBounds = true;
		// 第一采样解码里面的内容bitmap值为null
		try {
			BitmapFactory.decodeStream(cr.openInputStream(uri), null, options);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		// 获取原图的宽和高
//		int w = options.outWidth;
//		int h = options.outHeight;
		// 缩放为原图的1/sampleSize；当可以
		options.inSampleSize = sampleSize;
		// 第二次采样解码,加载缩放之后的图片
		options.inJustDecodeBounds = false;
		try {
			return BitmapFactory.decodeStream(cr.openInputStream(uri), null, options);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 此方法 用于将Bitmap转换为String
	 * @param bitmap
	 * @return String
	 */
	public static String bitmapToBase64(Bitmap bitmap) {
		String result = null;
		ByteArrayOutputStream baos = null;
		try {
			if (bitmap != null) {
				baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				baos.flush();
				baos.close();
				byte[] bitmapBytes = baos.toByteArray();
				result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (baos != null) {
					baos.flush();
					baos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	// 点击事件监听
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.id_imageView1:
			showPopwindow(101);
			break;
		case R.id.id_imageView2:
			showPopwindow(102);
			break;
		case R.id.driving_licence_imageView1:
			showPopwindow(201);
			break;
		case R.id.driving_licence_imageView2:
			showPopwindow(202);
			break;
		case R.id.button_back:
			defaultHttpClient.getConnectionManager().shutdown();
			finish();
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		defaultHttpClient.getConnectionManager().shutdown();
		finish();
	}
	
	 /**
	   * 用于显示从底部弹出的popupWindow
	   */
	  private void showPopwindow( int reqCode) {
		  requestCode = reqCode;
	    // 利用layoutInflater获得View
	    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View view = inflater.inflate(R.layout.regist_popwindowlayout, null);
	    // 下面是两种方法得到宽度和高度 getWindow().getDecorView().getWidth()
	    window = new PopupWindow(view,WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
	    // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
	    window.setFocusable(true);
	    // 实例化一个ColorDrawable颜色为半透明
	    ColorDrawable dw = new ColorDrawable(0xffffffff);
	    window.setBackgroundDrawable(dw);
	    // 设置popWindow的显示和消失动画
	    window.setAnimationStyle(R.style.mypopwindow_anim_style);
	    // 在底部显示
	    window.showAtLocation(view,Gravity.BOTTOM, 0, 0);
	    //window中按钮注册点击监听
	    //从相册中选择
	    Button bt_album = (Button) view.findViewById(R.id.bt_album);
	    bt_album.setOnClickListener(new OnClickListener() {
	      @Override
	      public void onClick(View v) {
	    	  getPicFromAubum();
	    	  window.dismiss();
	      }
	    });
	    //拍照
	    Button bt_camer = (Button) view.findViewById(R.id.bt_camer);
	    bt_camer.setOnClickListener(new OnClickListener() {
	      @Override
	      public void onClick(View v) {
	    	  takePicture();
	    	  window.dismiss();
	      }
	    });
	    //取消
	    Button bt_cancle = (Button) view.findViewById(R.id.bt_cancle);
	    bt_cancle.setOnClickListener(new OnClickListener() {
	      @Override
	      public void onClick(View v) {
	    	  window.dismiss();
	      }
	    });

	    //popWindow消失监听方法
	    window.setOnDismissListener(new OnDismissListener() {
	      @Override
	      public void onDismiss() {
	      }
	    });
	  }
	  
	  
}
