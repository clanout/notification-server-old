package reaper.notificationserver.service;

import com.google.gson.Gson;

public class GsonProvider
{
    public static Gson get()
    {
        return new Gson();
    }
}
