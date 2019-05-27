package datasync.entity;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: DataSync
 * @description: ftp upload and download utils
 * @author: huangwei
 * @create: 2018-09-27 10:16
 **/
public class FtpUtil {


    public static Map<String, Long> progressMap = new HashMap<String, Long>();
    public static Map<String, String> numberOfRequest = new HashMap<String,String>();
    public static Map<String, OutputStream> ftpOutputStream = new HashMap<String,OutputStream>();
    public static Map<String, Object> pauseTasks = new HashMap<String,Object>();

    public enum UploadStatus {
        Create_Directory_Fail,   //远程服务器相应目录创建失败
        Create_Directory_Success, //远程服务器闯将目录成功
        Upload_New_File_Success, //上传新文件成功
        Upload_New_File_Failed,   //上传新文件失败
        File_Exits,      //文件已经存在
        Remote_Bigger_Local,   //远程文件大于本地文件
        Upload_From_Break_Success, //断点续传成功
        Upload_From_Break_Failed, //断点续传失败
        Delete_Remote_Faild;   //删除远程文件失败
    }

    public enum DownloadStatus {
        Remote_File_Noexist, //远程文件不存在
        Local_Bigger_Remote, //本地文件大于远程文件
        Download_From_Break_Success, //断点下载文件成功
        Download_From_Break_Failed,   //断点下载文件失败
        Download_New_Success,    //全新下载文件成功
        Download_New_Failed;    //全新下载文件失败
    }

    public FTPClient ftpClient ;

    public boolean connect(String hostname, int port, String username, String password) throws IOException {
        ftpClient= new FTPClient();
        if(!ftpClient.isConnected()){
            ftpClient.connect(hostname, port);
        }
        ftpClient.setControlEncoding("GBK");
        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {//判断ftp是否正常连接
            if (ftpClient.login(username, password)) {
                return true;
            }
        }
        disconnect();
        return false;
    }

    public void disconnect() throws IOException {
        if (ftpClient.isConnected()) {
            ftpClient.disconnect();
        }
    }

