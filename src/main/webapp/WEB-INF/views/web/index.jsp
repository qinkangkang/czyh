<%@ page contentType="text/html;charset=UTF-8"%>
<!doctype html>
<html>
<head>
<title>零到壹，查找优惠</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<script type="text/javascript">
$(document).ready(function() {
	if ($("#username").val() == "" && $.trim($.cookie("fxlUsername")) != "") {
		$("#username").val($.cookie("fxlUsername"));
		$("#password").focus();
	} else {
		$("#username").focus();
	}
});
</script>

</head>
<body  >
<div class="row"  align="center" >
	<div style="margin-bottom: 1%;"  ><img alt="" src="${ctx}/styles/fxl/images/logo-md1.png"  height="5%" width="8% "></div>
	<div class="title" style="font-size: 30px;color: #FFFFFF;letter-spacing: 0;margin-bottom:2%; ">查找优惠运营管理系统</div>
	<div style="width:37%;  background-color: #FFF; padding: 2.5% 4.5% 6.5% 4.5%; " >
		<form id="loginForm" role="form" action="${ctx}/fxl/login" method="post" class="form">
			<div class="form-group " style="font-size: 16px;color: #B6B6B6; ">
				<input type="text" id="username" name="username"  class="form-control phone-input" value="${username}" check-type="required" 
				style="background:url('${ctx}/styles/fxl/images/username.png') 5px;background-repeat:no-repeat;padding-left:30px;vertical-align:center;"		placeholder="用户名"	 required-message='<fmt:message key="fxl.login.input.account" />' />
			</div>
			<div class="form-group" style="font-size: 16px;color: #B6B6B6; margin-top: 1.6%;">
				<input type="password" id="password" name="password" class="form-control" check-type="required" 
						style="background:url('${ctx}/styles/fxl/images/password.png') 5px;background-repeat:no-repeat;padding-left:30px;vertical-align:center;"	placeholder="密码"	required-message='<fmt:message key="fxl.login.input.password" />' />
			</div>
			<div style="font-family: PingFangSC-Semibold;" >
				<div class="checkbox" style="float: left;margin-left: 8%;">
					<label> <input type="checkbox" id="rememberMe" name="rememberMe" /> 记住密码</label>
				</div>
				
				<div style="float: right; margin-right: 8%; font-size: 16px;">
					<button type="reset" style="border-style: none; background-color: #fff;margin-top: 9px; color:#5A8EFA;">重置</button>
				</div>
			</div><br>
			
			<c:if test="${msg!=null}">
				<div class="alert alert-danger alert-dismissable" style="margin-top: 4%;">
					<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
					${msg}
				</div>
			</c:if>
			
			<br><br>
			<div >
					<button type="submit" style=" width:100%;background-color: #5A8EFA;font-size: 22px;color: #FFFFFF;" >登录</button>
		    </div>
		</form>
	</div>
	
	<!-- <ul>
		<li><p>为了正确的显示和运行本应用的功能，建议您采用Chrome、360极速浏览器来浏览。</p></li>
		<li><p>为了更好的展现网站的各项内容，建议您采用1024×768以上的分辨率来浏览。</p></li>
		<li><p>当本应用使用完毕后，请点击页面右上角的“退出”按钮来退出应用。</p></li>
	</ul> -->
	
</div>
</body>
</html>