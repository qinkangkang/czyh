<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/webuploader/webuploader.css" rel="stylesheet" type="text/css">
<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js"></script>
<script type="text/javascript" charset="utf-8" src="${ctx}/styles/ueditor/ueditor.config.js"></script>
<script type="text/javascript" charset="utf-8" src="${ctx}/styles/ueditor/ueditor.all.min.js"></script>
<script type="text/javascript" charset="utf-8" src="${ctx}/styles/ueditor/lang/zh-cn/zh-cn.js"></script>
<script src="${ctx}/styles/webuploader/webuploader.html5only.min.js" type="text/javascript"></script>
<script type="text/javascript">
$(document).ready(function() {
	
	var searchForm = $('form#searchForm');
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#select").click(function(e) {
		articleTable.ajax.reload();
		//$("#selectDiv").slideUp("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	var articleTable = $("table#articleTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/operating/article/getArticleList",
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
			"title" : '<center>标题</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "80px",
			"orderable" : false
		}, {
			"title" : '<center>类别</center>',
			"data" : "ftype",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>排序号</center>',
			"data" : "forder",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>推荐数量</center>',
			"data" : "frecommend",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>评价数量</center>',
			"data" : "fcomment",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>状态</center>',
			"data" : "fstatusString",
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
			"targets" : [7],
			"render" : function(data, type, full) {
				var retString = '<button id="edit" mId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs"><fmt:message key="fxl.button.edit" /></button><button id="del" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs"><fmt:message key="fxl.button.delete" /></button>';
				if(data.fstatus == 10 || data.fstatus == 90){
					retString += '<button id="onsale" mId="' + full.DT_RowId + '" type="button" class="btn btn-success btn-xs">上架</button>';
				}else{
					retString += '<button id="offsale" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs">下架</button>';
				}
				return retString;
			}
		}]
	});
	
	$("#articleTable").delegate("button[id=onsale]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要即刻上架该文章吗？',
		    okValue: '即刻上架',
		    ok: function () {
		    	$.post("${ctx}/fxl/operating/article/onSaleArticle/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						articleTable.ajax.reload(null,false);
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
	
	$("#articleTable").delegate("button[id=offsale]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要即刻下架该文章吗？',
		    okValue: '即刻下架',
		    ok: function () {
		    	$.post("${ctx}/fxl/operating/article/offSaleArticle/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						articleTable.ajax.reload(null,false);
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
	
	$("#articleTable").delegate("button[id=del]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该文章吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/operating/article/delArticle/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						articleTable.ajax.reload(null,false);
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
	
	var ue = UE.getEditor('container');
	
	$("#articleTable").delegate("button[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		$('#resetBtn').hide();
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/operating/article/getArticle/" + mId, function(data) {
			if(data.success){
				$("#id").val(mId);
				$("#ftitle").val(data.ftitle);
				$("#ftype").val(data.ftype);
				$("#fbrief").val(data.fbrief);
				$("#forder").val(data.forder);
				$("#fimage").val(data.fimage);
				$("#fcityId option[value='" + data.fcityId + "']").prop("selected",true);
				$("#fartType option[value='" + data.fartType + "']").prop("selected",true);
				$("#fartCity option[value='" + data.fartCity + "']").prop("selected",true);
				if($.trim(data.fimagePath) == ""){
					imageThumbnail.attr("src",noPicUrl);
				}else{
					imageThumbnail.attr("src",data.fimagePath);
				}
				if($.trim(data.fdetail) == ""){
					ue.setContent("", false);
				}else{
					ue.setContent(data.fdetail, false);
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
				createArticleModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	var uploadProgressModal =  $('#uploadProgressModal');
	
	uploadProgressModal.on('hidden.bs.modal', function(e){
		uploadFileProgress.css( 'width', '0%' );
		createArticleModal.css("overflow-y","auto");
	});
	
	$("#uploadStopBtn").click(function(){
		uploader.stop();
	});
	
	var defaultSize = {
		imageWidth:712,
		imageHeight:394
	};
	
	var imageWidth = $("#imageWidth");
    var imageHeight = $("#imageHeight");
    imageWidth.val(defaultSize.imageWidth);
    imageHeight.val(defaultSize.imageHeight);
    var imageThumbnail = $("#imageThumbnail");
    var uploadFileProgress = $("#uploadFileProgress");
	
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
			    data["pathVar"] = "articleImageMainPath";
			    data["watermark"] = true;
			    if($.trim(imageWidth.val()) == ""){
			    	imageWidth.val(defaultSize.imageWidth);
			    }
			    if($.trim(imageHeight.val()) == ""){
			    	imageHeight.val(defaultSize.imageHeight);
			    }
			    data["imageWidth"] = imageWidth.val();
			    data["imageHeight"] = imageHeight.val();
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
	
	var createArticleModal =  $('#createArticleModal');
	
	createArticleModal.on('hide.bs.modal', function(e){
		createArticleForm.trigger("reset");
	});
	
	createArticleModal.on('shown.bs.modal', function(e){
		initUploader();
	});
	
	$('#createArticleBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		$('#resetBtn').show();
		imageThumbnail.attr("src",noPicUrl);
		createArticleModal.modal('show');
	});
	
	var createArticleForm = $('#createArticleForm');
	
	createArticleForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	var fimage = $("#fimage");
	
	createArticleForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		if(fimage.val() == ""){
			dialog({
				fixed: true,
		        title: '操作提示',
		        content: '请选择要上传的图片介绍',
		        cancelValue: '关闭',
		        cancel: function (){}
		    }).showModal();
			return false;
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			if($('#actionFlag').val() == "add"){
				$.post(form.attr("action"), form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						articleTable.ajax.reload(null,false);
						createArticleModal.modal('hide')
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/operating/article/editArticle", form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						articleTable.ajax.reload(null,false);
						createArticleModal.modal('hide')
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
	
	createArticleForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createArticleForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		ue.setContent("", false);
		createArticleForm.validationEngine('hideAll');
	});
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-10"><h3>发现文章</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>发现文章列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createArticleBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 创建文章</button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/operating/article/getArticleList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_fname">文章标题：</label>
			<input type="text" id="s_fname" name="s_fname" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_flevel">文章状态：</label>
			<select id="s_flevel" name="s_flevel" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="levelItem" items="${sponsorLevelMap}"> 
				<option value="${levelItem.key}">${levelItem.value}</option>
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
<table id="articleTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
<!--编辑文章开始-->
<div class="modal fade" id="createArticleModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">编辑文章信息</h4>
      </div>
      <form id="createArticleForm" action="${ctx}/fxl/operating/article/addArticle" method="post" class="form-inline" role="form">
      <div class="modal-body">
      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
      		<input id="id" name="id" type="hidden">
      		<input id="ftype" name="ftype" value="1" type="hidden" />
      		<div class="form-group has-error"><label for="ftitle">文章标题：</label>
				<input type="text" id="ftitle" name="ftitle" class="form-control validate[required,minSize[2],maxSize[250]]" size="60">
		    </div>
		    <div class="form-group has-error"><label for="fcityId">所属城市：</label>
				<select id="fcityId" name="fcityId" class="validate[required] form-control">
					<option value=""><fmt:message key="fxl.common.select" /></option>
					<c:forEach var="cityItem" items="${cityMap}"> 
					<option value="${cityItem.key}">${cityItem.value}</option>
					</c:forEach>
				</select>
			</div>
		    <div class="form-group has-error"><label for="fartCity">区域：</label>
		        <select id="fartCity" name="fartCity" class="validate[required] form-control">
					<option value=""><fmt:message key="fxl.common.select" /></option>
					<c:forEach var="regionListItem" items="${regionList}">
					<option value="${regionListItem.value}" >${regionListItem.value}</option>
					</c:forEach>
				</select>
		    </div>
		    <div class="form-group has-error"><label for="fartType">文章类目：</label>
		        <select id="fartType" name="fartType" class="validate[required] form-control">
					<option value=""><fmt:message key="fxl.common.select" /></option>
					<c:forEach var="artTypeItem" items="${artTypeMap}">
					<option value="${artTypeItem.key}" >${artTypeItem.value}</option>
					</c:forEach>
				</select>
		    </div>
		    <div class="form-group"><label for="forder">文章排序号：</label>
				<div class="input-group">
		    		<input type="text" id="forder" name="forder" class="form-control validate[custom[integer],min[-100],max[100]]" size="5"><div class="input-group-addon">请输入-100~100之间整数，数值越大排序越靠前</div>
		    	</div>
		    </div>
		    <div class="form-group has-error" style="min-height: 110px;"><label for="fbrief">文章简介：</label>
		        <textarea id="fbrief" name="fbrief" cols="80" rows="5" class="validate[required,maxSize[250]] form-control"></textarea>
		    </div>
			<div class="row">
				<div class="col-md-2"><label for="fimage">文章主图：</label>
					<input id="fimage" name="fimage" type="hidden" value=""></div>
				<div class="col-md-7" style="min-height: 100px;" id="dndDiv"><a href="#" class="thumbnail"><img id="imageThumbnail" src="${ctx}/styles/fxl/images/nopic.png" alt=""></a></div>
				<div class="col-md-3"><div id="filePicker">选择图片</div></div>
			</div>
			<div class="row">
				<div class="col-md-6">
				<div class="form-group"><label for="imageWidth">图片宽度：</label>
					<div class="input-group">
			    		<input type="text" id="imageWidth" name="imageWidth" class="validate[custom[integer],min[1],max[2048]] form-control" size="6"><div class="input-group-addon">px</div>
			    	</div>
				</div>
				</div>
				<div class="col-md-6">
				<div class="form-group"><label for="imageHeight">图片高度：</label>
					<div class="input-group">
			    		<input type="text" id="imageHeight" name="imageHeight" class="validate[custom[integer],min[1],max[2048]] form-control" size="6"><div class="input-group-addon">px</div>
			    	</div>
				</div>
				</div>
			</div>
			<p class="text-center"><button id="uploadBtn" type="button" class="btn btn-success"><span class="glyphicon glyphicon-cloud-upload"></span> 开始上传</button>
			<button id="delUploadBtn" class="btn btn-danger" type="button"><span class="glyphicon glyphicon-trash"></span> 删除图片</button></p>
			<div class="form-group"><label>文章内容：</label>
		        <!-- 加载编辑器的容器 -->
				<script id="container" name="fdetail" type="text/plain" style="width:850px;height:450px;"></script>
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
<!--编辑文章结束-->
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
</body>
</html>