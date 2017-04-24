package com.jdyxtech.jindouyunxing.javabean;

import java.io.Serializable;
import java.util.List;

public class Node implements Serializable {
	private Local local;
	private List<CarBean> carlists;
	private List<ParkBean> parklists;
	public Local getLocal() {
		return local;
	}
	public void setLocal(Local local) {
		this.local = local;
	}
	public List<CarBean> getCarlists() {
		return carlists;
	}
	public void setCarlists(List<CarBean> carlists) {
		this.carlists = carlists;
	}
	public List<ParkBean> getParklists() {
		return parklists;
	}
	public void setParklists(List<ParkBean> parklists) {
		this.parklists = parklists;
	}

}
