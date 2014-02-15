package se.goransson.mqttexample;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class PublishFragment extends Fragment {
	
	EditText topic, message;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_publish, container, false);
		
		topic = (EditText) v.findViewById(R.id.publish_topic);
		message = (EditText) v.findViewById(R.id.publish_message);
		
		Button btn = (Button) v.findViewById(R.id.publish_button);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity act = (MainActivity) getActivity();
				act.publish(topic.getText().toString(), message.getText().toString());
			}
		});
		
		return v;
	}
}
