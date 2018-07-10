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
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import ca.cmpt276.walkinggroupindigo.walkinggroup.R;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Group;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class RemoveMonitoringUserGroupMessageFragment extends AppCompatDialogFragment{

    private WGServerProxy proxy;
    private User user;

        @NonNull
        @Override        public Dialog onCreateDialog(Bundle savedInstanceState) {

            User.getInstance();
            Long userId = user.getId();

            @SuppressLint("InflateParams") View v = LayoutInflater.from(getActivity())
                    .inflate(R.layout.monitoring_user_removal_message, null);

            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            removeMonitoredFromGroup();
                    }
                }

                private void removeMonitoredFromGroup() {
                    TextView someId = Objects.requireNonNull(getActivity()).findViewById(R.id.group_id);
                    String groupAddress = someId.getText().toString();
                    Long groupId = Long.parseLong(groupAddress);
                    Call<Group> monitoredGroupCall = proxy.getGroupById(groupId);
                    List<Group> monitoredGroup = new ArrayList<>();
                    ProxyBuilder.callProxy(getActivity(),
                            monitoredGroupCall, monitoredGroup::remove);
                    Call<Void> removeMonitoredUserGroupCaller = proxy.removeGroupMember(groupId, userId);
                    ProxyBuilder.callProxy(getActivity(),
                            removeMonitoredUserGroupCaller, returnMonitoredUserGroupRemoved -> {});
                }
            };

            return new AlertDialog.Builder(getActivity())
                    .setTitle("Removing monitored user from a group")
                    .setView(v)
                    .setPositiveButton(android.R.string.ok, listener)
                    .setNegativeButton(android.R.string.cancel, listener)
                    .create();
        }
}
