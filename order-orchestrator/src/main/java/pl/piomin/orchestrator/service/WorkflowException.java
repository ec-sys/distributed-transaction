package pl.piomin.orchestrator.service;

public class WorkflowException extends RuntimeException {

    public WorkflowException(String message) {
        super(message);
    }
}
