package com.czyh.czyhweb.web.order;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
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

import com.czyh.czyhweb.dao.CustomerBargainingDAO;
import com.czyh.czyhweb.dao.EventBargainingDAO;
import com.czyh.czyhweb.dao.SponsorDAO;
import com.czyh.czyhweb.dto.OrderRecipientDTO;
import com.czyh.czyhweb.dto.ResponseDTO;
import com.czyh.czyhweb.entity.TOrder;
import com.czyh.czyhweb.entity.TSponsor;
import com.czyh.czyhweb.entity.TSponsorStatement;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.log.Log;
import com.czyh.czyhweb.log.LogMessageObject;
import com.czyh.czyhweb.log.impl.LogUitls;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.customer.CustomerService;
import com.czyh.czyhweb.service.customer.UserService;
import com.czyh.czyhweb.service.finance.SettlementService;
import com.czyh.czyhweb.service.order.OrderService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.czyh.czyhweb.util.PropertiesUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 活动订单处理类
 * 
 * @author qkk
 * 
 */
@Controller
@RequestMapping("/fxl/order")
public class OrderController {

	private static Logger logger = LoggerFactory.getLogger(OrderController.class);

	private static JsonMapper mapper = new JsonMapper(Include.ALWAYS);

	@Autowired
	private UserService userService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private SettlementService settlementService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private SponsorDAO sponsorDAO;

	@Autowired
	private CustomerBargainingDAO customerBargainingDAO;

	@Autowired
	private EventBargainingDAO eventBargainingDAO;

