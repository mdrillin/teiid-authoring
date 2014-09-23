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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.authoring.client.services.DataSourceRpcService;
import org.teiid.authoring.client.services.QueryRpcService;
import org.teiid.authoring.client.services.VdbRpcService;
import org.teiid.authoring.client.services.rpc.IRpcServiceInvocationHandler;
import org.teiid.authoring.client.utils.DdlHelper;
import org.teiid.authoring.client.widgets.ColRow;
import org.teiid.authoring.client.widgets.ColumnNamesTable;
import org.teiid.authoring.client.widgets.DataSourceNamesTable;
import org.teiid.authoring.client.widgets.TablesProcNamesTable;
import org.teiid.authoring.client.widgets.VisibilityRadios;
import org.teiid.authoring.share.beans.Constants;
import org.teiid.authoring.share.beans.QueryColumnBean;
import org.teiid.authoring.share.beans.QueryColumnResultSetBean;
import org.teiid.authoring.share.beans.QueryTableProcBean;
import org.teiid.authoring.share.beans.VdbDetailsBean;
import org.teiid.authoring.share.beans.ViewModelRequestBean;
import org.teiid.authoring.share.services.StringUtils;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * CreateDataServiceScreen - used for creation of Data Services
 *
 */
@Dependent
@Templated("./CreateDataServiceScreen.html#page")
@WorkbenchScreen(identifier = "CreateDataServiceScreen")
public class CreateDataServiceScreen extends Composite {

	private Map<String,String> sourceNameToJndiMap = new HashMap<String,String>();
	private Map<String,String> shortToLongTableNameMap = new HashMap<String,String>();
	
	private String selectedDataSource = null;
	private String selectedTable = null;
	
    @Inject
    private PlaceManager placeManager;
    
    @Inject
    protected DataSourceRpcService dataSourceService;
    @Inject
    protected QueryRpcService queryService;
    @Inject
    protected VdbRpcService vdbService;
    
    @Inject @DataField("textbox-create-service-name")
    protected TextBox serviceNameTextBox;
    
    @Inject @DataField("textarea-create-service-description")
    protected TextArea serviceDescriptionTextBox;
    
    @Inject @DataField("radios-create-service-visibility")
    protected VisibilityRadios serviceVisibleRadios;
    
    @Inject @DataField("label-create-service-status")
    protected Label statusLabel;
    
    @Inject @DataField("btn-create-service-createDdl")
    protected Button createDdlButton;
    
    @Inject @DataField("btn-create-service-create")
    protected Button createServiceButton;
    
    @Inject @DataField("btn-create-service-cancel")
    protected Button cancelButton;
    
    @Inject @DataField("btn-create-service-manage-sources")
    protected Button manageSourceButton;
    
    @Inject @DataField("table-datasources")
    protected DataSourceNamesTable dsNamesTable;
    
    @Inject @DataField("table-tables-procs")
    protected TablesProcNamesTable tablesAndProcsTable;

    @Inject @DataField("table-columns")
    protected ColumnNamesTable columnsTable;
    
    @Inject @DataField("textarea-create-service-viewDdl")
    protected TextArea viewDdlTextArea;
    
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
    	serviceVisibleRadios.setValue(true);
    	tablesAndProcsTable.clear();
    	columnsTable.clear();
    	
    	doGetQueryableSources(false);

