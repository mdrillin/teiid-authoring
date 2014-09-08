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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TextBox;

/**
 * EditDataServiceScreen - used to edit existing Data Services
 *
 */
@Dependent
@Templated("./EditDataServiceScreen.html#page")
@WorkbenchScreen(identifier = "EditDataServiceScreen")
public class EditDataServiceScreen extends Composite {

    @Inject
    private PlaceManager placeManager;
    
    @Inject @DataField("textbox-edit-service-name")
    protected TextBox serviceNameTextBox;
    
    @Inject @DataField("textbox-edit-service-description")
    protected TextBox serviceDescriptionTextBox;
    
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
    
    /**
     * Event handler that fires when the user clicks the SaveChanges button.
     * @param event
     */
    @EventHandler("btn-edit-service-save")
    public void onSaveServiceButtonClick(ClickEvent event) {
    	placeManager.goTo("DataServiceDetailsScreen");
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
