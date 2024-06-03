package ndlib.views.simulate;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;
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
    private Div errorMessageDiv;

    public SimulateView() {
        // Intestazione
        H2 header = new H2("Simulation Configuration");
        header.getStyle().set("color", "#634227");
        header.getStyle().set("font-weight", "bold");

        // Menù a tendina per la selezione del tipo di modello di diffusione
        modelTypeComboBox = new ComboBox<>("Diffusion Methods type");
        modelTypeComboBox.setItems("Static Epidemic Models", "Dynamic Epidemic Models", "Opinion Dynamic Models");
        modelTypeComboBox.addValueChangeListener(event -> {
            resetComponents();
            updateSpecificModelComboBox(event.getValue());
        });
        modelTypeComboBox.getStyle().set("min-width", "280px");
        modelTypeComboBox.addClassName("custom-combobox");

        // Menù a tendina per la selezione del tipo specifico di modello
        specificModelComboBox = new ComboBox<>("Types of model");
        specificModelComboBox.setVisible(false);
        specificModelComboBox.addValueChangeListener(event -> {
            resetComponents();
            updateForm(event.getValue());
            updateModelDescription(event.getValue());
        });
        specificModelComboBox.getStyle().set("min-width", "280px");
        specificModelComboBox.addClassName("custom-combobox");


        // Layout verticale per i menù a tendina
        VerticalLayout comboBoxLayout = new VerticalLayout(modelTypeComboBox, specificModelComboBox);
        comboBoxLayout.setAlignItems(Alignment.START);
        comboBoxLayout.setSpacing(false);
        comboBoxLayout.setPadding(false);

        // Layout del form
        formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("800px", 1)
        );

        // TextArea per la descrizione del modello selezionato
        modelDescriptionArea = new TextArea("Model Description");
        modelDescriptionArea.setReadOnly(true);
        modelDescriptionArea.setWidthFull();
        modelDescriptionArea.setVisible(false);

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
        outputDiv.setVisible(false);

        // Bottone per eseguire la simulazione
        runSimulationButton = new Button("Run Simulation", e -> runSimulation());
        runSimulationButton.setWidthFull();
        runSimulationButton.setVisible(false);
        runSimulationButton.getStyle().set("cursor", "pointer");

        // Messaggio di errore
        errorMessageDiv = new Div();
        errorMessageDiv.setVisible(false);
        errorMessageDiv.getStyle().set("color", "red");
        errorMessageDiv.getStyle().set("font-weight", "bold");

        // Layout principale
        VerticalLayout mainLayout = new VerticalLayout(header, contentLayout);
        mainLayout.setAlignItems(Alignment.START);
        mainLayout.setWidthFull();

        // Aggiunta dei componenti al layout principale
        add(mainLayout);

        // Aggiunta del Div e del pulsante di esecuzione in fondo alla pagina
        VerticalLayout bottomLayout = new VerticalLayout(outputDiv, runSimulationButton, errorMessageDiv);
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
        errorMessageDiv.setVisible(false);
        errorMessageDiv.setText("");
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
            case "Opinion Dynamic Models":
                specificModelComboBox.setItems("Voter", "Snajzd", "Q-Voter", "Majority Rule", "Cognitive Opinion Dynamics");
                break;
            default:
                specificModelComboBox.setVisible(false);
                break;
        }
    }

    private void hideErrorMessageAfterDelay() {
        // Simula l'attesa di 3 secondi
        getUI().ifPresent(ui -> ui.access(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            errorMessageDiv.setVisible(false);
            ui.push();
        }));
    }

    private void updateForm(String model) {
        formLayout.removeAll();
        modelDescriptionArea.setVisible(true);
        if (model == null) {
            return;
        }

        switch (model) {
            case "SI":
                addFieldsToForm("Alfa", "Beta");
                break;
            case "SIR":
                addFieldsToForm("Alfa", "Beta", "Gamma");
                break;
            case "SIS":
            case "SEIS":
            case "SWIR":
            case "Threshold":
            case "Kertesz Threshold":
            case "Independent Cascades":
            case "Node Profile":
            case "Node Profile-Threshold":
                addFieldsToForm("Alfa", "Beta", "Gamma");
                break;
            case "DynSI":
            case "DynSIS":
            case "DynSIR":
                addFieldsToForm("Alfa", "Beta", "Gamma","Delta");
                break;
            case "Voter":
            case "Snajzd":
            case "Q-Voter":
            case "Majority Rule":
            case "Cognitive Opinion Dynamics":
                addFieldsToForm("Alfa", "Beta", "Gamma","Delta","Omega");
                break;
        }

        // Aggiungere un bottone per l'invio
        Button submitButton = new Button("Save Configuration", e -> {
            if (validateInputs()) {
                simulateLoadingAndScroll();
                hideErrorMessageAfterDelay();
            } else {
                showError("Please enter values between 0 and 1 for all parameters.");
            }
        });
        formLayout.add(submitButton);
        submitButton.getStyle().set("cursor", "pointer");
    }

    private void addFieldsToForm(String... fieldNames) {
        for (String fieldName : fieldNames) {
            TextField textField = new TextField();
            textField.setId(fieldName.replace(" ", "").toLowerCase());

            Span labelSpan = new Span(fieldName);
            Span floatRangeLabel = new Span("- float in [0, 1]");
            floatRangeLabel.getStyle().set("font-size", "smaller");
            floatRangeLabel.getStyle().set("color", "black");
            floatRangeLabel.getStyle().set("margin-left", "5px");

            HorizontalLayout labelLayout = new HorizontalLayout(labelSpan, floatRangeLabel);
            labelLayout.setSpacing(false);
            labelLayout.setMargin(false);
            labelLayout.setAlignItems(Alignment.BASELINE);

            VerticalLayout fieldLayout = new VerticalLayout(labelLayout, textField);
            fieldLayout.setSpacing(false);
            fieldLayout.setMargin(false);

            formLayout.add(fieldLayout);
        }
    }


    private boolean validateInputs() {
        for (com.vaadin.flow.component.Component component : formLayout.getChildren().toArray(com.vaadin.flow.component.Component[]::new)) {
            if (component instanceof TextField) {
                TextField textField = (TextField) component;
                try {
                    float value = Float.parseFloat(textField.getValue());
                    if (value < 0 || value > 1) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showError(String message) {
        errorMessageDiv.setText(message);
        errorMessageDiv.setVisible(true);
    }

    private void printSelectedModelValues() {
        StringBuilder sb = new StringBuilder();
        sb.append("<b><span style='color:black;'>Diffusion Methods Type:</span></b> ").append(modelTypeComboBox.getValue()).append("<br>");
        sb.append("<b><span style='color:black;'>Types of Model:</span></b> ").append(specificModelComboBox.getValue()).append("<br>");

        for (com.vaadin.flow.component.Component component : formLayout.getChildren().toArray(com.vaadin.flow.component.Component[]::new)) {
            if (component instanceof TextField) {
                TextField textField = (TextField) component;
                sb.append("<b><span style='color:black;'>").append(textField.getLabel()).append(":</span></b> ").append(textField.getValue()).append("<br>");
            }
        }
        outputDiv.setText(sb.toString());
        outputDiv.setVisible(true);
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
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            remove(loadingMessage);
            printSelectedModelValues();
            runSimulationButton.setVisible(true);
            outputDiv.getElement().callJsFunction("scrollIntoView");
            ui.push();
        }));
    }

    private void runSimulation() {
        // Salva le informazioni di simulazione nella sessione
        VaadinSession.getCurrent().setAttribute("diffusionMethodsType", modelTypeComboBox.getValue());
        VaadinSession.getCurrent().setAttribute("typesOfModel", specificModelComboBox.getValue());

        // Salva le variabili della simulazione
        StringBuilder simulationParams = new StringBuilder();
        for (com.vaadin.flow.component.Component component : formLayout.getChildren().toArray(com.vaadin.flow.component.Component[]::new)) {
            if (component instanceof TextField) {
                TextField textField = (TextField) component;
                simulationParams.append(textField.getLabel()).append(": ").append(textField.getValue()).append("<br>");
            }
        }
        VaadinSession.getCurrent().setAttribute("simulationParameters", simulationParams.toString());

        // Simula un'attesa di 3 secondi
        getUI().ifPresent(ui -> ui.access(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            // Naviga alla pagina postSimulationView
            ui.navigate("postSim");
        }));
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
            case "DynSI":
                return "DynSI: This model adapts the classical formulation of the SI model (where the transition is S → I) to the snapshot-based topology evolution where the network structure is updated during each iteration. The model applied at day t_i will then use as starting infected set the result of the iteration performed on the interaction graph of the previous day, and as social structure the current one. Such choice implies that not only the interactions of consecutive snapshots could vary but that the node sets can also differ.";
            case "DynSIS":
                return "DynSIS: As the DynSI dynamic model, the DynSIS adapts the classical formulation of the SIS model (where the transition is S → I → S) to the snapshot-based topology evolution where the network structure is updated during each iteration. The DynSIS implementation assumes that the process occurs on a directed/undirected dynamic network.";
            case "DynSIR":
                return "DynSIR: As the DynSIS dynamic model, the DynSIR adapts the classical formulation of the SIR model (where the transition is S → I → R) to the snapshot-based topology evolution where the network structure is updated during each iteration. The DynSIR implementation assumes that the process occurs on a directed/undirected dynamic network.";
            case "Voter":
                return "Voter: The Voter model is a simple model of opinion dynamics. In each time step, a randomly selected node adopts the state of a randomly selected neighbor. The process continues until a consensus is reached.";
            case "Snajzd":
                return "Snajzd: The Snajzd model is another opinion dynamics model where pairs of neighboring nodes are randomly selected, and if they agree on their opinion, they convince all their neighbors to adopt the same opinion.";
            case "Q-Voter":
                return "Q-Voter: In the Q-Voter model, a node adopts the opinion of a randomly chosen group of q neighbors if all neighbors in the group share the same opinion. If they do not, the node can either adopt the opposite opinion or maintain its current one.";
            case "Majority Rule":
                return "Majority Rule: In the Majority Rule model, a group of nodes is selected, and all nodes in the group adopt the majority opinion within the group. This process is repeated until a consensus is reached.";
            case "Cognitive Opinion Dynamics":
                return "Cognitive Opinion Dynamics: This model includes cognitive factors in opinion dynamics, considering how individuals process and store information, as well as how they are influenced by others.";
            default:
                return null;
        }
    }
}
