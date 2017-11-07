<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>奖品配置</title>
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
		    "url": "${ctx}/fxl/carnival/getCarnivalPrizeList/" + $("#carnivalId").val(),
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
		"lengthChange": false,
		"columns" : [
		{	"title" : "ID",
			"data" : "DT_RowId",
			"visible": false,
			"orderable" : false
		}, {
			"title" : '<center>奖品等级</center>',
			"data" : "flevel",
			"className": "text-center",
			"width" : "150px",
			"orderable" : false
		}, {
			"title" : '<center>奖品名称</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>奖品数量</center>',
			"data" : "fcount",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "20px",
			"className": "text-center operation",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [4],
			"render" : function(data, type, full) {
				var retString = '';
					retString = '<div class="btn-group"><button type="button" class="btn btn-success btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">执行<span class="caret"></span></button><ul class="dropdown-menu">'
						+ '<li><a href="javascript:;" id="edit" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-edit"></span> <fmt:message key="fxl.button.edit" />奖品</a></li>'
						+ '<li><a href="javascript:;" id="del" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-trash"></span> <fmt:message key="fxl.button.delete" />奖品</a></li>'
				return retString;
			}
		}]
	});
		
	$("#eventBargainingTable").delegate("a[id=del]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该奖品吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/carnival/delCarnivalPrize/" + mId , function(data) {
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
		$.post("${ctx}/fxl/carnival/getCarnivalPrizeDetail/" + mId, function(data) {
			if(data.success){
				$("#createEventBargainingForm #id").val(mId);
				$("#createEventBargainingForm #ftitle").val(data.ftitle);
				$("#createEventBargainingForm #eventId").val(data.feventId).trigger("change");
				$("#createEventBargainingForm #fimage").val(data.fimage);
				$("#createEventBargainingForm #flevel").val(data.flevel);
				$("#createEventBargainingForm #fcount").val(data.fcount);
				
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
    
    var prizeId = "";
	var selected = "";
	//活动表中的操作按钮的点击事件方法
	$("#eventBargainingTable").delegate("tbody tr[id]", "click", function(e){
		e.stopPropagation();
		var tr = $(this);
		if(tr.attr("id") != prizeId){
			prizeId = tr.attr("id");
			eventBargainingTable2.ajax.reload(null,false);
		}
		if($.trim(selected) != ""){
			$("#eventBargainingTable tr[id=" + selected + "]").removeClass('info');
		}
		selected = this.id;
        tr.addClass('info');
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
				$.post("${ctx}/fxl/carnival/editCarnivalPrize",  form.serializeArray(), function(data){
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
    
	
	 var eventBargainingTable2 = $("table#eventBargainingTable2").DataTable({
			"language": dataTableLanguage,
			"filter": false,
			"paging": false,
			"autoWidth" : false,
		  	//"scrollY": "350px",
			"scrollCollapse": true,
			"processing": true,
			"serverSide": true,
			"ajax": {
			    "url": "${ctx}/fxl/carnival/getCarnivalPrizeDaysList",
				    "type": "POST",
				    "data": function (data) {
				    	data['prizeId'] = prizeId;
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
			},{
				"title" : '<center>活动第几日</center>',
				"data" : "fcarnivalDaySerial",
				"className": "text-center",
				"width" : "100px",
				"orderable" : false
			}, {
				"title" : '<center>发奖时间</center>',
				"data" : "fstartTime",
				"className": "text-center",
				"width" : "100px",
				"orderable" : false
			}, {
				"title" : '<center>发奖数量</center>',
				"data" : "fcount",
				"className": "text-center",
				"width" : "30px",
				"orderable" : false
			}],
		"columnDefs" : [{
			"targets" : [3],
			"render" : function(data, type, full) {
				return '<div class="input-group"><input type="text" id="number_' + full.DT_RowId + '" class="form-control validate[required] input-sm" size="10" value="' + data + '"><span class="input-group-btn"><button id="saveOrderBtn" class="btn btn-default btn-sm" type="button" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></button></span></div>';
			}
		},  {
			"targets" : [2],
			"render" : function(data, type, full) {
				return '<div class="input-group"><input type="text" id="startTime_' + full.DT_RowId + '" class="form-control validate[required] input-sm" size="25" value="' + data + '"><span class="input-group-btn"><button id="saveOrderBtn" class="btn btn-default btn-sm" type="button" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></button></span></div>';
			}
		}]
	});
  
	$("#eventBargainingTable2").delegate("button[id=saveOrderBtn]", "click", function(){
		var mId = $(this).attr("mId");
		var number = $('#number_'+mId);
		var startTime = $('#startTime_'+mId);
		if(!number.validationEngine('validate')){
	    	$.post("${ctx}/fxl/carnival/editCarnivalPrzieDays", $.param({id:mId,number:number.val(),startTime:startTime.val()},true), function(data) {
				if(data.success){
					toastr.success(data.msg);
					eventBargainingTable2.ajax.reload(null,false);
				}else{
					toastr.error(data.msg);
				}
			}, "json");
		}
	}); 
	
	var createEventBargainingModal =  $('#createEventBargainingModal');
	
	createEventBargainingModal.on('hide.bs.modal', function(e){
		if(e.target.id === "createEventBargainingModal"){
			createEventBargainingForm.trigger("reset");
		}
	});
    
    /* 解决select2跟modal冲突*/
    $.fn.modal.Constructor.prototype.enforceFocus = function () {};
    
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-8"><h3>奖品配置</h3></div>
  <div class="col-md-4"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>奖品列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createEventBonusBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 创建商品</button>
</div>

<!-- 显示列表两个 上下联动 -->
<table id="eventBargainingTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
<div class="row">
   <div class="col-md-12"><h3 class="text-center">奖品所属发奖日</h3></div>
</div>
<table id="eventBargainingTable2" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>

<!-- 编辑商品开始 -->
<div class="modal fade" id="createEventBargainingModal"  role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">编辑奖品信息</h4>
      </div>
	<form id="createEventBargainingForm" action="${ctx}/fxl/carnival/addCarnivalPrize" method="post" class="form-inline" role="form">
	    <input id="id" name="id" type="hidden">
	    <input id="carnivalId" name="carnivalId" value="${carnivalId}" type="hidden">
	    <div class="modal-body">
      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
   
        <div class="form-group has-error"><label for="flevel">奖项选择：</label>
		      <select id="flevel" name="flevel" class="validate[required] form-control">
					 <option value=""><fmt:message key="fxl.common.select" /></option>
					 <c:forEach var="carnivalPrizeItem" items="${carnivalPrizeMap}">
					 <option value="${carnivalPrizeItem.key}" >${carnivalPrizeItem.value}</option>
					 </c:forEach>
			  </select>
		</div>   
		<div class="form-group has-error" style="margin-left: 70px;"><label for="fcount">奖项数量：</label>
			  <div class="input-group" style="margin-left: 20px;">
	    		 <input type="text" id="fcount" name="fcount" class="form-control validate[required,custom[number]]" size="5"><div class="input-group-addon">个</div>
	    	  </div>
		</div>
      	<div class="form-group has-error"><label for="faddress">奖项名称：</label>
		        <input type="text" id="ftitle" name="ftitle" class="form-control validate[required,minSize[1],maxSize[250]]" size="90" placeholder="请输入奖项名称">
		</div>
		<div class="form-group has-error"><label for="eventId">关联奖品活动：</label>
			<select id="eventId" name="eventId" class="validate[required] form-control" style="width: 600px;">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="eventPrizeItem" items="${eventPrizeListMap}"> 
				<option value="${eventPrizeItem.key}">${eventPrizeItem.value}</option>
				</c:forEach>
			</select>
	    </div>
		<br/><br/>
		<!-- 图片上传 -->
	    <div class="row">
		   	<div class="col-md-2"><label for="fimage">上传奖品图片：</label>
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
		<br/>
		
		<!--<c:forEach var="daysItem" items="${carnivalrDaysPrizeMap}"> 
		<div class="form-group has-error"><label for="fbeginTime">开始发放时间(${daysItem.value})：</label>
			<div id="fbeginTimeDiv" class="input-group date form_datetime">
			  	<input id="fbeginTime" name="fbeginTime" type="text" class="form-control validate[required]" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
			</div>
		</div>
		<input type="hidden" id="daysPrizeId" name="daysPrizeId" value="${daysItem.key}">
		<div class="form-group has-error" style="margin-left: 70px;"><label for="ffsl">发放数量：</label>
			  <div class="input-group" style="margin-left: 20px;">
	    		 <input type="text" id="ffsl" name="ffsl" class="form-control validate[required,custom[number]]" size="5" value="0" ><div class="input-group-addon">个</div>
	    	  </div>
		</div>
	    <br/>  
       </c:forEach>
	   -->
	    <div class="form-group has-error">
	    <c:forEach var="carnivalrCredentiaItem" items="${carnivalrCredentiaMap}"> 
	        <div class="form-group has-error" style="margin-left: 40px;"><label for="prizeName">${carnivalrCredentiaItem.value}：</label>
			  <div class="input-group">
	    		 <input type="hidden" id="prizeName" name="prizeName" value="${carnivalrCredentiaItem.value}">
	    		 <input type="hidden" id="prizeType" name="prizeType" value="${carnivalrCredentiaItem.key}">
	    	     <input type="text" id="prizeNumber" name="prizeNumber" class="form-control validate[required,custom[number]]" size="5" value="0"><div class="input-group-addon">个</div>
	    	  </div>
		    </div>
		</c:forEach>
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
<!--编辑奖品结束-->.
</body>
</html>