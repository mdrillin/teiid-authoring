package org.teiid.authoring.client.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;

public interface AppCss extends ClientBundle {

	@NotStrict
    @Source("css/CustomToggle.css")
    CssResource customToggleStyle();
	
	@NotStrict
    @Source("css/DataGrid.css")
    CssResource customDataGridStyle();
    
}
