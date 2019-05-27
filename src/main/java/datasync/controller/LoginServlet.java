package datasync.controller;

import datasync.service.login.GetInfoService;
import datasync.utils.MD5;
import vdb.mydb.VdbManager;
import vdb.mydb.security.VdbSecurityManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class LoginServlet extends HttpServlet {
    private GetInfoService loginService = new GetInfoService();
    private MD5 md5=new MD5();

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out=response.getWriter();
        VdbSecurityManager vsm = VdbManager.getInstance().getSecurityManager();
        String userName = request.getParameter("j_username");
        if(vsm.getUser(userName)==null){//优先判断账号是否存在！
            out.println("201");//账号不存在
            return;
        }
        String password= request.getParameter("j_password")+"{"+userName+"}";
        String passwordMd5=md5.getMD5(password);//将账号密码转化为vdb存储密码串，加密
        String derbyPassword=vsm.getUser(userName).getPassword();//获取vdb中存储的加密密码
        if(!derbyPassword.equals(passwordMd5)){
            out.println("203");//密码错误
            return;
        }

        //获得配置信息
        boolean statusOfGetInfo = false;
        try {
            statusOfGetInfo = GetInfoService.getSubjectConfig(); //返回值true， false， true表示同步信息成功，false表示同步信息失败
        }
        catch (Exception e)
        {
            e.printStackTrace();;
        }

        out.println("202");//登录成功
    }
}

