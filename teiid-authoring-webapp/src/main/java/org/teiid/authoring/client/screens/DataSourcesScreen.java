/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.teiid.authoring.client.screens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.authoring.client.services.DataSourceRpcService;
import org.teiid.authoring.client.services.rpc.IRpcServiceInvocationHandler;
import org.teiid.authoring.share.beans.DataSourceResultSetBean;
import org.teiid.authoring.share.beans.DataSourceSummaryBean;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;

/**
 * The default "DataSources" page.
 *
 * @author mdrillin@redhat.com
 */
@Dependent
@Templated("./DataSourcesScreen.html#page")
@WorkbenchScreen(identifier = "DataSourcesScreen")
public class DataSourcesScreen extends Composite {

//    @Inject @DataField("to-datasources-page")
//    private TransitionAnchor<DataSourcesScreen> toDataSourcesPage;
//    @Inject @DataField("to-vdbs-page")
//    private TransitionAnchor<VirtualDatabasesPage> toVDBsPage;
//    @Inject @DataField("to-querytest-page")
//    private TransitionAnchor<QueryTestPage> toQueryTestPage;

//    @Inject
//    protected ClientMessages i18n;
    @Inject
    protected DataSourceRpcService dataSourceService;
//    @Inject
//    protected NotificationService notificationService;
//    @Inject
//    protected ApplicationStateService stateService;
 
    @Inject @DataField("btn-get-sources")
    protected Button getSourcesButton;
    
    @Inject @DataField("textbox-sources")
    protected TextBox sourcesTextBox;
	
//    @Inject @DataField("datasource-search-box")
//    protected TextBox searchBox;
//
//    @Inject @DataField("btn-add-source")
//    protected Button addSourceButton;
//    
//    @Inject @DataField("btn-remove-source")
//    protected Button removeSourceButton;
//    @Inject
//    DeleteDataSourceDialog deleteDataSourceDialog;
    
//    @Inject @DataField("btn-test-source")
//    protected Button testSourceButton;
//    
//    @Inject
//    protected Instance<AddDataSourceDialog> addDataSourceDialogFactory;
//    @Inject @DataField("btn-refresh-sources")
//    protected Button refreshSourcesButton;
//
//    @Inject @DataField("datavirt-datasources-none")
//    protected HtmlSnippet noDataSourcesMessage;
//    @Inject @DataField("datavirt-datasources-searching")
//    protected HtmlSnippet datasourceSearchInProgressMessage;
    @Inject @DataField("datavirt-datasources-table")
    protected FlexTable dataSourcesTable;
//
//    @Inject @DataField("datavirt-datasources-pager")
//    protected DVPager sourcesPager;

    private int currentDataSourcePage = 1;
    private Collection<String> allDsNames = new ArrayList<String>();
    private Map<String, Boolean> sourceTestableMap = new HashMap<String,Boolean>();

    @Override
    @WorkbenchPartTitle
    public String getTitle() {
      return "";
    }
    
