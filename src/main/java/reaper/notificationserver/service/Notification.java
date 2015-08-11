package reaper.notificationserver.service;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class Notification
{
    @SerializedName("to")
    private String to;

    @SerializedName("registration_ids")
    private List<String> registrationIds;

    @SerializedName("data")
    private Map<String, String> data;

    public Notification(String to, List<String> registrationIds, Map<String, String> data)
    {
        this.to = to;
        this.registrationIds = registrationIds;
        this.data = data;
    }
}
