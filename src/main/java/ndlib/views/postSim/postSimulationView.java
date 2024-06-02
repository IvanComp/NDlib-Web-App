package ndlib.views.postSim;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ndlib.views.MainLayout;
import com.vaadin.flow.server.VaadinSession;

@PageTitle("postSim")
@Route(value = "postSim", layout = MainLayout.class)
public class postSimulationView extends VerticalLayout {

    public postSimulationView() {
        Div simulationResults = new Div();

        String diffusionMethodsType = (String) VaadinSession.getCurrent().getAttribute("diffusionMethodsType");
        String typesOfModel = (String) VaadinSession.getCurrent().getAttribute("typesOfModel");
        String simulationParameters = (String) VaadinSession.getCurrent().getAttribute("simulationParameters");

        if (diffusionMethodsType != null && typesOfModel != null && simulationParameters != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("<b>Diffusion Methods Type:</b> ").append(diffusionMethodsType).append("<br>");
            sb.append("<b>Types of Model:</b> ").append(typesOfModel).append("<br>");
            sb.append(simulationParameters);

            simulationResults.getElement().setProperty("innerHTML", sb.toString());
        } else {
            simulationResults.setText("No simulation results available.");
        }

        add(simulationResults);
    }
}
