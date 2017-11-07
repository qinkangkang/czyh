<%@ page contentType="text/html;charset=UTF-8" %>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<style>
.table-list input{border:1px solid #E9EBEC;border-radius: 0px;}
.coupon-tip{font-size: 20px;font-weight:bold;border-left: 3px solid #EFC428;padding-left:5px;}
.coupon-new{background-color: #fff;padding:20px 10px;}
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

	function show(){
		radioList = $("input[id=userList]:checked");
	    if (radioList.length>0){
	        document.getElementById("phoneList").style.display = "block";
	    }else{
	        document.getElementById("phoneList").style.display = "none";
	    }
	}
	
	function showpush(){
		radioList = $("input[id=is_push]:checked");
	    if (!radioList.length>0){
	        document.getElementById("pushcontent").style.display = "block";
	    }else{
	        document.getElementById("pushcontent").style.display = "none";
	    }
	}

$(document).ready(function() {
	
	
	$('#ckeckphoneList').click(function(){
		var fphone=$('#fphone').val();
		if(null==fphone||fphone==""){
			toastr.warning("请先输入客户手机号，再发放优惠券");
            return;
        }

        var strnew=fphone.replace(/^[0-9,]+$/,"");
        if(null!=strnew && strnew!=""){
        	toastr.warning("只能输入数字和英文逗号,请重新输入");
            return;
        }
        
        var phoneArray = fphone.split(",");
        var params = {};
        params['fphone'] = fphone;
        $.post("${ctx}/fxl/coupon/checkPhoneList",params, function(data) {
			if(data.success){
				toastr.success(data.msg);
			}else{
				toastr.error(data.msg);
			}
		}, "json");
	});
	
	var savedeliveryForm = $('#savedeliveryForm');
		
	savedeliveryForm.validationEngine({
		maxErrorsPerField: 1,
	    autoPositionUpdate: true,
	    scroll: false,
	    showOneMessage: true
	});
	
	savedeliveryForm.on("submit", function(event){
		if (!event.isDefaultPrevented()) {
			event.preventDefault();
		}
		var form = $(this);
		if(form.validationEngine("validate") && form.data("running") != "ok"){
			form.data("running","ok");
			
			$.post(form.attr("action"), $.param($.merge(form.serializeArray(),[{name:"couponinfo", value:$('#couponInfo').val()},{name:"fActivityType", value:10}]),true), function(data){
				if(data.success){
					toastr.success(data.msg);
					window.location.href = "${ctx}/fxl/coupon/deliverylist";
				}else{
					toastr.error(data.msg);
				}
				form.removeData("running");
			}, "json");
		}
		
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
	
	
	<div class="">
		<div class="col-md-10">
			<span class="coupon-tip">添加活动信息</span>
			<form id="savedeliveryForm" action="${ctx}/fxl/coupon/savedelivery" method="post" class="form-inline" role="form">
				<input id="couponInfo" type="hidden" value='${couponInfoMap}'>
				<input id="fActivityType" type="hidden" value='10'>
				<div class="modal-body">
					<%-- <div class="alert alert-danger text-center" role="alert" style="padding: 5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
					 --%><div class="form-group has-error"><label for="ftitle">活动名称：</label>
						<input type="text" id="ftitle" name="ftitle" class="form-control validate[required,minSize[2],maxSize[250]]" size="100">
					</div>
					<div class="form-group" style="min-height: 110px;"><label for="fdescription">发放说明：</label>
				        <textarea id="fdescription" name="fdescription" cols="100%" rows="5" class="validate[maxSize[250]] form-control"></textarea>
				    </div>
				    <div class="form-group" style="min-height: 110px;"><label for="fnotice">提示信息：</label>
				        <textarea id="fnotice" name="fnotice" cols="100%" rows="5" class="validate[maxSize[250]] form-control"></textarea>
				    </div>
				    <div class="form-group has-error"><label for="fdeliverType">推送：</label>
				    	<input class="form-control input-sm" type="radio" name="is_push" value="10" onclick="showpush()" id="is_push" checked ="checked"><label>不需推送</label>
						<input class="form-control input-sm" type="radio" name="is_push" value="20" onclick="showpush()"><label>短信推送</label>
						<input class="form-control input-sm" type="radio" name="is_push" value="30" onclick="showpush()"><label>push和微信推送</label>
					</div><br>
				    <div class="form-group" style="min-height: 110px;display: none;" id="pushcontent"><label for="fnotice">推送内容：</label>
				        <textarea id="fpush" name="fpush" cols="100%" rows="5" class="validate[maxSize[250]] form-control"></textarea>
				    </div>
					<div class="form-group has-error"><label for="fdeliverType">优惠券发放形式：</label>
						<c:forEach var="fDeliverTypeMap" items="${fDeliverTypeMap}">
							<c:choose>
							   	<c:when test="${fDeliverTypeMap.key==10}">
							   		<input class="form-control input-sm" type="radio" name="fDeliverType" value="${fDeliverTypeMap.key}" checked = "checked" onclick="show()"><label>${fDeliverTypeMap.value}</label>
								</c:when>
						   		<c:when test="${fDeliverTypeMap.key==30}">
							   		<input class="form-control input-sm" type="radio" name="fDeliverType" value="${fDeliverTypeMap.key}" onclick="show()"><label>${fDeliverTypeMap.value}</label>
								</c:when>
						   		<c:otherwise>
						   			<input class="form-control input-sm" type="radio" name="fDeliverType" value="${fDeliverTypeMap.key}" onclick="show()" id="userList"><label>${fDeliverTypeMap.value}</label>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</div>
				    <div class="form-group" style="min-height: 110px;display: none;" id="phoneList"><label for="fnotice">用户列表：</label>
				        <textarea id="fphone" name="fphone" cols="100%" rows="5" class="form-control"></textarea><br>
				        <div class="input-group input-large" style="margin-top: 30px;">
							<a class="btn btn-primary" id = "ckeckphoneList">校验用户列表</a>
						</div>
				    </div>
					<div style="clear: both;"></div>
				</div>
				
				<div class="input-group input-large" style="margin-top: 30px;margin-bottom: 50px;">
					<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span><fmt:message key="fxl.button.save" /></button>
				</div>
				
			</form>
		</div>
	</div>
	
</div>

</body>
</html>