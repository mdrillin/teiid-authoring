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
import org.teiid.authoring.client.widgets.DataSourceListWidget;
import org.teiid.authoring.client.widgets.DataSourcePropertiesPanel;
import org.teiid.authoring.share.Constants;
import org.teiid.authoring.share.beans.DataSourcePageRow;
import org.teiid.authoring.share.beans.DataSourcePropertyBean;
import org.teiid.authoring.share.beans.DataSourceWithVdbDetailsBean;
import org.teiid.authoring.share.beans.NotificationBean;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * ManageSourcesScreen - used for management of Data Sources
 *
 */
@Dependent
@Templated("./ManageSourcesScreen.html#page")
@WorkbenchScreen(identifier = "ManageSourcesScreen")
public class ManageSourcesScreen extends Composite {

	private String selectionStatusMsg_noSelection;
	
	private Map<String,String> defaultTranslatorMap = new HashMap<String,String>();
	private SingleSelectionModel<DataSourcePageRow> listSelectionModel;
	private List<DataSourcePageRow> currentDataSourceList = new ArrayList<DataSourcePageRow>();
	private boolean propPanelVisible = false;
	private ConfirmationDialog confirmationDialog;
	private HTMLPanel selectStatusHtmlPanel;

    @Inject
    protected ClientMessages i18n;
    
	@Inject
	private NotificationService notificationService;
	
    @Inject
    private PlaceManager placeManager;
    
    @Inject 
    private ConfirmationContentPanel confirmationContent;
    
    @Inject
    protected TeiidRpcService teiidService;
    @Inject
    protected QueryRpcService queryService;
    
    @Inject @DataField("anchor-goto-create-service")
    protected Anchor goToCreateServiceAnchor;
    
    @Inject @DataField("list-datasources")
    protected DataSourceListWidget dsList;
    
    @Inject @DataField("panel-selection-status")
    protected VerticalPanel selectionStatusPanel;
    
    @Inject @DataField("details-deckpanel")
    protected DeckPanel detailsDeckPanel;
    
    @Inject 
    protected DataSourcePropertiesPanel propsPanel;
    
    @Inject @DataField("btn-manage-sources-add")
    protected Button addSource;
    
    @Inject @DataField("btn-manage-sources-delete")
    protected Button deleteSource;

    @Override
    @WorkbenchPartTitle
    public String getTitle() {
      return Constants.BLANK;
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
    	selectionStatusMsg_noSelection = i18n.format("managesources.selected-source-no-selection");
    	String selectSourcePanelHtml = i18n.format("managesources.select-source-text");
        HTMLPanel selectSourcePanel = new HTMLPanel(selectSourcePanelHtml);
        
    	// Add properties panel and Select label to deckPanel
    	detailsDeckPanel.add(propsPanel);
    	detailsDeckPanel.add(selectSourcePanel);
    	detailsDeckPanel.showWidget(1);
    	propPanelVisible=false;
    	
    	doGetDataSourceInfos(null);

    	// Selection model for the dsList
    	listSelectionModel = new SingleSelectionModel<DataSourcePageRow>();
    	dsList.setSelectionModel(listSelectionModel);
    	listSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
    		public void onSelectionChange(SelectionChangeEvent event) {
    			DataSourcePageRow row = listSelectionModel.getSelectedObject();
    			if(row!=null) {
        			showPropertiesPanel(row.getName());
        			setSelectionStatusMessage(row.getMessage());
    			} else {
    				showBlankMessagePanel();
        			setSelectionStatusMessage(selectionStatusMsg_noSelection);
    			}
    		}
    	});
    	
    }
    
    private void showPropertiesPanel(String dsName) {
		propsPanel.setDataSource(dsName);
    	if(!propPanelVisible) {
			detailsDeckPanel.showWidget(0);
			propPanelVisible=true;
    	}
    }
    
    private void showBlankMessagePanel() {
    	if(propPanelVisible) {
			detailsDeckPanel.showWidget(1);
			propPanelVisible=false;
    	}
    }
    
    public void setSelectionStatusMessage(String message) {
    	if(selectStatusHtmlPanel!=null) {
    		selectionStatusPanel.remove(0);
    	}
    	if(message==null) message=i18n.format("managesources.selected-source-status-unavailable");
    	selectStatusHtmlPanel = new HTMLPanel("<p>"+message+"</p>");
    	selectionStatusPanel.add(selectStatusHtmlPanel);
    }
    
    /**
     * Handles UiEvents
     * @param dEvent
     */
    public void onUiEvent(@Observes UiEvent dEvent) {
    	// User has OK'd source deletion
    	if(dEvent.getType() == UiEventType.DELETE_SOURCE_OK) {
    		confirmationDialog.hide();
    		onDeleteConfirm();
    	// User has cancelled source deletion
    	} else if(dEvent.getType() == UiEventType.DELETE_SOURCE_CANCEL) {
    		confirmationDialog.hide();
    	} else if(dEvent.getType() == UiEventType.DATA_SOURCE_DEPLOY_STARTING) {
        	updateDataSourceInfos(dEvent.getDataSourceName(), UiEventType.DATA_SOURCE_DEPLOY_STARTING);
    	} else if(dEvent.getType() == UiEventType.DATA_SOURCE_DEPLOY_SUCCESS) {
    		doGetDataSourceInfos(dEvent.getDataSourceName());
    	} else if(dEvent.getType() == UiEventType.DATA_SOURCE_DEPLOY_FAIL) {
    		doGetDataSourceInfos(dEvent.getDataSourceName());
    	}
    }
    
    /**
     * Just update the current DS in the DS List with the given event
     * @param dsName the datasource that changed
     * @param eventType the event type
     */
    private void updateDataSourceInfos(String dsName, UiEventType eventType) {
    	for(DataSourcePageRow dsRow : this.currentDataSourceList) {
    		if(dsRow.getName().equals(dsName)) {
    			if(eventType==UiEventType.DATA_SOURCE_DEPLOY_STARTING) {
    				dsRow.setState(DataSourcePageRow.State.DEPLOYING);
    			} else if(eventType==UiEventType.DATA_SOURCE_DEPLOY_SUCCESS) {
    				dsRow.setState(DataSourcePageRow.State.OK);
    			} else if(eventType==UiEventType.DATA_SOURCE_DEPLOY_FAIL) {
    				dsRow.setState(DataSourcePageRow.State.ERROR);
    			}
    		}
    	}
    	dsList.setData(this.currentDataSourceList);
    }
    
    /**
     * Event handler that fires when the user clicks the create button.
     * @param event
     */
    @EventHandler("btn-manage-sources-add")
    public void onAddButtonClick(ClickEvent event) {
    	// Add a default Data Source
    	String newSourceName = getNewSourceName();
        DataSourceWithVdbDetailsBean sourceWithVdbBean = getDefaultSource(newSourceName);
        
        doCreateDataSource(sourceWithVdbBean);
    }
    
