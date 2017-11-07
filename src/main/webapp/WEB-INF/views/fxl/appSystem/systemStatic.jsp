<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>系统静态资源管理</title>
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
		systemStaticTable.ajax.reload();
		//$("#selectDiv").slideUp("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	var systemStaticTable = $("table#systemStaticTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/app/getSystemStaticList",
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
			"title" : '<center>排序号</center>',
			"data" : "forder",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>静态内容标题</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "80px",
			"orderable" : false
		}, {
			"title" : '<center>静态类别(2代表系统)</center>',
			"data" : "ftype",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>静态文件访问路径</center>',
			"data" : "htmlDetail",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		},{
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
			"targets" : [6],
			"render" : function(data, type, full) {
				var retString = '<button id="edit" mId="' + full.DT_RowId + '" type="button" class="btn btn-primary btn-xs"><fmt:message key="fxl.button.edit" /></button><button id="del" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs"><fmt:message key="fxl.button.delete" /></button>';
				return retString;
			}
		}]
	});
	
	$("#systemStaticTable").delegate("button[id=del]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该静态文件吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/operating/article/delArticle/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						systemStaticTable.ajax.reload(null,false);
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
	
	$("#systemStaticTable").delegate("button[id=edit]", "click", function(){
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
				$("#fimage").val(data.fimage);container
				$("#fcityId option[value='" + data.fcityId + "']").prop("selected",true);
				$("#fartType option[value='" + data.fartType + "']").prop("selected",true);
				$("#fartCity option[value='" + data.fartCity + "']").prop("selected",true);
				if($.trim(data.fdetail) == ""){
					ue.setContent("", false);
				}else{
					ue.setContent(data.fdetail, false);
				}
				createSystemStaticModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	
	var createSystemStaticModal =  $('#createSystemStaticModal');
	
	createSystemStaticModal.on('hide.bs.modal', function(e){
		createArticleForm.trigger("reset");
	});
	
	createSystemStaticModal.on('shown.bs.modal', function(e){
	});
	
	$('#createArticleBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		$('#resetBtn').show();
		createSystemStaticModal.modal('show');
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
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			if($('#actionFlag').val() == "add"){
				$.post(form.attr("action"), form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						systemStaticTable.ajax.reload(null,false);
						createSystemStaticModal.modal('hide')
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/operating/article/editArticle", form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						systemStaticTable.ajax.reload(null,false);
						createSystemStaticModal.modal('hide')
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
  <div class="col-md-10"><h3>静态文件管理</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>静态文件列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createArticleBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 创建静态文件</button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/operating/article/getArticleList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_fname">文件标题：</label>
			<input type="text" id="s_fname" name="s_fname" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_flevel">文件状态：</label>
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
<table id="systemStaticTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
<!--编辑文章开始-->
<div class="modal fade" id="createSystemStaticModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">编辑静态文件信息</h4>
      </div>
      <form id="createArticleForm" action="${ctx}/fxl/operating/article/addArticle" method="post" class="form-inline" role="form">
      <div class="modal-body">
      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
	     		<input id="id" name="id" type="hidden">
	   		<input id="fcityId" name="fcityId" value="1" type="hidden" />
	   		<input id="ftype" name="ftype" value="2" type="hidden" />
	   		<input id="fartCity" name="fartCity" value="朝阳区" type="hidden" />
	   		<input id="fartType" name="fartType" value="1" type="hidden" />
	   		<input id="fimage" name="fimage" value="3287" type="hidden" />
	   		
      		<div class="form-group has-error"><label for="ftitle">文件标题：</label>
				<input type="text" id="ftitle" name="ftitle" class="form-control validate[required,minSize[2],maxSize[250]]" size="90">
		    </div>
		    <div class="form-group"><label for="forder">文件排序号：</label>
				<div class="input-group">
		    		<input type="text" id="forder" name="forder" class="form-control validate[custom[integer],min[-100],max[100]]" size="5"><div class="input-group-addon">请输入-100~100之间整数，数值越大排序越靠前</div>
		    	</div>
		    </div>
		    
			<div class="form-group"><label>编辑文件内容：</label>
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

<!--图片上传进度条结束-->
</body>
</html>