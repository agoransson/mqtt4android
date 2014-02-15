package se.goransson.mqttexample;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

public class Controller {

	private Activity mActivity;
	private FragmentManager mFragmentManager;

	private ConnectionFragment mConnectionFragment;
	private SubscribeFragment mSubscribeFragment;
	private PublishFragment mPublishFragment;

	public Controller(Activity activity) {
		this.mActivity = activity;

		mFragmentManager = mActivity.getFragmentManager();
	}

	public void showConnectionFragment() {
		if (mConnectionFragment == null)
			mConnectionFragment = new ConnectionFragment();

		Bundle args = new Bundle();
		// Add arguments
		mConnectionFragment.setArguments(args);

		showFragment(mConnectionFragment, "connection", false);
	}

	public void showSubscribeFragment() {
		if (mSubscribeFragment == null)
			mSubscribeFragment = new SubscribeFragment();

		Bundle args = new Bundle();
		// Add arguments
		mSubscribeFragment.setArguments(args);

		showFragment(mSubscribeFragment, "subscribe");
	}

	public void showPublishFragment() {
		if (mPublishFragment == null)
			mPublishFragment = new PublishFragment();

		Bundle args = new Bundle();
		// Add arguments
		mPublishFragment.setArguments(args);

		showFragment(mPublishFragment, "subscribe");
	}

	private void showFragment(Fragment frag, String tag) {
		showFragment(frag, tag, true);
	}

	private void showFragment(Fragment frag, String tag, boolean backstack) {
		if (backstack)
			mFragmentManager.beginTransaction()
					.replace(R.id.container, frag, tag).addToBackStack(tag)
					.commit();
		else
			mFragmentManager.beginTransaction()
					.replace(R.id.container, frag, tag).commit();

	}

	public void appendMessage(String text) {
		if (mSubscribeFragment != null) {
			mSubscribeFragment.appendMessage(text);
		}
	}
}
