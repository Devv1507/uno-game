package univalle.tedesoft.uno.threads;

import javafx.application.Platform;
import univalle.tedesoft.uno.controller.GameController;
import univalle.tedesoft.uno.model.Players.HumanPlayer;

import java.util.concurrent.TimeUnit;

/**
 * Runnable que implementa un temporizador para la ventana de oportunidad
 * del jugador humano para declarar 'UNO'. Si el temporizador expira antes
 * de que el jugador declare 'UNO' (presionando el boton correspondiente),
 * se aplica una penalizacion al jugador humano. Las interacciones con
 * la logica del juego y la UI se realizan en el hilo de JavaFX.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla

 */
public class HumanUnoTimerRunnable implements Runnable {
    private final GameController gameController;
    private final long timeoutMilliseconds;
/**
 * Constructor para HumanUnoTimerRunnable.
 * @param gameController La instancia del GameController para interactuar con el estado del juego y la interfaz de usuario.
 * @param timeoutMilliseconds El tiempo en milisegundos que el jugador humano tiene para declarar 'UNO' antes de ser penalizado.
 */
    public HumanUnoTimerRunnable(GameController gameController, long timeoutMilliseconds) {
        this.gameController = gameController;
        this.timeoutMilliseconds = timeoutMilliseconds;
    }
    /**
     * Ejecuta la logica del temporizador para la declaracion de 'UNO' por parte del jugador humano.
     */
    @Override
    public void run() {
        try {
            TimeUnit.MILLISECONDS.sleep(this.timeoutMilliseconds);

            // Verificar si el hilo fue interrumpido durante el sleep
            if (Thread.currentThread().isInterrupted()) {
                return; // Salir si fue interrumpido
            }

            Platform.runLater(() -> {
                // Solo ejecutar si el juego no ha sido interrumpido/terminado
                if (gameController == null || gameController.getGameState() == null || gameController.getGameState().isGameOver()) {
                    return;
                }

                HumanPlayer humanPlayer = gameController.getHumanPlayer(); // Asumiendo que existe este getter

                // Solo penalizar y avanzar si el timer realmente expiró
                // y el jugador aún es candidato y no ha declarado UNO.
                if (humanPlayer.isUnoCandidate() && !humanPlayer.hasDeclaredUnoThisTurn()) {
                    // gameController.penalizeHumanForMissingUno ya maneja el mensaje, la penalización y la actualización de UI.
                    // Pero necesitamos pasarle el mensaje específico.
                    gameController.penalizeHumanForMissingUno("Uh, " + humanPlayer.getName() + " no cantaste UNO a tiempo :l");
                    humanPlayer.resetUnoStatus(); // Ya se hace en penalizeHumanForMissingUno, pero por si acaso.

                    if (!gameController.getIsChoosingColor()) { // Asumiendo getter isChoosingColor()
                        gameController.processTurnAdvancement();
                    }
                }
            });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("HumanUnoTimerRunnable interrumpido.");
        }
    }
}
