<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/webuploader/webuploader.css" rel="stylesheet" type="text/css">
<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js" charset="UTF-8"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js" charset="UTF-8"></script>
<script src="${ctx}/styles/webuploader/webuploader.html5only.min.js" type="text/javascript"></script>
<script type="text/javascript">
$(document).ready(function() {
	
	$('#createDateDiv').datepicker({
	    format : "yyyy-mm-dd",
	    endDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	var searchForm = $('form#searchForm');
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#select").click(function(e) {
		PushTable.ajax.reload();
		//$("#selectDiv").slideUp("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
    });
	
	var PushTable = $("table#PushTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/admin/system/getOperatLog",
 		    "type": "POST",
 		    "data": function (data) {
 		    	$.each(searchForm.serializeArray(), function(i, n){
 		    		data[n.name] = n.value;
 				});
 		        return data;
 		    }
		},
		"stateSave": true,
		"deferRender": true,
		//"pagingType": "full_numbers",
		"lengthMenu": [[10, 20, 30], [10, 20, 30]],
		"lengthChange": false,
		"displayLength" : datatablePageLength,
		"columns" : [
		{	"title" : "ID",
			"data" : "DT_RowId",
			"visible": false,
			"orderable" : false
		}, {
			"title" : '<center>操作人</center>',
			"data" : "username",
			"className": "text-center",
			"width" : "200px",
			"orderable" : false
		}, {
			"title" : '<center>操作模块</center>',
			"data" : "name",
			"className": "text-center",
			"width" : "200px",
			"orderable" : false
		}, {
			"title" : '<center>操作行为</center>',
			"data" : "message",
			"className": "text-center",
			"width" : "700px",
			"orderable" : false
		}, {
			"title" : '<center>操作时间</center>',
			"data" : "createTime",
			"className": "text-center",
			"width" : "200px",
			"orderable" : true
		}],
		"columnDefs" : []
	});
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<input id="s_fauditStatus" name="s_fauditStatus" type="hidden" value="10">
<div class="row">
	<div class="col-md-10"><h3>系统操作日志</h3></div>
	<div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
	<div class="col-md-2"><h4>系统操作日志列表</h4></div>
	<div class="col-md-10"><p class="text-right">
	<button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>

<!-- 搜索选项开始 -->
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/push/pushmsg/getPushList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="fclientType">操作人：</label>
			<select id="userId" name="userId" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="user" items="${userList}">
					<option value="${user.key}">${user.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="form-group"><label for="fclientType">操作模块：</label>
			<select id="czyhwebModule" name="czyhwebModule" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="czyhwebModuleItem" items="${czyhwebModule}"> 
				<option value="${czyhwebModuleItem.key}">${czyhwebModuleItem.value}</option>
				</c:forEach>
			</select>
		</div>
	    <div class="form-group"><label for="s_fdescription">操作行为：</label>
			<input type="text" id="message" name="message" class="form-control input-sm" >
		</div>
		<div class="form-group"><label>操作时间：</label>
		<div class="input-daterange input-group date" id="createDateDiv" style="width:330px;">
		    <input type="text" class="form-control input-sm" name="freportTimeStart" style="cursor: pointer;">
		    <span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
		    <input type="text" class="form-control input-sm" name="freportTimeEnd" style="cursor: pointer;">
		</div></div>
		<p class="text-center"><button id="select" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<!-- 搜索选项结束 -->
<table id="PushTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-bordered"></table>

</body>
</html>