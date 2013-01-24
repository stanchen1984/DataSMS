package org.Stan.db;

import java.io.Serializable;

import android.content.ContentValues;

public class Message implements Serializable {
	public static final String ID = "id";
	public static final String FROMORTO = "fromOrTo";
	public static final String NUMBER = "number";
	public static final String TIME = "time";
	public static final String MILLIONTIME = "millionTime";
	public static final String READABLE = "readable";
	public static final String MSG = "msg";
	public static final String FROM = "from";
	public static final String TO = "to";
	public int id;
	public String fromOrTo;
	public String number;
	public String time;
	public String millionTime;
	public String msg;
	public boolean readable;

	public Message() {
		fromOrTo = "Empty";
		number = "Empty";
		time = "0";
		msg = "Empty";
		millionTime = "0";
		readable = false;
	}

	public Message(String fromOrTo, String number, String time,
			String millionTime, boolean readable, String msg) {
		super();
		this.fromOrTo = fromOrTo;
		this.number = parsePhoneNumber(number);
		this.time = time;
		this.millionTime = millionTime;
		this.readable = readable;
		this.msg = msg;
	}

	@Override
	public String toString() {
		return "Message [id=" + id + ", fromOrTo=" + fromOrTo + ", number="
				+ number + ", time=" + time + ", millionTime=" + millionTime
				+ ", msg=" + msg + ", readable=" + readable + "]";
	}

	public ContentValues getContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(FROMORTO, fromOrTo);
		cv.put(NUMBER, number);
		cv.put(TIME, time);
		cv.put(MILLIONTIME, millionTime);
		cv.put(READABLE, readable);
		cv.put(MSG, msg);
		return cv;
	}

	public String parsePhoneNumber(String phoneNo) {
		if (phoneNo.startsWith("0")) {
			phoneNo.substring(1);
			String temp = "+61";
			temp += phoneNo;
			phoneNo = temp;
		}
		return phoneNo;
	}

	public String getFromOrTo() {
		return fromOrTo;
	}

	public void setFromOrTo(String fromOrTo) {
		this.fromOrTo = fromOrTo;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public void setMillionTime(String millionTime) {
		this.millionTime = millionTime;
	}

	public String getMillionTime() {
		return this.millionTime;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public boolean isReadable() {
		return readable;
	}

	public void setReadable(boolean readable) {
		this.readable = readable;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
