package datasync.connection;

import org.apache.commons.dbcp.BasicDataSource;
import org.hsqldb.server.Server;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.*;

public class HsqldbDataConnection {

    public static Connection makeConn(String dataConnParams) throws ClassNotFoundException, SQLException {
        String driver = "org.hsqldb.jdbcDriver";
        String url="jdbc:hsqldb:file:D:/workspace/vdb4.0/vdb-with-datasync/out/artifacts/vdb_war_exploded/WEB-INF/vdb/extdb;shutdown=true";
       // String url="jdbc:hsqldb:hsql://localhost:xdb";//"jdbc:hsqldb:D:\\workspace\\vdb4.0\\vdb-with-datasync\\out\\artifacts\\vdb_war_exploded\\WEB-INF\\vdb\\extdb\\user;shutdown=true";
        String user = "sa";
        String password ="";
        Class.forName(driver);
        Connection conn = DriverManager.getConnection(url, user, password);
        return conn;
    }

    public static JdbcTemplate makeJdbcTemplate(){
        JdbcTemplate jdbcTemplate=new JdbcTemplate();
        BasicDataSource ds=new BasicDataSource();
        ds.setDriverClassName("org.hsqldb.jdbcDriver");
       // req.getSession().getServletContext().getRealPath("/");
        ds.setUrl("jdbc:hsqldb:D:\\workspace\\vdb4.0\\vdb-with-datasync\\out\\artifacts\\vdb_war_exploded\\WEB-INF\\vdb\\extdb\\user;shutdown=true");
        jdbcTemplate.setDataSource(ds);
        return  jdbcTemplate;
    }

    private void startServer() {
        Server server = new Server();
        server.setDatabaseName(0, "test");
        server.setDatabasePath(0, "D:\\workspace\\vdb4.0\\vdb-with-datasync\\out\\artifacts\\vdb_war_exploded\\WEB-INF\\vdb\\extdb");
        server.setPort(9002);
        server.setSilent(true);
        server.setTrace(true);
        server.start();
    }


    public  static  void  main ( String [] args){
        try {
            Connection conn = makeConn("");
          //  conn.getMetaData();
            Statement st=conn.createStatement();
            ResultSet rs=st.executeQuery("select * from VDB_USERS");
            System.out.println();
            while(rs.next()){
               System.out.println(rs.getString("USERID")+" "
                    +rs.getString("PASSWORD"));
          }


            System.out.println();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