    @WorkbenchPartView
    public IsWidget getView() {
        return this;
    }
    
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
//        searchBox.addKeyUpHandler(new KeyUpHandler() {
//            @Override
//            public void onKeyUp(KeyUpEvent event) {
//            	searchIfValueChanged();
//            }
//        });
//        sourcesPager.addValueChangeHandler(new ValueChangeHandler<Integer>() {
//            @Override
//            public void onValueChange(ValueChangeEvent<Integer> event) {
//            	doDataSourceFetch(event.getValue());
//            }
//        });
//        dataSourcesTable.addTableSortHandler(new TableSortEvent.Handler() {
//            @Override
//            public void onTableSort(TableSortEvent event) {
//            	doDataSourceFetch(currentDataSourcePage);
//            }
//        });
//        dataSourcesTable.addTableRowSelectionHandler(new TableRowSelectionEvent.Handler() {
//            @Override
//            public void onTableRowSelection(TableRowSelectionEvent event) {
//            	doSetSourceButtonEnablements();
//            }
//        });
//        deleteDataSourceDialog.addClickHandler(new ClickHandler() {
//            @Override
//            public void onClick(ClickEvent event) {
//                onDeleteDataSourceConfirm();
//            }
//        });
//        sourceTypesPager.addValueChangeHandler(new ValueChangeHandler<Integer>() {
//            @Override
//            public void onValueChange(ValueChangeEvent<Integer> event) {
//            	doGetDataSourceTypes(event.getValue());
//            }
//        });
//        dataSourceTypesTable.addTableSortHandler(new TableSortEvent.Handler() {
//            @Override
//            public void onTableSort(TableSortEvent event) {
//            	doGetDataSourceTypes(currentDataSourceTypesPage);
//            }
//        });
//        dataSourceTypesTable.addTableRowSelectionHandler(new TableRowSelectionEvent.Handler() {
//            @Override
//            public void onTableRowSelection(TableRowSelectionEvent event) {
//            	doSetSourceTypeButtonEnablements();
//            }
//        });
//        deleteDataSourceTypeDialog.addClickHandler(new ClickHandler() {
//            @Override
//            public void onClick(ClickEvent event) {
//                onDeleteDataSourceTypeConfirm();
//            }
//        });

    }

    /**
     * Event handler that fires when the user clicks the AddSource button.
     * @param event
     */
    @EventHandler("btn-get-sources")
    public void onGetSourcesClick(ClickEvent event) {
    	doGetDataSources();
    }
    
    /**
     * Populate the DataSource ListBox
     */
    protected void doGetDataSources() {
        dataSourceService.getDataSources(new IRpcServiceInvocationHandler<List<String>>() {
            @Override
            public void onReturn(List<String> sources) {
            	StringBuffer sb = new StringBuffer();
            	for(String ds : sources) {
            		sb.append(ds+" : ");
            	}
            	sourcesTextBox.setText(sb.toString());
            }
            @Override
            public void onError(Throwable error) {
//                notificationService.sendErrorNotification(i18n.format("addSourceModelDialog.error-populating-datasources"), error); //$NON-NLS-1$
            }
        });
    }
    
    /**
     * Event handler that fires when the user clicks the AddSource button.
     * @param event
     */
//    @EventHandler("btn-add-source")
//    public void onAddSourceClick(ClickEvent event) {
//        AddDataSourceDialog dialog = addDataSourceDialogFactory.get();
//        dialog.setCurrentDsNames(this.allDsNames);
//        dialog.addValueChangeHandler(new ValueChangeHandler<DataSourceDetailsBean>() {
//        	@Override
//        	public void onValueChange(ValueChangeEvent<DataSourceDetailsBean> event) {
//        		doCreateDataSource(event.getValue());
//        	}
//        });
//        dialog.show();
//    }
    
    /**
     * Creates a DataSource
     * @param dsDetailsBean the data source details
     */
//    private void doCreateDataSource(DataSourceDetailsBean detailsBean) {
//    	final String dsName = detailsBean.getName();
//        final NotificationBean notificationBean = notificationService.startProgressNotification(
//                i18n.format("datasources.creating-datasource-title"), //$NON-NLS-1$
//                i18n.format("datasources.creating-datasource-msg", dsName)); //$NON-NLS-1$
//        dataSourceService.createDataSource(detailsBean, new IRpcServiceInvocationHandler<Void>() {
//            @Override
//            public void onReturn(Void data) {
//                notificationService.completeProgressNotification(notificationBean.getUuid(),
//                        i18n.format("datasources.datasource-created"), //$NON-NLS-1$
//                        i18n.format("datasources.create-success-msg")); //$NON-NLS-1$
//
//                // Refresh Page
//                doDataSourceFetch(currentDataSourcePage);
//            }
//            @Override
//            public void onError(Throwable error) {
//                notificationService.completeProgressNotification(notificationBean.getUuid(),
//                        i18n.format("datasources.create-error"), //$NON-NLS-1$
//                        error);
//            }
//        });
//    }
    
    /**
     * Event handler that fires when the user clicks the RemoveSource button.
     * @param event
     */
