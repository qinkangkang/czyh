<%@ page contentType="text/html;charset=UTF-8" %>
<meta name="decorator" content="/WEB-INF/decorators/decorator_no_theme.jsp">
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<script type="text/javascript">
$(document).ready(function() {
	
	$("#ftypeA").change(function(e){
		var ftypeB = $("#ftypeB");
		ftypeB.children("option[value!='']").remove();
		var select = $(this);   
		if(select.val()!=""){		
			$.post("${ctx}/fxl/event/getCategoryMapB", $.param({ftypeA:select.val()},true) ,function(data) {
				if(data.success){
					var ftypeBList = data.ftypeBList;
					$.each(ftypeBList, function(i, n){
						ftypeB.append("<option value='" + n.key + "'>" + n.value + "</option>");
					});
				}
			}, "json");
		}
    });
	
	var createEventAForm = $('#createEventAForm');
	
	createEventAForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	createEventAForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			$.post(form.attr("action"), $.param($.merge(form.serializeArray(),[{name:"eventId", value:$("#eventId").val()}]),true), function(data){
				if(data.success){
					$("#eventId").val(data.eventId);
					toastr.success(data.msg);
					$('#eventTab a:eq(1)').tab('show');
				}else{
					toastr.error(data.msg);
				}
				form.removeData("running");
			}, "json");
		}
	});
	
	$("#ftypeA option[value='${ftypeA}']").prop("selected",true);
	$("#ftypeB option[value='${ftypeB}']").prop("selected",true);
	
	$("#ftypeA").select2({
		placeholder: "请选择一个商品一级类目",
		allowClear: true,
		language: pickerLocal
	});
	
	$("#ftypeB").select2({
		placeholder: "请选择一个商品二级类目",
		allowClear: true,
		language: pickerLocal
	});
});
</script>
<div class="text-center">
<form id="createEventAForm" action="${ctx}/fxl/event/addEventA" method="post" class="form-inline" role="form">
    <div class="form-group has-error"><label for="ftypeA">商品一级类目：</label>
		<select id="ftypeA" name="ftypeA" class="validate[required] form-control" style="width: 200px;">
			<option value=""><fmt:message key="fxl.common.select" /></option>
			<c:forEach var="categoryItem" items="${categoryMapA}"> 
			<option value="${categoryItem.key}">${categoryItem.value}</option>
			</c:forEach>
		</select>
	</div>
	<div class="form-group has-error"><label for="ftypeB">商品二级类目：</label>
        <select id="ftypeB" name="ftypeB" class="validate[required] form-control" style="width: 200px;">
			<option value=""><fmt:message key="fxl.common.select" /></option>
			<c:forEach var="categoryItem" items="${categoryMapB}"> 
			<option value="${categoryItem.key}">${categoryItem.value}</option>
			</c:forEach>
		</select>
	</div>
	<br/>
	<p class="text-center">
	<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-arrow-right"></span> 保存并下一步</button>
	</p>
</form>
</div>