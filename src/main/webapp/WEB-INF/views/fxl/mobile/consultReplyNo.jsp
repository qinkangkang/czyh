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
	
	var searchForm = $('form#searchForm');
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#select").click(function(e) {
		consultTable.ajax.reload();
		//$("#selectDiv").slideUp("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
    });
	
	var consultTable = $("table#consultTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/operating/consult/getConsultList",
 		    "type": "POST",
 		    "data": function (data) {
 		    	data["s_fstatus"] = $("#s_fstatus").val();
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
			"title" : '<center>活动信息</center>',
			"data" : "eventInfo",
			"className": "text-center",
			"width" : "200px",
			"orderable" : false
		}, {
			"title" : '<center>商家信息</center>',
			"data" : "sponsorInfo",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>用户信息</center>',
			"data" : "fcustomerName",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>咨询时间</center>',
			"data" : "fcreateTime",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, /**{
			"title" : '<center>回复客服</center>',
			"data" : "fuserName",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>回复时间</center>',
			"data" : "freplyTime",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, **/{
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "30px",
			"className": "text-center",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [1],
			"render" : function(data, type, full) {
				return '<a id="viewEvent" href="javascript:;" mId="' + full.fobjectId + '">' + data + '</a>';
			}
		}, {
			"targets" : [5],
			"render" : function(data, type, full) {
				var retString = '';
				retString += '<button id="reply" mId="' + full.DT_RowId + '" type="button" class="btn btn-info btn-xs">回复</button>';
				retString += '<button id="del" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs"><fmt:message key="fxl.button.delete" /></button>';
				return retString;
			}
		}]
	});
	
	$("#consultTable").delegate("a[id=viewEvent]", "click", function(){
		window.open("${ctx}/fxl/event/eventView/" + $(this).attr("mId")) ;
	});

	$("#consultTable").delegate("button[id=reply]", "click", function(){
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/operating/consult/getConsult/" + mId, function(data) {
			if(data.success){
				$("#id").val(mId);
				$("#eventInfo").html(data.eventInfo);
				$("#fcustomerName").text(data.fcustomerName);
				$("#fcontent").val(data.fcontent);
				$("#freply").val(data.freply);
				replyModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	$("#consultTable").delegate("button[id=del]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该活动咨询吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/operating/coupon/delCoupon/" + $("#id").val() , function(data) {
					if(data.success){
						toastr.success(data.msg);
						couponTable.ajax.reload(null,false);
						replyModal.modal("hide");
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
	
	var replyModal =  $('#replyModal');
	
	replyModal.on('hide.bs.modal', function(e){
		if(e.target.id === "createCouponModal"){
			replyForm.trigger("reset");
		}
	});
	
	var replyForm = $('#replyForm');
	
	replyForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	replyForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			$.post(form.attr("action"), form.serialize(), function(data){
				if(data.success){
					toastr.success(data.msg);
					consultTable.ajax.reload(null,false);
					replyModal.modal("hide");
				}else{
					toastr.error(data.msg);
				}
				form.removeData("running");
			}, "json");
		}
	});
	
	replyForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',replyForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		replyForm.validationEngine('hideAll');
	});
	
	$('a[id="consultStatusBtn"]').each(function(i,n){
		$(this).click(function(e){
			e.stopPropagation();
			$("#s_fstatus").val($(this).data("status"));
			consultTable.ajax.reload();
		});
	});

});
</script>
</head>
<body>
<input id="s_fstatus" name="s_fstatus" type="hidden" value="10">
<div class="row">
	<div class="col-md-10"><h3>回复咨询</h3></div>
	<div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
	<div class="col-md-2"><h4>回复列表</h4></div>
	<div class="col-md-10"><p class="text-right">
	<button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/operating/coupon/getApplyList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_fcustomerName">咨询用户名：</label>
			<input type="text" id="s_fcustomerName" name="s_fcustomerName" class="form-control input-sm" >
		</div>
		<!-- <div class="form-group"><label>创建时间：</label>
		<div class="input-daterange input-group date" id="createDateDiv" style="width:330px;">
		    <input type="text" class="form-control input-sm" name="fcreateTimeStart" style="cursor: pointer;">
		    <span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
		    <input type="text" class="form-control input-sm" name="fcreateTimeEnd" style="cursor: pointer;">
		</div></div> -->
		<p class="text-center"><button id="select" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>

<table id="consultTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-bordered"></table>
<!--回复咨询开始-->
<div class="modal fade" id="replyModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="myModalLabel">回复咨询信息</h4>
			</div>
			<form id="replyForm" action="${ctx}/fxl/operating/consult/reply" method="post" class="form-inline" role="form">
				<input id="id" name="id" type="hidden">
				<div class="modal-body">
					<div class="alert alert-danger text-center" role="alert" style="padding: 5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
					<div class="row">
						<div class="col-md-4"><p class="text-right"><h4>活动信息：</h4></p></div>
						<div class="col-md-8" id="eventInfo"></div>
					</div>
					<div class="row">
						<div class="col-md-4"><p class="text-right"><h4>客户名称：</h4></p></div>
						<div class="col-md-8" id="fcustomerName"></div>
					</div>
					<div class="form-group" style="min-height: 110px;"><label for="fcontent">咨询内容：</label>
				        <textarea id="fcontent" name="fcontent" cols="100%" rows="5" class="validate[maxSize[250]] form-control" readonly="readonly"></textarea>
				    </div>
				    <div class="form-group" style="min-height: 110px;"><label for="freply">回复内容：</label>
				        <textarea id="freply" name="freply" cols="100%" rows="5" class="validate[required,maxSize[250]] form-control"></textarea>
				    </div>
					<div style="clear: both;"></div>
				</div>
				<div class="modal-footer">
					<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span><fmt:message key="fxl.button.save" /></button>
					<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span><fmt:message key="fxl.button.close" /></button>
				</div>
			</form>
		</div>
	</div>
</div>
<!--回复咨询结束-->
</body>
</html>