package com.czyh.czyhweb.security;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.UserFilter;

/**
 * 由于平台前台AJAX提交时如果会话超时，则返回json给页面并跳转到登录页面
 */
public class CustomUserFilter extends UserFilter {

	public final static String X_R = "X-Requested-With";
	public final static String X_R_VALUE = "XMLHttpRequest";

	/**
	 * 如果是ajax提交并且会话已经超时，则返回json提示会话超时并跳转到登录页面。
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 * @see org.apache.shiro.web.filter.AccessControlFilter#redirectToLogin(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse)
	 */
	@Override
	protected void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		String xrv = httpServletRequest.getHeader(X_R);
		if (xrv != null && xrv.equalsIgnoreCase(X_R_VALUE)) {
			PrintWriter out = response.getWriter();
			out.print("com.czyh.czyhweb.sessionTimeout");
		} else {
			super.redirectToLogin(request, response);
		}
	}

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
		return false;
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		Subject subject = getSubject(request, response);
		if (!subject.isAuthenticated()) {
			request.setAttribute("msg", "您之前登录应用的会话超时，请重新登录应用！");
			this.redirectToLogin(request, response);
			return false;
		}
		return true;
	}
}