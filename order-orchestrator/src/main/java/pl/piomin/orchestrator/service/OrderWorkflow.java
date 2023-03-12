package pl.piomin.orchestrator.service;

import java.util.Map;

public class OrderWorkflow implements Workflow {

    private final Map<Integer, WorkflowStep> stepMap;

    public OrderWorkflow(Map<Integer, WorkflowStep> stepMap) {
        this.stepMap = stepMap;
    }

    @Override
    public Map<Integer, WorkflowStep> getSteps() {
        return this.stepMap;
    }
}

