package hsmw.creator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * With the help of this small program it is possible to create scripts for the data population
 * tool AutoPodMobile. The script is a mermaid.js script. For visualization, mermaid.js is also
 * used in conjunction with a <code>Webview</code> object.
 *
 * @author D. Pawlaszczyk
 * @version 0.1
 */
public class APMCreator extends Application {

    // Syntax highlighting patterns
    private static final String[] KEYWORDS = new String[]{"StoryHeader", "sequenceDiagram", "title", "section", "participant", "actor", "loop", "alt", "opt", "par", "activate", "deactivate", "idle", "read mails", "send mail", "mock location", "browse", "call", "random", "send", "read", "take photo"};
    private static final String[] DIAGRAM_TYPES = new String[]{"TB", "TD", "BT", "RL", "LR", "TD", "DB", "DT", "UML", "YAML", "JSON", "XML"};
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String DIAGRAM_TYPE_PATTERN = "\\b(" + String.join("|", DIAGRAM_TYPES) + ")\\b";
    private static final String ARROW_PATTERN = "(->>|---|-->|--x|--o|<--|<->|<-->|x--|o--|==>|<==>|==>|<==)";
    private static final String BRACKET_PATTERN = "\\[|\\]|\\{|\\}|\\(|\\)";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "%%.*";
    private static final String ATTRIBUTE_PATTERN = "\\w+(?=\\=)";
    private static final String ENTITY_PATTERN = "\\b[A-Za-z][A-Za-z0-9_]*\\b";
    private static final Pattern PATTERN = Pattern.compile("(?<KEYWORD>" + KEYWORD_PATTERN + ")" + "|(?<DIAGRAMTYPE>" + DIAGRAM_TYPE_PATTERN + ")" + "|(?<ARROW>" + ARROW_PATTERN + ")" + "|(?<BRACKET>" + BRACKET_PATTERN + ")" + "|(?<STRING>" + STRING_PATTERN + ")" + "|(?<COMMENT>" + COMMENT_PATTERN + ")" + "|(?<ATTRIBUTE>" + ATTRIBUTE_PATTERN + ")" + "|(?<ENTITY>" + ENTITY_PATTERN + ")");
    // Quick insert button configurations - content to insert when button is clicked
    private static final String[][] QUICK_INSERT_BUTTONS = {
            // Row 1
            {"create header", """
                sequenceDiagram
                 title Example Storyboard\s
                
                %% Example for a storyboard
                
                actor A as Alice
                actor B as Bob
                A->>B: send[WhatsApp] "How are you?"
                B->>A: send[WhatsApp]"Great!\"""", "Insert a story board header."}, {"idle", "%% Wait for a minute.\n A->>A:idle(1)", "Create a new story board with default header", "Insert a delay."}, {"read mails", "A->>A: read mails", "Read mail from the default mail account of the use."}, {"send mail", """
                A->>B: send mail
                %% $receiver:"test@example.com"
                %% $subject:"urgent message"
                %% $msg:"hi, this mail is for you. Kind Regards \"""", "Insert send mail action."},

            // Row 2
            {"random", "%% do n random activities. \nA->>A:random(10)", "Start a series of random activities on the phone. "}, {"browse", "%% browse a website. \nA->>A: browse \"https://hs-mittweida.de\"", "Browse a website."}, {"take photo ", "\nA->>A: take photo", "Take a photo with the built-in camera."}, {"call", "%% Do a phone call. \nA->>B:call 10s", "Do a phone call."},
            // Row 3
            {"mock location", "%% person changes location to [HOME,WORK,SPARETIME]\nA->>A: mock location[HOME]", "Change the location of the user."}, {"send msg", "%% send a message with [Signal|Telegram|Threema|WhatsApp]\nA->>B: send[WhatsApp]\"message\"", "Send a message to another actor using a messenger app."}, {"read messages", "\nA->>A: read messages[WhatsApp]", "Read a message from a messenger app."}, {"note", "Note over A: place your note right here.", "Insert a simple comment to the story."}};
    private static final String clear_editor = """
            sequenceDiagram
             title Example Storyboard\s
            
            %% Example for a storyboard
            
            actor A as Alice
            actor B as Bob
            A->>B: send[WhatsApp] "How are you?"
            B->>A: send[WhatsApp]"Great!\"""";
    private CodeArea codeArea;
    private WebEngine webEngine;

