package ndlib.views.simulate;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextArea;
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
    private TextArea outputArea;
    private TextArea modelDescriptionArea;
    private Button runSimulationButton;

    public SimulateView() {
        // Intestazione
        H1 header = new H1("Simulation Page");
        header.getStyle().set("color", "black");
        header.getStyle().set("font-weight", "bold");

        // Menù a tendina per la selezione del tipo di modello di diffusione
        modelTypeComboBox = new ComboBox<>("Diffusion Methods type");
        modelTypeComboBox.setItems("Static Epidemic Models", "Dynamic Epidemic Models", "Opinion Models");
        modelTypeComboBox.addValueChangeListener(event -> updateSpecificModelComboBox(event.getValue()));
        modelTypeComboBox.getStyle().set("min-width", "280px");  // Imposta una larghezza minima per il ComboBox
        modelTypeComboBox.addClassName("custom-combobox");  // Aggiunge la classe CSS personalizzata

        // Menù a tendina per la selezione del tipo specifico di modello
        specificModelComboBox = new ComboBox<>("Types of model");
        specificModelComboBox.setVisible(false);
        specificModelComboBox.addValueChangeListener(event -> {
            updateForm(event.getValue());
            updateModelDescription(event.getValue());
        });
        specificModelComboBox.getStyle().set("min-width", "280px");  // Imposta una larghezza minima per il ComboBox
        specificModelComboBox.addClassName("custom-combobox");  // Aggiunge la classe CSS personalizzata

        // Layout verticale per i due menù a tendina
        VerticalLayout comboBoxLayout = new VerticalLayout(modelTypeComboBox, specificModelComboBox);
        comboBoxLayout.setAlignItems(Alignment.START);
        comboBoxLayout.setSpacing(false);
        comboBoxLayout.setPadding(false);

        // Layout del form
        formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 1)
        );

        // TextArea per la descrizione del modello selezionato
        modelDescriptionArea = new TextArea("Model Description");
        modelDescriptionArea.setReadOnly(true);
        modelDescriptionArea.setWidthFull();
        modelDescriptionArea.setVisible(false); // Nascondi inizialmente

        // Layout orizzontale per i menù a tendina e il form
        HorizontalLayout comboBoxAndFormLayout = new HorizontalLayout(comboBoxLayout, formLayout);
        comboBoxAndFormLayout.setAlignItems(Alignment.START);
        comboBoxAndFormLayout.setWidthFull();
        comboBoxAndFormLayout.setFlexGrow(1, comboBoxLayout);
        comboBoxAndFormLayout.setFlexGrow(2, formLayout);

        // Area di testo per mostrare i valori del modello scelto
        outputArea = new TextArea("Model & Parameters of the Simulation");
        outputArea.setReadOnly(true);
        outputArea.setWidthFull();
        outputArea.setVisible(false); // Nascondi inizialmente

        // Bottone per eseguire la simulazione
        runSimulationButton = new Button("Run Simulation", e -> runSimulation());
        runSimulationButton.setWidthFull();
        runSimulationButton.setVisible(false); // Nascondi inizialmente

        // Layout principale
        VerticalLayout mainLayout = new VerticalLayout(header, comboBoxAndFormLayout, modelDescriptionArea);
        mainLayout.setAlignItems(Alignment.START);
        mainLayout.setWidthFull();

        // Aggiunta dei componenti al layout principale
        add(mainLayout);

        // Aggiunta dell'area di testo e del pulsante di esecuzione in fondo alla pagina
        VerticalLayout bottomLayout = new VerticalLayout(outputArea, runSimulationButton);
        bottomLayout.setAlignItems(Alignment.CENTER);
        bottomLayout.setWidthFull();
        add(bottomLayout);
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
        modelDescriptionArea.setVisible(true); // Mostra la descrizione del modello
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
        Button submitButton = new Button("Save Parameters", e -> {
            printSelectedModelValues();
            runSimulationButton.setVisible(true); // Mostra il bottone "Run Simulation"
        });
        formLayout.add(submitButton);
    }

    private void addFieldsToForm(String... fieldNames) {
        for (String fieldName : fieldNames) {
            TextField textField = new TextField(fieldName);
            textField.setId(fieldName.replace(" ", "").toLowerCase()); // Imposta un ID univoco per ogni campo
            formLayout.add(textField);
        }
    }

    private void printSelectedModelValues() {
        StringBuilder sb = new StringBuilder();
        sb.append("Diffusion Methods type: ").append(modelTypeComboBox.getValue()).append("\n");
        sb.append("Types of model: ").append(specificModelComboBox.getValue()).append("\n");

        for (com.vaadin.flow.component.Component component : formLayout.getChildren().toArray(com.vaadin.flow.component.Component[]::new)) {
            if (component instanceof TextField) {
                TextField textField = (TextField) component;
                sb.append(textField.getLabel()).append(": ").append(textField.getValue()).append("\n");
            }
        }
        outputArea.setValue(sb.toString());
        outputArea.setVisible(true); // Mostra l'area di testo con i parametri salvati
    }

    private void runSimulation() {
        // Implementa la logica per eseguire la simulazione
        Span content = new Span("Running simulation with the current parameters...");
        com.vaadin.flow.component.dialog.Dialog dialog = new com.vaadin.flow.component.dialog.Dialog(content);
        dialog.open();
    }

    private void updateModelDescription(String model) {
        if (model != null) {
            modelDescriptionArea.setValue("Description for model: " + model);
        } else {
            modelDescriptionArea.setValue("");
        }
    }
}
