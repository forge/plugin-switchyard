package org.switchyard.tools.forge.camel;

public enum InterfaceType {
    WSDL("wsdl"), JAVA("java");
    
    String type;
    
    static InterfaceType fromString(String typeStr) {
        if (WSDL.toString().equalsIgnoreCase(typeStr)) {
            return WSDL;
        } else if (JAVA.toString().equalsIgnoreCase(typeStr)) {
            return JAVA;
        } else {
            return null;
        }
    }
    
    private InterfaceType(String type)
    {
       this.type = type;
    }
    public String getType()
    {
       return type;
    }
}
