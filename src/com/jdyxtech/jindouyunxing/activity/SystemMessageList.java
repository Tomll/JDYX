package com.jdyxtech.jindouyunxing.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.jdyxtech.jindouyunxing.R;
import com.jdyxtech.jindouyunxing.adapter.SystemMsgListAdapter;
import com.jdyxtech.jindouyunxing.adapter.SystemMsgListAdapter.ViewHolder;
import com.jdyxtech.jindouyunxing.javabean.Msg;
import com.jdyxtech.jindouyunxing.javabean.QueryMessage;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
/**
 * 消息列表界面
 * @author Tom
 *
 */
public class SystemMessageList extends Activity implements OnClickListener,OnCheckedChangeListener{
	private SharedPreferences sp;
	private List<Msg> msgList; //请求服务器 获得的消息列表
	private ProgressDialog proDialog; //加载消息列表 进度对话框
	private ListView msgListView;
	private SystemMsgListAdapter msgAdapter; //消息列表适配器
	private Button button_edit;//“编辑” 按钮
	private RelativeLayout relative_edit; //批量操作的相对布局
	private CheckBox checkBox;//批量操作相对布局中的 “全选”checkBox
	private boolean flag = false;// true表示：仅将“全选”按钮 设置为“非全选”就行了，不改变 listView中的item的 选中状态
	private Map<Integer, Integer> map_SelectedMsgId = new HashMap<Integer, Integer>(); //存放选中消息id 的map
	private boolean reLoad = false; //标志位：用来标志是否是重新加载数据 
	private DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if (0 == msg.what) { //消息列表加载成功
				msgAdapter = new SystemMsgListAdapter(SystemMessageList.this, msgList); //创建适配器
				msgListView.setAdapter(msgAdapter); //绑定适配器
				proDialog.cancel();
			}else if (1 == msg.what) { //列表加载失败
				if (reLoad) {
					Toast.makeText(SystemMessageList.this, "列表刷新失败！", Toast.LENGTH_LONG).show();
				}else {
					Toast.makeText(SystemMessageList.this, "消息列表加载失败！", Toast.LENGTH_LONG).show();
				}
				proDialog.cancel();
			}else if (2 == msg.what) { //消息批量操作成功，接下来重新加载数据列表，刷新适配器
				Toast.makeText(SystemMessageList.this, "操作成功！", Toast.LENGTH_LONG).show();
				map_SelectedMsgId.clear(); //清空map
				relative_edit.setVisibility(View.GONE);
				button_edit.setText("编辑");
				msgAdapter.showCheckBox(false);// 隐藏“全选”checkBox
				initData(); //再次加载数据：刷新数据
			}else if (3 == msg.what) { //批量操作失败
				Toast.makeText(SystemMessageList.this, "操作失败，请重试！", Toast.LENGTH_LONG).show();
				proDialog.cancel();
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.system_message_list);
		
