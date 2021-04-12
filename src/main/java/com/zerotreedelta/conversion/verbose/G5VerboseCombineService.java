package com.zerotreedelta.conversion.verbose;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.opencsv.CSVReaderHeaderAware;

public class G5VerboseCombineService {
	
	private Set<String> masterKeyset = new HashSet<String>();
	
	public String combine(File zip) throws ZipException, IOException {
		Map<Long, Map<String, String>> combined = new HashMap<Long, Map<String,String>>();
		
		ZipFile zipFile = new ZipFile(zip);

	    Enumeration<? extends ZipEntry> entries = zipFile.entries();

	    while(entries.hasMoreElements()){
	        ZipEntry entry = entries.nextElement();
	        if(!entry.isDirectory() 
	        		&& entry.getName().contains(".CSV") 
	        		&& !(entry.getName().startsWith(".") || entry.getName().startsWith("_") || entry.getName().contains("~")  )) {
	        	InputStream stream = zipFile.getInputStream(entry);
	        	parseFile(entry.getName().replaceAll(".CSV", ""), stream, combined);
	        	stream.close();
	        }
	    }
	    
	    zipFile.close();
	    
	    StringBuilder b = new StringBuilder();
	    Object[] sortedHeaders =  masterKeyset.toArray();
	    Arrays.sort(sortedHeaders);
	    
	    
	    Object[] times = combined.keySet().toArray();
	    Arrays.sort(times);
	    for(Object sec : times) {
	    	Map<String, String> vals = combined.get((Long)sec);
	    
	    	if(b.length()==0) {
	    	 b.append("Tick(sec),");
	    	 for(Object k : sortedHeaders) {
	    		 b.append(k+",");
	    	 }
	    	 b.append("\n");
	    	}
	    	b.append(sec+",");
	    	for(Object k : sortedHeaders) {
	    		String val = vals.get(k);
	    		if(val!=null && !"null".equals(val)) {
	    		 b.append(val+",");
	    		} else {
	    			b.append(",");
	    		}
	    	 }
	    	b.append("\n");
	    }
		return b.toString();
		
	}
	
	private void parseFile(String name, InputStream stream, Map<Long, Map<String, String>> combined) {
		System.out.println(name);
		try {
			
			InputStreamReader rdr = new InputStreamReader(stream);
			BufferedReader br = new BufferedReader(rdr);
			br.readLine();
			br.readLine();
			br.readLine();
			br.readLine();
			br.readLine();
			CSVReaderHeaderAware csv = new CSVReaderHeaderAware(br);
	
			while (br.ready()) {
				Map<String, String> values = csv.readMap();
				String ms = values.get("Tick(ms)");
				Long second = Long.parseLong(ms)/1000;
				
				Map<String, String> row = combined.get(second);
				if(row==null) {
					row = new HashMap<String, String>();
					combined.put(second, row);
				//	System.out.println("new row"+second);
				} else {
					//System.out.println("reused row"+second);
				}
				for(String k : values.keySet()){
					if(!"Tick(ms)".equals(k)) {
						row.put(name+"_"+k, values.get(k));
						masterKeyset.add(name+"_"+k);
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public static void main(String...strings ) throws ZipException, IOException {
		File f = new File("/home/dodgemich/Downloads/double.zip");
		
		G5VerboseCombineService s = new G5VerboseCombineService();
		System.out.println(s.combine(f));
	}
}
