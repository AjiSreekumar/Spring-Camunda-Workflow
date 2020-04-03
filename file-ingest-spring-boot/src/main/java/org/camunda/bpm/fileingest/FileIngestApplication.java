package org.camunda.bpm.fileingest;

import java.util.Collection;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@SpringBootApplication
@EnableProcessApplication
public class FileIngestApplication implements CommandLineRunner{

	@Autowired
	private ProcessBuilder processBuilder;

	@Autowired
	private ProcessDeployer processDeployer;
	
	private Collection<Process> processList;
	
	
	
	private static final Logger logger = LoggerFactory.getLogger(FileIngestApplication.class);

	public static void main(String[] args) {

		SpringApplication.run(FileIngestApplication.class, args);
		logger.debug("Started FileIngestApplication");

	}

	@EventListener
	private void processDeploy(PostDeployEvent event) {
		logger.debug("Initial Check in APP");
	

	}

	@Override
	public void run(String... args) throws Exception {
		BpmnModelInstance modelInstance = processBuilder.buildProcessModel();
		processDeployer.deployModel(modelInstance);
		processList= processDeployer.runModel(modelInstance);
		
	}
	
	@Scheduled(fixedDelay = 1500L)
	  public void runProcess() {
		
		processDeployer.startProcessInstance(processList);
		
	}

}
