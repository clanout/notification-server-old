package reaper.notificationserver.service.gcm;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import reaper.notificationserver.conf.ConfLoader;
import reaper.notificationserver.conf.ConfResource;
import reaper.notificationserver.service.GsonProvider;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

import java.util.concurrent.TimeUnit;

/**
 * Created by aditya on 06/08/15.
 */
public class GcmHelper
{
    private static RestAdapter restAdapter;

    static
    {
        restAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(getOkHttpClient()))
                .setConverter(new GsonConverter(GsonProvider.get()))
                .setEndpoint("https://gcm-http.googleapis.com/gcm")
                .setRequestInterceptor(requestFacade -> {
                    requestFacade.addHeader("Content-Type", "application/json");
                    requestFacade.addHeader("Authorization", "key="+ConfLoader.getConf(ConfResource.GCM).get("api_key"));
                })
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
    }

    public static GcmApi getApi()
    {
        return restAdapter.create(GcmApi.class);
    }

    private static OkHttpClient getOkHttpClient()
    {
        OkHttpClient client = new OkHttpClient();
        client.setReadTimeout(45, TimeUnit.SECONDS);
        return client;
    }
}
