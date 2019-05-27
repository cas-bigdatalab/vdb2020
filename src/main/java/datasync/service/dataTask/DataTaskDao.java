package datasync.service.dataTask;

import datasync.connection.SqlLiteDataConnection;
import datasync.entity.DataSrc;
import datasync.entity.DataTask;
import datasync.entity.FtpUtil;
import datasync.mapper.DataSrcMapper;
import datasync.mapper.DataTaskMapperDsName;
import datasync.service.login.GetInfoService;
import datasync.service.settingTask.UploadTaskService;
import datasync.utils.ConfigUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class DataTaskDao {

    //添加任务
    public int insertDatatask(final DataTask datatask,String connData,String dataSourceName){
        boolean flag = false;
        final String sql = "insert into t_datatask(dataSourceId,dataTaskName,dataTaskType," +
                "tableName,sqlString,sqlTableNameEn,sqlFilePath,filePath,createTime,creator,status,datataskId,subjectCode,remoteuploadpath) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlLiteDataConnection sqlLiteDataConnection=new SqlLiteDataConnection();
        JdbcTemplate jdbcTemplate=sqlLiteDataConnection.makeJdbcTemplate();
       // final String taskId=String.valueOf(UUID.randomUUID());
      //  final int dataSourceId= (int) System.currentTimeMillis();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, datatask.getDataSourceId());
                ps.setString(2,datatask.getDataTaskName());
                ps.setString(3,datatask.getDataTaskType());
                ps.setString(4,datatask.getTableName());
                ps.setString(5,datatask.getSqlString());
                ps.setString(6,datatask.getSqlTableNameEn());
                ps.setString(7,datatask.getSqlFilePath());
                ps.setString(8,datatask.getFilePath());
                ps.setTimestamp(9,new Timestamp(datatask.getCreateTime().getTime()));
                ps.setString(10,datatask.getCreator());
                ps.setString(11,datatask.getStatus());
                ps.setString(12, datatask.getDataTaskId());
                ps.setString(13,datatask.getSubjectCode());
                ps.setString(14,datatask.getRemoteuploadpath());
                return ps;
            }
        },keyHolder);
        insertDataSrc(datatask.getDataSourceId(),dataSourceName,connData);

        int generatedId = keyHolder.getKey().intValue();

        return generatedId;
    }

    //查询任务列表
    public List<DataTask> getDataTaskList(Map<Object,Object> params){
        StringBuffer sql = new StringBuffer();
        sql.append("select ds.DataSourceName,* from  t_datatask t  LEFT JOIN t_datasource ds ON ds.DataSourceId=t.DataSourceId where 1=1 ");
        //String sql = "select * from t_datatask order  by  DataTaskId desc ";
        if(StringUtils.isNotEmpty((String) params.get("SearchDataTaskName"))) {//任务标识
            sql.append("  and t.dataTaskName like  '%"+params.get("SearchDataTaskName")+"%'");
        }
        if(StringUtils.isNotEmpty((String) params.get("dataSourceList"))) {//数据类型
            if("file".equals((String) params.get("dataSourceList"))){
            sql.append("  and t.dataTaskType = '"+params.get("dataSourceList")+"'");
            }else{
                sql.append("  and t.dataTaskType != 'file'");
            }
        }
        if(StringUtils.isNotEmpty((String) params.get("dataStatusList"))) {//状态
            sql.append("  and t.status = '"+params.get("dataStatusList")+"'");
        }
        sql.append(" order  by   CreateTime desc");
        SqlLiteDataConnection sqlLiteDataConnection=new SqlLiteDataConnection();
        JdbcTemplate jdbcTemplate=sqlLiteDataConnection.makeJdbcTemplate();
        List<DataTask> DataTaskList = jdbcTemplate.query(sql+"", new DataTaskMapperDsName());
       // DataSrc dataSrc=getDataSourceById(DataTaskList);

        return DataTaskList;
    }

    //根据id获取任务对象
    public DataTask getDataTaskInfById(String taskId){
        DataTask dataTask=new DataTask();
        String sql = "select ds.DataSourceName,* from  t_datatask t  LEFT JOIN t_datasource ds ON ds.DataSourceId=t.DataSourceId  where dataTaskId = ?";
        SqlLiteDataConnection sqlLiteDataConnection=new SqlLiteDataConnection();
        JdbcTemplate jdbcTemplate=sqlLiteDataConnection.makeJdbcTemplate();
        List<DataTask> list = jdbcTemplate.query(sql, new Object[]{taskId}, new DataTaskMapperDsName());
        return list.size() > 0 ? list.get(0) : null;
    }

    //根据id删除task
    public int deleteTaskById(String taskId){
        String sql = "delete from t_datatask where  dataTaskId = '"+taskId+"'";
        SqlLiteDataConnection sqlLiteDataConnection=new SqlLiteDataConnection();
        JdbcTemplate jdbcTemplate=sqlLiteDataConnection.makeJdbcTemplate();
        DataTask dataTask=getDataTaskInfById(taskId);
        int result = jdbcTemplate.update(sql);//query(sql, new Object[]{taskId}, new DataTaskMapper());
        File file=new File(dataTask.getSqlFilePath().replace("%_%",File.separator));
        if(file.exists()){
            file.delete();//删除本地文件
        }
        return result;
    }
    //根据id修改task导入状态
    public int updateDataTaskStatusById(String taskId,String status){
        String sql = "update  t_datatask  set  status ='"+status+"' where  dataTaskId = '"+taskId+"'";
        SqlLiteDataConnection sqlLiteDataConnection=new SqlLiteDataConnection();
        JdbcTemplate jdbcTemplate=sqlLiteDataConnection.makeJdbcTemplate();
        int result = jdbcTemplate.update(sql);//query(sql, new Object[]{taskId}, new DataTaskMapper());
        return result;
    }

    public int insertDataSrc(final int dataSourceId, final String dataSourceName, final String connData){
        final String sql = "insert into t_datasource(dataSourceId,dataSourceName,dataSourceType)" +
                "VALUES (?,?,?)";
        SqlLiteDataConnection sqlLiteDataConnection=new SqlLiteDataConnection();
        JdbcTemplate jdbcTemplate=sqlLiteDataConnection.makeJdbcTemplate();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1,dataSourceId);
                ps.setString(2,dataSourceName);
                ps.setString(3,connData);
                return ps;
            }
        },keyHolder);
        int generatedId = keyHolder.getKey().intValue();
        return generatedId;
    }

    public int  updateSqlFilePathById(DataTask dataTask){

        String sql = "update  t_datatask  set  sqlFilePath ='"+dataTask.getSqlFilePath()+"' where  dataTaskId = '"+dataTask.getDataTaskId()+"'";
        SqlLiteDataConnection sqlLiteDataConnection=new SqlLiteDataConnection();
        JdbcTemplate jdbcTemplate=sqlLiteDataConnection.makeJdbcTemplate();
        int result = jdbcTemplate.update(sql);//query(sql, new Object[]{taskId}, new DataTaskMapper());
        return result;
    }

    public DataSrc getDataSourceById(String dataSourceId){
        String sql="select * from t_datasource where t_datasource.DataSourceId=?";
        SqlLiteDataConnection sqlLiteDataConnection=new SqlLiteDataConnection();
        JdbcTemplate jdbcTemplate=sqlLiteDataConnection.makeJdbcTemplate();
        List<DataSrc> list = jdbcTemplate.query(sql, new Object[]{dataSourceId}, new DataSrcMapper());
        return list.size() > 0 ? list.get(0) : null;
    }

    //根据id修改task信息
    public String updateSqlDataInfById(HttpServletRequest req, HttpServletResponse res) throws IOException {
        DataTask dataTask=new DataTask();
        final String datataskId=req.getParameter("dataTaskId");//获取任务名称--id
        String connDataValue=req.getParameter("connDataValue");
        String [] connDataValueArray=connDataValue.split("\\$");
        final String dataTaskType=connDataValueArray[connDataValueArray.length-2];
        final String checkedValue=req.getParameter("checkedValue");
        final String taskSql=req.getParameter("sql");
        final String sqlTableNameEn=req.getParameter("createNewTableName");

        final String sql = "update  t_datatask set  dataTaskType=?,tableName=?,sqlString=?,sqlTableNameEn=? where dataTaskId=?;";
        SqlLiteDataConnection sqlLiteDataConnection=new SqlLiteDataConnection();
        JdbcTemplate jdbcTemplate=sqlLiteDataConnection.makeJdbcTemplate();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
                ps.setString(1,dataTaskType);
                ps.setString(2,checkedValue);
                ps.setString(3,taskSql);
                ps.setString(4,sqlTableNameEn);
                ps.setString(5,datataskId);
                return ps;
            }
        },keyHolder);


        final String dataSourceId=req.getParameter("dataSourceId");
        final String dataSourceName=req.getParameter("dataSourceName");
        final String dataSourceType=req.getParameter("connDataValue");
        updateDataSourceInfById(dataSourceId,dataSourceName,dataSourceType);
        String zipFilePath=uploadTask(req,res,datataskId);//打包

        return "success";
    }

    //根据id修改dataSource信息
    public String updateDataSourceInfById(final String dataSourceId,final String dataSourceName,final String dataSourceType){

        final String sql = "update  t_datasource set  dataSourceName=?,dataSourceType=?  where dataSourceId=?;";
        SqlLiteDataConnection sqlLiteDataConnection=new SqlLiteDataConnection();
        JdbcTemplate jdbcTemplate=sqlLiteDataConnection.makeJdbcTemplate();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
                ps.setString(1,dataSourceName);
                ps.setString(2,dataSourceType);
                ps.setString(3,dataSourceId);
                return ps;
            }
        },keyHolder);

        return "";
    }

    /*
     * 根据taskId完成task的上传任务, 任务上传包括 导出数据、打包数据、上传数据到中心端，中心端导入数据到存放数据的数据库
     *
     */
    public String uploadTask(HttpServletRequest req, HttpServletResponse res,String dataTaskId) throws IOException {
        PrintWriter out = res.getWriter();
        UploadTaskService uploadTaskService = new UploadTaskService();
        String zipFilePath = uploadTaskService.exportDataTask(req, dataTaskId);
        if (zipFilePath!=null || zipFilePath!="")
        {
            try {
                System.out.println("packSuccess"+zipFilePath);
               // res.getWriter().println("packSuccess");
            }
            catch (Exception e)
            {
                res.getWriter().println("packFail");
                e.printStackTrace();
            }
        }
        else
        {
            try {
                res.getWriter().println("packFail");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return zipFilePath;
    }


    public int ftpLocalUpload(HttpServletRequest req, HttpServletResponse res,String taskId) throws IOException {
        PrintWriter out=res.getWriter();
       // String taskId=taskId;
        DataTask dataTask = new DataTaskService().getDataTaskInfById(taskId);
        String processId=dataTask.getDataTaskId();
        String fileName = dataTask.getDataTaskName ()+"log.txt";//文件名及类型
        String path=req.getRealPath("/")+"console/datasync/logFile/";//this.getClass().getResource("").getPath().substring(1, this.getClass().getResource("").getPath().indexOf("WEB-INF"))+"console/datasync/logFile/";
//        String path = "/logs/";
        FileWriter fw = null;
        File file = new File(path, fileName);
        if(!file.exists()){
            try {
                file.createNewFile();
                fw = new FileWriter(file, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            try{
                fw = new FileWriter(file, true);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        PrintWriter pw = new PrintWriter(fw);
        pw.println("日志存放路径"+":"+path + "\n");
        java.util.Date now = new java.util.Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
        String current = dateFormat.format(now);
        pw.println(current+":"+"=========================上传流程开始========================" + "\n");
        if("file".equals(dataTask.getDataTaskType())){
            pw.println("###########上传的文件为###########" + "\n");
            String[] fileAttr = dataTask.getFilePath().split(";");
            for(String fileAttrName : fileAttr){
                pw.println(fileAttrName+ "\n");
            }
        }
        String configFilePath = GetInfoService.class.getClassLoader().getResource("../../WEB-INF/config.properties").getFile();
        String subjectCode= ConfigUtil.getConfigItem(configFilePath, "SubjectCode");
        String host = ConfigUtil.getConfigItem(configFilePath, "FtpHost");// "10.0.86.77";
        String userName = ConfigUtil.getConfigItem(configFilePath, "FtpUser");//"ftpUserssdd";
        String password = ConfigUtil.getConfigItem(configFilePath, "FtpPassword");//"ftpPasswordssdd";
        String port = ConfigUtil.getConfigItem(configFilePath, "FrpPort");//"21";
        String ftpRootPath = "/";
        String portalUrl =ConfigUtil.getConfigItem(configFilePath, "PortalUrl");//"10.0.86.77/portal";
        FtpUtil ftpUtil=new FtpUtil();
        pw.println("数据任务名称为：" + dataTask.getDataTaskName() +"\n");
        try {
            ftpUtil.connect(host, Integer.parseInt(port), userName, password);

            String result = "";
            if(dataTask.getDataTaskType().equals("file")){
                String[] localFileList = {dataTask.getSqlFilePath()};
                result = ftpUtil.upload(localFileList, processId,ftpRootPath,dataTask,subjectCode).toString();
                if(result.equals("File_Exits") || result.equals("Remote_Bigger_Local")){
                    ftpUtil.removeDirectory(ftpRootPath+subjectCode+"_"+dataTask.getDataTaskId());
                    ftpUtil.deleteFile(ftpRootPath+subjectCode+"_"+dataTask.getDataTaskId()+".zip");
                    result = ftpUtil.upload(localFileList, processId,ftpRootPath,dataTask,subjectCode).toString();
                }
                if(localFileList.length == 0){
                    now = new java.util.Date();
                    dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
                    String current1 = dateFormat.format(now);
                    pw.println(current1+":"+"上传失败"+ "\n");
                    return 0;
                }
            }else if("mysql".equals(dataTask.getDataTaskType()) || "oracle".equals(dataTask.getDataTaskType())){
                String remoteFilepath = ftpRootPath+subjectCode+"_"+dataTask.getDataTaskId()+"_sql/";
                String[] localFileList = {dataTask.getSqlFilePath()};
                result = ftpUtil.upload(localFileList, processId,remoteFilepath,dataTask,subjectCode+"_sql").toString();
                if(result.equals("File_Exits") || result.equals("Remote_Bigger_Local")){
//                    ftpUtil.removeDirectory(ftpRootPath+subjectCode+"_"+dataTask.getDataTaskId()+"_sql");
                    ftpUtil.removeDirectory(subjectCode+"_"+dataTask.getDataTaskId()+"_sql");
                    ftpUtil.deleteFile(dataTask.getDataTaskId()+".zip");
                    result = ftpUtil.upload(localFileList, processId,remoteFilepath,dataTask,subjectCode).toString();
                }
                if(localFileList.length == 0){
                    now = new java.util.Date();
                    dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
                    String current1 = dateFormat.format(now);
                    pw.println(current1+":"+"上传失败"+ "\n");
                    return 0;
                }
            }

            pw.println("ftpDataTaskId"+dataTask.getDataTaskId()+"上传状态:" + result + "\n");

            if(result.equals("Upload_New_File_Success")||result.equals("Upload_From_Break_Succes")){
                System.out.println("开始调用解压！");
                String dataTaskString = com.alibaba.fastjson.JSONObject.toJSONString(dataTask);
                com.alibaba.fastjson.JSONObject requestJSON = new com.alibaba.fastjson.JSONObject();
                requestJSON.put("dataTask",dataTaskString);
                requestJSON.put("subjectCode",subjectCode);
                String requestString = com.alibaba.fastjson.JSONObject.toJSONString(requestJSON);
                HttpClient httpClient = null;
                HttpPost postMethod = null;
                HttpResponse response = null;
                try {
                    if("mysql".equals(dataTask.getDataTaskType()) || "oracle".equals(dataTask.getDataTaskType())){
                        now = new java.util.Date();
                        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
                        String current1 = dateFormat.format(now);
                        pw.println(current1+":"+"=========================导入流程开始========================" + "\n");
                    }
                    if("file".equals(dataTask.getDataTaskType())){
                        now = new java.util.Date();
                        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
                        String current1 = dateFormat.format(now);
                        pw.println(current1+":"+"=========================解压流程开始========================" + "\n");
                    }
                    httpClient = HttpClients.createDefault();
//                    postMethod = new HttpPost("http://localhost:8080/portal/service/getDataTask");
                    postMethod = new HttpPost("http://"+portalUrl+"/service/getDataTask");
//                    postMethod = new HttpPost(portalUrl);
                    postMethod.addHeader("Content-type", "application/json; charset=utf-8");
//                    postMethod.addHeader("X-Authorization", "AAAA");//设置请求头
                    postMethod.setEntity(new StringEntity(requestString, Charset.forName("UTF-8")));
                    response = httpClient.execute(postMethod);//获取响应
                    int statusCode = response.getStatusLine().getStatusCode();
                    System.out.println("HTTP Status Code:" + statusCode);
                    if (statusCode != HttpStatus.SC_OK) {
                        System.out.println("HTTP请求未成功！HTTP Status Code:" + response.getStatusLine());
                    }
                    HttpEntity httpEntity = response.getEntity();
                    String reponseContent = EntityUtils.toString(httpEntity);
                    EntityUtils.consume(httpEntity);//释放资源
                    System.out.println("响应内容："  + reponseContent);
                    if(reponseContent.equals("1")){
                        if("mysql".equals(dataTask.getDataTaskType()) || "oracle".equals(dataTask.getDataTaskType())){
                            now = new java.util.Date();
                            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
                            String current1 = dateFormat.format(now);
                            pw.println(current1+":"+"导入成功"+ "\n");
                            pw.println(current1+":"+"=========================导入流程结束========================" + "\n");
                        }
                        if("file".equals(dataTask.getDataTaskType())){
                            now = new java.util.Date();
                            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
                            String current1 = dateFormat.format(now);
                            pw.println(current1+":"+"解压成功"+ "\n");
                            pw.println(current1+":"+"=========================解压流程结束========================" + "\n");
                        }
                        ftpUtil.deleteFile(ftpRootPath+subjectCode+"_"+dataTask.getDataTaskId()+".zip");
                        dataTask.setStatus("1");
                        updateDataTaskStatusById(taskId,"1");
                        ftpUtil.numberOfRequest.remove(taskId+"Block");
                        ftpUtil.progressMap.put(taskId,Long.valueOf(100));
                        out.println(1);

                        return 1;
                    }else{
                        if("mysql".equals(dataTask.getDataTaskType()) || "oracle".equals(dataTask.getDataTaskType())){
                            now = new java.util.Date();
                            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
                            String current1 = dateFormat.format(now);
                            pw.println(current1+":"+"导入失败"+ "\n");
                            pw.println(current1+":"+"=========================导入流程结束========================" + "\n");
                        }
                        if("file".equals(dataTask.getDataTaskType())){
                            now = new java.util.Date();
                            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
                            String current1 = dateFormat.format(now);
                            pw.println(current1+":"+"解压失败"+ "\n");
                            pw.println(current1+":"+"=========================解压流程结束========================" + "\n");
                        }
                        out.println(3);
                        return 3;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if("mysql".equals(dataTask.getDataTaskType()) || "oracle".equals(dataTask.getDataTaskType())){
                        now = new java.util.Date();
                        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
                        String current1 = dateFormat.format(now);
                        pw.println(current1+":"+"导入失败"+ e+"\n");
                        pw.println(current1+":"+"=========================导入流程结束========================" + "\n");
                    }
                    if("file".equals(dataTask.getDataTaskType())){
                        now = new java.util.Date();
                        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
                        String current1 = dateFormat.format(now);
                        pw.println(current1+":"+"解压失败"+ e+"\n");
                        pw.println(current1+":"+"=========================解压流程结束========================" + "\n");
                    }
                }
            }else{
                out.println(0);
                return 0;
            }
        } catch (IOException e) {
            if(ftpUtil.pauseTasks.get(taskId)!=null && ftpUtil.pauseTasks.get(taskId)!="") {//点击暂停时触发
                ftpUtil.disconnect();
                pw.println(current+":"+"暂停传输:"+e+ "\n");
                System.out.println("暂停传输！" );
                ftpUtil.numberOfRequest.remove(taskId+"Block");

                ftpUtil.pauseTasks.remove(taskId);
                updateDataTaskStatusById(taskId,"0");
                out.println(4);
                return 4;//暂停
            }else{
                ftpUtil.disconnect();
                now = new java.util.Date();
                dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
                current = dateFormat.format(now);
                pw.println(current+":"+"连接FTP出错:"+e+ "\n");
                out.println("连接FTP出错：" + e.getMessage()+";"+dataTask.getDataTaskId());
                for(Iterator<Map.Entry<String, String>> it = ftpUtil.numberOfRequest.entrySet().iterator(); it.hasNext();){
                    Map.Entry<String, String> item = it.next();
                    it.remove();
                }
                System.out.println("连接FTP出错：" + e.getMessage());
                return 3;
            }
        }finally {
            if( ftpUtil.numberOfRequest.get(taskId+"Block")!=null){
                ftpUtil.numberOfRequest.remove(taskId+"Block");
            }
            ftpUtil.disconnect();
            now = new Date();
            dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
            String current1 = dateFormat.format(now);
            pw.println(current1+":"+"=========================上传流程结束========================" + "\r\n"+"\n\n\n\n\n");
            try {
                fw.flush();
                pw.close();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        out.println(1);
        return 1;
    }
}
