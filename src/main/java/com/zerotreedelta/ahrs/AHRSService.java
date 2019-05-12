package com.zerotreedelta.ahrs;

import java.io.File;
import java.io.IOException;

public interface AHRSService {

	public AhrsData getSeries(File g5File) throws IOException;
 
}
