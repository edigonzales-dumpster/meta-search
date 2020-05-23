package ch.so.agi.meta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import ch.interlis.ili2c.config.Configuration;
import ch.interlis.ili2c.config.FileEntry;
import ch.interlis.ili2c.config.FileEntryKind;
import ch.interlis.ili2c.gui.UserSettings;
import ch.interlis.ilirepository.Dataset;

@Controller
public class MainController {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    @GetMapping("/")
    public ResponseEntity<String> ping() {
        return new ResponseEntity<String>("meta search", HttpStatus.OK);
    }
    
    @GetMapping("/data")
    public ResponseEntity<String> ilidata() {
        UserSettings settings = new UserSettings();
        
        // Repositories. Falls nichts gesetzt wird, werden die Standardrepos verwendet (TODO prüfen).
        // Ist das überhaupt notwendig, wenn ich listData machen will?
        //settings.setIlidirs("https://geo.so.ch/models;http://models.interlis.ch");

        //
        Configuration config = new Configuration();
        ArrayList ilifilev = new ArrayList();
        ilifilev.add("http://models.geo.gl.ch");
        Iterator ilifilei = ilifilev.iterator();
        while (ilifilei.hasNext()) {
            String ilifile = (String) ilifilei.next();
            FileEntry file = new FileEntry(ilifile, FileEntryKind.ILIMODELFILE);
            config.addFileEntry(file);
        }

        
        List<Dataset> datasets = new ListData().listData(config, settings);
        for (Dataset dataset : datasets) {
            logger.info(dataset.getMetadata().getid());
            logger.info(dataset.getMetadata().getfiles()[0].toString());
        }
        
        
        return new ResponseEntity<String>("data", HttpStatus.OK);
    }

}
