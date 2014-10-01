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

import java.util.List;

import javax.inject.Inject;

import org.kie.uberfire.client.tables.PagedTable;
import org.teiid.authoring.client.services.QueryRpcService;
import org.teiid.authoring.client.services.rpc.IRpcServiceInvocationHandler;
import org.teiid.authoring.share.beans.QueryResultPageRow;
import org.uberfire.paging.PageRequest;
import org.uberfire.paging.PageResponse;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.dom.client.Style;
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
 * Composite for display of Query Results
 */
public class QueryResultPagedTableDisplayer extends Composite {

	private static int NUMBER_ROWS_DEFAULT = 10;
	private static String COLUMN_HEADER_NAME = "Name";
	
	private int numberRows = NUMBER_ROWS_DEFAULT;
	
    @Inject
    protected QueryRpcService queryService;

    protected FlowPanel panel = new FlowPanel();
    private Widget tableWidget;
    
    protected Label label = new Label();

    private PagedTable<QueryResultPageRow> table;

    public QueryResultPagedTableDisplayer() {
        initWidget( panel );
        tableWidget = createTablePanel();
        panel.add(tableWidget);
    }
    
    public void setNumberRows(int numberRows) {
		this.numberRows = numberRows;
	}
    
    public void setWidth(String widthStr) {
    	table.setWidth(widthStr);
	}

	/**
     * Set the data provider for the table
     * @param dataProvider the data provider
     */
    public void setDataProvider(String dataSource, String sql) {
    	panel.remove(tableWidget);
    	updateTableForQuery(dataSource,sql);
    }

    /**
     * Create the panel
     * @return the panel widget
     */
    protected Widget createTablePanel() {
    	table = new PagedTable<QueryResultPageRow>(this.numberRows);
        TextColumn<QueryResultPageRow> nameColumn = new TextColumn<QueryResultPageRow>() {
            public String getValue( QueryResultPageRow row ) {
                return row.getColumnData().get(0);
            }
        };
        table.addColumn( nameColumn, COLUMN_HEADER_NAME );

        final Button refreshButton = new Button();
        refreshButton.setIcon( IconType.REFRESH );
        refreshButton.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                table.refresh();
            }
        } );
        table.getToolbar().add( refreshButton );
        table.setWidth("100%");
        
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(table);
        return verticalPanel;
    }
    
    /**
     * This recreates the panel based on the query.  The number of columns and column labels will vary between queries
     * @return the panel widget
     */
    protected void updateTableForQuery(final String dataSource, final String sql) {
    	final VerticalPanel verticalPanel = new VerticalPanel();
    	final int nRows = this.numberRows;
    	
		queryService.getColumnNames(dataSource, sql, new IRpcServiceInvocationHandler<List<String>>() {
			@Override
			public void onReturn(final List<String> colNames) {
		    	table = new PagedTable<QueryResultPageRow>(nRows);
		    	for(int i=0; i<colNames.size(); i++) {
		    		final int colIndx = i;
		    		TextColumn<QueryResultPageRow> col = new TextColumn<QueryResultPageRow>() {
		    			public String getValue( QueryResultPageRow row ) {
		    				return row.getColumnData().get(colIndx);
		    			}
		    		};
		    		table.addColumn(col,colNames.get(i));
		    		table.setColumnWidth(col, 200, Style.Unit.PX);
		    	}
	    		
	            final Button refreshButton = new Button();
	            refreshButton.setIcon( IconType.REFRESH );
	            refreshButton.addClickHandler( new ClickHandler() {
	                @Override
	                public void onClick( ClickEvent event ) {
	                    table.refresh();
	                }
	            } );
	            table.getToolbar().add( refreshButton );
	            table.setWidth("100%");
	           
	            verticalPanel.add(table);
	            tableWidget = verticalPanel;
	            
	        	panel.add(tableWidget);
	        	table.setDataProvider(createDataProvider(dataSource,sql));
				//notification.fire( new NotificationEvent( M2RepoEditorConstants.INSTANCE.RefreshedSuccessfully() ) );
				//notification.fire( new NotificationEvent( "test" ) );
			}
			@Override
			public void onError(Throwable error) {
			}
		});
		
    }
    
    /**
     * Create DataProvider for the DataSources table
     * @return the data provider
     */
    private AsyncDataProvider<QueryResultPageRow> createDataProvider(final String source, final String sql) {
    	AsyncDataProvider<QueryResultPageRow> dataProvider = new AsyncDataProvider<QueryResultPageRow>() {
    		protected void onRangeChanged( HasData<QueryResultPageRow> display ) {
    			final Range range = display.getVisibleRange();
    			PageRequest request = new PageRequest( range.getStart(),
    					range.getLength() );

    			queryService.getQueryResults(request, source, sql, new IRpcServiceInvocationHandler<PageResponse<QueryResultPageRow>>() {
    				@Override
    				public void onReturn(final PageResponse<QueryResultPageRow> response) {
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
