package pl.piomin.orchestrator.service;

public interface WorkflowStep {
    WorkflowStepStatus getStatus();

    boolean process();

    boolean revert();
}
