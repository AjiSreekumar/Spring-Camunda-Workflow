package org.camunda.bpm.fileingest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessDeployer {

	private File file;

	private static final Logger logger = LoggerFactory.getLogger(ProcessDeployer.class);

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private RepositoryService repositoryService;

	/**
	 * @param modelInstance
	 * @param processEngine
	 */
	public void deployModel(BpmnModelInstance modelInstance) {

		createBpmnFile(modelInstance);
		String fileName = file.getName();

		try {

			DeploymentWithDefinitions depDef = repositoryService.createDeployment()
					.addInputStream(fileName, new FileInputStream(file)).name(fileName).deployWithResult();
			logger.debug("Deployment ID :" + depDef.getId());
		} catch (FileNotFoundException exp) {
			logger.debug(exp.getMessage());
		}

	}

	/**
	 * @param               <T>
	 * @param processEngine
	 */
	public Collection<Process> runModel(BpmnModelInstance modelInstance) {

		Definitions definitions = modelInstance.getDefinitions();
		Collection<Process> processList = null;
		if (null != definitions) {

			processList = definitions.getChildElementsByType(Process.class);

			startProcessInstance(processList);

		}
		return processList;

	}

	/**
	 * @param processList
	 */
	public void startProcessInstance(Collection<Process> processList) {
		
		if (null != processList) {
			for (Process process : processList) {
				String processKey = process.getId();
				logger.debug("Starting Process Instance with processKey :" + processKey);
				runtimeService.startProcessInstanceByKey(processKey);
			}
		}
	}

	/**
	 * @param modelInstance
	 */
	private void createBpmnFile(BpmnModelInstance modelInstance) {

		File directory = new File("./src/main/resources");

		for (File deleteFile : directory.listFiles()) {
			if (deleteFile.getName().startsWith("fileIngest-")) {
				deleteFile.delete();
				logger.debug("Deleted files with name starting with fileIngest ");
			}
		}
		try {

			file = File.createTempFile("fileIngest-", ".bpmn", directory);
			logger.debug("Filepath :" + file.getPath());
			Bpmn.writeModelToFile(file, modelInstance);

		} catch (IOException exp) {
			logger.debug("Bpmn File creation Exception " + exp.getMessage());
		}

	}

}
