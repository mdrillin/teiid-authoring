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
import java.util.Collections;
import java.util.List;

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
import org.teiid.authoring.client.widgets.DataSourcePropertiesTable;
import org.teiid.authoring.client.widgets.DataSourceTranslatorConnectionTable;
import org.teiid.authoring.share.beans.Constants;
import org.teiid.authoring.share.beans.DataSourceDetailsBean;
import org.teiid.authoring.share.beans.DataSourcePageRow;
import org.teiid.authoring.share.beans.DataSourcePropertyBean;
import org.teiid.authoring.share.beans.DataSourceTranslatorConnectionPageRow;
import org.teiid.authoring.share.beans.PropertyBeanComparator;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * ManageSourcesScreen - used for management of Data Sources
 *
 */
@Dependent
@Templated("./ManageSourcesScreen.html#page")
@WorkbenchScreen(identifier = "ManageSourcesScreen")
public class ManageSourcesScreen extends Composite {

    private List<DataSourcePropertyBean> currentPropList = new ArrayList<DataSourcePropertyBean>();
	
    @Inject
    private PlaceManager placeManager;
    
    @Inject
    protected DataSourceRpcService dataSourceService;
    @Inject
    protected QueryRpcService queryService;
    @Inject
    protected VdbRpcService vdbService;
    
    @Inject @DataField("anchor-goto-create-service")
    protected Anchor goToCreateServiceAnchor;
    
    @Inject @DataField("table-datasources")
    protected DataSourceTranslatorConnectionTable dsInfoTable;

    @Inject @DataField("textbox-manage-sources-name")
    protected TextBox name;
    @Inject @DataField("listbox-manage-sources-types")
    protected ListBox sourceTypeListBox;
    @Inject @DataField("listbox-manage-sources-translator")
    protected ListBox translatorListBox;
    @Inject @DataField("table-manage-sources-core-properties")
    protected DataSourcePropertiesTable dataSourceCorePropertiesTable;
    @Inject @DataField("table-manage-sources-adv-properties")
    protected DataSourcePropertiesTable dataSourceAdvancedPropertiesTable;
    
    @Inject @DataField("btn-manage-sources-create")
    protected Button createSource;
        
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

    	doGetDataSourceInfos( );
    	
    	doPopulateSourceTypeListBox();
    	
    	doPopulateTranslatorListBox();
    	
    	dataSourceCorePropertiesTable.clear();
    	dataSourceAdvancedPropertiesTable.clear();
    	
        name.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
            	//updateDialogStatus();
            }
        });
        // Change Listener for Type ListBox
        sourceTypeListBox.addChangeHandler(new ChangeHandler()
        {
        	// Changing the Type selection will re-populate property table with defaults for that type
        	public void onChange(ChangeEvent event)
        	{
        		String selectedType = getSelectedSourceType();                                
        		doPopulatePropertiesTable(selectedType);
        	}
        });
        
