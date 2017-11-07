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
	
	$("#fonSaleTimeDiv, #foffSaleTimeDiv").datetimepicker({
	    format : "yyyy-mm-dd hh:ii",
	    startDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	    /* ,
	    pickerPosition: "top-left" */
	});
	
	var fsponsorSelect = $("#s_fsponsor").select2({
		placeholder: "选择一个活动组织者",
		allowClear: true,
		language: pickerLocal
	});
	
	var searchForm = $('form#searchForm');
	
	searchForm.submit(function(e) {
		e.preventDefault();
		eventTable.ajax.reload();
	});
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		fsponsorSelect.val(null).trigger("change");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	var eventTable = $("table#eventTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/event/getEventList",
 		    "type": "POST",
 		    "data": function (data) {
 		    	data["statusBegin"] = "10";
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
			"title" : '<center>活动标题</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "150px",
			"orderable" : false
		}, {
			"title" : '<center>现价</center>',
			"data" : "fdeal",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>商家</center>',
			"data" : "fsponsor",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>活动<br/>类别</center>',
			"data" : "ftypeA",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>订单<br/>类型</center>',
			"data" : "forderType",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>BD</center>',
			"data" : "fbdId",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>小编</center>',
			"data" : "fcreaterId",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>上架时间</center>',
			"data" : "fonSaleTime",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>下架时间</center>',
			"data" : "foffSaleTime",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>活动<br/>状态</center>',
			"data" : "fstatusString",
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
			"targets" : [1],
			"render" : function(data, type, full) {
				return '<a id="viewEvent" href="javascript:;" mId="' + full.DT_RowId + '">' + data + '</a>';
			}
		}, {
			"targets" : [11],
			"render" : function(data, type, full) {
				if(data.fstatus == 10 || data.fstatus == 90){
					return '<button id="onsale" mId="' + full.DT_RowId + '" type="button" class="btn btn-success btn-xs">上架</button>';
				}else{
					return '<button id="offsale" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs">下架</button>';
				}
			}
		}]
	});
	
	$("#eventTable").delegate("a[id=viewEvent]", "click", function(){
		window.open("${ctx}/fxl/event/eventView/" + $(this).attr("mId")) ;
	});
	
	$("#eventTable").delegate("button[id=onsale]", "click", function(){
		var mId = $(this).attr("mId");
		$("#eventId").val(mId);
		$.post("${ctx}/fxl/event/getOnOffSale/" + mId, function(data) {
			if(data.success){
				$('#fonSaleTimeDiv').datetimepicker('update', data.fonSaleTime);
				$('#foffSaleTimeDiv').datetimepicker('update', data.foffSaleTime);
				eventOnOffSaleModal.modal("show");
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});

	$("#eventTable").delegate("button[id=view]", "click", function(){
		window.open("${ctx}/fxl/event/eventView/" + $(this).attr("mId")) ;
	});

	$("#eventTable").delegate("button[id=onsale]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要即刻上架该活动吗？',
		    okValue: '即刻上架',
		    ok: function () {
		    	$.post("${ctx}/fxl/event/onSale/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						eventTable.ajax.reload(null,false);
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
	
	$("#eventTable").delegate("button[id=offsale]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要即刻下架该活动吗？',
		    okValue: '即刻下架',
		    ok: function () {
		    	$.post("${ctx}/fxl/event/offSale/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						eventTable.ajax.reload(null,false);
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
	
	var eventOnOffSaleModal = $('#eventOnOffSaleModal');
	
	eventOnOffSaleModal.on('hide.bs.modal', function(e){
		if(e.target.id == "eventOnOffSaleModal"){
			eventOnOffSaleForm.trigger("reset");
		}
	});
	
	var eventOnOffSaleForm = $('#eventOnOffSaleForm');
	
	eventOnOffSaleForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	eventOnOffSaleForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			$.post(form.attr("action"), form.serialize() , function(data) {
				if(data.success){
					toastr.success(data.msg);
					eventTable.ajax.reload(null,false);
					eventOnOffSaleModal.modal("hide");
				}else{
					toastr.error(data.msg);
				}
				form.removeData("running");
			}, "json");
		}
	});
	
	/* var onSaleBtn = $("#onSaleBtn");
	onSaleBtn.click(function(event) {
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要即刻上架该活动吗？',
		    okValue: '确定',
		    ok: function () {
		    	$.post("${ctx}/fxl/event/onSale/" + $("#eventId").val() , function(data) {
					if(data.success){
						toastr.success(data.msg);
						eventTable.ajax.reload(null,false);
						eventOnOffSaleModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
				}, "json");
		    },
		    cancelValue: '<fmt:message key="fxl.button.cancel" />',
		    cancel: function (){
		    }
		}).showModal();
    }); */
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-10"><h3>活动上下架</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>活动列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/event/getEventList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_ftitle">活动标题：</label>
			<input type="text" id="s_ftitle" name="s_ftitle" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_fsponsor">组织者：</label>
	        <select id="s_fsponsor" name="s_fsponsor" class="form-control input-sm" style="width: 220px;">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="sponsorItem" items="${sponsorMap}"> 
				<option value="${sponsorItem.key}">${sponsorItem.value}</option>
				</c:forEach>
			</select>
	    </div>
	    <div class="form-group"><label for="s_fappChannelId">发布栏目：</label>
			<select id="s_fappChannelId" name="s_fappChannelId" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="appChannel" items="${appChannelList}">
					<option value="${appChannel.key}">${appChannel.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="form-group"><label for="s_status">活动状态：</label>
			<select id="s_status" name="s_status" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="statusItem" items="${eventStatusMap}"> 
				<option value="${statusItem.key}">${statusItem.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="form-group"><label for="s_fbdId">商家BD：</label>
			<select id="s_fbdId" name="s_fbdId" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="bd" items="${bdList}">
					<option value="${bd.key}">${bd.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="form-group"><label for="s_fcreaterId">编辑人员：</label>
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
		<p class="text-center"><button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="eventTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
<!--活动上下架开始-->
<div class="modal fade" id="eventOnOffSaleModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">请选择活动的上下架时间</h4>
      </div>
      <form id="eventOnOffSaleForm" action="${ctx}/fxl/event/eventOnOffSale" method="post" class="form-inline" role="form">
      <div class="modal-body">
    	<input id="eventId" name="eventId" type="hidden"/>
    	<div class="alert alert-info text-center" role="alert" style="padding:5px;"><strong>请根据活动时间来设定上下架时间。如果需要即刻上架，则点击选择上架时间日历后点击“今天”按钮即可。</strong></div>
    	<div class="form-group has-error"><label for="fonSaleTime">上架时间：</label>
	        <div id="fonSaleTimeDiv" class="input-group date form_datetime">
		    	<input id="fonSaleTime" name="fonSaleTime" type="text" class="form-control validate[required]" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
		    </div>
	    </div>
	    <div class="form-group"><label for="foffSaleTime">下架时间：</label>
	        <div id="foffSaleTimeDiv" class="input-group date form_datetime">
		    	<input id="foffSaleTime" name="foffSaleTime" type="text" class="form-control" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
		    </div>
	    </div>
      </div>
      <div class="modal-footer">
      	<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span> <fmt:message key="fxl.button.save" /></button>
        <button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="fxl.button.close" /></button>
      </div>
      </form>
    </div>
  </div>
</div>
<!--活动上下架结束-->
</body>
</html>