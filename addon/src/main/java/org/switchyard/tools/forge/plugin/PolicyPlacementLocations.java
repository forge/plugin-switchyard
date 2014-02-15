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
 * Enum representing the different types of policy placement locations.
 */
public enum PolicyPlacementLocations {
    IMPLEMENTATION("implementation"), SERVICE("service"), REFERENCE("reference");

    String location;
    
    /**
     * Return the enum value for the string.
     * @param locationStr location string
     * @return enum value
     */
    static PolicyPlacementLocations fromString(String locationStr) {
        if (IMPLEMENTATION.toString().equalsIgnoreCase(locationStr)) {
            return IMPLEMENTATION;
        } else if (SERVICE.toString().equalsIgnoreCase(locationStr)) {
            return SERVICE;
        } else if (REFERENCE.toString().equalsIgnoreCase(locationStr)) {
        	return REFERENCE;
        } else {
            return null;
        }
    }
    
    /**
     * Constructor method.
     * @param location location
     */
    private PolicyPlacementLocations(String location)
    {
       this.location = location;
    }
    
    /**
     * Return the String representation of the enum.
     * @return String representation
     */
    public String getLocation()
    {
       return location;
    }
}
