package org.camunda.bpm.fileingest;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("checkFileDownloaded")
public class CheckFileStatusDelegate implements JavaDelegate {

	private static final Logger logger = LoggerFactory.getLogger(CheckFileStatusDelegate.class);
	//boolean downloaded;
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		//Added for testing. This needs to be replaced with the status of file download either through direct DB fetch or through invoking a service call which provide the status
		Random random = new Random(); //Comment off the boolean variable and add code to get download status here.
		boolean isDownloaded = random.nextBoolean();
		execution.setVariable("downloaded", isDownloaded);
		
		logger.debug("File download status is : "+ isDownloaded);
		

	}

}
