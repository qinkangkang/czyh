package com.czyh.czyhweb.service.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.mapper.BeanMapper;
import org.springside.modules.mapper.JsonMapper;
import org.springside.modules.utils.Exceptions;

import com.czyh.czyhweb.dao.MenuDAO;
import com.czyh.czyhweb.entity.TGoodsTypeClass;
import com.czyh.czyhweb.entity.TModule;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.service.CommonService;
import com.czyh.czyhweb.util.CommonPage;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Component
@Transactional
public class MenuService {
	private static final Logger logger = LoggerFactory.getLogger(MenuService.class);

	private static JsonMapper mapper = new JsonMapper(Include.ALWAYS);

	@Autowired
	private CommonService commonService;

	@Autowired
	private MenuDAO menuDao;

	public void getMenuList(Map<String, Object> map, CommonPage page) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.name as name, t.className as className, t.sn as sn, t.priority as priority, t.url as url,t.description as description from TModule t   order by t.id  ASC");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		commonService.findPage(hql.toString(), page, hqlMap);
	}

	public List<TModule> findList() {

		return menuDao.findAll();
	}

	public TModule getMenuInfo(Long specId) {
		return menuDao.getOne(specId);
	}

	public String saveMenu(Map<String, Object> valueMap) {
		TModule module = new TModule();
		BeanMapper.copy(valueMap, module);
		if (valueMap.containsKey("parent.id") && StringUtils.isNotBlank(valueMap.get("parent.id").toString())) {
			TModule parent = new TModule();
			parent.setId(Long.valueOf(valueMap.get("parent.id").toString()));
			module.setParent(parent);
		}
		menuDao.save(module);
		return module.getId().toString();
	}

	public void editModule(Map<String, Object> valueMap) {
		String id = valueMap.get("id").toString();// 获取编辑属性的id
		TModule module = menuDao.getOne(Long.valueOf(id));
		if (valueMap.containsKey("name") && StringUtils.isNoneBlank(valueMap.get("name").toString())) {
			module.setName(valueMap.get("name").toString());
		} else {
			module.setName(null);
		}

		if (valueMap.containsKey("className") && StringUtils.isNotBlank(valueMap.get("className").toString())) {
			module.setClassName(valueMap.get("className").toString());
		} else {
			module.setClassName(null);
		}

		if (valueMap.containsKey("sn") && StringUtils.isNotBlank(valueMap.get("sn").toString())) {
			module.setSn(valueMap.get("sn").toString());
		} else {
			module.setSn(null);
		}

		if (valueMap.containsKey("url") && StringUtils.isNoneBlank(valueMap.get("url").toString())) {
			module.setUrl(valueMap.get("url").toString());
		} else {
			module.setUrl(null);
		}

		if (valueMap.containsKey("description") && StringUtils.isNotBlank(valueMap.get("description").toString())) {
			module.setDescription(valueMap.get("description").toString());
		} else {
			module.setDescription(null);
		}

		if (valueMap.containsKey("priority") && StringUtils.isNoneBlank(valueMap.get("priority").toString())) {
			module.setPriority(Integer.valueOf(valueMap.get("priority").toString()));
		} else {
			module.setPriority(null);
		}

		// 父級菜單

		// 子級菜單

		menuDao.save(module);

	}

	public void deleteModuleById(Long id) {
		menuDao.delete(id);
	}

	public List<Map<String, Object>> getMenuList(Map<String, Object> valueMap) {
		StringBuilder hql = new StringBuilder();
		hql.append("select *  from TModule t ");

		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			hql.append(" order by t.id");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询QL语句时出错！");
		}
		return commonService.find(hql.toString(), hqlMap);

	}

	public void editModuleName(Map<String, Object> valueMap) {
		String id = valueMap.get("id").toString();
		TModule module = menuDao.getOne(Long.valueOf(id));
		if (valueMap.containsKey("name") && StringUtils.isNotBlank(valueMap.get("name").toString())) {
			module.setName(valueMap.get("name").toString());
		}
		menuDao.save(module);
	}

	public TModule findMenuInfo(Long menuId) {
		return menuDao.getOne(menuId);
	}

	public void updateMenu(Map<String, Object> valueMap) {
		String id = valueMap.get("id").toString();
		// 根据id查找原本的menu信息
		TModule tModule = menuDao.getOne(Long.valueOf(id));
		// 更改菜单属性
		if (valueMap.containsKey("name") && StringUtils.isNotBlank(valueMap.get("name").toString())) {
			tModule.setName(valueMap.get("name").toString());
		} else {
			tModule.setName(null);
		}

		if (valueMap.containsKey("className") && StringUtils.isNotBlank(valueMap.get("className").toString())) {
			tModule.setClassName(valueMap.get("className").toString());
		} else {
			tModule.setClassName(null);
		}

		if (valueMap.containsKey("sn") && StringUtils.isNotBlank(valueMap.get("sn").toString())) {
			tModule.setSn(valueMap.get("sn").toString());
		} else {
			tModule.setSn(null);
		}
		if (valueMap.containsKey("url") && StringUtils.isNotBlank(valueMap.get("url").toString())) {
			tModule.setUrl(valueMap.get("url").toString());
		} else {
			tModule.setUrl(null);
		}
		if (valueMap.containsKey("description") && StringUtils.isNotBlank(valueMap.get("description").toString())) {
			tModule.setDescription(valueMap.get("description").toString());
		} else {
			tModule.setDescription(null);
		}
		if (valueMap.containsKey("priority") && StringUtils.isNotBlank(valueMap.get("priority").toString())) {
			tModule.setPriority(Integer.valueOf(valueMap.get("priority").toString()));
		} else {
			tModule.setPriority(null);
		}

		menuDao.save(tModule);

	}

	public Long countByMenuName(String name, Long id) {
		if(id==null || id.equals(0L)){
			return menuDao.countByMenuName(name);
		}else{
			return menuDao.countByMenuNameAndNotId(name,id) ;
		}
		
	}

}
