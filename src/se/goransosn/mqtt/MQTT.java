package se.goransosn.mqtt;

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
import java.io.UnsupportedEncodingException;

/**
 * Main interface for creating byte representations of MQTT messages.
 * 
 * @author ksango
 * 
 */
public class MQTT implements MQTTConstants, MQTTVersion {

	/**
	 * 
	 * @param message_id
	 * @param topic
	 * @return
	 * @throws IOException
	 */
	public static byte[] unsubscribe(int message_id, String topic)
			throws IOException {
		return encode(UNSUBSCRIBE, false, AT_LEAST_ONCE, false,
				topic.getBytes(), Integer.toString(message_id));
	}

	/**
	 * Create the PINGREQ message, it doesn't use the fixed header parameters
	 * other than message type, is has no payload and no variable header.
	 * 
	 * @return The MQTT package.
	 * @throws IOException
	 */
	public static byte[] ping() throws IOException {
		return encode(PINGREQ, false, 0, false, new byte[0]);
	}

	/**
	 * Create a SUBSCRIBE message, it has a QoS of {@link #AT_LEAST_ONCE}.
	 * 
	 * @param message_id
	 *            The message id of the subscribe, handled by the client.
	 * @param subscribe_topic
	 *            The topic to which the client wants to subscribe.
	 * @param subscribed_qos
	 *            The wanted QoS for the subscription, can be
	 *            {@link #AT_MOST_ONCE}, {@link #AT_LEAST_ONCE}, or
	 *            {@link #EXACTLY_ONCE}.
	 * @return The MQTT package.
	 * @throws IOException
	 */
	public static byte[] subscribe(int message_id, String subscribe_topic,
			int subscribed_qos) throws IOException {
		ByteArrayOutputStream payload = new ByteArrayOutputStream();

		payload.write((byte) ((subscribe_topic.length() >> 8) & 0xFF));
		payload.write((byte) (subscribe_topic.length() & 0xFF));
		payload.write(subscribe_topic.getBytes("UTF-8"));
		payload.write(subscribed_qos);

		return encode(SUBSCRIBE, false, AT_LEAST_ONCE, false,
				payload.toByteArray(), Integer.toString(message_id));
	}

	/**
	 * Default publish method. Uses lowest level QoS, AT_MOST_ONCE.
	 * 
	 * @param topic
	 * @param message
	 * @return
	 * @throws IOException
	 */
	public static byte[] publish(String topic, byte[] message)
			throws IOException {
		return publish(topic, message, AT_MOST_ONCE);
	}

	/**
	 * Create a PUBLISH MQTT message with QoS {@link #AT_MOST_ONCE} and message
	 * ID 0.
	 * 
	 * @param topic
	 *            Which topic to subscribe to.
	 * @param message
	 *            The message to send.
	 * @param QoS
	 *            The quality of service for this message, can be
	 *            {@link #AT_MOST_ONCE}, {@link #AT_LEAST_ONCE}, or
	 *            {@link #EXACTLY_ONCE}
	 * @return The resulting MQTT package.
	 * @throws IOException
	 */
	public static byte[] publish(String topic, byte[] message, byte QoS)
			throws IOException {
		return encode(PUBLISH, false, QoS, false, message, Integer.toString(0),
				topic);
	}

