package com.jdyxtech.jindouyunxing;

import java.util.List;

import com.baidu.mapapi.SDKInitializer;
import com.jdyxtech.jindouyunxing.javabean.CarBean;
import com.jdyxtech.jindouyunxing.javabean.ParkBean;

import android.app.Application;
/**
 * 用于保存 应用中的全局变量
 * @author Tom
 *
 */
public class MyApp extends Application{
	
	private double staLat,staLng,endLat,endLng;
	private String orgName,dstName,startTime,small,phone,orderNum,city;
	private int peopleNum,check,orderOff; //人数、审核状态 、当日取消订单的次数
	private String head_imageUrl; //用户头像 url 
	private String bizType; //交易支付费用 业务类型，“000202”：押金费用 or “000201”：出行费用 
	private String tel; //客服电话 
	private float add_deposit; //该用户需要补缴的押金数
	private int activity_id = 0,assess,cha_time; //优惠券id， 0表示未使用优惠券; assess表示订单是否已经评价 0：未评价 ; cha_time:超时时间，未超时为零
	
	private float free,dist,disttime,activity_free,integral_free;//费用、预估距离、预估时间、使用的优惠券金额、积分抵扣的金额
	private CarBean selectCarBean;
	private ParkBean selectParkBean;
	private List<Double> strLoc; //后期服务器需要：neworder的时候，提交起点停车场的坐标
	private List<Double> dstLoc; //后期服务器需要：neworder的时候，提交终点停车场的坐标
	private boolean falg3,moreThanOneHour; //flag3表示 地图选点or地理编码; isMoreThanOneHour:订单是否为1小时之后的订单
	private long longStartTime,opendoor_at; //订单开始时间 （长整型的）,第一次开启车门的时间（长整型），未开启则为0
	
	@Override
	public void onCreate() {
		super.onCreate();
        SDKInitializer.initialize(this); //百度地图初始化
	}
	
	
	
	public float getIntegral_free() {
		return integral_free;
	}
	public void setIntegral_free(float integral_free) {
		this.integral_free = integral_free;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public float getAdd_deposit() {
		return add_deposit;
	}
	public void setAdd_deposit(float add_deposit) {
		this.add_deposit = add_deposit;
	}
	public List<Double> getStrLoc() {
		return strLoc;
	}
	public void setStrLoc(List<Double> strLoc) {
		this.strLoc = strLoc;
	}
	public List<Double> getDstLoc() {
		return dstLoc;
	}
	public void setDstLoc(List<Double> dstLoc) {
		this.dstLoc = dstLoc;
	}
	public String getHead_imageUrl() {
		return head_imageUrl;
	}
	public int getCha_time() {
		return cha_time;
	}
	public void setCha_time(int cha_time) {
		this.cha_time = cha_time;
	}
	public float getActivity_free() {
		return activity_free;
	}
	public void setActivity_free(float activity_free) {
		this.activity_free = activity_free;
	}
	public int getOrderOff() {
		return orderOff;
	}
	public void setOrderOff(int orderOff) {
		this.orderOff = orderOff;
	}
	public void setHead_imageUrl(String head_imageUrl) {
		this.head_imageUrl = head_imageUrl;
	}
	public int getAssess() {
		return assess;
	}
	public void setAssess(int assess) {
		this.assess = assess;
	}
	public boolean isMoreThanOneHour() {
		return moreThanOneHour;
	}
	public void setMoreThanOneHour(boolean moreThanOneHour) {
		this.moreThanOneHour = moreThanOneHour;
	}
	public long getLongStartTime() {
		return longStartTime;
	}
	public void setLongStartTime(long longStartTime) {
		this.longStartTime = longStartTime;
	}
	public int getCheck() {
		return check;
	}
	public void setCheck(int check) {
		this.check = check;
	}
	public float getDist() {
		return dist;
	}
	public void setDist(float dist) {
		this.dist = dist;
	}
	public float getDisttime() {
		return disttime;
	}
	public void setDisttime(float disttime) {
		this.disttime = disttime;
	}
	public boolean isFalg3() {
		return falg3;
	}
	public void setFalg3(boolean falg3) {
		this.falg3 = falg3;
	}
	public float getFree() {
		return free;
	}
	public void setFree(float free) {
		this.free = free;
	}
	public int getActivity_id() {
		return activity_id;
	}
	public void setActivity_id(int activity_id) {
		this.activity_id = activity_id;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getOrderNum() {
		return orderNum;
	}
	public void setOrderNum(String orderNum) {
		this.orderNum = orderNum;
	}
	public ParkBean getSelectParkBean() {
		return selectParkBean;
	}
	public void setSelectParkBean(ParkBean selectParkBean) {
		this.selectParkBean = selectParkBean;
	}
	public String getSmall() {
		return small;
	}
	public void setSmall(String small) {
		this.small = small;
	}
	public CarBean getSelectCarBean() {
		return selectCarBean;
	}

	public void setSelectCarBean(CarBean selectCarBean) {
		this.selectCarBean = selectCarBean;
	}

	public double getStaLat() {
		return staLat;
	}

	public void setStaLat(double staLat) {
		this.staLat = staLat;
	}

	public double getStaLng() {
		return staLng;
	}

	public void setStaLng(double staLng) {
		this.staLng = staLng;
	}

	public double getEndLat() {
		return endLat;
	}

	public void setEndLat(double endLat) {
		this.endLat = endLat;
	}

	public double getEndLng() {
		return endLng;
	}

	public void setEndLng(double endLng) {
		this.endLng = endLng;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getDstName() {
		return dstName;
	}

	public void setDstName(String dstName) {
		this.dstName = dstName;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public int getPeopleNum() {
		return peopleNum;
	}

	public void setPeopleNum(int peopleNum) {
		this.peopleNum = peopleNum;
	}
	public String getBizType() {
		return bizType;
	}
	public void setBizType(String bizType) {
		this.bizType = bizType;
	}
	public long getOpendoor_at() {
		return opendoor_at;
	}
	public void setOpendoor_at(long opendoor_at) {
		this.opendoor_at = opendoor_at;
	}
	
}
