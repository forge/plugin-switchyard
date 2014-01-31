package org.switchyard.tools.forge.camel;

public enum RouteType {
    JAVA("java"), XML("xml");

    String type;
    
    static RouteType fromString(String typeStr) {
        if (JAVA.toString().equalsIgnoreCase(typeStr)) {
            return JAVA;
        } else if (XML.toString().equalsIgnoreCase(typeStr)) {
            return XML;
        } else {
            return null;
        }
    }
    
    private RouteType(String type)
    {
       this.type = type;
    }
    public String getType()
    {
       return type;
    }
}
