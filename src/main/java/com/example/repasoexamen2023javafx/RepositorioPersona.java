package com.example.repasoexamen2023javafx;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class RepositorioPersona {
    Connection conexion;
    public RepositorioPersona(Connection miConexion){
        this.conexion=miConexion;
        createTable();
    }

    public void createTable(){
        Statement stmt=null;
        try {
            stmt = conexion.createStatement();
            String CREATE_TABLE_SQL="CREATE TABLE IF NOT EXISTS personas (" +
                    "    id               INTEGER AUTO_INCREMENT,\n" +
                    "    nombre           VARCHAR (50),\n" +
                    "    edad             INTEGER,\n" +
                    "    fecha_nacimiento DATE,\n" +
                    "    lenguaje         VARCHAR(50),\n" +
                    "    carrera          BOOLEAN,\n" +
                    "    PRIMARY KEY (id)\n" +
                    ");";
            stmt.executeUpdate(CREATE_TABLE_SQL);
        }catch (SQLException sqlException){
            sqlException.printStackTrace();
        }
    }

    public ObservableList<Persona> leerTodosFX(){
        ObservableList<Persona> lista=null;
        try {
            PreparedStatement ps=conexion.prepareStatement("SELECT * FROM personas");
            ResultSet rs=ps.executeQuery();
            lista= FXCollections.observableArrayList();
            while(rs.next()){
                Persona aux=new Persona();
                aux.setId(rs.getInt("id"));
                aux.setNombre(rs.getString("nombre"));
                aux.setEdad(rs.getInt("edad"));
                aux.setFechaNacimiento(rs.getDate("fecha_nacimiento").toLocalDate());
                aux.setLenguaje(rs.getString("lenguaje"));
                aux.setCarrera(rs.getBoolean("carrera"));
                lista.add(aux);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;
    }

    public void inserta(Persona a){
        PreparedStatement aux = null;
        String sentenciaSql = "INSERT INTO personas (nombre, edad, fecha_nacimiento, lenguaje, carrera) VALUES (?, ?, ?, ?, ?)";
        try {
            aux = conexion.prepareStatement(sentenciaSql);
            aux.setString(1, a.getNombre());
            aux.setInt(2, a.getEdad());
            aux.setDate(3, Date.valueOf(a.getFechaNacimiento()));
            aux.setString(4, a.getLenguaje());
            aux.setBoolean(5, a.isCarrera());
            aux.executeUpdate();

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }


    public void modificar(Persona persona){
        PreparedStatement aux = null;

        String sentenciaSQL = "UPDATE personas SET nombre=?, edad=?, fecha_nacimiento=?, lenguaje=?, carrera=? WHERE id = ?";

        try {
            aux = conexion.prepareStatement(sentenciaSQL);
            aux.setString(1, persona.getNombre());
            aux.setInt(2, persona.getEdad());
            aux.setObject(3, persona.getFechaNacimiento());
            aux.setString(4, persona.getLenguaje());
            aux.setBoolean(5, persona.isCarrera());
            aux.setInt(6, persona.getId());
            aux.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ObservableList<Persona> buscarPersonaPorId (int id){
        ObservableList<Persona> lista=null;
        try {
            PreparedStatement ps=conexion.prepareStatement("SELECT * FROM personas WHERE id=?");
            ps.setInt(1, id);

            ResultSet rs=ps.executeQuery();
            lista= FXCollections.observableArrayList();
            while(rs.next()){
                Persona aux=new Persona();
                aux.setId(rs.getInt("id"));
                aux.setNombre(rs.getString("nombre"));
                aux.setEdad(rs.getInt("edad"));
                aux.setFechaNacimiento(rs.getDate("fecha").toLocalDate());
                aux.setLenguaje(rs.getString("lenguaje"));
                aux.setCarrera(rs.getBoolean("carrera"));
                lista.add(aux);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lista;
    }

    //método delete
    public void delete (int id){
        PreparedStatement aux = null;

        String sentenciaSQL = "DELETE FROM personas WHERE id = ?";

        try {
            aux = conexion.prepareStatement(sentenciaSQL);
            aux.setInt(1, id);
            aux.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void fromCsv(String rutaArchivoCsv){
        try {
            BufferedReader br = new BufferedReader(new FileReader(rutaArchivoCsv));
            String linea = br.readLine();

            //controlo que haya una primera línea de cabecera en el csv
            boolean primeraLinea = true;
            while (linea != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    linea = br.readLine();
                    continue;
                }

                String[] datos = linea.split(";");
                Persona persona = new Persona();
                persona.setNombre(datos[0]);
                persona.setEdad(Integer.parseInt(datos[1]));
                //persona.setFechaNacimiento(LocalDate.parse(datos[2]));
//formateo la fecha como sale en el csv
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                persona.setFechaNacimiento(LocalDate.parse(datos[2], formatter));

                persona.setLenguaje(datos[3]);
                persona.setCarrera(Boolean.parseBoolean(datos[4]));
                inserta(persona);
                linea = br.readLine();
            }
            br.close();
            System.out.println("Insertados datos del CSV a la bb.dd");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fromJson(String rutaArchivoJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            //estas dos líneas son para que pueda leer el formato de fecha que viene en el json
            JavaTimeModule module = new JavaTimeModule();
            mapper.registerModule(module);

            List<Persona> listaJson = mapper.readValue(new File(rutaArchivoJson), new com.fasterxml.jackson.core.type.TypeReference<List<Persona>>(){});

            for (Persona persona : listaJson) {
                inserta(persona);
            }
            System.out.println("Insertados datos del Json a la bb.dd");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void fromXml(String rutaArchivoXml) {

        //PARA IMPRIMIRLO POR PANTALLA ES IGUAL PERO HACIENDO SOUT EN CADA LÍNEA Y QUITANDO EL inserta(persona)

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document documento = builder.parse(new File(rutaArchivoXml));

            NodeList personas = documento.getElementsByTagName("persona");
            for (int i = 0; i < personas.getLength(); i++) {
                Node personaNode = personas.item(i);
                if (personaNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element personaElement = (Element) personaNode;

                    Persona persona = new Persona();
                    persona.setNombre(personaElement.getElementsByTagName("nombre").item(0).getTextContent());
                    persona.setEdad(Integer.parseInt(personaElement.getElementsByTagName("edad").item(0).getTextContent()));

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                    persona.setFechaNacimiento(LocalDate.parse(personaElement.getElementsByTagName("fechaNacimiento").item(0).getTextContent(), formatter));

                    persona.setLenguaje(personaElement.getElementsByTagName("lenguaje").item(0).getTextContent());
                    persona.setCarrera(Boolean.parseBoolean(personaElement.getElementsByTagName("carrera").item(0).getTextContent()));

                    inserta(persona);
                }
            }
            System.out.println("Insertados datos del XML a la bb.dd");
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void writeXML(String rutaArchivo){
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document documento = null;

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation dom = builder.getDOMImplementation();
            documento = dom.createDocument(null,  "xml", null);

            Element raiz = documento.createElement("Personas");
            documento.getDocumentElement().appendChild(raiz);

            Element nodoProducto = null, nodoDatos = null;
            Text texto = null;

            for (Persona persona : leerTodosFX()) {

                nodoProducto = documento.createElement("Persona");
                raiz.appendChild(nodoProducto);

                nodoDatos = documento.createElement("id");
                nodoProducto.appendChild(nodoDatos);

                texto = documento.createTextNode(String.valueOf(persona.getId()));
                nodoDatos.appendChild(texto);

                nodoDatos = documento.createElement("nombre");
                nodoProducto.appendChild(nodoDatos);

                texto = documento.createTextNode(persona.getNombre());
                nodoDatos.appendChild(texto);

                nodoDatos = documento.createElement("edad");
                nodoProducto.appendChild(nodoDatos);

                texto = documento.createTextNode(String.valueOf(persona.getEdad()));
                nodoDatos.appendChild(texto);

                nodoDatos = documento.createElement("fecha_nacimiento");
                nodoProducto.appendChild(nodoDatos);

                texto = documento.createTextNode(String.valueOf(persona.getFechaNacimiento()));
                nodoDatos.appendChild(texto);

                nodoDatos = documento.createElement("leanguaje");
                nodoProducto.appendChild(nodoDatos);

                texto = documento.createTextNode(persona.getLenguaje());
                nodoDatos.appendChild(texto);

                nodoDatos = documento.createElement("carrera");
                nodoProducto.appendChild(nodoDatos);

                texto = documento.createTextNode(String.valueOf(persona.isCarrera()));
                nodoDatos.appendChild(texto);

            }

            Source source = new DOMSource(documento);
            Result resultado = new StreamResult(new File(rutaArchivo));

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(source, resultado);

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerConfigurationException tce) {
            tce.printStackTrace();
        } catch (TransformerException te) {
            te.printStackTrace();
        }
    }

    public void writeCsv(String rutaArchivo) {
        // ESCRIBIR ARCHIVO CSV ESCRIBIR CSV HACER CSV  DESDE ARRAY CON VALORES DE PERSONA
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivo));
            for (Persona persona : leerTodosFX()) {
                writer.println(persona.getNombre() + "," +
                        persona.getEdad() + "," +
                        persona.getFechaNacimiento() + "," +
                        persona.getLenguaje() + "," +
                        persona.isCarrera());
            }
            writer.close();
            System.out.println("Datos escritos en el archivo CSV.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void printCsv() {
        // IMPRIMIR CSV POR PANTALLA  DESDE ARRAY CON VALORES DE PERSONA
        for (Persona persona : leerTodosFX()) {
            System.out.println(persona.getNombre() + "," +
                    persona.getEdad() + "," +
                    persona.getFechaNacimiento() + "," +
                    persona.getLenguaje() + "," +
                    persona.isCarrera());
        }
    }

    public void writeJson(String rutaArchivoJson) {
        // ESCRIBIR ARCHIVO JSON ESCRIBIR JSON DESDE ARRAY CON VALORES DE PERSONA
        try {
            ObjectMapper mapper = new ObjectMapper();
            JavaTimeModule module = new JavaTimeModule();
            mapper.registerModule(module);

            String json = mapper.writeValueAsString(leerTodosFX());

            PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivoJson));
            writer.write(json);
            writer.close();

            System.out.println("Datos escritos en el archivo JSON.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void printJson() {
        // IMPRIMIR JSON EN CONSOLA  DESDE ARRAY CON VALORES DE PERSONA
        try {
            ObjectMapper mapper = new ObjectMapper();
            JavaTimeModule module = new JavaTimeModule();
            mapper.registerModule(module);

            String json = mapper.writeValueAsString(leerTodosFX());

            System.out.println(json);

            System.out.println("Datos impresos en la consola.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
