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
//var name;
$(document).ready(function() {
	
	 $.validationEngineLanguage.allRules.checkGoodsNumber={
			"url" : '${ctx}/fxl/event/checkGoodsNO',
			"extraDataDynamic": ['#fgoodsId','#id'],
			"alertTextOk" : '您可以使用该编号！',
		    "alertText" : '您输入的编号已经存在，请更换其他编号！',
		    "alertTextLoad" : '正在验证该编号是否被占用……'
		}; 
	
		/* $.validationEngineLanguage.allRules.checkTypeClass={
			"url" : '${ctx}/fxl/event/checkTypeClassIsExist',
			"extraDataDynamic": ['#fextendClassId',"#fid"],
			"alertTextOk" : '您可以使用该排序！',
		    "alertText" : '您输入的排序值已被占用，请更换其它排序！',
		    "alertTextLoad" : '正在验证该排序是否被占用……'
		}; */ 
		
	var searchForm = $('form#searchForm');
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#select").click(function(e) {
		SkuTable.ajax.reload();
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	
	var SkuTable = $("table#SkuTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,//自适应宽度
	  	//"scrollY": "400px",//表格的高度
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,//分页
		"ajax": {
		    "url": "${ctx}/fxl/event/getSkuList?eventId=${eventId}",
 		    "type": "POST",
 		    "data": function (data) {//发送给服务器的数据
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
		},{
			"title" : '<center>属性一</center>',
			"data" : "cfvalue",
			"render":function(data,type,full){
					if(data!=null){
							return  data;	
					}else {
							return " ";
					}
				},
			
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		},{
			"title" : '<center>属性二</center>',
			"data" : "tfvalue",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>商品编号</center>',
			"data" :"fgoodsNO",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		},{
			"title" : '<center>原价</center>',
			"data" : "price",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>商品售价</center>',
			"data" : "fpriceMoney",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		},{
			"title" : '<center>总库存</center>',
			"data" : "ftotal",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		},{
			"title" : '<center>剩余库存</center>',
			"data" : "fstock",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		},{
			"title" : '<center>限购数量</center>',
			"data" : "flimitation",
			"render":function(data,type,full){
				if(data==-1){
					return "不限购";
				}else{
					return data;
				}
					
				
			},
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		},{
			"title" : '<center>上传图片</center>',
			"data" : "fHavingImage",
			"render":function(data,type,full){
				switch(data){
					case 0: return "是" ; break;
					case 1: return "否"; break;
				}
			},
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
			
		},{
			"title" : '<center>默认显示</center>',
			"data" : "flag",
			"render":function(data,type,full){
				switch(data){
					case 0: return "是" ; break;
					case 1: return "否"; break;
				}
			},
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		},{
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "50px",
			"className": "text-center",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [11],
			"render" : function(data, type, full) {
				var retString = '<button id="edit" mId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs"><fmt:message key="fxl.button.edit" /></button><button id="del" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs"><fmt:message key="fxl.button.delete" /></button>';
				return retString;
			}
		}]
	});
	
	
	$("#SkuTable").delegate("button[id=del]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该sku吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/event/delSku/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						SkuTable.ajax.reload(null,false);
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
	
	


	
	$("#SkuTable").delegate("button[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		$('#resetBtn').hide();//隐藏重置按钮
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/event/getSkuInfo/" + mId, function(data) {
			if(data.success){
				$("#id").val(mId);
				$("#fgoodsId").val(data.fgoodsId);
				$("#fclassTypeValue1").val(data.fclassTypeValue1);
				$("#fclassTypeValue2").val(data.fclassTypeValue2);
				$("#value").val(data.value);
				$("#fgoodsNO").val(data.fgoodsNO);
				$("#fprice").val(data.fprice);
				$("#fpriceMoney").val(data.fpriceMoney);
				$("#ftotal").val(data.ftotal);
				$("#fstock").val(data.fstock);
				$("#flimitation").val(data.flimitation);
				$("#fhavingImage").val(data.fhavingImage);
				$("#flag").val(data.flag);
				$("#fimage").val(data.fimage);
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
				createSkuModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
		
	var createSkuModal =  $('#createSkuModal');
	
	
	createSkuModal.on('hide.bs.modal', function(e){
		createSkuForm.trigger("reset");
	});
	
	createSkuModal.on('shown.bs.modal', function(e){
		initUploader();
	});
	
	$('#createSkuBtn').on('click',function(e) {
		$('#fgoodsId').val($('#feventId').val());
		$('#value').val($('#fTypeValue').val());
		$('#actionFlag').val("add");
		$('#resetBtn').show();
		//imageThumbnail.attr("src",noPicUrl);
		createSkuModal.modal('show');
	});
	
	var createSkuForm = $('#createSkuForm');
	
	createSkuForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	createSkuForm.on("submit", function(event){
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
						SkuTable.ajax.reload(null,false);
						createSkuModal.modal('hide')
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/event/editSku", form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						SkuTable.ajax.reload(null,false);
						createSkuModal.modal('hide')
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
	
	createSkuForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createSkuForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		createSkuForm.validationEngine('hideAll');
	});
	
var uploadProgressModal =  $('#uploadProgressModal');
	
	uploadProgressModal.on('hidden.bs.modal', function(e){
		uploadFileProgress.css( 'width', '0%' );
		createSkuModal.css("overflow-y","auto");
	});
	
	$("#uploadStopBtn").click(function(){
		uploader.stop();
	});
	
	var defaultSize = {
		imageWidth:165,
		imageHeight:162
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
			var fimage = $("#fimage");
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
				var $li = $('#' + file.id),
	            $error = $li.find('div.error');
	        	// 避免重复创建
	        	if (!$error.length) {
	         	   $error = $('<div class="error"></div>').appendTo($li);
	       		}
	        	$error.text('上传失败');
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
	
	
});

</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<input id="feventId" name="feventId"  value=${eventId}  type="hidden">
<input id="fTypeValue" name="fTypeValue"  value=${value}  type="hidden">
<div class="row">
  <div class="col-md-10"><h3>商品sku</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>商品库存列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createSkuBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 创建商品sku</button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<table id="SkuTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>

<!--编辑sku开始-->
<div class="modal fade" id="createSkuModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">编辑商品属性值信息</h4>
      </div>
      <form id="createSkuForm" action="${ctx}/fxl/event/addSku" method="post" class="form-inline" role="form">
	      <div class="modal-body">
	      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
	      		<input id="fgoodsId" name="fgoodsId"  value=${eventId}  type="hidden">
	      		<input id="id" name="id" type="hidden">
	      		<div class="form-group has-error"><label for="fclassTypeValue1">属性一：</label>
					<select id="fclassTypeValue1" name="fclassTypeValue1" class=" form-control" style="width: 120px;">
						<option value="0">---请选择---</option>
						<c:forEach var="typeClassValue" items="${typeValueList1}"> 
							<option value="${typeClassValue.id}">${typeClassValue.fvalue}</option>
						</c:forEach>
					</select>
			    </div>
	      		<div class="form-group has-error"><label for="fclassTypeValue2">属性二：</label>
					 <select id="fclassTypeValue2" name="fclassTypeValue2" class=" form-control" style="width: 120px;">
						<option value="0">---请选择---</option>
						<c:forEach var="typeClassValue" items="${typeValueList2}"> 
							<option value="${typeClassValue.id}">${typeClassValue.fvalue}</option> 
						</c:forEach>
					</select>
			    </div>
			    
	      		<div class="form-group has-error"><label for="fgoodsNO">商品编号：</label>
	      		   <span><input type="text" id="value" name="value" readonly="true" size="2" class="form-control"></span>
					<input type="text" id="fgoodsNO" name="fgoodsNO" class="form-control validate[required,minSize[1],maxSize[12],ajax[checkGoodsNumber]]" size="30" placeholder="***输入最长长度为12">
			    </div>

	      		<div class="form-group has-error" style="display:inline;"><label for="fprice" >商品原价：</label>
					<input type="text" id="fprice" name="fprice" class="form-control validate[required,minSize[1],maxSize[30]]" size="35">
			    </div>
	      		<div class="form-group has-error" style="display:inline;"><label for="fpriceMoney" >商品售价：</label>
					<input type="text" id="fpriceMoney" name="fpriceMoney" class="form-control validate[required,minSize[1],maxSize[30]]" size="35">
			    </div>
	      		<div class="form-group has-error"><label for="ftotal">总库存：</label>
					<input type="text" id="ftotal" name="ftotal" class="form-control validate[required,minSize[1],maxSize[30]]" size="35">
			    </div>
	      		<div class="form-group has-error"><label for="fstock">剩余库存：</label>
					<input type="text" id="fstock" name="fstock" class="form-control validate[required,minSize[1],maxSize[30]]" size="35">
			    </div>
				<div class="form-group"><label for="flimitation">限购数量：</label>
			 	<div class="input-group">
					<input type="text" id="flimitation" name="flimitation" class="validate[custom[integer],min[-1],max[18]] form-control" value="${flimitation}" size="4">
					<div class="input-group-addon">-1表示该商品不限购</div>
				</div>
			    </div>
			    <div class="form-group has-error"><label for="flag">默认显示：</label>
					<select id=flag name="flag" class="validate[required] form-control" style="width: 120px;">
							<option value="1">否</option>
							<option value="0">是</option>
					</select>
			    </div>
	      		<div class="form-group has-error"><label for="fhavingImage">上传图片：</label>
					<select id="fhavingImage" name="fhavingImage" class="validate[required] form-control" style="width: 120px;">
							<option value="0">是</option>
							<option value="1">否</option>
					</select>
			    </div>
			    
			    <hr>
			    <div class="row">
					<div class="col-md-2"><label for="fimage">sku主图：</label>
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
					
					<div class="form-group"><label for="watermark">上传图片是否加水印：</label>
		                   <select id="watermark" name="watermark" class="form-control">
						       <option value="false">不加水印</option>
						       <option value="true">加水印</option>
				           </select>
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
<!--编辑sku结束-->
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
      </div>
  </div>
</div>
<!--图片上传进度条结束-->

</body>
</html>