<%@ page contentType="text/html;charset=UTF-8"%>
<!doctype html>
<html>
<head>
<title>查找优惠</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<%@ include file="/WEB-INF/views/include/include.style.jsp"%>
<sitemesh:write property='head' />
</head>
<body role="document"  >
	<img src="${ctx}/styles/fxl/images/backgroundImage.png" width="100%" height="100%" style="z-index:-100;position:absolute;left:0;top:0"/>
<!-- <nav class="navbar navbar-fixed-top navbar-default" > -->
	<div  style="border-bottom: 1px  solid rgba(200,200,200,0.25); padding:1%;" >
			<div >
				<img alt="logo" src="${ctx}/styles/fxl/images/logo-md2.png" height="5%" width="2% " style="margin-left: 2%; margin-right: 15px; "/>
				<a  href="#" style="font-size: 16px;color: #FFFFFF; "> 查找优惠运营管理系统</a> 
			</div>
			<div style="font-size:2px;margin-left:10%;color: #FFFFFF; margin-top: 1px;">ChaoZhaoYouHui management system</div>
		
	</div>
<!-- </nav> -->
<!--head区结束-->
<div style="margin-bottom: 1.5%;"></div>
<!--主内容区-->
<div role="main" class="container">
<sitemesh:write property='body' />
</div>
<!--主内容区结束-->
<!--为了保证页脚不能阻挡内容信息添加的空DIV-->
<!-- <div style="height: 60px;"></div> -->
<%-- <%@ include file="/WEB-INF/views/include/footer.jsp"%> --%>
</body>
</html>