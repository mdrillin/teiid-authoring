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
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.authoring.client.messages.ClientMessages;
import org.teiid.authoring.client.services.DataSourceRpcService;
import org.teiid.authoring.client.services.NotificationService;
import org.teiid.authoring.client.services.QueryRpcService;
import org.teiid.authoring.client.services.VdbRpcService;
import org.teiid.authoring.client.services.rpc.IRpcServiceInvocationHandler;
import org.teiid.authoring.client.utils.DdlHelper;
import org.teiid.authoring.client.widgets.CheckableNameRow;
import org.teiid.authoring.client.widgets.ColumnNamesTable;
import org.teiid.authoring.client.widgets.DataSourceNamesTable;
import org.teiid.authoring.client.widgets.QueryResultPagedTableDisplayer;
import org.teiid.authoring.client.widgets.TablesProcNamesTable;
import org.teiid.authoring.client.widgets.VisibilityRadios;
import org.teiid.authoring.share.Constants;
import org.teiid.authoring.share.beans.NotificationBean;
import org.teiid.authoring.share.beans.QueryColumnBean;
import org.teiid.authoring.share.beans.QueryColumnResultSetBean;
import org.teiid.authoring.share.beans.QueryTableProcBean;
import org.teiid.authoring.share.beans.VdbDetailsBean;
import org.teiid.authoring.share.beans.VdbModelBean;
import org.teiid.authoring.share.beans.ViewModelRequestBean;
import org.teiid.authoring.share.services.StringUtils;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
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
 * EditDataServiceScreen - used to edit existing Data Services
 *
 */
@Dependent
@Templated("./EditDataServiceScreen.html#page")
@WorkbenchScreen(identifier = "EditDataServiceScreen")
public class EditDataServiceScreen extends Composite {

	private Map<String,String> sourceNameToJndiMap = new HashMap<String,String>();
	private Map<String,String> shortToLongTableNameMap = new HashMap<String,String>();
	private String selectedTable = null;
	private String statusEnterName = null;
	private String statusEnterView = null;
	private String statusTestView = null;
	private String statusClickSave = null;
	private boolean haveSuccessfullyTested = false;
	
    @Inject
    private PlaceManager placeManager;
    @Inject
    private ClientMessages i18n;
    @Inject
    private NotificationService notificationService;
    
    @Inject
    protected DataSourceRpcService dataSourceService;
    @Inject
    protected QueryRpcService queryService;
    @Inject
    protected VdbRpcService vdbService;

    @Inject @DataField("textbox-edit-service-name")
    protected TextBox serviceNameTextBox;
    
    @Inject @DataField("textarea-edit-service-description")
    protected TextArea serviceDescriptionTextBox;
    
    @Inject @DataField("radios-edit-service-visibility")
    protected VisibilityRadios serviceVisibleRadios;
    
    @Inject @DataField("label-edit-service-status")
    protected Label statusLabel;
    
    @Inject @DataField("table-datasources")
    protected DataSourceNamesTable dsNamesTable;
    
    @Inject @DataField("table-tables-procs")
    protected TablesProcNamesTable tablesAndProcsTable;

    @Inject @DataField("table-columns")
    protected ColumnNamesTable columnsTable;
    
    @Inject @DataField("btn-edit-service-createDdl")
    protected Button createDdlButton;
    
    @Inject @DataField("btn-edit-service-addToDdl")
    protected Button addToDdlButton;
        
    @Inject @DataField("btn-edit-service-manage-sources")
    protected Button manageSourceButton;
    
    @Inject @DataField("textarea-edit-service-viewDdl")
    protected TextArea viewDdlTextArea;
    
    @Inject @DataField("btn-edit-service-test")
    protected Button testViewButton;
    
    @Inject @DataField("btn-edit-service-save")
    protected Button saveServiceButton;
    
    @Inject @DataField("btn-edit-service-cancel")
    protected Button cancelButton;
    
