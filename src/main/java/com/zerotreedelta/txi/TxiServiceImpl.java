package com.zerotreedelta.txi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.zerotreedelta.ahrs.AhrsData;
import com.zerotreedelta.ahrs.AhrsDataType;
import com.zerotreedelta.ahrs.G5ServiceImpl;
import com.zerotreedelta.engine.EngineData;
import com.zerotreedelta.engine.JpiServiceImpl;

import net.iakovlev.timeshape.TimeZoneEngine;

@Service
public class TxiServiceImpl implements FlyGarminService {

	private static Logger LOG = LoggerFactory.getLogger(TxiServiceImpl.class);
	private static TimeZoneEngine tzEngine = TimeZoneEngine.initialize(15.8, -165.7, 71.0, -60.0);
	
	@Override
	public String combine(AhrsData ahrs, EngineData engine, int startingFuel) {
		
		StringBuilder result = new StringBuilder();
		try {
			InputStream resource = new ClassPathResource("txi.csv").getInputStream();

			BufferedReader templateReader = new BufferedReader(new InputStreamReader(resource));
			result.append(templateReader.readLine() + "\n");
			result.append(templateReader.readLine() + "\n");
			result.append(templateReader.readLine() + "\n");

			Set<DateTime> timeSet = engine.getData().keySet();
			List<DateTime> orderedTime = new ArrayList<>(timeSet);
			Collections.sort(orderedTime);
			
			DateTimeZone zone = findTimezone(ahrs, orderedTime);
			
			//just grab the first data line as a sample
			String sampleTxiRow = templateReader.readLine();
			for (DateTime t : orderedTime) {
				String[] outputRow = sampleTxiRow.split(",");
				Map<String, String> ahrsRow = ahrs.getData().get(t);
				Map<String, String> engineRow = engine.getData().get(t);

				injectDateTime(outputRow, t, zone);
				injectFuelData(outputRow, engineRow, startingFuel);
				if(ahrsRow!=null) {
					injectGenericData(outputRow, engineRow, ahrsRow);
					injectTempData(outputRow, engineRow, ahrsRow);
				}

				clearColumns(outputRow);

				result.append(String.join(",", outputRow) + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.toString();
	}
	
	public DateTimeZone findTimezone(AhrsData ahrs, List<DateTime> orderedTime) {
		DateTimeZone result = DateTimeZone.UTC;

		Map<ZoneId, Integer> count = new HashMap<>();
		//just check first 400 for a sampling...some errors on GPS boot so can't choose first
		for (int i=0; i<400; i++) {
			DateTime t = orderedTime.get(i);
			Map<String, String> ahrsRow = ahrs.getData().get(t);
			String gps = ahrsRow.get(AhrsDataType.LATITUDE.getG5());
			if(!gps.isEmpty()) {
				Double lat = Double.parseDouble(ahrsRow.get(AhrsDataType.LATITUDE.getG5()));
				Double lon = Double.parseDouble(ahrsRow.get(AhrsDataType.LONGITUDE.getG5()));
				Optional<ZoneId> zoneGuess = tzEngine.query(lat, lon);
				if(zoneGuess.isPresent()) {
					ZoneId id = zoneGuess.get();
					Integer tally = count.get(id)!=null?count.get(id):0;
					tally = tally+1;
					count.put(id, tally);
				}
			}
		}
		Integer maxValue = 0;
		for(ZoneId zid : count.keySet()) {
			Integer tally = count.get(zid);
			if(tally>maxValue) {
				result=DateTimeZone.forID(zid.getId());
				maxValue=tally;
			}
		}
		
		return result;
		
	}

	private void clearColumns(String[] txi) {
		int[] toClear = { 19, 22, 24, 26, 28, 29, 30, 44, 45, 46, 58, 62, 63, 64, 65, 66 };
		for (int col : toClear) {
			txi[col] = "";// date.print(t);
		}

		txi[3] = "______";

	}

	private void injectDateTime(String[] txi, DateTime t, DateTimeZone zone) {
		DateTimeFormatter date = DateTimeFormat.forPattern("yyyy-MM-dd").withZone(zone);
		DateTimeFormatter time = DateTimeFormat.forPattern("HH:mm:ss").withZone(zone);
		DateTimeFormatter tz = DateTimeFormat.forPattern("ZZ").withZone(zone);

		txi[0] = date.print(t);
		txi[1] = time.print(t);
		txi[2] = tz.print(t);

	}

	private void injectGenericData(String[] txi, Map<String, String> engine, Map<String, String> ahrs) {
		for (EngineDataType t : EngineDataType.values()) {
			if (t.getTxi() >= 0) {
				txi[t.getTxi()] = engine.get(t.getJpi());
			}
		}
		for (AhrsDataType t : AhrsDataType.values()) {
			if (t.getTxi() >= 0) {
				txi[t.getTxi()] = ahrs.get(t.getG5());
			}
		}
	}

	private void injectTempData(String[] txi, Map<String, String> engine, Map<String, String> ahrs) {
		// OAT - F to C, #9

		double tempF = Double.parseDouble(engine.get(EngineDataType.OAT.getJpi()));
		double oatC = (tempF - 32) * (0.555);
		txi[9] = String.format("%.1f", oatC);

		// TAS - OAT+IAS, 49
		Double baroAlt = Double.parseDouble(ahrs.get(AhrsDataType.BARO_ALT.getG5()));
		Double baro = Double.parseDouble(ahrs.get(AhrsDataType.BARO.getG5()));
		Double ias = Double.parseDouble(ahrs.get(AhrsDataType.IAS.getG5()));

		Double tas = calculateTAS(baro, baroAlt, oatC, ias);
		txi[49] = String.format("%.0f", tas);

		// Wind - Dir-57, Spd-56
		try {
			Double th = Double.parseDouble(ahrs.get(AhrsDataType.HDG.getG5()));
			Double tc = Double.parseDouble(ahrs.get(AhrsDataType.TRK.getG5()));
			Double gs = Double.parseDouble(ahrs.get(AhrsDataType.GND_SPD.getG5()));
			injectWind(th, tc, tas, gs, txi);
		} catch (Exception e) {
			LOG.debug("Can't calculate winds");
		}
	}

	private void injectFuelData(String[] txi, Map<String, String> engine, double startingFuel) {
		// Fuel - gals 21, lbs 23
		double used = Double.parseDouble(engine.get(EngineDataType.FUEL_USED.getJpi()));
		txi[21] = String.format("%.1f", startingFuel - used);
		txi[22] = "0";
		txi[23] = String.format("%.1f", (startingFuel - used) * 6);
		txi[24] = "0";
	}

	private void injectWind(Double th, Double tc, Double tas, Double gs, String[] txi) {
		Double gsh = Math.sin(Math.toRadians((tc % 360))) * gs; // Horizontal Portion of GS Vector
		Double gsv = Math.cos(Math.toRadians((tc % 360))) * gs; // Vertical Portion of GS Vector
		Double tash = Math.sin(Math.toRadians(th % 360)) * tas; // Horizontal Portion of TAS Vector
		Double tasv = Math.cos(Math.toRadians(th % 360)) * tas; // Vertical Portion of TAS Vector
		Double deltaH = tash - gsh; // Horizontal Vector Difference
		Double deltaV = tasv - gsv; // Vertical Vector Difference
		Double windSpeed = Math.sqrt((deltaH * deltaH) + (deltaV * deltaV)); // Pythagoream Theorem rounded
		Double windDir = Math.toDegrees(Math.atan(deltaH / deltaV)) + (deltaV < 0 ? 180 : 0) % 360;
		// Wind - Dir-57, Spd-56
		txi[56] = String.format("%.0f", windSpeed);
		txi[57] = String.format("%.0f", windDir);
	}

	private Double calculateTAS(Double baroSetting, Double indAlt, Double tempInC, Double ias) {
		// Constants
		Double stdtemp0 = 288.15; // deg Kelvin
		Double lapseRate = 0.0019812; // degrees / foot std. lapse rate C° in to K° result
		Double tempCorr = 273.15; // deg Kelvin

		Double xx = baroSetting / 29.92126;
		Double pressureAlt = indAlt + 145442.2 * (1 - Math.pow(xx, 0.190261));
		Double stdtemp = stdtemp0 - pressureAlt * lapseRate;
		Double tRatio = stdtemp / lapseRate;

		xx = stdtemp / (tempInC + tempCorr); // for temp in deg C
		Double densityAlt = pressureAlt + tRatio * (1 - Math.pow(xx, 0.234969));

		Double aa = densityAlt * lapseRate; // Calculate DA temperature
		Double bb = stdtemp0 - aa; // Correct DA temp to Kelvin
		Double cc = bb / stdtemp0; // Temperature ratio
		Double cc1 = 1 / 0.234969; // Used to find .235 root next
		Double dd = Math.pow(cc, cc1); // Establishes Density Ratio
		dd = Math.pow(dd, .5); // For TAS, square root of DR
		Double ee = 1 / dd; // For TAS; 1 divided by above
//		  var cas = document.densalt.IAS.value;
		Double ff = ee * ias;
		return ff;
	}
	
	
	public static void main(String... strings) throws IOException {

//		JpiServiceImpl jpi = new JpiServiceImpl();
//		EngineData ed = jpi.getEngineData("3192990/c86cb650-f8c0-440e-97d9-24769cdc20f6");
//		
//		G5ServiceImpl imp = new G5ServiceImpl();
//		File f = new File("/home/miodo6/workspaces/personal/garmin-jpi/src/test/resources/g5.csv");
//		AhrsData data = imp.getSeries(f);
//		
//		TxiServiceImpl i = new TxiServiceImpl();
//		System.out.println(i.combine(data, ed, 54));
	}

}