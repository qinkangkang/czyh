package com.czyh.czyhweb.service.customer;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springside.modules.mapper.BeanMapper;
import org.springside.modules.security.utils.Digests;
import org.springside.modules.utils.Encodes;
import org.springside.modules.utils.Exceptions;

import com.czyh.czyhweb.dao.DictionaryDao;
import com.czyh.czyhweb.dao.EventCategoryDAO;
import com.czyh.czyhweb.dao.ModuleDAO;
import com.czyh.czyhweb.dao.RoleDAO;
import com.czyh.czyhweb.dao.TSystemNoticeDAO;
import com.czyh.czyhweb.dao.UserDAO;
import com.czyh.czyhweb.entity.TDictionary;
import com.czyh.czyhweb.entity.TEventCategory;
import com.czyh.czyhweb.entity.TModule;
import com.czyh.czyhweb.entity.TRole;
import com.czyh.czyhweb.entity.TSystemNotice;
import com.czyh.czyhweb.entity.TUser;
import com.czyh.czyhweb.exception.NotDeletedException;
import com.czyh.czyhweb.exception.ServiceException;
import com.czyh.czyhweb.security.ShiroDbRealm;
import com.czyh.czyhweb.security.ShiroDbRealm.HashPassword;
import com.czyh.czyhweb.security.ShiroDbRealm.ShiroUser;
import com.czyh.czyhweb.service.CommonService;
import com.czyh.czyhweb.util.CommonPage;
import com.czyh.czyhweb.util.Constant;
import com.czyh.czyhweb.util.DictionaryUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * 系统管理员管理类
 * 
 * @author jinshengzhi
 */
@Component
@Transactional
public class UserService {

	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private CommonService commonService;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private EventCategoryDAO eventCategoryDAO;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private RoleDAO roleDAO;

	@Autowired
	private ModuleDAO moduleDAO;

	@Autowired
	private DictionaryDao dictionaryDao;

	@Autowired
	private ShiroDbRealm shiroRealm;

	@Autowired
	private TSystemNoticeDAO tsystemNoticeDAO;

	/**
	 * 设定安全的密码，生成随机的salt并经过1024次 sha-1 hash
	 */
	private void entryptPassword(TUser user) {
		byte[] salt = Digests.generateSalt(ShiroDbRealm.SALT_SIZE);
		user.setSalt(Encodes.encodeHex(salt));

		byte[] hashPassword = Digests.sha1(user.getPassword().getBytes(), salt, ShiroDbRealm.INTERATIONS);
		user.setPassword(Encodes.encodeHex(hashPassword));
	}

	@Transactional(readOnly = true)
	public void initDictionary() {
		Map<Long, Map<String, Map<Integer, String>>> dictionaryCalssMap = Maps.newHashMap();
		List<TDictionary> list = dictionaryDao.findAllOrderByClassId();
		Map<String, Map<Integer, String>> dictionaryMap = null;
		Map<Integer, String> dictionaryCnMap = null;
		Map<Integer, String> dictionaryEnMap = null;
		long tempId = 0;

		Map<Long, Map<String, String>> nameCodeMap = Maps.newHashMap();
		Map<String, String> nameCodeSubMap = null;

		for (TDictionary tDictionary : list) {
			if (!tDictionary.getTDictionaryClass().getId().equals(tempId)) {
				dictionaryCnMap = Maps.newTreeMap();
				dictionaryEnMap = Maps.newTreeMap();
				dictionaryMap = Maps.newHashMap();
				nameCodeSubMap = Maps.newTreeMap();

				dictionaryMap.put("zh_CN", dictionaryCnMap);
				dictionaryMap.put("en_US", dictionaryEnMap);
				dictionaryCalssMap.put(tDictionary.getTDictionaryClass().getId(), dictionaryMap);

				nameCodeMap.put(tDictionary.getTDictionaryClass().getId(), nameCodeSubMap);
			}
			dictionaryCnMap.put(tDictionary.getValue(), tDictionary.getName());
			dictionaryEnMap.put(tDictionary.getValue(), tDictionary.getCode());
			if (StringUtils.isNotBlank(tDictionary.getCode())) {
				nameCodeSubMap.put(tDictionary.getCode(), tDictionary.getName());
			}
			tempId = tDictionary.getTDictionaryClass().getId();
		}
		if (!CollectionUtils.isEmpty(list)) {
			DictionaryUtil.setCodeCalssMap(dictionaryCalssMap);
			DictionaryUtil.setNameCodeMap(nameCodeMap);
		}
	}

