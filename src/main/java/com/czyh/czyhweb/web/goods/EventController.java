package com.czyh.czyhweb.web.goods;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.RequestContext;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;
import org.springside.modules.web.Servlets;

import com.czyh.czyhweb.dao.ImageDAO;
import com.czyh.czyhweb.entity.TEvent;
import com.czyh.czyhweb.entity.TEventCategory;
import com.czyh.czyhweb.entity.TEventSession;
import com.czyh.czyhweb.entity.TEventSpec;
import com.czyh.czyhweb.entity.TGoodsSku;
import com.czyh.czyhweb.entity.TGoodsSpaceValue;
import com.czyh.czyhweb.entity.TGoodsTypeClass;
import com.czyh.czyhweb.entity.TGoodsTypeClassCategory;
import com.czyh.czyhweb.entity.TGoodsTypeClassValue;
import com.czyh.czyhweb.entity.TImage;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.log.Log;
import com.czyh.czyhweb.log.LogMessageObject;
import com.czyh.czyhweb.log.impl.LogUitls;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.FxlService;
import com.czyh.czyhweb.service.customer.CustomerService;
import com.czyh.czyhweb.service.customer.UserService;
import com.czyh.czyhweb.service.goods.GoodsService;
import com.czyh.czyhweb.service.system.SystemService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.DateTransferUtils;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.PropertiesUtil;
import net.sf.ehcache.CacheManager;

/**
 * 商品模块的操作理类
 * 
 * @author jinsey
 * 
 */
@Controller
@RequestMapping("/fxl/event")
public class EventController {

