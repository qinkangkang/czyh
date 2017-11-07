<%@ page contentType="text/html;charset=UTF-8"%>
<!doctype html>
<html>
<head>
<title>Welcome</title>
<%@ include file="/WEB-INF/views/include/include.jsp"%>
<link href="${ctx}/styles/ztree/css/zTreeStyle.css" rel="stylesheet"
	type="text/css">
<script type="text/javascript"
	src="${ctx}/styles/ztree/js/jquery.ztree.all.min.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		
		$.validationEngineLanguage.allRules.checkTMenuName={
				"url" : '${ctx}/fxl/menu/checkMenuName',
				"extraDataDynamic": ["#id"],
				"alertTextOk" : '您可以使用名称！',
			    "alertText" : '您输入的菜单名称已经存在，请更换其他菜单名称！',
			    "alertTextLoad" : '正在验证该该菜单名称是否被占用……'
			};
		
		var zTreeObj;
	    function addHoverDom(treeId, treeNode) {
     		var sObj = $("#" + treeNode.tId + "_span");
	        if (treeNode.editNameFlag || $("#addBtn_"+treeNode.tId).length>0) return;
	        var addStr='';
	        addStr += "<span class='button add' id='addBtn_" + treeNode.tId + "' title='新增下级菜单' onfocus='this.blur();'></span>";
	        addStr += "<span class='button edit' id='editBtn_" + treeNode.tId + "' title='修改'></span>";
	      	if (!treeNode.isParent) {
	        	 addStr += "<span class='button remove' id='removeBtn_" + treeNode.tId+ "' title='删除' onfocus='this.blur();'></span>";
	        }  
	        
	        sObj.after(addStr);//加载按钮
	        var addBtn = $("#addBtn_"+treeNode.tId);
	        //绑定添加事件，并定义添加操作  
	        if (addBtn) addBtn.bind("click", function(){
	        	$("#parent").val(treeNode.id);
	             createMenuModal.modal('show'); 
	       });  
	     
	      var editBtn = $("#editBtn_"+treeNode.tId);
	        if (editBtn) editBtn.bind("click", function(){
	        	$('#resetBtn').hide();
	        	var param= treeNode.id;
 	            $.post("${ctx}/fxl/menu/findMenuInfo/"+param,function(data){
 	            	if(data.success){
	 	            	$("#id").val(data.id);
	 	            	$("#name").val(data.name);
	 	            	$("#className").val(data.className);
	 	            	$("#url").val(data.url);
	 	            	$("#sn").val(data.sn);
	 	            	$("#description").val(data.description);
	 	            	$("#priority").val(data.priority);
	 	            	$("#parent").val(data.parentId);
	 	            	createMenuModal.modal('show');
 	            	}else{
 	            		toastr.error(data.msg);
 	            	}
 	            },'json');
 	           
	        });
	        var removeBtn = $("#removeBtn_"+treeNode.tId);
	        if (removeBtn) removeBtn.bind("click", function(){
	        	 if (confirm("请确认是否删除菜单-" + treeNode.name + "--吗?")) {
	 	            var param= treeNode.id;
	 	            $.post("${ctx}/fxl/menu/delMenu/"+param,function(data){
	 	            	return data;
	 	            },'json');
	 	          
	 	        } else {
	 	            return false;
	 	        }
	        });  
	    };
	    
		 function removeHoverDom(treeId, treeNode) {
		        $("#addBtn_"+treeNode.tId).unbind().remove();
		        $("#removeBtn_"+treeNode.tId).unbind().remove();
		        $("#editBtn_"+treeNode.tId).unbind().remove();
		    };
		    
		    
		    function initOrgTree(id){
		    	$.post("${ctx}/fxl/menu/treeData", function(data) {
					if(data.success){				
						if(zTreeObj){
							zTreeObj.destroy();
						}
						zTreeObj = $.fn.zTree.init($("#orgTree"), {
							check: {
								enable: true
							},
							view: {
					            addHoverDom: addHoverDom,
					            removeHoverDom: removeHoverDom,
					            dblClickExpand: false,
					            showLine: true,
					            selectedMulti: false
					        },
					        data: {
								simpleData: {
									enable:true,
									rootPId: ""
								}
							},
							callback: {
					           // beforeRename :beforeRename //重命名
					            
					        }
						}, data.tree);
						zTreeObj.expandAll(true);
					}else{
						toastr.error(data.msg);
					}
				}, "json");
		    }
	    
		
		
		
		//删除菜单
	/* 	function beforeRemove(treeId, treeNode) {
	        if (confirm("请确认是否删除菜单-" + treeNode.name + "--吗?")) {
	            var param= treeNode.id;
	            $.post("${ctx}/fxl/menu/delMenu/"+param,function(data){
	            	return data;
	            	initOrgTree();
	            },'json');
	            
	        } else {
	            return false;
	        }
	    } */
		
	/* 	//菜单重命名
		function beforeRename(treeId, treeNode, newName,isCancel) {
			var name=treeNode.name;
	        if (newName.length == 0) {
	            alert("菜单名称不能为空.");
	            return false;
	        }
	        var param = "id=" + treeNode.id + "&name=" + newName;
	        $.post("${ctx}/fxl/menu/changeName?"+ param,function(data){
            		return data;
	        },'json');
	        
	        return true;
	    } */
		
		var createMenuModal =  $('#createMenuModal');
		
		createMenuModal.on('hide.bs.modal', function(e){
			createMenuForm.trigger("reset");
		});
		
		var createMenuForm = $('form#createMenuForm');
		
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
				$.post(form.attr("action"), form.serialize(), function(data){
					if(data.success){
						toastr.success(data.msg);
						createMenuModal.modal('hide')
						 initOrgTree();
					}else{
						toastr.error(data.msg);
					}
					form.removeData("running");
				}, "json");
			}
		 });
	
		createMenuForm.on("reset", function(event){
			if (!event.isDefaultPrevented()) {
				event.preventDefault();
			}
			$(':input',createMenuForm).not(':button, :submit, :reset').val('').removeAttr('checked').removeAttr('selected');
			createMenuForm.validationEngine('hideAll');
		});
		
		$('#addMenuBtn').on('click',function(e) {
			createMenuModal.modal('show'); 
		});
		
		$(function(){
			 initOrgTree();
		})
});	
	
