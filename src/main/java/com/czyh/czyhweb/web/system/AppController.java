package com.czyh.czyhweb.web.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;
import org.springside.modules.web.Servlets;

import com.czyh.czyhweb.dao.ColumnBannerDAO;
import com.czyh.czyhweb.entity.TAppChannelSetting;
import com.czyh.czyhweb.entity.TAppChannelSlider;
import com.czyh.czyhweb.entity.TAppFlash;
import com.czyh.czyhweb.entity.TAppIndexItem;
import com.czyh.czyhweb.entity.TAppNotice;
import com.czyh.czyhweb.entity.TColumnBanner;
import com.czyh.czyhweb.entity.TImage;
import com.czyh.czyhweb.entity.TSubject;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.log.Log;
import com.czyh.czyhweb.log.LogMessageObject;
import com.czyh.czyhweb.log.impl.LogUitls;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.FxlService;
import com.czyh.czyhweb.service.system.AppService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.google.common.collect.Lists;

/**
 * app系统管理
 * 
 * @author qkk
 * 
 */
@Controller
@RequestMapping("/fxl/app")
public class AppController {

	private static Logger logger = LoggerFactory.getLogger(AppController.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private AppService appService;

	@Autowired
	private FxlService fxlService;

	@Autowired
	private ColumnBannerDAO columnBannerDAO;

	@RequestMapping(value = "/channel", method = RequestMethod.GET)
	public String channel(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("isVisibleMap", DictionaryUtil.getStatueMap(DictionaryUtil.YesNo, shiroUser.getLanguage()));
		model.addAttribute("cityMap", DictionaryUtil.getStatueMap(DictionaryUtil.City, shiroUser.getLanguage()));
		model.addAttribute("defaultOrderTypeMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.ChannelDefaultOrderType, shiroUser.getLanguage()));
		model.addAttribute("channelTypeMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.ChannelType, shiroUser.getLanguage()));
		model.addAttribute("channelFrontTypeMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.ChannelFrontType, shiroUser.getLanguage()));
		model.addAttribute("fcity", "1");
		return "fxl/app/appChannelManage";
	}

	/**
	 * 获取客户列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getChannelList", method = RequestMethod.POST)
	public String getChannelList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		appService.getChannelList(map, page);

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
	@RequestMapping(value = "/getChannel/{channelId}", method = RequestMethod.POST)
	public String getChannel(HttpServletRequest request, HttpServletResponse response, @PathVariable String channelId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TAppChannelSetting tAppChannelSetting = appService.getChannel(channelId);
			returnMap.put("id", tAppChannelSetting.getId());
			returnMap.put("fcode", tAppChannelSetting.getFcode());
			returnMap.put("ftitle", tAppChannelSetting.getFtitle());
			// returnMap.put("fsubTitle", tAppChannelSetting.getFsubTitle());
			returnMap.put("fcity", tAppChannelSetting.getFcity());
			returnMap.put("forder", tAppChannelSetting.getForder());
			returnMap.put("ffrontType", tAppChannelSetting.getFfrontType());
			returnMap.put("fallEvent", tAppChannelSetting.getFallEvent());
			returnMap.put("fwebType", tAppChannelSetting.getFwebType());
			returnMap.put("ftype", tAppChannelSetting.getFtype());
			returnMap.put("fisVisible", tAppChannelSetting.getFisVisible());
			// returnMap.put("fpromotion", tAppChannelSetting.getFpromotion());
			returnMap.put("fdefaultOrderType", tAppChannelSetting.getFdefaultOrderType());

			String imageId = tAppChannelSetting.getFicon();
			returnMap.put("fimage", imageId);
			if (StringUtils.isNotBlank(imageId)) {
				TImage tImage = fxlService.getImage(Long.valueOf(imageId));
				returnMap.put("imageWidth", tImage.getImageWidth());
				returnMap.put("imageHeight", tImage.getImageHeight());
				returnMap.put("fimagePath", fxlService.getImageUrl(imageId, true));
			}
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取APP栏目详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "无线小编[{0}]创建了ID为[{1}]的APP栏目。", module = 4)
	@ResponseBody
	@RequestMapping(value = "/addChannel", method = RequestMethod.POST)
	public String addChannel(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			String channelId = appService.saveChannel(map);
			returnMap.put("success", true);
			returnMap.put("msg", "创建APP栏目信息保存成功！");
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), channelId }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "创建APP栏目信息保存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "无线小编[{0}]编辑了ID为[{1}]的APP栏目。", module = 4)
	@ResponseBody
	@RequestMapping(value = "/editChannel", method = RequestMethod.POST)
	public String editChannel(HttpServletRequest request, HttpServletResponse response, @RequestParam String id) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		try {
			appService.eidtChannel(map);
			returnMap.put("msg", "编辑APP栏目信息保存成功！");
			returnMap.put("success", true);
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑APP栏目信息保存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "无线小编[{0}]删除了ID为[{1}]，栏目名称为[{2}]的APP栏目。", module = 4)
	@ResponseBody
	@RequestMapping(value = "/delChannel/{id}", method = RequestMethod.POST)
	public String delChannel(HttpServletRequest request, HttpServletResponse response, @PathVariable String id) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			String channelName = appService.delChannel(id);
			returnMap.put("msg", "删除APP栏目信息成功！");
			returnMap.put("success", true);
			LogUitls.putArgs(
					LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id, channelName }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "删除APP栏目信息失败！");
		}
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/flash", method = RequestMethod.GET)
	public String config(HttpServletRequest request, Model model) {
		model.addAttribute("isVisibleMap", DictionaryUtil.getStatueMap(DictionaryUtil.YesNo));
		model.addAttribute("sliderUrlTypeMap", DictionaryUtil.getStatueMap(DictionaryUtil.AppStartUrlType));
		return "fxl/app/appConfig";
	}

	/**
	 * 获取栏目轮播列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getAppFlashList", method = RequestMethod.POST)
	public String getAppFlashList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		List<Map<String, Object>> list = appService.getAppFlashList();

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", list.size());
		returnMap.put("recordsFiltered", list.size());
		returnMap.put("data", list);
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/getAppFlash/{appFlashId}", method = RequestMethod.POST)
	public String toAppConfig(HttpServletRequest request, Model model, @PathVariable String appFlashId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TAppFlash tAppFlash = appService.getAppFlash(appFlashId);
			returnMap.put("id", tAppFlash.getId());
			returnMap.put("cityId", tAppFlash.getFcity());
			returnMap.put("fcity", DictionaryUtil.getString(DictionaryUtil.City, tAppFlash.getFcity()));
			returnMap.put("fimage", tAppFlash.getFimage());
			returnMap.put("furlType", tAppFlash.getFurlType());
			returnMap.put("fentityId", tAppFlash.getFentityId());
			returnMap.put("fentityTitle", tAppFlash.getFentityTitle());
			returnMap.put("fexternalUrl", tAppFlash.getFexternalUrl());
			returnMap.put("fisVisible", tAppFlash.getFisVisible());
			String imageId = tAppFlash.getFimage();
			if (StringUtils.isNotBlank(imageId)) {
				TImage tImage = fxlService.getImage(Long.valueOf(imageId));
				returnMap.put("imageWidth", tImage.getImageWidth());
				returnMap.put("imageHeight", tImage.getImageHeight());
				returnMap.put("fimagePath", fxlService.getImageUrl(imageId, false));
			}
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取APP启动图详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "无线小编[{0}]编辑了ID为[{1}]的APP启动图。", module = 4)
	@ResponseBody
	@RequestMapping(value = "/editAppFlash", method = RequestMethod.POST)
	public String editAppFlash(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			String id = appService.editAppFlash(map);
			returnMap.put("msg", "编辑APP启动页信息保存成功！");
			returnMap.put("success", true);
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑APP启动页信息保存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/toChannelSlider/{channelId}", method = RequestMethod.GET)
	public String toChannelSlider(HttpServletRequest request, HttpServletResponse response, Model model,
			@PathVariable String channelId) {
		TAppChannelSetting tAppChannelSetting = appService.getChannel(channelId);
		model.addAttribute("channelId", channelId);
		model.addAttribute("cityId", tAppChannelSetting.getFcity());
		model.addAttribute("isVisibleMap", DictionaryUtil.getStatueMap(DictionaryUtil.YesNo));
		model.addAttribute("sliderUrlTypeMap", DictionaryUtil.getStatueMap(DictionaryUtil.ChannelSliderUrlType));
		return "fxl/app/appChannelSlider";
	}

	/**
	 * 获取栏目轮播列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getChannelSliderList", method = RequestMethod.POST)
	public String getChannelSliderList(HttpServletRequest request, HttpServletResponse response,
			@RequestParam String channelId) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		List<Map<String, Object>> list = appService.getChannelSliderList(channelId);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", list.size());
		returnMap.put("recordsFiltered", list.size());
		returnMap.put("data", list);
		return mapper.toJson(returnMap);
	}

	/**
	 * 根据轮播ID返回轮播详细信息
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getChannelSlider/{sliderId}", method = RequestMethod.POST)
	public String getChannelSlider(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String sliderId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TAppChannelSlider tAppChannelSlider = appService.getChannelSlider(sliderId);
			returnMap.put("id", tAppChannelSlider.getId());
			returnMap.put("fimage", tAppChannelSlider.getFimage());
			returnMap.put("furlType", tAppChannelSlider.getFurlType());
			returnMap.put("fentityId", tAppChannelSlider.getFentityId());
			returnMap.put("fentityTitle", tAppChannelSlider.getFentityTitle());
			returnMap.put("fexternalUrl", tAppChannelSlider.getFexternalUrl());
			returnMap.put("forder", tAppChannelSlider.getForder());
			returnMap.put("fisVisible", tAppChannelSlider.getFisVisible());
			String imageId = tAppChannelSlider.getFimage();
			if (StringUtils.isNotBlank(imageId)) {
				TImage tImage = fxlService.getImage(Long.valueOf(imageId));
				returnMap.put("imageWidth", tImage.getImageWidth());
				returnMap.put("imageHeight", tImage.getImageHeight());
				returnMap.put("fimagePath", fxlService.getImageUrl(imageId, false));
			}
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取栏目轮播项信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "无线小编[{0}]创建了ID为[{1}]的栏目轮播信息。", module = 4)
	@ResponseBody
	@RequestMapping(value = "/addChannelSlider", method = RequestMethod.POST)
	public String addChannelSlider(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			String id = appService.saveChannelSlider(map);
			returnMap.put("success", true);
			returnMap.put("msg", "创建栏目轮播项信息保存成功！");
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id }));
		} catch (Exception e) {

			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "创建栏目轮播项信息保存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "无线小编[{0}]修改了ID为[{1}]的栏目轮播信息。", module = 4)
	@ResponseBody
	@RequestMapping(value = "/editChannelSlider", method = RequestMethod.POST)
	public String editChannelSlider(HttpServletRequest request, HttpServletResponse response, @RequestParam String id) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		try {
			appService.editChannelSlider(map);
			returnMap.put("msg", "编辑栏目轮播项信息保存成功！");
			returnMap.put("success", true);
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑栏目轮播项信息保存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "无线小编[{0}]删除了ID为[{1}]的栏目轮播信息。", module = 4)
	@ResponseBody
	@RequestMapping(value = "/delChannelSlider/{sliderId}", method = RequestMethod.POST)
	public String delChannelSlider(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String sliderId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			appService.delChannelSlider(sliderId);
			returnMap.put("success", true);
			returnMap.put("msg", "栏目轮播项信息删除成功！");
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), sliderId }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "栏目轮播项信息删除失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 获取栏目轮播列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getSliderTargetList", method = RequestMethod.POST)
	public String getSliderTargetList(HttpServletRequest request, HttpServletResponse response,
			@RequestParam int cityId, @RequestParam int urlType, @RequestParam String searchKey) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		List<Map<String, Object>> list = appService.getSliderTargetList(cityId, 1, searchKey);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", list.size());
		returnMap.put("recordsFiltered", list.size());
		returnMap.put("data", list);
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/subject", method = RequestMethod.GET)
	public String subject(HttpServletRequest request, Model model) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();

		return "fxl/app/subjectManage";
	}

	/**
	 * 获取客户列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getSubjectList", method = RequestMethod.POST)
	public String getSubjectList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		appService.getSubjectList(map, page);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/getSubject/{subjectId}", method = RequestMethod.POST)
	public String getSubject(HttpServletRequest request, HttpServletResponse response, @PathVariable String subjectId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TSubject tSubject = appService.getSubject(subjectId);
			returnMap.put("id", tSubject.getId());
			returnMap.put("ftitle", tSubject.getFtitle());
			returnMap.put("fdetail", tSubject.getFdetail());
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取主题详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "无线小编[{0}]创建了ID为[{1}]的专题。", module = 4)
	@ResponseBody
	@RequestMapping(value = "/addSubject", method = RequestMethod.POST)
	public String addSubject(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			String id = appService.saveSubject(map);
			returnMap.put("success", true);
			returnMap.put("msg", "创建专题信息保存成功！");
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "创建专题信息保存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "无线小编[{0}]修改了ID为[{1}]的专题。", module = 4)
	@ResponseBody
	@RequestMapping(value = "/editSubject", method = RequestMethod.POST)
	public String editSubject(HttpServletRequest request, HttpServletResponse response, @RequestParam String id) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		try {
			appService.eidtSubject(map);
			returnMap.put("msg", "编辑专题信息保存成功！");
			returnMap.put("success", true);
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑专题信息保存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/hotkeyword", method = RequestMethod.GET)
	public String hotkeyword(HttpServletRequest request, Model model) {
		return "fxl/app/appTopSearchQuerie";
	}

	/**
	 * 获取配置类别列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getTopSearchQuerieList", method = RequestMethod.POST)
	public String getTopSearchQuerieList(HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		List<Map<String, Object>> list = appService.getTopSearchQuerieList(map);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", list.size());
		returnMap.put("recordsFiltered", list.size());
		returnMap.put("data", list);
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/tag", method = RequestMethod.GET)
	public String tag(HttpServletRequest request, Model model) {
		return "fxl/app/tag";
	}

	/**
	 * 获取配置类别列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getTagList", method = RequestMethod.POST)
	public String getConfigList(HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		List<Map<String, Object>> list = appService.getTagList(map);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", list.size());
		returnMap.put("recordsFiltered", list.size());
		returnMap.put("data", list);
		return mapper.toJson(returnMap);
	}

	/**
	 * 获取栏目所属活动列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getEventListByChannelId", method = RequestMethod.POST)
	public String getEventListByChannelId(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(required = false) String channelId) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		List<Map<String, Object>> list = Lists.newArrayList();
		if (StringUtils.isNotBlank(channelId)) {
			list = appService.getEventListByChannelId(map);
		}
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", list.size());
		returnMap.put("recordsFiltered", list.size());
		returnMap.put("data", list);
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/indexItem", method = RequestMethod.GET)
	public String indexItem(HttpServletRequest request, HttpServletResponse response, Model model) {
		model.addAttribute("cityId", 1);
		model.addAttribute("isVisibleMap", DictionaryUtil.getStatueMap(DictionaryUtil.YesNo));
		model.addAttribute("channelFrontTypeMap", DictionaryUtil.getStatueMap(DictionaryUtil.ChannelFrontType));
		model.addAttribute("sliderUrlTypeMap", DictionaryUtil.getStatueMap(DictionaryUtil.ChannelSliderUrlType));
		model.addAttribute("isflagTypeMap", DictionaryUtil.getStatueMap(DictionaryUtil.YesNo));
		model.addAttribute("flagType", 0);
		return "fxl/app/appIndexItemManage";
	}

	/**
	 * 获取栏目轮播列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getIndexItemList", method = RequestMethod.POST)
	public String getIndexItemList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		List<Map<String, Object>> list = appService.getIndexItemList();

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", list.size());
		returnMap.put("recordsFiltered", list.size());
		returnMap.put("data", list);
		return mapper.toJson(returnMap);
	}

	/**
	 * 根据轮播ID返回轮播详细信息
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getIndexItem/{itemId}", method = RequestMethod.POST)
	public String getIndexItem(HttpServletRequest request, HttpServletResponse response, @PathVariable String itemId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TAppIndexItem tAppIndexItem = appService.getIndexItem(itemId);
			returnMap.put("id", tAppIndexItem.getId());
			returnMap.put("fimage", tAppIndexItem.getFimage());
			returnMap.put("ffrontType", tAppIndexItem.getFfrontType());
			returnMap.put("furlType", tAppIndexItem.getFurlType());
			returnMap.put("fentityId", tAppIndexItem.getFentityId());
			returnMap.put("fentityTitle", tAppIndexItem.getFentityTitle());
			returnMap.put("fexternalUrl", tAppIndexItem.getFexternalUrl());
			returnMap.put("flocationNum", tAppIndexItem.getFlocationNum());
			returnMap.put("forder", tAppIndexItem.getForder());
			returnMap.put("fisVisible", tAppIndexItem.getFisVisible());
			returnMap.put("flagType", tAppIndexItem.getFlagType());
			String imageId = tAppIndexItem.getFimage();
			if (StringUtils.isNotBlank(imageId)) {
				TImage tImage = fxlService.getImage(Long.valueOf(imageId));
				returnMap.put("imageWidth", tImage.getImageWidth());
				returnMap.put("imageHeight", tImage.getImageHeight());
				returnMap.put("fimagePath", fxlService.getImageUrl(imageId, false));
			}
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取首页固定位信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "无线小编[{0}]创建了ID为[{1}]的首页固定位信息。", module = 4)
	@ResponseBody
	@RequestMapping(value = "/addIndexItem", method = RequestMethod.POST)
	public String addIndexItem(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			String id = appService.saveIndexItem(map);
			returnMap.put("success", true);
			returnMap.put("msg", "创建首页固定位信息保存成功！");
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id }));
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "创建首页固定位信息保存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "无线小编[{0}]修改了ID为[{1}]的首页固定位信息。", module = 4)
	@ResponseBody
	@RequestMapping(value = "/editIndexItem", method = RequestMethod.POST)
	public String editIndexItem(HttpServletRequest request, HttpServletResponse response, @RequestParam String id) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		try {
			appService.editIndexItem(map);
			returnMap.put("msg", "编辑首页固定位信息保存成功！");
			returnMap.put("success", true);
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id }));
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑首页固定位信息保存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "无线小编[{0}]删除了ID为[{1}]的首页固定位信息。", module = 4)
	@ResponseBody
	@RequestMapping(value = "/delIndexItem/{itemId}", method = RequestMethod.POST)
	public String delIndexItem(HttpServletRequest request, HttpServletResponse response, @PathVariable String itemId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			appService.delIndexItem(itemId);
			returnMap.put("success", true);
			returnMap.put("msg", "栏目首页固定位信息删除成功！");
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), itemId }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "栏目首页固定位信息删除失败！");
		}
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/columnbanner", method = RequestMethod.GET)
	public String columnbanner(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("fcity", "1");
		model.addAttribute("columnBannerMap", DictionaryUtil.getStatueMap(DictionaryUtil.ColumnBanner));
		model.addAttribute("seckillTypeMap", DictionaryUtil.getStatueMap(DictionaryUtil.TodaySeckillType));
		return "fxl/app/columnBanner";
	}

	@ResponseBody
	@RequestMapping(value = "/getColumnBannerList", method = RequestMethod.POST)
	public String getColumnBannerList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		appService.getColumnBannerList(map, page);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	@Log(message = "无线小编[{0}]创建了ID为[{1}]的栏目头图。", module = 4)
	@ResponseBody
	@RequestMapping(value = "/addColumnBanner", method = RequestMethod.POST)
	public String addColumnBanner(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			String channelId = appService.addColumnBanner(map);
			if (channelId.equals("false")) {
				returnMap.put("success", false);
				returnMap.put("msg", "创建栏目头图信息重复！");
			} else {
				returnMap.put("success", true);
				returnMap.put("msg", "创建栏目头图信息保存成功！");
				LogUitls.putArgs(
						LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), channelId }));
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "创建栏目头图信息保存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/systemStatic", method = RequestMethod.GET)
	public String systemStatic(HttpServletRequest request, Model model) {

		return "fxl/appSystem/systemStatic";
	}

	@ResponseBody
	@RequestMapping(value = "/getSystemStaticList", method = RequestMethod.POST)
	public String getSystemStaticList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		appService.getSystemStaticList(map, page);

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
	@RequestMapping(value = "/getColumnBanner/{bannerId}", method = RequestMethod.POST)
	public String getColumnBanner(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String bannerId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TColumnBanner columnBanner = columnBannerDAO.findOne(bannerId);
			returnMap.put("fstatus", columnBanner.getFstatus());
			returnMap.put("ftag", columnBanner.getFtag());
			returnMap.put("ftype", columnBanner.getFtype());
			returnMap.put("id", columnBanner.getId());
			if (columnBanner.getFchannelId() != null) {
				returnMap.put("channelId", columnBanner.getFchannelId());
			}
			String imageId = columnBanner.getFimageUrl();
			returnMap.put("fimage", imageId);
			if (StringUtils.isNotBlank(imageId)) {
				TImage tImage = fxlService.getImage(Long.valueOf(imageId));
				returnMap.put("imageWidth", tImage.getImageWidth());
				returnMap.put("imageHeight", tImage.getImageHeight());
				returnMap.put("fimagePath", fxlService.getImageUrl(imageId, true));
			}
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取栏目头图详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "无线小编[{0}]删除了ID为[{1}]，栏目名称为[{2}]的APP栏目。", module = 4)
	@ResponseBody
	@RequestMapping(value = "/delBanner/{id}", method = RequestMethod.POST)
	public String delBanner(HttpServletRequest request, HttpServletResponse response, @PathVariable String id) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			appService.delBanner(id);
			returnMap.put("msg", "删除栏目头图信息成功！");
			returnMap.put("success", true);
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id, "" }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "删除栏目头图信息失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "无线小编[{0}]编辑了ID为[{1}]的APP栏目。", module = 4)
	@ResponseBody
	@RequestMapping(value = "/editBanner", method = RequestMethod.POST)
	public String editBanner(HttpServletRequest request, HttpServletResponse response, @RequestParam String id) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		try {
			appService.editBanner(map);
			returnMap.put("msg", "编辑栏目头图信息保存成功！");
			returnMap.put("success", true);
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑栏目头图信息保存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/appNotice", method = RequestMethod.GET)
	public String appNotice(HttpServletRequest request, Model model) {
		model.addAttribute("cityId", 1);
		model.addAttribute("sliderUrlTypeMap", DictionaryUtil.getStatueMap(DictionaryUtil.ChannelSliderUrlType));
		model.addAttribute("typeNoticeMap", DictionaryUtil.getStatueMap(DictionaryUtil.ChannelSliderUrlType));
		return "fxl/app/appnotice/appNotice";
	}
	
	
	@ResponseBody
	@RequestMapping(value = "/getAppNoticeList", method = RequestMethod.POST)
	public String getAppNoticeList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		List<Map<String, Object>> list = appService.getAppNoticeList();

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", list.size());
		returnMap.put("recordsFiltered", list.size());
		returnMap.put("data", list);
		return mapper.toJson(returnMap);
	}
	
	/**
	 * 根据轮播ID返回轮播详细信息
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getAppNoticeDetail/{appNoticeId}", method = RequestMethod.POST)
	public String getAppNoticeDetail(HttpServletRequest request, HttpServletResponse response, @PathVariable String appNoticeId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TAppNotice tAppNotice = appService.getTAppNotice(appNoticeId);
			
			returnMap.put("id", tAppNotice.getId());
			returnMap.put("fnoticeName", tAppNotice.getFnoticeName());
			returnMap.put("fnoticeType", tAppNotice.getFnoticeType());
			
			returnMap.put("fnoticeTitle", tAppNotice.getFnoticeTitle());
			returnMap.put("fnoticeUrl", tAppNotice.getFnoticeUrl());
			returnMap.put("fnoticeId", tAppNotice.getFnoticeId());
			returnMap.put("forder", tAppNotice.getForder());
			
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取APP公告信息时失败！");
		}
		return mapper.toJson(returnMap);
	}
	
	@ResponseBody
	@RequestMapping(value = "/addAppNotice", method = RequestMethod.POST)
	public String addAppNotice(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			String id = appService.addAppNotice(map);
			returnMap.put("success", true);
			returnMap.put("msg", "创建APP公告信息保存成功！");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "创建APP公告信息保存失败！");
		}
		return mapper.toJson(returnMap);
	}
	
	@ResponseBody
	@RequestMapping(value = "/delAppNotice/{appNoticeId}", method = RequestMethod.POST)
	public String delAppNotice(HttpServletRequest request, HttpServletResponse response, @PathVariable String appNoticeId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			appService.delAppNotice(appNoticeId);
			returnMap.put("success", true);
			returnMap.put("msg", "APP公告信息删除成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "APP公告信息删除失败！");
		}
		return mapper.toJson(returnMap);
	}
	
	@ResponseBody
	@RequestMapping(value = "/editAppNotice", method = RequestMethod.POST)
	public String editAppNotice(HttpServletRequest request, HttpServletResponse response, @RequestParam String id) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		try {
			appService.editAppNotice(map);
			returnMap.put("msg", "编辑APP公告信息成功！");
			returnMap.put("success", true);
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑APP公告信息失败！");
		}
		return mapper.toJson(returnMap);
	}
}