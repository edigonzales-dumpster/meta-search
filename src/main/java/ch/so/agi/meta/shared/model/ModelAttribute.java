package ch.so.agi.meta.shared.model;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative=true, namespace=JsPackage.GLOBAL, name="Object")
public class ModelAttribute {
    public String attributeName;
    
    public String attributeType;
    
    public boolean mandatory;
    
    public String attributeDescription;
}
