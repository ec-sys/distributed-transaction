package pl.piomin.orchestrator.service;

import java.util.Map;

public interface Workflow {

    Map<Integer, WorkflowStep> getSteps();

}
