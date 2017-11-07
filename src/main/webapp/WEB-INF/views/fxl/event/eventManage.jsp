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
		placeholder: "选择一个商品组织者",
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
			"title" : '<center>商品名称</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "150px",
			"orderable" : false
		}, {
			"title" : '<center>现价</center>',
			"data" : "fpriceMoney",
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
			"title" : '<center>商品<br/>类别</center>',
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
			"title" : '<center>上架方式</center>',
			"data" : "falg",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>商品<br/>状态</center>',
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
				var retString = '';
				if(data.fstatus <= 10 || data.fstatus == 90){
					retString = '<div class="btn-group"><button type="button" class="btn btn-success btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">执行<span class="caret"></span></button><ul class="dropdown-menu">'
					+ '<li><a href="javascript:;" id="edit" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-edit"></span> <fmt:message key="fxl.button.edit" />商品</a></li>'
					+ '<li><a href="javascript:;" id="del" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-trash"></span> <fmt:message key="fxl.button.delete" />商品</a></li>'
					+ '<li><a href="javascript:;" id="onsale" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-open"></span> 上架商品</a></li>'
					+ '<li><a href="javascript:;" id="viewSku" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-open"></span> 查看库存</a></li>'
					if(data.fsdealsModel ==0){
						retString += '<li><a href="javascript:;" id="release" mId="' + full.DT_RowId + '" data-eventtitle="' + full.ftitle + '"><span class="glyphicon glyphicon-circle-arrow-up"></span> 发布商品</a></li>';
					}
					
				}else{
					retString = '<div class="btn-group"><button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">执行<span class="caret"></span></button><ul class="dropdown-menu"><li><a href="javascript:;" id="offsale" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-save"></span> 下架商品</a></li>'
					if(data.fsdealsModel ==0){
						retString += '<li><a href="javascript:;" id="release" mId="' + full.DT_RowId + '" data-eventtitle="' + full.ftitle + '"><span class="glyphicon glyphicon-circle-arrow-up"></span> 发布商品</a></li>';
					}
				}
				//retString += '<li><a href="javascript:;" id="onOff" mId="' + full.DT_RowId + '">定时上下架</a></li></ul></div>';
				return retString;
			}
		}]
	});
	
	$("#eventTable").delegate("a[id=viewSku]", "click", function(){
		window.open("${ctx}/fxl/event//toSs/" + $(this).attr("mId"),"_self") ;
	});
	
	$("#eventTable").delegate("a[id=viewEvent]", "click", function(){
		window.open("${ctx}/fxl/event/eventView/" + $(this).attr("mId")) ;
	});

	$("#eventTable").delegate("a[id=edit]", "click", function(){
		window.location.href = "${ctx}/fxl/event/toEventCreateMain/" + $(this).attr("mId");
	});
	
	$("#eventTable").delegate("a[id=ss]", "click", function(){
		window.location.href = "${ctx}/fxl/event/toSs/" + $(this).attr("mId");
	});
	
	$("#eventTable").delegate("a[id=del]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该商品吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/event/delEvent/" + mId , function(data) {
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
	
	$("#eventTable").delegate("a[id=onsale]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要即刻上架该商品吗？',
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
	
	$("#eventTable").delegate("a[id=offsale]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要即刻下架该商品吗？',
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
	
	$("#eventTable").delegate("a[id=onOff]", "click", function(){
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
	
	//商品发布
	$("#eventTable").delegate("a[id=release]", "click", function(){
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
	
	eventReleaseForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.data("running") != "ok"){
			form.data("running","ok");
			$.post(form.attr("action"), $.param($.merge(form.serializeArray(),[{name:"eventId", value:$("#eventId").val()}]),true), function(data){
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
	
	
	$("#createEventBtn").click(function(){
		window.location.href = "${ctx}/fxl/event/toEventCreateMain";
	});
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-8"><h3>商品维护</h3></div>
  <div class="col-md-4"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>商品列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createEventBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 创建商品</button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center">
	<form id="searchForm" action="${ctx}/fxl/event/getEventList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_ftitle">商品名称：</label>
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
		<div class="form-group"><label for="s_status">商品状态：</label>
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
<!--商品上下架开始-->
<div class="modal fade" id="eventOnOffSaleModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">请选择商品的上下架时间</h4>
      </div>
      <form id="eventOnOffSaleForm" action="${ctx}/fxl/event/eventOnOffSale" method="post" class="form-inline" role="form">
      <div class="modal-body">
    	<input id="eventId" name="eventId" type="hidden"/>
    	<div class="form-group has-error"><label for="fonSaleTime">上架时间：</label>
	        <div id="fonSaleTimeDiv" class="input-group date form_datetime">
		    	<input id="fonSaleTime" name="fonSaleTime" type="text" class="form-control" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
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
<!--商品上下架结束-->

<!-- 商品发布 -->
<div class="modal fade" id="eventReleaseModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h3 class="modal-title" id="myModalLabel">请选择该商品要发布到的前端栏目</h3>
			</div>
			<div class="modal-body">
				<table class="table table-hover table-striped table-bordered">
					<tr>
						<td width="20%" align="right"><strong>商品标题：</strong></td>
						<td width="80%" id="eventTitleTd"></td>
					</tr>
				</table>
				<form id="eventReleaseForm" action="${ctx}/fxl/event/eventAssociate" method="post" class="form-inline" role="form">
					
					<!-- 选择商品位栏目 -->
					<div class="panel panel-success">
						<div class="panel-heading">
							<strong>选择商品位栏目</strong>
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
<!-- 商品发布结束-->
</body>
</html>