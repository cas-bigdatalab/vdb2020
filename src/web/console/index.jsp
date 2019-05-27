<%@ page  language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="vdb.mydb.*" %>
<%@ page import="vdb.mydb.engine.VdbEngine" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>登录页面</title>
    <script type="text/javascript" src="/console/shared/js/jquery-3.2.1.min.js " ></script>
    <script src="/console/shared/bootstrap-3.3.7/js/bootstrap.js"></script>
    <script src="/console/datasync/js/dragdiv.js"></script>
    <style>
        * {
            margin: 0;
            padding: 0;
        }
        html, body{
            width: 100%;
            height: 100%;
            overflow: hidden;
        }
        .wrapper {
            position: relative;
            width: 100%;
            height: 100%;
            overflow: hidden;
            background: #C3E8F8;
        }
        img {
            display: block;
            width: 100%;
            height: auto;
        }
        .login {
            position: absolute;
            left: 0;
            right: 0;
            top: 0;
            bottom: 0;
            text-align: center;
            box-sizing: border-box;
            padding-top: 20%;
            padding-left: 3%;
        }
        label {
            display: inline-block;
            max-width: 100%;
            margin-bottom: 5px;
            font-weight: bold;
        }
        .btn{
            display: inline-block;
            padding: 6px 12px;
            margin-bottom: 0;
            font-size: 14px;
            font-weight: normal;
            line-height: 1.2;
            text-align: center;
            white-space: nowrap;
            vertical-align: middle;
            -ms-touch-action: manipulation;
            touch-action: manipulation;
            cursor: pointer;
            -webkit-user-select: none;
            -moz-user-select: none;
            -ms-user-select: none;
            user-select: none;
            background-image: none;
            border: 1px solid transparent;
            border-radius: 4px;
            float: right;
        }

        .btn:hover,
        .btn:focus,
        .btn:active{
            color: #333;
            background-color: #e6e6e6;
            border-color: #adadad;
        }


        .form-control {
            display: inline-block;
            padding: 6px 12px;
            font-size: 14px;
            line-height: 1.2;
            color: #555;
            background-color: #fff;
            background-image: none;
            border: 1px solid #ccc;
            border-radius: 4px;
            -webkit-box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075);
            box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075);
            -webkit-transition: border-color ease-in-out .15s, -webkit-box-shadow ease-in-out .15s;
            -o-transition: border-color ease-in-out .15s, box-shadow ease-in-out .15s;
            transition: border-color ease-in-out .15s, box-shadow ease-in-out .15s;
        }
        .form-control:focus {
            border-color: #66afe9;
            outline: 0;
            -webkit-box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075), 0 0 8px rgba(102, 175, 233, .6);
            box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075), 0 0 8px rgba(102, 175, 233, .6);
        }
        .form-control::-moz-placeholder {
            color: #999;
            opacity: 1;
        }
        .form-control:-ms-input-placeholder {
            color: #999;
        }
        .form-control::-webkit-input-placeholder {
            color: #999;
        }
    </style>
</head>

<body onkeydown="keyLogin();">

<div class="wrapper">
    <img src="/console/datasync/images/Log04_bg.jpg" alt="">
    <div class="login" >
        <div style="width:292px;margin: 20px auto 0px">
            <%--<hr style="width: 27%;margin-left: 37%"/>--%>
            <label for="userName">用户名：</label>
            <input type="text" id="userName" class="form-control" name="userName" style="width: 200px;margin: 10px;"/>
                <span id="checkedUserName" style="color: red;"></span>
            <br />
            <label for="password">密&nbsp;&nbsp;&nbsp;码：</label>
            <input type="password" id="password" class="form-control" name="password" style="width: 200px;margin: 10px;" />
                <span id="checkedUserPassword" style="color: red;"></span>
            <br />
            <div style="padding:10px 10px 0 0;overflow: hidden">
                <input type="submit" class="btn"  id="submit" value="登录"/>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">

    if (top != window)
        top.location.href = window.location.href;

    function keyLogin(){
        if (event.keyCode==13)  //回车键的键值为13
            start();
    }

    function start(){
        var username=$("#userName")[0].value;
        var password=$("#password")[0].value;
        var checked=true;
        if(username=="" || username==null){
            checked=false;
            $("#checkedUserName").html("账号不能为空！");
            return;
        }
        if(password=="" || password==null){
            checked=false;
            $("#checkedUserPassword").html("密码不能为空！");
            return;
        }

        if(checked){
            $.ajax({
                type:"POST",
                url:"/login",
                async:false,
                data:{
                    j_username:username,
                    j_password:password
                },
                success:function (data) {
                    $("#checkedUserName").html("");
                    $("#checkedUserPassword").html("");//清空错误信息！
                    if(data.replace(/[\r\n]/g,"")=="202"){
                       vdbLogin(username,password);
                    }else if(data.replace(/[\r\n]/g,"")=="201"){
                       $("#checkedUserName").html("账号不存在！");
                    }else if(data.replace(/[\r\n]/g,"")=="203"){
                       $("#checkedUserPassword").html("密码错误！");
                    }
                },
                error:function () {
                    console.log("请求失败")
                }
            })
        }
    };

    function vdbLogin(username,password){//密码验证通过后调用vdb登录接口
        $.ajax({
            type:"POST",
            url:"/j_spring_security_check",
            async:false,
            data:{
                j_username:username,
                j_password:password,
                j_uri:"%2Fcatalog%2F"
            },
            success:function (data) {
                window,location.href="/console/datasync/starter.jsp";
            },
            error:function () {
                console.log("请求失败")
            }
        })

    };

    document.getElementById("submit").onclick=function(){
        start();
    }


    function reset(){
        $("#userName")[0].value="";
        $("#password")[0].value="";
    }
</script>
</body>
</html>