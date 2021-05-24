
package com.overstock.mp.eos.e2e.common;

        import org.springframework.beans.factory.annotation.Value;
        import org.springframework.jdbc.datasource.DriverManagerDataSource;
        import org.springframework.stereotype.Component;

        import javax.sql.DataSource;

        import static java.util.Objects.isNull;

@Component
public class DbConnection {

    private DataSource dataSource;

    @Value("${dbClassName}")
    private String dbClassname;

    @Value("${dbURL}")
    private String dbUrl;

    @Value("${dbUserName}")
    private String getDbClassname;

    @Value("${dbPassword}")
    private String dbPassword;

    public DbConnection() {
    }

    public DataSource getDataSource() {

        if (isNull(this.dataSource)) {
            final DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName(dbClassname);
            ds.setUrl(dbUrl);
            ds.setUsername(getDbClassname);
            ds.setPassword(dbPassword);
            this.dataSource = ds;
        }
        return dataSource;
    }
}

