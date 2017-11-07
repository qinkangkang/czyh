<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>砍一砍管理</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/webuploader/webuploader.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2-bootstrap.min.css" rel="stylesheet" type="text/css">
<script src="${ctx}/styles/webuploader/webuploader.html5only.min.js" type="text/javascript"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js" charset="UTF-8"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js" charset="UTF-8"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/select2.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/i18n/zh-CN.js"></script>
<script type="text/javascript">
$(document).ready(function() {
		
	$("#eventId").select2({
		placeholder: "选择一个商品名称",
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
	
	$("#fbeginTimeDiv, #fendTimeDiv").datetimepicker({
	    format : "yyyy-mm-dd hh:ii",
	    //startDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true,
	    pickerPosition: "bottom-left"
	});
	
    var uploadProgressModal =  $('#uploadProgressModal');
	
	uploadProgressModal.on('hidden.bs.modal', function(e){
		uploadFileProgress.css("width","0%");
		channelSliderModal.css("overflow-y","auto");
	});
	
	$("#uploadStopBtn").click(function(){
		uploader.stop();
	});
	
	var defaultSize = {
		imageWidth:750,
		imageHeight:456
	};
	
	var imageWidth = $("#fimageWidth");
    var imageHeight = $("#fimageHeight");
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
			    data["pathVar"] = "channelIconPath";
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
	
	
	var searchForm = $('form#searchForm');
	
	searchForm.submit(function(e) {
		e.preventDefault();
		eventBargainingTable.ajax.reload();
	});
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		fsponsorSelect.val(null).trigger("change");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	
	var eventBargainingTable = $("table#eventBargainingTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/eventBargaining/getBargainingList",
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
			"data" : "fbeginTime",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>活动结束时间</center>',
			"data" : "fendTime",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
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
		},{
			"targets" : [5],
			"render" : function(data, type, full) {
				var retString = '';
				if(data.fstatus ==10 || data.fstatus ==30){
					retString = '<div class="btn-group"><button type="button" class="btn btn-success btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">执行<span class="caret"></span></button><ul class="dropdown-menu">'
					+ '<li><a href="javascript:;" id="edit" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-edit"></span> <fmt:message key="fxl.button.edit" />活动</a></li>'
					+ '<li><a href="javascript:;" id="del" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-trash"></span> <fmt:message key="fxl.button.delete" />活动</a></li>'
					+ '<li><a href="javascript:;" id="onsale" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-open"></span> 上架活动</a></li>'

				}else{
					retString = '<div class="btn-group"><button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">执行<span class="caret"></span></button><ul class="dropdown-menu"><li><a href="javascript:;" id="offsale" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-save"></span> 下架活动</a></li>';
				}
				return retString;
			}
		}]
	});
	
	$("#eventBargainingTable").delegate("a[id=del]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该砍一砍促销活动吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/eventBargaining/delEventBargaining/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						eventBargainingTable.ajax.reload(null,false);
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
	
	$("#eventBargainingTable").delegate("a[id=onsale]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要即刻上架该砍一砍促销活动吗？',
		    okValue: '即刻上架',
		    ok: function () {
		    	$.post("${ctx}/fxl/eventBargaining/onSaleBargaining/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						eventBargainingTable.ajax.reload(null,false);
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
	
	$("#eventBargainingTable").delegate("a[id=offsale]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要即刻下架该砍一砍促销活动吗？',
		    okValue: '即刻下架',
		    ok: function () {
		    	$.post("${ctx}/fxl/eventBargaining/offsaleBargaining/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						eventBargainingTable.ajax.reload(null,false);
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
    
	$("#eventBargainingTable").delegate("a[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		$('#resetBtn').hide();
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/eventBargaining/getBargaining/" + mId, function(data) {
			if(data.success){
				$("#createEventBargainingForm #id").val(mId);
				$("#createEventBargainingForm #ftitle").val(data.ftitle);
				$("#createEventBargainingForm #eventId").val(data.feventId).trigger("change");
				$("#createEventBargainingForm #fbeginTime").val(data.fbeginTime);
				$("#createEventBargainingForm #fendTime").val(data.fendTime);
				$("#createEventBargainingForm #fimage").val(data.fimage);
				$("#createEventBargainingForm #finputText").val(data.finputText);
				$("#createEventBargainingForm #fpackageDesc").val(data.fpackageDesc);
				$("#createEventBargainingForm #fstartPrice").val(data.fstartPrice);
				$("#createEventBargainingForm #fsettlementPrice").val(data.fsettlementPrice);
				$("#createEventBargainingForm #ffloorPrice1").val(data.ffloorPrice1);
				$("#createEventBargainingForm #ffloorPrice2").val(data.ffloorPrice2);
				$("#createEventBargainingForm #ffloorPrice3").val(data.ffloorPrice3);
				$("#createEventBargainingForm #fstock1").val(data.fstock1);
				$("#createEventBargainingForm #fstock2").val(data.fstock2);
				$("#createEventBargainingForm #fstock3").val(data.fstock3);
				$("#createEventBargainingForm #fmaxBargaining").val(data.fmaxBargaining);
				$("#createEventBargainingForm #fminBargaining").val(data.fminBargaining);
				$("#createEventBargainingForm #ftype option[value='" + data.ftype + "']").prop("selected",true);
				$("#createEventBargainingForm #eventId option[value='" + data.feventId + "']").prop("selected",true);
				
				if($.trim(data.fimagePath) == ""){
					imageThumbnail.attr("src",noPicUrl);
				}else{
					imageThumbnail.attr("src",data.fimagePath);
				}
				if($.trim(data.imageWidth) == ""){
					imageWidth.val(defaultSize.imageWidth);
				}else{
					imageWidth.val(data.imageWidth);
				}
				if($.trim(data.imageHeight) == ""){
					imageHeight.val(defaultSize.imageHeight);
				}else{
					imageHeight.val(data.imageHeight);
				}
				createEventBargainingModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	
	$("#eventBargainingTable").delegate("a[id=viewBargain]", "click", function(){
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/eventBargaining/getBargaining/" + mId, function(data) {
			if(data.success){
				$("#bargainDetailTable #ftitle").text(data.ftitle);
				$("#bargainDetailTable #feventtitle").text(data.feventtitle);
				$("#bargainDetailTable #fbeginTime").text(data.fbeginTime);
				$("#bargainDetailTable #fendTime").text(data.fendTime);
				$("#bargainDetailTable #finputText").text(data.finputText);
				$("#bargainDetailTable #fpackageDesc").text(data.fpackageDesc);
				$("#bargainDetailTable #fstartPrice").text(data.fstartPrice);
				$("#bargainDetailTable #fsettlementPrice").text(data.fsettlementPrice);
				$("#bargainDetailTable #ffloorPrice1").text(data.ffloorPrice1);
				$("#bargainDetailTable #ffloorPrice2").text(data.ffloorPrice2);
				$("#bargainDetailTable #ffloorPrice3").text(data.ffloorPrice3);
				$("#bargainDetailTable #fstock1").text(data.fstock1);
				$("#bargainDetailTable #fstock2").text(data.fstock2);
				$("#bargainDetailTable #fstock3").text(data.fstock3);
				$("#bargainDetailTable #getFremainingStock1").text(data.getFremainingStock1);
				$("#bargainDetailTable #getFremainingStock2").text(data.getFremainingStock2);
				$("#bargainDetailTable #getFremainingStock3").text(data.getFremainingStock3);
				$("#bargainDetailTable #fmaxBargaining").text(data.fmaxBargaining);
				$("#bargainDetailTable #fminBargaining").text(data.fminBargaining);
				$("#bargainDetailTable #fstatusString").text(data.fstatusString);
				$("#bargainDetailTable #ftypeString").text(data.ftypeString);
				$("#bargainDetailTable #bargainUrl").text(data.bargainUrl);
				$("#bargainDetailTable #fpackageDesc").text(data.fpackageDesc);
				
				bargainDetailModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	
	var bargainDetailModal =  $('#bargainDetailModal');
	
	
	
	$('#createEventBonusBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		imageThumbnail.attr("src",noPicUrl);
		createEventBargainingModal.modal('show');
	});
	
	var createEventBargainingModal =  $('#createEventBargainingModal');
	
	createEventBargainingModal.on('shown.bs.modal', function(e){
		initUploader();
		$("#mandatoryJpg option[value='false']").prop("selected",true);
	});
	
	createEventBargainingModal.on('hide.bs.modal', function(e){
		if(e.target.id === "createEventBargainingModal"){
			createEventBargainingForm.trigger("reset");
		}
	});
	    
    var createEventBargainingForm = $('#createEventBargainingForm');
    
    createEventBargainingForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
    createEventBargainingForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			if($('#actionFlag').val() == "add"){
				$.post(form.attr("action"), form.serialize() , function(data){
					if(data.success){
						toastr.success(data.msg);
						eventBargainingTable.ajax.reload(null,false);
						createEventBargainingModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/eventBargaining/editBargaining",  form.serializeArray(), function(data){
					if(data.success){
						toastr.success(data.msg);
						eventBargainingTable.ajax.reload(null,false);
						createEventBargainingModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
    
    createEventBargainingForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createEventBargainingForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
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
  <div class="col-md-8"><h3>商品配置</h3></div>
  <div class="col-md-4"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>商品列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createEventBonusBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 创建商品</button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center">
	<form id="searchForm" action="${ctx}/fxl/event/getEventList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_ftitle">活动名称：</label>
			<input type="text" id="s_ftitle" name="s_ftitle" class="form-control input-sm" >
		</div>
		
		<div class="form-group"><label>开始时间：</label>
		<div class="input-daterange input-group date" id="createDateDiv" style="width:330px;">
		    <input type="text" class="form-control input-sm" name="fbeginTime" style="cursor: pointer;">
		    <span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
		    <input type="text" class="form-control input-sm" name="fendTime" style="cursor: pointer;">
		</div></div>
		
		<div class="form-group"><label for="s_status">活动状态：</label>
			<select id="s_status" name="s_status" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="statusItem" items="${bonusStatusMap}"> 
				<option value="${statusItem.key}">${statusItem.value}</option>
				</c:forEach>
			</select>
		</div>
		
		<p class="text-center"><button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="eventBargainingTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>

<!-- 编辑商品开始 -->
<div class="modal fade" id="createEventBargainingModal"  role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">编辑砍一砍信息</h4>
      </div>
	<form id="createEventBargainingForm" action="${ctx}/fxl/eventBargaining/addBargaining" method="post" class="form-inline" role="form">
	    <input id="id" name="id" type="hidden">
	    <div class="modal-body">
      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
   
      	<div class="form-group has-error"><label for="faddress">活动名称：</label>
		        <input type="text" id="ftitle" name="ftitle" class="form-control validate[required,minSize[1],maxSize[250]]" size="90" placeholder="请输入本次砍一砍活动名称">
		</div>
		<div class="form-group has-error"><label for="fbeginTime">活动开始时间：</label>
			<div id="fbeginTimeDiv" class="input-group date form_datetime">
				<input id="fbeginTime" name="fbeginTime" type="text" class="form-control validate[required]" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
			</div>
		</div>
		<div class="form-group has-error"><label for="fendTime">活动结束时间：</label>
			<div id="fendTimeDiv" class="input-group date form_datetime">
				<input id="fendTime" name="fendTime" type="text" class="form-control validate[required]" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
			</div>
		</div>
		<br/>
		<!-- 图片上传 -->
	   <div class="row">
		   	<div class="col-md-2"><label for="fimage">上传海报图片：</label>
		   		<input id="fimage" name="fimage" type="hidden" value=""></div>
		   	<div class="col-md-7" style="min-height: 100px;" id="dndDiv"><a href="#" class="thumbnail"><img id="imageThumbnail" src="${ctx}/styles/fxl/images/nopic.png" alt=""></a></div>
		   	<div class="col-md-3"><div id="filePicker">选择图片</div></div>
	    </div>
     	<div class="row">
			<div class="col-md-4">
			 	<div class="form-group"><label for="fimageWidth">图片宽度：</label>
					<div class="input-group">
			    		<input type="text" id="fimageWidth" name="fimageWidth" class="validate[custom[integer],min[1],max[2048]] form-control" size="6"><div class="input-group-addon">px</div>
			    	</div>
				</div>
			</div>
			<div class="col-md-4">
				<div class="form-group"><label for="fimageHeight">图片高度：</label>
					<div class="input-group">
			    		<input type="text" id="fimageHeight" name="fimageHeight" class="validate[custom[integer],min[1],max[2048]] form-control" size="6"><div class="input-group-addon">px</div>
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
		&nbsp;	&nbsp;	&nbsp;
		<button id="delUploadBtn" class="btn btn-danger" type="button"><span class="glyphicon glyphicon-trash"></span> 删除图片</button></p>
        <div class="form-group has-error"><label for="eventId">请选择砍价商品：</label>
			<select id="eventId" name="eventId" class="validate[required] form-control" style="width: 600px;">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="eventBonusItem" items="${eventBonusListMap}"> 
				<option value="${eventBonusItem.key}">${eventBonusItem.value}</option>
				</c:forEach>
			</select>
	    </div>
		<br/>
		<div class="form-group has-error"><label for="ftype">砍一砍订单类型：</label>
		      <select id="ftype" name="ftype" class="validate[required] form-control">
					 <option value=""><fmt:message key="fxl.common.select" /></option>
					 <c:forEach var="bonusTypeItem" items="${bonusTypeMap}">
					 <option value="${bonusTypeItem.key}" >${bonusTypeItem.value}</option>
					 </c:forEach>
			  </select>
		 </div>   
		 <div class="form-group"><label for="finputText">入口文案：</label>
		      <input type="text" id="finputText" name="finputText" class="form-control validate[minSize[1],maxSize[250]]" size="60">
		 </div>  
		 <div class="form-group"><label for="fpackageDesc">砍价套餐描述：</label>
		      <input type="text" id="fpackageDesc" name="fpackageDesc" class="form-control validate[minSize[1],maxSize[250]]" size="90">
		 </div>
		 
		 <div class="form-group has-error"><label for="fstartPrice">砍前价格：</label>
			  <div class="input-group">
	    		 <input type="text" id="fstartPrice" name="fstartPrice" class="form-control validate[required,custom[number]]" size="5"><div class="input-group-addon">元</div>
	    	  </div>
		 </div>
		 <div class="form-group has-error" style="margin-left: 70px;"><label for="fsettlementPrice">结算价格：</label>
			  <div class="input-group">
	    		 <input type="text" id="fsettlementPrice" name="fsettlementPrice" class="form-control validate[required,custom[number]]" size="5"><div class="input-group-addon">元</div>
	    	  </div>
		 </div>
		 <br/>
		 
		 <div class="form-group has-error"><label for="ffloorPrice1">底价1：</label>
			  <div class="input-group" style="margin-left: 20px;">
	    		 <input type="text" id="ffloorPrice1" name="ffloorPrice1" class="form-control validate[required,custom[number]]" size="5"><div class="input-group-addon">元</div>
	    	  </div>
		 </div>
		 <div class="form-group has-error" style="margin-left: 70px;"><label for="fstock1">库存1：</label>
			  <div class="input-group" style="margin-left: 20px;">
	    		 <input type="text" id="fstock1" name="fstock1" class="form-control validate[required,custom[number]]" size="5"><div class="input-group-addon">个</div>
	    	  </div>
		 </div>
		 <br/>
		    
		 <div class="form-group has-error"><label for="ffloorPrice2">底价2：</label>
			  <div class="input-group" style="margin-left: 20px;">
	    		 <input type="text" id="ffloorPrice2" name="ffloorPrice2" class="form-control validate[required,custom[number]]" size="5"><div class="input-group-addon">元</div>
	    	  </div>
		 </div>
		 <div class="form-group has-error" style="margin-left: 70px;"><label for="fstock2">库存2：</label>
			  <div class="input-group" style="margin-left: 20px;">
	    		 <input type="text" id="fstock2" name="fstock2" class="form-control validate[required,custom[number]]" size="5"><div class="input-group-addon">个</div>
	    	  </div>
		 </div>
		 <br/>   
		
		 <div class="form-group has-error"><label for="ffloorPrice3">底价3：</label>
			  <div class="input-group" style="margin-left: 20px;">
	    		 <input type="text" id="ffloorPrice3" name="ffloorPrice3" class="form-control validate[required,custom[number]]" size="5"><div class="input-group-addon">元</div>
	    	  </div>
		 </div>
		 <div class="form-group has-error" style="margin-left: 70px;"><label for="fstock3">库存3：</label>
			  <div class="input-group" style="margin-left: 20px;">
	    		 <input type="text" id="fstock3" name="fstock3" class="form-control validate[required,custom[number]]" size="5"><div class="input-group-addon">个</div>
	    	  </div>
		 </div>
		 <br/>  
		 		 <div class="form-group has-error"><label for="fmaxBargaining">每刀最多砍掉：</label>
			  <div class="input-group">
	    		 <input type="text" id="fmaxBargaining" name="fmaxBargaining" class="form-control validate[required,custom[number]]" size="5"><div class="input-group-addon">元</div>
	    	  </div>
		 </div>
		 <div class="form-group has-error"><label for="fminBargaining">每刀最少砍掉：</label>
			  <div class="input-group">
	    		 <input type="text" id="fminBargaining" name="fminBargaining" class="form-control validate[required,custom[number]]" size="5"><div class="input-group-addon">元</div>
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
<div class="modal fade" id="bargainDetailModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="myModalLabel">砍一砍活动详情</h4>
			</div>
			<div class="modal-body">
				<div class="panel panel-info">
					<div class="panel-heading"><strong>砍一砍活动信息</strong></div>
					<div class="panel-body">
						<table id="bargainDetailTable" class="table table-hover table-striped table-bordered">
							<tr>
								<td width="15%" align="right"><strong>活动名称</strong></td>
								<td width="35%"><div id="ftitle"></div></td>
								<td width="15%" align="right"><strong>砍一砍商品名称</strong></td>
								<td width="35%"><div id="feventtitle"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>开始时间</strong></td>
								<td><div id="fbeginTime"></div></td>
								<td align="right"><strong>结束时间</strong></td>
								<td><div id="fendTime"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>活动状态</strong></td>
								<td><div id="fstatusString"></div></td>
								<td align="right"><strong>订单类型</strong></td>
								<td><div id="ftypeString"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>砍前价格</strong></td>
								<td><div id="fstartPrice"></div></td>
								<td align="right"><strong>结算价格</strong></td>
								<td><div id="fsettlementPrice"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>每刀最多</strong></td>
								<td><div id="fmaxBargaining"></div></td>
								<td align="right"><strong>每刀最少</strong></td>
								<td><div id="fminBargaining"></div></td>
							</tr>
						</table>
						<table id="bargainDetailTable" class="table table-hover table-striped table-bordered">
							<tr>
								<td width="10%" align="right"><strong>底价1</strong></td>
								<td width="24%"><div id="ffloorPrice1"></div></td>
								<td width="10%" align="right"><strong>总库存</strong></td>
								<td width="24%"><div id="fstock1"></div></td>
								<td width="10%" align="right"><strong>剩余库存</strong></td>
								<td width="26%"><div id="getFremainingStock1"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>底价2</strong></td>
								<td><div id="ffloorPrice2"></div></td>
								<td align="right"><strong>总库存</strong></td>
								<td><div id="fstock2"></div></td>
								<td align="right"><strong>剩余库存</strong></td>
								<td><div id="getFremainingStock2"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>底价3</strong></td>
								<td><div id="ffloorPrice3"></div></td>
								<td align="right"><strong>总库存</strong></td>
								<td><div id="fstock3"></div></td>
								<td align="right"><strong>剩余库存</strong></td>
								<td><div id="getFremainingStock3"></div></td>
							</tr>
							
							<tr>
								<td align="right"><strong>入口文案</strong></td>
								<td colspan="5"><div id="finputText"></div></td>
							</tr>
							
							<tr>
								<td align="right"><strong>砍价套餐描述</strong></td>
								<td colspan="5"><div id="fpackageDesc"></div></td>
							</tr>
							
							<tr>
								<td align="right"><strong>活动链接</strong></td>
								<td colspan="5"><div id="bargainUrl"></div></td>
							</tr>
							
						</table>
					</div>
					<div class="panel-footer"><p class="text-right" style="margin: 0;"><em>零到壹，查找优惠</em></p></div>
				</div>
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