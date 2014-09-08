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

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;

/**
 * DataServiceDetailsScreen - shows details about the Data Service.
 *
 */
@Dependent
@Templated("./DataServiceDetailsScreen.html#page")
@WorkbenchScreen(identifier = "DataServiceDetailsScreen")
public class DataServiceDetailsScreen extends Composite {

    @Inject
    private PlaceManager placeManager;
    
    @Inject @DataField("anchor-goto-library")
    protected Anchor goToLibraryAnchor;
    
    @Inject @DataField("btn-edit-service")
    protected Button editServiceButton;
        
    @Inject @DataField("textbox-service-details-odata")
    protected TextBox odataLinkTextBox;
    
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
    	placeManager.goTo("EditDataServiceScreen");
    }
    
    /**
     * Event handler that fires when the user clicks the GoTo Library anchor.
     * @param event
     */
    @EventHandler("anchor-goto-library")
    public void onGotoLibraryAnchorClick(ClickEvent event) {
    	placeManager.goTo("DataServicesLibraryScreen");
    }
            
}
