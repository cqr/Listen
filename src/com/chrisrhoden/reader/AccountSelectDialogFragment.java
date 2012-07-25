package com.chrisrhoden.reader;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

public class AccountSelectDialogFragment extends DialogFragment {
	
	public static final String ACCOUNTS = "Accounts";
	private Account[] accounts;
	private OnAccountSelectedListener listener;
	
	public static AccountSelectDialogFragment newInstance(Account[] accounts, OnAccountSelectedListener listener) {
		AccountSelectDialogFragment frag = new AccountSelectDialogFragment();
		frag.setAccounts(accounts);
		frag.setListener(listener);
		
		return frag;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		super.onCreateDialog(savedInstanceState);
		
		CharSequence[] accountNames = new CharSequence[accounts.length];
		for(int i=0; i<accountNames.length; i++){
			accountNames[i] = accounts[i].name;
		}
		
		return new AlertDialog.Builder(getActivity())
			.setTitle("Select Account to Use")
			.setCancelable(false)
			.setItems(accountNames, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					listener.onAccountSelected(accounts[which]);
				}			
			})
			.create();
	}
	
	private void setListener(OnAccountSelectedListener listener) {
		this.listener = listener;
	}
	
	private void setAccounts(Account[] accounts) {
		this.accounts = accounts;
	}

}
