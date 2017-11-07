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

	$("#PushTimeDiv").datetimepicker({
	    format : "yyyy-mm-dd hh:ii",
	    startView : 1,
	    language : pickerLocal,
	    autoclose : true,
	    minuteStep : 5
	    //,pickerPosition: "bottom-left"
	});
	
	$("#fvalidTimeDiv").datetimepicker({
	    format : "yyyy-mm-dd hh:ii",
	    startView : 1,
	    language : pickerLocal,
	    autoclose : true,
	    minuteStep : 5
	    //,pickerPosition: "bottom-left"
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
		}, /**{
			"title" : '<center>消息内容</center>',
			"data" : "fcontent",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		}, {
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
		}, {
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
		},**/{
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
			"targets" : [9],
			"render" : function(data, type, full) {
				var retString = '<div class="btn-group btn-group-xs" role="group" aria-label="零到壹，查找优惠">';
				retString += '<button id="details" mId="' + full.DT_RowId + '" type="button" class="btn btn-info btn-xs">获取详情</button>';
				retString += '</div>';
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
		    content: '您确认要删除该条推送信息吗？',
		    okValue: '删除',
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
	
	//上传图片
	createPushModal.on('shown.bs.modal', function(e){
		initUploader();
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
	
	createCouponForm.on("reset", function(push){
		if (!push.isDefaultPrevented()) {
			push.preventDefault();
		}
		$(':input',createCouponForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		createCouponForm.validationEngine('hideAll');
	});
	
	/* $('a[id="pushfauditStatusBtn"]').each(function(i,n){
		$(this).click(function(e){
			e.stopPropagation();
			$("#s_fauditStatus").val($(this).data("fauditStatus"));
			PushTable.ajax.reload();
		});
	}); */
	/* //后续动作类型
	var ftargetType = $('#ftargetType');
	ftargetType.change(function(e){
		if(ftargetType.val() == 0){
			$('#useTypeEntityDiv').slideUp();
			$('#useTypeCategoryDiv').slideUp();
		}else if(ftargetType.val() == 2){
			$('#useTypeEntityDiv').slideUp();
			$('#useTypeCategoryDiv').slideDown();
		}else if(ftargetType.val() == 11){
			$('#useTypeEntityDiv').slideUp();
			$('#useTypeCategoryDiv').slideDown();
		}else if(ftargetType.val() == 12){
			$('#useTypeEntityDiv').slideUp();
			$('#useTypeCategoryDiv').slideDown();
		}else if(ftargetType.val() == 13){
			$('#useTypeEntityDiv').slideUp();
			$('#useTypeCategoryDiv').slideDown();
		}else {
			$('#useTypeEntityDiv').slideDown();
			$('#useTypeCategoryDiv').slideUp();
		}
    }); */
	
	//推送时间类型
	var fpushtimeType = $('#fpushtimeType');
	fpushtimeType.change(function(e){
		if(fpushtimeType.val() == 2){
			$('#PushfixedTimesDiv').slideDown();
			$('#PushTimesGoDiv').slideUp();
			$('#PushResTimesDiv').slideUp();
		}else if(fpushtimeType.val() == 3){
			$('#PushResTimesDiv').slideDown();
			$('#PushTimesGoDiv').slideUp();
			$('#PushfixedTimesDiv').slideUp();
		}else {
			$('#PushTimesGoDiv').slideDown();
			$('#PushfixedTimesDiv').slideUp();
			$('#PushResTimesDiv').slideUp();
		}
    });
	
	//用户目标选择
	var fpushuserType = $('#fpushuserType');
	fpushuserType.change(function(e){
		if(fpushuserType.val() == 2){
			$('#PushUserTypeListDiv').slideDown();
			$('#PushUserTypeOneDiv').slideUp();
			$('#PushUserTypeAliasDiv').slideUp();
		}else if(fpushuserType.val() == 3){
			$('#PushUserTypeOneDiv').slideDown();
			$('#PushUserTypeListDiv').slideUp();
			$('#PushUserTypeAliasDiv').slideUp();
		}else if(fpushuserType.val() == 4){
			$('#PushUserTypeAliasDiv').slideDown();
			$('#PushUserTypeOneDiv').slideUp();
			$('#PushUserTypeListDiv').slideUp();
		}else {
			$('#PushUserTypeListDiv').slideUp();
			$('#PushUserTypeOneDiv').slideUp();
			$('#PushUserTypeAliasDiv').slideUp();
		}
    });
	
	//维度用户
	var fdimension = $('#fdimension');
	fdimension.change(function(e){
		if(fdimension.val() == 1){//标签
			$('#fuserTagDiv').slideDown();
			$('#fappVersionDiv').slideUp();
		}else if(fdimension.val() == 2){//版本号
			$('#fappVersionDiv').slideDown();
			$('#fuserTagDiv').slideUp();
		}else if(fdimension.val() == 3){//用户活跃度
			$('#fuserTagDiv').slideUp();
			$('#fappVersionDiv').slideUp();
		}else {
			$('#fuserTagDiv').slideUp();
			$('#fappVersionDiv').slideUp();

		}
    });
	
	//上传图片	
	
    var uploadProgressModal =  $('#uploadProgressModal');
	
	uploadProgressModal.on('hidden.bs.modal', function(e){
		uploadFileProgress.css("width","0%");
		channelSliderModal.css("overflow-y","auto");
	});
	
	$("#uploadStopBtn").click(function(){
		uploader.stop();
	});
	
	var defaultSize = {
		imageWidth:200,
		imageHeight:200
	};
	
	var imageWidth = $("#imageWidth");
    var imageHeight = $("#imageHeight");
    imageWidth.val(defaultSize.imageWidth);
    imageHeight.val(defaultSize.imageHeight);
    var imageThumbnail = $("#imageThumbnail");
    var uploadFileProgress = $("#uploadFileProgress");
    var fimage = $("#fimage");
	
	var uploader;
	
	function initUploader(){
		if(!uploader){
			// 初始化Web Uploader
			uploader = WebUploader.create({
			    // 选完文件后，是否自动上传。
				auto: false,
	    		dnd: $("#dndDiv"),
	    		disableGlobalDnd: true,
			    // 文件接收服务端。
			    server: '${ctx}/web/imageUploadDG',
			    // 选择文件的按钮。可选。
			    // 内部根据当前运行是创建，可能是input元素，也可能是flash.
			    pick: '#filePicker',
			    // 只允许选择图片文件。
			    accept: {
			        title: '请选择您要上传的图片文件',
			        extensions: 'gif,jpg,jpeg,bmp,png',
			        mimeTypes: 'image/*'
			    },
			    thumb: {
			        // 图片质量，只有type为`image/jpeg`的时候才有效。
			        quality: 70,
			     	// 是否允许裁剪。
			        crop: true
			    }
			});
			
			// 当有文件添加进来的时候
			uploader.on( 'fileQueued', function( file ) {
			    // 创建缩略图
			    // 如果为非图片文件，可以不用调用此方法。
			    uploader.makeThumb( file, function( error, src ) {
					if ( error ) {
						imageThumbnail.replaceWith('<span>不能预览</span>');
						return;
					}
					imageThumbnail.attr( 'src', src );
			    }, imageWidth.val(), imageHeight.val());
			    uploadFileProgress.css( 'width', '0%' );
			});
			
			// 文件上传之前，先附带上要提交的信息。
			uploader.on( 'uploadBeforeSend', function( object, data, headers ) {
			    data["pathVar"] = "pushImagePath";
			    data["watermark"] = false;
			    if($.trim(imageWidth.val()) == ""){
			    	imageWidth.val(defaultSize.imageWidth);
			    }
			    if($.trim(imageHeight.val()) == ""){
			    	imageHeight.val(defaultSize.imageHeight);
			    }
			    data["imageWidth"] = imageWidth.val();
			    data["imageHeight"] = imageHeight.val();
			    data["mandatoryJpg"] = $("#mandatoryJpg").val();
			});
			
			// 开始上传方法。
			uploader.on( 'uploadStart', function( file ) {
				uploadProgressModal.modal('show');
			});
			
			// 文件上传过程中创建进度条实时显示。
			uploader.on( 'uploadProgress', function( file, percentage ) {
				uploadFileProgress.css( 'width', percentage * 100 + '%' );
			});
		
			// 文件上传成功，给item添加成功class, 用样式标记上传成功。
			uploader.on( 'uploadSuccess', function(file, data) {
				if(data.success){
					toastr.success(data.msg);
					fimage.val(data.imageId);
				}else{
					toastr.error(data.msg);
				}
			});
		
			// 文件上传失败，显示上传出错。
			uploader.on( 'uploadError', function(file, reason) {
				toastr.error('图片文件上传失败，失败代码：' + reason);
			});
			
			// 上传完成方法，上传成功与否都执行
			uploader.on( 'uploadComplete', function( file ) {
				uploadProgressModal.modal('hide');
			});
		}
		uploadFileProgress.css( 'width', '0%' );
	}
	
	$("#uploadBtn").click(function(e) {
		uploader.upload();
    });
	
	$("#delUploadBtn").click(function(e) {
		uploader.reset();
		imageThumbnail.attr("src",noPicUrl);
		uploadFileProgress.css('width','0%');
		fimage.val("");
    });
	
	//审核备注信息
	$('a[id="pushfauditStatusBtn"]').each(function(i,n){
		$(this).click(function(e){
			e.stopPropagation();
			$("#s_fauditStatus").val($(this).data("fauditstatus"));
			PushTable.ajax.reload();
		});
	});
	
	//备注功能
	$("#PushTable").delegate("tbody tr[id]", "click", function(push){
		if (!push.isDefaultPrevented()) {
			push.preventDefault();
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
	
	
	//推送详情页
	$("#PushTable").delegate("button[id=details]", "click", function(){
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/push/getDetails/" + mId, function(data) {
			if(data.success){
				$("#id").val(mId);
				$("#x_ftitle").text(data.x_ftitle);
				$("#x_fcontent").text(data.x_fcontent);
				$("#x_ftargetObjectId").text(data.x_ftargetObjectId);
				$("#x_furl").text(data.furl);
				$("#x_ftargetType").text(data.x_ftargetType);
				$("#x_fdescription").text(data.x_fdescription);
				$("#x_fPushTime").text(data.x_fPushTime);
				$("#x_fvalidTime").text(data.x_fvalidTime);
				$("#x_fcreateTime").text(data.x_fcreateTime);
				$("#x_fauditTime").text(data.x_fauditTime);
				detailPushModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<input id="s_fauditStatus" name="s_fauditStatus" type="hidden" value="10">
<div class="row">
	<div class="col-md-10"><h3>推送管理</h3></div>
	<div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
	<div class="col-md-2"><h4>推送消息列表</h4></div>
	<div class="col-md-10"><p class="text-right"><button id="createPushBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span>创建推送消息</button>
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

<ul class="nav nav-tabs" id="couponTab">
	<li><a id="pushfauditStatusBtn" href="javascript:;" data-fauditstatus="10" data-toggle="tab">待审核</a></li>
	<li><a id="pushfauditStatusBtn" href="javascript:;" data-fauditstatus="20" data-toggle="tab">审核通过</a></li>
	<li><a id="pushfauditStatusBtn" href="javascript:;" data-fauditstatus="30" data-toggle="tab">审核失败</a></li>
</ul>
<div class="alert alert-warning text-center" role="alert" style="padding: 3px; margin-bottom: 0px;"><button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button><strong>点击推送消息列表中一列，即可显示该列的推送消息备注信息。</strong></div>
<table id="PushTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-bordered"></table>

<!--添加推送消息开始-->
<div class="modal fade" id="createPushModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="myModalLabel">添加推送消息</h4>
			</div>
			
			<form id="createCouponForm" action="${ctx}/fxl/push/pushmsg/addPush" method="post" class="form-inline" role="form">
				<input id="id" name="id" type="hidden">
				<input id="fstatus" name="fstatus" type="hidden" value="10">
				<input id="fauditStatus" name="fauditStatus" type="hidden" value="10">
				<input id="ftype" name="ftype" type="hidden" value="0">
				<div class="modal-body">
					<div class="alert alert-danger text-center" role="alert" style="padding: 5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
					<div class="form-group has-error"><label for="fdescription">消息描述：</label>
						<input type="text" id="fdescription" name="fdescription" class="form-control validate[required,minSize[2],maxSize[250]]" size="100">
					</div>
					
					
				   					
					
					<div class="form-group has-error"><label for="ftitle">消息标题：</label>
						<input type="text" id="ftitle" name="ftitle" class="form-control validate[required,minSize[2],maxSize[250]]" size="100">
					</div>
					
					<div class="form-group" style="min-height: 110px;"><label for="fcontent">推送内容：</label>
				        <textarea id="fcontent" name="fcontent" cols="100%" rows="5" class="validate[maxSize[250]] form-control"></textarea>
				    </div>
				    
				   
				    <div class="form-group"><label for="fpushTime">推送时间：</label>
					    <div id="PushTimeDiv" class="input-group date form_datetime">
						    <input id="fpushTime" name="fpushTime" type="text" class="form-control" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
					    </div>
				    </div>
				    
			       <div class="form-group"><label for="fvalidTime">推送时效：</label>
					    <div id="fvalidTimeDiv" class="input-group date form_datetime">
						    <input id="fvalidTime" name="fvalidTime" type="text" class="form-control" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
					    </div>
				   </div>
				    
				   <div class="row">
						<div class="col-md-2"><label for="fimage">栏目图标：</label>
							<input id="fimage" name="fimage" type="hidden" value=""></div>
						    <div class="col-md-7" style="min-height: 100px;" id="dndDiv"><a href="#" class="thumbnail"><img id="imageThumbnail" src="${ctx}/styles/fxl/images/nopic.png" alt=""></a></div>
						<div class="col-md-3"><div id="filePicker">选择图片</div></div>
		           </div>
		           <div class="row">
			<div class="col-md-4">
			<div class="form-group"><label for="imageWidth">图片宽度：</label>
				<div class="input-group">
		    		<input type="text" id="imageWidth" name="imageWidth" class="validate[custom[integer],min[1],max[2048]] form-control" size="6"><div class="input-group-addon">px</div>
		    	</div>
			</div>
			</div>
			<div class="col-md-4">
			<div class="form-group"><label for="imageHeight">图片高度：</label>
				<div class="input-group">
		    		<input type="text" id="imageHeight" name="imageHeight" class="validate[custom[integer],min[1],max[2048]] form-control" size="6"><div class="input-group-addon">px</div>
		    	</div>
			</div>
			</div>
			<div class="col-md-4">
			<div class="form-group"><label for="mandatoryJpg">强制转成JPG：</label>
		        <select id="mandatoryJpg" name="mandatoryJpg" class="form-control">
					<option value="false">不强转</option>
					<option value="true">强转</option>
				</select>
		    </div>
		    </div>
		</div>
		<p class="text-center"><button id="uploadBtn" type="button" class="btn btn-success"><span class="glyphicon glyphicon-cloud-upload"></span> 开始上传</button>
		<button id="delUploadBtn" class="btn btn-danger" type="button"><span class="glyphicon glyphicon-trash"></span> 删除图片</button></p>
				    <!-- 选择推送时间开始  
				    <div class="form-group has-error"><label for="fpushtimeType">推送时间类型：</label>
						<select id="fpushtimeType" name="fpushtimeType" class="form-control validate[required]">
							<c:forEach var="pushTimeItem" items="${pushTimeMap}">
								<option value="${pushTimeItem.key}">${pushTimeItem.value}</option>
							</c:forEach>
						</select>
					</div>
					
					<div id="PushTimesGoDiv" style="display: none;">
					    <div class="form-group"><label for="fpushTime">72.0/小时内在线设备可以接受到消息：</label>
						    <div id="PushTimeDiv" class="input-group date form_datetime">
							    <input id="fpushTime" name="fpushTime" type="text" class="form-control" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
						    </div>
					    </div>
				    </div>
				    
				    <div id="PushfixedTimesDiv" style="display: none;">
				         <div class="form-group has-error"><label for="fuseStartTime">定时推送时间：</label>
						    <div id="s_PushfixedTimeDiv" class="input-daterange input-group date" style="width:330px;">
							   <input type="text" class="form-control validate[required]" id="fuseStartTime" name="fuseStartTime" style="cursor: pointer;"><span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
							   <input type="text" class="form-control validate[required]" id="fuseEndTime" name="fuseEndTime" style="cursor: pointer;">
						    </div>
					    </div>
				    </div>
				    
				    <div id="PushResTimesDiv" style="display: none;">
                         <div class="form-group has-error"><label for="fuseStartTime">重复推送时间：</label>
						    <div id="s_PushResTimesDiv" class="input-daterange input-group date" style="width:330px;">
							   <input type="text" class="form-control validate[required]" id="fuseStartTime" name="fuseStartTime" style="cursor: pointer;"><span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
							   <input type="text" class="form-control validate[required]" id="fuseEndTime" name="fuseEndTime" style="cursor: pointer;">
						    </div>
					    </div>
				    </div>
				 -->     
				     <!-- 选择推送时间完毕 --> 
				    
				     <!-- 选择用户推送开始 -->
				    <div class="form-group has-error"><label for="fpushuserType">目标用户：</label>
						<select id="fpushuserType" name="fpushuserType" class="form-control validate[required]">
							<c:forEach var="pushUserTypeItem" items="${pushUserTypeMap}">
								<option value="${pushUserTypeItem.key}">${pushUserTypeItem.value}</option>
							</c:forEach>
						</select>
					</div>
					
					<div id="PushUserTypeListDiv" style="display: none;">
					<div class="form-group has-error"><label for="fdimension">基础维度：</label>
						    <select id="fdimension" name="fdimension" class="form-control validate[required]">
							   <c:forEach var="fdimensionItem" items="${pushDimensionMap}">
								   <option value="${fdimensionItem.key}">${fdimensionItem.value}</option>
							   </c:forEach>
						</select>
						</div>
				    </div>
					
					<div id="PushUserTypeOneDiv" style="display: none;">
						    <div class="form-group has-error"><label for="fdeviceToken">Device Token：</label>
						        <input type="text" id="fdeviceToken" name="fdeviceToken" class="form-control validate[required,minSize[2],maxSize[250]]" size="90">
					        </div>
				    </div>
					
					<div id="PushUserTypeAliasDiv" style="display: none;">
						    <div class="form-group has-error"><label for="xxxxxx">特定用户(Alias)：</label>
						        <input type="text" id="xxxxxx" name="xxxxxx" class="form-control validate[required,minSize[2],maxSize[250]]" size="90">
					        </div>
				    </div>
				    
				    <!-- 用户维度开始 -->
				    <div id="fuserTagDiv" style="display: none;">
				    	<div class="form-group has-error"><label for="fuserTag">用户标签：</label>
						    <select id="fuserTag" name="fuserTag" class="form-control validate[required]">
							   <c:forEach var="fuserTagItem" items="${pushUserTagMap}">
								   <option value="${fuserTagItem.key}">${fuserTagItem.value}</option>
							   </c:forEach>
						</select>
						</div>
				    </div>
				    
				     <div id="fappVersionDiv" style="display: none;">
				    	<div class="form-group has-error"><label for="fappVersion">版本号：</label>
						    <select id=fappVersion name="fappVersion" class="form-control validate[required]">
							   <c:forEach var="appVersionMapItem" items="${appVersionMap}">
								   <option value="${appVersionMapItem.key}">${appVersionMapItem.value}</option>
							   </c:forEach>
						</select>
						</div>
				    </div>
				    
				    
				     <!-- 用户维度结束 -->
					
					 <!-- 选择用户推送完毕 -->
					
					<div class="form-group has-error"><label for="ftargetType">后续动作：</label>
						<select id="ftargetType" name="ftargetType" class="form-control validate[required]">
							<c:forEach var="couponUseItem" items="${pushUrlMap}">
								<option value="${couponUseItem.key}">${couponUseItem.value}</option>
							</c:forEach>
						</select>
					</div>
					
					<div id="useTypeEntityDiv" >
						    <div class="form-group has-error"><label for="ftargetObject">打开指定活动：</label>
						        <input type="text" id="ftargetObject" name="ftargetObject" class="form-control validate[required,minSize[2],maxSize[250]]" size="90">
					        </div>
				    </div>
				     
					<div style="clear: both;"></div>
				</div>
				<div class="modal-footer">
					<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span><fmt:message key="添加推送" /></button>
					<button class="btn btn-warning" type="reset" id="resetBtn"><span class="glyphicon glyphicon-repeat"></span><fmt:message key="fxl.button.reset" /></button>
					<button class="btn btn-danger" type="button" id="delBtn"><span class="glyphicon glyphicon-trash"></span><fmt:message key="fxl.button.delete" /></button>
					<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span><fmt:message key="fxl.button.close" /></button>
				</div>
			</form>
		</div>
	</div>
</div>
<!--添加推送消息结束-->

<!--推送详情开始开始-->
<div class="modal fade" id="detailPushModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="myModalLabel">推送详情信息</h4>
			</div>
			<form id="replyForm" action="#" method="post" class="form-inline" role="form">
				<input id="id" name="id" type="hidden">
				<div class="modal-body">
					<div class="alert alert-danger text-center" role="alert" style="padding: 5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
					<div class="row">
						<div class="col-md-4"><p class="text-right"><h4>推送标题：</h4></p></div>
						<div class="col-md-8" id="x_ftitle"></div>
					</div>
					<div class="row">
						<div class="col-md-4"><p class="text-right"><h4>推送内容：</h4></p></div>
						<div class="col-md-8" id="x_fcontent"></div>
					</div>
					
					<div class="row">
						<div class="col-md-4"><p class="text-right"><h4>目标Id：</h4></p></div>
						<div class="col-md-8" id="x_ftargetObjectId"></div>
					</div>
					<div class="row">
						<div class="col-md-4"><p class="text-right"><h4>消息连接：</h4></p></div>
						<div class="col-md-8" id="x_furl"></div>
					</div>
					<div class="row">
						<div class="col-md-4"><p class="text-right"><h4>目标类型：</h4></p></div>
						<div class="col-md-8" id="x_ftargetType"></div>
					</div>
					<div class="row">
						<div class="col-md-4"><p class="text-right"><h4>推送描述：</h4></p></div>
						<div class="col-md-8" id="x_fdescription"></div>
					</div>
					<div class="row">
						<div class="col-md-4"><p class="text-right"><h4>推送时间：</h4></p></div>
						<div class="col-md-8" id="x_fPushTime"></div>
					</div>
					<div class="row">
						<div class="col-md-4"><p class="text-right"><h4>推送时效：</h4></p></div>
						<div class="col-md-8" id="x_fvalidTime"></div>
					</div>
					<div class="row">
						<div class="col-md-4"><p class="text-right"><h4>创建时间：</h4></p></div>
						<div class="col-md-8" id="x_fcreateTime"></div>
					</div>
					<div class="row">
						<div class="col-md-4"><p class="text-right"><h4>审核时间：</h4></p></div>
						<div class="col-md-8" id="x_fauditTime"></div>
					</div>
					
					<div style="clear: both;"></div>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span><fmt:message key="fxl.button.close" /></button>
				</div>
			</form>
		</div>
	</div>
</div>
<!--推送详情开始结束-->

</body>
</html>