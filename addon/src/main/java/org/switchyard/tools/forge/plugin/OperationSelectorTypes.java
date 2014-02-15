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
 * Enum representing operation selector types.
 */
public enum OperationSelectorTypes {
	STATIC("static"), XPATH("xpath"), REGEX("regex"), JAVA("java");
	
	String type;
	  
	/**
	 * Return the OperationSelectorType for the string parameter.
	 * @param typeStr type string
	 * @return enum value
	 */
	static OperationSelectorTypes fromString(String typeStr) {
		if (STATIC.toString().equalsIgnoreCase(typeStr)) {
			return STATIC;
		} else if (XPATH.toString().equalsIgnoreCase(typeStr)) {
			return XPATH;
		} else if (REGEX.toString().equalsIgnoreCase(typeStr)) {
			return REGEX;
		} else if (JAVA.toString().equalsIgnoreCase(typeStr)) {
			return JAVA;
		} else {
			return null;
		}
	}
	    
	/**
	 * Constructor method.
	 * @param type type
	 */
	private OperationSelectorTypes(String type) {
		this.type = type;
	}
	    
	/**
	 * Get the String representation.
	 * @return type
	 */
	public String getType() {
		return type;
	}
}
