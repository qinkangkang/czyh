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
		
	$("#eventId").select2({
		placeholder: "选择一个活动名称",
		allowClear: true,
		language: pickerLocal
	});
	
	$('#createDateDiv').datepicker({
	    format : "yyyy-mm-dd",
	    endDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	$('#fdateDiv').datepicker({
	    format : "yyyy-mm-dd",
	    //startDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	$("#fuseDateDiv").datetimepicker({
	    format : "yyyy-mm-dd hh:ii",
	    //startDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true,
	    pickerPosition: "top-left"
	});
	
	var searchForm = $('form#searchForm');
	
	searchForm.submit(function(e) {
		e.preventDefault();
		eventBonusTable.ajax.reload();
	});
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		fsponsorSelect.val(null).trigger("change");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	
	var eventBonusTable = $("table#eventBonusTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/bonus/getIssueBonusList",
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
			"title" : '<center>发放名称</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>发放描述</center>',
			"data" : "fdec",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>发放积分值</center>',
			"data" : "fbonus",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>总数量</center>',
			"data" : "fcount",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>发放类型</center>',
			"data" : "ftype",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>发放时间</center>',
			"data" : "fcreateTime",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}],
		"columnDefs" : []
	});
	
	
	$('#createEventBonusBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		createEventBonusModal.modal('show');
	});
	
	var createEventBonusModal =  $('#createEventBonusModal');
	
	createEventBonusModal.on('hide.bs.modal', function(e){
		if(e.target.id === "createEventBonusModal"){
			createEventBounsForm.trigger("reset");
		}
	});
	    
    var createEventBounsForm = $('#createEventBounsForm');
    
    createEventBounsForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
    createEventBounsForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			if($('#actionFlag').val() == "add"){
				$.post(form.attr("action"), form.serialize() , function(data){
					if(data.success){
						$("#bid").val(data.id);
						//console.log(data.id);
						toastr.success(data.msg);
						eventBonusTable.ajax.reload(null,false);
						createEventBonusModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
    
    createEventBounsForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createEventBounsForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		/* createMerchantForm.validationEngine('hideAll'); */
	});
    
    /* 解决select2跟modal冲突*/
    $.fn.modal.Constructor.prototype.enforceFocus = function () {};
    
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<input id="bonusEventId" name="bonusEventId" type="hidden" value="${event.id}">
<div class="row">
  <div class="col-md-8"><h3>发放积分</h3></div>
  <div class="col-md-4"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>发放积分列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createEventBonusBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 发放积分</button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center">
	<form id="searchForm" action="${ctx}/fxl/bonus/getIssueBonusList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_ftitle">发放名称：</label>
			<input type="text" id="s_ftitle" name="s_ftitle" class="form-control input-sm" >
		</div>
		<div class="form-group"><label>发放时间：</label>
		<div class="input-daterange input-group date" id="createDateDiv" style="width:330px;">
		    <input type="text" class="form-control input-sm" name="fcreateTimeStart" style="cursor: pointer;">
		    <span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
		    <input type="text" class="form-control input-sm" name="fcreateTimeEnd" style="cursor: pointer;">
		</div></div>
		<p class="text-center"><button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="eventBonusTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>

<!-- 编辑商品开始 -->
<div class="modal fade" id="createEventBonusModal"  role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">发放积分</h4>
      </div>
      <form id="createEventBounsForm" action="${ctx}/fxl/bonus/addIssueBonus" method="post" class="form-inline" role="form">
      	<input id="id" name="id" type="hidden">
      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
		    <div class="form-group has-error"><label for="ftitle">发放名称：</label>
		    	<input type="text" id="ftitle" name="ftitle" class="form-control validate[minSize[1],maxSize[250]]" size="40">
		    </div>
		     <div class="form-group has-error"><label for="fbonus">发放积分值：</label>
		    	<input type="text" id="fbonus" name="fbonus" class="form-control validate[minSize[1],maxSize[250]]" size="20">
		    </div>
		     <div class="form-group has-error"><label for="fdesc">发放描述：</label>
		    	<input type="text" id="fdesc" name="fdesc" class="form-control validate[minSize[1],maxSize[250]]" size="90">
		    </div>
		    <div class="form-group has-error"><label for="fstartDate">用户注册时间：</label>
				 <div id="fdateDiv" class="input-daterange input-group date" style="width:330px;">
					<input type="text" class="form-control validate[required]" id="fstartDate" name="fstartDate" style="cursor: pointer;"><span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
					<input type="text" class="form-control validate[required]" id="fendDate" name="fendDate" style="cursor: pointer;">
				 </div>
			</div>
			<br/>
      <div class="modal-footer">
      	<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span> <fmt:message key="fxl.button.save" /></button>
		<button class="btn btn-warning" type="reset" id="resetBtn"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.reset" /></button>
        <button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="fxl.button.close" /></button>
      </div>
      </form>
    </div>
  </div>
</div>
<!--编辑商品结束-->
</body>
</html>