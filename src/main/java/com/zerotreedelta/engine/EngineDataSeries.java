package com.zerotreedelta.engine;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EngineDataSeries {

	private EngineDataType type;
	private List<String> left;
	
	public EngineDataType getType() {
		return type;
	}

	public void setType(EngineDataType type) {
		this.type = type;
	}
	
	public List<String> getLeft() {
		return left;
	}

	public void setLefts(List<String> data) {
		this.left = data;
	}
	
	public List<String> getData(){
		return this.left;
	}

//    @Override
//    public String toString() {
//        return "BambooUser(" +
//                " fullName=" + fullName +
//                " email=" + email +
//                " isActive=" + isActive.toString() +
//                " )";
//    }

}
