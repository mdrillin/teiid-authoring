package org.teiid.authoring.client.widgets;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.teiid.authoring.client.dialogs.UiEvent;
import org.teiid.authoring.client.dialogs.UiEventType;
import org.teiid.authoring.client.messages.ClientMessages;
import org.teiid.authoring.client.services.VdbRpcService;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Label;

@Templated("./QueryResultsPanel.html")
public class QueryResultsPanel extends Composite {

	private Label statusLabel = new Label();
	private boolean resultTableVisible = false;
	private String defaultMessage;
	private String fetchingDataMessage;
	private String noRowsMessage;
	private QueryResultPagedTableDisplayer queryResultsTablePaged = new QueryResultPagedTableDisplayer();
	
    @Inject @DataField("table-qresults-paged")
    protected DeckPanel contentDeckPanel;
	
    @Inject
    private ClientMessages i18n;
    
    @Inject
    protected VdbRpcService vdbService;    
    
//    <div class="form-group">
//    <div class="col-md-6">
//      <div class="alert alert-info" role="alert" data-field="label-query-results-status"></div>
//    </div>
//    <div class="col-md-6"></div>
//  </div>
    
    /**
     * Called after construction.
     */
    @PostConstruct
    protected void postConstruct() {
    	defaultMessage = i18n.format("query-resultpanel.status-default-message");
    	fetchingDataMessage = i18n.format("query-resultpanel.status-fetch-data-message");
    	noRowsMessage = i18n.format("query-resultpanel.status-norows-message");
    	    	
    	statusLabel.setText(defaultMessage);
    	
    	// Add properties panel and Select label to deckPanel
    	contentDeckPanel.add(statusLabel);
    	contentDeckPanel.add(queryResultsTablePaged);
    	contentDeckPanel.showWidget(0);
    	resultTableVisible=false;
    }   

    /**
     * Set the status message
     */
    public void showStatusMessage(String statusMsg) {
    	statusLabel.setText(statusMsg);
    	showMessage();
    }
    
    private void showMessage() {
    	if(resultTableVisible) {
    		contentDeckPanel.showWidget(0);
    		resultTableVisible = false;
    	}
    }
    
    /**
     * Set the Source JNDI and the query for the panel
     */
    public void showResultsTable(String sourceJndiName, String sql) {
    	showStatusMessage(fetchingDataMessage);
    	
    	// Set the provider and sql.  UiEvent is fired when it completes.
    	queryResultsTablePaged.setDataProvider(sourceJndiName, sql);    
    }
    
    private void showQueryTable() {
    	if(!resultTableVisible) {
    		contentDeckPanel.showWidget(1);
    		resultTableVisible = true;
    	}
    }
    
    /**
     * Handles Events from QueryResultPagedTableDisplayer
     * @param dEvent
     */
    public void onRefreshedEvent(@Observes UiEvent dEvent) {
    	// Table refresh has completed successfully.  Show the table
    	if(dEvent.getType() == UiEventType.QUERY_RESULT_DISPLAYER_REFRESHED_OK) {
    		showQueryTable();
    	// Refresh ok, but no data rows.
    	} else if(dEvent.getType() == UiEventType.QUERY_RESULT_DISPLAYER_REFRESHED_NOROWS) {
    		showStatusMessage(noRowsMessage);
        // Table refresh failed.  Show error message
    	} else if(dEvent.getType() == UiEventType.QUERY_RESULT_DISPLAYER_REFRESHED_ERROR) {
    		String errorMsg = queryResultsTablePaged.getErrorMessage();
    		showStatusMessage(errorMsg);
    	}
    }

}