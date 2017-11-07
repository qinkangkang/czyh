package com.czyh.czyhweb.web.finance;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;
import org.springside.modules.utils.Identities;
import org.springside.modules.web.Servlets;

import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.log.Log;
import com.czyh.czyhweb.log.LogMessageObject;
import com.czyh.czyhweb.log.impl.LogUitls;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.FxlService;
import com.czyh.czyhweb.service.customer.CustomerService;
import com.czyh.czyhweb.service.customer.UserService;
import com.czyh.czyhweb.service.finance.FinanceService;
import com.czyh.czyhweb.service.goods.GoodsService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.PropertiesUtil;

import net.sf.ehcache.CacheManager;

/**
 * 财务管理模块的操作类
 * 
 * @author hxting
 *
 */
@Controller
@RequestMapping("/fxl/finance")
public class FinanceController {

	private static Logger logger = LoggerFactory.getLogger(FinanceController.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();
	
	@Autowired
	private FinanceService financeService;

	@Autowired
	private CustomerService customerService;


	@RequestMapping(value = "/reconciliationView", method = RequestMethod.GET)
	public String reconciliationView(HttpServletRequest request, Model model) {		
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("sponsorMap", customerService.getMerchantMapList());	
		model.addAttribute("sourceMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.OrderSource, shiroUser.getLanguage()));
		return "fxl/finance/reconciliationView";
	}
	
	@ResponseBody
	@RequestMapping(value = "/getReconciliationList", method = RequestMethod.POST)
	public String getReconciliationList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));
		financeService.getReconciliationList(map, page);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}
	
	
	/**
	 * 用户导出财务对账excel操作时生成excel文件的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/createReconciliationExcel", method = RequestMethod.POST)
	public String createReconciliationExcel(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String excelFileName = Identities.uuid2();
			String datePath = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
			financeService.createReconciliationExcel(map, datePath, excelFileName);
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
			returnMap.put("msg", "导出财务对账excel文件操作失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}
	
	@RequestMapping(value = "/exportExcel/{datePath}/{excelFileName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public String exportExcel(HttpServletRequest request, HttpServletResponse response, @PathVariable String datePath,
			@PathVariable String excelFileName) {
		InputStream is = null;
		OutputStream os = null;

		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("excelreconciliationPath")).append("/").append(datePath).append("/")
				.append(excelFileName).append(".xlsx");
		try {
			response.setHeader("Content-Disposition", "attachment; filename="
					+ URLEncoder.encode(new StringBuilder("商户对账列表").append(".xlsx").toString(), "UTF-8"));
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