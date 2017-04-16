package radim.alarmingLogger.utils;

/**
 * Created by radim on 17.9.16.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import radim.alarmingLogger.R;

public class FragmentAlertDialog extends DialogFragment {

    public FragmentAlertDialog (){}//default constructor only
    private String title="title";
    private String message="warning message";
    private String ok = "OK";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle retrieved = this.getArguments();

        if(retrieved != null){

        title = retrieved.getString("title");
        message = retrieved.getString("message");
        ok = retrieved.getString("ok");

        }

        return new AlertDialog.Builder(getActivity())

                // set dialog icon
                .setIcon(R.drawable.warning)
                // set Dialog Title
                .setTitle(title)
                // Set Dialog Message
                .setMessage(message)
                // positive button
                .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }

                // negative button

                }).create();
    }
}