//    @EventHandler("btn-remove-source")
//    public void onRemoveSourceClick(ClickEvent event) {
//    	Collection<String> dsNames = dataSourcesTable.getSelectedDataSources();
//        deleteDataSourceDialog.setDataSourceNames(dsNames);
//        deleteDataSourceDialog.show();
//    }
        
    /**
     * Called when the user confirms the dataSource deletion.
     */
//    private void onDeleteDataSourceConfirm() {
//    	Collection<String> dsNames = this.dataSourcesTable.getSelectedDataSources();
//    	String dsText = null;
//    	if(dsNames.size()==1) {
//    		dsText = "DataSource "+dsNames.iterator().next();
//    	} else {
//    		dsText = "DataSource(s)";
//    	}
//        final NotificationBean notificationBean = notificationService.startProgressNotification(
//                i18n.format("datasources.deleting-datasource-title"), //$NON-NLS-1$
//                i18n.format("datasources.deleting-datasource-msg", dsText)); //$NON-NLS-1$
//        dataSourceService.deleteDataSources(dsNames, new IRpcServiceInvocationHandler<Void>() {
//            @Override
//            public void onReturn(Void data) {
//                notificationService.completeProgressNotification(notificationBean.getUuid(),
//                        i18n.format("datasources.datasource-deleted"), //$NON-NLS-1$
//                        i18n.format("datasources.delete-success-msg")); //$NON-NLS-1$
//
//                // Deletion - go back to page 1 - delete could have made current page invalid
//                doDataSourceFetch(1);
//            }
//            @Override
//            public void onError(Throwable error) {
//                notificationService.completeProgressNotification(notificationBean.getUuid(),
//                        i18n.format("datasources.delete-error"), //$NON-NLS-1$
//                        error);
//            }
//        });
//    }
    
    /**
     * Event handler that fires when the user clicks the Test button.
     * @param event
     */
//    @EventHandler("btn-test-source")
//    public void onTestSourceClick(ClickEvent event) {
//    	// Get the selected source - set the application state.  Then go to Test page.
//    	String selectedSource = dataSourcesTable.getSelectedDataSources().iterator().next();
//		stateService.put(ApplicationStateKeys.QUERY_SOURCELIST_SELECTED, selectedSource);
//		
//		toQueryTestPage.click();
//    }

    /**
     * Event handler that fires when the user clicks the refresh button.
     * @param event
     */
//    @EventHandler("btn-refresh-sources")
//    public void onRefreshSourcesClick(ClickEvent event) {
//    	doDataSourceFetch(currentDataSourcePage);
//    }
    /**
     * Event handler that fires when the user clicks the AddSource button.
     * @param event
     */
//    @EventHandler("btn-add-source-type")
//    public void onAddSourceTypeClick(ClickEvent event) {
//        ImportDataSourceTypeDialog dialog = importDataSourceTypeDialog.get();
//        dialog.setCompletionHandler(new IImportCompletionHandler() {
//            @Override
//            public void onImportComplete() {
//                if (isAttached()) {
//                    refreshButton.click();
//                }
//            }
//        });
//        dialog.show();
//    }
    
    /**
     * Event handler that fires when the user clicks the Remove source type button.
     * @param event
     */
//    @EventHandler("btn-remove-source-type")
//    public void onRemoveSourceTypeClick(ClickEvent event) {
//    	Collection<String> dsNames = dataSourceTypesTable.getSelectedDataSourceTypes();
//        deleteDataSourceTypeDialog.setDataSourceTypeNames(dsNames);
//        deleteDataSourceTypeDialog.show();
//    }
        
    /**
     * Called when the user confirms the dataSource type deletion.
     */
