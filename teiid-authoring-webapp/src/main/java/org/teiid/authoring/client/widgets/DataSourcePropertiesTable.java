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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.teiid.authoring.share.beans.DataSourcePropertyBean;
import org.teiid.authoring.share.services.StringUtils;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.Column;
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
public class DataSourcePropertiesTable extends Composite {

	private static String COLUMN_HEADER_NAME = "Name";
	private static String COLUMN_HEADER_VALUE = "Value";

    protected FlowPanel panel = new FlowPanel();
    protected Label label = new Label();

    private SimpleTable<DataSourcePropertyBean> table;

	private static final String PASSWORD_KEY = "password"; //$NON-NLS-1$
	
    public DataSourcePropertiesTable() {
        initWidget( panel );
        panel.add(createTablePanel());
    }
    
    /**
     * Create the panel
     * @return the panel widget
     */
    protected Widget createTablePanel() {
    	table = new SimpleTable<DataSourcePropertyBean>();
        TextColumn<DataSourcePropertyBean> nameColumn = new TextColumn<DataSourcePropertyBean>() {
            public String getValue( DataSourcePropertyBean row ) {
                return row.getName();
            }
        };
        table.addColumn( nameColumn, COLUMN_HEADER_NAME );
        table.setColumnWidth(nameColumn, 200, Unit.PX);

        Column<DataSourcePropertyBean, String> valueColumn = new Column<DataSourcePropertyBean, String>(new TextInputCell()) {
        	@Override
        	public String getValue(DataSourcePropertyBean object) {
        		return object.getValue();
        	}
        };
        FieldUpdater<DataSourcePropertyBean, String> valueUpdater = new FieldUpdater<DataSourcePropertyBean, String>() {
        	@Override
        	public void update(int index, DataSourcePropertyBean object, String value) {
        		object.setValue(value);
        	}
        };
        valueColumn.setFieldUpdater(valueUpdater);
        
        table.addColumn( valueColumn, COLUMN_HEADER_VALUE );
        table.setColumnWidth(valueColumn, 200, Unit.PX);
        
    	table.setWidth("420px");
    	table.setHeight("200px");
    	
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(table);
        return verticalPanel;
    }
    
    public List<DataSourcePropertyBean> getBeansWithRequiredOrNonDefaultValue() {
    	List<DataSourcePropertyBean> tableRows = getData();
    	
    	List<DataSourcePropertyBean> resultBeans = new ArrayList<DataSourcePropertyBean>();
    	for(DataSourcePropertyBean propBean : tableRows) {
    		if(propBean.isRequired()) {
    			resultBeans.add(propBean);
    		} else {
        		String defaultValue = propBean.getDefaultValue();
        		String value = propBean.getValue();
        		if(!StringUtils.valuesAreEqual(value, defaultValue)) {
        			resultBeans.add(propBean);
        		}
    		}
    	}
    	return resultBeans;
    }
    
    /**
     * Adds a single row to the table.
     * @param dataSourcePropertyBean
     */
//    public void addRow(final DataSourcePropertyBean dataSourcePropertyBean) {
//        int rowIdx = this.rowElements.size();
//
//        InlineLabel name = new InlineLabel(dataSourcePropertyBean.getName());
//
//        TextBox valueTextBox = null;
//        String propName = dataSourcePropertyBean.getName();
//        if( propName!=null && propName.equalsIgnoreCase(PASSWORD_KEY) ) {
//        	valueTextBox = new PasswordTextBox();
//        } else {
//        	valueTextBox = new TextBox();
//        }
//        valueTextBox.setText(dataSourcePropertyBean.getValue());
//        
//        valueTextBox.addKeyUpHandler(new KeyUpHandler() {
//            @Override
//            public void onKeyUp(KeyUpEvent event) {
//            	updatePropertyValues();
//            }
//        });
//        valueTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
//            @Override
//            public void onValueChange(ValueChangeEvent<String> event) {
//            	updatePropertyValues();
//            }
//        });
//
//        add(rowIdx, 0, name);
//        add(rowIdx, 1, valueTextBox);
//        
//        rowNameMap.put(rowIdx,valueTextBox);
//        rowBeanMap.put(rowIdx,dataSourcePropertyBean);
//        
//    }
    
//    public void updatePropertyValues() {
//    	for(int i=0; i<this.rowElements.size(); i++) {
//    		TextBox textBox = this.rowNameMap.get(i);
//    		DataSourcePropertyBean propBean = this.rowBeanMap.get(i);
//    		propBean.setValue(textBox.getText());
//    	}
//        ValueChangeEvent.fire(this, null);
//    }
    
    public void clear() {
    	setData(Collections.EMPTY_LIST);
    }
    
    public List<DataSourcePropertyBean> getData( ) {
    	return table.getRowData();
    }
    
    public void setData(List<DataSourcePropertyBean> rows) {
    	table.setRowData(rows);
    }

    public void setSelectionModel( final SelectionModel selectionModel ) {
        table.setSelectionModel( selectionModel );
    }
    
}
