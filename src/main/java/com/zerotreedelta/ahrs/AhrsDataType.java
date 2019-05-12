package com.zerotreedelta.ahrs;

public enum AhrsDataType {
//	SELECTED_TRACK("Selected Track (deg)", ),
//	AP_PITCH("AP Pitch Command (deg)", ),
	BARO_ALT("AltB", 6),
//	VNAV_DEV("VNAV Deviation", ),
//	TIME_UTC("UTC Time", ),
//	TAS("TAS", ), 							//49
//	ATT_STATUS("Attitude Status", ),
//	GPS_VEL_E("GPS Velocity E (m/s)", ),
//	GPS_VEL_N("GPS Velocity N (m/s)", ),
	LATITUDE("Latitude", 4),
	HDG("HDG", 17),
	TRK("TRK", 18),
	GND_SPD("GndSpd", 11),
//	INTERNAL_TEMP("Internal Temperature (deg C)", ),
	LATERAL_ACCEL("Lateral Acceleration (G)", 15),
//	GPS_VDOP("GPS VDOP", ),
//	SEL_AIRSPEED("Selected Airspeed (kt)", ),
	MAG_VAR("MagVar", 60),
	VERT_SPEED("VSpd", 12),
//	AP_ROLL("AP Roll Command (deg)", ),
//	GPS_VEL_U("GPS Velocity U (m/s)", ),
//#	SLIP_SKID("Slip/Skid", ),
//	GPS_HDOP("GPS HDOP", ),
//#	TURN_RATE("Turn Rate", ),
//	GPS_STATUS("GPS Fix Status", ),
	SEL_HDG("Selected Heading (deg)", 59),  //USING BRG FOR NOW
	NAV_FRQ("Nav Frequency (MHz)", 52),
	NAV_SRC("Active Nav Source", 50),
	BARO("Baro Setting (inch Hg)", 7),
//	VNAV_PATH("VNAV Flight Path Angle (deg)", ),
//	BATT("Battery Status", ),
//	UTC_DATE("UTC Date", -1),
	LONGITUDE("Longitude", 5),
	ALT_MSL("AltMSL", 8),
//	GPS_SAT("GPS Sats", ),
	ALT_GPS("AltGPS", 48),
//	SEL_VERT_SPD("Selected Vertical Speed (fpm)", ),
	HORZ_DEV("Horizontal Deviation", 54),
	VERT_DEV("Vertical Deviation", 55),
//	VNAV_TARG_ALT("VNAV Target Altitude (ft)", ),
//	BATTERY_PCT("Battery Charge (%)", ),
	PITCH("Pitch", 13),
	ACCEL("Normal Acceleration (G)", 16),
	IAS("IAS", 10),
	ROLL("Roll", 14),
	SEL_ALT("Selected Altitude (ft)", 73),			//USING PRES ALT FOR NOW
	NAV_CRS("Nav Course (deg)", 51);

	
	private String g5;
	private int txi;

	public int getTxi() {
		return txi;
	}

	
	AhrsDataType(String g5, int txi) {
		this.g5=g5;
		this.txi=txi;
			
	}

	public String getG5() {
		return g5;
	}

	
}
