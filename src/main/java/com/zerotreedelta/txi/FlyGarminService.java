package com.zerotreedelta.txi;

import org.joda.time.DateTimeZone;

import com.zerotreedelta.ahrs.AhrsData;
import com.zerotreedelta.engine.EngineData;

public interface FlyGarminService {

	public String combine(AhrsData ahrs, EngineData engine, DateTimeZone zone, int startingFuel);
 
}
