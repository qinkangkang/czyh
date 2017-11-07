package com.czyh.czyhweb.web.push;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;
import org.springside.modules.web.Servlets;

import com.czyh.czyhweb.entity.TPush;
import com.czyh.czyhweb.log.Log;
import com.czyh.czyhweb.log.LogMessageObject;
import com.czyh.czyhweb.log.impl.LogUitls;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.push.PushService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.DictionaryUtil;

/**
 * 推送管理
 * 
 * @author jinsey
 *
 */
@Controller
@RequestMapping("/fxl/push")
public class PushController {

	private static Logger logger = LoggerFactory.getLogger(PushController.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private PushService PushService;

	/**
	 * 推送管理页面
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/main", method = RequestMethod.GET)
	public String main(HttpServletRequest request, ModelMap model) {

		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();

		List<TPush> tPush = PushService.getPushList();
		model.addAttribute("tPush", tPush);
		model.addAttribute("pushUrlMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.PushLinkTargetType, shiroUser.getLanguage()));
		model.addAttribute("pushTimeMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.PushTimeType, shiroUser.getLanguage()));
		model.addAttribute("pushUserTypeMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.PushUserType, shiroUser.getLanguage()));
		model.addAttribute("pushDimensionMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.PushDimension, shiroUser.getLanguage()));
		model.addAttribute("pushUserTagMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.PushUserTag, shiroUser.getLanguage()));
		model.addAttribute("appVersionMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.AppVersion, shiroUser.getLanguage()));
		model.addAttribute("pushSadelsModelMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.PushSadelsModel, shiroUser.getLanguage()));

		return "fxl/push/push";
	}

	/**
	 * 推送审核页面
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/audit", method = RequestMethod.GET)
	public String audit(HttpServletRequest request, ModelMap model) {

		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();

		List<TPush> tPush = PushService.getPushList();
		model.addAttribute("tPush", tPush);
		model.addAttribute("pushUrlMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.PushLinkTargetType, shiroUser.getLanguage()));
		model.addAttribute("pushTimeMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.PushTimeType, shiroUser.getLanguage()));
		model.addAttribute("pushUserTypeMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.PushUserType, shiroUser.getLanguage()));
		model.addAttribute("pushDimensionMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.PushDimension, shiroUser.getLanguage()));
		model.addAttribute("pushUserTagMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.PushUserTag, shiroUser.getLanguage()));
		model.addAttribute("appVersionMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.AppVersion, shiroUser.getLanguage()));

		return "fxl/push/pushAudit";
	}

	/**
	 * 获取推送列表的方法
	 * 
	 * @author jsz
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/pushmsg/getPushList", method = RequestMethod.POST)
	public String getPushList(HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));
		PushService.getPushList(map, page);

		Map<String, Object> returnMap = new HashMap<String, Object>();

		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());

		return mapper.toJson(returnMap);
	}

	@Log(message = "APP小编[{0}]增加一条新的Push消息。", module = 8)
	@ResponseBody
	@RequestMapping(value = "/pushmsg/addPush", method = RequestMethod.POST)
	public String addPush(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<String, Object> returnMap = new HashMap<String, Object>();

		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			PushService.savePush(map);
			returnMap.put("success", true);
			returnMap.put("msg", "推送消息成功!");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "推送消息失败!");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@Log(message = "系统管理员[{0}]删除了一条审核完成的Push消息。", module = 8)
	@ResponseBody
	@RequestMapping(value = "/pushmsg/delPush/{pid}", method = RequestMethod.POST)
	public String delPush(HttpServletRequest request, HttpServletResponse response, @PathVariable String pid) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			PushService.delPush(pid);
			returnMap.put("success", true);
			returnMap.put("msg", "推送消息删除成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "推送消息删除失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 审核通过推送消息
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "系统管理员[{0}]将PushID为[{1}]的Push消息审核通过。", module = 8)
	@ResponseBody
	@RequestMapping(value = "/pushmsg/successPush/{pushId}/{times}", method = RequestMethod.POST)
	public String successPush(HttpServletRequest request, Model model, @PathVariable String pushId, String times) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {

			PushService.saveUpdateSuccess(pushId, times);

			returnMap.put("success", true);
			returnMap.put("msg", "审核推送消息操作成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "审核推送消息操作失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), pushId }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 审核失败
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "系统管理员[{0}]将PushID为[{1}]的Push消息审核失败。", module = 8)
	@ResponseBody
	@RequestMapping(value = "/pushmsg/defeatePush/{pushId}", method = RequestMethod.POST)
	public String defeatePush(HttpServletRequest request, Model model, @PathVariable String pushId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			PushService.saveUpdatedefeate(pushId);
			returnMap.put("success", true);
			returnMap.put("msg", "审核推送消息操作成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "审核推送消息操作失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), pushId }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 撤回
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "系统管理员[{0}]将PushID为[{1}]的Push消息撤回。", module = 8)
	@ResponseBody
	@RequestMapping(value = "/pushmsg/removePush/{pushId}", method = RequestMethod.POST)
	public String removePush(HttpServletRequest request, Model model, @PathVariable String pushId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			PushService.removePush(pushId);
			returnMap.put("success", true);
			returnMap.put("msg", "撤回推送消息操作成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "撤回推送消息操作失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), pushId }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 保存审核备注方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/saveAuditMessage", method = RequestMethod.POST)
	public String saveAuditMessage(HttpServletRequest request, Model model, @RequestParam String id,
			@RequestParam String auditMessage) {

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String Message = PushService.saveAuditMessage(id, auditMessage);

			returnMap.put("success", true);
			returnMap.put("Message", Message);
			returnMap.put("msg", "推送审核备注保存成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "推送审核备注保存失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 推送详情信息
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getDetails/{pushId}", method = RequestMethod.POST)
	public String getConsult(HttpServletRequest request, HttpServletResponse response, @PathVariable String pushId) {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			TPush tpush = PushService.getDetails(pushId);
			String FpushTime = DateFormatUtils.format(tpush.getFpushTime(), "yyyy-MM-dd HH:mm");
			String FvalidTime = DateFormatUtils.format(tpush.getFvalidTime(), "yyyy-MM-dd HH:mm");
			String FcreateTime = DateFormatUtils.format(tpush.getFcreateTime(), "yyyy-MM-dd HH:mm");
			String FauditTime = null;
			if (tpush.getFauditTime() != null) {
				FauditTime = DateFormatUtils.format(tpush.getFauditTime(), "yyyy-MM-dd HH:mm");
			}
			returnMap.put("x_ftitle", tpush.getFtitle());
			returnMap.put("x_fcontent", tpush.getFcontent());
			returnMap.put("x_ftargetType", tpush.getFtargetType());
			returnMap.put("x_ftargetObject", tpush.getFtargetObject());
			returnMap.put("x_fdescription", tpush.getFdescription());
			returnMap.put("x_fPushTime", FpushTime);
			returnMap.put("x_fvalidTime", FvalidTime);
			returnMap.put("x_fcreateTime", FcreateTime);
			returnMap.put("x_fauditTime", FauditTime);
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取推送详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

}