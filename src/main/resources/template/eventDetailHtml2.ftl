<html class="no-js">
<head>
<title>零到壹</title>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge, chrome=1">
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta name="format-detection" content="telephone=no">
<meta name="viewport" content="width=device-width,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no">
<link rel="stylesheet" href="http://file.021-sdeals.cn/css/html.css">
<script src="http://file.021-sdeals.cn/js/layzr.min.js"></script>
<script src="http://file.021-sdeals.cn/js/html.js"></script>
</head>
<body>
<div class="content">
    <p style="margin-top: 20px; margin-bottom: 15px; white-space: normal; text-align: center;"><#if detailHtml??>${detailHtml}</#if></p>
</div>
<#if eventDetailList??>
<div class="desc-wrap">
	<#list eventDetailList as e>
	<div class="desc-box">
	    <div class="title">
	        <div class="img"><img style="height: 20px!important;width: 20px!important;" src="${e.imageUrl}"></div>
	        <div class="text">${e.title}</div>
	    </div>
	    <ul>
	    	<#list e.content as c>
	        <li>${c}</li>
	        </#list>
	    </ul>
	</div>
	</#list>
</div>
</#if>
</body>
</html>