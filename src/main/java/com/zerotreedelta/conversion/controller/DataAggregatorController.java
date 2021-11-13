package com.zerotreedelta.conversion.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.zerotreedelta.ahrs.AhrsData;
import com.zerotreedelta.ahrs.G5ServiceImpl;
import com.zerotreedelta.conversion.verbose.G5VerboseCombineService;
import com.zerotreedelta.engine.EngineData;
import com.zerotreedelta.engine.JpiServiceImpl;
import com.zerotreedelta.txi.DerivedData;
import com.zerotreedelta.txi.G3xServiceImpl;

@CrossOrigin(value = { "*" }, exposedHeaders = { "Content-Disposition" })
@RestController
class DataAggregatorController {

	private static Logger LOG = LoggerFactory.getLogger(DataAggregatorController.class);

	@Autowired
	JpiServiceImpl jpiService;

	@Autowired
	G5ServiceImpl g5Service;

	@Autowired
	G3xServiceImpl g3xServiceImpl;

	@PostMapping(value = "/combine", produces = "text/csv")
	public String combine(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "startingFuel", required = false, defaultValue = "100") Integer startingFuel,
			@RequestParam(value = "jpiSecondsOffset", required = false) Integer jpiSecondsOffset,
			@RequestParam("savvyFlight") String savvyFlight) {

		LOG.info("POST /combine");

		LOG.warn("File: " + file.getContentType() + ":" + file.getOriginalFilename());

		String response = "";
		if ("text/csv".equals(file.getContentType())) {

			// Get the file and save it somewhere

			try {
				byte[] bytes = file.getBytes();

				File f = File.createTempFile("g5upload", "csv");
				Files.write(f.toPath(), bytes);

				if (savvyFlight != null && !savvyFlight.isEmpty()) {
					response = combineWithJPI(f, startingFuel, jpiSecondsOffset, savvyFlight);
				} else {
					LOG.info("G5 info only");
					response = extendG5Only(f, null);
				}
				f.delete();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if ("application/zip".equals(file.getContentType())) {
			LOG.warn("Combined logs");
			try {
				response = smashTwoLogs(file, jpiSecondsOffset);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

//		 else if ("application/zip".equals(file.getContentType())) {
//				LOG.warn("Verbose logs");
//				try{
//					response = smashVerboseLogs(file, jpiSecondsOffset);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}

		return response;
	}

	private String combineWithJPI(File file, Integer startingFuel, Integer jpiSecondsOffset, String savvyFlight) {
		String response = "";

		// Get the file and save it somewhere
//			byte[] bytes = file.getBytes();
//			File f = File.createTempFile("g5upload", "csv");
//			Files.write(f.toPath(), bytes);

		EngineData engine = jpiService.getEngineData(savvyFlight);
		AhrsData ahrs = g5Service.getSeries(file);
		Integer secondsOffset = jpiSecondsOffset;
		if (secondsOffset == null) {
			LOG.info("auto calc of adjustment");
			DateTime jpiEstimated = jpiService.findTakeoffTime(engine);
			DateTime g5Estimated = g5Service.findEstimatedTakeoff(ahrs, jpiEstimated);

			secondsOffset = (int) ((g5Estimated.getMillis() - jpiEstimated.getMillis()) / 1000);
			LOG.info("Estimated correction: " + secondsOffset);
			LOG.info("JPI:" + jpiEstimated);
			LOG.info("G5 estimated: " + g5Estimated);
		} else {
			LOG.info("Using offset:" + jpiSecondsOffset);
		}

		DerivedData derived = g3xServiceImpl.derive(ahrs, engine, startingFuel, secondsOffset);
		response = g5Service.combine(ahrs, derived);

		return response;
	}

	private String extendG5Only(File ai, File hsi) {
		String response = "";

		// Get the file and save it somewhere
//			byte[] bytes = file.getBytes();
//			File f = File.createTempFile("g5upload", "csv");
//			Files.write(f.toPath(), bytes);

		System.out.println("processing file "+ ai.getName());
		AhrsData aiAhrs = g5Service.getSeries(ai);
		AhrsData hsiAhrs = null;
		if(hsi!=null) {
			System.out.println("processing file 2"+ hsi.getName());

			hsiAhrs = g5Service.getSeries(hsi);
		}
		
		System.out.println("Sizes:" +aiAhrs.getData().size() + ":"+hsiAhrs!=null?hsiAhrs.getData().size():"NA");

		DerivedData derived = g3xServiceImpl.derive(aiAhrs, hsiAhrs);
		response = g5Service.combine(aiAhrs, derived);

		return response;
	}

	private String smashTwoLogs(MultipartFile file, Integer flight) throws IOException {
		ZipFile zipFile = null;
		{
			File temp = File.createTempFile("verbose", "zip");
			InputStream initialStream = file.getInputStream();
			byte[] buffer = new byte[initialStream.available()];
			initialStream.read(buffer);

			try (OutputStream outStream = new FileOutputStream(temp)) {
				outStream.write(buffer);
			}

			zipFile = new ZipFile(temp);
		}

		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		List<File> files = new ArrayList<File>();

		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (!entry.isDirectory() 
					&& (entry.getName().contains(".CSV") || entry.getName().contains(".csv"))
					&& !entry.getName().startsWith(".")
					&& !entry.getName().startsWith("_")
					) {
				
				InputStream stream = zipFile.getInputStream(entry);
				File tmpCsv = File.createTempFile("tmp", "CSV");
				byte[] csvBuffer = new byte[stream.available()];
				stream.read(csvBuffer);
				try (OutputStream outStream = new FileOutputStream(tmpCsv)) {
					outStream.write(csvBuffer);
				}
				stream.close();
				files.add(tmpCsv);
				System.out.println("fromzip:" + entry.getName());
				System.out.println("temp: "+tmpCsv.getName() + ":"+tmpCsv.length());
				
				
			}
		}

		zipFile.close();

		return extendG5Only(files.get(0), files.get(1));
	}

	private String smashVerboseLogs(MultipartFile file, Integer flight) throws IOException {
		G5VerboseCombineService s = new G5VerboseCombineService();

		File temp = File.createTempFile("verbose", "zip");
		InputStream initialStream = file.getInputStream();
		byte[] buffer = new byte[initialStream.available()];
		initialStream.read(buffer);

		try (OutputStream outStream = new FileOutputStream(temp)) {
			outStream.write(buffer);
		}
		return s.combine(temp, flight);
	}

//	public static void main(String... strings) throws IOException {
//
//		DataAggregatorController cont = new DataAggregatorController();
//		File f = new File("/home/dodgemich/workspaces/personal/garmin-conversion-service/src/test/resources/DATA_LOG_orig.CSV");
//		cont.combine(f, 54, "", "4049287/b47f95b5-ca06-43f5-a729-2902fc740a20");
//		JpiServiceImpl jpi = new JpiServiceImpl();
//		EngineData ed = jpi.getEngineData();
//		
//		G5ServiceImpl imp = new G5ServiceImpl();
//		AhrsData data = imp.getSeries(f);
//		
//		G3xServiceImpl i = new G3xServiceImpl();
//		DerivedData der = i.derive(data, ed, 54);
//		
//		System.out.println(imp.combine(data, der));
//	}

}
