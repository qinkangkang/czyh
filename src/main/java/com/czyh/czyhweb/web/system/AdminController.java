package com.czyh.czyhweb.web.system;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.HttpException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContext;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;
import org.springside.modules.web.Servlets;

import com.czyh.czyhweb.dao.AppErrorDAO;
import com.czyh.czyhweb.dto.ResponseDTO;
import com.czyh.czyhweb.entity.TAppError;
import com.czyh.czyhweb.entity.TDictionary;
import com.czyh.czyhweb.entity.TDictionaryClass;
import com.czyh.czyhweb.entity.TModule;
import com.czyh.czyhweb.entity.TRole;
import com.czyh.czyhweb.entity.TUser;
import com.czyh.czyhweb.exception.ExistedException;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.log.Log;
import com.czyh.czyhweb.log.LogMessageObject;
import com.czyh.czyhweb.log.impl.LogUitls;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.ConfigurationService;
import com.czyh.czyhweb.service.customer.CustomerService;
import com.czyh.czyhweb.service.customer.UserService;
import com.czyh.czyhweb.service.goods.GoodsService;
import com.czyh.czyhweb.util.ArrayStringUtils;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.FileUtils;
import com.czyh.czyhweb.util.HttpClientUtil;
import com.czyh.czyhweb.util.ImageUtil;
import com.czyh.czyhweb.util.PropertiesUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 管理员的web处理类
 * 
 * @author qkk
 * 
 */
@Controller
@RequestMapping("/fxl/admin")
public class AdminController {

