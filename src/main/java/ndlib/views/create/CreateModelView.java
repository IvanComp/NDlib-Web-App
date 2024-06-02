package ndlib.views.create;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import ndlib.views.MainLayout;

@PageTitle("Create Model")
@Route(value = "create", layout = MainLayout.class)
public class CreateModelView  extends VerticalLayout {

    public CreateModelView() {


    }

}
