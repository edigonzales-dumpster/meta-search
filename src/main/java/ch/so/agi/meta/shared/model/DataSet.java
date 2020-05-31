package ch.so.agi.meta.shared.model;

import jsinterop.annotations.JsType;
import jsinterop.annotations.JsPackage;

@JsType(isNative=true, namespace=JsPackage.GLOBAL, name="Object")
public class DataSet {
    public String id;
    
    public String title;
    
    public String shortDescription;
    
    public String keywords;
    
    public String original;
    
    public String model;
    
    public String modelRepository;
    
    public String furtherInformation;
    
    public String furtherMetadata;
    
    public String knownWMS;
    
    // Geht das nicht besser?
    public String lastEditingDate;
    
    public double westLimit;
    
    public double southLimit;
    
    public double eastLimit;
    
    public double northLimit;
    
    public DataSetFile[] files;

    public DataSet() {}    
}
