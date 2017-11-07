package com.czyh.czyhweb.web.menu;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.protocol.HTTP;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContext;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;
import org.springside.modules.web.Servlets;

import com.czyh.czyhweb.entity.TEventSpec;
import com.czyh.czyhweb.entity.TModule;
import com.czyh.czyhweb.entity.TRole;
import com.czyh.czyhweb.log.LogMessageObject;
import com.czyh.czyhweb.log.impl.LogUitls;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.customer.UserService;
import com.czyh.czyhweb.service.menu.MenuService;
import com.czyh.czyhweb.util.CommonPage;
import com.fasterxml.jackson.databind.Module;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.squareup.okhttp.Request;

@Controller
@RequestMapping("/fxl/menu")
public class MenuController {

	private static Logger logger = LoggerFactory.getLogger(MenuController.class);
	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private MenuService menuService;

	/**
	 * 跳转到菜单列表
	 * 
	 * @author maxiao
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/toList", method = { RequestMethod.GET, RequestMethod.POST })
	public String toList(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		return "fxl/menu/menuManage";
	}

	/**
	 * 获取菜单树数据的方法
	 * 
	 * @author maxiao
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/treeData", method = RequestMethod.POST)
	public String getMenuList(HttpServletRequest request, HttpServletResponse response) {
		// 将request对象中的请求URL中的参数都放在Map中
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// 操作信息的map，将map通过json方式返回给页面
			List<Map<String, Object>> treeList = Lists.newArrayList();
			Map<String, Object> treeMap = null;
			List<TModule> list = menuService.findList();
			for (TModule module : list) {
				treeMap = Maps.newHashMap();
				treeMap.put("id", module.getId());
				if (module.getParent() == null) {
					treeMap.put("pId", "");
					treeMap.put("icon", request.getContextPath() + "/styles/ztree/css/img/diy/1_open.png");
					treeMap.put("nocheck", "true");
				} else {
					treeMap.put("pId", module.getParent().getId().toString());
				}
				treeMap.put("name", module.getName());
				treeList.add(treeMap);
			}
			returnMap.put("tree", treeList);
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取系统模块树形信息时出错！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 根据ID返回菜单详细信息
	 * 
	 * @param request
	 * @return
	 */

	@ResponseBody
	@RequestMapping(value = "/getMenuInfo/{specId}", method = RequestMethod.POST)
	public String getMenuInfo(HttpServletRequest request, HttpServletResponse response, @PathVariable Long specId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			TModule module = menuService.getMenuInfo(specId);
			returnMap.put("id", module.getId());
			returnMap.put("className", module.getClassName());
			returnMap.put("name", module.getName());
			returnMap.put("sn", module.getSn());
			returnMap.put("url", module.getUrl());
			returnMap.put("description", module.getDescription());
			returnMap.put("priority", module.getPriority());
			returnMap.put("parent", module.getParent());
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取菜单详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 新增菜单
	 * 
	 * @param request
	 * @param response
	 * @return
	 */

	@ResponseBody
	@RequestMapping(value = "/addMenu", method = RequestMethod.POST)
	public String addMenu(HttpServletRequest request, HttpServletResponse response, TModule module) {

		RequestContext requestContext = new RequestContext(request);
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		boolean isAdd = false;
		if (module != null && module.getId() == null) {
			isAdd = true;
		}
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			if (isAdd) {
				menuService.saveMenu(map);
			} else {
				menuService.updateMenu(map);
			}
			returnMap.put("success", true);
			if (isAdd) {
				returnMap.put("msg", requestContext.getMessage("新增菜单成功"));
			} else {
				returnMap.put("msg", requestContext.getMessage("编辑菜单成功"));
			}

		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "创建菜单失败!");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 修改菜单：修改后保存
	 * 
	 * @author maxiao
	 * @param request
	 * @param response
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/editMenu", method = RequestMethod.POST)
	public String editMenu(HttpServletRequest request, HttpServletResponse response, @RequestParam String id) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			menuService.editModule(map);
			returnMap.put("msg", "编辑菜单信息成功!");
			returnMap.put("success", true);
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑菜单信息失败!");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/delMenu/{moduleId}", method = RequestMethod.POST)
	public String delMenu(HttpServletRequest request, HttpServletResponse response, @PathVariable Long moduleId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			// TModule module = menuService.getMenuInfo(Long.valueOf(moduleId));
			menuService.deleteModuleById(moduleId);
			returnMap.put("success", true);
			returnMap.put("msg", "菜單删除成功！");

		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "菜单删除失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 菜单回显
	 * 
	 * @param request
	 * @param response
	 * @param module
	 * @param menuId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/findMenuInfo/{moduleId}", method = RequestMethod.POST)
	public String findMenuInfo(HttpServletRequest request, HttpServletResponse response, @PathVariable Long moduleId) {
		// 操作信息的map,将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			TModule tModule = menuService.findMenuInfo(moduleId);
			returnMap.put("id", tModule.getId());
			returnMap.put("name", tModule.getName());
			returnMap.put("className", tModule.getClassName());
			returnMap.put("url", tModule.getUrl());
			returnMap.put("sn", tModule.getSn());
			returnMap.put("priority", tModule.getPriority());
			returnMap.put("description", tModule.getDescription());
			if (null != tModule.getParent()) {
				returnMap.put("parentId", tModule.getParent().getId());
			}
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取菜单信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/changeName", method = RequestMethod.POST)
	public String changeName(HttpServletRequest request, HttpServletResponse response, @RequestParam String id,
			@RequestParam String name) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			menuService.editModuleName(map);
			returnMap.put("msg", "编辑菜单成功!");
			returnMap.put("success", true);
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑菜单名称失败!");
		}
		return mapper.toJson(returnMap);
	}
	
	
	
	@ResponseBody
	@RequestMapping(value = "/checkMenuName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkMenuName(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue,
			@RequestParam(required = false, name = "fid") Long id) {
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (menuService.countByMenuName(fieldValue.trim(), id).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

}