//    	// SelectionModel to handle Source selection 
//    	final SingleSelectionModel<String> dsSelectionModel = new SingleSelectionModel<String>();
//    	dsTable.setSelectionModel(dsSelectionModel); 
//    	dsSelectionModel. addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
//    		public void onSelectionChange( SelectionChangeEvent event) { 
//    			tablesAndProcsTable.clear();
//    			columnsTable.clear();
//    			String selected = dsSelectionModel.getSelectedObject();
//    			selectedDataSource = selected;
//    			if (selected != null) {
//    				doGetTablesAndProcs(selected);
//    			}
//    		} });
//
//    	// SelectionModel to handle Table-procedure selection 
//    	final SingleSelectionModel<String> tableSelectionModel = new SingleSelectionModel<String>();
//    	tablesAndProcsTable.setSelectionModel(tableSelectionModel); 
//    	tableSelectionModel. addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
//    		public void onSelectionChange( SelectionChangeEvent event) { 
//    			String selected = tableSelectionModel.getSelectedObject();
//    			selectedTable = selected;
//    			if (selected != null) {
//    				String theSource = dsSelectionModel.getSelectedObject();
//    				doGetTableColumns(theSource, selected, 1);
//    			}
//    		} });
    	
    }
    
    /**
     * Event handler that fires when the user clicks the create button.
     * @param event
     */
    @EventHandler("btn-manage-sources-create")
    public void onCreateButtonClick(ClickEvent event) {
    	// Gets details for the new source from the ui
        DataSourceDetailsBean sourceBean = getDetailsBean();
        doCreateDataSource(sourceBean);
    }
    
    /**
     * Creates a DataSource
     * @param dsDetailsBean the data source details
     */
    private void doCreateDataSource(DataSourceDetailsBean detailsBean) {
//    	final String dsName = detailsBean.getName();
//        final NotificationBean notificationBean = notificationService.startProgressNotification(
//                i18n.format("datasources.creating-datasource-title"), //$NON-NLS-1$
//                i18n.format("datasources.creating-datasource-msg", dsName)); //$NON-NLS-1$
        dataSourceService.createDataSource(detailsBean, new IRpcServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
//                notificationService.completeProgressNotification(notificationBean.getUuid(),
//                        i18n.format("datasources.datasource-created"), //$NON-NLS-1$
//                        i18n.format("datasources.create-success-msg")); //$NON-NLS-1$
//
//                // Refresh Page
//            	doGetDataSourceDetails();
            }
            @Override
            public void onError(Throwable error) {
//                notificationService.completeProgressNotification(notificationBean.getUuid(),
//                        i18n.format("datasources.create-error"), //$NON-NLS-1$
//                        error);
            }
        });
    }
    
    private String getSelectedSourceType() {
    	int index = sourceTypeListBox.getSelectedIndex();
    	return sourceTypeListBox.getValue(index);
    }
    
    /**
     * Populate the DataSource ListBox
     */
    protected void doGetDataSourceInfos() {
    	dataSourceService.getDataSources("filter", new IRpcServiceInvocationHandler<List<DataSourcePageRow>>() {
    		@Override
    		public void onReturn(List<DataSourcePageRow> dsInfos) {
    			// Create the list of sources to show.  (Not showing teiid local sources)
    			List<DataSourceTranslatorConnectionPageRow> tableRowList = new ArrayList<DataSourceTranslatorConnectionPageRow>();
    			for(DataSourcePageRow row : dsInfos) {
    				String serverDsType = row.getType();
    				if(!serverDsType.equals("teiid-local")) {
        				DataSourceTranslatorConnectionPageRow tableRow = new DataSourceTranslatorConnectionPageRow();
        				// The raw server source
        				String serverDsName = row.getName();
        				tableRow.setConnection(serverDsName);
        				tableRow.setTranslator("");
        				tableRow.setDataSource("");
        				tableRowList.add(tableRow);
    				}
    			}
    			
    			List<String> srcVdbNames = new ArrayList<String>();
    			// Now look for matching Teiid VDB source, based on name match
    			for(DataSourceTranslatorConnectionPageRow tableRow : tableRowList) {
    				String connectionName = tableRow.getConnection();
    				String srcVdbName = Constants.SERVICE_SOURCE_VDB_PREFIX+connectionName;
    				for(DataSourcePageRow row : dsInfos) {
    					if(row.getName().equals(srcVdbName)) {
    						tableRow.setDataSource(srcVdbName);
    						srcVdbNames.add(srcVdbName);
    					}
    				}
    			}
    			
    			// Get translators for list of srcVdbNames
    			doSetTranslatorNames(tableRowList, srcVdbNames);
    		}
    		@Override
    		public void onError(Throwable error) {
    			//             notificationService.sendErrorNotification(i18n.format("addSourceModelDialog.error-populating-datasources"), error); //$NON-NLS-1$
    		}
    	});
    }
        
    private void doSetTranslatorNames(final List<DataSourceTranslatorConnectionPageRow> tableRows, final List<String> srcVdbNames) {
    	vdbService.getTranslatorsForSrcVdbs(srcVdbNames, new IRpcServiceInvocationHandler<List<String>>() {
    		@Override
    		public void onReturn(List<String> translators) {
    			
    			for(int i=0; i<srcVdbNames.size(); i++) {
    				String srcVdbName = srcVdbNames.get(i);
    				String tName = translators.get(i);
        			for(DataSourceTranslatorConnectionPageRow row : tableRows) {
        				String dsName = row.getDataSource();
        				if(dsName.equals(srcVdbName)) {
        					row.setTranslator(tName);
        				}
        			}
    			}
    			dsInfoTable.setData(tableRows);
    		}
    		@Override
    		public void onError(Throwable error) {
    			//             notificationService.sendErrorNotification(i18n.format("addSourceModelDialog.error-populating-datasources"), error); //$NON-NLS-1$
    		}
    	});
    }
    
    /**
     * Populate the Data Source Type ListBox
     */
    protected void doPopulateSourceTypeListBox() {
        dataSourceService.getDataSourceTypes(new IRpcServiceInvocationHandler<List<String>>() {
            @Override
            public void onReturn(List<String> dsTypes) {
                populateSourceTypeListBox(dsTypes);
            }
            @Override
            public void onError(Throwable error) {
                //notificationService.sendErrorNotification(i18n.format("adddatasourcedialog.error-populating-types-listbox"), error); //$NON-NLS-1$
            }
        });
    }
    
    /**
     * Populate the Data Source Type ListBox
     */
    protected void doPopulateTranslatorListBox() {
        dataSourceService.getTranslators(new IRpcServiceInvocationHandler<List<String>>() {
            @Override
            public void onReturn(List<String> translators) {
                populateTranslatorListBox(translators);
            }
            @Override
            public void onError(Throwable error) {
                //notificationService.sendErrorNotification(i18n.format("adddatasourcedialog.error-populating-types-listbox"), error); //$NON-NLS-1$
            }
        });
    }
    
    
    
    /*
     * Init the List of DataSource Template Names
     * @param vdbName the name of the VDB
     * @param sourceName the source name
     * @param templateName the template name
     * @param translatorName the translator name
     * @param propsMap the property Map of name-value pairs
     */
    private void populateSourceTypeListBox(List<String> sourceTypes) {
    	// Make sure clear first
    	sourceTypeListBox.clear();

    	sourceTypeListBox.insertItem(Constants.NO_TYPE_SELECTION, 0);
    	
    	// Repopulate the ListBox. The actual names 
    	int i = 1;
    	for(String typeName: sourceTypes) {
    		sourceTypeListBox.insertItem(typeName, i);
    		i++;
    	}

    	// Initialize by setting the selection to the first item.
    	sourceTypeListBox.setSelectedIndex(0);
    }
    
    private void populateTranslatorListBox(List<String> translators) {
    	// Make sure clear first
    	translatorListBox.clear();

    	translatorListBox.insertItem(Constants.NO_TRANSLATOR_SELECTION, 0);
    	
    	// Repopulate the ListBox. The actual names 
    	int i = 1;
    	for(String translatorName: translators) {
    		translatorListBox.insertItem(translatorName, i);
    		i++;
    	}

    	// Initialize by setting the selection to the first item.
    	translatorListBox.setSelectedIndex(0);
    }
    
    /**
     * Populate the properties table for the supplied Source Type
     * @param selectedType the selected SourceType
     */
    protected void doPopulatePropertiesTable(String selectedType) {
        if(selectedType.equals(Constants.NO_TYPE_SELECTION)) {
        	dataSourceCorePropertiesTable.clear();
        	dataSourceAdvancedPropertiesTable.clear();
        	return;
        }

        dataSourceService.getDataSourceTypeProperties(selectedType, new IRpcServiceInvocationHandler<List<DataSourcePropertyBean>>() {
            @Override
            public void onReturn(List<DataSourcePropertyBean> propList) {
            	currentPropList.clear();
            	currentPropList.addAll(propList);
                populateCorePropertiesTable();
                populateAdvancedPropertiesTable();
                //updateDialogStatus();
            }
            @Override
            public void onError(Throwable error) {
                //notificationService.sendErrorNotification(i18n.format("adddatasourcedialog.error-populating-properties-table"), error); //$NON-NLS-1$
            }
        });

    }
    
    /*
     * Populate the core properties table
     * @param dsDetailsBean the Data Source details
     */
    private void populateCorePropertiesTable( ) {
    	dataSourceCorePropertiesTable.clear();

//        final SortColumn currentSortColumnCore = this.dataSourceCorePropertiesTable.getCurrentSortColumn();

    	// Separate property types, sorted in correct order
//    	List<DataSourcePropertyBean> corePropList = getPropList(this.currentPropList, true, !currentSortColumnCore.ascending);
    	List<DataSourcePropertyBean> corePropList = getPropList(this.currentPropList, true, true);
    	// Populate core properties table
//    	for(DataSourcePropertyBean defn : corePropList) {
//    		dataSourceCorePropertiesTable.addRow(defn);
//    	}
//    	dataSourceCorePropertiesTable.setValueColTextBoxWidths();
//    	dataSourceCorePropertiesTable.setVisible(true);
    	dataSourceCorePropertiesTable.setData(corePropList);
    }

    /*
     * Populate the advanced properties table
     * @param dsDetailsBean the Data Source details
     */
    private void populateAdvancedPropertiesTable() {
    	dataSourceAdvancedPropertiesTable.clear();

//        final SortColumn currentSortColumnAdv = this.dataSourceAdvancedPropertiesTable.getCurrentSortColumn();

    	// Separate property types, sorted in correct order
//    	List<DataSourcePropertyBean> advPropList = getPropList(this.currentPropList, false, !currentSortColumnAdv.ascending);
    	List<DataSourcePropertyBean> advPropList = getPropList(this.currentPropList, false, true);
    	// Populate advanced properties table
//    	for(DataSourcePropertyBean defn : advPropList) {
//    		dataSourceAdvancedPropertiesTable.addRow(defn);
//    	}
//    	dataSourceAdvancedPropertiesTable.setValueColTextBoxWidths();
//    	dataSourceAdvancedPropertiesTable.setVisible(true);
    	dataSourceAdvancedPropertiesTable.setData(advPropList);
    }
    
    /*
     * Filters the supplied list by correct type and order
     * @param propList the complete list of properties
     * @param getCore if 'true', returns the core properties.  if 'false' returns the advanced properties
     * @param acending if 'true', sorts in ascending name order.  descending if 'false'
     */
    private List<DataSourcePropertyBean> getPropList(List<DataSourcePropertyBean> propList, boolean getCore, boolean ascending) {
    	List<DataSourcePropertyBean> resultList = new ArrayList<DataSourcePropertyBean>();
    	
    	// Put only the desired property type into the resultList
    	for(DataSourcePropertyBean prop : propList) {
    		if(prop.isCoreProperty() && getCore) {
    			resultList.add(prop);
    		} else if(!prop.isCoreProperty() && !getCore) {
    			resultList.add(prop);    			
    		}
    	}
    	
    	// Sort by name in the desired order
    	Collections.sort(resultList,new PropertyBeanComparator(ascending));
    	
    	return resultList;
    }
    
    private DataSourceDetailsBean getDetailsBean() {
    	DataSourceDetailsBean resultBean = new DataSourceDetailsBean();
    	resultBean.setName(name.getText());
    	resultBean.setType(getSelectedSourceType());

    	List<DataSourcePropertyBean> props = new ArrayList<DataSourcePropertyBean>();
    	List<DataSourcePropertyBean> coreProps = dataSourceCorePropertiesTable.getBeansWithRequiredOrNonDefaultValue();
    	List<DataSourcePropertyBean> advancedProps = dataSourceAdvancedPropertiesTable.getBeansWithRequiredOrNonDefaultValue();
    	props.addAll(coreProps);
    	props.addAll(advancedProps);

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
    
    /**
     * Event handler that fires when the user clicks the Cancel button.
     * @param event
     */
//    @EventHandler("btn-create-service-cancel")
//    public void onCancelButtonClick(ClickEvent event) {
//    	placeManager.goTo("DataServicesLibraryScreen");
//    }
        
}
