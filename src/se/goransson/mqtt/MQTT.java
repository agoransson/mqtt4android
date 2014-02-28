package se.goransson.mqtt;

/*
 * Copyright (C) 2012 Andreas Göransson, David Cuartielles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Main interface for creating byte representations of MQTT messages.
 * 
 * @author ksango
 * 
 */
public class MQTT implements MQTTConstants, MQTTVersion {
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @param willRetain
	 * @param willQoS
	 * @param willFlag
	 * @param cleanSession
	 * @return
	 * @throws IOException
	 * @throws MQTTException
	 */
	public static byte[] connect(String identifier, String willTopic,
			String willMessage, String username, String password)
			throws IOException, MQTTException {
		
		// PAYLOAD
		ByteArrayOutputStream payload = new ByteArrayOutputStream();

		// CLIENT IDENTIFIER
		// ClientID MUST be present and MUST be the first field in payload
		if (identifier == null )
			throw new MQTTException("Client identifier invalid");
		// ClientID MUST be UTF-8
		if (MQTTHelper.isUTF8(identifier.getBytes("UTF-8")))
			throw new MQTTException("Invalid identifier encoding");
		// ClientID MUST be length between 0 and 65535
		if (identifier.getBytes("UTF-8").length < MIN_LENGTH || identifier.getBytes("UTF-8").length > MAX_LENGTH)
			throw new MQTTException("Client identifier invalid length");

		payload.write(MQTTHelper.MSB(identifier.getBytes("UTF-8"))); // MSB
		payload.write(MQTTHelper.LSB(identifier.getBytes("UTF-8"))); // LSB
		payload.write(identifier.getBytes("UTF-8"));
		
		// WILL
		if (willTopic.length() > 0 && willMessage.length() > 0) {
			// WILL TOPIC
			// Will topic MUST be UTF-8
			if (MQTTHelper.isUTF8(willTopic.getBytes("UTF-8")))
				throw new MQTTException("Invalid will topic encoding");
			// Write Will Topic
			payload.write(MQTTHelper.MSB(willTopic.getBytes("UTF-8"))); // MSB
			payload.write(MQTTHelper.LSB(willTopic.getBytes("UTF-8"))); // LSB
			payload.write(willTopic.getBytes("UTF-8"));

			// WILL MESSAGE
			// Will message MUST
			if (MQTTHelper.isUTF8(willMessage.getBytes("UTF-8")))
				throw new MQTTException("Invalid will message encoding");
			// Write Will Topic
			payload.write(MQTTHelper.MSB(willMessage.getBytes("UTF-8"))); // MSB
			payload.write(MQTTHelper.LSB(willMessage.getBytes("UTF-8"))); // LSB
			payload.write(willMessage.getBytes("UTF-8"));
		}
		
		// USERNAME
		if (username.length() > 0) {
			// Username MUST be UTF-8
			if (MQTTHelper.isUTF8(username.getBytes("UTF-8")))
				throw new MQTTException("Invalid username encoding");
			// Write username
			payload.write(MQTTHelper.MSB(username.getBytes("UTF-8"))); // MSB
			payload.write(MQTTHelper.LSB(username.getBytes("UTF-8"))); // LSB
			payload.write(username.getBytes("UTF-8"));
		}
		
		// PASSWORD
		if (password.length() > 0) {
			// Password MUST be UTF-8
			if (MQTTHelper.isUTF8(password.getBytes("UTF-8")))
				throw new MQTTException("Invalid password encoding");
			// Password contains 0 to 65535 bytes
			if (password.getBytes("UTF-8").length < MIN_LENGTH || password.getBytes("UTF-8").length > MAX_LENGTH)
				throw new MQTTException("Password invalid length");
			// Write password
			payload.write(MQTTHelper.MSB(password.getBytes("UTF-8"))); // MSB
			payload.write(MQTTHelper.LSB(password.getBytes("UTF-8"))); // LSB
			payload.write(password.getBytes("UTF-8"));
		}

		// Variable header
		byte[] variableHeader = encodeVariableHeader(CONNECT);

		// Fixed header
		byte[] fixedHeader = encodeFixedHeader(CONNECT, payload.toByteArray(),
				variableHeader);

		return encode(fixedHeader, variableHeader, payload.toByteArray());
	}

