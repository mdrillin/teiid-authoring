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
import org.teiid.authoring.share.beans.VdbModelBean;
import org.teiid.authoring.share.beans.ViewModelRequestBean;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * EditDataServiceScreen - used to edit existing Data Services
 *
 */
@Dependent
@Templated("./EditDataServiceScreen.html#page")
@WorkbenchScreen(identifier = "EditDataServiceScreen")
public class EditDataServiceScreen extends Composite {

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

    @Inject @DataField("textbox-edit-service-name")
    protected TextBox serviceNameTextBox;
    
    @Inject @DataField("textarea-edit-service-description")
    protected TextBox serviceDescriptionTextBox;
    
    @Inject @DataField("radios-edit-service-visibility")
    protected VisibilityRadios serviceVisibleRadios;
    
    @Inject @DataField("table-datasources")
    protected DataSourceNamesTable dsTable;
    
    @Inject @DataField("table-tables-procs")
    protected TablesProcNamesTable tablesAndProcsTable;

    @Inject @DataField("table-columns")
    protected ColumnNamesTable columnsTable;
    
    @Inject @DataField("btn-edit-service-createDdl")
    protected Button createDdlButton;
    
    @Inject @DataField("btn-edit-service-manage-sources")
    protected Button manageSourceButton;
    
    @Inject @DataField("textarea-edit-service-viewDdl")
    protected TextArea viewDdlTextArea;
    
    @Inject @DataField("btn-edit-service-save")
    protected Button saveServiceButton;
    
    @Inject @DataField("btn-edit-service-cancel")
    protected Button cancelButton;
    
    @Override
    @WorkbenchPartTitle
    public String getTitle() {
      return "";
    }
    
    @WorkbenchPartView
    public IsWidget getView() {
        return this;
    }
    
    @OnStartup
    public void onStartup( final PlaceRequest place ) {
    	String serviceName = place.getParameter(Constants.SERVICE_NAME_KEY, "[unknown]");
    	serviceNameTextBox.setText(serviceName);
    	serviceNameTextBox.setEnabled(false);
    	
    	tablesAndProcsTable.clear();
    	columnsTable.clear();
    	doGetQueryableSources(false);

    	// SelectionModel to handle Source selection 
    	final SingleSelectionModel<String> dsSelectionModel = new SingleSelectionModel<String>();
    	dsTable.setSelectionModel(dsSelectionModel); 
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
    	
    	doGetDataServiceDetails(serviceName);
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
            	dsTable.setData(dsList);
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
    @EventHandler("btn-edit-service-createDdl")
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
    	// Types hardcoded to string for now
    	List<String> typeNames = new ArrayList<String>(colNames.size());
    	for(String colName : colNames) {
    		typeNames.add("string");
    	}
    	
    	String viewString = DdlHelper.getODataViewDdl(Constants.SERVICE_VIEW_NAME, theTable, colNames, typeNames);
    	viewDdlTextArea.setText(viewString);    	
    }
    
    /**
     * Get the Data Service details to populate the page
     * @param serviceName the name of the service
     */
    protected void doGetDataServiceDetails(final String serviceName) {
    	String servicesVdb = Constants.SERVICES_VDB;
        vdbService.getVdbDetails(servicesVdb, new IRpcServiceInvocationHandler<VdbDetailsBean>() {
            @Override
            public void onReturn(VdbDetailsBean vdbDetailsBean) {
            	Collection<VdbModelBean> vdbModels = vdbDetailsBean.getModels();
            	for(VdbModelBean vdbModel : vdbModels) {
            		if(vdbModel.getName().equals(serviceName)) {
            			String description = vdbModel.getDescription();
            			serviceDescriptionTextBox.setText(description);
            			
            			boolean isVisible = vdbModel.isVisible();
            			serviceVisibleRadios.setValue(isVisible);
            			
            			String ddl = vdbModel.getDdl();
            			viewDdlTextArea.setText(ddl);
            		}
            	}
//            	currentVdbDetails = vdbDetailsBean;
//            	String title = "Virtual Database : "+vdbDetailsBean.getName();
//            	pageTitle.setText(title);
//            	breadcrumbLabel.setText(title);            	
//            	setVdbStatus(vdbDetailsBean);
//            	
//                updatePager(vdbDetailsBean);
//                updateVdbModelsTable(vdbDetailsBean);
//                doSetButtonEnablements();
            }
            @Override
            public void onError(Throwable error) {
//                notificationService.sendErrorNotification(i18n.format("vdbdetails.error-retrieving-details"), error); //$NON-NLS-1$
//                noDataMessage.setVisible(true);
//            	getModelsInProgressMessage.setVisible(false);
//                pageTitle.setText(Constants.STATUS_UNKNOWN);
//            	breadcrumbLabel.setText(Constants.STATUS_UNKNOWN);            	
//                vdbStatusLabel.setText(Constants.STATUS_UNKNOWN);
            }
        });       
    }
    
    /**
     * Event handler that fires when the user clicks the SaveChanges button.
     * @param event
     */
    @EventHandler("btn-edit-service-save")
    public void onSaveServiceButtonClick(ClickEvent event) {
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
    @EventHandler("btn-edit-service-manage-sources")
    public void onManageSourcesButtonClick(ClickEvent event) {
    	placeManager.goTo("ManageSourcesScreen");
    }
    
    /**
     * Event handler that fires when the user clicks the Cancel button.
     * @param event
     */
    @EventHandler("btn-edit-service-cancel")
    public void onCancelButtonClick(ClickEvent event) {
    	placeManager.goTo("DataServicesLibraryScreen");
    }
        
}
