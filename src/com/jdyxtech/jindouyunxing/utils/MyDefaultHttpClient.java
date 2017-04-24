package com.jdyxtech.jindouyunxing.utils;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 *用于创建的defaultHttpClient对象，网络超时等参数都已经在该类中设置好了
 * @author Tom
 *
 */
public class MyDefaultHttpClient {
	
	/**
	 * 正式环境下的 服务器主域名
	 */
	public static final String HOST = "http://user.jdyxtech.com";
	/**
	 * 测试环境下的 服务器主域名
	 */
//	public static final String HOST = "http://user-demo.jdyxtech.com";
	
	/**
	 * 全局使用的 defaultHttpClient
	 */
	public static DefaultHttpClient defaultHttpClient;
	
	//公有 并 静态的获取 defaultHttpClient实例的 get()方法
	public static DefaultHttpClient getDefaultHttpClient() {
		defaultHttpClient = new DefaultHttpClient();
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 5 * 1000);// 设置连接超时
		HttpConnectionParams.setSoTimeout(httpParameters, 15 * 1000); // 设置响应超时（等待数据超时）
		defaultHttpClient.setParams(httpParameters); // 将参数传入defaultHttpClient
		return defaultHttpClient;
	}


}
