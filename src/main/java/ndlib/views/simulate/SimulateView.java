package ndlib.views.simulate;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import ndlib.views.MainLayout;

@PageTitle("Simulate")
@Route(value = "simulate", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class SimulateView extends VerticalLayout {

    private FormLayout formLayout;
    private ComboBox<String> modelTypeComboBox;
    private ComboBox<String> specificModelComboBox;

    public SimulateView() {
        // Intestazione
        H1 header = new H1("Simulation Page");
        header.getStyle().set("color", "black");
        header.getStyle().set("font-weight", "bold");

        // Menù a tendina per la selezione del tipo di modello di diffusione
        modelTypeComboBox = new ComboBox<>("Diffusion Methods type");
        modelTypeComboBox.setItems("Static Epidemic Models", "Dynamic Epidemic Models", "Opinion Models");
        modelTypeComboBox.addValueChangeListener(event -> updateSpecificModelComboBox(event.getValue()));

        // Menù a tendina per la selezione del tipo specifico di modello
        specificModelComboBox = new ComboBox<>("Types of model");
        specificModelComboBox.setVisible(false);
        specificModelComboBox.addValueChangeListener(event -> updateForm(event.getValue()));

        // Layout del form
        formLayout = new FormLayout();

        // Aggiunta dei componenti al layout
        add(header, modelTypeComboBox, specificModelComboBox, formLayout);
    }

    private void updateSpecificModelComboBox(String modelType) {
        specificModelComboBox.clear();
        specificModelComboBox.setVisible(true);

        switch (modelType) {
            case "Static Epidemic Models":
                specificModelComboBox.setItems("SI", "SIR", "SIS", "SEIS", "SWIR", "Threshold", "Kertesz Threshold",
                        "Independent Cascades", "Node Profile", "Node Profile-Threshold");
                break;
            case "Dynamic Epidemic Models":
                specificModelComboBox.setItems("DynSI", "DynSIS", "DynSIR");
                break;
            case "Opinion Models":
                specificModelComboBox.setItems("Voter", "Snajzd", "Q-Vote", "Majority Rule", "Cognitive Opinion Dynamics");
                break;
            default:
                specificModelComboBox.setVisible(false);
                break;
        }
    }

    private void updateForm(String model) {
        formLayout.removeAll();
        if (model == null) {
            return;
        }

        switch (model) {
            case "SI":
            case "SIR":
            case "SIS":
            case "SEIS":
            case "SWIR":
            case "Threshold":
            case "Kertesz Threshold":
            case "Independent Cascades":
            case "Node Profile":
            case "Node Profile-Threshold":
                addFieldsToForm("Variable 1", "Variable 2", "Variable 3");
                break;
            case "DynSI":
            case "DynSIS":
            case "DynSIR":
                addFieldsToForm("Variable 1", "Variable 2", "Variable 3", "Variable 4");
                break;
            case "Voter":
            case "Snajzd":
            case "Q-Vote":
            case "Majority Rule":
            case "Cognitive Opinion Dynamics":
                addFieldsToForm("Variable 1");
                break;
        }

        // Aggiungere un bottone per l'invio
        Button submitButton = new Button("Submit");
        formLayout.add(submitButton);
    }

    private void addFieldsToForm(String... fieldNames) {
        for (String fieldName : fieldNames) {
            TextField textField = new TextField(fieldName);
            formLayout.add(textField);
        }
    }
}
