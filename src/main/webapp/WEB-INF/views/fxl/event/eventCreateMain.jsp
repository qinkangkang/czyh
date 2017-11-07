<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/webuploader/webuploader.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/fxl/css/webuploader_style.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/fxl/css/webuploader_demo.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2-bootstrap.min.css" rel="stylesheet" type="text/css">
<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js" charset="UTF-8"></script>
<script type="text/javascript" charset="utf-8" src="${ctx}/styles/ueditor/ueditor.config.js"></script>
<script type="text/javascript" charset="utf-8" src="${ctx}/styles/ueditor/ueditor.all.min.js"></script>
<script type="text/javascript" charset="utf-8" src="${ctx}/styles/ueditor/lang/zh-cn/zh-cn.js"></script>
<script type="text/javascript" src="${ctx}/styles/webuploader/webuploader.html5only.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/select2.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/i18n/zh-CN.js"></script>
<script type="text/javascript">
$(document).ready(function() {
	
	var eventId = $("#eventId");
	
	$('a[data-toggle="tab"]').on('show.bs.tab', function (e) {
		//e.target // activated tab
		//e.relatedTarget // previous tab
		var tab = $(e.target);
		if(tab.data("loaded") != "true"){
			var href = tab.attr("href");
			if(href == "#eventType"){
				$("#eventType").load("${ctx}/fxl/event/toEventCreateA/" + eventId.val());
				tab.data("loaded","true");
			}else if(href == "#baseInfo"){
				if($.trim(eventId.val()) != ""){
					$("#baseInfo").load("${ctx}/fxl/event/toEventCreateB/" + eventId.val());
					tab.data("loaded","true");
				}else{
					dialog({
						fixed: true,
				        title: '操作提示',
				        content: '请按照创建活动的步骤顺序填写',
				        cancelValue: '关闭',
				        cancel: function () {
				        	$('#eventTab a:first').tab('show');
				        }
				    }).showModal();
				}
			}/* else if(href == "#specInfo"){
				if($.trim(eventId.val()) != ""){
					$("#specInfo").load("${ctx}/fxl/event/toEventCreateB2/" + eventId.val());
					tab.data("loaded","true");
				}else{
					dialog({
						fixed: true,
				        title: '操作提示',
				        content: '请按照创建活动的步骤顺序填写',
				        cancelValue: '关闭',
				        cancel: function () {
				        	$('#eventTab a:first').tab('show');
				        }
				    }).showModal();
				}
			} */else if(href == "#image"){
				if($.trim(eventId.val()) != ""){
					$("#image").load("${ctx}/fxl/event/toEventCreateC/" + eventId.val());
					tab.data("loaded","true");
				}else{
					dialog({
						fixed: true,
				        title: '操作提示',
				        content: '请按照创建活动的步骤顺序填写',
				        cancelValue: '关闭',
				        cancel: function () {
				        	$('#eventTab a:first').tab('show');
				        }
				    }).showModal();
				}
			
			}else if(href == "#detailInfo"){
				if($.trim(eventId.val()) != ""){
					
					$("#detailInfo").load("${ctx}/fxl/event/toEventCreateD/" + eventId.val());
					tab.data("loaded","true");
				}else{
					dialog({
						fixed: true,
				        title: '操作提示',
				        content: '请按照创建活动的步骤顺序填写',
				        cancelValue: '关闭',
				        cancel: function () {
				        	$('#eventTab a:first').tab('show');
				        }
				    }).showModal();
				}
			}else if(href == "#detailtypeInfo"){
				if($.trim(eventId.val()) != ""){
					$("#detailtypeInfo").load("${ctx}/fxl/event/toEventCreateE/" + eventId.val());
					tab.data("loaded","true");
				}else{
					dialog({
						fixed: true,
				        title: '操作提示',
				        content: '请按照创建活动的步骤顺序填写',
				        cancelValue: '关闭',
				        cancel: function () {
				        	$('#eventTab a:first').tab('show');
				        }
				    }).showModal();
				}
			}
		}
	});
	
	$('#eventTab a:first').tab('show');

});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<input id="eventId" name="eventId" type="hidden" value="${eventId}">
<div class="row">
  <div class="col-md-8"><h3>商品信息维护</h3></div>
  <div class="col-md-4"><p class="text-right" style="margin:10px 0 0 0;">
  <button type="button" class="btn btn-primary btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/event/view'"><span class="glyphicon glyphicon-arrow-left"></span> 返回活动浏览</button>
  <button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<ul class="nav nav-tabs" id="eventTab">
	<li role="presentation"><a href="#eventType" role="tab" data-toggle="tab" data-loaded="false"><h3><span class="label label-primary">&nbsp;1&nbsp;</span>&nbsp;&nbsp;选择商品类目</h3></a></li>
	<li role="presentation"><a href="#baseInfo" role="tab" data-toggle="tab" data-loaded="false"><h3><span class="label label-success">&nbsp;2&nbsp;</span>&nbsp;基本信息</h3></a></li>
<!-- 	<li role="presentation"><a href="#specInfo" role="tab" data-toggle="tab" data-loaded="false"><h3><span class="label label-success">&nbsp;3&nbsp;</span>&nbsp;规格参数及包装信息</h3></a></li> -->
	<li role="presentation"><a href="#image" role="tab" data-toggle="tab" data-loaded="false"><h3><span class="label label-info">&nbsp;3&nbsp;</span>&nbsp;&nbsp;商品主图</h3></a></li>
	<li role="presentation"><a href="#detailInfo" role="tab" data-toggle="tab" data-loaded="false"><h3><span class="label label-warning">&nbsp;4&nbsp;</span>&nbsp;&nbsp;商品详情</h3></a></li>
	<li role="presentation"><a href="#detailtypeInfo" role="tab" data-toggle="tab" data-loaded="false"><h3><span class="label label-danger">&nbsp;5&nbsp;</span>&nbsp;&nbsp;规格参数</h3></a></li>
</ul>
<p/>
<!-- <div id="eventDiv" style="overflow-x:nano; height: 100%;"></div> -->
<div class="tab-content">
  <div role="tabpanel" class="tab-pane fade" id="eventType"></div>
  <div role="tabpanel" class="tab-pane fade" id="baseInfo"></div>
<!--   <div role="tabpanel" class="tab-pane fade" id="specInfo"></div> -->
  <div role="tabpanel" class="tab-pane fade" id="image"></div>
  <div role="tabpanel" class="tab-pane fade" id="detailInfo"></div>
  <div role="tabpanel" class="tab-pane fade" id="detailtypeInfo"></div>
</div>
</body>
</html>