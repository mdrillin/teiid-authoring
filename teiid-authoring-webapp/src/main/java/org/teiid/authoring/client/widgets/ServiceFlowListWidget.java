package org.teiid.authoring.client.widgets;

import org.jboss.errai.ui.client.widget.ListWidget;

import com.google.gwt.user.client.ui.FlowPanel;

/**
 * ServiceFlowPanel - extends Errai ListWidget, providing the desired Flow behavior for the DataServicesLibraryScreen
 * 
 */
public class ServiceFlowListWidget extends ListWidget<ServiceRow, LibraryServiceWidget> {
	 
	  public ServiceFlowListWidget() {
	    super(new FlowPanel());
	  }
	  
	  @Override
	  public Class<LibraryServiceWidget> getItemWidgetType() {
	    return LibraryServiceWidget.class;
	  }
}