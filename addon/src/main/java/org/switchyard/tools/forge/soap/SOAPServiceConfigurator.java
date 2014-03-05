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
package org.switchyard.tools.forge.soap;

import javax.inject.Inject;

import org.jboss.forge.addon.projects.Project;
import org.switchyard.common.net.SocketAddr;
import org.switchyard.component.soap.PortName;
import org.switchyard.component.soap.config.model.SOAPBindingModel;
import org.switchyard.component.soap.config.model.SOAPNamespace;
import org.switchyard.component.soap.config.model.v1.V1SOAPBindingModel;

import org.switchyard.config.model.composite.CompositeReferenceModel;
import org.switchyard.config.model.composite.CompositeServiceModel;
import org.switchyard.config.model.composite.InterfaceModel;
import org.switchyard.config.model.composite.v1.V1InterfaceModel;

import org.switchyard.tools.forge.plugin.SwitchYardFacet;

/**
 * Forge commands related to SOAP bindings.
 */
public class SOAPServiceConfigurator {
    @Inject
    private Project _project;
    
    /**
     * Add a SOAP binding to a SwitchYard service.
     * @param serviceName name of the reference to bind
     * @param wsdlLocation location of the WSDL to configure the SOAP binding
     * @param socketAddr optional value for the ip+port
     * @param portType optional value for interface portType
     * @param out shell output
     */
    public void bindService(Project project, 
    		final String serviceName,
            final String wsdlLocation,
            final String socketAddr,
            final String portType) {
        
        SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
        CompositeServiceModel service = switchYard.getCompositeService(serviceName);
        
        if (portType != null) {
            InterfaceModel intf = new V1InterfaceModel(InterfaceModel.WSDL);
            intf.setInterface(wsdlLocation + "#wsdl.porttype(" + portType + ")");
            service.setInterface(intf);
        }
        
        SOAPBindingModel binding = new V1SOAPBindingModel(SOAPNamespace.DEFAULT.uri());
        binding.setWsdl(wsdlLocation);
        if (socketAddr != null) {
            binding.setSocketAddr(new SocketAddr(socketAddr));
        }
        service.addBinding(binding);

        switchYard.saveConfig();
    }
    

    /**
     * Add a SOAP binding to a SwitchYard reference.
     * @param referenceName name of the reference to bind
     * @param wsdlLocation location of the WSDL to configure the SOAP binding
     * @param portName optional value for the endpoint port
     * @param portType optional value for interface portType
     * @param out shell output
     */
    public void bindReference(
    		Project project,
            final String referenceName,
            final String wsdlLocation,
            final String portName,
            final String portType) {
        
        SwitchYardFacet switchYard = project.getFacet(SwitchYardFacet.class);
        CompositeReferenceModel reference = switchYard.getCompositeReference(referenceName);
        // Check to see if the service is public
        if (reference == null) {
            //out.println(out.renderColor(ShellColor.RED, "No public reference named: " + referenceName));
            return;
        }

        if (portType != null) {
            InterfaceModel intf = new V1InterfaceModel(InterfaceModel.WSDL);
            intf.setInterface(wsdlLocation + "#wsdl.porttype(" + portType + ")");
            reference.setInterface(intf);
        }

        SOAPBindingModel binding = new V1SOAPBindingModel(SOAPNamespace.DEFAULT.uri());
        binding.setWsdl(wsdlLocation);
        if (portName != null) {
            binding.setPort(new PortName(portName));
        }
        reference.addBinding(binding);

        switchYard.saveConfig();
    }
}
