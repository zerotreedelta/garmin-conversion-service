package com.zerotreedelta.ahrs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReaderHeaderAware;
import com.zerotreedelta.engine.EngineData;
import com.zerotreedelta.engine.JpiServiceImpl;
import com.zerotreedelta.txi.DerivedData;
import com.zerotreedelta.txi.G3xServiceImpl;

@Service
public class G5ServiceImpl implements AHRSService {

	private static Logger LOG = LoggerFactory.getLogger(G5ServiceImpl.class);

	@Override
	public AhrsData getSeries(File g5File) {
		AhrsData result = new AhrsData();

		try {
			
			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC();

			FileReader rdr = new FileReader(g5File);
			BufferedReader br = new BufferedReader(rdr);
			br.readLine();
			br.readLine();
			CSVReaderHeaderAware csv = new CSVReaderHeaderAware(br);
			while (br.ready()) {
				Map<String, String> values = csv.readMap();
				String time = values.get("UTC Time");
				String date = values.get("UTC Date");
				DateTime dt = formatter.parseDateTime(date + " " + time);
				System.out.println(dt);
				result.getData().put(dt, values);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	@Override
	public DateTime findEstimatedTakeoff(AhrsData ahrs, DateTime jpiEstimated) {
		Set<DateTime> timeSet = ahrs.getData().keySet();
		List<DateTime> orderedTime = new ArrayList<>(timeSet);
		Collections.sort(orderedTime);
		Collections.reverse(orderedTime);
		
		DateTime startingPoint = jpiEstimated.plusMinutes(5);
		int startingIndex = orderedTime.indexOf(startingPoint);
		
		boolean foundHighIAS = false;
		for (int i = startingIndex; i<orderedTime.size(); i++) {
			Map<String, String> g5Row = ahrs.getData().get(orderedTime.get(i));
			String ias = g5Row.get(AhrsDataType.IAS.getG5());
			if(ias!=null && !ias.isEmpty()) {
				Double iasDbl = Double.parseDouble(ias);
				if(!foundHighIAS && iasDbl>80) {
					foundHighIAS=true;
				}
				if(foundHighIAS && iasDbl<15.0) {
					return orderedTime.get(i);
				}
			}
		}
		return null;	
		
	}
	
	
	public String combine(AhrsData ahrs, DerivedData derived) {
		StringBuffer result = new StringBuffer();
		
	
		
		Set<DateTime> timeSet = derived.getData().keySet();
		List<DateTime> orderedTime = new ArrayList<>(timeSet);
		Collections.sort(orderedTime);
		
		String[] origHdr = "UTC Date,UTC Time,GPS Fix Status,GPS Sats,Latitude,Longitude,AltGPS,GPS HDOP,GPS VDOP,GPS Velocity E (m/s),GPS Velocity N (m/s),GPS Velocity U (m/s),GndSpd,TRK,HDG,MagVar,AltMSL,Baro Setting (inch Hg),AltB,VSpd,IAS,OAT,TAS,Pitch,Roll,Turn Rate,Slip/Skid,Lateral Acceleration (G),Normal Acceleration (G),Selected Heading (deg),Selected Track (deg),Selected Altitude (ft),Selected Vertical Speed (fpm),Selected Airspeed (kt),Active Nav Source,Nav Course (deg),Nav Frequency (MHz),Horizontal Deviation,Vertical Deviation,VNAV Deviation,VNAV Flight Path Angle (deg),VNAV Altitude (ft),AP Roll Command (deg),AP Pitch Command (deg),Attitude Status,Attitude Dev,Network Status,Internal Temperature (deg C),Supply Voltage (V),Battery Status,Battery Charge (%)".split(",");
//		Set<String> all = ahrs.getData().get(orderedTime.get(0)).keySet();
		
		List<String> orderedHeaders = new ArrayList<String>(Arrays.asList(origHdr));
		List<String> extraHeaders = new ArrayList<String>();
		for (DateTime t : orderedTime) {
			Map<String, String> g5Row = ahrs.getData().get(t);
			Map<String, String> derivedRow = derived.getData().get(t);
			if(derivedRow!=null && g5Row !=null) {
				for(String key: derivedRow.keySet()) {
					if(!"UTC Time".equals(key) && !"USD".equals(key)) {
						g5Row.put(key, derivedRow.get(key));
						if(!orderedHeaders.contains(key) && !extraHeaders.contains(key)) {
							extraHeaders.add(key);
						}
					}
					
				}
			}
		}
		Collections.sort(extraHeaders);
		orderedHeaders.addAll(extraHeaders);
		
		result.append("#info,log_version=\"1.00\",software_part_number=\"006-B2304-23\",software_version=\"6.40\",serial_number=\"4JQ014566\"\n");
		result.append(String.join(",", orderedHeaders)+"\n");
		result.append(String.join(",", orderedHeaders)+"\n");
		
		for (DateTime t : orderedTime) {
			Map<String, String> dataMap = ahrs.getData().get(t);
			if(dataMap!=null) {
				List<String> row = new ArrayList<String>();
				for(String key : orderedHeaders) {
					String val = dataMap.get(key);
					row.add(val!=null?val:"");
				}
				result.append(String.join(",", row)+"\n");
			}
		}
		return result.toString();
		
	}

	public static void main(String... strings) throws IOException {


		JpiServiceImpl jpiService = new JpiServiceImpl();
		EngineData engine = jpiService.getEngineData("4138985/697e9575-dcce-4ba5-8ce3-df7aae3f6d0c");
		
		G5ServiceImpl g5Service = new G5ServiceImpl();
		File f = new File("/home/dodgemich/workspaces/personal/garmin-conversion-service/src/test/resources/eight.csv");
		AhrsData ahrs = g5Service.getSeries(f);

		//DateTime jpiEstimated = jpiService.findTakeoffTime(engine);
		//DateTime g5Takeoff = g5Service.findEstimatedTakeoff(ahrs, jpiEstimated);
		
		int secondsOffset = 28800;///(int)((g5Takeoff.getMillis()-jpiEstimated.getMillis())/1000);
		
		
		G3xServiceImpl i = new G3xServiceImpl();
		DerivedData der = i.derive(ahrs, engine, 54, secondsOffset);
		
		System.out.println(g5Service.combine(ahrs, der));
	}
}