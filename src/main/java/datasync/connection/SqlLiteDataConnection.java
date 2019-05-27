package datasync.connection;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class SqlLiteDataConnection {

    public static JdbcTemplate makeJdbcTemplate(){
        JdbcTemplate jdbcTemplate=new JdbcTemplate();
        BasicDataSource ds=new BasicDataSource();
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite::resource:vdb_datasync.db");
        jdbcTemplate.setDataSource(ds);
        return  jdbcTemplate;
    }
}
