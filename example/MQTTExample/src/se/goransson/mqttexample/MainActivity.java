package se.goransson.mqttexample;

import se.goransson.mqtt.MQTTConnectionConstants;
import se.goransson.mqtt.MQTTConstants;
import se.goransson.mqtt.MQTTMessage;
import se.goransson.mqtt.MQTTService;
import se.goransson.mqtt.MQTTService.MQTTBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity implements MQTTConnectionConstants,
		MQTTConstants {

	protected static final String TAG = "MQTTExample";

	MQTTService mqtt;

	boolean isBound;

	private Controller mController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		mController = new Controller(this);
	}

	@Override
	protected void onResume() {
		mController.showConnectionFragment();
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent service = new Intent(MainActivity.this, MQTTService.class);
		bindService(service, connection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindService(connection);
	}

	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			mqtt = ((MQTTBinder) binder).getService();
			isBound = true;

			mqtt.setHandler(mHandler);

			// // Default host is test.mosquitto.org (you should change this!)
			// mqtt.setHost("mqtt.mah.se");
			//
			// // Default mqtt port is 1883
			// mqtt.setPort(1883);
			//
			// // Set a unique id for this client-broker combination
			// mqtt.setId(Build.SERIAL);
			//
			// // Open the connection to the MQTT server
			// mqtt.connect();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			isBound = false;
		}
	};

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case STATE_CHANGE:
				switch (msg.arg1) {
				case STATE_NONE:
					Toast.makeText(MainActivity.this, "Not connected",
							Toast.LENGTH_SHORT).show();
					break;
				case STATE_CONNECTING:
					Toast.makeText(MainActivity.this, "Trying to connect...",
							Toast.LENGTH_SHORT).show();
					break;
				case STATE_CONNECTED:
					Toast.makeText(MainActivity.this, "Yay! Connected!",
							Toast.LENGTH_SHORT).show();
						mController.showSubscribeFragment();
					break;
				case STATE_CONNECTION_FAILED:
					Toast.makeText(MainActivity.this, "Connection failed",
							Toast.LENGTH_SHORT).show();
					break;
				}
				break;

			case PUBLISH:
				MQTTMessage message = (MQTTMessage) msg.obj;
				
				String topic = (String) message.variableHeader.remove("topic_name");
				
				byte[] payload = message.payload;

				Log.i(TAG, "recieved payload");
				
				String text = new String(payload);
				mController.appendMessage(text);
				break;

			case MQTT_RAW_READ:
				// byte[] buf = (byte[]) msg.obj;
				// String s = new String(buf);
				// Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT);
				break;
			}
		}

	};

	protected void connect(String host, String client) {
		// Default host is test.mosquitto.org (you should change this!)
		mqtt.setHost(host);

		// Default mqtt port is 1883
		mqtt.setPort(1883);

		// Set a unique id for this client-broker combination
		mqtt.setId(client);

		// Open the connection to the MQTT server
		mqtt.connect();
	}

	protected void publish(String topic, String message) {
		mqtt.publish(topic, message);
	}

	protected void subscribe(String topic) {
		mqtt.subscribe(topic, AT_MOST_ONCE);
	}
}