    //"mock location","send msg","read msg"
    private File currentFile;
    private Label statusLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("APM Creator - The Storyboard Editor");

        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        //Set icon on the taskbar/dock
        if (Taskbar.isTaskbarSupported()) {
            var taskbar = Taskbar.getTaskbar();

            if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
                var dockIcon = defaultToolkit.getImage(getClass().getResource("/filmrole.png"));
                taskbar.setIconImage(dockIcon);
            }

        }

        MenuItem mntopen = new MenuItem("Open Storyboard...");
        mntopen.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        mntopen.setOnAction(e -> openFile(primaryStage));

        MenuItem mntmExport = new MenuItem("Save Storyboard...");
        mntmExport.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        mntmExport.setOnAction(e -> saveFile());

        MenuItem mntmExit = new MenuItem("Exit");
        mntmExit.setAccelerator(KeyCombination.keyCombination("Alt+F4"));
        mntmExit.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        MenuItem mntAbout = new MenuItem("About...");
        mntAbout.setOnAction(e -> new AboutDialog(codeArea));

        SeparatorMenuItem sep = new SeparatorMenuItem();

        Menu mnFiles = new Menu("File");
        Menu mnInfo = new Menu("Info");

        mnFiles.getItems().addAll(mntopen, mntmExport, sep, mntmExit);
        mnInfo.getItems().addAll(mntAbout);


        /* MenuBar */
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(mnFiles, mnInfo);


        // Create main layout
        BorderPane master = new BorderPane();
        master.setPadding(new Insets(5));

        // Create code editor area with syntax highlighting
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setWrapText(true);
        codeArea.textProperty().addListener((obs, oldText, newText) -> codeArea.setStyleSpans(0, computeHighlighting(newText)));
        String default_code = """
                sequenceDiagram\s
                
                title Example Storyboard\s
                
                %% Example for a storyboard
                
                actor A as Alice
                actor B as Bob
                
                A->>B: send[WhatsApp]"hello"
                Note over A: Wait for an answer.
                B->>A: send[WhatsApp]"hi."
                B->>B: idle(1)
                A->>A: mock location[HOME].
                A->>B: send[WhatsApp]"I'm at home now."
                """;
        codeArea.appendText(default_code);


        // Add tab handling
        codeArea.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.TAB) {
                codeArea.replaceSelection("    ");
                e.consume();
            } else if (e.getCode() == KeyCode.ENTER) {

                updatePreview();
            }

        });

        // Create preview web view (for rendering the diagram)
        WebView webView = new WebView();
        webEngine = webView.getEngine();

        // Create buttons
        Button openButton = new Button();
        Button saveButton = new Button();
        Button clearButton = new Button();
        Button previewButton = new Button();

        Label logoLabel = new Label();
        String s = Objects.requireNonNull(APMCreator.class.getResource("/filmrole.png")).toExternalForm();
        javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(s);
        logoLabel.setGraphic(iv);

        String sOpen;
        sOpen = Objects.requireNonNull(APMCreator.class.getResource("/open_small.png")).toExternalForm();
        iv = new javafx.scene.image.ImageView(sOpen);
        openButton.setGraphic(iv);
        openButton.setTooltip(new Tooltip("Open a existing story..."));

        String ssave = Objects.requireNonNull(APMCreator.class.getResource("/save_small.png")).toExternalForm();
        iv = new javafx.scene.image.ImageView(ssave);
        saveButton.setGraphic(iv);
        saveButton.setTooltip(new Tooltip("Save the story to file..."));

        String sClear;
        sClear = Objects.requireNonNull(APMCreator.class.getResource("/clear_small.png")).toExternalForm();
        iv = new javafx.scene.image.ImageView(sClear);
        clearButton.setGraphic(iv);
        clearButton.setTooltip(new Tooltip("Clear editor window"));


        String sSync;
        sSync = Objects.requireNonNull(APMCreator.class.getResource("/sync_small.png")).toExternalForm();
        iv = new javafx.scene.image.ImageView(sSync);
        previewButton.setGraphic(iv);
        previewButton.setTooltip(new Tooltip("Refresh preview"));


        // Status label
        statusLabel = new Label("No file loaded");

        // Button actions
        openButton.setOnAction(e -> openFile(primaryStage));
        saveButton.setOnAction(e -> saveFile());
        clearButton.setOnAction(e -> {
            codeArea.clear();
            codeArea.insertText(0, clear_editor);
            updatePreview();
        });
        previewButton.setOnAction(e -> updatePreview());

        // Layout for buttons
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_LEFT);
        buttonBar.setPadding(new Insets(0, 0, 0, 0));
        buttonBar.getChildren().addAll(openButton, saveButton, clearButton, previewButton, statusLabel);


        // Create scrollable container for code area
        ScrollPane scrollPane = new ScrollPane(codeArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Create quick insert button grid
        GridPane quickInsertGrid = createQuickInsertButtons();


        // Split view for editor and preview
        VBox editorBox = new VBox();
        //editorBox.getChildren().addAll(new Label("APM-Creator"), scrollPane);
        editorBox.getChildren().addAll(new Label("Storyboard Editor"), scrollPane, new Label("Quick Insert"), quickInsertGrid);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox previewBox = new VBox();
        previewBox.getChildren().addAll(new Label("Preview"), webView);
        VBox.setVgrow(webView, Priority.ALWAYS);

        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.5f);

        splitPane.getItems().addAll(editorBox, previewBox);

        HBox.setHgrow(editorBox, Priority.ALWAYS);
        HBox.setHgrow(previewBox, Priority.ALWAYS);


        BorderPane top = new BorderPane();
        top.setPadding(new Insets(0));
        top.setLeft(buttonBar);
        top.setRight(logoLabel);

        // Add components to root layout
        VBox tt = new VBox();
        tt.getChildren().addAll(menuBar, top);
        master.setTop(tt);
        master.setCenter(splitPane);

        HBox statusline = new HBox(10);
        Label slabel = new Label("version 0.1");
        statusline.getChildren().add(slabel);
        statusline.setMaxHeight(32);

        master.setBottom(statusline);

        //Attach the icon to the stage/window
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(APMCreator.class.getResourceAsStream("/filmrole.png"))));


        // Create scene with CSS styling
        Scene scene = new Scene(master, Screen.getPrimary().getVisualBounds().getWidth() * 0.9, Screen.getPrimary().getVisualBounds().getHeight() * 0.9);

        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/mermaid-editor.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.sizeToScene();

        primaryStage.show();

        // Initialize the web view with basic HTML template
        initializeWebView();
    }

    /**
     * Insert a grid with 4x3 quick-insert buttons.
     *
     * @return the new GridPane
     */
    private GridPane createQuickInsertButtons() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);

        int numCols = 4;

        for (int i = 0; i < QUICK_INSERT_BUTTONS.length; i++) {
            int row = i / numCols;
            int col = i % numCols;

            String buttonText = QUICK_INSERT_BUTTONS[i][0];
            String insertText = QUICK_INSERT_BUTTONS[i][1];
            String tooltipText = QUICK_INSERT_BUTTONS[i][2];

            Button button = new Button(buttonText);
            button.setTooltip(new Tooltip(tooltipText));
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(e -> insertTextAtCursor(insertText));

            grid.add(button, col, row);
        }

        // Make buttons take up equal space
        for (int i = 0; i < numCols; i++) {
            javafx.scene.layout.ColumnConstraints colConstraints = new javafx.scene.layout.ColumnConstraints();
            colConstraints.setHgrow(Priority.ALWAYS);
            colConstraints.setFillWidth(true);
            grid.getColumnConstraints().add(colConstraints);
        }

        return grid;
    }

    private void insertTextAtCursor(String text) {
        codeArea.insertText(codeArea.getCaretPosition(), text);
        codeArea.requestFocus();
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            String styleClass = matcher.group("KEYWORD") != null ? "keyword" : matcher.group("DIAGRAMTYPE") != null ? "diagram-type" : matcher.group("ARROW") != null ? "arrow" : matcher.group("BRACKET") != null ? "bracket" : matcher.group("STRING") != null ? "string" : matcher.group("COMMENT") != null ? "comment" : matcher.group("ATTRIBUTE") != null ? "attribute" : matcher.group("ENTITY") != null ? "entity" : null;

            assert styleClass != null;

            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }

        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    /**
     * Creates an example diagram. This one is loaded by default.
     */
    private void initializeWebView() {

        String URI = Objects.requireNonNull(APMCreator.class.getResource("/mermaid.min.js")).toString();

        String htmlTemplate = "<!DOCTYPE html>\n" + "<html>\n" + "<head>\n" + "    <meta charset=\"UTF-8\">\n" + "    <script src=\"" + URI + "\"></script>\n" + "    <script>\n" + "        mermaid.initialize({ startOnLoad: true, theme: 'default' });\n" + "    </script>\n" + "    <style>\n" + "        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; }\n" + "        .mermaid { width: 100%; }\n" + "    </style>\n" + "</head>\n" + "<body>\n" + "    <div class=\"mermaid\">\n" + "sequenceDiagram \n\n" + "title Example Storyboard\n\n" + "%% This is a basic example for a storyboard\n\n" + "actor A as Alice\n" + "actor B as Bob\n" + " A->>B: send[WhatsApp]\"hello\"\n" + "            Note over A: Wait for an answer.\n" + "            B->>A: send[WhatsApp]\"hi.\"\n" + "            B->>B: idle\n" + "            A->>A: mock location[HOME].\n" + "            A->>B: send[WhatsApp]\"I'm at home now\"\n" + "    </div>\n" + "    <script>\n" + "        function updateDiagram(code) {\n" + "            document.querySelector('.mermaid').innerHTML = code;\n" + "            mermaid.init();\n" + "        }\n" + "    </script>\n" + "</body>\n" + "</html>";

        webEngine.loadContent(htmlTemplate);
    }

    /**
     * Creates new temporary html-page with the instructions from
     * the <code>codeArea</code>.
     *
     * @param mermaid_code a string containing the diagram text
     */
    private void updateWebView(String mermaid_code) {

        String URI = Objects.requireNonNull(APMCreator.class.getResource("mermaid.min.js")).toString();

        String htmlTemplate = "<!DOCTYPE html>\n" + "<html>\n" + "<head>\n" + "    <meta charset=\"UTF-8\">\n" + "    <script src=\"" + URI + "\"></script>\n" + "    <script>\n" + "        mermaid.initialize({ startOnLoad: true, theme: 'default' });\n" + "    </script>\n" + "    <style>\n" + "        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; }\n" + "        .mermaid { width: 100%; }\n" + "    </style>\n" + "</head>\n" + "<body>\n" + "    <div class=\"mermaid\">\n" + mermaid_code + "    </div>\n" + "    <script>\n" + "        function updateDiagram(code) {\n" + "            document.querySelector('.mermaid').innerHTML = code;\n" + "            mermaid.init();\n" + "        }\n" + "    </script>\n" + "</body>\n" + "</html>";
        webEngine.loadContent(htmlTemplate);
    }

    /**
     * Open a new story board file.
     *
     * @param stage this is the parent for the FileChooser Dialog
     */
    private void openFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Storyboard");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Mermaid Files", "*.mmd", "*.mermaid"), new FileChooser.ExtensionFilter("Text Files", "*.txt"), new FileChooser.ExtensionFilter("All Files", "*.*"));

        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                codeArea.replaceText(content);
                currentFile = file;
                statusLabel.setText("Loaded: " + file.getName());
                updatePreview();

            } catch (IOException e) {
                showAlert("Could not open file", e.getMessage());
            }
        }
    }

    /**
     * Save the current state of the codeArea to a file.
     */
    private void saveFile() {
        if (currentFile != null) {
            try {
                Files.write(currentFile.toPath(), codeArea.getText().getBytes());
                statusLabel.setText("Saved: " + currentFile.getName());
            } catch (IOException e) {
                showAlert("Could not save file", e.getMessage());
            }
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Storyboard File");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Mermaid Files", "*.mmd"), new FileChooser.ExtensionFilter("Text Files", "*.txt"));

            File file = fileChooser.showSaveDialog(codeArea.getScene().getWindow());

            if (file != null) {
                try {
                    Files.write(file.toPath(), codeArea.getText().getBytes());
                    currentFile = file;
                    statusLabel.setText("Saved: " + file.getName());
                } catch (IOException e) {
                    showAlert("Could not save file", e.getMessage());
                }
            }
        }
    }

    /**
     * Every time the content of the codeArea has changed, we have to redraw the
     * diagram.
     */
    private void updatePreview() {
        String mermaidCode = codeArea.getText();
        if (mermaidCode == null || mermaidCode.trim().isEmpty()) {
            return;
        }

        updateWebView(mermaidCode);
    }

    private void showAlert(String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}