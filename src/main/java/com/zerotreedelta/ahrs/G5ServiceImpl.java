package com.zerotreedelta.ahrs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.opencsv.CSVReaderHeaderAware;

@Service
public class G5ServiceImpl implements AHRSService {

	private static Logger LOG = LoggerFactory.getLogger(G5ServiceImpl.class);

	@Override
	public AhrsData getSeries(File g5File) {
		AhrsData result = new AhrsData();

		try {
			g5File = ResourceUtils.getFile("classpath:g5.csv");

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

	public static void main(String... strings) throws IOException {

		G5ServiceImpl imp = new G5ServiceImpl();
		imp.getSeries(null);
	}
}