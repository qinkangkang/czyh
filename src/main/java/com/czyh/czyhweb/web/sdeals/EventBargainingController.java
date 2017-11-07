package com.czyh.czyhweb.web.sdeals;

import java.util.HashMap;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;
import org.springside.modules.web.Servlets;

import com.czyh.czyhweb.entity.TEventBargaining;
import com.czyh.czyhweb.entity.TImage;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.FxlService;
import com.czyh.czyhweb.service.sdeals.BonusService;
import com.czyh.czyhweb.service.sdeals.EventBargainingService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.PropertiesUtil;

/**
 * 砍一砍营销活动
 * 
 * @author jinsey
 *
 */
@Controller
@RequestMapping("/fxl/eventBargaining")
public class EventBargainingController {

	private static Logger logger = LoggerFactory.getLogger(EventBargainingController.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private FxlService fxlService;

	@Autowired
	private EventBargainingService cutMessageService;

	@Autowired
	private BonusService bonusService;

	@RequestMapping(value = "/bargaining", method = RequestMethod.GET)
	public String cutIndex(HttpServletRequest request, Model model) {

		model.addAttribute("eventBonusListMap", bonusService.getEventBonusList());
		model.addAttribute("bonusStatusMap", DictionaryUtil.getStatueMap(DictionaryUtil.BonusStatus));
		model.addAttribute("bonusTypeMap", DictionaryUtil.getStatueMap(DictionaryUtil.EventBonusType));

		return "fxl/eventBargaining/eventBargaining";
	}

	@ResponseBody
	@RequestMapping(value = "/getBargainingList", method = RequestMethod.POST)
	public String getBargainingList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		cutMessageService.getBargainingList(map, page);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());

