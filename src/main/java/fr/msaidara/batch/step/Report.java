package fr.msaidara.batch.step;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;

import fr.msaidara.batch.bean.Constant;

public class Report implements Tasklet{
	private static Log log = LogFactory.getLog(CreateXML.class);
	private Resource directoryReport;
	@SuppressWarnings("unchecked")
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
			throws Exception {
		
		log.info(Constant.STEP_CREATE_RAPPORT);
		JobExecution jobExecution = chunkContext.getStepContext().getStepExecution().getJobExecution();            
		ExecutionContext jobContext = jobExecution.getExecutionContext(); 
		
		String dirRapport = directoryReport.getFile().getPath();
		String filename = dirRapport +"\\"+"Rapport_"+System.currentTimeMillis() +".log";
		
		String step = getStringFromContext(jobContext, Constant.STEP);
		String error = getStringFromContext(jobContext, Constant.ERROR);
		int totalfile = getIntFromContext(jobContext, Constant.DATA_TOTAL_FILE);
		int totalxml = getIntFromContext(jobContext, Constant.DATA_TOTAL_XML);
		
		List<String> lineError;
		try {
			lineError = (List<String>) jobContext.get(Constant.ERROR_DATA_LINE);
		} catch(Exception ex){
			lineError = null;
			log.error("ERROR_DATA_LINE :" + ex.getMessage());
		}
		
		List<String> lineData;
		try {
			lineData = (List<String>) jobContext.get(Constant.ERROR_DATA);
		} catch(Exception ex){
			lineData = null;
			log.error("ERROR_DATA :" + ex.getMessage());
		}
		
		createRapport(totalfile, totalxml, step, error, lineError, lineData, filename);
		
		
		return null;
	}
	
	public void createRapport(int totalfile, int totalxml, String step, String error, List<String> lineError , List<String> lineData, String filename){
		boolean isError = false;
		File file = new File(filename);
		 if(!file.exists()){
			 try{
					file.createNewFile();
					final FileWriter writer = new FileWriter(file);
					try{
						writer.write("________BATCH-EXECUTION-REPORT________\n");
						writer.write("Total Line in CSV File :"+ totalfile + "\n");
						writer.write("Total XML :"+ totalxml + "\n");
						if(step != null)
							writer.write(step);
						if(error != null)  {
							writer.write(step);
							isError = true;
						}
						if(lineError != null){
							 for(String lerror: lineError){
								 isError = true;
								 writer.write("line error in CSV file :");
								 writer.write(lerror + "\n");
							 }
						}
						
						if(lineData != null){
							 for(String derror: lineData){
								 isError = true;
								 writer.write("Data error xml :");
								 writer.write(derror + "\n");
								 
							 }
						}
						if(!isError){
							writer.write("__________________Execution successfull OK_____________________\n");
						}
						
						writer.write("_______________________END_______________________\n");
						
					}finally{
						writer.close();
					}
				}catch(Exception e){
					log.info("_____________Report not created_____________\n");
				}
		 }
	}
	
	public int  getIntFromContext(ExecutionContext ctx, String key){
		int tmp = 0;
		try {
			tmp = ctx.getInt(key);
		}  catch(Exception ex){
			return 0;
		}
		return tmp;
	}
	public String  getStringFromContext(ExecutionContext ctx, String key){
		String tmp = null;
		try {
			tmp = ctx.getString(key);
		}  catch(Exception ex){
			return null;
		}
		return tmp;
	}
	
	public void setDirectoryReport(Resource directoryReport) {
		this.directoryReport = directoryReport;
	}

}
