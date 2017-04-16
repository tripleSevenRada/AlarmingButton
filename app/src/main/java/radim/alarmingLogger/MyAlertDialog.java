package radim.alarmingLogger;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.widget.Button;


public class MyAlertDialog extends DialogFragment implements IAlertDialogTypes, ILocalBroadcastConstants {

    private String title = "TITLE";
    private String message = "MESSAGE";
    private String severity = "INFO"; //INFO,WARNING,ERROR
    private String type = DEFAULT;


    public MyAlertDialog() {
    } //default constructor only

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle retrieved = this.getArguments();

        if (retrieved != null) {

            title = retrieved.getString("title");
            message = retrieved.getString("message");
            severity = retrieved.getString("severity");
            type = retrieved.getString("type");

        }

        int icon;
        switch (severity) {

            case "INFO":
                icon = R.mipmap.ic_launcher;
                break;
            case "WARNING":
                icon = R.drawable.warning;
                break;
            case "ERROR":
                icon = R.mipmap.ic_launcher;
                break;
            default:
                icon = R.mipmap.ic_launcher;

        }

        if (type.equals(BATTERY_OPTIMIZATION)) {
            final android.app.AlertDialog d = new android.app.AlertDialog.Builder(getActivity())
                    .setIcon(icon)
                    .setTitle(title)
                    .setMessage(Html.fromHtml(message))
                    .setPositiveButton(getResources().getString(R.string.toSettings), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            launchIntentAimedAtLocalBroadcast(POSITIVE_BUTTON_SETTINGS,
                                    LOCAL_BROADCAST_IDENTITY);
                        }
                    })
                    .setNeutralButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            launchIntentAimedAtLocalBroadcast(NEUTRAL_BUTTON,
                                    LOCAL_BROADCAST_IDENTITY);
                        }
                    }).create();
            createAlertOnShowListener(d);
            return d;
        }


        return new android.app.AlertDialog.Builder(getActivity()) //DEFAULT
                .setIcon(icon)
                .setTitle("default object")
                .setMessage("default message NO TYPE specified")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
    }

    private void launchIntentAimedAtLocalBroadcast(String message, String designation) {

        Intent intent = new Intent(designation);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

    }

    private void createAlertOnShowListener(final android.app.AlertDialog d) {

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = d.getButton(DialogInterface.BUTTON_NEUTRAL);
                b.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            }
        });

    }

}
