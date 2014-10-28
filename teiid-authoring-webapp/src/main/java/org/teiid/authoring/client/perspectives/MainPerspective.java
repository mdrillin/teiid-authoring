package org.teiid.authoring.client.perspectives;

import javax.enterprise.context.ApplicationScoped;

import org.teiid.authoring.share.Constants;
import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PanelType;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;
  
/**
 * The MainPerspective for Teiid Web application
 */
@ApplicationScoped
@WorkbenchPerspective(identifier = "MainPerspective", isDefault = true)
public class MainPerspective {

    @Perspective
    public PerspectiveDefinition buildPerspective() {
        PerspectiveDefinition perspective = new PerspectiveDefinitionImpl( PanelType.ROOT_STATIC );
        perspective.setTransient(true);
        perspective.setName("MainPerspective");
        perspective.getRoot().addPart(new PartDefinitionImpl(new DefaultPlaceRequest(Constants.DATA_SERVICES_LIBRARY_SCREEN)));

        return perspective;
    }
}
