<%@ page contentType="text/html;charset=UTF-8" %>
<meta name="decorator" content="/WEB-INF/decorators/decorator_no_theme.jsp">
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link rel="stylesheet" type="text/css" href="${ctx}/styles/demo/css/webuploader.css">
<link rel="stylesheet" type="text/css" href="${ctx}/styles/demo/css/style.css">
<script type="text/javascript" src="${ctx}/styles/demo/js/upload.js"></script>
<script type="text/javascript" src="${ctx}/styles/demo/js/webuploader.min.js"></script>
<script type="text/javascript">
$(document).ready(function() {
	
	var defaultSize = {
		imageWidth:750,
		imageHeight:750
	};
	
	var fimage2 = $("#fimage2");
	var imageThumbnail = $("#imageThumbnail");
	var uploadFileProgress = $("#uploadFileProgress");
	var imageWidth = $("#imageWidth");
    var imageHeight = $("#imageHeight");
    imageWidth.val(defaultSize.imageWidth);
    imageHeight.val(defaultSize.imageHeight);

    var $list = $('#fileList'),
    // 优化retina, 在retina下这个值是2
    ratio = window.devicePixelRatio || 1,
    // 缩略图大小
    thumbnailWidth = 90 * ratio,
    thumbnailHeight = 90 * ratio,

    // Web Uploader实例
    uploader;
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
        fileNumLimit: 10,
        //只允许选择图片
        accept: {
        	title: '请选择您要上传的图片文件',
            extensions: 'gif,jpg,jpeg,bmp,png',
            mimeTypes: 'image/gif,image/jpg,image/jpeg,image/png,image/bmp'
        }
    });
    
    // 当有文件添加进来的时候
    uploader.on('fileQueued', function (file) {
        var $li = $('<div id="' + file.id + '" class="cp_img"><img><div class="cp_img_jian"></div></div>'),
        $img = $li.find('img');

        // $list为容器jQuery实例
        $list.append($li);

        // 创建缩略图
        // 如果为非图片文件，可以不用调用此方法。
        // thumbnailWidth x thumbnailHeight 为 100 x 100
        uploader.makeThumb(file, function (error, src) {
            if (error) {
                $img.replaceWith('<span>不能预览</span>');
                return;
            }
            $img.attr('src', src);
        }, thumbnailWidth, thumbnailHeight);
    });
    
	// 文件上传之前，先附带上要提交的信息。
	uploader.on( 'uploadBeforeSend', function( object, data, headers ) {
	    data["pathVar"] = "eventImageDetailPath";
	    if($.trim(imageWidth.val()) == ""){
	    	imageWidth.val(defaultSize.imageWidth);
	    }
	    if($.trim(imageHeight.val()) == ""){
	    	imageHeight.val(defaultSize.imageHeight);
	    }
	    data["imageWidth"] = imageWidth.val();
	    data["imageHeight"] = imageHeight.val();
	    data["watermark"] = $("#watermark").val();
	});

    // 文件上传过程中创建进度条实时显示。
    uploader.on('uploadProgress', function (file, percentage) {
        var $li = $('#' + file.id),
            $percent = $li.find('.progress span');

        // 避免重复创建
        if (!$percent.length) {
            $percent = $('<p class="progress"><span></span></p>').appendTo($li).find('span');
        }
        $percent.css('width', percentage * 100 + '%');
    });

    // 文件上传成功，给item添加成功class, 用样式标记上传成功。
    uploader.on('uploadSuccess', function (file, data) {
    	if(data.success){
    		toastr.success(data.msg);
			fimage2.val(fimage2.val() + data.imageId + ";");
		}else{
			toastr.error(data.msg);
		}
        $('#' + file.id).addClass('upload-state-done');
    });

    // 文件上传失败，显示上传出错。
    uploader.on('uploadError', function (file) {
        var $li = $('#' + file.id),
            $error = $li.find('div.error');
        // 避免重复创建
        if (!$error.length) {
            $error = $('<div class="error"></div>').appendTo($li);
        }
        $error.text('上传失败');
        toastr.warning('图片文件上传失败，失败代码：' + reason);
    });

    // 完成上传完了，成功或者失败，先删除进度条。
    uploader.on('uploadComplete', function (file) {
        $('#' + file.id).find('.progress').remove();
    });

    //所有文件上传完毕
    uploader.on("uploadFinished", function (){
       //提交表单
    });

    //开始上传
    $("#ctlBtn").click(function () {
        uploader.upload();
    });

    //显示删除按钮
    $("#dndDiv").delegate("div.cp_img", "mouseover", function(){
        $(this).children(".cp_img_jian").css('display', 'block');
    });
    
    //隐藏删除按钮
    $("#dndDiv").delegate("div.cp_img", "mouseout", function(){
        $(this).children(".cp_img_jian").css('display', 'none');

    });
    //执行删除方法
    $list.on("click", ".cp_img_jian", function (){
        var Id = $(this).parent().attr("id");
        uploader.removeFile(uploader.getFile(Id,true));
        $(this).parent().remove();
    });
	
	var createEventCForm = $('#createEventCForm');
	
	createEventCForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		if(fimage2.val() == ""){
			toastr.warning('请选择要上传的活动主图片！');
			return false;
		}
		var form = $(this);
		if(form.data("running") != "ok"){
			form.data("running","ok");
			$.post(form.attr("action"), $.param($.merge(form.serializeArray(),[{name:"eventId", value:$("#eventId").val()}]),true), function(data){
				if(data.success){
					toastr.success(data.msg);
					$('#eventTab a:eq(3)').tab('show');
				}else{
					toastr.error(data.msg);
				}
				form.removeData("running");
			}, "json");
		}
	});
	
});
</script>
<form id="createEventCForm" action="${ctx}/fxl/event/addEventC" method="post" class="form-inline" role="form">
	<h4>活动主图片与细节图片</h4>
	<div class="row">
		<input id="fimage2" name="fimage2" type="hidden">
		<div class="col-md-9" id="dndDiv">
			<table class="tc_table_cp" border="0">
		        <tr>
		            <td width="104">图片上传：</td>
		            <td colspan="3">
		                <div id="fileList"></div>
		                <div class="cp_img_jia" id="filePicker"></div>
		            </td>
		        </tr>
		        <tr>
		            <td width="104"></td>
		            <td colspan="3">
		                 <button id="ctlBtn" class="btn btn-default" type="button">开始上传</button>
		            </td>
		        </tr>
		    </table>
		</div>
		<div class="col-md-3">
			<div class="form-group"><label for="imageWidth">图片宽度：</label>
				<div class="input-group">
		    		<input type="text" id="imageWidth" name="imageWidth" class="validate[custom[integer],min[1],max[2048]] form-control" value="${imageWidth}" size="20"><div class="input-group-addon">px</div>
		    	</div>
		   	</div>
			<div class="form-group"><label for="imageHeight">图片高度：</label>
				<div class="input-group">
		    		<input type="text" id="imageHeight" name="imageHeight" class="validate[custom[integer],min[1],max[2048]] form-control" value="${imageHeight}" size="20"><div class="input-group-addon">px</div>
		    	</div>
		   	</div>
		   	<div class="form-group"><label for="watermark">上传图片是否加水印：</label>
		        <select id="watermark" name="watermark" class="form-control">
					<option value="false">不加水印</option>
					<option value="true">加水印</option>
				</select>
		    </div>
		</div>
	</div>
	<div class="form-group"><label for="feventTime">活动已有图片数量：</label>
		<div class="input-group">
    		<input type="text" id="feventTime" name="feventTime" class="form-control" value="${imageCount}" readonly="readonly" size="6"><div class="input-group-addon">张</div>
    	</div>
    </div>
    <div class="alert alert-warning" role="alert">
      <strong>操作提示！</strong> 您本次新上传的图片会覆盖之前的活动图片。如果上传多张图片，会默认将第一张图片作为活动的主图，其它图片作为活动的细节图。
    </div>
	<hr>
	<p class="text-center">
	<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-arrow-right"></span> 保存并下一步</button>
	</p>
</form>
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
			<div id="uploadFileProgress" class="progress-bar progress-bar-info progress-bar-striped active" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%"></div>
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