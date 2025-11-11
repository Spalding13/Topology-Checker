package com.company.gui;

import com.company.*;
import com.company.devicefactory.Device;
import com.company.devicefactory.DeviceFactory;
import com.company.esd.analyzer.ESDAnalyzer;
import com.company.esd.rule.*;
import com.company.graph.Graph;
import com.company.graph.GraphFactory;
import com.company.netFactory.Net;
import com.company.netFactory.NetFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MainGUI extends Application {

    private TextArea outputArea;
    private TableView<RuleResultEntry> ruleTable;


    @Override
    public void start(Stage stage) {
        stage.setTitle("ESD Rule Analyzer");

        Label fileLabel = new Label("Select .cdl Netlist File:");
        Button browseButton = new Button("Browse...");
        Button runButton = new Button("Run Analysis");

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");

        // Redirect console output to GUI
        ConsoleRedirect.redirectSystemStreams(outputArea);
        // Diagnostic test: print immediately after redirect to verify it appears in the TextArea
        System.out.println("[DEBUG] Console redirect active: stdout -> TextArea");
        System.err.println("[DEBUG] Console redirect active: stderr -> TextArea");

        // --- File selection row ---
        HBox fileBox = new HBox(10, fileLabel, browseButton);
        fileBox.setPadding(new Insets(10));

        // --- Rule results table ---
        ruleTable = new TableView<>();

        TableColumn<RuleResultEntry, String> nameCol = new TableColumn<>("Rule Name");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().ruleNameProperty());
        nameCol.setPrefWidth(150);

        TableColumn<RuleResultEntry, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        descCol.setPrefWidth(300);

        TableColumn<RuleResultEntry, String> resultCol = new TableColumn<>("Result");
        resultCol.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
        resultCol.setPrefWidth(100);

        ruleTable.getColumns().addAll(nameCol, descCol, resultCol);
        ruleTable.setPrefHeight(200);

        // --- Root layout with table and console ---
        VBox root = new VBox(10,
                fileBox,
                runButton,
                new Label("Rule Results:"), ruleTable,
                new Label("Console Output:"), outputArea
        );
        root.setPadding(new Insets(15));

        Scene scene = new Scene(root, 700, 600);
        stage.setScene(scene);
        stage.show();

        // --- File chooser setup ---
        final File[] selectedFile = new File[1];

        browseButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Netlist (.cdl)");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CDL files", "*.cdl"));
//            File file = chooser.showOpenDialog(stage);
            File file = new File("E:\\ESD Checks\\input\\netlist.cdl");
            if (file != null) {
                selectedFile[0] = file;
                fileLabel.setText("Selected: " + file.getName());
            }
        });

        // --- Run analysis button action ---
        runButton.setOnAction(e -> {
            if (selectedFile[0] == null) {
                showAlert("No File Selected", "Please select a .cdl file before running analysis.");
                return;
            }

            // Disable buttons while running
            runButton.setDisable(true);
            browseButton.setDisable(true);
            outputArea.appendText("Starting analysis in background...\n");

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    runAnalysis(selectedFile[0]);
                    return null;
                }
            };

            task.setOnSucceeded(ev -> {
                runButton.setDisable(false);
                browseButton.setDisable(false);
                outputArea.appendText("Background analysis finished.\n");
            });

            task.setOnFailed(ev -> {
                runButton.setDisable(false);
                browseButton.setDisable(false);
                Throwable ex = task.getException();
                Platform.runLater(() -> showAlert("Analysis Error", ex == null ? "Unknown error" : ex.getMessage()));
            });

            new Thread(task).start();
        });
    }


    private void runAnalysis(File file) {
        try {
            // Use Platform.runLater when interacting with UI elements from background threads
            appendOutput("Running ESD analysis...\n");

            // Step 1: Define ports (can be configurable later)
            List<String> ports = Arrays.asList("PAD", "VDD<1>", "GND<1>", "GND<2>");

            // Step 2: Read the .cdl input file
            String input = NetlistReader.openFile(file.getAbsolutePath());
            if (input == null) {
                appendOutput("Error: Could not read file.\n");
                return;
            }

            // Step 3: Parse netlist
            StateMachine stateMachine = new StateMachine();
            Map<String, List<String>> netlistInfo = stateMachine.parseNetlist(input);

            // Step 4: Create nets and devices
            NetFactory netFactory = new NetFactory();
            List<Net> nets = netFactory.createNets(netlistInfo.get("nets"));
            List<Device> devices = DeviceFactory.createDevicesFromLines(netlistInfo.get("devices"));

            // Step 5: Build graph
            Graph graph = GraphFactory.buildGraph(devices, netFactory.getNetMap());

            // Step 6: Reduce graph
            graph = Reducer.reduce(graph);

            // Step 7: Prepare rules
            List<StructuralRule> structuralRules = new ArrayList<>();
            List<ParametricRule> parametricRules = new ArrayList<>();

            EsdaRule structuralRule = new EsdaRule();
            EsdaParametricRule paramRule = new EsdaParametricRule();

            parametricRules.add(paramRule);
            structuralRules.add(structuralRule);

            // Step 8: Run analyzer
            ESDAnalyzer analyzer = new ESDAnalyzer(structuralRules, parametricRules);
            analyzer.analyze(graph, ports);

            appendOutput("\nAnalysis complete!\n");
            appendOutput("Rules executed: " + (parametricRules.size() + structuralRules.size()) + "\n");
            appendOutput("Check your console or log for detailed results.\n");

        } catch (IOException ex) {
            Platform.runLater(() -> showAlert("Error", "Failed to read or process file:\n" + ex.getMessage()));
        } catch (Exception ex) {
            Platform.runLater(() -> showAlert("Runtime Error", "An error occurred during analysis:\n" + ex.getMessage()));
        }
    }

    // Helper to safely append text to the outputArea from any thread
    private void appendOutput(String text) {
        Platform.runLater(() -> {
            if (outputArea != null) {
                outputArea.appendText(text);
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
