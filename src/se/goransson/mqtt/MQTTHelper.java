package se.goransson.mqtt;

import org.mozilla.universalchardet.UniversalDetector;

/**
 * Helper methods for MQTT
 * 
 * @author ksango
 * 
 */
public class MQTTHelper {
	private static final String UTF8 = "UTF-8";

	/**
	 * Detects if string is encoded with UTF-8
	 * 
	 * @param buffer
	 *            String
	 * @return true if UTF-8, false otherwise
	 */
	protected static boolean isUTF8(byte[] buffer) {
		UniversalDetector detector = new UniversalDetector(null);
		detector.handleData(buffer, 0, buffer.length);
		detector.dataEnd();

		String encoding = detector.getDetectedCharset();
		detector.reset();

		return (UTF8.equals(encoding));
	}

	/**
	 * Return the MSB of a string length
	 * 
	 * @param buffer
	 *            String
	 * @return MSB
	 */
	protected static byte MSB(byte[] buffer) {
		return (byte) ((buffer.length) >> 8 & 0xFF);
	}

	/**
	 * Return the LSB of the string length
	 * 
	 * @param buffer
	 *            String
	 * @return LSB
	 */
	protected static byte LSB(byte[] buffer) {
		return (byte) (buffer.length & 0xFF);
	}
}
