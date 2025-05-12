package univalle.tedesoft.uno.threads;

import javafx.application.Platform;
import univalle.tedesoft.uno.controller.GameController;
import univalle.tedesoft.uno.model.Players.HumanPlayer;

import java.util.concurrent.TimeUnit;
public class HumanUnoTimerRunnable implements Runnable {
    private final GameController gameController;
    private final long timeoutSeconds;

    public HumanUnoTimerRunnable(GameController gameController, long timeoutSeconds) {
        this.gameController = gameController;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public void run() {
        try {
            TimeUnit.SECONDS.sleep(this.timeoutSeconds);

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
                    gameController.penalizeHumanForMissingUno(humanPlayer.getName() + " no dijo UNO a tiempo.");
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
