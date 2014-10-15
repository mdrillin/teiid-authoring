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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.authoring.client.messages.ClientMessages;
import org.teiid.authoring.client.services.NotificationService;
import org.teiid.authoring.client.services.TeiidRpcService;
import org.teiid.authoring.client.services.rpc.IRpcServiceInvocationHandler;
import org.teiid.authoring.client.widgets.QueryResultsPanel;
import org.teiid.authoring.share.Constants;
import org.teiid.authoring.share.beans.VdbDetailsBean;
import org.teiid.authoring.share.beans.VdbModelBean;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

/**
 * DataServiceDetailsScreen - shows details about the Data Service.
 *
 */
@Dependent
@Templated("./DataServiceDetailsScreen.html#page")
@WorkbenchScreen(identifier = "DataServiceDetailsScreen")
public class DataServiceDetailsScreen extends Composite {

	private String serviceName;
	private String serviceSampleSQL;
	private String pgTitle;
	private String serviceInternal;
	private String serviceExternal;
	
    @Inject
    private PlaceManager placeManager;
    @Inject
    private ClientMessages i18n;
    @Inject
    private NotificationService notificationService;
    
    @Inject
    private TeiidRpcService teiidService;

    @Inject @DataField("label-service-details-pagetitle")
    protected Label pageTitleLabel;
    
    @Inject @DataField("label-service-details-description")
    protected Label pageDescription;

    @Inject @DataField("anchor-goto-library")
    protected Anchor goToLibraryAnchor;

    @Inject @DataField("anchor-download-clientjar")
    protected Anchor downloadClientJarAnchor;
    
    @Inject @DataField("btn-edit-service")
    protected Button editServiceButton;
        
    @Inject @DataField("textarea-service-details-jdbc-snippet")
    protected TextArea jdbcSnippetArea;
    
    @Inject @DataField("textbox-service-details-rest")
    protected TextBox restLinkTextBox;
    
    @Inject @DataField("btn-service-details-open-rest")
    protected Button openRestButton;
    
    @Inject @DataField("textbox-service-details-odata")
    protected TextBox odataLinkTextBox;
   
    @Inject @DataField("btn-service-details-open-odata")
    protected Button openODataButton;
    
