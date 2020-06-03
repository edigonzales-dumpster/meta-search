package ch.so.agi.meta.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.w3c.tidy.Tidy;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.Ili2c;
import ch.interlis.ili2c.Ili2cException;
import ch.interlis.ili2c.config.Configuration;
import ch.interlis.ili2c.config.FileEntry;
import ch.interlis.ili2c.config.FileEntryKind;
import ch.interlis.ili2c.gui.UserSettings;
import ch.interlis.ili2c.metamodel.AreaType;
import ch.interlis.ili2c.metamodel.AssociationDef;
import ch.interlis.ili2c.metamodel.AttributeDef;
import ch.interlis.ili2c.metamodel.CompositionType;
import ch.interlis.ili2c.metamodel.Enumeration;
import ch.interlis.ili2c.metamodel.EnumerationType;
import ch.interlis.ili2c.metamodel.NumericType;
import ch.interlis.ili2c.metamodel.SurfaceOrAreaType;
import ch.interlis.ili2c.metamodel.Table;
import ch.interlis.ili2c.metamodel.TextType;
import ch.interlis.ili2c.metamodel.Topic;
import ch.interlis.ili2c.metamodel.Type;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.metamodel.Viewable;
import ch.interlis.ili2c.modelscan.IliFile;
import ch.interlis.ili2c.modelscan.IliModel;
import ch.interlis.ilirepository.Dataset;
import ch.interlis.ilirepository.IliFiles;
import ch.interlis.ilirepository.IliManager;
import ch.interlis.ilirepository.impl.RepositoryAccess;
import ch.interlis.ilirepository.impl.RepositoryAccessException;
import ch.interlis.ilirepository.impl.RepositoryCrawler;
import ch.interlis.iom_j.itf.ModelUtilities;
import ch.interlis.models.DatasetIdx16.DataFile;
import ch.interlis.models.IliRepository20.RepositoryIndex.ModelMetadata;
import ch.so.agi.meta.shared.model.DataSet;
import ch.so.agi.meta.shared.model.DataSetFile;
import ch.so.agi.meta.shared.model.ModelAttribute;
import ch.so.agi.meta.shared.model.ModelClass;
import ch.so.agi.meta.shared.model.ModelMeta;
import elemental2.core.JsDate;

@RestController
public class MainController {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private static final String METAATTR_MAPPING = "ili2db.mapping";
    private static final String METAATTR_MAPPING_MULTISURFACE = "MultiSurface";
    private static final String UTF_8 = "UTF-8";

