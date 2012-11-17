package se.goransson.mqttexample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SubscribeFragment extends Fragment {

	EditText topic;
	TextView log;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View root = inflater.inflate(R.layout.fragment_subscribe, container,
				false);
		
		log = (TextView) root.findViewById(R.id.subscribe_log);
		topic = (EditText) root.findViewById(R.id.subscribe_topic);

		Button subscribe = (Button) root.findViewById(R.id.subscribe_button);
		subscribe.setOnClickListener(subscribeListener);

		return root;
	}

	private OnClickListener subscribeListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			MainActivity a = (MainActivity) getActivity();

			a.subscribe(topic.getText().toString());
		}
	};

	public void appendMessage(String msg) {
		log.append("\n");
		log.append(msg);
	}
}
