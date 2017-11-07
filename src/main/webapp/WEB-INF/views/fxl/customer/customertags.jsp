<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>

<title>用户标签管理</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<!-- 通用多图上传开始 -->
<link href="${ctx}/styles/bootstrap-file/css/fileinput.css" rel="stylesheet" type="text/css"  media="all">
<!-- <link href="${ctx}/styles/bootstrap-file/css/default.css" rel="stylesheet" type="text/css"> -->

<script src="${ctx}/styles/bootstrap-file/js/fileinput.js"></script>
<script src="${ctx}/styles/bootstrap-file/js/fileinput_locale_zh.js"></script>
<!-- 通用多图上传结束 -->
<script type="text/javascript">
$(document).ready(function() {
	
	var searchForm = $('form#searchForm');
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#select").click(function(e) {
		CustomerTagsTable.ajax.reload();
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
    });
	
	var CustomerTagsTable = $("table#CustomerTagsTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/customer/userTags/getCustomerTagsList",
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
			"title" : '<center>用户唯一标识</center>',
			"data" : "fcustomerId",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>用户标签</center>',
			"data" : "ftag",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>操作人</center>',
			"data" : "foperator",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>创建时间</center>',
			"data" : "fcreateTime",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "30px",
			"className": "text-center",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [5],
			"render" : function(data, type, full) {
				return '<div class="btn-group btn-group-xs" role="group" aria-label="零到壹，查找优惠"><button id="edit" mId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs"><fmt:message key="fxl.button.edit" /></button></div>';
			}
		}]
	});
	
	$("#CustomerTagsTable").delegate("button[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		$('#resetBtn').hide();
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/customer/userTags/getCustomerTags/" + mId, function(data) {
			if(data.success){
				$("#id").val(mId);
				$("#fcustomerId").val(data.fcustomerId);
				$("#ftag option[value='" + data.ftag + "']").prop("selected",true);
				$("#foperator").val(data.foperator);
				$("#fcreateTime").val(data.fcreateTime);
				createCustomerModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	var createCustomerModal =  $('#createCustomerModal');
	
	var createAddTagsModal =  $('#createAddTagsModal');//导入用户标签用
	
	createCustomerModal.on('hide.bs.modal', function(e){
		createCustomerForm.trigger("reset");
	});
		
	createAddTagsModal.on('hide.bs.modal', function(e){
		createCustomerForm.trigger("reset");
	});//导入标签用
	
	$('#createCustomerBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		$('#resetBtn').show();
		createAddTagsModal.modal('show')
	});
	
	var createCustomerForm = $('#createCustomerForm');
	
	createCustomerForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	createCustomerForm.on("submit", function(event){
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
						CustomerTagsTable.ajax.reload(null,false);
						createCustomerModal.modal('hide')
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/customer/userTags/editCustomerTags", form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						CustomerTagsTable.ajax.reload(null,false);
						createCustomerModal.modal('hide')
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
	
	createCustomerForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createCustomerForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		createCustomerForm.validationEngine('hideAll');
	});
});
</script>


</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<input id="shiroUserId" name="shiroUserId" type="hidden" value="${shiroUserId}">
<div class="row">
  <div class="col-md-10"><h3>用户标签</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>用户标签列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createCustomerBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 导入用户标签</button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/customer/userTags/getCustomerTagsList" method="post" class="form-inline" role="form">
	
		<div class="form-group has-error"><label for="flevel">用户标签：</label>
		        <select id="s_ftag" name="s_ftag" class="validate[required] form-control">
					<option value=""><fmt:message key="fxl.common.select" /></option>
					<c:forEach var="customerTagsItem" items="${customerTagsMap}">
					<option value="${customerTagsItem.key}" >${customerTagsItem.value}</option>
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
<table id="CustomerTagsTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>

