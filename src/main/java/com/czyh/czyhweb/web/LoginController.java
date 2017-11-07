package com.czyh.czyhweb.web;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.RequestContext;

@Controller
@RequestMapping("/fxl/login")
public class LoginController {

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	private static final String LOGIN_PAGE = "/web/index";

	private static final String INDEX_PAGE = "redirect:/fxl/index";

	/**
	 * login打酱油的方法，并且处理了一些多语言切管的功能处理
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String login(HttpServletRequest request) {
		Subject subject = SecurityUtils.getSubject();
		if (subject.isAuthenticated()) {
			return INDEX_PAGE;
		}
		return LOGIN_PAGE;
	}

	/**
	 * SHIRO登录验证失败后的方法
	 * 
	 * @param username
	 * @param map
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String fail(@RequestParam(FormAuthenticationFilter.DEFAULT_USERNAME_PARAM) String username, Model model,
			HttpServletRequest request) {
		String msg = parseException(request);
		model.addAttribute("msg", msg);
		model.addAttribute("username", username);

		return LOGIN_PAGE;
	}

	/**
	 * 解析SHIRO认证后返回的异常信息，将异常翻译为提示信息并返回
	 * 
	 * @param request
	 * @return
	 */
	private String parseException(HttpServletRequest request) {
		String errorString = (String) request.getAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME);
		RequestContext requestContext = new RequestContext(request);

		StringBuilder msg = new StringBuilder(requestContext.getMessage("fxl.login.failure.login"));
		if (errorString != null) {
			if (errorString.equals("org.apache.shiro.authc.DisabledAccountException")) {
				msg.append(requestContext.getMessage("fxl.login.failure.disabledAccount"));
			} else {
				msg.append(requestContext.getMessage("fxl.login.failure.accountOrPasswrodError"));
			}
		}
		return msg.toString();
	}
}