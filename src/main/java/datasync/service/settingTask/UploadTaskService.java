package datasync.service.settingTask;

import datasync.connection.MysqlDataConnection;
import datasync.connection.OracleDataConnection;
import datasync.connection.SqlLiteDataConnection;
import datasync.entity.DataSrc;
import datasync.entity.DataTask;
import datasync.mapper.DataSrcMapper;
import datasync.mapper.DataTaskMapper;
import datasync.service.FileResourceService;
import datasync.service.login.GetInfoService;
import datasync.utils.ConfigUtil;
import datasync.utils.DDL2SQLUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class UploadTaskService {
    private Logger logger = LoggerFactory.getLogger(UploadTaskService.class);

    public String exportDataTask(HttpServletRequest request,String dataTaskId) {

     //   System.out.println("enterring exportDataTask - dataTaskId = " + dataTaskId);

        //输入参数校验
        if (dataTaskId == null || dataTaskId.trim().equals(""))
        {
            return "";
        }

        //getDataTask有可能是null
        DataTask dataTask = getDataTask(dataTaskId);
        if (dataTaskId == null)
        {
            return "";
        }
        System.out.println("dataTask = " + dataTask.toString());

        String dataTaskType = "";
        dataTaskType = dataTask.getDataTaskType();
        dataTaskType = dataTaskType.trim();
        String ZipFilePath="";

        //防止dataTaskType字段没有值的情况
        if (dataTaskType == null || dataTaskType.equals(""))
        {
            return "";
        }

        //file类型的数据任务, 目前在导出中对file类型的数据任务不做任何处理
        if (dataTaskType.equals("file"))
        {
        }
        else if (dataTaskType.equals("mysql"))  //mysql类型的数据任务, mysql类型的数据任务需要做
        {
            try {
                //测试用的connection 和 dataTask
                String dataSourceId = dataTask.getDataSourceId() + "";
                DataSrc dataSrc = getDataSource(dataSourceId);
                String getConnectionParameter = dataSrc.getDataSourceType();
                //String getConnectionParameter = dataSrc.getDataSourceName() + "$" + dataSrc.getHost() + "$" + dataSrc.getPort() + "$" + dataSrc.getUserName() + "$" + dataSrc.getPassword() +"$mysql$" + dataSrc.getDatabaseName();

                //以下为测试数据
                /*String getConnectionParameter = "mysql-export$192.168.192.133$3306$root$123456$mysql$testdb";
                DataTask dataTask1 = new DataTask();
                dataTask1.setDataTaskId(dataTaskId);
                dataTask1.setTableName("t1;t2;");
                dataTask1.setSqlString("select * from t1");
                dataTask1.setSqlTableNameEn("t1Temp");*/

                Connection mysqlDataConnection = MysqlDataConnection.makeConn(getConnectionParameter);
                System.out.println("mysqlDataConnection = " + mysqlDataConnection);

                ZipFilePath=exportTaskDataFromSql(request, mysqlDataConnection, dataTask);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if (dataTaskType.equals("oracle"))  //oracle类型的数据任务,支持多表和多条sql语句
        {
            try {
                //测试用的connection 和 dataTask
                String dataSourceId = dataTask.getDataSourceId() + "";
                DataSrc dataSrc = getDataSource(dataSourceId);
                String getConnectionParameter = dataSrc.getDataSourceType();
                Connection oracleDataConnection = OracleDataConnection.makeConn(getConnectionParameter);
                System.out.println("oracleDataConnection = " + oracleDataConnection);

                ZipFilePath=exportTaskDataFromSql(request, oracleDataConnection, dataTask);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return ZipFilePath;
    }

    private String exportTaskDataFromSql(HttpServletRequest request, Connection connection, DataTask dataTask)
    {
        String dataTaskId = dataTask.getDataTaskId() + "";
        String exportedDataDir = request.getSession().getServletContext().getRealPath("/exportedData" + File.separator + dataTaskId);
        System.out.println("enterring exportTaskDataFromSql - exportedDataDir = " + exportedDataDir + ", dataTaskId = " + dataTaskId);

        //多表导出支持
        //直接选择的表名， tableName可能是多个
        String tableNames = dataTask.getTableName();
        if (tableNames == null || tableNames.trim().equals(""))
        {
            tableNames = "";
        }
        System.out.println("export tables - tableNames = " + tableNames);

        StringBuilder sqlSb = new StringBuilder();
        StringBuilder dataSb = new StringBuilder();
        for (String tableName : tableNames.split(";"))
        {
            if (tableName == null || tableName.trim().equals(""))
            {
                continue;
            }

            sqlSb.append(DDL2SQLUtils.generateDDLFromTable(connection, null, null, tableName.trim()));
            dataSb.append(DDL2SQLUtils.generateInsertSqlFromTable(connection, null, null, tableName));
            dataSb.append("\n");
        }

        //以select语句形式导出的数据，支持多条SqlString
        String sqlTableNames = dataTask.getSqlTableNameEn();
        String sqlStrings = dataTask.getSqlString();

        //防止sqlTableNames一个值也没有，引发NullPointerException
        if (sqlTableNames == null || sqlTableNames.trim().equals(""))
        {
            sqlTableNames = "";
        }
        //防止sqlStrings一个值也没有，引发NullPointerException
        if (sqlStrings == null || sqlStrings.trim().equals(""))
        {
            sqlStrings = "";
        }
        System.out.println("export sqlString - sqlString = " + sqlStrings + ", sqlTableNames = " + sqlTableNames);

        /*
         * 要求sqlString的个数和sqlTableName的个数一致，如果不一致，就不导出sqlString的数据
         *
         */
        int sqlStringNum = sqlStrings.split(";").length;
        int sqlTableNameNum = sqlTableNames.split(";").length;
        int len = 0;
        if (sqlStringNum != sqlTableNameNum)
        {
            len = 0;
        }
        else
        {
            len = sqlStringNum;
        }
        for (int i = 0; i < len; i++) {
            String sqlString = sqlStrings.split(";")[i];
            String sqlTableName = sqlTableNames.split(";")[i];

            if (sqlString == null || sqlString.trim().equals(""))
            {
                continue;
            }

            if (StringUtils.isNotEmpty(sqlString) && StringUtils.isNotEmpty(sqlTableName)) {
                sqlSb.append(DDL2SQLUtils.generateDDLFromSql(connection, sqlString, sqlTableName));
                dataSb.append(DDL2SQLUtils.generateInsertSqlFromSQL(connection, sqlString, sqlTableName));
            }
        }
        //导出表结构
        DDL2SQLUtils.generateFile(exportedDataDir, "struct.sql", sqlSb.toString());
        //导出表数据
        DDL2SQLUtils.generateFile(exportedDataDir, "data.sql", dataSb.toString());
        String struct=exportedDataDir+"/struct.sql";
        String data=exportedDataDir+"/data.sql";
        List<String> fileList=new ArrayList<String>();
        fileList.add(struct);
        fileList.add(data);
        packTaskData(fileList,dataTaskId,request.getSession().getServletContext().getRealPath("/"));
        return request.getSession().getServletContext().getRealPath("/")+"zipFile/";
    }


    //对导出sql文件进行zip打包
    public boolean packTaskData(List<String> fileList,String datataskId,String path) {
        String configFilePath = GetInfoService.class.getClassLoader().getResource("../../WEB-INF/config.properties").getFile();
        String subjectCode= ConfigUtil.getConfigItem(configFilePath, "SubjectCode");
        String fileName = subjectCode+"_"+datataskId+"_sql";
        FileResourceService fileResourceService=new FileResourceService();
        fileResourceService.packDataResource(fileName,fileList,path);
        return true;
    }

    public boolean uploadTaskData(String taskId) {
        return true;
    }

    public boolean importTaskData(String taskId) {
        return true;
    }


    private DataTask getDataTask(String dataTaskId) {
        String sql = "select * from t_datatask where DataTaskId = ?" ;
        JdbcTemplate jdbcTemplate = SqlLiteDataConnection.makeJdbcTemplate();
        List<DataTask> dataTaskList = jdbcTemplate.query(sql, new Object[]{dataTaskId}, new DataTaskMapper());

        if (dataTaskList.size() != 0) {
            return dataTaskList.get(0);
        }
        else {
            return null;
        }
    }

    private DataSrc getDataSource(String dataSrcId)
    {
        String sql = "select * from t_datasource where DataSourceId = ?" ;
        JdbcTemplate jdbcTemplate = SqlLiteDataConnection.makeJdbcTemplate();
        List<DataSrc> dataSrcList = jdbcTemplate.query(sql, new Object[]{dataSrcId}, new DataSrcMapper());

        if (dataSrcList.size() != 0) {
            return dataSrcList.get(0);
        }
        else {
            return null;
        }
    }
}