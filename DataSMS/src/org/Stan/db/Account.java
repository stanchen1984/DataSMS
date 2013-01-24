package org.Stan.db;

import java.io.Serializable;

import android.content.ContentValues;

public class Account implements Serializable {
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String NUMBER = "number";
	public static final String MASTERKEY = "masterKey";

	public String name;
	public String number;
	public String masterKey;

	public Account() {
		this.name = "Empty";
		this.number = "Empty";
		this.masterKey = "Empty";
	}

	public Account(String name, String number, String masterKey) {
		super();
		this.name = name;
		this.number = number;
		this.masterKey = masterKey;
	}

	@Override
	public String toString() {
		return "Account [name=" + name + ", number=" + number + ", masterKey="
				+ masterKey + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getMasterKey() {
		return masterKey;
	}

	public void setMasterKey(String masterKey) {
		this.masterKey = masterKey;
	}

	public ContentValues getContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(NAME, name);
		cv.put(NUMBER, number);
		cv.put(MASTERKEY, masterKey);
		return cv;
	}

	public boolean isEmpty() {
		if (this.name.equals("Empty")) {
			return true;
		}
		return false;
	}

}
