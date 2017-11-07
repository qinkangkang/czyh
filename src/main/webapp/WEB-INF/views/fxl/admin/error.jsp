<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/webuploader/webuploader.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/pre/pre.css" rel="stylesheet" type="text/css">
<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js" charset="UTF-8"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js" charset="UTF-8"></script>
<script src="${ctx}/styles/webuploader/webuploader.html5only.min.js" type="text/javascript"></script>
<style type="text/css">
	.max-Width {max-width: 100%;word-break: break-word;}
</style>
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
		    "url": "${ctx}/fxl/admin/system/getErrorList",
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
			"title" : '<center>报错时间</center>',
			"data" : "freportTime",
			"className": "text-center",
			"width" : "150px",
			"orderable" : true
		}, {
			"title" : '<center>报错终端</center>',
			"data" : "fclientType",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		}, {
			"title" : '<center>报错信息</center>',
			"data" : "ferrorMessage",
			"className": "max-Width text-left",
			"width" : "700px",
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "100px",
			"className": "text-center",
			"orderable" : false
		} ],
		"columnDefs" : [{
			"targets" : [4],
			"render" : function(data, type, full) {
				return '<button id="view" mId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs">查看详情</button>';
			}
		}]
	});
	
	
	$("#PushTable").delegate("button[id=view]", "click", function(){
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/admin/system/getError/" + mId, function(data) {
			if(data.success){
				$("#fclientType").text(data.fclientType);
				$("#ferrorMessage").text(data.ferrorMessage);
				$("#freportTime").text(data.freportTime);
				$("#fclientInfo").text(data.fclientInfo);
				$("#fdata").text(data.fdata);
				$("#ferrorText").text(data.ferrorText);
				$("#fsystem").text(data.fsystem);
				$("#fuser").text(data.fuser);
				$("#fview").text(decodeURIComponent(data.fview));
				errorDetailModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	var errorDetailModal =  $('#errorDetailModal');
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<input id="s_fauditStatus" name="s_fauditStatus" type="hidden" value="10">
<div class="row">
	<div class="col-md-10"><h3>错误上报日志</h3></div>
	<div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
	<div class="col-md-2"><h4>错误上报日志列表</h4></div>
	<div class="col-md-10"><p class="text-right">
	<button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>

<!-- 搜索选项开始 -->
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/push/pushmsg/getPushList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_fdescription">错误类型关键字：</label>
			<input type="text" id="s_fdescription" name="s_fdescription" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="fclientType">错误终端：</label>
			<select id="fclientType" name="fclientType" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<option value="1">web</option>
				<option value="2">ios</option>
				<option value="3">android</option>
			</select>
		</div>
	    
		<div class="form-group"><label>错误上传时间：</label>
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
<!--订单详情开始-->
<div class="modal fade" id="errorDetailModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="myModalLabel">错误日志详情</h4>
			</div>
			<div class="modal-body">
				<div class="panel panel-info">
					<div class="panel-heading"><strong>错误信息</strong></div>
					<div class="panel-body">
						<table class="table table-hover table-striped table-bordered">
							<tr>
								<td align="right"><strong>错误时间</strong></td>
								<td colspan="5"><div style="max-width: 100%;word-break: break-word;" id="freportTime"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>错误设备</strong></td>
								<td colspan="5"><div style="max-width: 100%;word-break: break-word;" id="fclientInfo"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>错误信息</strong></td>
								<td colspan="5"><div style="max-width: 100%;word-break: break-word;" id="ferrorMessage"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>错误详情</strong></td>
								<td colspan="5"><div style="max-width: 100%;word-break: break-word;" id="ferrorText"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>当前系统</strong></td>
								<td colspan="5"><div style="max-width: 100%;word-break: break-word;" id="fsystem"></div></td>
							</tr>
							
							<tr>
								<td align="right"><strong>当前用户</strong></td>
								<td colspan="5"><div style="max-width: 100%;word-break: break-word;" id="fuser"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>当前页面</strong></td>
								<td colspan="5"><div style="max-width: 100%;word-break: break-word;" id="fview"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>数据对象</strong></td>
								<td colspan="5"><div style="max-width: 100%;word-break: break-word;" id="fdata"></div></td>
							</tr>
						</table>
					</div>
					<div class="panel-footer"><p class="text-right" style="margin: 0;"><em>零到壹，查找优惠</em></p></div>
				</div>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span><fmt:message key="fxl.button.close" /></button>
			</div>
		</div>
	</div>
</div>
<!--订单详情结束-->
</body>
</html>