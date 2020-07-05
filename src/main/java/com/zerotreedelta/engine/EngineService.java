package com.zerotreedelta.engine;

import org.joda.time.DateTime;

public interface EngineService {

	public EngineData getEngineData(String key);

	DateTime findTakeoffTime(EngineData engine);
 
}