</script>
</head>
<body>
	<div class="row">
		<div class="col-md-5">
			<div class="panel panel-info">
				<div class="panel-heading">菜单模块</div>
				<div class="panel-body"  style="padding:0px;">
					<ul id="orgTree" class="ztree" ></ul>
				</div>
			</div>
		</div>
		<div class="col-md-7">
			<div class="panel panel-default">
			  <div class="panel-heading">菜单信息</div>
			  <div id="userToolbar123">
			       <form class="form-inline" role="form" id="userSearchForm22" style="padding:10px;">
				        <div class="btn-group" role="group" aria-label="...">
				       		 <button id="addMenuBtn" type="button" class="btn btn-primary" >
						         <i class="glyphicon glyphicon-plus"></i> <span class="hidden-xs">新增根菜单</span>
						     </button>
					     
					   </div>
				      
			      </form>
			   	</div>
			 
			</div>
		</div>
		<!--创建菜单modal开始-->
		<div class="modal fade" id="createMenuModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
		  <div class="modal-dialog modal-lg">
		    <div class="modal-content">
		      <div class="modal-header">
		        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
		        <h4 class="modal-title" id="myModalLabel">创建菜单</h4>
		      </div>
		      <form id="createMenuForm" action="${ctx}/fxl/menu/addMenu" method="post" role="form">
		      <div class="modal-body">
		      <div class="alert alert-danger text-center" role="alert" style="padding:5px;"><strong><fmt:message key="fxl.common.redRequired" /></strong></div>
		      	<div class="row">
					<div class="col-md-6">
						<input type="hidden" id="id" name="id">
						<div class="form-group has-error"><label for="name">菜单名称：</label>
					      	<input type="text" id="name" name="name" class="form-control validate[required,maxSize[30],ajax[checkTMenuName]]">
					    </div>
						<div class="form-group has-error"><label for="className">类名：</label>
					      	<input type="text" id="className" name="className" class="form-control validate[required,maxSize[80]]">
					    </div>
						<div class="form-group has-error"><label for="sn">权限标识：</label>
					      	<input type="text" id="sn" name="sn" class="form-control validate[required,maxSize[30]]">
					    </div>
						<div class="form-group has-error"><label for="url">链接地址：</label>
					      	<input type="text" id="url" name="url" class="form-control validate[required,maxSize[30]]">
					    </div>
						<div class="form-group has-error"><label for="priority">优先级：</label>
					      	<input type="text" id="priority" name="priority" class="form-control validate[required,maxSize[30]]">
					    </div>
					    <div class="form-group"><label for="description">描述：</label>
					    	<textarea id="description" name="description" cols="60" rows="4" class="form-control validate[maxSize[200]]"></textarea>
					    </div>
					</div>
					<div class="col-md-6">
						<div class="form-group has-error"><label for="parent.id">上级菜单：</label>
					      <input type="text" id="parent" name="parent.id"  readOnly="true" class="form-control">
					    </div>
					</div>
				</div>
		      </div>
		      <div class="modal-footer">
		      	<button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-floppy-saved"></span> <fmt:message key="fxl.button.save" /></button>
				<button class="btn btn-warning" type="reset" id="resetBtn"><span class="glyphicon glyphicon-repeat"></span> <fmt:message key="fxl.button.reset" /></button>
		        <button class="btn btn-default" type="button" data-dismiss="modal"><span class="glyphicon glyphicon-remove"></span> <fmt:message key="fxl.button.close" /></button>
		      </div>
		      </form>
		    </div>
		  </div>
		</div>
		<!--创建菜单Modal结束-->
	</div>
</body>
</html>