package datasync.service;


import com.alibaba.fastjson.JSONObject;
import com.google.common.io.Files;
import datasync.entity.DataSrc;
import datasync.entity.ZipUtils;
import datasync.service.login.ConfigPropertyService;
import datasync.service.login.GetInfoService;
import datasync.utils.ConfigUtil;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.jfunc.json.impl.JSONArray;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;

/**
 * @program: DataSync
 * @description: DataTask Service
 * @author:
 * @create:
 **/

@Service
public class FileResourceService {

    private Logger logger = LoggerFactory.getLogger(FileResourceService.class);
    public static  int fileNumber;
    public static Map<String, Object> progressMap = new HashMap<String, Object>();

    @Resource
    private FileResourceDao fileResourceDao;

    @Resource
    private ConfigPropertyService configPropertyService;

    @Transactional
    public String addData(DataSrc datasrc) {
        try {
            int addedRowCnt = fileResourceDao.addRelationData(datasrc);
            if (addedRowCnt == 1) {
                return "1";
            } else {
                return "0";
            }
        } catch (Exception e) {
            return "0";
        }
    }

    public List<DataSrc> queryData() {

        return fileResourceDao.queryRelationData();

    }

    public String editData(DataSrc dataSrc) {
        try {
            int addedRowCnt = fileResourceDao.editRelationData(dataSrc);
            if (addedRowCnt == 1) {
                return "1";
            } else {
                return "0";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @Transactional
    public String deleteRelation(int id) {
        int deletedRowCnt = fileResourceDao.deleteRelationData(id);
        if (deletedRowCnt == 1) {
            return "1";
        } else {
            return "0";
        }
    }

    public List<DataSrc> editQueryData(int id) {
        return fileResourceDao.editQueryData(id);
    }

    public Map queryTotalPage(String SubjectCode) {
        return fileResourceDao.queryTotalPage(SubjectCode);
    }

    public List<DataSrc> queryPage(int requestedPage,String SubjectCode) {
        return fileResourceDao.queryPage(requestedPage,SubjectCode);
    }

    public List<JSONObject> fileTreeLoading(String data, String filePath) {
        String FILE_SEPARATOR = System.getProperties().getProperty("file.separator");
        File file;
        data = data.replaceAll("%_%",Matcher.quoteReplacement(File.separator));
        filePath = filePath.replaceAll("%_%",Matcher.quoteReplacement(File.separator));
        List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
        if ("#".equals(data)) {
            /*//获取服务器盘符
            String os = System.getProperty("os.name");
            if(os.toLowerCase().startsWith("win")){
                File[] roots = File.listRoots();
                logger.info("服务器盘符为："+roots);
                logger.info("子目录个数为："+roots.length);
                for (int i =0; i < roots.length; i++) {
                    String root = roots[i].toString();
                    String rootName= root.substring(0, root.indexOf("\\"));
                    JSONObject jsonObject = new JSONObject();
                    if (roots[i].isDirectory()) {
                        File file1 = roots[i];
                        File[] rootNode = file1.listFiles();
                        if (rootNode != null) {
                            if (rootNode.length == 0) {
                                jsonObject.put("children", false);
                            } else {
                                jsonObject.put("children", true);
                            }
                        } else {
                            jsonObject.put("children", false);
                        }
                        jsonObject.put("type", "directory");
                        JSONObject jo = new JSONObject();
                        jo.put("disabled", "true");
                        jsonObject.put("state", jo);
                        jsonObject.put("id", root.replaceAll("\\\\", "%_%"));
                        jsonObject.put("name", rootName);
                    } else {
                        jsonObject.put("type", "file");
                        jsonObject.put("id", root.replaceAll("\\\\", "%_%"));
                        jsonObject.put("name", rootName);
                    }
                    jsonObjects.add(jsonObject);
                }
            }else{*/
                File roots = new File(filePath);
                logger.info("服务器盘符为："+roots);
                String root = roots.toString();
                logger.info("root为："+root);
/*
                String rootName= root.substring(0, root.indexOf("\\"));
*/
/*
                logger.info("rootName为："+rootName);
*/
                JSONObject jsonObject = new JSONObject();
                if (roots.isDirectory()) {
                    File file1 = roots;
                    File[] rootNode = file1.listFiles();
                    if (rootNode != null) {
                        if (rootNode.length == 0) {
                            jsonObject.put("children", false);
                        } else {
                            jsonObject.put("children", true);
                        }
                    } else {
                        jsonObject.put("children", false);
                    }
                    jsonObject.put("type", "directory");
                    JSONObject jo = new JSONObject();
                    jo.put("disabled", "true");
                    jsonObject.put("state", jo);
                    jsonObject.put("id", root.replaceAll(Matcher.quoteReplacement(File.separator), "%_%"));
                    jsonObject.put("name", root);
                } else {
                    jsonObject.put("type", "file");
                    jsonObject.put("id", root.replaceAll(Matcher.quoteReplacement(File.separator), "%_%"));
                    jsonObject.put("name", root);
                }
                jsonObjects.add(jsonObject);
                return jsonObjects;
        } else {
            file = new File(data);
            if (!file.exists() || !file.isDirectory()) {
                return jsonObjects;
            } else {
                File[] fileList = file.listFiles();
                for (int i = 0; i < fileList.length; i++) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", fileList[i].getPath().replaceAll(Matcher.quoteReplacement(File.separator), "%_%"));
                    jsonObject.put("name", fileList[i].getName().replaceAll(Matcher.quoteReplacement(File.separator), "%_%"));
                    if (fileList[i].isDirectory()) {
                        File file1 = fileList[i];
                        File[] fileNode = file1.listFiles();
                        if (fileNode != null) {
                            if (fileNode.length == 0) {
                                jsonObject.put("children", false);
                            } else {
                                jsonObject.put("children", true);
                            }
                        } else {
                            jsonObject.put("children", false);
                        }
                        jsonObject.put("type", "directory");
                        JSONObject jo = new JSONObject();
                        jo.put("disabled", "true");
                        jsonObject.put("state", jo);
                    } else {
                        jsonObject.put("type", "file");
                    }
                    jsonObjects.add(jsonObject);
                }
                return jsonObjects;

            }
        }

/*
        Collections.sort(jsonObjects, new FileComparator());
*/
    }

    public String traversingFiles(String nodeId) {
        File file = new File(nodeId);
        StringBuffer Sb = new StringBuffer();
        if (file.isDirectory()) {
            File[] fileNode = file.listFiles();
            if (fileNode != null) {
                if (fileNode.length == 0) {
                    String str1 = file.getPath() + ";";
                    Sb.append(str1);
                } else {
                    for (int i = 0; i < fileNode.length; i++) {
                        Sb.append(traversingFiles(fileNode[i].getPath()));
                    }
                }
            } else {
                String str2 = file.getPath() + ";";
                Sb.append(str2);
            }
        } else {
            String str3 = file.getPath() + ";";
            Sb.append(str3);
        }

        System.out.print("Service层中记录的文件路径为：" + Sb.toString());
        return Sb.toString();
    }

    //求两个字符串数组的并集，利用set的元素唯一性
    public static String[] union(String[] arr1, String[] arr2) {
        Set<String> set = new HashSet<String>();
        for (String str : arr1) {
            set.add(str);
        }
        for (String str : arr2) {
            set.add(str);
        }
        String[] result = {};
        return set.toArray(result);
    }

    public Boolean checkFilePath(String filePath){
        File file = new File(filePath);
        boolean flag = file.exists();
        return flag;
    }
    /**
     * Function Description:
     *
     * @param: []
     * @return: java.util.List<cn.csdb.drsr.model.DataSrc>
     * @auther: hw
     * @date: 2018/10/23 10:01
     */
    public List<DataSrc> findAll() {
        return fileResourceDao.findAll();
    }

    public List<JSONObject> fileSourceFileList(String filePath) {
        return fileResourceDao.fileSourceFileList(filePath);
    }

    public DataSrc findById(int id) {
        return fileResourceDao.findById(id);
    }

    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    /**
     *
     * Function Description: 压缩文件任务包含的文件
     *
     * @param: [datasrcId, filePaths, dbFlag]
     * @return: void
     * @auther:chq
     * @date: 2018/11/25 16:11
     */
    public String packDataResource(final String fileName ,final List<String> filePaths,String zipPath) {
//        dbFlag.await();
        String zipFilePath = "zipFile";
        File dir  = new File(zipPath + zipFilePath );
        if(!dir.exists()){
            dir.mkdirs();
        }
        String zipFile = zipPath + zipFilePath + File.separator + fileName + ".zip";
        ZipArchiveOutputStream outputStream = null;
        try {
            if (new File(zipFile).exists()) {
                new File(zipFile).delete();
            }
            Files.createParentDirs(new File(zipFile));
            outputStream = new ZipArchiveOutputStream(new File(zipFile));
            outputStream.setEncoding("utf-8"); //23412
            outputStream.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);
            outputStream.setFallbackToUTF8(true);
            logger.info(".zip:文件数据源,开始打包文件...");
            //获取文件总数量
            fileNumber=0;
            for(String filePath : filePaths){
                filePath = filePath.replace("%_%",File.separator);
                File file = new File(filePath);
                if(file.isDirectory()){
                    continue;
                }else{
                    fileNumber++;
                }
            }
            logger.info("文件总数: "+fileNumber+"");
            int compressedFilesNum=0;
            for (String filePath : filePaths) {
                filePath = filePath.replace("%_%",File.separator);
                File file = new File(filePath);
                if (!file.exists()) {
                    continue;
                }
                if(file.isDirectory()){
                    continue;
                }else{
                    ++compressedFilesNum;
                    //float num= (float)compressedFilesNum/fileNumber;
                   // System.out.println(num);

                    float process= (float)compressedFilesNum/fileNumber*100;
                    BigDecimal b   =   new   BigDecimal(process);
                    process   =   b.setScale(0,   BigDecimal.ROUND_HALF_UP).floatValue();
                    progressMap.put(fileName.substring(fileName.indexOf("_")+1), (long)process);
                    progressMap.put(fileName.substring(fileName.indexOf("_")+1)+"1", file.toString());
                    System.out.println(process+"%");
                    ZipUtils.zipDirectory(file, "", outputStream);
                }
            }
        } catch (Exception e) {
            logger.error("打包失败", e);
            return "error";
        } finally {
            try {
                outputStream.finish();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "ok";
    }

    public Object getPackProcess(String processId){
        if(progressMap.get(processId) == null){
            return 0L;
        }
        return progressMap.get(processId);
    }

    public String LoadingRemoteTree(){
        String configFilePath = GetInfoService.class.getClassLoader().getResource("../../WEB-INF/config.properties").getFile();
        String portalUrl = ConfigUtil.getConfigItem(configFilePath, "PortalUrl");
        String loginApiPath = "/service/treeNodeAsync";
        String ftpUserName= ConfigUtil.getConfigItem(configFilePath, "FtpUser");

        org.apache.http.client.HttpClient httpClient = null;
        HttpPost postMethod = null;
        HttpGet getMethod = null;
        HttpResponse response = null;
        httpClient = HttpClients.createDefault();
        getMethod = new HttpGet("http://"+portalUrl+loginApiPath+"?" + "subName=" + ftpUserName + "&async=" + "2async");
        getMethod.addHeader("Content-type", "application/json; charset=utf-8");
        try {
            response = httpClient.execute(getMethod);//获取响应
        } catch (IOException e) {
            e.printStackTrace();
        }
        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println(statusCode);
        HttpEntity entity = response.getEntity();
        String reponseContent = null;
        try {
            reponseContent = EntityUtils.toString(entity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONArray jsonArray=new JSONArray("["+reponseContent+"]");
        Object json= jsonArray.get(0);
        String jsonStr=((JSONObject) json).get("data").toString();

        return jsonStr;

    }



}