    @Inject @DataField("table-edit-service-queryResults")
    protected QueryResultPagedTableDisplayer queryResultsTablePaged;
    
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
		statusEnterName = i18n.format("editdataservice.status-label-enter-name");
		statusEnterView = i18n.format("editdataservice.status-label-enter-view");
		statusTestView = i18n.format("editdataservice.status-label-test-view");
		statusClickSave = i18n.format("editdataservice.status-label-click-save");
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
    	final SingleSelectionModel<CheckableNameRow> dsSelectionModel = new SingleSelectionModel<CheckableNameRow>();
    	dsNamesTable.setSelectionModel(dsSelectionModel); 
    	dsSelectionModel. addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
    		public void onSelectionChange( SelectionChangeEvent event) { 
    			tablesAndProcsTable.clear();
    			columnsTable.clear();
    			CheckableNameRow selectedRow = dsSelectionModel.getSelectedObject();
    			if (selectedRow != null) {
    				doGetTablesAndProcs(selectedRow.getName());
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
    				CheckableNameRow theSource = dsSelectionModel.getSelectedObject();
    				String longTableName = shortToLongTableNameMap.get(selected);
    				doGetTableColumns(theSource.getName(), longTableName, 1);
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
            	haveSuccessfullyTested = false;
            	updateStatus();
            }
        });
    	
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
            	List<CheckableNameRow> dsList = new ArrayList<CheckableNameRow>();
            	for(String dsName : sourceNameToJndiMap.keySet()) {
            		if(dsName.startsWith(Constants.SERVICE_SOURCE_VDB_PREFIX)) {
            			dsList.add(createCheckableNameRow(dsName, false));
            		}
            	}
            	dsNamesTable.setData(dsList);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("editdataservice.error-getting-svcsources"), error); //$NON-NLS-1$
            }
        });
    }
 
    private CheckableNameRow createCheckableNameRow(String name, boolean isSelected) {
		CheckableNameRow cRow = new CheckableNameRow();
		cRow.setName(name);
		cRow.setChecked(isSelected);
		return cRow;
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
					String name = tp.getName();
					if(name!=null) {
						if(name.contains(".PUBLIC.")) {
							String shortName = name.substring(name.indexOf(".PUBLIC.")+".PUBLIC.".length());
							shortToLongTableNameMap.put(shortName, name);
							nameList.add(shortName);
						} else if(!name.contains(".INFORMATION_SCHEMA.")) {
							shortToLongTableNameMap.put(name, name);
							nameList.add(name);
						}
					}
				}
				tablesAndProcsTable.setData(nameList);
			}
			@Override
			public void onError(Throwable error) {
				notificationService.sendErrorNotification(i18n.format("editdataservice.error-getting-tables-procs"), error); //$NON-NLS-1$
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

    	queryService.getQueryColumnResultSet(page, filterText, sourceNameToJndiMap.get(source), table,
    			new IRpcServiceInvocationHandler<QueryColumnResultSetBean>() {
    		@Override
    		public void onReturn(QueryColumnResultSetBean data) {
    			List<CheckableNameRow> colList = new ArrayList<CheckableNameRow>();
    			List<QueryColumnBean> qColumns = data.getQueryColumns();
    			for(QueryColumnBean col : qColumns) {
    				CheckableNameRow cRow = new CheckableNameRow();
    				cRow.setName(col.getName());
    				colList.add(cRow);
    			}
    			columnsTable.setData(colList);
    		}
    		@Override
    		public void onError(Throwable error) {
    			notificationService.sendErrorNotification(i18n.format("editdataservice.error-getting-tablecols"), error); //$NON-NLS-1$
    			// noColumnsMessage.setVisible(true);
    			// columnFetchInProgressMessage.setVisible(false);
    		}
    	});

    }
    
    /**
     * Event handler that fires when the user clicks the create markup button.
     * @param event
     */
    @EventHandler("btn-edit-service-createDdl")
    public void onCreateDdlButtonClick(ClickEvent event) {
    	String theTable = (selectedTable==null) ? "NULL" : selectedTable;
    	
    	List<String> colNames = columnsTable.getSelectedColumnNames();
    	// Types hardcoded to string for now
    	List<String> typeNames = new ArrayList<String>(colNames.size());
    	for(String colName : colNames) {
    		typeNames.add("string");
    	}
    	
    	String viewString = DdlHelper.getODataViewDdl(Constants.SERVICE_VIEW_NAME, theTable, colNames, typeNames);
    	viewDdlTextArea.setText(viewString);  
    	
    	updateStatus();
    }
    
    /**
     * Event handler that fires when the user clicks the Add to markup button.
     * @param event
     */
    @EventHandler("btn-edit-service-addToDdl")
    public void onAddToDdlButtonClick(ClickEvent event) {
    	String colString = columnsTable.getSelectedRowString();

    	String currentDdl = viewDdlTextArea.getText();
    	
    	viewDdlTextArea.setText(currentDdl+"\n"+colString);  
    	
    	updateStatus();
    }
    
	private void updateStatus( ) {
    	boolean isOK = true;
    	
    	// Warning for missing service name
    	String serviceName = serviceNameTextBox.getText();
    	if(StringUtils.isEmpty(serviceName)) {
    		statusLabel.setText(statusEnterName);
    		isOK = false;
    	}
    	
		// Check for missing view DDL - if serviceName passed
    	if(isOK) {
    		String viewDdl = viewDdlTextArea.getText();
    		if(StringUtils.isEmpty(viewDdl)) {
    			statusLabel.setText(statusEnterView);
    			isOK = false;
    		}
    	}
    	
		// Force the user to successfully test the service first
    	if(isOK) {
    		// Force the user to successfully test the service
    		if(!haveSuccessfullyTested) {
    			statusLabel.setText(statusTestView);
    			isOK = false;
    		}
    		testViewButton.setEnabled(true);
    	} else {
    		testViewButton.setEnabled(false);
    	}
    	
    	if(isOK) {
    		statusLabel.setText(statusClickSave);
    		saveServiceButton.setEnabled(true);
    	} else {
    		saveServiceButton.setEnabled(false);
    	}
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
            	
            	// Set the initial status
            	updateStatus();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("editdataservice.error-getting-svc-details"), error); //$NON-NLS-1$
