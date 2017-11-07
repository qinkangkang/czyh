package com.czyh.czyhweb.web.report;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.web.Servlets;

import com.czyh.czyhweb.service.customer.CustomerService;
import com.czyh.czyhweb.util.CommonPage;

/**
 * 
 * 
 * *************************************************
 * 文件名：RewardChartsController.java 包名：com.czyh.czyhweb.web 时间：2016年11月16日
 * 模块名：统计报表----奖励金排行榜 作者： zyp 简要描述:奖励金查询
 * 
 * @version :1.0 变更历史: -------------------------------------------------- 序号 变更人
 *          时间 变更原因 1 2 **************************************************
 */
@Controller
@RequestMapping("/fxl/report/rewardCharts")
public class RewardChartsController {

	private static Logger logger = LoggerFactory.getLogger(RewardChartsController.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private CustomerService customerService;

	/**
	 * 
	 * @Title: index
	 * @Description: 跳转到排行榜页面,没有加载数据
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 * @author zyp(添加方法的人)
	 * @Date 2016年11月17日 上午10:26:34
	 */
	@RequestMapping(value = "/index", method = { RequestMethod.GET, RequestMethod.POST })
	public String index(HttpServletRequest request, HttpServletResponse response, Model model) {
		return "fxl/report/rewardCharts";
	}

	/**
	 * 
	 * @Title: getRewardChartsList
	 * @Description: 异步请求加载奖金排行榜的数据
	 * @param request
	 * @param response
	 * @return 分页的排行榜列表
	 * @author zyp(添加方法的人)
	 * @Date 2016年11月17日 上午10:27:12
	 */
	@ResponseBody
	@RequestMapping(value = "/list", method = { RequestMethod.GET, RequestMethod.POST })
	public String getRewardChartsList(HttpServletRequest request, HttpServletResponse response) {
		logger.warn("奖金排行榜列表页面---begin");
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		// 将grid中的页大小、起始记录数赋值到CommonPage对象中
		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));
		customerService.getRewardChartsOrderList(map, page);
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());
		logger.warn("返回的数据为:" + mapper.toJson(returnMap));
		logger.warn("奖金排行榜列表页面---end");
		return mapper.toJson(returnMap);
	}

}
