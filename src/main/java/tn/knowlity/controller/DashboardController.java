package tn.knowlity.controller;

import com.example.demo.HelloApplication;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.mindrot.jbcrypt.BCrypt;
import tn.knowlity.entity.User;

import tn.knowlity.service.userService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.demo.HelloApplication;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.knowlity.entity.User;
import tn.knowlity.service.userService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;
import java.text.ParseException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.scene.shape.Circle;
import tn.knowlity.tools.UserSessionManager;

public class DashboardController {
    @FXML
    private Circle circle1;

    @FXML
    private Circle circle2;

    @FXML
    private Circle circle3;

    @FXML
    private Circle circle4;
    @FXML
    private Circle circle5;

    @FXML
    private Circle circle6;

    @FXML
    private Circle circle7;

    @FXML
    private Circle circle8;

    @FXML
    private Circle circle9;
    @FXML
    private Circle circle10;

    @FXML
    private Circle circle11;

    @FXML
    private Circle circle12;

    @FXML
    private TextField nom;

    @FXML
    private TextField prenom;

    @FXML
    private TextField email;

    @FXML
    private TextField numTelephone;

    @FXML
    private DatePicker dateNaissance;

    @FXML
    private TextField localisation;

    @FXML
    private Button imageChooserButton;

    @FXML
    private Label imagePathLabel;

    @FXML
    private PasswordField password;

    @FXML
    private PasswordField confirmPassword;

    @FXML
    private Button signupButton;

    @FXML
    private Hyperlink pageConnexion;

    @FXML
    private Label errorMessageLabel;




    @FXML
    private TextField id;

    @FXML
    private TextField searchField;


    @FXML
    private TextField age;


    private userService userService = new userService();

    @FXML
    private TableView<User> tableView;
    @FXML
    private TableColumn<User, String> nomColumn;
    @FXML
    private TableColumn<User, String> prenomColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, Integer> numeroColumn;

    @FXML
    private TableColumn<User, String> bannedColumn;
    @FXML
    private TableColumn<User, String> rolsColumn;

    @FXML
    private TableColumn<User, Void> actionsColumn;

    @FXML
    private ImageView imageView;


    @FXML
    private RadioButton adminRole;
    @FXML
    private RadioButton userRole;
    @FXML
    private RadioButton adminoRole;

