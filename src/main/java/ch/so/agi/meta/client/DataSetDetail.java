package ch.so.agi.meta.client;

import static org.jboss.elemento.Elements.*;
import static elemental2.dom.DomGlobal.console;

import java.util.Date;

import org.gwtproject.i18n.client.DateTimeFormat;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import org.dominokit.domino.ui.datatable.CellRenderer;
import org.dominokit.domino.ui.grid.Column;
import org.dominokit.domino.ui.grid.Row;
import org.dominokit.domino.ui.style.Styles;
import org.jboss.elemento.IsElement;

import ch.so.agi.meta.shared.model.DataSet;
import ch.so.agi.meta.shared.model.DataSetFile;

public class DataSetDetail implements IsElement<HTMLElement> {

    private Row rowElement = Row.create().css("DataSetDetail").style().add(Styles.margin_0).get();
    private CellRenderer.CellInfo<DataSet> cell;

    public DataSetDetail(CellRenderer.CellInfo<DataSet> cell) {
        this.cell = cell;
        initDetails();
    }

    private void initDetails() {
        // File download links.
        HTMLDivElement filesDiv = div().element();
        DataSetFile[] files = cell.getRecord().files;
        for (DataSetFile file : files) {
            String format = "unknown";
            if (file.format.toLowerCase().contains("sqlite")) {
                format = "GeoPackage";
            } else if (file.format.toLowerCase().contains("interlis") && file.format.toLowerCase().contains("version=2")) {
                format = "INTERLIS (XTF)";
            }

            int index = file.location.lastIndexOf('/');
            String fileName = file.location.substring(index+1);
     
            // TODO: 
            // Geht nicht wirklich gut mit S3 wie es aufgegleist ist (relative
            // URL etc.). Darum hier hardcodiert.
            String fileUrl = "https://s3.eu-central-1.amazonaws.com/" + file.location.substring(3);
            HTMLElement fileLink = a().attr("class", "DataSetDetailLink")
                    .attr("href", fileUrl)
                    .attr("target", "_blank")
                    .add(span().textContent(fileName)).element();

            Row fileRowElement = Row.create();
            fileRowElement
                .addColumn(
                    Column.span4().appendChild(span().textContent(format + ":")))
                .addColumn(
                    Column.span8().appendChild(fileLink));

            filesDiv.appendChild(fileRowElement.element());
        }
        
        // Dates
        DateTimeFormat dateTimeFormatInput = DateTimeFormat.getFormat("yyyy-MM-dd");        
        DateTimeFormat dateTimeFormatOutput = DateTimeFormat.getFormat("dd. MMMM yyyy");    

        String lastEditingDate = dateTimeFormatOutput.format(dateTimeFormatInput.parse(cell.getRecord().lastEditingDate));
        String publishingDate = dateTimeFormatOutput.format(new Date());

        Row dateRowElement = Row.create();
        dateRowElement
            .addColumn(
                    Column.span4().appendChild(span().textContent("Nachführungsdatum:")))
            .addColumn(
                    Column.span8().appendChild(span().textContent(lastEditingDate)))
            .addColumn(
                    Column.span4().appendChild(span().textContent("Daten publiziert am:")))
            .addColumn(
                    Column.span8().appendChild(span().textContent(publishingDate)));
                
        // Further Information
        HTMLDivElement furtherInfoDiv = div().element();
        
        HTMLElement metaDataLink = a().attr("class", "DataSetDetailLink")
                .attr("href", cell.getRecord().furtherMetadata)
                .attr("target", "_blank")
                .add(span().textContent(cell.getRecord().furtherMetadata)).element();

        HTMLElement wmsLink = a().attr("class", "DataSetDetailLink")
                .attr("href", cell.getRecord().knownWMS + "?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.3.0")
                .attr("target", "_blank")
                .add(span().textContent(cell.getRecord().knownWMS)).element();

        Row furtherInfoElement = Row.create();
        furtherInfoElement
            .addColumn(
                    Column.span4().appendChild(span().textContent("Weitere Metadaten:")))
            .addColumn(
                    Column.span8().appendChild(span().add(metaDataLink)))
            .addColumn(
                    Column.span4().appendChild(span().textContent("Datenherr:")))
            .addColumn(
                    Column.span8().appendChild(span().textContent("-")))
            .addColumn(
                    Column.span4().appendChild(span().textContent("WMS:")))
            .addColumn(
                    Column.span8().appendChild(span().add(wmsLink)));

        furtherInfoDiv.appendChild(furtherInfoElement.element());
        
        // Main row element.
        rowElement
            .addColumn(Column.span4().style().add(Styles.padding_15).get()
                    .appendChild(
                            div().css("DataSetDetailHeader").textContent("Beschreibung").element())
                    .appendChild(
                            div().css("DataSetDetailText").textContent(cell.getRecord().shortDescription).element())
                    .appendChild(
                            div().css("DataSetDetailHeader").textContent("Keywords").element())
                    .appendChild(
                            div().css("DataSetDetailText").textContent(cell.getRecord().keywords)))
            .addColumn(Column.span4().style().add(Styles.padding_15).get()
                    .appendChild(
                            div().css("DataSetDetailHeader").textContent("Download").element())
                    .appendChild(
                            filesDiv)
                    .appendChild(
                            div().css("DataSetDetailHeader").textContent("Datumsangaben").element())
                    .appendChild(
                            div().css("DataSetDetailText")
                                .add(dateRowElement)))
            .addColumn(Column.span4().style().add(Styles.padding_15).get()
                    .appendChild(
                            div().css("DataSetDetailHeader").textContent("Weitere Informationen").element())
                    .appendChild(
                            furtherInfoDiv));
    }
    
    @Override
    public HTMLElement element() {
        return rowElement.element();
    }

    private String fixUri(String uri) {
        if (uri.endsWith("/")) {
            return uri;
        }
        return uri = uri + "/";
    }
}
