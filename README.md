plugin-switchyard
=================

### SwitchYard Forge Plugin

SwitchYard integrates with JBoss Forge to provide a set of rapid application development tools for service-oriented applications.&nbsp; Please consult the Getting Started guide for information on how to install Forge and the SwitchYard extensions to Forge.

### Creating a Project

The first thing you'll want to do with Forge is create a new project.&nbsp; This can be done inside the Forge shell using the new-project command.

<code>
switchyard$ forge

JBoss Forge, version [ 2.0.0.Final ] - JBoss, by Red Hat, Inc. [ http://forge.jboss.org ]
[switchyard]$ project-new --named syApp --topLevelPackage org.switchyard.examples.forge
***SUCCESS*** Project named 'syApp' has been created.

</code>
At this point, you have an empty application with a few Maven facets installed.&nbsp; What's a facet you ask?&nbsp; Read on ....

### Facets

Facets add capabilities to an application and to the forge environment itself.&nbsp; This allows SwitchYard to add  dependencies to your application's pom based on the functionality you will be  using, instead of sticking every possible SwitchYard dependency in the  application by default.&nbsp; Facets are also used to add commands specific to SwitchYard itself and components which you will be using in your  application.&nbsp; The following facets are currently available:

* *switchyard* \- core set of commands and dependencies for the SwitchYard runtime
* *switchyard.bean* \- commands and dependencies for Bean component services
* *switchyard.bpm*&nbsp;\- commands and dependencies for BPM component services
* *switchyard.rules*&nbsp;\- commands and dependencies for Rules component services
* *switchyard.soap* \- commands and dependencies for SOAP gateway bindings
* *switchyard.camel* \- commands and dependencies for Camel services and gateway bindings
* *switchyard.rest*&nbsp;\- commands and dependencies for RESTEasy gateway bindings
* *switchyard.http*&nbsp;\- commands and dependencies for HTTP gateway bindings

### Commands

The following SwitchYard commands are available in Forge (grouped by facet).

### switchyard

* <i>switchyard-show-config</i> : displays the current state of your application's configuration, including services, references, and bindings.
* <i>switchyard-promote-service</i> : promotes an internal application-scoped service to be visible to other applications.
* <i>switchyard-promote-reference</i> : promotes an internal application-scoped reference so that it can be mapped to services provided in other applications.
* <i>switchyard-create-service-test</i>&nbsp;: create a new unit test for a service.
* <i>switchyard-get-version</i>&nbsp;: returns the version of SwitchYard used by the application.
* <i>switchyard-trace-messages</i>&nbsp;: enables message tracing at runtime - all messages will be logged.
* <i>switchyard-import-artifacts</i>&nbsp;: add a dependency on a service artifact module to the application's configuration
* <i>switchyard-add-operation-selector</i>&nbsp;: add an operation selector to a service binding.
* <i>switchyard-add-policy</i>&nbsp;: add a required policy on a component service or reference.
* <i>switchyard-add-reference</i>&nbsp;: adds a reference to a service implementation; particularly useful for service types which do not autogenerate references (Camel, BPM, BPEL).
* <i>switchyard-add-transformer</i>&nbsp;: adds a Transformer from one message format to another.
* <i>switchyard-add-validator</i>&nbsp;: adds a message validator to a service implementation.

### switchyard-bean
* <i>switchyard-bean-service-create</i> : creates a new CDI Bean service, consisting of a service interface and implementation class.
* <i>switchyard-bean-reference-create</i> : creates a new CDI Bean reference.

### switchyard-bpm
* <i>switchyard-bpm-service-create</i>&nbsp;: creates a new BPM service, consisting of a service interface and implementation class.

### switchyard-rules
* <i>switchyard-rules-service create</i>&nbsp;: creates a new Rules service, consisting of a service interface and implementation class.

### switchyard-camel
* <i>switchayrd-camel-service-create</i> : creates a new XML or Java DSL Camel route.
* <i>switchyard-camel-bind-service</i> : binds a service using a Camel endpoint URI.
* <i>switchyard-camel-bind-reference</i> : binds a reference using a Camel endpoint URI.

### switchyard-soap
* <i>switchyard-soap-bind-service</i> : binds a service to a SOAP endpoint.
* <i>switchyard-soap-bind-reference</i> : binds a reference to a SOAP endpoint.

### switchyard-rest
* <i>switchyard-rest-bind-service</i>&nbsp;: binds a service to a RESTEasy endpoint.
* <i>switchyard-rest-bind-reference</i>&nbsp;: binds a reference to a RESTEasy endpoint.

### switchyard-http
* <i>switchyard-http-bind-service</i>&nbsp;: binds a service to a HTTP endpoint.
* <i>switchyard-http-bind-reference</i>&nbsp;: binds a reference to a HTTP endpoint.
