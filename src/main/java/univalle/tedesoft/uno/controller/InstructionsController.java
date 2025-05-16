package univalle.tedesoft.uno.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import univalle.tedesoft.uno.view.InstructionsView;
import univalle.tedesoft.uno.view.WelcomeView;

import java.io.IOException;

/**
 * Controlador para la vista de instrucciones del juego UNO.
 * Maneja la interacción con la interfaz de instrucciones.
 *
 * @author Juan Pablo Escamilla
 * @author David Esteban Valencia
 * @author Santiago Guerrero
 */
public class InstructionsController {
    @FXML
    private Button backButton;
    
    private InstructionsView instructionsView;

    /**
     * Establece la referencia a la vista de instrucciones.
     * @param view La instancia de InstructionsView
     */
    public void setInstructionsView(InstructionsView view) {
        this.instructionsView = view;
    }

    /**
     * Maneja el evento del botón "Volver al Menú".
     * Cierra la ventana de instrucciones y muestra la ventana de bienvenida.
     */
    @FXML
    private void handleBackButton() {
        try {
            // Cerrar la ventana de instrucciones
            this.instructionsView.close();
            
            // Mostrar la ventana de bienvenida
            WelcomeView.getInstance().show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 