	private static Logger logger = LoggerFactory.getLogger(EventController.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private GoodsService goodsService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private UserService userService;

	@Autowired
	private FxlService fxlService;

	@Autowired
	private SystemService systemService;

	@Autowired
	private ImageDAO imageDAO;

	@RequestMapping(value = "/view", method = RequestMethod.GET)
	public String manage(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<Integer, String> statusTempMap = ObjectUtils
				.clone(DictionaryUtil.getStatueMap(DictionaryUtil.EventStatus, shiroUser.getLanguage()));
		statusTempMap.remove(1);
		statusTempMap.remove(5);
		statusTempMap.remove(6);
		statusTempMap.remove(30);
		statusTempMap.remove(40);
		statusTempMap.remove(50);
		statusTempMap.remove(99);
		statusTempMap.remove(999);
		model.addAttribute("eventStatusMap", statusTempMap);
		model.addAttribute("sponsorMap", customerService.getMerchantMapList());
		model.addAttribute("bdList", userService.getUserListByCategoryId(2));
		model.addAttribute("editorList", userService.getUserListByCategoryId(3));
		model.addAttribute("appChannelList", Constant.appChannelMap);
		model.addAttribute("appChannelList", Constant.appChannelMap);
		Map<Integer, List<Map<String, Object>>> map = goodsService.getTAppChannelSettingList(1);
		model.addAttribute("tappChannelOneList", map.get(1));
		model.addAttribute("tappChannelTwoList", map.get(2));
		model.addAttribute("tappChannelThreeList", map.get(3));

		return "fxl/event/eventManage";
	}

	/**
	 * 获取活动列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getEventList", method = RequestMethod.POST)
	public String getEventList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));
		goodsService.getEventList(map, page);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	/**
	 * 获取活动列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getEventListByRelease", method = RequestMethod.POST)
	public String getEventListByRelease(HttpServletRequest request, HttpServletResponse response) {
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));
		goodsService.getEventListByRelease(map, page);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	@Log(message = "活动小编[{0}]活动ID为[{1}]的活动信息进行删除。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/delEvent/{eventId}", method = RequestMethod.POST)
	public String delEvent(HttpServletRequest request, HttpServletResponse response, @PathVariable String eventId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			goodsService.delEvent(eventId);
			returnMap.put("success", true);
			returnMap.put("msg", "该活动信息删除成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "该活动信息删除失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), eventId }));
		return mapper.toJson(returnMap);
	}

	/*
	 * @RequestMapping(value = "/toSs/{eventId}", method = RequestMethod.GET)
	 * public String toSs(HttpServletRequest request, Model model, @PathVariable
	 * String eventId) { ShiroUser shiroUser = (ShiroUser)
	 * SecurityUtils.getSubject().getPrincipal(); try { // 获取到活动类目缓存对象 Cache
	 * eventCategoryCache = cacheManager.getCache(Constant.EventCategory);
	 * Element ele = null;
	 * 
	 * TEvent tEvent = goodsService.getEvent(eventId);
	 * model.addAttribute("event", tEvent); model.addAttribute("eventId",
	 * eventId); Integer ftypeB = tEvent.getFtypeB();//获得是category的value值
	 * //根据value获得categoryId TEventCategory
	 * category=goodsService.getCategoryByValue(ftypeB); Long
	 * ftypeBId=category.getId(); //通过商品二级类目获得属性 if(null!=ftypeBId){
	 * List<TGoodsTypeClassCategory>typeClassCategoryList=goodsService.
	 * getTypeCategory(ftypeBId); if(null!=typeClassCategoryList &&
	 * typeClassCategoryList.size()>0){//typeClassCategoryList的size大于0,属性一肯定存在
	 * //获得属性一的id Long typeClass1Id=typeClassCategoryList.get(0).getFtypeId();
	 * if(null!=typeClass1Id){ //判断这个属性是否存在 TGoodsTypeClass typeClass1
	 * =goodsService.getTypeClassById(typeClass1Id); if (null!=typeClass1) {
	 * model.addAttribute("typeClas1", typeClass1); //通过属性id得到属性值
	 * List<TGoodsTypeClassValue>
	 * typeValueList1=goodsService.getTypeValueListByTypeId(typeClass1Id);
	 * model.addAttribute("typeValueList1",typeValueList1); }else{
	 * //如果这个属性值不存在,则将与此属性值关联的类别中间表数据删除
	 * goodsService.delTypeVlaueCategoryByTypeClassId(typeClass1Id); }
	 * 
	 * } if(typeClassCategoryList.size()==2){//
	 * 如果typeClassCategoryList说明此分类关联2个属性规格 //属性二的id Long
	 * typeClass2Id=typeClassCategoryList.get(1).getFtypeId();
	 * if(null!=typeClass2Id){
	 * 
	 * TGoodsTypeClass typeClass2 =goodsService.getTypeClassById(typeClass2Id);
	 * if(null!=typeClass2){ model.addAttribute("typeClas2", typeClass2);
	 * //通过id得到属性值 List<TGoodsTypeClassValue>
	 * typeValueList2=goodsService.getTypeValueListByTypeId(typeClass2Id);
	 * model.addAttribute("typeValueList2",typeValueList2); } }
	 * 
	 * } } }
	 * 
	 * // 所属城市 model.addAttribute("fcity",
	 * DictionaryUtil.getString(DictionaryUtil.City, tEvent.getFcity())); ele =
	 * eventCategoryCache.get(tEvent.getFtypeA()); // 活动一级类目
	 * model.addAttribute("ftypeA", ele != null ?
	 * ele.getObjectValue().toString() : StringUtils.EMPTY); ele =
	 * eventCategoryCache.get(tEvent.getFtypeB()); // 活动二级类目
	 * model.addAttribute("ftypeB", ele != null ?
	 * ele.getObjectValue().toString() : StringUtils.EMPTY);
	 * 
	 * // 商家BD model.addAttribute("bd", tEvent.getFbdId() != null ?
	 * userService.get(tEvent.getFbdId()).getRealname() : StringUtils.EMPTY); //
	 * 小编 model.addAttribute("creater", tEvent.getFcreaterId() != null ?
	 * userService.get(tEvent.getFcreaterId()).getRealname() :
	 * StringUtils.EMPTY); // 订单类型 model.addAttribute("forderType",
	 * DictionaryUtil.getString(DictionaryUtil.OrderType,
	 * tEvent.getForderType())); // 记录库存位置 //
	 * model.addAttribute("fcommentRewardType", //
	 * DictionaryUtil.getString(DictionaryUtil.CommentRewardType, //
	 * tEvent.getFcommentRewardType()));
	 * 
	 * if (tEvent.getTSponsor().getId() != null && tEvent.getTSponsor().getId()
	 * !="") { model.addAttribute("faddress",
	 * tEvent.getTSponsor().getFaddress()); }
	 * 
	 * if (tEvent.getTSponsor().getFgps() != null) { model.addAttribute("fgps",
	 * tEvent.getTSponsor().getFgps()); }
	 * 
	 * if (tEvent.getFsettlementType() != null) {
	 * model.addAttribute("fsettlementType",
	 * DictionaryUtil.getString(DictionaryUtil.SettlementType,
	 * tEvent.getFsettlementType())); } if (tEvent.getFverificationType() !=
	 * null) { model.addAttribute("fverificationType",
	 * DictionaryUtil.getString(DictionaryUtil.VerificationType,
	 * tEvent.getFverificationType())); } if (tEvent.getFusePreferential() !=
	 * null) { model.addAttribute("fusePreferential",
	 * DictionaryUtil.getString(DictionaryUtil.YesNo,
	 * tEvent.getFusePreferential())); }
	 * 
	 * model.addAttribute("saleMap",
	 * DictionaryUtil.getStatueMap(DictionaryUtil.EventSalesType,
	 * shiroUser.getLanguage())); model.addAttribute("fsalesFlag", "0"); //
	 * model.addAttribute("frealNameTypeMap", //
	 * DictionaryUtil.getStatueMap(DictionaryUtil.RealnameType)); //
	 * model.addAttribute("frealNameType", "0"); } catch (Exception e) {
	 * logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e))
	 * ); } return "fxl/event/eventSku"; }
	 */

	@ResponseBody
	@RequestMapping(value = "/getEventSpecList", method = RequestMethod.POST)
	public String getEventSpecList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		List<Map<String, Object>> list = null;
		try {
			list = goodsService.getEventSpecList(map);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", list.size());
		returnMap.put("recordsFiltered", list.size());
		returnMap.put("data", list);
		return mapper.toJson(returnMap);
	}

	/**
	 * 根据项目ID返回项目详细信息
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getEventSpec/{specId}", method = RequestMethod.POST)
	public String getEventSpec(HttpServletRequest request, HttpServletResponse response, @PathVariable String specId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TEventSpec tEventSpec = goodsService.getEventSpec(specId);
			returnMap.put("id", tEventSpec.getId());
			returnMap.put("ftitle", tEventSpec.getFtitle());
			returnMap.put("fdescription", tEventSpec.getFdescription());
			returnMap.put("fprice", tEventSpec.getFprice());
			returnMap.put("fdeal", tEventSpec.getFdeal());
			returnMap.put("fadult", tEventSpec.getFadult());
			returnMap.put("fchild", tEventSpec.getFchild());
			returnMap.put("fpostage", tEventSpec.getFpostage());
			returnMap.put("ftotal", tEventSpec.getFtotal());
			returnMap.put("fstock", tEventSpec.getFstock());
			returnMap.put("forder", tEventSpec.getForder());
			returnMap.put("fsettlementPrice", tEventSpec.getFsettlementPrice());
			// returnMap.put("frealNameType", tEventSpec.getFrealNameType());
			if (tEventSpec.getFpointsPrice() != null) {
				returnMap.put("fpointsPrice", tEventSpec.getFpointsPrice()
						.multiply(new BigDecimal(100).setScale(2, BigDecimal.ROUND_HALF_EVEN)));
			}
			// if (tEventSpec.getFexternalGoodsCode() != null) {
			// returnMap.put("fexternalGoodsCode",
			// tEventSpec.getFexternalGoodsCode());
			// }

			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取活动详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "活动小编[{0}]创建活动规格信息。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/addEventSpec", method = RequestMethod.POST)
	public String addEventSpec(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			goodsService.saveEventSpec(map);
			returnMap.put("success", true);
			returnMap.put("msg", "创建活动规格信息保存成功！");
		} catch (ServiceException e) {
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "创建活动规格信息保存失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@Log(message = "活动小编[{0}]将该活动规格信息进行编辑。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/editEventSpec", method = RequestMethod.POST)
	public String editEventSpec(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		try {
			goodsService.eidtEventSpec(map);
			returnMap.put("msg", "编辑活动规格信息保存成功！");
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑活动规格信息保存失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@Log(message = "活动小编[{0}]将ID为[{1}]的活动规格信息进行删除。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/delEventSpec/{specId}", method = RequestMethod.POST)
	public String delEventSpec(HttpServletRequest request, HttpServletResponse response, @PathVariable String specId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			goodsService.delEventSpec(specId);
			returnMap.put("success", true);
			returnMap.put("msg", "该活动规格信息删除成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "该活动规格信息删除失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), specId }));
		return mapper.toJson(returnMap);
	}

	@Log(message = "活动小编[{0}]将ID为[{1}]的活动规格信息进行复制。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/copyEventSpec/{specId}", method = RequestMethod.POST)
	public String copyEventSpec(HttpServletRequest request, HttpServletResponse response, @PathVariable String specId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			goodsService.copyEventSpec(specId);
			returnMap.put("success", true);
			returnMap.put("msg", "该活动规格信息复制成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "该活动规格信息复制失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), specId }));
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/getEventSessionList/{eventId}", method = RequestMethod.POST)
	public String getEventSessionList(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String eventId) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		List<Map<String, Object>> list = null;
		try {
			map.put("eventId", eventId);
			list = goodsService.getEventSessionList(map);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", list.size());
		returnMap.put("recordsFiltered", list.size());
		returnMap.put("data", list);
		return mapper.toJson(returnMap);
	}

	/**
	 * 根据项目ID返回项目详细信息
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getEventSession/{sessionId}", method = RequestMethod.POST)
	public String getEventSession(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String sessionId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TEventSession tEventSession = goodsService.getEventSession(sessionId);
			returnMap.put("id", tEventSession.getId());
			returnMap.put("ftitle", tEventSession.getFtitle());
			returnMap.put("fgps", tEventSession.getFgps());
			returnMap.put("faddress", tEventSession.getFaddress());
			returnMap.put("fstartDate", tEventSession.getFstartDate() != null
					? DateFormatUtils.format(tEventSession.getFstartDate(), "yyyy-MM-dd") : StringUtils.EMPTY);
			returnMap.put("fendDate", tEventSession.getFendDate() != null
					? DateFormatUtils.format(tEventSession.getFendDate(), "yyyy-MM-dd") : StringUtils.EMPTY);
			returnMap.put("frefoundPeriod",
					tEventSession.getFrefoundPeriod() != null
							? DateFormatUtils.format(tEventSession.getFrefoundPeriod(), "yyyy-MM-dd HH:mm")
							: StringUtils.EMPTY);
			// returnMap.put("fautoVerificationTime",
			// tEventSession.getFautoVerificationTime() != null
			// ?
			// DateFormatUtils.format(tEventSession.getFautoVerificationTime(),
			// "yyyy-MM-dd HH:mm")
			// : StringUtils.EMPTY);
			returnMap.put("fdeadline", tEventSession.getFdeadline() != null
					? DateFormatUtils.format(tEventSession.getFdeadline(), "yyyy-MM-dd HH:mm") : StringUtils.EMPTY);
			returnMap.put("forder", tEventSession.getForder());
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取活动详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "活动小编[{0}]创建活动场次信息。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/addEventSession", method = RequestMethod.POST)
	public String addEventSession(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			goodsService.saveEventSession(map);
			returnMap.put("success", true);
			returnMap.put("msg", "创建活动场次信息保存成功！");
		} catch (ServiceException e) {
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "创建活动场次信息保存失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@Log(message = "活动小编[{0}]将该活动场次信息进行编辑。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/editEventSession", method = RequestMethod.POST)
	public String editEventSession(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		try {
			goodsService.eidtEventSession(map);
			returnMap.put("msg", "编辑活动场次信息保存成功！");
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑活动场次信息保存失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@Log(message = "活动小编[{0}]将ID为[{1}]的活动场次信息进行删除。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/delEventSession/{sessionId}", method = RequestMethod.POST)
	public String delEventSession(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String sessionId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			goodsService.delEventSession(sessionId);
			returnMap.put("success", true);
			returnMap.put("msg", "该活动场次信息删除成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "该活动场次信息删除失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), sessionId }));
		return mapper.toJson(returnMap);
	}

	@Log(message = "活动小编[{0}]将ID为[{1}]的活动场次信息进行复制。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/copyEventSession/{sessionId}", method = RequestMethod.POST)
	public String copyEventSession(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String sessionId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			goodsService.copyEventSession(sessionId);
			returnMap.put("success", true);
			returnMap.put("msg", "该活动场次信息复制成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "该活动场次信息复制失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), sessionId }));
		return mapper.toJson(returnMap);
	}

	@Log(message = "活动小编[{0}]将ID为[{1}]的活动场次信息和规格信息进行复制。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/copyEventSpecSessionId/{sessionId}", method = RequestMethod.POST)
	public String copyEventSpecSessionId(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String sessionId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			goodsService.copyEventSpecSessionId(sessionId);
			returnMap.put("success", true);
			returnMap.put("msg", "该活动场次和规格信息复制成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "该活动场次和规格信息复制失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), sessionId }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/release", method = RequestMethod.GET)
	public String release(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<Integer, String> statusTempMap = ObjectUtils
				.clone(DictionaryUtil.getStatueMap(DictionaryUtil.EventStatus, shiroUser.getLanguage()));
		statusTempMap.remove(1);
		statusTempMap.remove(4);
		statusTempMap.remove(5);
		statusTempMap.remove(6);
		statusTempMap.remove(30);
		statusTempMap.remove(40);
		statusTempMap.remove(50);
		statusTempMap.remove(99);
		statusTempMap.remove(999);
		model.addAttribute("eventStatusMap", statusTempMap);
		model.addAttribute("sponsorMap", customerService.getMerchantMapList());
		model.addAttribute("bdList", userService.getUserListByCategoryId(2));
		model.addAttribute("editorList", userService.getUserListByCategoryId(3));
		model.addAttribute("appChannelList", Constant.appChannelMap);

		Map<Integer, List<Map<String, Object>>> map = goodsService.getTAppChannelSettingList(1);
		model.addAttribute("tappChannelOneList", map.get(1));
		model.addAttribute("tappChannelTwoList", map.get(2));
		model.addAttribute("tappChannelThreeList", map.get(3));

		return "fxl/event/eventRelease";
	}

	/**
	 * 获取客户列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getAppChannelList", method = RequestMethod.POST)
	public String getAppChannelList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		List<Map<String, Object>> list = goodsService.getAppChannelList(map);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", list.size());
		returnMap.put("recordsFiltered", list.size());
		returnMap.put("data", list);
		return mapper.toJson(returnMap);
	}

	/**
	 * 获取客户列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getAppChannelListByEventId/{eventId}", method = RequestMethod.POST)
	public String getAppChannelListByEventId(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String eventId) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		map.put("eventId", eventId);
		List<Map<String, Object>> list = goodsService.getAppChannelListByEventId(map);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", list.size());
		returnMap.put("recordsFiltered", list.size());
		returnMap.put("data", list);
		return mapper.toJson(returnMap);
	}

	/**
	 * 活动发布
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "活动小编[{0}]进行活动关联栏目信息保存。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/eventAssociate", method = RequestMethod.POST)
	public String eventAssociate(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			goodsService.eventAssociate(map);
			returnMap.put("success", true);
			returnMap.put("msg", "活动关联栏目信息保存成功！");
		} catch (ServiceException e) {
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "活动关联栏目信息保存失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 删除活动和APP栏目的关联
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "活动小编[{0}]将ID为[{1}]的活动关联栏目信息删除。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/delEventAssociate/{channelEventId}", method = RequestMethod.POST)
	public String delEventAssociate(HttpServletRequest request, Model model, @PathVariable String channelEventId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			goodsService.delEventAssociate(channelEventId);
			returnMap.put("success", true);
			returnMap.put("msg", "删除活动关联栏目信息成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "删除活动关联栏目信息失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), channelEventId }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 编辑活动和APP栏目的关联的排序号
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "活动小编[{0}]进行活动所在栏目排序号保存。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/saveEventOrder", method = RequestMethod.POST)
	public String saveEventOrder(HttpServletRequest request, Model model, @RequestParam() String id,
			@RequestParam() Integer forder) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			goodsService.saveEventOrder(id, forder);
			returnMap.put("success", true);
			returnMap.put("msg", "活动所在栏目排序号保存成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "活动所在栏目排序号保存失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/onOffSale", method = RequestMethod.GET)
	public String onOffSale(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();

		Map<Integer, String> statusTempMap = ObjectUtils
				.clone(DictionaryUtil.getStatueMap(DictionaryUtil.EventStatus, shiroUser.getLanguage()));
		statusTempMap.remove(1);
		statusTempMap.remove(4);
		statusTempMap.remove(5);
		statusTempMap.remove(6);
		statusTempMap.remove(30);
		statusTempMap.remove(40);
		statusTempMap.remove(50);
		statusTempMap.remove(99);
		statusTempMap.remove(999);
		model.addAttribute("eventStatusMap", statusTempMap);
		model.addAttribute("sponsorMap", customerService.getMerchantMapList());
		model.addAttribute("bdList", userService.getUserListByCategoryId(2));
		model.addAttribute("editorList", userService.getUserListByCategoryId(3));
		model.addAttribute("appChannelList", Constant.appChannelMap);
		return "fxl/event/eventOnOffSale";
	}

	@ResponseBody
	@RequestMapping(value = "/getOnOffSale/{eventId}", method = RequestMethod.POST)
	public String getOnOffSale(HttpServletRequest request, HttpServletResponse response, @PathVariable String eventId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TEvent tEvent = goodsService.getEvent(eventId);

			returnMap.put("fonSaleTime", tEvent.getFonSaleTime() != null
					? DateFormatUtils.format(tEvent.getFonSaleTime(), "yyyy-MM-dd HH:mm") : StringUtils.EMPTY);
			returnMap.put("foffSaleTime", tEvent.getFoffSaleTime() != null
					? DateFormatUtils.format(tEvent.getFoffSaleTime(), "yyyy-MM-dd HH:mm") : StringUtils.EMPTY);
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取活动上下架时间信息时出错！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 商品设置自动上下架时间
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "活动小编[{0}]设置活动上下架时间。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/eventOnOffSale", method = RequestMethod.POST)
	public String eventOnOffSale(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			goodsService.eventOnOffSale(map);
			returnMap.put("success", true);
			returnMap.put("msg", "活动上下架时间保存成功！设定的时间达到后会自动上下架活动。");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "活动上下架时间保存失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 商品即刻上架
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "活动小编[{0}]将活动ID为[{1}]的活动即刻上架 。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/onSale/{eventId}", method = RequestMethod.POST)
	public String onSale(HttpServletRequest request, Model model, @PathVariable String eventId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			goodsService.onSale(eventId);
			returnMap.put("success", true);
			returnMap.put("msg", "活动即刻上架操作成功！");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());

		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "活动即刻上架操作失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), eventId }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 商品即刻下架
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "活动小编[{0}]将活动ID为[{1}]的活动即刻下架 。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/offSale/{eventId}", method = RequestMethod.POST)
	public String offSale(HttpServletRequest request, Model model, @PathVariable String eventId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			goodsService.offSale(eventId);
			returnMap.put("success", true);
			returnMap.put("msg", "商品即刻下架操作成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "商品即刻下架操作失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), eventId }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/toEventCreateMain", method = RequestMethod.GET)
	public String toEventCreateMain(HttpServletRequest request, Model model) {
		return "fxl/event/eventCreateMain";
	}

	@RequestMapping(value = "/toEventCreateMain/{eventId}", method = RequestMethod.GET)
	public String toEventCreateMainById(HttpServletRequest request, Model model, @PathVariable String eventId) {
		model.addAttribute("eventId", eventId);
		return "fxl/event/eventCreateMain";
	}

	@RequestMapping(value = "/toEventCreateA", method = RequestMethod.GET)
	public String toEventCreateA(HttpServletRequest request, Model model) {
		model.addAttribute("categoryMapA", goodsService.getCategoryMapA());
		return "fxl/event/eventCreateA";
	}

	@RequestMapping(value = "/toEventCreateA/{eventId}", method = RequestMethod.GET)
	public String toEventCreateAById(HttpServletRequest request, Model model, @PathVariable String eventId) {
		TEvent tEvent = goodsService.getEvent(eventId);
		model.addAttribute("ftypeA", tEvent.getFtypeA());
		model.addAttribute("categoryMapA", goodsService.getCategoryMapA());
		if (tEvent.getFtypeB() != null) {
			model.addAttribute("ftypeB", tEvent.getFtypeB());
			model.addAttribute("categoryMapB", goodsService.getCategoryMapB(tEvent.getFtypeA()));
		}
		return "fxl/event/eventCreateA";
	}

	@ResponseBody
	@RequestMapping(value = "/getCategoryMapB", method = RequestMethod.POST)
	public String getCategoryMapB(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("ftypeA") Long ftypeA) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			List<Map<String, Object>> ftypeBList = goodsService.getCategoryMapB(ftypeA);

			returnMap.put("ftypeBList", ftypeBList);
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取活动二级类目时出错！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "活动小编[{0}]进行活动类目信息保存。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/addEventA", method = RequestMethod.POST)
	public String addEventA(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			String eventId = goodsService.saveEventA(map);
			returnMap.put("eventId", eventId);
			returnMap.put("success", true);
			returnMap.put("msg", "活动类目信息保存成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "创建活动信息保存失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/toEventCreateB/{eventId}", method = RequestMethod.GET)
	public String toEventCreateB(HttpServletRequest request, Model model, @PathVariable String eventId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();

		// model.addAttribute("orderTypeMap",
		// DictionaryUtil.getStatueMap(DictionaryUtil.OrderType,
		// shiroUser.getLanguage()));
		// model.addAttribute("tagMap",
		// DictionaryUtil.getStatueMap(DictionaryUtil.EventTag,
		// shiroUser.getLanguage()));
		// model.addAttribute("subtitleImgMap",
		// DictionaryUtil.getStatueMap(DictionaryUtil.EventSubtitleImg,
		// shiroUser.getLanguage()));
		// model.addAttribute("siteTypeMap",
		// DictionaryUtil.getStatueMap(DictionaryUtil.SiteType,
		// shiroUser.getLanguage()));
		// model.addAttribute("durationMap",
		// DictionaryUtil.getStatueMap(DictionaryUtil.EventDuration,
		// shiroUser.getLanguage()));
		// model.addAttribute("settlementTypeMap",
		// DictionaryUtil.getStatueMap(DictionaryUtil.SettlementType,
		// shiroUser.getLanguage()));

		// model.addAttribute("usePreferentialMap",
		// DictionaryUtil.getStatueMap(DictionaryUtil.YesNo,
		// shiroUser.getLanguage()));
		// model.addAttribute("verificationTypeMap",
		// DictionaryUtil.getStatueMap(DictionaryUtil.VerificationType,
		// shiroUser.getLanguage()));
		// model.addAttribute("commentRewardTypeMap",
		// DictionaryUtil.getStatueMap(DictionaryUtil.CommentRewardType,
		// shiroUser.getLanguage()));
		// model.addAttribute("externalSystemMap",
		// DictionaryUtil.getStatueMap(DictionaryUtil.ExternalSystem,
		// shiroUser.getLanguage()));

		model.addAttribute("cityMap", DictionaryUtil.getStatueMap(DictionaryUtil.City, shiroUser.getLanguage()));
		model.addAttribute("sponsorMap", customerService.getMerchantMapList());
		model.addAttribute("yesNoMap", DictionaryUtil.getStatueMap(DictionaryUtil.YesNo, shiroUser.getLanguage()));
		model.addAttribute("bdList", userService.getUserListByCategoryId(2));
		model.addAttribute("editorList", userService.getUserListByCategoryId(3));
		model.addAttribute("fsellModelMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.SellModel, shiroUser.getLanguage()));
		model.addAttribute("promotionModelMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.PromotionModel, shiroUser.getLanguage()));
		model.addAttribute("goodsModelMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.GoodsTag, shiroUser.getLanguage()));
		model.addAttribute("fsadelModelMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.SadelsModel, shiroUser.getLanguage()));

		// model.addAttribute("specModelMap",
		// DictionaryUtil.getStatueMap(DictionaryUtil.SpecModel,
		// shiroUser.getLanguage()));

		TEvent tEvent = goodsService.getEvent(eventId);
		model.addAttribute("fUsePreferential", tEvent.getFusePreferential());
		model.addAttribute("ftitle", tEvent.getFtitle());
		model.addAttribute("fsubTitle", tEvent.getFsubTitle());
		// model.addAttribute("fsubTitleImg", tEvent.getFsubTitleImg());
		model.addAttribute("fcity", tEvent.getFcity() != null ? tEvent.getFcity() : "1");
		model.addAttribute("fsponsor", tEvent.getTSponsor() != null ? tEvent.getTSponsor().getId() : StringUtils.EMPTY);
		model.addAttribute("fprice", tEvent.getFprice());
		model.addAttribute("forderPrice", tEvent.getForderPrice());
		model.addAttribute("forderType", tEvent.getForderType() != null ? tEvent.getForderType() : "1");
		model.addAttribute("fbrief", tEvent.getFbrief());
		model.addAttribute("fbdId", tEvent.getFbdId());
		model.addAttribute("fcreaterId", tEvent.getFcreaterId() != null ? tEvent.getFcreaterId() : shiroUser.getId());
		model.addAttribute("fpriceMoney", tEvent.getFpriceMoney());
		model.addAttribute("fsellModel", tEvent.getFsellModel() != null ? tEvent.getFsellModel() : "0");
		model.addAttribute("fsadelModel", tEvent.getFsdealsModel() != null ? tEvent.getFsdealsModel() : "0");
		model.addAttribute("fspec", tEvent.getFspec());
		model.addAttribute("fpromotionModel", tEvent.getFpromotionModel() != null ? tEvent.getFpromotionModel() : "0");
		model.addAttribute("flimitation", tEvent.getFlimitation() != null ? tEvent.getFlimitation() : "-1");
		model.addAttribute("ftotal", tEvent.getFtotal());
		model.addAttribute("fstock", tEvent.getFstock());
		model.addAttribute("fgoodsTag", tEvent.getFgoodsTag() != null ? tEvent.getFgoodsTag() : "1");
		model.addAttribute("fonSaleTime", tEvent.getFonSaleTime() != null ? tEvent.getFonSaleTime() : "0");
		model.addAttribute("fonSaleTime", tEvent.getFonSaleTime() != null
				? DateFormatUtils.format(tEvent.getFonSaleTime(), "yyyy-MM-dd hh:mm:ss") : StringUtils.EMPTY);

		return "fxl/event/eventCreateB";
	}

	@Log(message = "活动小编[{0}]进行活动基本信息保存。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/addEventB", method = RequestMethod.POST)
	public String addEventB(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			goodsService.saveEventB(map);
			returnMap.put("success", true);
			returnMap.put("msg", "商品基本信息保存成功！");
		} catch (ServiceException se) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(se)));
			returnMap.put("success", false);
			returnMap.put("msg", se.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "商品基本信息保存失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/toEventCreateC/{eventId}", method = RequestMethod.GET)
	public String toEventCreateC(HttpServletRequest request, Model model, @PathVariable String eventId) {
		TEvent tEvent = goodsService.getEvent(eventId);
		// model.addAttribute("fimage2", tEvent.getFimage2());

		if (StringUtils.isNotBlank(tEvent.getFimage1())) {
			String id = tEvent.getFimage1();
			TImage tImage = goodsService.getImage(Long.valueOf(id));
			model.addAttribute("imageWidth", tImage.getImageWidth());
			model.addAttribute("imageHeight", tImage.getImageHeight());

			StringBuilder imageUrl = new StringBuilder(PropertiesUtil.getProperty("fileServerUrl"))
					.append(PropertiesUtil.getProperty("imageRootPath")).append(tImage.getRelativePath());
			model.addAttribute("fimage1Url",
					imageUrl.append(tImage.getStoreFileName()).append(".").append(tImage.getStorefileExt()).toString());
		} else {
			model.addAttribute("fimage1Url", StringUtils.EMPTY);
		}
		int s = tEvent.getFimage2() != null ? tEvent.getFimage2().split(";").length : 0;
		model.addAttribute("eventId", eventId);
		model.addAttribute("imageCount", s);
		return "fxl/event/eventCreateC";
	}

	@Log(message = "活动小编[{0}]进行活动图片信息保存。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/addEventC", method = RequestMethod.POST)
	public String addEventC(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			goodsService.saveEventC(map);
			returnMap.put("success", true);
			returnMap.put("msg", "活动图片信息保存成功！");
		} catch (ServiceException se) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(se)));
			returnMap.put("success", false);
			returnMap.put("msg", se.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "活动图片信息保存失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/toEventCreateD/{eventId}", method = RequestMethod.GET)
	public String toEventCreateD(HttpServletRequest request, Model model, @PathVariable String eventId) {
		TEvent tEvent = goodsService.getEvent(eventId);
		model.addAttribute("fdetail", tEvent.getFdetail());
		return "fxl/event/eventCreateD";
	}

	@Log(message = "活动小编[{0}]进行活动图文介绍信息保存。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/addEventD", method = RequestMethod.POST)
	public String addEventD(HttpServletRequest request, HttpServletResponse response, @RequestParam String eventId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			goodsService.saveEventD(map);
			returnMap.put("success", true);
			returnMap.put("msg", "活动图文介绍信息保存成功！");
		} catch (ServiceException se) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(se)));
			returnMap.put("success", false);
			returnMap.put("msg", se.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "活动图文介绍信息保存失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/toEventCreateE/{eventId}", method = RequestMethod.GET)
	public String toEventCreateE(HttpServletRequest request, Model model, @PathVariable String eventId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("eventId", eventId);
		/*
		 * model.addAttribute("evenTypeDetailtMap",
		 * DictionaryUtil.getStatueMap(DictionaryUtil.EventTypeDetail,
		 * shiroUser.getLanguage())); model.addAttribute("evenTypeImageMap",
		 * DictionaryUtil.getStatueMap(DictionaryUtil.EventIcon,
		 * shiroUser.getLanguage()));
		 */
		List<TGoodsSpaceValue> tSpecList = goodsService.getSpecList(eventId);
		model.addAttribute("tSpecList", tSpecList);

		return "fxl/event/eventCreateE";
	}

