plugin-switchyard
=================

SwitchYard Forge Plugin

SwitchYard integrates with JBoss Forge to provide a set of rapid application development tools for service-oriented applications.&nbsp; Please consult the Getting Started guide for information on how to install Forge and the SwitchYard extensions to Forge.

h3. Creating a Project

The first thing you'll want to do with Forge is create a new project.&nbsp; This can be done inside the Forge shell using the new-project command.

{code}
switchyard$ forge

    _____                    
   |  ___|__  _ __ __ _  ___ 
   | |_ / _ \| `__/ _` |/ _ \  \\
   |  _| (_) | | | (_| |  __/  //
   |_|  \___/|_|  \__, |\___| 
                   |___/      

JBoss Forge, version [ 2.0.0.Final ] - JBoss, by Red Hat, Inc. [ http://forge.jboss.org ]
[switchyard]$ project-new --named syApp --topLevelPackage org.switchyard.examples.forge
***SUCCESS*** Project named 'syApp' has been created.

{code}
At this point, you have an empty application with a few Maven facets installed.&nbsp; What's a facet you ask?&nbsp; Read on ....

h3. Facets

Facets add capabilities to an application and to the forge environment itself.&nbsp; This allows SwitchYard to add  dependencies to your application's pom based on the functionality you will be  using, instead of sticking every possible SwitchYard dependency in the  application by default.&nbsp; Facets are also used to add commands specific to SwitchYard itself and components which you will be using in your  application.&nbsp; The following facets are currently available:
* *switchyard* \- core set of commands and dependencies for the SwitchYard runtime
* *switchyard.bean* \- commands and dependencies for Bean component services
* *switchyard.bpm*&nbsp;\- commands and dependencies for BPM component services
* *switchyard.rules*&nbsp;\- commands and dependencies for Rules component services
* *switchyard.soap* \- commands and dependencies for SOAP gateway bindings
* *switchyard.camel* \- commands and dependencies for Camel services and gateway bindings
* *switchyard.rest*&nbsp;\- commands and dependencies for RESTEasy gateway bindings
* *switchyard.http*&nbsp;\- commands and dependencies for HTTP gateway bindings

h3. Commands

The following SwitchYard commands are available in Forge (grouped by facet).

*{_}switchyard{_}*
* {{switchyard-show-confi{}}}g : displays the current state of your application's configuration, including services, references, and bindings.
* {{switchyard-promote-service}} : promotes an internal application-scoped service to be visible to other applications.
* s{{{}witchyard-promote-reference}} : promotes an internal application-scoped reference so that it can be mapped to services provided in other applications.
* s{{{}witchyard-create-service-test}}&nbsp;: create a new unit test for a service.
* s{{{}witchyard-get-version}}&nbsp;: returns the version of SwitchYard used by the application.
* s{{{}witchyard-trace-messages}}&nbsp;: enables message tracing at runtime - all messages will be logged.
* s{{{}witchyard-import-artifacts}}&nbsp;: add a dependency on a service artifact module to the application's configuration
* s{{{}witchyard-add-operation-selector}}&nbsp;: add an operation selector to a service binding.
* s{{{}witchyard-add-policy}}&nbsp;: add a required policy on a component service or reference.
* s{{{}witchyard-add-reference}}&nbsp;: adds a reference to a service implementation; particularly useful for service types which do not autogenerate references (Camel, BPM, BPEL).
* s{{{}witchyard-add-transformer}}&nbsp;: adds a Transformer from one message format to another.
* s{{{}witchyard-add-validator}}&nbsp;: adds a message validator to a service implementation.

*{_}switchyard-bean{_}*
* {{switchyard-bean-service-create}} : creates a new CDI Bean service, consisting of a service interface and implementation class.
* {{switchyard-bean-reference-create}} : creates a new CDI Bean reference.

*{_}switchyard-bpm{_}*
* {{switchyard-bpm-service-create}}&nbsp;: creates a new BPM service, consisting of a service interface and implementation class.

*{_}switchyard-rules{_}*
* {{switchyard-rules-service create}}&nbsp;: creates a new Rules service, consisting of a service interface and implementation class.

*{_}switchyard-camel{_}*
* {{switchayrd-camel-service-create}} : creates a new XML or Java DSL Camel route.
* {{switchyard-camel-bind-service}} : binds a service using a Camel endpoint URI.
* {{switchyard-camel-bind-reference}} : binds a reference using a Camel endpoint URI.

*{_}switchyard-soap{_}*
* {{switchyard-soap-bind-service}} : binds a service to a SOAP endpoint.
* {{switchyard-soap-bind-reference}} : binds a reference to a SOAP endpoint.

*{_}switchyard-rest{_}*
* {{switchyard-rest-bind-service}}&nbsp;: binds a service to a RESTEasy endpoint.
* {{switchyard-rest-bind-reference}}&nbsp;: binds a reference to a RESTEasy endpoint.

*{_}switchyard-http{_}*
* {{switchyard-http-bind-service}}&nbsp;: binds a service to a HTTP endpoint.
* {{switchyard-http-bind-reference}}&nbsp;: binds a reference to a HTTP endpoint.

