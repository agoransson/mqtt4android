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

/**
 * Defines all MQTT constants; including message types, quality of service, etc.
 * 
 * @author ksango
 * 
 */
public interface MQTTConstants {

	/*
	 * Message Types
	 */
	/** Client request to connect to a server */
	static final byte CONNECT = 0x01;

	/** Connect Acknowledgement */
	static final byte CONNACK = 0x02;

	/** Publish message */
	static final byte PUBLISH = 0x03;

	/** Publish Acknowledgment */
	static final byte PUBACK = 0x04;

	/** Publish Received */
	static final byte PUBREC = 0x05;

	/** Publish Release */
	static final byte PUBREL = 0x06;

	/** Publish Complete */
	static final byte PUBCOMP = 0x07;

	/** Client Subscription request */
	static final byte SUBSCRIBE = 0x08;

	/** Subscription Acknowledgment */
	static final byte SUBACK = 0x09;

	/** Client Unsubscribe request */
	static final byte UNSUBSCRIBE = 0x0a;

	/** Unsubscribe Acknowledgment */
	static final byte UNSUBACK = 0x0b;

	/** PING Request */
	static final byte PINGREQ = 0x0c;

	/** PING Response */
	static final byte PINGRESP = 0x0d;

	/** Client is Disconnecting */
	static final byte DISCONNECT = 0x0e;

	/*
	 * Quality of Service levels
	 */
	/** Fire and Forget */
	static final byte AT_MOST_ONCE = 0x00;

	/** Acknowledged deliver */
	static final byte AT_LEAST_ONCE = 0x01;

	/** Assured Delivery */
	static final byte EXACTLY_ONCE = 0x02;

	/*
	 * Connection responses
	 */
	/** Connection was accepted by server */
	static final byte CONNECTION_ACCEPTED = 0x00;

	/** Connection was refused: unacceptable protocol version */
	static final byte CONNECTION_REFUSED_VERSION = 0x01;

	/** Connection was refused: identifier rejected */
	static final byte CONNECTION_REFUSED_IDENTIFIER = 0x02;

	/** Connection was refused: server unavailable */
	static final byte CONNECTION_REFUSED_SERVER = 0x03;

	/** Connection was refused: bad username or password */
	static final byte CONNECTION_REFUSED_USER = 0x04;

	/** Connection was refused: not authorized */
	static final byte CONNECTION_REFUSED_AUTH = 0x05;

}
