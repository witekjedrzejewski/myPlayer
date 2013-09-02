package com.example.newplayer;

import com.example.newplayer.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

public class FlagDialogFragment extends DialogFragment {
    
	OnFlagSelectedListener mCallback;
	public int selected = -1;
	
	/* container activity must implement thos interface.
	 * this is a recommended way to do this
	 */
	public interface OnFlagSelectedListener {
		public void onFlagSelected(int flag);
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnFlagSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFlagSelectedListener");
        }
    }
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

    	SharedPreferences pref = getActivity()
    			.getSharedPreferences(PlayingActivity.PREFERENCE_FILE, 0);
    	selected = pref.getInt(PlayingActivity.SELECTED_FLAG_PREFERENCE, 0);
    	
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_flags_dialog_fragment)
        		.setSingleChoiceItems(R.array.flags_array, selected, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        selected = which;
                    }
                })
        		.setPositiveButton(R.string.flags_dialog_ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       mCallback.onFlagSelected(selected);
                   }
        		})
        		.setNegativeButton(R.string.flags_dialog_cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
        		});
        // Create the AlertDialog object and return it
        return builder.create();
    }
}