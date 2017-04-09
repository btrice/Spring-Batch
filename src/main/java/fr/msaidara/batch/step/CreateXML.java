package fr.msaidara.batch.step;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

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
import fr.msaidara.batch.bean.Flux;

public class CreateXML implements Tasklet{
	
	private static Log log = LogFactory.getLog(CreateXML.class);
	private Resource directoryOut;
	private Resource directoryIn;

	@SuppressWarnings("unchecked")
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
			throws Exception {

		log.info(Constant.STEP_CREATE_XML_FILE);
		JobExecution jobExecution = chunkContext.getStepContext().getStepExecution().getJobExecution();            
		ExecutionContext jobContext = jobExecution.getExecutionContext(); 
		
		List<Flux> data  = null;
		try {
			data = (List<Flux>) jobContext.get(Constant.DATA_XML_TO_CREATE);
		} catch(Exception ex){
			data = null;
			log.error("ERROR DATA_XML_TO_CREATE :" + ex.getMessage());
		}
		int nb = 0;
		
		try{
			nb = jobContext.getInt(Constant.DATA_TOTAL_FILE);
		} catch(Exception ex){
			nb = 0;
			log.error("ERROR DATA_TOTAL_FILE -- :" + ex.getMessage());
		}
		
		
		
		String dirOut = directoryOut.getFile().getPath();
		List<String> dataError = new ArrayList<String>();
		int nbxml = generateXML(data,dirOut,dataError);
		if(dataError != null && dataError.size()> 0)
			jobContext.put(Constant.ERROR_DATA, dataError);
		jobContext.put(Constant.DATA_TOTAL_XML, nbxml);
		
		// if every flux is generated, we delete CSV file
		if(nb == nbxml){
			File files= directoryIn.getFile();
			File[] listFiles = files.listFiles();
			if(null != listFiles){
				for (int compt = 0; compt < listFiles.length ; compt ++){
					if(deleteFile(listFiles[compt].getPath())){
						log.info("file " + listFiles[compt].getPath() + " deleted.");
					}					
				}
			}
		} else {
			jobContext.put(Constant.STEP, Constant.STEP_CREATE_XML_FILE);
			jobContext.put(Constant.ERROR,"Error while creating xml data nb : "+ nb +" created : "+ nbxml);
			contribution.setExitStatus(ExitStatus.FAILED);
		}
		
		
		return RepeatStatus.FINISHED;
	}
	
	public int generateXML(List<Flux> _data, String dest,List<String> _dataError){
		int id = 0;
		int nbxml = 0;
		try{
			for(Flux _flux: _data){
				 String filename = _flux.getRootFileName()+"_"+System.currentTimeMillis() +"_" +id+".XML";
				 File file = new File(dest+"\\"+filename);
				 
				 if(!file.exists()){
					 try{
							file.createNewFile();
							final FileWriter writer = new FileWriter(file);
							try{
								writer.write(_flux.toXml());
								nbxml ++;
							}finally{
								writer.close();
							}
						}catch(Exception e){
							log.info("_____________Flux not created_____________");
							log.info(_flux.toXml());
							_dataError.add(_flux.toXml());
						}
				 }
				 id++;
			}
		} catch (Exception ex){
			log.error(" generateXML :" + ex.getMessage());
		}
		return nbxml;
	}
	
	private boolean deleteFile(String filepath){
		File f = null;
		try {
			f= new File(filepath);
		} catch (Exception x) {
			log.info("Error " + filepath);
			log.info(x.getMessage());
		}
		if(f != null){
			if(f.delete()){
				log.info("Deleted file: " + filepath);
				return true;
			}else{
				log.info("Error while deleting file "+ filepath);
				return false;
			}
		}else{
			log.info("Error while deleting file "+ filepath);
			return false;
		}
	}
	
	public void setDirectoryOut(Resource directoryOut) {
		this.directoryOut = directoryOut;
	}
	
	public void setDirectoryIn(Resource directoryIn) {
		this.directoryIn = directoryIn;
	}

}
