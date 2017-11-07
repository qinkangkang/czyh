<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/webuploader/webuploader.css" rel="stylesheet" type="text/css">
<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js" charset="UTF-8"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js" charset="UTF-8"></script>
<script src="${ctx}/styles/webuploader/webuploader.html5only.min.js" type="text/javascript"></script>
<script type="text/javascript">
$(document).ready(function() {
	
	var searchForm = $('form#searchForm');
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#select").click(function(e) {
		PushTable.ajax.reload();
		//$("#selectDiv").slideUp("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
    });
	
	var PushTable = $("table#PushTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/admin/system/getNoticeList",
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
		"lengthMenu": [[10, 20, 30], [10, 20, 30]],
		"lengthChange": false,
		"displayLength" : datatablePageLength,
		"columns" : [
		{	"title" : "ID",
			"data" : "DT_RowId",
			"visible": false,
			"orderable" : false
		}, {
			"title" : '<center>公告标题</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},{
			"title" : '<center>公告内容</center>',
			"data" : "fnoticeContent",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},{
			"title" : '<center>公告类型</center>',
			"data" : "fnoticeType",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},{
			"title" : '<center>公告版本</center>',
			"data" : "fnoticeVersion",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},{
			"title" : '<center>公告创建时间</center>',
			"data" : "fcreateTime",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},{
			"title" : '<center>公告状态</center>',
			"data" : "fstatus",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},{
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "30px",
			"className": "text-center operation",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [7],
			"render" : function(data, type, full) {
				var retString = '<div class="btn-group btn-group-xs" role="group" aria-label="零到壹，查找优惠">';
				return '<div class="btn-group btn-group-xs" role="group" aria-label="零到壹，查找优惠"><button id="del" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs"><fmt:message key="fxl.button.delete" /></button></div>';
				return retString;
			}
		}]
	});
	
	//删除功能
	$("#PushTable").delegate("button[id=del]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该条公告吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/admin/system/delNotice/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						PushTable.ajax.reload(null,false);
					}else{
						toastr.error(data.msg);
					}
				}, "json");
		    },
		    cancelValue: '<fmt:message key="fxl.button.delete" />',
		    cancel: function (){
		    }
		}).showModal();
	});
	
    var delBtn = $("#delBtn");
    
	var createPushModal =  $('#createPushModal');
	
	createPushModal.on('hide.bs.modal', function(e){
		if(e.target.id === "createPushModal"){
			createCouponForm.trigger("reset");
		}
	});
	
	
    var detailPushModal =  $('#detailPushModal');
	
    detailPushModal.on('hide.bs.modal', function(e){
		if(e.target.id === "detailPushModal"){
			createCouponForm.trigger("reset");
		}
	});
	
	$('#createPushBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		$('#resetBtn').show();
		delBtn.hide();
		createPushModal.modal('show')
	});
	
	var createCouponForm = $('#createCouponForm');
	
	createCouponForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	createCouponForm.on("submit", function(push){
		if (!push.isDefaultPrevented()) {
			push.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			if($('#actionFlag').val() == "add"){
				$.post(form.attr("action"), form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						PushTable.ajax.reload(null,false);
						createPushModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/operating/coupon/editCoupon", form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						PushTable.ajax.reload(null,false);
						createPushModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
	
	$("#PushTable").delegate("button[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		$('#resetBtn').hide();
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/customer/userTags/getCustomerTags/" + mId, function(data) {
			if(data.success){
				$("#id").val(mId);
				$("#fcustomerId").val(data.fcustomerId);
				$("#ftag option[value='" + data.ftag + "']").prop("selected",true);
				$("#foperator").val(data.foperator);
				$("#fcreateTime").val(data.fcreateTime);
				createCustomerModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	
	createCouponForm.on("reset", function(push){
		if (!push.isDefaultPrevented()) {
			push.preventDefault();
		}
		$(':input',createCouponForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		createCouponForm.validationEngine('hideAll');
	});
	
	$("#PushTable").on('click', 'td.operation', function (push) {
		if (!push.isDefaultPrevented()) {
			push.preventDefault();
		}
	    return false;
	});
	
	$("#PushTable").delegate("input[id^=pushMessage_]", "keypress", function(push){
		var pushMessageInput = $(this);
		if(push.keyCode == "13" && pushMessageInput.data("running") != "ok"){
			pushMessageInput.data("running","ok");
			var mId = pushMessageInput.attr("mId");
			if(!pushMessageInput.validationEngine('validate')){
		    	$.post("${ctx}/fxl/push/saveAuditMessage", $.param({id:mId,auditMessage:pushMessageInput.val()},true), function(data) {
					if(data.success){
						toastr.success(data.msg);
						$('#pushMessageDiv_' + mId).text(data.Message);
						pushMessageInput.val("");
					}else{
						toastr.error(data.msg);
					}
					pushMessageInput.removeData("running");
				}, "json");
			}
		}
	});
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<input id="s_fauditStatus" name="s_fauditStatus" type="hidden" value="10">
<div class="row">
	<div class="col-md-10"><h3>公告管理</h3></div>
	<div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
	<div class="col-md-2"><h4>公告消息列表</h4></div>
	<div class="col-md-10"><p class="text-right"><button id="createPushBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span>添加公告</button>
	<button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>

<!-- 搜索选项开始 -->
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/push/pushmsg/getPushList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_fdescription">推送描述：</label>
			<input type="text" id="s_fdescription" name="s_fdescription" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_ftitle">消息标题：</label>
			<input type="text" id="s_ftitle" name="s_ftitle" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_ftargetType">目标类型：</label>
	        <select id="s_ftargetType" name="s_ftargetType" class="form-control input-sm" style="width: 220px;">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="couponUseItem" items="${pushUrlMap}"> 
				<option value="${couponUseItem.key}">${couponUseItem.value}</option>
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
<!-- 搜索选项结束 -->
<table id="PushTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-bordered"></table>

<!--公告消息开始-->
<div class="modal fade" id="createPushModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="myModalLabel">添加系统公告</h4>
			</div>
			
			<form id="createCouponForm" action="${ctx}/fxl/admin/system/addNotice" method="post" class="form-inline" role="form">
				<input id="id" name="id" type="hidden">
				<input id="fstatus" name="fstatus" type="hidden" value="10">
				<input id="fauditStatus" name="fauditStatus" type="hidden" value="10">
				<input id="ftype" name="ftype" type="hidden" value="0">
				<div class="modal-body">
					
					<div class="form-group has-error"><label for="ftitle">公告标题：</label>
						<input type="text" id="ftitle" name="ftitle" class="form-control validate[required,minSize[2],maxSize[250]]" size="100">
				</div>

                <div class="form-group has-error"><label for="fnoticeVersion">公告版本：</label>
						<input type="text" id="fnoticeVersion" name="fnoticeVersion" class="form-control validate[required,minSize[2],maxSize[250]]" size="100">
				</div>
				
                <div class="form-group has-error"><label for="fnoticeType">公告类型：</label>
						<select id="fnoticeType" name="fnoticeType" class="form-control validate[required]">
						    <option value=""><fmt:message key="fxl.common.select" /></option>
							<c:forEach var="noticeItem" items="${noticeMap}">
								<option value="${noticeItem.key}">${noticeItem.value}</option>
						</c:forEach>
						</select>
				</div>
							
                <div class="form-group" style="min-height: 110px;"><label for="fnoticeContent">公告内容：</label>
				        <textarea id="fnoticeContent" name="fnoticeContent" cols="100%" rows="5" class="validate[maxSize[250]] form-control"></textarea>
				</div>
				
					<div style="clear: both;"></div>
				</div>
				<div class="modal-footer">
					<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span><fmt:message key="保存" /></button>
					<button class="btn btn-warning" type="reset" id="resetBtn"><span class="glyphicon glyphicon-repeat"></span><fmt:message key="fxl.button.reset" /></button>
					<button class="btn btn-danger" type="button" id="delBtn"><span class="glyphicon glyphicon-trash"></span><fmt:message key="fxl.button.delete" /></button>
					<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span><fmt:message key="fxl.button.close" /></button>
				</div>
			</form>
		</div>
	</div>
</div>
<!--添加公告消息结束-->
</body>
</html>