	@Transactional(readOnly = true)
	public void initEventCategory() {
		Cache eventCategoryCache = cacheManager.getCache(Constant.EventCategory);
		eventCategoryCache.removeAll();
		// 获取所有活动一级类目和二级类目
		List<TEventCategory> eventCategoryList = eventCategoryDAO.findAll();

		Element element = null;
		for (TEventCategory tEventCategory : eventCategoryList) {
			element = new Element(tEventCategory.getValue(), tEventCategory.getName());
			eventCategoryCache.put(element);
		}
	}

	@Transactional(readOnly = true)
	public TUser get(Long id) {
		return userDAO.findOne(id);
	}

	public void save(TUser user, String[] ids) {
		// 设定安全的密码，使用passwordService提供的salt并经过1024次 sha-1 hash
		if (StringUtils.isNotBlank(user.getPassword()) && shiroRealm != null) {
			HashPassword hashPassword = ShiroDbRealm.encryptPassword(user.getPassword());
			user.setSalt(hashPassword.salt);
			user.setPassword(hashPassword.password);
		}

		Set<TRole> set = user.getRoles();
		for (String roleId : ids) {
			set.add(roleDAO.getOne(Long.valueOf(roleId)));
		}
		userDAO.save(user);
		shiroRealm.clearCachedAuthorizationInfo(user.getUsername());
	}

	public void edit(TUser user, String[] ids) {
		Set<TRole> set = user.getRoles();
		set.clear();
		for (String roleId : ids) {
			set.add(roleDAO.getOne(Long.valueOf(roleId)));
		}
		userDAO.save(user);
	}

	public String delete(Long id) {
		if (id.equals(1L)) {
			logger.warn("操作员{}，尝试删除超级管理员用户", SecurityUtils.getSubject().getPrincipal() + "。");
			throw new NotDeletedException("不能删除超级管理员用户。");
		}
		TUser user = userDAO.getOne(id);
		user.setStatus(999);
		userDAO.save(user);

		// 从shiro中注销
		shiroRealm.clearCachedAuthorizationInfo(user.getUsername());
		return user.getRealname();
	}

	@Transactional(readOnly = true)
	public List<String> getAllPermissions(Long userId) {
		return moduleDAO.findByUserId(userId);
	}

	public void updatePassword(long userId, String password) {
		TUser user = userDAO.getOne(userId);
		user.setPassword(password);
		entryptPassword(user);
		userDAO.save(user);
	}

	public void updatePassword(long userId, String oldPassword, String password) {
		TUser user = userDAO.getOne(userId);

		HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(ShiroDbRealm.ALGORITHM);
		matcher.setHashIterations(ShiroDbRealm.INTERATIONS);

		UsernamePasswordToken token = new UsernamePasswordToken(user.getUsername(), oldPassword);

		SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, user.getPassword(),
				ByteSource.Util.bytes(Encodes.decodeHex(user.getSalt())), user.getUsername());