	private static Logger logger = LoggerFactory.getLogger(AdminController.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private UserService userService;

	@Autowired
	private GoodsService goodsService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private AppErrorDAO appErrorDAO;

	@RequiresPermissions(value = "user")
	@RequestMapping(value = "/user/main", method = RequestMethod.GET)
	public String userMain(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<Integer, String> statusMap = DictionaryUtil.getStatueMap(DictionaryUtil.UserStatus,
				shiroUser.getLanguage());
		Map<Integer, String> statusTempMap = org.apache.commons.lang3.ObjectUtils.clone(statusMap);
		statusTempMap.remove(999);
		model.addAttribute("statusMap", statusTempMap);
		model.addAttribute("categoryMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.UserCategory, shiroUser.getLanguage()));
		return "fxl/admin/user";
	}

	/**
	 * 获取用户列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/user/getUserList", method = RequestMethod.POST)
	public String getUserList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		userService.getUserList(map, page);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	/**
	 * 创建用户时AJAX验证登录名是否存在的方法。
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/user/checkUsername", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkUsername(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue) {
		// try {
		// fieldValue =
		// StringUtils.toEncodedString(fieldValue.getBytes("ISO-8859-1"),
		// Charset.forName("UTF-8"));
		// } catch (UnsupportedEncodingException e) {
		// logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		// }
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (userService.checkUsername(fieldValue.trim()).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 修改用户时AJAX验证登录名是否存在的方法。
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/user/checkEditUsername", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkEditUsername(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue,
			@RequestParam(name = "id", required = false, defaultValue = "0") String id) {
		// try {
		// fieldValue =
		// StringUtils.toEncodedString(fieldValue.getBytes("ISO-8859-1"),
		// Charset.forName("UTF-8"));
		// } catch (UnsupportedEncodingException e) {
		// logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		// }
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (userService.checkEditUsername(fieldValue.trim(), id).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 创建用户时AJAX验证手机号码是否存在的方法。
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/user/checkPhone", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkPhone(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue) {
		// try {
		// fieldValue =
		// StringUtils.toEncodedString(fieldValue.getBytes("ISO-8859-1"),
		// Charset.forName("UTF-8"));
		// } catch (UnsupportedEncodingException e) {
		// logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		// }
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (userService.checkPhone(fieldValue.trim()).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 创建用户时AJAX验证邮箱地址是否存在的方法。
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/user/checkEmail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkEmail(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue) {
		// try {
		// fieldValue =
		// StringUtils.toEncodedString(fieldValue.getBytes("ISO-8859-1"),
		// Charset.forName("UTF-8"));
		// } catch (UnsupportedEncodingException e) {
		// logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		// }
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (userService.checkEmail(fieldValue.trim()).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 创建用户时AJAX验证邮箱地址是否存在的方法。
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/user/checkEditEmail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkEditEmail(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue,
			@RequestParam(name = "edit_id", required = false, defaultValue = "") String id) {
		// try {
		// fieldValue =
		// StringUtils.toEncodedString(fieldValue.getBytes("ISO-8859-1"),
		// Charset.forName("UTF-8"));
		// } catch (UnsupportedEncodingException e) {
		// logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		// }
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (userService.checkEditEmail(fieldValue.trim(), id).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 创建用户
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@Log(message = "添加了“{0}”运维人员。", module = 11)
	@ResponseBody
	@RequestMapping(value = "/user/addUser", method = RequestMethod.POST)
	public String addUser(HttpServletRequest request, HttpServletResponse response, @Valid TUser user) {
		RequestContext requestContext = new RequestContext(request);
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			if (userService.getByUsername(user.getUsername()) != null) {
				throw new ExistedException();
			}
			String[] ids = null;
			if (!map.containsKey("ids")) {
				ids = ArrayUtils.EMPTY_STRING_ARRAY;
			} else if (map.containsKey("ids") && ObjectUtils.isArray(map.get("ids"))) {
				ids = (String[]) map.get("ids");
			} else {
				ids = ArrayUtils.toArray((map.get("ids").toString()));
			}
			if (map.containsKey("category") && StringUtils.isNotBlank(map.get("category").toString())) {
				String[] ids2 = ArrayUtils.EMPTY_STRING_ARRAY;
				if (map.containsKey("category") && ObjectUtils.isArray(map.get("category"))) {
					ids2 = (String[]) map.get("category");
				} else {
					ids2 = ArrayUtils.toArray((map.get("category").toString()));
				}
				String tagInfo = ArrayStringUtils.arrayToString(ids2, ArrayStringUtils.Separator);
				user.setCategory(tagInfo);
			} else {
				user.setCategory(null);
			}

			user.setCreateTime(new Date());
			userService.save(user, ids);
			returnMap.put("success", true);
			returnMap.put("msg", requestContext.getMessage("fxl.admin.user.addUser.success"));
		} catch (ExistedException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", requestContext.getMessage("fxl.admin.user.addUser.existed.fail", user.getUsername()));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", requestContext.getMessage("fxl.admin.user.addUser.fail"));
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { user.getRealname() }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 编辑用户信息的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@Log(message = "修改了“{0}”运维人员。", module = 11)
	@ResponseBody
	@RequestMapping(value = "/user/editUser", method = RequestMethod.POST)
	public String editUser(HttpServletRequest request, HttpServletResponse response) {
		RequestContext requestContext = new RequestContext(request);

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		String realname = null;
		try {
			String[] ids = null;
			if (!map.containsKey("ids")) {
				ids = ArrayUtils.EMPTY_STRING_ARRAY;
			} else if (map.containsKey("ids") && ObjectUtils.isArray(map.get("ids"))) {
				ids = (String[]) map.get("ids");
			} else {
				ids = ArrayUtils.toArray((map.get("ids").toString()));
			}

			TUser user = userService.get(Long.valueOf(map.get("id").toString()));
			if (map.containsKey("realname") && StringUtils.isNotBlank(map.get("realname").toString())) {
				user.setRealname(map.get("realname").toString());
			}
			if (map.containsKey("email") && StringUtils.isNotBlank(map.get("email").toString())) {
				user.setEmail(map.get("email").toString());
			}
			if (map.containsKey("phone") && StringUtils.isNotBlank(map.get("phone").toString())) {
				user.setPhone(map.get("phone").toString());
			}
			if (map.containsKey("status") && StringUtils.isNotBlank(map.get("status").toString())) {
				user.setStatus(Integer.valueOf(map.get("status").toString()));
			}
			if (map.containsKey("category") && StringUtils.isNotBlank(map.get("category").toString())) {
				String[] ids2 = ArrayUtils.EMPTY_STRING_ARRAY;
				if (map.containsKey("category") && ObjectUtils.isArray(map.get("category"))) {
					ids2 = (String[]) map.get("category");
				} else {
					ids2 = ArrayUtils.toArray((map.get("category").toString()));
				}
				String categoryInfo = ArrayStringUtils.arrayToString(ids2, ArrayStringUtils.Separator);
				user.setCategory(categoryInfo);
			} else {
				user.setCategory(null);
			}
			realname = user.getRealname();
			userService.edit(user, ids);
			returnMap.put("msg", requestContext.getMessage("fxl.admin.user.editUser.success"));
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", requestContext.getMessage("fxl.admin.user.editUser.fail"));
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { realname }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 删除用户信息的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@Log(message = "删除了“{0}”运维人员。", module = 11)
	@ResponseBody
	@RequestMapping(value = "/user/delUser/{userId}", method = RequestMethod.POST)
	public String delUser(HttpServletRequest request, HttpServletResponse response, @PathVariable Long userId) {
		RequestContext requestContext = new RequestContext(request);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String realname = null;
		try {
			realname = userService.delete(userId);
			returnMap.put("msg", requestContext.getMessage("fxl.admin.user.delUser.success"));
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", requestContext.getMessage("fxl.admin.user.delUser.fail"));
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { realname }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 获取用户的方法
	 * 
	 * @param request
	 * @param model
	 * @param customerId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/user/getUser/{userId}", method = RequestMethod.POST)
	public String getUser(HttpServletRequest request, Model model, @PathVariable Long userId) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			TUser user = userService.get(userId);
			returnMap.put("username", user.getUsername());
			returnMap.put("realname", user.getRealname());
			returnMap.put("phone", user.getPhone());
			returnMap.put("email", user.getEmail());
			returnMap.put("status", user.getStatus());
			returnMap.put("category", user.getCategory());

			// 操作信息的map，将map通过json方式返回给页面
			List<Map<String, Object>> treeList = Lists.newArrayList();
			Map<String, Object> treeMap = Maps.newHashMap();
			treeMap.put("id", "0");
			treeMap.put("pId", "");
			treeMap.put("name", "fxl");
			treeMap.put("icon", request.getContextPath() + "/styles/ztree/css/img/diy/1_open.png");
			treeMap.put("nocheck", "true");
			treeList.add(treeMap);

			Set<TRole> rs = user.getRoles();

			List<Long> rsIds = Lists.newArrayList();
			for (TRole role : rs) {
				rsIds.add(role.getId());
			}

			List<TRole> list = userService.getAllRole();
			for (TRole role : list) {
				treeMap = Maps.newHashMap();
				treeMap.put("id", role.getId());
				treeMap.put("pId", "0");
				treeMap.put("name", role.getName());
				if (rsIds.contains(role.getId())) {
					treeMap.put("checked", "true");
				}
				treeList.add(treeMap);
			}
			returnMap.put("tree", treeList);
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取用户信息时出错！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 机构管理员重置子会员密码为“111111”
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/user/resetMemberPassword/{userId}", method = RequestMethod.POST)
	public String resetMemberPassword(HttpServletRequest request, Model model, @PathVariable Long userId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			userService.updatePassword(userId, "111111");
			returnMap.put("success", true);
			returnMap.put("msg", "当前该用户的密码重置为“111111”，请尽快登录本系统并修改初始密码！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "密码重置失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 获取角色信息的方法，返回的json是ztree格式的数据
	 * 
	 * @param request
	 * @param model
	 * @param customerId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/user/getRoleTree", method = RequestMethod.POST)
	public String getRoleTree(HttpServletRequest request, Model model) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			// 操作信息的map，将map通过json方式返回给页面
			List<Map<String, Object>> treeList = Lists.newArrayList();
			Map<String, Object> treeMap = Maps.newHashMap();
			treeMap.put("id", "0");
			treeMap.put("pId", "");
			treeMap.put("name", "FangXueLe");
			treeMap.put("icon", request.getContextPath() + "/styles/ztree/css/img/diy/1_open.png");
			treeMap.put("nocheck", "true");
			treeList.add(treeMap);
			List<TRole> list = userService.getAllRole();
			for (TRole role : list) {
				treeMap = Maps.newHashMap();
				treeMap.put("id", role.getId());
				treeMap.put("pId", "0");
				treeMap.put("name", role.getName());
				treeList.add(treeMap);
			}
			returnMap.put("tree", treeList);
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取系统角色树形信息时出错！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 进入角色管理页面的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@RequiresPermissions(value = "role")
	@RequestMapping(value = "/role/main", method = RequestMethod.GET)
	public String roleMain(HttpServletRequest request, Model model) {
		return "fxl/admin/role";
	}

	/**
	 * 创建角色时AJAX验证角色编码是否存在的方法。
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/role/checkRolecode", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkRolecode(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue) {
		// try {
		// fieldValue =
		// StringUtils.toEncodedString(fieldValue.getBytes("ISO-8859-1"),
		// Charset.forName("UTF-8"));
		// } catch (UnsupportedEncodingException e) {
		// logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		// }
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (userService.checkRolecode(fieldValue.trim()).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 编辑角色时AJAX验证角色编码是否存在的方法。
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/role/checkEditRolecode", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkEditRolecode(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue,
			@RequestParam(name = "edit_id", required = false, defaultValue = "") String id) {
		// try {
		// fieldValue =
		// StringUtils.toEncodedString(fieldValue.getBytes("ISO-8859-1"),
		// Charset.forName("UTF-8"));
		// } catch (UnsupportedEncodingException e) {
		// logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		// }
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (userService.checkEditRolecode(fieldValue.trim(), id).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 获取角色列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/role/getRoleList", method = RequestMethod.POST)
	public String getRoleList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		List<Map<String, Object>> list = userService.getRoleList(map);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", list.size());
		returnMap.put("recordsFiltered", list.size());
		returnMap.put("data", list);
		return mapper.toJson(returnMap);
	}

	/**
	 * 创建角色
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/role/addRole", method = RequestMethod.POST)
	public String addRole(HttpServletRequest request, HttpServletResponse response, @Valid TRole role) {
		RequestContext requestContext = new RequestContext(request);

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			String[] ids = null;
			if (!map.containsKey("ids")) {
				ids = ArrayUtils.EMPTY_STRING_ARRAY;
			} else if (map.containsKey("ids") && ObjectUtils.isArray(map.get("ids"))) {
				ids = (String[]) map.get("ids");
			} else {
				ids = ArrayUtils.toArray((map.get("ids").toString()));
			}

			userService.saveRole(role, ids);
			returnMap.put("success", true);
			returnMap.put("msg", requestContext.getMessage("fxl.admin.role.addRole.success"));
		} catch (ExistedException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", requestContext.getMessage("fxl.admin.role.addRole.fail"));
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 编辑角色信息的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/role/editRole", method = RequestMethod.POST)
	public String editRole(HttpServletRequest request, HttpServletResponse response) {
		RequestContext requestContext = new RequestContext(request);

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, "edit_");
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			userService.editRole(map);
			returnMap.put("msg", requestContext.getMessage("fxl.admin.role.editRole.success"));
			returnMap.put("success", true);
		} catch (ExistedException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", requestContext.getMessage("fxl.admin.role.editRole.fail"));
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 获取角色信息的方法
	 * 
	 * @param request
	 * @param model
	 * @param customerId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/role/getRole/{roleId}", method = RequestMethod.POST)
	public String getRole(HttpServletRequest request, Model model, @PathVariable Long roleId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			TRole role = userService.getRole(roleId);
			returnMap.put("roleId", roleId);
			returnMap.put("name", role.getName());
			returnMap.put("code", role.getCode());
			returnMap.put("description", role.getDescription());

			// 操作信息的map，将map通过json方式返回给页面
			List<Map<String, Object>> treeList = Lists.newArrayList();
			Map<String, Object> treeMap = null;

			Set<TModule> ms = role.getModules();

			List<Long> msIds = Lists.newArrayList();
			for (TModule module : ms) {
				msIds.add(module.getId());
			}

			List<TModule> list = userService.getAllModule();
			for (TModule module : list) {
				treeMap = Maps.newHashMap();
				treeMap.put("id", module.getId());
				if (module.getId().equals(1L)) {
					treeMap.put("pId", "");
					treeMap.put("icon", request.getContextPath() + "/styles/ztree/css/img/diy/1_open.png");
					treeMap.put("nocheck", "true");
				} else {
					treeMap.put("pId", module.getParent().getId());
				}
				treeMap.put("name", module.getName());
				if (msIds.contains(module.getId())) {
					treeMap.put("checked", "true");
				}
				treeList.add(treeMap);
			}
			returnMap.put("tree", treeList);
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取角色信息时出错！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 获取角色信息的方法，返回的json是ztree格式的数据
	 * 
	 * @param request
	 * @param model
	 * @param customerId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/role/getModuleTree", method = RequestMethod.POST)
	public String getModuleTree(HttpServletRequest request, Model model) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			// 操作信息的map，将map通过json方式返回给页面
			List<Map<String, Object>> treeList = Lists.newArrayList();
			Map<String, Object> treeMap = null;

			List<TModule> list = userService.getAllModule();
			for (TModule module : list) {
				treeMap = Maps.newHashMap();
				treeMap.put("id", module.getId());
				if (module.getParent() == null) {
					treeMap.put("pId", "");
					treeMap.put("icon", request.getContextPath() + "/styles/ztree/css/img/diy/1_open.png");
					treeMap.put("nocheck", "true");
				} else {
					treeMap.put("pId", module.getParent().getId());
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

	@RequiresPermissions(value = "config")
	@RequestMapping(value = "/config/main", method = RequestMethod.GET)
	public String configMain(HttpServletRequest request, Model model) {
		return "fxl/admin/config";
	}

	/**
	 * 维护配置项时AJAX验证配置项中文名称是否存在的方法。
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/config/checkCnName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkCnName(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue,
			@RequestParam(required = false, name = "classId") Long classId,
			@RequestParam(required = false, name = "id") Long id) {
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (userService.countDictionaryByNameAndClassId(fieldValue.trim(), classId, id).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 维护配置项时AJAX验证配置项英文名称是否存在的方法。
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/config/checkEnName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkEnName(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue,
			@RequestParam(required = false, name = "classId") Long classId,
			@RequestParam(required = false, name = "id") Long id) {
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (userService.countDictionaryByCodeAndClassId(fieldValue.trim(), classId, id).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 维护配置项时AJAX验证配置项值是否存在的方法。
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/config/checkValue", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkValue(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue,
			@RequestParam(required = false, name = "classId") Long classId,
			@RequestParam(required = false, name = "id") Long id) {
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (userService.countDictionaryByValueAndClassId(fieldValue.trim(), classId, id).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 获取配置类别列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/config/getConfigList", method = RequestMethod.POST)
	public String getConfigList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		List<Map<String, Object>> list = userService.getConfigList(map);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", list.size());
		returnMap.put("recordsFiltered", list.size());
		returnMap.put("data", list);
		return mapper.toJson(returnMap);
	}

	/**
	 * 获取配置类别列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/config/getConfigItemList", method = RequestMethod.POST)
	public String getConfigItemList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		if (StringUtils.isNotBlank(map.get("classId").toString())) {
			List<Map<String, Object>> list = null;
			try {
				list = userService.getConfigItemList(map);
			} catch (Exception e) {
				logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			}
			// 操作信息的map，将map通过json方式返回给页面
			Map<String, Object> returnMap = new HashMap<String, Object>();
			returnMap.put("draw", map.get("draw").toString());
			returnMap.put("recordsTotal", list.size());
			returnMap.put("recordsFiltered", list.size());
			returnMap.put("data", list);
			return mapper.toJson(returnMap);
		} else {
			// 操作信息的map，将map通过json方式返回给页面
			Map<String, Object> returnMap = new HashMap<String, Object>();
			returnMap.put("draw", "0");
			returnMap.put("recordsTotal", 0);
			returnMap.put("recordsFiltered", 0);
			returnMap.put("data", StringUtils.EMPTY);
			return mapper.toJson(returnMap);
		}
	}

	/**
	 * 获取配置项的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/config/getConfigItem/{itemId}", method = RequestMethod.POST)
	public String getConfigItem(HttpServletRequest request, HttpServletResponse response, @PathVariable Long itemId) {
		RequestContext requestContext = new RequestContext(request);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			TDictionary tDictionary = userService.getDictionaryById(itemId);
			returnMap.put("id", tDictionary.getId());
			returnMap.put("name", tDictionary.getName());
			returnMap.put("code", tDictionary.getCode());
			returnMap.put("value", tDictionary.getValue());
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", requestContext.getMessage("fxl.admin.config.getConfig.fail"));
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 增加配置项
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/config/addConfigItem", method = RequestMethod.POST)
	public String addConfigItem(HttpServletRequest request, HttpServletResponse response,
			@Valid TDictionary dictionary) {
		RequestContext requestContext = new RequestContext(request);
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		boolean isAdd = false;
		if (dictionary != null && dictionary.getId() == null) {
			isAdd = true;
		}

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			if (isAdd) {
				Long classId = null;
				if (!map.containsKey("classId") && StringUtils.isBlank(map.get("classId").toString())) {
					throw new ServiceException("缺少配置类别信息，无法保存配置项！");
				}
				classId = Long.valueOf(map.get("classId").toString());
				dictionary.setTDictionaryClass(new TDictionaryClass(classId));
				dictionary.setStatus(1);
				userService.addDictionary(dictionary);
			} else {
				userService.updateDictionary(dictionary);
			}
			returnMap.put("success", true);
			if (isAdd) {
				returnMap.put("msg", requestContext.getMessage("fxl.admin.config.addConfig.success"));
			} else {
				returnMap.put("msg", requestContext.getMessage("fxl.admin.config.editConfig.success"));
			}
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			if (isAdd) {
				returnMap.put("msg", requestContext.getMessage("fxl.admin.config.addConfig.fail"));
			} else {
				returnMap.put("msg", requestContext.getMessage("fxl.admin.config.editConfig.fail"));
			}
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 删除配置项
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/config/delConfigItem/{id}", method = RequestMethod.POST)
	public String delConfigItem(HttpServletRequest request, HttpServletResponse response, @PathVariable Long id) {
		RequestContext requestContext = new RequestContext(request);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			userService.deleteDictionary(id);
			returnMap.put("success", true);
			returnMap.put("msg", requestContext.getMessage("fxl.admin.config.delConfig.success"));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", requestContext.getMessage("fxl.admin.config.delConfig.fail"));
		}
		return mapper.toJson(returnMap);
	}

	@RequiresPermissions(value = "system")
	@RequestMapping(value = "/system/main", method = RequestMethod.GET)
	public String systemMain(HttpServletRequest request, Model model) {
		model.addAttribute("czyhServiceUrl", Constant.czyhServiceUrl);
		return "fxl/admin/system";
	}

	@ResponseBody
	@RequestMapping(value = "/system/updateDictionary", method = RequestMethod.POST)
	public String updateDictionary(HttpServletRequest request, HttpServletResponse response) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			userService.initDictionary();
			Map<String, Object> map = Maps.newHashMap();
			map.put("ticket", "oGFYuX493SON3uHu");
			String reusltSt = HttpClientUtil.callUrlPost(Constant.czyhServiceUrl + "/api/system/refreshCache", map);
			ResponseDTO responseDTO = mapper.fromJson(reusltSt, ResponseDTO.class);
			if (!responseDTO.isSuccess()) {
				throw new ServiceException("更新零到壹服务接口数据字典缓存失败！");
			}
			reusltSt = HttpClientUtil.callUrlPost(Constant.couponInterfaceUrl + "/api/system/refreshCache", map);
			responseDTO = mapper.fromJson(reusltSt, ResponseDTO.class);
			if (!responseDTO.isSuccess()) {
				throw new ServiceException("更新零到壹服务优惠券接口数据字典缓存失败！");
			}
			returnMap.put("success", true);
			returnMap.put("msg", "更新系统数据字典缓存成功！");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (HttpException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "更新零到壹服务接口数据字典缓存失败！");
		} catch (HttpHostConnectException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "更新零到壹服务接口数据字典缓存失败！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "更新系统数据字典缓存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/system/updateEventCategory", method = RequestMethod.POST)
	public String updateEventCategory(HttpServletRequest request, HttpServletResponse response) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			userService.initEventCategory();
			Map<String, Object> map = Maps.newHashMap();
			map.put("ticket", "oGFYuX493SON3uHu");
			String reusltSt = HttpClientUtil.callUrlPost(Constant.czyhServiceUrl + "/api/system/refreshEventCategory",
					map);
			ResponseDTO responseDTO = mapper.fromJson(reusltSt, ResponseDTO.class);
			if (!responseDTO.isSuccess()) {
				throw new ServiceException("更新零到壹服务接口活动类目缓存失败！");
			}
			returnMap.put("success", true);
			returnMap.put("msg", "更新活动类目缓存成功！");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "更新零到壹服务接口活动类目缓存失败！");
		} catch (HttpException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "更新零到壹服务接口活动类目缓存失败！");
		} catch (HttpHostConnectException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "更新零到壹服务接口活动类目缓存失败！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "更新活动类目缓存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/system/eventDetailHtml", method = RequestMethod.POST)
	public String eventDetailHtml(HttpServletRequest request, HttpServletResponse response) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			goodsService.createEventDetailHtml();
			returnMap.put("success", true);
			returnMap.put("msg", "后台生成活动详情页面静态化任务启动成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "后台生成活动详情页面静态化任务启动失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/system/merchantDetailHtml", method = RequestMethod.POST)
	public String merchantDetailHtml(HttpServletRequest request, HttpServletResponse response) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			customerService.createMerchantDetailHtml();
			returnMap.put("success", true);
			returnMap.put("msg", "后台生成商家详情页面静态化任务启动成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "后台生成商家详情页面静态化任务启动失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/system/merchantNumber", method = RequestMethod.POST)
	public String merchantNumber(HttpServletRequest request, HttpServletResponse response) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			customerService.merchantNumber();
			returnMap.put("success", true);
			returnMap.put("msg", "重新生成商家编码任务执行成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "重新生成商家编码任务执行失败！");
		}
		return mapper.toJson(returnMap);
	}

//	@ResponseBody
//	@RequestMapping(value = "/system/eventStockStatistical", method = RequestMethod.POST)
//	public String eventStockStatistical(HttpServletRequest request, HttpServletResponse response) {
//		// 操作信息的map，将map通过json方式返回给页面
//		Map<String, Object> returnMap = new HashMap<String, Object>();
//		try {
//			goodsService.eventStockStatistical();
//			returnMap.put("success", true);
//			returnMap.put("msg", "重新生成商家编码任务执行成功！");
//		} catch (Exception e) {
//			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
//			returnMap.put("success", false);
//			returnMap.put("msg", "重新生成商家编码任务执行失败！");
//		}
//		return mapper.toJson(returnMap);
//	}

	@ResponseBody
	@RequestMapping(value = "/system/setczyhServiceUrl", method = RequestMethod.POST)
	public String setczyhServiceUrl(HttpServletRequest request, HttpServletResponse response,
			@RequestParam String url) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			Constant.czyhServiceUrl = url;

			// File dir = new File("d:/fxlimage/");
			// File dir=new File("F:\\");
			// 如果使用上述的盘符的根目录，会出现java.lang.NullPointerException
			// 为什么？
			// getAllFiles(dir, 0);// 0表示最顶层
			// List<TCustomer> list = customerService.getCusetomerList();
			// for (TCustomer tCustomer : list) {
			// if (tCustomer.getFtype().intValue() == 1) {
			// customerService.saveUnionId(tCustomer.getFweixinId(),
			// tCustomer.getId());
			// }
			// }

			returnMap.put("success", true);
			returnMap.put("msg", "更新零到壹服务接口地址成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "更新零到壹服务接口地址失败！");
		}
		return mapper.toJson(returnMap);
	}

	// 获取层级的方法
	public String getLevel(int level) {
		// A mutable sequence of characters.
		StringBuilder sb = new StringBuilder();
		for (int l = 0; l < level; l++) {
			sb.append("|--");
		}
		return sb.toString();
	}

	public void getAllFiles(File dir, int level) {
		System.out.println(getLevel(level) + dir.getName());
		level++;
		File[] files = dir.listFiles();
		File tempFile = null;
		File tempFile2 = null;
		File destDir = null;
		String fileName = null;
		String filePath = null;
		String newFilePath = null;
		try {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					// 这里面用了递归的算法
					getAllFiles(files[i], level);
				} else {
					System.out.println(getLevel(level) + files[i] + " 对象类型：" + files[i].getClass().getName());

					tempFile = files[i];
					// 获取上传文件名
					fileName = tempFile.getName();
					filePath = tempFile.getParent();

					newFilePath = filePath.replaceAll("fxlimage", "newfxlimage");

					destDir = new File(newFilePath);
					if (!destDir.exists()) {
						destDir.mkdirs();
					}

					// 获取上传文件扩展名
					String fileExt = FileUtils.getFileExt(fileName);
					// 定义最终目标文件对象

					File destFile = null;

					if (filePath.indexOf("\\umeditorUpload\\") < 0) {
						if (fileExt.equalsIgnoreCase("png")) {
							destFile = new File(newFilePath, fileName.replaceAll(".png", ".jpg"));
							ImageUtil.toJpg(tempFile, destFile);
						} else {
							destFile = new File(newFilePath, fileName);
							org.apache.commons.io.FileUtils.copyFile(tempFile, destFile);
						}
					} else {
						if (fileExt.equalsIgnoreCase("png")) {
							tempFile2 = new File(System.getProperty("java.io.tmpdir"),
									fileName.replaceAll(".png", ".jpg"));
							ImageUtil.toJpg(tempFile, tempFile2);
							destFile = new File(newFilePath, fileName.replaceAll(".png", ".jpg"));
							ImageUtil.thumbnail(tempFile2, destFile,
									Integer.valueOf(PropertiesUtil.getProperty("umeditorImageWidth")),
									Integer.MAX_VALUE, true);
						} else {
							destFile = new File(newFilePath, fileName);
							ImageUtil.thumbnail(tempFile, destFile,
									Integer.valueOf(PropertiesUtil.getProperty("umeditorImageWidth")),
									Integer.MAX_VALUE, true);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("保存上传的附件文件时出错！");
		} finally {

			if (tempFile2 != null && tempFile2.exists()) {
				tempFile2.delete();
			}
		}
	}

	@RequestMapping(value = "/system/notice", method = RequestMethod.GET)
	public String notice(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();

		model.addAttribute("noticeMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.czyhwebNotice, shiroUser.getLanguage()));

		return "fxl/admin/notice";
	}

	@ResponseBody
	@RequestMapping(value = "/system/getNoticeList", method = RequestMethod.POST)
	public String getNoticeList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		userService.getNoticeList(map, page);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	@Log(message = "系统管理员[{0}]添加系统公告信息。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/system/addNotice", method = RequestMethod.POST)
	public String addEventC(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();

		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			userService.addNotice(map);
			returnMap.put("success", true);
			returnMap.put("msg", "公告信息保存成功！");
		} catch (ServiceException se) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(se)));
			returnMap.put("success", false);
			returnMap.put("msg", se.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "公告信息保存失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@Log(message = "系统管理员[{0}]删除了一条公告消息。", module = 8)
	@ResponseBody
	@RequestMapping(value = "/system/delNotice/{NoticeId}", method = RequestMethod.POST)
	public String delPush(HttpServletRequest request, HttpServletResponse response, @PathVariable String NoticeId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			userService.delNotice(NoticeId);
			returnMap.put("success", true);
			returnMap.put("msg", "公告删除成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "公告删除失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/system/updateConfiguration", method = RequestMethod.POST)
	public String updateConfiguration(HttpServletRequest request, HttpServletResponse response) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			configurationService.initConfigurationMap();
			Map<String, Object> map = Maps.newHashMap();
			map.put("ticket", "oGFYuX493SON3uHu");
			String reusltSt = HttpClientUtil.callUrlPost(Constant.czyhServiceUrl + "/api/system/refreshConfiguration",
					map);
			ResponseDTO responseDTO = mapper.fromJson(reusltSt, ResponseDTO.class);
			if (!responseDTO.isSuccess()) {
				throw new ServiceException("更新零到壹服务接口配置表缓存失败！");
			}
			reusltSt = HttpClientUtil.callUrlPost(Constant.couponInterfaceUrl + "/api/system/refreshCache", map);
			responseDTO = mapper.fromJson(reusltSt, ResponseDTO.class);
			if (!responseDTO.isSuccess()) {
				throw new ServiceException("更新零到壹服务优惠券接口数据字典缓存失败！");
			}
			returnMap.put("success", true);
			returnMap.put("msg", "更新系统配置表缓存成功！");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (HttpException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "更新零到壹服务接口配置表缓存失败！");
		} catch (HttpHostConnectException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "更新零到壹服务接口配置表缓存失败！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "更新系统配置表缓存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/system/error", method = RequestMethod.GET)
	public String error(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();

		model.addAttribute("noticeMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.czyhwebNotice, shiroUser.getLanguage()));

		return "fxl/admin/error";
	}

	@ResponseBody
	@RequestMapping(value = "/system/getErrorList", method = RequestMethod.POST)
	public String getErrorList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		userService.getErrorList(map, page);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	/**
	 * 根据项目ID返回项目详细信息
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/system/getError/{errorId}", method = RequestMethod.POST)
	public String getOrder(HttpServletRequest request, HttpServletResponse response, @PathVariable String errorId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			TAppError appError = appErrorDAO.findOne(errorId);
			returnMap.put("id", appError.getId());
			if (appError.getFclientType().intValue() == 1) {
				returnMap.put("fclientType", "web");
			} else if (appError.getFclientType().intValue() == 2) {
				returnMap.put("fclientType", "ios");
			} else if (appError.getFclientType().intValue() == 3) {
				returnMap.put("fclientType", "android");
			}
			returnMap.put("ferrorMessage", appError.getFerrorMessage());
			if (appError.getFreportTime() != null) {
				returnMap.put("freportTime", DateFormatUtils.format(appError.getFreportTime(), "yyyy-MM-dd HH:mm"));
			}
			returnMap.put("fclientInfo", appError.getFclientInfo());
			returnMap.put("fdata", appError.getFdata());
			returnMap.put("ferrorText", appError.getFerrorText());
			returnMap.put("fsystem", appError.getFsystem());
			returnMap.put("fuser", appError.getFuser());
			returnMap.put("fview", appError.getFview());

			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取活动错误日志信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/system/operatLog", method = RequestMethod.GET)
	public String operatLog(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();

		model.addAttribute("czyhwebModule",
				DictionaryUtil.getStatueMap(DictionaryUtil.czyhwebModule, shiroUser.getLanguage()));
		model.addAttribute("userList", userService.getUserList());
		return "fxl/admin/operatLog";
	}

	@ResponseBody
	@RequestMapping(value = "/system/getOperatLog", method = RequestMethod.POST)
	public String getOperatLog(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		userService.getOperatLog(map, page);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

}