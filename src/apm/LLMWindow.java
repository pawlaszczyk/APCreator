package apm;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * APM Chat agent frame
 *
 * Features:
 * - Chat-Interface with input prompt
 * - Output area for conversations
 * - Toolbar with "back" and "apply" buttons
 * - Chat-History
 *     The llama.cpp library is a C/C++ implementation of Meta's LLaMA model, optimised for CPU usage.
 *     It allows running the LLaMA model on consumer hardware without requiring high-end GPUs.
 *     LocalAI is a framework that enables running AI models locally without relying on cloud services.
 *     It provides APIs compatible with OpenAI's interfaces, allowing developers to use their own models with the same
 *     code they would use for OpenAI services.
 *
 *  Original Author:  Dirk Pawlaszczyk
 */
public class LLMWindow extends Application {

    // UI components
    private TextArea outputArea;
    private TextArea promptField;
    private Button resetButton;
    private Button applyButton;
    private Button backButton;
    private Button configButton;
    private Label statusLabel;
    private LLMMgr agent;
    ProgressBar progress;
    private Stage primaryStage;

    // Chat management
    private List<ChatMessage> chatHistory;
    private int historyIndex;
    private APMCreator parent;

    public LLMWindow(APMCreator parent){
        this.parent = parent;
    }

    /**
     * The main function of this class. It prepares and shows the window.
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(APMCreator.class.getResourceAsStream("/cognition_small.png"))));

        agent = LLMMgr.getInstance();

        chatHistory = new ArrayList<>();
        historyIndex = -1;

        primaryStage.setTitle("APM Chat Agent");

        // Main layout
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Toolbar top
        ToolBar toolbar = createToolbar();
        root.setTop(toolbar);

        // Output area in the centre
        VBox centerBox = createCenterArea();
        root.setCenter(centerBox);

        // Input area at the bottom
        VBox bottomBox = createBottomArea();
        root.setBottom(bottomBox);

        // create scene
        Scene scene = new Scene(root, 800, 600);
        applyStyling(scene);

        // Optional: apply CSS style
        //pplyStyling(scene);

        primaryStage.setScene(scene);
        primaryStage.show();

        promptField.setText("Type your prompt here...");

        // focus on the input prompt
        promptField.requestFocus();
    }

    /**
     * Creates a Toolbar with buttons.
     */
    private ToolBar createToolbar() {
        ToolBar toolbar = new ToolBar();

        // Back-Button
        resetButton = new Button(); //("← Reset");
        String sReset = Objects.requireNonNull(APMCreator.class.getResource("/delete_history_small.png")).toExternalForm();
        ImageView iv = new javafx.scene.image.ImageView(sReset);
        resetButton.setTooltip(new Tooltip("Clear chat history."));
        resetButton.setGraphic(iv);
        resetButton.setOnAction(e -> handleReset());
        resetButton.setDisable(true);

        // Separator
        Separator separator = new Separator();

        // Apply-Button
        applyButton = new Button(); //("✓ Apply");
        String sApply = Objects.requireNonNull(APMCreator.class.getResource("/apply_small.png")).toExternalForm();
        applyButton.setTooltip(new Tooltip("Apply last answer and close window"));
        iv = new javafx.scene.image.ImageView(sApply);
        applyButton.setGraphic(iv);
        applyButton.setOnAction(e -> handleApply());

        backButton = new Button(); //" \u2716 Back");
        backButton.setTooltip(new Tooltip("Close this Window"));
        String sBack = Objects.requireNonNull(APMCreator.class.getResource("/exit_small.png")).toExternalForm();
        iv = new javafx.scene.image.ImageView(sBack);
        backButton.setGraphic(iv);
        backButton.setOnAction(e -> primaryStage.close());

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);


        configButton = new Button(); //" \u2716 Back");
        configButton.setTooltip(new Tooltip("Settings..."));
        String sConfig = Objects.requireNonNull(APMCreator.class.getResource("/settings_small.png")).toExternalForm();
        iv = new javafx.scene.image.ImageView(sConfig);
        configButton.setGraphic(iv);
        configButton.setOnAction(e -> showConfigDialog());

