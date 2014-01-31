package org.switchyard.tools.forge.http;


import javax.inject.Inject;

import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.switchyard.component.http.config.model.BasicAuthModel;
import org.switchyard.component.http.config.model.HttpBindingModel;
import org.switchyard.component.http.config.model.NtlmAuthModel;
import org.switchyard.component.http.config.model.v1.V1BasicAuthModel;
import org.switchyard.component.http.config.model.v1.V1HttpBindingModel;
import org.switchyard.component.http.config.model.v1.V1NtlmAuthModel;
import org.switchyard.config.model.composite.CompositeReferenceModel;
import org.switchyard.config.model.composite.CompositeServiceModel;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

public class HttpServiceConfigurator {
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

        HttpBindingModel binding = new V1HttpBindingModel();
        String projectName = project.getFacet(MetadataFacet.class).getProjectName();
        if ((contextPath != null) && contextPath.length() > 0) {
            binding.setContextPath(contextPath);
        } else {
            binding.setContextPath(projectName);
        }
        service.addBinding(binding);

        switchYard.saveConfig();
    }
    
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

        HttpBindingModel binding = new V1HttpBindingModel();
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
            NtlmAuthModel ntlm= new V1NtlmAuthModel();
            ntlm.setUser(user);
            ntlm.setPassword(password);
            ntlm.setRealm(realm);
            ntlm.setDomain(domain);
            binding.setNtlmAuthConfig(ntlm);
        } else {
            BasicAuthModel basic= new V1BasicAuthModel();
            basic.setUser(user);
            basic.setPassword(password);
            basic.setRealm(realm);
            binding.setBasicAuthConfig(basic);
        }
        reference.addBinding(binding);

        switchYard.saveConfig();

    }
}
