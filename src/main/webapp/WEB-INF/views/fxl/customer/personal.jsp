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
		personalTable.ajax.reload();
		//$("#selectDiv").slideUp("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	var personalTable = $("table#personalTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/customer/personal/getPersonalList",
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
			"title" : '<center>用户名称</center>',
			"data" : "fname",
			"className": "text-center",
			"width" : "60px",
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
			"title" : '<center>积分</center>',
			"data" : "point",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>用户注册来源</center>',
			"data" : "fregisterChannel",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>是否关注公众号</center>',
			"data" : "fsubscribe",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>状态</center>',
			"data" : "fstatusString",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "40px",
			"className": "text-center",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [8],
			"render" : function(data, type, full) {
				if(data.fstatus == 10){
					return '<button id="enable" mId="' + full.DT_RowId + '" type="button" class="btn btn-success btn-xs">启用</button>';
				}else{
					return '<button id="disable" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs">停用</button>';
				}
				
			}
		}]
	});
	
	$("#personalTable").delegate("button[id=enable]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要即刻启用该用户吗？',
		    okValue: '即刻启用',
		    ok: function () {
		    	$.post("${ctx}/fxl/customer/personal/doEnable/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						personalTable.ajax.reload(null,false);
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
	
	$("#personalTable").delegate("button[id=disable]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要即刻停用该用户吗？',
		    okValue: '即刻停用',
		    ok: function () {
		    	$.post("${ctx}/fxl/customer/personal/doDisable/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						personalTable.ajax.reload(null,false);
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
			    content: '您确定要导出现有积分兑换订单列表的EXCEL文档吗？',
			    okValue: '导出',
			    ok: function () {
			    	exportProgressModal.modal('show');
			    	$.post("${ctx}/fxl/customer/createCustomerExcel" , searchForm.serializeArray(), function(data) {
						if(data.success){
							exportProgressModal.modal('hide');
							if(downFlag){
								$("#downFile").attr("src","${ctx}/fxl/customer/customerExcel/" + data.datePath + "/" + data.excelFileName);
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
<div class="row">
  <div class="col-md-10"><h3>个人用户</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>个人用户列表</h4></div>
  <div class="col-md-10"><p class="text-right">
	  <button id="exportExcelBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-list-alt"></span> 导出EXCEL</button>
	  <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button>
  </p></div>
</div>
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/customer/personal/getPersonalList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_fphone">手机号码：</label>
			<input type="text" id="s_fphone" name="s_fphone" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_fweixinName">微信昵称：</label>
			<input type="text" id="s_fweixinName" name="s_fweixinName" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_sex">性别：</label>
			<select id="s_sex" name="s_sex" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="sexItem" items="${sexTypeMap}"> 
				<option value="${sexItem.key}">${sexItem.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="form-group"><label for="s_status">用户状态：</label>
			<select id="s_status" name="s_status" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="userItem" items="${userTypeMap}"> 
				<option value="${userItem.key}">${userItem.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="form-group"><label for="s_registerChannel">用户来源：</label>
			<select id="s_registerChannel" name="s_registerChannel" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="channelItem" items="${channelTypeMap}"> 
				<option value="${channelItem.key}">${channelItem.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="form-group"><label for="s_subscribe">是否关注公众号：</label>
			<select id="s_subscribe" name="s_subscribe" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="subItem" items="${subTypeMap}"> 
				<option value="${subItem.key}">${subItem.value}</option>
				</c:forEach>
			</select>
		</div>
		
		<div class="form-group"><label>注册时间：</label>
		<div class="input-daterange input-group date" id="createDateDiv" style="width:330px;">
		    <input type="text" class="form-control input-sm" name="fcreateTimeStart" style="cursor: pointer;">
		    <span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
		    <input type="text" class="form-control input-sm" name="fcreateTimeEnd" style="cursor: pointer;">
		</div></div>
		<p class="text-center"><button id="select" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="personalTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
<!--编辑个人用户开始-->
<div class="modal fade" id="createPersonalModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">个人用户信息</h4>
      </div>
      <form id="createPersonalForm" action="${ctx}/fxl/customer/personal/editPersonal" method="post" class="form-inline" role="form">
      <div class="modal-body">
      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
      		<input id="id" name="id" type="hidden">
      		
		    <div style="clear:both;"></div>
      </div>
      <div class="modal-footer">
      	<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span> <fmt:message key="fxl.button.save" /></button>
        <button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="fxl.button.close" /></button>
      </div>
      </form>
    </div>
  </div>
</div>
<!--编辑个人用户结束-->
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