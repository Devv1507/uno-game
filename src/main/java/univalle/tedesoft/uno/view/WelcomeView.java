package univalle.tedesoft.uno.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import univalle.tedesoft.uno.Main;
import univalle.tedesoft.uno.controller.WelcomeController;

import java.io.IOException;

/**
 * Esta clase representa la vista del menú principal del juego de UNO.
 * Se encarga de mostrar las opciones del menú, tales como ingresar el nombre del usuario,
 * salir del juego o mostrar las instrucciones.
 *
 * @author Juan Pablo Escamilla
 * @author David Esteban Valencia
 */
public class WelcomeView extends Stage {
    private final WelcomeController controller;

    /**
     * Clase interna para implementar el patrón Singleton.
     */
    private static class WelcomeViewHolder {
        private static WelcomeView INSTANCE;
    }

    /**
     * Devuelve la instancia única de WelcomeView.
     * @return instancia singleton de WelcomeView
     * @throws IOException si ocurre un error al cargar el archivo FXML
     */
    public static WelcomeView getInstance() throws IOException {
        if (WelcomeViewHolder.INSTANCE == null) {
            WelcomeViewHolder.INSTANCE = new WelcomeView();
        }
        return WelcomeViewHolder.INSTANCE;
    }

    /**
     * Constructor privado que carga la vista desde el archivo FXML.
     * @throws IOException si falla la carga del FXML
     */
    private WelcomeView() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("welcome-view.fxml"));
        Scene scene = new Scene(loader.load());
        this.controller = loader.getController();

        if (this.controller == null) {
            throw new IOException("No se pudo obtener el WelcomeController desde el FXML");
        }

        // Establecer la referencia a esta vista en el controlador
        this.controller.setWelcomeView(this);

        this.setTitle("UNO! - Bienvenida");
        this.setScene(scene);
    }


}
