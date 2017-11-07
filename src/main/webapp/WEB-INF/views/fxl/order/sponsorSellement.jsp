<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2.min.css" rel="stylesheet" type="text/css">
<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/select2.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/i18n/zh-CN.js"></script>
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
	
	$("#s_fsponsor").select2({
		placeholder: "选择一个商家",
		allowClear: true,
		language: pickerLocal
	});
	
	var searchForm = $('form#searchForm');
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#select").click(function(e) {
		settlementTable.ajax.reload();
		//$("#selectDiv").slideUp("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
    });
	
	var settlementTable = $("table#settlementTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/order/getSettlementList",
 		    "type": "POST",
 		    "data": function (data) {
 		    	$.each(searchForm.serializeArray(), function(i, n){
 		    		data[n.name] = n.value;
 				});
 		    	data['s_fsponsor'] = $("#tSponsorId").val();
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
			"title" : '<center>结算单编号</center>',
			"data" : "fstatementNum",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>结算开始时间</center>',
			"data" : "fbeginTime",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>结算止时间</center>',
			"data" : "fendTime",
			"className": "text-center",
			"width" : "40px",
			"orderable" : true
		},  {
			"title" : '<center>实收金额</center>',
			"data" : "fpadinAmount",
			"className": "text-center",
			"width" : "40px",
			"orderable" : true
		}, {
			"title" : '<center>优惠金额</center>',
			"data" : "forderChangelAmount",
			"className": "text-center",
			"width" : "40px",
			"orderable" : true
		},{
			"title" : '<center>结算总额</center>',
			"data" : "famount",
			"className": "text-center",
			"width" : "40px",
			"orderable" : true
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "30px",
			"className": "text-center",
			"orderable" : false
		}],
		"columnDefs" : [ {
			"targets" : [7],
			"render" : function(data, type, full) {
				//retString = '<button id="delete" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs"><fmt:message key="fxl.button.delete" /></button>';
				return '<button id="detail" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs">详情</button>';
			}
		}]
	});
	
	$("#settlementTable").delegate("button[id=detail]", "click", function(){
		window.open("${ctx}/fxl/order/toSettlementDetail/" + $(this).attr("mId")) ;
	});
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<input id="s_fstatus" name="s_fstatus" type="hidden" value="10">
<input id="tSponsorId" name="tSponsorId" type="hidden" value="${tSponsor.id}">
<div class="row">
  <div class="col-md-10"><h3>商家结算单列表</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="panel panel-info">
	<div class="panel-heading">商家信息</div>
	<div class="panel-body">
		<table class="table table-hover table-striped table-bordered">
			<tr>
				<td width="20%" align="right"><strong>商家名称：</strong></td>
				<td width="30%">${tSponsor.fname}</td>
				<td width="20%" align="right"><strong>商家全称：</strong></td>
				<td width="30%">${tSponsor.ffullName}</td>
			</tr>
			<tr>
				<td width="20%" align="right"><strong>商家电话：</strong></td>
				<td width="30%">${tSponsor.fphone}</td>
				<td width="20%" align="right"><strong>运营负责人：</strong></td>
				<td width="30%">${bdName}</td>
			</tr>
		</table>
	</div>
	<div class="panel-footer"><p class="text-right" style="margin: 0;"><em>零到壹，查找优惠</em></p></div>
</div>
<div id="selectDiv" class="text-center">
	<form id="searchForm" action="${ctx}/fxl/event/getEventList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_forderNum">结算单编号：</label>
			<input type="text" id="s_forderNum" name="s_forderNum" class="form-control input-sm" >
		</div>
		<div class="form-group"><label>结算单周期：</label>
		<div class="input-daterange input-group date" id="createDateDiv" style="width:330px;">
		    <input type="text" class="form-control input-sm" name="fcreateTimeStart" style="cursor: pointer;">
		    <span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
		    <input type="text" class="form-control input-sm" name="fcreateTimeEnd" style="cursor: pointer;">
		</div></div>
		<p class="text-center"><button id="select" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="settlementTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-bordered"></table>

</body>
</html>