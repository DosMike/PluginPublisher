package de.dosmike.sponge.pluginpublisher.tasks;

public class TaskRunException extends Exception {
	public TaskRunException() {
	}

	public TaskRunException(String message) {
		super(message);
	}

	public TaskRunException(String message, Throwable cause) {
		super(message, cause);
	}

	public TaskRunException(Throwable cause) {
		super(cause);
	}
}
