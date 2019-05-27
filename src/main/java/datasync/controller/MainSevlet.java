package datasync.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import datasync.entity.DataTask;
import datasync.entity.FileTreeNode;
import datasync.entity.FtpUtil;
import datasync.service.FileResourceDao;
import datasync.service.FileResourceService;
import datasync.service.dataNodeInf.AchieveFtpConfigInf;
import datasync.service.dataTask.DataTaskDao;
import datasync.service.dataTask.DataTaskService;
import datasync.service.login.GetInfoService;
import datasync.service.settingTask.DataConnDaoService;
import datasync.service.settingTask.LocalConnDaoService;
import datasync.utils.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vdb.mydb.filestat.impl.LocalRepository;
import vdb.mydb.filestat.impl.RepositoriesService;
import vdb.mydb.repo.FileRepository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainSevlet extends HttpServlet{

    private Logger logger = LoggerFactory.getLogger(MainSevlet.class);
    private  FtpUtil ftpUtil=new FtpUtil();
    public static BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(20);
    //创建线程池，池中保存的线程数为5，允许的最大线程数为20
    static ThreadPoolExecutor pool = new ThreadPoolExecutor(5,20,50,TimeUnit.MILLISECONDS, queue);
    private FileResourceService fileResourceService=new FileResourceService();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

       HttpSession session=req.getSession();

        //获取请求路径
        String path = req.getServletPath();
        if ("/searchDataList.do".equals(path)){
            //获取数据库列表
            searchDataList(req,res);
        }else if("/searchTables.do".equals(path)){
            try {
                //查询数据库下表单
                searchTables(req, res);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }else if("/searchBdDirList.do".equals(path)){
            //查询本地数据源列表
            searchBdDirList(req, res);
        }else if("/searchBdDirListPath.do".equals(path)) {
            searchBdDirListPath(req, res);
        }else if("/getTreeOfDirList.do".equals(path)) {
            getTreeOfDirList(req, res);
        }else if("/searchDataBySql.do".equals(path)){
            try {
                searchDataBySql(req,res);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }else if("/submitSqlData.do".equals(path)){
            //新建任務（提交）
            try {
                submitSqlData(req,res);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else if("/searchDataTaskList.do".equals(path)){
            //查询任务列表
            searchDataTaskList(req,res);
        }else if("/submitFileData.do".equals(path)){
            try {
                submitFileData(req,res);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }else if("/ftpLocalUpload.do".equals(path)){
            ftpLocalUpload(req,res);
        }else if ("/searchTaskDetailById.do".equals(path)){
            searchTaskDetailById(req,res);
        }else if("/deleteTaskById.do".equals(path)){
            deleteTaskById(req,res);
        }else if("/ftpUploadProcess.do".equals(path)){
            //实时加载上传进度
            ftpUploadProcess(req,res);
        }else if("/achieveDataNodeInf.do".equals(path)){
            achieveDataNodeInf(req,res);
        }else if("/updateSqlData.do".equals(path)){
            //根据id修改数据库task
            updateSqlData(req, res);
        }else if("/updateLocalTaskData.do".equals(path)){
            //根据id修改本地task
            updateLocalTaskData(req, res);
        }else if("/getPackProcess.do".equals(path)){
            getPackProcess(req, res);
        }else if ("/pauseUpLoading.do".equals(path)){
            pauseUpLoading(req, res);
        }else if("/asyncGetNodes.do".equals(path)){
            asyncGetNodes(req,res);
        }else if("/getEditTreeOfDirList.do".equals(path)){
            getEditTreeOfDirList(req,res);
        }
        else{
            //错误路径
            throw new RuntimeException("查无此页");
        }
    }

    /**
     * 根据前端传过来的本地数据源获取它的文件列表
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    public String getTreeOfDirList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out =response.getWriter();
        JSONObject jsonObject = new JSONObject();
        String localDataSource = request.getParameter("localDataSource");
        RepositoriesService repositoriesService=new RepositoriesService();//getAllRepositories
        List<FileRepository> localFileRepositories = repositoriesService.getAllRepositories(localDataSource);
        String data="";
        List<FileTreeNode> list=new ArrayList<FileTreeNode>();
        if (1 != localFileRepositories.size())
        {
            for(int j = 0;j < localFileRepositories.size() - 1; j++)
            {
                String localFilePath = ((LocalRepository)(localFileRepositories.get(j))).getPath();
                list.addAll(new FileResourceDao().asynLoadingTree("",localFilePath,"init"));
            }
        }

        String remoteFileStr=new FileResourceDao().LoadingRemoteTree();////获取远程上传目录
        jsonObject.put("list",list);
        jsonObject.put("remoteFileStr",remoteFileStr);
        out.println(jsonObject);

        return data;
    }

    private String getLocalFileTreeData(File localFile)
    {
        String retStr = "";
        if (!localFile.exists()) {
            retStr = "";
        }

        String localFileAbsolutePath = localFile.getAbsolutePath().replaceAll("\\\\", "/");
        System.out.println("localFileAbsolutePath = " + localFileAbsolutePath);

        if (localFile.isFile())
        {
            retStr = "{ \"text\" : \"" + localFileAbsolutePath + "\"," +
                    " \"icon\" : \"jstree-file\"}";
        }

        if (localFile.isDirectory())
        {
            retStr =  "{ \"text\" : \"" + localFileAbsolutePath + "\"," +
                    " \"icon\" : \"jstree-folder\"," +
                    " \"children\" : [";
            File[] subFiles = localFile.listFiles();

            for (int i = 0; i < subFiles.length; i++)
            {
                if (i < subFiles.length - 1) {
                    retStr += getSubTreeData(subFiles[i]) + ",";
                }
                else
                {
                    retStr += getSubTreeData(subFiles[i]);
                }
            }
            retStr += "]}";
        }

        return retStr;
    }

    private String getSubTreeData(File localFile)
    {
        String retStr = "";
        if (!localFile.exists()) {
            retStr = "";
        }

        if (localFile.isFile())
        {
            retStr = "{ \"text\" : \"" + localFile.getName() + "\"," +
                    " \"icon\" : \"jstree-file\"}";
        }

        if (localFile.isDirectory())
        {
            retStr =  "{ \"text\" : \"" + localFile.getName() + "\"," +
                    " \"icon\" : \"jstree-folder\"," +
                    " \"children\" : [";
            File[] subFiles = localFile.listFiles();

            for (int i = 0; i < subFiles.length; i++)
            {
                if (i < subFiles.length - 1) {
                    retStr += getSubTreeData(subFiles[i]) + ",";
                }
                else
                {
                    retStr += getSubTreeData(subFiles[i]);
                }
            }
            retStr += "]}";
        }

        return retStr;
    }

    //设置任务---数据库---查询数据库名称列表
    public List<Object> searchDataList(HttpServletRequest req, HttpServletResponse res) throws IOException {//获取数据库LIST
        PrintWriter out = res.getWriter();
        DataConnDaoService dataConnDaoService=new DataConnDaoService();
        List<Object> list=dataConnDaoService.searchDataListImp(req, res);
        out.println(list);
        return list;
    }

    //获取任务---本地上传--获取本地数据源列表
    public List<Object> searchBdDirList(HttpServletRequest req, HttpServletResponse res) throws IOException {
        LocalConnDaoService localConnDaoService=new LocalConnDaoService();
        List<Object> list=localConnDaoService.searchBdDirListImp(req, res);
        PrintWriter out = res.getWriter();
        out.println(list);
        return list;
    }

    //设置任务---本地上传---获取本地数据源数据路径（路径/文件）
    public List<Object> searchBdDirListPath(HttpServletRequest req, HttpServletResponse res) throws IOException {
        LocalConnDaoService localConnDaoService=new LocalConnDaoService();
        JSONObject jsonObject = new JSONObject();
        List<Object> list=localConnDaoService.searchBdDirListPathImp(req, res);
        PrintWriter out = res.getWriter();
        jsonObject.put("list",list);
        out.println(jsonObject);
        return list;
    }

    //设置任务---根据数据库查询表名称
    public String  searchTables(HttpServletRequest request, HttpServletResponse response) throws SQLException, ClassNotFoundException, IOException {
        PrintWriter out = response.getWriter();
        DataConnDaoService dataConnDaoService=new DataConnDaoService();
        String dataType=dataConnDaoService.judgeDataType(request.getParameter("connData"));
        List<Object> list=new ArrayList<Object>();
        try {
            if ("mysql".equals(dataType)){
                list=dataConnDaoService.getMysqlTablesNameList(request.getParameter("connData"));
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("tableList",list);
                out.println(jsonObject);
            }else  if ("oracle".equals(dataType)){
                list=dataConnDaoService.getOracleTablesNameList(request.getParameter("connData"));
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("tableList",list);
                out.println(jsonObject);
            }else if("sqlServer".equals(dataType)){
                list=dataConnDaoService.getSqlServerTablesNameList(request.getParameter("connData"));
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("tableList",list);
                out.println(jsonObject);
            }
        } catch (SQLException e) {
            out.println("数据库连接异常！");
            e.printStackTrace();
        }
        return "success!";
    }

    //设置任务中---根据sql查询预览结果
    public List<Map<Object,Object>> searchDataBySql(HttpServletRequest req, HttpServletResponse res) throws SQLException, ClassNotFoundException, IOException {
        PrintWriter out = res.getWriter();
        Map<Object,Object> columnsMap=new HashMap<Object, Object>();
        String sql=req.getParameter("sql");//获取sql
        String connData=req.getParameter("connData");//获取数据库连接信息
        List<Map<Object,Object>> list =new ArrayList<Map<Object, Object>>();
        try {
            list=new DataConnDaoService().searchDataBySql(connData,sql);
            List<Map<Object,Object>> columnsListMap=new ArrayList<Map<Object, Object>>();
            columnsMap=list.get(list.size()-1);
            //columnsListMap.add(columnsMap.get("columnsList"));
            list.remove(list.size()-1);
            String dataList=list+"?*$*?"+(columnsMap.get("columnsList"));
            out.println(dataList.replaceAll("\"=\"","\":\""));
        } catch (SQLException e) {
            out.println("sql语句错误！");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return list;
    }

   //新建数据库任务
    public JSONObject submitSqlData(HttpServletRequest res, HttpServletResponse req) throws SQLException, IOException {
        String configFilePath = GetInfoService.class.getClassLoader().getResource("../../WEB-INF/config.properties").getFile();
        String subjectCode= ConfigUtil.getConfigItem(configFilePath, "SubjectCode");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date=new Date();
        JSONObject jsonObject = new JSONObject();
        DataTask datatask = new DataTask();
        HttpSession session=res.getSession();
        String datataskId=res.getParameter("dataTaskName");//获取任务名称--id
        String connDataValue=res.getParameter("connDataValue");
        String [] connDataValueArray=connDataValue.split("\\$");
        String dataSourceName=res.getParameter("connDataName");
        // String dataTaskName=res.getParameter("dataTaskName");//获取任务名称--id
        int dataSourceId= (int) System.currentTimeMillis();
        // datatask.setDataSourceId(5);
        datatask.setDataTaskId(datataskId);
        datatask.setDataTaskName(res.getParameter("taskName"));//任务名
        datatask.setTableName(res.getParameter("checkedValue"));//选择表的名称
        datatask.setSqlString(res.getParameter("sql"));//SQL语句
        datatask.setSqlTableNameEn(res.getParameter("createNewTableName"));//新建表名
        datatask.setCreateTime(date);
        datatask.setDataSourceId(dataSourceId);//(res.getParameter("connDataName"));//数据源名称
        if(connDataValueArray.length>2){
           datatask.setDataTaskType(connDataValueArray[connDataValueArray.length-2]);//数据源类型
        }
        datatask.setCreator(session.getAttribute("SPRING_SECURITY_LAST_USERNAME")==null?"": (String) session.getAttribute("SPRING_SECURITY_LAST_USERNAME"));
        datatask.setStatus("0");
        String reslut="";
        if(res.getParameter("sql")!="" && res.getParameter("sql")!=null){
            reslut=new DataConnDaoService().checkSql(res.getParameter("sql"),connDataValue);
            PrintWriter out=req.getWriter();
            if(!"success".equals(reslut)){
                out.println(reslut);
                return jsonObject;
            }
        }
        int flag = new DataTaskService().insertDatatask(datatask,connDataValue,dataSourceName);
        String zipFilePath=new DataTaskService().uploadTask(res, req,datataskId);//打包
        String fileName = subjectCode +"_"+datataskId+"_sql.zip";
        datatask.setSqlFilePath((zipFilePath+fileName).replace(File.separator,"%_%"));
        //datatask.setSqlFilePath(fileName);
        int flag1= new DataTaskService().updateSqlFilePathById(datatask);
        jsonObject.put("result",flag);
        if(flag < 0){
            return  jsonObject;
        }
        PrintWriter printWriter=req.getWriter();
        printWriter.println("success");
        return jsonObject;
    }

    //数据任务---获取任务列表
    public JSONObject searchDataTaskList(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String SearchDataTaskName=req.getParameter("SearchDataTaskName");//查询条件
        String dataSourceList=req.getParameter("dataSourceList");//查询条件
        String dataStatusList=req.getParameter("dataStatusList");//查询条件
        Map<Object,Object> params=new HashMap<Object, Object>();
        params.put("SearchDataTaskName",SearchDataTaskName);
        params.put("dataSourceList",dataSourceList);
        params.put("dataStatusList",dataStatusList);
        PrintWriter out = res.getWriter();
        JSONObject jsonObject = new JSONObject();
        String connData="";
        List<DataTask> dataTasks = new DataTaskService().getDataTaskList(params);
        List<Map<Object,Object>> taskProcessList = new ArrayList<Map<Object,Object>>();
        List<Map<Object,Object>> requestList = new ArrayList<Map<Object,Object>>();
        Map<Object,Object> map=new HashMap<Object, Object>();
        Map<Object,Object> requestMap=new HashMap<Object, Object>();
       // FtpUtil fileUtil=new FtpUtil();
        if(dataTasks.size()!=0){
            for(int i=0;i<dataTasks.size();i++){
                Object process =  ftpUtil.getFtpUploadProcess(dataTasks.get(i).getDataTaskId());
                map.put(dataTasks.get(i).getDataTaskId(),process);
                System.out.println(process);
            }
        }

        for (String in : ftpUtil.numberOfRequest.keySet()) {
               String value = ftpUtil.numberOfRequest.get(in);//得到每个key多对用value的值
                requestMap.put(in,value);
            }

        taskProcessList.add(map);
        requestList.add(requestMap);
        jsonObject.put("dataTasks",dataTasks);
        jsonObject.put("taskProcessList",taskProcessList);
        jsonObject.put("requestList",requestList);
        res.setContentType("text/plain");
        res.setContentType("application/json");
        out.println(jsonObject);
        return jsonObject;
    }

    //新建任务--本地文件上传任务
    public  JSONObject submitFileData(HttpServletRequest req, HttpServletResponse res) throws SQLException, IOException {
        String configFilePath = GetInfoService.class.getClassLoader().getResource("../../WEB-INF/config.properties").getFile();
        String subjectCode= ConfigUtil.getConfigItem(configFilePath, "SubjectCode");
   //   PrintWriter out = res.getWriter();
        JSONObject jsonObject = new JSONObject();
        DataTask datatask = new DataTask();
    //  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date=new Date();
        int dataSourceId= (int) System.currentTimeMillis();
        HttpSession session=req.getSession();
        String  connDataName=req.getParameter("connDataName");//本地连接名称
        String connDataValue=req.getParameter("connDataValue");//数据源名称
        String uploadRemoteFile=req.getParameter("getRemoteFile");//获取上传路径
//        String  getCheckedFile=req.getParameter("getCheckedFile");//文件路径
        String checkedValue=req.getParameter("getCheckedFile");//获取选中文件或者路径
        String getLocalTaskName=req.getParameter("dataTaskName");//任務id
        String datataskId=req.getParameter("dataTaskName");//任務id
        datatask.setCreateTime(date);
        datatask.setDataSourceId(dataSourceId);//数据源名称
        datatask.setDataTaskName(getLocalTaskName);//任务名
        datatask.setFilePath(checkedValue);
        datatask.setDataTaskType("file");
        datatask.setStatus("0");
        datatask.setDataTaskId(req.getParameter("dataTaskName"));
        datatask.setRemoteuploadpath(uploadRemoteFile);
        datatask.setCreator(session.getAttribute("SPRING_SECURITY_LAST_USERNAME")==null?"": (String) session.getAttribute("SPRING_SECURITY_LAST_USERNAME"));

        List<String> filepaths =new LinkedList<String>();
        String [] filepathArray=checkedValue.split(";");
          for (String str:filepathArray){
              ((LinkedList<String>) filepaths).add(str);
          }
        String fileName = subjectCode+"_"+datataskId;
        FileResourceService fileResourceService=new FileResourceService();
        fileResourceService.packDataResource(fileName,filepaths,req.getSession().getServletContext().getRealPath("/"));
        String zipFile =req.getSession().getServletContext().getRealPath("/") + "zipFile" + File.separator + fileName + ".zip";
        DataTaskService dataTaskService=new DataTaskService();
        datatask.setSqlFilePath(zipFile.replace(File.separator,"%_%"));
        //boolean upresult = dataTaskService.update(dt);
        int flag = new DataTaskService().insertDatatask(datatask,connDataValue,connDataName);
        return jsonObject;
    }

    //上传文件到FTP
    public  int ftpLocalUpload( HttpServletRequest req, HttpServletResponse res) throws IOException {
        String taskId=req.getParameter("taskId");
        new DataTaskDao().updateDataTaskStatusById(taskId,"0");//修改任务状态
        ftpUtil.numberOfRequest.put(taskId+"Block",taskId);//存放请求
        Long process= Long.valueOf(0);
        ftpUtil.setProgressMap(taskId,process);//初始化进度
        new DataTaskDao().ftpLocalUpload(req,res,req.getParameter("taskId"));
        return 1;
    }

    //通过id查询task信息
    public JSONObject searchTaskDetailById(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String taskId=req.getParameter("taskId");
        PrintWriter out = res.getWriter();
        JSONObject jsonObject = new JSONObject();
        DataTask dataTask = new DataTaskService().getDataTaskInfById(taskId);
        String filePathStr="";
        if(dataTask.getFilePath()!=null && dataTask.getFilePath()!=""){
            String[] filePathArray=dataTask.getFilePath().split(";");
            for(String filePath:filePathArray){
                File dirFile = new File(filePath);//获取文件第一层
                if(dirFile.isFile()){
                    filePathStr=filePathStr+dirFile+";";
                }
            }
        }
        dataTask.setFilePath(filePathStr.replaceAll("\\\\","/"));
        jsonObject.put("dataTask",dataTask);
        out.println(jsonObject);
        return  jsonObject;
    }

    //通过id删除task
    public int deleteTaskById(HttpServletRequest req, HttpServletResponse res) throws IOException {
        PrintWriter out = res.getWriter();
        String taskId=req.getParameter("taskId");
        int result = new DataTaskService().deleteTaskById(taskId);
        out.println(result);
        return result;
    }

    //获取上传进度
    public Long ftpUploadProcess(HttpServletRequest req, HttpServletResponse res) throws IOException {
        PrintWriter out=res.getWriter();
        //FtpUtil ftpUtil =new FtpUtil();
        JSONObject jsonObject = new JSONObject();
        List<Long> processList=new ArrayList<Long>();
        List<String> blockList=new ArrayList<String>();
        String processId=req.getParameter("processId");
        for(String value:ftpUtil.numberOfRequest.values()){
            blockList.add(value);
        }
        Long process =  ftpUtil.getFtpUploadProcess(processId);
        processList.add(process);
        jsonObject.put("process",processList);
        jsonObject.put("blockList",blockList);
        out.println(jsonObject);
        return process;
    }

    //获取文件压缩进度
    public Object getPackProcess(HttpServletRequest req, HttpServletResponse res) throws IOException {
        PrintWriter out=res.getWriter();
        Map<Object,Object> map=new HashMap<Object, Object>();
        List<Object> list=new ArrayList<Object>();
        JSONObject jsonObject=new JSONObject();
        FileResourceService fileResourceService=new FileResourceService();
        Object process =  fileResourceService.getPackProcess(req.getParameter("processId"));
        Object fileName=fileResourceService.getPackProcess(req.getParameter("processId")+"1");
        list.add(process);
        list.add(fileName);
        jsonObject.put("list",list);
        out.println(jsonObject);
        return process;
    }

    //获取节点信息
    public void achieveDataNodeInf(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session=req.getSession();
        PrintWriter out=res.getWriter();
        AchieveFtpConfigInf achieveFtpConfigInf=new AchieveFtpConfigInf();
        List<String> list=new ArrayList<String>();
        JSONObject jsonObject=new JSONObject();
        String subjectName=achieveFtpConfigInf.getConfigInf("SubjectName");//专业库名称
        String subjectCode=achieveFtpConfigInf.getConfigInf("SubjectCode");//专业库代码
        String userName= (String) session.getAttribute("SPRING_SECURITY_LAST_USERNAME");//当前登录用户
        String brief=achieveFtpConfigInf.getConfigInf("Brief");//描述
        if(subjectName==null){
            subjectName="";
        }
        if(brief==null){
            brief="";
        }

        list.add(subjectName);
        list.add(subjectCode);
        list.add(userName);
        list.add(brief);

        jsonObject.put("DataNodeInf",list);
        out.println(jsonObject);
    }

    //通過id修改task信息
    public void updateSqlData(HttpServletRequest req, HttpServletResponse res) throws IOException {
        PrintWriter printWriter=res.getWriter();
        DataTaskService dataTaskService=new DataTaskService();
        String result="";
        String connDataValue=req.getParameter("connDataValue");
        if(req.getParameter("sql")!="" && req.getParameter("sql")!=null){
            String reslut1=new DataConnDaoService().checkSql(req.getParameter("sql"),connDataValue);
            if(!"success".equals(reslut1)){
                printWriter.println(reslut1);
            }
        }
        try {
            result=dataTaskService.updateSqlDataInfById(req,res);
        } catch (Exception e) {
            printWriter.println("fail!");
            e.printStackTrace();
        }
        printWriter.println(result);
    }

    //获取--树--数据源
   public List<Object> getJobTree(String path,List<Object> list){
       System.out.println("服务器系统:"+System.getProperties().getProperty("os.name"));
       String systemName=System.getProperties().getProperty("os.name");
       int isWindows=systemName.indexOf("Windows");
       // List<Object> list=new ArrayList<Object>();//递归获取文件
        List<Object> fileList=new ArrayList<Object>();//递归获取文件
        File dirFile = new File(path);//获取文件第一层
        File[] fs = dirFile.listFiles();
       list.add("{ id:\""+path+"\", pId:0, name:\""+path+"\", open:true,checked:false}");
       for (int i = 0; i < fs.length; i++) {
           if(fs[i].isFile()){//当对象为文件时
               boolean checked=false;
               String pidStr="";
               if("-1".equals(isWindows+"")){//linux
                    pidStr=fs[i].toString().substring(0,fs[i].toString().lastIndexOf("/"));
               }else {
                    pidStr=fs[i].toString().substring(0,fs[i].toString().lastIndexOf("\\"));
               }
               list.add("{ id:\""+fs[i].toString()+"\", pId:\""+pidStr+"\", name:\""+fs[i].getName()+"\", open:true,checked:\""+checked+"\"}");
               System.out.println();
           }else if(fs[i].isDirectory()){//当对象为路径时
               boolean checked=false;
               String pidStr="";
               if("-1".equals(isWindows+"")){//linux
                   pidStr=fs[i].toString().substring(0,fs[i].toString().lastIndexOf("/"));
               }else {
                   pidStr=fs[i].toString().substring(0,fs[i].toString().lastIndexOf("\\"));
               }
               list.add("{ id:\""+fs[i].toString()+"\", pId:\""+pidStr+"\", name:\""+fs[i].getName()+"\", open:false,checked:\""+checked+"\"}");
               List<Object> listStr=new ArrayList<Object>();//递归获取文件
               fileList=getFileList( fs[i].toString(),listStr);
               for(Object o:fileList){
                  list.add(o);
               }
           }
       }
        return list;
   }

   public List<Object> getFileList(String path, List<Object> list){
       String systemName=System.getProperties().getProperty("os.name");
       int isWindows=systemName.indexOf("Windows");
       File dirFile = new File(path);//获取文件第一层
       File[] fs = dirFile.listFiles();
       for(int i=0; i < fs.length; i++){
           if(fs[i].isDirectory()){
               boolean checked=false;
               String pidStr="";
               if("-1".equals(isWindows+"")){//linux
                   pidStr=fs[i].toString().substring(0,fs[i].toString().lastIndexOf("/"));
               }else {
                   pidStr=fs[i].toString().substring(0,fs[i].toString().lastIndexOf("\\"));
               }
               list.add("{ id:\""+fs[i].toString()+"\", pId:\""+pidStr+"\", name:\""+fs[i].getName()+"\", open:false,checked:\""+checked+"\"}");
               getFileList(fs[i].toString(),list);
           }else if (fs[i].isFile()){
               boolean checked=false;
               String pidStr="";
               if("-1".equals(isWindows+"")){//linux
                   pidStr=fs[i].toString().substring(0,fs[i].toString().lastIndexOf("/"));
               }else {
                   pidStr=fs[i].toString().substring(0,fs[i].toString().lastIndexOf("\\"));
               }
               list.add("{ id:\""+fs[i].toString()+"\", pId:\""+pidStr+"\", name:\""+fs[i].getName()+"\", open:false,checked:\""+checked+"\"}");
           }
       }
        return list;
   }

    /**
     * 修改本地上传任务
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    public void updateLocalTaskData(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out=response.getWriter();
        Long process= Long.valueOf(0);
        ftpUtil.setProgressMap(request.getParameter("dataTaskId"),process);//初始化进度
        try {
            String result=new LocalConnDaoService().updateLocalTaskDataById(request,response);
        } catch (Exception e) {
            out.println("fail");
            return;
        }
        out.println("success");

    }

    public void pauseUpLoading(HttpServletRequest request, HttpServletResponse response){
        String taskId=request.getParameter("taskId");
        try {
            ftpUtil.ftpOutputStream.get(taskId).flush();
            ftpUtil.ftpOutputStream.get(taskId).close();
            ftpUtil.pauseTasks.put(taskId,taskId);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("暂停异常！");
        }
    }

    public List<FileTreeNode> asyncGetNodes(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out=response.getWriter();
        List<FileTreeNode> nodeList = new ArrayList<FileTreeNode>();
        String id=request.getParameter("id");
        nodeList=new FileResourceDao().asynLoadingTree("",id,"false");
        out.println(JSON.toJSONString(nodeList));
        return nodeList;
    }

    public JSONObject getEditTreeOfDirList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out=response.getWriter();
        String datataskId=request.getParameter("dataTaskId");
        JSONObject jsonObject = new JSONObject();
        List<FileTreeNode> nodeList=new ArrayList<FileTreeNode>();
        DataTask dataTask = new DataTaskService().getDataTaskInfById(datataskId);
        String jsonObjectStr=null;
//        DataSrcService dataSrcService=new DataSrcService();
        //    String filePath =new FileSourceController().fileSourceFileList(datataskId)；
        String localDataSource = request.getParameter("localDataSource");
        RepositoriesService repositoriesService=new RepositoriesService();//getAllRepositories
        List<FileRepository> localFileRepositories = repositoriesService.getAllRepositories(localDataSource);

        if("file".equals(dataTask.getDataTaskType())){
            String [] checkedFilePath=dataTask.getFilePath().split(";");

            for(int i=0;i<checkedFilePath.length;i++){
                File dirFile = new File(checkedFilePath[i]);
                if(dirFile.isDirectory()){
                    if(i==0){
                        nodeList.add(new FileTreeNode(checkedFilePath[i],"0",checkedFilePath[i],"true","true","false"));
                    }
                    nodeList=new FileResourceDao().loadingTree(checkedFilePath[i],nodeList);
                }
            }

            if (1 != localFileRepositories.size())
            {
                for(int j = 0;j < localFileRepositories.size() - 1; j++)
                {
                    String localFilePath = ((LocalRepository)(localFileRepositories.get(j))).getPath();
                    if(!localFilePath.equals(checkedFilePath[0])){
                        nodeList.addAll(new FileResourceDao().asynLoadingTree("",localFilePath,"init"));
                    }
                }
            }

            for(int i=0;i<checkedFilePath.length;i++){
                for(int j=0;j<nodeList.size();j++){
                    if(checkedFilePath[i].equals(nodeList.get(j).getId())){
                        nodeList.get(j).setChecked("true");
                    }
                }
            }

            String remoteFilePath=fileResourceService.LoadingRemoteTree();

//            try {
//                jsonObjectStr=fileResourceService.LoadingRemoteTree();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            jsonObject.put("datatask",dataTask);
            jsonObject.put("nodeList",nodeList);
            jsonObject.put("jsonObjectStr",jsonObjectStr);
            jsonObject.put("remoteFilePath",remoteFilePath);
        }else {
            jsonObject.put("datatask",dataTask);
        }
            out.println(jsonObject);
        return jsonObject;
    }


}
