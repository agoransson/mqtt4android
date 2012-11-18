package se.goransson.mqtt;

import se.goransson.mqtt.R;

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
 * 
 * @author ksango
 * 
 */
public interface MQTTConnectionConstants {

	// We're using this base number to make it less likely that any of these
	// constans will overlap with constants defined in the activity or by MQTT
	final int CONSTANTS_BASE = 846751925;

	/*
	 * Connection state constants
	 */
	/** Doing nothing... */
	public static final int STATE_NONE = CONSTANTS_BASE + 0;

	/** Trying to establish connection... */
	public static final int STATE_CONNECTING = CONSTANTS_BASE + 1;

	/** Connection established! */
	public static final int STATE_CONNECTED = CONSTANTS_BASE + 2;

	/** Indicate that the state of the connection has changed */
	public static final int STATE_CHANGE = CONSTANTS_BASE + 5;

	/*
	 * Handler messages
	 */
	/**
	 * Sent directly when the client has published, can be used if client wants
	 * to receive all messages it sends without subscribing.
	 */
	public static final int MQTT_RAW_PUBLISH = CONSTANTS_BASE + 10;

	/** Raw byte-array when a message has been read (any message on the stream) */
	public static final int MQTT_RAW_READ = CONSTANTS_BASE + 11;
}
