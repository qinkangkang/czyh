<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>今日秒杀商品管理</title>
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
		
	$("#goodsId").select2({
		placeholder: "选择一个秒杀商品名称",
		allowClear: true,
		language: pickerLocal
	});
	
	$('#createDateDiv').datepicker({
	    format : "yyyy-mm-dd",
	    endDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	$("#fbeginTimeDiv, #fendTimeDiv").datetimepicker({
	    format : "yyyy-mm-dd hh:ii",
	    //startDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true,
	    pickerPosition: "bottom-left"
	});
	
	var searchForm = $('form#searchForm');
	
	searchForm.submit(function(e) {
		e.preventDefault();
		SeckilModelTable.ajax.reload();
	});
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		fsponsorSelect.val(null).trigger("change");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	
	var SeckilModelTable = $("table#SeckilModelTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/seckill/getSeckillList",
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
			"title" : '<center>商品名称</center>',
			"data" : "fgoodsTitle",
			"className": "text-center",
			"width" : "150px",
			"orderable" : false
		}, {
			"title" : '<center>商品原价</center>',
			"data" : "fgoodsPrice",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>商品现价</center>',
			"data" : "fgoodsPriceMoney",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>秒杀商品类型</center>',
			"data" : "ftodaySeckillTypeString",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		},{
			"title" : '<center>商品状态</center>',
			"data" : "fstatusString",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center><fmt:message key="fxl.button.operation" /></center>',
			"data" : null,
			"width" : "20px",
			"className": "text-center",
			"orderable" : false
		}],
		"columnDefs" : [{
			"targets" : [1],
			"render" : function(data, type, full) {
				var retString = '<a id="viewSeckillModule" href="javascript:;" mId="' + full.DT_RowId + '">' + data + '</a>';
				return retString;
			}
		},{
			"targets" : [6],
			"render" : function(data, type, full) {
				var retString = '';
				if(data.fgoodstatus ==10 || data.fgoodstatus ==30){
					retString = '<div class="btn-group"><button type="button" class="btn btn-success btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">执行<span class="caret"></span></button><ul class="dropdown-menu">'
					+ '<li><a href="javascript:;" id="edit" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-edit"></span> <fmt:message key="fxl.button.edit" />活动</a></li>'
					+ '<li><a href="javascript:;" id="del" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-trash"></span> <fmt:message key="fxl.button.delete" />活动</a></li>'
					+ '<li><a href="javascript:;" id="onsale" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-open"></span> 上架活动</a></li>'

				}else{
					retString = '<div class="btn-group"><button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">执行<span class="caret"></span></button><ul class="dropdown-menu"><li><a href="javascript:;" id="offsale" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-save"></span> 下架活动</a></li>';
				}
				return retString;
			}
		}]
	});
	
	$("#SeckilModelTable").delegate("a[id=del]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该砍一砍促销活动吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/seckill/delSeckillModule/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						SeckilModelTable.ajax.reload(null,false);
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
	
	$("#SeckilModelTable").delegate("a[id=onsale]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要即刻上架该秒杀商品吗？',
		    okValue: '即刻上架',
		    ok: function () {
		    	$.post("${ctx}/fxl/seckill/onSaleSeckillModule/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						SeckilModelTable.ajax.reload(null,false);
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
	
	
	$("#SeckilModelTable").delegate("a[id=offsale]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要即刻下架该砍一砍促销活动吗？',
		    okValue: '即刻下架',
		    ok: function () {
		    	$.post("${ctx}/fxl/seckill/offSaleSeckillModule/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						SeckilModelTable.ajax.reload(null,false);
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
    
	$("#SeckilModelTable").delegate("a[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		$('#resetBtn').hide();
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/seckill/getseckillModule/" + mId, function(data) {
			if(data.success){
				$("#createSeckillModuleForm #id").val(mId);
				$("#createSeckillModuleForm #goodsId").val(data.goodsId).trigger("change");
				$("#createSeckillModuleForm #fgoodsTitle").val(data.fgoodsTitle);
				$("#createSeckillModuleForm #fgoodsSubTitle").val(data.fgoodsSubTitle);
				$("#createSeckillModuleForm #fgoodsPrice").val(data.fgoodsPrice);
				$("#createSeckillModuleForm #fgoodsPriceMoney").val(data.fgoodsPriceMoney);
				
				$("#createSeckillModuleForm #fgoodsLimitation").val(data.fgoodsLimitation);
				$("#createSeckillModuleForm #ftype option[value='" + data.ftype + "']").prop("selected",true);
				$("#createSeckillModuleForm #ftodaySeckillType option[value='" + data.ftodaySeckillType + "']").prop("selected",true);
				$("#createSeckillModuleForm #goodsId option[value='" + data.goodsId + "']").prop("selected",true);
				
				createSecKillModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	
	$("#SeckilModelTable").delegate("a[id=viewSeckillModule]", "click", function(){
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/seckill/getseckillModule/" + mId, function(data) {
			if(data.success){
				$("#bargainDetailTable #fgoodsTitle").text(data.fgoodsTitle);
				$("#bargainDetailTable #fgoodsSubTitle").text(data.fgoodsSubTitle);
				$("#bargainDetailTable #fgoodsPrice").text(data.fgoodsPrice);
				$("#bargainDetailTable #fgoodsPriceMoney").text(data.fgoodsPriceMoney);
				$("#bargainDetailTable #fgoodsLimitation").text(data.fgoodsLimitation);
				$("#bargainDetailTable #ftodaySeckillTypeString").text(data.ftodaySeckillTypeString);
				$("#bargainDetailTable #ftypeString").text(data.ftypeString);
				bargainDetailModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	
	var bargainDetailModal =  $('#bargainDetailModal');
	
	$('#createEventBonusBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		createSecKillModal.modal('show');
	});
	
	var createSecKillModal =  $('#createSecKillModal');
	
	createSecKillModal.on('shown.bs.modal', function(e){
		initUploader();
		$("#mandatoryJpg option[value='false']").prop("selected",true);
	});
	
	createSecKillModal.on('hide.bs.modal', function(e){
		if(e.target.id === "createSecKillModal"){
			createSeckillModuleForm.trigger("reset");
		}
	});
	    
    var createSeckillModuleForm = $('#createSeckillModuleForm');
    
    createSeckillModuleForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
    createSeckillModuleForm.on("submit", function(event){
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
						SeckilModelTable.ajax.reload(null,false);
						createSecKillModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/seckill/editSeckillModule",  form.serializeArray(), function(data){
					if(data.success){
						toastr.success(data.msg);
						SeckilModelTable.ajax.reload(null,false);
						createSecKillModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
    
    createSeckillModuleForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createSeckillModuleForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		/* createMerchantForm.validationEngine('hideAll'); */
	});
    
    /* 解决select2跟modal冲突*/
    $.fn.modal.Constructor.prototype.enforceFocus = function () {};
    
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-8"><h3>今日秒杀商品配置</h3></div>
  <div class="col-md-4"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>今日秒杀商品列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createEventBonusBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 创建商品</button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center">
	<form id="searchForm" action="${ctx}/fxl/event/getEventList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_ftitle">商品名称：</label>
			<input type="text" id="s_ftitle" name="s_ftitle" class="form-control input-sm" >
		</div>
		
		<div class="form-group"><label>开始时间：</label>
		<div class="input-daterange input-group date" id="createDateDiv" style="width:330px;">
		    <input type="text" class="form-control input-sm" name="fbeginTime" style="cursor: pointer;">
		    <span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
		    <input type="text" class="form-control input-sm" name="fendTime" style="cursor: pointer;">
		</div></div>
		
		<div class="form-group"><label for="s_status">商品状态：</label>
			<select id="s_status" name="s_status" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="statusItem" items="${bonusStatusMap}"> 
				<option value="${statusItem.key}">${statusItem.value}</option>
				</c:forEach>
			</select>
		</div>
		
		<p class="text-center"><button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="SeckilModelTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>

<!-- 编辑商品开始 -->
<div class="modal fade" id="createSecKillModal"  role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">编辑今日秒杀信息</h4>
      </div>
	<form id="createSeckillModuleForm" action="${ctx}/fxl/seckill/addSeckillModule" method="post" class="form-inline" role="form">
	    <input id="id" name="id" type="hidden">
	    <div class="modal-body">
      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
   
        <div class="form-group has-error"><label for="goodsId">请选择秒杀商品：</label>
			<select id="goodsId" name="goodsId" class="validate[required] form-control" style="width: 600px;">
				<option value=""><fmt:message key="fxl.common.select" /></option>
				<c:forEach var="seckilModulelItem" items="${seckilModulelListMap}"> 
				<option value="${seckilModulelItem.key}">${seckilModulelItem.value}</option>
				</c:forEach>
			</select>
	    </div>
		<br/>
		<div class="form-group has-error"><label for="ftype">今日秒杀促销类型：</label>
		      <select id="ftype" name="ftype" class="validate[required] form-control">
					 <option value=""><fmt:message key="fxl.common.select" /></option>
					 <c:forEach var="SeckillModuleTypeItem" items="${SeckillModuleTypeMap}">
					 <option value="${SeckillModuleTypeItem.key}" >${SeckillModuleTypeItem.value}</option>
					 </c:forEach>
			  </select>
		 </div>   
		 <div class="form-group has-error"><label for="ftodaySeckillType">今日秒杀商品类型：</label>
		      <select id="ftodaySeckillType" name="ftodaySeckillType" class="validate[required] form-control">
					 <option value=""><fmt:message key="fxl.common.select" /></option>
					 <c:forEach var="todaySeckillTypeItem" items="${todaySeckillTypeMap}">
					 <option value="${todaySeckillTypeItem.key}" >${todaySeckillTypeItem.value}</option>
					 </c:forEach>
			  </select>
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
<!--编辑商品结束-->
<!--秒杀详情开始-->
<div class="modal fade" id="bargainDetailModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="myModalLabel">秒杀商品详情</h4>
			</div>
			<div class="modal-body">
				<div class="panel panel-info">
					<div class="panel-heading"><strong>秒杀商品信息</strong></div>
					<div class="panel-body">
						<table id="bargainDetailTable" class="table table-hover table-striped table-bordered">
							<tr>
								<td width="15%" align="right"><strong>秒杀商品名称</strong></td>
								<td width="35%"><div id="fgoodsTitle"></div></td>
								<td width="15%" align="right"><strong>秒杀商品副标题</strong></td>
								<td width="35%"><div id="fgoodsSubTitle"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>秒杀商品现价</strong></td>
								<td><div id="fgoodsPrice"></div></td>
								<td align="right"><strong>秒杀商品原价</strong></td>
								<td><div id="fgoodsPriceMoney"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>限购(-1不限购)</strong></td>
								<td><div id="fgoodsLimitation"></div></td>
								<td align="right"><strong>秒杀促销类型</strong></td>
								<td><div id="ftypeString"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>秒杀商品类型</strong></td>
								<td><div id="ftodaySeckillTypeString"></div></td>
								<td align="right"><strong>创建时间</strong></td>
								<td><div id="fgoodsCreateTime"></div></td>
							</tr>
						</table>
					</div>
					<div class="panel-footer"><p class="text-right" style="margin: 0;"><em>零到壹，查找优惠</em></p></div>
				</div>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span><fmt:message key="fxl.button.close" /></button>
			</div>
		</div>
	</div>
</div>
<!--活动详情结束-->
</body>
</html>