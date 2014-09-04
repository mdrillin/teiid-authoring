package org.teiid.authoring.client.perspectives;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PanelType;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;
  
// /**
// * A Perspective to show File Explorer
// */
@ApplicationScoped
@WorkbenchPerspective(identifier = "MainPerspective", isDefault = true)
public class MainPerspective {

    @Perspective
    public PerspectiveDefinition buildPerspective() {
        PerspectiveDefinition perspective = new PerspectiveDefinitionImpl( PanelType.ROOT_STATIC );
        perspective.setTransient(true);
        perspective.setName("MainPerspective");
        perspective.getRoot().addPart(new PartDefinitionImpl(new DefaultPlaceRequest("DataSourcesScreen")));

//        PanelDefinitionImpl westPanel = new PanelDefinitionImpl(PanelType.MULTI_LIST);
//        perspective.getRoot().insertChild(Position.WEST, westPanel);
//        westPanel.setWidth(250);
//        westPanel.addPart(
//                new PartDefinitionImpl(
//                        new DefaultPlaceRequest("MoodScreen")));

        return perspective;
    }
}
