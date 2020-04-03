package org.camunda.bpm.fileingest;

import java.util.UUID;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.bpmn.instance.ConditionExpression;
import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnLabel;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import org.camunda.bpm.model.bpmn.instance.dc.Bounds;
import org.camunda.bpm.model.bpmn.instance.di.Waypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProcessBuilder {

	private BpmnModelInstance modelInstance;
	private Process process;
	private BpmnDiagram diagram;
	private BpmnPlane plane;

	private static final Logger logger = LoggerFactory.getLogger(ProcessBuilder.class);

	/**
	 * @return BpmnModelInstance
	 */
	public BpmnModelInstance buildProcessModel() {

		modelInstance = Bpmn.createEmptyModel();
		Definitions definitions = modelInstance.newInstance(Definitions.class);
		definitions.setTargetNamespace("http://camunda.org/examples");
		modelInstance.setDefinitions(definitions);

		// create the process-To be refactored
		process = modelInstance.newInstance(Process.class);
		String processId= "file-ingest"+UUID.randomUUID().toString();
		process.setId(processId);
		process.setExecutable(true);
		definitions.addChildElement(process);

		// create BPMN Diagram
		diagram = modelInstance.newInstance(BpmnDiagram.class);
		plane = modelInstance.newInstance(BpmnPlane.class);
		plane.setBpmnElement(process);
		diagram.setBpmnPlane(plane);
		definitions.addChildElement(diagram);

		// create start event, serviceTask and end event
		StartEvent startEvent = createElement(process, "start", StartEvent.class, plane, 15, 15, 50, 50, true);
		startEvent.setName("Start");

		ServiceTask serviceTask = createElement(process, "service_task_check_file_downloaded", ServiceTask.class, plane,
				150, 0, 80, 100, false);
		serviceTask.setName("Check File Downloaded");
		serviceTask.setCamundaDelegateExpression("#{checkFileDownloaded}");
		ExclusiveGateway exclusiveGateway = createElement(process, "gateway_file_dowloaded", ExclusiveGateway.class,
				plane, 350, 8, 60, 60, true);
		exclusiveGateway.setName("File downloaded ?");
		exclusiveGateway.setCamundaExclusive(true);

//		UserTask task1 = createElement(process, "test-user-task", UserTask.class, 
//				plane, 470, 0, 80, 100, false);
//		task1.setName("TestUserTask");

		ServiceTask serviceTask2 = createElement(process, "trigger_task_start_dataloader", ServiceTask.class, plane,
				470, 0, 80, 100, false);
		serviceTask2.setName("Trigger DataLoader");
		serviceTask2.setCamundaDelegateExpression("#{triggerDataLoader}");

		EndEvent endEvent = createElement(process, "end", EndEvent.class, plane, 625, 15, 50, 50, true);
		endEvent.setName("End");
		// create the connections between the elements
		createConnections(startEvent, serviceTask, exclusiveGateway, serviceTask2, endEvent);

		logger.debug(Bpmn.convertToString(modelInstance));
		Bpmn.validateModel(modelInstance);

		return modelInstance;

	}

	/**
	 * @param startEvent
	 * @param serviceTask
	 * @param exclusiveGateway
	 * @param serviceTask2
	 * @param endEvent
	 */
	private void createConnections(StartEvent startEvent, ServiceTask serviceTask, ExclusiveGateway exclusiveGateway,
			ServiceTask serviceTask2, EndEvent endEvent) {
		createSequenceFlow(process, startEvent, serviceTask, plane, 65, 40, 150, 40);
		createSequenceFlow(process, serviceTask, exclusiveGateway, plane, 250, 40, 350, 40);

		SequenceFlow seqFlow3 = createSequenceFlow(process, exclusiveGateway, serviceTask2, plane, 410, 40, 470, 40);
		seqFlow3.setName("YES");
		createConditionExpression(seqFlow3);

		SequenceFlow seqFlow4 = createSequenceFlow(process, exclusiveGateway, endEvent, plane, 380, 65, 380, 120, 650,
				120, 650, 65);
		seqFlow4.setName("NO");
		createConditionExpression(seqFlow4);

		createSequenceFlow(process, serviceTask2, endEvent, plane, 570, 40, 625, 40);
	}

	/**
	 * @param seqFlow
	 * @return
	 */
	private ConditionExpression createConditionExpression(SequenceFlow seqFlow) {
		ConditionExpression conditionExpression = modelInstance.newInstance(ConditionExpression.class);
		if (seqFlow.getName().equalsIgnoreCase("YES")) {
			conditionExpression.setTextContent("#{downloaded}");
		} else {
			conditionExpression.setTextContent("#{!downloaded}");
		}

		seqFlow.setConditionExpression(conditionExpression);
		return conditionExpression;
	}

	/**
	 * @param parentElement
	 * @param id
	 * @param elementClass
	 * @param plane
	 * @param x
	 * @param y
	 * @param heigth
	 * @param width
	 * @param withLabel
	 * @return
	 */
	protected <T extends BpmnModelElementInstance> T createElement(BpmnModelElementInstance parentElement, String id,
			Class<T> elementClass, BpmnPlane plane, double x, double y, double heigth, double width,
			boolean withLabel) {
		T element = parentElement.getModelInstance().newInstance(elementClass);
		element.setAttributeValue("id", id, true);
		parentElement.addChildElement(element);

		BpmnShape bpmnShape = modelInstance.newInstance(BpmnShape.class);
		bpmnShape.setBpmnElement((BaseElement) element);

		Bounds bounds = modelInstance.newInstance(Bounds.class);
		bounds.setX(x);
		bounds.setY(y);
		bounds.setHeight(heigth);
		bounds.setWidth(width);
		bpmnShape.setBounds(bounds);

		if (withLabel) {
			BpmnLabel bpmnLabel = modelInstance.newInstance(BpmnLabel.class);
			Bounds labelBounds = modelInstance.newInstance(Bounds.class);
			labelBounds.setX(x);
			labelBounds.setY(y + heigth);
			labelBounds.setHeight(heigth);
			labelBounds.setWidth(width);
			bpmnLabel.addChildElement(labelBounds);
			bpmnShape.addChildElement(bpmnLabel);
		}
		plane.addChildElement(bpmnShape);

		return element;
	}

	/**
	 * @param parentElement
	 * @param id
	 * @param elementClass
	 * @return
	 */
	protected <T extends BpmnModelElementInstance> T createElement(BpmnModelElementInstance parentElement, String id,
			Class<T> elementClass) {
		T element = parentElement.getModelInstance().newInstance(elementClass);
		element.setAttributeValue("id", id, true);
		parentElement.addChildElement(element);
		return element;
	}

	/**
	 * @param process
	 * @param from
	 * @param to
	 * @param plane
	 * @param waypoints
	 * @return
	 */
	private SequenceFlow createSequenceFlow(Process process, FlowNode from, FlowNode to, BpmnPlane plane,
			int... waypoints) {
		String identifier = from.getId() + "-" + to.getId();
		SequenceFlow sequenceFlow = createElement(process, identifier, SequenceFlow.class);
		process.addChildElement(sequenceFlow);
		sequenceFlow.setSource(from);
		from.getOutgoing().add(sequenceFlow);
		sequenceFlow.setTarget(to);
		to.getIncoming().add(sequenceFlow);

		BpmnEdge bpmnEdge = modelInstance.newInstance(BpmnEdge.class);
		bpmnEdge.setBpmnElement(sequenceFlow);
		for (int i = 0; i < waypoints.length / 2; i++) {
			double waypointX = waypoints[i * 2];
			double waypointY = waypoints[i * 2 + 1];
			Waypoint wp = modelInstance.newInstance(Waypoint.class);
			wp.setX(waypointX);
			wp.setY(waypointY);
			bpmnEdge.addChildElement(wp);
		}
		plane.addChildElement(bpmnEdge);

		return sequenceFlow;
	}

}
