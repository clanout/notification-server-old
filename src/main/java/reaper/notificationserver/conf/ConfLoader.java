package reaper.notificationserver.conf;


import reaper.notificationserver.conf.implementation.ApacheConf;

public class ConfLoader
{
    public static Conf getConf(ConfResource confResource)
    {
        return new ApacheConf(confResource.getResource());
    }
}
