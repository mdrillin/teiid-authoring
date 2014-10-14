package org.teiid.authoring.client.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.authoring.client.dialogs.UiEvent;
import org.teiid.authoring.client.dialogs.UiEventType;
import org.teiid.authoring.client.messages.ClientMessages;
import org.teiid.authoring.client.services.NotificationService;
import org.teiid.authoring.client.services.QueryRpcService;
import org.teiid.authoring.client.services.TeiidRpcService;
import org.teiid.authoring.client.services.rpc.IRpcServiceInvocationHandler;
import org.teiid.authoring.client.utils.DdlHelper;
import org.teiid.authoring.share.Constants;
import org.teiid.authoring.share.beans.DataSourcePageRow;
import org.teiid.authoring.share.beans.NotificationBean;
import org.teiid.authoring.share.beans.QueryColumnBean;
import org.teiid.authoring.share.beans.QueryColumnResultSetBean;
import org.teiid.authoring.share.beans.QueryTableProcBean;
import org.teiid.authoring.share.beans.VdbDetailsBean;
import org.teiid.authoring.share.beans.ViewModelRequestBean;
import org.teiid.authoring.share.services.StringUtils;
import org.uberfire.client.mvp.PlaceManager;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

@Templated("./ViewEditorPanel.html")
public class ViewEditorPanel extends Composite {

	private Map<String,String> shortToLongTableNameMap = new HashMap<String,String>();
	private String serviceName = null;
	private String selectedTable = null;
	private boolean haveSuccessfullyTested = false;
	private String statusEnterName = null;
	private String statusEnterView = null;
	private String statusTestView = null;
	private String statusDefineSources = null;
	private String queryResultDefaultMsg = null;
	private String currentStatus = null;
	
    @Inject
    private PlaceManager placeManager;
    @Inject
    private ClientMessages i18n;
    @Inject
    private NotificationService notificationService;
    
    @Inject
    protected TeiidRpcService teiidService;
    @Inject
    protected QueryRpcService queryService;
    
    @Inject @DataField("label-vieweditor-title")
    protected Label viewEditorPanelTitle;
    
    @Inject @DataField("label-vieweditor-description")
    protected Label viewEditorPanelDescription;
    
    @Inject
    protected DataSourceNamesTable dsNamesTable;
    
    @Inject
    protected TablesProcNamesTable tablesAndProcsTable;
    
    @Inject
    protected ColumnNamesTable columnsTable;
    
    @Inject @DataField("picker-tables")
    protected HorizontalPanel horizPanel;
    
    @Inject @DataField("btn-vieweditor-createDdl")
    protected Button createDdlButton;
    
    @Inject @DataField("btn-vieweditor-addToDdl")
    protected Button addToDdlButton;
        
    @Inject @DataField("btn-vieweditor-manage-sources")
    protected Button manageSourceButton;
    
    @Inject @DataField("textarea-vieweditor-viewDdl")
    protected TextArea viewDdlTextArea;
    
    @Inject @DataField("btn-vieweditor-test")
    protected Button testViewButton;
    
    @Inject @DataField("table-vieweditor-queryResults")
    protected QueryResultsPanel queryResultsPanel;
    
    @Inject Event<UiEvent> stateChangedEvent;
    
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
		queryResultDefaultMsg = i18n.format("vieweditor-panel.query-results-default-message");
		statusEnterName = i18n.format("vieweditor-panel.status-label-enter-name");
		statusEnterView = i18n.format("vieweditor-panel.status-label-enter-view");
		statusTestView = i18n.format("vieweditor-panel.status-label-test-view");
		statusDefineSources = i18n.format("vieweditor-panel.status-label-define-sources");
		currentStatus = statusEnterView;

    	tablesAndProcsTable.clear();
    	columnsTable.clear();

    	// Add the three picker tables to horizontal panel
    	horizPanel.setSpacing(0);
    	horizPanel.add(dsNamesTable);
    	horizPanel.add(tablesAndProcsTable);
    	horizPanel.add(columnsTable);
    	
