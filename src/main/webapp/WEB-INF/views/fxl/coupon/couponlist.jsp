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
	
	var searchForm = $('form#searchForm');
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#select").click(function(e) {
		customerTable.ajax.reload();
		//$("#selectDiv").slideUp("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	var customerTable = $("table#customerTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/coupon/getcustomertList",
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
			"title" : '<center>用户登录名</center>',
			"data" : "fusername",
			"className": "text-center",
			"width" : "60px",
			"orderable" : false
		}, {
			"title" : '<center>用户名称</center>',
			"data" : "fname",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>性别</center>',
			"data" : "fsex",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>联系电话</center>',
			"data" : "fphone",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>状态</center>',
			"data" : "fstatusString",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}],
		"columnDefs" : [{"targets" : [1],
			"render" : function(data, type, full) {
				var retString = '<a id="customercoupon" href="javascript:;" mId="' + full.DT_RowId + '">' + data + '</a>';
				return retString;
			}
		}]
	});
	
	$("#customerTable").delegate("a[id=customercoupon]", "click", function(){
		var mId = $(this).attr("mId");
		$("#customerId").val(mId);
		couponDetailModal.modal('show');
		couponDetailTable.ajax.url("${ctx}/fxl/coupon/getcustomertdetail/" + mId).load();
	});
	
	var couponDetailModal =  $('#couponDetailModal');
	
	var couponDetailTable = $("table#couponDetailTable").DataTable({
		"language": dataTableLanguage,
		"language": dataTableLanguage,
		"filter": false,
		"paging": true,
		"autoWidth" : false,
	  	//"scrollY": "350px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/coupon/getcustomertdetail",
		    "type": "POST",
		    "data": function (data) {
		        return data;
		    }
		},
		"deferLoading": 0,
		"deferRender": true,
		"lengthChange": false,
		"retrieve": true,
		"columns" : [
		{
			"title" : '<center>活动名称</center>',
			"data" : "dtitle",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		},{
			"title" : '<center>优惠券名称</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		},{
			"title" : '<center>使用条件</center>',
			"data" : "conditions",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		},{
			"title" : '<center>状态</center>',
			"data" : "fstatusString",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		},{
			"title" : '<center>使用时间</center>',
			"data" : "usetime",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}]
	});
	
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-8"><h3>客户优惠券</h3></div>
  <div class="col-md-4"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>

<div id="selectDiv" class="text-center">
	<form id="searchForm" action="${ctx}/fxl/coupon/getcustomertList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_fhone">用户登录名：</label>
			<input type="text" id="s_fhone" name="s_fhone" class="form-control input-sm" >
		</div>
		<p class="text-center"><button id="select" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="customerTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
<!--客户优惠券详情开始-->
<div class="modal fade" id="couponDetailModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="myModalLabel">客户优惠券详情</h4>
			</div>
			<div class="modal-body">
			<input id="customerId" name="customerId" type="hidden">
				<table id="couponDetailTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span><fmt:message key="fxl.button.close" /></button>
			</div>
		</div>
	</div>
</div>
<!--客户优惠券详情结束-->
</body>
</html>