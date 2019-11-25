package logintime;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

public class PrimaryController {
    @FXML
    public Button openLoginFile;
    @FXML
    private BorderPane borderPane;
    @FXML
    private TextField userSearch;
    @FXML
    private TextField computerSearch;
    @FXML
    private Text credit;
    @FXML
    private TableView<Login> tableView;
    @FXML
    private TableColumn<Login, String> timeColumn;
    @FXML
    private TableColumn<Login, String> usernameColumn;
    @FXML
    private TableColumn<Login, String> computernameColumn;

    private ObservableList<Login> allData = FXCollections.observableArrayList();

    private Preferences preferences; // stores location of last file opened

    private void setPath(String newLoginPath) {
        preferences = Preferences.userNodeForPackage(this.getClass());
        String preferenceName = "pathToLoginFile";
        preferences.put(preferenceName, newLoginPath);

    }

    private String getStoredPath() {
        preferences = Preferences.userNodeForPackage(this.getClass());
        String preferenceName = "pathToLoginFile";
        return preferences.get(preferenceName, System.getProperty("user.home"));
    }

    @FXML
    private void initialize() {
        // lots of help from: https://code.makery.ch/blog/javafx-8-tableview-sorting-filtering/
        // 0. Initialize the columns.
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        computernameColumn.setCellValueFactory(new PropertyValueFactory<>("computerName"));

        // allow copy and paste
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableUtils.installCopyPasteHandler(tableView);

        String preferredPath = getStoredPath();
        credit.setText("Reading from: " + preferredPath);
        System.out.println("Reading from: " + preferredPath);
        readFileContents(new File(getStoredPath()));
    }

    @FXML
    public void openFile() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        File loginFile = fileChooser.showOpenDialog(borderPane.getScene().getWindow());
        if (loginFile != null) {
            setPath(loginFile.getCanonicalPath());
            System.out.println("Now reading from: " + loginFile.getCanonicalPath());
            readFileContents(loginFile);
        } else
            System.out.println("Selection cancelled");
    }

    private void readFileContents(File file) {
        ArrayList<Login> allLogins = new ArrayList<>();

        try {
            long startTime = System.currentTimeMillis();
            allData.clear();
            Stream<String> lines = Files.lines(file.toPath(), StandardCharsets.ISO_8859_1); // reads without issue
            AtomicInteger linesRead = new AtomicInteger();
            lines.forEach(line -> {
                int lineNum = linesRead.getAndIncrement();

                if (!line.trim().isEmpty()) { // not a blank line
                    int endOfLine = line.length() - 1;
                    int endOfUsername = line.substring(0, endOfLine).lastIndexOf(" ");
                    int endOfTime = line.substring(0, endOfUsername - 1).lastIndexOf(" ");

                    try {
                        String time = line.substring(0, endOfTime);
                        String username = line.substring(endOfTime + 1, endOfUsername);
                        String computerName = line.substring(endOfUsername + 1, endOfLine);

                        allLogins.add(new Login(time, username, computerName));
                    } catch (Exception e) {
                        System.out.println("Bad format on line: " + lineNum);
                    }
                } else {
                    System.out.println(lineNum + " is blank");
                }
            });
            credit.setText("Reading from: " + file.toPath() + ", Time Elapsed: " + (System.currentTimeMillis() - startTime) + "ms");

            allData.addAll(allLogins);
            allowFiltering();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            allData.clear();
            credit.setText("Invalid File");
            System.out.println("Invalid file");
        }

    }

    private void allowFiltering() {
        // 1. Wrap the ObservableList in a FilteredList (initially display all data).
        FilteredList<Login> filteredList = new FilteredList<>(allData, p -> true);

        // 2. Set the filter Predicate whenever the filter changes.
        userSearch.textProperty().addListener((observable, oldValue, newValue) -> filteredList.setPredicate(login -> {
            String computerSeachTextLowerCase = computerSearch.getText().toLowerCase();

            // If filter text is empty, display all logins.
            if (newValue.isEmpty() && computerSeachTextLowerCase.isEmpty()) {
                return true;
            }

            // Compare username of every login with filter text.
            String lowerCaseFilter = newValue.toLowerCase();

            return login.getUsername().toLowerCase().contains(lowerCaseFilter) &&
                    login.getComputerName().toLowerCase().contains(computerSeachTextLowerCase); // Filter matches first name.
        }));

        computerSearch.textProperty().addListener((observable, oldValue, newValue) -> filteredList.setPredicate(login -> {
            String userSeachTextLowerCase = userSearch.getText().toLowerCase();
            // If filter text is empty, display all logins.
            if (newValue.isEmpty() && userSeachTextLowerCase.isEmpty()) {
                return true;
            }

            // Compare username of every login with filter text.
            String lowerCaseFilter = newValue.toLowerCase();
            return login.getComputerName().toLowerCase().contains(lowerCaseFilter) &&
                    login.getUsername().contains(userSeachTextLowerCase); // Filter matches first name.
        }));

        // 3. Wrap the FilteredList in a SortedList.
        SortedList<Login> sortedData = new SortedList<>(filteredList);

        // 4. Bind the SortedList comparator to the TableView comparator.
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());

        // 5. Add sorted (and filtered) data to the table.
        tableView.setItems(sortedData);
    }
}