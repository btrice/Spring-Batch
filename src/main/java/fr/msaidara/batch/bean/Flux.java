package fr.msaidara.batch.bean;

import java.util.Map;

public class Flux {
	private Map<String, String> mapCSVData;
	private String[] header;
	private String rootFileName;
	public Flux(Map<String, String> _mapCSVData,String[] headerLine, String rootFile){
		this.mapCSVData = _mapCSVData;
		this.header = headerLine;
		String name = rootFile;
		int pos = name.lastIndexOf(".");
		if (pos > 0) {
			this.rootFileName = name.substring(0, pos);
		} else {
			this.rootFileName = "DATA";
		}
		 
	}
	public String getRootFileName(){
		return this.rootFileName;
	}
	public String toXml(){
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<"+this.rootFileName+">\n");
		if(!mapCSVData.isEmpty()){
			for (String key: header) {
				
				sb.append("<"+key+">\n");
				sb.append("<value>\n");
				sb.append(mapCSVData.get(key)+"\n");
				sb.append("</value>\n");
				sb.append("</"+key+">\n");
				
				
			}
		}
		
		sb.append("</"+this.rootFileName+">\n");
		return sb.toString();
	}

}