    	doGetQueryableSources();
    	queryResultsPanel.showStatusMessage(queryResultDefaultMsg);

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
    	
    	
    	viewDdlTextArea.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
            	haveSuccessfullyTested = false;
            	// Show default querypanel message
            	queryResultsPanel.showStatusMessage(queryResultDefaultMsg);
            	// Update status
            	updateStatus();
            }
        });
    	
    	updateStatus();
    }
    
    public void setTitle(String title) {
    	viewEditorPanelTitle.setText(title);
    }
    
    public void setDescription(String desc) {
    	viewEditorPanelDescription.setText(desc);
    }
    
    public void setViewDdl(String ddlStr) {
    	this.viewDdlTextArea.setText(ddlStr); 
    	updateStatus();
    }
    
    public String getViewDdl( ) {
    	return this.viewDdlTextArea.getText();    	
    }
    
    public void setServiceName(String svcName) {
    	this.serviceName = svcName;  
    	updateStatus();
    }
    
    protected void doGetQueryableSources( ) {
    	teiidService.getDataSources("filter", Constants.SERVICE_SOURCE_VDB_PREFIX, new IRpcServiceInvocationHandler<List<DataSourcePageRow>>() {
    		@Override
    		public void onReturn(List<DataSourcePageRow> dsInfos) {
    			// Create list of DataSources that are accessible.  Only the Sources that have 'OK' state
    			// have an associated VDB source and are reachable...
            	List<CheckableNameRow> dsList = new ArrayList<CheckableNameRow>();
    			for(DataSourcePageRow row : dsInfos) {
    				if(row.getState()==DataSourcePageRow.State.OK) {
    					String dsName = row.getName();
            			dsList.add(createCheckableNameRow(dsName,false));
    				}
    			}
            	dsNamesTable.setData(dsList);
    		}
    		@Override
    		public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("vieweditor-panel.error-getting-svcsources"), error); //$NON-NLS-1$
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
    	String vdbSrcName = Constants.SERVICE_SOURCE_VDB_PREFIX+dataSourceName;
    	String vdbSrcJndi = Constants.JNDI_PREFIX+vdbSrcName;
		queryService.getTablesAndProcedures(vdbSrcJndi, vdbSrcName, new IRpcServiceInvocationHandler<List<QueryTableProcBean>>() {
			@Override
			public void onReturn(List<QueryTableProcBean> tablesAndProcs) {
				List<String> nameList = new ArrayList<String>();
				shortToLongTableNameMap.clear();
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
				notificationService.sendErrorNotification(i18n.format("vieweditor-panel.error-getting-tables-procs"), error); //$NON-NLS-1$
			}
		});

    }
    
    /**
     * Search for QueryColumns based on the current page and filter settings.
     * @param page
     */
    protected void doGetTableColumns(String source, String table, int page) {
    	String filterText = "";
    	String vdbSrcJndi = Constants.JNDI_PREFIX+Constants.SERVICE_SOURCE_VDB_PREFIX+source;
//    	String filterText = (String)stateService.get(ApplicationStateKeys.QUERY_COLUMNS_FILTER_TEXT,"");
//        stateService.put(ApplicationStateKeys.QUERY_COLUMNS_PAGE, currentQueryColumnsPage);

    	queryService.getQueryColumnResultSet(page, filterText, vdbSrcJndi, table,
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
    			notificationService.sendErrorNotification(i18n.format("vieweditor-panel.error-getting-tablecols"), error); //$NON-NLS-1$
    			// noColumnsMessage.setVisible(true);
    			// columnFetchInProgressMessage.setVisible(false);
    		}
    	});

    }
    
    /**
     * Event handler that fires when the user clicks the create markup button.
     * @param event
     */
    @EventHandler("btn-vieweditor-createDdl")
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
    	
    	haveSuccessfullyTested = false;
    	queryResultsPanel.showStatusMessage(queryResultDefaultMsg);
    	updateStatus();
    }
    
    /**
     * Event handler that fires when the user clicks the Add to markup button.
     * @param event
     */
    @EventHandler("btn-vieweditor-addToDdl")
    public void onAddToDdlButtonClick(ClickEvent event) {
    	String colString = columnsTable.getSelectedRowString();

    	String currentDdl = viewDdlTextArea.getText();
    	
    	viewDdlTextArea.setText(currentDdl+"\n"+colString);  
    	
    	haveSuccessfullyTested = false;
    	queryResultsPanel.showStatusMessage(queryResultDefaultMsg);
    	updateStatus();
    }
    
    /**
     * Event handler that fires when the user clicks the Test button.
     * @param event
     */
    @EventHandler("btn-vieweditor-test")
    public void onTestViewButtonClick(ClickEvent event) {
    	doTestView();
    }
    
    /**
     * Event handler that fires when the user clicks the Manage Sources button.
     * @param event
     */
    @EventHandler("btn-vieweditor-manage-sources")
    public void onManageSourcesButtonClick(ClickEvent event) {
    	placeManager.goTo("ManageSourcesScreen");
    }
    
    private void doTestView() {
    	final String serviceName = this.serviceName;
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("vieweditor-panel.testing-service-title"), //$NON-NLS-1$
                i18n.format("vieweditor-panel.testing-service-msg", serviceName)); //$NON-NLS-1$
            	
    	final String viewModel = serviceName;
    	String viewDdl = viewDdlTextArea.getText();
    	List<String> rqdImportVdbNames = getSrcVdbNames();
    	
    	ViewModelRequestBean viewModelRequest = new ViewModelRequestBean();
    	viewModelRequest.setName(serviceName);
    	viewModelRequest.setDescription("Test Service");
    	viewModelRequest.setDdl(viewDdl);
    	viewModelRequest.setVisible(true);
    	viewModelRequest.setRequiredImportVdbNames(rqdImportVdbNames);
    	    	
    	final String testVDBName = Constants.SERVICE_TEST_VDB_PREFIX+serviceName;
    	teiidService.deployNewVDB(testVDBName, 1, viewModelRequest, new IRpcServiceInvocationHandler<VdbDetailsBean>() {
            @Override
            public void onReturn(VdbDetailsBean vdbDetailsBean) {            	
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("vieweditor-panel.testing-service-complete"), //$NON-NLS-1$
                        i18n.format("vieweditor-panel.testing-service-complete-msg")); //$NON-NLS-1$

                String testVdbJndi = "java:/"+testVDBName;
    			String serviceSampleSQL = "SELECT * FROM "+serviceName+"."+Constants.SERVICE_VIEW_NAME+" LIMIT 10";
    	    	queryResultsPanel.showResultsTable(testVdbJndi, serviceSampleSQL);
    	    	
                haveSuccessfullyTested = true;
                updateStatus();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("vieweditor-panel.testing-service-error"), error); //$NON-NLS-1$
                haveSuccessfullyTested = false;
                updateStatus();
            }
        });           	
    }
    
    /**
     * Get the corresponding SrcVdbNames for the DSTable row selections
     * @return
     */
    public List<String> getSrcVdbNames( ) {
    	List<String> selectedDSNames = this.dsNamesTable.getSelectedSourceNames();
    	List<String> srcVdbNames = new ArrayList<String>(selectedDSNames.size());
    	for(String dsName : selectedDSNames) {
    		srcVdbNames.add(Constants.SERVICE_SOURCE_VDB_PREFIX+dsName);
    	}
    	return srcVdbNames;
    }
    
    /**
     * Fire state changed
     */
    public void fireStateChanged( ) {
    	stateChangedEvent.fire(new UiEvent(UiEventType.VIEW_EDITOR_CHANGED));
    }
    
	private void updateStatus( ) {
    	currentStatus = "OK";
    	
    	// Must have the service name
    	if(StringUtils.isEmpty(this.serviceName)) {
    		currentStatus = statusEnterName;
    	}
    	
		// Check view DDL - if serviceName ok
    	if("OK".equals(currentStatus)) {
    		String viewDdl = viewDdlTextArea.getText();
    		if(StringUtils.isEmpty(viewDdl)) {
    			currentStatus = statusEnterView;
    		}
    	}
    	
//		// Check at least one view source is defined
//    	if("OK".equals(currentStatus)) {
//    		if(getSrcVdbNames().isEmpty()) {
//    			currentStatus = statusDefineSources;
//    		}
//    	}
    	
		// Force the user to successfully test the service first
    	if("OK".equals(currentStatus)) {
    		// Force the user to successfully test the service
    		if(!haveSuccessfullyTested) {
    			currentStatus = statusTestView;
    		}
    		testViewButton.setEnabled(true);
    	} else {
    		testViewButton.setEnabled(false);
    	}
    	
    	fireStateChanged();
    }
	
	public String getStatus() {
		return this.currentStatus;
	}
        
}