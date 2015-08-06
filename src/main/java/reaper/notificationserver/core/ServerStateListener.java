package reaper.notificationserver.core;


import org.apache.log4j.Logger;
import reaper.notificationserver.db.DataSource;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ServerStateListener implements ServletContextListener
{
    private static Logger log = Logger.getLogger(ServerStateListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent)
    {
        log.info("[[ NOTIFICATION SERVER STARTED ]]");

        try
        {
            log.info("[ Initializing DB Connection Pool ]");
            DataSource.init();
        }
        catch (Exception e)
        {
            log.fatal("Unable to initialize DB Connection Pool", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent)
    {
        log.info("[ Closing DB Connection Pool ]");
        DataSource.close();

        log.info("[[ NOTIFICATION SERVER STOPPED ]]");
    }
}
