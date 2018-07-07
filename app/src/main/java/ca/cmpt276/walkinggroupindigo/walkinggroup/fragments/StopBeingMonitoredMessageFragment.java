package ca.cmpt276.walkinggroupindigo.walkinggroup.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class StopBeingMonitoredMessageFragment extends AppCompatDialogFragment {

    private WGServerProxy proxy;
    private User user;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        User.getInstance();
        Long userId = user.getId();

        @SuppressLint("InflateParams") View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.stop_monitored_message_fragment, null);

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        removeMonitoringUser();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }

            private void removeMonitoringUser() {
                Call<User> parentUserCall = proxy.getUserById(userId);
                List<User> monitored = new ArrayList<>();
                ProxyBuilder.callProxy(getActivity(),
                        parentUserCall, monitored::remove);
                User parentUser = monitored.get(0);
                Call<Void> removeMonitoringCaller = proxy.removeFromMonitoredByUsers(userId, parentUser.getId());
                ProxyBuilder.callProxy(getActivity(),
                        removeMonitoringCaller, returnRemovedMonitoring -> {});
            }
        };

        return new AlertDialog.Builder(getActivity())
                .setTitle("Stop being monitored")
                .setView(v)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, listener)
                .create();
    }

}
