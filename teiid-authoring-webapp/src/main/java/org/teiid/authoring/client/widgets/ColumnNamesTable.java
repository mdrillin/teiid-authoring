/*
 * Copyright 2014 JBoss Inc
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionModel;

/**
 * Composite for display of Column names
 */
public class ColumnNamesTable extends Composite {

	private static String COLUMN_HEADER_NAME = "Columns";

    protected VerticalPanel panel = new VerticalPanel();
    protected Label label = new Label();

    private SimpleTable<CheckableNameTypeRow> table;
    private CheckboxHeader cbHeader;

    public ColumnNamesTable() {
        initWidget( panel );
        panel.add(createTablePanel());
    }
    
    /**
     * Create the panel
     * @return the panel widget
     */
    protected Widget createTablePanel() {
    	table = new SimpleTable<CheckableNameTypeRow>();
    	
        // Add Checkbox column
    	Column<CheckableNameTypeRow, Boolean> checkboxColumn= new Column<CheckableNameTypeRow, Boolean>(new CheckboxCell(true,false))
    			{
    		@Override
    		public Boolean getValue(CheckableNameTypeRow object)
    		{
    			if(object == null) return false;
    			return object.isChecked();
    		}
    	};
    	checkboxColumn.setFieldUpdater(new FieldUpdater<CheckableNameTypeRow, Boolean>() {
    	    public void update(int index, CheckableNameTypeRow object, Boolean value) {
    	    	object.setChecked(value);
    	    	
    	    	boolean allRowsSame = true;
            	List<CheckableNameTypeRow> tableRows = table.getRowData();
            	boolean firstState = false;
            	for(int i=0; i<tableRows.size(); i++) {
            		CheckableNameTypeRow row = tableRows.get(i);
            		if(i==0) {
            			firstState = row.isChecked();
            		} else {
            			boolean thisState = row.isChecked();
            			if(thisState!=firstState) {
            				allRowsSame = false;
            				break;
            			}
            		}
            	}
            	if(allRowsSame) {
            		cbHeader.setValue(firstState);
            	} else {
            		cbHeader.setValue(false);
            	}
        		table.redrawHeaders();
    	    }
    	});

    	// Checkbox Header
        cbHeader = createCBHeader(false);

        table.addColumn(checkboxColumn, cbHeader);
    	table.setColumnWidth(checkboxColumn, 40, Unit.PX);
    		        
        // --------------
    	// Name Column
    	// --------------
        TextColumn<CheckableNameTypeRow> nameColumn = new TextColumn<CheckableNameTypeRow>() {
            public String getValue( CheckableNameTypeRow row ) {
                return row.getName();
            }
        };
        table.addColumn( nameColumn, COLUMN_HEADER_NAME );
        table.setColumnWidth(nameColumn, 350, Unit.PX);
        
    	table.setWidth("395px");
    	table.setHeight("200px");
    	
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(table);
        return verticalPanel;
    }
    
    private CheckboxHeader createCBHeader(boolean isChecked) {
    	CheckboxHeader cbHeader = new CheckboxHeader(new CheckboxCell(),false) {
    		@Override
    		protected void headerUpdated(boolean checkState){
    			List<CheckableNameTypeRow> tableRows = table.getRowData();
    			for(CheckableNameTypeRow aRow : tableRows) {
    				aRow.setChecked(checkState);
    			}
    			table.redraw();
    		}
    	};
    	return cbHeader;
    }
    
    public void clear() {
    	setData(Collections.<CheckableNameTypeRow>emptyList());
    }
    
    public String getSelectedRowString() {
    	StringBuilder sb = new StringBuilder();
    	
    	List<CheckableNameTypeRow> rows = table.getRowData();
    	for(CheckableNameTypeRow row : rows) {
    		if(row.isChecked()) {
    			if(!sb.toString().isEmpty()) {
    				sb.append(",");
    			}
    			sb.append(row.getName());
    		}
    	}
    	
    	return sb.toString();
    }
    
    public List<String> getSelectedColumnNames() {
    	List<String> colNames = new ArrayList<String>();
    	
    	List<CheckableNameTypeRow> rows = table.getRowData();
    	for(CheckableNameTypeRow row : rows) {
    		if(row.isChecked() && row.getName()!=null) {
    			colNames.add(row.getName());
    		}
    	}
    	
    	return colNames;
    }
    public List<String> getSelectedColumnTypes() {
    	List<String> colTypes = new ArrayList<String>();
    	
    	List<CheckableNameTypeRow> rows = table.getRowData();
    	for(CheckableNameTypeRow row : rows) {
    		if(row.isChecked() && row.getType()!=null) {
    			colTypes.add(row.getType());
    		}
    	}
    	
    	return colTypes;
    }
    
    public void setData(List<CheckableNameTypeRow> rows) {
    	// Resets table rows
    	table.setRowData(rows);
    	
    	// Header checkbox initially unchecked
    	cbHeader.setValue(false);
    	table.redrawHeaders();
    }
    
    public List<CheckableNameTypeRow> getData() {
    	return table.getRowData();
    }
    
	public void setSelectionModel( final SelectionModel<CheckableNameTypeRow> selectionModel ) {
        table.setSelectionModel( selectionModel );
    }
	
	/**
	 * Checkbox Header
	 */
	private class CheckboxHeader extends Header<Boolean> {
		Boolean checkedState = false;
		
	    public CheckboxHeader(CheckboxCell cell, boolean isChecked) {
	        super(cell);
	        checkedState = isChecked;
	    }
	    
	    public void setValue(boolean isChecked) {
	    	checkedState = isChecked;
	    }
	    
	    @Override
	    public Boolean getValue() {
	    	return checkedState;
	    }
	 
	    @Override
	    public void onBrowserEvent(Context context, Element elem, NativeEvent event) {
	        InputElement input = elem.getFirstChild().cast();
	        checkedState = input.isChecked();
	        headerUpdated(checkedState);
	    }
	    
	    protected void headerUpdated(boolean isChecked) {
	    	// override this method in defining class
	    }
	}
}
