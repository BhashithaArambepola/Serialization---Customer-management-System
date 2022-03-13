package lk.ijse.dep8.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import lk.ijse.dep8.CustomerTM;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class ManageCustomerFormController {
    private final Path dbPath = setPathfile();
    public TextField txtID;
    public TextField txtName;
    public TextField txtAddress;
    public TableView<CustomerTM> tblCustomers;
    public Button btnSelectPic;
    public TextField txtSelectPic;
    public Button btnSaveCustomer;

    public void initialize() {
        tblCustomers.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblCustomers.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblCustomers.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<CustomerTM, ImageView> colpicture = (TableColumn<CustomerTM, ImageView>) tblCustomers.getColumns().get(3);
        colpicture.setCellValueFactory(param -> {
            if (param.getValue().getImage() != null) {
                byte[] picture = param.getValue().getImage();
                ByteArrayInputStream bais = new ByteArrayInputStream(picture);
                ImageView imageView = new ImageView(new Image(bais));
                imageView.setFitWidth(75);
                imageView.setFitHeight(75);
                return new ReadOnlyObjectWrapper<>(imageView);
            }
            return null;
        });

        TableColumn<CustomerTM, Button> lastCol = (TableColumn<CustomerTM, Button>) tblCustomers.getColumns().get(4);
        lastCol.setCellValueFactory(param -> {
            Button button = new Button("Delete");
            button.setOnAction(event -> {
                tblCustomers.getItems().remove(param.getValue());
                saveCustomers();

            });
            return new ReadOnlyObjectWrapper<>(button);
        });

        initDatabase();
        tblCustomers.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnSaveCustomer.setText(newValue == null ? "Save Customer" : "Update Customer");
            txtID.setDisable(true);
            fillFields(newValue);
        });

    }

    public void btnSaveCustomer_OnAction(ActionEvent actionEvent) throws IOException {

        if (!isValidate()) return;
        else {
            CustomerTM newCustomer;
            if (btnSaveCustomer.getText().equals("Save Customer")) {

                if (!txtSelectPic.getText().isEmpty()) {
                    byte[] picture = Files.readAllBytes(Paths.get(txtSelectPic.getText()));
                    newCustomer = new CustomerTM(txtID.getText(), txtName.getText(), txtAddress.getText(), picture);
                    tblCustomers.getItems().add(newCustomer);
                } else {
                    newCustomer = new CustomerTM(txtID.getText(), txtName.getText(), txtAddress.getText(), null);
                    tblCustomers.getItems().add(newCustomer);
                }

            } else {

                CustomerTM selectedCustomer = tblCustomers.getSelectionModel().getSelectedItem();
                String id = selectedCustomer.getId();
                selectedCustomer.setName(txtName.getText());
                selectedCustomer.setAddress(txtAddress.getText());
                if (!txtSelectPic.getText().equals("[IMAGE]")) {
                    selectedCustomer.setImage(Files.readAllBytes(Paths.get(txtSelectPic.getText())));
                }
                tblCustomers.getSelectionModel().clearSelection();
                tblCustomers.refresh();
            }

            boolean result = saveCustomers();
            if (result) {
                new Alert(Alert.AlertType.INFORMATION, "Customer saved").show();
                clearFields();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to save").show();
            }
        }

    }

    private void clearFields() {
        txtID.clear();
        txtName.clear();
        txtAddress.clear();
        txtSelectPic.clear();
        txtID.requestFocus();
    }

    private boolean saveCustomers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(dbPath, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))) {
            oos.writeObject(new ArrayList<CustomerTM>(tblCustomers.getItems()));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    private void fillFields(CustomerTM customer) {
        txtID.setText(customer.getId());
        txtName.setText(customer.getName());
        txtAddress.setText(customer.getAddress());
        txtSelectPic.setText("[IMAGE]");
    }

    public void btnSelectPicture_OnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("iamge", "*.jpg", "*.png", "*.jpeg"));
        File file = fileChooser.showOpenDialog(tblCustomers.getScene().getWindow());
        if (file != null) {
            txtSelectPic.setText(file.getAbsolutePath());
        }
    }

    private void initDatabase() {

        try {
            if (!Files.exists(dbPath)) {
                Path pathDir = Files.createDirectory(dbPath.getParent());
                Files.createFile(dbPath);
            }

            loadDatabase();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to initialize").showAndWait();
            Platform.exit();
        }
    }

    private Path setPathfile() {
        String homeDirPath = System.getProperty("user.home");
        Path pathDir = Paths.get(homeDirPath, "Desktop", "Serialization");
        Path pathFile = Paths.get(pathDir.toString(), "db.dep8");
        return pathFile;
    }

    private void loadDatabase() {
        try {
            ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(dbPath, StandardOpenOption.READ));
            tblCustomers.getItems().clear();
            tblCustomers.setItems(FXCollections.observableArrayList((ArrayList<CustomerTM>) (ois.readObject())));

        } catch (IOException | ClassNotFoundException e) {
            if (e instanceof EOFException) {
                return;
            }
        }
    }

    public boolean isValidate() {
        if (!txtID.getText().matches("C\\d{3}") || tblCustomers.getItems().stream().anyMatch(c -> c.getId().equalsIgnoreCase(txtID.getText()))) {
            txtID.requestFocus();
            txtID.selectAll();
            return false;
        } else if (txtName.getText().trim().isEmpty()) {
            txtName.requestFocus();
            txtName.selectAll();
            return false;
        } else if (txtAddress.getText().trim().isEmpty()) {
            txtAddress.requestFocus();
            txtAddress.selectAll();
            return false;
        } else {
            return true;
        }
    }
}
