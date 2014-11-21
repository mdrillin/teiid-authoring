/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.teiid.authoring.client.dialogs;

import org.kie.uberfire.client.common.popups.KieBaseModal;

import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * This builds on the FormStyleLayout for providing common popup features in a
 * columnar form layout, with a title and a large (ish) icon.
 */
public class UploadDialog extends KieBaseModal {

	private VerticalPanel vPanel = new VerticalPanel();

    public UploadDialog(final UploadContentPanel contentPanel, final String title) {
        setup( contentPanel, title);
    }

    protected void setup(final UploadContentPanel contentPanel, final String title) {
    	contentPanel.setDialog(this);
    	this.vPanel.add(contentPanel);
    	
        setTitle(title);
    }

    public void setSize(int width, int height) {
    	//this.contentPanel.setSize(width+"px", height+"px");
    }
}
