package com.chrisrhoden.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ReaderActivity extends ListActivity implements
		OnAccountSelectedListener {

	public static final int ACCOUNT_SELECT = 9909;
	public static final String ACCOUNT_NAME = "account_name";
	private AccountManager accountManager;

	private SharedPreferences preferences;

	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			ReaderActivity.this.setListAdapter((ListAdapter) msg.obj);
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity, menu);
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);

		preferences = getPreferences(MODE_PRIVATE);

		accountManager = AccountManager.get(this);

		Account[] accounts = accountManager.getAccountsByType("com.google");
		if (preferences.contains(ACCOUNT_NAME)) {
			String accountName = preferences.getString(ACCOUNT_NAME, "");
			for (int i = 0; i < accounts.length; i++) {
				if (accounts[i].name.equals(accountName)) {
					onAccountSelected(accounts[i], false);
					return;
				}
			}
		}

		AccountSelectDialogFragment.newInstance(accounts, this).show(
				getFragmentManager(), "dialog");

	}

	@Override
	public void onAccountSelected(Account account) {
		onAccountSelected(account, true);
	}

	public void onAccountSelected(final Account account, final boolean writeConfig) {
		
		final AccountManagerFuture<Bundle> future = accountManager
				.getAuthToken(account, "reader", null, this, null, null);

		setProgressBarIndeterminateVisibility(true);

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				if (writeConfig) {
					preferences.edit().putString(ACCOUNT_NAME, account.name).commit();
				}
				
				try {
					Bundle b = future.getResult();
					String token = b.getString(AccountManager.KEY_AUTHTOKEN);

					try {
						String tag = "stream/contents/user/" + "-"
								+ "/label/Listen%20Subscriptions";

						HttpClient client = new DefaultHttpClient();
						HttpGet request = new HttpGet(
								"http://www.google.com/reader/api/0/" + tag);
						request.addHeader("Authorization", "GoogleLogin auth="
								+ token);

						HttpResponse response = client.execute(request);

						InputStreamReader is = new InputStreamReader((response
								.getEntity()).getContent());

						Gson gson = new Gson();

						StreamResponse data = gson.fromJson(is,
								StreamResponse.class);

						final ArrayAdapter<StreamItem> aa = new ArrayAdapter<StreamItem>(
								ReaderActivity.this,
								android.R.layout.simple_list_item_1,
								android.R.id.text1, data.items);

						ReaderActivity.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								ReaderActivity.this.setListAdapter(aa);
								ReaderActivity.this
										.setProgressBarIndeterminateVisibility(false);
							}
						});

					} catch (IOException e) {
						e.printStackTrace();
					}
				} catch (OperationCanceledException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (AuthenticatorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		});
		t.start();
	}
}