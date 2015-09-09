package reaper.notificationserver.api.request;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.log4j.Logger;
import reaper.notificationserver.api.exceptions.HttpExceptions;
import reaper.notificationserver.service.GsonProvider;

import java.lang.reflect.Type;
import java.util.Map;

public class RequestFactory
{
    private static Logger log = Logger.getLogger(RequestFactory.class);

    public static Request create(String json) throws HttpExceptions.ServerError
    {
        try
        {
            Type type = new TypeToken<Map<String, String>>()
            {
            }.getType();
            Map<String, String> data = GsonProvider.get().fromJson(json, type);
            return new Request(data);
        }
        catch (Exception e)
        {
            log.error("Unable to process request json [" + e.getMessage() + "]", e);
            throw new HttpExceptions.ServerError();
        }
    }
}
