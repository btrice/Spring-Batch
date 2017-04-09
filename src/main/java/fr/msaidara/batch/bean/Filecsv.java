package fr.msaidara.batch.bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Filecsv {
    private File csvfile;
    private String[] dataPerline;
    private String csvSeparator;
    private List<String> csvlines;
    private List<String> errorlines;
    private List<String[] > data;
	public final static char CSV_SEPARATOR = ';';
    private List<Map<String, String>> mappedCSVData;
    
    private static Log log = LogFactory.getLog(Filecsv.class);
    
    public Filecsv(File file) throws IOException{
        this.csvfile = file;
        this.csvSeparator = new Character(';').toString();
        this.errorlines = new ArrayList<String>();
        processFile();
    }
    
    /**
     * 
     * @param fileHeader
     * @throws IOException
     */
    public void processFile() throws IOException {
    	
    	csvlines = readFile(csvfile);
    	data = new ArrayList<String[] >(csvlines.size());
        boolean firstLine = true;
        for (String line : csvlines) {
        	line = line.trim();
        	if (line.length() == 0) { //
                continue;
            }
        	if(firstLine) { // First line should be headerLine
        		this.dataPerline = line.split(csvSeparator); 
        		firstLine = false;
        	} else {
        	
	        	String[] dataLine = line.split(csvSeparator);
	        	if(dataLine.length < dataPerline.length){
	        		log.info("Incorrete line  :");
	            	log.info(line);
	            	errorlines.add(line);
	        	} else {
	                data.add(dataLine);
	            }
        	}
        	
        }
        mapData();
    	
    }
    /**
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static List<String> readFile(File file) throws IOException {

        List<String> result = new ArrayList<String>();

        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "Cp1252"));

        for (String line = br.readLine(); line != null; line = br.readLine()) {
            result.add(line);
        }

        br.close();
        fr.close();

        return result;
    }
    
    private void mapData(){
    	mappedCSVData = new ArrayList<Map<String, String>>(data.size());
        for (String[] oneData : data) {
            final Map<String, String> map = new HashMap<String, String>();
            for (int i = 0; i < dataPerline.length; i++) {
                final String key = dataPerline[i];
                final String value = oneData[i];
                             
                map.put(key, value);
            }
            if(map != null)
            	mappedCSVData.add(map);
        }
    }
    
    public List<Map<String, String>> getMappedData() {
        return mappedCSVData;
    }
    
    public List<String[] > getData(){
    	return data;
    }
    
    public String[] getDataHeader() {
        return dataPerline;
    }
    
    public List<String> getErrorLines() {
        return errorlines;
    }
    
    public int getTotalLine(){
    	//remove header line
    	return csvlines.size() - 1;
    }
}