		return mapper.toJson(returnMap);
	}

	// @Log(message = "活动小编[{0}]活动ID为[{1}]的活动信息进行删除。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/delEventBargaining/{eventBargainingId}", method = RequestMethod.POST)
	public String delCutMessage(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String eventBargainingId) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			cutMessageService.delEventBargaining(eventBargainingId);
			returnMap.put("success", true);
			returnMap.put("msg", "活动信息删除成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "活动品信息删除失败！");
		}
		return mapper.toJson(returnMap);
	}

	// @Log(message = "活动小编[{0}]将活动ID为[{1}]的活动即刻上架 。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/onSaleBargaining/{eventBargainingId}", method = RequestMethod.POST)
	public String onSaleBargaining(HttpServletRequest request, Model model, @PathVariable String eventBargainingId) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			cutMessageService.onBargaining(eventBargainingId, 20);
			returnMap.put("success", true);
			returnMap.put("msg", "砍一砍活动即刻上架操作成功！");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "砍一砍活动即刻上架操作失败！");
		}

		// LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[]
		// { shiroUser.getName(), eventId }));
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/offsaleBargaining/{eventBargainingId}", method = RequestMethod.POST)
	public String offsaleBargaining(HttpServletRequest request, Model model, @PathVariable String eventBargainingId) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			cutMessageService.offBargaining(eventBargainingId, 10);
			returnMap.put("success", true);
			returnMap.put("msg", "砍一砍活动即刻下架操作成功！");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "砍一砍活动即刻下架操作失败！");
		}
		return mapper.toJson(returnMap);
	}

	// @Log(message = "活动小编[{0}]进行活动基本信息保存。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/addBargaining", method = RequestMethod.POST)
	public String addBargaining(HttpServletRequest request, HttpServletResponse response) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			cutMessageService.addBargaining(map);
			returnMap.put("success", true);
			returnMap.put("msg", "砍一砍活动信息保存成功！");
		} catch (ServiceException se) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(se)));
			returnMap.put("success", false);
			returnMap.put("msg", se.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "砍一砍活动信息保存失败！");
		}
		// LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[]
		// { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/getBargaining/{eventBargainingId}", method = RequestMethod.POST)
	public String getEventBargaining(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String eventBargainingId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TEventBargaining tEventBargaining = cutMessageService.gettEventBargaining(eventBargainingId);
			returnMap.put("feventId", tEventBargaining.getFeventId());
			returnMap.put("ftitle", tEventBargaining.getFtitle());
			returnMap.put("feventtitle", tEventBargaining.getFeventTitle());
			returnMap.put("fbeginTime", DateFormatUtils.format(tEventBargaining.getFbeginTime(), "yyyy-MM-dd HH:mm"));
			returnMap.put("fendTime", DateFormatUtils.format(tEventBargaining.getFendTime(), "yyyy-MM-dd HH:mm"));
			returnMap.put("ftype", tEventBargaining.getFtype());
			returnMap.put("finputText", tEventBargaining.getFinputText());
			returnMap.put("fpackageDesc", tEventBargaining.getFpackageDesc());
			returnMap.put("fstartPrice", tEventBargaining.getFstartPrice());
			returnMap.put("fsettlementPrice", tEventBargaining.getFsettlementPrice());
			returnMap.put("ffloorPrice1", tEventBargaining.getFfloorPrice1());
			returnMap.put("ffloorPrice2", tEventBargaining.getFfloorPrice2());
			returnMap.put("ffloorPrice3", tEventBargaining.getFfloorPrice3());
			returnMap.put("fstock1", tEventBargaining.getFstock1());
			returnMap.put("fstock2", tEventBargaining.getFstock2());
			returnMap.put("fstock3", tEventBargaining.getFstock3());
			returnMap.put("getFremainingStock1", tEventBargaining.getFremainingStock1());
			returnMap.put("getFremainingStock2", tEventBargaining.getFremainingStock2());
			returnMap.put("getFremainingStock3", tEventBargaining.getFremainingStock3());
			returnMap.put("fmaxBargaining", tEventBargaining.getFmaxBargaining());
			returnMap.put("fminBargaining", tEventBargaining.getFminBargaining());
			returnMap.put("fstatus", tEventBargaining.getFstatus());
			returnMap.put("fstatusString", DictionaryUtil.getString(DictionaryUtil.BonusStatus,
					tEventBargaining.getFstatus(), shiroUser.getLanguage()));
			returnMap.put("ftypeString", DictionaryUtil.getString(DictionaryUtil.EventBonusType,
					tEventBargaining.getFtype(), shiroUser.getLanguage()));
			StringBuilder shareUrl = new StringBuilder();
			shareUrl.append(PropertiesUtil.getProperty("shareBargainingUrl")).append("?").append("eventBargainingId=")
					.append(eventBargainingId);
			returnMap.put("bargainUrl", shareUrl.toString());

			Long imageId = tEventBargaining.getFimage();
			returnMap.put("fimage", imageId);
			if (imageId != null) {
				TImage tImage = fxlService.getImage(imageId);
				returnMap.put("imageWidth", tImage.getImageWidth());
				returnMap.put("imageHeight", tImage.getImageHeight());
				returnMap.put("fimagePath", fxlService.getImageUrl(imageId.toString(), true));
			}

			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取活动详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	// @Log(message = "用户管理员[{0}]对该商家用户进行编辑。", module = 7)
	@ResponseBody
	@RequestMapping(value = "/editBargaining", method = RequestMethod.POST)
	public String editBargaining(HttpServletRequest request, HttpServletResponse response) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		try {
			cutMessageService.editBargaining(map);
			returnMap.put("msg", "编辑活动信息成功!");
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑活动信信息失败!");
		}
		// LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[]
		// { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/bargainingRank", method = RequestMethod.GET)
	public String cutRank(HttpServletRequest request, Model model) {

		model.addAttribute("bargainingMap", DictionaryUtil.getStatueMap(DictionaryUtil.BonusStatus));
		model.addAttribute("bargainTitleMap", cutMessageService.getBargainingTitleList());
		return "fxl/eventBargaining/bargainingRank";
	}

	@ResponseBody
	@RequestMapping(value = "/getBargainingRank", method = RequestMethod.POST)
	public String getBargainingRank(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		cutMessageService.getBargainingRank(map, page);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());

		return mapper.toJson(returnMap);
	}

}