		initView(); //初始化组件
		initData(); //初始化数据,放在onResume()中执行了，保证了：从消息详情页面返回后 获取到最新的数据
		//给系统消息列表msgListView注册 item 点击监听
		msgListView.setOnItemClickListener(new OnItemClickListener() {
			@SuppressLint("NewApi")
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
				if (relative_edit.getVisibility() == View.GONE) { //单条点击
					//以下两句代码用于将 消息的未读状态修改为已读
					view.findViewById(R.id.msgState_imageView).setBackgroundResource(R.drawable.have_read); //将被点击的item中的 未读消息图片 设为 已读消息图片
					msgList.get(position).setSeestatus(1);//（本地状态修改）将本地数据列表中的 该条消息设为已读
					setMsgSeeState(msgList.get(position).getId()); //（服务器端状态修改）调用此方法：请求服务器 将被点击的消息状态设置为：已读
					//以下是消息信息的获取，及界面间的传递（SystemMessageList -->SystemMessage ）
					int msgType = msgList.get(position).getMsgclass(); //获取被点击的消息 的类型（1：审核通过2：审核未通过3：其他系统消息）
					String msgTitle = msgList.get(position).getTitle(); //获取被点击的消息的标题title
					String msg = msgList.get(position).getMsg(); //获取被点击的消息的主体 msg
					Intent intent = new Intent(SystemMessageList.this, SystemMessage.class);
					intent.putExtra("msgType", msgType).putExtra("msgTitle", msgTitle).putExtra("msg", msg);
					startActivity(intent); //开启意图，携带消息，跳转到SystemMessage
				}else if (relative_edit.getVisibility()==View.VISIBLE) { //批量操作
					// 取得ViewHolder对象，这样就省去了通过层层的findViewById去实例化我们需要的cb实例的步骤
					ViewHolder holder = (ViewHolder) view.getTag();
					// 将 CheckBox的状态 置反
					holder.checkBox.toggle();
					// 将CheckBox的选中状况记录下来
					SystemMsgListAdapter.getMapMsgSelectedStatus().put(position, holder.checkBox.isChecked());
					// 根据点击后checkBox选中状态，更新 selectedMsgId集合中的数据
					if (holder.checkBox.isChecked() == true) {
						map_SelectedMsgId.put(position, msgList.get(position).getId());
					} else {
						map_SelectedMsgId.remove(position);
					}
					if (!checkBox.isChecked() && map_SelectedMsgId.size() == msgList.size()) {
						checkBox.setChecked(true);
					}else if (checkBox.isChecked() && map_SelectedMsgId.size() < msgList.size()) {
						flag = true;
						checkBox.setChecked(false);
					}
				}
			}
		});
		
	}

	/**
	 * “全选”checkBox的 onCheckedChanged监听回调方法
	 */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (flag) { // 只是将“全选”按钮 设置成了 “非全选”，并没有改变 已经全选的 数据列表的选中状态
			flag = false; // 将flag设置为默认值false ，以便于下一次操作
		} else {
			if (isChecked) { // “全选”
				map_SelectedMsgId.clear(); // “全选”操作之前：先清空map_SelectedMsgId，避免数据积累
				// 遍历 消息列表list的长度，将SystemMsgListAdapter中的map值全部设为true，全部选中
				for (int i = 0; i < msgList.size(); i++) {
					SystemMsgListAdapter.getMapMsgSelectedStatus().put(i, true);
					map_SelectedMsgId.put(i, msgList.get(i).getId());
				}
				msgAdapter.notifyDataSetChanged();// 刷新适配器
			} else { // “取消全选”
				map_SelectedMsgId.clear(); // “取消全选”，所以清空map_SelectedMsgId
				// 遍历 消息列表list的长度，将已选的按钮设为未选
				for (int i = 0; i < msgList.size(); i++) {
					if (SystemMsgListAdapter.getMapMsgSelectedStatus().get(i)) {
						SystemMsgListAdapter.getMapMsgSelectedStatus().put(i, false);
					}
				}
				msgAdapter.notifyDataSetChanged();// 刷新适配器
			}
		}
	}
	
	/**
	 * 初始化 组件的方法
	 */
	private void initView() {
		sp = getSharedPreferences("user", MODE_PRIVATE);
		proDialog = createDialog(); //创建加载消息列表进度对话框
		proDialog.show(); //展示对话框
		msgListView = (ListView) findViewById(R.id.msg_listView);
		relative_edit = (RelativeLayout) findViewById(R.id.relative_edit);
		button_edit = (Button) findViewById(R.id.button_edit);
		checkBox = (CheckBox) findViewById(R.id.checkBox1);
		checkBox.setOnCheckedChangeListener(this);
		
	}
	/**
	 * 初始化数据：从服务器请求 消息列表
	 */
	private void initData() {
		// 创建post请求对象
		final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/msg");
		// 向服务器上传json格式的登陆数据
		try {
			JSONObject json = new JSONObject();
			json.put("token", sp.getString("token", ""));
			StringEntity se = new StringEntity(json.toString(), HTTP.UTF_8);
			se.setContentEncoding(new BasicHeader(HTTP.UTF_8, "application/json"));
			request.setEntity(se);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		// 开启一个线程，进行post带参网络请求
		new Thread() {
			public void run() {
				try {
					HttpResponse httpResponse = defaultHttpClient.execute(request);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						String result = EntityUtils.toString(httpResponse.getEntity());
						QueryMessage queryMessage = new Gson().fromJson(result, QueryMessage.class);
						if (200 == queryMessage.getStatus()) {
							msgList = queryMessage.getLists();
							handler.sendEmptyMessage(0); // 获取消息列表成功，告诉主线程中的handler进行相应的操作
						} else {
							handler.sendEmptyMessage(1); // 获取消息列表失败，告诉主线程中的handler进行相应的操作
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
	 * 创建 “正在加载消息列表” ProgressDialog对话框的 方法
	 */
	public ProgressDialog createDialog() {
		ProgressDialog mypDialog=new ProgressDialog(this);
		mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mypDialog.setTitle("加载消息列表");
		mypDialog.setMessage("正在加载消息列表，请您耐心等候...");
		mypDialog.setIndeterminate(false);
		mypDialog.setCancelable(true);
		return mypDialog;
	}
	/**
	 * 与后台服务器交互，将点击的消息设置为已读状态
	 */
	public void setMsgSeeState(int msgid) {
		// 创建post请求对象
		final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/msg/seestatus");
		// 向服务器上传json格式的登陆数据
		try {
			JSONObject json = new JSONObject();
			json.put("token", sp.getString("token", ""));
			json.put("msgid", msgid);
			json.put("seestatus", 1);
			StringEntity se = new StringEntity(json.toString(), HTTP.UTF_8);
			se.setContentEncoding(new BasicHeader(HTTP.UTF_8, "application/json"));
			request.setEntity(se);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		// 开启一个线程，进行post带参网络请求
		new Thread() {
			public void run() {
				try {
					new DefaultHttpClient().execute(request);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}
	/**
	 * 批量操作消息的方法
	 * @param doType ：消息批量操作类型，1：删除 2：标为已读
	 */
	public void batchOperateMsg(String doType) {
		// 创建post请求对象
		final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/msg/sign");
		// 向服务器上传json格式的登陆数据
		try {
			JSONObject json = new JSONObject();
			json.put("token", sp.getString("token", ""));
			json.put("msglist", getSelectedMsgId());
			json.put("doType", doType); //1：删除 2：标为已读
			StringEntity se = new StringEntity(json.toString(), HTTP.UTF_8);
			se.setContentEncoding(new BasicHeader(HTTP.UTF_8, "application/json"));
			request.setEntity(se);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		// 开启一个线程，进行post带参网络请求
		new Thread() {
			public void run() {
				try {
					HttpResponse httpResponse = defaultHttpClient.execute(request);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						String result = EntityUtils.toString(httpResponse.getEntity());
						JSONObject jsonObject = new JSONObject(result);
						if (200 == jsonObject.getInt("status")) {
							handler.sendEmptyMessage(2); // 批量操作成功，告诉主线程中的handler进行相应的操作
						} else {
							handler.sendEmptyMessage(3); // 批量操作失败，告诉主线程中的handler进行相应的操作
						}
					}
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	/**
	 * 此方法：将map_SelectedMsgId中存放的 选中msg的 id遍历取出，存入jsonArray_SelectedMsgId中
	 */
	public JSONArray getSelectedMsgId() {
		JSONArray jsonArray_SelectedMsgId = new JSONArray();
		Iterator<Entry<Integer, Integer>> iterator = map_SelectedMsgId.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<Integer, Integer> entry = iterator.next();
			jsonArray_SelectedMsgId.put((int)entry.getValue());
		}
		return jsonArray_SelectedMsgId;
	}
	
	//点击监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back: //返回键
			defaultHttpClient.getConnectionManager().shutdown();
			finish();
			break;
		case R.id.button_edit: //“编辑”按钮
			if (relative_edit.getVisibility() == View.GONE) {
				relative_edit.setVisibility(View.VISIBLE);
				button_edit.setText("取消");
				msgAdapter.showCheckBox(true); // 显示 “全选”checkBox
			}else if (relative_edit.getVisibility()==View.VISIBLE) {
				relative_edit.setVisibility(View.GONE);
				button_edit.setText("编辑");
				msgAdapter.showCheckBox(false);// 隐藏“全选”checkBox
			}
			break;
		case R.id.textView1: //“删除”按钮
			if (map_SelectedMsgId.size() <= 0) {
				Toast.makeText(SystemMessageList.this, "请至少选择一条消息！", Toast.LENGTH_SHORT).show();
			} else {
				new AlertDialog.Builder(SystemMessageList.this).setMessage("确定要删除所选的消息吗？").setNegativeButton("取消", null)
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								proDialog.setTitle("删除消息");
								proDialog.setMessage("正在执行操作...");
								proDialog.show();
								reLoad = true; // 表示重新加载数据了
								batchOperateMsg("del");
							}
						}).create().show();
			}
			break;
		case R.id.textView2: //“标为已读”按钮
			if (map_SelectedMsgId.size() <= 0) {
				Toast.makeText(SystemMessageList.this, "请至少选择一条消息！", Toast.LENGTH_SHORT).show();
			} else {
				proDialog.setTitle("标为已读");
				proDialog.setMessage("正在执行操作...");
				proDialog.show();
				reLoad = true; // 表示重新加载数据了
				batchOperateMsg("read");
			}
			break;
		default:
			break;
		}
	}
	
	//返回键
	@Override
	public void onBackPressed() {
		defaultHttpClient.getConnectionManager().shutdown();
		finish();
	}

}
