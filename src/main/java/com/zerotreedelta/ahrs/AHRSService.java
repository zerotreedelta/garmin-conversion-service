package com.zerotreedelta.ahrs;

import java.io.File;
import java.io.IOException;

import org.joda.time.DateTime;

public interface AHRSService {

	public AhrsData getSeries(File g5File) throws IOException;

	DateTime findEstimatedTakeoff(AhrsData ahrs, DateTime jpiEstimated);
 
}
