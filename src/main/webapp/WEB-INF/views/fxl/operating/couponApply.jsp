<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css">
<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js" charset="UTF-8"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js" charset="UTF-8"></script>
<script type="text/javascript">
$(document).ready(function() {
	
	$('#createDateDiv').datepicker({
	    format : "yyyy-mm-dd",
	    endDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	$("#fdeliverStartTimeDiv, #fdeliverEndTimeDiv").datetimepicker({
	    format : "yyyy-mm-dd hh:ii",
	    startDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	    //,pickerPosition: "bottom-left"
	});
	
	$('#fuseTimeDiv').datepicker({
	    format : "yyyy-mm-dd",
	    startDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	var searchForm = $('form#searchForm');
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#select").click(function(e) {
		couponTable.ajax.reload();
		//$("#selectDiv").slideUp("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
    });
	
	var couponTable = $("table#couponTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/operating/coupon/getApplyList",
 		    "type": "POST",
 		    "data": function (data) {
 		    	data["s_fstatus"] = $("#s_fstatus").val();
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
			"title" : '<center>优惠券编号</center>',
			"data" : "forderNum",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>优惠券名称</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		}, {
			"title" : '<center>发放领<br/>取时间</center>',
			"data" : "deliverInfo",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>可用时间</center>',
			"data" : "useInfo",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>金额信息</center>',
			"data" : "amountInfo",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>总数量</center>',
			"data" : "fcount",
			"className": "text-right",
			"width" : "20px",
			"orderable" : true
		}, {
			"title" : '<center>已发数量</center>',
			"data" : "fsendCount",
			"className": "text-right",
			"width" : "20px",
			"orderable" : true
		}, {
			"title" : '<center>适用类型</center>',
			"data" : "fuseType",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>发放类型</center>',
			"data" : "fdeliverType",
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
			"targets" : [10],
			"render" : function(data, type, full) {
				var retString = '';
				if(data.fstatus == 10){
					retString += '<button id="edit" mId="' + full.DT_RowId + '" type="button" class="btn btn-info btn-xs"><fmt:message key="fxl.button.edit" /></button>';
				}else if(data.fstatus == 20){
					retString += '<button id="cancel" mId="' + full.DT_RowId + '" type="button" class="btn btn-info btn-xs"><fmt:message key="fxl.button.cancel" /></button>';
				}else if(data.fstatus == 30){
					retString += '<button id="issue" mId="' + full.DT_RowId + '" type="button" class="btn btn-info btn-xs">发放</button>';
				}
				return retString;
			}
		}]
	});
	
	/* 发放优惠券功能，应该独立成一个功能，但是由于时间关系，就先写这了 */
	$("#couponTable").delegate("button[id=issue]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要即刻将该优惠券发放给用户吗？',
		    okValue: '即刻发放',
		    ok: function () {
		    	$.post("${ctx}/fxl/operating/coupon/issueCoupon/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						couponTable.ajax.reload(null,false);
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
	
	$("#couponTable").delegate("button[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		$('#resetBtn').hide();
		delBtn.show();
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/operating/coupon/getCoupon/" + mId, function(data) {
			if(data.success){
				$("#id").val(mId);
				$("#ftitle").val(data.ftitle);
				$("#fcouponNum").val(data.fcouponNum);
				$("#fcity option[value='" + data.fcity + "']").prop("selected",true);
				$("#fdescription").val(data.fdescription);
				$("#fnotice").val(data.fnotice);
				$("#fuseRange").val(data.fuseRange);
				$("#famount").val(data.famount);
				$("#fdiscount").val(data.fdiscount);
				$("#fcount").val(data.fcount);
				$('#fdeliverStartTime').datetimepicker('update', data.fdeliverStartTime);
				$('#fdeliverEndTime').datetimepicker('update', data.fdeliverEndTime);
				$("#fuseStartTime").datepicker('update', data.fuseStartTime);
				$("#fuseEndTime").datepicker('update', data.fuseEndTime);
				$("#fuseType option[value='" + data.fuseType + "']").prop("selected",true);
				$("#fdeliverType option[value='" + data.fdeliverType + "']").prop("selected",true);
				//$("#ftypeA option[value='" + data.ftypeA + "']").prop("selected",true);
				createCouponModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	var createCouponModal =  $('#createCouponModal');
	
	createCouponModal.on('hide.bs.modal', function(e){
		if(e.target.id === "createCouponModal"){
			createCouponForm.trigger("reset");
		}
	});
	
	$('#createCouponBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		$('#resetBtn').show();
		delBtn.hide();
		createCouponModal.modal('show')
	});
	
	var createCouponForm = $('#createCouponForm');
	
	createCouponForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	createCouponForm.on("submit", function(event){
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
						couponTable.ajax.reload(null,false);
						createCouponModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/operating/coupon/editCoupon", form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						couponTable.ajax.reload(null,false);
						createCouponModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
	
	createCouponForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createCouponForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		createCouponForm.validationEngine('hideAll');
	});
	
	var delBtn = $("#delBtn");
	
	delBtn.click(function(event) {
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该优惠券申请吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/operating/coupon/delCoupon/" + $("#id").val() , function(data) {
					if(data.success){
						toastr.success(data.msg);
						couponTable.ajax.reload(null,false);
						createCouponModal.modal("hide");
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

	$('a[id="couponStatusBtn"]').each(function(i,n){
		$(this).click(function(e){
			e.stopPropagation();
			$("#s_fstatus").val($(this).data("status"));
			couponTable.ajax.reload();
		});
	});
	
	var fuseType = $('#fuseType');
	fuseType.change(function(e){
		if(fuseType.val() == 10){
			$('#useTypeEntityDiv').slideUp();
			$('#useTypeCategoryDiv').slideUp();
		}else if(fuseType.val() == 30){
			$('#useTypeEntityDiv').slideUp();
			$('#useTypeCategoryDiv').slideDown();
		}else {
			$('#useTypeEntityDiv').slideDown();
			$('#useTypeCategoryDiv').slideUp();
		}
    });
	
	var sliderTargetTable = $("table#sliderTargetTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"paging": false,
		"autoWidth" : false,
	  	"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/app/getSliderTargetList",
 		    "type": "POST",
 		    "data": function (data) {
 		    	data["cityId"] = $("#fcity").val();
 		    	if(fuseType.val() == '40'){
 		    		data["urlType"] = 1;
 		    	}else{
 		    		data["urlType"] = 2;
 		    	}
 		    	data["searchKey"] = $("#searchKey").val();
 		    	/* $.each(searchForm.serializeArray(), function(i, n){
 		    		data[n.name] = n.value;
 				}); */
 		        return data;
 		    }
		},
		"deferLoading": 0,
		"deferRender": true,
		"lengthChange": false,
		"retrieve": true,
		"columns" : [{
			"title" : '<center><fmt:message key="fxl.common.select2" /></center>',
			"data" : "DT_RowId",
			"width" : "20px",
			"className": "text-center",
			"orderable" : false
		}, {
			"title" : '<center>检索结果</center>',
			"data" : "info",
			"className": "text-left",
			"width" : "500px",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [ 0 ],
			"render" : function(data, type, full) {
				return '<input id="entityId" name="entityId" type="radio" value="'+ data + '" data-title="' + full.title + '" class="radio">';
			}
		}]
	});
	
	$("#sliderTargetTable").delegate("input[id=entityId]", "click", function(){
		$("#fentityId").val($(this).val());
		$("#fentityTitle").val($(this).data("title"));
	});
	
	var sliderTargetModal =  $('#sliderTargetModal');
	sliderTargetModal.on('hidden.bs.modal', function(e){
		$("#searchKey").val("");
		createCouponModal.css("overflow-y","auto");
	});

	$('#selectSliderTarget').on('click',function(e) {
		sliderTargetModal.modal('show');
	});
	
	$("#sliderTargetSearchBtn").on('click',function(e) {
		sliderTargetTable.ajax.reload();
    });

});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<input id="s_fstatus" name="s_fstatus" type="hidden" value="10">
<div class="row">
	<div class="col-md-10"><h3>优惠券申请</h3></div>
	<div class="col-md-2"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
	<div class="col-md-2"><h4>申请列表</h4></div>
	<div class="col-md-10"><p class="text-right"><button id="createCouponBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 创建优惠券</button>
	<button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center" style="display: none;">
	<form id="searchForm" action="${ctx}/fxl/operating/coupon/getApplyList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_forderNum">优惠券编号：</label>
			<input type="text" id="s_forderNum" name="s_forderNum" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_feventTitle">优惠券名称：</label>
			<input type="text" id="s_feventTitle" name="s_feventTitle" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_fsponsor">组织者：</label>
	        <select id="s_fsponsor" name="s_fsponsor" class="form-control input-sm" style="width: 220px;">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="sponsorItem" items="${sponsorMap}"> 
				<option value="${sponsorItem.key}">${sponsorItem.value}</option>
				</c:forEach>
			</select>
	    </div>
		<div class="form-group"><label for="s_fcustomerName">用户名称：</label>
			<input type="text" id="s_fcustomerName" name="s_fcustomerName" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_fcustomerPhone">用户手机号：</label>
			<input type="text" id="s_fcustomerPhone" name="s_fcustomerPhone" class="form-control input-sm" >
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
<ul class="nav nav-tabs" id="couponTab">
	<li><a id="couponStatusBtn" href="javascript:;" data-status="10" data-toggle="tab">待审核</a></li>
	<li><a id="couponStatusBtn" href="javascript:;" data-status="20" data-toggle="tab">审核中</a></li>
	<li><a id="couponStatusBtn" href="javascript:;" data-status="30" data-toggle="tab">审核通过</a></li>
	<li><a id="couponStatusBtn" href="javascript:;" data-status="40" data-toggle="tab">用户可领取</a></li>
	<li><a id="couponStatusBtn" href="javascript:;" data-status="50" data-toggle="tab">审核未通过</a></li>
	<li><a id="couponStatusBtn" href="javascript:;" data-status="60" data-toggle="tab">取消审核</a></li>
</ul>
<table id="couponTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-bordered"></table>
<!--优惠券编辑开始-->
<div class="modal fade" id="createCouponModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="myModalLabel">编辑优惠券信息</h4>
			</div>
			<form id="createCouponForm" action="${ctx}/fxl/operating/coupon/addCoupon" method="post" class="form-inline" role="form">
				<input id="id" name="id" type="hidden">
				<div class="modal-body">
					<div class="alert alert-danger text-center" role="alert" style="padding: 5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
					<div class="form-group has-error"><label for="ftitle">优惠券名称：</label>
						<input type="text" id="ftitle" name="ftitle" class="form-control validate[required,minSize[2],maxSize[250]]" size="100">
					</div>
					<div class="form-group has-error"><label for="fcouponNum">优惠券编号：</label>
						<input type="text" id="fcouponNum" name="fcouponNum" class="form-control validate[required,minSize[2],maxSize[15]]" size="20">
					</div>
					<div class="form-group"><label for="fcity">所属城市：</label>
						<select id="fcity" name="fcity" class="validate[required] form-control">
							<option value=""><fmt:message key="fxl.common.select" /></option>
							<c:forEach var="cityItem" items="${cityMap}"> 
							<option value="${cityItem.key}">${cityItem.value}</option>
							</c:forEach>
						</select>
					</div>
					<div class="form-group" style="min-height: 110px;"><label for="fdescription">发放说明：</label>
				        <textarea id="fdescription" name="fdescription" cols="100%" rows="5" class="validate[maxSize[250]] form-control"></textarea>
				    </div>
				    <div class="form-group" style="min-height: 110px;"><label for="fnotice">提示信息：</label>
				        <textarea id="fnotice" name="fnotice" cols="100%" rows="5" class="validate[maxSize[250]] form-control"></textarea>
				    </div>
				    <div class="form-group has-error"><label for="fuseRange">适用范围说明：</label>
				        <input type="text" id="fuseRange" name="fuseRange" class="form-control validate[required,minSize[2],maxSize[16]]" size="30">
				    </div>
				    <div class="form-group"><label for="fdeliverStartTime">发放起时间：</label>
						<div id="fdeliverStartTimeDiv" class="input-group date form_datetime">
							<input id="fdeliverStartTime" name="fdeliverStartTime" type="text" class="form-control" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
						</div>
					</div>
					<div class="form-group"><label for="fdeliverEndTime">发放止时间：</label>
						<div id="fdeliverEndTimeDiv" class="input-group date form_datetime">
							<input id="fdeliverEndTime" name="fdeliverEndTime" type="text" class="form-control" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
						</div>
					</div>
					<div class="form-group has-error"><label for="fuseStartTime">可用有效期：</label>
						<div id="fuseTimeDiv" class="input-daterange input-group date" style="width:330px;">
							<input type="text" class="form-control validate[required]" id="fuseStartTime" name="fuseStartTime" style="cursor: pointer;"><span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
							<input type="text" class="form-control validate[required]" id="fuseEndTime" name="fuseEndTime" style="cursor: pointer;">
						</div>
					</div>
 				    <div class="form-group has-error"><label for="flimitation">优惠金额信息：</label>
						<div class="input-group">
							<div class="input-group-addon">满</div><input type="text" id="flimitation" name="flimitation" class="form-control validate[custom[number],min[0],max[99999]]" size="5"><div class="input-group-addon">元，减</div>
							<input type="text" id="famount" name="famount" class="form-control validate[custom[number],min[0],max[99999]]" size="5"><div class="input-group-addon">元，或者打</div>
							<input type="text" id="fdiscount" name="fdiscount" class="form-control validate[custom[integer],min[0],max[100]]" size="5"><div class="input-group-addon">％折扣</div>
						</div>
					</div>
					<div class="form-group has-error"><label for="fcount">发券数量：</label>
						<div class="input-group">
							<input type="text" id="fcount" name="fcount" class="form-control validate[required,custom[integer],min[0],max[999999]]" size="5"><div class="input-group-addon">张。0表示不限量</div>
						</div>
					</div>
					<div class="form-group has-error"><label for="fdeliverType">优惠券发放形式：</label>
						<select id="fdeliverType" name="fdeliverType" class="form-control validate[required]">
							<option value=""><fmt:message key="fxl.common.select" /></option>
							<c:forEach var="couponDeliverItem" items="${couponDeliverMap}">
								<option value="${couponDeliverItem.key}">${couponDeliverItem.value}</option>
							</c:forEach>
						</select>
					</div>
					<div class="form-group has-error"><label for="fuseType">优惠券适用范围：</label>
						<select id="fuseType" name="fuseType" class="form-control validate[required]">
							<c:forEach var="couponUseItem" items="${couponUseMap}">
								<option value="${couponUseItem.key}">${couponUseItem.value}</option>
							</c:forEach>
						</select>
					</div>
					<div id="useTypeEntityDiv" style="display: none;">
						<div class="form-group has-error"><label for="fentityTitle">优惠券适用目标：</label>
							<input id="fentityId" name="fentityId" type="hidden" value="">
							<div id="selectSliderTarget" class="input-group" style="cursor: pointer;">
								<input type="text" id="fentityTitle" name="fentityTitle" placeholder="点击选择适用目标" class="validate[required] form-control" readonly="readonly" size="30">
								<span class="input-group-addon"><i class="glyphicon glyphicon-send"></i></span>
							</div>
					    </div>
				    </div>
				    <div id="useTypeCategoryDiv" style="display: none;">
						<div class="form-group has-error"><label for="ftypeA">优惠券适用目标：</label>
							<select id="ftypeA" name="ftypeA" class="validate[required] form-control" style="width: 200px;">
								<option value=""><fmt:message key="fxl.common.select" /></option>
								<c:forEach var="categoryItem" items="${categoryMapA}"> 
								<option value="${categoryItem.key}">${categoryItem.value}</option>
								</c:forEach>
							</select>
					    </div>
				    </div>
					<div style="clear: both;"></div>
				</div>
				<div class="modal-footer">
					<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span><fmt:message key="fxl.button.save" /></button>
					<button class="btn btn-warning" type="reset" id="resetBtn"><span class="glyphicon glyphicon-repeat"></span><fmt:message key="fxl.button.reset" /></button>
					<button class="btn btn-danger" type="button" id="delBtn"><span class="glyphicon glyphicon-trash"></span><fmt:message key="fxl.button.delete" /></button>
					<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span><fmt:message key="fxl.button.close" /></button>
				</div>
			</form>
		</div>
	</div>
</div>
<!--优惠券编辑结束-->
<!--选择优惠券适用目标开始-->
<div class="modal fade" id="sliderTargetModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">选择优惠券适用目标</h4>
      </div>
      <div class="modal-body">
      <div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong>根据您选择的“优惠券适用类型”项，通过下方输入的关键字信息，分别搜索活动或商家信息</strong></div>
      <div class="row">
      	<div class="col-md-3 text-right">搜索关键字：</div>
		<div class="col-md-6"><input type="text" id="searchKey" name="searchKey" class="form-control" ></div>
      	<div class="col-md-3"><button id="sliderTargetSearchBtn" class="btn btn-primary" type="button"><span class="glyphicon glyphicon-search"></span> 搜索</button></div>
      </div>
      <p/>
      <table id="sliderTargetTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover"></table>
      <p/>
      </div>
      <div class="modal-footer">
		<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="fxl.button.close" /></button>
      </div>
    </div>
  </div>
</div>
<!--选择优惠券适用目标结束-->
</body>
</html>