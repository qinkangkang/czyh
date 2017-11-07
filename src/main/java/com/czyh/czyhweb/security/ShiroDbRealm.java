package com.czyh.czyhweb.security;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.security.utils.Digests;
import org.springside.modules.utils.Encodes;

import com.czyh.czyhweb.entity.TModule;
import com.czyh.czyhweb.entity.TRole;
import com.czyh.czyhweb.entity.TUser;
import com.czyh.czyhweb.service.customer.UserService;

public class ShiroDbRealm extends AuthorizingRealm {

	// private static final Logger logger =
	// LoggerFactory.getLogger(ShiroDbRealm.class);

	public static final int INTERATIONS = 1024;
	public static final int SALT_SIZE = 8;
	public static final String ALGORITHM = "SHA-1";

	@Autowired
	private UserService userService;

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	/**
	 * 给ShiroDbRealm提供编码信息，用于密码密码比对 描述
	 */
	public ShiroDbRealm() {
		super();
		HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(ALGORITHM);
		matcher.setHashIterations(INTERATIONS);
		setCredentialsMatcher(matcher);
	}

	/**
	 * 认证回调函数, 登录时调用.
	 */
	// TODO 对认证进行缓存处理
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken)
			throws AuthenticationException {

		UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
		TUser user = userService.getByUsername(token.getUsername());
		if (user != null) {
			if (!user.getStatus().equals(1)) {
				throw new DisabledAccountException();
			}
			byte[] salt = Encodes.decodeHex(user.getSalt());

			ShiroUser shiroUser = new ShiroUser(user.getId(), user.getUsername(), user.getRealname());
			shiroUser.setIpAddress(token.getHost());

			// 这里可以缓存认证
			return new SimpleAuthenticationInfo(shiroUser, user.getPassword(), ByteSource.Util.bytes(salt), getName());
		} else {
			return null;
		}

	}

	/**
	 * 授权查询回调函数, 进行鉴权但缓存中无用户的授权信息时调用.
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		ShiroUser shiroUser = (ShiroUser) principals.getPrimaryPrincipal();
		TUser user = userService.getByUsername(shiroUser.loginName);
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		// 将用户的角色名称保存到角色信息中
		Collection<String> hasRoles = new HashSet<String>();
		for (TRole role : user.getRoles()) {
			hasRoles.add(role.getCode());
		}
		info.addRoles(hasRoles);
		info.addStringPermissions(userService.getAllPermissions(user.getId()));
		return info;
	}

	public static class HashPassword {
		public String salt;
		public String password;
	}

	public static HashPassword encryptPassword(String plainPassword) {
		HashPassword result = new HashPassword();
		byte[] salt = Digests.generateSalt(SALT_SIZE);
		result.salt = Encodes.encodeHex(salt);

		byte[] hashPassword = Digests.sha1(plainPassword.getBytes(), salt, INTERATIONS);
		result.password = Encodes.encodeHex(hashPassword);
		return result;
	}

	/**
	 * 
	 * 验证密码
	 * 
	 * @param plainPassword
	 *            明文密码
	 * @param password
	 *            密文密码
	 * @param salt
	 *            盐值
	 * @return
	 */
	public static boolean validatePassword(String plainPassword, String password, String salt) {
		byte[] hashPassword = Digests.sha1(plainPassword.getBytes(), Encodes.decodeHex(salt), INTERATIONS);
		String oldPassword = Encodes.encodeHex(hashPassword);
		return password.equals(oldPassword);
	}

	/*
	 * 覆盖父类方法，设置AuthorizationCacheKey为ShiroUser的loginName，这样才能顺利删除shiro中的缓存。
	 * 因为shiro默认的AuthorizationCacheKey为PrincipalCollection的对象。
	 * 
	 * @see
	 * org.apache.shiro.realm.AuthorizingRealm#getAuthorizationCacheKey(org.
	 * apache.shiro.subject.PrincipalCollection)
	 */
	@Override
	protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
		ShiroUser shiroUser = (ShiroUser) principals.getPrimaryPrincipal();
		return shiroUser.getLoginName();
	}

	/**
	 * 更新用户授权信息缓存.
	 */
	public void clearCachedAuthorizationInfo(String loginName) {
		ShiroUser shiroUser = new ShiroUser(loginName);

		SimplePrincipalCollection principals = new SimplePrincipalCollection(shiroUser, getName());
		clearCachedAuthorizationInfo(principals);
	}

	/**
	 * 清除所有用户授权信息缓存.
	 */
	public void clearAllCachedAuthorizationInfo() {
		Cache<Object, AuthorizationInfo> cache = getAuthorizationCache();
		if (cache != null) {
			for (Object key : cache.keys()) {
				cache.remove(key);
			}
		}
	}

	/**
	 * 自定义Authentication对象，使得Subject除了携带用户的登录名外还可以携带更多信息.
	 */
	public static class ShiroUser implements Serializable {

		private static final long serialVersionUID = 1L;
		private Long id;
		private String loginName;
		private String name;
		private String ipAddress;
		private String language = "zh_CN";
		private Map<String, TModule> hasModules = new HashMap<String, TModule>();

		public ShiroUser() {
		}

		public ShiroUser(String loginName) {
			this.loginName = loginName;
		}

		public ShiroUser(Long id, String loginName, String name) {
			this.id = id;
			this.loginName = loginName;
			this.name = name;
		}

		/**
		 * @return the id
		 */
		public Long getId() {
			return id;
		}

		/**
		 * @param id
		 *            the id to set
		 */
		public void setId(Long id) {
			this.id = id;
		}

		/**
		 * @return the loginName
		 */
		public String getLoginName() {
			return loginName;
		}

		/**
		 * @param loginName
		 *            the loginName to set
		 */
		public void setLoginName(String loginName) {
			this.loginName = loginName;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return the ipAddress
		 */
		public String getIpAddress() {
			return ipAddress;
		}

		/**
		 * @param ipAddress
		 *            the ipAddress to set
		 */
		public void setIpAddress(String ipAddress) {
			this.ipAddress = ipAddress;
		}

		public String getLanguage() {
			return this.language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

		/**
		 * @return the hasModules
		 */
		public Map<String, TModule> getHasModules() {
			return hasModules;
		}

		/**
		 * @param hasModules
		 *            the hasModules to set
		 */
		public void setHasModules(Map<String, TModule> hasModules) {
			this.hasModules = hasModules;
		}

		/**
		 * 本函数输出将作为默认的<shiro:principal/>输出.
		 */
		@Override
		public String toString() {
			return loginName;
		}
	}

}