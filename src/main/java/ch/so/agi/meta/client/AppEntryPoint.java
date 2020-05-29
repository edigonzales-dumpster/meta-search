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

import ch.so.agi.meta.shared.ConfigResponse;
import ch.so.agi.meta.shared.ConfigService;
import ch.so.agi.meta.shared.ConfigServiceAsync;
import ch.so.agi.meta.shared.model.DataSet;
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
    private final ConfigServiceAsync configService = GWT.create(ConfigService.class);
    
    private String SEARCH_SERVICE_URL = "https://geo.so.ch/api/search/v2/?filter=foreground,ch.so.agi.gemeindegrenzen,ch.so.agi.av.gebaeudeadressen.gebaeudeeingaenge,ch.so.agi.av.bodenbedeckung,ch.so.agi.av.grundstuecke.projektierte,ch.so.agi.av.grundstuecke.rechtskraeftig,ch.so.agi.av.nomenklatur.flurnamen,ch.so.agi.av.nomenklatur.gelaendenamen&searchtext=";    

    private NumberFormat fmtDefault = NumberFormat.getDecimalFormat();
    private NumberFormat fmtPercent = NumberFormat.getFormat("#0.0");
    
    // Settings
    String myVar;
    
    SuggestBox suggestBox;
    
    List<DataSet> dataSets = new ArrayList<DataSet>();
    public void onModuleLoad() {
        configService.configServer(new AsyncCallback<ConfigResponse>() {
            @Override
            public void onFailure(Throwable caught) {
                console.error(caught.getMessage());
                DomGlobal.window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(ConfigResponse result) {
                myVar = result.getMyVar();  
                console.log(myVar);
                
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
                    
                    return null;
                }).catch_(error -> {
                    console.log(error);
                    return null;
                });                
            }
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
        
        
    }

   private static native void updateURLWithoutReloading(String newUrl) /*-{
        $wnd.history.pushState(newUrl, "", newUrl);
    }-*/;
}