package com.czyh.czyhweb.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContext;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;
import org.springside.modules.web.Servlets;

import com.czyh.czyhweb.entity.TModule;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.log.Log;
import com.czyh.czyhweb.log.LogMessageObject;
import com.czyh.czyhweb.log.impl.LogUitls;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.customer.UserService;
import com.czyh.czyhweb.util.Constant;
import com.google.common.collect.Maps;

/**
 * 平台前台会员操作的web处理类
 * 
 * @author zzg
 * 
 */
@Controller
@RequestMapping("/fxl")
public class FxlController {

	private static Logger logger = LoggerFactory.getLogger(FxlController.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private UserService userService;

	/**
	 * 会员登录平台通过验证后，返回会员中心首页方法
	 * 
	 * @param request
	 * @return
	 */
	@Log(message = "{0}[{1}]登录了系统。", module = 1)
	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String index(HttpServletRequest request, Model model, @RequestParam(required = false) String locale,
			@CookieValue(required = false, defaultValue = "zh_CN", value = "fxlLanguage") String language) {
		if (StringUtils.isNotBlank(locale)) {
			model.addAttribute("language", locale);
		} else {
			model.addAttribute("language", language);
		}
		Subject subject = SecurityUtils.getSubject();

		if (!subject.isAuthenticated()) {
			return "redirect:/";
		}
		ShiroUser shiroUser = (ShiroUser) subject.getPrincipal();
		shiroUser.setLanguage(language);
		Session session = subject.getSession(true);
		if (session.getAttribute("menu") == null) {
			List<TModule> menuList = userService.getMenu(shiroUser.getId());
			session.setAttribute("menu", menuList);
		}
		List<Map<String, Object>> noticeList = userService.getNoticeDetailList();
		model.addAttribute("noticeList", noticeList);

		LogUitls.putArgs(
				LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), shiroUser.getLoginName() }));
		return "fxl/index";
	}

	/**
	 * 修改密码的方法
	 * 
	 * @param request
	 * @param response
	 */
	@ResponseBody
	@RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
	public String updatePassword(HttpServletRequest request, HttpServletResponse response) {
		RequestContext requestContext = new RequestContext(request);

		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		Map<String, Object> returnMap = Maps.newHashMap();
		try {
			Validate.notBlank(map.get("oldPassword").toString(), "oldPassword不能为空", ArrayUtils.EMPTY_OBJECT_ARRAY);
			Validate.notBlank(map.get("password").toString(), "password不能为空", ArrayUtils.EMPTY_OBJECT_ARRAY);
			Validate.notBlank(map.get("password2").toString(), "password2不能为空", ArrayUtils.EMPTY_OBJECT_ARRAY);
			userService.updatePassword(shiroUser.getId(), map.get("oldPassword").toString(),
					map.get("password").toString());
			returnMap.put("success", true);
			returnMap.put("msg", requestContext.getMessage("fxl.index.updatePassword.success"));
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", requestContext.getMessage("fxl.index.updatePassword.fail"));
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 根据path参数返回图片文件到Response对象中
	 * 
	 * @param request
	 * @param response
	 * @param path
	 *            图片的相对路径，如果为空将返回默认的无图图片
	 * @param size
	 *            返回不同的尺寸版本的图片，如果为空将返回默认路径的图片
	 * @return
	 */
	@RequestMapping(value = "/getImage", method = RequestMethod.GET, produces = { MediaType.IMAGE_GIF_VALUE,
			MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE })
	public String getImage(HttpServletRequest request, HttpServletResponse response, @RequestParam String path) {

		InputStream is = null;
		OutputStream os = null;

		StringBuilder allPath = new StringBuilder();
		try {
			os = response.getOutputStream();
			if (StringUtils.isBlank(path) || path.equalsIgnoreCase("null")) {
				throw new ServiceException("获取图片的路径为空，将返回默认的无图图片作为返回图片");
			}
			allPath.append(Constant.RootPath).append(File.separator).append(path);
			File imgFile = new File(allPath.toString());
			is = FileUtils.openInputStream(imgFile);
			IOUtils.copy(is, os);
		} catch (Exception e) {
			logger.error("获取图片时出错！");
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			try {
				allPath.delete(0, allPath.length());
				is = FileUtils.openInputStream(
						new File(allPath.append(request.getSession().getServletContext().getRealPath("/"))
								.append("/styles/images/attachment/newsnopic.jpg").toString()));
				IOUtils.copy(is, os);
			} catch (IOException e1) {
				logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e1)));
			}
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
				}
			}
		}
		return null;
	}

	/**
	 * 根据path参数返回图片文件到Response对象中
	 * 
	 * @param request
	 * @param response
	 * @param path
	 *            图片的相对路径，如果为空将返回默认的无图图片
	 * @param size
	 *            返回不同的尺寸版本的图片，如果为空将返回默认路径的图片
	 * @return
	 */
	@RequestMapping(value = "/getLogoImage", method = RequestMethod.GET, produces = { MediaType.IMAGE_GIF_VALUE,
			MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE })
	public String getLogoImage(HttpServletRequest request, HttpServletResponse response, @RequestParam String path) {

		InputStream is = null;
		OutputStream os = null;

		StringBuilder allPath = new StringBuilder();
		try {
			os = response.getOutputStream();
			if (StringUtils.isBlank(path) || path.equalsIgnoreCase("null")) {
				throw new ServiceException("获取图片的路径为空，将返回默认的无图图片作为返回图片");
			}
			allPath.append(Constant.RootPath).append(File.separator).append(path);
			File imgFile = new File(allPath.toString());
			is = FileUtils.openInputStream(imgFile);
			IOUtils.copy(is, os);
		} catch (Exception e) {
			logger.error("获取图片时出错！");
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			try {
				allPath.delete(0, allPath.length());
				is = FileUtils.openInputStream(
						new File(allPath.append(request.getSession().getServletContext().getRealPath("/"))
								.append("/styles/images/attachment/nopic.jpg").toString()));
				IOUtils.copy(is, os);
			} catch (IOException e1) {
				logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e1)));
			}
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
				}
			}
		}
		return null;
	}

	/**
	 * 根据path参数返回文件到Response对象中
	 * 
	 * @param request
	 * @param response
	 * @param docId
	 *            文档表的ID
	 * @return
	 */
	@RequestMapping(value = "/getFile", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public String getFile(HttpServletRequest request, HttpServletResponse response, @RequestParam String docpath,
			@RequestParam(required = false, defaultValue = "file") String fileName) {
		InputStream is = null;
		OutputStream os = null;

		int index = docpath.indexOf(".");
		String suffix = docpath.substring(index);
		StringBuilder allPath = new StringBuilder();
		try {
			response.setHeader("Content-Disposition", "attachment; filename="
					+ URLEncoder.encode(new StringBuilder(fileName).append(suffix).toString(), "UTF-8"));
			os = response.getOutputStream();
			allPath.append(Constant.RootPath).append(docpath);
			File imgFile = new File(allPath.toString());
			is = FileUtils.openInputStream(imgFile);
			IOUtils.copy(is, os);
		} catch (IOException e) {
			logger.error("获取文件时出错！");
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
				}
			}
		}
		return null;
	}

}