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
 * Enum for types of transformers (java/smooks/xslt/json/jaxb).
 */
public enum TransformerTypes {
	JAVA("java"), SMOOKS("smooks"), XSLT("xslt"), JSON("json"), JAXB("jaxb");
	
	String type;
	  
	/**
	 * Returns enum value from the String representation.
	 * @param typeStr type string
	 * @return enum value
	 */
	static TransformerTypes fromString(String typeStr) {
		if (JAVA.toString().equalsIgnoreCase(typeStr)) {
			return JAVA;
		} else if (SMOOKS.toString().equalsIgnoreCase(typeStr)) {
			return SMOOKS;
		} else if (XSLT.toString().equalsIgnoreCase(typeStr)) {
			return XSLT;
		} else if (JSON.toString().equalsIgnoreCase(typeStr)) {
			return JSON;
		} else if (JAXB.toString().equalsIgnoreCase(typeStr)) {
			return JAXB;
		} else {
			return null;
		}
	}
	    
	/**
	 * Constructor method.
	 * @param type type
	 */
	private TransformerTypes(String type) {
		this.type = type;
	}
	    
	/**
	 * Return the string representation of the enum value.
	 * @return type
	 */
	public String getType() {
		return type;
	}
}
