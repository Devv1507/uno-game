package univalle.tedesoft.uno.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import univalle.tedesoft.uno.Main;
import univalle.tedesoft.uno.controller.InstructionsController;

import java.io.IOException;

/**
 * Esta clase representa la vista de instrucciones del juego UNO.
 * Muestra las reglas básicas del juego en un formato legible.
 *
 * @author Juan Pablo Escamilla
 * @author David Esteban Valencia
 * @author Santiago Guerrero
 */
public class InstructionsView extends Stage {
    private final InstructionsController controller;

    /**
     * Clase interna para implementar el patrón Singleton.
     */
    private static class InstructionsViewHolder {
        private static InstructionsView INSTANCE;
    }

    /**
     * Devuelve la instancia única de InstructionsView.
     * @return instancia singleton de InstructionsView
     * @throws IOException si ocurre un error al cargar el archivo FXML
     */
    public static InstructionsView getInstance() throws IOException {
        if (InstructionsViewHolder.INSTANCE == null) {
            InstructionsViewHolder.INSTANCE = new InstructionsView();
        }
        return InstructionsViewHolder.INSTANCE;
    }

    /**
     * Constructor privado que carga la vista desde el archivo FXML.
     * @throws IOException si falla la carga del FXML
     */
    private InstructionsView() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("instructions-view.fxml"));
        Scene scene = new Scene(loader.load());
        this.controller = loader.getController();

        if (this.controller == null) {
            throw new IOException("No se pudo obtener el InstructionsController desde el FXML");
        }

        // Establecer la referencia a esta vista en el controlador
        this.controller.setInstructionsView(this);

        this.setTitle("UNO! - Instrucciones");
        this.setScene(scene);
    }
} 