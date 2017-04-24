package com.jdyxtech.jindouyunxing.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.android.pushservice.PushMessageReceiver;
import com.jdyxtech.jindouyunxing.MainActivity;
import com.jdyxtech.jindouyunxing.utils.MyDefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
/**
 * 百度推送Receiver
 * @author Tom
 *
 */
public class BaiDuPushResiver extends PushMessageReceiver {
    /**
     * 调用PushManager.startWork后，sdk将对push
     * server发起绑定请求，这个过程是异步的。绑定请求的结果通过onBind返回。 如果您需要用单播推送，需要把这里获取的channel
     * id和user id上传到应用server中，再调用server接口用channel id和user id给单个手机或者用户推送。
     *
     * @param context
     *            BroadcastReceiver的执行Context
     * @param errorCode
     *            绑定接口返回值，0 - 成功
     * @param appid
     *            应用id。errorCode非0时为null
     * @param userId
     *            应用user id。errorCode非0时为null
     * @param channelId
     *            应用channel id。errorCode非0时为null
     * @param requestId
     *            向服务端发起的请求id。在追查问题时有用；
     * @return none
     */
	@Override
	public void onBind(Context context, int errorCode, String appid,String userId, String channelId, String requestId) {
		//上传channelId给服务器
		submintPushChannelId(channelId);
	}

	@Override
	public void onDelTags(Context arg0, int arg1, List<String> arg2, List<String> arg3, String arg4) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onListTags(Context arg0, int arg1, List<String> arg2, String arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessage(Context arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNotificationArrived(Context arg0, String arg1, String arg2, String arg3) {
		// TODO Auto-generated method stub

	}

    /**
     * 接收通知点击的函数。
     *
     * @param context
     *            上下文
     * @param title
     *            推送的通知的标题
     * @param description
     *            推送的通知的描述
     * @param customContentString
     *            自定义内容，为空或者json字符串
     */
	@Override
	public void onNotificationClicked(Context context, String title, String description, String customContentString) {
		int msgType = 0; //消息类型 1：审核通过 2：审核未通过 3：普通消息
		String msgTitle = null; //消息标题
		String msg = null; //消息主体
		//解析customContentString（json字符串），获取到 推送消息时 添加的 附加字段信息
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(customContentString);
			msgType = jsonObject.getInt("msgclass");
			msgTitle = jsonObject.getString("title");
			msg = jsonObject.getString("msg");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Intent intent = new Intent();
        intent.setClass(context.getApplicationContext(), SystemMessage.class).putExtra("msgType", msgType).putExtra("msgTitle", msgTitle).putExtra("msg", msg);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);

	}

	@Override
	public void onSetTags(Context arg0, int arg1, List<String> arg2, List<String> arg3, String arg4) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUnbind(Context arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	/**
	 *  向服务器提交 百度推送的channelId
	 */
	public void submintPushChannelId(String channelId) {
		//创建post请求对象
	    final HttpPost request = new HttpPost(MyDefaultHttpClient.HOST+"/clientapi/client/baiduid");
        //向服务器上传json格式的登陆数据
        try {
        	JSONObject json = new JSONObject();
			json.put("token",MainActivity.sp.getString("token", ""));
			json.put("baiduid", channelId);
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
			        	 //json 解析
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
