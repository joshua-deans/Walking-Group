package ca.cmpt276.walkinggroupindigo.walkinggroup;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.util.List;

import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.Message;
import ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyBuilder;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.ProxyFunctions;
import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;

public class UpdateMessages extends IntentService {

    private WGServerProxy proxy;
    private User mUser;

    public UpdateMessages(String name) {
        super(name);
    }

    public UpdateMessages() {
        super("");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        assert intent != null;
        mUser = User.getInstance();
        proxy = ProxyFunctions.setUpProxy(UpdateMessages.this, getString(R.string.apikey));
        Call<List<Message>> messageCall = proxy.getMessages(mUser.getId());
        ProxyBuilder.callProxy(messageCall, messageList -> updateUser(messageList));
    }

    private void updateUser(List<Message> messageCall) {
        mUser.setMessages(messageCall);
    }
}
