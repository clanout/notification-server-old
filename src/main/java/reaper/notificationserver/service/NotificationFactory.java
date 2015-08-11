package reaper.notificationserver.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;
import reaper.notificationserver.api.exceptions.HttpExceptions;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class NotificationFactory
{
    private static Logger log = Logger.getLogger(NotificationFactory.class);

    public static Notification multicastNotification(List<String> registrationIds, String jsonData) throws HttpExceptions.ServerError
    {
        try
        {
            Type type = new TypeToken<Map<String, String>>()
            {
            }.getType();
            return new Notification(null, registrationIds, GsonProvider.get().fromJson(jsonData, type));
        }
        catch (Exception e)
        {
            log.error("Unable to create multicast notification [" + e.getMessage() + "]");
            throw new HttpExceptions.ServerError();
        }
    }

    public static Notification broadcastNotification(String channelId, String jsonData) throws HttpExceptions.ServerError
    {
        try
        {
            Type type = new TypeToken<Map<String, String>>()
            {
            }.getType();
            return new Notification(channelId, null, GsonProvider.get().fromJson(jsonData, type));
        }
        catch (Exception e)
        {
            log.error("Unable to create broadcast notification [" + e.getMessage() + "]");
            throw new HttpExceptions.ServerError();
        }
    }
}
