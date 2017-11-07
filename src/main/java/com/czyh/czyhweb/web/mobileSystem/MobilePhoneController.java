package com.czyh.czyhweb.web.mobileSystem;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springside.modules.mapper.JsonMapper;

import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.customer.UserService;
import com.czyh.czyhweb.util.DictionaryUtil;

/**
 * 移动端系统处理类
 * 
 * @author jinshengzhi
 * 
 */
@Controller
@RequestMapping("/fxl/Mobile")
public class MobilePhoneController {

	private static Logger logger = LoggerFactory.getLogger(MobilePhoneController.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/indexPhone", method = RequestMethod.GET)
	public String userMain(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();

		Map<Integer, String> statusMap = DictionaryUtil.getStatueMap(DictionaryUtil.UserStatus,
				shiroUser.getLanguage());
		Map<Integer, String> statusTempMap = org.apache.commons.lang3.ObjectUtils.clone(statusMap);
		statusTempMap.remove(999);
		model.addAttribute("statusMap", statusTempMap);
		model.addAttribute("categoryMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.UserCategory, shiroUser.getLanguage()));

		return "fxl/mobile/indexPhone";
	}

}