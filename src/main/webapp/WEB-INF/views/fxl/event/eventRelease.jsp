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
	
	var fsponsorSelect = $("#s_fsponsor").select2({
		placeholder: "选择一个活动组织者",
		allowClear: true,
		language: pickerLocal
	});
	
	var searchForm = $('form#searchForm');
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	searchForm.submit(function(e) {
		e.preventDefault();
		eventTable.ajax.reload();
	});
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		fsponsorSelect.val(null).trigger("change");
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
		    "url": "${ctx}/fxl/event/getEventListByRelease",
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
			"width" : "120px",
			"orderable" : false
		}, {
			"title" : '<center>商家</center>',
			"data" : "fsponsor",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>活动位栏目</center>',
			"data" : "releaseA",
			"className": "text-center",
			"width" : "80px",
			"orderable" : false
		}, {
			"title" : '<center>固定位栏目</center>',
			"data" : "releaseB",
			"className": "text-center",
			"width" : "80px",
			"orderable" : false
		}, {
			"title" : '<center>TAB位栏目</center>',
			"data" : "releaseC",
			"className": "text-center",
			"width" : "80px",
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
			"width" : "30px",
			"className": "text-center",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [1],
			"render" : function(data, type, full) {
				return '<a id="viewEvent" href="javascript:;" mId="' + full.DT_RowId + '">' + data + '</a>';
			}
		}, {
			"targets" : [7],
			"render" : function(data, type, full) {
				return '<button id="release" mId="' + full.DT_RowId + '" data-eventtitle="' + full.ftitle + '" type="button" class="btn btn-success btn-xs">发布</button>';
			}
		}]
	});

	$("#eventTable").delegate("a[id=viewEvent]", "click", function(){
		window.open("${ctx}/fxl/event/eventView/" + $(this).attr("mId")) ;
	});

	$("#eventTable").delegate("button[id=release]", "click", function(){
		var mId = $(this).attr("mId");
		var eventTitle = $(this).data("eventtitle");
		$("#eventId").val(mId);
		$.post("${ctx}/fxl/event/getChannelEvent/" + mId, function(data) {
			if(data.success){
				$.each(data.channelEventList, function(i, n){
					$("#channelId_" + n.fchannelID).prop("checked","true");
 				});
				$("#eventTitleTd").text(eventTitle);
				eventReleaseModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	var eventReleaseModal = $('#eventReleaseModal');
	
	eventReleaseModal.on('hide.bs.modal', function(e){
		eventReleaseForm.trigger("reset");
	});
	
	var eventReleaseForm = $('#eventReleaseForm');
	
	/* eventReleaseForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	}); */
	
	eventReleaseForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.data("running") != "ok"){
			form.data("running","ok");
			$.post(form.attr("action"), form.serialize() , function(data) {
				if(data.success){
					toastr.success(data.msg);
					eventTable.ajax.reload(null,false);
					eventReleaseModal.modal("hide");
				}else{
					toastr.error(data.msg);
				}
				form.removeData("running");
			}, "json");
		}
	});
	
	eventReleaseForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(":checkbox",eventReleaseForm).not("[isallevent]").removeAttr("checked");
		//eventReleaseForm.validationEngine('hideAll');
	});

});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-10"><h3>活动发布</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>活动列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<!-- 搜索开始 -->
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/event/getEventListByRelease" method="post" class="form-inline" role="form">
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

<!-- 活动发布开始-->
<div class="modal fade" id="eventReleaseModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h3 class="modal-title" id="myModalLabel">请选择该活动要发布到的前端栏目</h3>
			</div>
			<div class="modal-body">
				<table class="table table-hover table-striped table-bordered">
					<tr>
						<td width="20%" align="right"><strong>活动标题：</strong></td>
						<td width="80%" id="eventTitleTd"></td>
					</tr>
				</table>
				<form id="eventReleaseForm" action="${ctx}/fxl/event/eventAssociate" method="post" class="form-inline" role="form">
					<input id="eventId" name="eventId" type="hidden">
					
					<!-- 选择活动位栏目 -->
					<div class="panel panel-success">
						<div class="panel-heading">
							<strong>选择活动位栏目</strong>
						</div>
						<div class="panel-body">
							<div class="row">
							<c:forEach items="${tappChannelOneList}" var="tappChannelOneListMap">
								<div class="col-md-4">
									<div class="checkbox">
										<label><input id="channelId_${tappChannelOneListMap.DT_RowId}" name="channelId" type="checkbox"
											 <c:if test="${tappChannelOneListMap.fallEvent == 1}">
											 isallevent="1"
											 </c:if>
											 value="${tappChannelOneListMap.DT_RowId}" /> ${tappChannelOneListMap.ftitle}
										 </label>
									</div>
								</div>
							</c:forEach>
						</div>
						</div>
					</div>
	
					<!-- 选择TAB位栏目 -->
					<div class="panel panel-warning">
						<div class="panel-heading">
							<strong>选择TAB位栏目</strong>
						</div>
						<div class="panel-body">
							<div class="row">
								<c:forEach items="${tappChannelThreeList}" var="tappChannelThreeListMap">
								<div class="col-md-4">
									<div class="checkbox">
										<label><input id="channelId_${tappChannelThreeListMap.DT_RowId}" name="channelId" type="checkbox"
											 <c:if test="${tappChannelThreeListMap.fallEvent == 1}">
											 isallevent="1"
											 </c:if>
											 value="${tappChannelThreeListMap.DT_RowId}" /> ${tappChannelThreeListMap.ftitle}
										 </label>
									</div>
								</div>
							</c:forEach>
							</div>
						</div>
					</div>
				</div>
				<div class="modal-footer">
					<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span><fmt:message key="fxl.button.save" /></button>
					<button class="btn btn-warning" type="reset"><span class="glyphicon glyphicon-repeat"></span><fmt:message key="fxl.button.clear" /></button>
					<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span><fmt:message key="fxl.button.close" /></button>
				</div>
			</form>
		</div>
	</div>
</div>
<!-- 活动发布结束-->
</body>
</html>