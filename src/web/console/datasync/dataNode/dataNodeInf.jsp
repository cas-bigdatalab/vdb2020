<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>Insert title here</title>
    <script type="text/javascript" src="/console/shared/js/jquery-3.2.1.min.js " ></script>
    <script src="/console/shared/bootstrap-3.3.7/js/bootstrap.min.js"></script>
    <link href="/console/datasync/css/projectMessage.css" rel="stylesheet" type="text/css" />
    <link href="/console/shared/bootstrap-3.3.7/css/bootstrap.min.css" rel="stylesheet" type="text/css" />
    <style type="text/css">
        .form-group div{
            font-size: 18px;
        }
    </style>
</head>

<body>
<div class="page-content">

    <div class="task-head">

    </div>

    <div class="task-title">
        <span>主题库信息</span>
    </div>

    <div class="messageCon" >

        <form class="form-horizontal">

            <div class="form-group">
                <label  class="col-sm-3 control-label">主题库名称:</label>
                <div class="col-sm-8" name="subjectName"></div>
            </div>

            <div class="form-group">
                <label  class="col-sm-3 control-label">主题库代码:</label>
                <div class="col-sm-8" name="subjectCode"></div>
            </div>

            <div class="form-group">
                <label  class="col-sm-3 control-label">管理员账号:</label>
                <div class="col-sm-8" name="userName"></div>
            </div>

            <div class="form-group">
                <label  class="col-sm-3 control-label">密码:</label>
                <div class="col-sm-8">******</div>
            </div>

            <div class="form-group">
                <label  class="col-sm-3 control-label">描述:</label>
                <div class="col-sm-8" name="brief"></div>
            </div>
        </form>
    </div>
</div>



<script type="text/javascript">

   $(function(){
       $.ajax({
           url:"/achieveDataNodeInf.do",
           data:{},
           success:function (dataSession) {
               var obj=JSON.parse(dataSession);
               document.getElementsByName("subjectName")[0].textContent=obj.DataNodeInf[0];//专业库名称
               document.getElementsByName("subjectCode")[0].textContent=obj.DataNodeInf[1];//专业库代码
               document.getElementsByName("userName")[0].textContent=obj.DataNodeInf[2];//管理员账号
               document.getElementsByName("brief")[0].textContent=obj.DataNodeInf[3];//描述
           },
           error:function () {
               console.log("请求失败")
           }
       })
   });

</script>

</body>

</html>