    public List<JSONObject> downloadFtpFile(String host, String username, String password, String port, String filepath) {
        List<JSONObject> jsonObjects = new ArrayList<JSONObject>();
        FTPClient ftpClient = new FTPClient();
        ftpClient.setControlEncoding("utf-8");
        try {
            ftpClient.connect(host, Integer.parseInt(port)); //连接ftp服务器
            ftpClient.login(username, password); //登录ftp服务器
            ftpClient.enterLocalPassiveMode();
            ftpClient.changeWorkingDirectory(filepath);
            FTPFile[] ftpFiles = ftpClient.listFiles();
            if (ftpFiles.length < 1) {
                return jsonObjects;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObjects;
    }

    public DownloadStatus download(String host, String username, String password, String port, String remoteFilepath, String localFilepath) throws IOException {
        //设置被动模式
        ftpClient.enterLocalPassiveMode();
        //设置以二进制方式传输
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        DownloadStatus result;

        //检查远程文件是否存在
        FTPFile[] files = ftpClient.listFiles(new String(remoteFilepath.getBytes("GBK"), "iso-8859-1"));
        if (files.length != 1) {
            System.out.println("远程文件不存在");
            return DownloadStatus.Remote_File_Noexist;
        }

        long lRemoteSize = files[0].getSize();
        File f = new File(localFilepath);
        //本地存在文件，进行断点下载
        if (f.exists()) {
            long localSize = f.length();
            //判断本地文件大小是否大于远程文件大小
            if (localSize >= lRemoteSize) {
                System.out.println("本地文件大于远程文件，下载中止");
                return DownloadStatus.Local_Bigger_Remote;
            }

            //进行断点续传，并记录状态
            FileOutputStream out = new FileOutputStream(f, true);
            ftpClient.setRestartOffset(localSize);
            InputStream in = ftpClient.retrieveFileStream(new String(remoteFilepath.getBytes("GBK"), "iso-8859-1"));
            byte[] bytes = new byte[1024];
            long step = lRemoteSize / 100;
            long process = localSize / step;
            int c;
            while ((c = in.read(bytes)) != -1) {
                out.write(bytes, 0, c);
                localSize += c;
                long nowProcess = localSize / step;
                if (nowProcess > process) {
                    process = nowProcess;
                    if (process % 10 == 0)
                        System.out.println("下载进度：" + process);
                    //TODO 更新文件下载进度,值存放在process变量中
                }
            }
            in.close();
            out.close();
            boolean isDo = ftpClient.completePendingCommand();
            if (isDo) {
                result = DownloadStatus.Download_From_Break_Success;
            } else {
                result = DownloadStatus.Download_From_Break_Failed;
            }
        } else {
            OutputStream out = new FileOutputStream(f);
            InputStream in = ftpClient.retrieveFileStream(new String(remoteFilepath.getBytes("GBK"), "iso-8859-1"));
            byte[] bytes = new byte[1024];
            long step = lRemoteSize / 100;
            long process = 0;
            long localSize = 0L;
            int c;
            while ((c = in.read(bytes)) != -1) {
                out.write(bytes, 0, c);
                localSize += c;
                long nowProcess = localSize / step;
                if (nowProcess > process) {
                    process = nowProcess;
                    if (process % 10 == 0)
                        System.out.println("下载进度：" + process);
                    //TODO 更新文件下载进度,值存放在process变量中
                }
            }
            in.close();
            out.close();
            boolean upNewStatus = ftpClient.completePendingCommand();
            if (upNewStatus) {
                result = DownloadStatus.Download_New_Success;
            } else {
                result = DownloadStatus.Download_New_Failed;
            }
        }
        return result;
    }

    public UploadStatus upload(String[] localFileList, String processId,String remoteFilepath,DataTask dataTask,String subjectCode) throws IOException {
//        ftpClient.enterLocalPassiveMode();
//        ftpClient.enterRemotePassiveMode();
        System.out.println("开始调用上传程序！");
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.setControlEncoding("GBK");
        ftpClient.changeWorkingDirectory("/temp/");
        ftpClient.enterLocalPassiveMode();
        System.out.println(ftpClient.getStatus());
        UploadStatus result;
        Map resultmap;
        Long fileTotalSize = 0L;
        Long finishedSize = 0L;
        for (String s : localFileList) {
//            s = s.replace("%_%",File.separator);
            File file = new File(s.replace("%_%",File.separator));
            fileTotalSize += file.length();
            System.out.println("-------fileTotalSize"+fileTotalSize);
        }

        for (String localFilepath : localFileList) {
            String newlocalFilepath = localFilepath.replace("%_%",File.separator);
            String fileName = "";
            System.out.println("-------localFilepath"+localFilepath);
            System.out.println("-------localFilepath.indexOf(%_%)>0-----"+(localFilepath.indexOf("%_%")>0));
            /*System.out.println("-------localFilepath.indexOf(File.separator)>0"+(localFilepath.indexOf(File.separator)>0));
            if(localFilepath.indexOf("%_%")>0){
                fileName = localFilepath.substring(localFilepath.lastIndexOf("%_%")+3);
                System.out.println("-------fileName"+fileName);
            }*/
            if("mysql".equals(dataTask.getDataTaskType()) || "oracle".equals(dataTask.getDataTaskType())){
                fileName = dataTask.getDataTaskId()+".zip";
            }else{
                fileName = subjectCode + "_" +dataTask.getDataTaskId()+".zip";
            }

            //对远程目录的处理
            String remoteFileName = fileName.replace("%_%",File.separator);
            System.out.println("-------------remoteFileName"+remoteFileName);


            //检查远程是否存在文件
            FTPFile[] files = ftpClient.listFiles(new String(remoteFileName.getBytes("GBK"), "iso-8859-1"));
            if (files.length == 1) {
                  long remoteSize = files[0].getSize();
                File f = new File(newlocalFilepath);
                long localSize = f.length();
                if (remoteSize == localSize) {
                    return UploadStatus.File_Exits;
                } else if (remoteSize > localSize) {
                    return UploadStatus.Remote_Bigger_Local;
                }

                //尝试移动文件内读取指针,实现断点续传
                 resultmap = uploadFile(fileTotalSize, finishedSize,processId, remoteFileName, f, ftpClient, remoteSize);
                result = (UploadStatus)(resultmap.get("status"));
//                finishedSize = (Long)(resultmap.get("finishedSize"));

                //如果断点续传没有成功，则删除服务器上文件，重新上传
                if (result == UploadStatus.Upload_From_Break_Failed){//(result == UploadStatus.Upload_From_Break_Failed) {
                    if (!ftpClient.deleteFile(remoteFileName)) {
                        return UploadStatus.Delete_Remote_Faild;
                    }
                    resultmap = uploadFile(fileTotalSize, finishedSize, processId, remoteFileName, f, ftpClient, 0);
                    result = (UploadStatus)(resultmap.get("status"));
                    finishedSize = (Long)(resultmap.get("finishedSize"));
                }
            } else {
                if (remoteFilepath.contains("/")) {
//                remoteFileName = remoteFilepath.substring(remoteFilepath.lastIndexOf("/") + 1);
                    //创建服务器远程目录结构，创建失败直接返回
//                    ftpClient.changeWorkingDirectory(remoteFilepath);
                    if (CreateDirecroty(remoteFilepath, ftpClient) == UploadStatus.Create_Directory_Fail) {
                        return UploadStatus.Create_Directory_Fail;
                    }
                }
                ftpClient.changeWorkingDirectory("/temp/");
                resultmap = uploadFile(fileTotalSize, finishedSize, processId, remoteFileName, new File(newlocalFilepath), ftpClient, 0);
                result = (UploadStatus)(resultmap.get("status"));
                finishedSize = (Long)(resultmap.get("finishedSize"));
            }
            if(result.equals(UploadStatus.Upload_New_File_Failed))
                return result;
            if(result.equals(UploadStatus.Upload_From_Break_Failed))
                return result;
        }
        return UploadStatus.Upload_New_File_Success;
    }

    public Map uploadFile(long fileTotalSize, long finishedSize, String processId, String remoteFile, File localFile, FTPClient ftpClient, long remoteSize) throws IOException {
        Map returnMap = new HashMap();
        UploadStatus status;
        //显示上传进度
        long step = fileTotalSize / 100;
        long process = progressMap.get(processId)==null?0:progressMap.put(processId,0L);
        long localreadbytes = 0L;
        RandomAccessFile raf = new RandomAccessFile(localFile, "r");
       // System.out.println("------------remoteFile.getBytes(\"GBK\")="+remoteFile.getBytes("GBK"));
        String testtring = new String(remoteFile.getBytes("GBK"), "iso-8859-1");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!"+testtring);


        OutputStream out = ftpClient.appendFileStream(new String(remoteFile.getBytes("GBK"), "iso-8859-1"));
        ftpOutputStream.put(processId,out);
        if(out == null){
            System.out.println("=============null out");
        }
      //  try{
            //断点续传
            if (remoteSize > 0) {
                /*ftpClient.setRestartOffset(remoteSize);
                process = process+remoteSize / step;
                raf.seek(remoteSize);
                localreadbytes = remoteSize;*/
                finishedSize += remoteSize;
                ftpClient.setRestartOffset(remoteSize);
                process = finishedSize / step;
                raf.seek(remoteSize);
                localreadbytes = remoteSize;
                progressMap.put(processId, process);
            }
            byte[] bytes = new byte[1024];
            int c;
            while ((c = raf.read(bytes)) != -1) {
                out.write(bytes, 0, c);
                localreadbytes += c;
                finishedSize += c;
                /*if (localreadbytes / step != process) {
                    process = localreadbytes / step;
                    System.out.println("上传进度:" + process);
                    progressMap.put(processId, process);
                }*/
                if (finishedSize / step != process) {
                    process = finishedSize / step;
                    if(process>100){
                        process = 100;
                    }
                    System.out.println("上传进度:" + process);
                    if(process!=100){
                        progressMap.put(processId, process);
                    }
                }
            }
        //}catch (IOException e){
         //   System.out.println("ftp 连接失败！");
       // }finally {
            out.flush();
            raf.close();
            out.close();
      //  }
        boolean result = ftpClient.completePendingCommand();
        if (remoteSize > 0) {
            status = result ? UploadStatus.Upload_From_Break_Success : UploadStatus.Upload_From_Break_Failed;
        } else {
            status = result ? UploadStatus.Upload_New_File_Success : UploadStatus.Upload_New_File_Failed;
        }
        returnMap.put("status",status);
        returnMap.put("finishedSize",finishedSize);
        return returnMap;
    }

    public UploadStatus CreateDirecroty(String remote, FTPClient ftpClient) throws IOException {
        UploadStatus status = UploadStatus.Create_Directory_Success;
        String directory = remote.substring(0, remote.lastIndexOf("/") + 1);
        if (!directory.equalsIgnoreCase("/") && !ftpClient.changeWorkingDirectory(new String(directory.getBytes("GBK"), "iso-8859-1"))) {
            //如果远程目录不存在，则递归创建远程服务器目录
            int start = 0;
            int end = 0;
            if (directory.startsWith("/")) {
                start = 1;
            } else {
                start = 0;
            }
            end = directory.indexOf("/", start);
            while (true) {
                String subDirectory = new String(remote.substring(start, end).getBytes("GBK"), "iso-8859-1");
                if (!ftpClient.changeWorkingDirectory(subDirectory)) {
                    if (ftpClient.makeDirectory(subDirectory)) {
                        ftpClient.changeWorkingDirectory(subDirectory);
                    } else {
                        System.out.println("创建目录失败");
                        return UploadStatus.Create_Directory_Fail;
                    }
                }

                start = end + 1;
                end = directory.indexOf("/", start);

                //检查所有目录是否创建完毕
                if (end <= start) {
                    break;
                }
            }
        }
        return status;
    }

    public long getFtpUploadProcess(String processId){
        if(progressMap.get(processId) == null){
            return 0L;
        }
        return progressMap.get(processId);
    }

    public boolean deleteFile(String delFile){
        boolean flag = false;
        try {
            flag = ftpClient.deleteFile(delFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public boolean removeDirectory(String delDirectory){
        boolean flag = false;
        try {
            FTPFile[] files = ftpClient.listFiles(new String(delDirectory.getBytes("GBK"), "iso-8859-1"));
            for(FTPFile file:files){
                String fname = delDirectory+"/"+file.getName();
                String name = new String(fname.getBytes("GBK"),"iso-8859-1");
                boolean f = ftpClient.deleteFile(name);
               // System.out.println(f);//删除远程文件是否成功
            }
            ftpClient.removeDirectory(delDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public  void setProgressMap(String  key,long process) {
        FtpUtil.progressMap .put(key,process);
    }

    //获取阻塞请求的数量
    public  String getBlockRequest(String request) {
//        requestId="blockRequest";
//        requestId="successRequest";
        if(numberOfRequest.get(request) == null){
            return "";
        }
        return numberOfRequest.get(request);
    }
}
