package ndlib.views.postSim;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ndlib.views.MainLayout;
import com.vaadin.flow.server.VaadinSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.Arrays;

@PageTitle("postSim")
@Route(value = "postSim", layout = MainLayout.class)
public class postSimulationView extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(postSimulationView.class);

    public postSimulationView() {
        Div simulationResults = new Div();

        String diffusionMethodsType = (String) VaadinSession.getCurrent().getAttribute("diffusionMethodsType");
        String typesOfModel = (String) VaadinSession.getCurrent().getAttribute("typesOfModel");
        String simulationParameters = (String) VaadinSession.getCurrent().getAttribute("simulationParameters");

        if (diffusionMethodsType == null || typesOfModel == null || simulationParameters == null) {
            simulationResults.setText("No simulation results available.");
            add(simulationResults);
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<b>Diffusion Methods Type:</b> ").append(diffusionMethodsType).append("<br>");
            sb.append("<b>Types of Model:</b> ").append(typesOfModel).append("<br>");
            sb.append(simulationParameters);

            simulationResults.getElement().setProperty("innerHTML", sb.toString());

            // Determine the script path
            String scriptPath = getPythonScriptPath(diffusionMethodsType, typesOfModel);

            if (scriptPath != null) {
                // Run the Python script
                String scriptOutput = runPythonScript(scriptPath, simulationParameters);
                if (scriptOutput != null) {
                    Div scriptOutputDiv = new Div();
                    scriptOutputDiv.getElement().setProperty("innerHTML", scriptOutput);
                    add(scriptOutputDiv);

                    // Read and display the generated plot.html content
                    String plotHtmlContent = readPlotHtmlContent("src/main/resources/pythonScripts/simulation/plot/plot.html");
                    if (plotHtmlContent != null && !plotHtmlContent.isEmpty()) {
                        // Wrap the content in a single top-level <div> element
                        String wrappedHtmlContent = "<div>" + plotHtmlContent + "</div>";
                        // Create a Html component with the wrapped plot.html content
                        Html plotHtml = new Html(wrappedHtmlContent);
                        add(plotHtml);

                        // Add download buttons
                        addDownloadButtons();
                    } else {
                        simulationResults.setText("Plot HTML content not found.");
                    }
                } else {
                    simulationResults.setText("Failed to execute the simulation script.");
                }
            } else {
                simulationResults.setText("No valid script path found for the selected model.");
            }
        } catch (Exception e) {
            logger.error("Error in postSimulationView constructor", e);
            simulationResults.setText("An error occurred: " + e.getMessage());
        }

        add(simulationResults);

        // Create and add the transparent button with the home icon
        Button homeButton = new Button(new Icon("vaadin", "home"));
        homeButton.getElement().setAttribute("aria-label", "Home");
        homeButton.getStyle().set("color", "rgb(99, 66, 39)");
        homeButton.getStyle().set("border", "1px solid rgb(99, 66, 39)");
        homeButton.getStyle().set("background", "transparent");
        homeButton.getStyle().set("position", "fixed");
        homeButton.getStyle().set("bottom", "30px");
        homeButton.getStyle().set("right", "30px");
        homeButton.getStyle().set("z-index", "1000");
        homeButton.getStyle().set("width", "50px");
        homeButton.getStyle().set("height", "50px");
        homeButton.getStyle().set("cursor", "pointer");
        homeButton.getStyle().set("padding", "10px");

        homeButton.addClickListener(e -> showConfirmationDialog());
        add(homeButton);
    }

    private String readPlotHtmlContent(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            logger.error("Error reading plot HTML content", e);
            return null;
        }
    }

    private void showConfirmationDialog() {
        Dialog dialog = new Dialog();
        dialog.add(new Text("Do you want to return to the home page?"));

        Button yesButton = new Button("Yes", event -> {
            dialog.close();
            getUI().ifPresent(ui -> ui.navigate("simulate"));
        });
        yesButton.getStyle().set("cursor", "pointer");

        Button noButton = new Button("No", event -> dialog.close());
        noButton.getStyle().set("cursor", "pointer");

        HorizontalLayout buttons = new HorizontalLayout(yesButton, noButton);
        dialog.add(buttons);
        dialog.open();
    }

    private String getPythonScriptPath(String diffusionMethodsType, String typesOfModel) {
        String basePath = "src/main/resources/pythonScripts/";

        switch (diffusionMethodsType) {
            case "Static Epidemic Models":
                return basePath + "SEM/" + typesOfModel + ".py";
            case "Dynamic Epidemic Models":
                return basePath + "DEM/" + typesOfModel + ".py";
            case "Opinion Dynamic Models":
                return basePath + "ODM/" + typesOfModel + ".py";
            default:
                return null;
        }
    }

    private String runPythonScript(String scriptPath, String simulationParameters) {
        try {
            // Ensure parameters are correctly formatted and remove any HTML tags and text labels
            simulationParameters = simulationParameters.replaceAll("Alfa:|Beta:|Gamma:|<[^>]*>", "").trim();
            System.out.println("Formatted Parameters: " + simulationParameters);

            // Split the parameters
            String[] params = simulationParameters.split("\\s+");
            System.out.println("Parameters Array: " + Arrays.toString(params));

            // Construct the command to run the Python script
            String command = "python " + scriptPath + " " + String.join(" ", params);
            System.out.println("Command: " + command);

            // Run the command
            Process process = Runtime.getRuntime().exec(command);

            // Read the output from the command
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            String line;
            while ((line = stdInput.readLine()) != null) {
                output.append(line).append("<br>");
            }

            while ((line = stdError.readLine()) != null) {
                errorOutput.append(line).append("<br>");
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();
            System.out.println("Exit Code: " + exitCode);
            if (exitCode == 0) {
                System.out.println("Script Output: " + output.toString());
                return output.toString();
            } else {
                System.err.println("Script Error Output: " + errorOutput.toString());
                return "Error running script. Exit code: " + exitCode + "<br>" + errorOutput.toString();
            }
        } catch (Exception e) {
            logger.error("Exception occurred while running the script", e);
            return "Exception occurred while running the script: " + e.getMessage();
        }
    }

    private void addDownloadButtons() {
        // Ensure file paths
        Path csvFilePath = Paths.get("src/main/resources/pythonScripts/simulation/data/simulation_data.csv");
        Path plotFilePath = Paths.get("src/main/resources/pythonScripts/simulation/plot/plot.html");

        if (Files.exists(csvFilePath) && Files.exists(plotFilePath)) {
            // Create download buttons
            Anchor downloadCsvButton = new Anchor("/src/main/resources/pythonScripts/simulation/data/simulation_data.csv", "Download CSV");
            downloadCsvButton.getElement().setAttribute("download", true);
            downloadCsvButton.getStyle().set("margin-top", "20px");
            downloadCsvButton.getStyle().set("display", "block");

            Anchor downloadImageButton = new Anchor("/src/main/resources/pythonScripts/simulation/plot/plot.html", "Download Plot Image");
            downloadImageButton.getElement().setAttribute("download", true);
            downloadImageButton.getStyle().set("margin-top", "20px");
            downloadImageButton.getStyle().set("display", "block");

            add(downloadCsvButton, downloadImageButton);
        } else {
            if (!Files.exists(csvFilePath)) {
                logger.error("CSV file not found at " + csvFilePath.toString());
            }
            if (!Files.exists(plotFilePath)) {
                logger.error("Plot file not found at " + plotFilePath.toString());
            }
        }
    }
}
