package com.czyh.czyhweb.web.coupon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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

import com.czyh.czyhweb.entity.TArticle;
import com.czyh.czyhweb.entity.TConsult;
import com.czyh.czyhweb.entity.TCoupon;
import com.czyh.czyhweb.entity.TCouponChannel;
import com.czyh.czyhweb.entity.TImage;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.FxlService;
import com.czyh.czyhweb.service.coupon.CouponService;
import com.czyh.czyhweb.service.customer.UserService;
import com.czyh.czyhweb.service.finance.SettlementService;
import com.czyh.czyhweb.service.goods.GoodsService;
import com.czyh.czyhweb.service.order.OrderService;
import com.czyh.czyhweb.service.system.OperatingService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.DictionaryUtil;

/**
 * 活动运营优惠券模块的操作类
 * 
 * @author qkk
 * 
 */
@Controller
@RequestMapping("/fxl/coupon")
public class CouponController {

	private static Logger logger = LoggerFactory.getLogger(CouponController.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private FxlService fxlService;

	@Autowired
	private GoodsService goodsService;

	@Autowired
	private OperatingService operatingService;

	@Autowired
	private CouponService couponService;

	@Autowired
	private UserService userService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private SettlementService settlementService;

	@RequestMapping(value = "/coupon/apply", method = RequestMethod.GET)
	public String apply(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("couponUseMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.CouponUseType, shiroUser.getLanguage()));
		model.addAttribute("couponDeliverMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.CouponDeliverType, shiroUser.getLanguage()));
		model.addAttribute("cityMap", DictionaryUtil.getStatueMap(DictionaryUtil.City, shiroUser.getLanguage()));
		model.addAttribute("categoryMapA", goodsService.getCategoryMapA());
		return "fxl/operating/couponApply";
	}

	/**
	 * 获取订单列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/coupon/getApplyList", method = RequestMethod.POST)
	public String getApplyList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));
		operatingService.getApplyList(map, page);
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
	@RequestMapping(value = "/coupon/getCoupon/{couponId}", method = RequestMethod.POST)
	public String getCoupon(HttpServletRequest request, HttpServletResponse response, @PathVariable String couponId) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TCoupon tCoupon = operatingService.getCoupon(couponId);
			returnMap.put("ftitle", tCoupon.getFtitle());
			returnMap.put("fcouponNum", tCoupon.getFcouponNum());
			returnMap.put("fcity", tCoupon.getFcity());
			returnMap.put("fdescription", tCoupon.getFdescription());
			returnMap.put("fnotice", tCoupon.getFnotice());
			returnMap.put("fuseRange", tCoupon.getFuseRange());
			returnMap.put("fdeliverStartTime", tCoupon.getFdeliverStartTime() != null
					? DateFormatUtils.format(tCoupon.getFdeliverStartTime(), "yyyy-MM-dd HH:mm") : StringUtils.EMPTY);
			returnMap.put("fdeliverEndTime", tCoupon.getFdeliverEndTime() != null
					? DateFormatUtils.format(tCoupon.getFdeliverEndTime(), "yyyy-MM-dd HH:mm") : StringUtils.EMPTY);
			returnMap.put("fuseStartTime", tCoupon.getFuseStartTime() != null
					? DateFormatUtils.format(tCoupon.getFuseStartTime(), "yyyy-MM-dd") : StringUtils.EMPTY);
			returnMap.put("fuseEndTime", tCoupon.getFuseEndTime() != null
					? DateFormatUtils.format(tCoupon.getFuseEndTime(), "yyyy-MM-dd") : StringUtils.EMPTY);
			returnMap.put("famount", tCoupon.getFamount());
			if (tCoupon.getFdiscount() != null) {
				BigDecimal discount = tCoupon.getFdiscount().multiply(new BigDecimal(100));
				returnMap.put("fdiscount", discount.toString());
			} else {
				returnMap.put("fdiscount", StringUtils.EMPTY);
			}
			returnMap.put("flimitation", tCoupon.getFlimitation());
			returnMap.put("fcount", tCoupon.getFcount());
			returnMap.put("fuseType", tCoupon.getFuseType());
			returnMap.put("fdeliverType", tCoupon.getFdeliverType());

			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取优惠券申请详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/coupon/addCoupon", method = RequestMethod.POST)
	public String addCoupon(HttpServletRequest request, HttpServletResponse response) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			operatingService.saveCoupon(map);
			returnMap.put("success", true);
			returnMap.put("msg", "创建优惠券申请信息成功!");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "创建优惠券申请信息失败!");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/coupon/editCoupon", method = RequestMethod.POST)
	public String editCoupon(HttpServletRequest request, HttpServletResponse response) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		try {
			operatingService.editCoupon(map);
			returnMap.put("msg", "编辑优惠券申请信息成功!");
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑优惠券申请信息失败!");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/coupon/delCoupon/{couponId}", method = RequestMethod.POST)
	public String delCoupon(HttpServletRequest request, HttpServletResponse response, @PathVariable String merchantId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			operatingService.delCoupon(merchantId);
			returnMap.put("success", true);
			returnMap.put("msg", "优惠券申请信息删除成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "优惠券申请信息删除失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 将优惠券发放给用户的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/coupon/issueCoupon/{couponId}", method = RequestMethod.POST)
	public String issueCoupon(HttpServletRequest request, Model model, @PathVariable String couponId) {

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			operatingService.issueCoupon(couponId);
			returnMap.put("success", true);
			returnMap.put("msg", "优惠券发放给用户操作成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "优惠券发放给用户操作失败！");
		}
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/article", method = RequestMethod.GET)
	public String article(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("cityMap", DictionaryUtil.getStatueMap(DictionaryUtil.City, shiroUser.getLanguage()));
		return "fxl/operating/article";
	}

	/**
	 * 获取发现文章列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/article/getArticleList", method = RequestMethod.POST)
	public String getArticleList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		try {
			operatingService.getArticleList(map, page);
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
	 * 根据项目ID返回项目详细信息
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/article/getArticle/{articleId}", method = RequestMethod.POST)
	public String getArticle(HttpServletRequest request, HttpServletResponse response, @PathVariable String articleId) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TArticle tArticle = operatingService.getArticle(articleId);
			returnMap.put("fcityId", tArticle.getFcityId());
			returnMap.put("ftitle", tArticle.getFtitle());
			returnMap.put("ftype", tArticle.getFtype());
			returnMap.put("fbrief", tArticle.getFbrief());
			returnMap.put("fdetail", tArticle.getFdetail());
			returnMap.put("forder", tArticle.getForder());
			Long imageId = tArticle.getFimage();
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
			returnMap.put("msg", "获取文章详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/article/addArticle", method = RequestMethod.POST)
	public String addArticle(HttpServletRequest request, HttpServletResponse response) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			operatingService.saveArticle(map);
			returnMap.put("success", true);
			returnMap.put("msg", "创建文章信息成功!");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "创建文章信息失败!");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/article/editArticle", method = RequestMethod.POST)
	public String editArticle(HttpServletRequest request, HttpServletResponse response) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		try {
			operatingService.editArticle(map);
			returnMap.put("msg", "编辑文章信息成功!");
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑文章信息失败!");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/article/delArticle/{articleId}", method = RequestMethod.POST)
	public String delArticle(HttpServletRequest request, HttpServletResponse response, @PathVariable String articleId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			operatingService.delArticle(articleId);
			returnMap.put("success", true);
			returnMap.put("msg", "该文章信息删除成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "该文章信息删除失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 文章即刻下架
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/article/onSaleArticle/{articleId}", method = RequestMethod.POST)
	public String onSaleArticle(HttpServletRequest request, Model model, @PathVariable String articleId) {

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			operatingService.onSaleArticle(articleId);

			returnMap.put("success", true);
			returnMap.put("msg", "文章即刻上架操作成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "文章即刻上架操作失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 文章即刻下架
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/article/offSaleArticle/{articleId}", method = RequestMethod.POST)
	public String offSaleArticle(HttpServletRequest request, Model model, @PathVariable String articleId) {

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			operatingService.offSaleArticle(articleId);

			returnMap.put("success", true);
			returnMap.put("msg", "文章即刻下架操作成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "文章即刻下架操作失败！");
		}
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/consult/reply", method = RequestMethod.GET)
	public String reply(HttpServletRequest request, Model model) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		// model.addAttribute("cityMap",
		// DictionaryUtil.getStatueMap(DictionaryUtil.City,
		// shiroUser.getLanguage()));
		return "fxl/operating/consultReply";
	}

	/**
	 * 获取发现文章列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/consult/getConsultList", method = RequestMethod.POST)
	public String getConsultList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		try {
			operatingService.getConsultList(map, page);
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
	 * 根据项目ID返回项目详细信息
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/consult/getConsult/{consultId}", method = RequestMethod.POST)
	public String getConsult(HttpServletRequest request, HttpServletResponse response, @PathVariable String consultId) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TConsult tConsult = operatingService.getConsult(consultId);
			returnMap.put("eventInfo", goodsService.getEventInfo(tConsult.getFobjectId()));
			returnMap.put("fcustomerName", tConsult.getFcustomerName());
			returnMap.put("fcontent", tConsult.getFcontent());
			returnMap.put("freply", tConsult.getFreply());
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取咨询详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/consult/reply", method = RequestMethod.POST)
	public String replyConsult(HttpServletRequest request, HttpServletResponse response) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			operatingService.saveConsult(map);
			returnMap.put("success", true);
			returnMap.put("msg", "回复咨询操作成功!");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "回复咨询操作失败!");
		}
		return mapper.toJson(returnMap);
	}

	/******************************** 新版优惠券 ************************************/

	/**
	 * 调到定向投放活动列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/deliverylist", method = RequestMethod.GET)
	public String manage(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<Integer, String> CouponAuditStatusMap = ObjectUtils
				.clone(DictionaryUtil.getStatueMap(DictionaryUtil.CouponAuditStatus, shiroUser.getLanguage()));
		CouponAuditStatusMap.remove(20);
		CouponAuditStatusMap.remove(60);
		CouponAuditStatusMap.remove(999);
		CouponAuditStatusMap.remove(40);
		CouponAuditStatusMap.remove(100);
		CouponAuditStatusMap.remove(110);
		CouponAuditStatusMap.remove(120);
		model.addAttribute("CouponAuditStatusMap", CouponAuditStatusMap);
		List codeList = new ArrayList<String>();
		codeList.add("kaola");
		model.addAttribute("editorList", userService.getUserListByRoleCode(codeList));
		return "fxl/coupon/deliveryManage";
	}

	/**
	 * 获取定向投放活动列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getDeliveryList", method = RequestMethod.POST)
	public String getEventList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));
		couponService.getDeliveryList(map, page);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	/**
	 * 调到添加定向投放活动页的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/toCouponCreateMain", method = RequestMethod.GET)
	public String toEventCreateMain(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("couponUseMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.CouponUseType, shiroUser.getLanguage()));
		model.addAttribute("categoryMapA", goodsService.getChannelSetting());
		model.addAttribute("cityMap", DictionaryUtil.getStatueMap(DictionaryUtil.City, shiroUser.getLanguage()));
		List codeList = new ArrayList<String>();
		codeList.add("kaola");
		model.addAttribute("editorList", userService.getUserListByRoleCode(codeList));
		model.addAttribute("userPointMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.UserPoint, shiroUser.getLanguage()));
		return "fxl/coupon/couponCreateMain";
	}

	/**
	 * 获取定向投放活动优惠券列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getCouponList", method = RequestMethod.POST)
	public String getCouponList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));
		couponService.getCouponList(map, page);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	/**
	 * 保存定向投放活动优惠券的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/saveCoupon", method = RequestMethod.POST)
	public String saveCoupon(HttpServletRequest request, HttpServletResponse response) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			couponService.saveCoupon(map);
			returnMap.put("success", true);
			returnMap.put("msg", "创建优惠券申请信息成功!");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "创建优惠券申请信息失败!");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 删除优惠券
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/delcoupon/{couponId}", method = RequestMethod.POST)
	public String delEventAssociate(HttpServletRequest request, Model model, @PathVariable String couponId) {

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			couponService.delCoupon(couponId);
			returnMap.put("success", true);
			returnMap.put("msg", "删除优惠券信息成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "删除优惠券信息失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 调到添加活动信息页面的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/toDeliveryCreate", method = RequestMethod.GET)
	public String toDeliveryCreate(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<Integer, String> fDeliverTypeMap = ObjectUtils
				.clone(DictionaryUtil.getStatueMap(DictionaryUtil.CouponDeliverType, shiroUser.getLanguage()));
		fDeliverTypeMap.remove(20);
		fDeliverTypeMap.remove(30);
		fDeliverTypeMap.remove(40);
		fDeliverTypeMap.remove(50);
		fDeliverTypeMap.remove(60);
		fDeliverTypeMap.remove(70);
		fDeliverTypeMap.remove(90);
		fDeliverTypeMap.remove(100);
		fDeliverTypeMap.remove(110);
		fDeliverTypeMap.remove(120);
		fDeliverTypeMap.remove(130);
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		model.addAttribute("couponInfoMap", map.get("ids"));
		model.addAttribute("fDeliverTypeMap", fDeliverTypeMap);
		return "fxl/coupon/deliveryCreate";
	}

	/**
	 * 校验按客户列表发放客户列表的准确性的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/checkPhoneList", method = RequestMethod.POST)
	public String checkPhoneList(HttpServletRequest request, HttpServletResponse response) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			List<Map<String, Object>> list = couponService.checkPhoneList(map);
			if (list != null && list.size() != 0) {
				returnMap.put("success", true);
				returnMap.put("msg", "此次发放有效客户数共为" + list.size() + "!");
			} else {
				returnMap.put("success", false);
				returnMap.put("msg", "此次发放有效客户数共为" + list.size() + "，请重新检查输入手机号!");
			}

		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "校验客户列表失败，请重新检查输入手机号!");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 保存定向投放活动信息的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/savedelivery", method = RequestMethod.POST)
	public String savedelivery(HttpServletRequest request, HttpServletResponse response) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			couponService.savedelivery(map);
			returnMap.put("success", true);
			returnMap.put("msg", "创建定向投放活动信息成功!");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "创建定向投放活动信息失败!");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 查看定向投放活动信息详情的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/deliveryView/{deliveryId}", method = RequestMethod.GET)
	public String eventView(HttpServletRequest request, Model model, @PathVariable String deliveryId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<String, Object> tDelivery = couponService.getDeliveryDetail(deliveryId);
		model.addAttribute("fdeliveryId", deliveryId);
		model.addAttribute("tDelivery", tDelivery);
		return "fxl/coupon/deliveryView";
	}

	/**
	 * 获取定向投放活动优惠券列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getDeliveryCoupon", method = RequestMethod.POST)
	public String getDeliveryCoupon(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));
		couponService.getDeliveryCoupon(map, page);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	/**
	 * 调到添加定向投放活动审核页的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/deliverylistcheck", method = RequestMethod.GET)
	public String deliverylistcheck(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<Integer, String> CouponAuditStatusMap = ObjectUtils
				.clone(DictionaryUtil.getStatueMap(DictionaryUtil.CouponAuditStatus, shiroUser.getLanguage()));
		CouponAuditStatusMap.remove(20);
		CouponAuditStatusMap.remove(60);
		CouponAuditStatusMap.remove(999);
		CouponAuditStatusMap.remove(40);
		CouponAuditStatusMap.remove(100);
		CouponAuditStatusMap.remove(110);
		model.addAttribute("CouponAuditStatusMap", CouponAuditStatusMap);
		List codeList = new ArrayList<String>();
		codeList.add("kaola");
		model.addAttribute("editorList", userService.getUserListByRoleCode(codeList));
		return "fxl/coupon/deliveryManagecheck";
	}

	/**
	 * 审核活动
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/auditDelivery", method = RequestMethod.POST)
	public String successPush(HttpServletRequest request, Model model) {

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			couponService.auditDelivery(map);
			returnMap.put("success", true);
			returnMap.put("msg", "审核该活动操作成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "审核该活动操作失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 调到定向投放活动列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/issueList", method = RequestMethod.GET)
	public String issueList(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<Integer, String> CouponAuditStatusMap = ObjectUtils
				.clone(DictionaryUtil.getStatueMap(DictionaryUtil.CouponAuditStatus, shiroUser.getLanguage()));
		CouponAuditStatusMap.remove(20);
		CouponAuditStatusMap.remove(60);
		CouponAuditStatusMap.remove(999);
		CouponAuditStatusMap.remove(90);
		CouponAuditStatusMap.remove(110);
		model.addAttribute("CouponAuditStatusMap", CouponAuditStatusMap);
		List codeList = new ArrayList<String>();
		codeList.add("kaola");
		model.addAttribute("editorList", userService.getUserListByRoleCode(codeList));
		return "fxl/coupon/issueListManage";
	}

	/**
	 * 调到添加客户领取活动页的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/toissueCreateMain", method = RequestMethod.GET)
	public String toissueCreateMain(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("couponUseMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.CouponUseType, shiroUser.getLanguage()));
		model.addAttribute("categoryMapA", goodsService.getCategoryMapA());
		model.addAttribute("cityMap", DictionaryUtil.getStatueMap(DictionaryUtil.City, shiroUser.getLanguage()));
		List codeList = new ArrayList<String>();
		codeList.add("kaola");
		model.addAttribute("editorList", userService.getUserListByRoleCode(codeList));
		model.addAttribute("userPointMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.UserPoint, shiroUser.getLanguage()));
		return "fxl/coupon/toIssueCreateMain";
	}

	/**
	 * 调到添加活动信息页面的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/toissueDeliveryCreate", method = RequestMethod.GET)
	public String toissueDeliveryCreate(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<Integer, String> fDeliverTypeMap = ObjectUtils
				.clone(DictionaryUtil.getStatueMap(DictionaryUtil.CouponDeliverType, shiroUser.getLanguage()));
		fDeliverTypeMap.remove(10);
		fDeliverTypeMap.remove(20);
		fDeliverTypeMap.remove(30);
		fDeliverTypeMap.remove(40);
		fDeliverTypeMap.remove(50);
		fDeliverTypeMap.remove(60);
		fDeliverTypeMap.remove(70);
		fDeliverTypeMap.remove(80);
		fDeliverTypeMap.remove(90);
		fDeliverTypeMap.remove(100);
		fDeliverTypeMap.remove(110);
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		model.addAttribute("couponInfoMap", map.get("ids"));
		model.addAttribute("fDeliverTypeMap", fDeliverTypeMap);
		return "fxl/coupon/issueDeliveyCreate";
	}

	/**
	 * 调到添加定向投放客户领取活动审核页的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/issueCheck", method = RequestMethod.GET)
	public String issueCheck(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<Integer, String> CouponAuditStatusMap = ObjectUtils
				.clone(DictionaryUtil.getStatueMap(DictionaryUtil.CouponAuditStatus, shiroUser.getLanguage()));
		CouponAuditStatusMap.remove(20);
		CouponAuditStatusMap.remove(60);
		CouponAuditStatusMap.remove(999);
		CouponAuditStatusMap.remove(90);
		CouponAuditStatusMap.remove(110);
		model.addAttribute("CouponAuditStatusMap", CouponAuditStatusMap);
		List codeList = new ArrayList<String>();
		codeList.add("kaola");
		model.addAttribute("editorList", userService.getUserListByRoleCode(codeList));
		return "fxl/coupon/issueManageCheck";
	}

	/**
	 * 调到客户优惠券页面
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/couponLlist", method = RequestMethod.GET)
	public String couponLlist(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<Integer, String> CouponAuditStatusMap = ObjectUtils
				.clone(DictionaryUtil.getStatueMap(DictionaryUtil.CouponAuditStatus, shiroUser.getLanguage()));
		CouponAuditStatusMap.remove(20);
		CouponAuditStatusMap.remove(60);
		CouponAuditStatusMap.remove(999);
		CouponAuditStatusMap.remove(90);
		CouponAuditStatusMap.remove(40);
		CouponAuditStatusMap.remove(100);
		CouponAuditStatusMap.remove(110);
		CouponAuditStatusMap.remove(120);
		model.addAttribute("CouponAuditStatusMap", CouponAuditStatusMap);
		List codeList = new ArrayList<String>();
		codeList.add("kaola");
		model.addAttribute("editorList", userService.getUserListByRoleCode(codeList));
		return "fxl/coupon/couponlist";
	}

	/**
	 * 获取有优惠券的客户列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getcustomertList", method = RequestMethod.POST)
	public String getcustomertList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));
		couponService.getcustomertList(map, page);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	/**
	 * 查看客户优惠券详情的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getcustomertdetail/{customerId}", method = RequestMethod.POST)
	public String getcustomertdetail(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String customerId) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		map.put("customerId", customerId);
		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));
		couponService.getcustomertdetail(map, page);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	/**
	 * 发放优惠券
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/issueDelivery", method = RequestMethod.POST)
	public String issueDelivery(HttpServletRequest request, Model model) {

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			couponService.auditDelivery(map);
			returnMap.put("success", true);
			returnMap.put("msg", "优惠券发放给用户操作成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "优惠券发放给用户操作失败！");
		}
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/couponChannel", method = RequestMethod.GET)
	public String couponChannel(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("couponChannelMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.CouponChannelStatus, shiroUser.getLanguage()));
		return "fxl/app/appCouponChannel";
	}

	/**
	 * 获取客户列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getCouponChannelList", method = RequestMethod.POST)
	public String getCouponChannelList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		couponService.getCouponChannelList(map, page);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/addCouponChannel", method = RequestMethod.POST)
	public String addChannel(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			Map<String, Object> msg = couponService.addCouponChannel(map);
			if ((Integer) msg.get("status") != 1) {
				returnMap.put("success", false);
				returnMap.put("msg", msg.get("msg").toString());
			} else {
				returnMap.put("success", true);
				returnMap.put("msg", "创建优惠券频道信息保存成功！");
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "创建优惠券频道信息保存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/onsaleChannel/{id}", method = RequestMethod.POST)
	public String onsaleChannel(HttpServletRequest request, HttpServletResponse response, @PathVariable String id) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			couponService.updateChannelStatus(id, 10);
			returnMap.put("msg", "上架优惠券频道栏目信息成功！");
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "上架优惠券频道栏目信息失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/offsaleChannel/{id}", method = RequestMethod.POST)
	public String offsaleChannel(HttpServletRequest request, HttpServletResponse response, @PathVariable String id) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			couponService.updateChannelStatus(id, 20);
			returnMap.put("msg", "下架优惠券频道栏目信息成功！");
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "下架优惠券频道栏目信息失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 根据项目ID返回项目详细信息
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getCouponChannel/{channelId}", method = RequestMethod.POST)
	public String getCouponChannel(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String channelId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TCouponChannel tCouponChannel = couponService.getCouponChannel(channelId);
			returnMap.put("id", tCouponChannel.getId());
			returnMap.put("fDeliveryId", tCouponChannel.getFdeliveryId());
			returnMap.put("fname", tCouponChannel.getFcouponName());
			returnMap.put("fSubtitle", tCouponChannel.getFsubtitle());
			returnMap.put("fUseRange", tCouponChannel.getFuseRange());
			returnMap.put("forder", tCouponChannel.getForder());
			returnMap.put("fstartDate", DateFormatUtils.format(tCouponChannel.getFbeginTime(), "yyyy-MM-dd"));
			returnMap.put("fendDate", DateFormatUtils.format(tCouponChannel.getFendTime(), "yyyy-MM-dd"));
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取优惠券频道详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/editCouponChannel", method = RequestMethod.POST)
	public String editCouponChannel(HttpServletRequest request, HttpServletResponse response, @RequestParam String id) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		try {
			Map<String, Object> msg = couponService.editCouponChannel(map);
			if ((Integer) msg.get("status") != 1) {
				returnMap.put("success", false);
				returnMap.put("msg", msg.get("msg").toString());
			} else {
				returnMap.put("msg", "编辑优惠券频道信息保存成功！");
				returnMap.put("success", true);
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑优惠券频道信息保存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/createCouponChannelphoto", method = RequestMethod.POST)
	public String createCouponChannelphoto(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			String id = couponService.updateChannelStatus();
			returnMap.put("id", id);
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "配置轮播图栏目信息失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/test1", method = RequestMethod.GET)
	public void test1(HttpServletRequest request, Model model) {
		orderService.test();
	}

	@ResponseBody
	@RequestMapping(value = "/test2", method = RequestMethod.GET)
	public void test2(HttpServletRequest request, Model model) {
		settlementService.allSettlement();
	}

	@ResponseBody
	@RequestMapping(value = "/callInterface", method = RequestMethod.GET)
	public void callInterface(HttpServletRequest request, Model model) {
		fxlService.callInterface();
	}

}