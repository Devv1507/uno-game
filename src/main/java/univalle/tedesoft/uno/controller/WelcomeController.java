package univalle.tedesoft.uno.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import univalle.tedesoft.uno.view.GameView;
import univalle.tedesoft.uno.view.WelcomeView;
import univalle.tedesoft.uno.view.InstructionsView;

import java.io.IOException;

/**
 * Clase encargada de mostrar la pantalla de inicio, y de brindar el label que
 * asignara un nombre al jugador
 * @author Juan Pablo Escamilla
 * @author David Esteban Valencia
 * @author Santiago Guerrero
 */

public class WelcomeController {
    /** Botón para confirmar el nombre ingresado y proceder al juego. */
    @FXML private Button enterNameButton;
    /** Botón para cerrar la aplicación. */
    @FXML private Button exitButton;
    /** Botón para mostrar la ventana de instrucciones del juego. */
    @FXML private Button instructionsButton;
    /** Campo de texto donde el usuario ingresa su nombre o nick. */
    @FXML private TextField nameTextField;
    /** ImageView para mostrar el logo del juego UNO. */
    @FXML private ImageView unoLogoImage;
    /** Label descriptivo para el campo de ingreso de nombre (ej. "Usuario"). */
    @FXML private Label userLabel;

    /** Referencia a la vista de bienvenida (WelcomeView) que este controlador maneja. */
    private WelcomeView welcomeView;

    /**
     * Inicialización de JavaFX.
     */
    @FXML
    public void initialize() {
        // Inicialmente deshabilitar el botón de ingresar
        enterNameButton.setDisable(true);

        // Agregar listener al TextField para habilitar/deshabilitar el botón
        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            enterNameButton.setDisable(newValue.trim().isEmpty());
        });

        // Configurar los manejadores de eventos
        exitButton.setOnAction(event -> handleExit());
        enterNameButton.setOnAction(event -> handleEnter());
        instructionsButton.setOnAction(event -> handleInstructions());
    }

    /**
     * Establece la referencia a la vista de bienvenida.
     * @param welcomeView La instancia de WelcomeView
     */
    public void setWelcomeView(WelcomeView welcomeView) {
        this.welcomeView = welcomeView;
    }

    /**
     * Obtiene el nombre ingresado por el usuario.
     * @return El nombre del usuario
     */
    public String getName() {
        return nameTextField.getText().trim();
    }

    /**
     * Maneja el evento del botón de salir.
     */
    private void handleExit() {
        Platform.exit();
    }

    /**
     * Maneja el evento del botón de ingresar.
     */
    private void handleEnter() {
        String playerName = getName();
        if (!playerName.isEmpty()) {
            try {
                // Obtener la instancia de GameView
                GameView gameView = GameView.getInstance();
                // Obtener el controlador y configurar el nombre del jugador
                GameController gameController = gameView.getGameController();
                gameController.setPlayerName(playerName);
                // Mostrar la vista del juego
                gameView.show();
                // Cerrar la vista de bienvenida
                welcomeView.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Maneja el evento del botón de instrucciones.
     */
    private void handleInstructions() {
        try {
            // Obtener la instancia de InstructionsView
            InstructionsView instructionsView = InstructionsView.getInstance();
            // Mostrar la vista de instrucciones
            instructionsView.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
