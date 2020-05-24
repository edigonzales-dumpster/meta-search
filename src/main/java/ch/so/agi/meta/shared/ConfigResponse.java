package ch.so.agi.meta.shared;

import java.util.List;
import com.google.gwt.user.client.rpc.IsSerializable;

public class ConfigResponse implements IsSerializable {
    private List<BackgroundMapConfig> backgroundMaps;
    
    public List<BackgroundMapConfig> getBackgroundMaps() {
        return backgroundMaps;
    }

    public void setBackgroundMaps(List<BackgroundMapConfig> backgroundMaps) {
        this.backgroundMaps = backgroundMaps;
    }
}
