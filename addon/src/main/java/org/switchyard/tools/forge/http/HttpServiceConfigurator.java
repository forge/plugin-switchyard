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
package org.switchyard.tools.forge.http;

import javax.inject.Inject;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.switchyard.component.http.config.model.BasicAuthModel;
import org.switchyard.component.http.config.model.HttpBindingModel;
import org.switchyard.component.http.config.model.HttpNamespace;
import org.switchyard.component.http.config.model.NtlmAuthModel;
import org.switchyard.component.http.config.model.v1.V1BasicAuthModel;
import org.switchyard.component.http.config.model.v1.V1HttpBindingModel;
import org.switchyard.component.http.config.model.v1.V1NtlmAuthModel;
import org.switchyard.config.model.composite.CompositeReferenceModel;
import org.switchyard.config.model.composite.CompositeServiceModel;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

/**
 * Forge commands related to HTTP bindings.
 */
public class HttpServiceConfigurator {
	
    /**
     * Add a HTTP binding to a SwitchYard service.
     * @param project project 
     * @param serviceName service name
     * @param contextPath context path
     */
    public void bindService(Project project,
            final String serviceName,
            final String contextPath) {

        SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
        CompositeServiceModel service = switchYard.getCompositeService(serviceName);
        // Check to see if the service is public
        if (service == null) {
            //out.println(out.renderColor(ShellColor.RED, "No public service named: " + serviceName));
            return;
        }

        HttpBindingModel binding = new V1HttpBindingModel(HttpNamespace.DEFAULT.uri());
        String projectName = project.getFacet(MetadataFacet.class).getProjectName();
        if ((contextPath != null) && contextPath.length() > 0) {
            binding.setContextPath(contextPath);
        } else {
            binding.setContextPath(projectName);
        }
        service.addBinding(binding);

        switchYard.saveConfig();
    }
    
    /**
     * Add a HTTP binding to a SwitchYard reference
     * @param project project
     * @param referenceName name of the reference to be bound
     * @param address required value of the root url where the HTTP endpoints are hosted
     * @param method optional value of HTTP method used to invoke the endpoint
     * @param contentType optional value of content type of the request body
     * @param user optional value of user name
     * @param password optional value of password
     * @param realm optional value of authentication realm
     * @param domain optional value of authentication domain
     */
    public void bindReference(
    		Project project,
            final String referenceName,
            final String address,
            final String method,
            final String contentType,
            final String user,
            final String password,
            final String realm,
            final String domain) {
        SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
        CompositeReferenceModel reference = switchYard.getCompositeReference(referenceName);
        // Check to see if the service is public
        if (reference == null) {
//            out.println(out.renderColor(ShellColor.RED, "No public reference named: " + referenceName));
            return;
        }

        HttpBindingModel binding = new V1HttpBindingModel(HttpNamespace.DEFAULT.uri());
        if (address != null) {
            binding.setAddress(address);
        }
        if (method != null) {
            binding.setMethod(method);
        }
        if (contentType != null) {
            binding.setContentType(contentType);
        }
        if (domain != null) {
            NtlmAuthModel ntlm= new V1NtlmAuthModel(HttpNamespace.DEFAULT.uri());
            ntlm.setUser(user);
            ntlm.setPassword(password);
            ntlm.setRealm(realm);
            ntlm.setDomain(domain);
            binding.setNtlmAuthConfig(ntlm);
        } else {
            BasicAuthModel basic= new V1BasicAuthModel(HttpNamespace.DEFAULT.uri());
            basic.setUser(user);
            basic.setPassword(password);
            basic.setRealm(realm);
            binding.setBasicAuthConfig(basic);
        }
        reference.addBinding(binding);

        switchYard.saveConfig();

    }
}
