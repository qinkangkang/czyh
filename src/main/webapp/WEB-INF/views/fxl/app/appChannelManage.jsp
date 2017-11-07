<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/webuploader/webuploader.css" rel="stylesheet" type="text/css">
<script src="${ctx}/styles/webuploader/webuploader.html5only.min.js" type="text/javascript"></script>
<script type="text/javascript">
$(document).ready(function() {
	
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
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	searchForm.submit(function(e) {
		e.preventDefault();
		channelTable.ajax.reload();
	});
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	var channelTable = $("table#channelTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/app/getChannelList",
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
			"title" : '<center>栏目编码</center>',
			"data" : "fcode",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>栏目名称</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "60px",
			"orderable" : false
		}, {
			"title" : '<center>WEB端<br/>栏目类型</center>',
			"data" : "fwebType",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>APP端<br/>栏目类型</center>',
			"data" : "ftype",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>所属<br/>城市</center>',
			"data" : "fcity",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>栏目<br/>排序号</center>',
			"data" : "forder",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>全活动<br/>栏目</center>',
			"data" : "fallEvent",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>是否<br/>显示</center>',
			"data" : "fisVisible",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>默认<br/>排序类型</center>',
			"data" : "fdefaultOrderType",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "40px",
			"className": "text-center",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [10],
			"render" : function(data, type, full) {
				return '<button id="edit" mId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs"><fmt:message key="fxl.button.edit" /></button><button id="del" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs"><fmt:message key="fxl.button.delete" /></button><button id="slider" mId="' + full.DT_RowId + '" type="button" class="btn btn-success btn-xs">轮播</button><button id="eventOrder" mId="' + full.DT_RowId + '" type="button" class="btn btn-warning btn-xs">活动排序</button>';
			}
		}]
	});
	
	$("#channelTable").delegate("button[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		$('#resetBtn').hide();
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/app/getChannel/" + mId, function(data) {
			if(data.success){
				$("#id").val(mId);
				$("#fcode").val(data.fcode);
				$("#ftitle").val(data.ftitle);
				//$("#fsubTitle").val(data.fsubTitle);
				//$("#fcity option[value='" + data.fcity + "']").prop("selected",true);
				$("#fcity option[value='${fcity}']").prop("selected",true);
				$("#forder").val(data.forder);
				$("#ffrontType option[value='" + data.ffrontType + "']").prop("selected",true);
				$("#fisVisible option[value='" + data.fisVisible + "']").prop("selected",true);
				$("#fdefaultOrderType option[value='" + data.fdefaultOrderType + "']").prop("selected",true);
				$("#ftype option[value='" + data.ftype + "']").prop("selected",true);
				$("#fwebType option[value='" + data.fwebType + "']").prop("selected",true);
				$("#fallEvent option[value='" + data.fallEvent + "']").prop("selected",true);
				$("#fimage").val(data.fimage);
				//$("#fpromotion").val(data.fpromotion);
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
				createChannelModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	$("#channelTable").delegate("button[id=del]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该栏目吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/app/delChannel/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						channelTable.ajax.reload(null,false);
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
	
	$("#channelTable").delegate("button[id=slider]", "click", function(){
		window.location.href = "${ctx}/fxl/app/toChannelSlider/" + $(this).attr("mId");
	});
	
	$("#channelTable").delegate("button[id=eventOrder]", "click", function(){
		channelId = $(this).attr("mId");
		eventListModal.modal('show');
		eventTable.ajax.reload(null,false);
		//eventTable.ajax.url("${ctx}/fxl/app/getEventListByChannelId/" + mId).load();
	});
	
	var createChannelModal =  $('#createChannelModal');
	
 	createChannelModal.on('hide.bs.modal', function(e){
		createChannelForm.trigger("reset");
	});
	
	createChannelModal.on('shown.bs.modal', function(e){
		initUploader();
		$("#mandatoryJpg option[value='false']").prop("selected",true);
	});
	
	$('#createChannelBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		$('#resetBtn').show();
		imageThumbnail.attr("src",noPicUrl);
		createChannelModal.modal('show')
	});
	
	var createChannelForm = $('#createChannelForm');
	
	createChannelForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	createChannelForm.on("submit", function(event){
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
						channelTable.ajax.reload(null,false);
						createChannelModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/app/editChannel", form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						channelTable.ajax.reload(null,false);
						createChannelModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
	
	createChannelForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createChannelForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		createChannelForm.validationEngine('hideAll');
	});
	
	var eventListModal =  $('#eventListModal');
	
 	eventListModal.on('hide.bs.modal', function(e){
		eventListForm.trigger("reset");
	})
	
	var eventListForm = $('#eventListForm');
	
	eventListForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	var channelId;
	var eventTable = $("table#eventTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"paging": false,
		"autoWidth" : false,
	  	//"scrollY": "350px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/app/getEventListByChannelId",
 		    "type": "POST",
 		    "data": function (data) {
 		    	data["channelId"] = channelId;
 		    	/* $.each(searchForm.serializeArray(), function(i, n){
 		    		data[n.name] = n.value;
 				}); */
 		        return data;
 		    }
		},
		"deferRender": true,
		"lengthChange": false,
		"retrieve": true,
		"columns" : [
		{	"title" : "ID",
			"data" : "DT_RowId",
			"visible": false,
			"orderable" : false
		}, {
			"title" : '<center>序号</center>',
			"data" : "xh",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>活动标题</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "150px",
			"orderable" : false
		}, {
			"title" : '<center>商家</center>',
			"data" : "fsponsor",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>报名截止</center>',
			"data" : "foffSaleTime",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>活动排序</center>',
			"data" : "forder",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [2],
			"render" : function(data, type, full) {
				return '<a id="viewEvent" href="javascript:;" mId="' + full.feventId + '">' + data + '</a>';
			}
		}, {
			"targets" : [5],
			"render" : function(data, type, full) {
				return '<div class="input-group"><input type="text" id="eventOrder_' + full.DT_RowId + '" class="form-control validate[required,custom[integer],min[-10],max[10]] input-sm" size="1" value="' + data + '"><span class="input-group-btn"><button id="saveOrderBtn" class="btn btn-default btn-sm" type="button" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></button></span></div>';
			}
		}]
	});
	
	$("#eventTable").delegate("a[id=viewEvent]", "click", function(){
		window.open("${ctx}/fxl/event/eventView/" + $(this).attr("mId")) ;
	});
	
	$("#eventTable").delegate("button[id=saveOrderBtn]", "click", function(){
		var mId = $(this).attr("mId");
		var eventOrder = $('#eventOrder_'+mId);
		if(!eventOrder.validationEngine('validate')){
	    	$.post("${ctx}/fxl/event/saveEventOrder", $.param({id:mId,forder:eventOrder.val()},true), function(data) {
				if(data.success){
					toastr.success(data.msg);
					eventTable.ajax.reload(null,false);
				}else{
					toastr.error(data.msg);
				}
			}, "json");
		}
	});
	
	$("#fcity option[value='${fcity}']").prop("selected",true);
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-10"><h3>APP栏目设置</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>APP栏目列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createChannelBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 创建栏目</button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/app/getChannelList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_fcode">栏目编码：</label>
			<input type="text" id="s_fcode" name="s_fcode" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_ftitle">栏目名称：</label>
			<input type="text" id="s_ftitle" name="s_ftitle" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_fcity">所属城市：</label>
			<select id="s_fcity" name="s_fcity" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="cityItem" items="${cityMap}"> 
				<option value="${cityItem.key}">${cityItem.value}</option>
				</c:forEach>
			</select>
		</div>
		<p class="text-center"><button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="channelTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
