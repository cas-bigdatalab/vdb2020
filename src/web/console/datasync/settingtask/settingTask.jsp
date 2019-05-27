<%--
  Created by IntelliJ IDEA.
  User: caohq
  Date: 2018/11/16
  Time: 17:03
  To change this template use File | Settings | File Templates.
--%>
<%@ page  language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<html style="height: 82%;">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>Insert title here</title>
    <script type="text/javascript" src="/console/shared/js/jquery-3.2.1.min.js " ></script>
    <script src="/console/shared/bootstrap-3.3.7/js/bootstrap.js"></script>
    <script src="/console/datasync/js/layer/layer.js"></script>
    <script src="/console/shared/bootstrap-toastr/toastr.js"></script>
    <script src="/console/shared/zTree_v3/js/jquery.ztree.all.js"></script>
    <script src="/console/shared/zTree_v3/js/rightClick.js"></script>
    <link rel="stylesheet" type="text/css" href="/console/datasync/css/createTask.css" />
    <link rel="stylesheet" type="text/css" href="/console/datasync/css/style.min.css" />
    <link rel="stylesheet" type="text/css" href="/console/datasync/js/layer/layer.css" />
    <link rel="stylesheet" type="text/css" href="/console/shared/bootstrap-toastr/toastr.css" />
    <link type="text/css" rel="stylesheet" href="/console/shared/zTree_v3/css/demo.css" />
    <link type="text/css" rel="stylesheet" href="/console/shared/zTree_v3/css/zTreeStyle/zTreeStyle.css" />
    <link type="text/css" rel="stylesheet" href="/console/datasync/css/editorLoadingCss.css" />
    <link rel="stylesheet" type="text/css" href="/console/shared/bootstrap-3.3.7/css/bootstrap.css">
    <link rel="stylesheet" type="text/css" href="/console/shared/bootstrap-3.3.7/css/bootstrap-table.min.css">
    <style>
        .fixed-table-pagination .page-list{
            display: none !important;
        }
        * {
            outline: 0;
            padding: 0;
            margin: 0;
            border: 0;
        }

        TABLE {
            font-size: 10.5pt;
        }

        #loading {    /*弹出层样式*/
            z-index: 100000;
            position: fixed;
            width: 380px;
            height: 100px;
            left: 46%;
            top: 50%;
            margin-left: -145px;
            margin-top: -45px;
            background-color: #ccc;
            border: 5px solid #eee;
            box-shadow: 7px 7px 10px #999;
            font-size: 20px;
            line-height: 100px;
            text-align: center;
            color: #333;
        }
        ul.ztree {
            width: 100%;
            height: 93%;
        }
        div.content_wrap {
            width: 90%;
            height: 380px;
        }

        #Progress{
            left: 45% !important;
            top:  24% !important;
        }
        .circle-info{
            top:58% !important;
            line-height: 1.5 !important;
        }
        div#rMenu {position:absolute; visibility:hidden; top:0; background-color: #555;text-align: left;padding: 2px;}
        div#rMenu ul li{
            margin: 1px 0;
            padding: 0 5px;
            cursor: pointer;
            list-style: none outside none;
            background-color: #DFDFDF;
        }

    </style>

</head>
<body style="overflow: auto; ">

