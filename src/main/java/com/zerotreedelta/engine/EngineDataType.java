package com.zerotreedelta.engine;

public enum EngineDataType {
	TIME_UTC("TIME_UTC"),
	EGT1("EGT1"),
	EGT2("EGT2"),
	EGT3("EGT3"),
	EGT4("EGT4"),
	EGT5("EGT5"),
	EGT6("EGT6"),
	CHT1("CHT1"),
	CHT2("CHT2"),
	CHT3("CHT3"),
	CHT4("CHT4"),
	CHT5("CHT5"),
	CHT6("CHT6"),
	FF("FF"),
	OAT("OAT"),
//	OIL_TEMP("OIL"),
	FUEL_USED("USD"),
	VOLTS("VOLTS");
	
	private String jpi;

	
	EngineDataType(String jpi) {
		this.jpi=jpi;
			
	}

	public String getJpi() {
		return jpi;
	}

	
}
