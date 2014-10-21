package org.teiid.authoring.client.resources;

import com.github.gwtbootstrap.client.ui.DataGrid;
import com.github.gwtbootstrap.client.ui.DataGrid.Resources;
import com.google.gwt.core.client.GWT;

public interface DataGridResources extends Resources {

	public static final DataGridResources INSTANCE = GWT.create(DataGridResources.class);

    @Source({DataGrid.Style.DEFAULT_CSS, "css/DataGrid.css"})
    DataGrid.Style dataGridStyle();
   
}
