package com.czyh.czyhweb.web.system;

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

import com.czyh.czyhweb.entity.TArticle;
import com.czyh.czyhweb.entity.TConsult;
import com.czyh.czyhweb.entity.TImage;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.log.Log;
import com.czyh.czyhweb.log.LogMessageObject;
import com.czyh.czyhweb.log.impl.LogUitls;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.FxlService;
import com.czyh.czyhweb.service.customer.CustomerService;
import com.czyh.czyhweb.service.goods.GoodsService;
import com.czyh.czyhweb.service.system.OperatingService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.PropertiesUtil;

/**
 * 无线模块操作类
 * 
 * @author jinshengzhi
 * 
 */
@Controller
@RequestMapping("/fxl/operating")
public class OperatingController {

	private static Logger logger = LoggerFactory.getLogger(OperatingController.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private FxlService fxlService;

	@Autowired
	private GoodsService goodsService;

	@Autowired
	private OperatingService operatingService;

	@Autowired
	private CustomerService customerService;

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

	@RequestMapping(value = "/article", method = RequestMethod.GET)
	public String article(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("cityMap", DictionaryUtil.getStatueMap(DictionaryUtil.City, shiroUser.getLanguage()));
		model.addAttribute("regionList", customerService.getRegionList(1));
		model.addAttribute("artTypeMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.ArticleType, shiroUser.getLanguage()));
		return "fxl/operating/article";
	}

	/**
	 * 获取发现文章列表的方法
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
			returnMap.put("fartType", tArticle.getFartType());
			returnMap.put("fartCity", tArticle.getFartCity());
			Long imageId = tArticle.getFimage();
			returnMap.put("fimage", imageId);
			if (imageId != null) {
				TImage tImage = fxlService.getImage(imageId);
				returnMap.put("imageWidth", tImage.getImageWidth());
				returnMap.put("imageHeight", tImage.getImageHeight());
				returnMap.put("fimagePath", fxlService.getImageUrl(imageId.toString(), false));
			}

			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取文章详细信息时失败！");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "运营小编[{0}]创建了ID为[{1}]的文章。", module = 8)
	@ResponseBody
	@RequestMapping(value = "/article/addArticle", method = RequestMethod.POST)
	public String addArticle(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			String articleId = operatingService.saveArticle(map);
			returnMap.put("success", true);
			returnMap.put("msg", "创建文章信息成功!");
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), articleId }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "创建文章信息失败!");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "运营小编[{0}]修改了ID为[{1}]的文章。", module = 8)
	@ResponseBody
	@RequestMapping(value = "/article/editArticle", method = RequestMethod.POST)
	public String editArticle(HttpServletRequest request, HttpServletResponse response, @RequestParam String id) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			operatingService.editArticle(map);
			returnMap.put("msg", "编辑文章信息成功!");
			returnMap.put("success", true);
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑文章信息失败!");
		}
		return mapper.toJson(returnMap);
	}

	@Log(message = "运营小编[{0}]删除了ID为[{1}]的文章。", module = 8)
	@ResponseBody
	@RequestMapping(value = "/article/delArticle/{articleId}", method = RequestMethod.POST)
	public String delArticle(HttpServletRequest request, HttpServletResponse response, @PathVariable String articleId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			operatingService.delArticle(articleId);
			returnMap.put("success", true);
			returnMap.put("msg", "该文章信息删除成功！");
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), articleId }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "该文章信息删除失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 文章即刻上架
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "运营小编[{0}]上架了ID为[{1}]的文章。", module = 8)
	@ResponseBody
	@RequestMapping(value = "/article/onSaleArticle/{articleId}", method = RequestMethod.POST)
	public String onSaleArticle(HttpServletRequest request, Model model, @PathVariable String articleId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			operatingService.onSaleArticle(articleId);
			returnMap.put("success", true);
			returnMap.put("msg", "文章即刻上架操作成功！");
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), articleId }));
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
	@Log(message = "运营小编[{0}]下架了ID为[{1}]的文章。", module = 8)
	@ResponseBody
	@RequestMapping(value = "/article/offSaleArticle/{articleId}", method = RequestMethod.POST)
	public String offSaleArticle(HttpServletRequest request, Model model, @PathVariable String articleId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			operatingService.offSaleArticle(articleId);
			returnMap.put("success", true);
			returnMap.put("msg", "文章即刻下架操作成功！");
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), articleId }));
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
	 * 获取回复咨询信息
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

	@Log(message = "运营小编[{0}]回复了ID为[{1}]的用户咨询。", module = 8)
	@ResponseBody
	@RequestMapping(value = "/consult/reply", method = RequestMethod.POST)
	public String replyConsult(HttpServletRequest request, HttpServletResponse response, @RequestParam String id) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			operatingService.saveConsult(map);
			returnMap.put("success", true);
			returnMap.put("msg", "回复咨询操作成功!");
			LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), id }));
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "回复咨询操作失败!");
		}
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/consult/replyPhone/{flag}", method = RequestMethod.GET)
	public String replyPhone(HttpServletRequest request, Model model, @PathVariable Integer flag) {

		if (flag == 10) {
			return "fxl/mobile/consultReplyNo";
		} else {
			return "fxl/mobile/consultReplyYes";
		}
	}

	/**
	 * 用户导出订单excel操作时生成excel文件的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "运营小编[{0}]导出了咨询excel文件。", module = 8)
	@ResponseBody
	@RequestMapping(value = "/consult/createConsultExcel", method = RequestMethod.POST)
	public String createConsultExcel(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String excelFileName = Identities.uuid2();
			String datePath = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
			operatingService.createConsultExcel(map, datePath, excelFileName);
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
			returnMap.put("msg", "生成活动咨询Excel文件操作失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/consult/exportExcel/{datePath}/{excelFileName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public String exportExcel(HttpServletRequest request, HttpServletResponse response, @PathVariable String datePath,
			@PathVariable String excelFileName) {
		InputStream is = null;
		OutputStream os = null;

		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("excelConsultPath")).append("/").append(datePath).append("/")
				.append(excelFileName).append(".xlsx");
		try {
			response.setHeader("Content-Disposition", "attachment; filename="
					+ URLEncoder.encode(new StringBuilder("用户活动咨询表").append(".xlsx").toString(), "UTF-8"));
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

	@ResponseBody
	@RequestMapping(value = "/consult/delConsult/{consultId}", method = RequestMethod.POST)
	public String delCoupon(HttpServletRequest request, HttpServletResponse response, @PathVariable String consultId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			operatingService.delConsult(consultId);
			returnMap.put("success", true);
			returnMap.put("msg", "活动咨询信息删除成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "活动咨询信息删除失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/consult/restoreConsult/{consultId}", method = RequestMethod.POST)
	public String restoreConsult(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String consultId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			operatingService.restoreConsult(consultId);
			returnMap.put("success", true);
			returnMap.put("msg", "活动咨询信息还原成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "活动咨询信息还原失败！");
		}
		return mapper.toJson(returnMap);
	}

}