/*
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.switchyard.tools.forge.plugin;

/**
 * Enum representing the different types of validators (Java/XML).
 */
public enum ValidatorTypes {
	JAVA("java"), XML("xml");
	
	String type;
	  
	/**
	 * Returns enum value from the string representation.
	 * @param typeStr typeStr
	 * @return enum value
	 */
	static ValidatorTypes fromString(String typeStr) {
		if (JAVA.toString().equalsIgnoreCase(typeStr)) {
			return JAVA;
		} else if (XML.toString().equalsIgnoreCase(typeStr)) {
			return XML;
		} else {
			return null;
		}
	}
	    
	/**
	 * Constructor method
	 * @param type type
	 */
	private ValidatorTypes(String type) {
		this.type = type;
	}
	    
	/**
	 * Returns the String representation of the enum.
	 * @return type
	 */
	public String getType() {
		return type;
	}
}
