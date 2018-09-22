package com.heima.testbluetooth.entitys;

import java.io.Serializable;

/**
 * 数据传输类
 *
 */
public class Message implements Serializable{

	private String msg = "";

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getMsg() {
		return this.msg;
	}

}
