<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css">
<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js" charset="UTF-8"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js" charset="UTF-8"></script>
<script type="text/javascript">
$(document).ready(function() {
	
	$('#PushTimeDiv').datetimepicker({
	    format : "yyyy-mm-dd hh:ii",
	    endDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	$('#createDateDiv').datepicker({
	    format : "yyyy-mm-dd",
	    endDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	$('#s_PushfixedTimeDiv').datepicker({
	    format : "yyyy-mm-dd",
	    startDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	$('#s_PushResTimesDiv').datepicker({
	    format : "yyyy-mm-dd",
	    startDate : new Date(),
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
		    "url": "${ctx}/fxl/push/pushmsg/getPushList",
 		    "type": "POST",
 		    "data": function (data) {
 		    	data["s_fauditStatus"] = $("#s_fauditStatus").val();
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
			"title" : '<center>消息标题</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		}, {
			"title" : '<center>消息内容</center>',
			"data" : "fcontent",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},/** {
			"title" : '<center>目标Id</center>',
			"data" : "ftargetObjectId",
			"className": "text-center",
			"width" : "60px",
			"orderable" : false
		}, {
			"title" : '<center>消息连接</center>',
			"data" : "furl",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},**/ {
			"title" : '<center>目标类型</center>',
			"data" : "ftargetType",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},{
			"title" : '<center>推送描述</center>',
			"data" : "fdescription",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},{
			"title" : '<center>推送时间</center>',
			"data" : "fPushTime",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},{
			"title" : '<center>推送时效</center>',
			"data" : "fvalidTime",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},{
			"title" : '<center>创建时间</center>',
			"data" : "fcreateTime",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},{
			"title" : '<center>审核时间</center>',
			"data" : "fauditTime",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},{
			"title" : '<center>推送状态</center>',
			"data" : "fstatusString",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},{
			"title" : '<center>审核状态</center>',
			"data" : "fauditStatusString",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},{
			"title" : '<center>操作人</center>',
			"data" : "foperator",
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
			"targets" : [12],
			"render" : function(data, type, full) {
				var retString = '<div class="btn-group btn-group-xs" role="group" aria-label="零到壹，查找优惠">';
				if(data.fstatus == 10){
					retString += '<button id="success" mId="' + full.DT_RowId + '" times="' + full.fPushTime + '" type="button" class="btn btn-info btn-xs">审核通过</button>';	
					retString += '<button id="defeate" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs">审核失败</button>';
					
				}else if(data.fstatus == 20){
					return '<div class="btn-group btn-group-xs" role="group" aria-label="零到壹，查找优惠"><button id="del" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs"><fmt:message key="fxl.button.delete" /></button></div>';
				}else if(data.fstatus == 30){
					return '<div class="btn-group btn-group-xs" role="group" aria-label="零到壹，查找优惠"><button id="del" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs"><fmt:message key="fxl.button.delete" /></button></div>';
				}else if(data.fstatus == 40){
					return '<div class="btn-group btn-group-xs" role="group" aria-label="零到壹，查找优惠"><button id="remove" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs">撤回推送消息</button></div>';
				}else if(data.fstatus == 50){
					return '<div class="btn-group btn-group-xs" role="group" aria-label="零到壹，查找优惠"><button id="del" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs"><fmt:message key="fxl.button.delete" /></button></div>';
				}
				retString += '</div>';
				return retString;
			}
		}]
	});	
	
	//审核成功
	$("#PushTable").delegate("button[id=success]", "click", function(){
		var mId = $(this).attr("mId");
		var times = $(this).attr("times");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要成功审核该条推送消息吗？',
		    okValue: '审核通过',
		    ok: function () {
		    	$.post("${ctx}/fxl/push/pushmsg/successPush/" + mId +"/" + times, function(data) {
					if(data.success){
						toastr.success(data.msg);
						PushTable.ajax.reload(null,false);
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
	
	//审核失败
	$("#PushTable").delegate("button[id=defeate]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要审核失败该条推送消息吗？',
		    okValue: '审核失败',
		    ok: function () {
		    	$.post("${ctx}/fxl/push/pushmsg/defeatePush/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						PushTable.ajax.reload(null,false);
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
	
	
	
	//删除功能
	$("#PushTable").delegate("button[id=del]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该条推送信息吗？',
		    okValue: '删除消息',
		    ok: function () {
		    	$.post("${ctx}/fxl/push/pushmsg/delPush/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						PushTable.ajax.reload(null,false);
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
	
	//撤回功能
	$("#PushTable").delegate("button[id=remove]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要撤回该条推送信息吗？',
		    okValue: '撤回消息',
		    ok: function () {
		    	$.post("${ctx}/fxl/push/pushmsg/removePush/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						PushTable.ajax.reload(null,false);
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
	
	
	var createCouponModal =  $('#createCouponModal');
	
	createCouponModal.on('hide.bs.modal', function(e){
		if(e.target.id === "createCouponModal"){
			createCouponForm.trigger("reset");
		}
	});
	
	$('#createCouponBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		$('#resetBtn').show();
		delBtn.hide();
		createCouponModal.modal('show')
	});
	
	var createCouponForm = $('#createCouponForm');
	
	createCouponForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	createCouponForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			if($('#actionFlag').val() == "add"){
				$.post(form.attr("action"), form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						PushTable.ajax.reload(null,false);
						createCouponModal.modal("hide");
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
						createCouponModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
	
	createCouponForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createCouponForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		createCouponForm.validationEngine('hideAll');
	});
		
	//审核备注
	$('a[id="pushexamineBtn"]').each(function(i,n){
		$(this).click(function(e){
			e.stopPropagation();
			$("#s_fauditStatus").val($(this).data("fauditstatus"));
			PushTable.ajax.reload();
		});
	});
	
	//备注功能
	$("#PushTable").delegate("tbody tr[id]", "click", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
	    var tr = $(this);
	    var row = PushTable.row(tr);
	    if(tr.data("initOk") != "ok"){
	    	var pushMessage = "";
	    	if(row.data().fauditMessage != null){
	    		pushMessage = row.data().fauditMessage;
	    	}
	    	var childDiv = $("<div class='alert alert-info' role='alert' style='margin-bottom:0px;'></div>").html("<span class='glyphicon glyphicon-hand-up' aria-hidden='true'></span> <strong>客服备注：</strong><div id='pushMessageDiv_" + row.data().DT_RowId + "'>"
	    			+ pushMessage
	    			+ '</div><div class="row"><div class="col-md-12"><div class="input-group"><input type="text" id="pushMessage_' + row.data().DT_RowId + '" mId="' + row.data().DT_RowId + '" class="form-control validate[required,maxSize[250]]" size="200"><div class="input-group-addon">回车即可上传</div></div></div></div>');
			row.child(childDiv);
			childDiv.closest("td").css("white-space","pre-line");
			tr.data("initOk","ok");
		}
	    if(row.child.isShown()){
	        row.child.hide();
	    }else{        	
	        row.child.show();
	    }
	});
	
	$("#PushTable").on('click', 'td.operation', function (event) {
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
	    return false;
	});
	
	$("#PushTable").delegate("input[id^=pushMessage_]", "keypress", function(event){
		var pushMessageInput = $(this);
		if(event.keyCode == "13" && pushMessageInput.data("running") != "ok"){
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
	<div class="col-md-10"><h3>推送审核</h3></div>
	<div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
	<div class="col-md-2"><h4>审核消息列表</h4></div>
	<div class="col-md-10"><p class="text-right"><button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
	
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
<ul class="nav nav-tabs" id="couponTab">
	<li><a id="pushexamineBtn" href="javascript:;" data-fauditstatus="10" data-toggle="tab">待审核</a></li>
	<li><a id="pushexamineBtn" href="javascript:;" data-fauditstatus="20" data-toggle="tab">审核通过</a></li>
	<li><a id="pushexamineBtn" href="javascript:;" data-fauditstatus="30" data-toggle="tab">审核失败</a></li>
</ul>
<div class="alert alert-warning text-center" role="alert" style="padding: 3px; margin-bottom: 0px;"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button><strong>点击推送消息列表中一列，即可显示该列的推送消息备注信息。</strong></div>
<table id="PushTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-bordered"></table>

</body>
</html>