<div class="content">

    <div style="width:50%;float:left;margin-left: 46px;margin-top: 20px;height:27px;">
        <span style="font-weight: 700;font-size: 24px;">数据源:&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <label for="aaa" style="font-size: 16px" >数据库</label>
        <input name="ways" type="radio" checked="checked" value="database" id="aaa"/>
        <label for="bbb" style="font-size: 16px">&nbsp;&nbsp;&nbsp;&nbsp;本地上传</label>
        <input name="ways" type="radio" value="file" id="bbb"/>
    </div>

    <!--数据库-->
    <div style="width:80%;margin-top:20px;float:left;margin-left: 100px;">

        <div id="sjk" class="form-group" style="width: 100%;height: 100%;margin-left: 76px;">
            <label for="aaa" >数据库列表:</label>
            <select id="selectId" class="form-control selectpicker" style="width: 200px;display: inline !important;"></select>
            <div>
                <label id="tableLabel" style="margin-top: 10px">&nbsp;&nbsp;选择表资源</label>
                <div id="tablesDiv" style="margin-left: 40px;height:260px;overflow-y:auto;"></div>
            </div>
            <div style="width: 100%;height: 40px;margin-top: 20px; " id="sqlSearchDiv">
                <label>sql查询</label>
                <input id="sqlInputBox0" class="form-control sqlStatements" style="width: 300px;display: inline !important;" type="text"/>
                <input id="createNewTableName0" class="form-control sqlStatements" style="width: 100px;display: inline !important;" type="text" placeholder="请输入表名"/>
                <button type="button" class="btn blue preview" onclick="showPreviewModal(sqlInputBox0)" >预览</button>
                <button type="button" class="btn green" onclick="addSqlInput()"><span class="glyphicon glyphicon-plus"></span>sql查询</button>
                <button type="button" class="btn green" onclick="submitSqlData()">提交</button>
            </div>
        </div>

        <!--本地数据源列表-->
        <div id="bdsc" style="width: 100%;height: 100%;margin-left: 76px;">
            <div style="" class="form-group">
                <label for="aaa" >本地数据源列表:</label>
                <select id="selectBdDirID"  class="form-control selectpicker" style="width: 200px;display: inline !important;"></select>
                <div class="content_wrap">
                    <label id="bdTableLabel">&nbsp;&nbsp;请选择资源</label>
                    <div id="bdDirDiv" style="margin-left: 10px;overflow-y:auto;width: 42%;float: left;">
                        <ul id="LocalTreeDemo" class="ztree" ></ul>
                    </div>
                    <div id="remoteDirDiv" style="margin-left: 70px;overflow-y:auto;width: 42%;float: left;">
                        <ul id="RemoteTreeDemo" class="ztree" ></ul>
                    </div>
                </div>
            </div>
            <div style="width: 100%;height: 40px;float: right;">
                <button type="button"  id="bdSubmitButton" class="btn blue preview" onclick="submitLocalFileData()"  style="float: right;">提交</button>
            </div>
        </div>
    </div>
    <input type="hidden" id="sql"/>
    <input type="hidden" id="connData"/>
    <input type="hidden" id="dataTaskName"/>
</div>
<div>
    <div id="Mask"></div>
    <div id="Progress" data-dimension="250" data-text="0%" data-info="导出进度" data-width="30" data-fontsize="38" data-percent="0" data-fgcolor="#61a9dc" data-bgcolor="#eee"></div>
</div>
<div id="rMenu">
    <ul style="margin-bottom: 0px !important;">
        <li id="m_add" onclick="addTreeNode(this);">增加文件夹</li>
        <li id="m_del" onclick="removeTreeNode();">删除文件夹</li>
        <%--<li id="m_check" onclick="checkTreeNode(true);">Check节点</li>--%>
        <%--<li id="m_unCheck" onclick="checkTreeNode(false);">unCheck节点</li>--%>
        <li id="m_reset" onclick="resetTree();">恢复初始目录</li>
    </ul>
</div>

<div class="modal fade" id="createFileMModal" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
    <div class="modal-dialog" role="document">
        <div class="modal-content" style="width: 66%;top: 25px;margin: 0 auto;">
            <div class="modal-header" style="height: 48px;">
                <h5 class="modal-title" id="createFileTitle" style="float: left;">创建文件夹</h5>
                <button type="button" class="close" onclick="hidenFileNameModal()">
                    <span >&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <form>
                    <div class="form-group">
                        <label for="fileName" class="col-form-label">文件夹名称</label>
                        <input type="text" autofocus class="form-control" id="fileName">
                        <span id="promptInf" style="color: red;"></span>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" id="createFileSureBut" onclick="addFilePath()">确定</button>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript" src="/console/shared/bootstrap-3.3.7/js/bootstrap-table.js"></script>
