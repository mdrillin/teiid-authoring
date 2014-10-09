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

import org.teiid.authoring.client.resources.AppResource;
import org.teiid.authoring.client.resources.ImageHelper;
import org.teiid.authoring.share.beans.DataSourcePageRow;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.view.client.SelectionModel;

/**
 * Composite for display of DataSource names
 */
public class DataSourceListWidget extends Composite {

    protected ScrollPanel scrollPanel = new ScrollPanel();
    protected Label label = new Label();

    private CellList<DataSourcePageRow> dsList;

    public DataSourceListWidget() {
        initWidget( scrollPanel );
        createListPanel();
    }
    
    /**
     * Create the panel
     * @return the panel widget
     */
    protected void createListPanel() {
        // Create a CellList.
        DataSourceCell contactCell = new DataSourceCell( );
        
    	dsList = new CellList<DataSourcePageRow>(contactCell);
    	dsList.setPageSize(3);
    	//dsList.setKeyboardPagingPolicy(KeyboardPagingPolicy.INCREASE_RANGE);
    	//dsList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
    	
        scrollPanel.add(dsList);
        scrollPanel.setHeight("600px");
    }
    
    public void clear() {
    	setData(Collections.<DataSourcePageRow>emptyList());
    }
    
    public void setData(List<DataSourcePageRow> rows) {
    	dsList.setRowData(rows);
    }
    
    public List<DataSourcePageRow> getData( ) {
    	return dsList.getVisibleItems();
    }
    
    public List<String> getDataSourceNames() {
    	List<String> dsNames = new ArrayList<String>();
    	for(DataSourcePageRow dsRow : getData()) {
    		dsNames.add(dsRow.getName());
    	}
    	return dsNames;
    }
    
    public void setSelection(String dsName) {
    	SelectionModel selModel = dsList.getSelectionModel();
    	for(DataSourcePageRow dSource : getData()) {
    		if(dSource.getName().equals(dsName)) {
    			selModel.setSelected(dSource, true);
    			break;
    		}
    	}
    }
    
    public void setSelectionModel( final SelectionModel selectionModel ) {
    	dsList.setSelectionModel( selectionModel );
    }
    
    /**
     * The Cell used to render a {@link ContactInfo}.
     */
    static class DataSourceCell extends AbstractCell<DataSourcePageRow> {

      /**
       * The html of the images used for ok or error.
       */
        private final String okImageHtml;
        private final String errorImageHtml;

      public DataSourceCell( ) {
        this.okImageHtml = AbstractImagePrototype.create(AppResource.INSTANCE.images().okIcon32x32Image()).getHTML();
        this.errorImageHtml = AbstractImagePrototype.create(AppResource.INSTANCE.images().errorIcon32x32Image()).getHTML();
      }

      @Override
      public void render(Context context, DataSourcePageRow value, SafeHtmlBuilder sb) {
        // Value can be null, so do a null check..
        if (value == null) {
          return;
        }

        String statusImageHtml = null;
        if(value.hasVdb()) {
        	statusImageHtml = this.okImageHtml;
        } else {
        	statusImageHtml = this.errorImageHtml;
        }
        String dType = value.getType();
        String dTypeImageHtml = ImageHelper.getInstance().getDataSourceImageHtmlForType(dType);
                
        sb.appendHtmlConstant("<table>");

        // Add the contact image.
        sb.appendHtmlConstant("<tr><td>");
        sb.appendHtmlConstant(statusImageHtml);
        sb.appendHtmlConstant("</td>");

        sb.appendHtmlConstant("<td>");
        sb.appendHtmlConstant(dTypeImageHtml);
        sb.appendHtmlConstant("</td>");
        
        // Add the name and address.
        sb.appendHtmlConstant("<td class='h6'><h6>");
        sb.appendEscaped(value.getName());
        sb.appendHtmlConstant("</h6></td></tr></table>");
      }
    }  

}
