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

    public static Notification multicastNotification(List<String> registrationIds, Notification.Data data) throws HttpExceptions.ServerError
    {
        try
        {
            return new Notification(null, registrationIds, data);
        }
        catch (Exception e)
        {
            log.error("Unable to create multicast notification [" + e.getMessage() + "]");
            throw new HttpExceptions.ServerError();
        }
    }

    public static Notification broadcastNotification(String channelId, Notification.Data data) throws HttpExceptions.ServerError
    {
        try
        {
            return new Notification(channelId, null, data);
        }
        catch (Exception e)
        {
            log.error("Unable to create broadcast notification [" + e.getMessage() + "]");
            throw new HttpExceptions.ServerError();
        }
    }
}