//    private void onDeleteDataSourceTypeConfirm() {
//    	Collection<String> dsTypes = this.dataSourceTypesTable.getSelectedDataSourceTypes();
//    	String dsText = null;
//    	if(dsTypes.size()==1) {
//    		dsText = "DataSource Type "+dsTypes.iterator().next();
//    	} else {
//    		dsText = "DataSource Type(s)";
//    	}
//        final NotificationBean notificationBean = notificationService.startProgressNotification(
//                i18n.format("datasource-types.deleting-datasource-type-title"), //$NON-NLS-1$
//                i18n.format("datasource-types.deleting-datasource-type-msg", dsText)); //$NON-NLS-1$
//        dataSourceService.deleteTypes(dsTypes, new IRpcServiceInvocationHandler<Void>() {
//            @Override
//            public void onReturn(Void data) {
//                notificationService.completeProgressNotification(notificationBean.getUuid(),
//                        i18n.format("datasource-types.datasource-type-deleted"), //$NON-NLS-1$
//                        i18n.format("datasource-types.delete-success-msg")); //$NON-NLS-1$
//
//                // Deletion - go back to page 1 - delete could have made current page invalid
//            	doGetDataSourceTypes(1);
//            }
//            @Override
//            public void onError(Throwable error) {
//                notificationService.completeProgressNotification(notificationBean.getUuid(),
//                        i18n.format("datasource-types.delete-error"), //$NON-NLS-1$
//                        error);
//            }
//        });
//    }
    
    /**
     * Event handler that fires when the user clicks the refresh button.
     * @param event
     */
//    @EventHandler("btn-refresh-source-types")
//    public void onRefreshClick(ClickEvent event) {
//    	doGetDataSourceTypes(this.currentDataSourceTypesPage);
//    }
    
    /**
     * Invoked on page showing
     *
     * @see org.jboss.datavirt.ui.client.local.pages.AbstractPage#onPageShowing()
     */
//    @Override
//    protected void onPageShowing() {
//        String filterText = (String) stateService.get(ApplicationStateKeys.DATASOURCES_FILTER_TEXT, ""); //$NON-NLS-1$
//        Integer sourcesPage = (Integer) stateService.get(ApplicationStateKeys.DATASOURCES_PAGE, 1);
//        SortColumn sourcesSortColumn = (SortColumn) stateService.get(ApplicationStateKeys.DATASOURCES_SORT_COLUMN, dataSourcesTable.getDefaultSortColumn());
//
//    	this.searchBox.setValue(filterText);
//    	this.dataSourcesTable.sortBy(sourcesSortColumn.columnId, sourcesSortColumn.ascending);
//    	
//        // Kick off an dataSource retrieval
//    	doDataSourceFetch(sourcesPage);
//
//    
//        Integer sourceTypesPage = (Integer) stateService.get(ApplicationStateKeys.DATASOURCE_TYPES_PAGE, 1);
//        SortColumn sourceTypesSortColumn = (SortColumn) stateService.get(ApplicationStateKeys.DATASOURCE_TYPES_SORT_COLUMN, dataSourceTypesTable.getDefaultSortColumn());
//
//    	this.dataSourceTypesTable.sortBy(sourceTypesSortColumn.columnId, sourceTypesSortColumn.ascending);
//    	
//        // Kick off an dataSource retrieval
//    	doGetDataSourceTypes(sourceTypesPage);
//    
//    
//    }

//    private void doSetSourceButtonEnablements() {
//
//    	// Remove DataSource Button - enabled if at least one row is selected.
//    	int selectedRows = this.dataSourcesTable.getSelectedDataSources().size();
//    	if(selectedRows==0) {
//    		removeSourceButton.setEnabled(false);
//    	} else {
//    		removeSourceButton.setEnabled(true);
//    	}
//    	
//    	// Test DataSource Button - enabled if only one row is selected - and source is testable
//    	if(selectedRows==1) {
//    		String selectedSource = this.dataSourcesTable.getSelectedDataSources().iterator().next();
//    		boolean isTestable = this.sourceTestableMap.get(selectedSource);
//    		testSourceButton.setEnabled(isTestable);
//    	} else {
//    		testSourceButton.setEnabled(false);
//    	}
//    }

//    private void searchIfValueChanged() {
//    	// Current Search Text state
//        String appFilterText = (String) stateService.get(ApplicationStateKeys.DATASOURCES_FILTER_TEXT, ""); //$NON-NLS-1$
//        // SearchBox text
//        String searchBoxText = this.searchBox.getText();
//        // Search if different
//        if(!StringUtils.equals(appFilterText, searchBoxText)) {
//        	doDataSourceFetch();
//        }    	
//    }
    
    /**
     * Search for the datasources based on the current filter settings and search text.
     */