	@Log(message = "活动小编[{0}]进行活动规则信息保存。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/addEventE", method = RequestMethod.POST)
	public String addEventE(HttpServletRequest request, HttpServletResponse response, @RequestParam String eventId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			goodsService.saveEventE(map);
			returnMap.put("success", true);
			returnMap.put("msg", "新增参数保存成功,请继续编辑该商品的库存规格！");
		} catch (ServiceException se) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(se)));
			returnMap.put("success", false);
			returnMap.put("msg", se.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "新增参数保存失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/eventView/{eventId}", method = RequestMethod.GET)
	public String eventView(HttpServletRequest request, Model model, @PathVariable String eventId) {
		model.addAttribute("viewUrl", new StringBuilder().append(Constant.getH5EventUrl()).append(eventId).toString());
		return "fxl/event/eventView";
	}

	/**
	 * 根据项目ID返回活动发布 栏目 类别 标签 信息
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getChannelEvent/{eventId}", method = RequestMethod.POST)
	public String getMerchant(HttpServletRequest request, HttpServletResponse response, @PathVariable String eventId) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			List<Map<String, Object>> channelEventList = goodsService.getChannelEventList(eventId);
			returnMap.put("channelEventList", channelEventList);
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取活动发布详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 添加活动字段
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@Log(message = "活动小编[{0}]添加活动扩展字段。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/addEventTexInfo", method = RequestMethod.POST)
	public String addEventTexInfo(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			goodsService.addEventTexInfo(map);
			returnMap.put("success", true);
			returnMap.put("msg", "保存活动扩展字段成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "保存活动扩展字段失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 编辑扩展字段
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "活动小编[{0}]编辑活动扩展属性。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/editEventTexInfo", method = RequestMethod.POST)
	public String editEventTexInfo(HttpServletRequest request, Model model, @RequestParam() String id,
			@RequestParam() Integer forder, @RequestParam() String fprompt, @RequestParam() String fname,
			@RequestParam() String fisRequired, @RequestParam() String fisEveryone) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			goodsService.editEventTexInfo(id, forder, fprompt, fname, fisRequired, fisEveryone);
			returnMap.put("success", true);
			returnMap.put("msg", "编辑活动扩展字段成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑活动扩展字段失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 获取扩展熟悉列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getTEventExtInfo/{eventId}", method = RequestMethod.POST)
	public String getTEventExtInfo(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String eventId) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));
		goodsService.getTEventExtInfo(map, page, eventId);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/recommend", method = RequestMethod.GET)
	public String recommend(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<Integer, String> statusTempMap = ObjectUtils
				.clone(DictionaryUtil.getStatueMap(DictionaryUtil.EventStatus, shiroUser.getLanguage()));
		statusTempMap.remove(1);
		// statusTempMap.remove(4);
		statusTempMap.remove(5);
		statusTempMap.remove(6);
		statusTempMap.remove(30);
		statusTempMap.remove(40);
		statusTempMap.remove(50);
		statusTempMap.remove(99);
		statusTempMap.remove(999);
		model.addAttribute("eventStatusMap", statusTempMap);
		model.addAttribute("tagMap", DictionaryUtil.getStatueMap(DictionaryUtil.EventTag, shiroUser.getLanguage()));
		model.addAttribute("sponsorMap", customerService.getMerchantMapList());
		model.addAttribute("bdList", userService.getUserListByCategoryId(2));
		model.addAttribute("editorList", userService.getUserListByCategoryId(3));
		model.addAttribute("appChannelList", Constant.appChannelMap);

		return "fxl/event/eventRecommend";
	}

	/**
	 * 获取个性化设置活动列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getEventPersonalizedList", method = RequestMethod.POST)
	public String getEventPersonalizedList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));
		goodsService.getEventPersonalizedList(map, page);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	@Log(message = "活动小编[{0}]设置个性化推荐标签。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/addPersonalizedTag", method = RequestMethod.POST)
	public String addPersonalizedTag(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			goodsService.addPersonalizedTag(map);
			returnMap.put("success", true);
			returnMap.put("msg", "活动基本信息保存成功！");
		} catch (ServiceException se) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(se)));
			returnMap.put("success", false);
			returnMap.put("msg", se.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "活动基本信息保存失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	// @Log(message = "运营小编[{0}]删除了ID为[{1}]的文章。", module = 8)
	@ResponseBody
	@RequestMapping(value = "/deleteEventExtInfo/{extInfoId}", method = RequestMethod.POST)
	public String deleteEventExtInfo(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String extInfoId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			goodsService.deleteEventExtInfo(extInfoId);
			returnMap.put("success", true);
			returnMap.put("msg", "扩展属性删除成功！");
			// LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new
			// Object[] { shiroUser.getName(), extInfoId }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "扩展属性删除失败！");
		}
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/relationRecommend", method = RequestMethod.GET)
	public String relationRecommend(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<Integer, String> statusTempMap = ObjectUtils
				.clone(DictionaryUtil.getStatueMap(DictionaryUtil.EventStatus, shiroUser.getLanguage()));
		statusTempMap.remove(1);
		// statusTempMap.remove(4);
		statusTempMap.remove(5);
		statusTempMap.remove(6);
		statusTempMap.remove(30);
		statusTempMap.remove(40);
		statusTempMap.remove(50);
		statusTempMap.remove(99);
		statusTempMap.remove(999);
		model.addAttribute("eventStatusMap", statusTempMap);
		model.addAttribute("tagMap", DictionaryUtil.getStatueMap(DictionaryUtil.EventTag, shiroUser.getLanguage()));
		model.addAttribute("eventDetailListMap", goodsService.getEventDetailList());
		model.addAttribute("sponsorMap", customerService.getMerchantMapList());
		model.addAttribute("bdList", userService.getUserListByCategoryId(2));
		model.addAttribute("editorList", userService.getUserListByCategoryId(3));
		model.addAttribute("appChannelList", Constant.appChannelMap);

		return "fxl/event/relationRecommend";
	}

	@Log(message = "活动小编[{0}]设置关联推荐活动。", module = 5)
	@ResponseBody
	@RequestMapping(value = "/addRelationRecommend", method = RequestMethod.POST)
	public String addRelationRecommend(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			goodsService.addRelationRecommend(map);
			returnMap.put("success", true);
			returnMap.put("msg", "关联推荐设置成功！");
		} catch (ServiceException se) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(se)));
			returnMap.put("success", false);
			returnMap.put("msg", se.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "关联推荐设置失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/categoryView", method = RequestMethod.GET)
	public String categoryView(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("cityMap", DictionaryUtil.getStatueMap(DictionaryUtil.City, shiroUser.getLanguage()));
		model.addAttribute("categoryList", goodsService.getCategoryList(1));
		model.addAttribute("typeClassList", goodsService.getAllTypeClassList());
		model.addAttribute("artTypeMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.ArticleType, shiroUser.getLanguage()));
		return "fxl/category/categoryView";
	}

	/**
	 * 获取商品类目的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getCategoryList", method = RequestMethod.POST)
	public String getCategoryList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		try {
			goodsService.getCategoryList(map, page);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	/**
	 * 添加商品分类 eventBargaining
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/addCategory", method = RequestMethod.POST)
	public String addCategory(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			String articleId = goodsService.saveEventCategory(map);
			returnMap.put("success", true);
			returnMap.put("msg", "创建分类信息成功!");
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), articleId }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "创建分类信息失败!");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 根据商品分类ID获得分类的详细信息
	 * 
	 * @param request
	 * @param response
	 * @param categoryId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getCategory/{categoryId}", method = RequestMethod.POST)
	public String getArticle(HttpServletRequest request, HttpServletResponse response, @PathVariable Long categoryId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TEventCategory eventCategory = goodsService.geteventCategory(categoryId);
			// 判断下此分类是否为二级分类,如是二级分类,则查找对应的属性值
			if (eventCategory.getLevel() == 2) {
				List<TGoodsTypeClassCategory> list = goodsService.getTypeCategory(categoryId);
				if (null != list && list.size() > 0) {
					if (list.size() == 1) {
						returnMap.put("typeClass1", list.get(0).getFtypeId());
						returnMap.put("typeClass2", "");
					}
					if (list.size() == 2) {
						returnMap.put("typeClass1", list.get(0).getFtypeId());
						returnMap.put("typeClass2", list.get(1).getFtypeId());
					}
				}
			} else {
				returnMap.put("typeClass1", "");
				returnMap.put("typeClass1", "");
			}

			returnMap.put("id", eventCategory.getId());
			returnMap.put("name", eventCategory.getName());
			returnMap.put("level", eventCategory.getLevel());
			returnMap.put("parentId", eventCategory.getParentId());
			returnMap.put("value", eventCategory.getValue());
			String imageA = eventCategory.getImageA();

			returnMap.put("imageA", imageA);
			returnMap.put("imageB", eventCategory.getImageA());

			if (imageA != null) {
				TImage tImage = imageDAO.findOne(Long.valueOf(imageA));
				returnMap.put("imageWidth", tImage.getImageWidth());
				returnMap.put("imageHeight", tImage.getImageHeight());
				returnMap.put("imageAPath", fxlService.getImageUrl(imageA, true));
			}

			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取分类详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 修改分类
	 * 
	 * @param request
	 * @param response
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/editCategory", method = RequestMethod.POST)
	public String editArticle(HttpServletRequest request, HttpServletResponse response, @RequestParam String id) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			goodsService.editCategory(map);
			returnMap.put("msg", "编辑分类信息成功!");
			returnMap.put("success", true);
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑分类信息失败!");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 删除分类
	 * 
	 * @param request
	 * @param response
	 * @param categoryId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/delCategory/{categoryId}", method = RequestMethod.POST)
	public String delArticle(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String categoryId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			goodsService.delCategory(categoryId);
			returnMap.put("success", true);
			returnMap.put("msg", "该分类信息删除成功！");
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), categoryId }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "该分类信息删除失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 去商品类别属性页面
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @author maxiao
	 */
	@RequestMapping(value = "/typeClassView", method = RequestMethod.GET)
	public String typeClassView(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 查询商品类别目录
		model.addAttribute("categoryMapA", goodsService.getCategoryMapA());
		// 查询所有的属性
		List<TGoodsTypeClass> typeClassList = goodsService.getAllTypeClassList();
		model.addAttribute("typeClassList", typeClassList);
		return "fxl/typeClass/typeClassView";
	}

	/**
	 * 查找所有商品属性
	 * 
	 * @author maxiao
	 */

	@ResponseBody
	@RequestMapping(value = "/getTypeClassList", method = RequestMethod.POST)
	public String getTypeClassList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		try {
			goodsService.getTypeClassList(map, page);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	/**
	 * 添加商品属性
	 * 
	 * @author maxiao
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/addTypeClass", method = RequestMethod.POST)
	public String addTypeClass(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			String articleId = goodsService.saveTypeClass(map);
			returnMap.put("success", true);
			returnMap.put("msg", "创建商品属性成功!");
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), articleId }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "创建商品属性失败!");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 修改商品属性：修改后保存
	 * 
	 * @param request
	 * @param response
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/editTypeClass", method = RequestMethod.POST)
	public String editTypeClass(HttpServletRequest request, HttpServletResponse response, @RequestParam String id) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			goodsService.editTypeClass(map);
			returnMap.put("msg", "编辑商品属性信息成功!");
			returnMap.put("success", true);
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑商品属性信息失败!");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 根据商品属性ID获得商品属性信息：页面回显
	 * 
	 * @author maxiao
	 * @param request
	 * @param response
	 * @param categoryId
	 * @return
	 * 
	 */
	@ResponseBody
	@RequestMapping(value = "/getTypeClass/{typeClassId}", method = RequestMethod.POST)
	public String getTypeClassInfo(HttpServletRequest request, HttpServletResponse response,
			@PathVariable Long typeClassId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			TGoodsTypeClass typeClass = goodsService.getTypeClassInfo(typeClassId);
			returnMap.put("id", typeClass.getId());
			returnMap.put("fclassName", typeClass.getFclassName());
			returnMap.put("fsort", typeClass.getFsort());
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取商品属性信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 根据id删除商品属性
	 * 
	 * @author maxiao
	 * @param request
	 * @param response
	 * @param typeClassId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/delTypeClass/{typeClassId}", method = RequestMethod.POST)
	public String delTypeClassArticle(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String typeClassId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			goodsService.delTypeClass(Long.valueOf(typeClassId));
			returnMap.put("success", true);
			returnMap.put("msg", "该商品属性删除成功！");
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), typeClassId }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "该商品属性删除失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 查找某一属性对应的所有属性值
	 * 
	 * @author maxiao
	 * @param request
	 * @param response
	 * @return
	 */

	@ResponseBody
	@RequestMapping(value = "/getTypeValueList", method = RequestMethod.POST)
	public String getTypeValueList(HttpServletRequest request, HttpServletResponse response) {
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		if (StringUtils.isNotBlank(map.get("fextendClassId").toString())) {
			List<Map<String, Object>> list = null;
			try {

				goodsService.getTypeValueList(map, page);

			} catch (Exception e) {
				logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			}

		}
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);

	}

	/**
	 * 编辑某一项属性值
	 * 
	 * @author maxiao
	 * @param request
	 * @param response
	 * @param itemId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/editTypeValue/{itemId}", method = RequestMethod.POST)
	public String getConfigItem(HttpServletRequest request, HttpServletResponse response, @PathVariable Long itemId) {
		RequestContext requestContext = new RequestContext(request);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			TGoodsTypeClassValue typeClassValue = goodsService.getTypeValueById(itemId);
			System.out.println(typeClassValue.getId());
			returnMap.put("id", typeClassValue.getId());
			returnMap.put("fvalue", typeClassValue.getFvalue());
			returnMap.put("fsort", typeClassValue.getFsort());
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", requestContext.getMessage("获取商品属性值信息失败"));
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 新增/编辑属性值
	 * 
	 * @author maxiao
	 * @param request
	 * @param response
	 * @param goodsTypeValue
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/addTypeValue", method = RequestMethod.POST)
	public String addConfigItem(HttpServletRequest request, HttpServletResponse response,
			TGoodsTypeClassValue goodsTypeValue) {
		RequestContext requestContext = new RequestContext(request);
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		boolean isAdd = false;
		if (goodsTypeValue != null && goodsTypeValue.getId() == null) {
			isAdd = true;
		}

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			if (isAdd) {
				Long fextendClassId = null;
				if (!map.containsKey("fextendClassId") && StringUtils.isBlank(map.get("fextendClassId").toString())) {
					throw new ServiceException("缺少配置商品属性信息，无法保存属性值！");
				}
				fextendClassId = Long.valueOf(map.get("fextendClassId").toString());
				TGoodsTypeClass typeClass = goodsService.getTypeClassInfo(fextendClassId);
				goodsTypeValue.setFextendClassName(typeClass.getFclassName());
				goodsTypeValue.setFextendClassId(fextendClassId);
				goodsService.addTypeValue(goodsTypeValue);
			} else {
				goodsService.updateTypeValue(goodsTypeValue);
			}
			returnMap.put("success", true);
			if (isAdd) {
				returnMap.put("msg", requestContext.getMessage("新增商品属性值成功"));
			} else {
				returnMap.put("msg", requestContext.getMessage("编辑商品属性值成功"));
			}
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			if (isAdd) {
				returnMap.put("msg", requestContext.getMessage("新增商品属性值失败"));
			} else {
				returnMap.put("msg", requestContext.getMessage("编辑商品属性值失败"));
			}
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 删除某一属性的属性值
	 * 
	 * @author maxiao
	 * @param request
	 * @param response
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/delTypeValue/{id}", method = RequestMethod.POST)
	public String delConfigItem(HttpServletRequest request, HttpServletResponse response, @PathVariable Long id) {
		RequestContext requestContext = new RequestContext(request);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			goodsService.deleteTypeValue(id);
			returnMap.put("success", true);
			returnMap.put("msg", requestContext.getMessage("删除属性值成功"));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", requestContext.getMessage("删除属性值失败"));
		}
		return mapper.toJson(returnMap);
	}
	
	/**
	 * 验证:参数名称是否已经存在
	 * @param request
	 * @param fieldId
	 * @param fieldValue
	 * @param id
	 * @return
	 */
	
	@ResponseBody
	@RequestMapping(value = "/checkValueName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkValueName(ServletRequest request, 
			@RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue, 
			@RequestParam(required = false, name = "fgoodsId") String fgoodsId,
			@RequestParam(required = false, name = "fid") Long id) {
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (goodsService.countByGoodsIdAndValueName(fieldValue.trim(),fgoodsId, id).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}


	/**
	 * 验证:商品分类名称是否已存在
	 * 
	 * @param request
	 * @param fieldId
	 * @param fieldValue
	 * @param fextendClassId
	 * @param id
	 * @return
	 */

	@ResponseBody
	@RequestMapping(value = "/checkCategoryName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkCategoryName(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue, @RequestParam(required = false, name = "fid") Long id) {
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (goodsService.countByCategoryName(fieldValue.trim(), id).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 验证属性名是否已存在
	 * 
	 * @author maxiao
	 * @param request
	 * @param fieldId
	 * @param fieldValue
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/checkTypeClassName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkTypeClassName(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue, @RequestParam(required = false, name = "fid") Long id) {
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (goodsService.countByTypeClassName(fieldValue.trim(), id).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 验证属性名的排序
	 * 
	 * @param request
	 * @param fieldId
	 * @param fieldValue
	 * @param id
	 * @return
	 */

	@ResponseBody
	@RequestMapping(value = "/checkTypeClassSort", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkTypeClassSort(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue, @RequestParam(required = false, name = "fid") Long id) {
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (goodsService.countByTypeClassSort(Integer.valueOf(fieldValue.trim()), id).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 验证属性值是否已存在
	 * 
	 * @author maxiao
	 * @param request
	 * @param fieldId
	 * @param fieldValue
	 * @param fextendClassId
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/checkTypeValueName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkTypeValueName(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue,
			@RequestParam(required = false, name = "fextendClassId") Long fextendClassId,
			@RequestParam(required = false, name = "fid") Long id) {
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (goodsService.countTypeValueByNameAndClassId(fieldValue.trim(), fextendClassId, id).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 验证sku商品编号是否已存在
	 * 
	 * @author maxiao
	 * @param request
	 * @param fieldId
	 * @param fieldValue
	 * @param fextendClassId
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/checkGoodsNO", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkGoodsNO(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue,
			@RequestParam(required = false, name = "fgoodsId") String fgoodsId,
			@RequestParam(required = false, name = "id") String id) {
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		TEvent event = goodsService.getEvent(fgoodsId.toString());
		String value = event.getFtypeB().toString();
		if (value.length() < 3) {
			value = "0" + value;
		}
		if (goodsService.countGoodsByNameAndClassId(fieldValue.trim(), value, fgoodsId, id).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 验证属性值排序是否
	 * 
	 * @param request
	 * @param fieldId
	 * @param fieldValue
	 * @param fextendClassId
	 * @param id
	 * @return
	 */

	@ResponseBody
	@RequestMapping(value = "/checkTypeValueSort", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkTypeValueSort(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue,
			@RequestParam(required = false, name = "fextendClassId") Long fextendClassId,
			@RequestParam(required = false, name = "fid") Long id) {
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (goodsService.countTypeValueBySortAndClassId(Integer.valueOf(fieldValue.trim()), fextendClassId, id)
				.equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 依据商品的ID获得对应所有的sku列表
	 * 
	 * @param request
	 * @param response
	 * @param eventId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getSkuList", method = RequestMethod.POST)
	public String getSkuList(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		if (StringUtils.isNotBlank(map.get("eventId").toString())) {
			List<Map<String, Object>> list = null;
			try {
				list = goodsService.getSkuList(map);

			} catch (Exception e) {
				logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			}
			// 操作信息的map，将map通过json方式返回给页面
			Map<String, Object> returnMap = new HashMap<String, Object>();
			returnMap.put("draw", map.get("draw").toString());
			returnMap.put("recordsTotal", list.size());
			returnMap.put("recordsFiltered", list.size());
			returnMap.put("data", list);
			return mapper.toJson(returnMap);
		} else {
			// 操作信息的map，将map通过json方式返回给页面
			Map<String, Object> returnMap = new HashMap<String, Object>();
			returnMap.put("draw", "0");
			returnMap.put("recordsTotal", 0);
			returnMap.put("recordsFiltered", 0);
			returnMap.put("data", StringUtils.EMPTY);
			return mapper.toJson(returnMap);
		}
	}

	/**
	 * 添加sku
	 * 
	 * @author maxiao
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/addSku", method = RequestMethod.POST)
	public String addSku(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			String articleId = goodsService.addSku(map);
			if ("1".equals(articleId)) {
				returnMap.put("success", false);
				returnMap.put("msg", "属性设置与默认库存属性设置不一致,保存sku失败！");
			} else {
				returnMap.put("success", true);
				returnMap.put("msg", "保存sku成功！");
				LogUitls.putArgs(
						LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), articleId }));
			}

		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "保存sku失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 编辑sku页面回显功能
	 * 
	 * @param request
	 * @param response
	 * @param itemId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getSkuInfo/{skuId}", method = RequestMethod.POST)
	public String getSku(HttpServletRequest request, HttpServletResponse response, @PathVariable String skuId) {
		RequestContext requestContext = new RequestContext(request);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			TGoodsSku goodsSku = goodsService.getSkuById(skuId);
			String valueAndfgoodsNO = goodsSku.getFgoodsNO();
			if (valueAndfgoodsNO != null && valueAndfgoodsNO != "") {
				returnMap.put("value", valueAndfgoodsNO.substring(0, 3));
				returnMap.put("fgoodsNO", valueAndfgoodsNO.substring(3));
			} else {
				TEvent event = goodsService.getEvent(goodsSku.getFgoodsId());
				Integer value = event.getFtypeB();
				if (value != null) {
					int leng = value.toString().length();
					String temp = value.toString();
					if (leng < 3) {
						for (int i = 0; i < 3 - leng; i++) {
							temp = "0" + temp;
						}
					}
					returnMap.put("value", temp);
				}

			}
			returnMap.put("id", goodsSku.getId());
			returnMap.put("fgoodsId", goodsSku.getFgoodsId());
			returnMap.put("fclassTypeValue1", goodsSku.getFclassTypeValue1());
			returnMap.put("fclassTypeValue2", goodsSku.getFclassTypeValue2());
			returnMap.put("ftotal", goodsSku.getFtotal());
			returnMap.put("fstock", goodsSku.getFstock());
			returnMap.put("flimitation", goodsSku.getFlimitation());
			returnMap.put("flag", goodsSku.getFlag());
			returnMap.put("fhavingImage", goodsSku.getFhavingImage());
			returnMap.put("fprice", goodsSku.getFprice());
			returnMap.put("fpriceMoney", goodsSku.getFpriceMoney());
			returnMap.put("fimage", goodsSku.getFimage());
			if (goodsSku.getFimage() != null) {
				TImage tImage = imageDAO.findOne(Long.valueOf(goodsSku.getFimage()));
				returnMap.put("imageWidth", tImage.getImageWidth());
				returnMap.put("imageHeight", tImage.getImageHeight());
				returnMap.put("fimagePath", fxlService.getImageUrl(goodsSku.getFimage(), true));
			}
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", requestContext.getMessage("获取商品属性值信息失败"));
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 删除某一sku
	 * 
	 * @param request
	 * @param response
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/delSku/{skuId}", method = RequestMethod.POST)
	public String delSku(HttpServletRequest request, HttpServletResponse response, @PathVariable String skuId) {
		RequestContext requestContext = new RequestContext(request);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			goodsService.deleteSku(skuId);
			returnMap.put("success", true);
			returnMap.put("msg", requestContext.getMessage("删除sku成功"));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", requestContext.getMessage("删除sku失败"));
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 跳转到sku列表页面,查找对应商品的所有sku
	 * 
	 * @param request
	 * @param model
	 * @param eventId
	 * @return
	 */
	@RequestMapping(value = "/toSs/{eventId}", method = RequestMethod.GET)
	public String toSku(HttpServletRequest request, Model model, @PathVariable String eventId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		try {
			/*
			 * // 获取到活动类目缓存对象 Cache eventCategoryCache =
			 * cacheManager.getCache(Constant.EventCategory); Element ele =
			 * null;
			 */
			TEvent tEvent = goodsService.getEvent(eventId);
			model.addAttribute("event", tEvent);
			model.addAttribute("eventId", eventId);
			if (tEvent.getFtypeB() != null) {
				int leng = tEvent.getFtypeB().toString().length();
				String temp = tEvent.getFtypeB().toString();
				if (leng < 3) {
					for (int i = 0; i < 3 - leng; i++) {
						temp = "0" + temp;
					}
				}
				model.addAttribute("value", temp);
			}
			Integer ftypeBValue = tEvent.getFtypeB();// 获得是category的value值
			// 根据value获得对应分类的Id
			TEventCategory category = goodsService.getCategoryByValue(ftypeBValue);
			Long ftypeBId = category.getId();
			// 通过商品二级类目获得属性
			if (null != ftypeBId) {
				//
				List<TGoodsTypeClassCategory> typeClassCategoryList = goodsService.getTypeCategory(ftypeBId);
				if (null != typeClassCategoryList && typeClassCategoryList.size() > 0) {// typeClassCategoryList的size大于0,属性一肯定存在
					// 获得属性一的id
					Long typeClass1Id = typeClassCategoryList.get(0).getFtypeId();
					TGoodsTypeClass typeClass1 = goodsService.getTypeClassById(typeClass1Id);
					model.addAttribute("typeClas1", typeClass1);
					// 通过属性id得到属性值
					List<TGoodsTypeClassValue> typeValueList1 = goodsService.getTypeValueListByTypeId(typeClass1Id);
					model.addAttribute("typeValueList1", typeValueList1);
				}
				if (typeClassCategoryList.size() == 2) {// 如果typeClassCategoryList说明此分类关联2个属性规格
					// 属性二的id
					Long typeClass2Id = typeClassCategoryList.get(1).getFtypeId();
					TGoodsTypeClass typeClass2 = goodsService.getTypeClassById(typeClass2Id);
					model.addAttribute("typeClas2", typeClass2);// 属性二
					// 通过id得到属性值
					List<TGoodsTypeClassValue> typeValueList2 = goodsService.getTypeValueListByTypeId(typeClass2Id);// 属性值
					model.addAttribute("typeValueList2", typeValueList2);
				}

			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
		return "fxl/event/eventSku";
	}

	/**
	 * 获得所有的商品规格及包装列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */

	@ResponseBody
	@RequestMapping(value = "/getSpecList", method = RequestMethod.POST)
	public String getSpecList(HttpServletRequest request, HttpServletResponse response) {
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		if (StringUtils.isNotBlank(map.get("fgoodsId").toString())) {
			List<Map<String, Object>> list = null;
			try {
				list = goodsService.getSpecList(map);

			} catch (Exception e) {
				logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			}
			// 操作信息的map，将map通过json方式返回给页面
			Map<String, Object> returnMap = new HashMap<String, Object>();
			returnMap.put("draw", map.get("draw").toString());
			returnMap.put("recordsTotal", list.size());
			returnMap.put("recordsFiltered", list.size());
			returnMap.put("data", list);
			return mapper.toJson(returnMap);
		} else {
			// 操作信息的map，将map通过json方式返回给页面
			Map<String, Object> returnMap = new HashMap<String, Object>();
			returnMap.put("draw", "0");
			returnMap.put("recordsTotal", 0);
			returnMap.put("recordsFiltered", 0);
			returnMap.put("data", StringUtils.EMPTY);
			return mapper.toJson(returnMap);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/addSpecAndPacking", method = RequestMethod.POST)
	public String addSpecAndPacking(HttpServletRequest request, HttpServletResponse response,
			TGoodsSpaceValue goodsSpaceValue) {
		RequestContext requestContext = new RequestContext(request);
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		boolean isAdd = false;
		if (goodsSpaceValue != null && goodsSpaceValue.getId() == null) {// id为空,则为新增
			isAdd = true;
		}

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			if (isAdd) {
				String fgoodsId = null;
				if (!map.containsKey("fgoodsId") && StringUtils.isBlank(map.get("fgoodsId").toString())) {
					throw new ServiceException("缺少配置商品规格和包装信息，无法保存商品规格和包装信息！");
				}
				fgoodsId = map.get("fgoodsId").toString();
				goodsSpaceValue.setFgoodsId(fgoodsId);
				goodsService.addSpecAndPacking(goodsSpaceValue);
			} else {
				goodsService.updateSpecAndPacking(goodsSpaceValue);
			}
			returnMap.put("success", true);
			if (isAdd) {
				returnMap.put("msg", requestContext.getMessage("新增商品规格和包装信息成功"));
			} else {
				returnMap.put("msg", requestContext.getMessage("编辑商品规格和包装信息成功"));
			}
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			if (isAdd) {
				returnMap.put("msg", requestContext.getMessage("新增商品属性值失败"));
			} else {
				returnMap.put("msg", requestContext.getMessage("编辑商品属性值失败"));
			}
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * sku编辑后保存
	 * 
	 * @param request
	 * @param response
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/editSku", method = RequestMethod.POST)
	public String editSku(HttpServletRequest request, HttpServletResponse response, @RequestParam String id) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			goodsService.editSku(map);
			returnMap.put("msg", "编辑商品sku信息成功!");
			returnMap.put("success", true);
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑商品sku信息失败!");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 获取该商品的所有参数
	 * 
	 * @param request
	 * @param response
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getGoodsSpecList", method = RequestMethod.POST)
	public String getGoodsSpecList(HttpServletRequest request, HttpServletResponse response,
			@RequestParam String fgoodsId) {

		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		if (StringUtils.isNotBlank(map.get("fgoodsId").toString())) {
			List<Map<String, Object>> list = null;
			try {
				list = goodsService.getSpecList(map);
			} catch (Exception e) {
				logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			}
			// 操作信息的map，将map通过json方式返回给页面
			Map<String, Object> returnMap = new HashMap<String, Object>();
			returnMap.put("draw", map.get("draw").toString());
			returnMap.put("recordsTotal", list.size());
			returnMap.put("recordsFiltered", list.size());
			returnMap.put("data", list);
			return mapper.toJson(returnMap);
		} else {
			// 操作信息的map，将map通过json方式返回给页面
			Map<String, Object> returnMap = new HashMap<String, Object>();
			returnMap.put("draw", "0");
			returnMap.put("recordsTotal", 0);
			returnMap.put("recordsFiltered", 0);
			returnMap.put("data", StringUtils.EMPTY);
			return mapper.toJson(returnMap);
		}
	}

	/**
	 * 下载导入参数的excel模板
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */

	@ResponseBody
	@RequestMapping(value = "/downLoadTemplate")
	public String downLoadTemplate(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			// 1.创建一个workbook，对应一个Excel文件
			HSSFWorkbook wb = new HSSFWorkbook();
			// 2.在workbook中添加一个sheet，对应Excel中的一个sheet
			HSSFSheet sheet = wb.createSheet("参数表");
			sheet.autoSizeColumn(1);// 设置每个单元格宽度根据字多少自适应
			// 3.在sheet中添加表头第0行
			HSSFRow row = sheet.createRow((int) 0);
			HSSFFont font = wb.createFont();
			font.setFontHeightInPoints((short) 12); // 设置字体的大小
			font.setFontName("微软雅黑"); // 设置字体的样式，如：宋体、微软雅黑等
			font.setItalic(false); // 斜体true为斜体
			font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); // 对文中进行加粗
			font.setColor(HSSFColor.BLACK.index); // 设置字体的颜色
			// 4.创建单元格，设置值表头，设置表头居中
			HSSFCellStyle style = wb.createCellStyle();
			sheet.setColumnWidth(0, 5000); // 设置列宽
			sheet.setColumnWidth(1, 10000); // 设置列宽
			style.setFont(font);
			// 居中格式
			style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			// 设置垂直对齐的样式为居中对齐;
			style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			// 设置单元格边框
			style.setBorderBottom(HSSFCellStyle.BORDER_THIN);// 下
			style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左
			style.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右
			// style.setBorderTop(HSSFCellStyle.BORDER_DOUBLE);//上

			style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND); // 设置单元格北京颜色

			// 设置表头
			HSSFCell cell = row.createCell(0, CellType.STRING);// 参数类型
			cell.setCellValue("参数名称");
			cell.setCellStyle(style);

			cell = row.createCell(1, CellType.STRING);
			cell.setCellValue("参数值");
			cell.setCellStyle(style);

			// 下载后保存的文件名称
			String fileName = "参数导入模板" + DateTransferUtils.getDate("yyyyMMddHHmmss");
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			wb.write(os);
			byte[] content = os.toByteArray();
			InputStream is = new ByteArrayInputStream(content);
			// 设置response参数，可以打开下载页面
			response.reset();
			response.setContentType("application/vnd.ms-excel;charset=utf-8");
			response.setHeader("Content-Disposition",
					"attachment;filename=" + new String((fileName + ".xls").getBytes(), "iso-8859-1"));
			ServletOutputStream out = response.getOutputStream();

			bis = new BufferedInputStream(is);
			bos = new BufferedOutputStream(out);
			byte[] buff = new byte[2048];
			int bytesRead;
			while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
				bos.write(buff, 0, bytesRead);
			}
			returnMap.put("success", true);
			returnMap.put("msg", "下载参数模板成功");
		} catch (Exception e) {
			e.printStackTrace();
			returnMap.put("success", false);
			returnMap.put("msg", "下载参数模板失败");
		} finally {
			if (bis != null)
				bis.close();
			if (bos != null)
				bos.close();
		}
		return mapper.toJson(returnMap);

	}

	/**
	 * POI:excel表中数据导入数据库
	 * 
	 * @param request
	 * @param response
	 * @return
	 */

	@ResponseBody
	@RequestMapping(value = "/import", method = RequestMethod.POST, produces={"application/json;charset=utf-8"})
	public String upload(HttpServletRequest request, HttpServletResponse response,
			@RequestParam MultipartFile uploadFile) {

		Map<String, Object> returnMap = new HashMap<String, Object>();// 返回json格式数据给页面
		try {

			String fgoodsId = request.getParameter("fgoodsId");
			// 1.workbook读取整个excel
			InputStream stream = uploadFile.getInputStream();
			HSSFWorkbook wb = new HSSFWorkbook(stream);
			// 2.读取sheet页
			HSSFSheet sheet = wb.getSheetAt(0);
			// 3.循环读取行
			List<TGoodsSpaceValue> list = new ArrayList<TGoodsSpaceValue>();
			int num = 0;
			if (sheet != null) {
				int totalRowNum = sheet.getLastRowNum();

				for (int rowNum = 1; rowNum <= totalRowNum; rowNum++) {
					HSSFRow row = sheet.getRow(rowNum);
					if (row == null) {
						continue;
					}
					// 读取单元格
					if (row.getCell(1) != null && row.getCell(0) != null) {
						row.getCell(0).setCellType(Cell.CELL_TYPE_STRING);
						row.getCell(1).setCellType(Cell.CELL_TYPE_STRING);
						String fspaceName = row.getCell(0).getStringCellValue();
						Long count = goodsService.countBySpaceName(fspaceName, fgoodsId);
						if (count > 0) {
							num++;
							continue;

						}
						String fvalueName = row.getCell(1).getStringCellValue();
						// 将数据封装到TGoodsSpaceValue对象中
						TGoodsSpaceValue value = new TGoodsSpaceValue();
						value.setFspaceName(fspaceName);
						value.setFvalueName(fvalueName);
						value.setFgoodsId(fgoodsId);
						list.add(value);
					}

				}
				goodsService.saveGoodsSpec(list);
				returnMap.put("success", true);
				if(num>0){
					returnMap.put("msg", "导入" + (totalRowNum - num) + "条参数成功，" + num + "条参数已存在，导入失败");	
				}else{
					returnMap.put("msg", "导入" + (totalRowNum - num) + "条参数成功");
				}
				

			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "导入参数失败");
		}

		return mapper.toJson(returnMap);

	}

}