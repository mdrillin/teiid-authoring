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

/**
 * DataServicesLibraryScreen - shows all published Data Services.
 *
 */
@Dependent
@Templated("./DataServicesLibraryScreen.html#page")
@WorkbenchScreen(identifier = "DataServicesLibraryScreen")
public class DataServicesLibraryScreen extends Composite {

 
    @Inject
    private PlaceManager placeManager;
    
    @Inject @DataField("btn-create-service")
    protected Button createServiceButton;
    
    @Inject @DataField("anchor-create-service")
    protected Anchor createServiceAnchor;
    
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
     * Event handler that fires when the user clicks the CreateService button.
     * @param event
     */
    @EventHandler("btn-create-service")
    public void onCreateServiceButtonClick(ClickEvent event) {
    	doCreateService();
    }
    
    /**
     * Event handler that fires when the user clicks the CreateService anchor.
     * @param event
     */
    @EventHandler("anchor-create-service")
    public void onCreateServiceAnchorClick(ClickEvent event) {
    	doCreateService();
    }
        
    /**
     * Create Service - transitions to the Create Services page
     */
    protected void doCreateService() {
    	placeManager.goTo("CreateDataServiceScreen");
    }
    
}
