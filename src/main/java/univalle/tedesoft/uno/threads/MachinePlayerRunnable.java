package univalle.tedesoft.uno.threads;

import javafx.application.Platform;
import univalle.tedesoft.uno.controller.GameController;

/**
 * Runnable encargado de gestionar el turno de la máquina en un hilo separado.
 * Incluye un retraso para simular el "pensamiento" de la máquina antes de ejecutar su lógica de juego.
 *  @author Juan Pablo Escamilla
 *  @author David Esteban Valencia
 *  @author Santiago David Guerrero
 */
public class MachinePlayerRunnable implements Runnable {
    private final GameController gameController;
    private final long thinkDelayMs;

    /**
     * Constructor para MachinePlayerRunnable.
     * @param gameController La instancia del controlador del juego.
     * @param thinkDelayMs El tiempo en milisegundos que la máquina "pensará" antes de actuar.
     */
    public MachinePlayerRunnable(GameController gameController, long thinkDelayMs) {
        this.gameController = gameController;
        this.thinkDelayMs = thinkDelayMs;
    }

    /**
     * Simula el pensamiento de la máquina y luego ejecuta su lógica de turno
     * en el hilo de la interfaz de usuario de JavaFX.
     */
    @Override
    public void run() {
        try {
            Thread.sleep(this.thinkDelayMs);
            // Una vez finalizado el pensamiento, la lógica del turno de la máquina
            // debe ejecutarse en el hilo de la aplicación JavaFX
            Platform.runLater(() -> {
                // Solo ejecutar si el juego no ha sido interrumpido
                // y sigue siendo el turno de la máquina.
                if (!this.gameController.getGameState().isGameOver() &&
                        this.gameController.getCurrentPlayer() == this.gameController.getMachinePlayer()) {
                    this.gameController.executeMachineTurnLogic();
                }
            });

        } catch (InterruptedException e) {
            // Si el hilo es interrumpido, se termina la ejecución.
            Thread.currentThread().interrupt();
        }
    }
}