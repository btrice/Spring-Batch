package fr.msaidara.batch;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import fr.msaidara.batch.bean.Constant;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/launch-context.xml")
public class JobTest {
	private final Log log = LogFactory.getLog(getClass());
	
	@Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
	private static final String dIN = "src/test/resources/IN";
	private static final String dOUT = "src/test/resources/OUT";
	private static final String dREPORT = "src/test/resources/REPORT";
	private static final String dLog = "src/test/resources/Log/batch.log";
	private ExecutionContext context;
	private JobExecution jobExecution;
	public static ExecutionContext saveContext;
	
	@BeforeClass
	public static void setSystemProps(){
		System.setProperty("batch.arg.dIN", dIN);
		System.setProperty("batch.arg.dOUT", dOUT);
		System.setProperty("batch.arg.dREPORT", dREPORT);
		System.setProperty("batch.arg.dlog", dLog);
		PropertyConfigurator.configure("src/test/resources/log4j.properties");
		/*Copy file because it will be delete at the end of batch process*/
		copyFile("src/test/resources/Data/cereal.csv","src/test/resources/IN/cereal.csv");
		/*Delete output/report/log file before run test*/
		deleteFile(dOUT);
		deleteFile(dREPORT);

	}
	
	
	@Test
	public void testSTepCheckResources() throws Exception {
		context = new ExecutionContext();
		jobExecution = jobLauncherTestUtils.launchStep("CheckResource",context);
		context = jobExecution.getExecutionContext();
		Assert.assertEquals(0,context.getInt(Constant.DATA_XML_TO_CREATE));
		Assert.assertEquals(0,context.getInt(Constant.DATA_TOTAL_FILE));
		Assert.assertEquals(0,context.getInt(Constant.DATA_TOTAL_XML));
		Assert.assertNull(context.get(Constant.ERROR));
		Assert.assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
	}
	
	@Test
	public void testReadCSVStep() throws Exception {
		context = new ExecutionContext();
		//log.info("before testReadCSVStep :"+context.toString());
	    jobExecution = jobLauncherTestUtils.launchStep("ReadCSV",context);
		context = jobExecution.getExecutionContext();
		//log.info("after testReadCSVStep :"+context.toString());
		Assert.assertNotNull(context.get(Constant.DATA_XML_TO_CREATE));
		Assert.assertEquals(77,context.getInt(Constant.DATA_TOTAL_FILE));
		Assert.assertNull(context.get(Constant.DATA_TOTAL_XML));
		Assert.assertNull(context.get(Constant.ERROR));
		Assert.assertNull(context.get(Constant.ERROR_DATA_LINE));
		Assert.assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		saveContext = context;
	}
	
	@Test
	public void testCreateXMLStep() throws Exception {
		//log.info("before testCreateXMLStep :"+saveContext.toString());
		jobExecution = jobLauncherTestUtils.launchStep("CreateXML",saveContext);
		context = jobExecution.getExecutionContext();
		Assert.assertEquals(77,context.getInt(Constant.DATA_TOTAL_XML));
		Assert.assertNull(context.get(Constant.ERROR));
		Assert.assertNull(context.get(Constant.ERROR_DATA));
		Assert.assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		saveContext = context;
	}
	
	@Test
	public void testReportStep() throws Exception {
		jobExecution = jobLauncherTestUtils.launchStep("Report",saveContext);
		context = jobExecution.getExecutionContext();
		File files = new FileSystemResource(dREPORT).getFile();
		Assert.assertNotNull(files.listFiles());
		
	}
	
	
	public static void copyFile(String from, String to){
		InputStream inStream = null;
		OutputStream outStream = null;
		
	    	try{

	    	    File _from =new File(from);
	    	    File _to =new File(to);

	    	    inStream = new FileInputStream(_from);
	    	    outStream = new FileOutputStream(_to);

	    	    byte[] buffer = new byte[1024];

	    	    int length;
	    	    //copy the file content in bytes
	    	    while ((length = inStream.read(buffer)) > 0){

	    	    	outStream.write(buffer, 0, length);

	    	    }

	    	    inStream.close();
	    	    outStream.close();
	    	}catch(IOException e){
	    		e.printStackTrace();
	    	}
	}
	
	public static void deleteFile(String out){
		File files = new FileSystemResource(out).getFile();
		File[] listFiles = files.listFiles();
		if(null != listFiles){
			for (int compt = 0; compt < listFiles.length ; compt ++){
				new File(listFiles[compt].getPath()).delete();				
			}
		}
	}
	
}
