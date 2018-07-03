package ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects;

import ca.cmpt276.walkinggroupindigo.walkinggroup.proxy.WGServerProxy;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {

    private static WGServerProxy service;
    private static ApiManager apiManager;

    private ApiManager(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://cmpt276-1177-bf.cmpt.sfu.ca:8184/users/signup")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(WGServerProxy.class);
    }

    public static ApiManager getInstance(){
        if(apiManager == null){
            apiManager = new ApiManager();
        }
        return apiManager;
    }

    public void createUser(User user, Callback<User> callback){
        Call<User> userCall = service.createUser(user);
        userCall.enqueue(callback);
    }
}
