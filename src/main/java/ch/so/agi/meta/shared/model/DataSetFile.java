package ch.so.agi.meta.shared.model;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative=true, namespace=JsPackage.GLOBAL, name="Object")
public class DataSetFile {
    public String format;
    
    public String type;
    
    public String location;

    public DataSetFile() {}
}
