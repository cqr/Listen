package com.chrisrhoden.reader;

import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.chrisrhoden.reader.api.StreamItem;
import com.chrisrhoden.reader.api.StreamResponse;
import com.google.gson.Gson;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.Window;
import android.widget.ArrayAdapter;

public class ReaderActivity extends ListActivity implements
		OnAccountSelectedListener, AccountManagerCallback<Bundle> {

	public static final int ACCOUNT_SELECT = 9909;
	public static final String ACCOUNT_NAME = "account_name";

	private AccountManager accountManager;
	private SharedPreferences preferences;

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
		if (accounts.length == 1){
			onAccountSelected(accounts[0]);
		} else if (preferences.contains(ACCOUNT_NAME)) {
			String accountName = preferences.getString(ACCOUNT_NAME, "");
			for (int i = 0; i < accounts.length; i++) {
				if (accounts[i].name.equals(accountName)) {
					onAccountSelected(accounts[i]);
					return;
				}
			}
		}

		// Nothing is saved.
		selectAccount(accounts);
	}

	private void selectAccount() {
		selectAccount(accountManager.getAccountsByType("com.google"));
	}

	private void selectAccount(Account[] accounts) {
		AccountSelectDialogFragment.newInstance(accounts, this).show(
				getFragmentManager(), "dialog");
	}

	@Override
	public void onAccountSelected(final Account account) {
		setProgressBarIndeterminateVisibility(true);
		accountManager.getAuthToken(account, "reader", null, this, this, null);
	}

	@Override
	public void run(AccountManagerFuture<Bundle> tokenResult) {
		try {
			Bundle bundle = tokenResult.getResult();
			String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);

			if (authToken == null) {
				selectAccount();
			} else {
				preferences
						.edit()
						.putString(
								ACCOUNT_NAME,
								bundle.getString(AccountManager.KEY_ACCOUNT_NAME))
						.commit();
				doThings(authToken);
			}
		} catch (OperationCanceledException e) {
			selectAccount();
		} catch (AuthenticatorException e) {
			selectAccount();
		} catch (IOException e) {
			selectAccount();
		}
	}

	private void doThings(final String token) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {

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

			}

		});
		t.start();
	}
}