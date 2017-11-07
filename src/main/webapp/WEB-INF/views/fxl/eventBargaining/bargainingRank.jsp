<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2-bootstrap.min.css" rel="stylesheet" type="text/css">

<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js" charset="UTF-8"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/select2.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/i18n/zh-CN.js"></script>
<script type="text/javascript">
$(document).ready(function() {
		
	var fsponsorSelect = $("#s_ftitle").select2({
		placeholder: "选择一个活动标题",
		allowClear: true,
		language: pickerLocal
	});
	
	var searchForm = $('form#searchForm');
	
	searchForm.submit(function(e) {
		e.preventDefault();
		orderBonusTable.ajax.reload();
	});
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		fsponsorSelect.val(null).trigger("change");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	
	var orderBonusTable = $("table#orderBonusTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/eventBargaining/getBargainingRank",
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
		"lengthMenu": [[10, 20, 30, 50], [10, 20, 30, 50]],
		"lengthChange": false,
		"displayLength" : datatablePageLength,
		"columns" : [
		{	"title" : "ID",
			"data" : "DT_RowId",
			"visible": false,
			"orderable" : false
		}, {
			"title" : '<center>序号</center>',
			"data" : "rank",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		},{
			"title" : '<center>用户名</center>',
			"data" : "fname",
			"className": "text-center",
			"width" : "150px",
			"orderable" : false
		}, {
			"title" : '<center>活动名称</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>OPENID</center>',
			"data" : "fweixinId",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>原价</center>',
			"data" : "fstartPrice",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>砍掉</center>',
			"data" : "lowpic",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>砍后价格</center>',
			"data" : "fendPrice",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>参与人数</center>',
			"data" : "fbargainingCount",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>状态</center>',
			"data" : "fstatus",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}],
		"columnDefs" : []
	});

	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-8"><h3>砍一砍排行榜</h3></div>
  <div class="col-md-4"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>砍一砍列表</h4></div>
  <div class="col-md-10"><p class="text-right">
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center">
	<form id="searchForm" action="${ctx}/fxl/event/getEventList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_name">用户名：</label>
			<input type="text" id="s_name" name="s_name" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_ftitle">活动名称：</label>
	        <select id="s_ftitle" name="s_ftitle" class="form-control input-sm" style="width: 220px;">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="bargainTitleItem" items="${bargainTitleMap}"> 
				<option value="${bargainTitleItem.value}">${bargainTitleItem.value}</option>
				</c:forEach>
			</select>
	    </div>
		<div class="form-group"><label for="s_status">砍价状态：</label>
			<select id="s_status" name="s_status" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="statusItem" items="${bargainingMap}"> 
				<option value="${statusItem.key}">${statusItem.value}</option>
				</c:forEach>
			</select>
		</div>
		
		<p class="text-center"><button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="orderBonusTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>

</body>
</html>