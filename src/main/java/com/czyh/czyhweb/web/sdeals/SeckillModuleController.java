package com.czyh.czyhweb.web.sdeals;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.czyh.czyhweb.entity.TEvent;
import com.czyh.czyhweb.entity.TSeckillModule;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.sdeals.SeckillModuleService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.DictionaryUtil;

/**
 * 秒杀模块管理
 * 
 * @author jinsey
 * 
 */
@Controller
@RequestMapping("/fxl/seckill")
public class SeckillModuleController {

	private static Logger logger = LoggerFactory.getLogger(SeckillModuleController.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private SeckillModuleService seckillModuleService;

	@RequestMapping(value = "/seckillView", method = RequestMethod.GET)
	public String seckillView(HttpServletRequest request, Model model) {

		model.addAttribute("seckilModulelListMap", seckillModuleService.seckilModulelList());
		model.addAttribute("todaySeckillTypeMap", DictionaryUtil.getStatueMap(DictionaryUtil.TodaySeckillType));
		model.addAttribute("SeckillModuleTypeMap", DictionaryUtil.getStatueMap(DictionaryUtil.SeckillModuleType));

		return "fxl/seckillModule/seckillModuleView";
	}

	/**
	 * 获取秒杀商品列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getSeckillList", method = RequestMethod.POST)
	public String getSeckillList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		seckillModuleService.getSeckillList(map, page);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/addSeckillModule", method = RequestMethod.POST)
	public String addSeckillModule(HttpServletRequest request, HttpServletResponse response) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			seckillModuleService.saveSeckillModule(map);
			returnMap.put("success", true);
			returnMap.put("msg", "今日秒杀商品信息保存成功！");
		} catch (ServiceException e) {
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "今日秒杀商品信息保存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/getseckillModule/{goodsId}", method = RequestMethod.POST)
	public String getseckillModule(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String goodsId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TSeckillModule tSeckillModule = seckillModuleService.getSeckillModule(goodsId);
			TEvent tEvent = seckillModuleService.getGoodsModule(tSeckillModule.getFgoodsId());

			returnMap.put("goodsId", tEvent.getId());
			returnMap.put("fgoodsTitle", tEvent.getFtitle());
			returnMap.put("fgoodsSubTitle", tEvent.getFsubTitle());
			returnMap.put("fgoodsPrice", tEvent.getFprice());
			returnMap.put("fgoodsPriceMoney", tEvent.getFpriceMoney());
			returnMap.put("fgoodsLimitation", tEvent.getFlimitation());
			returnMap.put("fimage", tEvent.getFimage1());

			returnMap.put("ftodaySeckillType", tSeckillModule.getFtodaySeckillType());
			returnMap.put("ftype", tSeckillModule.getFtype());

			returnMap.put("ftodaySeckillTypeString", DictionaryUtil.getString(DictionaryUtil.TodaySeckillType,
					tSeckillModule.getFtodaySeckillType(), shiroUser.getLanguage()));
			returnMap.put("ftypeString", DictionaryUtil.getString(DictionaryUtil.SeckillModuleType,
					tSeckillModule.getFtype(), shiroUser.getLanguage()));
			returnMap.put("fgoodsCreateTime", tSeckillModule.getFgoodsCreateTime());

			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取秒杀商品信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/editSeckillModule", method = RequestMethod.POST)
	public String editSeckillModule(HttpServletRequest request, HttpServletResponse response) {

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		try {
			seckillModuleService.editSeckillModule(map);
			returnMap.put("msg", "编辑秒杀商品信息成功!");
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑秒杀商品信息失败!");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/delSeckillModule/{goodsId}", method = RequestMethod.POST)
	public String delSeckillModule(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String goodsId) {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			seckillModuleService.delSeckillModule(goodsId);
			returnMap.put("success", true);
			returnMap.put("msg", "秒杀商品信息删除成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "秒杀商品信息删除失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 秒杀商品上架
	 * 
	 * @param request
	 * @param model
	 * @param goodsId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/onSaleSeckillModule/{goodsId}", method = RequestMethod.POST)
	public String onSaleSeckillModule(HttpServletRequest request, Model model, @PathVariable String goodsId) {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			seckillModuleService.onSaleSeckillModule(goodsId, 20);
			returnMap.put("success", true);
			returnMap.put("msg", "秒杀商品即刻上架操作成功！");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "秒杀商品即刻上架操作失败！");
		}

		return mapper.toJson(returnMap);
	}

	/**
	 * 秒杀商品下架
	 * 
	 * @param request
	 * @param model
	 * @param goodsId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/offSaleSeckillModule/{goodsId}", method = RequestMethod.POST)
	public String offSaleSeckillModule(HttpServletRequest request, Model model, @PathVariable String goodsId) {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			seckillModuleService.onSaleSeckillModule(goodsId, 10);
			returnMap.put("success", true);
			returnMap.put("msg", "秒杀商品即刻下架操作成功！");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "秒杀商品即刻下架操作失败！");
		}

		return mapper.toJson(returnMap);
	}

}