package se.goransson.mqtt;

public class MQTTException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6482564182049683857L;

	public MQTTException() {
		super("MQTT Exception");
	}

	public MQTTException(String message) {
		super(message);
	}

	public MQTTException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
