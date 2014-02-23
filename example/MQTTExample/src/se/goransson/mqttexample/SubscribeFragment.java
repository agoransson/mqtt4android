package se.goransson.mqttexample;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Copyright 2014 Andreas G�ransson
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @author ksango
 * 
 */
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
		String text = log.getText().toString();
		text = msg + "\n" + text;
		log.setText(text);
	}
}
