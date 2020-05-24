package ch.so.agi.meta.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ConfigServiceAsync {
    void configServer(AsyncCallback<ConfigResponse> callback)
            throws IllegalArgumentException;
}
