package com.zerotreedelta.ahrs;

public enum AhrsDataType {
	UTC_DATE("UTC Date", 0),
	TIME_UTC("UTC Time", 1 ),
	LATITUDE("Latitude", 3),
	LONGITUDE("Longitude", 4),
	ALT_GPS("AltGPS", 5),
	GPS_STATUS("GPS Fix Status", 6),
	GND_SPD("GndSpd", 7),
	TRK("TRK", 8),
	GPS_VEL_E("GPS Velocity E (m/s)", 9),
	GPS_VEL_N("GPS Velocity N (m/s)", 10),
	GPS_VEL_U("GPS Velocity U (m/s)", 11),
	HDG("HDG", 12),
	ALT_MSL("AltMSL", 13),
	BARO_ALT("AltB", 14),
	IAS("IAS", 16),
	OAT("OAT", 16),
	TAS("TAS", 99), 
	PITCH("Pitch", 18),
	ROLL("Roll", 19),
	LATERAL_ACCEL("Lateral Acceleration (G)", 21),
	ACCEL("Normal Acceleration (G)", 22),
	TURN_RATE("Turn Rate", 23),
	SLIP_SKID("Slip/Skid", 25),
	SEL_ALT("Selected Altitude (ft)", 28),
	BARO("Baro Setting (inch Hg)", 29),
	NAV_SRC("Active Nav Source", 30),
	NAV_FRQ("Nav Frequency (MHz)", 32),
	NAV_CRS("Nav Course (deg)", 35),
	HORZ_DEV("Horizontal Deviation", 37),
	VERT_DEV("Vertical Deviation", 40),
	VERT_SPEED("VSpd", 49),
	MAG_VAR("MagVar", 58),
	SEL_HDG("Selected Heading (deg)", 59),
	ATT_STATUS("Attitude Status", 64);
//	SELECTED_TRACK("Selected Track (deg)", ),
//	AP_PITCH("AP Pitch Command (deg)", ),
//	VNAV_DEV("VNAV Deviation", ),
//	INTERNAL_TEMP("Internal Temperature (deg C)", ),
//	GPS_VDOP("GPS VDOP", ),
//	SEL_AIRSPEED("Selected Airspeed (kt)", ),
//	AP_ROLL("AP Roll Command (deg)", ),
//	GPS_HDOP("GPS HDOP", ),
//	VNAV_PATH("VNAV Flight Path Angle (deg)", ),
//	BATT("Battery Status", ),
//	GPS_SAT("GPS Sats", ),
//	SEL_VERT_SPD("Selected Vertical Speed (fpm)", ),
//	VNAV_TARG_ALT("VNAV Target Altitude (ft)", ),
//	BATTERY_PCT("Battery Charge (%)", ),

	
	private String g5;
	private int txi;
	
	AhrsDataType(String g5, int txi) {
		this.g5=g5;
		this.txi=txi;
			
	}

	public String getG5() {
		return g5;
	}

	
}
