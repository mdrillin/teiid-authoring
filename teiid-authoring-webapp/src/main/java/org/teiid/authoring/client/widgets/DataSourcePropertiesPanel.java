package org.teiid.authoring.client.widgets;

import java.util.ArrayList;
import java.util.Collections;
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
import org.teiid.authoring.client.messages.ClientMessages;
import org.teiid.authoring.client.resources.AppResource;
import org.teiid.authoring.client.services.DataSourceRpcService;
import org.teiid.authoring.client.services.NotificationService;
import org.teiid.authoring.client.services.rpc.IRpcServiceInvocationHandler;
import org.teiid.authoring.share.Constants;
import org.teiid.authoring.share.beans.DataSourcePropertyBean;
import org.teiid.authoring.share.beans.DataSourceWithVdbDetailsBean;
import org.teiid.authoring.share.beans.NotificationBean;
import org.teiid.authoring.share.beans.PropertyBeanComparator;
import org.teiid.authoring.share.services.StringUtils;
import org.uberfire.client.common.Modal;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

@Templated("./DataSourcePropertiesPanel.html")
public class DataSourcePropertiesPanel extends Composite {

    @Inject
    private ClientMessages i18n;
    @Inject
    private NotificationService notificationService;
    
	private String statusEnterName = null;
	private String statusSelectTrans = null;
	private String statusClickSave = null;
	private String statusEnterProps = null;
	
	// Map of server sourceName to corresponding default translator
	private Map<String,String> defaultTranslatorMap = new HashMap<String,String>();
	// Current properties
    private List<DataSourcePropertyBean> currentPropList = new ArrayList<DataSourcePropertyBean>();
	// The type of the current source
    private String selectedSourceType;
    // List of Data Source type Toggle Buttons
    private List<ToggleButton> dsTypeButtons = new ArrayList<ToggleButton>();

    // Keeps track of original type, name and translator, before any edits are made
    private String originalType;
    private String originalName;
    private String originalTranslator;
    
    Modal modalDialog = new Modal();
    
    @Inject
    protected DataSourceRpcService dataSourceService;
   
    @Inject @DataField("label-dsprops-title")
    protected Label dsDetailsPanelTitle;
    
    @Inject @DataField("label-dsprops-status")
    protected Label statusLabel;
    
    @Inject @DataField("textbox-dsprops-name")
    protected TextBox name;
    @Inject @DataField("dtypes-button-panel")
    protected FlowPanel dTypesButtonPanel;
    
    @Inject @DataField("listbox-dsprops-translator")
    protected ListBox translatorListBox;
    @Inject @DataField("editor-dsprops-core-properties")
    protected DataSourcePropertyEditor dataSourceCorePropertyEditor;
    @Inject @DataField("editor-dsprops-adv-properties")
    protected DataSourcePropertyEditor dataSourceAdvancedPropertyEditor;
    
    @Inject @DataField("btn-dsprops-save1")
    protected Button saveSourceChanges1;
    
    @Inject @DataField("btn-dsprops-save2")
    protected Button saveSourceChanges2;
    
    @Inject Event<String> saveEvent;
    
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	dsDetailsPanelTitle.setText("Data Source: [New Source]");
    	
		statusEnterName = i18n.format("ds-properties-panel.status-enter-name");
		statusSelectTrans = i18n.format("ds-properties-panel.status-select-translator");
		statusClickSave = i18n.format("ds-properties-panel.status-click-save");
		statusEnterProps = i18n.format("ds-properties-panel.status-enter-props");
		
    	doPopulateSourceTypesPanel();
    	
    	doPopulateTranslatorListBox();
    	
    	dataSourceCorePropertyEditor.clear();
    	dataSourceAdvancedPropertyEditor.clear();
    	
