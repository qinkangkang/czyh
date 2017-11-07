package com.czyh.czyhweb.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class CountOrderDTO implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String count;
	
	private BigDecimal total;
	
	private Integer orderStatus;
	
	private String title;
	
	private BigDecimal profit;
	
	private BigDecimal changeAmount;

	private void getValues(Integer status) {
		String title = "";
		if (status == 10) {
			title = "待付款";
		}else if (status == 20) {
			title = "待使用";
		} else if (status == 60) {
			title = "已核销";
		}else if (status == 70) {
			title = "已评价";
		} else if (status == 100) {
			title = "客户取消";
		} else if (status == 101) {
			title = "客服取消";
		} else if (status == 109) {
			title = "超时未支付";
		} else if (status == 110) {
			title = "退款申请中";
		} else if (status == 120) {
			title = "退款成功";
		} else {
			title = "合计";
		}
		this.title = title;
    }
	
	public String getTitle() {
		return this.title;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public Integer getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(Integer orderStatus) {
		this.orderStatus = orderStatus;
		this.getValues(this.orderStatus);
	}

	public BigDecimal getProfit() {
		return profit;
	}

	public void setProfit(BigDecimal profit) {
		this.profit = profit;
	}

	public BigDecimal getChangeAmount() {
		return changeAmount;
	}

	public void setChangeAmount(BigDecimal changeAmount) {
		this.changeAmount = changeAmount;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}
	
	
}
