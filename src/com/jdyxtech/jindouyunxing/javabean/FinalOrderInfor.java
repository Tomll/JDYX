package com.jdyxtech.jindouyunxing.javabean;

public class FinalOrderInfor {
	private String desc;
	private int status,dist,cha_time;
	private float free,disttime;
	private String integral_str; //“N积分抵扣 N 元”
	private float deduction_free; //积分可以抵扣金额
	private Activitys activity;//订单实际信息中，服务器默认返回的优惠券对象
	
	
	public String getIntegral_str() {
		return integral_str;
	}
	public void setIntegral_str(String integral_str) {
		this.integral_str = integral_str;
	}
	public float getDeduction_free() {
		return deduction_free;
	}
	public void setDeduction_free(float deduction_free) {
		this.deduction_free = deduction_free;
	}
	public int getCha_time() {
		return cha_time;
	}
	public void setCha_time(int cha_time) {
		this.cha_time = cha_time;
	}
	public Activitys getActivity() {
		return activity;
	}
	public void setActivity(Activitys activity) {
		this.activity = activity;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getDist() {
		return dist;
	}
	public void setDist(int dist) {
		this.dist = dist;
	}
	public float getFree() {
		return free;
	}
	public void setFree(float free) {
		this.free = free;
	}
	public float getDisttime() {
		return disttime;
	}
	public void setDisttime(float disttime) {
		this.disttime = disttime;
	}




}