//                noDataMessage.setVisible(true);
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
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("editdataservice.saving-service-title"), //$NON-NLS-1$
                i18n.format("editdataservice.saving-service-msg", serviceName)); //$NON-NLS-1$
            	
    	String serviceDescription = this.serviceDescriptionTextBox.getText();
    	final String viewModel = serviceName;
    	String viewDdl = viewDdlTextArea.getText();
    	boolean isVisible = serviceVisibleRadios.isVisibleSelected();
    	List<String> rqdImportVdbNames = dsNamesTable.getSelectedSourceNames();
    	
    	ViewModelRequestBean viewModelRequest = new ViewModelRequestBean();
    	viewModelRequest.setName(serviceName);
    	viewModelRequest.setDescription(serviceDescription);
    	viewModelRequest.setDdl(viewDdl);
    	viewModelRequest.setVisible(isVisible);
    	viewModelRequest.setRequiredImportVdbNames(rqdImportVdbNames);
    	    	
        vdbService.addOrReplaceViewModelAndRedeploy("ServicesVDB", 1, viewModelRequest, new IRpcServiceInvocationHandler<VdbDetailsBean>() {
            @Override
            public void onReturn(VdbDetailsBean vdbDetailsBean) {            	
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("editdataservice.saving-service-complete"), //$NON-NLS-1$
                        i18n.format("editdataservice.saving-service-complete-msg")); //$NON-NLS-1$
                
            	Map<String,String> parameters = new HashMap<String,String>();
            	parameters.put(Constants.SERVICE_NAME_KEY, viewModel);
            	placeManager.goTo(new DefaultPlaceRequest("DataServiceDetailsScreen",parameters));
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("editdataservice.saving-service-error"), error); //$NON-NLS-1$
                //addModelInProgressMessage.setVisible(false);
            }
        });           	
    }
    
    /**
     * Event handler that fires when the user clicks the Test button.
     * @param event
     */
    @EventHandler("btn-edit-service-test")
    public void onTestViewButtonClick(ClickEvent event) {
    	doTestView();
    }
    
    private void doTestView() {
    	final String serviceName = this.serviceNameTextBox.getText();
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("editdataservice.testing-service-title"), //$NON-NLS-1$
                i18n.format("editdataservice.testing-service-msg", serviceName)); //$NON-NLS-1$
            	
    	String serviceDescription = this.serviceDescriptionTextBox.getText();
    	final String viewModel = serviceName;
    	String viewDdl = viewDdlTextArea.getText();
    	boolean isVisible = serviceVisibleRadios.isVisibleSelected();
    	List<String> rqdImportVdbNames = dsNamesTable.getSelectedSourceNames();
    	
    	ViewModelRequestBean viewModelRequest = new ViewModelRequestBean();
    	viewModelRequest.setName(serviceName);
    	viewModelRequest.setDescription(serviceDescription);
    	viewModelRequest.setDdl(viewDdl);
    	viewModelRequest.setVisible(isVisible);
    	viewModelRequest.setRequiredImportVdbNames(rqdImportVdbNames);
    	    	
    	final String testVDBName = "TEST-"+serviceName;
        vdbService.deployNewVDB(testVDBName, 1, viewModelRequest, new IRpcServiceInvocationHandler<VdbDetailsBean>() {
            @Override
            public void onReturn(VdbDetailsBean vdbDetailsBean) {            	
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("editdataservice.testing-service-complete"), //$NON-NLS-1$
                        i18n.format("editdataservice.testing-service-complete-msg")); //$NON-NLS-1$

                String testVdbJndi = "java:/"+testVDBName;
    			String serviceSampleSQL = "SELECT * FROM "+serviceName+"."+Constants.SERVICE_VIEW_NAME+" LIMIT 10";
    	    	queryResultsTablePaged.setDataProvider(testVdbJndi, serviceSampleSQL);
    	    	
                haveSuccessfullyTested = true;
                saveServiceButton.setEnabled(true);                
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("editdataservice.testing-service-error"), error); //$NON-NLS-1$
                haveSuccessfullyTested = false;
                saveServiceButton.setEnabled(false);                
//                addModelInProgressMessage.setVisible(false);
            }
        });           	
    }
    
    private void doDeleteTestVDB() {
    	final String serviceName = this.serviceNameTextBox.getText();
    	if(serviceName!=null) {
        	final String testVDBName = "TEST-"+serviceName;
        	List<String> dsNames = new ArrayList<String>(1);
        	dsNames.add(testVDBName);
            final NotificationBean notificationBean = notificationService.startProgressNotification(
                    i18n.format("editdataservice.deleting-service-title"), //$NON-NLS-1$
                    i18n.format("editdataservice.deleting-service-msg", "sourceList")); //$NON-NLS-1$
            dataSourceService.deleteDataSources(dsNames, new IRpcServiceInvocationHandler<Void>() {
                @Override
                public void onReturn(Void data) {
                    notificationService.completeProgressNotification(notificationBean.getUuid(),
                            i18n.format("editdataservice.service-deleted"), //$NON-NLS-1$
                            i18n.format("editdataservice.service-deleted-success-msg")); //$NON-NLS-1$
                }
                @Override
                public void onError(Throwable error) {
                  notificationService.completeProgressNotification(notificationBean.getUuid(),
                  i18n.format("editdataservice.service-delete-error"), //$NON-NLS-1$
                  error);
                }
            });
    	}   	
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
    	doDeleteTestVDB();
    	placeManager.goTo("DataServicesLibraryScreen");
    }
        
}
