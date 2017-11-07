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
<script src="http://api.map.baidu.com/api?v=2.0&ak=8vxHpx4PyxOzXyGIjUb5GAoT" type="text/javascript"></script>
<script type="text/javascript">
$(document).ready(function() {

	var createMenuForm = $('#createMenuForm');
	
	createMenuForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	createMenuForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			if($('#actionFlag').val() == "add"){
				$.post(form.attr("action"), $.param($.merge(form.serializeArray(),[{name:"eventId", value:$("#eventId").val()}]),true), function(data){
					if(data.success){
						toastr.success(data.msg);
						createMenuForm.ajax.reload(null,false);
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}else{
				$.post("${ctx}/fxl/wechat/wechatMenu/saveUpdateMenu", form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						//window.location.reload(true);
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		}
	});
	
	createMenuForm.on("reset", function(event){
/* 		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		} */
		$(':input',createMenuForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
		createMenuForm.validationEngine('hideAll');
	});

	$("#fiveAMenuItemOrder option[value='${fiveAMenuItemOrder}']").prop("selected",true);
	$("#fourAMenuItemOrder option[value='${fourAMenuItemOrder}']").prop("selected",true);
	$("#threeAMenuItemOrder option[value='${threeAMenuItemOrder}']").prop("selected",true);
	$("#towAMenuItemOrder option[value='${towAMenuItemOrder}']").prop("selected",true);
	$("#oneAMenuItemOrder option[value='${oneAMenuItemOrder}']").prop("selected",true);
	
	$("#fiveBMenuItemOrder option[value='${fiveBMenuItemOrder}']").prop("selected",true);
	$("#fourBMenuItemOrder option[value='${fourBMenuItemOrder}']").prop("selected",true);
	$("#threeBMenuItemOrder option[value='${threeBMenuItemOrder}']").prop("selected",true);
	$("#towBMenuItemOrder option[value='${towBMenuItemOrder}']").prop("selected",true);
	$("#oneBMenuItemOrder option[value='${oneBMenuItemOrder}']").prop("selected",true);
	
	$("#fiveMenuItemOrder option[value='${fiveMenuItemOrder}']").prop("selected",true);
	$("#fourHMenuItemOrder option[value='${fourHMenuItemOrder}']").prop("selected",true);
	$("#threeMenuItemOrder option[value='${threeMenuItemOrder}']").prop("selected",true);
	$("#towMenuItemOrder option[value='${towMenuItemOrder}']").prop("selected",true);
	$("#oneMenuItemOrder option[value='${oneMenuItemOrder}']").prop("selected",true);
	
});
</script>
</head>
<body>
<input id="actionFlag" name="actionFlag" type="hidden">
<input id="eventId" name="eventId" type="hidden" value="${event.id}">
<div class="row">
  <div class="col-md-8"><h3>微信自定义菜单配置</h3></div>
  <div class="col-md-4"><p class="text-right" style="margin:10px 0 0 0;">

  <button type="button" class="btn btn-success btn-sm" onclick="javascrtpt:window.location.href='${ctx}/fxl/index'"><span class="glyphicon glyphicon-home"></span> <fmt:message key="fxl.common.returnMain" /></button></p></div>
</div>
<div class="panel panel-info">
	<div class="panel-heading"><strong>微信底部菜单一</strong></div>
	<div class="panel-body">
 <form id="createMenuForm" action="${ctx}/fxl/event/xx" method="post" class="form-inline" role="form">
	
	 <!-- 一级主菜单 -->
	 <div class="form-group has-error"><label for="oneMenu">一级菜单：</label>
	       <input type="text" id="oneMenu" name="oneMenu" class="form-control validate[required,minSize[2],maxSize[10]]" value="${oneMenuName}" size="20">
	 </div>
	 <input id="oneMenuId" name="oneMenuId" value="${oneMenuId}" type="hidden">
	  
	 <div class="form-group"><label for="oneMenuUrl" style="margin-left: 50px;">一级菜单url：</label>
	       <input type="text" id="oneMenuUrl" name="oneMenuUrl" class="form-control validate[minSize[2],maxSize[300]]" value="${oneMenuUrl}" size="100">
	 </div>
	 <br/>
	 
	 <!-- 以下为一级菜单子菜单分布 -->
	 <div class="form-group"><label for="fiveAMenuItemName" style="margin-left: 80px;">子菜单名称：</label>
	       <input type="text" id="fiveAMenuItemName" name="fiveAMenuItemName" class="form-control validate[minSize[2],maxSize[300]]" value="${fiveAMenuItemName}" size="20">
	 </div>
	 <div class="form-group"><label for="fiveAMenuItemCount" style="margin-left: 120px;">url：</label>
	       <input type="text" id="fiveAMenuItemCount" name="fiveAMenuItemCount" class="form-control validate[minSize[2],maxSize[300]]" value="${fiveAMenuItemCount}" size="80">
	 </div>
	       <input id="fiveAMenuItemId" name="fiveAMenuItemId" value="${fiveAMenuItemId}" type="hidden">
	       
	       <div class="form-group has-error"><label for="fiveAMenuItemOrder">是否开启：</label>
	          <select id="fiveAMenuItemOrder" name="fiveAMenuItemOrder" class="validate[required] form-control" style="width: 120px;">
				    <option value=""><fmt:message key="fxl.common.select" /></option>
				    <c:forEach var="yesNoItem" items="${yesNoMap}"> 
				    <option value="${yesNoItem.key}">${yesNoItem.value}</option>
				    </c:forEach>
			  </select>
	       </div>
	 <br/>
	 
	 <div class="form-group"><label for="fourAMenuItemName" style="margin-left: 80px;">子菜单名称：</label>
	       <input type="text" id="fourAMenuItemName" name="fourAMenuItemName" class="form-control validate[minSize[2],maxSize[300]]" value="${fourAMenuItemName}" size="20">
	 </div>
	 <div class="form-group"><label for="fourAMenuItemCount" style="margin-left: 120px;">url：</label>
	       <input type="text" id="fourAMenuItemCount" name="fourAMenuItemCount" class="form-control validate[minSize[2],maxSize[300]]" value="${fourAMenuItemCount}" size="80">
	 </div>
	       <input id="fourAMenuItemId" name="fourAMenuItemId" value="${fourAMenuItemId}" type="hidden">
	       
	      <div class="form-group has-error"><label for="fourAMenuItemOrder">是否开启：</label>
	          <select id="fourAMenuItemOrder" name="fourAMenuItemOrder" class="validate[required] form-control" style="width: 120px;">
				    <option value=""><fmt:message key="fxl.common.select" /></option>
				    <c:forEach var="yesNoItem" items="${yesNoMap}"> 
				    <option value="${yesNoItem.key}">${yesNoItem.value}</option>
				    </c:forEach>
			  </select>
	       </div>
	 <br/>
	 
	 <div class="form-group"><label for="threeAMenuItemName" style="margin-left: 80px;">子菜单名称：</label>
	       <input type="text" id="threeAMenuItemName" name="threeAMenuItemName" class="form-control validate[minSize[2],maxSize[300]]" value="${threeAMenuItemName}" size="20">
	 </div>
	 <div class="form-group"><label for="threeAMenuItemCount" style="margin-left: 120px;">url：</label>
	       <input type="text" id="threeAMenuItemCount" name="threeAMenuItemCount" class="form-control validate[minSize[2],maxSize[300]]" value="${threeAMenuItemCount}" size="80">
	 </div>
	       <input id="threeAMenuItemId" name="threeAMenuItemId" value="${threeAMenuItemId}" type="hidden">
	       
	       <div class="form-group has-error"><label for="threeAMenuItemOrder">是否开启：</label>
	          <select id="threeAMenuItemOrder" name="threeAMenuItemOrder" class="validate[required] form-control" style="width: 120px;">
				    <option value=""><fmt:message key="fxl.common.select" /></option>
				    <c:forEach var="yesNoItem" items="${yesNoMap}"> 
				    <option value="${yesNoItem.key}">${yesNoItem.value}</option>
				    </c:forEach>
			  </select>
	       </div>
	 <br/>
	 
	 
	 <div class="form-group"><label for="towAMenuItemName" style="margin-left: 80px;">子菜单名称：</label>
	       <input type="text" id="towAMenuItemName" name="towAMenuItemName" class="form-control validate[minSize[2],maxSize[300]]" value="${towAMenuItemName}" size="20">
	 </div>
	 <div class="form-group"><label for="towAMenuItemCK" style="margin-left: 120px;">Click：</label>
	       <input type="text" id="towAMenuItemCK" name="towAMenuItemCK" class="form-control validate[minSize[2],maxSize[300]]" value="${towAMenuItemCK}" size="80">
	 </div>
	       <input id="towAMenuItemId" name="towAMenuItemId" value="${towAMenuItemId}" type="hidden">
	       
	       <div class="form-group has-error"><label for="towAMenuItemOrder">是否开启：</label>
	          <select id="towAMenuItemOrder" name="towAMenuItemOrder" class="validate[required] form-control" style="width: 120px;">
				    <option value=""><fmt:message key="fxl.common.select" /></option>
				    <c:forEach var="yesNoItem" items="${yesNoMap}"> 
				    <option value="${yesNoItem.key}">${yesNoItem.value}</option>
				    </c:forEach>
			  </select>
	       </div>
	 <br/>
	 
	 <div class="form-group"><label for="oneAMenuItemName" style="margin-left: 80px;">子菜单名称：</label>
	       <input type="text" id="oneAMenuItemName" name="oneAMenuItemName" class="form-control validate[minSize[2],maxSize[300]]" value="${oneAMenuItemName}" size="20">
	 </div>
	 <div class="form-group"><label for="oneAMenuItemCK" style="margin-left: 120px;">Click：</label>
	       <input type="text" id="oneAMenuItemCK" name="oneAMenuItemCK" class="form-control validate[minSize[2],maxSize[300]]" value="${oneAMenuItemCK}" size="80">
	 </div>
	      <input id="oneAMenuItemId" name="oneAMenuItemId" value="${oneAMenuItemId}" type="hidden">
	      
	      <div class="form-group has-error"><label for="oneAMenuItemOrder">是否开启：</label>
	          <select id="oneAMenuItemOrder" name="oneAMenuItemOrder" class="validate[required] form-control" style="width: 120px;">
				    <option value=""><fmt:message key="fxl.common.select" /></option>
				    <c:forEach var="yesNoItem" items="${yesNoMap}"> 
				    <option value="${yesNoItem.key}">${yesNoItem.value}</option>
				    </c:forEach>
			  </select>
	       </div>
	 <br/>
	 
	 <div class="panel panel-warning">
	<div class="panel-heading"><strong>微信底部菜单二</strong></div>
	 <!-- 二级主菜单 -->
	 <div class="form-group has-error"><label for="towMenu">二级菜单：</label>
	       <input type="text" id="towMenu" name="towMenu" class="form-control validate[required,minSize[2],maxSize[10]]" value="${towMenuName}" size="20">
	 </div>
	  
	 <div class="form-group"><label for="towMenuUrl" style="margin-left: 50px;">二级菜单url：</label>
	       <input type="text" id="towMenuUrl" name="towMenuUrl" class="form-control validate[minSize[2],maxSize[300]]" value="${towMenuUrl}" size="100">
	 </div>
	 <input id="towMenuId" name="towMenuId" value="${towMenuId}" type="hidden">
	 <br/>
	 
	 <!-- 二级子菜单 -->
	 
	 <div class="form-group"><label for="fiveBMenuItemName" style="margin-left: 80px;">子菜单名称：</label>
	       <input type="text" id="fiveBMenuItemName" name="fiveBMenuItemName" class="form-control validate[minSize[2],maxSize[300]]" value="${fiveBMenuItemName}" size="20">
	 </div>
	 <div class="form-group"><label for="fiveBMenuItemCount" style="margin-left: 120px;">url：</label>
	       <input type="text" id="fiveBMenuItemCount" name="fiveBMenuItemCount" class="form-control validate[minSize[2],maxSize[300]]" value="${fiveBMenuItemCount}" size="80">
	 </div>
	       <input id="fiveBMenuItemId" name="fiveBMenuItemId" value="${fiveBMenuItemId}" type="hidden">
	       
	       <div class="form-group has-error"><label for="fiveBMenuItemOrder">是否开启：</label>
	          <select id="fiveBMenuItemOrder" name="fiveBMenuItemOrder" class="validate[required] form-control" style="width: 120px;">
				    <option value=""><fmt:message key="fxl.common.select" /></option>
				    <c:forEach var="yesNoItem" items="${yesNoMap}"> 
				    <option value="${yesNoItem.key}">${yesNoItem.value}</option>
				    </c:forEach>
			  </select>
	       </div>
	 <br/>
	 
	 <div class="form-group"><label for="fourBMenuItemName" style="margin-left: 80px;">子菜单名称：</label>
	       <input type="text" id="fourBMenuItemName" name="fourBMenuItemName" class="form-control validate[minSize[2],maxSize[300]]" value="${fourBMenuItemName}" size="20">
	 </div>
	 <div class="form-group"><label for="fourBMenuItemCount" style="margin-left: 120px;">url：</label>
	       <input type="text" id="fourBMenuItemCount" name="fourBMenuItemCount" class="form-control validate[minSize[2],maxSize[300]]" value="${fourBMenuItemCount}" size="80">
	 </div>
	       <input id="fourBMenuItemId" name="fourBMenuItemId" value="${fourBMenuItemId}" type="hidden">
	       
	       <div class="form-group has-error"><label for="fourBMenuItemOrder">是否开启：</label>
	          <select id="fourBMenuItemOrder" name="fourBMenuItemOrder" class="validate[required] form-control" style="width: 120px;">
				    <option value=""><fmt:message key="fxl.common.select" /></option>
				    <c:forEach var="yesNoItem" items="${yesNoMap}"> 
				    <option value="${yesNoItem.key}">${yesNoItem.value}</option>
				    </c:forEach>
			  </select>
	       </div>
	 <br/>
	 
	 <div class="form-group"><label for="threeBMenuItemName" style="margin-left: 80px;">子菜单名称：</label>
	       <input type="text" id="threeBMenuItemName" name="threeBMenuItemName" class="form-control validate[minSize[2],maxSize[300]]" value="${threeBMenuItemName}" size="20">
	 </div>
	 <div class="form-group"><label for="threeBMenuItemCount" style="margin-left: 120px;">url：</label>
	       <input type="text" id="threeBMenuItemCount" name="threeBMenuItemCount" class="form-control validate[minSize[2],maxSize[300]]" value="${threeBMenuItemCount}" size="80">
	 </div>
	       <input id="threeBMenuItemId" name="threeBMenuItemId" value="${threeBMenuItemId}" type="hidden">
	       
	       <div class="form-group has-error"><label for="threeBMenuItemOrder">是否开启：</label>
	          <select id="threeBMenuItemOrder" name="threeBMenuItemOrder" class="validate[required] form-control" style="width: 120px;">
				    <option value=""><fmt:message key="fxl.common.select" /></option>
				    <c:forEach var="yesNoItem" items="${yesNoMap}"> 
				    <option value="${yesNoItem.key}">${yesNoItem.value}</option>
				    </c:forEach>
			  </select>
	       </div>
	 <br/>

	 <div class="form-group"><label for="towBMenuItemName" style="margin-left: 80px;">子菜单名称：</label>
	       <input type="text" id="towBMenuItemName" name="towBMenuItemName" class="form-control validate[minSize[2],maxSize[300]]" value="${towBMenuItemName}" size="20">
	 </div>
	 <div class="form-group"><label for="towBMenuItemCK" style="margin-left: 120px;">Click：</label>
	       <input type="text" id="towBMenuItemCK" name="towBMenuItemCK" class="form-control validate[minSize[2],maxSize[300]]" value="${towBMenuItemCK}" size="80">
	 </div>
	       <input id="towBMenuItemId" name="towBMenuItemId" value="${towBMenuItemId}" type="hidden">
	       
	        <div class="form-group has-error"><label for="towBMenuItemOrder">是否开启：</label>
	          <select id="towBMenuItemOrder" name="towBMenuItemOrder" class="validate[required] form-control" style="width: 120px;">
				    <option value=""><fmt:message key="fxl.common.select" /></option>
				    <c:forEach var="yesNoItem" items="${yesNoMap}"> 
				    <option value="${yesNoItem.key}">${yesNoItem.value}</option>
				    </c:forEach>
			  </select>
	       </div>
	 <br/>
	 
	 <div class="form-group"><label for="oneBMenuItemName" style="margin-left: 80px;">子菜单名称：</label>
	       <input type="text" id="oneBMenuItemName" name="oneBMenuItemName" class="form-control validate[minSize[2],maxSize[300]]" value="${oneBMenuItemName}" size="20">
	 </div>
	 <div class="form-group"><label for="oneBMenuItemCK" style="margin-left: 120px;">Click：</label>
	       <input type="text" id="oneBMenuItemCK" name="oneBMenuItemCK" class="form-control validate[minSize[2],maxSize[300]]" value="${oneBMenuItemCK}" size="80">
	 </div>
	      <input id="oneBMenuItemId" name="oneBMenuItemId" value="${oneBMenuItemId}" type="hidden">
	      
	      <div class="form-group has-error"><label for="oneBMenuItemOrder">是否开启：</label>
	          <select id="oneBMenuItemOrder" name="oneBMenuItemOrder" class="validate[required] form-control" style="width: 120px;">
				    <option value=""><fmt:message key="fxl.common.select" /></option>
				    <c:forEach var="yesNoItem" items="${yesNoMap}"> 
				    <option value="${yesNoItem.key}">${yesNoItem.value}</option>
				    </c:forEach>
			  </select>
	       </div>
	 <br/>	 
	</div>
	
	<!-- 三级主菜单 -->
	<div class="panel panel-danger">
	<div class="panel-heading"><strong>微信底部菜单三</strong></div>
	 <div class="form-group has-error"><label for="threeMenu">三级菜单：</label>
	       <input type="text" id="threeMenu" name="threeMenu" class="form-control validate[required,minSize[2],maxSize[10]]" value="${threeMenuName}" size="20">
	 </div>
	      <input id="threeMenuId" name="threeMenuId" value="${threeMenuId}" type="hidden">
	 <br/>
	  
	 <!-- 以下为三级菜单子菜单分布 -->
	 <div class="form-group"><label for="fiveMenuItem" style="margin-left: 50px;">子菜单名称：</label>
	       <input type="text" id="fiveMenuItem" name="fiveMenuItem" class="form-control validate[minSize[2],maxSize[300]]" value="${fiveMenuItemName}" size="20">
	 </div>
	 <div class="form-group"><label for="fiveMenuItemContent" style="margin-left: 90px;">url：</label>
	       <input type="text" id="fiveMenuItemContent" name="fiveMenuItemContent" class="form-control validate[minSize[2],maxSize[300]]" value="${fiveMenuItemContent}" size="90">
	 </div>
	       <input id="fiveMenuItemId" name="fiveMenuItemId" value="${fiveMenuItemId}" type="hidden">
	       
	        <div class="form-group has-error"><label for="fiveMenuItemOrder">是否开启：</label>
	          <select id="fiveMenuItemOrder" name="fiveMenuItemOrder" class="validate[required] form-control" style="width: 120px;">
				    <option value=""><fmt:message key="fxl.common.select" /></option>
				    <c:forEach var="yesNoItem" items="${yesNoMap}"> 
				    <option value="${yesNoItem.key}">${yesNoItem.value}</option>
				    </c:forEach>
			  </select>
	       </div>
	 <br/>
	 
	 <div class="form-group"><label for="fourHMenuItem" style="margin-left: 50px;">子菜单名称：</label>
	       <input type="text" id="fourHMenuItem" name="fourHMenuItem" class="form-control validate[minSize[2],maxSize[300]]" value="${fourHMenuItemName}" size="20">
	 </div>
	 <div class="form-group"><label for="fourHMenuItemCount" style="margin-left: 90px;">url：</label>
	       <input type="text" id="fourHMenuItemCount" name="fourHMenuItemCount" class="form-control validate[minSize[2],maxSize[300]]" value="${fourHMenuItemCount}" size="90">
	 </div>
	       <input id="fourHMenuItemId" name="fourHMenuItemId" value="${fourHMenuItemId}" type="hidden">
	 
	       <div class="form-group has-error"><label for="fourHMenuItemOrder">是否开启：</label>
	          <select id="fourHMenuItemOrder" name="fourHMenuItemOrder" class="validate[required] form-control" style="width: 120px;">
				    <option value=""><fmt:message key="fxl.common.select" /></option>
				    <c:forEach var="yesNoItem" items="${yesNoMap}"> 
				    <option value="${yesNoItem.key}">${yesNoItem.value}</option>
				    </c:forEach>
			  </select>
	       </div>
	 <br/>
	 
	 <div class="form-group"><label for="threeMenuItem" style="margin-left: 50px;">子菜单名称：</label>
	       <input type="text" id="threeMenuItem" name="threeMenuItem" class="form-control validate[minSize[2],maxSize[300]]" value="${threeMenuItemName}" size="20">
	 </div>
	 <div class="form-group"><label for="threeMenuItemCount" style="margin-left: 90px;">url：</label>
	       <input type="text" id="threeMenuItemCount" name="threeMenuItemCount" class="form-control validate[minSize[2],maxSize[300]]" value="${threeMenuItemCount}" size="90">
	 </div>
	       <input id="threeMenuItemId" name="threeMenuItemId" value="${threeMenuItemId}" type="hidden">
	
	       <div class="form-group has-error"><label for="threeMenuItemOrder">是否开启：</label>
	          <select id="threeMenuItemOrder" name="threeMenuItemOrder" class="validate[required] form-control" style="width: 120px;">
				    <option value=""><fmt:message key="fxl.common.select" /></option>
				    <c:forEach var="yesNoItem" items="${yesNoMap}"> 
				    <option value="${yesNoItem.key}">${yesNoItem.value}</option>
				    </c:forEach>
			  </select>
	       </div>
	 <br/>
	 
	 <div class="form-group"><label for="towMenuItem" style="margin-left: 50px;">子菜单名称：</label>
	       <input type="text" id="towMenuItem" name="towMenuItem" class="form-control validate[minSize[2],maxSize[300]]" value="${towMenuItemName}" size="20">
	 </div>
	 <div class="form-group"><label for="towMenuItemCK" style="margin-left: 90px;">Click：</label>
	       <input type="text" id="towMenuItemCK" name="towMenuItemCK" class="form-control validate[minSize[2],maxSize[300]]" value="${towMenuItemCK}" size="90">
	 </div>
	       <input id="towMenuItemId" name="towMenuItemId" value="${towMenuItemId}" type="hidden">
	       
	       <div class="form-group has-error"><label for="towMenuItemOrder">是否开启：</label>
	          <select id="towMenuItemOrder" name="towMenuItemOrder" class="validate[required] form-control" style="width: 120px;">
				    <option value=""><fmt:message key="fxl.common.select" /></option>
				    <c:forEach var="yesNoItem" items="${yesNoMap}"> 
				    <option value="${yesNoItem.key}">${yesNoItem.value}</option>
				    </c:forEach>
			  </select>
	       </div>
	 <br/>
	 
	 <div class="form-group"><label for="oneMenuItem" style="margin-left: 50px;">子菜单名称：</label>
	       <input type="text" id="oneMenuItem" name="oneMenuItem" class="form-control validate[minSize[2],maxSize[300]]" value="${oneMenuItemName}" size="20">
	 </div>
	 <div class="form-group"><label for="oneMenuItemCK" style="margin-left: 90px;">Click：</label>
	       <input type="text" id="oneMenuItemCK" name="oneMenuItemCK" class="form-control validate[minSize[2],maxSize[300]]" value="${oneMenuItemCK}" size="90">
	 </div>
	       <input id="oneMenuItemId" name="oneMenuItemId" value="${oneMenuItemId}" type="hidden">
	       
	       <div class="form-group has-error"><label for="oneMenuItemOrder">是否开启：</label>
	          <select id="oneMenuItemOrder" name="oneMenuItemOrder" class="validate[required] form-control" style="width: 120px;">
				    <option value=""><fmt:message key="fxl.common.select" /></option>
				    <c:forEach var="yesNoItem" items="${yesNoMap}"> 
				    <option value="${yesNoItem.key}">${yesNoItem.value}</option>
				    </c:forEach>
			  </select>
	       </div>
	 <br/>
	  </div>
	  <p class="text-center">
		<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-arrow-right"></span> 保存菜单</button>
	  </p>
	</form>
		
	</div>
	<div class="panel-footer"><p class="text-right" style="margin: 0;"><em>零到壹，查找优惠</em></p></div>
</div>

</body>
</html>