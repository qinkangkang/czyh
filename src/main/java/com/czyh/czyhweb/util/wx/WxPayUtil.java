package com.czyh.czyhweb.util.wx;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springside.modules.utils.Exceptions;

import com.czyh.czyhweb.exception.ServiceException;
import com.github.cuter44.wxapppay.WxAppPayFactory;
import com.github.cuter44.wxh5pay.WxH5PayFactory;
import com.github.cuter44.wxh5pay.WxpayException;
import com.github.cuter44.wxh5pay.WxpayProtocolException;
import com.github.cuter44.wxh5pay.constants.TradeState;
import com.github.cuter44.wxh5pay.reqs.OrderQuery;
import com.github.cuter44.wxh5pay.reqs.Refund;
import com.github.cuter44.wxh5pay.resps.OrderQueryResponse;
import com.github.cuter44.wxh5pay.resps.RefundResponse;
import com.github.cuter44.wxmp.WxmpFactory;
import com.github.cuter44.wxmp.reqs.UserInfo;
import com.github.cuter44.wxmp.resps.UserInfoResponse;

public class WxPayUtil {

	private static final Logger logger = LoggerFactory.getLogger(WxPayUtil.class);

	/**
	 * 调用微信退款的工具类方法
	 * 
	 * 
	 * @return
	 * @throws ServiceException
	 */
	public static WxRefundResult wxRefund(String orderNum, String transactionId) throws ServiceException {
		WxRefundResult wxRefundResult = new WxRefundResult();

		try {
			if (StringUtils.isBlank(orderNum) && StringUtils.isBlank(transactionId)) {
				throw new ServiceException("调用微信退款时出错！传入的订单编号和微信订单编号都为空！");
			}
			WxH5PayFactory factory = WxH5PayFactory.getDefaultInstance();

			OrderQuery orderQuery = new OrderQuery(ObjectUtils.clone(factory.getConf()));
			// if (StringUtils.isNotBlank(transactionId)) {
			orderQuery.setTransactionId(transactionId);
			// } else {
			orderQuery.setOutTradeNo(orderNum);
			// }

			OrderQueryResponse orderQueryResponse = orderQuery.build().sign().execute();
			// Properties prop1 = orderQueryResponse.getProperties();
			// System.out.println(prop1);
			if (orderQueryResponse.getTradeState() != TradeState.SUCCESS) {
				wxRefundResult.setResponse(orderQueryResponse.getReturnMsg().getMessage());
				wxRefundResult.setCashRefundFee(orderQueryResponse.getCashFeeFen());
				wxRefundResult.setRefundFee(orderQueryResponse.getTotalFee());
				// wxRefundResult.setCouponRefundFee(
				// orderQueryResponse.getCouponFeeFen() != null ?
				// orderQueryResponse.getCouponFeeFen() : null);
				// wxRefundResult.setCouponRefundCount(
				// orderQueryResponse.getCouponCount() != null ?
				// orderQueryResponse.getCouponCount() : null);
				wxRefundResult.setRefundId(orderQueryResponse.getTransactionId());
				if (orderQueryResponse.getTradeState() == TradeState.REFUND) {
					wxRefundResult.setSuccess(true);
				} else {
					wxRefundResult.setSuccess(false);
				}
				return wxRefundResult;
			}

			Refund refund = new Refund(ObjectUtils.clone(factory.getConf()), orderQueryResponse);
			wxRefundResult.setRequest(factory.getConf().toString());

			RefundResponse refundResponse = refund.build().sign().execute();
			// Properties prop2 = refundResponse.getProperties();
			// System.out.println(prop2);
			wxRefundResult.setRefundId(refundResponse.getRefundId());
			wxRefundResult.setRefundFee(refundResponse.getRefundFee());
			wxRefundResult.setCashRefundFee(refundResponse.getCashRefundFee());
			wxRefundResult.setCouponRefundCount(refundResponse.getCouponRefundCount());
			wxRefundResult.setCouponRefundFee(refundResponse.getCouponRefundFee());
			wxRefundResult.setResponse(refundResponse.getProperty("xml"));

		} catch (WxpayException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("调用微信退款时出错！出错信息：" + e.getMessage());
		} catch (WxpayProtocolException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("调用微信退款时出错！出错信息：" + e.getMessage());
		} catch (UnsupportedEncodingException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("调用微信退款时出错！出错信息：" + e.getMessage());
		} catch (IOException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("调用微信退款时出错！出错信息：" + e.getMessage());
		}
		wxRefundResult.setSuccess(true);
		return wxRefundResult;
	}

