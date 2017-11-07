package com.czyh.czyhweb.web.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springside.modules.mapper.JsonMapper;

import com.czyh.czyhweb.service.ConfigurationService;

/**
 * 管理员的web处理类
 * 
 * @author jinshengzhgi
 * 
 */
@Controller
@RequestMapping("/fxl/configuration")
public class ConfigurationController {

	private static Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

	private static JsonMapper mapper = JsonMapper.nonDefaultMapper();

	@Autowired
	private ConfigurationService configurationService;

	/**
	 * 刷新查询到得配置文件数据
	 **/
	@RequestMapping("/fresh")
	public @ResponseBody String fresh() throws Exception {
		configurationService.initConfigurationMap();
		return "刷新配置缓存成功";
	}

}