    private ToggleGroup roleGroup;
    @FXML
    private Label errornom;
    @FXML
    private Label prenomError;
    @FXML
    private Label numTelephoneError;
    @FXML
    private Label dateNaissanceError;
    @FXML
    private Label localisationError;
    @FXML
    private Label passwordError;
    @FXML
    private Label confirmPasswordError;
    @FXML
    private Label emailError;
    @FXML
    private Label rolesError;
    @FXML
    private TableColumn<User, Void> imageUserColumn;
    @FXML
    public void initialize() throws SQLException {
        // Groupes de rôles radio
        roleGroup = new ToggleGroup();
        adminRole.setToggleGroup(roleGroup);
        userRole.setToggleGroup(roleGroup);
        adminoRole.setToggleGroup(roleGroup);

        // Colonne image
        imageUserColumn.setCellFactory(column -> new TableCell<User, Void>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitHeight(75);
                imageView.setFitWidth(60);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    User res = getTableRow().getItem();
                    String imagePath = res.getImage();

                    try {
                        Image image;
                        if (imagePath.startsWith("http") || imagePath.startsWith("file:/")) {
                            image = new Image(imagePath);
                        } else {
                            image = new Image(new File(imagePath).toURI().toString());
                        }

                        imageView.setImage(image);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        System.out.println("Erreur lors du chargement de l'image: " + e.getMessage());
                        setGraphic(null);
                    }
                }
            }
        });

        if (UserSessionManager.getInstance().isLoggedIn()) {
            User user = userService.recherparid(UserSessionManager.getInstance().getCurrentUser().getId());
            String imagePath = user.getImage();
            loadImage(imageView, imagePath);
        } else {
            System.out.println("rahou mahouch connecte");
        }

        imagePathLabel.setText("Aucune image Séléectionné");
        imagePathLabel.setMaxWidth(200);
        imagePathLabel.setEllipsisString("...");

        animateBackgroundCircles();

        // Colonnes simples
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        numeroColumn.setCellValueFactory(new PropertyValueFactory<>("num_telephone"));

        // Colonne "Banned"
        bannedColumn.setPrefWidth(100.0);
        bannedColumn.setCellValueFactory(cellData -> {
            int bannedValue = cellData.getValue().getBanned();
            String displayText = (bannedValue == 1) ? "Banned" : "Not Banned";
            return new SimpleStringProperty(displayText);
        });

        bannedColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Banned")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    }
                }
            }
        });

        rolsColumn.setPrefWidth(160.0);

        rolsColumn.setCellValueFactory(cellData -> {
            String[] rolesArray = cellData.getValue().getRoles();
            if (rolesArray == null || rolesArray.length == 0) {
                return new SimpleStringProperty("Inconnu");
            }

            String rawRole = rolesArray[0].toUpperCase();

            String displayText;
            if (rawRole.contains("ETUDIANT")) {
                displayText = "Etudiant";
            } else if (rawRole.contains("ENSEIGNANT")) {
                displayText = "Enseignant";
            } else if (rawRole.contains("ADMIN")) {
                displayText = "Admin";
            } else {
                displayText = "Inconnu";
            }

            return new SimpleStringProperty(displayText);
        });

        rolsColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Etudiant":
                            setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                            break;
                        case "Enseignant":
                            setStyle("-fx-text-fill: darkgreen; -fx-font-weight: bold;");
                            break;
                        case "Admin":
                            setStyle("-fx-text-fill: goldenrod; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: gray;");
                            break;
                    }
                }
            }
        });



        // Colonnes triables manuellement
        nomColumn.setSortable(true);
        prenomColumn.setSortable(true);

        nomColumn.setOnEditStart(event -> {
            ObservableList<User> sortedList = tableView.getItems().stream()
                    .sorted(Comparator.comparing(User::getNom))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), FXCollections::observableArrayList));
            tableView.setItems(sortedList);
        });

        prenomColumn.setOnEditStart(event -> {
            ObservableList<User> sortedList = tableView.getItems().stream()
                    .sorted(Comparator.comparing(User::getPrenom))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), FXCollections::observableArrayList));
            tableView.setItems(sortedList);
        });

        // Colonne actions (boutons)
        actionsColumn.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button updateButton = new Button("\uD83D\uDD04");
            private final Button supprimerButton = new Button("❌");
            private final Button BannerButton = new Button("\uD83D\uDEAB");
            private final HBox pane = new HBox(10, supprimerButton,BannerButton,updateButton);

            {
                supprimerButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    System.out.println(user.getEmail());
                    supprimerUtilisateur(user);
                });
                BannerButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    System.out.println(user);
                    System.out.println(user.getEmail());
                    banneruser(user);
                    List<User> users = null;
                    try {
                        users = userService.afficherdetailsuser();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    ObservableList<User> observableUsers = FXCollections.observableArrayList(users);
                    tableView.setItems(observableUsers);


                });
                updateButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    System.out.println(user);
                    System.out.println(user.getEmail());
                    unbanneruser(user);
                    List<User> users = null;
                    try {
                        users = userService.afficherdetailsuser();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    ObservableList<User> observableUsers = FXCollections.observableArrayList(users);
                    tableView.setItems(observableUsers);


                });
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    updateButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 8px 16px; -fx-background-radius: 5px;");
                    supprimerButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 8px 16px; -fx-background-radius: 5px;");
                    BannerButton.setStyle("-fx-background-color: #FF4500; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 8px 16px; -fx-background-radius: 5px;");
                    setGraphic(pane);
                }
            }
        });

        // Charger les utilisateurs
        try {
            List<User> users = userService.afficherdetailsuser();
            ObservableList<User> observableUsers = FXCollections.observableArrayList(users);
            tableView.setItems(observableUsers);
        } catch (SQLException e) {
            e.printStackTrace();
            // Affichage ou gestion d'erreur possible
        }
    }

    private void supprimerUtilisateur(User user) {
        try {
            System.out.println(user.getId());

            userService.supprimerUser(user);


            tableView.getItems().remove(user);
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }



    private void unbanneruser(User user) {
        try {

            userService.unbannneruser(user);


        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    private void banneruser(User user) {
        try {


            userService.bannneruser(user);


        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    private void animateBackgroundCircles() {
        // Left side circles (x from 0 to 400)

        // Circle 1 - Top left
        TranslateTransition transition1 = new TranslateTransition(Duration.seconds(3), circle1);
        transition1.setFromX(0);
        transition1.setFromY(0);
        transition1.setToX(100);
        transition1.setToY(-50);
        transition1.setAutoReverse(true);
        transition1.setCycleCount(TranslateTransition.INDEFINITE);
        transition1.play();

        // Circle 2 - Bottom left
        TranslateTransition transition2 = new TranslateTransition(Duration.seconds(4), circle2);
        transition2.setFromX(0);
        transition2.setFromY(0);
        transition2.setToX(-100);
        transition2.setToY(50);
        transition2.setAutoReverse(true);
        transition2.setCycleCount(TranslateTransition.INDEFINITE);
        transition2.play();

        // Circle 3 - Middle left
        TranslateTransition transition3 = new TranslateTransition(Duration.seconds(3), circle3);
        transition3.setFromX(200);
        transition3.setFromY(0);
        transition3.setToX(300);
        transition3.setToY(100);
        transition3.setAutoReverse(true);
        transition3.setCycleCount(TranslateTransition.INDEFINITE);
        transition3.play();

        // Circle 4 - Upper middle left
        TranslateTransition transition4 = new TranslateTransition(Duration.seconds(4), circle4);
        transition4.setFromX(300);
        transition4.setFromY(0);
        transition4.setToX(400);
        transition4.setToY(-75);
        transition4.setAutoReverse(true);
        transition4.setCycleCount(TranslateTransition.INDEFINITE);
        transition4.play();

        // Circle 5 - Far left side
        TranslateTransition transition5 = new TranslateTransition(Duration.seconds(3), circle5);
        transition5.setFromX(-100);
        transition5.setFromY(0);
        transition5.setToX(0);
        transition5.setToY(50);
        transition5.setAutoReverse(true);
        transition5.setCycleCount(TranslateTransition.INDEFINITE);
        transition5.play();

        // Circle 6 - Center left side
        TranslateTransition transition6 = new TranslateTransition(Duration.seconds(4), circle6);
        transition6.setFromX(150);
        transition6.setFromY(0);
        transition6.setToX(200);
        transition6.setToY(-50);
        transition6.setAutoReverse(true);
        transition6.setCycleCount(TranslateTransition.INDEFINITE);
        transition6.play();

        // Right side circles (x from 600 to 950)

        // Circle 7 - Top right
        TranslateTransition transition7 = new TranslateTransition(Duration.seconds(3), circle7);
        transition7.setFromX(600);
        transition7.setFromY(0);
        transition7.setToX(700);
        transition7.setToY(-50);
        transition7.setAutoReverse(true);
        transition7.setCycleCount(TranslateTransition.INDEFINITE);
        transition7.play();

        // Circle 8 - Bottom right
        TranslateTransition transition8 = new TranslateTransition(Duration.seconds(4), circle8);
        transition8.setFromX(700);
        transition8.setFromY(0);
        transition8.setToX(800);
        transition8.setToY(100);
        transition8.setAutoReverse(true);
        transition8.setCycleCount(TranslateTransition.INDEFINITE);
        transition8.play();

        // Circle 9 - Middle right
        TranslateTransition transition9 = new TranslateTransition(Duration.seconds(3), circle9);
        transition9.setFromX(800);
        transition9.setFromY(0);
        transition9.setToX(900);
        transition9.setToY(-75);
        transition9.setAutoReverse(true);
        transition9.setCycleCount(TranslateTransition.INDEFINITE);
        transition9.play();

        // Circle 10 - Upper middle right
        TranslateTransition transition10 = new TranslateTransition(Duration.seconds(4), circle10);
        transition10.setFromX(900);
        transition10.setFromY(0);
        transition10.setToX(950);
        transition10.setToY(50);
        transition10.setAutoReverse(true);
        transition10.setCycleCount(TranslateTransition.INDEFINITE);
        transition10.play();

        // Circle 11 - Far right side
        TranslateTransition transition11 = new TranslateTransition(Duration.seconds(3), circle11);
        transition11.setFromX(1000);
        transition11.setFromY(0);
        transition11.setToX(1100);
        transition11.setToY(-50);
        transition11.setAutoReverse(true);
        transition11.setCycleCount(TranslateTransition.INDEFINITE);
        transition11.play();

        // Circle 12 - Center right side
        TranslateTransition transition12 = new TranslateTransition(Duration.seconds(4), circle12);
        transition12.setFromX(950);
        transition12.setFromY(0);
        transition12.setToX(1050);
        transition12.setToY(75);
        transition12.setAutoReverse(true);
        transition12.setCycleCount(TranslateTransition.INDEFINITE);
        transition12.play();
    }


    public void pageupdate(ActionEvent event) throws IOException {
        // Charger la nouvelle scène
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("inscription.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 800);

        // Obtenir la fenêtre actuelle (celle qui déclenche l'événement)
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Fermer la fenêtre actuelle
        currentStage.close();

        // Ouvrir la nouvelle fenêtre
        Stage stage = new Stage();
        stage.setTitle("Inscription Page");
        stage.setScene(scene);
        stage.show();
    }
    @FXML
    public void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une Image de Profil");

        // Filtrer uniquement les fichiers image
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            imagePathLabel.setText(selectedFile.getAbsolutePath()); // Affiche le chemin de l'image
        }
    }

    @FXML
    public void signin(ActionEvent event) throws IOException {
        String nomInput = nom.getText();
        int compteur =0;

        if( nomInput==null || nomInput.equals("")  ||nomInput.length()<3 ) {
            showError("selectionnez Gennre",errornom);


        }
        else{
            errornom.setOpacity(0);
        }
        String prenomInput = prenom.getText();
        if( prenomInput==null || prenomInput.equals("")  ||prenomInput.length()<3 ) {
            showError("PrenomInvalide",prenomError);
            System.out.println("Prenom invalide");
            compteur ++;
        }
        else{
            prenomError.setOpacity(0);
        }



        String emailInput = email.getText();

        if (emailInput.isEmpty() || !emailInput.matches("^[\\w.-]+@(gmail\\.com|esprit\\.tn)$")) {
            showError("Email invalide",emailError);
            System.out.println("Email valide");
            compteur++;
        } else {
            System.out.println("Email invalide");
            emailError.setOpacity(0);
        }

        String phoneInput1 = numTelephone.getText();

        if (!phoneInput1.matches("[0-9]{8}")) {
            showError("Numéro invalide",numTelephoneError);
            compteur ++;
            System.out.println("numTelephone invalide");
        }
        else{
            numTelephoneError.setOpacity(0);
        }
        //lena probléme


        String passwordInput = password.getText();
        if( passwordInput==null || passwordInput.equals("")  ||passwordInput.length()<3 ) {
            compteur ++;
            showError("Password Invalide",passwordError);
            System.out.println("Password invalide");

        }
        else{
            passwordError.setOpacity(0);
        }
        String hashedPassword = BCrypt.hashpw(passwordInput, BCrypt.gensalt());

        String confirmPasswordInput = confirmPassword.getText();
        if( confirmPasswordInput==null || confirmPasswordInput.equals("")  || confirmPasswordInput.length()<3 ) {
            compteur ++;
            showError("Confirm Password Invalide",confirmPasswordError);
            System.out.println("Confirm Password invalide");

        }
        else{
            System.out.println("Confirm Password valide yes yes yes");
            confirmPasswordError.setOpacity(0);
        }
        Date dateNaissanceInput = null;
        if (dateNaissance.getValue() != null) {
            dateNaissanceInput = Date.from(dateNaissance.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
            System.out.println("Date de naissance : " + dateNaissanceInput);
            dateNaissanceError.setOpacity(0);
        } else {
            System.out.println("Veuillez sélectionner une date.");
            showError("date Invalide" ,dateNaissanceError);
            compteur ++;
        }

        String localisationInput = localisation.getText();
        if (localisationInput.length()<3 || localisationInput.equals("") ) {

            showError("Localisation invalide",localisationError);
            System.out.println("Localisation invalide");
            compteur ++;
        }
        else{
            localisationError.setOpacity(0);
            System.out.println("Localisation valide");
        }
        String imagePathInput = imagePathLabel.getText();

        // Only process image if one was selected
        if (imagePathInput != null && !imagePathInput.equals("Aucune image Séléectionné")) {
            try {
                Path sourcePath = Paths.get(imagePathInput);
                
                // Verify source file exists
                if (!Files.exists(sourcePath)) {
                    showError("L'image source n'existe pas", errorMessageLabel);
                    return;
                }

                // Create destination directory if it doesn't exist
                Path destinationFolder = Paths.get("src/main/resources/images");
                if (!Files.exists(destinationFolder)) {
                    Files.createDirectories(destinationFolder);
                }

                // Generate unique filename
                String fileName = sourcePath.getFileName().toString();
                Path destinationPath = destinationFolder.resolve(fileName);

                // Handle duplicate filenames
                int counter = 1;
                while (Files.exists(destinationPath)) {
                    String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
                    String fileExtension = fileName.substring(fileName.lastIndexOf('.'));
                    destinationPath = destinationFolder.resolve(fileNameWithoutExtension + "_" + counter + fileExtension);
                    counter++;
                }

                // Copy file with error handling
                try {
                    Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                    imagePathInput = destinationPath.toString(); // Update path to new location
                    System.out.println("Image déplacée avec succès vers : " + destinationPath);
                } catch (IOException e) {
                    showError("Erreur lors de la copie de l'image: " + e.getMessage(), errorMessageLabel);
                    e.printStackTrace();
                    return;
                }
            } catch (Exception e) {
                showError("Erreur lors du traitement de l'image: " + e.getMessage(), errorMessageLabel);
                e.printStackTrace();
                return;
            }
        }

        System.out.println("Continuing with user creation...");

        String[] roles = new String[]{"Etudiant"};
        RadioButton selectedRadioButton = (RadioButton) roleGroup.getSelectedToggle();
        if (selectedRadioButton != null) {
            String selectedText = selectedRadioButton.getText();
            if( selectedText.equalsIgnoreCase("Admin") ){
                roles = new String[]{"Admin"};
            }
            if( selectedText.equalsIgnoreCase("Enseignant") ){
                roles = new String[]{"Enseignant"};
            }
            rolesError.setOpacity(0);
        } else {
            showError("selectionnez Roles",rolesError);
        }

        System.out.println("Image path: " + imagePathInput);
        if (compteur ==0) {
            int phoneInput = Integer.parseInt(numTelephone.getText());
            User user = new User(prenomInput, emailInput,dateNaissanceInput, phoneInput,passwordInput,imagePathInput,"homme",localisationInput, confirmPasswordInput, 0,"math",roles,nomInput);
            try {
               if(selectedRadioButton.getText().equalsIgnoreCase("Etudiant")){
               userService.ajouterEtudiant(user);}
                   if(selectedRadioButton.getText().equalsIgnoreCase("Enseignant")){
                    userService.ajouterEnseignant(user);}
                if(selectedRadioButton.getText().equalsIgnoreCase("Admin")){
                    userService.ajouterEtudiant(user);}


                List<User> users = userService.afficherdetailsuser();
                ObservableList<User> observableUsers = FXCollections.observableArrayList(users);
                tableView.setItems(observableUsers);

                // Réinitialiser les champs après l'ajout
                nom.clear();
                prenom.clear();
                numTelephone.clear();
                email.clear();
                password.clear();
                confirmPassword.clear();

                localisation.clear();
                imagePathLabel.setText("");
                dateNaissance.setValue(null);
                roleGroup.selectToggle(null);


            } catch (SQLException e) {
                e.printStackTrace();
                showError("Une erreur est survenue lors du chargement de la fenêtre suivante.");
            }
        }
        else{
            return;
        }
    }


    @FXML
    private void search() throws SQLException {

        String nomsearch = searchField.getText().trim();
        if(nomsearch.length()!=0){
            List<User> list =    userService.recherparnom(nomsearch);
            ObservableList<User> observableList = FXCollections.observableArrayList(list);
            tableView.setItems(observableList);}
        else{
            List<User> list =  userService.afficherdetailsuser();
            ObservableList<User> observableList = FXCollections.observableArrayList(list);
            tableView.setItems(observableList);
        }
        System.out.println(nomsearch);
        System.out.println(userService.recherparnom(nomsearch).size());

    }

    private void showError(String message) {
        errorMessageLabel.setText(message);
        errorMessageLabel.setOpacity(1); // Rendre le label visible
    }
    private void showError(String message,Label nomlabel) {
        nomlabel.setText(message);
        nomlabel.setOpacity(1);
    }
    public void affichertest() throws IOException {
        this.pageupdate();
    }


    public void logout() throws IOException {
        UserSessionManager.getInstance().logout();
        String file = "/User/loginPage.fxml";

        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(file));
        Scene scene = new Scene(fxmlLoader.load(), 1500, 800);

        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();


        Stage currentStage = (Stage) email.getScene().getWindow();
        currentStage.close();

    }
    public void pagedashboard() throws IOException {

        Stage currentStage = (Stage) circle1.getScene().getWindow();
        navbarController.changeScene("user.fxml", currentStage);
    }
    public void pageupdate() throws IOException {

        Stage currentStage = (Stage) circle1.getScene().getWindow();
        navbarController.changeScene("/User/updateBack.fxml", currentStage);
    }

    public void pageAssociation() throws IOException {

        Stage currentStage = (Stage) circle1.getScene().getWindow();
        navbarController.changeScene("/Benevolat/Association/affichageBack.fxml", currentStage);
    }

    private void loadImage(ImageView imageView, String imagePath) {
        String path = imagePath != null && !imagePath.isEmpty() ? imagePath.trim() : "/images/user_placeholder.png";
        if (path != null && !path.isEmpty()) {
            path = path.substring(path.lastIndexOf("\\") + 1); // Handles backslashes
            path = path.substring(path.lastIndexOf("/") + 1);  // Handles forward slashes
        }

        if (!path.startsWith("/images/") && !path.equals("/images/user_placeholder.png")) {
            path = "/images/" + path;
        }

        try {
            InputStream stream = getClass().getResourceAsStream(path);
            if (stream == null) {
                System.err.println("Image not found: " + path);
                stream = getClass().getResourceAsStream("/images/user_placeholder.png");
            }
            imageView.setImage(new Image(stream));
        } catch (Exception e) {
            System.err.println("Error loading image: " + path + ". Error: " + e.getMessage());
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/user_placeholder.png")));
        }
    }



}
