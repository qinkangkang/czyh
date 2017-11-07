<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>微信定向推送</title>
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
	
	var searchForm = $('form#searchForm');
	
	searchForm.submit(function(e) {
		e.preventDefault();
		wechatPushTable.ajax.reload();
	});
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		fsponsorSelect.val(null).trigger("change");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	
	var wechatPushTable = $("table#wechatPushTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/wechat/getWechatPushList",
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
			"title" : '<center>推送活动</center>',
			"data" : "ftitle",
			"className": "text-center",
			"width" : "150px",
			"orderable" : false
		}, {
			"title" : '<center>创建时间</center>',
			"data" : "fcreateTime",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>开始推送时间</center>',
			"data" : "fstartTime",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>推送完成时间</center>',
			"data" : "fendTime",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>计划推送数</center>',
			"data" : "fplanNum",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>送达用户数</center>',
			"data" : "fdeliveryNum",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>状态</center>',
			"data" : "fstatusString",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>操作人</center>',
			"data" : "foperator",
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
				var retString = '<a id="viewBargain" href="javascript:;" mId="' + full.DT_RowId + '">' + data + '</a>';
				return retString;
			}
		},{
			"targets" : [9],
			"render" : function(data, type, full) {
				var retString = '';
				if(data.fstatus ==10 || data.fstatus ==30){
					retString = '<div class="btn-group"><button type="button" class="btn btn-success btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">执行<span class="caret"></span></button><ul class="dropdown-menu">'
					+ '<li><a href="javascript:;" id="push" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-edit"></span>发送</a></li>'
					+ '<li><a href="javascript:;" id="del" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-open"></span>作废</a></li>'
				}else{
					retString = '<div class="btn-group"><button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">执行<span class="caret"></span></button><ul class="dropdown-menu"><li><a href="javascript:;" id="stop" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-save"></span>叫停</a></li>';
				}
				return retString;
			}
		}]
	});
	
	$("#wechatPushTable").delegate("a[id=del]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要作废该条微信消息吗？',
		    okValue: '作废',
		    ok: function () {
		    	$.post("${ctx}/fxl/wechat/delWechatPush/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						wechatPushTable.ajax.reload(null,false);
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
	
	$("#wechatPushTable").delegate("a[id=push]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要发微信送消息吗？',
		    okValue: '立即发送',
		    ok: function () {
		    	$.post("${ctx}/fxl/wechat/pushMessage/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						wechatPushTable.ajax.reload(null,false);
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
	
	$("#wechatPushTable").delegate("a[id=stop]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要叫停该微信消息推送吗？',
		    okValue: '即刻上架',
		    ok: function () {
		    	$.post("${ctx}/fxl/wechat/stopMessage/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						wechatPushTable.ajax.reload(null,false);
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
	
	$("#wechatPushTable").delegate("a[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		$('#resetBtn').hide();
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/wechat/getBargaining/" + mId, function(data) {
			if(data.success){
				$("#createwechatPushForm #id").val(mId);
				$("#createwechatPushForm #ftitle").val(data.ftitle);
				$("#createwechatPushForm #eventId").val(data.feventId).trigger("change");
				$("#createwechatPushForm #fbeginTime").val(data.fbeginTime);
				$("#createwechatPushForm #fendTime").val(data.fendTime);
				$("#createwechatPushForm #fimage").val(data.fimage);
				$("#createwechatPushForm #finputText").val(data.finputText);
				$("#createwechatPushForm #fpackageDesc").val(data.fpackageDesc);
				$("#createwechatPushForm #fstartPrice").val(data.fstartPrice);
				$("#createwechatPushForm #fsettlementPrice").val(data.fsettlementPrice);
				$("#createwechatPushForm #ffloorPrice1").val(data.ffloorPrice1);				
				createwechatPushModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	
	$("#wechatPushTable").delegate("a[id=viewBargain]", "click", function(){
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/wechatPush/getWechatPushDetail/" + mId, function(data) {
			if(data.success){
				$("#bargainDetailTable #ftitle").text(data.ftitle);
				$("#bargainDetailTable #feventtitle").text(data.feventtitle);
				$("#bargainDetailTable #fbeginTime").text(data.fbeginTime);
				$("#bargainDetailTable #fendTime").text(data.fendTime);
				$("#bargainDetailTable #finputText").text(data.finputText);
				$("#bargainDetailTable #fpackageDesc").text(data.fpackageDesc);				
				bargainDetailModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	
	var bargainDetailModal =  $('#bargainDetailModal');
	
	$('#createEventBonusBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		createwechatPushModal.modal('show');
	});
	
	var createwechatPushModal =  $('#createwechatPushModal');
	
	createwechatPushModal.on('hide.bs.modal', function(e){
		if(e.target.id === "createwechatPushModal"){
			createwechatPushForm.trigger("reset");
		}
	});
	    
    var createwechatPushForm = $('#createwechatPushForm');
    
    createwechatPushForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
    createwechatPushForm.on("submit", function(event){
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
						wechatPushTable.ajax.reload(null,false);
						createwechatPushModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/wechat/editWechatPush",  form.serializeArray(), function(data){
					if(data.success){
						toastr.success(data.msg);
						wechatPushTable.ajax.reload(null,false);
						createwechatPushModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
    
    createwechatPushForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createwechatPushForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		/* createMerchantForm.validationEngine('hideAll'); */
	});
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<div class="row">
  <div class="col-md-8"><h3>推送配置</h3></div>
  <div class="col-md-4"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>推送列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createEventBonusBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 创建商品</button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center">
	<form id="searchForm" action="${ctx}/fxl/event/getEventList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_ftitle">活动名称：</label>
			<input type="text" id="s_ftitle" name="s_ftitle" class="form-control input-sm" >
		</div>
		
		<div class="form-group"><label>发送时间：</label>
		<div class="input-daterange input-group date" id="createDateDiv" style="width:330px;">
		    <input type="text" class="form-control input-sm" name="fbeginTime" style="cursor: pointer;">
		    <span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
		    <input type="text" class="form-control input-sm" name="fendTime" style="cursor: pointer;">
		</div></div>
		
		<div class="form-group"><label for="s_status">发送状态：</label>
			<select id="s_status" name="s_status" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="statusItem" items="${WechatPushStatus}"> 
				<option value="${statusItem.key}">${statusItem.value}</option>
				</c:forEach>
			</select>
		</div>
		
		<p class="text-center"><button type="submit" class="btn btn-primary"><span class="glyphicon glyphicon-play"></span> <fmt:message key="fxl.button.query" /></button>
	   	<button id="clear" type="button" class="btn btn-warning"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.clear" /></button></p>
	</form>
</div>
<table id="wechatPushTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>

<!-- 编辑推送开始 -->
<div class="modal fade" id="createwechatPushModal"  role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">编辑微信推送信息</h4>
      </div>
	<form id="createwechatPushForm" action="${ctx}/fxl/wechat/addWechatPush" method="post" class="form-inline" role="form">
	    <input id="id" name="id" type="hidden">
	    <div class="modal-body">
      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
   
      	<div class="form-group has-error"><label for="ftitle">活动名称：</label>
		        <input type="text" id="ftitle" name="ftitle" class="form-control validate[required,minSize[1],maxSize[250]]" size="90" placeholder="请输入活动名称">
		</div>
		<div class="form-group has-error"><label for="fbeginTitle">开头语：</label>
		        <input type="text" id="fbeginTitle" name="fbeginTitle" class="form-control validate[required,minSize[1],maxSize[250]]" size="90" placeholder="请输入开头语" style="margin-left: 18px;">
		</div>
		<div class="form-group has-error"><label for="femailTitle">邮件标题：</label>
		        <input type="text" id="femailTitle" name="femailTitle" class="form-control validate[required,minSize[1],maxSize[250]]" size="90" placeholder="请输入邮件标题">
		</div>
		<div class="form-group has-error" style="min-height: 110px;"><label for="fsender">发件人：</label>
		        <textarea id="fsender" name="fsender" cols="80" rows="5" class="validate[required,maxSize[250]] form-control" style="margin-left: 18px;"></textarea>
		</div>
		
		<div class="form-group has-error"><label for="furl">跳转链接：</label>
		        <input type="text" id="furl" name="furl" class="form-control validate[required,minSize[1],maxSize[250]]" size="90" placeholder="请输入跳转链接" >
		</div>
		
		<div class="form-group has-error"><label for="fendTitle">结束语：</label>
		        <input type="text" id="fendTitle" name="fendTitle" class="form-control validate[required,minSize[1],maxSize[250]]" size="90" placeholder="请输入结束语" style="margin-left: 18px;">
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
<!--编辑推送结束-->

<!--推送详情开始-->
<div class="modal fade" id="bargainDetailModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="myModalLabel">砍一砍活动详情</h4>
			</div>
			<div class="modal-body">
				<div class="panel panel-info">
					<div class="panel-heading"><strong>砍一砍活动信息</strong></div>
					<div class="panel-body">
						<table id="bargainDetailTable" class="table table-hover table-striped table-bordered">
							<tr>
								<td width="15%" align="right"><strong>活动名称</strong></td>
								<td width="35%"><div id="ftitle"></div></td>
								<td width="15%" align="right"><strong>砍一砍商品名称</strong></td>
								<td width="35%"><div id="feventtitle"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>开始时间</strong></td>
								<td><div id="fbeginTime"></div></td>
								<td align="right"><strong>结束时间</strong></td>
								<td><div id="fendTime"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>活动状态</strong></td>
								<td><div id="fstatusString"></div></td>
								<td align="right"><strong>订单类型</strong></td>
								<td><div id="ftypeString"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>砍前价格</strong></td>
								<td><div id="fstartPrice"></div></td>
								<td align="right"><strong>结算价格</strong></td>
								<td><div id="fsettlementPrice"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>每刀最多</strong></td>
								<td><div id="fmaxBargaining"></div></td>
								<td align="right"><strong>每刀最少</strong></td>
								<td><div id="fminBargaining"></div></td>
							</tr>
						</table>
						<table id="bargainDetailTable" class="table table-hover table-striped table-bordered">
							<tr>
								<td width="10%" align="right"><strong>底价1</strong></td>
								<td width="24%"><div id="ffloorPrice1"></div></td>
								<td width="10%" align="right"><strong>总库存</strong></td>
								<td width="24%"><div id="fstock1"></div></td>
								<td width="10%" align="right"><strong>剩余库存</strong></td>
								<td width="26%"><div id="getFremainingStock1"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>底价2</strong></td>
								<td><div id="ffloorPrice2"></div></td>
								<td align="right"><strong>总库存</strong></td>
								<td><div id="fstock2"></div></td>
								<td align="right"><strong>剩余库存</strong></td>
								<td><div id="getFremainingStock2"></div></td>
							</tr>
							<tr>
								<td align="right"><strong>底价3</strong></td>
								<td><div id="ffloorPrice3"></div></td>
								<td align="right"><strong>总库存</strong></td>
								<td><div id="fstock3"></div></td>
								<td align="right"><strong>剩余库存</strong></td>
								<td><div id="getFremainingStock3"></div></td>
							</tr>
							
							<tr>
								<td align="right"><strong>入口文案</strong></td>
								<td colspan="5"><div id="finputText"></div></td>
							</tr>
							
							<tr>
								<td align="right"><strong>砍价套餐描述</strong></td>
								<td colspan="5"><div id="fpackageDesc"></div></td>
							</tr>
							
							<tr>
								<td align="right"><strong>活动链接</strong></td>
								<td colspan="5"><div id="bargainUrl"></div></td>
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