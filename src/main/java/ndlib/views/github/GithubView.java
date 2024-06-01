package ndlib.views.github;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import ndlib.views.MainLayout;

@PageTitle("Github")
@Route(value = "github", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)

public class GithubView  extends VerticalLayout {


    public GithubView() {

    }

}
