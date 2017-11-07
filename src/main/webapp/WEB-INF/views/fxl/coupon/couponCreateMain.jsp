<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<style>
.table-list input{border:1px solid #E9EBEC;border-radius: 0px;}
.coupon-tip{font-size: 20px;font-weight:bold;border-left: 3px solid #EFC428;padding-left:5px;}
.coupon-new{background-color: #fff;padding:20px 10px;}
#coupon_panel_history table{min-width:800px;}
</style>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/webuploader/webuploader.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/fxl/css/webuploader_style.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/fxl/css/webuploader_demo.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2-bootstrap.min.css" rel="stylesheet" type="text/css">
<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js" charset="UTF-8"></script>
<script type="text/javascript" charset="utf-8" src="${ctx}/styles/ueditor/ueditor.config.js"></script>
<script type="text/javascript" charset="utf-8" src="${ctx}/styles/ueditor/ueditor.all.min.js"></script>
<script type="text/javascript" charset="utf-8" src="${ctx}/styles/ueditor/lang/zh-cn/zh-cn.js"></script>
<script type="text/javascript" src="${ctx}/styles/webuploader/webuploader.html5only.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/select2.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/i18n/zh-CN.js"></script>
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
	
	$('#fuseTimeDiv').datepicker({
	    format : "yyyy-mm-dd",
	    startDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	$("#select").click(function(e) {
		coupon_panel_history.ajax.reload();
		//$("#selectDiv").slideUp("slow");
    });
	
	var searchForm = $('form#searchForm');
	
	
	//最近用过的券
	var coupon_panel_history = $("table#coupon_panel_history").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/coupon/getCouponList",
 		    "type": "POST",
 		    "data": function (data) {
 		    	data["couponStartOffsetKey"] = "couponViewStartOffset";
 		    	$.each(searchForm.serializeArray(), function(i, n){
 		    		data[n.name] = n.value;
 				});
 		    	data['status'] = parseInt(10);
 		    	data['type'] = parseInt(10);
 		        return data;
 		    }
		},
		"stateSave": false,
		"deferRender": true,
		//"pagingType": "full_numbers",
		"lengthMenu": [[10, 20, 30, 50], [10, 20, 30, 50]],
		"lengthChange": false,
		"displayLength" : 4,
		"columns" : [
		{	"title" : "ID",
			"data" : "DT_RowId",
			"visible": false,
			"orderable" : false
		},{
			"title" : '<center>复用券</center>',
			"data" : "",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		},{
			"title" : '<center>券名称</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "150px",
			"orderable" : false
		}, {
			"title" : '<center>使用条件</center>',
			"data" : "conditions",
			"className": "text-center",
			"width" : "60px",
			"orderable" : false
		}, {
			"title" : '<center>创建人</center>',
			"data" : "operator",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>创建时间 </center>',
			"data" : "createtime",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		}, {
			"title" : "发放范围",
			"data" : "fcreaterId",
			"visible": false,
			"orderable" : false
		}, {
			"title" : "审批状态",
			"data" : "statusString",
			"visible": false,
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [1],
			"render" : function(data, type, full) {
				return '<input id="reuse" name="reuse" type="radio"  value="" s_fcouponTitle="' + 
				full.ftitle + '" mId="' + full.DT_RowId + '" s_forderNum="' + full.couponnum + '" fuseRange="' + 
				full.useRange + '" fuseStartTime="' + full.starttimeS + '" fuseEndTime="' + full.endtimeS + '" limitation="' + 
				full.limitation + '" amount="' + full.amount + '" discount="' + full.discount + '" coupondesc="' + 
				full.coupondesc + '"/>'
			}
		}]
	});
	
	// 复用历史优惠券
	$("#coupon_panel_history").delegate("input[id=reuse]", "click", function(){
		$("#fcouponNum").val($(this).attr("s_forderNum"));
		$("#ftitle").val($(this).attr("s_fcouponTitle"));
		$("#fuseRange").val($(this).attr("fuseRange"));
		$("#fuseStartTime").val($(this).attr("fuseStartTime"));
		$("#fuseEndTime").val($(this).attr("fuseEndTime"));
		if($(this).attr("amount")==null){
			$("#fdiscount").val($(this).attr("discount"));
		}else{
			$("#famount").val($(this).attr("amount"));
		}
		$("#fcouponDesc").val($(this).attr("coupondesc"));
	});
	
    var saveCouponForm = $('#saveCouponForm');
	
    saveCouponForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
    saveCouponForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			
			$.post(form.attr("action"), $.param($.merge(form.serializeArray(),[{name:"fCouponType", value:10}]),true), function(data){
				if(data.success){
					toastr.success(data.msg);
					window.location.href = "${ctx}/fxl/coupon/toCouponCreateMain";
				}else{
					toastr.error(data.msg);
				}
				form.removeData("running");
			}, "json");
		}
		
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
	
	//待发放优惠券列表
    var coupon_grid_new = $("table#coupon_grid_new").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/coupon/getCouponList",
 		    "type": "POST",
 		    "data": function (data) {
 		    	data["couponStartOffsetKey"] = "couponViewStartOffset";
 		    	data['status'] = parseInt(0);
 		    	data['type'] = parseInt(10);
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
			"title" : '<center>券名称</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "150px",
			"orderable" : false
		}, {
			"title" : '<center>使用条件</center>',
			"data" : "conditions",
			"className": "text-center",
			"width" : "60px",
			"orderable" : false
		}, {
			"title" : '<center>创建人</center>',
			"data" : "operator",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>创建时间 </center>',
			"data" : "createtime",
			"className": "text-center",
			"width" : "100px",
			"orderable" : false
		}, {
			"title" : "发放范围",
			"data" : "fcreaterId",
			"visible": false,
			"orderable" : false
		}, {
			"title" : "审批状态",
			"data" : "statusString",
			"visible": false,
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "20px",
			"className": "text-center",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [7],
			"render" : function(data, type, full) {
				return '<button id="del" mId="' + full.DT_RowId + '" type="button" class="btn btn-danger btn-xs">删除</button>';
			}
		}]
	});
	
	//删除待发放优惠券
    $("#coupon_grid_new").delegate("button[id=del]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该优惠券吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/coupon/delcoupon/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						coupon_grid_new.ajax.reload(null,false);
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
	
  	//添加完优惠券后，执行下一步
  	$('#nextCouponNew').click(function(){
  		var ids = "";
        $.each($("#coupon_grid_new tbody tr[id]"),function(){
            var self = $(this);
            var id = self.attr("id");
            //var couponCount = self.find("#new_coupon_num").html();
            //param.id = id;
            //param.count = couponCount;
            ids = ids + id+",";
        });
        ids = ids.substring(0,ids.length-1);
        if($("#coupon_grid_new tbody tr[id]").size()==0){
        	toastr.error("请先添加优惠券");
            return false;
        }
        window.location.href = "${ctx}/fxl/coupon/toDeliveryCreate?ids="+ids;
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
<div class="text">
	<div class="row">
	  <div class="col-md-8"><h3>定向投放活动配置</h3></div>
	  <div class="col-md-4"><p class="text-right" style="margin:10px 0 0 0;">
	  <button type="button" class="btn btn-primary btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/coupon/deliverylist'"><span class="glyphicon glyphicon-arrow-left"></span> 返回活动浏览</button>
	  <button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
	</div>
	<div id="createEventAForm"  class="form-inline" >

		<div class="contentpanel saleslist" >
			<div class="row coupon-new">
				<div class="col-md-6" style="height: 500px;">
					<span class="coupon-tip">新建券</span>
					<form id="saveCouponForm" action="${ctx}/fxl/coupon/saveCoupon" method="post" class="form-inline" role="form">
					<div class="table-responsive">
						<div class="form-group has-error">
							<label for="s_forderNum has-error">编号：</label> <input type="text"
								id="fcouponNum" name="fcouponNum"
								class="form-control input-sm">
						</div>
						<div class="form-group has-error">
							<label for="s_fcouponTitle">名称：</label> <input type="text"
								id="ftitle" name="ftitle"
								class="form-control input-sm">
						</div>
						<div class="form-group has-error"><label for="fuseRange">适用范围说明：</label>
					        <input type="text" id="fuseRange" name="fuseRange" class="form-control validate[required,minSize[2],maxSize[16]]" size="30">
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
						<div class="form-group has-error"><label for="fcity">所属城市：</label>
							<select id="fcity" name="fcity" class="validate[required] form-control">
								<option value=""><fmt:message key="fxl.common.select" /></option>
								<c:forEach var="cityItem" items="${cityMap}"> 
								<option value="${cityItem.key}">${cityItem.value}</option>
								</c:forEach>
							</select>
						</div>
						<div class="form-group has-error"><label for="fcouponClass">券类型：</label>
							<select id="fcouponClass" name="fcouponClass" class="validate[required] form-control">
								<option value=""><fmt:message key="fxl.common.select" /></option>
								<option value="1">满减券</option>
								<option value="2">折扣券</option>
								<option value="3">代金券</option>
								<option value="4">包邮券</option>
							</select>
						</div>
						<div class="form-group has-error">
							<label for="s_fcoupondesc">优惠券描述：</label> <input type="text"
								id="fcouponDesc" name="fcouponDesc"
								class="form-control input-sm">
						</div>
						<div class="form-group has-error">
							<label for="s_fusepoint">优惠券的使用終端：</label>
							<select id="fuserPoint" name="fuserPoint" class="validate[required] form-control">
								<option value=""><fmt:message key="fxl.common.select" /></option>
								<c:forEach var="userPointItem" items="${userPointMap}"> 
								<option value="${userPointItem.key}">${userPointItem.value}</option>
								</c:forEach>
							</select>
						</div><br>
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
						
						
						<!-- <div class="input-group input-large" ><input id="add_new_coupon_button" class="btn btn-primary" type="submit" value="添加优惠券"/></div>
					 --></div>
					<div class="input-group input-large" style="margin-top: 30px;margin-bottom: 50px;">
						<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span>添加优惠券</button>
					</div>
					</form>
				</div>

				<div class="col-md-6"
					style="border-left: 1px solid #C5C3C3; float: right">
					<span class="coupon-tip">最近用过的券</span>
					<div class="search-form" style="margin-top: 10px;">
						<form class="form-inline" id="searchForm">
							<div class="form-group mc-form-group">
								<div class="form-group">
									<label for="s_forderNum">编号：</label> <input type="text"
										id="s_forderNum" name="s_forderNum"
										class="form-control input-sm">
								</div>
								<div class="form-group">
									<label for="s_feventTitle">名称：</label> <input type="text"
										id="s_feventTitle" name="s_feventTitle"
										class="form-control input-sm">
								</div>
								<div class="form-group">
									<label>创建时间：</label>
									<div class="input-daterange input-group date"
										id="createDateDiv" style="width: 330px;">
										<input type="text" class="form-control input-sm"
											name="fcreateTimeStart" style="cursor: pointer;"> <span
											class="input-group-addon"><fmt:message
												key="fxl.common.to" /></span> <input type="text"
											class="form-control input-sm" name="fcreateTimeEnd"
											style="cursor: pointer;">
									</div>
								</div>
								<div class="form-group">
									<label for="s_fcreaterId">创建人员：</label> <select
										id="s_fcreaterId" name="s_fcreaterId"
										class="form-control input-sm">
										<option value=""><fmt:message key="fxl.common.all" /></option>
										<c:forEach var="editor" items="${editorList}">
											<option value="${editor.key}">${editor.value}</option>
										</c:forEach>
									</select>
								</div>
								
								<label style="width: 80px;"></label>
								<button type="button" class="btn btn-primary sys-btn"
									id="select">查询</button>
							</div>
						</form>
					</div>
					<span style="color: red;">选择左侧按钮，可复用历史券信息</span>
					<table id="coupon_panel_history" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
				</div>
			</div>
		</div>


		<div class="row coupon-new">
			<span class="coupon-tip">即将发放的券</span>
				<table id="coupon_grid_new" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>
			<div class="input-group input-large" style="margin-top: 30px;">
				<a class="btn btn-primary" id = "nextCouponNew">下一步</a>
			</div>
		</div>

	</div>
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
</div>

</body>
</html>