	@RequestMapping(value = "/view", method = RequestMethod.GET)
	public String view(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("sponsorMap", customerService.getMerchantMapList());
		model.addAttribute("bdList", userService.getUserListByCategoryId(2));
		model.addAttribute("sourceMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.OrderSource, shiroUser.getLanguage()));
		model.addAttribute("ExpressCodeMap",
				DictionaryUtil.getStatueMap(DictionaryUtil.ExpressCode, shiroUser.getLanguage()));
		return "fxl/order/orderManage";
	}

	/**
	 * 获取订单列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getOrderList", method = { RequestMethod.GET, RequestMethod.POST })
	public String getOrderList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));
		orderService.getOrderList(map, page);
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
	@RequestMapping(value = "/getOrder/{orderId}", method = RequestMethod.POST)
	public String getOrder(HttpServletRequest request, HttpServletResponse response, @PathVariable String orderId) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		try {
			TOrder tOrder = orderService.getOrder(orderId);
			returnMap.put("id", tOrder.getId());
			returnMap.put("forderNum", tOrder.getForderNum());
			returnMap.put("fprice", tOrder.getFprice() + "元");
			returnMap.put("fcount", tOrder.getFcount());
			returnMap.put("fpostage", (tOrder.getFpostage() != null ? tOrder.getFpostage() : BigDecimal.ZERO) + "元");
			returnMap.put("freceivableTotal", tOrder.getFreceivableTotal() + "元");
			returnMap.put("fchangeAmount",
					(tOrder.getFchangeAmount() != null ? tOrder.getFchangeAmount() : BigDecimal.ZERO) + "元");
			returnMap.put("fchangeAmountInstruction", tOrder.getFchangeAmountInstruction());
			returnMap.put("ftotal", tOrder.getFtotal() + "元");
			StringBuilder remark = new StringBuilder();
			StringBuilder addRemark = new StringBuilder();
			if (StringUtils.isNotBlank(tOrder.getFremark())) {
				if (tOrder.getFremark().contains("&")) {
					remark.append(tOrder.getFremark().split("&", -1)[0]);
					addRemark.append(tOrder.getFremark().split("&", -1)[1]);
				}
			}
			returnMap.put("fremark", remark.toString());
			returnMap.put("addRemark", addRemark.toString());
			returnMap.put("fcsRemark", tOrder.getFcsRemark());
			returnMap.put("forderType", DictionaryUtil.getString(DictionaryUtil.OrderType, tOrder.getForderType()));
			// returnMap.put("freturn", tOrder.getFreturn() == 0 ? "否" : "是");
			returnMap.put("fpayType", DictionaryUtil.getString(DictionaryUtil.PayType, tOrder.getFpayType()));
			// returnMap.put("flockFlag", tOrder.getFlockFlag() == 0 ? "否" :
			// "是");
			OrderRecipientDTO recipientDTO = null;
			if (StringUtils.isNotBlank(tOrder.getFrecipient())) {
				recipientDTO = mapper.fromJson(tOrder.getFrecipient(), OrderRecipientDTO.class);
			} else {
				recipientDTO = new OrderRecipientDTO();
			}
			returnMap.put("recipient", recipientDTO.getRecipient());
			returnMap.put("phone", recipientDTO.getPhone());
			returnMap.put("address", recipientDTO.getAddress());
			returnMap.put("insuredInfo", orderService.getInsuredInfo(recipientDTO));
			returnMap.put("fsponsorName", tOrder.getFsponsorName());
			returnMap.put("fchannel", DictionaryUtil.getString(DictionaryUtil.RegistrationChannel, tOrder.getFchannel(),
					shiroUser.getLanguage()));
			returnMap.put("fsponsorFullName", tOrder.getFsponsorFullName());
			returnMap.put("fsponsorNumber", tOrder.getFsponsorNumber());
			returnMap.put("fsponsorPhone", tOrder.getFsponsorPhone());

			returnMap.put("fcustomerName", tOrder.getFcustomerName());
			returnMap.put("fcustomerSex", tOrder.getFcustomerSex());
			returnMap.put("fcustomerPhone", tOrder.getFcustomerPhone());
			returnMap.put("fCreateTime", DateFormatUtils.format(tOrder.getFcreateTime(), "yyyy-MM-dd HH:mm"));

			returnMap.put("fsellModel", DictionaryUtil.getString(DictionaryUtil.SellModel, tOrder.getFsellModel(),
					shiroUser.getLanguage()));
			// returnMap.put("verification",
			// DictionaryUtil.getString(DictionaryUtil.VerificationType,
			// tOrder.getTEvent().getFverificationType(),
			// shiroUser.getLanguage()));
			returnMap.put("orderStatusList", orderService.getOrderStatusList(orderId));
			// returnMap.put("orderExtList",
			// orderService.getOrderExtList(orderId));
			if (tOrder.getFsource() != null && tOrder.getFsource().intValue() == 20) {
				returnMap.put("bargain", eventBargainingDAO
						.findOne(customerBargainingDAO.getByOrderId(tOrder.getId()).getFbargainingId()).getFtitle());
			}
			returnMap.put("orderGoodsList", orderService.getOrderGoods(tOrder.getId()));
			StringBuilder reward = new StringBuilder();
			// if (tOrder.getFcommentRewardAmount() != null) {
			// reward.append(tOrder.getFcommentRewardAmount()).append("元");
			// if (tOrder.getfCommentRewardBonus() != null) {
			// reward.append("／").append(tOrder.getfCommentRewardBonus()).append("积分");
			// }
			// } else {
			// if (tOrder.getfCommentRewardBonus() != null) {
			// reward.append(tOrder.getfCommentRewardBonus()).append("积分");
			// }
			// }
			// returnMap.put("reward", reward.toString());
			returnMap.put("orderReplay", tOrder.getFreply());
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取订单详情失败！");
		}
		return mapper.toJson(returnMap);
	}

	/**
	 * 客服取消订单的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "订单客服[{0}]将订单ID为[{1}]的订单设置成客服取消。", module = 6)
	@ResponseBody
	@RequestMapping(value = "/csCancel/{orderId}", method = RequestMethod.POST)
	public String csCancel(HttpServletRequest request, Model model, @PathVariable String orderId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String msg = orderService.csCancel(orderId);
			returnMap.put("success", true);
			returnMap.put("msg", msg);
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "该订单取消失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), orderId }));
		return mapper.toJson(returnMap);
	}

	@Log(message = "订单客服[{0}]将订单ID为[{1}]的订单重发。", module = 6)
	@ResponseBody
	@RequestMapping(value = "/resend/{orderId}", method = RequestMethod.POST)
	public String resend(HttpServletRequest request, Model model, @PathVariable String orderId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			boolean code = orderService.resend(orderId);
			if (code) {
				returnMap.put("success", true);
				returnMap.put("msg", "重发成功");
			} else {
				returnMap.put("success", false);
				returnMap.put("msg", "重发失败！");
			}
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "重发失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), orderId }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 客服回复订单的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "订单客服[{0}]将订单ID为[{1}]的订单设置成锁定状态。", module = 6)
	@ResponseBody
	@RequestMapping(value = "/csLock/{orderId}", method = RequestMethod.POST)
	public String csLock(HttpServletRequest request, Model model, @PathVariable String orderId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			orderService.csLock(orderId);
			returnMap.put("success", true);
			returnMap.put("msg", "该订单已经锁定。");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "该订单锁定失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), orderId }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 客服解锁订单的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "订单客服[{0}]将订单ID为[{1}]的订单设置成解锁状态。", module = 6)
	@ResponseBody
	@RequestMapping(value = "/csUnLock/{orderId}", method = RequestMethod.POST)
	public String csUnLock(HttpServletRequest request, Model model, @PathVariable String orderId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			orderService.csUnLock(orderId);
			returnMap.put("success", true);
			returnMap.put("msg", "该订单已经解锁。");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "该订单解锁失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), orderId }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 客服核销订单的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "订单客服[{0}]将订单ID为[{1}]的订单进行了客服核销。", module = 6)
	@ResponseBody
	@RequestMapping(value = "/csVerification/{orderId}", method = RequestMethod.POST)
	public String csVerification(HttpServletRequest request, Model model, @PathVariable String orderId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			orderService.csVerification(orderId, false);
			returnMap.put("success", true);
			returnMap.put("msg", "该订单核销操作成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "该订单核销操作失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), orderId }));
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/queryTicket/{orderId}", method = RequestMethod.POST)
	public String queryTicket(HttpServletRequest request, Model model, @PathVariable String orderId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			ResponseDTO responseDTO = orderService.queryTicket(orderId);
			if (responseDTO.isSuccess()) {
				returnMap.put("success", true);
				returnMap.put("data", responseDTO.getData().get("order_status"));
				returnMap.put("msg", "查询票务系统成功！");
			} else {
				returnMap.put("success", false);
				returnMap.put("msg", "查询票务系统失败！");
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "查询票务系统失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), orderId }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 客服核销订单并且选择微信push的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "订单客服[{0}]将订单ID为[{1}]的订单进行了客服核销。", module = 6)
	@ResponseBody
	@RequestMapping(value = "/csVerificationPush/{orderId}", method = RequestMethod.POST)
	public String csVerificationPush(HttpServletRequest request, Model model, @PathVariable String orderId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			orderService.csVerification(orderId, true);
			returnMap.put("success", true);
			returnMap.put("msg", "该订单核销操作成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "该订单核销操作失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), orderId }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 客服锁定的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "订单客服[{0}]将订单ID为[{1}]的订单添加了客服备注。", module = 6)
	@ResponseBody
	@RequestMapping(value = "/saveCsRemark", method = RequestMethod.POST)
	public String saveCsRemark(HttpServletRequest request, Model model, @RequestParam String id,
			@RequestParam String csRemark) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String newCsRemark = orderService.saveCsRemark(id, csRemark);
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

	@RequestMapping(value = "/refund", method = RequestMethod.GET)
	public String toRefund(HttpServletRequest request, Model model) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("sponsorMap", customerService.getMerchantMapList());
		model.addAttribute("bdList", userService.getUserListByCategoryId(2));
		return "fxl/order/refundManage";
	}

	/**
	 * 执行微信退款
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "订单客服[{0}]将订单ID为[{1}]的订单进行了办理退款处理。", module = 6)
	@ResponseBody
	@RequestMapping(value = "/refund/{orderId}", method = RequestMethod.POST)
	public String refund(HttpServletRequest request, Model model, @PathVariable String orderId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			Map<String, Object> refundMsg = orderService.refund(orderId);
			returnMap.put("success", true);
			if (refundMsg.get("url") != null && StringUtils.isNotBlank(refundMsg.get("url").toString())) {
				returnMap.put("msg", "订单退款操作成功！订单支付款项将原路返还给用户。");
				returnMap.put("url", refundMsg.get("url").toString());
			} else {
				returnMap.put("msg", "订单退款操作成功！订单支付款项将原路返还给用户。");
			}
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "订单退款操作失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), orderId }));
		return mapper.toJson(returnMap);
	}
	
	
	/**
	 * 执行微信退款
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "订单客服[{0}]将订单ID为[{1}]的订单进行了办理退款处理。", module = 6)
	@ResponseBody
	@RequestMapping(value = "/refundAudit/{orderId}", method = RequestMethod.POST)
	public String refundAudit(HttpServletRequest request, Model model, @PathVariable String orderId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			Map<String, Object> refundMsg = orderService.refundAudit(orderId);
			returnMap.put("success", true);
			if (refundMsg.get("url") != null && StringUtils.isNotBlank(refundMsg.get("url").toString())) {
				returnMap.put("msg", "订单退款审核操作成功！");
				returnMap.put("url", refundMsg.get("url").toString());
			} else {
				returnMap.put("msg", "订单退款审核操作成功！");
			}
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "订单退款操作失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), orderId }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 用户导出订单excel操作时生成excel文件的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "订单客服[{0}]导出了订单excel文件。", module = 6)
	@ResponseBody
	@RequestMapping(value = "/createOrderExcel", method = RequestMethod.POST)
	public String createOrderExcel(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String excelFileName = Identities.uuid2();
			String datePath = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
			orderService.createOrderExcel(map, datePath, excelFileName);
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
			returnMap.put("msg", "订单导出Excel操作失败！");
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
				.append(PropertiesUtil.getProperty("excelOrderPath")).append("/").append(datePath).append("/")
				.append(excelFileName).append(".xlsx");
		try {
			response.setHeader("Content-Disposition", "attachment; filename="
					+ URLEncoder.encode(new StringBuilder("用户订单表").append(".xlsx").toString(), "UTF-8"));
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

	@RequestMapping(value = "/settlement", method = RequestMethod.GET)
	public String toSettlement(HttpServletRequest request, Model model) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("sponsorMap", customerService.getMerchantMapList());
		return "fxl/order/settlementManage";
	}

	/**
	 * 获取商家结算单列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getSettlementList", method = RequestMethod.POST)
	public String getSettlementList(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));
		settlementService.getSettlementList(map, page);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/toSettlementDetail/{settlementId}", method = RequestMethod.GET)
	public String toSs(HttpServletRequest request, Model model, @PathVariable String settlementId) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		try {
			TSponsorStatement tSponsorStatement = settlementService.getSettlement(settlementId);
			model.addAttribute("statement", tSponsorStatement);
			// 操作人
			if (tSponsorStatement.getFoperator().longValue() == 0L) {
				model.addAttribute("operator", "系统自动生成");
			} else {
				model.addAttribute("operator", userService.get(tSponsorStatement.getFoperator()).getRealname());
			}

		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
		return "fxl/order/settlementDetail";
	}

	/**
	 * 获取商家结算单列表的方法
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getSettlementDetailList/{settlementId}", method = RequestMethod.POST)
	public String getSettlementDetailList(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String settlementId) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		map.put("statementId", settlementId);
		settlementService.getSettlementDetailList(map, page);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	/**
	 * 订单审核
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/orderAudit", method = RequestMethod.GET)
	public String orderAudit(HttpServletRequest request, Model model) {

		model.addAttribute("sponsorMap", customerService.getMerchantMapList());
		return "fxl/order/refundManageAudit";
	}

	/**
	 * 获取审核订单列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/getOrderListAudit", method = { RequestMethod.GET, RequestMethod.POST })
	public String getOrderListAudit(HttpServletRequest request, HttpServletResponse response) {

		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));
		orderService.getOrderListAudit(map, page);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		return mapper.toJson(returnMap);
	}

	/**
	 * 审核通过该条订单退款
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "系统管理员[{0}]将orderId为[{1}]的Push消息审核通过。", module = 6)
	@ResponseBody
	@RequestMapping(value = "/successAuditOrder/{orderId}", method = RequestMethod.POST)
	public String successPush(HttpServletRequest request, Model model, @PathVariable String orderId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {

			orderService.saveUpdateOrderAudit(orderId, 1);

			returnMap.put("success", true);
			returnMap.put("msg", "审核订单退款操作成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "审核订单退款操作失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), orderId }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 审核通过该条订单退款
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "系统管理员[{0}]将orderId为[{1}]的Push消息审核通过。", module = 6)
	@ResponseBody
	@RequestMapping(value = "/defeateAuditOrder/{orderId}", method = RequestMethod.POST)
	public String defeateAuditOrder(HttpServletRequest request, Model model, @PathVariable String orderId) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {

			orderService.saveUpdateOrderAudit(orderId, 2);

			returnMap.put("success", true);
			returnMap.put("msg", "审核订单退款操作成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "审核订单退款操作失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), orderId }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 移动端订单管理用
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/refundPhone", method = RequestMethod.GET)
	public String refundPhone(HttpServletRequest request, Model model) {

		model.addAttribute("sponsorMap", customerService.getMerchantMapList());
		return "fxl/mobile/refundManagePhone";
	}

	/**
	 * 移动端退款审核用
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/orderAuditPhone", method = RequestMethod.GET)
	public String orderAuditPhone(HttpServletRequest request, Model model) {

		model.addAttribute("sponsorMap", customerService.getMerchantMapList());
		return "fxl/mobile/refundManageAuditPhone";
	}

	/**
	 * 移动端查询订单用
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/orderViewPhone", method = RequestMethod.GET)
	public String orderViewPhone(HttpServletRequest request, Model model) {

		model.addAttribute("sponsorMap", customerService.getMerchantMapList());
		return "fxl/mobile/orderManagePhone";
	}

	/**
	 * 用户导出地推excel操作时生成excel文件的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/createStatementExcel", method = RequestMethod.POST)
	public String createStatementExcel(HttpServletRequest request, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		String sessionid = request.getSession().getId();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			String excelFileName = Identities.uuid2();
			String datePath = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
			settlementService.createStatementExcel(map, datePath, excelFileName, sessionid);
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

	@RequestMapping(value = "/exportStatementExcel/{datePath}/{excelFileName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public String exportEventExcel(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String datePath, @PathVariable String excelFileName) {
		InputStream is = null;
		OutputStream os = null;

		StringBuilder rootPath = new StringBuilder(Constant.RootPath)
				.append(PropertiesUtil.getProperty("excelStatementPath")).append("/").append(datePath).append("/")
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

	@RequestMapping(value = "/sponsorSellement/{sponsorId}", method = RequestMethod.GET)
	public String sponsorSellement(HttpServletRequest request, Model model, @PathVariable String sponsorId) {
		try {
			TSponsor tSponsor = sponsorDAO.findOne(sponsorId);
			model.addAttribute("tSponsor", tSponsor);
			model.addAttribute("bdName", userService.get(tSponsor.getFbdId()).getRealname());

		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
		}
		return "fxl/order/sponsorSellement";
	}

	/**
	 * 客服锁定订单的方法
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@Log(message = "订单客服[{0}]回复了订单ID为[{1}]的订单。", module = 6)
	@ResponseBody
	@RequestMapping(value = "/orderReplay/{orderId}/{replay}", method = RequestMethod.POST)
	public String orderReplay(HttpServletRequest request, Model model, @PathVariable String orderId,
			@PathVariable String replay) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			orderService.orderReplay(orderId, replay);
			returnMap.put("success", true);
			returnMap.put("msg", "该订单已回复。");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "该订单回复失败！");
		}
		LogUitls.putArgs(LogMessageObject.newWrite().setObjects(new Object[] { shiroUser.getName(), orderId }));
		return mapper.toJson(returnMap);
	}

	/**
	 * 填写物流信息
	 * 
	 * @param request
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/saveExpress", method = RequestMethod.POST)
	public String saveExpress(HttpServletRequest request, Model model, @RequestParam String orderId,
			@RequestParam Integer expressCode, @RequestParam String expressName, @RequestParam String expressFrom,
			@RequestParam String expressTo) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {
			orderService.saveExpress(orderId, expressCode, expressName, expressFrom, expressTo);
			returnMap.put("success", true);
			returnMap.put("msg", "物流信息保存成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "物流信息保存失败！");
		}
		return mapper.toJson(returnMap);
	}

}