package reaper.notificationserver.api.request;

import java.util.HashMap;
import java.util.Map;

public class Request
{
    private Map<String, String> data;

    public Request(Map<String, String> data)
    {
        if(data == null)
        {
            data = new HashMap<>();
        }
        this.data = data;
    }

    public String get(String key)
    {
        return data.get(key);
    }
}
