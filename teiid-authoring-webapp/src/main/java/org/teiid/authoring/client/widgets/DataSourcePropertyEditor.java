/**
 * Copyright (C) 2014 JBoss Inc
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
package org.teiid.authoring.client.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.teiid.authoring.share.beans.Constants;
import org.teiid.authoring.share.beans.DataSourcePropertyBean;
import org.teiid.authoring.share.services.StringUtils;

import com.github.gwtbootstrap.client.ui.base.TextBox;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Composite for display of DataSource names
 */
public class DataSourcePropertyEditor extends Composite {

    protected VerticalPanel panel = new VerticalPanel();
    protected Label label = new Label();

    private List<DataSourcePropertyBean> propertyList = new ArrayList<DataSourcePropertyBean>();
    private Map<String,TextBox> nameTextBoxMap = new HashMap<String,TextBox>();
    
	@Inject Event<DataSourcePropertyBean> propertyChangeEvent;
	
    public DataSourcePropertyEditor() {
        initWidget( panel );
    }
    
    public void setProperties(List<DataSourcePropertyBean> properties) {
    	this.propertyList.clear();
    	this.nameTextBoxMap.clear();
    	VerticalPanel allPropsPanel = new VerticalPanel();
    	
    	for(DataSourcePropertyBean prop : properties) {     		
        	HorizontalPanel nameValuePanel = new HorizontalPanel();
    		Label nameLabel = new Label();
    		nameLabel.setWidth("180px");
    		nameLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    		nameLabel.setText(prop.getDisplayName());
    		nameValuePanel.add(nameLabel);
    		
    		TextBox valueTextBox = new TextBox();
    		valueTextBox.setWidth("400px");
    		valueTextBox.setText(prop.getValue());
    		valueTextBox.addKeyUpHandler(new KeyUpHandler() {
    			@Override
    			public void onKeyUp(KeyUpEvent event) {
    				updatePropertyValues();
    			}
    		});
    		valueTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
    			@Override
    			public void onValueChange(ValueChangeEvent<String> event) {
    				updatePropertyValues();
    			}
    		});
    		nameValuePanel.add(valueTextBox);
    		
    		allPropsPanel.add(nameValuePanel);
    		
    		this.propertyList.add(prop);
    		this.nameTextBoxMap.put(prop.getName(), valueTextBox);
    	}
    	panel.clear();
    	panel.add(allPropsPanel);
    }
    
    
    public List<DataSourcePropertyBean> getBeansWithRequiredOrNonDefaultValue() {
    	List<DataSourcePropertyBean> tableRows = getProperties();
    	
    	List<DataSourcePropertyBean> resultBeans = new ArrayList<DataSourcePropertyBean>();
    	for(DataSourcePropertyBean propBean : tableRows) {
    		if(propBean.isRequired()) {
    			resultBeans.add(propBean);
    		} else {
        		String defaultValue = propBean.getDefaultValue();
        		String value = propBean.getValue();
        		if(!StringUtils.valuesAreEqual(value, defaultValue)) {
        			resultBeans.add(propBean);
        		}
    		}
    	}
    	return resultBeans;
    }
    
    public void updatePropertyValues() {
    	for(DataSourcePropertyBean propBean : getProperties()) {
    		String propName = propBean.getName();
    		TextBox textBox = this.nameTextBoxMap.get(propName);
    		propBean.setValue(textBox.getText());
    	}
        propertyChangeEvent.fire(new DataSourcePropertyBean());
    }
    
    public void clear() {
    	setProperties(Collections.EMPTY_LIST);
    }
    
    public List<DataSourcePropertyBean> getProperties( ) {
    	return this.propertyList;
    }
        
    /*
     * Returns an overall status of the table properties.  Currently the only check is that required properties
     * have a value, but this can be expanded in the future.  If all properties pass, the status is 'OK'. If not, a
     * String identifying the problem is returned.
     * @return the status - 'OK' if no problems.
     */
    public String getStatus() {
    	// Assume 'OK' until a problem is found
    	String status = Constants.OK;

    	for(DataSourcePropertyBean propBean : getProperties()) {
    		String propName = propBean.getName();
    		String propValue = propBean.getValue();
    		boolean isRequired = propBean.isRequired();

    		// Check that required properties have a value
    		if(isRequired) {
    			if(propValue==null || propValue.trim().length()==0) {
    				status = "A value is required for property: '"+propName+"'";
    				break;
    			}
    		}
    	}

    	return status;
    }
    
    public boolean anyPropertyHasChanged() {
    	boolean hasChanges = false;
    	for(DataSourcePropertyBean propBean : getProperties()) {
    		String originalValue = propBean.getOriginalValue();
    		String value = propBean.getValue();
    		if(!StringUtils.valuesAreEqual(value, originalValue)) {
    			hasChanges = true;
    			break;
    		}
    	}
    	return hasChanges;
    }
    
}
