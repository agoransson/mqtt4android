mqtt4android
============

This library creates a new service called MQTTService.java, your application will conn

# Installation (Eclipse only)

1. Clone library `git clone git@github.com:agoransson/mqtt4android
2. Add Eclipse project to your workspace
3. Make sure you have correct Android SDK installed (you can change this SDK version to one that suits your project also, there is no need for special SDK)

# Create MQTT enabled Android project

1. Create new Android application
`File &#8594; New &#8594; Other &#8594; Android project``

2. Add the mqtt4android library to your project
Open the dialog `File &#8594; Properties &#8594; Android` and then click `Add...` and select the mqtt4android library.

3. Add the MQTTService service to your manifest
```xml
<service android:name="se.goransson.mqtt.MQTTService"></service> 
```

4. The library uses a Handler to communicate to your UI for safety reasons, create a handler that listens for MQTT state and messages.
```java
private Handler mHandler = new Handler() {

  @Override
  public void handleMessage(Message msg) {
    switch (msg.what) {
      case STATE_CHANGE:
        switch (msg.arg1) {
          case STATE_NONE:
            // Not connected
            break;
          case STATE_CONNECTING:
            // Trying to connect
            break;
          case STATE_CONNECTED:
            // Connected
            break;
          case STATE_CONNECTION_FAILED:
            // Connection failed
            break;
        }
          break;
        case PUBLISH:
          MQTTMessage message = (MQTTMessage) msg.obj;
          String topic = (String) message.variableHeader.remove("topic_name");
          byte[] payload = message.payload;
          // Do something with the received message
          break;
    }
  }
 };
```

5. Create a service connection instance. Also, set your previously created handler as the service handler
```java
private ServiceConnection connection = new ServiceConnection() {
  @Override
  public void onServiceConnected(ComponentName name, IBinder binder) {
    mqtt = ((MQTTBinder) binder).getService();
    isBound = true;
    
    mqtt.setHandler(mHandler);
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    isBound = false;
  }
};
```

6. Bind to the service when application starts.
```java
@Override
protected void onStart() {
  super.onStart();
  Intent service = new Intent(MainActivity.this, MQTTService.class);
  bindService(service, connection, Context.BIND_AUTO_CREATE);
}
```

7. Unbind the service when application is stopped.
```java
@Override
protected void onStop() {
  super.onStop();
  unbindService(connection);
}
```

# Example usage
The following small examples show how basic usage for MQTT.

# Connect to MQTT Server
1. Connecting to an MQTT server can only be done once the service connection has been established. Therefore you're recommended to attempt the connection in the service connection object after the Handler has been linked.
You can of course issue the connection elsewhere, as lone as you make sure the service connection is "alive and kicking".

```java
// Default host is test.mosquitto.org (you should change this!)
mqtt.setHost("mqtt.mah.se");

// Default mqtt port is 1883
mqtt.setPort(1883);

// Set a unique id for this client-broker combination (Should be a server-wide unique String)
mqtt.setId(Build.SERIAL);

// Open the connection to the MQTT server
mqtt.connect();
```

# Subscribe to topic
```java
// Subscribe to a topic with Quality of Service AT_MOST_ONCE
mqtt.subscribe(topic);
// Subscribe to a topic with specified Quality of Service ()
mqtt.subscribe(topic, EXACTLY_ONCE);
// Subscribe to multiple topics (String[]) with Quality of Service AT_MOST_ONCE
mqtt.subscribe(topics[]);
// Subscribe to multiple topics (String[]) with specified quality of service (byte[]) for each topic
mqtt.subscribe(topics[], qoss[]);
```

# Publish to topic
There are multiple ways of publishing to an MQTT topic, the simplest
```java
// Publish a String message
mqtt.publish("mytopic", "my message");
// Publish a byte[] message
mqtt.publish("mytopic", message[]);
// Publish a byte[] message with RETAIN flag set
mqtt.publish("mytopic", message[], true);
```