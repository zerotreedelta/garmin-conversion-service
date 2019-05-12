package com.zerotreedelta.ahrs;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

public class AhrsData {
	
	private Map<DateTime, Map<String, String>> data = new HashMap<>();

	public Map<DateTime, Map<String, String>> getData() {
		return data;
	}

	public void setData(Map<DateTime, Map<String, String>> data) {
		this.data = data;
	}

	
	

}
