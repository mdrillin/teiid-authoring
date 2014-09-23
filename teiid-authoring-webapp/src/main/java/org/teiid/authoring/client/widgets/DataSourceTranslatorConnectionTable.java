/**
 * Copyright (C) 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.teiid.authoring.client.widgets;

import java.util.Collections;
import java.util.List;

import org.teiid.authoring.share.beans.DataSourceTranslatorConnectionPageRow;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionModel;

/**
 * Composite for display of DataSource names
 */
public class DataSourceTranslatorConnectionTable extends Composite {

	private static String COLUMN_HEADER_SOURCE = "Source";
	private static String COLUMN_HEADER_TRANSLATOR = "Translator";
	private static String COLUMN_HEADER_CONNECTION = "Connection";

    protected FlowPanel panel = new FlowPanel();
    protected Label label = new Label();

    private SimpleTable<DataSourceTranslatorConnectionPageRow> table;

    public DataSourceTranslatorConnectionTable() {
        initWidget( panel );
        panel.add(createTablePanel());
    }
    
    /**
     * Create the panel
     * @return the panel widget
     */
    protected Widget createTablePanel() {
    	table = new SimpleTable<DataSourceTranslatorConnectionPageRow>();
        TextColumn<DataSourceTranslatorConnectionPageRow> sourceColumn = new TextColumn<DataSourceTranslatorConnectionPageRow>() {
            public String getValue( DataSourceTranslatorConnectionPageRow row ) {
                return row.getDataSource();
            }
        };
        table.addColumn( sourceColumn, COLUMN_HEADER_SOURCE );

        TextColumn<DataSourceTranslatorConnectionPageRow> translatorColumn = new TextColumn<DataSourceTranslatorConnectionPageRow>() {
            public String getValue( DataSourceTranslatorConnectionPageRow row ) {
                return row.getTranslator();
            }
        };
        table.addColumn( translatorColumn, COLUMN_HEADER_TRANSLATOR );
        
        TextColumn<DataSourceTranslatorConnectionPageRow> connectionColumn = new TextColumn<DataSourceTranslatorConnectionPageRow>() {
            public String getValue( DataSourceTranslatorConnectionPageRow row ) {
                return row.getConnection();
            }
        };
        table.addColumn( connectionColumn, COLUMN_HEADER_CONNECTION );
        
        table.setColumnWidth(sourceColumn, 200, Unit.PX);
        table.setColumnWidth(translatorColumn, 200, Unit.PX);
        table.setColumnWidth(connectionColumn, 200, Unit.PX);
        
    	table.setWidth("620px");
    	table.setHeight("200px");
    	
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(table);
        return verticalPanel;
    }
    
    public void clear() {
    	setData(Collections.EMPTY_LIST);
    }
    
    public void setData(List<DataSourceTranslatorConnectionPageRow> rows) {
    	table.setRowData(rows);
    }
    
    public void setSelectionModel( final SelectionModel selectionModel ) {
        table.setSelectionModel( selectionModel );
    }
    
}
