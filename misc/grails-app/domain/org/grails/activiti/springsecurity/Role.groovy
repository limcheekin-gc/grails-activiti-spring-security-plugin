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

/**
 *
 * @author <a href='mailto:limcheekin@vobject.com'>Lim Chee Kin</a>
 *
 * @since 0.1
 */
class Role implements org.activiti.engine.identity.Group {
	String id
	String name
	String authority
	String description
	
	static belongsTo = [parent: Role]
	static hasMany = [children: Role]
	
	static mapping = { 
		cache true
		id generator: 'uuid'
	}
	
	static constraints = {
		authority blank: false, unique: true
		name blank: false
		description nullable:true
		parent nullable:true
	}
	
	def beforeDelete = { UserRole.removeAll(this) }	
	
	static transients = ["type"]
	
	public String getType() {
		return null
	}
	
	public void setType(String type) {
		throw new UnsupportedOperationException("Group type is not implemented! Please contact the developer if you need it.")
	}
}
