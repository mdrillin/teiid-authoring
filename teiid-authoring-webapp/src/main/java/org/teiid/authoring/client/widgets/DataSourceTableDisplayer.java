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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.kie.uberfire.client.tables.PagedTable;
import org.teiid.authoring.client.services.DataSourceRpcService;
import org.teiid.authoring.client.services.rpc.IRpcServiceInvocationHandler;
import org.teiid.authoring.share.beans.DataSourcePageRow;
import org.uberfire.paging.PageRequest;
import org.uberfire.paging.PageResponse;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

/**
 * Composite for display of DataSources
 */
public class DataSourceTableDisplayer extends Composite {

	private static int NUMBER_ROWS = 10;
	private static String COLUMN_HEADER_NAME = "Name";
	private static String COLUMN_HEADER_TYPE = "Type";
	
    @Inject
    protected DataSourceRpcService dataSourceService;

    protected FlowPanel panel = new FlowPanel();
    protected Label label = new Label();

    private PagedTable<DataSourcePageRow> table;

    public DataSourceTableDisplayer() {
        initWidget( panel );
        panel.add(createTablePanel());
    }
    
    /**
     * Set the data provider for the table
     * @param dataProvider the data provider
     */
    public void setDataProvider(AsyncDataProvider<DataSourcePageRow> dataProvider) {
    	table.setDataProvider(dataProvider);
    }

    /**
     * Create the panel
     * @return the panel widget
     */
    protected Widget createTablePanel() {
    	table = new PagedTable<DataSourcePageRow>(NUMBER_ROWS);
        TextColumn<DataSourcePageRow> nameColumn = new TextColumn<DataSourcePageRow>() {
            public String getValue( DataSourcePageRow row ) {
                return row.getName();
            }
        };
        table.addColumn( nameColumn, COLUMN_HEADER_NAME );

        TextColumn<DataSourcePageRow> typeColumn = new TextColumn<DataSourcePageRow>() {
            public String getValue( DataSourcePageRow row ) {
                return row.getType();
            }
        };
        table.addColumn( typeColumn, COLUMN_HEADER_TYPE );

        final Button refreshButton = new Button();
        refreshButton.setIcon( IconType.REFRESH );
        refreshButton.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                table.refresh();
            }
        } );
        table.getToolbar().add( refreshButton );
        
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(table);
        return verticalPanel;
    }
    
    /**
     * Set the data provider after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	table.setDataProvider(createDataProvider());
    }

    /**
     * Create DataProvider for the DataSources table
     * @return the data provider
     */
    private AsyncDataProvider<DataSourcePageRow> createDataProvider() {
    	AsyncDataProvider<DataSourcePageRow> dataProvider = new AsyncDataProvider<DataSourcePageRow>() {
    		protected void onRangeChanged( HasData<DataSourcePageRow> display ) {
    			final Range range = display.getVisibleRange();
    			PageRequest request = new PageRequest( range.getStart(),
    					range.getLength() );

    			dataSourceService.getDSs(request, "test", new IRpcServiceInvocationHandler<PageResponse<DataSourcePageRow>>() {
    				@Override
    				public void onReturn(final PageResponse<DataSourcePageRow> response) {
    					updateRowCount( response.getTotalRowSize(),
    							response.isTotalRowSizeExact() );
    					updateRowData( response.getStartRowIndex(),
    							response.getPageRowList() );
    					//notification.fire( new NotificationEvent( M2RepoEditorConstants.INSTANCE.RefreshedSuccessfully() ) );
    					//notification.fire( new NotificationEvent( "test" ) );
    				}
    				@Override
    				public void onError(Throwable error) {
    					//    					notificationService.completeProgressNotification(notificationBean.getUuid(),
    					//    							i18n.format("datasources.create-error"), //$NON-NLS-1$
    					//    							error);
    				}
    			});
    		}
    	};

    	return dataProvider;
    }

}
