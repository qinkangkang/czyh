package com.czyh.czyhweb.web.wechat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;
import org.springside.modules.web.Servlets;

import com.czyh.czyhweb.dao.WxMenuDAO;
import com.czyh.czyhweb.dao.WxMenuItemDAO;
import com.czyh.czyhweb.entity.TWxMenu;
import com.czyh.czyhweb.entity.TWxMenuItem;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.wechat.WxService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 微信处理service
 * 
 * @author jinshengzhi
 *
 */
@Controller
@RequestMapping("/fxl/wechat")
public class WechatController {

	private static Logger logger = LoggerFactory.getLogger(WechatController.class);

	private static JsonMapper mapper = new JsonMapper(Include.ALWAYS);

	@Autowired
	private WxMenuDAO wxMenuDAO;

	@Autowired
	private WxMenuItemDAO wxMenuItemDAO;

	@Autowired
	private WxService wxService;

	@RequestMapping(value = "/wechatMenu", method = { RequestMethod.GET, RequestMethod.POST })
	public String sale(HttpServletRequest request, HttpServletResponse response, Model model) {
		List<TWxMenu> list = wxMenuDAO.getMenuList();

		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("yesNoMap", DictionaryUtil.getStatueMap(DictionaryUtil.YesNo, shiroUser.getLanguage()));

		// 第一栏主菜单分布
		model.addAttribute("oneMenuName", list.get(0).getMenuName());
		model.addAttribute("oneMenuUrl", list.get(0).getUrl());
		model.addAttribute("oneMenuId", list.get(0).getId());

		// 第一栏主菜单分布
		List<TWxMenuItem> tWxMenuItemList1 = wxMenuItemDAO.getMenuList(list.get(0).getId());
		model.addAttribute("fiveAMenuItemName", tWxMenuItemList1.get(4).getItemName());
		model.addAttribute("fiveAMenuItemCount", tWxMenuItemList1.get(4).getContent());
		model.addAttribute("fiveAMenuItemId", tWxMenuItemList1.get(4).getId());
		model.addAttribute("fiveAMenuItemOrder", tWxMenuItemList1.get(4).getMenuOrder());

		model.addAttribute("fourAMenuItemName", tWxMenuItemList1.get(3).getItemName());
		model.addAttribute("fourAMenuItemCount", tWxMenuItemList1.get(3).getContent());
		model.addAttribute("fourAMenuItemId", tWxMenuItemList1.get(3).getId());
		model.addAttribute("fourAMenuItemOrder", tWxMenuItemList1.get(3).getMenuOrder());

		model.addAttribute("threeAMenuItemName", tWxMenuItemList1.get(2).getItemName());
		model.addAttribute("threeAMenuItemCount", tWxMenuItemList1.get(2).getContent());
		model.addAttribute("threeAMenuItemId", tWxMenuItemList1.get(2).getId());
		model.addAttribute("threeAMenuItemOrder", tWxMenuItemList1.get(2).getMenuOrder());

		model.addAttribute("towAMenuItemName", tWxMenuItemList1.get(1).getItemName());
		model.addAttribute("towAMenuItemCK", tWxMenuItemList1.get(1).getText());
		model.addAttribute("towAMenuItemId", tWxMenuItemList1.get(1).getId());
		model.addAttribute("towAMenuItemOrder", tWxMenuItemList1.get(1).getMenuOrder());

		model.addAttribute("oneAMenuItemName", tWxMenuItemList1.get(0).getItemName());
		model.addAttribute("oneAMenuItemCK", tWxMenuItemList1.get(0).getText());
		model.addAttribute("oneAMenuItemId", tWxMenuItemList1.get(0).getId());
		model.addAttribute("oneAMenuItemOrder", tWxMenuItemList1.get(0).getMenuOrder());

		// 第二栏主菜单分布
		model.addAttribute("towMenuName", list.get(1).getMenuName());
		model.addAttribute("towMenuUrl", list.get(1).getUrl());
		model.addAttribute("towMenuId", list.get(1).getId());

		// 第二栏子菜单分布
		List<TWxMenuItem> tWxMenuItemList2 = wxMenuItemDAO.getMenuList(list.get(1).getId());
		model.addAttribute("fiveBMenuItemName", tWxMenuItemList2.get(4).getItemName());
		model.addAttribute("fiveBMenuItemCount", tWxMenuItemList2.get(4).getContent());
		model.addAttribute("fiveBMenuItemId", tWxMenuItemList2.get(4).getId());
		model.addAttribute("fiveBMenuItemOrder", tWxMenuItemList2.get(4).getMenuOrder());

		model.addAttribute("fourBMenuItemName", tWxMenuItemList2.get(3).getItemName());
		model.addAttribute("fourBMenuItemCount", tWxMenuItemList2.get(3).getContent());
		model.addAttribute("fourBMenuItemId", tWxMenuItemList2.get(3).getId());
		model.addAttribute("fourBMenuItemOrder", tWxMenuItemList2.get(3).getMenuOrder());

		model.addAttribute("threeBMenuItemName", tWxMenuItemList2.get(2).getItemName());
		model.addAttribute("threeBMenuItemCount", tWxMenuItemList2.get(2).getContent());
		model.addAttribute("threeBMenuItemId", tWxMenuItemList2.get(2).getId());
		model.addAttribute("threeBMenuItemOrder", tWxMenuItemList2.get(2).getMenuOrder());

		model.addAttribute("towBMenuItemName", tWxMenuItemList2.get(1).getItemName());
		model.addAttribute("towBMenuItemCK", tWxMenuItemList2.get(1).getText());
		model.addAttribute("towBMenuItemId", tWxMenuItemList2.get(1).getId());
		model.addAttribute("towBMenuItemOrder", tWxMenuItemList2.get(1).getMenuOrder());

		model.addAttribute("oneBMenuItemName", tWxMenuItemList2.get(0).getItemName());
		model.addAttribute("oneBMenuItemCK", tWxMenuItemList2.get(0).getText());
		model.addAttribute("oneBMenuItemId", tWxMenuItemList2.get(0).getId());
		model.addAttribute("oneBMenuItemOrder", tWxMenuItemList2.get(0).getMenuOrder());

		// 第三栏主菜单分布
		model.addAttribute("threeMenuName", list.get(2).getMenuName());
		model.addAttribute("threeMenuUrl", list.get(2).getUrl());
		model.addAttribute("threeMenuId", list.get(2).getId());

		// 第三栏子菜单分布
		List<TWxMenuItem> tWxMenuItemList3 = wxMenuItemDAO.getMenuList(list.get(2).getId());
		model.addAttribute("fiveMenuItemName", tWxMenuItemList3.get(4).getItemName());
		model.addAttribute("fiveMenuItemContent", tWxMenuItemList3.get(4).getContent());
		model.addAttribute("fiveMenuItemId", tWxMenuItemList3.get(4).getId());
		model.addAttribute("fiveMenuItemOrder", tWxMenuItemList3.get(4).getMenuOrder());

		model.addAttribute("fourHMenuItemName", tWxMenuItemList3.get(3).getItemName());
		model.addAttribute("fourHMenuItemCount", tWxMenuItemList3.get(3).getContent());
		model.addAttribute("fourHMenuItemId", tWxMenuItemList3.get(3).getId());
		model.addAttribute("fourHMenuItemOrder", tWxMenuItemList3.get(3).getMenuOrder());

		model.addAttribute("threeMenuItemName", tWxMenuItemList3.get(2).getItemName());
		model.addAttribute("threeMenuItemCount", tWxMenuItemList3.get(2).getContent());
		model.addAttribute("threeMenuItemId", tWxMenuItemList3.get(2).getId());
		model.addAttribute("threeMenuItemOrder", tWxMenuItemList3.get(2).getMenuOrder());

		model.addAttribute("towMenuItemName", tWxMenuItemList3.get(1).getItemName());
		model.addAttribute("towMenuItemCK", tWxMenuItemList3.get(1).getText());
		model.addAttribute("towMenuItemId", tWxMenuItemList3.get(1).getId());
		model.addAttribute("towMenuItemOrder", tWxMenuItemList3.get(1).getMenuOrder());

		model.addAttribute("oneMenuItemName", tWxMenuItemList3.get(0).getItemName());
		model.addAttribute("oneMenuItemCK", tWxMenuItemList3.get(0).getText());
		model.addAttribute("oneMenuItemId", tWxMenuItemList3.get(0).getId());
		model.addAttribute("oneMenuItemOrder", tWxMenuItemList3.get(0).getMenuOrder());

		return "fxl/wechat/menuCreate";
	}