		if (!matcher.doCredentialsMatch(token, info)) {
			throw new ServiceException("您输入的原登录密码错误！");
		}
		user.setPassword(password);
		entryptPassword(user);
		userDAO.save(user);
	}

	@Transactional(readOnly = true)
	public TUser getByUsername(String username) {
		return userDAO.getByUsername(username);
	}

	@Transactional(readOnly = true)
	public TUser getByPhone(String phone) {
		return userDAO.getByPhone(phone);
	}

	@Transactional(readOnly = true)
	public TUser getByEmail(String email) {
		return userDAO.getByEmail(email);
	}

	@Transactional(readOnly = true)
	public TUser getByUsernameOrPhoneOrEmail(String username) {
		return userDAO.getByUsernameOrPhoneOrEmail(username, username, username);
	}

	@Transactional(readOnly = true)
	public TRole getByRolename(String rolename) {
		return roleDAO.findByName(rolename);
	}

	@Transactional(readOnly = true)
	public Long checkUsername(String username) {
		return userDAO.checkUsername(username);
	}

	@Transactional(readOnly = true)
	public Long checkEditUsername(String username, String id) {
		return userDAO.checkEditUsername(username, Long.valueOf(id));
	}

	@Transactional(readOnly = true)
	public Long checkPhone(String phone) {
		return userDAO.checkPhone(phone);
	}

	@Transactional(readOnly = true)
	public Long checkEmail(String email) {
		return userDAO.checkEmail(email);
	}

	@Transactional(readOnly = true)
	public Long checkEditPhone(String phone, String id) {
		return userDAO.checkPhone(phone, Long.valueOf(id));
	}

	@Transactional(readOnly = true)
	public Long checkEditEmail(String email, String id) {
		return userDAO.checkEmail(email, Long.valueOf(id));
	}

	@Transactional(readOnly = true)
	public Long checkRolecode(String rolecode) {
		return roleDAO.checkRolecode(rolecode);
	}

	@Transactional(readOnly = true)
	public Long checkEditRolecode(String rolecode, String id) {
		return roleDAO.checkEditRolecode(rolecode, Long.valueOf(id));
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getEditorMapList() {
		return commonService.find("select t.id as key, t.fname as value from TUser t where t.category like '%3%'");
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getBDMapList() {
		return commonService.find("select t.id as key, t.fname as value from TUser t where t.category like '%2%'");
	}

	/***
	 * 查询用户列表
	 * 
	 * @param valueMap
	 * @param page
	 * @return
	 */
	@Transactional(readOnly = true)
	public void getUserList(Map<String, Object> valueMap, CommonPage page) {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.realname as realname, t.username as username, t.phone as phone, t.email as email, t.createTime as createTime, t.status as status from TUser t where t.status < 999");

		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_username") && StringUtils.isNotBlank(valueMap.get("s_username").toString())) {
				hql.append(" and t.username like :s_username");
				hqlMap.put("s_username", "%" + valueMap.get("s_username").toString() + "%");
			}
			if (valueMap.containsKey("s_realname") && StringUtils.isNotBlank(valueMap.get("s_realname").toString())) {
				hql.append(" and t.realname like :s_realname");
				hqlMap.put("s_realname", "%" + valueMap.get("s_realname").toString() + "%");
			}
			if (valueMap.containsKey("s_email") && StringUtils.isNotBlank(valueMap.get("s_email").toString())) {
				hql.append(" and t.email like :s_email");
				hqlMap.put("s_email", "%" + valueMap.get("s_email").toString() + "%");
			}
			if (valueMap.containsKey("s_status") && StringUtils.isNotBlank(valueMap.get("s_status").toString())) {
				hql.append(" and t.status = s_status");
				hqlMap.put("s_status", Integer.valueOf(valueMap.get("s_name").toString()));
			}
			if (valueMap.containsKey("s_category") && StringUtils.isNotBlank(valueMap.get("s_category").toString())) {
				hql.append(" and t.category like :s_category");
				hqlMap.put("s_category", "%" + valueMap.get("s_category").toString() + "%");
			}
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by t.createTime desc ");
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询QL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		Date date = null;
		for (Map<String, Object> amap : list) {
			if (amap.get("createTime") != null && StringUtils.isNotBlank(amap.get("createTime").toString())) {
				date = (Date) amap.get("createTime");
				amap.put("createTime", DateFormatUtils.format(date, "yyyy-MM-dd"));
			}
			if (amap.get("status") != null && StringUtils.isNotBlank(amap.get("status").toString())) {
				amap.put("status", DictionaryUtil.getString(DictionaryUtil.UserStatus, (Integer) amap.get("status"),
						shiroUser.getLanguage()));
			}
		}
	}

	@Transactional(readOnly = true)
	public TRole getRole(Long id) {
		return roleDAO.getOne(id);
	}

	public void saveRole(TRole role, String[] ids) {
		Set<TModule> set = role.getModules();
		for (String moduleId : ids) {
			set.add(moduleDAO.getOne(Long.valueOf(moduleId)));
		}
		roleDAO.save(role);
	}

	public void editRole(Map<String, Object> valueMap) {

		TRole role = roleDAO.getOne(Long.valueOf(valueMap.get("id").toString()));
		role.setName(valueMap.get("name").toString());
		role.setCode(valueMap.get("code").toString());
		role.setDescription(valueMap.get("description").toString());
		Set<TModule> set = role.getModules();
		set.clear();

		String[] ids = null;
		if (!valueMap.containsKey("ids")) {
			ids = ArrayUtils.EMPTY_STRING_ARRAY;
		} else if (valueMap.containsKey("ids") && ObjectUtils.isArray(valueMap.get("ids"))) {
			ids = (String[]) valueMap.get("ids");
		} else {
			ids = ArrayUtils.toArray((valueMap.get("ids").toString()));
		}
		for (String moduleId : ids) {
			set.add(moduleDAO.getOne(Long.valueOf(moduleId)));
		}
		roleDAO.save(role);
	}

	public void delRole(Long roleId) {
		roleDAO.delete(roleId);
	}

	/***
	 * 查询角色列表
	 * 
	 * @param valueMap
	 * @param page
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<Map<String, Object>> getRoleList(Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.name as name, t.code as code, t.description as description, count(u.id) as userCount from TRole t left join t.users as u where 1 = 1");

		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_name") && StringUtils.isNotBlank(valueMap.get("s_name").toString())) {
				hql.append(" and t.name like :name ");
				hqlMap.put("name", "%" + valueMap.get("s_name").toString() + "%");
			}
			hql.append(" group by t.id");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询QL语句时出错！");
		}
		return commonService.find(hql.toString(), hqlMap);
	}

	@Transactional(readOnly = true)
	public List<TModule> getAllModule() {
		return moduleDAO.findAll();
	}

	@Transactional(readOnly = true)
	public List<TRole> getAllRole() {
		return roleDAO.findAll();
	}

	/***
	 * 查询配置类别信息列表
	 * 
	 * @param valueMap
	 * @param page
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<Map<String, Object>> getConfigList(Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.name as name, t.code as code from TDictionaryClass t where t.editable = 1");

		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {
			if (valueMap.containsKey("s_name") && StringUtils.isNotBlank(valueMap.get("s_name").toString())) {
				hql.append(" and t.name like :name ");
				hqlMap.put("name", "%" + valueMap.get("s_name").toString() + "%");
			}
			hql.append(" order by t.id");
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询QL语句时出错！");
		}
		return commonService.find(hql.toString(), hqlMap);
	}

	/***
	 * 查询配置项信息列表
	 * 
	 * @param valueMap
	 * @param page
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<Map<String, Object>> getConfigItemList(Map<String, Object> valueMap) {
		// ShiroUser shiroUser = (ShiroUser)
		// SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.name as name, t.code as code, t.value as value from TDictionary t where t.TDictionaryClass.id = :classId and t.status = 1");

		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("classId", Long.valueOf(valueMap.get("classId").toString()));

		try {
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by t.id asc");
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询QL语句时出错！");
		}

		return commonService.find(hql.toString(), hqlMap);
	}

	@Transactional(readOnly = true)
	public Long countDictionaryByNameAndClassId(String name, Long classId, Long id) {
		if (id == null || id.equals(0L)) {
			return dictionaryDao.countByNameAndClassId(name, classId);
		} else {
			return dictionaryDao.countByNameAndClassIdNotId(name, classId, id);
		}
	}

	@Transactional(readOnly = true)
	public Long countDictionaryByCodeAndClassId(String code, Long classId, Long id) {
		if (id == null || id.equals(0L)) {
			return dictionaryDao.countByCodeAndClassId(code, classId);
		} else {
			return dictionaryDao.countByCodeAndClassIdNotId(code, classId, id);
		}
	}

	@Transactional(readOnly = true)
	public Long countDictionaryByValueAndClassId(String val, Long classId, Long id) {
		if (id == null || id.equals(0L)) {
			return dictionaryDao.countByValueAndClassId(Integer.valueOf(val), classId);
		} else {
			return dictionaryDao.countByValueAndClassIdNotId(Integer.valueOf(val), classId, id);
		}
	}

	@Transactional(readOnly = true)
	public TDictionary getDictionaryById(Long id) {
		return dictionaryDao.getOne(id);
	}

	public void addDictionary(TDictionary dictionary) {
		dictionaryDao.save(dictionary);
	}

	public void updateDictionary(TDictionary dictionary) {
		TDictionary db = dictionaryDao.getOne(dictionary.getId());
		db.setName(dictionary.getName());
		db.setCode(dictionary.getCode());
		db.setValue(dictionary.getValue());
		dictionaryDao.save(db);
	}

	public void deleteDictionary(Long id) {
		dictionaryDao.delete(id);
	}

	@Transactional(readOnly = true)
	public List<TModule> getMenu(Long userId) {
		// 先取得该用户有权限的Module的List
		List<TModule> moduleList = moduleDAO.getModuleByUserId(userId);
		List<TModule> newModuleList = Lists.newArrayList();
		for (TModule tModule : moduleList) {
			if (tModule.getParent().getId().equals(1L)) {
				newModuleList.add(tModule);
			}
		}

		List<TModule> subModuleList = null;
		for (TModule tModule : newModuleList) {
			subModuleList = Lists.newArrayList();
			for (TModule tModule2 : moduleList) {
				if (tModule.getId().equals(tModule2.getParent().getId())) {
					subModuleList.add(tModule2);
				}
			}
			tModule.setChildren(subModuleList);
		}
		return newModuleList;
	}

	/**
	 * 公共方法，根据类别ID返回用户的MAP对象列表
	 * 
	 * @param roleId
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<Map<String, Object>> getUserListByCategoryId(int categoryId) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as key, t.realname as value from TUser t where t.category like :categoryId and t.status < 999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("categoryId", "%" + categoryId + "%");
		return commonService.find(hql.toString(), hqlMap);
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getUserList() {
		StringBuilder hql = new StringBuilder();
		hql.append("select t.id as key, t.realname as value from TUser t where t.status < 999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		return commonService.find(hql.toString(), hqlMap);
	}

	/**
	 * 公共方法，根据用户角色返回用户的map对象列表
	 * 
	 * @param codeList
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<Map<String, Object>> getUserListByRoleCode(List codeList) {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select u.id as key, u.username as value from TUser u left join u.roles r where r.code in(:codeList)");
		Map<String, Object> hqlMap = new HashMap<String, Object>();
		hqlMap.put("codeList", codeList);
		return commonService.find(hql.toString(), hqlMap);
	}

	@Transactional(readOnly = true)
	public void getNoticeList(Map<String, Object> valueMap, CommonPage page) {

		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.ftitle as ftitle, t.fnoticeContent as fnoticeContent, t.fnoticeType as fnoticeType,t.fnoticeVersion as fnoticeVersion,t.fstatus as fstatus,t.fcreateTime as fcreateTime,t.fupdateTime as fupdateTime from TSystemNotice t where t.fstatus<999");

		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {

			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by t.fcreateTime desc ");
			}
		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询QL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		Date date = null;
		for (Map<String, Object> amap : list) {

			if (amap.get("fcreateTime") != null && StringUtils.isNotBlank(amap.get("fcreateTime").toString())) {
				date = (Date) amap.get("fcreateTime");
				amap.put("fcreateTime", DateFormatUtils.format(date, "yyyy-MM-dd"));
			}
			if (amap.get("fupdateTime") != null && StringUtils.isNotBlank(amap.get("fupdateTime").toString())) {
				date = (Date) amap.get("fupdateTime");
				amap.put("fupdateTime", DateFormatUtils.format(date, "yyyy-MM-dd"));
			}
			if (amap.get("fnoticeType") != null && StringUtils.isNotBlank(amap.get("fnoticeType").toString())) {
				amap.put("fnoticeType", DictionaryUtil.getString(DictionaryUtil.czyhwebNotice,
						(Integer) amap.get("fnoticeType"), shiroUser.getLanguage()));
			}
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("fstatus", DictionaryUtil.getString(DictionaryUtil.NoticeStatus, (Integer) amap.get("fstatus"),
						shiroUser.getLanguage()));
			}
		}
	}

	public void addNotice(Map<String, Object> valueMap) {

		// TSystemNotice db =
		// tsystemNoticeDAO.getOne(valueMap.get("id").toString());
		//
		// if (valueMap.containsKey("ftitle") &&
		// StringUtils.isNotBlank(valueMap.get("ftitle").toString())) {
		// db.setFtitle(valueMap.get("ftitle").toString());
		// } else {
		// db.setFtitle(null);
		// }
		// if (valueMap.containsKey("fnoticeVersion") &&
		// StringUtils.isNotBlank(valueMap.get("fnoticeVersion").toString())) {
		// db.setFnoticeVersion(valueMap.get("fnoticeVersion").toString());
		// } else {
		// db.setFnoticeVersion(null);
		// }
		// if (valueMap.containsKey("fnoticeType")
		// && StringUtils.isNotBlank(valueMap.get("fnoticeType").toString())) {
		// db.setFnoticeType(Integer.valueOf(valueMap.get("fnoticeType").toString()));
		// } else {
		// db.setFnoticeType(null);
		// }
		// if (valueMap.containsKey("fnoticeContent") &&
		// StringUtils.isNotBlank(valueMap.get("fnoticeContent").toString())) {
		// db.setFnoticeVersion(valueMap.get("fnoticeContent").toString());
		// } else {
		// db.setFnoticeVersion(null);
		// }

		TSystemNotice db = new TSystemNotice();
		Date date = new Date();
		BeanMapper.copy(valueMap, db);
		db.setFcreateTime(date);

		tsystemNoticeDAO.save(db);
	}

	public void delNotice(String NoticeId) {
		tsystemNoticeDAO.del(999, NoticeId);
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> getNoticeDetailList() {
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.ftitle as ftitle, t.fnoticeContent as fnoticeContent, t.fnoticeType as fnoticeType,t.fnoticeVersion as fnoticeVersion,t.fstatus as fstatus,t.fcreateTime as fcreateTime,t.fupdateTime as fupdateTime from TSystemNotice t where t.fstatus<999");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		List<Map<String, Object>> list = commonService.find(hql.toString(), hqlMap);
		return list;
	}

	@Transactional(readOnly = true)
	public void getErrorList(Map<String, Object> valueMap, CommonPage page) {

		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.id as DT_RowId, t.freportTime as freportTime, t.fclientType as fclientType,t.fclientInfo as fclientInfo,t.ferrorMessage as ferrorMessage")
				.append(",t.ferrorText as ferrorText,t.fsystem as fsystem,t.fuser as fuser,t.fview as fview,t.fdata as fdata from TAppError t where 1=1 ");

		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {

			if (valueMap.containsKey("s_fdescription")
					&& StringUtils.isNotBlank(valueMap.get("s_fdescription").toString())) {
				hql.append(" and t.ferrorText like :s_fdescription ");
				hqlMap.put("s_fdescription", "%" + valueMap.get("s_fdescription").toString() + "%");
			}

			if (valueMap.containsKey("fclientType") && StringUtils.isNotBlank(valueMap.get("fclientType").toString())) {
				hql.append(" and t.fclientType = :fclientType ");
				hqlMap.put("fclientType", Integer.valueOf(valueMap.get("fclientType").toString()));
			}
			if (valueMap.containsKey("freportTimeStart")
					&& StringUtils.isNotBlank(valueMap.get("freportTimeStart").toString())) {
				hql.append(" and t.freportTime >= :freportTimeStart ");
				hqlMap.put("freportTimeStart",
						DateUtils.parseDate(valueMap.get("freportTimeStart").toString(), "yyyy-MM-dd"));
			}
			if (valueMap.containsKey("freportTimeEnd")
					&& StringUtils.isNotBlank(valueMap.get("freportTimeEnd").toString())) {
				hql.append(" and t.freportTime < :freportTimeEnd ");
				// 选择的截止日期加一天，保证了查询条件的完整性，解决了时分秒的问题。
				hqlMap.put("freportTimeEnd", DateUtils
						.addDays(DateUtils.parseDate(valueMap.get("freportTimeEnd").toString(), "yyyy-MM-dd"), 1));
			}
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by t.freportTime desc ");
			}

		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询QL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		Date date = null;
		for (Map<String, Object> amap : list) {

			if (amap.get("freportTime") != null && StringUtils.isNotBlank(amap.get("freportTime").toString())) {
				date = (Date) amap.get("freportTime");
				amap.put("freportTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
			if (amap.get("fclientType") != null && StringUtils.isNotBlank(amap.get("fclientType").toString())) {
				if (Integer.valueOf(amap.get("fclientType").toString()).intValue() == 1) {
					amap.put("fclientType", "web");
				} else if (Integer.valueOf(amap.get("fclientType").toString()).intValue() == 2) {
					amap.put("fclientType", "ios");
				} else if (Integer.valueOf(amap.get("fclientType").toString()).intValue() == 3) {
					amap.put("fclientType", "android");
				}
			}
			if (amap.get("fnoticeType") != null && StringUtils.isNotBlank(amap.get("fnoticeType").toString())) {
				amap.put("fnoticeType", DictionaryUtil.getString(DictionaryUtil.czyhwebNotice,
						(Integer) amap.get("fnoticeType"), shiroUser.getLanguage()));
			}
			if (amap.get("fstatus") != null && StringUtils.isNotBlank(amap.get("fstatus").toString())) {
				amap.put("fstatus", DictionaryUtil.getString(DictionaryUtil.NoticeStatus, (Integer) amap.get("fstatus"),
						shiroUser.getLanguage()));
			}
		}
	}

	@Transactional(readOnly = true)
	public void getOperatLog(Map<String, Object> valueMap, CommonPage page) {

		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		StringBuilder hql = new StringBuilder();
		hql.append(
				"select t.createTime as createTime,t.ipAddress as ipAddress,t.logLevel as logLevel,t.message as message,t.username as username,t.userId as userId,m.name as name from TLogInfo t inner join TModule m on t.module = m.id");
		Map<String, Object> hqlMap = new HashMap<String, Object>();

		try {

			if (valueMap.containsKey("czyhwebModule")
					&& StringUtils.isNotBlank(valueMap.get("czyhwebModule").toString())) {
				hql.append(" and t.module = :czyhwebModule ");
				hqlMap.put("czyhwebModule", Integer.valueOf(valueMap.get("czyhwebModule").toString()));
			}
			if (valueMap.containsKey("message") && StringUtils.isNotBlank(valueMap.get("message").toString())) {
				hql.append(" and t.message like :message ");
				hqlMap.put("message", "%" + valueMap.get("message").toString() + "%");
			}
			if (valueMap.containsKey("userId") && StringUtils.isNotBlank(valueMap.get("userId").toString())) {
				hql.append(" and t.userId = :userId ");
				hqlMap.put("userId", Long.valueOf(valueMap.get("userId").toString()));
			}
			if (valueMap.containsKey("freportTimeStart")
					&& StringUtils.isNotBlank(valueMap.get("freportTimeStart").toString())) {
				hql.append(" and t.createTime >= :freportTimeStart ");
				hqlMap.put("freportTimeStart",
						DateUtils.parseDate(valueMap.get("freportTimeStart").toString(), "yyyy-MM-dd"));
			}
			if (valueMap.containsKey("freportTimeEnd")
					&& StringUtils.isNotBlank(valueMap.get("freportTimeEnd").toString())) {
				hql.append(" and t.createTime < :freportTimeEnd ");
				// 选择的截止日期加一天，保证了查询条件的完整性，解决了时分秒的问题。
				hqlMap.put("freportTimeEnd", DateUtils
						.addDays(DateUtils.parseDate(valueMap.get("freportTimeEnd").toString(), "yyyy-MM-dd"), 1));
			}
			if (Integer.valueOf(valueMap.get("order[0][column]").toString()) > 0) {
				hql.append(" order by ").append(
						valueMap.get("columns[" + valueMap.get("order[0][column]").toString() + "][data]").toString())
						.append(" ").append(valueMap.get("order[0][dir]").toString());
			} else {
				hql.append(" order by t.createTime desc ");
			}

		} catch (Exception e) {
			logger.error(Exceptions.getStackTraceAsString(Exceptions.getRootCause(e)));
			throw new ServiceException("生成查询QL语句时出错！");
		}
		commonService.findPage(hql.toString(), page, hqlMap);
		List<Map<String, Object>> list = page.getResult();

		Date date = null;
		for (Map<String, Object> amap : list) {

			if (amap.get("createTime") != null && StringUtils.isNotBlank(amap.get("createTime").toString())) {
				date = (Date) amap.get("createTime");
				amap.put("createTime", DateFormatUtils.format(date, "yyyy-MM-dd HH:mm"));
			}
		}
	}

}