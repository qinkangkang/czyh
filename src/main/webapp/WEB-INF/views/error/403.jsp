<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
<title>访问权限不足</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<%@ include file="/WEB-INF/views/include/include.style.jsp"%>
<%response.setStatus(200);%>
</head>
<body>
<div style="padding: 15px;">
	<div class="one column fluid ui grid">
		<div class="column right aligned">
			<button class="tiny ui green icon button" onclick="javascript:history.go(-1);"><i class="reply all icon"></i>&nbsp;&nbsp;&nbsp;返回</button>
		</div>
	</div>
</div>
<div style="height: 560px; width: 1000px; background-image: url('${ctx}/styles/frameworks/img/403.svg');"></div>
</div>
</body>
</html>