<!--编辑商家开始-->
<div class="modal fade" id="createCustomerModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">编辑用户标签信息</h4>
      </div>
      <form id="createCustomerForm" action="${ctx}/fxl/customer/merchant/xx" method="post" class="form-inline" role="form">
      <div class="modal-body">
      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
      		<input id="id" name="id" type="hidden">
      		<div class="form-group has-error"><label for="fcustomerId">用户id：</label>
				<input type="text" id="fcustomerId" name="fcustomerId" class="form-control validate[required,minSize[2],maxSize[250]]" readonly>
		    </div>
		    
		   <div class="form-group has-error"><label for="ftag">用户标签：</label>
		        <select id="ftag" name="ftag" class="validate[required] form-control">
					<option value=""><fmt:message key="fxl.common.select" /></option>
					<c:forEach var="customerTagsItem" items="${customerTagsMap}">
					<option value="${customerTagsItem.key}" >${customerTagsItem.value}</option>
					</c:forEach>
				</select>
		  </div>
						<br/>
		    <div class="form-group has-error"><label for="foperator">操作人：</label>
				<input type="text" id="foperator" name="foperator" class="form-control validate[required,minSize[2],maxSize[250]]" readonly>
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
<!--编辑商家结束-->


<!--开始导入用户标签-->
<div class="modal fade" id="createAddTagsModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">导入用户标签</h4>
      </div>
    <!--   <form id="createCustomerForm" action="${ctx}/fxl/customer/xxx/xxx" method="post" class="form-inline" role="form"> -->
      <div class="modal-body">
      	    
		   <!--<div class="form-group has-error"><label for="ftag">用户标签：</label>
		        <select id="ftag" name="ftag" class="validate[required] form-control">
					<option value=""><fmt:message key="fxl.common.select" /></option>
					<c:forEach var="customerTagsItem" items="${customerTagsMap}">
					<option value="${customerTagsItem.key}" >${customerTagsItem.value}</option>
					</c:forEach>
				</select>
		   </div> -->
		    
			<div class="htmleaf-container">
				<div class="container kv-main">
					<form enctype="multipart/form-data">					
						<div class="form-group">
							<input id="file-1" class="file" type="file" name="file" multiple
								data-preview-file-type="any" data-upload-url="${ctx}/fxl/customer/merchant/addUserTagExcel"
								data-preview-file-icon="">
						</div>
					</form>
				</div>
			</div>
		  <div style="clear:both;"></div>
      </div>
     <!--   <div class="modal-footer">
      	<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span> <fmt:message key="fxl.button.save" /></button>
		<button class="btn btn-warning" type="reset" id="resetBtn"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.reset" /></button>
        <button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="fxl.button.close" /></button>
      </div>
      -->
    <!--    </form>-->
    </div>
  </div>
</div>
<!--结束导入用户标签-->
<script>
	$("#file-0").fileinput({
		'allowedFileExtensions' : [ 'jpg', 'png', 'gif' ],
	});
	$("#file-1").fileinput({
		alert(111);
		uploadUrl : '${ctx}/fxl/customer/merchant/addUserTagExcel', // you must set a valid URL here else you will get an error
		allowedFileExtensions : [ 'jpg', 'png', 'gif' ],
		overwriteInitial : false,
		maxFileSize : 1000,
		maxFilesNum : 10,
		//allowedFileTypes: ['image', 'video', 'flash'],
		slugCallback : function(filename) {
			return filename.replace('(', '_').replace(']', '_');
		}
	});
	/*
	$(".file").on('fileselect', function(event, n, l) {
	    alert('File Selected. Name: ' + l + ', Num: ' + n);
	});
	 */
	$("#file-3").fileinput({
		showUpload : false,
		showCaption : false,
		browseClass : "btn btn-primary btn-lg",
		fileType : "any",
		previewFileIcon : "<i class='glyphicon glyphicon-king'></i>"
	});
	$("#file-4").fileinput({
		uploadExtraData : {
			kvId : '10'
		}
	});
	$(".btn-warning").on('click', function() {
		if ($('#file-4').prop('disabled')) {
			$('#file-4').fileinput('enable');
		} else {
			$('#file-4').fileinput('disable');
		}
	});
	$(".btn-info").on('click', function() {
		$('#file-4').fileinput('refresh', {
			previewClass : 'bg-info'
		});
	});
	/*
	$('#file-4').on('fileselectnone', function() {
	    alert('Huh! You selected no files.');
	});
	$('#file-4').on('filebrowse', function() {
	    alert('File browse clicked for #file-4');
	});
	 */
	$(document).ready(function() {
		$("#test-upload").fileinput({
			'showPreview' : false,
			'allowedFileExtensions' : [ 'jpg', 'png', 'gif' ],
			'elErrorContainer' : '#errorBlock'
		});
		/*
		$("#test-upload").on('fileloaded', function(event, file, previewId, index) {
		    alert('i = ' + index + ', id = ' + previewId + ', file = ' + file.name);
		});
		 */
	});
</script>
</body>
</html>