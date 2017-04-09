package fr.msaidara.batch.step;

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

public class CheckResources implements Tasklet {
    private static Log log = LogFactory.getLog(CheckResources.class);
	/* Ouput directory */
	private Resource directoryOut;
	/* Input directory */
	private Resource directoryIn;
	/* Rapport directory */
	private Resource directoryReport;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
			throws Exception {
		
		//recuperation du contexte
				JobExecution jobExecution = chunkContext.getStepContext().getStepExecution().getJobExecution();            
				ExecutionContext jobContext = jobExecution.getExecutionContext();
				
				log.info(Constant.STEP_CHECK_RESOURCES);
				
				long heureDebutTrt = System.currentTimeMillis();
				jobContext.put("heureDebutTrt", heureDebutTrt);
				//Veriication des differents parametres
				if (directoryIn == null || !directoryIn.getFile().isDirectory()) {
					jobContext.put(Constant.STEP, Constant.STEP_CHECK_RESOURCES);
					jobContext.put(Constant.ERROR, "Directory IN not found");
					log.info("Directory IN not available");
					contribution.setExitStatus(ExitStatus.FAILED);
				}else{
					log.info("Directory IN found OK");
				}
				
				if (directoryOut == null || !directoryOut.getFile().isDirectory()) {
					jobContext.put(Constant.STEP, Constant.STEP_CHECK_RESOURCES);
					jobContext.put(Constant.ERROR, "Directory Out not found");
					log.info("Directory Out not found");
					contribution.setExitStatus(ExitStatus.FAILED);
				}else{
					log.info("Directory Out found OK");
				}
				
				if (directoryReport == null || !directoryReport.getFile().isDirectory()) {
					jobContext.put(Constant.STEP, Constant.STEP_CHECK_RESOURCES);
					jobContext.put(Constant.ERROR, "Directory Rapport not found");
					log.info("Directory Rapport not found");
					contribution.setExitStatus(ExitStatus.FAILED);
				}else{
					log.info("Directory Rapport found OK");
				}
				
				jobContext.put(Constant.DATA_XML_TO_CREATE, 0);
				jobContext.put(Constant.DATA_TOTAL_FILE,0);
				jobContext.put(Constant.DATA_TOTAL_XML,0);
						
				return RepeatStatus.FINISHED;
			}

			
			public void setDirectoryOut(Resource directoryOut) {
				this.directoryOut = directoryOut;
			}
			
			public void setDirectoryIn(Resource directoryIn) {
				this.directoryIn = directoryIn;
			}
			
			public void setDirectoryReport(Resource directoryReport) {
				this.directoryReport = directoryReport;
			}
			

}
