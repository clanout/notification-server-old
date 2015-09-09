package reaper.notificationserver.api.request;

import com.google.gson.annotations.SerializedName;
import reaper.notificationserver.service.Notification;

import java.util.List;
import java.util.Set;

public class MulticastNotificationRequest
{
    @SerializedName("to")
    private List<String> userIds;

    @SerializedName("data")
    private Notification.Data notification;

    public List<String> getUserIds()
    {
        return userIds;
    }

    public Notification.Data getNotification()
    {
        return notification;
    }
}
