package datasync.service.settingTask;

import datasync.connection.SqlLiteDataConnection;
import datasync.service.FileResourceService;
import datasync.service.dataTask.DataTaskDao;
import datasync.service.login.GetInfoService;
import datasync.utils.ConfigUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import vdb.metacat.Repository;
import vdb.mydb.VdbManager;
import vdb.mydb.engine.VdbEngine;
import vdb.mydb.filestat.impl.LocalRepository;
import vdb.mydb.filestat.impl.RepositoriesService;
import vdb.mydb.repo.FileRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class LocalConnDaoService {

   //查询本地数据源下路径列表
    public List<Object> searchBdDirListImp(HttpServletRequest req, HttpServletResponse res) throws IOException {
        List<Object> list=new ArrayList<Object>();
        List<FileRepository> repositories = new ArrayList<FileRepository>();
        // RepositoryManager rm = new RepositoryManager(); // rm.setRepositories();
        RepositoriesService rs=new RepositoriesService();//getAllRepositories
        VdbManager vdbManager=new VdbManager();
        VdbEngine vdbEngine=vdbManager.getInstance();
        int vdbLength=VdbManager.getInstance().getDomain().getDataSets().length;
        for (int i=0;i<vdbLength;i++){
            Repository repository=vdbEngine.getDomain().getDataSets()[i].getRepository();
                String uri=vdbEngine.getDomain().getDataSets()[i].getUri();
                String title=vdbEngine.getDomain().getDataSets()[i].getTitle();
                List<FileRepository> fileRepository=rs.getAllRepositories(uri);
                if(fileRepository.size()>1){
                    list.add(title+"?*"+uri);
                }
        }
        return list;
    }

    //获取本地数据源数据路径-接口（路径/文件）
    public List<Object> searchBdDirListPathImp(HttpServletRequest req, HttpServletResponse res) throws IOException {
        List<Object> list=new ArrayList<Object>();
        RepositoriesService rs=new RepositoriesService();//getAllRepositories
        String uri=req.getParameter("dirListData");
        List<FileRepository> fileRepository=rs.getAllRepositories(uri);
        List<Map<Object,Object>> listFileName = new ArrayList<Map<Object, Object>>();
        if (1!=fileRepository.size()){
            for(int j=0;j<fileRepository.size()-1;j++){
                String path=((LocalRepository) fileRepository.get(j)).getPath();
                File file = new File(path);
                if(file.isDirectory()){//判断是否为路径
                    List<Map<Object, Object>> listFileNameDirs= findAllFileDirByPath(file,listFileName);
                    list.addAll(listFileNameDirs);
                }else{
                    list.add(file.getName());
                }
            }
        }
        return list;
    }

    //获取路径下所有文件-递归
    public List<Map<Object,Object>> findAllFileDirByPath(File file, List<Map<Object,Object>> list){
        if(file!=null){
            if(file.isDirectory()){
                File[] fileArray=file.listFiles();
                if(fileArray!=null){
                    for (int i = 0; i < fileArray.length; i++) {
                        //递归调用
                        findAllFileDirByPath(fileArray[i],list);
                    }
                }
            }
            else{
                Map<Object,Object> map=new HashMap<Object, Object>();
                map.put(file,file.getName());
                list.add(map);
            }
        }
        return list;
    }

    public  String updateLocalTaskDataById(HttpServletRequest req, HttpServletResponse res) throws IOException {
        final String connDataName=req.getParameter("connDataName");//数据源名称
        final String connDataValue=req.getParameter("connDataValue");//数据源code
        final String getCheckedFile=req.getParameter("getCheckedFile");//文件path
        final String dataTaskId=req.getParameter("dataTaskId");//taskid
        final String dataSourceId=req.getParameter("dataSourceId");//数据源id
        final String getRemoteFile=req.getParameter("getRemoteFile");//上传路径

        final String sql = "update  t_datatask set  filePath=? , remoteuploadpath=? where dataTaskId=?;";
        SqlLiteDataConnection sqlLiteDataConnection=new SqlLiteDataConnection();
        JdbcTemplate jdbcTemplate=sqlLiteDataConnection.makeJdbcTemplate();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
                ps.setString(1,getCheckedFile);
                ps.setString(2,getRemoteFile);
                ps.setString(3,dataTaskId);
                return ps;
            }
        },keyHolder);
        new DataTaskDao().updateDataSourceInfById(dataSourceId,connDataName,connDataValue);
        FileResourceService fileResourceService=new FileResourceService();
        String configFilePath = GetInfoService.class.getClassLoader().getResource("../../WEB-INF/config.properties").getFile();
        String subjectCode= ConfigUtil.getConfigItem(configFilePath, "SubjectCode");
        //重新打包
        String fileName = subjectCode+"_"+dataTaskId;
        List<String> filepaths =new LinkedList<String>();
        String [] filepathArray=getCheckedFile.split(";");
        for (String str:filepathArray){
            ((LinkedList<String>) filepaths).add(str);
        }
        fileResourceService.packDataResource(fileName,filepaths,req.getSession().getServletContext().getRealPath("/"));
        return  "success";
    }





}
