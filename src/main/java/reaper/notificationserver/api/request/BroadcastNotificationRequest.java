package reaper.notificationserver.api.request;

import com.google.gson.annotations.SerializedName;
import reaper.notificationserver.service.Notification;

public class BroadcastNotificationRequest
{
    @SerializedName("to")
    private String channelId;

    @SerializedName("data")
    private Notification.Data notification;

    public String getChannelId()
    {
        return channelId;
    }

    public Notification.Data getNotification()
    {
        return notification;
    }
}
