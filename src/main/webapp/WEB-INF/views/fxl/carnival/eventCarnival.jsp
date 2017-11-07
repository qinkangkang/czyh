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
	
	$('#fdateDiv').datepicker({
	    format : "yyyy-mm-dd",
	    //startDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	var searchForm = $('form#searchForm');
	
	searchForm.submit(function(e) {
		e.preventDefault();
		eventCarnivalTable.ajax.reload();
	});
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		fsponsorSelect.val(null).trigger("change");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	
	var eventCarnivalTable = $("table#eventCarnivalTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/carnival/getEventCarnivalList",
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
			"title" : '<center>活动名称</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "150px",
			"orderable" : false
		}, {
			"title" : '<center>活动开始时间</center>',
			"data" : "fstartTime",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>活动结束时间</center>',
			"data" : "fendTime",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		},  {
			"title" : '<center>活动状态</center>',
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
				var retString = '<a id="viewBargain" href="javascript:;" mId="' + full.DT_RowId + '">' + data + '</a>';
				return retString;
		}
		}, {
			"targets" : [5],
			"render" : function(data, type, full) {
				var retString = '';
				if(data.fstatus ==10 || data.fstatus ==30){
					retString = '<div class="btn-group"><button type="button" class="btn btn-success btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">执行<span class="caret"></span></button><ul class="dropdown-menu">'
					+ '<li><a href="javascript:;" id="edit" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-edit"></span> <fmt:message key="fxl.button.edit" />活动</a></li>'
					+ '<li><a href="javascript:;" id="del" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-trash"></span> <fmt:message key="fxl.button.delete" />活动</a></li>'
					+ '<li><a href="javascript:;" id="onsale" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-open"></span> 上架活动</a></li>'
					+ '<li><a href="javascript:;" id="gift" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-gift"></span> 奖项配置</a></li>'

				}else{
					retString = '<div class="btn-group"><button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">执行<span class="caret"></span></button><ul class="dropdown-menu"><li><a href="javascript:;" id="offsale" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-save"></span> 下架活动</a></li>';
				}
				return retString;
			}
		}]
	});
	
	var eventCarnivalTable = $("table#eventCarnivalTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/carnival/getEventCarnivalList",
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
			"title" : '<center>活动名称</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "150px",
			"orderable" : false
		}, {
			"title" : '<center>活动开始时间</center>',
			"data" : "fstartTime",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>活动结束时间</center>',
			"data" : "fendTime",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		},  {
			"title" : '<center>活动状态</center>',
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
				var retString = '<a id="viewBargain" href="javascript:;" mId="' + full.DT_RowId + '">' + data + '</a>';
				return retString;
		}
		}, {
			"targets" : [5],
			"render" : function(data, type, full) {
				var retString = '';
				if(data.fstatus ==10 || data.fstatus ==30){
					retString = '<div class="btn-group"><button type="button" class="btn btn-success btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">执行<span class="caret"></span></button><ul class="dropdown-menu">'
					+ '<li><a href="javascript:;" id="edit" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-edit"></span> <fmt:message key="fxl.button.edit" />活动</a></li>'
					+ '<li><a href="javascript:;" id="del" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-trash"></span> <fmt:message key="fxl.button.delete" />活动</a></li>'
					+ '<li><a href="javascript:;" id="onsale" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-open"></span> 上架活动</a></li>'
					+ '<li><a href="javascript:;" id="gift" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-gift"></span> 奖项配置</a></li>'

				}else{
					retString = '<div class="btn-group"><button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">执行<span class="caret"></span></button><ul class="dropdown-menu"><li><a href="javascript:;" id="offsale" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-save"></span> 下架活动</a></li>';
				}
				return retString;
			}
		}]
	});
	
	$("#eventCarnivalTable").delegate("a[id=viewBargain]", "click", function(){
		window.open("${ctx}/fxl/carnival/getCarnivalPrizeListDetail/" + $(this).attr("mId")) ;
	});
	
	$("#eventCarnivalTable").delegate("a[id=del]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该嘉年华活动吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/carnival/delCarnival/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						eventCarnivalTable.ajax.reload(null,false);
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
	
	$("#eventCarnivalTable").delegate("a[id=onsale]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要即刻上架该嘉年华活动吗？',
		    okValue: '即刻上架',
		    ok: function () {
		    	$.post("${ctx}/fxl/carnival/onSaleCarnival/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						eventCarnivalTable.ajax.reload(null,false);
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
	
	$("#eventCarnivalTable").delegate("a[id=offsale]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要即刻下架该嘉年华活动吗？',
		    okValue: '即刻下架',
		    ok: function () {
		    	$.post("${ctx}/fxl/carnival/offsaleCarnival/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						eventCarnivalTable.ajax.reload(null,false);
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
    
	$("#eventCarnivalTable").delegate("a[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		$('#resetBtn').hide();
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/carnival/getCarnivalDetail/" + mId, function(data) {
			if(data.success){
				$("#createEventBounsForm #id").val(mId);
				$("#createEventBounsForm #ftitle").val(data.ftitle);
				$("#createEventBounsForm #fstartTime").val(data.fstartTime);
				$("#createEventBounsForm #fendTime").val(data.fendTime);
				$("#createEventBounsForm #fdayNumber").val(data.fdayNumber);
				$("#createEventBounsForm #fimage").val(data.fimage);
				$("#createEventBounsForm #fchannel").val(data.fchannel);
				$("#createEventBounsForm #flotteryNumber").val(data.flotteryNumber);
				$("#createEventBounsForm #fcredentialNumber").val(data.fcredentialNumber);
				$("#createEventBounsForm #fodds").val(data.fodds);
				$("#createEventBounsForm #frule").val(data.frule);
				createEventBonusModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	/* $("#eventCarnivalTable").delegate("a[id=viewBargain]", "click", function(){
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/carnival/getCarnivalDetail/" + mId, function(data) {
			if(data.success){
				$("#carnivalDetailTable #id").val(mId);
				$("#carnivalDetailTable #ftitle").text(data.ftitle);
				$("#carnivalDetailTable #fstartTime").text(data.fstartTime);
				$("#carnivalDetailTable #fendTime").text(data.fendTime);
				$("#carnivalDetailTable #fdayNumber").text(data.fdayNumber);
				$("#carnivalDetailTable #fimage").text(data.fimage);
				$("#carnivalDetailTable #fchannel").text(data.fchannel);
				$("#carnivalDetailTable #flotteryNumber").text(data.flotteryNumber);
				$("#carnivalDetailTable #fcredentialNumber").text(data.fcredentialNumber);
				$("#carnivalDetailTable #fstatusStrings").text(data.fstatus);
				$("#carnivalDetailTable #fodds").text(data.fodds);
				$("#carnivalDetailTable #carnivalUrl").text(data.carnivalUrl);				
				carnivalDetailModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	}); */

	
	
	$('#createEventBonusBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		createEventBonusModal.modal('show');
	});
	
	var carnivalDetailModal =  $('#carnivalDetailModal');
	
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
						eventCarnivalTable.ajax.reload(null,false);
						createEventBonusModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/carnival/editCarnival",  form.serialize(), function(data){
					if(data.success){
						$("#bid").val(data.id);
						console.log(data.id);
						toastr.success(data.msg);
						eventCarnivalTable.ajax.reload(null,false);
						createEventBonusModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
    
	$("#eventCarnivalTable").delegate("a[id=gift]", "click", function(){
		window.location.href = "${ctx}/fxl/carnival/gift/" + $(this).attr("mId");
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
<div class="row">
  <div class="col-md-8"><h3>嘉年华活动配置</h3></div>
  <div class="col-md-4"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>嘉年华列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createEventBonusBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 创建嘉年华活动</button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center">
	<form id="searchForm" action="${ctx}/fxl/event/getEventList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_ftitle">活动名称：</label>
			<input type="text" id="s_ftitle" name="s_ftitle" class="form-control input-sm" >
		</div>
		<div class="form-group"><label>开始时间：</label>
		<div class="input-daterange input-group date" id="createDateDiv" style="width:330px;">
		    <input type="text" class="form-control input-sm" name="fstartTime" style="cursor: pointer;">
		    <span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
		    <input type="text" class="form-control input-sm" name="fendTime" style="cursor: pointer;">
		</div></div>
		<div class="form-group"><label for="s_status">活动状态：</label>
			<select id="s_status" name="s_status" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="statusItem" items="${carnivalStatusMap}"> 
				<option value="${statusItem.key}">${statusItem.value}</option>
				</c:forEach>
			</select>
		</div>
		<p class="text-center"><button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="eventCarnivalTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>

<!-- 编辑商品开始 -->
<div class="modal fade" id="createEventBonusModal"  role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">编辑嘉年华信息</h4>
      </div>
      <form id="createEventBounsForm" action="${ctx}/fxl/carnival/addCarnival" method="post" class="form-inline" role="form">
      	<input id="id" name="id" type="hidden">
      	 <div class="modal-body">
		    <div class="form-group"><label for="ftitle">活动名称：</label>
				 <input type="text" id="ftitle" name="ftitle" class="form-control validate[minSize[1],maxSize[250]]" size="90">
			</div>
		    <div class="form-group has-error"><label for="fstartTime">活动日期：</label>
				 <div id="fdateDiv" class="input-daterange input-group date" style="width:330px;">
					<input type="text" class="form-control validate[required]" id="fstartTime" name="fstartTime" style="cursor: pointer;"><span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
					<input type="text" class="form-control validate[required]" id="fendTime" name="fendTime" style="cursor: pointer;">
				 </div>
			</div>
		    <div class="form-group has-error"><label for="fdayNumber">活动天数：</label>
				 <div class="input-group">
		    		<input type="text" id="fdayNumber" name="fdayNumber" class="form-control validate[required,custom[number]]" size="5"><div class="input-group-addon">天</div>
		    	 </div>
		    </div>
		    <br/>
		    <div class="form-group has-error"><label for="flotteryNumber">每人抽奖次数：</label>
				 <div class="input-group">
		    		<input type="text" id="flotteryNumber" name="flotteryNumber" class="form-control validate[required,custom[number]]" size="5"><div class="input-group-addon">次</div>
		    	 </div>
		    </div>
		    <div class="form-group has-error"><label for="fodds">整体中间概率：</label>
				 <div class="input-group">
		    		<input type="text" id="fodds" name="fodds" class="form-control" size="5"><div class="input-group-addon">%</div>
		    	 </div>
		    </div>
		    
		    <div class="form-group has-error"><label for="fcredentialNumber">兑奖碎片数：</label>
				 <div class="input-group">
		    		<input type="text" id="fcredentialNumber" name="fcredentialNumber" class="form-control" size="5"><div class="input-group-addon">小于抽奖次数/4</div>
		    	 </div>
		    </div>

		</div>    
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

<!--活动详情开始-->
<div class="modal fade" id="carnivalDetailModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="myModalLabel">嘉年华活动详情</h4>
			</div>
			<div class="modal-body">
				<div class="panel panel-primary">
					<div class="panel-heading"><strong>嘉年华活动信息</strong></div>
					<div class="panel-body">
						<table id="carnivalDetailTable" class="table table-hover table-striped table-bordered">
							<tr>
								<td width="15%" align="center"><strong>活动名称</strong></td>
								<td width="35%" colspan="3" align="center"><div id="ftitle"></div></td>
							</tr>
							<tr>
								<td width="15%" align="center"><strong>开始时间</strong></td>
								<td width="35%" align="center"><div id="fstartTime"></div></td>
								<td width="15%" align="center"><strong>结束时间</strong></td>
								<td width="35%" align="center"><div id="fendTime"></div></td>
							</tr>
							<tr>
								<td align="center"><strong>活动状态</strong></td>
								<td align="center"><div id="fstatusStrings"></div></td>
								<td align="center"><strong>整体中奖率</strong></td>
								<td align="center"><div id="fodds"></div></td>
							</tr>
							<tr>
								<td align="center"><strong>活动天数</strong></td>
								<td align="center"><div id="fdayNumber"></div></td>
								<td align="center"><strong>每个人抽奖次数</strong></td>
								<td align="center"><div id="flotteryNumber"></div></td>
							</tr>
							<tr>
								<td align="center"><strong>兑奖碎片数</strong></td>
								<td align="center" colspan="4"><div id="fcredentialNumber"></div></td>
							</tr>
							<tr>
								<td align="center"><strong>寻宝游戏链接</strong></td>
								<td align="center"  colspan="4"><div id="carnivalUrl"></div></td>
							</tr>
						</table>
						</div>
						</div>
						
					  <div class="panel panel-info">
					  <div class="panel-heading"><strong>奖品信息</strong></div>
					 
					  <table id="eventCarnivalTable1" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
					 
					  </div>
					    
					   <div class="panel panel-warning">
					     <div class="panel-heading"><strong>奖品信息</strong></div>
					     <table id="carnivalDetailTable" class="table table-hover table-striped table-bordered">
							<tr>
							    <td width="10%" align="center"><strong>奖品等级</strong></td>
								<td width="10%" align="center"><strong>发放天数</strong></td>
								<td width="10%" align="center"><strong>开始发放日期</strong></td>
								<td width="10%" align="center"><strong>计划发放库存</strong></td>
								<td width="10%" align="center"><strong>已发放</strong></td>
							</tr>
							<c:forEach items="${cPrizeTwoList}" var="cPrizeTwoListMap">
							 <tr>
							    <td><div id="getFremainingStock2"></div>${cPrizeTwoListMap.flevel}</td>
								<td><div id="getFremainingStock2"></div>${cPrizeTwoListMap.fcarnivalDaySerial}</td>
								<td><div id="getFremainingStock2"></div>${cPrizeTwoListMap.fcarnivalDay}</td>
								<td><div id="getFremainingStock2"></div>${cPrizeTwoListMap.fcount}</td>
								<td><div id="getFremainingStock2"></div>${cPrizeTwoListMap.facceptCount}</td>
							 </tr>
							</c:forEach>
						 </table>
					   </div>
					   
					  <div class="panel panel-danger">
					  <div class="panel-heading"><strong>奖品信息</strong></div>
					  <table id="carnivalDetailTable" class="table table-hover table-striped table-bordered">
							<tr>
							    <td width="10%" align="center"><strong>奖品等级</strong></td>
								<td width="10%" align="center"><strong>发放天数</strong></td>
								<td width="10%" align="center"><strong>开始发放日期</strong></td>
								<td width="10%" align="center"><strong>计划发放库存</strong></td>
								<td width="10%" align="center"><strong>已发放</strong></td>
							</tr>
						<c:forEach items="${cPrizeThreeList}" var="cPrizeThreeListMap">
							 <tr>
							    <td><div id=""></div>${cPrizeThreeListMap.flevel}</td>
								<td><div id=""></div>${cPrizeThreeListMap.fcarnivalDaySerial}</td>
								<td><div id=""></div>${cPrizeThreeListMap.fcarnivalDay}</td>
								<td><div id=""></div>${cPrizeThreeListMap.fcount}</td>
								<td><div id=""></div>${cPrizeThreeListMap.facceptCount}</td>
							 </tr>
						</c:forEach>
						</table>
					   </div>
					 <div class="panel-footer"><p class="text-right" style="margin: 0;"><em>零到壹，查找优惠</em></p></div>
			      </div>		   	
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span><fmt:message key="fxl.button.close" /></button>
			</div>
		</div>
	</div>
</div>
<!--活动详情结束-->

</body>
</html>