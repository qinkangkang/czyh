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
 <script type="text/javascript" charset="utf-8" src="${ctx}/styles/ueditor/ueditor.TypeValue.js"></script>
<script type="text/javascript" charset="utf-8" src="${ctx}/styles/ueditor/ueditor.all.min.js"></script>
<script type="text/javascript" charset="utf-8" src="${ctx}/styles/ueditor/lang/zh-cn/zh-cn.js"></script>
<script src="${ctx}/styles/webuploader/webuploader.html5only.min.js" type="text/javascript"></script>
<script type="text/javascript">
$(document).ready(function() {
	
	
	$.validationEngineLanguage.allRules.checkTypeClassName={
			"url" : '${ctx}/fxl/event/checkTypeClassName',
			"extraDataDynamic": ["#fid"],
			"alertTextOk" : '您可以使用该属性名！',
		    "alertText" : '您输入的属性名已经存在，请更换其他属性名！',
		    "alertTextLoad" : '正在验证该该属性名是否被占用……'
		};
		
	$.validationEngineLanguage.allRules.checkTypeClassSort={
			"url" : '${ctx}/fxl/event/checkTypeClassSort',
			"extraDataDynamic": ["#fid"],
			"alertTextOk" : '您可以使用该排序！',
		    "alertText" : '您输入的排序值已被占用，请更换其它排序！',
		    "alertTextLoad" : '正在验证该排序是否被占用……'
		};
		
	$.validationEngineLanguage.allRules.checkTypeValueName={
			"url" : '${ctx}/fxl/event/checkTypeValueName',
			"extraDataDynamic": ['#fextendClassId',"#fid"],
			"alertTextOk" : '您可以使用该属性值！',
		    "alertText" : '您输入的属性值已经存在，请更换其他属性值！',
		    "alertTextLoad" : '正在验证该该属性值是否被占用……'
		};
		
	$.validationEngineLanguage.allRules.checkTypeValueSort={
			"url" : '${ctx}/fxl/event/checkTypeValueSort',
			"extraDataDynamic": ['#fextendClassId',"#fid"],
			"alertTextOk" : '您可以使用该排序！',
		    "alertText" : '您输入的排序值已被占用，请更换其它排序！',
		    "alertTextLoad" : '正在验证该排序是否被占用……'
		};
		
	
	var searchForm = $('form#searchForm');
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#select").click(function(e) {
		TypeClassTable.ajax.reload();
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	var TypeClassTable = $("table#TypeClassTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/event/getTypeClassList",
 		    "type": "POST",
 		    "data": function (data) {
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
		{	
			"title" : "ID",
			"data" : "DT_RowId",
			"visible": false,
			"orderable" : false
		}, {
			"title" : '<center>商品属性名称</center>',
			"data" : "fclassName",
			"className": "text-center",
			"width" : "80px",
			"orderable" : false
		},{
			"title" : '<center>排序</center>',
			"data" : "fsort",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		},{
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "50px",
			"className": "text-center",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [3],
			"render" : function(data, type, full) {
				var retString = '<button id="edit" mId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs"><fmt:message key="fxl.button.edit" /></button><button id="editTypeValue" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs">编辑属性值</button>';
				return retString;
			}
		}]
	});
	
	
	$("#TypeClassTable").delegate("button[id=del]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该属性吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/event/delTypeClass/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						TypeClassTable.ajax.reload(null,false);
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
	
	


	
	$("#TypeClassTable").delegate("button[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		$('#resetBtn').hide();//隐藏重置按钮
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/event/getTypeClass/" + mId, function(data) {
			if(data.success){
				$("#id").val(mId);
				$("#fclassName").val(data.fclassName);
				$("#fsort").val(data.fsort);
				createTypeClassModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	
	var createTypeClassModal =  $('#createTypeClassModal');
	
	
	createTypeClassModal.on('hide.bs.modal', function(e){
		createTypeClassForm.trigger("reset");
	});
	
	createTypeClassModal.on('shown.bs.modal', function(e){
		initUploader();
	});
	
	$('#createTypeClassBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		$('#resetBtn').show();
		createTypeClassModal.modal('show');
	});
	
	var createTypeClassForm = $('#createTypeClassForm');
	
	createTypeClassForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	createTypeClassForm.on("submit", function(event){
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
						TypeClassTable.ajax.reload(null,false);
						createTypeClassModal.modal('hide')
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/event/editTypeClass", form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						TypeClassTable.ajax.reload(null,false);
						createTypeClassModal.modal('hide')
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
	
	createTypeClassForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createTypeClassForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		createTypeClassForm.validationEngine('hideAll');
	});
	
	
	
	
	
	$("#TypeClassTable").delegate("button[id=editTypeValue]", "click", function(){
		$("#fextendClassId").val($(this).attr("mId"));
		typeValueTable.ajax.reload(null,false);
		addTypeClassValueModal.modal('show');
	});
	
	
	
	
	// 属性值设置
	var addTypeClassValueModal =  $('#addTypeClassValueModal');
	addTypeClassValueModal.on('hide.bs.modal', function(e){
		editTypeValueForm.trigger("reset");
	});

	var editTypeValueForm = $('form#editTypeValueForm');
	
	editTypeValueForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	editTypeValueForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			$.post(form.attr("action"), form.serialize(), function(data){
				if(data.success){
					toastr.success(data.msg);
					typeValueTable.ajax.reload(null,false);
					form.trigger("reset");
				}else{
					toastr.error(data.msg);
				}
				form.removeData("running");
			}, "json");
		}
	});
	
	editTypeValueForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$("#id").val("");
		$("#fvalue").val("");
		$("#sort").val("");
		editTypeValueForm.validationEngine('hideAll');
	});

	
	
	
	//属性值tables初始化
	var typeValueTable = $("table#typeValueTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/event/getTypeValueList",
 		    "type": "POST",
 		    "data": function (data) {
 		    	data["fextendClassId"] = $("#fextendClassId").val();
 		        return data;
 		    }
		},
		"stateSave": true,
		"deferRender": true,
		"lengthMenu": [[10, 20, 30, 50], [10, 20, 30, 50]],
		"lengthChange": false,
		"bjqueryui" : true,
		"displayLength" : datatablePageLength,
		 "spaginationtype" : "full_numbers", // 分页
		"retrieve": true,
		"columns" : [
		{	"title" : "ID",
			"data" : "DT_RowId",
			"visible": false,
			"orderable" : false
		}, {
			"title" : '<center>属性值</center>',
			"data" : "fvalue",
			"className": "text-center",
			"width" : "80px",
			"orderable" : false
		}, {
			"title" : '<center>排序</center>',
			"data" : "fsort",
			"className": "text-center",
			"width" : "80px",
			"orderable" : false
		},{
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "50px",
			"className": "text-center",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [3],
			"render" : function(data, type, full) {
				var retString = '<button id="edit" tId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs"><fmt:message key="fxl.button.edit" /></button><button id="delete" tId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs"><fmt:message key="fxl.button.delete" />';
				return retString;
			}
		}]
	});
	
	
	$("#typeValueTable").delegate("button[id=edit]", "click", function(){
		var tId = $(this).attr("tId");
		$.post("${ctx}/fxl/event/editTypeValue/" + tId, function(data) {
			if(data.success){
				$("#fid").val(tId);
				$("#fvalue").val(data.fvalue);
				$("#sort").val(data.fsort);
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	$("#typeValueTable").delegate("button[id=delete]", "click", function(){
		var tId = $(this).attr("tId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '请确认是否要删除该属性值',
		    okValue: '<fmt:message key="fxl.button.ok" />',
		    ok: function () {
		    	$.post("${ctx}/fxl/event/delTypeValue/" + tId, function(data) {
					if(data.success){
						toastr.success(data.msg);
						typeValueTable.ajax.reload(null,false);
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
  <div class="col-md-10"><h3>商品属性</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>商品属性列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createTypeClassBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 创建商品属性</button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<table id="TypeClassTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>

<!--编辑文章开始-->
<div class="modal fade" id="createTypeClassModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">编辑商品属性值信息</h4>
      </div>
      <form id="createTypeClassForm" action="${ctx}/fxl/event/addTypeClass" method="post" class="form-inline" role="form">
      <div class="modal-body">
      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
      		<input id="id" name="id" type="hidden">
      		<div class="form-group has-error"><label for="fclassName">商品属性名称：</label>
				<input type="text" id="fclassName" name="fclassName" class="form-control validate[required,minSize[1],maxSize[30],ajax[checkTypeClassName]]" size="60">
		    </div>
      		<div class="form-group has-error"><label for="fsort">排序：</label>
				<input type="text" id="fsort" name="fsort" class="form-control validate[required,minSize[1],maxSize[30],ajax[checkTypeClassSort]]" size="60">
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

<!-- 编辑属性值modal--dataTables -->
<div class="modal fade" id="addTypeClassValueModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" 
	aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
  		<div class="modal-content">
	       <div class="modal-header">
		        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		        <h4 class="modal-title" id="myModalLabel">设置商品属性值</h4>
	       </div>
	       <div class="modal-body">
				<div class="alert alert-danger text-center" role="alert" style="padding: 5px;">
					<strong><fmt:message key="fxl.common.redRequired" /></strong>
				</div>
					<form id="editTypeValueForm" class="form-inline" role="form" action="${ctx}/fxl/event/addTypeValue" method="post">
						<input type="hidden" id="fextendClassId" name="fextendClassId"><!--对应的属性id  -->
						<input type="hidden" id="fid" name="id"><!-- 属性值的id -->
						<div class="form-group has-error"><label for="fvalue">属性值：</label>
						<input type="text" id="fvalue" name="fvalue" class="form-control validate[required,maxSize[100],ajax[checkTypeValueName]]" ></div>
						<div class="form-group has-error"><label for="fsort">排序：</label>
						<input type="text" id="sort" name="fsort" class="form-control  validate[required,maxSize[100],ajax[checkTypeValueSort]]" ></div>
						<button class="btn btn-primary" type="submit">
							<span class="glyphicon glyphicon-floppy-saved"></span>
							<fmt:message key="fxl.button.save" />
						</button>
						<button class="btn btn-warning" type="reset">
							<span class="glyphicon glyphicon-repeat"></span>
							<fmt:message key="fxl.button.reset" />
						</button>
					</form>
	    			<table id="typeValueTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
	    		</div>
			    <div class="modal-footer">
					<button class="btn btn-default" type="button" data-dismiss="modal">
						<span class="glyphicon glyphicon-remove"></span><fmt:message key="fxl.button.close" />
					</button>
				</div>
    		</div>
		</div>
	</div> 
	<!-- 设置属性值modal结束 -->
</body>
</html>