        // Status Label
        statusLabel = new Label("Ready");
        statusLabel.setFont(Font.font("System", 11));

        progress = new ProgressBar();
        progress.setVisible(false);

        Label logoLabel = new Label();
        String s = Objects.requireNonNull(APMCreator.class.getResource("/cognition_small.png")).toExternalForm();
        iv = new javafx.scene.image.ImageView(s);
        logoLabel.setGraphic(iv);

        toolbar.getItems().addAll(
                resetButton,
                applyButton,
                configButton,
                separator,
                spacer2,
                backButton,
                spacer,
                logoLabel
        );

        return toolbar;
    }

    /**
     * Creates the central output area.
     */
    private VBox createCenterArea() {
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(10, 0, 10, 0));

        // Label
        Label outputLabel = new Label("Conversation:");
        outputLabel.setFont(Font.font("System", 14));

        // Output TextArea
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setFont(Font.font("Monospaced", 12));
        outputArea.setPromptText("Hello...");
        VBox.setVgrow(outputArea, Priority.ALWAYS);

        centerBox.getChildren().addAll(outputLabel, outputArea);
        return centerBox;
    }

    /**
     * Creates the lower input area.
     */
    private VBox createBottomArea() {
        VBox bottomBox = new VBox(10);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));

        // Label
        Label promptLabel = new Label("Your input:");
        promptLabel.setFont(Font.font("System", 14));

        // Input field with button
        HBox inputBox = new HBox(10);

        promptField = new TextArea();
        promptField.setWrapText(true);
        promptField.setFont(Font.font("System", 12));
        HBox.setHgrow(promptField, Priority.ALWAYS);

        Button sendenButton = new Button("Ask \u2753");
        sendenButton.setDefaultButton(true);
        sendenButton.setOnAction(e -> handleRun());
        sendenButton.setPrefWidth(100);

        Button templateButton = new Button("Example \uD83D\uDCD1");
        templateButton.setDefaultButton(true);
        templateButton.setOnAction(e -> copyExample2Input());
        templateButton.setPrefWidth(100);

        //Button configButton = new Button("Configuration");
        //configButton.setDefaultButton(true);
        //configButton.setOnAction(e -> showConfigDialog());
        //configButton.setPrefWidth(100);

        VBox btnfield = new VBox(10);
        btnfield.getChildren().addAll(sendenButton,templateButton);

        inputBox.getChildren().addAll(promptField, btnfield);

        progress = new ProgressBar();
        progress.setVisible(false);

        HBox statusline = new HBox(10);
        statusline.getChildren().addAll(statusLabel,progress);

        bottomBox.getChildren().addAll(promptLabel, inputBox, statusline);
        return bottomBox;
    }

    private void copyExample2Input(){
        promptField.setText(
                "Create an APM storyboard.\n" +
                "Title: How about the Weather Today?\n" +
                "Each action should be printed on a separate line.\n" +
                "Comments should start with %%.\n" +
                "Include two actors: Alice, who will be aliased as A, and Bob, who will be aliased as B.\n" +
                "Both actors will send messages to each other via WhatsApp using send message[WhatsApp].\n" +
                "They should discuss the weather today and exchange a total of 3 messages.\n" +
                "Two additional messages should be sent over Telegram. The topic should be Berlin.");

        // focus on the input prompt
        promptField.requestFocus();
    }


    /**
     * Handling method for the 'go' button.
     */
    private void handleRun() {
        String userInput = promptField.getText().trim();

        if (userInput.isEmpty()) {
            return;
        }

        // add next user message to history
        addMessage("User", userInput);

        // clear the prompt for the next question
        promptField.clear();
        progress.setVisible(true);

        // Status update
        statusLabel.setText("Processing...");
        applyButton.setDisable(true);

        // forward current user prompt to LLM
        askLLM(userInput);


    }

    private void showConfigDialog(){

        LLMConfigDialog dl = new LLMConfigDialog();
        Stage configstage = new Stage();
        configstage.initModality(Modality.APPLICATION_MODAL);
        dl.start(configstage);

    }

    /**
     * This method actually starts the inference process of the LLM.
     * @param userinput the prompt
     */
    private void askLLM(String userinput){

        new Thread(() -> {
            try {

                // forward to LLM
                String response = agent.run(userinput);

                // UI update as soon as the response is available
                Platform.runLater(() -> {
                    addMessage("LLM:", response);
                    statusLabel.setText("Ready");
                    applyButton.setDisable(false);
                    resetButton.setDisable(false);
                    progress.setVisible(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Add a new message to the output screen.
     */
    private void addMessage(String sender, String message) {
        ChatMessage chatMessage = new ChatMessage(sender, message);
        chatHistory.add(chatMessage);
        historyIndex = chatHistory.size() - 1;

        // add to the output window
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String formattedMessage = String.format("[%s] %s:\n%s\n\n",
                timestamp, sender, message);

        outputArea.appendText(formattedMessage);

        // Scroll down to the bottom
        outputArea.setScrollTop(Double.MAX_VALUE);
    }

    /**
     * Removes the last chat from history.
     */
    private void handleReset() {
        if (chatHistory.isEmpty()) {
            return;
        }

        // Remove the last two messages (User + Agent)
        if (chatHistory.size() >= 2) {
            chatHistory.remove(chatHistory.size() - 1); // Agent
            chatHistory.remove(chatHistory.size() - 1); // User
        } else if (chatHistory.size() == 1) {
            chatHistory.remove(chatHistory.size() - 1);
        }

        refreshOutput();

        // deactivate Button if history is empty
        if (chatHistory.isEmpty()) {
            resetButton.setDisable(true);
        }

        statusLabel.setText("Removed last messages");
    }

    /**
     * Handler method.
     * Copies the last answer to the clipboard.
     */
    private void handleApply() {

        if (chatHistory.isEmpty()) {
            return;
        }

        // find the last LLM response
        for (int i = chatHistory.size() - 1; i >= 0; i--) {
            ChatMessage msg = chatHistory.get(i);
            if (msg.sender.equals("LLM:")) {

                String m = msg.message;
                int idx = m.indexOf("sequenceDiagram");
                String cn;
                if (idx >= 0)
                    cn = m.substring(idx);
                else
                    cn = "sequenceDiagram \n" + m;
                cn = cn.replaceAll("```","");

                javafx.scene.input.ClipboardContent content =
                        new javafx.scene.input.ClipboardContent();
                content.putString(msg.message);
                javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);

                statusLabel.setText("Copied content to clipboard.");

                // Optional: Visuelles Feedback
                showNotification("Applied changes to editor");

                String finalCn = cn;
                Platform.runLater(() -> {
                 parent.updateCodeArea(finalCn);
                 parent.updatePreview();}
                );

                primaryStage.close();
                return;
            }
        }

    }

    /**
     * Update chat window. Append the latest request/answer to the output area.
     */
    private void refreshOutput() {
        outputArea.clear();

        for (ChatMessage msg: chatHistory) {
            String timestamp = msg.timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String formattedMessage = String.format("[%s] %s:\n%s\n\n",
                    timestamp, msg.sender, msg.message);
            outputArea.appendText(formattedMessage);
        }
    }

    /**
     * Show a short notification.
     */
    private void showNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();

        // Auto-close after two seconds
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Platform.runLater(() -> alert.close());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Apply CSS Styling
     */
    private void applyStyling(Scene scene) {
        scene.getRoot().setStyle(
                "-fx-background-color: #f5f5f5;"
        );

        outputArea.setStyle(
                "-fx-control-inner-background: white;" +
                "-fx-border-color: #cccccc;" +
                "-fx-border-radius: 5;" +
                "-fx-background-radius: 5;"
        );

        promptField.setStyle(
                "-fx-background-radius: 5;" +
                "-fx-border-color: #cccccc;" +
                "-fx-border-radius: 5;"
        );
    }

    /**
     * Chat Message class. Just for internal handling.
     */
    private static class ChatMessage {
        String sender;
        String message;
        LocalTime timestamp;

        ChatMessage(String sender, String message) {
            this.sender = sender;
            this.message = message;
            this.timestamp = LocalTime.now();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