<script type="text/javascript" src="/console/datasync/js/editorLoadingJs.js"></script>
<script type="text/javascript">
   // $.ajaxSettings.async = false;
    var idNum=0;
    var sqlArray= "";
    var sqlTableArray= "";
   var remoteZTree, rMenu;
   var jsonObjectStr="";
    start();
    loadBdDataList();
    // $(function(){
    //     debugger
    //     window.parent.document.getElementById("iframe").height=$('.content').height();
    // });
    function start() {
        document.getElementById("tableLabel").style.display="none";//隐藏“选择表资源标签”
        document.getElementById("bdTableLabel").style.display="none";//隐藏“选择表资源标签”
        document.getElementById("bdDirDiv").style.display="none";//隐藏“选择表资源标签”
        document.getElementById("remoteDirDiv").style.display="none";//隐藏远程目录
        document.getElementById("bdSubmitButton").style.display="none";//隐藏“选择表资源标签”
        document.getElementById("sqlSearchDiv").style.display="none";//隐藏“选择表资源标签”
        document.getElementById("bdsc").style.display="none";//隐藏“选择表资源标签”

        $.ajax({
            type:"POST",
            url:"/searchDataList.do",
            data:{},
            success:function (dataSession) {
                var dataSessiobArray=dataSession.replace(/\[|]/g,'').split(',');
                $("#selectId").append("<option style='width: 300px;display: none;'>请选择...</option>");
                for(var i=0;i<dataSessiobArray.length;i++){
                    $("#selectId").append("<option style=width: 300px; value='"+ dataSessiobArray[i].replace(/[\r\n]/g,"")+"'>"+ dataSessiobArray[i].substr(0, dataSessiobArray[i].indexOf('$'))+"</option>");
                }
            },
            error:function () {
                console.log("请求失败")
            }
        })
    };

    //选中数据库，加载tables
    $("#selectId").on("change",function () {
        var connData = $("#selectId option:selected")[0].value;//获取数据库参数
        $.ajax({
            type:"POST",
            url:"/searchTables.do",
            data:{
                connData:connData
            },
            async:true,
            beforeSend:function(data){
                index = layer.load(1, {
                    shade: [0.5,'#fff'] //0.1透明度的白色背景
                });
            },
            success:function (data) {
                if(data.trim()=="数据库连接异常！"){
                    toastr["error"](data);
                    $("#layui-layer-shade"+index+"").remove();
                    $("#layui-layer"+index+"").remove();
                    return;
                }
                $('#tablesDiv').empty();//清空div
                document.getElementById("tableLabel").style.display="block";//显示“选择表资源标签”
                document.getElementById("sqlSearchDiv").style.display="block";//显示“选择表资源标签”
                var obj=JSON.parse(data).tableList;
                for(var i=0;i<obj.length;i++){
                    $("#tablesDiv").append("<div style='width: 200px;float:left;'><input type='checkbox' value='"+obj[i]+"'>"+obj[i]+"</input></div>");
                }
                $("#layui-layer-shade"+index+"").remove();
                $("#layui-layer"+index+"").remove();
            },
            error:function () {
                console.log("请求失败")
            }
        })

    });

    //切换数据库和本地数据源radio
    $("[name='ways']").on("change",function () {
        if(this.value =="database"){
            document.getElementById("sjk").style.display="block";//隐藏“选择表资源标签”
            document.getElementById("bdsc").style.display="none";//隐藏“选择表资源标签”
        }else {
            document.getElementById("sjk").style.display="none";//隐藏“选择表资源标签”
            document.getElementById("bdsc").style.display="block";//隐藏“选择表资源标签”
        }
    });

    //加载本地数据源列表
    function loadBdDataList() {
        $.ajax({
            type:"POST",
            url:"/searchBdDirList.do",
            data:{},
            success:function (dataSession) {
                var dataSessiobArray=dataSession.replace(/\[|]/g,'').split(',');
              //  var dataSessiobArray=dataSession.substr(1,dataSession.length-4).split(',');
                $("#selectBdDirID").append("<option style='width: 300px;display: none;'>请选择...</option>");
                for(var i=0;i<dataSessiobArray.length;i++){
                    var title=dataSessiobArray[i].substr(0, dataSessiobArray[i].indexOf('?*'));
                    var uri=dataSessiobArray[i].substr(dataSessiobArray[i].indexOf('?*')+2,dataSessiobArray[i].length);
                    $("#selectBdDirID").append("<option style=width:300px; value='"+uri.replace(/[\r\n]/g,"")+"'>"+title+"</option>");
                }
            },
            error:function () {
                console.log("请求失败")
            }
        })
    };

    //列出本地数据源中的文件树
    $("#selectBdDirID").on("change", function () {
        var localDataSource = $("#selectBdDirID option:selected")[0].value;//获取数据库参数
        $.ajax({
            type:"POST",
            url:"/getTreeOfDirList.do",
            data:{
                localDataSource: localDataSource
            },
            dataType: "text",
            async:true,
            beforeSend:function(data){
                index = layer.load(1, {
                    shade: [0.5,'#fff'] //0.1透明度的白色背景
                });
            },
            success: function (data) {
                $("#bdTableLabel").css("display", "block");//显示“选择资源”标签
                $("#bdDirDiv").css("display", "block");//显示“选择资源”标签
                $("#remoteDirDiv").css("display", "block");//显示“远程上传目录”标签
                $("#bdSubmitButton").css("display", "block"); //显示“提交”按钮

                var coreData = JSON.parse(data).list;
                var zTreeObj = $.fn.zTree.getZTreeObj("LocalTreeDemo");
                jsonObjectStr=eval(JSON.parse(data).remoteFileStr)
                if(zTreeObj!=null){
                    zTreeObj.destroy();//用之前先销毁tree
                }
                $.fn.zTree.init($("#LocalTreeDemo"), setting, coreData);
                $.fn.zTree.init($("#RemoteTreeDemo"),remoteSetting,jsonObjectStr);

                remoteZTree = $.fn.zTree.getZTreeObj("RemoteTreeDemo");
                rMenu = $("#rMenu");

                $("#layui-layer-shade"+index+"").remove();
                $("#layui-layer"+index+"").remove();
            },
            error: function (data) {
                toastr["error"]("获得本地目录中的文件树失败,请检查文件上路径及数据源！");
                console.log("获得本地目录中的文件树失败")
            }
        })
    });

    //预览按钮
    function showPreviewModal(sqlId){

        var connData = $("#selectId option:selected")[0].value;//获取数据库参数
        if(connData=="请选择..." || connData==null){
            toastr["error"]("请选择数据库！");
            //alert("请选择数据库！");
            return;
        }
        var sql=document.getElementById(sqlId.id).value;//获取输入框中的sql语句
        if(sql=="" || sql==null){
            toastr["error"]("请输入sql！");
            // alert("请输入sql！");
            return;
        }
        $("#connData").val(connData);
        $("#sql").val(sql);
        openModal();
    }

    //打开模态框
    function openModal(){
        var fatherBody = $(window.top.document.body);
        var id = 'pages';
        var dialog = $('#' + id);
        if (dialog.length == 0) {
            dialog = $('<div class="modal fade" role="dialog" id="' + id + '"/>');
            dialog.appendTo(fatherBody);
        }
        dialog.load("/console/datasync/settingtask/sqlPopPreviewWin.html", function() {
            dialog.modal({
                backdrop: false
            });
        });
            fatherBody.append("<div id='backdropId' class='modal-backdrop fade in'></div>");
    };

    // 数据库任务提交
    function submitSqlData(title){

        var checked= checkTaskData();
        if(checked==true){
            var dateDef = new Date();
            var month=dateDef.getMonth()+1;
            var dataTaskName = ""+dateDef.getFullYear()+month+dateDef.getDate()+dateDef.getHours()+dateDef.getMinutes()+dateDef.getSeconds();
            var connDataName = $("#selectId option:selected")[0].text;//获取数据源
            var connDataValue = $("#selectId option:selected")[0].value;//获取数据源value
            var sql=sqlArray;//获取sql语句
            var createNewTableName=sqlTableArray;//获取新建表名
            var checkedValue=getChecedValue();
            $.ajax({
                type:"POST",
                url:"/submitSqlData.do",
                data:{
                    connDataName:connDataName,
                    taskName:dataTaskName,
                    sql:sql,
                    createNewTableName:createNewTableName,
                    checkedValue:checkedValue,
                    connDataValue:connDataValue,
                    dataTaskName:dataTaskName
                },
                beforeSend:function(data){
                    index = layer.load(1, {
                        shade: [0.5,'#fff'] //0.1透明度的白色背景
                    });
                },
                success:function (dataSession) {
                    if(dataSession.replace(/[\r\n]/g,"")!="success"){
                        toastr["error"]("sql语句错误，请“预览”调试！");
                        $("#layui-layer-shade"+index+"").remove();
                        $("#layui-layer"+index+"").remove();
                        $('#sqlInputBox'+dataSession.split("？")[0]+'')[0].style.borderColor="red";
                        return;
                    }else{
                       parent.goToPage("datatask/dataTask.jsp");
                    }
                },
                error:function () {
                    console.log("请求失败")
                }
            })
        }else
            return;
    }

    //检测任务名称
    $("#fileName").bind("input propertychange",function(){
        //你要触发的函数内容
        if($("#fileName").val()!="" && $("#fileName").val()!=null){
            $('#checkedFileName').empty();
        }else{
            $('#checkedFileName').html("请输入任务名称！");
        }
    })

    //获取界面中所有被选中的radio
    function getChecedValueInLocalTree() {
        var pathsOfCheckedFiles = '';
        var treeObj=$.fn.zTree.getZTreeObj("LocalTreeDemo"),
            nodes=treeObj.getCheckedNodes(true),v="";
        for(var i=0;i<nodes.length;i++){
            pathsOfCheckedFiles=pathsOfCheckedFiles+nodes[i].id+";";
        }
        return pathsOfCheckedFiles;
    }

   //获取界面中所有被选中的radio
   function getChecedValueInRemoteTree() {
       var pathsOfCheckedFiles = new Array();
       var treeObj=$.fn.zTree.getZTreeObj("RemoteTreeDemo"),
           nodes=treeObj.getCheckedNodes(true),v="";
       for(var i=0;i<nodes.length;i++){
           pathsOfCheckedFiles.push(nodes[i].pid+"/"+nodes[i].name);
       }
       return pathsOfCheckedFiles;
   }

    function getChecedValue() {
        var i = 0;
        var values = '';
        var checked = $("input:checked"); //获取所有被选中的标签元素
        for (i = 0; i < checked.length; i++)
        { //将所有被选中的标签元素的值保存成一个字符串，以逗号隔开
            if (i < checked.length - 1 && $("input:checked")[i].type=='checkbox')
            {
                values += checked[i].value + ';';
            }
            else if($("input:checked")[i].type=='checkbox')
            {
                values += checked[i].value;
            }
        }
        return values;
    }

    //数据源（数据库）任务新建检测
    function checkTaskData() {
        var checked=true;
        var checkedValue=getChecedValue();
        sqlArray="";
        sqlTableArray="";
        for(var i=0;i<=idNum;i++){//sql非空校驗
            $('#sqlInputBox'+i+'')[0].style.borderColor="";
            $('#createNewTableName'+i+'')[0].style.borderColor="";
            //sql框均为空时--sql语句 表名
            if(($('#createNewTableName'+i+'').val()=="" || $('#createNewTableName'+i+'').val()==null)&&($('#sqlInputBox'+i+'').val()=="" || $('#sqlInputBox'+i+'').val()==null)){

            }else{
                if($('#sqlInputBox'+i+'').val()=="" || $('#sqlInputBox'+i+'').val()==null){
                    $('#sqlInputBox'+i+'')[0].style.borderColor="red";
                    toastr["error"]("请输入sql语句！");//alert("请输入新建表名！");
                    return;
                  }else{
                    sqlArray=sqlArray+$('#sqlInputBox'+i+'').val()+";";
                }

                if($('#createNewTableName'+i+'').val()=="" || $('#createNewTableName'+i+'').val()==null){
                    $('#createNewTableName'+i+'')[0].style.borderColor="red";
                    toastr["error"]("请输入新建表名！");//alert("请输入新建表名！");
                    return;
                }else{
                    sqlTableArray=sqlTableArray+$('#createNewTableName'+i+'').val()+";";
                 }
             }
      }
         var tableNameArray=sqlTableArray.split(";");
         for(var i=0;i<tableNameArray.length;i++){
             for(var j=0;j<=tableNameArray.length;j++){
                 if((tableNameArray[i]==tableNameArray[j])&&(i!=j)){
                     toastr["error"]("新建表名不能相同！");
                     $('#createNewTableName'+i+'')[0].style.borderColor="red";
                     $('#createNewTableName'+j+'')[0].style.borderColor="red";
                     return;
                 }
             }
         }

        if((sqlArray=="" || sqlTableArray=="")&&(checkedValue==null || checkedValue=="")){
            checked=false;
            toastr["error"]("请至少输入sql语句或选择表资源");//alert("请至少输入sql语句或选择表资源！");
            return;
        }
        return checked;
    }

    //本地文件任务提交
    function submitLocalFileData(){
        var getCheckedFile = getChecedValueInLocalTree();//获取选中的文件
        var getRemoteFile=getChecedValueInRemoteTree();//获取远程上传路径
        if(getCheckedFile=="" || getCheckedFile ==null){
            toastr["error"]("请选择文件！");
            return;
        }else if(getRemoteFile=="" || getRemoteFile==null){
            toastr["error"]("请选择远程上传路径！");
            return;
        }else{
            var dateDef = new Date();
            var month=dateDef.getMonth()+1;
            var dataTaskName = ""+dateDef.getFullYear()+month+dateDef.getDate()+dateDef.getHours()+dateDef.getMinutes()+dateDef.getSeconds();
            $("#dataTaskName").val(dataTaskName);
            var connDataName = $("#selectBdDirID  option:selected")[0].text;//获取数据源
            var connDataValue = $("#selectBdDirID  option:selected")[0].value;//获取数据源value
            var getLocalTaskName=$("#localFileName").val();//获取本地新建任务名称
            showPackProgress(0,0);
            $.ajax({
                type:"POST",
                url:"/submitFileData.do",
                timeout:600000,
                data:{
                    connDataName:connDataName,
                    getCheckedFile:getCheckedFile,
                    getLocalTaskName:getLocalTaskName,
                    connDataValue:connDataValue,
                    dataTaskName:dataTaskName,
                    "getRemoteFile":getRemoteFile[0]
                },
                success:function (dataSession) {
                    parent.goToPage("datatask/dataTask.jsp");
                    // $("#createLocalFileModal").modal("hide");//隐藏弹出框
                  //  parent.goToPage("datatask/dataTask.jsp");
                },
                error:function () {
                    console.log("请求失败")
                }
            })
            getProcess(dataTaskName,dataTaskName+ new Date().getTime());//获取上传进度
        }
        return;
    }

    //检测任务名称
    $("#localFileName").bind("input propertychange",function(){
        //你要触发的函数内容
        if($("#localFileName").val()!="" && $("#localFileName").val()!=null){
            $('#checkedLocalFileName').empty();
        }else{
            $('#checkedLocalFileName').html("请输入任务名称！");
        }
    })

    //添加sql语句
    function addSqlInput(){
        if(($('#createNewTableName'+idNum+'').val()=="" || $('#createNewTableName'+idNum+'').val()==null)&&($('#sqlInputBox'+idNum+'').val()=="" || $('#sqlInputBox'+idNum+'').val()==null)){
            toastr["error"]("请补充已有“sql查询”后再添加新的SQL查询！");
            return;
        }
        idNum=idNum+1;
        var divId="sqlSearchDiv"+idNum;
        var divSqlId="sqlInputBox"+idNum+"";
        $("#sjk").append("<div style=\"width: 100%;height: 40px;margin-top: 20px;margin-bottom: 20px; \" id=\""+divId+"\">\n" +
            " <label>sql查询</label>\n" +
            " <input id=\""+divSqlId+"\" class=\"form-control sqlStatements\" style=\"width: 300px;display: inline !important;\" type=\"text\"/>\n" +
            " <input id=\"createNewTableName"+idNum+"\"  class=\"form-control sqlStatements\" style=\"width: 100px;display: inline !important;\" type=\"text\" placeholder=\"请输入表名\"/>\n" +
            " <button type=\"button\" class=\"btn blue preview\" onclick=\"showPreviewModal("+divSqlId+")\" >预览</button>\n" +
            " <button type=\"button\" class=\"btn blue preview\" onclick=\"deleteSqlSearchDiv("+divId+")\" >删除</button>\n" +
            " </div>");
    }

    //删除新增的sql语句输入框
    function deleteSqlSearchDiv(idDom){
        idNum=idNum-1;
        idDom.remove();
    }

   var setting = {
       async: {
           enable: true,
           url:"/asyncGetNodes.do",
           autoParam:["id", "pid", "name"],
           dataFilter: filter
       },
       data: {
           simpleData: {
               enable: true,
               idKey:'id',
               pIdKey:'pid',
               rootPId: 0
           }
       },
       check: {
           enable: true
       },
       callback : {
           beforeAsync: beforeAsync,
           onAsyncSuccess: onAsyncSuccess,
           onAsyncError: onAsyncError,
           onCheck : asyncAll
       }
   };


   function filter(treeId, parentNode, childNodes) {
       childNodes=eval(childNodes);
       if(parentNode.checked==true){
           for(var num=0;num<childNodes.length;num++){
               childNodes[num].open=true;
               childNodes[num].checked=true;
           }
       }
       return childNodes;
   }

    //获取压缩进度
    function getProcess(keyID,souceID) {
        var setout= setInterval(function () {
            $.ajax({
                url:"/getPackProcess.do",
                type:"POST",
                async:true,
                data:{
                    processId:keyID
                },
                success:function (dataReult) {
                    var data=dataReult.replace(/[\r\n]/g,"");
                    var process=JSON.parse(data).list[0];//上传进度
                    var fileName="";//上传文件
                    if(JSON.parse(data).list.length>=1){
                        fileName=JSON.parse(data).list[1];//上传进度
                    }
                    //$("#layui-layer"+index+"").html("");
                    if(process >= 100){
                        $("#Progress .circle-text").text(process+"%");
                        $("#Progress .circle-info").text(fileName);
                        clearInterval(setout);
                        return;
                    }
                    $("#Progress .circle-text").text(process+"%");
                    $("#Progress .circle-info").text(fileName);
                }
            })
        },1000)
    }

    var isFirstExport=true;
    //展示压缩进度
    function showPackProgress(progress,filename){
        $("#Mask").css("height",window.innerHeight);
        $("#Mask").css("width",window.innerWidth);
        $("#Mask").show();
        if(isFirstExport){
            $("#Progress").circliful();
        }else{
            $("#Progress .circle-text").text(progress+"%");
            $("#Progress .circle-info").text(filename);
            $("#Progress").show();
        }
    }


   // 修改弹出框的title, 显示弹框
   function ShowCreateModal(title){
       if(remoteZTree.getSelectedNodes()<=0){
           toastr["error"]("请选择父节点！");
           return;
       }
       $("#createFileTitle").text(title);
       $("#promptInf")[0].textContent="";
       $("#fileName")[0].style.borderColor="#d2c3c3";
       $('#createFileMModal').modal('show');
   }
   // 关闭弹框， 获取输入值，然后执行逻辑
   $("#createFileSureBut").click(function (){
       // $("#createFileMModal").modal("hide");
       // var inputFileName = $("#fileName").val();
       // console.log("input file name : " + inputFileName);
   });
   function hidenFileNameModal() {
       $("#createFileMModal").modal("hide");
   }

   function addTreeNode(e) {
       hideRMenu();
       ShowCreateModal();

   }

   function addFilePath(){
       var inputFileName = $("#fileName").val().trim();
       if(inputFileName==null || inputFileName==""){
           $("#promptInf")[0].textContent="文件夹名称不能为空！";
           $("#fileName")[0].style.borderColor="red";
           return;
       }
       var newNode = { name:""+inputFileName+""};
       if (remoteZTree.getSelectedNodes()[0]) {
           newNode.checked = false;
           newNode.isParent=true;
           newNode.id=remoteZTree.getSelectedNodes()[0].id+"/"+newNode.name;
           remoteZTree.addNodes(remoteZTree.getSelectedNodes()[0], newNode);
       } else {
           newNode.isParent=true;
           newNode.id=remoteZTree.getSelectedNodes()[0].id+"/"+newNode.name;
           remoteZTree.addNodes(null, newNode);
       }
       $("#createFileMModal").modal("hide");
   }

   function resetTree() {
       hideRMenu();
       $.fn.zTree.init($("#RemoteTreeDemo"), remoteSetting, jsonObjectStr);
   }

   var curStatus = "init", curAsyncCount = 0, asyncForAll = false,
       goAsync = false;

   function asyncAll(event, treeId, treeNode) {
       if (!check()) {
           return;
       }
       var zTree = $.fn.zTree.getZTreeObj("LocalTreeDemo");
       if (false) {
       } else {
           var nodes=new Array([treeNode]);
           asyncNodes(nodes[0]);
       }
   };

   function asyncNodes(nodes) {
       if (!nodes) return;
       curStatus = "async";
       var zTree = $.fn.zTree.getZTreeObj("LocalTreeDemo");
       for (var i=0, l=nodes.length; i<l; i++) {
           if (nodes[i].isParent && nodes[i].zAsync) {
               asyncNodes(nodes[i].children);
               // whetherChecked=false;
           } else {
               goAsync = true;
               zTree.reAsyncChildNodes(nodes[i], "refresh", true);
           }
       }
   };

   function beforeAsync() {
       curAsyncCount++;
   }
   function onAsyncSuccess(event, treeId, treeNode, msg) {
       curAsyncCount--;
       if (curStatus == "expand") {
           expandNodes(treeNode.children);
       } else if (curStatus == "async") {
           asyncNodes(treeNode.children);
       }

       if (curAsyncCount <= 0) {
           if (curStatus != "init" && curStatus != "") {
               asyncForAll = true;
           }
           curStatus = "";
       }
   }
   function onAsyncError(event, treeId, treeNode, XMLHttpRequest, textStatus, errorThrown) {
       curAsyncCount--;

       if (curAsyncCount <= 0) {
           curStatus = "";
           if (treeNode!=null) asyncForAll = true;
       }
   }
   var curStatus = "init", curAsyncCount = 0, asyncForAll = false,
       goAsync = false;

   function expandNodes(nodes) {
       if (!nodes) return;
       curStatus = "expand";
       var zTree = $.fn.zTree.getZTreeObj("LocalTreeDemo");
       for (var i=0, l=nodes.length; i<l; i++) {
           zTree.expandNode(nodes[i], true, false, false);
           if (nodes[i].isParent && nodes[i].zAsync) {
               expandNodes(nodes[i].children);
           } else {
               goAsync = true;
           }
       }
   }

   function check() {
       if (curAsyncCount > 0) {
           return false;
       }
       return true;
   }





</script>

</body>
</html>
