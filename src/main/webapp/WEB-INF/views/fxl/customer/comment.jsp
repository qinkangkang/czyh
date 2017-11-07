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
$(document).ready(function(){
	
	var searchForm = $('form#searchForm');
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#select").click(function(e) {
		commentTable.ajax.reload();
		//$("#selectDiv").slideUp("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
    });
	
	$('#createDateDiv').datepicker({
	    format : "yyyy-mm-dd",
	    endDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	var commentTable = $("table#commentTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
	  	"paging": true,
		"scrollCollapse": true,
		"serverSide": true,
		"stateSave": true,
		"deferRender": true,
		//"pagingType": "full_numbers",
		"lengthMenu": [[10, 20, 30, 50], [10, 20, 30, 50]],
		"lengthChange": false,
		"displayLength" : "10",
		"ajax": {
		    "url": "${ctx}/fxl/customer/getCustomerCommentList",
 		    "type": "POST",
 		    "data": function (data) {
 				data["s_fstatus"] = $("#s_fstatus").val();
 				$.each(searchForm.serializeArray(), function(i, n){
 		    		data[n.name] = n.value;
 				});
 		        return data;
 		    }
		},
		"columns" : [{
			"title" : "ID",
			"data" : "DT_RowId",
			"visible": false,
			"orderable" : false
		}, {
			"title" : '<center>序号</center>',
			"data" : "serNum",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>评价活动</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		}, {
			"title" : '<center>活动商家</center>',
			"data" : "sponsorInfo",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>评价客户</center>',
			"data" : "fcustomerName",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>评价时间</center>',
			"data" : "fcreateTime",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>评价内容</center>',
			"data" : "fcontent",
			"className": "text-left",
			"width" : "100px",
			"orderable" : false
		}, {
			"title" : '<center>评论星级</center>',
			"data" : "fscore",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>回复客服</center>',
			"data" : "fuserName",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>回复时间</center>',
			"data" : "freplyTime",
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
			"targets" : [2],
			"render" : function(data, type, full) {
				return '<a id="viewEvent" href="javascript:;" mId="' + full.fobjectId + '">' + data + '</a>';
			}
		}, {
			"targets" : [10],
			"render" : function(data, type, full) {
				if (data.fstatus == 10 ) {
					return '<button id="del" cId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs"><fmt:message key="fxl.button.delete" /></button>'
					+'<button id="passedok" cId="' + full.DT_RowId + '" type="button" class="btn  btn-info btn-xs">审核通过</button>'
					+'<button id="passedno" cId="' + full.DT_RowId + '" type="button" class="btn  btn-danger btn-xs">审核不通过</button>';
				} else if (data.fstatus == 20 ) {
					return '<button id="edit" cId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs">排序</button>'
					+'<button id="reply" mId="' + full.DT_RowId + '" type="button" class="btn btn-info btn-xs">回复</button>'
					+'<button id="del" cId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs"><fmt:message key="fxl.button.delete" /></button>';
				} else if (data.fstatus == 30 ) {
					return '<button id="del" cId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs"><fmt:message key="fxl.button.delete" /></button>';
				} else if (data.fstatus == 40 ) {
					return '<button id="del" cId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs"><fmt:message key="fxl.button.delete" /></button>';
				} 
			}
		}]
	});
	
	$("#commentTable").delegate("a[id=viewEvent]", "click", function(){
		window.open("${ctx}/fxl/event/eventView/" + $(this).attr("mId")) ;
	});
	
	$("#commentTable").delegate("button[id=reply]", "click", function(){
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/customer/comment/getComment/" + mId, function(data) {
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
			$.post(form.attr("action"), $.param($.merge(form.serializeArray(),[{name:"id", value:$("#id").val()}]),true), function(data){
				if(data.success){
					toastr.success(data.msg);
					commentTable.ajax.reload(null,false);
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
	
	//审核成功
	$("#commentTable").delegate("button[id=passedok]", "click", function(){
		var cId = $(this).attr("cId");
		//var times = $(this).attr("times");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要审核通过这条评价吗？',
		    okValue: '审核通过',
		    ok: function () {
		    	/* $.post("${ctx}/fxl/push/pushmsg/successPush/" + mId +"/" + times, function(data) {
					if(data.success){
						toastr.success(data.msg);
						PushTable.ajax.reload(null,false);
					}else{
						toastr.error(data.msg);
					}
				}, "json"); */
				passed(cId,20);
		    },
		    cancelValue: '<fmt:message key="fxl.button.cancel" />',
		    cancel: function (){
		    }
		}).showModal();
	});
	
	function passed(cId,status) {
		$.post("${ctx}/fxl/customer/comment/passedComment/" + cId + "/" + status , function(data) {
			if(data.success){
				toastr.success(data.msg);
				commentTable.ajax.reload(null,false);
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	}
	//审核失败
	$("#commentTable").delegate("button[id=passedno]", "click", function(){
		var cId = $(this).attr("cId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要审核不通过该条评价吗？',
		    okValue: '审核不通过',
		    ok: function () {
		    	/* $.post("${ctx}/fxl/push/pushmsg/defeatePush/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						PushTable.ajax.reload(null,false);
					}else{
						toastr.error(data.msg);
					}
				}, "json"); */
		    	passed(cId,30);
		    },
		    cancelValue: '<fmt:message key="fxl.button.cancel" />',
		    cancel: function (){
		    }
		}).showModal();
	});
	
	$("#commentTable").delegate("button[id=del]", "click", function(){
		var cId = $(this).attr("cId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该评价吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/customer/comment/delComment/" + cId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						commentTable.ajax.reload(null,false);
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
	
	$("#commentTable").delegate("button[id=edit]", "click", function(){
		var cId = $(this).attr("cId");
		$("#id").val(cId);
		createCommentModal.modal('show');
	});
	
	var createCommentModal =  $('#createCommentModal');
	
	createCommentModal.on('hide.bs.modal', function(e){
		createCommentForm.trigger("reset");
	});
	
	var createCommentForm = $('#createCommentForm');
	
	createCommentForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	createCommentForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			$.post(form.attr("action"), form.serialize(), function(data){
				if(data.success){
					toastr.success(data.msg);
					commentTable.ajax.reload(null,false);
					createCommentModal.modal('hide')
				}else{
					toastr.error(data.msg);
				}
				form.removeData("running");
			}, "json");
		}
	});
	
	createCommentForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createCommentForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		createCommentForm.validationEngine('hideAll');
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
			    content: '您确定要导出当前评价列表EXCEL文档吗？',
			    okValue: '导出',
			    ok: function () {
			    	exportProgressModal.modal('show');
			    	$.post("${ctx}/fxl/customer/comment/createCommentExcel", $.param($.merge(searchForm.serializeArray(),[{name:"s_fstatus", value:$("#s_fstatus").val()}]),true), function(data) {
						if(data.success){
							exportProgressModal.modal('hide');
							if(downFlag){
								$("#downFile").attr("src","${ctx}/fxl/customer/comment/exportExcel/" + data.datePath + "/" + data.excelFileName);
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
	
	$('a[id="orderStatusBtn"]').each(function(i,n){
		$(this).click(function(e){
			e.stopPropagation();
			$("#s_fstatus").val($(this).data("status"));
			commentTable.ajax.reload();
			$(this).tab('show');
		});
	});
	
});
</script>
</head>
<body>
<!-- <input id="actionFlag" name="actionFlag" type="hidden"> -->
<input id="s_fstatus" name="s_fstatus" type="hidden" value="10">
<div class="row">
  <div class="col-md-10"><h3>评价管理</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>评价列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="exportExcelBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-list-alt"></span> 导出EXCEL</button>
  <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/operating/coupon/getApplyList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_fcustomerName">回复用户名：</label>
			<input type="text" id="s_fcustomerName" name="s_fcustomerName" class="form-control input-sm" >
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
<ul class="nav nav-tabs" id="commentTab">
	<li class="active"><a id="orderStatusBtn" data-status="10" data-toggle="tab">未审核</a></li>
	<li><a id="orderStatusBtn" data-status="20" data-toggle="tab">审核通过</a></li>
	<li><a id="orderStatusBtn" data-status="30" data-toggle="tab">审核未通过</a></li>
	<li><a id="orderStatusBtn" data-status="40" data-toggle="tab">已回复</a></li>
	<li><a id="orderStatusBtn" data-status="999" data-toggle="tab">删除</a></li>
</ul>
<table id="commentTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
<div class="modal fade" id="createCommentModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
	  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">评价排序</h4>
      </div>
      <form id="createCommentForm" action="${ctx}/fxl/customer/comment/editComment" method="post" class="form-inline" role="form">
	      <div class="modal-body">
	      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
	      		<input id="id" name="id" type="hidden">
			    <div class="form-group"><label for="forder">置顶置底排序：</label>
				  	<div class="input-group">
						<input type="text" id="forder" name="forder" class="form-control validate[custom[integer],min[-10],max[10]]" size="5"><div class="input-group-addon">请输入-10~10之间整数，数值越大排序越靠前</div>
					</div>
				</div>
			  <div style="clear:both;"></div>
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
<!--回复咨询开始-->
<div class="modal fade" id="replyModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="myModalLabel">回复评价信息</h4>
			</div>
			<form id="replyForm" action="${ctx}/fxl/customer/comment/reply" method="post" class="form-inline" role="form">
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
					<div class="form-group" style="min-height: 110px;"><label for="fcontent">评价内容：</label>
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