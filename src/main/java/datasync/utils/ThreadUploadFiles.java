package datasync.utils;

import datasync.entity.FtpUtil;
import datasync.service.dataTask.DataTaskDao;
import datasync.service.login.GetInfoService;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ThreadUploadFiles extends  Thread{

     private HttpServletRequest request;
     private HttpServletResponse response;
     private BlockingQueue<Runnable> queue;
     public static List<String> taskIdList=new ArrayList<String>();
     private static String waitTaskId;
    private FtpUtil ftpUtil=new FtpUtil();

    public ThreadUploadFiles(HttpServletRequest request, HttpServletResponse response, BlockingQueue<Runnable> queue,String taskId){
         super();
         this.request=request;
         this.response=response;
         this.queue=queue;
         this.taskIdList.add(taskId);
    }
    public FTPClient ftpClient = new FTPClient();
    @Override
    public void run() {

     synchronized (queue){
        try {
            System.out.println("当前线程名称："+Thread.currentThread().getName());
            System.out.println("线程启动："+Thread.currentThread().getName());
            System.out.println("任务"+taskIdList.get(0)+"开始！");
            String runningTaskId=taskIdList.get(0);

            int result2=new DataTaskDao().ftpLocalUpload(request,response,runningTaskId);

            System.out.println("上传结果："+result2);
            if("4".equals(result2+"")){
                ThreadUploadFiles.waitTaskId=runningTaskId;
                taskIdList.remove(0);
                System.out.println(Thread.currentThread().getName()+"进入等待队列");
                checkFtpConnected();
                queue.wait();
                System.out.println("重新启动线程："+Thread.currentThread().getName());
                result2=new DataTaskDao().ftpLocalUpload(request,response,waitTaskId);
            }
                taskIdList.remove(0);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            queue.notify();
         }
    }

    public boolean connect(String hostname, int port, String username, String password){
            try {
                ftpClient.connect(hostname, port);
                ftpClient.setControlEncoding("GBK");
                if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {//判断ftp是否正常连接
                    if (ftpClient.login(username, password)) {
                        return true;
                    }
                }
                disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        return false;
    }

    public void disconnect(){
        if (ftpClient.isConnected()) {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("FTP关闭失败！");
            }
        }
    }

    public void checkFtpConnected () throws InterruptedException {
        String configFilePath = GetInfoService.class.getClassLoader().getResource("../../WEB-INF/config.properties").getFile();
        String host = ConfigUtil.getConfigItem(configFilePath, "FtpHost");// "10.0.86.77";
        String userName = ConfigUtil.getConfigItem(configFilePath, "FtpUser");//"ftpUserssdd";
        String password = ConfigUtil.getConfigItem(configFilePath, "FtpPassword");//"ftpPasswordssdd";
        String port = ConfigUtil.getConfigItem(configFilePath, "FrpPort");//"21";
        while (true){
            sleep(1000);
            Boolean result=connect(host,Integer.parseInt(port),userName,password);
            System.out.println("网络连接"+result);
            if (result){
                queue.notify();
                break;
            }
        }
    }
}
