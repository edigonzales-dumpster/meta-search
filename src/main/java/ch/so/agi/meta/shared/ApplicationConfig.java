package ch.so.agi.meta.shared;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.gwt.user.client.rpc.IsSerializable;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app")
public class ApplicationConfig implements IsSerializable {
    private String myVar;
    
    public ApplicationConfig() {}

    public String getMyVar() {
        return myVar;
    }

    public void setMyVar(String myVar) {
        this.myVar = myVar;
    }
}
