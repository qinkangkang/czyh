<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/webuploader/webuploader.css" rel="stylesheet" type="text/css">
<script src="${ctx}/styles/webuploader/webuploader.html5only.min.js" type="text/javascript"></script>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css">
<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js" charset="UTF-8"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js" charset="UTF-8"></script>
<script type="text/javascript">
$(document).ready(function() {
	
	$('#fstartTimeDiv').datepicker({
	    format : "yyyy-mm-dd",
	    //startDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
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
		imageHeight:1099
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
			    data["pathVar"] = "bonusPosterPath";
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
		posterTable.ajax.reload();
	});
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	var posterTable = $("table#posterTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/bonus/getPosterList",
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
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>活动开始日期</center>',
			"data" : "fstartTime",
			"className": "text-center",
			"width" : "60px",
			"orderable" : false
		}, {
			"title" : '<center>活动结束日期</center>',
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
			"width" : "40px",
			"className": "text-center",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [5],
			"render" : function(data, type, full) {
				var retString = '';
				if(data.fstatus ==10 || data.fstatus ==30){
					retString = '<div class="btn-group"><button type="button" class="btn btn-success btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">执行<span class="caret"></span></button><ul class="dropdown-menu">'
					+ '<li><a href="javascript:;" id="edit" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-edit"></span> <fmt:message key="fxl.button.edit" />海报</a></li>'
					+ '<li><a href="javascript:;" id="del" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-trash"></span> <fmt:message key="fxl.button.delete" />海报</a></li>'
					+ '<li><a href="javascript:;" id="onsale" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-open"></span> 发布海报</a></li>'

				}else{
					retString = '<div class="btn-group"><button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">执行<span class="caret"></span></button><ul class="dropdown-menu"><li><a href="javascript:;" id="offsale" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-save"></span> 取消发布海报</a></li>';
				}
				return retString;
			}
		}]
	});
	
	$("#posterTable").delegate("a[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		$('#resetBtn').hide();
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/bonus/getPoster/" + mId, function(data) {
			if(data.success){
				$("#id").val(mId);
				$("#ftitle").val(data.ftitle);
				$("#fstartTime").val(data.fstartTime);
				$("#fendTime").val(data.fendTime);
				$("#fremark").val(data.fremark);
				$("#fimageWidth").val(data.fimageWidth);
				$("#fimageHeight").val(data.fimageHeight);
				$("#fqrcodeWh").val(data.fqrcodeWh);
				$("#fqrcodeX").val(data.fqrcodeX);
				$("#fqrcodeY").val(data.fqrcodeY);
				$("#fimage").val(data.fimage);
				//console.log(data.fimage);
				//console.log(data.fimagePath);
				//$("#fwaterMark option[value='" + data.fwaterMark + "']").prop("selected",true);
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
				createPosterModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	
	var createPosterModal =  $('#createPosterModal');
	 	
 	createPosterModal.on('hide.bs.modal', function(e){
		if(e.target.id === "createPosterModal"){
			createPosterForm.trigger("reset");
		}
	});
	
	createPosterModal.on('shown.bs.modal', function(e){
		initUploader();
		$("#mandatoryJpg option[value='false']").prop("selected",true);
	});
	
	$('#createChannelBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		$('#resetBtn').show();
		imageThumbnail.attr("src",noPicUrl);
		createPosterModal.modal('show')
	});
	
	var createPosterForm = $('#createPosterForm');
	
	/* 	createPosterForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	}); */
	
	createPosterForm.on("submit", function(event){
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
						posterTable.ajax.reload(null,false);
						createPosterModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/bonus/editorPoster", form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						posterTable.ajax.reload(null,false);
						createPosterModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
	
	$("#posterTable").delegate("a[id=onsale]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要发布当前活动海报吗？',
		    okValue: '立即发布',
		    ok: function () {
		    	$.post("${ctx}/fxl/bonus/onPoster/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						posterTable.ajax.reload(null,false);
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
	
	$("#posterTable").delegate("a[id=offsale]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要取消发布当前海报吗？',
		    okValue: '即刻取消',
		    ok: function () {
		    	$.post("${ctx}/fxl/bonus/offPoster/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						posterTable.ajax.reload(null,false);
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
	
	$("#posterTable").delegate("a[id=del]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除当前海报吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/bonus/delPoster/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						posterTable.ajax.reload(null,false);
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
	
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-10"><h3>海报活动内容配置</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>海报活动内列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createChannelBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 创建海报</button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/app/getChannelList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_ftitle">活动名称：</label>
			<input type="text" id="s_ftitle" name="s_ftitle" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_status">海报状态：</label>
			<select id="s_status" name="s_status" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="statusItem" items="${posterStatusMap}"> 
				<option value="${statusItem.key}">${statusItem.value}</option>
				</c:forEach>
			</select>
		</div>
		<p class="text-center"><button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>

<table id="posterTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
<!--编辑海报开始-->
<div class="modal fade" id="createPosterModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
   <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header">
           <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
               <h4 class="modal-title" id="myModalLabel">编辑海报信息</h4>
        </div>
    <form id="createPosterForm" action="${ctx}/fxl/bonus/addPoster" method="post" class="form-inline" role="form">
        <input id="id" name="id" type="hidden">
        <div class="modal-body">
           <div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
		   
		   <div class="form-group has-error"><label for="ftitle">活动名称：</label>
			  	<input type="text" id="ftitle" name="ftitle" class="form-control validate[required,minSize[2],maxSize[250]]" size="90">
	       </div>
	       <div class="form-group has-error"><label for="fstartTime">活动有效期：</label>
			   <div id="fstartTimeDiv" class="input-daterange input-group date" style="width:330px;">
				<input type="text" class="form-control validate[required]" id="fstartTime" name="fstartTime" style="cursor: pointer;"><span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
			 	<input type="text" class="form-control validate[required]" id="fendTime" name="fendTime" style="cursor: pointer;">
			   </div>
		   </div>
		   <div class="form-group" style="min-height: 110px;"><label for="fremark">活动备注：</label>
		     	<textarea id="fremark" name="fremark" cols="100%" rows="5" class="validate[required,maxSize[250]] form-control"></textarea>
		   </div>
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
			<div class="form-group"><label for="fqrcodeWh">二维码边长：</label>
				<div class="input-group">
		    		<input type="text" id="fqrcodeWh" name="fqrcodeWh" class="validate[custom[integer],min[1],max[2048]] form-control" size="6"><div class="input-group-addon">px</div>
		    	</div>
			</div>
			<div class="form-group"><label for="fqrcodeXy">二维码坐标：</label>
		    	<div class="input-group">
					<div class="input-group-addon">X</div>
					<input type="text" id="fqrcodeX" name="fqrcodeX" class="validate[required,custom[integer],min[0],max[2000]] form-control"  size="4">
					<div class="input-group-addon">-</div>
					<div class="input-group-addon">Y</div>
					<input type="text" id="fqrcodeY" name="fqrcodeY" class="validate[required,custom[integer],min[0],max[2000]] form-control"  size="4">
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
<!--编辑海报结束-->
</body>
</html>