        name.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                updateStatus();
            }
        });
        // Change Listener for Type ListBox
        translatorListBox.addChangeHandler(new ChangeHandler()
        {
        	// Changing the Type selection will re-populate property table with defaults for that type
        	public void onChange(ChangeEvent event)
        	{
                updateStatus();
        	}
        });
        updateStatus();
    }
    
    /**
     * Event from properties table when property is edited
     * @param propertyName
     */
    public void onPropertyChanged(@Observes DataSourcePropertyBean propertyBean) {
    	updateStatus();
    }
    
    /**
     * Event handler that fires when the user clicks the top save button.
     * @param event
     */
    @EventHandler("btn-dsprops-save1")
    public void onSaveChangesTopButtonClick(ClickEvent event) {
    	saveChangesButtonClick(event);
    }
    
    /**
     * Event handler that fires when the user clicks the top save button.
     * @param event
     */
    @EventHandler("btn-dsprops-save1")
    public void onSaveChangesBottomButtonClick(ClickEvent event) {
    	saveChangesButtonClick(event);
    }
    
    private void saveChangesButtonClick(ClickEvent event) {
    	modalDialog = new Modal();
    	modalDialog.setTitle("Saving Source Changes");
    	modalDialog.show();
    	
        DataSourceWithVdbDetailsBean sourceBean = getDetailsBean();
        // No name change - create will take care of redeploys
        if(StringUtils.valuesAreEqual(this.originalName, this.name.getText())) {
        	doCreateDataSource(sourceBean);
        // Name change - first undeploy the original named sources
        } else {
        	List<String> originalDsNames = new ArrayList<String>();
        	originalDsNames.add(this.originalName);
        	originalDsNames.add(Constants.SERVICE_SOURCE_VDB_PREFIX+this.originalName);
        	
        	doDeleteThenCreateDataSource(originalDsNames,sourceBean);
        }
    }
    
    private DataSourceWithVdbDetailsBean getDetailsBean() {
    	DataSourceWithVdbDetailsBean resultBean = new DataSourceWithVdbDetailsBean();
    	String dsName = name.getText();
    	resultBean.setName(dsName);
    	resultBean.setType(this.selectedSourceType);
    	resultBean.setTranslator(getSelectedTranslator());
    	resultBean.setSourceVdbName(Constants.SERVICE_SOURCE_VDB_PREFIX+dsName);

    	List<DataSourcePropertyBean> props = new ArrayList<DataSourcePropertyBean>();
    	List<DataSourcePropertyBean> coreProps = dataSourceCorePropertyEditor.getBeansWithRequiredOrNonDefaultValue();
    	List<DataSourcePropertyBean> advancedProps = dataSourceAdvancedPropertyEditor.getBeansWithRequiredOrNonDefaultValue();
    	props.addAll(coreProps);
    	props.addAll(advancedProps);

    	resultBean.setProperties(props);
    	return resultBean;
    }
    
    /**
     * Populate the Data Source Types Panel
     */
    protected void doPopulateSourceTypesPanel() {
        dataSourceService.getDataSourceTypes(new IRpcServiceInvocationHandler<List<String>>() {
            @Override
            public void onReturn(List<String> dsTypes) {
            	dsTypeButtons.clear();
            	// Generates toggle buttons for each type
                for(String dType : dsTypes) {
                	ImageResource img = getImageForType(dType);
                	Image buttonImage = new Image(img);
                	ToggleButton button = new ToggleButton(buttonImage);
                	button.getElement().setId(dType);
                	button.addClickHandler(new ClickHandler() {
                		public void onClick(ClickEvent event) {
            				Widget sourceWidget = (Widget)event.getSource();
            				String sourceType = sourceWidget.getElement().getId();
            				doPopulatePropertiesTable(sourceType);
            				selectedSourceType = sourceType;
                		}
                	});                	
                	DOM.setStyleAttribute(button.getElement(), "cssFloat", "left");
                	dTypesButtonPanel.add(button);
                	dsTypeButtons.add(button);
                }
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("ds-properties-panel.error-populating-dstypes"), error); //$NON-NLS-1$
            }
        });
    }
    
    /**
     * Gets the selected Data Source type
     * @return the currently selected type
     */
    public String getSelectedDataSourceType( ) {
    	return this.selectedSourceType;
    }
    
    /**
     * Sets the selected Data Source type button to the down position.
     * This method does not fire an event, just changes toggle position
     * @param dsType the data source type
     */
    public void setSelectedDataSourceType(String dsType) {
    	// Only need to change if dsType is different than current selection
    	if(dsType!=null && !dsType.equals(this.selectedSourceType)) {
    		// First de-select the current selection
    		for(ToggleButton tButton : dsTypeButtons) {
    			if(tButton.getElement().getId().equals(this.selectedSourceType)) {
    				tButton.setValue(false);
    			}
    		}
    		// Set new button toggle state down
    		for(ToggleButton tButton : dsTypeButtons) {
    			if(tButton.getElement().getId().equals(dsType)) {
    				tButton.setValue(true);
    				this.selectedSourceType = dsType;
    			}
    		}
    	}
    }
    
    private ImageResource getImageForType(String dsType) {
    	ImageResource img = null;
    	if(dsType.equals("file")) {
        	img = AppResource.INSTANCE.images().dsType_file_Image();
    	} else if(dsType.equals("google")) {
        	img = AppResource.INSTANCE.images().dsType_google_Image();
    	} else if(dsType.equals("h2")) {
        	img = AppResource.INSTANCE.images().dsType_h2_Image();
    	} else if(dsType.equals("infinispan")) {
        	img = AppResource.INSTANCE.images().dsType_infinispan_Image();
    	} else if(dsType.equals("ldap")) {
        	img = AppResource.INSTANCE.images().dsType_ldap_Image();
    	} else if(dsType.equals("modeshape")) {
        	img = AppResource.INSTANCE.images().dsType_modeshape_Image();
    	} else if(dsType.equals("mongodb")) {
        	img = AppResource.INSTANCE.images().dsType_mongodb_Image();
    	} else if(dsType.equals("salesforce")) {
        	img = AppResource.INSTANCE.images().dsType_salesforce_Image();
    	} else if(dsType.equals("teiid")) {
        	img = AppResource.INSTANCE.images().dsType_teiid_Image();
    	} else if(dsType.equals("teiid-local")) {
        	img = AppResource.INSTANCE.images().dsType_teiid_Image();
    	} else if(dsType.equals("webservice")) {
        	img = AppResource.INSTANCE.images().dsType_webservice_Image();
    	}
    	return img;
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
                notificationService.sendErrorNotification(i18n.format("ds-properties-panel.error-populating-translators"), error); //$NON-NLS-1$
            }
        });
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
     * Get the selected translator from the translator dropdown
     * @return
     */
    public String getSelectedTranslator() {
    	int index = translatorListBox.getSelectedIndex();
    	return translatorListBox.getValue(index);
    }
    
	public void setSelectedTranslator(String translatorName) {
		int indx = 0;
		int nItems = translatorListBox.getItemCount();
		for(int i=0; i<nItems; i++) {
			String itemText = translatorListBox.getItemText(i);
			if(itemText.equalsIgnoreCase(translatorName)) {
				indx = i;
				break;
			}
		}
		translatorListBox.setSelectedIndex(indx);
	}
	
	public void selectTranslatorForSource(String sourceName) {
		String translator = this.defaultTranslatorMap.get(sourceName);
		if(!StringUtils.isEmpty(translator)) {
			setSelectedTranslator(translator);
		} else {
			setSelectedTranslator(Constants.NO_TRANSLATOR_SELECTION);
		}
	}

	public void setDefaultTranslatorMappings(Map<String,String> defaultTranslatorMap) {
		this.defaultTranslatorMap.clear();
		this.defaultTranslatorMap.putAll(defaultTranslatorMap);
	}

    /**
     * Populate the properties table for the supplied Source Type
     * @param selectedType the selected SourceType
     */
    protected void doPopulatePropertiesTable(String selectedType) {
        if(selectedType.equals(Constants.NO_TYPE_SELECTION)) {
        	dataSourceCorePropertyEditor.clear();
        	dataSourceAdvancedPropertyEditor.clear();
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
                notificationService.sendErrorNotification(i18n.format("ds-properties-panel.error-populating-properties"), error); //$NON-NLS-1$
            }
        });
    }
    
    /**
     * Creates a DataSource
     * @param dsDetailsBean the data source details
     */
    private void doCreateDataSource(final DataSourceWithVdbDetailsBean detailsBean) {
    	final String dsName = detailsBean.getName();
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("ds-properties-panel.creating-datasource-title"), //$NON-NLS-1$
                i18n.format("ds-properties-panel.creating-datasource-msg", dsName)); //$NON-NLS-1$
        dataSourceService.createDataSourceWithVdb(detailsBean, new IRpcServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("ds-properties-panel.datasource-created"), //$NON-NLS-1$
                        i18n.format("ds-properties-panel.create-success-msg")); //$NON-NLS-1$
            	modalDialog.hide();
            	// fire event with the created DataSource name
            	saveEvent.fire(detailsBean.getName());
            }
            @Override
            public void onError(Throwable error) {
            	modalDialog.hide();
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("ds-properties-panel.create-error"), //$NON-NLS-1$
                        error);
            }
        });
    }
    
    /**
     * Called when the user confirms the dataSource deletion.
     */
    private void doDeleteThenCreateDataSource(final List<String> dsNamesToDelete, final DataSourceWithVdbDetailsBean detailsBean) {
        final NotificationBean notificationBean = notificationService.startProgressNotification(
                i18n.format("ds-properties-panel.creating-datasource-title"), //$NON-NLS-1$
                i18n.format("ds-properties-panel.creating-datasource-msg")); //$NON-NLS-1$
        dataSourceService.deleteDataSources(dsNamesToDelete, new IRpcServiceInvocationHandler<Void>() {
            @Override
            public void onReturn(Void data) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("ds-properties-panel.datasource-created"), //$NON-NLS-1$
                        i18n.format("ds-properties-panel.create-success-msg")); //$NON-NLS-1$
            	doCreateDataSource(detailsBean);
            }
            @Override
            public void onError(Throwable error) {
                notificationService.completeProgressNotification(notificationBean.getUuid(),
                        i18n.format("ds-properties-panel.create-error"), //$NON-NLS-1$
                        error);
            }
        });
    }
    
    /*
     * Populate the core properties table
     * @param dsDetailsBean the Data Source details
     */
    private void populateCorePropertiesTable( ) {
    	dataSourceCorePropertyEditor.clear();

    	List<DataSourcePropertyBean> corePropList = getPropList(this.currentPropList, true, true);
    	dataSourceCorePropertyEditor.setProperties(corePropList);
    }

    /*
     * Populate the advanced properties table
     * @param dsDetailsBean the Data Source details
     */
    private void populateAdvancedPropertiesTable() {
    	dataSourceAdvancedPropertyEditor.clear();

    	List<DataSourcePropertyBean> advPropList = getPropList(this.currentPropList, false, true);
    	dataSourceAdvancedPropertyEditor.setProperties(advPropList);
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
    
    public void setDataSource(String dsName) {
    	doGetDataSourceDetails(dsName);
    }
    
    /**
     * Get the Data Source With Source VDB details for the current Data Source name.
     * Populate the PropertiesPanel with it's properties
     * @param dataSourceName the data source name
     */
    protected void doGetDataSourceDetails(String dataSourceName) {

        dataSourceService.getDataSourceWithVdbDetails(dataSourceName, new IRpcServiceInvocationHandler<DataSourceWithVdbDetailsBean>() {
            @Override
            public void onReturn(DataSourceWithVdbDetailsBean dsDetailsBean) {
            	String title = "Data Source : "+dsDetailsBean.getName();
            	dsDetailsPanelTitle.setText(title);
            	
            	name.setText(dsDetailsBean.getName());
            	originalName = dsDetailsBean.getName();
            	
            	setSelectedDataSourceType(dsDetailsBean.getType());
            	originalType = dsDetailsBean.getType();
            	
            	String translator = dsDetailsBean.getTranslator();
            	if(!StringUtils.isEmpty(translator)) {
            		setSelectedTranslator(translator);
            		originalTranslator=translator;
            	} else {
        			setSelectedTranslator(Constants.NO_TRANSLATOR_SELECTION);
        			originalTranslator=Constants.NO_TRANSLATOR_SELECTION;
            	}

            	currentPropList.clear();
            	currentPropList.addAll(dsDetailsBean.getProperties());
            	populateCorePropertiesTable();
            	populateAdvancedPropertiesTable();
            	
            	updateStatus();
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("ds-properties-panel.details-fetch-error"), error); //$NON-NLS-1$
            }
        });       
        
    }
    
    private void updateStatus( ) {
    	String statusText = Constants.OK;
    	
    	// Warn for missing source name
    	String serviceName = name.getText();
    	if(StringUtils.isEmpty(serviceName)) {
    		statusText = statusEnterName;
    	}
    	
		// Warn for translator not selected
    	if(statusText.equals(Constants.OK)) {
        	String translator = getSelectedTranslator();
    		if(translator!=null && translator.equals(Constants.NO_TRANSLATOR_SELECTION)) {
    			statusText = statusSelectTrans;
    		}
    	}
    	
		// Get properties message
    	if(statusText.equals(Constants.OK)) {
        	statusText = getPropertyStatus();
    	}
    	
    	// Determine if any properties were changed
    	if(statusText.equals(Constants.OK)) {
    		boolean hasNameChange = hasNameChange();
    		boolean hasTypeChange = hasDataSourceTypeChange();
    		boolean hasTranslatorChange = hasTranslatorChange();
        	boolean hasPropChanges = hasPropertyChanges();
    		if(hasNameChange || hasTypeChange || hasTranslatorChange || hasPropChanges) {
    			statusLabel.setText(statusClickSave);
        		saveSourceChanges1.setEnabled(true);
        		saveSourceChanges2.setEnabled(true);
    		} else {
    			statusLabel.setText(statusEnterProps);
        		saveSourceChanges1.setEnabled(false);
        		saveSourceChanges2.setEnabled(false);
    		}
    	} else {
			statusLabel.setText(statusText);
    		saveSourceChanges1.setEnabled(false);
    		saveSourceChanges2.setEnabled(false);
    	}
    }
    
    /**
     * Returns 'true' if the name has changed, false if not
     * @return name changed status
     */
    private boolean hasNameChange() {
    	return !StringUtils.valuesAreEqual(this.originalName, this.name.getText());
    }
    
    /**
     * Returns 'true' if the type has changed, false if not
     * @return type changed status
     */
    private boolean hasDataSourceTypeChange() {
    	return !StringUtils.valuesAreEqual(this.originalType, getSelectedDataSourceType());
    }
    
    /**
     * Returns 'true' if the translator has changed, false if not
     * @return translator changed status
     */
    private boolean hasTranslatorChange() {
    	return !StringUtils.valuesAreEqual(this.originalTranslator, getSelectedTranslator());
    }
    
    /**
     * Returns 'true' if any properties have changed, false if no changes
     * @return property changed status
     */
    private boolean hasPropertyChanges() {
    	boolean coreTableHasChanges = this.dataSourceCorePropertyEditor.anyPropertyHasChanged();
    	boolean advTableHasChanges = this.dataSourceAdvancedPropertyEditor.anyPropertyHasChanged();
    	
    	return (coreTableHasChanges || advTableHasChanges) ? true : false;
    }
    
    private String getPropertyStatus() {
    	// ------------------
    	// Set status message
    	// ------------------
    	String propStatus = this.dataSourceCorePropertyEditor.getStatus();
    	if(propStatus.equalsIgnoreCase(Constants.OK)) {
    		propStatus = this.dataSourceAdvancedPropertyEditor.getStatus();
    	}

    	return propStatus;
    }
    
}