    @Inject @DataField("table-service-details-queryResults")
    protected QueryResultsPanel queryResultsPanel;

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
    	pgTitle = i18n.format("servicedetails.page-title");
    	serviceInternal = i18n.format("servicedetails.page-title-internal");
    	serviceExternal = i18n.format("servicedetails.page-title-external");
    }
    
    @OnStartup
    public void onStartup( final PlaceRequest place ) {
    	serviceName = place.getParameter(Constants.SERVICE_NAME_KEY, "[unknown]");
    	doGetDataServiceDetails(serviceName);    	
    }
    
    /**
     * Get the Data Service details to populate the page
     * @param serviceName the name of the service
     */
    protected void doGetDataServiceDetails(final String serviceName) {
    	final String serviceVdb = Constants.SERVICE_VDB_PREFIX+serviceName;
        teiidService.getVdbDetails(serviceVdb, new IRpcServiceInvocationHandler<VdbDetailsBean>() {
            @Override
            public void onReturn(VdbDetailsBean vdbDetailsBean) {
            	Collection<VdbModelBean> vdbModels = vdbDetailsBean.getModels();
            	for(VdbModelBean vdbModel : vdbModels) {
            		if(vdbModel.getName().equals(serviceName)) {
            			StringBuilder titleBuilder = new StringBuilder();
            			titleBuilder.append(pgTitle+serviceName);
            			if(vdbModel.isVisible()) {
            				titleBuilder.append(Constants.SPACE+serviceExternal);
            			} else {
            				titleBuilder.append(Constants.SPACE+serviceInternal);
            			}
             			
    	                pageTitleLabel.setText(titleBuilder.toString());
            			
            			String description = vdbModel.getDescription();
            			pageDescription.setText(description);
            			
            			String serverHostName = vdbDetailsBean.getServerHost();
            			           			
            			jdbcSnippetArea.setText(getJDBCConnectionString(serverHostName, serviceVdb));
            			
            			restLinkTextBox.setText(getRestLink(serverHostName,serviceVdb,1,serviceName));
            			odataLinkTextBox.setText(getODataLink(serverHostName,serviceVdb,1,serviceName));
            			
            			// Rest controls disabled.  May remove completely
            			restLinkTextBox.setEnabled(false);
            			openRestButton.setEnabled(false);
            			
            			serviceSampleSQL = Constants.SELECT_STAR_FROM+Constants.SPACE + 
            					           serviceName+Constants.DOT+Constants.SERVICE_VIEW_NAME+
            					           Constants.SPACE+Constants.LIMIT_10;
            			
            			queryResultsPanel.showResultsTable(Constants.JNDI_PREFIX+serviceVdb, serviceSampleSQL);
            		}
            	}
            }
            @Override
            public void onError(Throwable error) {
                notificationService.sendErrorNotification(i18n.format("servicedetails.error-retrieving-details"), error); //$NON-NLS-1$
            }
        });       
    }
    
    /**
     * Event handler that fires when the user clicks the Open REST in Browser button.
     * @param event
     */
    @EventHandler("btn-service-details-open-rest")
    public void onOpenRestButtonClick(ClickEvent event) {
    	String restLink = restLinkTextBox.getText();
        Window.open(restLink, "_blank", "");
    }
    
    /**
     * Event handler that fires when the user clicks the Open OData in Browser button.
     * @param event
     */
    @EventHandler("btn-service-details-open-odata")
    public void onOpenODataButtonClick(ClickEvent event) {
    	String oDataLink = odataLinkTextBox.getText();
        Window.open(oDataLink, "_blank", "");
    }
    
    private String getJDBCConnectionString(String serverHostName, String vdbName) {
    	StringBuilder sb = new StringBuilder();
    	sb.append("jdbc:teiid:"+vdbName);
    	sb.append("@mm://");
    	sb.append(serverHostName+":31000;[prop-name=prop-value;]");
    	return sb.toString();
    }
    
    private String getRestLink(String serverHostName, String vdbName,int vdbVersion,String modelName) {
        StringBuilder sb = new StringBuilder();
    	sb.append("http://"+serverHostName+":8080/");
    	sb.append(vdbName.toLowerCase()+"_"+vdbVersion+'/');
    	sb.append(modelName.toLowerCase()+'/');
    	// This is the uri property for the generated rest procedure
    	sb.append("procUriProperty");
    	return sb.toString();
    }
    
    private String getODataLink(String serverHostName, String vdbName,int vdbVersion,String modelName) {
        StringBuilder sb = new StringBuilder();
    	sb.append("http://"+serverHostName+":8080/odata/");
    	sb.append(vdbName.toLowerCase()+"."+vdbVersion+'/');
    	sb.append(modelName+'.');
    	sb.append(Constants.SERVICE_VIEW_NAME);
    	sb.append("?$format=JSON");
    	return sb.toString();
    }
    
    /**
     * Event handler that fires when the user clicks the EditService button.
     * @param event
     */
    @EventHandler("btn-edit-service")
    public void onEditServiceButtonClick(ClickEvent event) {
    	doEditService();
    }
    
    /**
     * Create Service - transitions to CreateDataServiceScreen
     */
    protected void doEditService() {
    	Map<String,String> parameters = new HashMap<String,String>();
    	parameters.put(Constants.SERVICE_NAME_KEY, serviceName);
    	
    	placeManager.goTo(new DefaultPlaceRequest("EditDataServiceScreen",parameters));
    }
    
    /**
     * Event handler that fires when the user clicks the GoTo Library anchor.
     * @param event
     */
    @EventHandler("anchor-goto-library")
    public void onGotoLibraryAnchorClick(ClickEvent event) {
    	placeManager.goTo("DataServicesLibraryScreen");
    }
    
    /**
     * Event handler that fires when the user clicks the Download Client jar anchor.
     * @param event
     */
    @EventHandler("anchor-download-clientjar")
    public void onDownloadClientJarAnchorClick(ClickEvent event) {
    	Window.alert("Sorry, download not yet implemented");
    }
            
}
