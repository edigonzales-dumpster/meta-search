package ch.so.agi.meta.client;

import static elemental2.dom.DomGlobal.console;
import static org.jboss.elemento.Elements.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.dominokit.domino.ui.forms.SuggestBox.DropDownPositionDown;
import org.dominokit.domino.ui.datatable.ColumnConfig;
import org.dominokit.domino.ui.datatable.DataTable;
import org.dominokit.domino.ui.datatable.TableConfig;
import org.dominokit.domino.ui.datatable.store.LocalListDataStore;
import org.dominokit.domino.ui.dropdown.DropDownMenu;
import org.dominokit.domino.ui.dropdown.DropDownPosition;
import org.dominokit.domino.ui.forms.SuggestBox;
import org.dominokit.domino.ui.forms.SuggestBoxStore;
import org.dominokit.domino.ui.forms.SuggestItem;
import org.dominokit.domino.ui.icons.Icon;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.style.ColorScheme;
import org.dominokit.domino.ui.themes.Theme;
import org.dominokit.domino.ui.utils.HasChangeHandlers.ChangeHandler;
import org.dominokit.domino.ui.utils.HasSelectionHandler.SelectionHandler;
import org.dominokit.domino.ui.utils.TextNode;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.so.agi.meta.shared.DataSet;
//import ch.so.agi.meta.client.ui.BackgroundSwitcher;
//import ch.so.agi.meta.client.ui.SearchBox;
//import ch.so.agi.meta.shared.BackgroundMapConfig;
//import ch.so.agi.meta.shared.ConfigResponse;
//import ch.so.agi.meta.shared.ConfigService;
//import ch.so.agi.meta.shared.ConfigServiceAsync;
import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.Headers;
import elemental2.dom.RequestInit;
import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.core.JsString;
import elemental2.core.JsNumber;
import elemental2.dom.CSSProperties;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
//import ol.Coordinate;
//import ol.Extent;
//import ol.Map;
//import ol.MapBrowserEvent;
//import ol.MapEvent;
//import ol.View;
////import ol.events.Event;
//import ol.layer.Tile;

import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.EventType.*;


public class AppEntryPoint implements EntryPoint {
//    private MyMessages messages = GWT.create(MyMessages.class);
//    private final ConfigServiceAsync configService = GWT.create(ConfigService.class);
    
//    private List<BackgroundMapConfig> backgroundMapsConfig;
//    private String SEARCH_SERVICE_URL = "https://api3.geo.admin.ch/rest/services/api/SearchServer?sr=2056&limit=15&type=locations&origins=address,parcel&searchText=";
    private String SEARCH_SERVICE_URL = "https://geo.so.ch/api/search/v2/?filter=foreground,ch.so.agi.gemeindegrenzen,ch.so.agi.av.gebaeudeadressen.gebaeudeeingaenge,ch.so.agi.av.bodenbedeckung,ch.so.agi.av.grundstuecke.projektierte,ch.so.agi.av.grundstuecke.rechtskraeftig,ch.so.agi.av.nomenklatur.flurnamen,ch.so.agi.av.nomenklatur.gelaendenamen&searchtext=";    

    private NumberFormat fmtDefault = NumberFormat.getDecimalFormat();
    private NumberFormat fmtPercent = NumberFormat.getFormat("#0.0");
    
//    private String MAP_DIV_ID = "map";
//
//    private WgcMap map;
    
    SuggestBox suggestBox;
    
