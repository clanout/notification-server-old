package reaper.notificationserver.conf;

import java.net.URL;

public enum ConfResource
{
    DB("db.conf"),
    GCM("gcm.conf");

    private String FILE;

    private ConfResource(String filename)
    {
        this.FILE = filename;
    }

    public URL getResource()
    {
        return ConfResource.class.getResource("/conf/" + FILE);
    }
}
