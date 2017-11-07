package com.czyh.czyhweb.web.sdeals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
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
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;
import org.springside.modules.utils.Identities;
import org.springside.modules.web.Servlets;

import com.czyh.czyhweb.dao.OrderBonusDAO;
import com.czyh.czyhweb.entity.TEventBonus;
import com.czyh.czyhweb.entity.TImage;
import com.czyh.czyhweb.entity.TOrderBonus;
import com.czyh.czyhweb.entity.TPoster;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.log.Log;
import com.czyh.czyhweb.log.LogMessageObject;
import com.czyh.czyhweb.log.impl.LogUitls;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.FxlService;
import com.czyh.czyhweb.service.sdeals.BonusService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.PropertiesUtil;

/**
 * 积分系统
 * 
 * @author jinsey
 *
 */

@Controller
@RequestMapping("/fxl/bonus")
public class BonusController {

	private static Logger logger = LoggerFactory.getLogger(BonusController.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private BonusService bonusService;

	@Autowired
	private FxlService fxlService;

	@Autowired
	private OrderBonusDAO orderBonusDAO;

	@RequestMapping(value = "/eventBonus", method = RequestMethod.GET)
	public String eventBonus(HttpServletRequest request, Model model) {

		model.addAttribute("deliveyListMap", bonusService.getDeliveryList());
		model.addAttribute("eventBonusListMap", bonusService.getEventBonusList());
		model.addAttribute("bonusStatusMap", DictionaryUtil.getStatueMap(DictionaryUtil.BonusStatus));
		model.addAttribute("bonusTypeMap", DictionaryUtil.getStatueMap(DictionaryUtil.EventBonusType));
		model.addAttribute("customerLevelMap", bonusService.getcustomerLevelMap());
		return "fxl/bonus/eventBonus";
	}

	/**
	 * 获取积分商城货品列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getEventBonusList", method = RequestMethod.POST)
	public String getEventBonusList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		bonusService.getEventBonusList(map, page);

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
	@RequestMapping(value = "/delEventBonus/{eventId}", method = RequestMethod.POST)
	public String delEventBonus(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String eventId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			bonusService.delEventBonus(eventId);
			returnMap.put("success", true);
			returnMap.put("msg", "该商品信息删除成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "该商品信息删除失败！");
		}
		// LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[]
		// { shiroUser.getName(), eventId }));
		return mapper.toJson(returnMap);
	}

	// @Log(message = "活动小编[{0}]将活动ID为[{1}]的活动即刻上架 。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/onSaleBonus/{eventId}", method = RequestMethod.POST)
	public String onSale(HttpServletRequest request, Model model, @PathVariable String eventId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			bonusService.onAndOffBonus(eventId, 20);
			returnMap.put("success", true);
			returnMap.put("msg", "商品即刻上架操作成功！");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "商品即刻上架操作失败！");
		}
		// LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[]
		// { shiroUser.getName(), eventId }));
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/offsaleBonus/{eventId}", method = RequestMethod.POST)
	public String offsaleBonus(HttpServletRequest request, Model model, @PathVariable String eventId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			bonusService.onAndOffBonus(eventId, 10);
			returnMap.put("success", true);
			returnMap.put("msg", "商品即刻下架操作成功！");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "商品即刻下架操作失败！");
		}
		// LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[]
		// { shiroUser.getName(), eventId }));
		return mapper.toJson(returnMap);
	}

	// @Log(message = "活动小编[{0}]进行活动基本信息保存。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/addBonus", method = RequestMethod.POST)
	public String addBonus(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			// System.out.println(map.get("eventId").toString()+"xxxxxxxxx");
			bonusService.addBonus(map);
			returnMap.put("success", true);
			returnMap.put("msg", "积分商品信息保存成功！");
		} catch (ServiceException se) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(se)));
			returnMap.put("success", false);
			returnMap.put("msg", se.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "积分商品信息保存失败！");
		}
		// LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[]
		// { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/getEventBonus/{eventBonusId}", method = RequestMethod.POST)
	public String getEventBonus(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String eventBonusId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TEventBonus tEventBonus = bonusService.getTEventBonus(eventBonusId);
			returnMap.put("eventId", tEventBonus.getTEvent().getId());
			returnMap.put("fstartDate", DateFormatUtils.format(tEventBonus.getFstartDate(), "yyyy-MM-dd"));
			returnMap.put("fendDate", DateFormatUtils.format(tEventBonus.getFendDate(), "yyyy-MM-dd"));
			returnMap.put("fprice", tEventBonus.getFprice());
			returnMap.put("fbonus", tEventBonus.getFbonus());
			/*
			 * returnMap.put("fuseDate",
			 * DateFormatUtils.format(tEventBonus.getFuseDate(),
			 * "yyyy-MM-dd HH:mm")); returnMap.put("fusePerson",
			 * tEventBonus.getFusePerson());
			 */
			returnMap.put("funit", tEventBonus.getFunit());
			returnMap.put("fcustomerLevel", tEventBonus.getFlevel());
			returnMap.put("fstorage", tEventBonus.getFstorage());
			returnMap.put("faddress", tEventBonus.getFaddress());
			returnMap.put("fuseType", tEventBonus.getFuseType());
			returnMap.put("fuseNote", tEventBonus.getFuseNote());
			returnMap.put("ftype", tEventBonus.getFtype());
			returnMap.put("flimitation", tEventBonus.getFlimitation());
			returnMap.put("fdeal", tEventBonus.getFdeal());
			returnMap.put("forder", tEventBonus.getForder());
			returnMap.put("ftitle", tEventBonus.getFtitle());

			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取商品详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "用户管理员[{0}]对该商家用户进行编辑。", module = 7)
	@ResponseBody
	@RequestMapping(value = "/editBonus", method = RequestMethod.POST)
	public String editBonus(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		try {
			bonusService.editBonus(map);
			returnMap.put("msg", "编辑商品信息成功!");
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑商品信信息失败!");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 获取活动海报列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getPosterList", method = RequestMethod.POST)
	public String getPosterList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		bonusService.getPosterList(map, page);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	/**
	 * 海报配置页面
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/eventPoster", method = RequestMethod.GET)
	public String eventPoster(HttpServletRequest request, Model model) {

		model.addAttribute("eventBonusListMap", bonusService.getEventBonusList());
		model.addAttribute("posterStatusMap", DictionaryUtil.getStatueMap(DictionaryUtil.PosterStatus));

		return "fxl/bonus/eventBonusPoster";
	}

	// @Log(message = "活动小编[{0}]创建了ID为[{1}]的活动海报。", module = 4)
	@ResponseBody
	@RequestMapping(value = "/addPoster", method = RequestMethod.POST)
	public String addChannel(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			bonusService.savePoster(map);

			returnMap.put("success", true);
			returnMap.put("msg", "保存海报信息保存成功！");
			// LogUitls.putArgs(LogMessageObject.newWrite().setObjects(newObject[]
			// { shiroUser.getName(), channelId }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "保存海报信息保存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/getPoster/{posterId}", method = RequestMethod.POST)
	public String getChannel(HttpServletRequest request, HttpServletResponse response, @PathVariable String posterId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TPoster tPoster = bonusService.getPoster(posterId);

			returnMap.put("id", tPoster.getId());
			returnMap.put("ftitle", tPoster.getFtitle());
			returnMap.put("fstartTime", DateFormatUtils.format(tPoster.getFstartTime(), "yyyy-MM-dd"));
			returnMap.put("fendTime", DateFormatUtils.format(tPoster.getFendTime(), "yyyy-MM-dd"));
			returnMap.put("fremark", tPoster.getFremark());
			returnMap.put("fimageWidth", tPoster.getFimageWidth());
			returnMap.put("fimageHeight", tPoster.getFimageHeight());
			returnMap.put("fwaterMark", tPoster.getFwaterMark());
			returnMap.put("fqrcodeWh", tPoster.getFqrcodeWh());
			String[] fqrcodeXY = tPoster.getFqrcodeXy().split(";");
			returnMap.put("fqrcodeX", fqrcodeXY[0]);
			returnMap.put("fqrcodeY", fqrcodeXY[1]);
			String imageId = tPoster.getFimage();
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

	// @Log(message = "活动小编[{0}]创建了ID为[{1}]的活动海报。", module = 4)
	@ResponseBody
	@RequestMapping(value = "/editorPoster", method = RequestMethod.POST)
	public String editChannel(HttpServletRequest request, HttpServletResponse response, @RequestParam String id) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		try {
			bonusService.editorPoster(map);
			returnMap.put("msg", "编辑APP栏目信息保存成功！");
			returnMap.put("success", true);
			// LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new
			// Object[] { shiroUser.getName(), id }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑APP栏目信息保存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/orderBonus", method = RequestMethod.GET)
	public String orderBonus(HttpServletRequest request, Model model) {

		model.addAttribute("bonusEventMap", bonusService.getbonusEventMapList());
		model.addAttribute("orderBonusType", DictionaryUtil.getStatueMap(DictionaryUtil.OrderBonusType));
		return "fxl/bonus/orderBonus";
	}

	/**
	 * 获取兑换订单列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getOrdeBonusList", method = RequestMethod.POST)
	public String getOrdeBonusList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		bonusService.getOrdeBonusList(map, page);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());

		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/verOrderBonus/{orderId}", method = RequestMethod.POST)
	public String verOrderBonus(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String orderId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			bonusService.verOrderBonus(orderId, null);
			returnMap.put("success", true);
			returnMap.put("msg", "兑换该订单成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "兑换该订单失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/cancelBonusOrder/{orderId}", method = RequestMethod.POST)
	public String cancelBonusOrder(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String orderId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			bonusService.cancelBonusOrder(orderId);
			returnMap.put("success", true);
			returnMap.put("msg", "取消该订单成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "取消该订单失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 根据项目ID返回兑换订单详细信息
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getBonusOrder/{orderId}", method = RequestMethod.POST)
	public String getBonusOrder(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String orderId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		try {
			TOrderBonus tOrderBonus = orderBonusDAO.findOne(orderId);
			returnMap.put("id", tOrderBonus.getId());
			returnMap.put("feventTitle", tOrderBonus.getTEventBonus().getTEvent().getFtitle());
			returnMap.put("fbonus", tOrderBonus.getTEventBonus().getFbonus());
			returnMap.put("customerName", tOrderBonus.getFcustomerName());
			returnMap.put("customerPhone", tOrderBonus.getFcustomerPhone());
			returnMap.put("fstatusString", DictionaryUtil.getString(DictionaryUtil.OrderBonusType,
					tOrderBonus.getFstatus(), shiroUser.getLanguage()));
			returnMap.put("address", tOrderBonus.getFexpress());
			returnMap.put("remark", tOrderBonus.getFremark());
			returnMap.put("note", tOrderBonus.getFnote());
			returnMap.put("fbonusPrice", tOrderBonus.getFtotal());

			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取活动详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	// @Log(message = "活动小编[{0}]将活动ID为[{1}]的活动即刻上架 。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/onPoster/{posterId}", method = RequestMethod.POST)
	public String onPoster(HttpServletRequest request, Model model, @PathVariable String posterId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			bonusService.onPoster(posterId, 20);
			returnMap.put("success", true);
			returnMap.put("msg", "海报立即上架操作成功！");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "海报立即上架操作失败！");
		}
		// LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[]
		// { shiroUser.getName(), eventId }));
		return mapper.toJson(returnMap);
	}

	// @Log(message = "活动小编[{0}]将活动ID为[{1}]的活动即刻下架 。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/offPoster/{posterId}", method = RequestMethod.POST)
	public String offPoster(HttpServletRequest request, Model model, @PathVariable String posterId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			bonusService.offPoster(posterId, 10);
			returnMap.put("success", true);
			returnMap.put("msg", "海报立即下架操作成功！");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "海报立即下架操作失败！");
		}
		// LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[]
		// { shiroUser.getName(), eventId }));
		return mapper.toJson(returnMap);
	}

	// @Log(message = "活动小编[{0}]活动ID为[{1}]的活动信息进行删除。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/delPoster/{posterId}", method = RequestMethod.POST)
	public String delPoster(HttpServletRequest request, HttpServletResponse response, @PathVariable String posterId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			bonusService.delPoster(posterId);
			returnMap.put("success", true);
			returnMap.put("msg", "海报信息删除成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "海报信息删除失败！");
		}
		// LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[]
		// { shiroUser.getName(), eventId }));
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/saveCsRemark", method = RequestMethod.POST)
	public String saveCsRemark(HttpServletRequest request, Model model, @RequestParam String id,
			@RequestParam String csRemark) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String newCsRemark = bonusService.saveCsRemark(id, csRemark);
			returnMap.put("success", true);
			returnMap.put("newCsRemark", newCsRemark);
			returnMap.put("msg", "客服备注订单信息保存成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "客服备注订单信息保存失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/bonusRank", method = RequestMethod.GET)
	public String bonusRank(HttpServletRequest request, Model model) {

		return "fxl/bonus/bonusRank";
	}

	/**
	 * 获取兑换订单列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getBonusRank", method = RequestMethod.POST)
	public String getBonusRank(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		bonusService.getBonusRank(map, page);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());

		return mapper.toJson(returnMap);
	}

	/**
	 * 用户导出积分excel操作时生成excel文件的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/createBonusOrderExcel", method = RequestMethod.POST)
	public String createBonusOrderExcel(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		String sessionid = request.getSession().getId();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String excelFileName = Identities.uuid2();
			String datePath = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
			bonusService.createBonusOrderExcel(map, datePath, excelFileName, sessionid);
			returnMap.put("success", true);
			returnMap.put("datePath", datePath);
			returnMap.put("excelFileName", excelFileName);
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "导出积分订单excel文件操作失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/bonusOrderExcel/{datePath}/{excelFileName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public String bonusOrderExcel(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String datePath, @PathVariable String excelFileName) {
		InputStream is = null;
		OutputStream os = null;

		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("bonusOrderExcel")).append("/").append(datePath).append("/")
				.append(excelFileName).append(".xlsx");
		try {
			response.setHeader("Content-Disposition", "attachment; filename="
					+ URLEncoder.encode(new StringBuilder("积分订单报表").append(".xlsx").toString(), "UTF-8"));
			response.setContentType("application/vnd.ms-excel;charset=UTF-8");
			os = response.getOutputStream();
			File excelFile = new File(rootPath.toString());
			is = FileUtils.openInputStream(excelFile);
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

	@RequestMapping(value = "/bonusIssue", method = RequestMethod.GET)
	public String bonusIssue(HttpServletRequest request, Model model) {

		model.addAttribute("eventBonusListMap", bonusService.getEventBonusList());
		model.addAttribute("bonusStatusMap", DictionaryUtil.getStatueMap(DictionaryUtil.BonusStatus));
		model.addAttribute("bonusTypeMap", DictionaryUtil.getStatueMap(DictionaryUtil.EventBonusType));
		return "fxl/bonus/bonusIssue";
	}

	/**
	 * 获取积分商城货品列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getIssueBonusList", method = RequestMethod.POST)
	public String getIssueBonusList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		bonusService.getIssueBonusList(map, page);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());

		return mapper.toJson(returnMap);
	}

	// @Log(message = "活动小编[{0}]进行活动基本信息保存。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/addIssueBonus", method = RequestMethod.POST)
	public String addIssueBonus(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			// System.out.println(map.get("eventId").toString()+"xxxxxxxxx");
			bonusService.addIssueBonus(map);
			returnMap.put("success", true);
			returnMap.put("msg", "发放积分成功！");
		} catch (ServiceException se) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(se)));
			returnMap.put("success", false);
			returnMap.put("msg", se.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "发放积分失败！");
		}
		// LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[]
		// { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/verOrderBonusPush/{orderId}/{pushContent}", method = RequestMethod.POST)
	public String verOrderBonusPush(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String orderId, @PathVariable String pushContent) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			bonusService.verOrderBonus(orderId, pushContent);
			returnMap.put("success", true);
			returnMap.put("msg", "兑换该订单成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "兑换该订单失败！");
		}
		return mapper.toJson(returnMap);
	}

}