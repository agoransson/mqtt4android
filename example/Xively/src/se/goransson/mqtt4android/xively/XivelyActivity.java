package se.goransson.mqtt4android.xively;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class XivelyActivity extends Activity implements
		MQTTConnectionConstants, MQTTConstants {

	protected static final String TAG = "XivelyActivity";

	private MQTTService mqtt;

	boolean isBound;

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_xively);
		
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	
		Button btn = (Button) findViewById(R.id.button1);
		btn.setOnClickListener(clickListener);
	}
	
	@Override
	protected void onResume() {
		mSensorManager.registerListener(sensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		mSensorManager.unregisterListener(sensorListener);
		super.onPause();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Intent service = new Intent(XivelyActivity.this, MQTTService.class);
		bindService(service, connection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindService(connection);
	}
	
	private SensorEventListener sensorListener = new SensorEventListener() {
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			valX = event.values[0];
			valY = event.values[1];
			valZ = event.values[2];
		}
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}
	};

	protected float valX, valY, valZ;
	
	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				JSONObject datastreamX = new JSONObject();
				datastreamX.put("id", "x");
				datastreamX.put("current_value", valX);

				JSONObject datastreamY = new JSONObject();
				datastreamY.put("id", "y");
				datastreamY.put("current_value", valY);

				JSONObject datastreamZ = new JSONObject();
				datastreamZ.put("id", "z");
				datastreamZ.put("current_value", valZ);
				
				JSONArray datastreams = new JSONArray();
				datastreams.put(datastreamX);
				datastreams.put(datastreamY);
				datastreams.put(datastreamZ);
				
				JSONObject request = new JSONObject();
				request.put("version", "1.0.0");
				request.put("datastreams", datastreams);
				
				mqtt.publish(
						"TBUu1rX3sHnDE6hNkRN5eGzHq4qzbhxCx8jUlnM4NmhRDkZd/v2/feeds/1145308537.json",
						request.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};

	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			mqtt = ((MQTTBinder) binder).getService();
			isBound = true;

			mqtt.setHandler(mHandler);

			// Default host is test.mosquitto.org (you should change this!)
			mqtt.setHost("api.xively.com");

			// Default mqtt port is 1883
			mqtt.setPort(1883);

			// Set a unique id for this client-broker combination
			mqtt.setId(Build.SERIAL);


			// Open the connection to the MQTT server
			mqtt.connect();
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
					Toast.makeText(XivelyActivity.this, "Not connected",
							Toast.LENGTH_SHORT).show();
					break;
				case STATE_CONNECTING:
					Toast.makeText(XivelyActivity.this, "Trying to connect...",
							Toast.LENGTH_SHORT).show();
					break;
				case STATE_CONNECTED:
					Toast.makeText(XivelyActivity.this, "Yay! Connected!",
							Toast.LENGTH_SHORT).show();
					break;
				case STATE_CONNECTION_FAILED:
					Toast.makeText(XivelyActivity.this, "Connection failed",
							Toast.LENGTH_SHORT).show();
					break;
				}
				break;

			case PUBLISH:
				MQTTMessage message = (MQTTMessage) msg.obj;

				String topic = (String) message.variableHeader
						.remove("topic_name");

				byte[] payload = message.payload;

				Log.i(TAG, "recieved payload");

				break;

			case MQTT_RAW_READ:
				// byte[] buf = (byte[]) msg.obj;
				// String s = new String(buf);
				// Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT);
				break;
			}
		}

	};
}
