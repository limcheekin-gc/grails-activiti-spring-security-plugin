/* Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grails.activiti.springsecurity

import org.activiti.engine.identity.Group
import org.activiti.engine.identity.GroupQuery
import org.activiti.engine.identity.User
import org.activiti.engine.identity.UserQuery
import org.activiti.engine.impl.Page
import org.activiti.engine.impl.cfg.IdentitySession
import org.activiti.engine.impl.interceptor.Session
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.web.pages.FastStringWriter
import grails.util.GrailsNameUtils as GNU
import org.activiti.engine.impl.GroupQueryImpl
import org.activiti.engine.impl.UserQueryImpl
import org.activiti.engine.impl.context.Context
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils as SSU
import org.activiti.engine.impl.identity.IdentityInfoEntity

/**
 *
 * @author <a href='mailto:limcheekin@vobject.com'>Lim Chee Kin</a>
 *
 * @since 0.1
 */
class SpringSecurityIdentitySession implements IdentitySession, Session {
	static final Log LOG = LogFactory.getLog(SpringSecurityIdentitySession.class)
	
	/* User */
	User createNewUser(String userId) {
		throw new UnsupportedOperationException("Please use ${getUserDomainClassName()}.save() to create User.")
	}
	
	void insertUser(User user) {
		throw new UnsupportedOperationException("Please use ${getUserDomainClassName()}.save() to insert User.")
	}
	
	void updateUser(User updatedUser) {
		throw new UnsupportedOperationException("Please use ${getUserDomainClassName()}.save() to update User.")
	}
	
	void deleteUser(String userId) {
		throw new UnsupportedOperationException("Please use ${getUserDomainClassName()}.delete() to delete User.")
	}
	
	User findUserById(String userId) {
		LOG.debug "findUserById ($userId)"
		User user = getUserDomainClass()."findBy${getUsernameClassName()}"(userId)
		return user
	}
	
	private getUserDomainClassName() {
		return SSU.securityConfig.userLookup.userDomainClassName
	}
	
	private getUserDomainClass() {
		return AH.application.getDomainClass(getUserDomainClassName()).clazz
	}
	
	private getGroupDomainClassName() {
		return SSU.securityConfig.authority.className
	}
	
	private getGroupDomainClass() {
		return AH.application.getDomainClass(getGroupDomainClassName()).clazz
	}
	
	private getGroupJoinDomainClassName() {
		return SSU.securityConfig.userLookup.authorityJoinClassName
	}
	
	private getGroupJoinDomainClass() {
		return AH.application.getDomainClass(getGroupJoinDomainClassName()).clazz
	}
	
	private getUsernamePropertyName() {
		return SSU.securityConfig.userLookup.usernamePropertyName
	}
		
	private getUsernameClassName() {
		return GNU.getClassNameRepresentation(getUsernamePropertyName())
	}
	
	List<User> findUsersByGroupId(String groupId) {
		LOG.debug "findUsersByGroupId ($groupId)"
		throw new UnsupportedOperationException()
	}
	
	boolean isValidUser(String userId) {
		LOG.debug "isValidUser ($userId)"
		return getUserDomainClass()."findBy${getUsernameClassName()}"(userId) != null
	}
	
