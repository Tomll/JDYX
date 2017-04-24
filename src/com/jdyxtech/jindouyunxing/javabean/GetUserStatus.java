package com.jdyxtech.jindouyunxing.javabean;

/**
 * 用户状态 javaBean
 */
public class GetUserStatus {
	private int status ,manger_check ,lock,newmsg,orderoff ;
	private float deposit,add_deposit;
	private FirstOrderstatus order;
	private String desc, headpic,tel,version,downloadurl;
	

	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getDownloadurl() {
		return downloadurl;
	}
	public void setDownloadurl(String downloadurl) {
		this.downloadurl = downloadurl;
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
	public int getLock() {
		return lock;
	}
	public void setLock(int lock) {
		this.lock = lock;
	}
	public int getNewmsg() {
		return newmsg;
	}
	public void setNewmsg(int newmsg) {
		this.newmsg = newmsg;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getManger_check() {
		return manger_check;
	}
	public void setManger_check(int manger_check) {
		this.manger_check = manger_check;
	}
	public int getOrderoff() {
		return orderoff;
	}
	public void setOrderoff(int orderoff) {
		this.orderoff = orderoff;
	}
	public float getDeposit() {
		return deposit;
	}
	public void setDeposit(float deposit) {
		this.deposit = deposit;
	}
	public FirstOrderstatus getOrder() {
		return order;
	}
	public void setOrder(FirstOrderstatus order) {
		this.order = order;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getHeadpic() {
		return headpic;
	}
	public void setHeadpic(String headpic) {
		this.headpic = headpic;
	}

}