    	// SelectionModel to handle Source selection 
    	final SingleSelectionModel<String> dsSelectionModel = new SingleSelectionModel<String>();
    	dsNamesTable.setSelectionModel(dsSelectionModel); 
    	dsSelectionModel. addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
    		public void onSelectionChange( SelectionChangeEvent event) { 
    			tablesAndProcsTable.clear();
    			columnsTable.clear();
    			String selected = dsSelectionModel.getSelectedObject();
    			selectedDataSource = selected;
    			if (selected != null) {
    				doGetTablesAndProcs(selected);
    			}
    		} });

    	// SelectionModel to handle Table-procedure selection 
    	final SingleSelectionModel<String> tableSelectionModel = new SingleSelectionModel<String>();
    	tablesAndProcsTable.setSelectionModel(tableSelectionModel); 
    	tableSelectionModel. addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
    		public void onSelectionChange( SelectionChangeEvent event) { 
    			String selected = tableSelectionModel.getSelectedObject();
    			selectedTable = selected;
    			if (selected != null) {
    				String theSource = dsSelectionModel.getSelectedObject();
    				String longTableName = shortToLongTableNameMap.get(selected);
    				doGetTableColumns(theSource, longTableName, 1);
    			}
    		} });
    	
    	serviceNameTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
            	updateStatus();
            }
        });
    	
    	viewDdlTextArea.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
            	updateStatus();
            }
        });
    	
    	// Set the initial status
    	updateStatus();
    	
    }
    
    private void updateStatus( ) {
    	boolean isOK = true;
    	
    	// Warning for missing service name
    	String serviceName = serviceNameTextBox.getText();
    	if(StringUtils.isEmpty(serviceName)) {
    		statusLabel.setText("Please enter a name for your service");
    		isOK = false;
    	}
    	
		// Check for missing view DDL - if serviceName passed
    	if(isOK) {
    		String viewDdl = viewDdlTextArea.getText();
    		if(StringUtils.isEmpty(viewDdl)) {
    			statusLabel.setText("Please create the View markup");
    			isOK = false;
    		}
    	}
    	
    	if(isOK) {
    		statusLabel.setText("Click 'Create Data Service' to add your service");
    		createServiceButton.setEnabled(true);
    	} else {
    		createServiceButton.setEnabled(false);
    	}
    }
        
    /**
     * Populate the DataSource ListBox
     */
    protected void doGetDataSourceNames() {
    	dataSourceService.getDataSourceNames(new IRpcServiceInvocationHandler<List<String>>() {
    		@Override
    		public void onReturn(List<String> sourceNames) {
    			dsNamesTable.setData(sourceNames);
    		}
    		@Override
    		public void onError(Throwable error) {
    			//             notificationService.sendErrorNotification(i18n.format("addSourceModelDialog.error-populating-datasources"), error); //$NON-NLS-1$
    		}
    	});
    }
    
    /**
     * Populate the DataSource ListBox
     */
    protected void doGetQueryableSources(boolean teiidOnly) {
        dataSourceService.getQueryableDataSourceMap(new IRpcServiceInvocationHandler<Map<String,String>>() {
            @Override
            public void onReturn(Map<String,String> sourceToJndiMap) {
            	sourceNameToJndiMap.clear();
            	sourceNameToJndiMap.putAll(sourceToJndiMap);
            	List<String> dsList = new ArrayList<String>();
            	for(String dsName : sourceNameToJndiMap.keySet()) {
            		if(dsName.startsWith(Constants.SERVICE_SOURCE_VDB_PREFIX)) {
            			dsList.add(dsName);
            		}
            	}
            	dsNamesTable.setData(dsList);
            }
            @Override
            public void onError(Throwable error) {
                //notificationService.sendErrorNotification(i18n.format("queryTest.error-populating-sources"), error); //$NON-NLS-1$
            }
        });
    }
    
    /**
     * Get the Tables and Procs for the supplied data source
     * @param dataSourceName the name of the source
     */
    protected void doGetTablesAndProcs(String dataSourceName) {
		queryService.getTablesAndProcedures(sourceNameToJndiMap.get(dataSourceName), dataSourceName, new IRpcServiceInvocationHandler<List<QueryTableProcBean>>() {
			@Override
			public void onReturn(List<QueryTableProcBean> tablesAndProcs) {
				List<String> nameList = new ArrayList<String>();
				for(QueryTableProcBean tp : tablesAndProcs) {
					String longName = tp.getName();
					if(longName!=null && longName.contains(".PUBLIC.")) {
						String shortName = longName.substring(longName.indexOf(".PUBLIC.")+".PUBLIC.".length());
						shortToLongTableNameMap.put(shortName, longName);
						nameList.add(shortName);
					}
				}
				tablesAndProcsTable.setData(nameList);
			}
			@Override
			public void onError(Throwable error) {
				//notificationService.sendErrorNotification(i18n.format("queryTest.error-populating-tables"), error); //$NON-NLS-1$
			}
		});

    }
    
    /**
     * Search for QueryColumns based on the current page and filter settings.
     * @param page
     */
    protected void doGetTableColumns(String source, String table, int page) {
    	String filterText = "";
//    	String filterText = (String)stateService.get(ApplicationStateKeys.QUERY_COLUMNS_FILTER_TEXT,"");
//        stateService.put(ApplicationStateKeys.QUERY_COLUMNS_PAGE, currentQueryColumnsPage);
        
//        if(table!=null && table.equalsIgnoreCase(NO_TABLES_FOUND)) {
//        	clearColumnsTable();
//        } else {
        	queryService.getQueryColumnResultSet(page, filterText, sourceNameToJndiMap.get(source), table,
        			new IRpcServiceInvocationHandler<QueryColumnResultSetBean>() {
        		@Override
        		public void onReturn(QueryColumnResultSetBean data) {
        			List<ColRow> colList = new ArrayList<ColRow>();
        			List<QueryColumnBean> qColumns = data.getQueryColumns();
        			for(QueryColumnBean col : qColumns) {
        				ColRow cRow = new ColRow();
        				cRow.setName(col.getName());
        				colList.add(cRow);
        			}
        			columnsTable.setData(colList);
        		}
        		@Override
        		public void onError(Throwable error) {
//        			notificationService.sendErrorNotification(i18n.format("queryTest.error-fetching-columns"), error); //$NON-NLS-1$
//        		    noColumnsMessage.setVisible(true);
//        		    columnFetchInProgressMessage.setVisible(false);
        		}
        	});
//        }

    }
    
    /**
     * Event handler that fires when the user clicks the showView button.
     * @param event
     */
    @EventHandler("btn-create-service-createDdl")
    public void onShowViewButtonClick(ClickEvent event) {
    	StringBuilder sb = new StringBuilder();
    	sb.append(" DataSource: ");
    	String theSource = (selectedDataSource==null) ? "NULL" : selectedDataSource;
    	sb.append(theSource+"\n");
    	
    	sb.append(" Table: ");
    	String theTable = (selectedTable==null) ? "NULL" : selectedTable;
    	sb.append(theTable+"\n");
    	
    	sb.append(" Columns: ");
    	String colString = columnsTable.getSelectedRowString();
    	String theCols = (colString==null) ? "NONE SELECTED" : colString;
    	sb.append(theCols+"\n");
    	
    	List<String> colNames = columnsTable.getSelectedColumnNames();
    	
    	String viewString = DdlHelper.getViewDdl(Constants.SERVICE_VIEW_NAME, null, theTable, colNames);
    	viewDdlTextArea.setText(viewString);  
    	
    	updateStatus();
    }
    
    /**
     * Event handler that fires when the user clicks the Create button.
     * @param event
     */
    @EventHandler("btn-create-service-create")
    public void onPublishServiceButtonClick(ClickEvent event) {
    	doCreateService();
    }
    
    private void doCreateService() {
    	// A separate view model is created for each "service".  This makes editing easier, and easier to debug issues
    	
    	String serviceName = this.serviceNameTextBox.getText();
    	String serviceDescription = this.serviceDescriptionTextBox.getText();
    	final String viewModel = serviceName;
    	String viewDdl = viewDdlTextArea.getText();
    	boolean isVisible = serviceVisibleRadios.isVisibleSelected();
    	
    	ViewModelRequestBean viewModelRequest = new ViewModelRequestBean();
    	viewModelRequest.setName(serviceName);
    	viewModelRequest.setDescription(serviceDescription);
    	viewModelRequest.setDdl(viewDdl);
    	viewModelRequest.setVisible(isVisible);
    	    	
        vdbService.addOrReplaceViewModelAndRedeploy("ServicesVDB", 1, viewModelRequest, new IRpcServiceInvocationHandler<VdbDetailsBean>() {
            @Override
            public void onReturn(VdbDetailsBean vdbDetailsBean) {            	
//            	currentVdbDetails = vdbDetailsBean;
//            	setVdbStatus(vdbDetailsBean);
//                updatePager(vdbDetailsBean);
//                updateVdbModelsTable(vdbDetailsBean);
//                doSetButtonEnablements();
            	
            	Map<String,String> parameters = new HashMap<String,String>();
            	parameters.put(Constants.SERVICE_NAME_KEY, viewModel);
            	placeManager.goTo(new DefaultPlaceRequest("DataServiceDetailsScreen",parameters));
            }
            @Override
            public void onError(Throwable error) {
//                notificationService.sendErrorNotification(i18n.format("vdbdetails.error-adding-view-model"), error); //$NON-NLS-1$
//                addModelInProgressMessage.setVisible(false);
//                setVdbStatus(currentVdbDetails);
//                updatePager(currentVdbDetails);
//                updateVdbModelsTable(currentVdbDetails);
//                doSetButtonEnablements();
            }
        });           	
    }
    
    /**
     * Event handler that fires when the user clicks the Manage Sources button.
     * @param event
     */
    @EventHandler("btn-create-service-manage-sources")
    public void onManageSourcesButtonClick(ClickEvent event) {
    	placeManager.goTo("ManageSourcesScreen");
    }
    
    /**
     * Event handler that fires when the user clicks the Cancel button.
     * @param event
     */
    @EventHandler("btn-create-service-cancel")
    public void onCancelButtonClick(ClickEvent event) {
    	placeManager.goTo("DataServicesLibraryScreen");
    }
        
}
