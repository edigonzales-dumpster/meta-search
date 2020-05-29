package ch.so.agi.meta.client;

import static org.jboss.elemento.Elements.*;
import static elemental2.dom.DomGlobal.console;

import java.util.List;

import elemental2.dom.HTMLElement;

import org.dominokit.domino.ui.datatable.CellRenderer;
import org.dominokit.domino.ui.grid.Column;
import org.dominokit.domino.ui.grid.Row;
import org.dominokit.domino.ui.style.Styles;
import org.jboss.elemento.IsElement;

import ch.so.agi.meta.shared.model.DataSet;
import ch.so.agi.meta.shared.model.DataSetFile;

public class DataSetDetail implements IsElement<HTMLElement> {

    private Row rowElement = Row.create().style().add(Styles.margin_0).get();
    private CellRenderer.CellInfo<DataSet> cell;

    public DataSetDetail(CellRenderer.CellInfo<DataSet> cell) {
        this.cell = cell;
        initDetails();
    }

    private void initDetails() {
        
        // TODO JSON-Gedöns... Cast whatever... List...
        List<DataSetFile> files = cell.getRecord().files;
        console.log(cell.getRecord().files);

        for (DataSetFile file : files) {
            div().textContent(file.type + ": " + file.location);
        }
        
        console.log(files.get(0).location);
        
        
        rowElement
        .addColumn(Column.span4()
                .appendChild(
                        div().css("DataSetDetailHeader").textContent("Beschreibung").element())
                .appendChild(
                        div().css("DataSetDetailText").textContent(cell.getRecord().shortDescription).element())
                .appendChild(
                        div().css("DataSetDetailHeader").textContent("Keywords").element())
                .appendChild(
                        div().css("DataSetDetailText").textContent(cell.getRecord().keywords)))
        .addColumn(Column.span4()
                .appendChild(
                        div().css("DataSetDetailHeader").textContent("Download").element()));

    }
    
    @Override
    public HTMLElement element() {
        return rowElement.element();
    }

}
