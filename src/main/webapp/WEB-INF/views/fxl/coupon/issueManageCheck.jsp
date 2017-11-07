<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2-bootstrap.min.css" rel="stylesheet" type="text/css">
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
		placeholder: "选择一个活动组织者",
		allowClear: true,
		language: pickerLocal
	});
	
	var searchForm = $('form#searchForm');
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#select").click(function(e) {
		couponTable.ajax.reload();
		//$("#selectDiv").slideUp("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	var couponTable = $("table#couponTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/coupon/getDeliveryList",
 		    "type": "POST",
 		    "data": function (data) {
 		    	data["couponStartOffsetKey"] = "couponViewStartOffset";
 		    	$.each(searchForm.serializeArray(), function(i, n){
 		    		data[n.name] = n.value;
 				});
 		    	data['factivityType']=20;
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
			"title" : '<center>定向投放活动标题</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "150px",
			"orderable" : false
		},{
			"title" : '<center>活动创建时间 </center>',
			"data" : "deliveryTime",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		}, {
			"title" : '<center>创建人</center>',
			"data" : "operator",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		},{
			"title" : '<center>活动开始时间 </center>',
			"data" : "startTime",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},{
			"title" : '<center>活动结束时间 </center>',
			"data" : "endTime",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		}, {
			"title" : '<center>发放范围</center>',
			"data" : "deliveryType",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>审批状态</center>',
			"data" : "statusString",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "20px",
			"className": "text-center",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [8],
			"render" : function(data, type, full) {
				var retString = '<button id="offsale" mId="' + full.DT_RowId + '" type="button" class="btn btn-success btn-xs">查看</button>';
				if(data.status == 10){
					retString += '<br><button id="success" mId="' + full.DT_RowId + '" times="' + full.fPushTime + '" type="button" class="btn btn-info btn-xs">审核通过</button>';	
					retString += '<button id="defeate" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs">审核不通过</button>';
				}
				if(data.status == 40){
					retString += '<button id="stop" mId="' + full.DT_RowId + '" times="' + full.fPushTime + '" type="button" class="btn btn-info btn-xs">叫停</button>';
				}
				return retString;
			}
		}]
	});
	
	$("#couponTable").delegate("button[id=offsale]", "click", function(){
		var mId = $(this).attr("mId");
		window.open("${ctx}/fxl/coupon/deliveryView/" + mId );
	});
	
	$("#createEventBtn").click(function(){
		window.location.href = "${ctx}/fxl/coupon/toCouponCreateMain";
	});
	
	//审核通过
	$("#couponTable").delegate("button[id=success]", "click", function(){
		var deliveryId = $(this).attr("mId");
		var params = {};
		params['deliveryId']=deliveryId;
		params['status']=30;//审核通过
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要审核通过该客户领券活动吗？',
		    okValue: '审核通过',
		    ok: function () {
		    	$.post("${ctx}/fxl/coupon/auditDelivery",params, function(data) {
					if(data.success){
						toastr.success(data.msg);
						couponTable.ajax.reload(null,false);
					}else{
						toastr.error(data.msg);
					}
				}, "json");
		    },
		    cancelValue: '<fmt:message key="fxl.button.cancel" />',
		    cancel: function (){
		    }
		}).showModal();
	});
	
	
	//审核不通过
	$("#couponTable").delegate("button[id=defeate]", "click", function(){
		var deliveryId = $(this).attr("mId");
		var params = {};
		params['deliveryId']=deliveryId;
		params['status']=50;//审核不通过
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要审核不通过该客户领券活动吗？',
		    okValue: '审核不通过',
		    ok: function () {
		    	$.post("${ctx}/fxl/coupon/auditDelivery",params, function(data) {
					if(data.success){
						toastr.success(data.msg);
						couponTable.ajax.reload(null,false);
					}else{
						toastr.error(data.msg);
					}
				}, "json");
		    },
		    cancelValue: '<fmt:message key="fxl.button.cancel" />',
		    cancel: function (){
		    }
		}).showModal();
	});
	
	//审核通过
	$("#couponTable").delegate("button[id=stop]", "click", function(){
		var deliveryId = $(this).attr("mId");
		var params = {};
		params['deliveryId']=deliveryId;
		params['status']=120;//审核通过
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要叫停该客户领券活动吗？',
		    okValue: '叫停活动',
		    ok: function () {
		    	$.post("${ctx}/fxl/coupon/auditDelivery",params, function(data) {
					if(data.success){
						toastr.success(data.msg);
						couponTable.ajax.reload(null,false);
					}else{
						toastr.error(data.msg);
					}
				}, "json");
		    },
		    cancelValue: '<fmt:message key="fxl.button.cancel" />',
		    cancel: function (){
		    }
		}).showModal();
	});
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-8"><h3>客户领取活动审核</h3></div>
  <div class="col-md-4"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
	<div class="col-md-2">
		<h4>客户领取活动列表</h4>
	</div>
	<div class="col-md-10">
		<p class="text-right">
			<button id="selectSwitch" type="button" class="btn btn-primary">
				<span class="glyphicon glyphicon-search"></span>
				<fmt:message key="fxl.button.search" />
			</button>
		</p>
	</div>
</div>
	<div id="selectDiv" class="text-center">
	<form id="searchForm" action="${ctx}/fxl/coupon/getDeliveryList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_ftitle">客户领取活动标题：</label>
			<input type="text" id="s_ftitle" name="s_ftitle" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_status">活动状态：</label>
			<select id="s_status" name="s_status" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="couponAuditStatusMap" items="${CouponAuditStatusMap}"> 
				<option value="${couponAuditStatusMap.key}">${couponAuditStatusMap.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="form-group"><label for="s_fcreaterId">创建人员：</label>
			<select id="s_fcreaterId" name="s_fcreaterId" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="editor" items="${editorList}">
					<option value="${editor.key}">${editor.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="form-group"><label>创建时间：</label>
		<div class="input-daterange input-group date" id="createDateDiv" style="width:330px;">
		    <input type="text" class="form-control input-sm" name="fcreateTimeStart" style="cursor: pointer;">
		    <span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
		    <input type="text" class="form-control input-sm" name="fcreateTimeEnd" style="cursor: pointer;">
		</div></div>
		<p class="text-center"><button id="select" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="couponTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
</body>
</html>