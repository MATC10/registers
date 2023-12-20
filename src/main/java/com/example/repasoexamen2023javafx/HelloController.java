package com.example.repasoexamen2023javafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateStringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnInsertar;

    @FXML
    private Button btnModificar;

    @FXML
    private Button btnCsv;

    @FXML
    private Button btnJson;

    @FXML
    private Button btnXml;

    @FXML
    private Button btnEscribirXml;

    @FXML
    private ComboBox<String> cbLenguaje;

    @FXML
    private CheckBox checkCarrera;

    @FXML
    private TableColumn<Persona, Integer> colId;

    @FXML
    private TableColumn<Persona, Boolean> colCarrera;

    @FXML
    private TableColumn<Persona, Integer> colEdad;

    @FXML
    private TableColumn<Persona, LocalDate> colFecha;

    @FXML
    private TableColumn<Persona, String> colLenguaje;

    @FXML
    private TableColumn<Persona, String> colNombre;

    @FXML
    private TableView<Persona> tableViewPersonas;

    @FXML
    private Label lblId;

    @FXML
    private TextField txtEdad;

    @FXML
    private TextField txtFecha;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtCsv;

    @FXML
    private TextField txtJson;

    @FXML
    private TextField txtXml;

    @FXML
    private TextField txtEscribirXml;

    RepositorioPersona repositorioPersona;
    Conexion conexion;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        conexion=new Conexion();
        repositorioPersona = new RepositorioPersona(conexion.conexion);

        ObservableList<Persona> listaPersonas=repositorioPersona.leerTodosFX();

        //id, nombre, edad... se refieren a los atributos de la clase Persona
        //introdcimos los datos en las columnas de la tabla
        colId.setCellValueFactory(new PropertyValueFactory<Persona, Integer>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<Persona, String>("nombre"));
        colEdad.setCellValueFactory(new PropertyValueFactory<Persona, Integer>("edad"));
        colFecha.setCellValueFactory(new PropertyValueFactory<Persona, LocalDate>("fechaNacimiento"));
        colLenguaje.setCellValueFactory(new PropertyValueFactory<Persona, String>("lenguaje"));
        colCarrera.setCellValueFactory(new PropertyValueFactory<Persona, Boolean>("carrera"));
//lo formateo
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringConverter<LocalDate> fechaFormateada = new LocalDateStringConverter(formatter, null);
        colFecha.setCellFactory(TextFieldTableCell.forTableColumn(fechaFormateada));

        //introducimos los datos en el combobox
        ObservableList<String> listaLenguajes = FXCollections.observableArrayList("Java", "Python", "Kotlin", "Otro");
        cbLenguaje.setItems(listaLenguajes);
        cbLenguaje.getSelectionModel().selectFirst();

        //introducimos los datos en la tabla
        tableViewPersonas.setItems(listaPersonas);

        //si seleccionamos una fila de la tabla, se rellenan los campos
        tableViewPersonas.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            Persona p = tableViewPersonas.getSelectionModel().getSelectedItem();
            if (p != null) {
                lblId.setText(String.valueOf(p.getId()));
                txtNombre.setText(p.getNombre());
                txtEdad.setText(String.valueOf(p.getEdad()));
                //txtFecha.setText(String.valueOf(p.getFechaNacimiento()));

//lo formateo para que se muestre en el formato correcto en la tabla
                DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                txtFecha.setText(p.getFechaNacimiento().format(formatter2));

                cbLenguaje.getSelectionModel().select(p.getLenguaje());
                checkCarrera.setSelected(p.isCarrera());
            }
        });
    }

    public void actualizarTabla(){
        ObservableList<Persona> listaPersonas = repositorioPersona.leerTodosFX();
        tableViewPersonas.setItems(listaPersonas);
    }

    public void limpiarCampos(){
        lblId.setText("");
        txtNombre.setText("");
        txtEdad.setText("");
        txtFecha.setText("");
        cbLenguaje.getSelectionModel().selectFirst();
        checkCarrera.setSelected(false);
    }

    @FXML
    void onEliminar(ActionEvent event) {
        Persona p = tableViewPersonas.getSelectionModel().getSelectedItem();
        if (p != null) {
            repositorioPersona.delete(p.getId());
            actualizarTabla();
            limpiarCampos();
        }
    }

    @FXML
    void onInsertar(ActionEvent event) {
        Persona p = new Persona();
        p.setNombre(txtNombre.getText());
        p.setEdad(Integer.parseInt(txtEdad.getText()));
        //p.setFechaNacimiento(LocalDate.parse(txtFecha.getText()));
//formateo la fecha para que se guarde en la bbdd en el formato correcto
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        p.setFechaNacimiento(LocalDate.parse(txtFecha.getText(), formatter));

        p.setLenguaje(cbLenguaje.getSelectionModel().getSelectedItem());
        p.setCarrera(checkCarrera.isSelected());

        repositorioPersona.inserta(p);
        actualizarTabla();
        limpiarCampos();
    }

    @FXML
    void onModificar(ActionEvent event) {
        Persona p = new Persona();
        p.setId(Integer.parseInt(lblId.getText()));
        p.setNombre(txtNombre.getText());
        p.setEdad(Integer.parseInt(txtEdad.getText()));
        //p.setFechaNacimiento(LocalDate.parse(txtFecha.getText()));
//formateo la fecha para que se guarde en la bbdd en el formato correcto
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        p.setFechaNacimiento(LocalDate.parse(txtFecha.getText(), formatter));

        p.setLenguaje(cbLenguaje.getSelectionModel().getSelectedItem());
        p.setCarrera(checkCarrera.isSelected());

        repositorioPersona.modificar(p);
        actualizarTabla();
        limpiarCampos();
    }

    @FXML
    void onCsv(ActionEvent event){
        repositorioPersona.fromCsv(txtCsv.getText());
        actualizarTabla();
    }

    @FXML
    void onJson(ActionEvent event){
        repositorioPersona.fromJson(txtJson.getText());
        actualizarTabla();
    }

    @FXML
    void onXml(ActionEvent event){
        repositorioPersona.fromXml(txtXml.getText());
        actualizarTabla();
    }

    @FXML
    void onEscribirXml(ActionEvent event){
        repositorioPersona.writeXML(txtEscribirXml.getText());
        System.out.println("Archivo " + txtEscribirXml.getText() + " creado.");
    }

}
