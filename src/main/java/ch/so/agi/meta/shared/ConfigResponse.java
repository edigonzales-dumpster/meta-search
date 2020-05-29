package ch.so.agi.meta.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ConfigResponse implements IsSerializable {
    private String myVar;

    public String getMyVar() {
        return myVar;
    }

    public void setMyVar(String myVar) {
        this.myVar = myVar;
    }
}
