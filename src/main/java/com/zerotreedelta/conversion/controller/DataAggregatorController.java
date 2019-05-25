package com.zerotreedelta.conversion.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
import com.zerotreedelta.engine.EngineData;
import com.zerotreedelta.engine.JpiServiceImpl;
import com.zerotreedelta.txi.TxiServiceImpl;

@CrossOrigin(value = {"*"}, exposedHeaders = {"Content-Disposition"})
@RestController
class DataAggregatorController {

	private static Logger LOG = LoggerFactory.getLogger(DataAggregatorController.class);

	@Autowired
	JpiServiceImpl jpiService;
	
	@Autowired
	G5ServiceImpl g5Service;
	
	@Autowired
	TxiServiceImpl txiServiceImpl;


	
	@PostMapping(value = "/combine", produces = "text/csv")
	public String registerEmailAddresses(@RequestParam("file") MultipartFile file,
		    @RequestParam(value="startingFuel", required=false) Integer startingFuel,
		    @RequestParam(value="jpiSecondsOffset", required=false) Integer jpiSecondsOffset,
			@RequestParam("savvyFlight") String savvyFlight) {

		LOG.debug("POST /combine");
		String response = "";
		try {
			// Get the file and save it somewhere
			byte[] bytes = file.getBytes();
			File f = File.createTempFile("g5upload", "csv");
			Files.write(f.toPath(), bytes);

			AhrsData ahrs = g5Service.getSeries(f);
			EngineData engine = jpiService.getEngineData(savvyFlight, jpiSecondsOffset!=null?jpiSecondsOffset:0);
			response = txiServiceImpl.combine(ahrs, engine, startingFuel!=null?startingFuel:80);
			f.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return response;
	}

}
