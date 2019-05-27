<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>Insert title here</title>
    <script type="text/javascript" src="/console/shared/js/jquery-3.2.1.min.js " ></script>
    <link rel="stylesheet" href="/console/shared/plugins/dhtmlx/layout/skins/dhtmlxlayout_dhx_skyblue.css">
    <script src="/console/shared/bootstrap-3.3.7/js/bootstrap.min.js"></script>
    <link href="/console/datasync/vdbNewLog/css/second.css" rel="stylesheet" type="text/css" />
</head>
<style>
    .foot_div {
        height: 48px;
        width: 100%;
        background: url(/console/shared/images/foot_bg.jpg) repeat-x;
        text-align: center;
        padding-top: 10px;
        color: #FFFFFF;
        position:fixed;
        bottom:-1;
    }
    .foot_top {
        height: 10px;
        background: #428BEA;
    }
    body {
        font-family: "宋体";
        font-size: 12px;
    }

    #top_left_div {
        height: 77px;
        width: 418px !important;
    }
    .top_mod li{display: inline-block;font-size:14px;margin:10px;
        line-height: 30px;width:100px;text-align: center;float: left}
    .top_mod a {
        color: #FFF !important;
    }

</style>
<body>

<div class="top_div">
    <div class="top_left_div" id="top_left_div" style="padding-left: 86px !important;"></div>
    <ul class="top_mod">
        <li>
            <a href="/console/catalog/index.jsp">数据配置</a>
        </li>
        <li>
            <a href="/console/editor/">数据管理</a>
        </li>
        <li><a href="/console/datasync/starter.jsp">数据汇交</a></li>
        <%--<li><a href="#">数据汇交</a></li>--%>
    </ul>
    <div class="top_right_div"><img src="/console/datasync/vdbNewLog/images/top_right_bg.jpg" width="350"  height="80" border="0" usemap="#Map" />
        <map name="Map" id="Map"> <area shape="rect" coords="196,19,335,46" href="/console/shared/logout.jsp" /> </map>
    </div>
</div>


<div  id="footDiv" class="foot_div" style="z-index:99999999;" >
    <div id="footTopDiv" class="foot_top" style="height: 10px;" ></div>
    <div class="foot_div" style="height: 38px !important;">
        @中国烟草总公司郑州烟草研究院 本网站由国家烟草专卖局科技司主管、中国烟草科技信息中心承办
    </div>
</div>

</body>

</html>