package org.teiid.authoring.client.dialogs;

import org.kie.uberfire.client.common.Popup;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This builds on the FormStyleLayout for providing common popup features in a
 * columnar form layout, with a title and a large (ish) icon.
 */
public class UploadDialog extends Popup {

	private VerticalPanel vPanel = new VerticalPanel();

    public UploadDialog(final UploadContentPanel contentPanel, final String title) {
        setup( contentPanel, title);
    }

    protected void setup(final UploadContentPanel contentPanel, final String title) {
    	contentPanel.setDialog(this);
    	this.vPanel.add(contentPanel);
    	
        setModal( true );
        
        setTitle(title);
    }

    @Override
    public Widget getContent() {
        return this.vPanel;
    }
            
    public void setSize(int width, int height) {
    	//this.contentPanel.setSize(width+"px", height+"px");
    }
}
