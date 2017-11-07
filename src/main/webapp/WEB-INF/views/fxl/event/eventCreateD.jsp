<%@ page contentType="text/html;charset=UTF-8" %>
<meta name="decorator" content="/WEB-INF/decorators/decorator_no_theme.jsp">
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<script type="text/javascript">
$(document).ready(function() {

	var ue;	
	if($.isPlainObject(ue)){
		ue.destroy();
	}else{
		ue = UE.getEditor('container',{
			autoHeightEnabled : true,
			autoFloatEnabled : true,
			zIndex : 1029,
			topOffset : 51
		});
	}
	
	$("#saveBtn").on("click", function(event){
		var btn = $(this);
		if(btn.data("running") != "ok"){
			btn.data("running","ok");
			$.post("${ctx}/fxl/event/addEventD", $.param({eventId:$("#eventId").val(),fdetail:ue.getContent()},true), function(data){
				if(data.success){
					toastr.success(data.msg);
					$('#eventTab a:eq(4)').tab('show');
				}else{
					toastr.error(data.msg);
				}
				btn.removeData("running");
			}, "json");
		}
	});
	
	$("#topBtn").click(function(e) {
		$('html, body').animate({scrollTop:0}, 'slow');
    });
});
</script>
<div class="row">
	<div class="col-md-7 col-md-offset-1" role="main">
	    <div class="form-group"><label>活动详细图文介绍：</label>
	        <!-- 加载编辑器的容器 -->
			<div class="well well-sm" style="width: 620px;"><script id="container" name="fdetail" type="text/plain" style="width: 600px;height: 500px;">${fdetail}</script></div>
	    </div>
    </div>
	<div class="col-md-4">
		<div class="affix">
		<!-- <button class="btn btn-success" type="button"><span class="glyphicon glyphicon-eye-open"></span> 预览</button><br/><br/><br/> -->
		<button id="topBtn" class="btn btn-info" type="button"><span class="glyphicon glyphicon-arrow-up"></span> 顶部</button><br/><br/><br/>
		<button class="btn btn-primary" type="button" id="saveBtn"><span class="glyphicon glyphicon-floppy-saved"></span> 保存并下一步</button>
		</div>
	</div>
</div>