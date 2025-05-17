package univalle.tedesoft.uno;

import javafx.application.Application;
import javafx.stage.Stage;
import univalle.tedesoft.uno.view.WelcomeView;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        WelcomeView welcomeView = WelcomeView.getInstance();
        welcomeView.show();
    }

    public static void main(String[] args) {
        launch();
    }
}