//    /**
//     * Shows a status popup with message
//     */
//    private void showStatusPopup(String title, String message) {
//    	statusPopup = new StatusPopup(statusContent, title );
//    	statusPopup.setContentTitle(title);
//    	statusPopup.setContentMessage(message);
//    	statusPopup.show();
//    }
    
    private String getNewSourceName() {
    	String sourceRootName = Constants.DATA_SOURCE_NEW_NAME;
    	String newSourceName = Constants.DATA_SOURCE_NEW_NAME;
    	Collection<String> existingNames = this.dsList.getDataSourceNames();
    	int i = 1;
    	while(existingNames.contains(newSourceName)) {
    		newSourceName = sourceRootName+i;
    		i++;
    	}
    	return newSourceName;
    }
    
    /**
     * Event handler that fires when the user clicks the delete button.
     * @param event
     */
    @EventHandler("btn-manage-sources-delete")
    public void onDeleteButtonClick(ClickEvent event) {
    	// Show confirmation dialog before the deletion
    	showConfirmDeleteDialog();
    }
    
    /**
     * Shows the confirmation dialog for deleting a DataSource
     */
    private void showConfirmDeleteDialog() {
    	String dTitle = i18n.format("managesources.confirm-delete-dialog-title");
    	String dMsg = i18n.format("managesources.confirm-delete-dialog-message");
    	confirmationDialog = new ConfirmationDialog(confirmationContent, dTitle );
    	confirmationDialog.setContentTitle(dTitle);
    	confirmationDialog.setContentMessage(dMsg);
    	confirmationDialog.setOkCancelEventTypes(UiEventType.DELETE_SOURCE_OK, UiEventType.DELETE_SOURCE_CANCEL);
    	confirmationDialog.show();
    }
    
    /**
     * Does the DataSource deletion upon user confirmation
     */
    private void onDeleteConfirm() {
		DataSourcePageRow row = listSelectionModel.getSelectedObject();
    	List<String> dsNames = new ArrayList<String>();
    	String dsName = row.getName();
		dsNames.add(Constants.SERVICE_SOURCE_VDB_PREFIX+dsName);
    	dsNames.add(dsName);
		doDeleteDataSources(dsNames,null);
    }
    
    /**
     * Creates a DataSource
     * @param dsDetailsBean the data source details
     */
    private void doCreateDataSource(final DataSourceWithVdbDetailsBean detailsBean) {
    	final String dsName = detailsBean.getName();
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("managesources.creating-datasource-title"), //$NON-NLS-1$
                i18n.format("managesources.creating-datasource-msg", dsName)); //$NON-NLS-1$
        
        teiidService.createDataSourceWithVdb(detailsBean, new IRpcServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("managesources.datasource-created"), //$NON-NLS-1$
                        i18n.format("managesources.create-success-msg")); //$NON-NLS-1$

                // Refresh Page
            	doGetDataSourceInfos(detailsBean.getName());
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("managesources.create-error"), //$NON-NLS-1$
                        error);
            }
        });
    }
    
    /**
     * Called when the user confirms the dataSource deletion.
     */
    private void doDeleteDataSources(final List<String> dsNames, final String selectedDS) {
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("managesources.deleting-datasource-title"), //$NON-NLS-1$
                i18n.format("managesources.deleting-datasource-msg", "sourceList")); //$NON-NLS-1$
        teiidService.deleteDataSources(dsNames, new IRpcServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("managesources.datasource-deleted"), //$NON-NLS-1$
                        i18n.format("managesources.delete-success-msg")); //$NON-NLS-1$

            	doGetDataSourceInfos(selectedDS);
            }
            @Override
            public void onError(Throwable error) {
              notificationService.completeProgressNotification(notificationBean.getUuid(),
              i18n.format("managesources.delete-error"), //$NON-NLS-1$
              error);
            }
        });
    }
    
    /**
     * Populate the DataSource List.
     * @param selectedDS the selected DataSource, if selection is desired.
     */
    protected void doGetDataSourceInfos(final String selectedDS) {
    	teiidService.getDataSources("filter", Constants.SERVICE_SOURCE_VDB_PREFIX, new IRpcServiceInvocationHandler<List<DataSourcePageRow>>() {
    		@Override
    		public void onReturn(List<DataSourcePageRow> dsInfos) {
    			// Filter out the sources starting with SERVICE_SOURCE_VDB_PREFIX and SERVICES_VDB 
    			List<DataSourcePageRow> tableRowList = new ArrayList<DataSourcePageRow>();
    			for(DataSourcePageRow row : dsInfos) {
    				String name = row.getName();
    				if(!name.startsWith(Constants.SERVICE_SOURCE_VDB_PREFIX) && !name.startsWith(Constants.SERVICE_VDB_PREFIX)) {
    					tableRowList.add(row);
    				}
    			}
    			currentDataSourceList.clear();
    			currentDataSourceList.addAll(tableRowList);
    			
    			dsList.setData(tableRowList);
    			if(selectedDS!=null) {
    				dsList.setSelection(selectedDS);
    			} else {
    				showBlankMessagePanel();
    			}
    			
    	    	doPopulateDefaultTranslatorMappings();
    		}
    		@Override
    		public void onError(Throwable error) {
    			notificationService.sendErrorNotification(i18n.format("managesources.error-getting-datasources"), error); //$NON-NLS-1$
    		}
    	});
    }
    
    /**
     * Cache the Default Translator Mappings for later use.
     */
    protected void doPopulateDefaultTranslatorMappings() {
    	teiidService.getDefaultTranslatorMap(new IRpcServiceInvocationHandler<Map<String,String>>() {
    		@Override
    		public void onReturn(Map<String,String> defaultTranslatorsMap) {
    			defaultTranslatorMap = defaultTranslatorsMap;
    			propsPanel.setDefaultTranslatorMappings(defaultTranslatorMap);
    		}
    		@Override
    		public void onError(Throwable error) {
    			defaultTranslatorMap = new HashMap<String,String>();
    			notificationService.sendErrorNotification(i18n.format("managesources.error-populating-translatormappings"), error); //$NON-NLS-1$
    		}
    	});
    }
    
	/**
	 * Create a Default data source connected to the DashboardDS (for now)
	 * @return
	 */
    private DataSourceWithVdbDetailsBean getDefaultSource(String dsName) {
    	DataSourceWithVdbDetailsBean resultBean = new DataSourceWithVdbDetailsBean();
    	resultBean.setName(dsName);
    	resultBean.setSourceVdbName(Constants.SERVICE_SOURCE_VDB_PREFIX+dsName);
    	resultBean.setTranslator("jdbc-ansi");
    	resultBean.setType("h2");
    	
    	List<DataSourcePropertyBean> props = new ArrayList<DataSourcePropertyBean>();
    	DataSourcePropertyBean urlProp = new DataSourcePropertyBean();
    	urlProp.setName("connection-url");
    	urlProp.setValue("jdbc:h2:file:${jboss.server.data.dir}/teiid-dashboard/teiid-dashboard-ds;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1");

    	DataSourcePropertyBean userProp = new DataSourcePropertyBean();
    	userProp.setName("user-name");
    	userProp.setValue("sa");

    	DataSourcePropertyBean pwProp = new DataSourcePropertyBean();
    	pwProp.setName("password");
    	pwProp.setValue("sa");
    	
    	props.add(urlProp);
    	props.add(userProp);
    	props.add(pwProp);
    	
    	resultBean.setProperties(props);

    	return resultBean;
    }
    
    /**
     * Event handler that fires when the user clicks the GoTo Library anchor.
     * @param event
     */
    @EventHandler("anchor-goto-create-service")
    public void onGotoCreateServiceAnchorClick(ClickEvent event) {
    	placeManager.goTo("CreateDataServiceScreen");
    }
    
}
