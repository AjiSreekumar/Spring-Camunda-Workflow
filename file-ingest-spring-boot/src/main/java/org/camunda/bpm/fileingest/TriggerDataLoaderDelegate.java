package org.camunda.bpm.fileingest;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("triggerDataLoader")
public class TriggerDataLoaderDelegate implements JavaDelegate {
	
	private static final Logger logger = LoggerFactory.getLogger(TriggerDataLoaderDelegate.class);

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		logger.debug("Starting DataLoader : "+execution.getVariable("downloaded"));
		
		//Invoke the method to trigger dataloader-Add code here

	}

}
