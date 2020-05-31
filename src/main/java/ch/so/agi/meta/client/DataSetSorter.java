package ch.so.agi.meta.client;

import java.util.Comparator;

import org.dominokit.domino.ui.datatable.plugins.SortDirection;
import org.dominokit.domino.ui.datatable.store.RecordsSorter;

import ch.so.agi.meta.shared.model.DataSet;

public class DataSetSorter implements RecordsSorter<DataSet> {

    @Override
    public Comparator<DataSet> onSortChange(String sortBy, SortDirection sortDirection) {
        if ("id".equals(sortBy)) {
            if (SortDirection.ASC.equals(sortDirection)) {
                return Comparator.comparing((DataSet d) -> d.id);
            } else {
                return (o1, o2) -> o2.id.compareTo(o1.id);
            }
        }

        if ("model".equals(sortBy)) {
            if (SortDirection.ASC.equals(sortDirection)) {
                return Comparator.comparing((DataSet d) -> d.model);
            } else {
                return (o1, o2) -> o2.model.compareTo(o1.model);
            }
        }
        
        return (o1, o2) -> 0;
    }
}
