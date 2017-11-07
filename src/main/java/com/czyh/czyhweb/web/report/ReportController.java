package com.czyh.czyhweb.web.report;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;
import org.springside.modules.utils.Identities;
import org.springside.modules.web.Servlets;

import com.czyh.czyhweb.dto.CountOrderDTO;
import com.czyh.czyhweb.dto.EventNumberDTO;
import com.czyh.czyhweb.entity.TSceneUserMap;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.log.Log;
import com.czyh.czyhweb.log.LogMessageObject;
import com.czyh.czyhweb.log.impl.LogUitls;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.customer.CustomerService;
import com.czyh.czyhweb.service.report.ReportService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.PropertiesUtil;

/**
 * 报表处理类
 * 
 * @author qkk
 *
 */
@Controller
@RequestMapping("/fxl/report")
public class ReportController {

	private static Logger logger = LoggerFactory.getLogger(ReportController.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private ReportService reportService;
	
	@Autowired
	private CustomerService customerService;

	/**
	 * 跳转到财务对账报表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/sale", method = { RequestMethod.GET, RequestMethod.POST })
	public String sale(HttpServletRequest request, HttpServletResponse response, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("sourceMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.OrderSource, shiroUser.getLanguage()));
		model.addAttribute("sponsorMap", customerService.getMerchantMapList());	
		return "fxl/report/sale";
	}
	
	/**
	 * 财务对账列表
	 * @param request
	 * @param response
	 * @return
	 */
	
	@ResponseBody
	@RequestMapping(value = "/getFinanceReconList", method = RequestMethod.POST)
	public String getFinanceReconList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));
		reportService.getFinanceReconList(map, page);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}
	
	/**
	 * 财务导出财务对账excel操作时生成excel文件的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/createFinanceReconExcel", method = RequestMethod.POST)
	public String createFinanceReconExcel(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String excelFileName = Identities.uuid2();
			String datePath = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
			reportService.createFinanceReconExcel(map, datePath, excelFileName);
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
	
	
	@RequestMapping(value = "/exportFinanceReconExcel/{datePath}/{excelFileName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public String exportFinanceReconExcel(HttpServletRequest request, HttpServletResponse response, @PathVariable String datePath,
			@PathVariable String excelFileName) {
		InputStream is = null;
		OutputStream os = null;
		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("excelfinanceReconPath")).append("/").append(datePath).append("/")
				.append(excelFileName).append(".xlsx");
		try {
			response.setHeader("Content-Disposition", "attachment; filename="
					+ URLEncoder.encode(new StringBuilder("财务对账列表").append(".xlsx").toString(), "UTF-8"));
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
	

	/**
	 * 订单统计
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/countOrder", method = { RequestMethod.GET, RequestMethod.POST })
	public String countOrder(HttpServletRequest request, HttpServletResponse response) {
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		List<CountOrderDTO> list = reportService.countOrder(map);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("list", list);
		returnMap.put("success", true);
		return mapper.toJson(returnMap);
	}

	/**
	 * 跳转到用户报表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/customer", method = { RequestMethod.GET, RequestMethod.POST })
	public String reportCustomer(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("userTagMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.PushUserTag, shiroUser.getLanguage()));
		model.addAttribute("registrationChannelMap",
				DictionaryUtil.getOneNameCodeMap(DictionaryUtil.RegistrationChannel));
		return "fxl/report/customer";
	}

	/**
	 * 用户报表统计
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getReportCustomerList", method = { RequestMethod.GET, RequestMethod.POST })
	public String getReportCustomerList(HttpServletRequest request, HttpServletResponse response) {
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));
		reportService.getReportCustomerList(map, page);
		// 操作信息的map，将map通过json方式返回给页面
		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());

		return mapper.toJson(returnMap);
	}

	/**
	 * 活动数量统计
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/eventNumber", method = { RequestMethod.GET, RequestMethod.POST })
	public String eventNumber(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return "fxl/report/eventNumber";
	}

	/**
	 * 获取活动数量统计
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getEventNumber", method = { RequestMethod.GET, RequestMethod.POST })
	public String getEventNumber(HttpServletRequest request, HttpServletResponse response) {
		// 将request对象中的请求URL中的参数都放在Map中
		// Map<String, Object> map = Servlets.getParametersStartingWith(request,
		// null);
		List<EventNumberDTO> list = reportService.getEventNumber();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("list", list);
		returnMap.put("success", true);
		return mapper.toJson(returnMap);
	}

	/**
	 * 活动上下架状态页面
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/eventStatus", method = { RequestMethod.GET, RequestMethod.POST })
	public String eventStatus(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		return "fxl/report/eventStatus";
	}

	/**
	 * 活动上下架数据集合
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getReportEventStatusList", method = { RequestMethod.GET, RequestMethod.POST })
	public String getReportEventStatusList(HttpServletRequest request, HttpServletResponse response) {
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		List<Map<String, Object>> list = reportService.getReportEventStatusList(map);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", list.size());
		returnMap.put("recordsFiltered", list.size());
		returnMap.put("data", list);
		return mapper.toJson(returnMap);
		// 将grid中的页大小、起始记录数赋值到CommonPage对象中

	}

	/**
	 * 地推报表
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/sceneUser", method = { RequestMethod.GET, RequestMethod.POST })
	public String sceneUser(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		model.addAttribute("sceneStrList", reportService.getSceneStr());
		return "fxl/report/sceneUser";
	}

	/**
	 * 地推列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getReportSceneUserList", method = RequestMethod.POST)
	public String getReportSceneUserList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		try {
			reportService.getReportSceneUserList(page, map);
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
	 * 用户导出地推excel操作时生成excel文件的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "地推人员[{0}]导出了地推excel文件。", module = 6)
	@ResponseBody
	@RequestMapping(value = "/createSceneExcel", method = RequestMethod.POST)
	public String createSceneExcel(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String excelFileName = Identities.uuid2();
			String datePath = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
			reportService.createSceneExcel(map, datePath, excelFileName);
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
			returnMap.put("msg", "导出地推excel文件操作失败！");
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
				.append(PropertiesUtil.getProperty("excelScenePath")).append("/").append(datePath).append("/")
				.append(excelFileName).append(".xlsx");
		try {
			response.setHeader("Content-Disposition", "attachment; filename="
					+ URLEncoder.encode(new StringBuilder("地推报表").append(".xlsx").toString(), "UTF-8"));
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

	/**
	 * 地推汇总报表
	 * 
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/sceneUserGather", method = { RequestMethod.GET, RequestMethod.POST })
	public String sceneUserGather(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("sceneStrList", reportService.getSceneStr());
		Map<Integer, String> sceneMap = DictionaryUtil.getStatueMap(DictionaryUtil.SceneMap, shiroUser.getLanguage());
		model.addAttribute("sceneMap", sceneMap);
		return "fxl/report/sceneUserGather";
	}

	/**
	 * 地推汇总列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getReportSceneUserGatherList", method = RequestMethod.POST)
	public String getReportSceneUserGatherList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		String sessionid = request.getSession().getId();
		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		try {
			reportService.getReportSceneUserGatherList(page, map, sessionid);
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
	 * 用户导出地推excel操作时生成excel文件的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "地推人员[{0}]导出了地推汇总excel文件。", module = 6)
	@ResponseBody
	@RequestMapping(value = "/createSceneGatherExcel", method = RequestMethod.POST)
	public String createSceneGatherExcel(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		String sessionid = request.getSession().getId();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String excelFileName = Identities.uuid2();
			String datePath = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
			reportService.createSceneGatherExcel(map, datePath, excelFileName, sessionid);
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
			returnMap.put("msg", "导出地推excel文件操作失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/exportGatherExcel/{datePath}/{excelFileName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public String exportGatherExcel(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String datePath, @PathVariable String excelFileName) {
		InputStream is = null;
		OutputStream os = null;

		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("excelSceneGatherPath")).append("/").append(datePath).append("/")
				.append(excelFileName).append(".xlsx");
		try {
			response.setHeader("Content-Disposition", "attachment; filename="
					+ URLEncoder.encode(new StringBuilder("地推汇总报表").append(".xlsx").toString(), "UTF-8"));
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

	@RequestMapping(value = "/sceneMap", method = RequestMethod.GET)
	public String sceneMap(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<Integer, String> sceneMap = DictionaryUtil.getStatueMap(DictionaryUtil.SceneMap, shiroUser.getLanguage());
		model.addAttribute("sceneMap", sceneMap);
		return "fxl/report/sceneMap";
	}

	/**
	 * 获取用户列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getSceneMapList", method = RequestMethod.POST)
	public String getSceneMapList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		reportService.getSceneMapList(map, page);

		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/addScene", method = RequestMethod.POST)
	public String addScene(HttpServletRequest request, HttpServletResponse response) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			if (map.containsKey("id") && StringUtils.isBlank(map.get("id").toString())
					&& reportService.findBySceneCode(map) > 0) {
				returnMap.put("success", false);
				returnMap.put("msg", "该地推码已存在映射关系！");
			} else {
				reportService.addScene(map);
				returnMap.put("success", true);
				returnMap.put("msg", "地推人员与地推码映射保存成功！");
			}
		} catch (ServiceException se) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(se)));
			returnMap.put("success", false);
			returnMap.put("msg", se.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "地推人员与地推码映射保存失败！");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/getSceneUserMap/{sceneUserMapId}", method = RequestMethod.POST)
	public String getEventBonus(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String sceneUserMapId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			TSceneUserMap tSceneUserMap = reportService.getTSceneUserMap(sceneUserMapId);

			returnMap.put("sceneCode", tSceneUserMap.getFsceneCode());
			returnMap.put("user", tSceneUserMap.getFuserId());
			returnMap.put("status", tSceneUserMap.getFstatus());
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取地推人员与地推码映射时失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 用户导出地推excel操作时生成excel文件的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "工作人员[{0}]导出了活动excel文件。", module = 6)
	@ResponseBody
	@RequestMapping(value = "/createEventExcel", method = RequestMethod.POST)
	public String createEventExcel(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		String sessionid = request.getSession().getId();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String excelFileName = Identities.uuid2();
			String datePath = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
			reportService.createEventExcel(map, datePath, excelFileName, sessionid);
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
			returnMap.put("msg", "导出地推excel文件操作失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName() }));
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/exportEventExcel/{datePath}/{excelFileName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public String exportEventExcel(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String datePath, @PathVariable String excelFileName) {
		InputStream is = null;
		OutputStream os = null;

		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("excelEventPath")).append("/").append(datePath).append("/")
				.append(excelFileName).append(".xlsx");
		try {
			response.setHeader("Content-Disposition", "attachment; filename="
					+ URLEncoder.encode(new StringBuilder("活动数据报表").append(".xlsx").toString(), "UTF-8"));
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
