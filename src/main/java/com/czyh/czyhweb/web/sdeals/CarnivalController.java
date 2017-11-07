package com.czyh.czyhweb.web.sdeals;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;
import org.springside.modules.web.Servlets;

import com.czyh.czyhweb.entity.TCarnival;
import com.czyh.czyhweb.entity.TCarnivalPrize;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.sdeals.CarnivalService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.PropertiesUtil;

/**
 * 嘉年华管理
 * 
 * @author jinsey
 *
 */
@Controller
@RequestMapping("/fxl/carnival")
public class CarnivalController {

	private static Logger logger = LoggerFactory.getLogger(CarnivalController.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private CarnivalService carnivalService;

	@RequestMapping(value = "/eventCarnival", method = RequestMethod.GET)
	public String eventCarnival(HttpServletRequest request, Model model) {
		model.addAttribute("carnivalStatusMap", DictionaryUtil.getStatueMap(DictionaryUtil.CarnivalStatus));
		return "fxl/carnival/eventCarnival";
	}

	@ResponseBody
	@RequestMapping(value = "/getEventCarnivalList", method = RequestMethod.POST)
	public String getEventCarnivalList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		carnivalService.getEventCarnivalList(map, page);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());

		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/addCarnival", method = RequestMethod.POST)
	public String addCarnival(HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			carnivalService.addCarnival(map);
			returnMap.put("success", true);
			returnMap.put("msg", "嘉年华活动信息保存成功！");
		} catch (ServiceException se) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(se)));
			returnMap.put("success", false);
			returnMap.put("msg", se.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "嘉年华活动信息保存失败！");
		}

		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/delCarnival/{carnivalId}", method = RequestMethod.POST)
	public String delCarnival(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String carnivalId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			carnivalService.delCarnival(carnivalId);
			returnMap.put("success", true);
			returnMap.put("msg", "嘉年华活动信息删除成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "嘉年华活动信息删除失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/editCarnival", method = RequestMethod.POST)
	public String editCarnival(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();

		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		try {
			carnivalService.editCarnival(map);
			returnMap.put("msg", "嘉年华活动信息编辑成功!");
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "嘉年华活动信息编辑失败!");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/getCarnivalDetail/{carnivalId}", method = RequestMethod.POST)
	public String getEventBonus(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String carnivalId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TCarnival tCarnival = carnivalService.getCarnivalDetail(carnivalId);

			returnMap.put("ftitle", tCarnival.getFtitle());
			returnMap.put("fstartTime", DateFormatUtils.format(tCarnival.getFstartTime(), "yyyy-MM-dd"));
			returnMap.put("fendTime", DateFormatUtils.format(tCarnival.getFendTime(), "yyyy-MM-dd"));
			returnMap.put("fdayNumber", tCarnival.getFdayNumber());
			returnMap.put("fimage", tCarnival.getFimage());
			returnMap.put("fchannel", tCarnival.getFchannel());
			returnMap.put("flotteryNumber", tCarnival.getFlotteryNumber());
			returnMap.put("fcredentialNumber", tCarnival.getFcredentialNumber());
			returnMap.put("fodds", tCarnival.getFodds());
			returnMap.put("frule", tCarnival.getFrule());
			returnMap.put("fstatus", tCarnival.getFstatus());
			returnMap.put("carnivalUrl", PropertiesUtil.getProperty("carnivalUrl") + tCarnival.getId());

			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取嘉年华详细信息失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/onSaleCarnival/{carnivalId}", method = RequestMethod.POST)
	public String onSale(HttpServletRequest request, Model model, @PathVariable String carnivalId) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			carnivalService.onAndOffCarnival(carnivalId, 20);
			returnMap.put("success", true);
			returnMap.put("msg", "嘉年华活动上架操作成功！");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "嘉年华活动上架操作失败！");
		}

		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/offsaleCarnival/{carnivalId}", method = RequestMethod.POST)
	public String offsaleBonus(HttpServletRequest request, Model model, @PathVariable String carnivalId) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			carnivalService.onAndOffCarnival(carnivalId, 10);
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

		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/gift/{carnivalId}", method = RequestMethod.GET)
	public String gift(HttpServletRequest request, Model model, @PathVariable String carnivalId) {

		model.addAttribute("carnivalId", carnivalId);
		model.addAttribute("eventPrizeListMap", carnivalService.getEventPrizeList());
		model.addAttribute("carnivalPrizeMap", DictionaryUtil.getStatueMap(DictionaryUtil.CarnivalPrize));
		model.addAttribute("carnivalrCredentiaMap", DictionaryUtil.getStatueMap(DictionaryUtil.CarnivalrCredentia));
		// model.addAttribute("carnivalrDaysPrizeMap",
		// carnivalService.getDaysPrizeList(carnivalId));

		return "fxl/carnival/carnivalGift";
	}

	@ResponseBody
	@RequestMapping(value = "/getCarnivalPrizeList/{carnivalId}", method = RequestMethod.POST)
	public String getCarnivalPrizeList(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String carnivalId) {

		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		carnivalService.getCarnivalPrizeList(map, page, carnivalId);

		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());

		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/addCarnivalPrize", method = RequestMethod.POST)
	public String addCarnivalPrize(HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			carnivalService.addCarnivalPrize(map);
			returnMap.put("success", true);
			returnMap.put("msg", "嘉年华奖品信息保存成功！");
		} catch (ServiceException se) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(se)));
			returnMap.put("success", false);
			returnMap.put("msg", se.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "嘉年华奖品信息保存失败！");
		}

		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/delCarnivalPrize/{carnivalId}", method = RequestMethod.POST)
	public String delCarnivalPrize(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String carnivalId) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			carnivalService.delCarnivalPrize(carnivalId);
			returnMap.put("success", true);
			returnMap.put("msg", "嘉年华奖品信息删除成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "嘉年华奖品信息删除失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/getCarnivalPrizeDaysList", method = { RequestMethod.GET, RequestMethod.POST })
	public String getCarnivalPrizeDaysList(HttpServletRequest request, HttpServletResponse response,
			@RequestParam String prizeId) {

		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		carnivalService.getCarnivalPrizeDaysList(map, page, prizeId);

		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());

		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/editCarnivalPrzieDays", method = RequestMethod.POST)
	public String editCarnivalPrzieDays(HttpServletRequest request, HttpServletResponse response,
			@RequestParam() String id, @RequestParam() Integer number, @RequestParam() String startTime) {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			carnivalService.editCarnivalPrzieDays(id, number, startTime);
			returnMap.put("msg", "嘉年华活动信息编辑成功!");
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "嘉年华活动信息编辑失败!");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/getCarnivalPrizeDetail/{carnivalId}", method = RequestMethod.POST)
	public String getCarnivalPrizeDetail(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String carnivalId) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TCarnivalPrize tCarnivalPrize = carnivalService.getCarnivalPrizeDetail(carnivalId);

			returnMap.put("ftitle", tCarnivalPrize.getFtitle());
			returnMap.put("fimage", tCarnivalPrize.getFimage());
			returnMap.put("flevel", tCarnivalPrize.getFlevel());
			returnMap.put("fcount", tCarnivalPrize.getFcount());
			returnMap.put("frule", tCarnivalPrize.getFrule());
			returnMap.put("feventId", tCarnivalPrize.getFeventId());

			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取嘉年华奖品详细信息失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/editCarnivalPrize", method = RequestMethod.POST)
	public String editCarnivalPrize(HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			carnivalService.editCarnivalPrize(map);
			returnMap.put("msg", "嘉年华奖品信息编辑成功!");
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "嘉年华奖品信息编辑失败!");
		}
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/getCarnivalPrizeListDetail/{carnivalId}", method = RequestMethod.GET)
	public String eventView(HttpServletRequest request, Model model, @PathVariable String carnivalId) {

		TCarnival tCarnival = carnivalService.getCarnivalDetail(carnivalId);

		model.addAttribute("ftitle", tCarnival.getFtitle());
		model.addAttribute("fstartTime", DateFormatUtils.format(tCarnival.getFstartTime(), "yyyy-MM-dd"));
		model.addAttribute("fendTime", DateFormatUtils.format(tCarnival.getFendTime(), "yyyy-MM-dd"));
		model.addAttribute("fdayNumber", tCarnival.getFdayNumber());
		model.addAttribute("fimage", tCarnival.getFimage());
		model.addAttribute("fchannel", tCarnival.getFchannel());
		model.addAttribute("flotteryNumber", tCarnival.getFlotteryNumber());
		model.addAttribute("fcredentialNumber", tCarnival.getFcredentialNumber());
		model.addAttribute("fodds", tCarnival.getFodds());
		model.addAttribute("frule", tCarnival.getFrule());
		model.addAttribute("fstatus", DictionaryUtil.getString(DictionaryUtil.CarnivalStatus, tCarnival.getFstatus()));
		model.addAttribute("carnivalUrl", PropertiesUtil.getProperty("carnivalUrl") + tCarnival.getId());

		Map<Integer, List<Map<String, Object>>> map = carnivalService.getCarnivalPrizeListDetail(carnivalId);
		model.addAttribute("cPrizeOneList", map.get(1));
		model.addAttribute("cPrizeTwoList", map.get(2));
		model.addAttribute("cPrizeThreeList", map.get(3));
		return "fxl/carnival/carnivalPrizeDetail";
	}

}
