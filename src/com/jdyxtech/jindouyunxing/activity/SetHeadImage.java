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

import com.jdyxtech.jindouyunxing.MyApp;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.widget.PopupWindow.OnDismissListener;
import android.view.Window;
import android.view.WindowManager;
/**
 * 修改头像界面
 * @author Tom
 *
 */
public class SetHeadImage extends Activity implements OnClickListener{
	private PopupWindow window;
	private String imgPath = null;
	private ImageView head_imageView;
	private Bitmap second_bitmap;
	private ContentResolver cr;//内容解析者
	private SharedPreferences sp;
	private ProgressDialog proDialog; //“正在还车” 进度对话框

	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			proDialog.dismiss();
			if (1 == msg.what) {
				Toast.makeText(SetHeadImage.this, "头像修改成功！", Toast.LENGTH_SHORT).show();
			}else if (0 == msg.what) {
				Toast.makeText(SetHeadImage.this, "头像修改失败！", Toast.LENGTH_SHORT).show();
			}
			finish();
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.set_head_image);
		
		initView();
		
	}

	 private void initView() {
		sp = getSharedPreferences("user", MODE_PRIVATE);
		proDialog = createDialog();
		head_imageView = (ImageView) findViewById(R.id.head_imageView);
		Picasso.with(this).load(((MyApp)getApplication()).getHead_imageUrl()).into(head_imageView);
	}

	/**
	   * 用于显示从底部弹出的popupWindow
	   */
	  private void showPopwindow() {
	    // 利用layoutInflater获得View
	    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View view = inflater.inflate(R.layout.regist_popwindowlayout, null);
	    // 下面是创建popWindow，设置pop中的view以及宽高，方法得到宽度和高度 getWindow().getDecorView().getWidth()
	    window = new PopupWindow(view,WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
	    // 设置popWindow弹出窗体可点击，这句话必须添加，并且是true
	    window.setFocusable(true);
	    // 实例化一个ColorDrawable颜色为半透明
	    ColorDrawable dw = new ColorDrawable(0xffffffff);
	    window.setBackgroundDrawable(dw); //保证点击pop的外部，pop消失 
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
			backgroundAlpha(1.0f); //popWindow消失后设置背景不透明
	      }
	    });
	  }
	
	/**
	 * popwindow弹出时，设置屏幕的背景透明度
	 * @param bgAlpha:透明度
	 */
	public void backgroundAlpha(float bgAlpha) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = bgAlpha; // 0.0-1.0
		getWindow().setAttributes(lp);
	}
	  
	/**
	 * 此方法从相册中选取图片（开启定向意图：相册）
	 */
	public void getPicFromAubum() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		startActivityForResult(intent, 100);
	}
	  
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
		startActivityForResult(intent, 100);
	}
	
	// 获取位图的（拍照/相册选取） 回传结果
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {// 回传成功
			if (imgPath!=null) {  //通过拍照获取的图片
				Bitmap bitmap = Regist.handleBitmap(imgPath); //就去读取imgPath路径中的图片，进行图片二次采样
				second_bitmap = bitmap;
				imgPath = null; //完成后 再将imagePath置空，便于下次使用
			}else {  //从相册中获取图片
				Bitmap bitmap2 =null;
				cr= this.getContentResolver(); //获取到内容解析者
		        Uri uri = data.getData();  //定向意图回传的相册中图片的uri(非文件存储路径)
		        bitmap2 = Regist.handleBitmap2(uri,cr); //调用图片二次采样的方法	
		        second_bitmap = bitmap2;
			}
			head_imageView.setImageBitmap(second_bitmap);
			popAlertDialog();

		}
	}

	/**
	 * 弹出警示窗口
	 */
	public void popAlertDialog() {
		AlertDialog.Builder builder1 = new AlertDialog.Builder(SetHeadImage.this);
		builder1.setCancelable(false).setMessage("是否使用该头像？");
		/**
		 * 设置积极的按钮 text:设置button显示的文本 listener：button的监听事件
		 */
		builder1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				proDialog.show();
				submitHeadImage();
			}
		});
		builder1.setNegativeButton("取消",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});
		builder1.create().show();
	}
	
	/**
	 * 创建“设置头像” ProgressDialog对话框的 方法
	 */
	public ProgressDialog createDialog() {
		ProgressDialog mypDialog =new ProgressDialog(this);
		mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mypDialog.setTitle("设置头像");
		mypDialog.setMessage("正在上传头像，请稍后...");
		mypDialog.setIndeterminate(false);
		mypDialog.setCancelable(true);
		return mypDialog;
	}
	/**
	 *  上传头像图片到服务器 
	 */
	public void submitHeadImage() {

		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/clientapi/client/headpic");
        //向服务器上传Base64格式的用户头像数据
        try {
        	JSONObject json = new JSONObject();
			json.put("token",sp.getString("token", ""));
			json.put("headpic", Regist.bitmapToBase64(second_bitmap));
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
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						String result = EntityUtils.toString(httpResponse.getEntity());
						JSONObject jsonObject = null;
						try {
							jsonObject = new JSONObject(result);
							if (200 == jsonObject.getInt("status")) { // 成功，告诉主线程中的handler进行相应的操作
								handler.sendEmptyMessage(1);
							} else { // 失败，告诉主线程中的handler进行相应的操作
								handler.sendEmptyMessage(0);
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
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
		case R.id.textView1: // “更换头像” 按钮
			backgroundAlpha(0.5f); //popWindow弹出时设置背景 半透
			showPopwindow();
			break;
		case R.id.textView2: // “取消” 按钮
			finish();
			break;

		default:
			break;
		}
	}

	
}
