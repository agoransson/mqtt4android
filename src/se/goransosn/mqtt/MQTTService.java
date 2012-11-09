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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * A simple local service implementation of an mqtt interface for android
 * applications.
 * 
 * @author ksango
 * 
 */
public class MQTTService extends Service implements MQTTConnectionConstants {

	private static final boolean DEBUG = true;

	private static final String TAG = "MQTTService";

	/** Current state of the connection */
	private int mState = STATE_NONE;

	/** Thread to handle setup of connections */
	private ConnectThread mConnectThread;

	/** Thread to handle communication when connection is established. */
	private ConnectedThread mConnectedThread;

	/** */
	private Handler mHandler = null;

	/** The local binder object */
	private final MQTTBinder mBinder = new MQTTBinder();

	private String host = "test.mosquitto.org";

	private int port = 1883;

	/** Unique identifier for this client */
	private String uid;

	@Override
	public void onCreate() {
		super.onCreate();

		// TODO something when started
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Start a sticky service, the system will try to recreate this service
		// if killed.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// TODO something when destroyed
	}

	@Override
	public IBinder onBind(Intent intent) {
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
		try {
			new MQTTHelperThread().execute(MQTT.connect(uid));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void publish(String topic, String message) {
		// TODO
	}

	public void subscribe(String topic) {
		// TODO
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

	public synchronized void connect() {
		if (DEBUG)
			Log.d(TAG, "connect to: " + host);

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

		connect(host, port, uid);
		
		setState(STATE_CONNECTED);
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		if (DEBUG)
			Log.d(TAG, "connectionFailed");

		// MQTTService.this.start();
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		if (DEBUG)
			Log.d(TAG, "connectionLost");

		// MQTTService.this.start();
	}

	private synchronized void setState(int state) {
		if (DEBUG)
			Log.d(TAG, "setState() " + mState + " -> " + state);

		mState = state;

		if (mHandler != null)
			// Give the new state to the Handler so the UI Activity can update
			mHandler.obtainMessage(STATE_CHANGE, state, -1).sendToTarget();
	}

	private class ConnectThread extends Thread {
		private static final String TAG = "ConnectThread";

		private final Socket mmSocket;

		/** Timeout of connection attempts (ms) */
		private int timeout = 3000;

		private InetSocketAddress remoteAddr;

		public ConnectThread(String host, int port) {
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
				Log.d(TAG, "create ConnectedThread: ");

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

			byte[] buffer = new byte[1024];
			int bytes;

			// Keep listening to the InputStream while connected
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);

					if (mHandler != null)
						// Send the obtained bytes to the UI Activity
						mHandler.obtainMessage(MQTT_RAW_READ, bytes, -1, buffer)
								.sendToTarget();
				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					connectionLost();

					// Start the service over to restart listening mode
					// MQTTService.this.start();
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
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
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

			for (int i = 0; i < params.length; i++)
				mConnectedThread.write(params[i]);

			return null;
		}
	};
}
