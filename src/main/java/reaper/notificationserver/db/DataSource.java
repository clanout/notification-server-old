package reaper.notificationserver.db;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import reaper.notificationserver.conf.Conf;
import reaper.notificationserver.conf.ConfLoader;
import reaper.notificationserver.conf.ConfResource;

import java.sql.Connection;
import java.sql.SQLException;

public class DataSource
{
    private static ComboPooledDataSource pooledDataSource;

    public static void init() throws Exception
    {
        Conf dbConf = ConfLoader.getConf(ConfResource.DB);

        pooledDataSource = new ComboPooledDataSource();
        pooledDataSource.setDriverClass(dbConf.get("db.postgres.driver"));
        pooledDataSource.setJdbcUrl(dbConf.get("db.postgres.url"));
        pooledDataSource.setUser(dbConf.get("db.postgres.user"));
        pooledDataSource.setPassword(dbConf.get("db.postgres.password"));

        pooledDataSource.setMinPoolSize(Integer.parseInt(dbConf.get("db.postgres.pool.min_size")));
        pooledDataSource.setAcquireIncrement(Integer.parseInt(dbConf.get("db.postgres.pool.increment_size")));
        pooledDataSource.setMaxPoolSize(Integer.parseInt(dbConf.get("db.postgres.pool.max_size")));
    }

    public static Connection getConnection() throws SQLException
    {
        return pooledDataSource.getConnection();
    }

    public static void close()
    {
        pooledDataSource.close();
    }
}
