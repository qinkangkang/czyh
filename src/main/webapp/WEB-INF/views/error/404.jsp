<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
<title>访问资源不存在</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<%@ include file="/WEB-INF/views/include/include.style.jsp"%>
<%response.setStatus(200);%>
</head>
<body>
<header>
	<div><div class="cd-logo">
		<img src="${ctx}/styles/frameworks/img/hd-logo1.png" width="200" alt="Logo">
	</div></div>
</header>
<main class="cd-main-content">
<div id="main_content_container" class="main-content-container">
<div style="padding: 15px;">
	<div class="one column fluid ui grid">
		<div class="column right aligned">
			<button class="tiny ui green icon button" onclick="javascript:history.go(-1);"><i class="reply all icon"></i>&nbsp;&nbsp;&nbsp;返回</button>
		</div>
	</div>
</div>
<div style="height: 560px; width: 1000px; background-image: url('${ctx}/styles/frameworks/img/404.svg');"></div>
</div>
</main>
<!--为了保证页脚不能阻挡内容信息添加的空DIV-->
<%@ include file="/WEB-INF/views/include/footer.jsp"%>
</body>
</html>