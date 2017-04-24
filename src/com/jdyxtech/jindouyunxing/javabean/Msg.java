package com.jdyxtech.jindouyunxing.javabean;

public class Msg {
	private String title,msg,created_at;
	private int id,user_id,create_by,msgclass,seestatus;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public int getCreate_by() {
		return create_by;
	}
	public void setCreate_by(int create_by) {
		this.create_by = create_by;
	}
	public int getMsgclass() {
		return msgclass;
	}
	public void setMsgclass(int msgclass) {
		this.msgclass = msgclass;
	}
	public int getSeestatus() {
		return seestatus;
	}
	public void setSeestatus(int seestatus) {
		this.seestatus = seestatus;
	}
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

}
