package univalle.tedesoft.uno;

import javafx.application.Application;
import javafx.stage.Stage;
import univalle.tedesoft.uno.view.GameView;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        GameView welcomeView = GameView.getInstance();
        welcomeView.show();
    }

    public static void main(String[] args) {
        launch();
    }
}