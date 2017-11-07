<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
<title>平台内部发送错误</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<%@ include file="/WEB-INF/views/include/include.style.jsp"%>
<%response.setStatus(200);%>
</head>
<body>
<div style="padding: 15px;">
	<% Exception ex = (Exception)request.getAttribute("exception"); %> 
	<H2>Exception: <%= ex.getMessage()%></H2> 
	<P/> 
	<% ex.printStackTrace(new java.io.PrintWriter(out)); %> 
	<div class="one column fluid ui grid">
		<div class="column right aligned">
			<button class="tiny ui green icon button" onclick="javascript:history.go(-1);"><i class="reply all icon"></i>&nbsp;&nbsp;&nbsp;<fmt:message key="fxl.button.return"/></button>
		</div>
	</div>
</div>
<div style="height: 560px; width: 1000px; background-image: url('${ctx}/styles/frameworks/img/500.svg');"></div>
</div>
</body>
</html>