<!--编辑活动开始-->
<div class="modal fade" id="createChannelModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">编辑栏目信息</h4>
      </div>
      <form id="createChannelForm" action="${ctx}/fxl/app/addChannel" method="post" class="form-inline" role="form">
      <input id="id" name="id" type="hidden">
      <div class="modal-body">
      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
		<div class="form-group has-error"><label for="fcity">所属城市：</label>
			<select id="fcity" name="fcity" class="validate[required] form-control">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="cityItem" items="${cityMap}"> 
				<option value="${cityItem.key}">${cityItem.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="form-group has-error"><label for="fcode">栏目编码：</label>
			<input type="text" id="fcode" name="fcode" class="form-control validate[required,minSize[2],maxSize[250]]">
	    </div>
     	<div class="form-group has-error"><label for="ftitle">栏目标题：</label>
			<input type="text" id="ftitle" name="ftitle" class="form-control validate[required,minSize[2],maxSize[250]]">
	    </div>
	    <!-- <div class="form-group has-error"><label for="fsubTitle">栏目副标题：</label>
			<input type="text" id="fsubTitle" name="fsubTitle" class="form-control validate[required,minSize[2],maxSize[6]]" size="10">
	    </div>
	    <div class="form-group"><label for="fpromotion">促销短语：</label>
			<input type="text" id="fpromotion" name="fpromotion" class="form-control validate[minSize[2],maxSize[16]]" size="40">
	    </div> -->
	    <div class="form-group has-error"><label for="ffrontType">适用前端类型：</label>
	        <select id="ffrontType" name="ffrontType" class="validate[required] form-control">
	        	<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="frontType" items="${channelFrontTypeMap}"> 
				<option value="${frontType.key}">${frontType.value}</option>
				</c:forEach>
			</select>
	    </div>
	    <div class="form-group"><label for="fwebType">WEB端栏目类型：</label>
	        <select id="fwebType" name="fwebType" class="form-control">
	        	<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="channelType" items="${channelTypeMap}"> 
				<option value="${channelType.key}">${channelType.value}</option>
				</c:forEach>
			</select>
	    </div>
	    <div class="form-group"><label for="ftype">APP端栏目类型：</label>
	        <select id="ftype" name="ftype" class="form-control">
	        	<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="channelType" items="${channelTypeMap}"> 
				<option value="${channelType.key}">${channelType.value}</option>
				</c:forEach>
			</select>
	    </div>
		<div class="form-group has-error"><label for="forder">栏目排序号：</label>
			<div class="input-group">
	    		<input type="text" id="forder" name="forder" class="form-control validate[required,integer,min[1],max[100]]" size="5"><div class="input-group-addon">请输入1-100之间整数，数值越小排序越靠前</div>
	    	</div>
	    </div>
	    <div class="form-group has-error"><label for="fallEvent">是全活动栏目：</label>
	        <select id="fallEvent" name="fallEvent" class="validate[required] form-control">
	        	<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="isVisibleItem" items="${isVisibleMap}"> 
				<option value="${isVisibleItem.key}">${isVisibleItem.value}</option>
				</c:forEach>
			</select>
	    </div>
		<div class="form-group has-error"><label for="fisVisible">是否显示：</label>
	        <select id="fisVisible" name="fisVisible" class="validate[required] form-control">
	        	<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="isVisibleItem" items="${isVisibleMap}"> 
				<option value="${isVisibleItem.key}">${isVisibleItem.value}</option>
				</c:forEach>
			</select>
	    </div>
	    <div class="form-group has-error"><label for="fdefaultOrderType">默认排序类型：</label>
	        <select id="fdefaultOrderType" name="fdefaultOrderType" class="validate[required] form-control">
	        	<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="orderTypeItem" items="${defaultOrderTypeMap}"> 
				<option value="${orderTypeItem.key}">${orderTypeItem.value}</option>
				</c:forEach>
			</select>
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
<!--编辑活动结束-->
<!--关联APP栏目信息开始-->
<div class="modal fade" id="eventListModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">请编辑该栏目下活动的排序</h4>
      </div>
      <form id="eventListForm" action="${ctx}/fxl/app/channelEventOrder" method="post" class="form-inline" role="form">
      <div class="modal-body">
    	<input id="eventId" name="eventId" type="hidden">
		<table id="eventTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="fxl.button.close" /></button>
      </div>
      </form>
    </div>
  </div>
</div>
<!--关联APP栏目信息结束-->
</body>
</html>