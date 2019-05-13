package com.zerotreedelta.txi;

import com.zerotreedelta.ahrs.AhrsData;
import com.zerotreedelta.engine.EngineData;

public interface FlyGarminService {

	public String combine(AhrsData ahrs, EngineData engine, int startingFuel);
 
}
