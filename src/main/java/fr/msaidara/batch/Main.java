package fr.msaidara.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Log4jConfigurer;



public class Main {
	
	
	/**
	 * The main entry point parses the parameters to pass to the job then launches the main job.
	 * @param args List of key/value pairs formatted as "key=value"
	 */
	public static void main(String[] args) {
		System.setProperty("batch.arg.dlog", "log_batch.log"); // default value
		List<String> parsingErrors = commandLineParser(args);

		/** Check environment settings. */
		checkProperties();

		/** Starting log engine using command arguments and environment settings. */
		try {
			String log4jConfigLocation = System.getProperty("log4jConfigLocation");
			long log4jRefreshInterval = -1;
			
			try {
				log4jRefreshInterval = Long.parseLong(System.getProperty("log4jRefreshInterval"));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

			if (log4jRefreshInterval > 0) {
				// Initialize with refresh interval, i.e. with log4j's watchdog thread, checking the file in the background.
				Log4jConfigurer.initLogging(log4jConfigLocation, log4jRefreshInterval);
			} else {
				// Initialize without refresh check, i.e. without log4j's watchdog thread.
				Log4jConfigurer.initLogging(log4jConfigLocation);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1); // unable to log is fatal
		}
		Logger log = Logger.getLogger(Main.class);

		// now that the log is up, write down parsing errors
		if (parsingErrors!= null && parsingErrors.size() > 0) {
			for (String error : parsingErrors) {
				log.fatal(error);
			}
			System.exit(-2);
		}

		/** Prepare the job's parameters. */
		JobParametersBuilder jpb = new JobParametersBuilder();
		for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
			String key = (String)entry.getKey();
			if (key.startsWith("batch.arg.")) {
				String parameter = key.replace("batch.arg.", "");
				String value = (String)entry.getValue();
				jpb.addString(parameter, value);
				log.info("parametre: "+ parameter +" = "+value);
				// enumerate parameters for debug purpose
				if (log.isDebugEnabled()) {
					log.debug("Job Parameter: " + parameter + "=" + value);
				}
			}
		}
		JobParameters jobParameters = jpb.toJobParameters();

		/** Create a new Spring context and get the mainJob */
		ApplicationContext ctx = applicationContextFactory();
		
		 Job job = ctx.getBean("mainJob", Job.class);
	     JobLauncher jobLauncher = ctx.getBean("jobLauncher", JobLauncher.class);

		/** Launch the job! */
		try {
			JobExecution jobExecution = jobLauncher.run(job, jobParameters);
			// Simple exit codes logic: not-completed=1, else=0
			// Customize this code to adapt to more complex scenario.
			if (jobExecution.getStatus() != BatchStatus.COMPLETED) {
				log.error("Batch does not complete successfuly: status=" + jobExecution.getStatus().toString());
				System.exit(1);
			}
		} catch (JobParametersInvalidException e) {
			log.fatal(e.getMessage());
			System.exit(-1);
		} catch (JobExecutionAlreadyRunningException e) {
			log.fatal(e.getMessage());
			System.exit(-2);
		} catch (JobInstanceAlreadyCompleteException e) {
			log.fatal(e.getMessage());
			System.exit(-3);
		} catch (JobRestartException e) {
			log.fatal(e.getMessage());
			System.exit(-4);
		} catch (Throwable t) {
			log.fatal(t.getMessage(), t);
			System.exit(-10);
		}
	}

	/**
	 * Checks that the standard properties are defined.
	 * Missing ones are created with default values.
	 */
	private static void checkProperties() {
		checkProperty("log4jConfigLocation", "classpath:log4j.properties");
		checkProperty("propertyPlaceholderLocation", "classpath:batch.properties");
		checkProperty("log4jRefreshInterval", "-1");
	}

	private static void checkProperty(String propertyName, String defaultValue) {
		String propertyValue = System.getProperty(propertyName);
		if (propertyValue == null || propertyValue.isEmpty()) {
			System.setProperty(propertyName, defaultValue);
		}
	}

	/**
	 * Create a new Spring context instance based on 'batchContext*.xml' configuration files.
	 * @return A new Spring context.
	 */
	private static ApplicationContext applicationContextFactory() {
		return new ClassPathXmlApplicationContext("classpath:launch-context.xml");
	}
	
	/**
	 * Parse args parameterss
	 * @param args
	 * @return A list of message error occurred during args parsing 
	 */
	private static List<String>  commandLineParser(String[] args){
		List<String> cmderror = new ArrayList<String>();
		for(int i=0; i< args.length; i++){
			try {
				String [] cmd = args[i].split("=");
				System.setProperty("batch.arg."+cmd[0], cmd[1]);
				
			} catch (Exception ex){
				cmderror.add(args[i]+" : "+ ex.getMessage());
			}
		}
		
		return cmderror;
	}

}
