package com.zerotreedelta.txi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.zerotreedelta.ahrs.AhrsData;
import com.zerotreedelta.ahrs.AhrsDataType;
import com.zerotreedelta.engine.EngineData;
import com.zerotreedelta.engine.EngineDataType;

@Service
public class G3xServiceImpl implements FlyGarminService {

	private static final String WND_DR = "WndDr";
	private static final String WND_SPD = "WndSpd";
	private static final String F_QTY_LLBS = "FQtyLlbs";
	private static final String F_QTY_L = "FQtyL";
	private static final String OAT = "OAT";
	private static final String TAS = "TAS";
	private static final String DENSITY_ALTITUDE = "Density Altitude";
	
	private static final String STATUS_1_TIT = "E1 TIT1";
	private static final String STATUS_2_IAT = "E1 TIT2";
	
	private static Logger LOG = LoggerFactory.getLogger(G3xServiceImpl.class);
	
	@Override
	public DerivedData derive(AhrsData ahrs, EngineData engineOrig, int startingFuel, int secondsOffset) {
		
		
		EngineData engine = adjust(engineOrig, secondsOffset);
		DerivedData result = new DerivedData();
		try {
			Set<DateTime> timeSet = engine.getData().keySet();
			List<DateTime> orderedTime = new ArrayList<>(timeSet);
			Collections.sort(orderedTime);
			
			for (DateTime t : orderedTime) {

				Map<String, String> derivedRow = new HashMap<String, String>();

				Map<String, String> ahrsRow = ahrs.getData().get(t);
				
				Map<String, String> engineRow = engine.getData().get(t);

				injectFuelData(derivedRow, engineRow, startingFuel);
				if(ahrsRow!=null) {
					injectGenericData(derivedRow, engineRow);
					injectTempData(derivedRow, engineRow, ahrsRow);
					injectConfidenceData(derivedRow, ahrsRow);
				}

				result.getData().put(t, derivedRow);
//				result.append(String.join(",", outputRow) + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	@Override
	public DerivedData derive(AhrsData ahrs) {
		DerivedData result = new DerivedData();
		try {
			Set<DateTime> timeSet = ahrs.getData().keySet();
			List<DateTime> orderedTime = new ArrayList<>(timeSet);
			Collections.sort(orderedTime);
			
			for (DateTime t : orderedTime) {

				Map<String, String> derivedRow = new HashMap<String, String>();

				Map<String, String> ahrsRow = ahrs.getData().get(t);

				injectConfidenceData(derivedRow, ahrsRow);

				result.getData().put(t, derivedRow);
//				result.append(String.join(",", outputRow) + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
		
		
	
	private EngineData adjust(EngineData engine, int secondsOffset) {
		Set<DateTime> timeSet = engine.getData().keySet();
		List<DateTime> orderedTime = new ArrayList<>(timeSet);
		Collections.sort(orderedTime);
		EngineData result = new EngineData();
		Map<DateTime, Map<String, String>> resultMap = new HashMap<DateTime, Map<String,String>>();
		result.setData(resultMap);
		for (DateTime t : orderedTime) {
			DateTime adjustedDateTime = t.plusSeconds(secondsOffset);
			resultMap.put(adjustedDateTime, engine.getData().get(t));
		}
		return result;
	}
	

//	private void clearColumns(String[] txi) {
//		for (int i=0; i<txi.length; i++) {
//			txi[i] = "";// date.print(t);
//		}
//	}

	
	private void injectConfidenceData(Map<String, String> derived, Map<String, String> ahrs) {
		String status = ahrs.get("Attitude Status");
		if(status!=null) {
			status = status.replace("N", "");
			status = status.replace("D", "-");
			status = status.replace("X", "");

			String[] fields = status.split(" ");
			derived.put(STATUS_1_TIT, fields[1]);
			derived.put(STATUS_2_IAT, fields[2]);

		}
	}

		

	private void injectGenericData(Map<String, String> derived, Map<String, String> engine) {
		for (EngineDataType t : EngineDataType.values()) {
			if (t.isAutoprocess()) {
				derived.put(t.getGarmin(), engine.get(t.getGarmin()));
			}
		}
	}

	private void injectTempData(Map<String, String> derived, Map<String, String> engine, Map<String, String> ahrs) {
		// OAT - F to C, #9
		try {
			double tempF = Double.parseDouble(engine.get(EngineDataType.OAT.getJpi()));
			double oatC = (tempF - 32) * (0.555);
			String oatCString = String.format("%.1f", oatC);
			derived.put(OAT, oatCString);
			String oatFString = String.format("%d", (int)(tempF));
			derived.put("Nav Identifier", oatFString+'\u00B0'+" F");
			
			// TAS - OAT+IAS, 49
			Double baroAlt = Double.parseDouble(ahrs.get(AhrsDataType.BARO_ALT.getG5()));
			Double baro = Double.parseDouble(ahrs.get(AhrsDataType.BARO.getG5()));
			Double ias = Double.parseDouble(ahrs.get(AhrsDataType.IAS.getG5()));
	
			Double tas = calculateTAS(baro, baroAlt, oatC, ias);
			String trueair = String.format("%.0f", tas);
			derived.put(TAS, trueair);
	
			
			Double densAlt = calculateDensityAlt(baro, baroAlt, oatC, ias);
			String da = String.format("%f", densAlt);
			derived.put(DENSITY_ALTITUDE, da);
			// Wind - Dir-57, Spd-56
			Double th = Double.parseDouble(ahrs.get(AhrsDataType.HDG.getG5()));
			Double tc = Double.parseDouble(ahrs.get(AhrsDataType.TRK.getG5()));
			Double gs = Double.parseDouble(ahrs.get(AhrsDataType.GND_SPD.getG5()));
			injectWind(th, tc, tas, gs, derived);
		} catch (Exception e) {
			LOG.debug("Can't calculate winds/temp", e);
		
		}
	}

	private void injectFuelData(Map<String, String> derived, Map<String, String> engine, double startingFuel) {
		// Fuel - gals 21, lbs 23
		double used = Double.parseDouble(engine.get(EngineDataType.FUEL_USED.getGarmin()));
		String cap = String.format("%.1f", startingFuel - used);
		String lbs = String.format("%.1f", (startingFuel - used) * 6);
		derived.put(F_QTY_L, cap);
		derived.put(F_QTY_LLBS, lbs);
	}

	private void injectWind(Double th, Double tc, Double tas, Double gs, Map<String, String> derived) {
		////WndSpd, WndDr, TAS

		Double gsh = Math.sin(Math.toRadians((tc % 360))) * gs; // Horizontal Portion of GS Vector
		Double gsv = Math.cos(Math.toRadians((tc % 360))) * gs; // Vertical Portion of GS Vector
		Double tash = Math.sin(Math.toRadians(th % 360)) * tas; // Horizontal Portion of TAS Vector
		Double tasv = Math.cos(Math.toRadians(th % 360)) * tas; // Vertical Portion of TAS Vector
		Double deltaH = tash - gsh; // Horizontal Vector Difference
		Double deltaV = tasv - gsv; // Vertical Vector Difference
		Double windSpeed = Math.sqrt((deltaH * deltaH) + (deltaV * deltaV)); // Pythagoream Theorem rounded
		Double windDir = Math.toDegrees(Math.atan(deltaH / deltaV)) + (deltaV < 0 ? 180 : 0) % 360;
		// Wind - Dir-57, Spd-56
		String spd = String.format("%.0f", windSpeed);
		String dir = String.format("%.0f", windDir);
		derived.put(WND_SPD, spd);
		derived.put(WND_DR, dir);

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
	
	private Double calculateDensityAlt(Double baroSetting, Double indAlt, Double tempInC, Double ias) {
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
		return densityAlt;
	}
	
	
//	public static void main(String... strings) throws IOException {
//
//		JpiServiceImpl jpi = new JpiServiceImpl();
//		EngineData ed = jpi.getEngineData("4034913/681541e3-13b4-4778-98b9-8ae89e2dd195");
//		
//		G5ServiceImpl imp = new G5ServiceImpl();
//		File f = new File("/home/dodgemich/workspaces/personal/garmin-conversion-service/src/test/resources/data.csv");
//		AhrsData data = imp.getSeries(f);
//		
//		G3xServiceImpl i = new G3xServiceImpl();
//		System.out.println(i.derive(data, ed, 54, 0));
//	}

}