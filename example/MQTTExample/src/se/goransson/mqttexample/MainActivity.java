package se.goransson.mqttexample;

import java.util.ArrayList;

import se.goransosn.mqtt.MQTTConnectionConstants;
import se.goransosn.mqtt.MQTTService;
import se.goransosn.mqtt.MQTTService.MQTTBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		MQTTConnectionConstants {

	ViewPager mViewPager;

	ArrayList<Fragment> pages = new ArrayList<Fragment>();

	MyFragmentPagerAdapter pageAdapter;

	MQTTService mqtt;

	boolean isBound;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.pager);

		pages.add(new SubscribeFragment());
		pages.add(new PublishFragment());

		pageAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(),
				pages);

		mViewPager.setAdapter(pageAdapter);

		setContentView(mViewPager);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			mqtt = ((MQTTBinder) binder).getService();
			isBound = true;

			mqtt.setHandler(mHandler);

			// mqtt.setHost("");
			// mqtt.setPort(1883);

			mqtt.setId("andreas");

			mqtt.open();
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
					mqtt.connect();
					break;
				}
				break;

			case MQTT_RAW_READ:
//				byte[] buf = (byte[]) msg.obj;
//				String s = new String(buf);
//				Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT);
				break;
			}
		}

	};

	protected void publish(String topic, String message) {
		// mqtt.publish(topic, message);
	}

	protected void subscribe(String topic) {
		// mqtt.subscribe(topic);
	}
}
