package ch.so.agi.meta.shared.model;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative=true, namespace=JsPackage.GLOBAL, name="Object")
public class ModelClass {
    public String className;
    
    public String classDescripion;
    
    public ModelAttribute[] modelAttributes;
}
