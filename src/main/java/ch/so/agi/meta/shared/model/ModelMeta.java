package ch.so.agi.meta.shared.model;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative=true, namespace=JsPackage.GLOBAL, name="Object")
public class ModelMeta {
    public String name;
    
    public String version;
    
    public String derivedModel;
    
    public ModelClass[] modelClasses;
}