    List<DataSet> dataSets = new ArrayList<DataSet>();
    public void onModuleLoad() {
        
        
        
//        configService.configServer(new AsyncCallback<ConfigResponse>() {
//            @Override
//            public void onFailure(Throwable caught) {
//                console.error(caught.getMessage());
//                DomGlobal.window.alert(caught.getMessage());
//            }
//
//            @Override
//            public void onSuccess(ConfigResponse result) {
//                backgroundMapsConfig = result.getBackgroundMaps();                
//                init();
//            }
//        });
        
        RequestInit requestInit = RequestInit.create();
        Headers headers = new Headers();
        headers.append("Content-Type", "application/x-www-form-urlencoded"); 
        requestInit.setHeaders(headers);

        DomGlobal.fetch("data", requestInit)
        .then(response -> {
            if (!response.ok) {
                return null;
            }
            return response.text();
        })
        .then(json -> {
//            List<SuggestItem<SearchResult>> suggestItems = new ArrayList<>();
            JsArray<?> parsed = Js.cast(Global.JSON.parse(json));
            console.log(parsed.length);
            
            dataSets.clear();
            for (int i = 0; i < parsed.length; i++) { 
                DataSet dataset = (DataSet) parsed.getAt(i);
                console.log(dataset.id);
                console.log(dataset.files);
                
                dataSets.add(dataset);
            }
            
            init();

            
//            List<DataSet> dataSets = (List<DataSet>) Global.JSON.parse(json);

//            console.log(parsed.toString());
            
//            JsArray<?> results = Js.cast(parsed.get("results"));
//            for (int i = 0; i < results.length; i++) {
//                JsPropertyMap<?> resultObj = Js.cast(results.getAt(i));
//                if (resultObj.has("feature")) {
//                    JsPropertyMap feature = (JsPropertyMap) resultObj.get("feature");
//                    String display = ((JsString) feature.get("display")).normalize();
//                    String dataproductId = ((JsString) feature.get("dataproduct_id")).normalize();
//                    String idFieldName = ((JsString) feature.get("id_field_name")).normalize();
//                    int featureId = new Double(((JsNumber) feature.get("feature_id")).valueOf()).intValue();
//                    List<Double> bbox = ((JsArray) feature.get("bbox")).asList();
//
//                    SearchResult searchResult = new SearchResult();
//                    searchResult.setLabel(display);
//                    searchResult.setDataproductId(dataproductId);
//                    searchResult.setIdFieldName(idFieldName);
//                    searchResult.setFeatureId(featureId);
//                    searchResult.setBbox(bbox);
//                    searchResult.setType("feature");
//                    
//                    Icon icon;
//                    if (dataproductId.contains("gebaeudeadressen")) {
//                        icon = Icons.ALL.mail();
//                    } else if (dataproductId.contains("grundstueck")) {
//                        icon = Icons.ALL.home();
//                    } else {
//                        icon = Icons.ALL.place();
//                    }
//                    
//                    SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, searchResult.getLabel(), icon);
//                    suggestItems.add(suggestItem);
//                }
//            }
//            suggestionsHandler.onSuggestionsReady(suggestItems);
            return null;
        }).catch_(error -> {
            console.log(error);
            return null;
        });

    }

