package se.goransson.mqttexample;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;



public class ConnectionFragment extends Fragment {

	private EditText host, client;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_connect, container, false);
		
		host = (EditText) v.findViewById(R.id.connection_host);
		client = (EditText) v.findViewById(R.id.connection_client);
	
		Button btn = (Button) v.findViewById(R.id.connection_button);
		btn.setOnClickListener(connectListener);
		return v;
	}
	
	@Override
	public void onResume() {
		getLastConnection();
		super.onResume();
	}
	
	private OnClickListener connectListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			MainActivity act = (MainActivity) getActivity();
			act.connect(host.getText().toString(), client.getText().toString());
			
			setLastConnection(host.getText().toString(), client.getText().toString());
		}
	};
	
	private void getLastConnection(){
		SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
		
		host.setText(prefs.getString("host", ""));
		client.setText(prefs.getString("client", ""));
	}
	
	private void setLastConnection(String host, String client){
		SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
		Editor edit = prefs.edit();
		edit.putString("host", host);
		edit.putString("client", client);
		edit.commit();
	}
}
