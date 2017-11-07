<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/bootstrap-datepicker/css/bootstrap-datepicker3.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2.min.css" rel="stylesheet" type="text/css">
<link href="${ctx}/styles/select2/css/select2-bootstrap.min.css" rel="stylesheet" type="text/css">

<script src="${ctx}/styles/bootstrap-datepicker/js/bootstrap-datepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datepicker/js/locales/bootstrap-datepicker.zh-CN.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
<script src="${ctx}/styles/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js" charset="UTF-8"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/select2.min.js"></script>
<script type="text/javascript" src="${ctx}/styles/select2/js/i18n/zh-CN.js"></script>
<script type="text/javascript">
$(document).ready(function() {
		
	$("#eventId").select2({
		placeholder: "选择一个活动名称",
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
	
	$('#fdateDiv').datepicker({
	    format : "yyyy-mm-dd",
	    //startDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true
	});
	
	$("#fuseDateDiv").datetimepicker({
	    format : "yyyy-mm-dd hh:ii",
	    //startDate : new Date(),
	    todayBtn : "linked",
	    language : pickerLocal,
	    autoclose : true,
	    todayHighlight : true,
	    pickerPosition: "top-left"
	});
	
	var searchForm = $('form#searchForm');
	
	searchForm.submit(function(e) {
		e.preventDefault();
		eventBonusTable.ajax.reload();
	});
	
	$("#selectSwitch").click(function(e) {
		$("#selectDiv").slideToggle("slow");
    });
	
	$("#clear").click(function(e) {
		searchForm.trigger("reset");
		fsponsorSelect.val(null).trigger("change");
		//$(':input',searchForm).not(':button, :submit, :reset, :hidden').val('').removeAttr('checked').removeAttr('selected');
    });
	
	
	var eventBonusTable = $("table#eventBonusTable").DataTable({
		"language": dataTableLanguage,
		"filter": false,
		"autoWidth" : false,
	  	//"scrollY": "400px",
		"scrollCollapse": true,
		"processing": true,
		"serverSide": true,
		"ajax": {
		    "url": "${ctx}/fxl/bonus/getEventBonusList",
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
			"data" : "ftitle",
			"className": "text-center",
			"width" : "150px",
			"orderable" : false
		}, {
			"title" : '<center>兑换开始时间</center>',
			"data" : "fstartDate",
			"className": "text-center",
			"width" : "40px",
			"orderable" : false
		}, {
			"title" : '<center>兑换结束时间</center>',
			"data" : "fendDate",
			"className": "text-center",
			"width" : "50px",
			"orderable" : false
		}, {
			"title" : '<center>总数量</center>',
			"data" : "fstorage",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>所需积分</center>',
			"data" : "fbonus",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
			"title" : '<center>剩余数量</center>',
			"data" : "fstock",
			"className": "text-center",
			"width" : "30px",
			"orderable" : false
		}, {
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
				return '<a id="viewEvent" href="javascript:;" mId="' + full.eventId + '">' + data + '</a>';
			}
		}, {
			"targets" : [8],
			"render" : function(data, type, full) {
				var retString = '';
				if(data.fstatus ==10 || data.fstatus ==30){
					retString = '<div class="btn-group"><button type="button" class="btn btn-success btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">执行<span class="caret"></span></button><ul class="dropdown-menu">'
					+ '<li><a href="javascript:;" id="edit" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-edit"></span> <fmt:message key="fxl.button.edit" />商品</a></li>'
					+ '<li><a href="javascript:;" id="del" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-trash"></span> <fmt:message key="fxl.button.delete" />商品</a></li>'
					+ '<li><a href="javascript:;" id="onsale" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-open"></span> 上架商品</a></li>'

				}else{
					retString = '<div class="btn-group"><button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">执行<span class="caret"></span></button><ul class="dropdown-menu"><li><a href="javascript:;" id="offsale" mId="' + full.DT_RowId + '"><span class="glyphicon glyphicon-save"></span> 下架活动</a></li>';
				}
				return retString;
			}
		}]
	});
	
	
	$("#eventBonusTable").delegate("a[id=viewEvent]", "click", function(){
		window.open("${ctx}/fxl/event/eventView/" + $(this).attr("mId")) ;
	});
	
	$("#eventBonusTable").delegate("a[id=del]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确认要删除该商品吗？',
		    okValue: '删除',
		    ok: function () {
		    	$.post("${ctx}/fxl/bonus/delEventBonus/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						eventBonusTable.ajax.reload(null,false);
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
	
	$("#eventBonusTable").delegate("a[id=onsale]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要即刻上架该商品吗？',
		    okValue: '即刻上架',
		    ok: function () {
		    	$.post("${ctx}/fxl/bonus/onSaleBonus/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						eventBonusTable.ajax.reload(null,false);
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
	
	$("#eventBonusTable").delegate("a[id=offsale]", "click", function(){
		var mId = $(this).attr("mId");
		dialog({
			fixed: true,
		    title: '操作提示',
		    content: '您确定要即刻下架该活动吗？',
		    okValue: '即刻下架',
		    ok: function () {
		    	$.post("${ctx}/fxl/bonus/offsaleBonus/" + mId , function(data) {
					if(data.success){
						toastr.success(data.msg);
						eventBonusTable.ajax.reload(null,false);
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
    
	$("#eventBonusTable").delegate("a[id=edit]", "click", function(){
		$('#actionFlag').val("edit");
		$('#resetBtn').hide();
		var mId = $(this).attr("mId");
		$.post("${ctx}/fxl/bonus/getEventBonus/" + mId, function(data) {
			if(data.success){
				$("#createEventBounsForm #id").val(mId);
				$("#createEventBounsForm #fprompt").val(data.fprompt);
				$("#createEventBounsForm #fstartDate").val(data.fstartDate);
				$("#createEventBounsForm #fendDate").val(data.fendDate);
				$("#createEventBounsForm #fprice").val(data.fprice);
				$("#createEventBounsForm #fbonus").val(data.fbonus);
				$("#createEventBounsForm #fuseDate").val(data.fuseDate);
				$("#createEventBounsForm #fusePerson").val(data.fusePerson);
				$("#createEventBounsForm #funit").val(data.funit);
				$("#createEventBounsForm #fstorage").val(data.fstorage);
				$("#createEventBounsForm #faddress").val(data.faddress);
				$("#createEventBounsForm #fuseType").val(data.fuseType);
				$("#createEventBounsForm #fuseNote").val(data.fuseNote);
				$("#createEventBounsForm #fdeal").val(data.fdeal);
				$("#createEventBounsForm #forder").val(data.forder);
				$("#createEventBounsForm #ftitle").val(data.ftitle);
				$("#createEventBounsForm #fcustomerLevel option[value='" + data.fcustomerLevel + "']").prop("selected",true);
				$("#createEventBounsForm #ftype option[value='" + data.ftype + "']").prop("selected",true);
				$("#createEventBounsForm #eventId option[value='" + data.eventId + "']").prop("selected",true);
				createEventBonusModal.modal('show');
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	
	$('#createEventBonusBtn').on('click',function(e) {
		$('#actionFlag').val("add");
		createEventBonusModal.modal('show');
	});
	
	var createEventBonusModal =  $('#createEventBonusModal');
	
	createEventBonusModal.on('hide.bs.modal', function(e){
		if(e.target.id === "createEventBonusModal"){
			createEventBounsForm.trigger("reset");
		}
	});
	    
    var createEventBounsForm = $('#createEventBounsForm');
    
    createEventBounsForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
    createEventBounsForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			if($('#actionFlag').val() == "add"){
				$.post(form.attr("action"), form.serialize() , function(data){
					if(data.success){
						$("#bid").val(data.id);
						//console.log(data.id);
						toastr.success(data.msg);
						eventBonusTable.ajax.reload(null,false);
						createEventBonusModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/bonus/editBonus",  form.serialize(), function(data){
					if(data.success){
						$("#bid").val(data.id);
						console.log(data.id);
						toastr.success(data.msg);
						eventBonusTable.ajax.reload(null,false);
						createEventBonusModal.modal("hide");
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
    
    createEventBounsForm.on("reset", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		$(':input',createEventBounsForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		/* createMerchantForm.validationEngine('hideAll'); */
	});
    
    /* 解决select2跟modal冲突*/
    $.fn.modal.Constructor.prototype.enforceFocus = function () {};
    
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<input id="bonusEventId" name="bonusEventId" type="hidden" value="${event.id}">
<div class="row">
  <div class="col-md-8"><h3>商品配置</h3></div>
  <div class="col-md-4"><p class="text-right" style="margin:10px 0 0 0;"><button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="row">
  <div class="col-md-2"><h4>商品列表</h4></div>
  <div class="col-md-10"><p class="text-right"><button id="createEventBonusBtn" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-plus-sign"></span> 创建商品</button>
    <button id="selectSwitch" type="button" class="btn btn-primary"><span class="glyphicon glyphicon-search"></span> <fmt:message key="fxl.button.search" /></button></p></div>
</div>
<div id="selectDiv" class="text-center">
	<form id="searchForm" action="${ctx}/fxl/event/getEventList" method="post" class="form-inline" role="form">
		<div class="form-group"><label for="s_ftitle">商品名称：</label>
			<input type="text" id="s_ftitle" name="s_ftitle" class="form-control input-sm" >
		</div>
		<div class="form-group"><label for="s_status">商品状态：</label>
			<select id="s_status" name="s_status" class="form-control input-sm">
				<option value=""><fmt:message key="fxl.common.all" /></option>
				<c:forEach var="statusItem" items="${bonusStatusMap}"> 
				<option value="${statusItem.key}">${statusItem.value}</option>
				</c:forEach>
			</select>
		</div>
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
<table id="eventBonusTable" cellpadding="0" cellspacing="0" border="0" class="table table-hover table-striped table-bordered"></table>

<!-- 编辑商品开始 -->
<div class="modal fade" id="createEventBonusModal"  role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel">编辑商品信息</h4>
      </div>
      <form id="createEventBounsForm" action="${ctx}/fxl/bonus/addBonus" method="post" class="form-inline" role="form">
      	<input id="id" name="id" type="hidden">
      	 <div class="modal-body">
      	<div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
            <div class="form-group"><label for="fprompt">活动名称：</label>
				 <input type="text" id="ftitle" name="ftitle" class="form-control validate[minSize[1],maxSize[250]]" size="75">
		    </div>
            <div class="form-group has-error"><label for="eventId">请选择兑换商品：</label>
                  <select id="eventId" name="eventId" class="validate[required] form-control" style="width: 600px;">
                      <option value=""><fmt:message key="fxl.common.select" /></option>
                      <c:forEach var="eventBonusItem" items="${eventBonusListMap}"> 
                      <option value="${eventBonusItem.key}">${eventBonusItem.value}</option>
                      </c:forEach>
                 </select>
	        </div>
	        <div class="form-group"><label for="deliveyId">请选择兑换兑换优惠券：</label>
                  <select id="deliveyId" name="deliveyId" class="form-control" style="width: 600px;">
                      <option value=""><fmt:message key="fxl.common.select" /></option>
                      <c:forEach var="deliveyItem" items="${deliveyListMap}"> 
                      <option value="${deliveyItem.key}">${deliveyItem.value}</option>
                      </c:forEach>
                 </select>
	        </div>
		    <div class="form-group"><label for="fprompt">商品标签：</label>
				 <input type="text" id="fprompt" name="fprompt" class="form-control validate[minSize[1],maxSize[250]]" size="40">
			</div>
		    <div class="form-group has-error"><label for="fstartDate">活动日期：</label>
				 <div id="fdateDiv" class="input-daterange input-group date" style="width:330px;">
					<input type="text" class="form-control validate[required]" id="fstartDate" name="fstartDate" style="cursor: pointer;"><span class="input-group-addon"><fmt:message key="fxl.common.to" /></span>
					<input type="text" class="form-control validate[required]" id="fendDate" name="fendDate" style="cursor: pointer;">
				 </div>
			</div>
		    <div class="form-group has-error"><label for="fprice">商品原价：</label>
				 <div class="input-group">
		    		<input type="text" id="fprice" name="fprice" class="form-control validate[required,custom[number]]" size="5"><div class="input-group-addon">元</div>
		    	 </div>
		    </div>
		    <div class="form-group has-error" style="margin-left: 50px;"><label for="fbonus">兑换积分：</label>
				 <div class="input-group">
		    		<input type="text" id="fbonus" name="fbonus" class="form-control validate[required,custom[number]]" size="5"><div class="input-group-addon">分</div>
		    	 </div>
		    </div>
		    <div class="form-group"><label for="fcustomerLevel">兑换所需会员等级：</label>
			<select id="fcustomerLevel" name="fcustomerLevel" class="form-control input-sm">
				<c:forEach var="statusItem" items="${customerLevelMap}"> 
				<option value="${statusItem.key}">${statusItem.value}</option>
				</c:forEach>
			</select>
		</div>
		    <div class="form-group has-error" style="margin-left: 50px;"><label for="fdeal">支付金额：</label>
				 <div class="input-group">
		    		<input type="text" id="fdeal" name="fdeal" class="form-control" size="5"><div class="input-group-addon">元</div>
		    	 </div>
		    </div>
		    <div class="form-group has-error"><label for="forder">排序：</label>
				 <div class="input-group" style="margin-left: 25px;">
		    		<input type="text" id="forder" name="forder" class="form-control" size="5" value="0"><div class="input-group-addon">↑</div>
		    	 </div>
		    </div>
      		<!--<div class="form-group" style="margin-left: 55px;"><label for="fuseDate">有效日期：</label>
				 <div id="fuseDateDiv" class="input-group date form_datetime">
					<input id="fuseDate" name="fuseDate" type="text" class="form-control validate[required]" style="cursor: pointer;"><span class="input-group-addon"><i class="glyphicon glyphicon-calendar"></i></span>
				 </div>
			</div>-->
			
			<!--<div class="form-group"><label for="fusePerson">使用人数：</label>
		    	<input type="text" id="fusePerson" name="fusePerson" class="form-control validate[minSize[1],maxSize[250]]" size="40">
		    </div>
		    -->
		    <div class="form-group"><label for="fstorage" style="margin-left: 45px;">库存数量：</label>
		        <input type="text" id="fstorage" name="fstorage" class="validate[required,custom[integer]] form-control" size="5">
		    </div>
		   <!--  <div class="form-group"><label for="funit">库存单位：</label>
		        <input type="text" id="funit" name="funit" class="form-control"  size="5">
		    </div>
		   -->
		    <div class="form-group"><label for="faddress">活动地址：</label>
		    	<input type="text" id="faddress" name="faddress" class="form-control validate[minSize[1],maxSize[250]]" size="90">
		    </div>
		    <div class="form-group"><label for="fuseType">兑换内容：</label>
		    	<input type="text" id="fuseType" name="fuseType" class="form-control validate[required,minSize[1],maxSize[250]]" size="90">
		    </div>
		    <div class="form-group"><label for="fuseNote">注意事项：</label>
		    	<input type="text" id="fuseNote" name="fuseNote" class="form-control validate[minSize[1],maxSize[250]]" size="40">
		    </div>
		    <div class="form-group has-error"><label for="ftype">发货类型：</label>
		        <select id="ftype" name="ftype" class="validate[required] form-control">
					<option value=""><fmt:message key="fxl.common.select" /></option>
					<c:forEach var="bonusTypeItem" items="${bonusTypeMap}">
					<option value="${bonusTypeItem.key}" >${bonusTypeItem.value}</option>
					</c:forEach>
				</select>
		    </div>
		    <div class="form-group has-error"><label for="flimitation">限购数量：</label>
				<div class="input-group">
					<input type="text" id="flimitation" name="flimitation" class="form-control validate[required,custom[number]]" size="5"><div class="input-group-addon">个</div>
				</div>
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
</body>
</html>