    @SuppressWarnings("unchecked")
    private void init() {      
        console.log("init");
        
//        Theme theme = new Theme(ColorScheme.RED);
//        theme.apply();

//        body().add(div().id("fubar").textContent("Hallo Welt."));
        
        TableConfig<DataSet> tableConfig = new TableConfig<>();
        tableConfig
            .addColumn(ColumnConfig.<DataSet>create("id", "Id")
                .textAlign("left")
                //.asHeader()
                .setCellRenderer(cell -> TextNode.of(cell.getTableRow().getRecord().id)))
            .addColumn(ColumnConfig.<DataSet>create("title", "Titel")
                    .textAlign("left")
                    //.asHeader()
                    //.setCellRenderer(cell -> TextNode.of(cell.getTableRow().getRecord().title)));
                    .setCellRenderer(cell -> TextNode.of(cell.getTableRow().getRecord().title)))
            .addColumn(ColumnConfig.<DataSet>create("model", "Modell")
                .textAlign("left")
                //.asHeader()
                //.setCellRenderer(cell -> TextNode.of(cell.getTableRow().getRecord().title)));
                .setCellRenderer(cell -> TextNode.of(cell.getTableRow().getRecord().model)));
        
        LocalListDataStore<DataSet> localListDataStore = new LocalListDataStore<>();
        DataTable<DataSet> table = new DataTable<>(tableConfig, localListDataStore);
        
        localListDataStore.setData(dataSets);
        table.element().style.paddingTop = CSSProperties.PaddingTopUnionType.of("100px");
        table.load();
        
        body().add(table);
        
//        map = new WgcMapBuilder()
//                .setMapId(MAP_DIV_ID)
//                .addBackgroundLayers(backgroundMapsConfig)
//                .build();
//        
//        body().add(new BackgroundSwitcher(map, backgroundMapsConfig));
//        
//        body().add(new SearchBox(map));
        
        
        
        
        // TODO getfeatureinfo
        // - url in config
        // - Den Rest selber zusammenstöpseln (und berechnen).
        // - fetch()
//        map.addClickListener(new ol.event.EventListener<MapBrowserEvent>() {
//            @Override
//            public void onEvent(MapBrowserEvent event) {
//                ol.source.Vector vectorSource = map.getHighlightLayer().getSource();
//                vectorSource.clear(false);
//                
//                
//                //console.log(event.getCoordinate().toString());
//                
//                double resolution = map.getView().getResolution();
//                //console.log(map.getView().getResolution());
//
//                // 50/51/101-Ansatz ist anscheinend bei OpenLayers normal.
//                // -> siehe baseUrlFeatureInfo resp. ein Original-Request
//                // im Web GIS Client.
//                double minX = event.getCoordinate().getX() - 50 * resolution;
//                double maxX = event.getCoordinate().getX() + 51 * resolution;
//                double minY = event.getCoordinate().getY() - 50 * resolution;
//                double maxY = event.getCoordinate().getY() + 51 * resolution;
//
////                String baseUrlFeatureInfo = map.getBaseUrlFeatureInfo();
////                List<String> foregroundLayers = map.getForegroundLayers();
////                //console.log(foregroundLayers);
////                String layers = String.join(",", foregroundLayers);
////                String urlFeatureInfo = baseUrlFeatureInfo + "&layers=" + layers;
////                urlFeatureInfo += "&query_layers=" + layers;
////                urlFeatureInfo += "&bbox=" + minX + "," + minY + "," + maxX + "," + maxY;
//                
//                //console.log(urlFeatureInfo);
//            }
//        });        
                
//        map.addMoveEndListener(new ol.event.EventListener<MapEvent>() {
//            @Override
//            public void onEvent(MapEvent event) {
//                ol.View view = map.getView();
//                
//                ol.Extent extent = view.calculateExtent(map.getSize());
//                double easting = extent.getLowerLeftX() + (extent.getUpperRightX() - extent.getLowerLeftX()) / 2;
//                double northing = extent.getLowerLeftY() + (extent.getUpperRightY() - extent.getLowerLeftY()) / 2;
//                
//                String newUrl = Window.Location.getProtocol() + "//" + Window.Location.getHost() + Window.Location.getPath();
//                newUrl += "?bgLayer=" + map.getBackgroundLayer();
//                newUrl += "&layers=" + String.join(",", map.getForegroundLayers());
//                newUrl += "&layers_opacity=" + map.getForgroundLayerOpacities().stream().map(String::valueOf).collect(Collectors.joining(","));
//                newUrl += "&E=" + String.valueOf(easting);
//                newUrl += "&N=" + String.valueOf(northing);
//                newUrl += "&zoom=" + String.valueOf(view.getZoom());
//
//                updateURLWithoutReloading(newUrl);
//                
//                Element bigMapLinkElement = DomGlobal.document.getElementById("bigMapLink");
//                bigMapLinkElement.removeAttribute("href");
//                bigMapLinkElement.setAttribute("href", map.createBigMapUrl());
//            }
//        });
        
    }

   private static native void updateURLWithoutReloading(String newUrl) /*-{
        $wnd.history.pushState(newUrl, "", newUrl);
    }-*/;
}