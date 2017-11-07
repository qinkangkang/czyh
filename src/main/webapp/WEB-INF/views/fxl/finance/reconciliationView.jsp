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
	
	$('#createDateDiv').datepicker({
	    format : "yyyy-mm-dd",
	    endDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	var fpayTypeSelect = $("#o_fpayType").select2({
		placeholder: "选择支付方式",
		allowClear: true,
		language: pickerLocal
	});
	
	var fpayTypeSelect = $("#o_forderStatus").select2({
		placeholder: "选择资金出入",
		allowClear: true,
		language: pickerLocal
	});
	
	var fpayTypeSelect = $("#t_fsponsorFullName").select2({
		placeholder: "选择流水店铺",
		allowClear: true,
		language: pickerLocal
	});
	
	var searchForm = $('form#searchForm');
	
	searchForm.submit(function(e) {
		e.preventDefault();
		reconciliationTable.ajax.reload();
	});
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		fpayTypeSelect.val(null).trigger("change");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	var reconciliationTable = $("table#reconciliationTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/finance/getReconciliationList",
 		    "type": "POST",
 		    "data": function (data) {
 		    	data["eventStartOffsetKey"] = "eventViewStartOffset";
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
			"title" : '<center>用户名</center>',
			"data" : "fname",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		}, {
			"title" : '<center>手机号</center>',
			"data" : "fphone",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>订单号</center>',
			"data" : "forderNum",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		}, {
			"title" : '<center>邮费</center>',
			"data" : "fpostage",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>订单金额</center>',
			"data" : "ftotal",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>订单状态</center>',
			"data" : "fstatus",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>支付时间</center>',
			"data" : "fpayTime",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>支付方式</center>',
			"data" : "fchannelString",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}]
	});
			
		
var exportProgressModal =  $('#exportProgressModal');
	
	var downFlag = false;
	$("#exportExcelBtn").click(function(e){
		e.stopPropagation();
		var exportExcelBtn = $(this);
		if(exportExcelBtn.data("running") != "ok"){
			exportExcelBtn.data("running","ok");
			downFlag = true;
			var mId = $(this).attr("mId");
			dialog({
				fixed: true,
			    title: '操作提示',
			    content: '您确定要导出当前财务对账列表的EXCEL文档吗？',
			    okValue: '导出',
			    ok: function () {
			    	exportProgressModal.modal('show');
			    	$.post("${ctx}/fxl/finance/createReconciliationExcel" , searchForm.serializeArray(), function(data) {
						if(data.success){
							exportProgressModal.modal('hide');
							if(downFlag){
								$("#downFile").attr("src","${ctx}/fxl/finance/exportExcel/" + data.datePath + "/" + data.excelFileName);
							}
						}else{
							toastr.error(data.msg);
						}
						exportExcelBtn.removeData("running");
					}, "json");
			    },
			    cancelValue: '<fmt:message key="fxl.button.cancel" />',
			    cancel: function (){
			    }
			}).showModal();
		}
	});
	
	$("#exportStopBtn").click(function(e){
		e.stopPropagation();
		downFlag = false;
		exportProgressModal.modal('hide');
	});
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-8"><h3>商户对账管理</h3></div>
  <div class="col-md-4"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>商户对账列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="exportExcelBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span>下载对账单</button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center">
	<form id="searchForm" action="${ctx}/fxl/finance/getReconciliationList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="o_fpayType">支付方式：</label>
	        <select id="o_fpayType" name="o_fpayType" class="form-control input-sm" style="width: 220px;">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<option value="wx">微信支付</option>
				<option value="wx_pub">微信H5支付</option>
				<option value="alipay">支付宝支付</option>
			</select>
	    </div>
	    <div class="form-group"><label for="o_forderStatus">资金出入：</label>
	        <select id="o_forderStatus" name="o_forderStatus" class="form-control input-sm" style="width: 220px;">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<option value="20">入账</option>
				<option value="120">出账</option>
			</select>
	    </div>
		<div class="form-group"><label for="t_fsponsorFullName">流水店铺：</label>
	        <select id="t_fsponsorFullName" name="t_fsponsorFullName" class="form-control input-sm" style="width: 220px;">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="sponsorItem" items="${sponsorMap}"> 
				<option value="${sponsorItem.key}">${sponsorItem.value}</option>
				</c:forEach>
			</select>
	    </div>
		<div class="form-group"><label>查账日期：</label>
		<div class="input-daterange input-group date" id="createDateDiv" style="width:330px;">
		    <input type="text" class="form-control input-sm" name="fpayTimeStart" style="cursor: pointer;">
		    <span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
		    <input type="text" class="form-control input-sm" name="fpayTimeEnd" style="cursor: pointer;">
		</div></div>
		<p class="text-center"><button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="reconciliationTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>

<!--订单详情结束-->
<!--图片上传进度条开始-->
<div class="modal fade" id="exportProgressModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <h4 class="modal-title" id="myModalLabel">请稍等，后台正在生成EXCEL文档……</h4>
      </div>
      <div class="modal-body">
      	<div class="progress">
			<div id="exportProgress" class="progress-bar progress-bar-info progress-bar-striped active" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%"></div>
		</div>
      </div>
      <div class="modal-footer">
		<button id="exportStopBtn" type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> 取消生成</button>
      </div>
      </form>
    </div>
  </div>
</div>
<!--图片上传进度条结束-->
<iframe id="downFile" height="0" width="0" style="display: none;"></iframe>
</body>
</html>