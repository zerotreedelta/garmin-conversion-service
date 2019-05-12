package com.zerotreedelta.txi;

public enum EngineDataType {
	TIME_UTC("TIME_UTC", -1),
	EGT1("EGT1", 38),
	EGT2("EGT2", 39),
	EGT3("EGT3", 40),
	EGT4("EGT4", 41),
	EGT5("EGT5", 42),
	EGT6("EGT6", 43),
	CHT1("CHT1", 31),
	CHT2("CHT2", 32),
	CHT3("CHT3", 33),
	CHT4("CHT4", 34),
	CHT5("CHT5", 35),
	CHT6("CHT6", 36),
	FF("FF", 25),
	OAT("OAT", -1), //9
	CLD("CLD", 37),
	OIL_TEMP("OIL", 27),
	FUEL_USED("USD", -1), //21
	VOLTS("VOLTS", 20);
	
	private String jpi;
	private int txi;

	public int getTxi() {
		return txi;
	}

	
	EngineDataType(String jpi, int txi) {
		this.jpi=jpi;
		this.txi=txi;
			
	}

	public String getJpi() {
		return jpi;
	}

	
}
