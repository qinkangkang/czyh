<%@ page contentType="text/html;charset=UTF-8" %>
<meta name="decorator" content="/WEB-INF/decorators/decorator_no_theme.jsp">
<!doctype html>
<html>
<head>
<title>零到壹 - 活动预览</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link rel="shortcut icon" href="${ctx}/styles/fxl/images/favicon.png">
<link href="${ctx}/styles/bootstrap/css/bootstrap.min.css" rel="stylesheet">
<link href="${ctx}/styles/bootstrap/css/bootstrap-theme.min.css" rel="stylesheet">
<script src="${ctx}/styles/jquery/jquery2.min.js"></script>
<script src="${ctx}/styles/jquery/qrcode.min.js"></script>
<script type="text/javascript">
$(document).ready(function() {
	
	var viewUrl = "${viewUrl}";
	$("#eventView").attr("src",viewUrl);
	$("#eventUrl").text(viewUrl);
	
	var qrcode = new QRCode('qrcode', { 
		text: viewUrl, 
		width: 256, 
		height: 256, 
		colorDark : '#000000', 
		colorLight : '#ffffff', 
		correctLevel : QRCode.CorrectLevel.H 
	});
});
</script>
</head>
<body role="document">
<div role="main" class="container">
<div class="row">
	<div class="col-md-6 col-md-offset-3 well well-sm" style="height: 98%; overflow-x: hidden; overflow-y: hidden;">
	<iframe id="eventView" style="height: 100%; width: 100%; background-color: white;"></iframe>
	</div>
	<div class="col-md-3">
		<div class="affix">
			<p />
			<h3>用微信扫描下方二维码<br />即可查看手机浏览效果</h3>
			<p />
			<div id="qrcode"></div>
			<p />
			<div class="row">
				<div class="col-md-9"><div id="eventUrl" class="alert alert-info" role="alert"></div></div>
			</div>
		</div>
	</div>
</div>
</div>
</body>
</html>