	@ResponseBody
	@RequestMapping(value = "/wechatMenu/saveUpdateMenu", method = RequestMethod.POST)
	public String createWeChatMenu(HttpServletRequest request, HttpServletResponse response) {
		// 操作信息的map，将map通过json方式返回给页面
		Map<String, Object> returnMap = new HashMap<String, Object>();
		// 将request对象中的请求URL中的参数都放在Map中
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {
			wxService.saveUpdateMenu(map);
			returnMap.put("success", true);
			returnMap.put("msg", "微信创建菜单成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "微信创建菜单失败!");
		}
		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/wechatPush", method = { RequestMethod.GET, RequestMethod.POST })
	public String wechatPush(HttpServletRequest request, HttpServletResponse response, Model model) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();

		// model.addAttribute("wechatPushMap",
		// DictionaryUtil.getStatueMap(DictionaryUtil.WechatPushStatus,
		// shiroUser.getLanguage()));

		return "fxl/wechat/wechatPush";
	}

	@ResponseBody
	@RequestMapping(value = "/getWechatPushList", method = RequestMethod.POST)
	public String getWechatPushList(HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		CommonPage page = new CommonPage();
		page.setOffset(Integer.valueOf(map.get("start").toString()));
		page.setPageSize(Integer.valueOf(map.get("length").toString()));

		wxService.getWechatPushList(map, page);

		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("draw", map.get("draw").toString());
		returnMap.put("recordsTotal", page.getTotalCount());
		returnMap.put("recordsFiltered", page.getTotalCount());
		returnMap.put("data", page.getResult());

		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/addWechatPush", method = RequestMethod.POST)
	public String addWechatPush(HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);
		try {

			wxService.addWechatPush(map);
			returnMap.put("success", true);
			returnMap.put("msg", "微信推送信息保存成功！");
		} catch (ServiceException se) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(se)));
			returnMap.put("success", false);
			returnMap.put("msg", se.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "微信推送信息保存失败！");
		}

		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/delWechatPush/{wechatPushId}", method = RequestMethod.POST)
	public String delWechatPush(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String wechatPushId) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {

			wxService.delWechatPush(wechatPushId);
			returnMap.put("success", true);
			returnMap.put("msg", "微信推送信息删除成功！");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "微信推送信息删除失败！");
		}

		return mapper.toJson(returnMap);
	}

	@RequestMapping(value = "/editWechatPush", method = RequestMethod.POST)
	public String editWechatPush(HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		Map<String, Object> map = Servlets.getParametersStartingWith(request, null);

		try {
			wxService.editWechatPush(map);
			returnMap.put("msg", "编辑微信推送信息成功!");
			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "编辑微信推送信息失败!");
		}
		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/stopWechatPush/{wechatPushId}", method = RequestMethod.POST)
	public String onSaleBargaining(HttpServletRequest request, Model model, @PathVariable String wechatPushId) {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {

			wxService.stopWechatPush(wechatPushId, 20);
			returnMap.put("success", true);
			returnMap.put("msg", "微信推送信息叫停成功！");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "微信推送信息叫停失败！");
		}

		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/getWechatPushDetail/{wechatPushId}", method = RequestMethod.POST)
	public String getEventBargaining(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String wechatPushId) {

		Map<String, Object> returnMap = new HashMap<String, Object>();

		try {
			// TEventBargaining tEventBargaining =
			// cutMessageService.gettEventBargaining(wechatPushId);

			// returnMap.put("feventId", tEventBargaining.getFeventId());
			// returnMap.put("ftitle", tEventBargaining.getFtitle());
			// returnMap.put("feventtitle", tEventBargaining.getFeventTitle());
			// returnMap.put("fbeginTime",
			// DateFormatUtils.format(tEventBargaining.getFbeginTime(),
			// "yyyy-MM-dd HH:mm"));
			// returnMap.put("fendTime",
			// DateFormatUtils.format(tEventBargaining.getFendTime(),
			// "yyyy-MM-dd HH:mm"));
			// returnMap.put("ftype", tEventBargaining.getFtype());
			// returnMap.put("finputText", tEventBargaining.getFinputText());

			returnMap.put("success", true);
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "获取微信推送详细信息时失败！");
		}

		return mapper.toJson(returnMap);
	}

	@ResponseBody
	@RequestMapping(value = "/pushMessage/{wechatPushId}", method = RequestMethod.POST)
	public String pushMessage(HttpServletRequest request, Model model, @PathVariable String wechatPushId) {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		try {

			wxService.pushMessage(wechatPushId);
			returnMap.put("success", true);
			returnMap.put("msg", "微信推送信息成功！");
		} catch (ServiceException e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", e.getMessage());
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			returnMap.put("success", false);
			returnMap.put("msg", "微信推送信息失败！");
		}

		return mapper.toJson(returnMap);
	}

	public static void main(String arg[]) {
		try {
			String encoding = "GBK"; // 字符编码(可解决中文乱码问题 )

			File file = new File("D:/errlog.txt");

			if (file.isFile() && file.exists()) {

				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);

				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTXT = null;
				while ((lineTXT = bufferedReader.readLine()) != null) {

					// 将文本读取出来的字符去掉"," 因为读取出来的字符是用","来分隔的
					String text = lineTXT.replaceAll(",", "");

					// 用字符分隔成数组
					String[] version_1 = text.split("-");

					for (int i = 0; i < version_1.length; i++) {

						String[] version_2 = version_1[i].split(":");

						for (int j = 0; j < version_2.length; j++) {
							System.out.println(version_2[j]);
						}
					}
				}
				read.close();
			} else {
				System.out.println("找不到指定的文件！");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容操作出错");
			e.printStackTrace();
		}
	}

}
