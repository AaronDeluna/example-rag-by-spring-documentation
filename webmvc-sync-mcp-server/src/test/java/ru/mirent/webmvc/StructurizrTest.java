//package ru.mirent.webmvc;
//
//import com.structurizr.Workspace;
//import com.structurizr.export.Diagram;
//import com.structurizr.export.plantuml.StructurizrPlantUMLExporter;
//import com.structurizr.model.Component;
//import com.structurizr.model.Container;
//import com.structurizr.model.Person;
//import com.structurizr.model.SoftwareSystem;
//import com.structurizr.view.ComponentView;
//import com.structurizr.view.ContainerView;
//import com.structurizr.view.SystemContextView;
//import org.junit.jupiter.api.Test;
//
//public class StructurizrTest {
//
//    @Test
//    void exampleViewsDiagramTest() {
//        Workspace workspace = new Workspace("Test Workspace", "Workspace Description");
//        SoftwareSystem softwareSystem = workspace.getModel().addSoftwareSystem("Test SoftwareSystem");
//
//        SystemContextView systemContextView = workspace.getViews().createSystemContextView(softwareSystem, "k1", "v1");
//        Person person = workspace.getModel().addPerson("Test Person");
//        systemContextView.add(person);
//        systemContextView.add(softwareSystem);
//
//        Container container1 = softwareSystem.addContainer("Test Container 1");
//        ComponentView componentView = workspace.getViews().createComponentView(container1, "k2", "v2");
//        Component component11 = container1.addComponent("Test Component11");
//        component11.setGroup("Test Group 1");
//        componentView.add(component11);
//        Component component12 = container1.addComponent("Test Component12");
//        component12.setGroup("Test Group 1");
//        componentView.add(component12);
//
//        Container container2 = softwareSystem.addContainer("Test Container 2");
//        Component component21 = container2.addComponent("Test Component21");
//        component21.setGroup("Test Group 2");
//        componentView.add(component21);
//        Component component22 = container2.addComponent("Test Component22");
//        component22.setGroup("Test Group 2");
//        componentView.add(component22);
//
//        ContainerView containerView = workspace.getViews().createContainerView(softwareSystem, "k3", "v3");
//        containerView.add(container1);
//        containerView.add(container2);
//
//        Diagram diagramSystemContextView = new StructurizrPlantUMLExporter().export(systemContextView);
//        System.out.println(diagramSystemContextView.getDefinition());
//        System.out.println();
//        Diagram diagramContainerView = new StructurizrPlantUMLExporter().export(containerView);
//        System.out.println(diagramContainerView.getDefinition());
//        System.out.println();
//        Diagram diagramComponentView = new StructurizrPlantUMLExporter().export(componentView);
//        System.out.println(diagramComponentView.getDefinition());
//    }
//}