    @Autowired
    private SpringTemplateEngine springTemplateEngine;
    
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return new ResponseEntity<String>("meta search", HttpStatus.OK);
    }
    
    @GetMapping("/model/pdf/{model}")
    public ResponseEntity<?> getModelAsPdf(@PathVariable String model) throws FileNotFoundException, IOException {
        File tmpFolder = Files.createTempDirectory("metaclientws-").toFile();
        if (!tmpFolder.exists()) {
            tmpFolder.mkdirs();
        }
        logger.info("tmpFolder {}", tmpFolder.getAbsolutePath());

//        File htmlFile = new File(Paths.get(tmpFolder.getAbsolutePath(), "helloworld.html").toFile().getAbsolutePath());
//        InputStream htmlFileInputStream = MainController.class.getResourceAsStream("/helloworld.html"); 
//        Files.copy(htmlFileInputStream, htmlFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//        htmlFileInputStream.close();
//        
        File pdfFile = new File(Paths.get(tmpFolder.getAbsolutePath(), "helloworld.pdf").toFile().getAbsolutePath());
//        
//        logger.info(htmlFile.toURI().toString());
        
        
//        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
//        templateResolver.setSuffix(".html");
//        templateResolver.setPrefix("templates/");
//        templateResolver.setCacheable(false);
//        templateResolver.setTemplateMode("HTML");
//         
//        TemplateEngine templateEngine = new TemplateEngine();
//        templateEngine.setTemplateResolver(templateResolver);
         
        Context context = new Context();
        context.setVariable("name", "Thomas");
         
        // Get the plain HTML with the resolved ${name} variable!
//        String html = templateEngine.process("fubar", context);
        String html = springTemplateEngine.process("fubar", context);
        String xHtml = convertToXhtml(html);
        logger.info(xHtml);
        
        
        // TODO exception?
        try (OutputStream os = new FileOutputStream(pdfFile.getAbsolutePath())) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
//            builder.withUri(htmlFile.toURI().toString());
            builder.withHtmlContent(xHtml, "");
            builder.toStream(os);
            builder.run();
            
            InputStream is = new java.io.FileInputStream(pdfFile);
            return ResponseEntity
                    .ok().header("content-disposition", "attachment; filename=" + pdfFile.getName())
                    .contentLength(pdfFile.length())
                    .contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(is));                
        }
    }
    
    private String convertToXhtml(String html) throws UnsupportedEncodingException {
        Tidy tidy = new Tidy();
        tidy.setInputEncoding(UTF_8);
        tidy.setOutputEncoding(UTF_8);
        tidy.setXHTML(true);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(html.getBytes(UTF_8));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        tidy.parseDOM(inputStream, outputStream);
        return outputStream.toString(UTF_8);
    }

    
    @GetMapping("/model/{model}")
    public ResponseEntity<ModelMeta> model(@PathVariable String model) throws RepositoryAccessException, Ili2cException, IOException {
        logger.info(model);
        
        // TODO: steht bereits im ilidata.xml (wie transportieren?)
        RepositoryCrawler crawler = new RepositoryCrawler(new RepositoryAccess());
        //String[] repo = new String[] {UserSettings.ILI_REPOSITORY};
        String[] repo = new String[] {"https://s3.eu-central-1.amazonaws.com/ch.so.geo.repository/", "http://models.interlis.ch/"};
        crawler.setRepositories(repo);
        IliFile iliFile = crawler.getIliFileMetadataDeep(model, 2.3, true);
        logger.info(iliFile.getPath());
        logger.info(iliFile.getRepositoryUri());

        String repositoryUri = "https://s3.eu-central-1.amazonaws.com/ch.so.geo.repository/";
        String iliPath = null;
        String iliName = null;
        String iliVersion = null;
        String iliDerivedModel = null;
        
        RepositoryAccess repoAccess = new RepositoryAccess();
        List<ch.ehi.iox.ilisite.IliRepository09.RepositoryIndex.ModelMetadata> modelMetadataList = repoAccess.readIlimodelsXml(repositoryUri);   
        
        for (ch.ehi.iox.ilisite.IliRepository09.RepositoryIndex.ModelMetadata modelMetadata : modelMetadataList) {
            if (modelMetadata.getName().equals(model)) {                
                iliPath = modelMetadata.getFile();
                iliName = modelMetadata.getName();
                iliVersion = modelMetadata.getVersion();
                if (modelMetadata.getderivedModel().length > 0) {
                    iliDerivedModel = modelMetadata.getderivedModel()[0].getvalue();
                }
                break;
            }
        }

        // Download ili file
        HttpURLConnection connection = null;
        int responseCode = 0;
        URL url = new URL(repositoryUri + iliPath);
        logger.info(url.toString());
        
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        responseCode = connection.getResponseCode();
        
        File tmpFolder = Files.createTempDirectory("metasearchws-").toFile();
        if (!tmpFolder.exists()) {
            tmpFolder.mkdirs();
        }
        logger.info("tmpFolder {}", tmpFolder.getAbsolutePath());

        File tmpModelFile = new java.io.File(tmpFolder, iliName + ".ili");
        InputStream initialStream = connection.getInputStream();
        java.nio.file.Files.copy(initialStream, tmpModelFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        initialStream.close();
        logger.info("File downloaded: " + tmpModelFile.getAbsolutePath());  

        // Meta info 
        ModelMeta modelMeta = new ModelMeta();
        modelMeta.name = iliName;
        modelMeta.version = iliVersion;
        modelMeta.derivedModel = iliDerivedModel;
        
        // Get class and attribute description from model
        TransferDescription td = getTransferDescriptionFromFileName(tmpModelFile.getAbsolutePath());

        // TODO
        // Das ist natürlich sehr vereinfacht und noch ziemlich
        // Mumpitz. Enumerations im Modelheader etc. pp.
        
        List<ModelClass> modelClasses = new ArrayList<ModelClass>();
        Iterator modeli = td.getLastModel().iterator();
        while (modeli.hasNext()) {
            Object tObj = modeli.next();
            
            if (tObj instanceof Topic) {
                Topic topic = (Topic) tObj;
                logger.info("Topic: " + topic.getName());
                Iterator iter = topic.getViewables().iterator();
                while (iter.hasNext()) {
                    Object obj = iter.next();
                    logger.info(obj.toString());
                    
                    if (obj instanceof Viewable) {
                        Viewable v = (Viewable) obj;
                        
                        if(isPureRefAssoc(v)){
                            continue;
                        }

                        logger.info(v.getDocumentation());
                        //logger.info(v.getMetaValues().toString());

                        String className = v.getScopedName(null);
                        //logger.info("classname: " + className);
                        logger.info("Class: " + v.getName());
                        
                        ModelClass modelClass = new ModelClass();
                        modelClass.className = topic.getName() + "." + v.getName();
                        modelClass.classDescripion = v.getDocumentation();
                        
                        Iterator attri = v.getAttributes();

                        List<ModelAttribute> modelAttributes =  new ArrayList<ModelAttribute>();
                        while (attri.hasNext()) {
                            Object aObj = attri.next();
                            //logger.info("aObj: " + aObj);
                            if (aObj instanceof AttributeDef) {
                                ModelAttribute modelAttribute = new ModelAttribute();
                                
                                AttributeDef attr = (AttributeDef) aObj;
                                logger.info("Attribut: " + attr.getName());
                                logger.info("Attributbeschreibung: " + attr.getDocumentation());
                                modelAttribute.attributeName = attr.getName();                                
                                modelAttribute.attributeDescription = attr.getDocumentation();
                                
                                Type type = attr.getDomainResolvingAll();  
                                logger.info("Attributtyp: " + type.toString());
                                logger.info("Kardinalität: " + type.getCardinality());
                                
                                modelAttribute.mandatory = type.isMandatory();
                                
                                if (type instanceof TextType) { 
                                    TextType textType = (TextType) type;
                                    logger.info("Attributtyp2: " + "TextType");
                                    // MTEXT isNormalized=false
                                    // TEXT isNormalized=true
                                    logger.info("normalized: " + textType.isNormalized());
                                    
                                    modelAttribute.attributeType = textType.toString();
                                } else if (type instanceof NumericType) {
                                    NumericType numericType = (NumericType) type;
                                    logger.info("Attributtyp2: " + "NumericType");
                                    
                                    modelAttribute.attributeType = numericType.toString();
                                } else if (type instanceof AreaType) {
                                    AreaType areaType = (AreaType) type;
                                    logger.info("Attributtyp2: " + "AreaType");
                                    
                                    modelAttribute.attributeType = "Polygon (AREA)";
                                } else if (type instanceof EnumerationType) {
                                    EnumerationType enumType = (EnumerationType) type;
                                    //logger.info("enumType: " + enumType.toString());
                                    List<String> enumValues = enumType.getValues();
                                    //logger.info(enumType.getValues().toString());
                                    List<Map.Entry<String,ch.interlis.ili2c.metamodel.Enumeration.Element>> ev = new ArrayList<Map.Entry<String,ch.interlis.ili2c.metamodel.Enumeration.Element>>();
                                    ModelUtilities.buildEnumElementList(ev,"",enumType.getConsolidatedEnumeration());
                                    logger.info(ev.toString());
                                    
                                    Iterator<Map.Entry<String,ch.interlis.ili2c.metamodel.Enumeration.Element>> evi = ev.iterator();
                                    while(evi.hasNext()){
                                        java.util.Map.Entry<String,ch.interlis.ili2c.metamodel.Enumeration.Element> ele = evi.next();
                                        String eleName = ele.getKey();
                                        Enumeration.Element eleElement=ele.getValue();
                                        //logger.info("eleName: " + eleName);
                                        //logger.info("eleElement: " + eleElement);
                                        
                                        String description = eleElement.getDocumentation();
                                        //logger.info("description: " + description);
                                        
                                        Settings meta = eleElement.getMetaValues();
                                        //logger.info("meta: " + meta.toString());
                                    }
                                } else if (type instanceof CompositionType) {
                                    CompositionType compositionType = (CompositionType) type;
                                    Table struct = compositionType.getComponentType();
                                    if (struct.getName().equalsIgnoreCase("MultiSurface")) {
                                        logger.info("Attributtyp2: " + "MultiSurface");
                                        modelAttribute.attributeType = "MultiPolygon (Surface)";
                                    }
                                }
                                modelAttributes.add(modelAttribute);
                                logger.info("-------------------------------------");
                            }   
                        }
                        modelClass.modelAttributes = modelAttributes.toArray(new ModelAttribute[0]);
                        modelClasses.add(modelClass);
                    }
                }   
            }
        }
        modelMeta.modelClasses = modelClasses.toArray(new ModelClass[0]);
        return new ResponseEntity<ModelMeta>(modelMeta, HttpStatus.OK);
    }
    
    private TransferDescription getTransferDescriptionFromFileName(String fileName) throws Ili2cException {
        IliManager manager = new IliManager();
        String repositories[] = new String[] { "https://s3.eu-central-1.amazonaws.com/ch.so.geo.repository/", "http://models.interlis.ch/" };
        manager.setRepositories(repositories);
        
        ArrayList<String> ilifiles = new ArrayList<String>();
        ilifiles.add(fileName);
        Configuration config = manager.getConfigWithFiles(ilifiles);
        ch.interlis.ili2c.metamodel.TransferDescription iliTd = Ili2c.runCompiler(config);

        if (iliTd == null) {
            throw new IllegalArgumentException("INTERLIS compiler failed");
        }
        return iliTd;
    }

    public static boolean isPureRefAssoc(Viewable v) {
        if (!(v instanceof AssociationDef)) {
            return false;
        }
        AssociationDef assoc = (AssociationDef) v;
        // embedded and no attributes/embedded links?
        if (assoc.isLightweight() && !assoc.getAttributes().hasNext()
                && !assoc.getLightweightAssociations().iterator().hasNext()) {
            return true;
        }
        return false;
    }
    
    private boolean isMultiSurfaceAttr(TransferDescription td, AttributeDef attr) {
        Type typeo = attr.getDomain();
        logger.info("aaaa");
        if (typeo instanceof CompositionType) {
            logger.info("aaaa");

            CompositionType type = (CompositionType) attr.getDomain();
            logger.info(type.toString());
            if (type.getCardinality().getMaximum() == 1) {
                logger.info("aaaa");

                Table struct = type.getComponentType();
                logger.info(struct.getName());
                logger.info(struct.getMetaValue(METAATTR_MAPPING));
                if (METAATTR_MAPPING_MULTISURFACE.equals(struct.getMetaValue(METAATTR_MAPPING))) {
                    return true;
                }
            }
        }
        return false;
    }

    @GetMapping("/ilidata")
    public ResponseEntity<List<DataSet>> ilidata() throws ParseException {
        UserSettings settings = new UserSettings();
        
        // Repositories. Falls nichts gesetzt wird, werden die Standardrepos verwendet (TODO prüfen).
        // Ist das überhaupt notwendig, wenn ich listData machen will?
        //settings.setIlidirs("https://geo.so.ch/models;http://models.interlis.ch");
        
        Configuration config = new Configuration();
        ArrayList ilifilev = new ArrayList();
        //ilifilev.add("http://models.geo.gl.ch");
        ilifilev.add("https://s3.eu-central-1.amazonaws.com/ch.so.geo.repository");
        Iterator ilifilei = ilifilev.iterator();
        while (ilifilei.hasNext()) {
            String ilifile = (String) ilifilei.next();
            FileEntry file = new FileEntry(ilifile, FileEntryKind.ILIMODELFILE);
            config.addFileEntry(file);
        }

        List<DataSet> dataSets = new ArrayList<DataSet>();
        List<Dataset> datasets = new ListData().listData(config, settings);
        for (Dataset dataset : datasets) {            
            String id = dataset.getMetadata().getid();
            String title = dataset.getMetadata().gettitle().getLocalisedText()[0].getText();
            String shortDescription = dataset.getMetadata().getshortDescription().getLocalisedText()[0].getText();
            String keywords = dataset.getMetadata().getkeywords();
            String original = dataset.getMetadata().getoriginal();
            String model = dataset.getMetadata().getmodel().getname();
            String modelRepository = dataset.getMetadata().getmodel().getlocationHint();
            String furtherInformation = dataset.getMetadata().getfurtherInformation();
            String furtherMetadata = dataset.getMetadata().getfurtherMetadata();
            String knownWMS = dataset.getMetadata().getknownWMS()[0].getvalue();
            Date lastEditingDate = null;
            try {
                lastEditingDate = new SimpleDateFormat("yyyy-MM-dd").parse(dataset.getMetadata().getlastEditingDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }  
            double westLimit = dataset.getMetadata().getboundary().getwestlimit();
            double southLimit = dataset.getMetadata().getboundary().getsouthlimit();
            double eastLimit = dataset.getMetadata().getboundary().geteastlimit();
            double northLimit = dataset.getMetadata().getboundary().getnorthlimit();

            List<DataSetFile> dataSetFiles = new ArrayList<DataSetFile>();
            DataFile[] dataFiles = dataset.getMetadata().getfiles();
            for (DataFile dataFile : dataFiles) {
                ch.interlis.models.DatasetIdx16.File file = dataFile.getfile()[0];
                String filePath = file.getpath();
                String fileFormat = dataFile.getfileFormat();
                
                DataSetFile dataSetFile = new DataSetFile();
                dataSetFile.location = filePath;
                dataSetFile.format = fileFormat;
                
                dataSetFiles.add(dataSetFile);
            }
            
            DataSet dataSet = new DataSet();
            dataSet.id = id;
            dataSet.title = title;
            dataSet.shortDescription = shortDescription;
            dataSet.keywords = keywords;
            dataSet.original = original;
            dataSet.model = model;
            dataSet.modelRepository = modelRepository;
            dataSet.furtherInformation = furtherInformation;
            dataSet.furtherMetadata = furtherMetadata;
            dataSet.knownWMS = knownWMS;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            dataSet.lastEditingDate = format.format(lastEditingDate);
            dataSet.westLimit = westLimit;
            dataSet.southLimit = southLimit;
            dataSet.eastLimit = eastLimit;
            dataSet.northLimit = northLimit;
            dataSet.files = dataSetFiles.toArray(new DataSetFile[0]);
            
            dataSets.add(dataSet);
        }
        return new ResponseEntity<List<DataSet>>(dataSets, HttpStatus.OK);
    }

}