	/**
	 * 调用微信退款的工具类方法
	 * 
	 * 
	 * @return
	 * @throws ServiceException
	 */
	public static WxRefundResult wxAppRefund(String orderNum, String transactionId) throws ServiceException {
		WxRefundResult wxRefundResult = new WxRefundResult();

		try {
			if (StringUtils.isBlank(orderNum) && StringUtils.isBlank(transactionId)) {
				throw new ServiceException("调用微信退款时出错！传入的订单编号和微信订单编号都为空！");
			}
			WxAppPayFactory factory = WxAppPayFactory.getDefaultInstance();

			OrderQuery orderQuery = new OrderQuery(ObjectUtils.clone(factory.getConf()));
			// if (StringUtils.isNotBlank(transactionId)) {
			orderQuery.setTransactionId(transactionId);
			// } else {
			orderQuery.setOutTradeNo(orderNum);
			// }

			OrderQueryResponse orderQueryResponse = orderQuery.build().sign().execute();
			if (orderQueryResponse.getTradeState() != TradeState.SUCCESS) {
				wxRefundResult.setResponse(orderQueryResponse.getReturnMsg().getMessage());
				wxRefundResult.setCashRefundFee(orderQueryResponse.getCashFeeFen());
				wxRefundResult.setRefundFee(orderQueryResponse.getTotalFee());
				// wxRefundResult.setCouponRefundFee(
				// orderQueryResponse.getCouponFeeFen() != null ?
				// orderQueryResponse.getCouponFeeFen() : null);
				// wxRefundResult.setCouponRefundCount(
				// orderQueryResponse.getCouponCount() != null ?
				// orderQueryResponse.getCouponCount() : null);
				wxRefundResult.setRefundId(orderQueryResponse.getTransactionId());
				wxRefundResult.setMsg(orderQueryResponse.getProperty("trade_state_desc"));
				if (orderQueryResponse.getTradeState() == TradeState.REFUND) {
					wxRefundResult.setSuccess(true);
				} else {
					wxRefundResult.setSuccess(false);
				}
				return wxRefundResult;
			}

			com.github.cuter44.wxapppay.reqs.Refund refund = new com.github.cuter44.wxapppay.reqs.Refund(
					ObjectUtils.clone(factory.getConf()), orderQueryResponse);
			wxRefundResult.setRequest(refund.toString());

			com.github.cuter44.wxapppay.resps.RefundResponse refundResponse = refund.build().sign().execute();
			// Properties prop2 = refundResponse.getProperties();
			// System.out.println(prop2);
			wxRefundResult.setRefundId(refundResponse.getRefundId());
			wxRefundResult.setRefundFee(refundResponse.getRefundFee());
			wxRefundResult.setCashRefundFee(refundResponse.getCashRefundFee());
			wxRefundResult.setCouponRefundCount(refundResponse.getCouponRefundCount());
			wxRefundResult.setCouponRefundFee(refundResponse.getCouponRefundFee());
			wxRefundResult.setResponse(refundResponse.getProperty("xml"));
		} catch (WxpayException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("调用微信退款时出错！出错信息：" + e.getMessage());
		} catch (WxpayProtocolException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("调用微信退款时出错！出错信息：" + e.getMessage());
		} catch (UnsupportedEncodingException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("调用微信退款时出错！出错信息：" + e.getMessage());
		} catch (IOException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("调用微信退款时出错！出错信息：" + e.getMessage());
		}
		wxRefundResult.setSuccess(true);
		return wxRefundResult;
	}

	public static String getUnionId(String openid) {
		WxmpFactory factory = WxmpFactory.getDefaultInstance();
		UserInfo userInfo = (UserInfo) factory.instantiateWithToken(UserInfo.class);
		String unionId = null;
		try {
			UserInfoResponse userInfoResponse = userInfo.setOpenid(openid).build().execute();
			unionId = userInfoResponse.getProperty("unionid");
			System.out.println(userInfoResponse.getSubscribe() + "    " + unionId);
		} catch (IOException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			logger.error("获取unionId时出错！", e);
		}
		return unionId;
	}
}