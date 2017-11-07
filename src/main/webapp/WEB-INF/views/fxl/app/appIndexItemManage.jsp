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
		uploadFileProgress.css( 'width', '0%' );
		channelSliderModal.css("overflow-y","auto");
	});
	
	$("#uploadStopBtn").click(function(){
		uploader.stop();
	});
	
	var defaultSize = {
		imageWidth:750,
		imageHeight:330
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
			    data["pathVar"] = "indexItemPath";
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
	
	var indexItemTable = $("table#indexItemTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"paging": false,
		"autoWidth" : false,
	  	//"scrollY": "350px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/app/getIndexItemList",
 		    "type": "POST",
 		    "data": function (data) {
 		    	/* $.each(searchForm.serializeArray(), function(i, n){
 		    		data[n.name] = n.value;
 				}); */
 		        return data;
 		    }
		},
		"stateSave": true,
		"deferRender": true,
		"lengthChange": false,
		"retrieve": true,
		"columns" : [
		{	"title" : "ID",
			"data" : "DT_RowId",
			"visible": false,
			"orderable" : false
		}, {
			"title" : '<center>图片</center>',
			"data" : "imageUrl",
			"className": "text-center",
			"width" : "90px",
			"orderable" : false
		}, {
			"title" : '<center>链接跳转类型</center>',
			"data" : "furlType",
			"className": "text-center",
			"width" : "60px",
			"orderable" : false
		}, {
			"title" : '<center>跳转实体标题</center>',
			"data" : "fentityTitle",
			"className": "text-center",
			"width" : "150px",
			"orderable" : false
		}, {
			"title" : '<center>适用前端</center>',
			"data" : "ffrontType",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>位置编号</center>',
			"data" : "flocationNum",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>是否显示</center>',
			"data" : "fisVisible",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		},{
			"title" : '<center>是否转换广告</center>',
			"data" : "flagType",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		},{
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "30px",
			"className": "text-center",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [1],
			"render" : function(data, type, full) {
				return '<img src="' + data + '" height="80" width="80" class="img-thumbnail"/>';
			}
		}, {
			"targets" : [8],
			"render" : function(data, type, full) {
				return '<div class="btn-group btn-group-xs" role="group" aria-label="零到壹，查找优惠"><button id="edit" mId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs"><fmt:message key="fxl.button.edit" /></button><button id="delete" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs"><fmt:message key="fxl.button.delete" /></button></div>';
			}
		}]
	});
	
	$("#indexItemTable").delegate("button[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/app/getIndexItem/" + mId, function(data) {
			if(data.success){
				$("#id").val(mId);
				$("#fimage").val(data.fimage);
				$("#fentityId").val(data.fentityId);
				$("#fentityTitle").val(data.fentityTitle);
				$("#fexternalUrl").val(data.fexternalUrl);
				$("#ffrontType option[value='" + data.ffrontType + "']").prop("selected",true);
				$("#furlType option[value='" + data.furlType + "']").prop("selected",true);
				$("#flocationNum").val(data.flocationNum);
				$("#forder").val(data.forder);
				$("#fisVisible option[value='" + data.fisVisible + "']").prop("selected",true);
				$("#flagType option[value='" + data.flagType + "']").prop("selected",true);
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
				indexItemModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	$("#indexItemTable").delegate("button[id=delete]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该首页广告位吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/app/delIndexItem/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						indexItemTable.ajax.reload(null,false);
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
	
	var indexItemModal =  $('#indexItemModal');
	
	indexItemModal.on('hide.bs.modal', function(e){
		indexItemForm.trigger("reset");
	});
	
	indexItemModal.on('shown.bs.modal', function(e){
		initUploader();
		$("#mandatoryJpg option[value='false']").prop("selected",true);
	});
	
	$('#createIndexItemBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		$('#resetBtn').show();
		imageThumbnail.attr("src",noPicUrl);
		indexItemModal.modal('show');
	});
	
	var indexItemForm = $('#indexItemForm');
	
	indexItemForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	indexItemForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		if(fimage.val() == ""){
			toastr.error("请上传首页广告位的图片！");
			return false;
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			if($('#actionFlag').val() == "add"){
				$.post(form.attr("action"), form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						indexItemTable.ajax.reload(null,false);
						indexItemModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/app/editIndexItem", form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						indexItemTable.ajax.reload(null,false);
						indexItemModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
	
	indexItemForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',indexItemForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		indexItemForm.validationEngine('hideAll');
	});
	
	var sliderTargetTable = $("table#sliderTargetTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"paging": false,
		"autoWidth" : false,
	  	"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/app/getSliderTargetList",
 		    "type": "POST",
 		    "data": function (data) {
 		    	data["cityId"] = $("#cityId").val();
 		    	data["urlType"] = furlType.val();
 		    	data["searchKey"] = $("#searchKey").val();
 		    	/* $.each(searchForm.serializeArray(), function(i, n){
 		    		data[n.name] = n.value;
 				}); */
 		        return data;
 		    }
		},
		"deferLoading": 0,
		"deferRender": true,
		"lengthChange": false,
		"retrieve": true,
		"columns" : [{
			"title" : '<center><fmt:message key="fxl.common.select2" /></center>',
			"data" : "DT_RowId",
			"width" : "20px",
			"className": "text-center",
			"orderable" : false
		}, {
			"title" : '<center>检索结果</center>',
			"data" : "info",
			"className": "text-left",
			"width" : "500px",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [ 0 ],
			"render" : function(data, type, full) {
				return '<input id="entityId" name="entityId" type="radio" value="'+ data + '" data-title="' + full.title + '" class="radio">';
			}
		}]
	});
	
	$("#sliderTargetTable").delegate("input[id=entityId]", "click", function(){
		$("#fentityId").val($(this).val());
		$("#fentityTitle").val($(this).data("title"));
	});
	
	var sliderTargetModal =  $('#sliderTargetModal');
	sliderTargetModal.on('hidden.bs.modal', function(e){
		$("#searchKey").val("");
		indexItemModal.css("overflow-y","auto");
	});
	
	var furlType = $('#furlType');

	$('#selectSliderTarget').on('click',function(e) {
		if($.trim(furlType.val()) == ""){
			toastr.error("请先选择“广告位跳转类型”再点选广告位跳转目标！");
		}else{
			sliderTargetModal.modal('show');
		}
	});
	
	$("#sliderTargetSearchBtn").on('click',function(e) {
		sliderTargetTable.ajax.reload();
    });
	
	$("#flagType option[value='${flagType}']").prop("selected",true);
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<input id="cityId" name="cityId" type="hidden" value="${cityId}">
<div class="row">
  <div class="col-md-8"><h3>首页广告位设置</h3></div>
  <div class="col-md-4"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>首页广告位列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createIndexItemBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 添加广告位</button></p></div>
</div>
<table id="indexItemTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover"></table>
<!--编辑轮播开始-->
<div class="modal fade" id="indexItemModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">编辑首页广告位信息</h4>
      </div>
      <form id="indexItemForm" action="${ctx}/fxl/app/addIndexItem" method="post" class="form-inline" role="form">
      <input id="id" name="id" type="hidden">
      <div class="modal-body">
      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
		<div class="row">
			<div class="col-md-2"><label for="fimage">广告位图片：</label>
			<input id="fimage" name="fimage" type="hidden" value=""></div>
			<div class="col-md-7" style="min-height: 100px;" id="dndDiv"><a href="#" class="thumbnail"><img id="imageThumbnail" src="${ctx}/styles/fxl/images/nopic.png" alt=""></a></div>
			<div class="col-md-3"><div id="filePicker">选择图片</div></div>
		</div>
		<div class="form-group"><label for="imageWidth">图片宽度：</label>
			<div class="input-group">
	    		<input type="text" id="imageWidth" name="imageWidth" class="validate[custom[integer],min[1],max[2048]] form-control" size="6"><div class="input-group-addon">px</div>
	    	</div>
		</div>
		<div class="form-group"><label for="imageHeight">图片高度：</label>
			<div class="input-group">
	    		<input type="text" id="imageHeight" name="imageHeight" class="validate[custom[integer],min[1],max[2048]] form-control" size="6"><div class="input-group-addon">px</div>
	    	</div>
		</div>
		<div class="form-group"><label for="mandatoryJpg">强制转成JPG：</label>
	        <select id="mandatoryJpg" name="mandatoryJpg" class="form-control">
				<option value="false">不强转</option>
				<option value="true">强转</option>
			</select>
	    </div>
		<p class="text-center"><button id="uploadBtn" type="button" class="btn btn-success"><span class="glyphicon glyphicon-cloud-upload"></span> 开始上传</button>
		<button id="delUploadBtn" class="btn btn-danger" type="button"><span class="glyphicon glyphicon-trash"></span> 删除图片</button></p>
		<div class="form-group has-error"><label for="furlType">广告位跳转类型：</label>
			<select id="furlType" name="furlType" class="validate[required] form-control">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="sliderUrlTypeItem" items="${sliderUrlTypeMap}"> 
				<option value="${sliderUrlTypeItem.key}">${sliderUrlTypeItem.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="form-group has-error"><label for="ffrontType">适用前端类型：</label>
	        <select id="ffrontType" name="ffrontType" class="validate[required] form-control">
	        	<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="frontType" items="${channelFrontTypeMap}"> 
				<option value="${frontType.key}">${frontType.value}</option>
				</c:forEach>
			</select>
	    </div>
		<div class="form-group has-error"><label for="flocationNum">位置编号：</label>
			<div class="input-group">
				<input type="text" id="flocationNum" name="flocationNum" class="form-control validate[required,integer,min[1],max[10]]" size="3"><div class="input-group-addon">请输入1-10，分别表示在首页的10个广告位区域显示</div>
		    </div>
	    </div>
	    <div class="form-group has-error"><label for="forder">排序号：</label>
			<div class="input-group">
				<input type="text" id="forder" name="forder" class="form-control validate[required,integer,min[1],max[100]]" size="5"><div class="input-group-addon">请输入1-100之间整数，数值越小排序越靠前</div>
		    </div>
	    </div>
		<div class="form-group has-error"><label for="fisVisible">是否显示：</label>
	        <select id="fisVisible" name="fisVisible" class="form-control validate[required]">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="isVisibleItem" items="${isVisibleMap}"> 
				<option value="${isVisibleItem.key}">${isVisibleItem.value}</option>
				</c:forEach>
			</select>
	    </div>
	    	<div class="form-group has-error"><label for="flagType">转换为广告：</label>
	        <select id="flagType" name="flagType" class="form-control validate[required]">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="isflagTypeTtem" items="${isflagTypeMap}"> 
				<option value="${isflagTypeTtem.key}">${isflagTypeTtem.value}</option>
				</c:forEach>
			</select>
	    </div>
	    <div class="form-group"><label for="fentityTitle">广告位跳转目标：</label>
			<input id="fentityId" name="fentityId" type="hidden" value="">
			<div id="selectSliderTarget" class="input-group" style="cursor: pointer;">
				<input type="text" id="fentityTitle" name="fentityTitle" placeholder="点击选择广告位跳转目标" class="form-control" readonly="readonly" size="60">
				<span class="input-group-addon"><i class="glyphicon glyphicon-send"></i></span>
			</div>
	    </div>
	    <div class="form-group"><label for="fexternalUrl">跳转URL：</label>
		    <div class="input-group">
				<input type="text" id="fexternalUrl" name="fexternalUrl" class="form-control" size="50"><div class="input-group-addon">输入完整的URL，如果不跳转则请输入“javascript:;”</div>
			</div>
	    </div>
	    
	    <div style="clear:both;"></div>
      </div>
      <div class="modal-footer">
		<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span> <fmt:message key="fxl.button.save" /></button>
		<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="fxl.button.close" /></button>
      </div>
      </form>
    </div>
  </div>
</div>
<!--编辑轮播结束-->
<!--选择轮播跳转目标开始-->
<div class="modal fade" id="sliderTargetModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">选择广告位跳转目标</h4>
      </div>
      <div class="modal-body">
      <div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong>根据您选择的“广告位跳转类型”项，通过下方输入的关键字信息，分别搜索活动、商家或专题信息</strong></div>
      <div class="row">
      	<div class="col-md-3 text-right">跳转目标关键字：</div>
		<div class="col-md-6"><input type="text" id="searchKey" name="searchKey" class="form-control" ></div>
      	<div class="col-md-3"><button id="sliderTargetSearchBtn" class="btn btn-primary" type="button"><span class="glyphicon glyphicon-search"></span> 搜索</button></div>
      </div>
      <p/>
      <table id="sliderTargetTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover"></table>
      <p/>
      </div>
      <div class="modal-footer">
		<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="fxl.button.close" /></button>
      </div>
    </div>
  </div>
</div>
<!--选择轮播跳转目标结束-->
<!--图片上传进度条开始-->
<div class="modal fade" id="uploadProgressModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">图片上传进度</h4>
      </div>
      <div class="modal-body">
      	<div class="progress">
			<div id="uploadFileProgress" class="progress-bar progress-bar-info progress-bar-striped active" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;"></div>
		</div>
      </div>
      <div class="modal-footer">
		<button id="uploadStopBtn" type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> 取消上传</button>
      </div>
      </form>
    </div>
  </div>
</div>
<!--图片上传进度条结束-->
</body>
</html>