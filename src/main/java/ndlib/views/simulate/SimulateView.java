package ndlib.views.simulate;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import ndlib.views.MainLayout;

@PageTitle("Simulate")
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class SimulateView  extends VerticalLayout {


    public SimulateView() {

    }

}
