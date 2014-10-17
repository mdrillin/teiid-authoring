package org.teiid.authoring.client.dialogs;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.authoring.client.messages.ClientMessages;
import org.teiid.authoring.client.widgets.IImportCompletionHandler;
import org.teiid.authoring.client.widgets.ImportDataSourceTypeFormSubmitHandler;
import org.teiid.authoring.client.widgets.TemplatedFormPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

@Templated("./UploadContentPanel.html")
public class UploadContentPanel extends Composite {

    @Inject
    private ClientMessages i18n;
    
	@Inject @DataField("upload-content-form")
	protected TemplatedFormPanel form;

	@Inject @DataField("btn-upload")
	protected Button uploadButton;
	
	@Inject @DataField("btn-cancel")
	protected Button cancelButton;
	
	@Inject @DataField("label-title")
	protected Label titleLabel;
	
	@Inject @DataField("label-choose-file-message")
	protected Label chooseFileLabel;
	
	@Inject @DataField("label-enter-deployname-message")
	protected Label enterDeployNameLabel;

    @Inject
    private Instance<ImportDataSourceTypeFormSubmitHandler> formHandlerFactory;
    
    private ImportDataSourceTypeFormSubmitHandler formHandler;
    private UploadDialog uploadDialog;

	/**
	 * Called after construction.
	 */
	@PostConstruct
	protected void postConstruct() {
		titleLabel.setText(i18n.format("upload-dtype-dialog.dialog-title"));
		chooseFileLabel.setText(i18n.format("upload-dtype-dialog.dialog-choosefile-message"));
		enterDeployNameLabel.setText(i18n.format("upload-dtype-dialog.dialog-entername-message"));
		
		formHandler = formHandlerFactory.get();
		form.addSubmitHandler(formHandler);
		form.addSubmitCompleteHandler(formHandler);
	}
	
	public void setDialog(UploadDialog theDialog) {
		uploadDialog = theDialog;
		formHandler.setDialog(theDialog);
	}

    /**
     * @return the completionHandler
     */
    public IImportCompletionHandler getCompletionHandler() {
        return formHandler.getCompletionHandler();
    }

    /**
     * @param completionHandler the completionHandler to set
     */
    public void setCompletionHandler(IImportCompletionHandler completionHandler) {
        this.formHandler.setCompletionHandler(completionHandler);
    }
    
	/**
	 * Event handler that fires when the user clicks the Upload button.
	 * @param event
	 */
	@EventHandler("btn-upload")
	public void onUploadButtonClick(ClickEvent event) {
		form.setAction(getWebContext() + "/services/dataVirtUpload");
		form.submit();
	}  
	
	/**
	 * Event handler that fires when the user clicks the Cancel button.
	 * @param event
	 */
	@EventHandler("btn-cancel")
	public void onCancelButtonClick(ClickEvent event) {
		uploadDialog.hide();
	}  
	
    private String getWebContext() {
        String context = GWT.getModuleBaseURL().replace( GWT.getModuleName() + "/", "" );
        if ( context.endsWith( "/" ) ) {
            context = context.substring( 0, context.length() - 1 );
        }
        return context;
    }

}