//    protected void doDataSourceFetch() {
//    	doDataSourceFetch(1);
//    }

    /**
     * Search for Data Sources based on the current filter settings and search text.
     * @param page
     */
//    protected void doDataSourceFetch(int page) {
//    	onDataSourceFetchStarting();
//    	currentDataSourcePage = page;
//		final String filterText = this.searchBox.getValue();
//        final SortColumn currentSortColumn = this.dataSourcesTable.getCurrentSortColumn();
//
//        stateService.put(ApplicationStateKeys.DATASOURCES_FILTER_TEXT, filterText);
//        stateService.put(ApplicationStateKeys.DATASOURCES_PAGE, currentDataSourcePage);
//        stateService.put(ApplicationStateKeys.DATASOURCES_SORT_COLUMN, currentSortColumn);
//        
//        dataSourceService.search(filterText, page, currentSortColumn.columnId, !currentSortColumn.ascending,
//		        new IRpcServiceInvocationHandler<DataSourceResultSetBean>() {
//            @Override
//            public void onReturn(DataSourceResultSetBean data) {
//            	allDsNames = data.getAllDsNames();
//                updateDataSourcesTable(data);
//                updateDataSourcesPager(data);
//                doSetSourceButtonEnablements();
//            }
//            @Override
//            public void onError(Throwable error) {
//                notificationService.sendErrorNotification(i18n.format("datasources.error-searching"), error); //$NON-NLS-1$
//                noDataSourcesMessage.setVisible(true);
//                datasourceSearchInProgressMessage.setVisible(false);
//            }
//        });
//
//    }

    /**
     * Called when a new Data Source search is kicked off.
     */
//    protected void onDataSourceFetchStarting() {
//        this.sourcesPager.setVisible(false);
//        this.datasourceSearchInProgressMessage.setVisible(true);
//        this.dataSourcesTable.setVisible(false);
//        this.noDataSourcesMessage.setVisible(false);
//    }

    /**
     * Updates the table of Data Sources with the given data. Also updates sourceTestableMap.
     * @param data
     */
    protected void updateDataSourcesTable(DataSourceResultSetBean data) {
        this.dataSourcesTable.clear();
        this.sourceTestableMap.clear();
        //this.datasourceSearchInProgressMessage.setVisible(false);
        if (data.getDataSources().size() > 0) {
            for (DataSourceSummaryBean dataSourceSummaryBean : data.getDataSources()) {
                //this.dataSourcesTable.addRow(dataSourceSummaryBean);
                if(dataSourceSummaryBean.isTestable()) {
                	this.sourceTestableMap.put(dataSourceSummaryBean.getName(), true);
                } else {
                	this.sourceTestableMap.put(dataSourceSummaryBean.getName(), false);
                }
            }
            this.dataSourcesTable.setVisible(true);
        } else {
            //this.noDataSourcesMessage.setVisible(true);
        }
    }

    /**
     * Updates the pager with the given data.
     * @param data
     */
//    protected void updateDataSourcesPager(DataSourceResultSetBean data) {
//        int numPages = ((int) (data.getTotalResults() / data.getItemsPerPage())) + (data.getTotalResults() % data.getItemsPerPage() == 0 ? 0 : 1);
//        int thisPage = (data.getStartIndex() / data.getItemsPerPage()) + 1;
//        
//        long totalResults = data.getTotalResults();
//        
//        this.sourcesPager.setNumPages(numPages);
//        this.sourcesPager.setPageSize(Constants.DATASOURCES_TABLE_PAGE_SIZE);
//        this.sourcesPager.setTotalItems(totalResults);
//        
//        // setPage is last - does render
//        this.sourcesPager.setPage(thisPage);
//        
//        if(data.getDataSources().isEmpty()) {
//        	this.sourcesPager.setVisible(false);
//        } else {
//        	this.sourcesPager.setVisible(true);
//        }
//    }    
    
}
