package com.zerotreedelta.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class JpiServiceImpl implements EngineService {

	private static Logger LOG = LoggerFactory.getLogger(JpiServiceImpl.class);

	@Override
	public EngineData getEngineData(String key, int secondsOffset) {
		EngineData result = new EngineData();
		EngineDataSeries timeSeries = getSeries(key, EngineDataType.TIME_UTC);
		int intervalInSeconds = getIntervalInSeconds(timeSeries);
		
		Map<EngineDataType, EngineDataSeries> tempMap = new HashMap<>();
		
		for (EngineDataType type : EngineDataType.values()) {
			EngineDataSeries s = getSeries(key, type);
			tempMap.put(type, s);
		}
		
		for (int i = 0; i < timeSeries.getData().size(); i++) {
			Map<String, String> row = new HashMap<>();
			
			String time = timeSeries.getData().get(i);
			long utc = Long.parseLong(time + "000");
			DateTime dt = new DateTime(utc, DateTimeZone.forOffsetHours(-8));// .withZone();
			DateTime utcTime = dt.withZoneRetainFields(DateTimeZone.UTC);
			utcTime=utcTime.plusSeconds(secondsOffset);
			for (EngineDataType type : tempMap.keySet()) {
				String value = tempMap.get(type).getData().get(i);
				row.put(type.getGarmin(), value);
			}
//			result.getData().put(utcTime, row);
			for (int x = 0; x < intervalInSeconds; x++) {
				DateTime incrementedTime = utcTime.plusSeconds(x);
				result.getData().put(incrementedTime, row);
			}
			
		}
		
		return result;
	}

	
	private EngineDataSeries getSeries(String key, EngineDataType type) {
		RestTemplate restTemplate = new RestTemplate();

		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();

		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		// Note: here we are making this converter to process any kind of response,
		// not only application/*json, which is the default behaviour
		converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
		messageConverters.add(converter);
		restTemplate.setMessageConverters(messageConverters);

		EngineDataSeries series = restTemplate.getForObject(
				"https://apps.savvyaviation.com/flights/" + key + "/json?left=" + type.getJpi(), EngineDataSeries.class);
		series.setType(type);
		return series;

	}


	private int getIntervalInSeconds(EngineDataSeries series) {
		if (series.getData().size() > 2) {
			long first = Long.parseLong(series.getData().get(0));
			long second = Long.parseLong(series.getData().get(1));
			return (int) (second - first);
		}
		return 0;
	}

	public static void main(String... strings) {

//		JpiServiceImpl imp = new JpiServiceImpl();
//
//		EngineData s = imp.getEngineData("3163931/9f8a4ab8-a7a5-471b-8baa-970c578309a7");
//
//		System.out.println("foo");

	}
}