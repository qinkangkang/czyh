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
	
	$('#fdateDiv').datepicker({
	    format : "yyyy-mm-dd",
	    //startDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	var searchForm = $('form#searchForm');
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	searchForm.submit(function(e) {
		e.preventDefault();
		couponChannelTable.ajax.reload();
	});
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
    });
	
	var couponChannelTable = $("table#couponChannelTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/coupon/getCouponChannelList",
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
			"title" : '<center>优惠券名称</center>',
			"data" : "fcouponName",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>使用范围</center>',
			"data" : "fuseRange",
			"className": "text-center",
			"width" : "60px",
			"orderable" : false
		}, {
			"title" : '<center>状态</center>',
			"data" : "statusString",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>排序</center>',
			"data" : "forder",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>开始时间</center>',
			"data" : "fbeginTime",
			"className": "text-center",
			"width" : "60px",
			"orderable" : false
		}, {
			"title" : '<center>结束时间</center>',
			"data" : "fendTime",
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
			"targets" : [1],
			"render" : function(data, type, full) {
				var retString = '<a id="view" href="javascript:;" mId="' + full.fdeliveryId + '">' + data + '</a>';
				return retString;
			}
		}, {
			"targets" : [7],
			"render" : function(data, type, full) {
				var retString = '';
				if(data.fstatus == 10 ){
					retString = '<div class="btn-group"><button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">执行<span class="caret"></span></button><ul class="dropdown-menu"><li><a href="javascript:;" id="offsale" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-save"></span> 下架</a></li>';
				}else{	
					retString = '<div class="btn-group"><button type="button" class="btn btn-success btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">执行<span class="caret"></span></button><ul class="dropdown-menu">'
					+ '<li><a href="javascript:;" id="edit" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-edit"></span> <fmt:message key="fxl.button.edit" /></a></li>'
					+ '<li><a href="javascript:;" id="onsale" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-open"></span> 上架</a></li>';
				}
				return retString;
			}
		}]
	});
	
	$("#couponChannelTable").delegate("a[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		$('#resetBtn').hide();
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/coupon/getCouponChannel/" + mId, function(data) {
			if(data.success){
				$("#id").val(mId);
				$("#fDeliveryId").val(data.fDeliveryId);
				$("#fname").val(data.fname);
				$("#fSubtitle").val(data.fSubtitle);
				$("#fUseRange").val(data.fUseRange);
				$("#forder").val(data.forder);
				$("#fstartDate").val(data.fstartDate);
				$("#fendDate").val(data.fendDate);
				createChannelModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	$("#couponChannelTable").delegate("a[id=onsale]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要上架该栏目吗？',
		    okValue: '上架',
		    ok: function () {
		    	$.post("${ctx}/fxl/coupon/onsaleChannel/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						couponChannelTable.ajax.reload(null,false);
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
	
	$("#couponChannelTable").delegate("a[id=offsale]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要下架该栏目吗？',
		    okValue: '下架',
		    ok: function () {
		    	$.post("${ctx}/fxl/coupon/offsaleChannel/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						couponChannelTable.ajax.reload(null,false);
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
	
	$("#couponChannelTable").delegate("a[id=view]", "click", function(){
		var mId = $(this).attr("mId");
		window.open("${ctx}/fxl/coupon/deliveryView/" + mId );
	});
	
	var createChannelModal =  $('#createChannelModal');
	
 	createChannelModal.on('hide.bs.modal', function(e){
 		if(e.target.id === "createChannelModal"){
 			createChannelForm.trigger("reset");
 		}
	});
	
	$('#createChannelBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		$('#resetBtn').show();
		createChannelModal.modal('show')
	});
	
	var createChannelForm = $('#createChannelForm');
	
	createChannelForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	createChannelForm.on("submit", function(event){
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
						couponChannelTable.ajax.reload(null,false);
						createChannelModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/coupon/editCouponChannel", form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						couponChannelTable.ajax.reload(null,false);
						createChannelModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
	
	createChannelForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createChannelForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		createChannelForm.validationEngine('hideAll');
	});
	
	
	$('#createCouponChannelphotoBtn').on('click',function(e) {
		$.post("${ctx}/fxl/coupon/createCouponChannelphoto" , function(data) {
			if(data.success){
				window.open("${ctx}/fxl/app/toChannelSlider/"+data.id);
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-10"><h3>优惠券频道设置</h3></div>
  <div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>优惠券频道列表</h4></div>
  <div class="col-md-10"><p class="text-right">
    <button id="createCouponChannelphotoBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 轮播图配置</button>
    <button id="createChannelBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 新增</button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/app/getChannelList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_ftitle">优惠券名称：</label>
			<input type="text" id="s_ftitle" name="s_ftitle" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_fstatus">状态：</label>
			<select id="s_fstatus" name="s_fstatus" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="couponChannelItem" items="${couponChannelMap}"> 
				<option value="${couponChannelItem.key}">${couponChannelItem.value}</option>
				</c:forEach>
			</select>
		</div>
		<p class="text-center"><button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="couponChannelTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
<!--编辑活动开始-->
<div class="modal fade" id="createChannelModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">新增优惠券</h4>
      </div>
      <form id="createChannelForm" action="${ctx}/fxl/coupon/addCouponChannel" method="post" class="form-inline" role="form">
      <input id="id" name="id" type="hidden">
      <div class="modal-body">
      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
		
		<div class="form-group has-error"><label for="fDeliveryId">促销活动id：</label>
			<input type="text" id="fDeliveryId" name="fDeliveryId" class="form-control validate[required,minSize[2],maxSize[250]]">
	    </div>
     	<div class="form-group has-error"><label for="fname">名称：</label>
			<input type="text" id="fname" name="fname" class="form-control validate[required,minSize[2],maxSize[250]]">
	    </div>
	    <div class="form-group has-error"><label for="fSubtitle">类型：</label>
			<input type="text" id="fSubtitle" name="fSubtitle" class="form-control validate[minSize[2],maxSize[250]]" size="40">
	    </div></br>
	    <div class="form-group has-error"><label for="fUseRange">使用范围：</label>
			<input type="text" id="fUseRange" name="fUseRange" class="form-control validate[minSize[2],maxSize[250]]" size="40">
	    </div>
		<div class="form-group has-error"><label for="forder">排序：</label>
			<div class="input-group">
	    		<input type="text" id="forder" name="forder" class="form-control validate[required,integer,min[1],max[100]]" size="5"><div class="input-group-addon">请输入1-100之间整数，数值越小排序越靠前</div>
	    	</div>
	    </div>
	    <div class="form-group has-error"><label for="fstartDate">活动日期：</label>
			<div id="fdateDiv" class="input-daterange input-group date" style="width:330px;">
				<input type="text" class="form-control validate[required]" id="fstartDate" name="fstartDate" style="cursor: pointer;"><span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
				<input type="text" class="form-control validate[required]" id="fendDate" name="fendDate" style="cursor: pointer;">
			</div>
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
<!--编辑活动结束-->

</body>
</html>