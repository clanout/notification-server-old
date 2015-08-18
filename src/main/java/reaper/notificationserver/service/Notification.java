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
    private Data data;

    public Notification(String to, List<String> registrationIds, Data data)
    {
        this.to = to;
        this.registrationIds = registrationIds;
        this.data = data;
    }

    public static class Data
    {
        @SerializedName("type")
        private String type;

        @SerializedName("message")
        private String message;

        @SerializedName("parameters")
        private Map<String, String> parameters;
    }
}
