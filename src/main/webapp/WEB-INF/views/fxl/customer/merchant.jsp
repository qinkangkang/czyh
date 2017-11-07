<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/webuploader/webuploader.css" rel="stylesheet" type="text/css">
<script src="http://api.map.baidu.com/api?v=2.0&ak=8vxHpx4PyxOzXyGIjUb5GAoT" type="text/javascript"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js" charset="UTF-8"></script>
<%-- <script type="text/javascript" charset="utf-8" src="${ctx}/styles/ueditor/ueditor.config.js"></script>
<script type="text/javascript" charset="utf-8" src="${ctx}/styles/ueditor/ueditor.all.min.js"></script>
<script type="text/javascript" charset="utf-8" src="${ctx}/styles/ueditor/lang/zh-cn/zh-cn.js"></script> --%>
<script src="${ctx}/styles/webuploader/webuploader.html5only.min.js" type="text/javascript"></script>
<script type="text/javascript">
$(document).ready(function() {
	
	$.validationEngineLanguage.allRules.checkUsername={
		"url" : '${ctx}/fxl/customer/merchant/checkUsername',
		"alertTextOk" : '您可以使用该商家账户名！',
	    "alertText" : '您输入的商家账户名已被占用，请更换其它商家账户名！',
	    "alertTextLoad" : '正在验证该商家账户名是否被占用……'
	};
	
	$.validationEngineLanguage.allRules.checkName={
		"url" : '${ctx}/fxl/customer/merchant/checkName',
		"alertTextOk" : '您可以使用该商家名称！',
	    "alertText" : '您输入的商家名称已被占用，请更换其它商家名称！',
	    "alertTextLoad" : '正在验证该商家名称是否被占用……'
	};

	$.validationEngineLanguage.allRules.checkEditUsername={
		"url" : '${ctx}/fxl/customer/merchant/checkEditUsername',
		"extraDataDynamic": ['#id'],
		"alertTextOk" : '您可以使用该商家账户名！',
	    "alertText" : '您输入的商家账户名已被占用，请更换其它商家账户名！',
	    "alertTextLoad" : '正在验证该商家账户名是否被占用……'
	};
		
	$.validationEngineLanguage.allRules.checkEditName={
		"url" : '${ctx}/fxl/customer/merchant/checkEditName',
		"extraDataDynamic": ['#id'],
		"alertTextOk" : '您可以使用该商家名称！',
	    "alertText" : '您输入的商家名称已被占用，请更换其它商家名称！',
	    "alertTextLoad" : '正在验证该商家名称是否被占用……'
	};
	
	$('#createDateDiv').datepicker({
	    format : "yyyy-mm-dd",
	    endDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	var searchForm = $('form#searchForm');
	
	searchForm.submit(function(e) {
		e.preventDefault();
		merchantTable.ajax.reload();
	});
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	var merchantTable = $("table#merchantTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/customer/merchant/getMerchantList",
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
			"title" : '<center>商家登录名</center>',
			"data" : "fusername",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>商家编号</center>',
			"data" : "fnumber",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>商家名称</center>',
			"data" : "fname",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>销售类型</center>',
			"data" : "fsponsorModel",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false,
			"render":function(data,type,full){
				if(data==0){
						return  "自营";	
				}else if(data==1) {
						return "自提";
				}else{
					return "";
				}
			}
		},{
			"title" : '<center>联系电话</center>',
			"data" : "fphone",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>BD</center>',
			"data" : "fbdId",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>小编</center>',
			"data" : "fcreaterId",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>状态</center>',
			"data" : "fstatus",
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
			"targets" : [9],
			"render" : function(data, type, full) {
				return '<button id="edit" mId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs"><fmt:message key="fxl.button.edit" /></button><button id="del" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs"><fmt:message key="fxl.button.delete" /></button>';
			}
		}]
	});
	
	$("#merchantTable").delegate("button[id=del]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该商家吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/customer/merchant/delMerchant/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						merchantTable.ajax.reload(null,false);
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
	
	//var ue = UE.getEditor('container');
	
	$("#merchantTable").delegate("button[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		$("#fusername").removeClass("validate[required,minSize[2],maxSize[250],ajax[checkUsername]]");
		$("#fusername").addClass("validate[required,minSize[2],maxSize[250],ajax[checkEditUsername]]");
		$("#fname").removeClass("validate[required,minSize[2],maxSize[250],ajax[checkName]]");
		$("#fname").addClass("validate[required,minSize[2],maxSize[250],ajax[checkEditName]]");
		$("#fpassword").removeClass("validate[required,minSize[6],maxSize[32]]");
		$("#fpassword").addClass("validate[minSize[6],maxSize[32]]");
		$("#fpassword2").removeClass("validate[required,equals[fpassword]]");
		$("#fpassword2").addClass("validate[equals[fpassword]]");
		/* $('#passwordDiv').hide(); */
		$('#resetBtn').hide();
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/customer/merchant/getMerchant/" + mId, function(data) {
			if(data.success){
				$("#createMerchantForm #id").val(mId);
				$("#createMerchantForm #fusername").val(data.fusername);
				$("#createMerchantForm #fmobile").val(data.fmobile);
				$("#createMerchantForm #fphone").val(data.fphone);
				$("#createMerchantForm #fname").val(data.fname);
				$("#createMerchantForm #ffullName").val(data.ffullName);
				$("#createMerchantForm #fbrief").val(data.fbrief);
				$("#createMerchantForm #fimage").val(data.fimage);
				$("#createMerchantForm #fgps").val(data.fgps);
				$("#createMerchantForm #faddress").val(data.faddress);
				
				$("#createMerchantForm #frange").val(data.frange);
				$("#createMerchantForm #fpinkage").val(data.fpinkage);
				$("#createMerchantForm #fperPrice").val(data.fperPrice);
				$("#createMerchantForm #fsponsorModel").val(data.fsponsorModel);
				
				$("#createMerchantForm #fcontractEffective").val(data.fcontractEffective);
				$("#createMerchantForm #frate").val(data.frate);
				$("#createMerchantForm #fbank").val(data.fbank);
				$("#createMerchantForm #fbankAccount").val(data.fbankAccount);
				$("#createMerchantForm #fbankAccountName").val(data.fbankAccountName);
				$("#createMerchantForm #fbankAccountPersonId").val(data.fbankAccountPersonId);
				$("#createMerchantForm #fsponsorTag option[value='" + data.fsponsorTag + "']").prop("selected",true);
				$("#createMerchantForm #region option[value='" + data.fregion + "']").prop("selected",true);
				$("#createMerchantForm #ftype option[value='" + data.ftype + "']").prop("selected",true);
				$("#createMerchantForm #flevel option[value='" + data.flevel + "']").prop("selected",true);
				$("#createMerchantForm #fstatus option[value='" + data.fstatus + "']").prop("selected",true);
				$("#createMerchantForm #fbdId option[value='" + data.fbdId + "']").prop("selected",true);
				$("#createMerchantForm #fcreaterId option[value='" + data.fcreaterId + "']").prop("selected",true);
				if($.trim(data.fimagePath) == ""){
					imageThumbnail.attr("src",noPicUrl);
				}else{
					imageThumbnail.attr("src",data.fimagePath);
				}
				/* if($.trim(data.fdetail) == ""){
					ue.setContent("", false);
				}else{
					ue.setContent(data.fdetail, false);
				} */
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
				createMerchantModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	var uploadProgressModal =  $('#uploadProgressModal');
	
	uploadProgressModal.on('hidden.bs.modal', function(e){
		uploadFileProgress.css( 'width', '0%' );
		createMerchantModal.css("overflow-y","auto");
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
			    data["pathVar"] = "merchantImagePath";
			    data["watermark"] = false;
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
	
	var createMerchantModal =  $('#createMerchantModal');
	
	createMerchantModal.on('hide.bs.modal', function(e){
		createMerchantForm.trigger("reset");
	});
	
	createMerchantModal.on('shown.bs.modal', function(e){
		initUploader();
	});
	
	$('#createMerchantBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		$("#createMerchantForm #fusername").removeClass("validate[required,minSize[2],maxSize[250],ajax[checkEditUsername]]");
		$("#createMerchantForm #fusername").addClass("validate[required,minSize[2],maxSize[250],ajax[checkUsername]]");
		$("#createMerchantForm #fname").removeClass("validate[required,minSize[2],maxSize[250],ajax[checkEditName]]");
		$("#createMerchantForm #fname").addClass("validate[required,minSize[2],maxSize[250],ajax[checkName]]");
		$("#createMerchantForm #fpassword").removeClass("validate[minSize[6],maxSize[32]]");
		$("#createMerchantForm #fpassword").addClass("validate[required,minSize[6],maxSize[32]]");
		$("#createMerchantForm #fpassword2").removeClass("validate[equals[fpassword]]");
		$("#createMerchantForm #fpassword2").addClass("validate[required,equals[fpassword]]");
		$('#resetBtn').show();
		imageThumbnail.attr("src",noPicUrl);
		$("#createMerchantForm #fcreaterId option[value='" + $('#shiroUserId').val() + "']").prop("selected",true);
		createMerchantModal.modal('show');
	});
	
	var createMerchantForm = $('#createMerchantForm');
	
	createMerchantForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	var fimage = $("#fimage");
	
	createMerchantForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		/* if(fimage.val() == ""){
			dialog({
				fixed: true,
		        title: '操作提示',
		        content: '请选择要上传的图片介绍',
		        cancelValue: '关闭',
		        cancel: function (){}
		    }).showModal();
			return false;
		} */
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			if($('#actionFlag').val() == "add"){
				$.post(form.attr("action"), form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						merchantTable.ajax.reload(null,false);
						createMerchantModal.modal('hide')
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/customer/merchant/editMerchant", form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						merchantTable.ajax.reload(null,false);
						createMerchantModal.modal('hide')
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
	
	createMerchantForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createMerchantForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		//ue.setContent("", false);
		createMerchantForm.validationEngine('hideAll');
	});
	
	var baiduMapModal =  $('#baiduMapModal');
	
	baiduMapModal.on('hidden.bs.modal', function(e){
		baiduCoordinate.val("");
		baiduAddress.val("");
		createMerchantModal.css("overflow-y","auto");
	});
	
	var map = new BMap.Map("fxlBaiduMap");        // 创建地图实例  
	var point = new BMap.Point(116.404, 39.915);  // 创建点坐标  
	map.centerAndZoom(point, 15);
	map.enableScrollWheelZoom();
	map.addControl(new BMap.NavigationControl());    
	map.addControl(new BMap.ScaleControl());    
	//map.addControl(new BMap.OverviewMapControl());    
	//map.addControl(new BMap.MapTypeControl());    
	map.setCurrentCity("北京"); 
	// 仅当设置城市信息时，MapTypeControl的切换功能才能可用
	
	map.addEventListener("click", function(e){
		baiduCoordinate.val(e.point.lng + "," + e.point.lat);
	});
	
	var baiduCoordinate = $('#baiduCoordinate');
	var baiduAddress = $('#baiduAddress');
	
	$('#selectBaiduOkBtn').on('click',function(e) {
		if($.trim(baiduCoordinate.val()) == ""){
			toastr.warning('请点击百度地图上的活动地点来获取坐标值！');
		}else{
			$('#fgps').val(baiduCoordinate.val());
			$('#faddress').val(baiduAddress.val());
			baiduMapModal.modal('hide')
		}
	});
	
	var searchListDiv = $("#searchListDiv");
	
	var options = {
		onSearchComplete: function(results){
			if (local.getStatus() == BMAP_STATUS_SUCCESS){
				if(results.getCurrentNumPois() > 0){
					map.centerAndZoom(new BMap.Point(results.getPoi(0).point.lng, results.getPoi(0).point.lat), 15);
				}
				searchListDiv.empty();
				var liList;
				// 判断状态是否正确
				for (var i = 0; i < results.getCurrentNumPois(); i ++){
					var point = new BMap.Point(results.getPoi(i).point.lng, results.getPoi(i).point.lat);
					var marker = new BMap.Marker(point);
					map.addOverlay(marker);
					searchListDiv.append($("<a id='resultItem' href='#' class='list-group-item' data-lng='" + results.getPoi(i).point.lng + "' data-lat='" + results.getPoi(i).point.lat + "' data-title='" + results.getPoi(i).title + "' data-address='" + results.getPoi(i).address + "'>" + results.getPoi(i).title + "<br/><strong>地址：</strong>" + results.getPoi(i).address + "</a>"));
				}
			}
		}
	};
	
	searchListDiv.delegate("a[id=resultItem]", "click", function(){
		var thisa = $(this);
		baiduCoordinate.val(thisa.data("lng") + "," + thisa.data("lat"));
		baiduAddress.val(thisa.data("address"));
		
		var sContent = "<div><h4 style='margin:0 0 5px 0;padding:0.2em 0'>" + thisa.data("title") 
		+ "</h4><p style='margin:0;line-height:1.5;text-indent:2em'>" + thisa.data("address") + "</p></div>";
		var infoWindow = new BMap.InfoWindow(sContent);  // 创建信息窗口对象
		var point = new BMap.Point(thisa.data("lng"), thisa.data("lat"));
		map.centerAndZoom(point, 18);
		map.openInfoWindow(infoWindow,point);
	});
	
	var local = new BMap.LocalSearch(map, options);  
	
	$("#searchKey").bind('keypress',function(event){
        if(event.keyCode == "13"){
        	var searchKey = $.trim($("#searchKey").val());
    		if(searchKey != ""){
    			local.search(searchKey);
    		}
        }
    });
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<input id="shiroUserId" name="shiroUserId" type="hidden" value="${shiroUserId}">
<div class="row">
  <div class="col-md-10"><h3>商家用户</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>商家用户列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createMerchantBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 创建商家</button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center">
	<form id="searchForm" action="${ctx}/fxl/customer/merchant/getMerchantList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_fname">商家名称：</label>
			<input type="text" id="s_fname" name="s_fname" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_fnumber">商家编号：</label>
			<input type="text" id="s_fnumber" name="s_fnumber" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_fphone">商家电话：</label>
			<input type="text" id="s_fphone" name="s_fphone" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_fbdId">商家BD：</label>
			<select id="s_fbdId" name="s_fbdId" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="bd" items="${bdList}">
					<option value="${bd.key}">${bd.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="form-group"><label for="s_fcreaterId">编辑人员：</label>
			<select id="s_fcreaterId" name="s_fcreaterId" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="editor" items="${editorList}">
					<option value="${editor.key}">${editor.value}</option>
				</c:forEach>
			</select>
		</div>
		<%-- <div class="form-group"><label for="s_ftype">商家类型：</label>
			<select id="s_ftype" name="s_ftype" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="typeItem" items="${sponsorTypeMap}"> 
				<option value="${typeItem.key}">${typeItem.value}</option>
				</c:forEach>
			</select>
		</div>
		<div class="form-group"><label for="s_flevel">商家级别：</label>
			<select id="s_flevel" name="s_flevel" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="levelItem" items="${sponsorLevelMap}"> 
				<option value="${levelItem.key}">${levelItem.value}</option>
				</c:forEach>
			</select>
		</div> --%>
		<div class="form-group"><label>创建时间：</label>
		<div class="input-daterange input-group date" id="createDateDiv" style="width:330px;">
		    <input type="text" class="form-control input-sm" name="fcreateTimeStart" style="cursor: pointer;">
		    <span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
		    <input type="text" class="form-control input-sm" name="fcreateTimeEnd" style="cursor: pointer;">
		</div></div>
		<p class="text-center"><button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="merchantTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
<!--编辑商家开始-->
<div class="modal fade" id="createMerchantModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">编辑商家信息</h4>
      </div>
      <form id="createMerchantForm" action="${ctx}/fxl/customer/merchant/addMerchant" method="post" class="form-inline" role="form">
      <div class="modal-body">
      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
      		<input id="id" name="id" type="hidden">
      		<div class="form-group has-error"><label for="fusername">商家登录名：</label>
				<input type="text" id="fusername" name="fusername" class="form-control validate[required,minSize[2],maxSize[250],ajax[checkUsername]]">
		    </div>
		    <div class="form-group has-error"><label for="fmobile">手机号码：</label>
				<input type="text" id="fmobile" name="fmobile" class="form-control validate[required,minSize[2],maxSize[250],custom[phone]]">
		    </div>
		    <div class="form-group">
			    <label for="fpassword"><fmt:message key="fxl.admin.user.password" />：</label>
		      	 <input type="password" id="fpassword" name="fpassword" class="form-control validate[required,minSize[6],maxSize[32]]">
		    </div>
			<div class="form-group">
		    	<label for="fpassword2"><fmt:message key="fxl.admin.user.password2" />：</label>
		    	<input type="password" id="fpassword2" name="fpassword2" class="form-control validate[required,equals[fpassword]]">
		    </div>
		    <div class="form-group has-error"><label for="fname">商家名称：</label>
				<input type="text" id="fname" name="fname" class="form-control validate[required,minSize[2],maxSize[250],ajax[checkName]]">
		    </div>
		    <div class="form-group has-error"><label for="ffullName">商家全称：</label>
				<input type="text" id="ffullName" name="ffullName" class="form-control validate[required,minSize[2],maxSize[250]]" size="60">
		    </div>
      		<div class="form-group"><label for="fphone">商家电话：</label>
				<input type="text" id="fphone" name="fphone" class="form-control validate[minSize[2],maxSize[250]]" size="40">
		    </div>
		    <div class="form-group has-error"><label for="region">商家区域：</label>
		        <select id="region" name="region" class="validate[required] form-control">
					<option value=""><fmt:message key="fxl.common.select" /></option>
					<c:forEach var="regionListItem" items="${regionList}">
					<option value="${regionListItem.value}" >${regionListItem.value}</option>
					</c:forEach>
				</select>
		    </div>
		    
		    <div class="form-group has-error"><label for="ftype">商户分类：</label>
		        <select id="ftype" name="ftype" class="validate[required] form-control">
					<option value=""><fmt:message key="fxl.common.select" /></option>
					<c:forEach var="sponsorTypeItem" items="${sponsorTypeMap}">
					<option value="${sponsorTypeItem.key}" >${sponsorTypeItem.value}</option>
					</c:forEach>
				</select>
		    </div>
		    
		     <div class="form-group has-error"><label for="fsponsorTag">商户标签：</label>
		        <select id="fsponsorTag" name="fsponsorTag" class="validate[required] form-control">
					<option value=""><fmt:message key="fxl.common.select" /></option>
					<c:forEach var="sponsorTagItem" items="${sponsorTagMap}">
					<option value="${sponsorTagItem.key}" >${sponsorTagItem.value}</option>
					</c:forEach>
				</select>
		    </div>
		    
		    
		    <div class="form-group has-error"><label for="fstatus">商家状态：</label>
		        <select id="fstatus" name="fstatus" class="validate[required] form-control">
					<option value=""><fmt:message key="fxl.common.select" /></option>
					<c:forEach var="sponsorStatusItem" items="${sponsorStatusMap}">
					<option value="${sponsorStatusItem.key}" >${sponsorStatusItem.value}</option>
					</c:forEach>
				</select>
		    </div>
		    
		    <div class="form-group"><label for="fperPrice">人均价格：</label>
		        <input type="text" id="fperPrice" name="fperPrice" class="form-control validate[minSize[1],maxSize[250]]" size="20">
		    </div>
		    
		    <div class="form-group"><label for="frange">店铺运费：</label>
		        <input type="text" id="frange" name="frange" class="form-control validate[minSize[1],maxSize[250]]" size="20">
		    </div>
		    
		    <div class="form-group"><label for="fpinkage">免邮金额：</label>
		        <input type="text" id="fpinkage" name="fpinkage" class="form-control validate[minSize[1],maxSize[250]]" size="20">
		    </div>
		    
		    <div class="form-group"><label for="faddress">商家地址：</label>
				<input type="text" id="faddress" name="faddress" class="form-control validate[minSize[2],maxSize[250]]" size="40">
			</div>
		    <div class="form-group"><label for="location">百度地图坐标：</label>
				<div class="input-group" style="cursor: pointer;" data-toggle="modal" data-target="#baiduMapModal">
					<input type="text" id="fgps" name="fgps" class="form-control" readonly="readonly">
					<span class="input-group-addon"><i class="glyphicon glyphicon-map-marker"></i></span>
				</div>
			</div>

			<div class="form-group has-error"><label for="fsponsorModel">商家销售类型：</label>
		        <select id="fsponsorModel" name="fsponsorModel" class="validate[required] form-control">
					<option value=""><fmt:message key="fxl.common.select" /></option>
					<option value=0>自营</option>
					<option value=1>自提</option>
				</select>
		    </div>
			<%-- 
			<div class="form-group has-error"><label for="flevel">商家级别：</label>
		        <select id="flevel" name="flevel" class="validate[required] form-control">
					<option value=""><fmt:message key="fxl.common.select" /></option>
					<c:forEach var="sponsorLevelItem" items="${sponsorLevelMap}">
					<option value="${sponsorLevelItem.key}" >${sponsorLevelItem.value}</option>
					</c:forEach>
				</select>
		    </div> --%>

		    <div class="form-group"><label for="fwebSite">商家网站：</label>
		        <input type="text" id="fwebSite" name="fwebSite" class="form-control validate[minSize[3],maxSize[250]]" size="40">
		    </div>
		    <div class="form-group"><label for="fcontractEffective">合同起止时间：</label>
		        <input type="text" id="fcontractEffective" name="fcontractEffective" class="form-control validate[minSize[1],maxSize[250]]" size="40">
		    </div>
		    <div class="form-group"><label for="frate">结算费率：</label>
		    	<div class="input-group">
		    		<input type="text" id="frate" name="frate" class="form-control validate[custom[number],min[0],max[100]]" size="6"><div class="input-group-addon">％</div>
		    	</div>
		    </div>
		    <div class="form-group"><label for="fbank">银行卡开户行：</label>
		        <input type="text" id="fbank" name="fbank" class="form-control validate[minSize[3],maxSize[250]]" size="30">
		    </div>
		    <div class="form-group"><label for="fbankAccount">银行卡号：</label>
		        <input type="text" id="fbankAccount" name="fbankAccount" class="form-control validate[minSize[3],maxSize[250]]" size="30">
		    </div>
		    <div class="form-group"><label for="fbankAccountName">卡主姓名：</label>
		        <input type="text" id="fbankAccountName" name="fbankAccountName" class="form-control validate[minSize[1],maxSize[250]]" size="20">
		    </div>
		    <div class="form-group"><label for="fbankAccountPersonId">卡主身份证号码：</label>
		        <input type="text" id="fbankAccountPersonId" name="fbankAccountPersonId" class="form-control validate[minSize[3],maxSize[250]]" size="30">
		    </div>
		    <div class="form-group has-error" style="min-height: 110px;"><label for="fbrief">商家简介：</label>
		        <textarea id="fbrief" name="fbrief" cols="80" rows="5" class="validate[required,maxSize[2000]] form-control"></textarea>
		    </div>
		    <div class="form-group has-error"><label for="fbdId">商家BD：</label>
		        <select id="fbdId" name="fbdId" class="validate[required] form-control">
					<option value=""><fmt:message key="fxl.common.select" /></option>
					<c:forEach var="bd" items="${bdList}">
						<option value="${bd.key}">${bd.value}</option>
					</c:forEach>
				</select>
		    </div>
		    <div class="form-group has-error"><label for="fcreaterId">编辑人员：</label>
		        <select id="fcreaterId" name="fcreaterId" class="validate[required] form-control">
					<option value=""><fmt:message key="fxl.common.select" /></option>
					<c:forEach var="editor" items="${editorList}">
						<option value="${editor.key}">${editor.value}</option>
					</c:forEach>
				</select>
		    </div>
			<div class="row">
				<div class="col-md-2"><label for="fimage">LOGO图片：</label>
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
			<!-- <div class="form-group"><label>商家详情：</label>
		        加载编辑器的容器
				<script id="container" name="fdetail" type="text/plain" style="width:850px;height:450px;"></script>
		    </div> -->
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
<!--编辑商家结束-->
<!--百度地图开始-->
<div class="modal fade" id="baiduMapModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg" style="width: 1280px;">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">百度地图选点</h4>
      </div>
      <div class="modal-body">
      <div class="row">
      	<div class="col-md-3 text-right">地址搜索：</div>
		<div class="col-md-6"><input type="text" id="searchKey" name="searchKey" class="form-control" ></div>
      	<div class="col-md-3"><small>回车即可搜索</small><!-- <button id="mapSearchBtn" class="btn btn-primary" type="button"><span class="glyphicon glyphicon-search"></span> 搜索</button> --></div>
      </div>
      <p/>
      <div class="row">
      	<div class="col-md-8">
	      	<div class="well well-sm">
	  		<div id="fxlBaiduMap" style="height: 580px;width: 800px;"></div></div>
      	</div>
      	<div class="col-md-4">
			<div id="searchListDiv" class="list-group" style="max-height: 580px; overflow-y: auto;"></div>
      	</div>
      </div>
      <p/>
      <div class="row"><label for="baiduCoordinate" class="col-md-2 text-right">百度坐标值：</label>
		<div class="col-md-3"><input type="text" id="baiduCoordinate" name="baiduCoordinate" class="form-control" readonly="readonly"></div>
		<label for="baiduAddress" class="col-md-2 text-right">百度地址：</label>
		<div class="col-md-3"><input type="text" id="baiduAddress" name="baiduAddress" class="form-control" readonly="readonly"></div>
		<div class="col-md-2"></div>
	  </div>
      </div>
      <div class="modal-footer">
      	<button type="button" class="btn btn-primary" id="selectBaiduOkBtn"><span class="glyphicon glyphicon-ok"></span> <fmt:message key="fxl.button.ok" /></button>
		<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="fxl.button.close" /></button>
      </div>
    </div>
  </div>
</div>
<!--百度地图结束-->
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