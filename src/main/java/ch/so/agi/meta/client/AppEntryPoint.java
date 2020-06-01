package ch.so.agi.meta.client;

import static elemental2.dom.DomGlobal.console;
import static elemental2.dom.DomGlobal.alert;

import static org.jboss.elemento.Elements.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.dominokit.domino.ui.forms.SuggestBox.DropDownPositionDown;
import org.dominokit.domino.ui.grid.Column;
import org.dominokit.domino.ui.grid.Row;
import org.dominokit.domino.ui.button.Button;
import org.dominokit.domino.ui.datatable.ColumnConfig;
import org.dominokit.domino.ui.datatable.DataTable;
import org.dominokit.domino.ui.datatable.TableConfig;
import org.dominokit.domino.ui.datatable.plugins.RecordDetailsPlugin;
import org.dominokit.domino.ui.datatable.plugins.SortPlugin;
import org.dominokit.domino.ui.datatable.store.LocalListDataStore;
import org.dominokit.domino.ui.dropdown.DropDownMenu;
import org.dominokit.domino.ui.dropdown.DropDownPosition;
import org.dominokit.domino.ui.forms.SuggestBox;
import org.dominokit.domino.ui.forms.SuggestBoxStore;
import org.dominokit.domino.ui.forms.SuggestItem;
import org.dominokit.domino.ui.icons.Icon;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.modals.IsModalDialog;
import org.dominokit.domino.ui.modals.ModalDialog;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.style.ColorScheme;
import org.dominokit.domino.ui.themes.Theme;
import org.dominokit.domino.ui.utils.HasChangeHandlers.ChangeHandler;
import org.dominokit.domino.ui.utils.HasSelectionHandler.SelectionHandler;
import org.dominokit.domino.ui.utils.TextNode;
import org.gwtproject.safehtml.shared.SafeHtmlUtils;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.so.agi.meta.shared.ConfigResponse;
import ch.so.agi.meta.shared.ConfigService;
import ch.so.agi.meta.shared.ConfigServiceAsync;
import ch.so.agi.meta.shared.model.DataSet;
import ch.so.agi.meta.shared.model.ModelAttribute;
import ch.so.agi.meta.shared.model.ModelClass;
import ch.so.agi.meta.shared.model.ModelMeta;
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
    
    // Configuration
    String myVar;

    private String SEARCH_SERVICE_URL = "https://geo.so.ch/api/search/v2/?filter=foreground,ch.so.agi.gemeindegrenzen,ch.so.agi.av.gebaeudeadressen.gebaeudeeingaenge,ch.so.agi.av.bodenbedeckung,ch.so.agi.av.grundstuecke.projektierte,ch.so.agi.av.grundstuecke.rechtskraeftig,ch.so.agi.av.nomenklatur.flurnamen,ch.so.agi.av.nomenklatur.gelaendenamen&searchtext=";    

    private NumberFormat fmtDefault = NumberFormat.getDecimalFormat();
    private NumberFormat fmtPercent = NumberFormat.getFormat("#0.0");
    
    SuggestBox suggestBox;
    
    DataSet[] dataSets;
    ModelMeta modelMeta;
    
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
                
                RequestInit requestInit = RequestInit.create();
                Headers headers = new Headers();
                headers.append("Content-Type", "application/x-www-form-urlencoded"); 
                requestInit.setHeaders(headers);

                DomGlobal.fetch("ilidata", requestInit)
                .then(response -> {
                    if (!response.ok) {
                        return null;
                    }
                    return response.text();
                })
                .then(json -> {                    
                    dataSets = (DataSet[]) Global.JSON.parse(json);
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
        
        Theme theme = new Theme(ColorScheme.BLUE);
        theme.apply();
        
        TableConfig<DataSet> tableConfig = new TableConfig<>();
        tableConfig
            .addColumn(ColumnConfig.<DataSet>create("id", "Id")
                .textAlign("left")
                .sortable()
                .setCellRenderer(cell -> TextNode.of(cell.getTableRow().getRecord().id)))
            .addColumn(ColumnConfig.<DataSet>create("title", "Titel")
                .textAlign("left")
                .setCellRenderer(cell -> TextNode.of(cell.getTableRow().getRecord().title)))
            .addColumn(ColumnConfig.<DataSet>create("model", "Datenmodell")
                .textAlign("left")
                .sortable()
                .setCellRenderer(cell -> a().on(click, event -> showModel(cell.getRecord().model)).id("modelLink").attr("class", "DataSetDetailLink").add(span().textContent(cell.getRecord().model)).element()));

        tableConfig.addPlugin(new RecordDetailsPlugin<>(cell -> new DataSetDetail(cell).element()));
        tableConfig.addPlugin(new SortPlugin<>());
        
        LocalListDataStore<DataSet> listStore = new LocalListDataStore<>();
        listStore.setData(Arrays.asList(dataSets));
        listStore.setRecordsSorter(new DataSetSorter());

        DataTable<DataSet> table = new DataTable<>(tableConfig, listStore);
              
        table.element().style.paddingTop = CSSProperties.PaddingTopUnionType.of("100px");
        table.load();
        
        body().add(div().css("table-responsive").style("padding: 20px;").add(table));
    }

    private void showModel(String modelName) {
        console.log(modelName);
        
        RequestInit requestInit = RequestInit.create();
        Headers headers = new Headers();
        headers.append("Content-Type", "application/x-www-form-urlencoded"); 
        requestInit.setHeaders(headers);

        DomGlobal.fetch("model/" + modelName , requestInit)
        .then(response -> {
            if (!response.ok) {
                return null;
            }
            return response.text();
        })
        .then(json -> {                    
            modelMeta = (ModelMeta) Global.JSON.parse(json);
                        
            HTMLDivElement content = div().element();
            
            Row generalInfoRow = Row.create();
            generalInfoRow.style().setColor("#333").setPaddingBottom("10px").get();
            generalInfoRow
                .addColumn(
                        Column.span2().appendChild(span().textContent("Name:")))
                .addColumn(
                        Column.span10().appendChild(span().textContent(modelMeta.name)))
                .addColumn(
                        Column.span2().appendChild(span().textContent("Version:")))
                .addColumn(
                        Column.span10().appendChild(span().textContent(modelMeta.version)));
            content.appendChild(generalInfoRow.element());
            
            Row classHeaderRow = Row.create();
            classHeaderRow.style().setColor("#333").setPaddingBottom("10px").setPaddingTop("20px").get();
            classHeaderRow
                .addColumn(
                        Column.span12().appendChild(span().style("font-weight:bold;").textContent("Tabellen")));
            content.appendChild(classHeaderRow.element());

            
            for (ModelClass modelClass : modelMeta.modelClasses) {                
                Row classInfoRow = Row.create();
                classInfoRow.style().setColor("#333").setPaddingTop("10px;").get();
                classInfoRow
                    .addColumn(
                            Column.span2().appendChild(span().textContent("Name:")))
                    .addColumn(
                            Column.span10().appendChild(span().style("font-weight:600;").textContent(modelClass.className)))
                    .addColumn(
                            Column.span2().appendChild(span().textContent("Beschreibung:")))
                    .addColumn(
                            Column.span10().appendChild(span().textContent(modelClass.classDescripion)))
                    .addColumn(
                            Column.span2().appendChild(span().textContent("Attribute:")))
                    .addColumn(
                            Column.span10().appendChild(a().on(click, event -> toggleAttributes(event, modelClass.className+".Attributes")).attr("class", "DataSetDetailLink").add(span().id(modelClass.className+".Attributes.LinkText").textContent("anzeigen"))));
                    
                content.appendChild(classInfoRow.element());
                
                HTMLDivElement attributesElement = div().id(modelClass.className+".Attributes").element();
                attributesElement.style.display = "none";
                
                Row attributesHeaderRow = Row.create();
                attributesHeaderRow.style().setColor("#333").get();
                attributesHeaderRow
                    .addColumn(
                            Column.span3().appendChild(span().style("font-size:12px;font-style:italic;font-weight:600;").textContent("Name")))
                    .addColumn(
                            Column.span5().appendChild(span().style("font-size:12px;font-style:italic;font-weight:600;").textContent("Beschreibung")))
                    .addColumn(
                            Column.span2().appendChild(span().style("font-size:12px;font-style:italic;font-weight:600;").textContent("Typ")))
                    .addColumn(
                            Column.span2().appendChild(span().style("font-size:12px;font-style:italic;font-weight:600;").textContent("Zwingend")));
                attributesElement.appendChild(attributesHeaderRow.element());
                
                for (ModelAttribute modelAttribute : modelClass.modelAttributes) {
                    String isMandatory = modelAttribute.mandatory ? "j" : "n";
                    Row attributesInfoRow = Row.create();
                    attributesInfoRow.style().setColor("#333").get();
                    attributesInfoRow
                    .addColumn(
                            Column.span3().appendChild(span().textContent(modelAttribute.attributeName)))
                    .addColumn(
                            Column.span5().appendChild(span().textContent(modelAttribute.attributeDescription)))
                    .addColumn(
                            Column.span2().appendChild(span().textContent(modelAttribute.attributeType)))
                    .addColumn(
                            Column.span2().appendChild(span().textContent(isMandatory)));
                    attributesElement.appendChild(attributesInfoRow.element());
                }
                content.appendChild(attributesElement);

                content.appendChild(span().innerHtml(SafeHtmlUtils.fromTrustedString("<hr>")).element());

            }

            ModalDialog modal = ModalDialog.create("Datenmodell")
                    .setAutoClose(true)
                    //.setType(IsModalDialog.ModalType.RIGHT_SHEET)
                    .setSize(IsModalDialog.ModalSize.LARGE);
            modal.style().cssText("max-height:90%;");
            modal.appendChild(content);
            Button closeButton = Button.create("CLOSE").linkify();
            EventListener closeModalListener = (evt) -> modal.close();
            closeButton.addClickListener(closeModalListener);
            modal.appendFooterChild(closeButton);
            modal.open();
            
            // Damit nicht ans Ende gescrollt wieder, wenn sich das Fenster
            // öffnet und der Inhalt grösser als das Fenster ist.
            modal.element().scrollTop = 0;
            
            return null;
        }).catch_(error -> {
            console.log(error);
            return null;
        });                

        
    }
    
    private void toggleAttributes(Event event, String id) {
        console.log(event.currentTarget);
        console.log(event.target);
        console.log(id);
        HTMLElement element = (HTMLElement) DomGlobal.document.getElementById(id);
        HTMLElement textElement = (HTMLElement) event.target;        
        if (element.style.display == "none" ) {
            element.style.display = "block";
            textElement.innerHTML = "verbergen";
        } else {
            element.style.display = "none";
            textElement.innerHTML = "anzeigen";            
        }
    }
    
    private static native void updateURLWithoutReloading(String newUrl) /*-{
        $wnd.history.pushState(newUrl, "", newUrl);
    }-*/;
}