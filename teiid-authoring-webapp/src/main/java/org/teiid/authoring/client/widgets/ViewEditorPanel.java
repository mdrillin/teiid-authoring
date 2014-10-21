package org.teiid.authoring.client.widgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.authoring.client.dialogs.ConfirmationContentPanel;
import org.teiid.authoring.client.dialogs.ConfirmationDialog;
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

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
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
    
    @Inject @DataField("lbl-vieweditor-sources-message")
    protected Label sourcesMessageLabel;
    
    @Inject @DataField("lbl-vieweditor-samples-message")
    protected Label samplesMessageLabel;
    
    @Inject @DataField("listbox-vieweditor-templates")
    protected ListBox ddlTemplatesListBox;

    @Inject @DataField("textarea-vieweditor-sampleDdl")
    protected TextArea sampleDdlTextArea;
    
    @Inject @DataField("btn-vieweditor-apply-sample")
    protected Button applySampleDdlButton;
    
    @Inject 
    private ConfirmationContentPanel confirmationContent;
	private ConfirmationDialog confirmationDialog;
	private String workingDdl;
	private SingleSelectionModel<CheckableNameRow> dsSelectionModel;
	private SingleSelectionModel<String> tableSelectionModel;
	
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
    	dsSelectionModel = new SingleSelectionModel<CheckableNameRow>();
    	dsNamesTable.setSelectionModel(dsSelectionModel); 
    	dsSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
    		public void onSelectionChange( SelectionChangeEvent event) { 
    			tablesAndProcsTable.clear();
    			columnsTable.clear();
    			CheckableNameRow selectedRow = dsSelectionModel.getSelectedObject();
    			if (selectedRow != null) {
    				doGetTablesAndProcs(selectedRow.getName());
    			}
    		} });

    	// SelectionModel to handle Table-procedure selection 
    	tableSelectionModel = new SingleSelectionModel<String>();
    	tablesAndProcsTable.setSelectionModel(tableSelectionModel); 
    	tableSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
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
    	
    	sourcesMessageLabel.setText(i18n.format("vieweditor-panel.sources-ddl-picksource-message"));
    	samplesMessageLabel.setText(i18n.format("vieweditor-panel.sample-ddl-message"));

    	populateDdlTemplatesListBox();
    	sampleDdlTextArea.setText(DdlHelper.getDdlTemplate(DdlHelper.DDL_TEMPLATE_SINGLE_SOURCE));
    	// Change Listener for Type ListBox
    	ddlTemplatesListBox.addChangeHandler(new ChangeHandler()
        {
        	// Changing the Type selection will re-populate property table with defaults for that type
        	public void onChange(ChangeEvent event)
        	{
        		String ddlSample = null;
        		String template = getSelectedDdlTemplate(); 
        		if(template.equals(DdlHelper.DDL_TEMPLATE_SINGLE_SOURCE)) {
        			ddlSample = DdlHelper.getDdlTemplate(DdlHelper.DDL_TEMPLATE_SINGLE_SOURCE);
        		} else if(template.equals(DdlHelper.DDL_TEMPLATE_TWO_SOURCE_JOIN)) {
        			ddlSample = DdlHelper.getDdlTemplate(DdlHelper.DDL_TEMPLATE_TWO_SOURCE_JOIN);
        		} else if(template.equals(DdlHelper.DDL_TEMPLATE_FLAT_FILE)) {
        			ddlSample = DdlHelper.getDdlTemplate(DdlHelper.DDL_TEMPLATE_FLAT_FILE);
        		} 
        		sampleDdlTextArea.setText(ddlSample);
        	}
        });
    	
    	updateStatus();
    }
    
    /**
     * Init the List of Service actions
     */
    private void populateDdlTemplatesListBox( ) {
    	// Make sure clear first
    	ddlTemplatesListBox.clear();

    	ddlTemplatesListBox.insertItem(DdlHelper.DDL_TEMPLATE_SINGLE_SOURCE, 0);
    	ddlTemplatesListBox.insertItem(DdlHelper.DDL_TEMPLATE_TWO_SOURCE_JOIN, 1);
    	ddlTemplatesListBox.insertItem(DdlHelper.DDL_TEMPLATE_FLAT_FILE, 2);
    	
    	// Initialize by setting the selection to the first item.
    	ddlTemplatesListBox.setSelectedIndex(0);
    }
    
    /**
     * Get the selected action from the MoreActions dropdown
     * @return
     */
    private String getSelectedDdlTemplate() {
    	int index = ddlTemplatesListBox.getSelectedIndex();
    	return ddlTemplatesListBox.getValue(index);
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
            	dsSelectionModel.clear();
             	dsNamesTable.setData(dsList);
            	sourcesMessageLabel.setText(i18n.format("vieweditor-panel.sources-ddl-picksource-message"));
            	updateStatus();
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
				tableSelectionModel.clear();
				tablesAndProcsTable.setData(nameList);
            	sourcesMessageLabel.setText(i18n.format("vieweditor-panel.sources-ddl-picktable-message"));
            	updateStatus();
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
    			List<CheckableNameTypeRow> colList = new ArrayList<CheckableNameTypeRow>();
    			List<QueryColumnBean> qColumns = data.getQueryColumns();
    			for(QueryColumnBean col : qColumns) {
    				CheckableNameTypeRow cRow = new CheckableNameTypeRow();
    				cRow.setName(col.getName());
    				cRow.setType(col.getType());
    				colList.add(cRow);
    			}
    			columnsTable.setData(colList);
            	sourcesMessageLabel.setText(i18n.format("vieweditor-panel.sources-ddl-pickcolumns-message"));
            	updateStatus();
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
    	List<String> colTypes = columnsTable.getSelectedColumnTypes();
    	
    	if(colNames.isEmpty()) {
    		Window.alert("Please select one or more columns");
    		return;
    	}
     	String viewDdl = DdlHelper.getODataViewDdl(Constants.SERVICE_VIEW_NAME, theTable, colNames, colTypes);
    	// Nothing in viewDDL area - safe to replace.
    	if(StringUtils.isEmpty(viewDdlTextArea.getText())) {
    		replaceViewMarkup(viewDdl);
        // has View DDL - prompt before replace.
    	} else {
    		workingDdl = viewDdl;
    		showConfirmOverwriteDialog();
    	}
    
    }
    
    /**
     * Event handler that fires when the user clicks the Add to markup button.
     * @param event
     */
    @EventHandler("btn-vieweditor-addToDdl")
    public void onAddToDdlButtonClick(ClickEvent event) {
    	String colString = columnsTable.getSelectedRowString();
    	if(colString.isEmpty()) {
    		Window.alert("Please select one or more columns");
    		return;
    	}

    	String currentDdl = viewDdlTextArea.getText();
    	
    	viewDdlTextArea.setText(currentDdl+"\n"+colString);  
    	
    	haveSuccessfullyTested = false;
    	queryResultsPanel.showStatusMessage(queryResultDefaultMsg);
    	updateStatus();
    }
    
    /**
     * Event handler that fires when the user clicks the Apply sample button.
     * @param event
     */
    @EventHandler("btn-vieweditor-apply-sample")
    public void onApplySampleDdlButtonClick(ClickEvent event) {
    	String ddlTemplate = sampleDdlTextArea.getText();
    	if(ddlTemplate.isEmpty()) {
    		Window.alert("Please select a template");
    		return;
    	}
    	// Nothing in viewDDL area - safe to replace.
    	if(StringUtils.isEmpty(viewDdlTextArea.getText())) {
    		replaceViewMarkup(ddlTemplate);
        // has View DDL - prompt before replace.
    	} else {
    		workingDdl = ddlTemplate;
    		showConfirmOverwriteDialog();
    	}
    }
    
    /**
     * Shows the confirmation dialog for overwrite of view markup
     */
    private void showConfirmOverwriteDialog() {
    	String dTitle = i18n.format("ds-properties-panel.confirm-overwrite-dialog-title");
    	String dMsg = i18n.format("ds-properties-panel.confirm-overwrite-dialog-message");
    	confirmationDialog = new ConfirmationDialog(confirmationContent, dTitle );
    	confirmationDialog.setContentTitle(dTitle);
    	confirmationDialog.setContentMessage(dMsg);
    	confirmationDialog.setOkCancelEventTypes(UiEventType.VIEW_MARKUP_REPLACE_OK, UiEventType.VIEW_MARKUP_REPLACE_CANCEL);
    	confirmationDialog.show();
    }
    
    /**
     * Handles UiEvents
     * @param dEvent
     */
    public void onDialogEvent(@Observes UiEvent dEvent) {
    	// User has OK'd source rename
    	if(dEvent.getType() == UiEventType.VIEW_MARKUP_REPLACE_OK) {
    		confirmationDialog.hide();
    		replaceViewMarkup(workingDdl);
    	// User has OK'd source redeploy
    	} else if(dEvent.getType() == UiEventType.VIEW_MARKUP_REPLACE_CANCEL) {
    		confirmationDialog.hide();
    	} 
    }
    
    /**
     * Replace the ViewMarkup TextArea with the supplied DDL
     * @param ddl
     */
    private void replaceViewMarkup(String ddl) {
    	viewDdlTextArea.setText(ddl);  
    	
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
    	currentStatus = Constants.OK;
    	
    	// Must have the service name
    	if(StringUtils.isEmpty(this.serviceName)) {
    		currentStatus = statusEnterName;
    	}
    	
		// Check view DDL - if serviceName ok
    	if(Constants.OK.equals(currentStatus)) {
    		String viewDdl = viewDdlTextArea.getText();
    		if(StringUtils.isEmpty(viewDdl)) {
    			currentStatus = statusEnterView;
    		}
    	}
    	
//		// Check at least one view source is defined
//    	if(Constants.OK.equals(currentStatus)) {
//    		if(getSrcVdbNames().isEmpty()) {
//    			currentStatus = statusDefineSources;
//    		}
//    	}
    	
    	List<CheckableNameTypeRow> colRows = columnsTable.getData();
    	if(colRows.isEmpty()) {
    		createDdlButton.setEnabled(false);
    		addToDdlButton.setEnabled(false);
    	} else {
    		createDdlButton.setEnabled(true);
    		addToDdlButton.setEnabled(true);
    	}
    	
    	if(StringUtils.isEmpty(sampleDdlTextArea.getText())) {
    		applySampleDdlButton.setEnabled(false);
    	} else {
    		applySampleDdlButton.setEnabled(true);
    	}
    	
		// Force the user to successfully test the service first
    	if(Constants.OK.equals(currentStatus)) {
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