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
	
	var imageWidth = $("#imageWidth");
    var imageHeight = $("#imageHeight");
    imageWidth.val(600);
    imageHeight.val(960);
	
	// 初始化Web Uploader
	var uploader = WebUploader.create({
	    // 选完文件后，是否自动上传。
	    auto: false,
	    dnd: $("#dndDiv"),
	    disableGlobalDnd: true,
	    // 文件接收服务端。
	    server: '${ctx}/web/imageUpload',
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
    
    var imageThumbnail = $("#imageThumbnail");
	
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
	    data["pathVar"] = "appFlashImagePath";
	    data["imageWidth"] = imageWidth.val();
	    data["imageHeight"] = imageHeight.val();
	});
	
	var uploadFileProgress = $("#uploadFileProgress");
	
	var uploadFileProgressDialog = dialog({
		fixed: true,
        title: '上传文件',
        content: '<img src="${ctx}/styles/fxl/images/loading.gif" alt="" height="60" width="60">',
        cancelValue: '取消上传',
        cancel: function () {
        	uploader.stop();
        }
    });
	
	// 开始上传方法。
	uploader.on( 'uploadStart', function( file ) {
		uploadFileProgressDialog.showModal();
	});
	
	// 文件上传过程中创建进度条实时显示。
	uploader.on( 'uploadProgress', function( file, percentage ) {
		uploadFileProgress.css( 'width', percentage * 100 + '%' );
	});

	// 文件上传成功，给item添加成功class, 用样式标记上传成功。
	uploader.on( 'uploadSuccess', function(file, data) {
		if(data.success){
			toastr.success(data.msg);
			imageId.val(data.imageId);
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
		uploadFileProgressDialog.close();
	});
	
	$("#uploadBtn").click(function(e) {
		uploader.upload();
    });
	
	var imageId = $("#imageId");
	
	var editAppFlashForm = $('#editAppFlashForm');
	
	editAppFlashForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	editAppFlashForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(imageId.val() == ""){
			toastr.warning('请选择要上传的启动页图片！');
			return false;
		}
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			$.post(form.attr("action"), form.serialize(), function(data){
				if(data.success){
					toastr.success(data.msg);
				}else{
					toastr.error(data.msg);
				}
				form.removeData("running");
			}, "json");
		}
	});
	
	var thumbnailFileName = '${appConfig.thumbnailFileName}';
	if($.trim(thumbnailFileName) != ""){
		imageThumbnail.attr('src', thumbnailFileName);
	}else{
		imageThumbnail.attr('src', noPicUrl);
	}
	
	$("#fisVisible option[value='${appConfig.fisVisible}']").prop("selected",true);

});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-6"><h3>APP参数设置</h3></div>
  <div class="col-md-6"><p class="text-right" style="margin:10px 0 0 0;">
	<button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button>
  </p></div>
</div>
<div class="panel panel-info">
	<div class="panel-heading">设置APP城市定制信息</div>
	<div class="panel-body">
		<table class="table table-hover table-striped table-bordered">
			<tr>
				<td width="50%" align="right"><strong>所属城市：</strong></td>
				<td width="50%">${appConfig.configCityName}</td>
			</tr>
		</table>
	</div>
	<div class="panel-footer"><p class="text-rigth"><small>零到壹，查找优惠</small></p></div>
</div>
<form id="editAppFlashForm" action="${ctx}/fxl/app/editAppFlash" method="post" class="form-inline" role="form">
	<input id="cityId" name="cityId" type="hidden" value="${appConfig.cityId}">
	<div class="alert alert-danger text-center" role="alert" style="padding: 5px;">
		<strong><fmt:message key="fxl.common.redRequired" /></strong>
	</div>
	<div class="form-group has-error"><label for="fisVisible">启动页是否显示：</label>
		<select id="fisVisible" name="fisVisible" class="validate[required] form-control">
			<option value=""><fmt:message key="fxl.common.select" /></option>
			<c:forEach var="isVisibleItem" items="${isVisibleMap}"> 
			<option value="${isVisibleItem.key}">${isVisibleItem.value}</option>
			</c:forEach>
		</select>
	</div>
	<div class="form-group col-md-12">
		<label for="imageId">启动页图片：</label>
		<input id="imageId" name="imageId" type="hidden" value="${appConfig.imageId}">
		<div class="row">
			<div class="col-md-7" style="min-height: 100px;" id="dndDiv"><a href="#" class="thumbnail"><img id="imageThumbnail" src='' alt=""></a></div>
			<div class="col-md-2"><div id="filePicker">选择图片</div></div>
			<div class="col-md-3">
				<div class="form-group"><label for="imageWidth">图片宽度：</label>
					<div class="input-group">
			    		<input type="text" id="imageWidth" name="imageWidth" class="validate[custom[integer],min[1],max[2048]] form-control" size="5" value="${appConfig.imageWidth}"><div class="input-group-addon">px</div>
			    	</div>
				</div>
				<div class="form-group"><label for="imageHeight">图片高度：</label>
					<div class="input-group">
			    		<input type="text" id="imageHeight" name="imageHeight" class="validate[custom[integer],min[1],max[2048]] form-control" size="5" value="${appConfig.imageHeight}"><div class="input-group-addon">px</div>
			    	</div>
				</div>
			</div>
		</div>
		<p class="text-center"><button id="uploadBtn" type="button" class="btn btn-success"><span class="glyphicon glyphicon-cloud-upload"></span> 开始上传</button></p>
		<div class="progress">
			<div id="uploadFileProgress" class="progress-bar progress-bar-info progress-bar-striped active" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%"></div>
		</div>
	</div>
	<p class="text-center">
	<button class="btn btn-primary" type="submit">
		<span class="glyphicon glyphicon-floppy-saved"></span>
		<fmt:message key="fxl.button.save" />
	</button>
	<button class="btn btn-warning" type="button" onclick="javascrtpt:window.location.href='${ctx}/fxl/app/config'">
		<span class="glyphicon glyphicon-share-alt"></span>
		<fmt:message key="fxl.button.return"/>
	</button>
	</p>
</form>
</body>
</html>