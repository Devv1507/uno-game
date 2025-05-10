package univalle.tedesoft.uno.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import univalle.tedesoft.uno.view.GameView;
import univalle.tedesoft.uno.view.WelcomeView;

import java.io.IOException;

public class WelcomeController {
    @FXML private Button enterNameButton;
    @FXML private Button exitButton;
    @FXML private Button instructionsButton;
    @FXML private TextField nameTextField;
    @FXML private ImageView unoLogoImage;
    @FXML private Label userLabel;

    private WelcomeView welcomeView;

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

    public String setName() {
        return userLabel.getText();
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
        // TODO: Implementar la lógica para mostrar las instrucciones
        System.out.println("Mostrar instrucciones");
    }
}
