package org.teiid.authoring.client.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.resources.client.TextResource;

public interface AppCss extends ClientBundle {

	@NotStrict
    @Source("css/CustomToggle.css")
    CssResource customToggleStyle();
	
	@NotStrict
    @Source("css/DataGrid.css")
    CssResource customDataGridStyle();

	@NotStrict
    @Source("css/teiid-authoring.css")
    CssResource teiidAuthoringStyle();
	
	@Source("css/rcue.css") TextResource rcueCss();

}
