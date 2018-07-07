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

public class StopMonitoringUserMessageFragment extends AppCompatDialogFragment {

    private WGServerProxy proxy;
    private User user;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        User.getInstance();
        Long userId = user.getId();

        @SuppressLint("InflateParams") View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.monitoring_user_removal_message, null);

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        removeMonitoredUser();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }

            private void removeMonitoredUser() {
//                TextView emailText  = Objects.requireNonNull(getActivity()).findViewById(R.id.txtMonitoringEmail)
//                String someId = emailText.getText().toString();
//                Long childId = Long.parseLong(someId);
                Call<User> childUserCall = proxy.getUserById(userId);
                List<User> monitors = new ArrayList<>();
                ProxyBuilder.callProxy(getActivity(),
                        childUserCall, monitors::remove);
                User childUser = monitors.get(0);
                Call<Void> removeMonitoredCaller = proxy.removeFromMonitorsUsers(userId, childUser.getId());
                ProxyBuilder.callProxy(getActivity(),
                        removeMonitoredCaller, returnRemovedMonitored -> {});
            }
        };

        return new AlertDialog.Builder(getActivity())
                .setTitle("Removing a user")
                .setView(v)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, listener)
                .create();
    }


}
