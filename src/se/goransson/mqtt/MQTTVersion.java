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

/**
 * Defines the current MQTT version.
 * 
 * @author ksango
 * 
 */
public interface MQTTVersion {

	/** MQTT 3.1 Version name */
	static String NAME_31 = "MQIsdp";
	/** MQTT 3.1 Version number */
	static byte VERSION_31 = (byte) 0x03;

	/** MQTT 3.1.1 Version name */
	static String NAME_311 = "MQTT";
	/** MQTT 3.1 Version number */
	static byte VERSION_311 = (byte) 0x04;
}