	/**
	 * Create a CONNECT MQTT message.
	 * 
	 * @return The resulting MQTT package.
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] connect(String identifier) throws UnsupportedEncodingException,
			IOException {
		ByteArrayOutputStream payload = new ByteArrayOutputStream();
		payload.write(0);
		payload.write(identifier.length());
		payload.write(identifier.getBytes("UTF-8"));
		return encode(CONNECT, false, 0, false, payload.toByteArray(), "false",
				"false", "false", "false", "false");
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
	 * @param retain
	 *            Only used on publish messages. The server holds on to the last
	 *            message for each topic.
	 * @param qos
	 *            Quality of Service, can be {@link #AT_MOST_ONCE},
	 *            {@link #AT_LEAST_ONCE}, or {@link EXACTLY_ONCE}
	 * @param dup
	 *            Should be set to true when a message is being re-delivered.
	 *            Only when QoS is {@link #AT_LEAST_ONCE} or
	 *            {@link #EXACTLY_ONCE}
	 * @param payload
	 *            The message payload, varies depending on message type.
	 * @param params
	 *            The parameters for the Variable Header.
	 * @return The MQTT message as a byte array.
	 * @throws IOException
	 */
	protected static byte[] encode(int type, boolean retain, int qos,
			boolean dup, byte[] payload, String... params) throws IOException {
		ByteArrayOutputStream mqtt = new ByteArrayOutputStream();

		// Fixed Header properties
		mqtt.write((byte) ((retain ? 1 : 0) | qos << 1 | (dup ? 1 : 0) << 3 | type << 4));

		ByteArrayOutputStream variableHeader = new ByteArrayOutputStream();

		switch (type) {
		case CONNECT:
			// Variable Header, read the params.
			boolean username = Boolean.parseBoolean(params[0]);
			boolean password = Boolean.parseBoolean(params[1]);
			boolean will = Boolean.parseBoolean(params[2]);
			boolean will_retain = Boolean.parseBoolean(params[3]);
			boolean cleansession = Boolean.parseBoolean(params[4]);

			variableHeader.write(0x00); // LSB
			variableHeader.write(NAME.getBytes("UTF-8").length); // MSB
			variableHeader.write(NAME.getBytes("UTF-8")); // Version
															// name
			variableHeader.write(VERSION); // Version number
			// Connect flags
			variableHeader.write((cleansession ? 1 : 0) << 1
					| (will ? 1 : 0) << 2 | (qos) << 3
					| (will_retain ? 1 : 0) << 5 | (password ? 1 : 0) << 6
					| (username ? 1 : 0) << 7);
			variableHeader.write(0x00); // Keep Alive MSB
			variableHeader.write(0x000A); // Keep Alive LSB (10 seconds)
			break;

		case PUBLISH:
			// Variable header, read the params.
			int message_id = Integer.parseInt(params[0]);
			String topic_name = params[1];

			variableHeader.write(0x00); // Topic MSB
			variableHeader.write(topic_name.getBytes("UTF-8").length); // Topic
																		// LSB
			variableHeader.write(topic_name.getBytes("UTF-8")); // Topic
			// variableHeader.write((message_id >> 8) & 0xFF); // Message ID MSB
			// variableHeader.write(message_id & 0xFF); // Message ID LSB
			break;

		case SUBSCRIBE:
			// Variable header, read the params.
			message_id = Integer.parseInt(params[0]);

			variableHeader.write((message_id >> 8) & 0xFF); // Message ID MSB
			variableHeader.write(message_id & 0xFF); // Message ID LSB
			break;

		case PINGREQ:
			// PINGREQ Doesn't have a variable header.
			break;
		}

		// Remaining length
		int length = payload.length + variableHeader.size();
		do {
			byte digit = (byte) (length % 128);
			length /= 128;
			if (length > 0)
				digit = (byte) (digit | 0x80);
			mqtt.write(digit);
		} while (length > 0);

		// Variable header
		mqtt.write(variableHeader.toByteArray());

		// Payload
		mqtt.write(payload);

		return mqtt.toByteArray();
	}

	public static MQTTMessage decode(byte[] message) {
		int i = 0;
		MQTTMessage mqtt = new MQTTMessage();
		mqtt.type = (message[i] >> 4) & 0x0F;
		mqtt.DUP = ((message[i] >> 3) & 0x01) == 0 ? false : true;
		mqtt.QoS = (message[i] >> 1) & 0x03;
		mqtt.retain = (message[i++] & 0x01) == 0 ? false : true;

		int multiplier = 1;
		int len = 0;
		byte digit = 0;
		do {
			digit = message[i++];
			len += (digit & 127) * multiplier;
			multiplier *= 128;
		} while ((digit & 128) != 0);
		mqtt.remainingLength = len;

		int offset = 0;

		switch (mqtt.type) {
		case CONNECT:
			// Not needed...
			break;

		case CONNACK:
			// Reserved byte - not used
			byte reserved = message[i++];
			offset += 1;

			mqtt.variableHeader.put("return_code", message[i++]);
			offset += 1;

			break;

		case PUBLISH:
			int topic_name_len = (message[i++] * 256 + message[i++]);
			offset += 2;

			String protocol_name = new String(message, i, topic_name_len);
			mqtt.variableHeader.put("topic_name", protocol_name);
			offset += topic_name_len;

			int message_id = (message[i++] << 8 & 0xFF00 | message[i++] & 0xFF);
			mqtt.variableHeader.put("message_id", Integer.toString(message_id));
			offset += 2;
			break;

		case SUBSCRIBE:
			// Not needed...
			break;

		case PINGREQ:
			// Not needed...
			break;
		}

		ByteArrayOutputStream payload = new ByteArrayOutputStream();
		for (int b = offset; b < mqtt.remainingLength+2; b++)
			payload.write(message[b]);
		mqtt.payload = payload.toByteArray();

		return mqtt;
	}
}
