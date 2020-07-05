package com.zerotreedelta.txi;

import com.zerotreedelta.ahrs.AhrsData;
import com.zerotreedelta.engine.EngineData;

public interface FlyGarminService {

	public DerivedData derive(AhrsData ahrs, EngineData engine, int startingFuel, int secondsOffset);
 
}
