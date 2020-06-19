package com.zerotreedelta.engine;

public enum EngineDataType {
	TIME_UTC("TIME_UTC", "UTC Time", false),
	EGT1("EGT1", "E1 EGT1", true),
	EGT2("EGT2", "E1 EGT2", true),
	EGT3("EGT3", "E1 EGT3", true),
	EGT4("EGT4", "E1 EGT4", true),
	EGT5("EGT5", "E1 EGT5", true),
	EGT6("EGT6", "E1 EGT6", true),
	CHT1("CHT1", "E1 CHT1", true),
	CHT2("CHT2", "E1 CHT2", true),
	CHT3("CHT3", "E1 CHT3", true),
	CHT4("CHT4", "E1 CHT4", true),
	CHT5("CHT5", "E1 CHT5", true),
	CHT6("CHT6", "E1 CHT6", true),
	FF("FF", "E1 FFlow", true),
	OAT("OAT", "OAT", true),
//	OIL_TEMP("OIL"),
	FUEL_USED("USD", "USD", false),
	VOLTS("VOLTS", "bus1volts", true);
	
	private String jpi;
	private String garmin;
	private boolean autoprocess;
	

	EngineDataType(String jpi, String garmin, boolean autoprocess) {
		this.jpi=jpi;
		this.garmin=garmin;
		this.autoprocess=autoprocess;
			
	}

	public String getJpi() {
		return jpi;
	}

	public String getGarmin() {
		return garmin;
	}
	
	public boolean isAutoprocess() {
		return autoprocess;
	}
}
