package fr.msaidara.batch.step;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;

import fr.msaidara.batch.bean.Constant;
import fr.msaidara.batch.bean.Filecsv;
import fr.msaidara.batch.bean.Flux;

public class ReadFileCSV implements Tasklet{
	
	private static Log log = LogFactory.getLog(ReadFileCSV.class);
	private Resource directoryIn;
	private Filecsv fileCsv;
	//private String fileHeader;
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
			throws Exception {
		
		JobExecution jobExecution = chunkContext.getStepContext().getStepExecution().getJobExecution();            
		ExecutionContext jobContext = jobExecution.getExecutionContext();
		
		log.info(Constant.STEP_READ_FILE);
		int nbDataFile = 0;
		File files= directoryIn.getFile();
		File[] listFiles = files.listFiles();
		List<Flux> DATA = new ArrayList <Flux>();
		List<String> lineError = new ArrayList<String>();
		
		if(null != listFiles){
			log.info("There are  "+listFiles.length+" file CSV for process");
			for (int compt = 0; compt < listFiles.length ; compt ++){
				setFileCsv(listFiles[compt]);
				if(fileCsv.getErrorLines() != null && fileCsv.getErrorLines().size()> 0){
					
					for(String error : fileCsv.getErrorLines())
						lineError.add(error);
				}
				List<Map<String, String>> listeLine = fileCsv.getMappedData();
				if(listeLine != null){
					nbDataFile = nbDataFile + fileCsv.getTotalLine();
					log.info("There are " +listeLine.size()+" line in file : "+listFiles[compt].getName());
				
					for (Map<String, String> xmldata : listeLine) {
						DATA.add(new Flux(xmldata,this.fileCsv.getDataHeader(),listFiles[compt].getName()));
					}
				}
			}
			
			if(lineError != null && lineError.size()> 0){
				jobContext.put(Constant.ERROR_DATA_LINE,lineError);
			}
		
			if(DATA != null){
				jobContext.put(Constant.DATA_XML_TO_CREATE, DATA);
				jobContext.put(Constant.DATA_TOTAL_FILE,nbDataFile);
			}
		
		
		}else{
			jobContext.put(Constant.STEP, Constant.STEP_READ_FILE);
			jobContext.put(Constant.ERROR,"No File Found");
			contribution.setExitStatus(ExitStatus.FAILED);
		}
	
	return RepeatStatus.FINISHED;
	}
	
	public void setFileCsv(File file) throws IOException {
		this.fileCsv = new Filecsv(file);
    }
	public void setDirectoryIn(Resource directoryIn) {
		this.directoryIn = directoryIn;
	}
	

}
