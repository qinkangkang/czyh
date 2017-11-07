package com.czyh.czyhweb.web.customer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
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
import org.springframework.web.multipart.MultipartFile;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;
import org.springside.modules.utils.Identities;
import org.springside.modules.web.Servlets;

import com.czyh.czyhweb.dao.CommentDAO;
import com.czyh.czyhweb.dto.ImageUploadDTO;
import com.czyh.czyhweb.entity.TComment;
import com.czyh.czyhweb.entity.TCustomer;
import com.czyh.czyhweb.entity.TCustomerTag;
import com.czyh.czyhweb.entity.TImage;
import com.czyh.czyhweb.entity.TSponsor;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.log.Log;
import com.czyh.czyhweb.log.LogMessageObject;
import com.czyh.czyhweb.log.impl.LogUitls;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.FxlService;
import com.czyh.czyhweb.service.customer.CustomerService;
import com.czyh.czyhweb.service.customer.UserService;
import com.czyh.czyhweb.service.goods.GoodsService;
import com.czyh.czyhweb.service.system.OperatingService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.PropertiesUtil;

/**
 * 用户管理类
 * 
 * @author jinsey
 * 
 */
@Controller
@RequestMapping("/fxl/customer")
public class CustomerController {

	private static Logger logger = LoggerFactory.getLogger(CustomerController.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private CustomerService customerService;

	@Autowired
	private FxlService fxlService;

	@Autowired
	private UserService userService;

	@Autowired
	private GoodsService goodsService;

	@Autowired
	private CommentDAO commentDAO;

	@Autowired
	private OperatingService operatingService;

	@RequestMapping(value = "/merchant", method = RequestMethod.GET)
	public String merchant(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();

		model.addAttribute("sponsorTypeMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.SponsorTag, shiroUser.getLanguage()));
		model.addAttribute("sponsorTagMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.GoodsTag, shiroUser.getLanguage()));
		model.addAttribute("sponsorLevelMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.SponsorLevel, shiroUser.getLanguage()));
		Map<Integer, String> statusTempMap = ObjectUtils
				.clone(DictionaryUtil.getStatueMap(DictionaryUtil.UserStatus, shiroUser.getLanguage()));
		statusTempMap.remove(999);
		model.addAttribute("sponsorStatusMap", statusTempMap);
		model.addAttribute("bdList", userService.getUserListByCategoryId(2));
		model.addAttribute("editorList", userService.getUserListByCategoryId(3));
		model.addAttribute("shiroUserId", shiroUser.getId());
		model.addAttribute("regionList", customerService.getRegionList(1));

		return "fxl/customer/merchant";
	}

	/**
	 * 获取商户列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/merchant/getMerchantList", method = RequestMethod.POST)
	public String getMerchantList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		try {
			customerService.getMerchantList(map, page);
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
	 * 创建用户时AJAX验证登录名是否存在的方法。
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/merchant/checkUsername", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkUsername(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue) {
		// try {
		// fieldValue =
		// StringUtils.toEncodedString(fieldValue.getBytes("ISO-8859-1"),
		// Charset.forName("UTF-8"));
		// } catch (UnsupportedEncodingException e) {
		// logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		// }
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (customerService.checkUsername(fieldValue.trim()).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 创建用户时AJAX验证登录名是否存在的方法。
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/merchant/checkName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkName(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue) {
		// try {
		// fieldValue =
		// StringUtils.toEncodedString(fieldValue.getBytes("ISO-8859-1"),
		// Charset.forName("UTF-8"));
		// } catch (UnsupportedEncodingException e) {
		// logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		// }
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (customerService.checkName(fieldValue.trim()).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 创建用户时AJAX验证登录名是否存在的方法。
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/merchant/checkEditUsername", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkEditUsername(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue, @RequestParam("id") String id) {
		// try {
		// fieldValue =
		// StringUtils.toEncodedString(fieldValue.getBytes("ISO-8859-1"),
		// Charset.forName("UTF-8"));
		// } catch (UnsupportedEncodingException e) {
		// logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		// }
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (customerService.checkEditUsername(id, fieldValue.trim()).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 创建用户时AJAX验证登录名是否存在的方法。
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/merchant/checkEditName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String checkEditName(ServletRequest request, @RequestParam("fieldId") String fieldId,
			@RequestParam("fieldValue") String fieldValue, @RequestParam("id") String id) {
		// try {
		// fieldValue =
		// StringUtils.toEncodedString(fieldValue.getBytes("ISO-8859-1"),
		// Charset.forName("UTF-8"));
		// } catch (UnsupportedEncodingException e) {
		// logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		// }
		StringBuilder retString = new StringBuilder("[\"");
		retString.append(fieldId).append("\",");
		if (customerService.checkEditName(id, fieldValue.trim()).equals(0L)) {
			retString.append("true");
		} else {
			retString.append("false");
		}
		retString.append("]");
		return retString.toString();
	}

	/**
	 * 根据项目ID返回项目详细信息
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/merchant/getMerchant/{merchantId}", method = RequestMethod.POST)
	public String getMerchant(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String merchantId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TSponsor tSponsor = customerService.getMerchant(merchantId);
			TCustomer tCustomer = tSponsor.getTCustomer();
			returnMap.put("fusername", tCustomer.getFusername());
			returnMap.put("fmobile", tCustomer.getFphone());
			returnMap.put("fphone", tSponsor.getFphone());
			returnMap.put("fname", tSponsor.getFname());
			returnMap.put("ffullName", tSponsor.getFfullName());
			returnMap.put("fnumber", tSponsor.getFnumber());
			returnMap.put("fbrief", tSponsor.getFbrief());
			returnMap.put("fdetail", tSponsor.getFdetail());
			returnMap.put("fsponsorModel", tSponsor.getFsponsorModel());
			returnMap.put("ftype", tSponsor.getFtype());
			returnMap.put("flevel", tSponsor.getFlevel());
			returnMap.put("fwebSite", tSponsor.getFwebSite());
			returnMap.put("fcontractEffective", tSponsor.getFcontractEffective());
			if (tSponsor.getFrate() != null) {
				BigDecimal rate = tSponsor.getFrate().multiply(new BigDecimal(100));
				returnMap.put("frate", rate.toString());
			} else {
				returnMap.put("frate", StringUtils.EMPTY);
			}

			returnMap.put("fsponsorTag", tSponsor.getFsponsorTag());
			returnMap.put("fperPrice", tSponsor.getFperPrice());
			returnMap.put("frange", tSponsor.getFrange());
			returnMap.put("fpinkage", tSponsor.getFpinkage());
			returnMap.put("fregion", tSponsor.getFregion());

			returnMap.put("fbank", tSponsor.getFbank());
			returnMap.put("fbankAccount", tSponsor.getFbankAccount());
			returnMap.put("fbankAccountName", tSponsor.getFbankAccountName());
			returnMap.put("fbankAccountPersonId", tSponsor.getFbankAccountPersonId());
			returnMap.put("faddress", tSponsor.getFaddress());
			returnMap.put("fgps", tSponsor.getFgps());
			returnMap.put("fstatus", tSponsor.getFstatus());
			returnMap.put("fbdId", tSponsor.getFbdId());
			returnMap.put("fcreaterId",
					tSponsor.getFcreaterId() != null ? tSponsor.getFcreaterId() : shiroUser.getId());

			String imageId = tSponsor.getFimage();
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
			returnMap.put("msg", "获取商家用户详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "用户管理员[{0}]创建一个商家用户。", module = 7)
	@ResponseBody
	@RequestMapping(value = "/merchant/addMerchant", method = RequestMethod.POST)
	public String addMerchant(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			customerService.saveMerchant(map);
			returnMap.put("success", true);
			returnMap.put("msg", "创建商家用户信息成功!");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "创建商家用户信息失败!");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@Log(message = "用户管理员[{0}]对该商家用户进行编辑。", module = 7)
	@ResponseBody
	@RequestMapping(value = "/merchant/editMerchant", method = RequestMethod.POST)
	public String editMerchant(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		try {
			customerService.editMerchant(map);
			returnMap.put("msg", "编辑商家用户信息成功!");
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑商家用户信息失败!");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@Log(message = "用户管理员[{0}]将商家ID为[{1}]的商家进行删除。", module = 7)
	@ResponseBody
	@RequestMapping(value = "/merchant/delMerchant/{merchantId}", method = RequestMethod.POST)
	public String delMerchant(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String merchantId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			customerService.delMerchant(merchantId);
			returnMap.put("success", true);
			returnMap.put("msg", "该商家信息删除成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "该商家信息删除失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), merchantId }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/personal", method = RequestMethod.GET)
	public String release(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("userTypeMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.UserStatus, shiroUser.getLanguage()));

		model.addAttribute("sexTypeMap", DictionaryUtil.getStatueMap(DictionaryUtil.Sex, shiroUser.getLanguage()));

		model.addAttribute("channelTypeMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.RegistrationChannel, shiroUser.getLanguage()));

		model.addAttribute("subTypeMap", DictionaryUtil.getStatueMap(DictionaryUtil.YesNo, shiroUser.getLanguage()));
		return "fxl/customer/personal";
	}

	/**
	 * 获取客户列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/personal/getPersonalList", method = RequestMethod.POST)
	public String getPersonalList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		try {
			customerService.getPersonalList(map, page);
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

	@Log(message = "用户管理员[{0}]将将ID为[{1}]的商家用户进行启用。", module = 7)
	@ResponseBody
	@RequestMapping(value = "/personal/doEnable/{customerId}", method = RequestMethod.POST)
	public String doEnable(HttpServletRequest request, HttpServletResponse response, @PathVariable String customerId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			customerService.updateStatus(customerId, 1);
			returnMap.put("success", true);
			returnMap.put("msg", "启用商家用户成功!");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "启用商家用户失败!");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), customerId }));
		return mapper.toJson(returnMap);
	}

	@Log(message = "用户管理员[{0}]将将ID为[{1}]的商家用户进行停用。", module = 7)
	@ResponseBody
	@RequestMapping(value = "/personal/doDisable/{customerId}", method = RequestMethod.POST)
	public String doDisable(HttpServletRequest request, HttpServletResponse response, @PathVariable String customerId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			customerService.updateStatus(customerId, 10);
			returnMap.put("success", true);
			returnMap.put("msg", "停用商家用户成功!");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "停用商家用户失败!");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), customerId }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/customerTags", method = RequestMethod.GET)
	public String merchantTags(HttpServletRequest request, Model model) {

		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();

		model.addAttribute("customerTagsMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.PushUserTag, shiroUser.getLanguage()));

		return "fxl/customer/customertags";
	}

	/**
	 * 获取客户列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/userTags/getCustomerTagsList", method = RequestMethod.POST)
	public String getCustomerTagsList(HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		try {
			customerService.getCustomerTagsList(map, page);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}

		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	/**
	 * 根据项目ID返回用户标签详细信息
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/userTags/getCustomerTags/{customerTagsId}", method = RequestMethod.POST)
	public String getCustomerTags(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String customerTagsId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TCustomerTag tcustomerTag = customerService.getCustomerTags(customerTagsId);

			returnMap.put("fcustomerId", tcustomerTag.getFcustomerId());
			returnMap.put("ftag", tcustomerTag.getFtag());
			returnMap.put("foperator", shiroUser.getName());
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取该用户标签详细信息失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "用户管理员[{0}]对用户标签信息进行编辑。", module = 7)
	@ResponseBody
	@RequestMapping(value = "/userTags/editCustomerTags", method = RequestMethod.POST)
	public String editCustomerTags(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			customerService.editCustomerTags(map);
			returnMap.put("success", true);
			returnMap.put("msg", "编辑用户标签信息成功!");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑用户标签信息失败!");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/comment", method = RequestMethod.GET)
	public String comment(HttpServletRequest request, HttpServletResponse response) {
		return "fxl/customer/comment";
	}

	/**
	 * 评论列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getCustomerCommentList", method = RequestMethod.POST)
	public String getCustomerCommentList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		try {
			customerService.getCommentList(page, map);
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

	@Log(message = "用户管理员[{0}]将评论ID为[{1}]的评论进行删除。", module = 7)
	@ResponseBody
	@RequestMapping(value = "/comment/delComment/{commentId}", method = RequestMethod.POST)
	public String delComment(HttpServletRequest request, HttpServletResponse response, @PathVariable String commentId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			customerService.delComment(commentId);
			returnMap.put("success", true);
			returnMap.put("msg", "该评论信息删除成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "该评论信息删除失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), commentId }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 用户导出订单excel操作时生成excel文件的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "用户管理员[{0}]导出了评论excel文件。", module = 7)
	@ResponseBody
	@RequestMapping(value = "/comment/createCommentExcel", method = RequestMethod.POST)
	public String createCommentExcel(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String excelFileName = Identities.uuid2();
			String datePath = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
			customerService.createCommentExcel(map, datePath, excelFileName);
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
			returnMap.put("msg", "生成活动评价Excel文件操作失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/comment/exportExcel/{datePath}/{excelFileName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public String exportExcel(HttpServletRequest request, HttpServletResponse response, @PathVariable String datePath,
			@PathVariable String excelFileName) {
		InputStream is = null;
		OutputStream os = null;

		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("excelCommentPath")).append("/").append(datePath).append("/")
				.append(excelFileName).append(".xlsx");
		try {
			response.setHeader("Content-Disposition", "attachment; filename="
					+ URLEncoder.encode(new StringBuilder("用户活动评价表").append(".xlsx").toString(), "UTF-8"));
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

	@Log(message = "用户管理员[{0}]对该评论ID为[{1}]的评论进行排序编辑。", module = 7)
	@ResponseBody
	@RequestMapping(value = "/comment/editComment", method = RequestMethod.POST)
	public String editComment(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			customerService.editComment(map);
			returnMap.put("msg", "编辑评论排序信息成功!");
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑商家用户信息失败!");
		}
		LogUitls.putArgs(
				LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), map.get("id").toString() }));
		return mapper.toJson(returnMap);
	}

	// @Log(message = "用户管理员[{0}]导入用户标签。", module = 7)
	@ResponseBody
	@RequestMapping(value = "/merchant/addUserTagExcel", method = RequestMethod.POST)
	public String addUserTagExcel(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "file") MultipartFile file) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			ImageUploadDTO imageUploadDTO = new ImageUploadDTO();
			imageUploadDTO.setFile(file);
			customerService.addUserTagExcel(imageUploadDTO);

			returnMap.put("success", true);
			returnMap.put("msg", "导入用户标签信息成功!");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "导入用户标签信息失败!");
		}
		// LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[]
		// { shiroUser.getName()}));
		return mapper.toJson(returnMap);
	}

	@Log(message = "用户管理员[{0}]对该评论ID为[{1}]的评论进行审核。", module = 7)
	@ResponseBody
	@RequestMapping(value = "/comment/passedComment/{cId}/{status}", method = RequestMethod.POST)
	public String passedComment(HttpServletRequest request, Model model, @PathVariable String cId,
			@PathVariable Integer status) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// // 将request对象中的请求URL中的参数都放在Map中
		// Map<String, Object> map = Servlets.getParametersStartingWith(request,
		// null);
		try {
			customerService.passedComment(cId, status);
			returnMap.put("msg", "审核操作成功!");
			returnMap.put("success", true);
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "审核操作失败!");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), cId }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 根据项目ID返回项目详细信息
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/comment/getComment/{consultId}", method = RequestMethod.POST)
	public String getConsult(HttpServletRequest request, HttpServletResponse response, @PathVariable String consultId) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TComment tComment = commentDAO.findOne(consultId);
			returnMap.put("eventInfo", goodsService.getEventInfo(tComment.getFobjectId()));
			returnMap.put("fcustomerName", tComment.getFcustomerName());
			returnMap.put("fcontent", tComment.getFcontent());
			returnMap.put("freply", tComment.getFreply());
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取咨询详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/comment/reply", method = RequestMethod.POST)
	public String replyConsult(HttpServletRequest request, HttpServletResponse response, @RequestParam String id) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			operatingService.saveComment(map);
			returnMap.put("success", true);
			returnMap.put("msg", "回复评论操作成功!");
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "回复评论操作失败!");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 用户导出用户excel操作时生成excel文件的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/createCustomerExcel", method = RequestMethod.POST)
	public String createCustomerExcel(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		String sessionid = request.getSession().getId();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String excelFileName = Identities.uuid2();
			String datePath = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
			customerService.createCustomerExcel(map, datePath, excelFileName, sessionid);
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
			returnMap.put("msg", "导出用户excel文件操作失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/customerExcel/{datePath}/{excelFileName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public String customerExcel(HttpServletRequest request, HttpServletResponse response, @PathVariable String datePath,
			@PathVariable String excelFileName) {
		InputStream is = null;
		OutputStream os = null;

		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("customerExcel")).append("/").append(datePath).append("/")
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

}