	UserQuery createNewUserQuery() {
		LOG.debug "SpringSecurityIdentitySession.createNewUserQuery()"
		return new UserQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired())
	}
	
	List<User> findUserByQueryCriteria(Object query, Page page) {
		LOG.debug "findUserByQueryCriteria (${query.class.name}, $page)"
		List<User> users
		String queryString = createUserQueryString(query)
		LOG.debug "queryString = $queryString"
		if (page) { // listPage()
			users = getUserDomainClass().findAll(queryString, [offset:page.firstResult, max:page.maxResults])
		} else { // list()
			users = getUserDomainClass().findAll(queryString, Collections.emptyMap())
		}
		LOG.debug "query.groupId = ${query.groupId}"
		if (users && query.groupId) {
			users = users.findAll { it.authorities*.id.contains(query.groupId) }
		}
		return users
	}
	
	long findUserCountByQueryCriteria(Object query) {
		LOG.debug "findUserCountByQueryCriteria (${query.class.name})"
		String queryString = createUserQueryString(query)
		LOG.debug "queryString = $queryString"
		return getUserDomainClass().executeQuery("select count(u) ${queryString}")[0]
	}
	
	private String createUserQueryString(Object query) {
		FastStringWriter queryString = new FastStringWriter()
		queryString << "from ${getUserDomainClassName()} as u"
		if (query.id)
			queryString << " where u.${getUsernamePropertyName()}='${query.id}'"
		
		if (query.firstName) {
			queryString << appendWhereOrAnd(queryString)
			queryString << "u.firstName = '${query.firstName}'"
		}
		
		if (query.firstNameLike) {
			queryString << appendWhereOrAnd(queryString)
			queryString << "u.firstName like '${query.firstNameLike}'"
		}
		
		if (query.lastName) {
			queryString << appendWhereOrAnd(queryString)
			queryString << "u.lastName = '${query.lastName}'"
		}
		
		if (query.lastNameLike) {
			queryString << appendWhereOrAnd(queryString)
			queryString << "u.lastName like '${query.lastNameLike}'"
		}
		
		if (query.email) {
			queryString << appendWhereOrAnd(queryString)
			queryString << "u.email = '${query.email}'"
		}
		
		if (query.emailLike) {
			queryString << appendWhereOrAnd(queryString)
			queryString << "u.email like '${query.emailLike}'"
		}
		
		if (query.orderBy) {
			String orderBy = query.orderBy.toLowerCase().replace('_', '')
			orderBy = orderBy.replace("last", "lastName")
			orderBy = orderBy.replace("first", "firstName")
			queryString << " order by ${orderBy}"
		}
		return queryString.toString()
	}
	
	/* Group */
	Group createNewGroup(String groupId) {
		throw new UnsupportedOperationException("Please use ${getGroupDomainClass()}.save() to create Group.")
	}
	
	void insertGroup(Group group) {
		throw new UnsupportedOperationException("Please use ${getGroupDomainClass()}.save() to create Group.")
	}
	
	void updateGroup(Group updatedGroup) {
		throw new UnsupportedOperationException("Please use ${getGroupDomainClass()}.save() to update Group.")
	}
	
	void deleteGroup(String groupId) {
		throw new UnsupportedOperationException("Please use ${getGroupDomainClass()}.delete() to delete Group.")
	}
	
	Group findGroupById(String groupId) {
		throw new UnsupportedOperationException("Please use ${getGroupDomainClass()}.get(id) to find Group by Id.")
	}
	
	List<Group> findGroupsByUser(String userId) {
		LOG.debug "findGroupsByUser (${userId})"
		def user = getUserDomainClass()."findBy${getUsernameClassName()}"(userId)
		def groups = user?.authorities.toList()
		return groups
	}
	
	GroupQuery createNewGroupQuery() {
		return new GroupQueryImpl(Context.getProcessEngineConfiguration().getCommandExecutorTxRequired())
	}
	
	List<Group> findGroupByQueryCriteria(Object query, Page page) {
		LOG.debug "findGroupByQueryCriteria (${query.class.name}, $page)"
		List<Group> groups
		String queryString = createGroupQueryString(query)
		LOG.debug "queryString = $queryString"
		if (page) { // listPage()
			groups = getGroupJoinDomainClass().findAll(queryString, [offset:page.firstResult, max:page.maxResults]).collect{it.userGroup}
		} else { // list()
			groups = getGroupJoinDomainClass().findAll(queryString, Collections.emptyMap()).collect{it."${GNU.getPropertyName(getGroupDomainClassName())}"}
		}
		return groups
	}
	
	long findGroupCountByQueryCriteria(Object query) {
		LOG.debug "findGroupCountByQueryCriteria (${query.class.name})"
		String queryString = createGroupQueryString(query)
		LOG.debug "queryString = $queryString"
		return getGroupJoinDomainClass().executeQuery("select count(g) ${queryString}")[0]
	}
	
	private String createGroupQueryString(Object query) {
		FastStringWriter queryString = new FastStringWriter()
		queryString << "from ${getGroupJoinDomainClassName()} as g"
		String groupPropertyName = GNU.getPropertyName(getGroupDomainClassName())
		if (query.id)
			queryString << " where g.${groupPropertyName}.id='${query.id}'"
		
		if (query.name) {
			queryString << appendWhereOrAnd(queryString)
			queryString << "g.${groupPropertyName}.name = '${query.name}'"
		}
		
		if (query.nameLike) {
			queryString << appendWhereOrAnd(queryString)
			queryString << "g.${groupPropertyName}.name like '${query.nameLike}'"
		}
		
		if (query.type) {
			queryString << appendWhereOrAnd(queryString)
			queryString << "g.${groupPropertyName}.type = '${query.type}'"
		}
		
		if (query.userId) {
			queryString << appendWhereOrAnd(queryString)
			queryString << "g.${GNU.getPropertyName(getUserDomainClassName())}.id = '${query.userId}'"
		}
		
		if (query.orderBy) {
			String orderBy = query.orderBy.toLowerCase().replace('_', '')
			orderBy = orderBy.replace('g', "g.${groupPropertyName}")
			queryString << " order by ${orderBy}"
		}
		return queryString.toString()
	}
	/* Membership */
	void createMembership(String userId, String groupId) {
		throw new UnsupportedOperationException("Please use ${getGroupJoinDomainClassName()}.create() to create membership.")
	}
	void deleteMembership(String userId, String groupId) {
		throw new UnsupportedOperationException("Please use ${getGroupJoinDomainClassName()}.remove() to delete membership.")
	}
	
	private String appendWhereOrAnd(FastStringWriter queryString) {
		return queryString.value.indexOf("where") > -1? " and ": " where "
	}
	
	void deleteUserInfoByUserIdAndKey(String userId, String key) {
		throw new UnsupportedOperationException()
	}
	
	void deleteIdentityInfo(IdentityInfoEntity identityInfo) {
		throw new UnsupportedOperationException()
	}
	
	IdentityInfoEntity findUserAccountByUserIdAndKey(String userId, String userPassword, String key) {
		throw new UnsupportedOperationException()
	}
	
	void setUserInfo(String userId, String userPassword, String type, String key, String value, String accountPassword, Map<String, String> accountDetails) {
		throw new UnsupportedOperationException()
	}
	
	byte[] encryptPassword(String accountPassword, String userPassword) {
		throw new UnsupportedOperationException()
	}
	
	String decryptPassword(byte[] storedPassword, String userPassword) {
		throw new UnsupportedOperationException()
	}
	
	IdentityInfoEntity findUserInfoByUserIdAndKey(String userId, String key) {
		throw new UnsupportedOperationException()
	}
	
	List<String> findUserInfoKeysByUserIdAndType(String userId, String type) {
		throw new UnsupportedOperationException()
	}
	
	
	// Session's methods
	void flush() {
	}
	void close() {
	}
}
