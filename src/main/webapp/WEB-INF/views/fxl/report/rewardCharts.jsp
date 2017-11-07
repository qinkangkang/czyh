<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js"></script>
<script type="text/javascript">
$(document).ready(function() {
	var searchForm = $('form#searchForm');
	//点击查询按钮
	searchForm.submit(function(e) {
		e.preventDefault();
		orderTable.ajax.reload();
	});
	// 点击清除按钮
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
    });
	// datatables 初始化	
	var orderTable = $("table#chartsTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/report/rewardCharts/list",
 		    "type": "POST",
 		    "data": function (data) {
 		    	data["s_fstatus"] = $("#s_fstatus").val();
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
			"data" : "id",
			"visible": false,
			"orderable" : false
		}, {
			"title" : '<center>用户名称</center>',
			"data" : "name",
			"className": "text-center operation",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>openid</center>',
			"data" : "weiXinId",
			"className": "text-center",
			"width" : "150px",
			"orderable" : false
		}, {
			"title" : '<center>奖励订单总数</center>',
			"data" : "rewardOrderNumber",
			"className": "text-center",
			"width" : "60px",
			"orderable" : false
		}, {
			"title" : '<center>总奖励收益</center>',
			"data" : "totalBalance",
			"className": "text-center",
			"width" : "50px",
			"orderable" : true
		}, {
			"title" : '<center>预到账金额</center>',
			"data" : "freezeBalance",
			"className": "text-center",
			"width" : "50px",
			"orderable" : true
		}, {
			"title" : '<center>已到账金额</center>',
			"data" : "arrivaled",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>已提现金额</center>',
			"data" : "withdrawalBalance",
			"className": "text-center",
			"width" : "50px",
			"orderable" : true
		}, {
			"title" : '<center>可提现余额</center>',
			"data" : "balance",
			"width" : "50px",
			"className": "text-center operation",
			"orderable" : true
		}]
	});
});
</script>
</head>
<body>
<div class="row">
  <div class="col-md-10"><h3>统计报表</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>奖励金排行榜</h4></div>
</div>
<div id="selectDiv" class="text-center">
	<form id="searchForm" action="${ctx}/fxl/report/rewardCharts" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_forderNum">用户名称：</label>
			<input type="text" id="realname" name="realname" class="form-control input-sm" >
		</div>
		<p class="text-center"><button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="chartsTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
</body>
</html>