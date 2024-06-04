package ndlib.views.postSim;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.component.html.Image;
import ndlib.views.MainLayout;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@PageTitle("postSim")
@Route(value = "postSim", layout = MainLayout.class)
public class postSimulationView extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(postSimulationView.class);
    private HorizontalLayout mainLayout;
    private Div simulationResults;

    public postSimulationView() {
        // Add title
        add(new Html("<h1>Result of the Simulation</h1>"));

        // Container for results and chart
        mainLayout = new HorizontalLayout();
        add(mainLayout);

        simulationResults = new Div();

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
            sb.append("<b>Diffusion Methods Type:</b> ")
                    .append(diffusionMethodsType).append("<br>")
                    .append("<b>Types of Model:</b> ")
                    .append(typesOfModel).append("<br>")
                    .append(simulationParameters);

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
                        displayChartFromCSV("src/main/resources/pythonScripts/simulation/data/simulation_data.csv");
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

        mainLayout.add(simulationResults);

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

        Button yesButton = createStyledButton("Yes", null);
        yesButton.addClickListener(event -> {
            dialog.close();
            getUI().ifPresent(ui -> ui.navigate("simulate"));
        });

        Button noButton = createStyledButton("No", null);
        noButton.addClickListener(event -> dialog.close());

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
            simulationParameters = simulationParameters.replaceAll("Transmission Rate:|Recovery Rate:|Percentage Infected:|<[^>]*>", "").trim();
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
        try {
            // Load file content
            byte[] csvContent = Files.readAllBytes(Paths.get("src/main/resources/pythonScripts/simulation/data/simulation_data.csv"));
            byte[] plotContent = Files.readAllBytes(Paths.get("src/main/resources/pythonScripts/simulation/plot/plot.html"));

            // Create StreamResource for CSV
            StreamResource csvResource = new StreamResource("simulation_data.csv", () -> new ByteArrayInputStream(csvContent));
            Button csvButton = new Button("Download CSV", new Icon(VaadinIcon.FILE_TABLE));
            csvButton.getElement().setAttribute("aria-label", "Download CSV");
            styleButton(csvButton);

            Anchor downloadCsvAnchor = new Anchor(csvResource, "");
            downloadCsvAnchor.getElement().setAttribute("download", true);
            downloadCsvAnchor.add(csvButton);

            // Create StreamResource for Plot
            StreamResource plotResource = new StreamResource("plot.html", () -> new ByteArrayInputStream(plotContent));
            Button plotButton = new Button("Download Plot Image", new Icon(VaadinIcon.CHART_3D));
            plotButton.getElement().setAttribute("aria-label", "Download Interactive Plot");
            styleButton(plotButton);

            Anchor downloadPlotAnchor = new Anchor(plotResource, "");
            downloadPlotAnchor.getElement().setAttribute("download", true);
            downloadPlotAnchor.add(plotButton);

            // Add buttons to layout
            VerticalLayout downloadButtonsLayout = new VerticalLayout(downloadCsvAnchor, downloadPlotAnchor);
            mainLayout.add(downloadButtonsLayout);
        } catch (IOException e) {
            logger.error("Error reading files for download", e);
        }
    }


    private void displayChartFromCSV(String csvFilePath) {
        try {
            Reader in = new FileReader(csvFilePath);
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);

            Map<String, List<Integer>> dataSeries = new LinkedHashMap<>();
            List<Integer> iterations = new ArrayList<>();

            for (CSVRecord record : records) {
                if (iterations.isEmpty()) {
                    for (String header : record.toMap().keySet()) {
                        if (!header.equals("Iteration")) {
                            dataSeries.put(header, new ArrayList<>());
                        }
                    }
                }
                iterations.add(Integer.parseInt(record.get("Iteration")));
                for (Map.Entry<String, List<Integer>> entry : dataSeries.entrySet()) {
                    entry.getValue().add(Integer.parseInt(record.get(entry.getKey())));
                }
            }

            XYChart chart = new XYChartBuilder().width(800).height(600).title("Model Simulation").xAxisTitle("Iteration").yAxisTitle("Number of Nodes").build();
            for (Map.Entry<String, List<Integer>> entry : dataSeries.entrySet()) {
                chart.addSeries(entry.getKey(), iterations, entry.getValue()).setMarker(SeriesMarkers.NONE);
            }

            // Save chart as an image
            BitmapEncoder.saveBitmap(chart, "src/main/resources/pythonScripts/simulation/plot/simulation_chart", BitmapEncoder.BitmapFormat.PNG);

            // Display chart in Vaadin UI
            StreamResource chartResource = new StreamResource("simulation_chart.png", () -> {
                try {
                    return new ByteArrayInputStream(Files.readAllBytes(Paths.get("src/main/resources/pythonScripts/simulation/plot/simulation_chart.png")));
                } catch (IOException e) {
                    logger.error("Error reading chart image file", e);
                    return null;
                }
            });

            Image chartImage = new Image(chartResource, "Simulation Chart");
            chartImage.setWidth("800px");
            chartImage.setHeight("600px");
            mainLayout.add(chartImage, simulationResults);

        } catch (IOException e) {
            logger.error("Error reading CSV file", e);
        }
    }

    private Button createStyledButton(String text, String icon) {
        Button button;
        if (icon != null) {
            button = new Button(new Icon("vaadin", icon));
        } else {
            button = new Button(text);
        }
        button.getElement().setAttribute("aria-label", text);
        styleButton(button);
        return button;
    }

    private void styleButton(Button button) {
        button.getStyle().set("color", "rgb(99, 66, 39)");
        button.getStyle().set("border", "1px solid rgb(99, 66, 39)");
        button.getStyle().set("background", "transparent");
        button.getStyle().set("cursor", "pointer");
        button.getStyle().set("padding", "10px");
    }

    private void styleButton(Anchor anchor) {
        anchor.getStyle().set("color", "rgb(99, 66, 39)");
        anchor.getStyle().set("border", "1px solid rgb(99, 66, 39)");
        anchor.getStyle().set("background", "transparent");
        anchor.getStyle().set("cursor", "pointer");
        anchor.getStyle().set("padding", "10px");
        anchor.getStyle().set("margin-top", "10px");
        anchor.getStyle().set("display", "block");
    }
}