	/**
	 * Assembly of all MQTT package parts.
	 * 
	 * @param fixHead
	 *            Fixed Header
	 * @param varHead
	 *            Variable Header
	 * @param payload
	 *            Payload
	 * @return Assembled MQTT package
	 * @throws IOException
	 *             If package assembly failed
	 */
	protected static byte[] encode(byte[] fixHead, byte[] varHead,
			byte[] payload) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		out.write(fixHead);
		out.write(varHead);
		out.write(payload);

		return out.toByteArray();
	}

	/**
	 * Low level compilation of an MQTT package.
	 * 
	 * @param type
	 *            Message type, can be {@link #CONNACK}, {@link #PUBLISH},
	 *            {@link #PUBACK}, {@link #PUBREC}, {@link #PUBREL},
	 *            {@link #PUBCOMP}, {@link #SUBSCRIBE}, {@link #SUBACK},
	 *            {@link #UNSUBSCRIBE}, {@link #UNSUBACK}, {@link #PINGREQ},
	 *            {@link #PINGRESP} or {@link #DISCONNECT}
	 * @param payload
	 *            The message payload, varies depending on message type.
	 * @param variableHeader
	 *            The message variable header
	 * @return The MQTT message as a byte array.
	 * @throws IOException
	 */
	private static byte[] encodeFixedHeader(int type, byte[] payload,
			byte[] variableHeader) throws IOException {
		// Output
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		// Type
		out.write((byte) (type << 4));

		// Flags
		switch (type) {
		case CONNECT:
			break;
		}

		// Remaining length
		int length = variableHeader.length + payload.length;
		do {
			byte digit = (byte) (length % 128);
			length /= 128;
			if (length > 0)
				digit = (byte) (digit | 0x80);
			out.write(digit);
		} while (length > 0);

		return out.toByteArray();
	}

	/**
	 * Low level assembly of variable header
	 * 
	 * @param type
	 * @param params
	 *            The parameters for the Variable Header.
	 * @return
	 * @throws IOException
	 */
	private static byte[] encodeVariableHeader(int type, String... params)
			throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		// Protocol Name (Used by all messages)
		out.write((NAME_311.getBytes("UTF-8").length >> 8) & 0xFF); // MSB
		out.write(NAME_311.getBytes("UTF-8").length & 0xFF); // LSB
		out.write(NAME_311.getBytes("UTF-8"));
		
		// Protocol Level (Used by all messages)
		out.write((byte) VERSION_311);

		// Message specific settings
		switch (type) {
		case CONNECT:

			

			// Connect Flags
			String username = params[0];
			out.write((byte) ((username.length() > 0 ? 1 : 0) << 8)
					| ((username.length() > 0 ? 1 : 0) << 7));
		}

		return out.toByteArray();
	}

	protected static MQTTMessage decode(byte[] message) {
		int i = 0;
		MQTTMessage mqtt = new MQTTMessage();
		mqtt.type = (message[i] >> 4) & 0x0F;
		mqtt.DUP = ((message[i] >> 3) & 0x01) == 0 ? false : true;
		mqtt.QoS = (message[i] >> 1) & 0x03;
		mqtt.retain = (message[i++] & 0x01) == 0 ? false : true;

		int multiplier = 1;
		int len = 0;
		byte digit = 0;
		int headerOffset = 1;
		do {
			headerOffset++;

			digit = message[i++];
			len += (digit & 127) * multiplier;
			multiplier *= 128;
		} while ((digit & 128) != 0);
		mqtt.remainingLength = len;

		switch (mqtt.type) {
		case CONNECT:
			// Not needed...
			break;

		case CONNACK:
			// Reserved byte - not used
			byte reserved = message[i++];
			mqtt.variableHeader.put("return_code", message[i++]);

			break;

		case PUBLISH:
			int topic_name_len = (message[i++] * 256 + message[i++]);
			String topic_name = new String(message, i, topic_name_len);
			mqtt.variableHeader.put("topic_name", topic_name);
			i += topic_name_len;

			if (mqtt.QoS > AT_MOST_ONCE) {
				int message_id = (message[i++] << 8 & 0xFF00 | message[i++] & 0xFF);
				mqtt.variableHeader.put("message_id",
						Integer.toString(message_id));
			}
			break;

		case SUBSCRIBE:
			// Not needed...
			break;

		case PINGREQ:
			// Not needed...
			break;
		}

		ByteArrayOutputStream payload = new ByteArrayOutputStream();
		for (int b = i; b < headerOffset + mqtt.remainingLength; b++) {
			payload.write(message[b]);
		}

		mqtt.payload = payload.toByteArray();

		return mqtt;
	}
}
