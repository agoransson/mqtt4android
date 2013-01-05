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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * A simple local service implementation of an mqtt interface for android
 * applications.
 * 
 * Parts of this class (prominently ConnectThread and ConnectedThread and
 * methods connected to thread handling) are based on code found in Android
 * Sample Application "BluetoothChat" by Google, distributed via Android SDK.
 * 
 * @author ksango
 * 
 */
public class MQTTService extends Service implements MQTTConnectionConstants,
		MQTTConstants {

	private static final boolean DEBUG = true;

	private static final String TAG = "MQTTService";

	/** Current state of the connection */
	private volatile int mState = STATE_NONE;

	/** How long to wait before a reconnect attempt is made */
	private long RECONNECT_TIMER = 5000;

	/** Thread to handle setup of connections */
	private ConnectThread mConnectThread;

	/** Thread to handle communication when connection is established. */
	private ConnectedThread mConnectedThread;

	/** Thread to handle ping requests and responses */
	private PingThread mPingThread;

	/** */
	private Handler mHandler = null;

	/** The local binder object */
	private final MQTTBinder mBinder = new MQTTBinder();

	private String host = "test.mosquitto.org";

	private int port = 1883;

	/** Unique identifier for this client */
	private String uid;

	private int message_id;

	private boolean clean_session = true;

	// PING VARIABLES
	private volatile boolean pingreq = false;
	private volatile long pingtime = 0;
	private volatile long lastaction = 0;

	private boolean doAutomaticReconnect = false;
	private Handler reconnectHandler = new Handler();
	private Runnable recoonectRunnable = new Runnable() {
		@Override
		public void run() {
			connect();
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();

		// TODO something when started

		if (DEBUG)
			Log.i(TAG, "onCreate");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Start a sticky service, the system will try to recreate this service
		// if killed.

		if (DEBUG)
			Log.i(TAG, "onStartCommand");

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Kill everything when we stop the service
		// Cancel the thread that completed the connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Cancel any thread currently running a ping check
		if (mPingThread != null) {
			mPingThread.cancel();
			mPingThread = null;
		}

		if (DEBUG)
			Log.i(TAG, "onDestroy");
	}

	@Override
	public IBinder onBind(Intent intent) {

		if (DEBUG)
			Log.i(TAG, "onBind");

		return mBinder;
	}

	public class MQTTBinder extends Binder {
		public MQTTService getService() {
			return MQTTService.this;
		}
	}

	public void setHandler(Handler handler) {
		mHandler = handler;
	}

	/**
	 * Send a CONNECT message to the server.
	 */
	private void connect(String host, int port, String uid) {
		if (getState() != STATE_CONNECTED) {
			try {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
					new MQTTHelperThread().execute(MQTT.connect(uid,
							clean_session));
				else
					mConnectedThread.write(MQTT.connect(uid, clean_session));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			if (DEBUG)
				Log.i(TAG, "Already connected");
		}
	}

	public int publish(String topic, byte[] message) {
		int message_id = getMessageid();

		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				new MQTTHelperThread().execute(MQTT.publish(topic, message));
			else
				mConnectedThread.write(MQTT.publish(topic, message));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return message_id;
	}

	public int publish(String topic, byte[] message, boolean retain) {
		int message_id = getMessageid();

		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				new MQTTHelperThread().execute(MQTT.encode(PUBLISH, retain,
						AT_MOST_ONCE, false, message, Integer.toString(1),
						topic));
			else
				mConnectedThread.write(MQTT.publish(topic, message));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return message_id;
	}

	public int publish(String topic, String message) {
		int message_id = getMessageid();

		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				new MQTTHelperThread().execute(MQTT.publish(topic,
						message.getBytes()));
			else
				mConnectedThread.write(MQTT.publish(topic, message.getBytes()));

		} catch (IOException e) {
			e.printStackTrace();
		}

		return message_id;
	}

	public int subscribe(String topic, byte qos) {
		int message_id = getMessageid();
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				new MQTTHelperThread().execute(MQTT.subscribe(message_id,
						topic, qos));
			else
				mConnectedThread.write(MQTT.subscribe(message_id, topic, qos));

		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		return message_id;
	}

	public int subscribe(String[] topic, byte[] qos) {
		int message_id = getMessageid();
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
				new MQTTHelperThread().execute(MQTT.subscribe(message_id,
						topic, qos));
			else
				mConnectedThread.write(MQTT.subscribe(message_id, topic, qos));

		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		return message_id;
	}

	// public synchronized void start() {
	// if (DEBUG)
	// Log.d(TAG, "start");
	//
	// // Cancel any thread attempting to make a connection
	// if (mConnectThread != null) {
	// mConnectThread.cancel();
	// mConnectThread = null;
	// }
	//
	// // Cancel any thread currently running a connection
	// if (mConnectedThread != null) {
	// mConnectedThread.cancel();
	// mConnectedThread = null;
	// }
	//
	// setState(STATE_NONE);
	//
	// // if (host != null)
	// // connect(host, port);
	// }

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setId(String uid) {
		this.uid = uid;
	}

	public void setCleanSession(boolean clean_session) {
		this.clean_session = clean_session;
	}

	private int getMessageid() {
		return (message_id == 65536 ? (message_id = 0) : message_id++);
	}

	public void setReconnect(boolean reconnect) {
		doAutomaticReconnect = reconnect;
	}

	public void reconnect() {
		reconnectHandler.postDelayed(recoonectRunnable, RECONNECT_TIMER);
	}

	public void reconnect(long millis) {
		reconnectHandler.postDelayed(recoonectRunnable, millis);
	}

	public synchronized void connect() {
		if (DEBUG)
			Log.d(TAG, "connect to: " + host);

		// Cancel any thread currently running a ping check
		if (mPingThread != null) {
			mPingThread.cancel();
			mPingThread = null;
		}

		// Cancel any thread attempting to make a connection
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(host, port);
		mConnectThread.start();

		setState(STATE_CONNECTING);
	}

	public synchronized void connected(Socket socket) {
		if (DEBUG)
			Log.d(TAG, "connected, Socket Type:");

		// Cancel any thread currently running a ping check
		if (mPingThread != null) {
			mPingThread.cancel();
			mPingThread = null;
		}

		// Cancel the thread that completed the connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();

		// Start the ping check thread
		mPingThread = new PingThread();
		mPingThread.start();

		// Send the connect message
		connect(host, port, uid);

		setState(STATE_CONNECTED);

		// Set the current time as the last action
		lastaction = System.currentTimeMillis();
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		if (DEBUG)
			Log.d(TAG, "connectionFailed");

		if (doAutomaticReconnect)
			reconnect();
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		if (DEBUG)
			Log.d(TAG, "connectionLost");

		if (doAutomaticReconnect)
			reconnect();
	}

	public void disconnect() {
		// Cancel any thread currently running a ping check
		if (mPingThread != null) {
			mPingThread.cancel();
			mPingThread = null;
		}

		// Cancel the thread that completed the connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		setState(STATE_NONE);
	}

	private synchronized void setState(int state) {
		if (DEBUG)
			Log.d(TAG, "setState() " + mState + " -> " + state);

		mState = state;

		if (mHandler != null)
			// Give the new state to the Handler so the UI Activity can update
			mHandler.obtainMessage(STATE_CHANGE, state, -1).sendToTarget();
	}

	public synchronized int getState() {
		return mState;
	}

	public void setKeepAlive(int milliseconds) {
		KEEP_ALIVE_TIMER = milliseconds;
	}

	private int KEEP_ALIVE_TIMER = 10000;

	private class PingThread extends Thread {

		private static final int KEEP_ALIVE_GRACE = 2000;

		public PingThread() {
			if (DEBUG)
				Log.d(TAG, "CREATE mPingthread ");

			// Set the current time as the last action
			lastaction = System.currentTimeMillis();
		}

		@Override
		public void run() {
			if (DEBUG)
				Log.i(TAG, "BEGIN mPingthread");

			boolean local_pingreq = pingreq;
			long local_pingtime = pingtime;
			long local_lastaction = lastaction;

			int local_state = mState;

			while (true) {
				if (local_state != mState) {
					local_state = mState;

					if (DEBUG)
						Log.i(TAG, "Detected change in volatile var: state");
				}

				if (local_state == STATE_CONNECTED) {
					if (local_pingreq != pingreq) {
						// TODO React to when the listener thread changed the
						// pingreq
						local_pingreq = pingreq;

						if (DEBUG)
							Log.i(TAG,
									"Detected change in volatile var: pingreq");
					}

					if (local_lastaction != lastaction) {
						// TODO React to when a new action is set
						local_lastaction = lastaction;

						if (DEBUG)
							Log.i(TAG,
									"Detected change in volatile var: lastaction");
					}

					if (local_pingreq) {
						// If we're expecting a ping response; detect if we've
						// timed
						// out.
						if ((System.currentTimeMillis() - local_lastaction) > (KEEP_ALIVE_TIMER + KEEP_ALIVE_GRACE)) {
							// Disconnect?
							// TODO Disconnect
							// if (DEBUG)
							Log.i(TAG,
									"Ping time out detected, should disconnect?");
							pingreq = false;
						}
					} else {
						// If the last action was too long ago; send a ping
						if ((System.currentTimeMillis() - local_lastaction) > KEEP_ALIVE_TIMER) {
							try {
								// Send ping
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
									new MQTTHelperThread().execute(MQTT.ping());
								else
									mConnectedThread.write(MQTT.ping());

								// Set volatile pingreq var to true
								pingreq = true;

								if (DEBUG)
									Log.i(TAG, "Sending ping req");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}

					// if (local_state != mState)
					// local_state = mState;

				} else {
					if (DEBUG)
						Log.i(TAG, "Not connected??");
				}

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void cancel() {
			// TODO do we need to do something?
		}

	}

	private class ConnectThread extends Thread {
		private static final String TAG = "ConnectThread";

		private final Socket mmSocket;

		/** Timeout of connection attempts (ms) */
		private int timeout = 3000;

		private InetSocketAddress remoteAddr;

		public ConnectThread(String host, int port) {
			if (DEBUG)
				Log.d(TAG, "CREATE mConnectThread ");

			Socket tmp = new Socket();

			mmSocket = tmp;
		}

		public void run() {
			if (DEBUG)
				Log.i(TAG, "BEGIN mConnectThread");

			setName("ConnectThread");

			remoteAddr = new InetSocketAddress(host, port);

			// Make a connection to the Socket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mmSocket.connect(remoteAddr, timeout);
			} catch (IOException e) {
				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG,
							"unable to close() socket during connection failure",
							e2);
				}
				connectionFailed();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (MQTTService.this) {
				mConnectThread = null;
			}

			// Start the connected thread
			connected(mmSocket);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private final Socket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(Socket socket) {
			if (DEBUG)
				Log.d(TAG, "CREATE mConnectedThread");

			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the Socket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			if (DEBUG)
				Log.i(TAG, "BEGIN mConnectedThread");

			byte[] buffer = new byte[16384];
			int bytes;

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);

					if (mHandler != null)
						// Send the obtained bytes to the UI Activity
						// mHandler.obtainMessage(MQTT_RAW_READ, bytes, -1,
						// buffer)
						// .sendToTarget();

						if (bytes > 0) {
							MQTTMessage msg = MQTT.decode(buffer);

							// Share the recieved msg type back to activity
							if (mHandler != null)
								mHandler.obtainMessage(msg.type, msg)
										.sendToTarget();

							// Handle automatic responses here
							switch (msg.type) {
							case PUBLISH:
								// No need to act on normal PUBLISH messages.
								break;
							case PUBACK:
								break;
							case PUBREC:
								break;
							case PUBREL:
								break;
							case PUBCOMP:
								break;
							case SUBSCRIBE:
								// The client shouldn't receive any SUBSCRIBE
								// messages.
								break;
							case SUBACK:
								break;
							case UNSUBSCRIBE:
								break;
							case UNSUBACK:
								break;
							case PINGREQ:
								// The client shouldn't receive any PINGREQ
								// messages.
								break;
							case PINGRESP:
								// TODO PINGREQ was successful, connections
								// still
								// alive.
								pingreq = false;

								if (DEBUG)
									Log.i(TAG, "Got ping response");

								break;
							case DISCONNECT:
								// TODO close all threads when receiving the
								// DISCONNECT message.
								break;
							}
						}

				} catch (IOException e) {
					if (DEBUG)
						Log.e(TAG, "disconnected", e);

					connectionLost();

					disconnect();

					break;
				}
			}
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		public void write(byte[] buffer) {
			try {
				mmOutStream.write(buffer);

				if (mHandler != null)
					// Share the sent message back to the UI Activity
					mHandler.obtainMessage(MQTT_RAW_PUBLISH, -1, -1, buffer)
							.sendToTarget();

				lastaction = System.currentTimeMillis();
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);

				disconnect();

				if (doAutomaticReconnect)
					reconnect();
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

	/**
	 * Helper thread, making sure that all network access is handled outside of
	 * the UI thread.
	 * 
	 * @author ksango
	 * 
	 */
	class MQTTHelperThread extends AsyncTask<byte[], Void, Void> {

		@Override
		protected Void doInBackground(byte[]... params) {

			for (int i = 0; i < params.length; i++) {
				if (mConnectedThread != null)
					mConnectedThread.write(params[i]);
				else
					break;
			}

			return null;
		}
	};
}
