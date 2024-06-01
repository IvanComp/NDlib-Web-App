package ndlib.views.simulate;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
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
    private Div outputDiv;
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
        modelTypeComboBox.addValueChangeListener(event -> {
            resetComponents();
            updateSpecificModelComboBox(event.getValue());
        });
        modelTypeComboBox.getStyle().set("min-width", "280px");  // Imposta una larghezza minima per il ComboBox
        modelTypeComboBox.addClassName("custom-combobox");  // Aggiunge la classe CSS personalizzata

        // Menù a tendina per la selezione del tipo specifico di modello
        specificModelComboBox = new ComboBox<>("Types of model");
        specificModelComboBox.setVisible(false);
        specificModelComboBox.addValueChangeListener(event -> {
            resetComponents();
            updateForm(event.getValue());
            updateModelDescription(event.getValue());
        });
        specificModelComboBox.getStyle().set("min-width", "280px");  // Imposta una larghezza minima per il ComboBox
        specificModelComboBox.addClassName("custom-combobox");  // Aggiunge la classe CSS personalizzata

        // Layout verticale per i menù a tendina
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

        // Layout orizzontale per menù a tendina, form delle variabili e descrizione del modello
        HorizontalLayout contentLayout = new HorizontalLayout(comboBoxLayout, formLayout, modelDescriptionArea);
        contentLayout.setAlignItems(Alignment.START);
        contentLayout.setWidthFull();
        contentLayout.setSpacing(true);
        contentLayout.setPadding(false);
        contentLayout.setFlexGrow(1, comboBoxLayout);
        contentLayout.setFlexGrow(1, formLayout);
        contentLayout.setFlexGrow(1, modelDescriptionArea);

        // Div per mostrare i valori del modello scelto con HTML
        outputDiv = new Div();
        outputDiv.setWidthFull();
        outputDiv.getStyle().set("white-space", "pre-wrap");
        outputDiv.setVisible(false); // Nascondi inizialmente

        // Bottone per eseguire la simulazione
        runSimulationButton = new Button("Run Simulation", e -> runSimulation());
        runSimulationButton.setWidthFull();
        runSimulationButton.setVisible(false); // Nascondi inizialmente

        // Layout principale
        VerticalLayout mainLayout = new VerticalLayout(header, contentLayout);
        mainLayout.setAlignItems(Alignment.START);
        mainLayout.setWidthFull();

        // Aggiunta dei componenti al layout principale
        add(mainLayout);

        // Aggiunta del Div e del pulsante di esecuzione in fondo alla pagina
        VerticalLayout bottomLayout = new VerticalLayout(outputDiv, runSimulationButton);
        bottomLayout.setAlignItems(Alignment.CENTER);
        bottomLayout.setWidthFull();
        add(bottomLayout);
    }

    private void resetComponents() {
        formLayout.removeAll();
        modelDescriptionArea.setVisible(false);
        outputDiv.setVisible(false);
        runSimulationButton.setVisible(false);
        outputDiv.setText("");
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
            simulateLoadingAndScroll();
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
        sb.append("<b><span style='color:black;'>Diffusion Methods Type:</span></b> ").append(modelTypeComboBox.getValue()).append("<br>");
        sb.append("<b><span style='color:black;'>Types of Model:</span></b> ").append(specificModelComboBox.getValue()).append("<br>");

        for (com.vaadin.flow.component.Component component : formLayout.getChildren().toArray(com.vaadin.flow.component.Component[]::new)) {
            if (component instanceof TextField) {
                TextField textField = (TextField) component;
                sb.append(textField.getLabel()).append(": ").append(textField.getValue()).append("<br>");
            }
        }
        outputDiv.setText(sb.toString());
        outputDiv.setVisible(true); // Mostra il Div con i parametri salvati
        outputDiv.getElement().setProperty("innerHTML", sb.toString());
    }

    private void simulateLoadingAndScroll() {
        // Mostra un messaggio di caricamento
        Span loadingMessage = new Span("Loading...");
        loadingMessage.getStyle().set("font-size", "20px");
        add(loadingMessage);

        // Simula un'attesa di 3 secondi
        getUI().ifPresent(ui -> ui.access(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            remove(loadingMessage);
            printSelectedModelValues();
            runSimulationButton.setVisible(true); // Mostra il bottone "Run Simulation"
            outputDiv.getElement().callJsFunction("scrollIntoView");
            ui.push();
        }));
    }

    private void runSimulation() {
        // Implementa la logica per eseguire la simulazione
        Span content = new Span("Running simulation with the current parameters...");
        com.vaadin.flow.component.dialog.Dialog dialog = new com.vaadin.flow.component.dialog.Dialog(content);
        dialog.open();
    }

    private void updateModelDescription(String model) {
        String description = getModelDescription(model);
        modelDescriptionArea.setValue(description != null ? description : "");
    }

    private String getModelDescription(String model) {
        switch (model) {
            case "SI":
                return "SI: This model was introduced in 1927 by Kermack. In the SI model, during the course of an epidemic, a node is allowed to change its status only from Susceptible (S) to Infected (I). SI assumes that if, during a generic iteration, a susceptible node comes into contact with an infected one, it becomes infected with probability β: once a node becomes infected, it stays infected (the only transition is S → I).";
            case "SIR":
                return "SIR: This model was still introduced in 1927 by Kermack. In the SIR model, during the course of an epidemic, a node is allowed to change its status from Susceptible (S) to Infected (I), then to Removed (R). SIR assumes that if, during a generic iteration, a susceptible node comes into contact with an infected one, it becomes infected with probability β, then it can switch to removed with probability γ (the only transitions allowed are S → I → R).";
            case "SIS":
                return "SIS: As SIR, the SIS model is a variation of the SI model introduced in 1927. The model assumes that if, during a generic iteration, a susceptible node comes into contact with an infected one, it becomes infected with probability β, then it can switch again to susceptible with probability λ (the only transitions allowed are S → I → S).";
            case "SEIS":
                return "SEIS: As the previous models, the SEIS is a variation of the SI model. For many infections, there is a significant incubation period during which the individual has been infected but is not yet infectious themselves. During this period the individual is in status exposed (E). SEIS assumes that if, during a generic iteration, a susceptible node comes into contact with an infected one, it switches to exposed with probability β, then it becomes infected with probability ε and then it can switch again to susceptible with probability λ (the only transitions allowed are S → E → I → S).";
            case "SEIR":
                return "SEIR: As the SEIS model, the SEIR takes into consideration the incubation period, considering the status exposed (E). SEIR assumes that if, during a generic iteration, a susceptible node comes into contact with an infected one, it switches to exposed with probability β, then it becomes infected with probability ε and then it can switch to removed with probability γ (the only transitions allowed are S → E → I → R).";
            case "SWIR":
                return "SWIR: This model has four states: Susceptible (S), Infected (I), Recovered (R), and Weakened (W). Besides the usual transition S → I → R, we have also the transition S → W → I → R. At timestamp n, a node in state I is selected and the state of all its neighbors is checked one by one. If the state of a neighbor is S, then this state changes either to I with probability κ or to W with likelihood µ. If the state of a neighbor is W, with probability ν its state changes to I. Then we repeat the process for all nodes in state I and the state for all these nodes becomes R.";
            case "Threshold":
                return "Threshold: This model was introduced in 1978 by Granovetter. In the Threshold model during an epidemic, a node has two distinct and mutually exclusive behavioral alternatives, e.g., it can adopt or not a given behavior, participate or not participate in a riot. A node's individual decision depends on the percentage of its neighbors that have made the same choice, thus imposing a threshold. The model works as follows: each node starts with its own threshold τ and status (infected or susceptible). During iteration t, every node is observed: if the percentage of its neighbors that were infected at time t - 1 is greater than its threshold, it becomes infected as well.";
            case "Kertesz Threshold":
                return "Kertesz Threshold: This model was introduced in 2015 by Ruan et al. and it is an extension of the Watts threshold model. The authors extend the classical model by introducing a density r of blocked nodes - nodes which are immune to social influence - and a probability of spontaneous adoption p to capture external influence. Thus, the model distinguishes three kinds of nodes: Blocked (B), Susceptible (S), and Adopting (A). A node can adopt either under its neighbors' influence or due to endogenous effects.";
            case "Independent Cascades":
                return "Independent Cascades: This model was introduced by Kempe et al. in 2003. The Independent Cascades model starts with an initial set of active nodes A0: the diffusive process unfolds in discrete steps according to the following randomized rule:\n" +
                        "– When node v becomes active in step t, it is given a single chance to activate each currently inactive neighbor w; it succeeds with a probability p_v,w.\n" +
                        "– If w has multiple newly activated neighbors, their attempts are sequenced in an arbitrary order.\n" +
                        "– If v succeeds, then w will become active in step t+1; but whether or not v succeeds, it cannot make any further attempts to activate w in subsequent rounds.\n" +
                        "The process runs until no more activations are possible.";
            case "Node Profile":
                return "Node Profile: This model is a variation of the Threshold one, introduced in 2003. It assumes that the diffusion process is only apparent; each node decides to adopt or not a given behavior - once known its existence - only on the basis of its own interests. In this scenario, peer pressure is completely ruled out from the overall model: it is not important how many of its neighbors have adopted a specific behavior; if the node does not like it, it will not change its interests. Each node has its own profile describing how likely it is to accept a behavior similar to the one that is currently spreading. The diffusion process starts from a set of nodes that have already adopted a given behavior H: for each of the susceptible nodes in the neighborhood of a node u that has already adopted H, an unbalanced coin is flipped, the unbalance given by the personal profile of the susceptible node; if a positive result is obtained, the susceptible node will adopt the behavior.";
            case "Node Profile-Threshold":
                return "Node Profile-Threshold: This model, still an extension of the Threshold one, assumes the existence of node profiles that act as preferential schemas for individual tastes but relax the constraints imposed by the Profile model by letting nodes be influenced via peer pressure mechanisms. Peer pressure is modeled with a threshold. The diffusion process starts from a set of nodes that have already adopted a given behavior H: for each of the susceptible nodes, an unbalanced coin is flipped if the percentage of its neighbors that are already infected exceeds its threshold. As in the Profile Model, the coin unbalance is given by the personal profile of the susceptible node; if a positive result is obtained, the susceptible node will adopt the behavior.";
            default:
                return null;
        }
    }
}
