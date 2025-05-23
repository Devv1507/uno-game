package univalle.tedesoft.uno.threads;

import javafx.application.Platform;
import univalle.tedesoft.uno.controller.GameController;
import univalle.tedesoft.uno.model.Players.MachinePlayer;

/**
 * Runnable encargado de que la maquina declare 'UNO' despues de un retraso.
 * Este hilo simula el tiempo de reaccion de la maquina, dando una oportunidad
 * al jugador humano para 'atrapar' a la maquina si no declara 'UNO' a tiempo.
 * La logica se ejecuta en el hilo de la aplicacion JavaFX para actualizar la UI.
 * @author David Esteban Valencia
 * @author Santiago David Guerrero
 * @author Juan Pablo Escamilla
 */

public class MachineDeclareUnoRunnable implements Runnable {
    /** Controlador del juego para interactuar con la lógica y la UI. */
    private final GameController gameController;
    /** Tiempo de espera en milisegundos antes de que la máquina intente declarar UNO. */
    private final long delayMs;

    /**
     * Constructor para MachineDeclareUnoRunnable.
     *
     * @param gameController La instancia del GameController para interactuar con la logica y la vista del juego.
     * @param delayMs  El tiempo en milisegundos que este hilo espera antes de intentar que la maquina declare UNO.
     */
    public MachineDeclareUnoRunnable(GameController gameController, long delayMs) {
        this.gameController = gameController;
        this.delayMs = delayMs;
    }

    /**
     * Ejecuta la logica para que la maquina declare 'UNO'.
     * Espera cierto tiempo y ejecuta la logica necesaria para declarar uno dadas las condiciones.
     */
    @Override
    public void run() {
        try {
            Thread.sleep(this.delayMs);

            // Verificar si el hilo fue interrumpido durante el sleep
            if (Thread.currentThread().isInterrupted()) {
                return; // Salir si fue interrumpido
            }

            Platform.runLater(() -> {
                // Solo ejecutar si el juego no ha sido interrumpido/terminado
                if (gameController == null || gameController.getGameState() == null || gameController.getGameState().isGameOver()) {
                    return;
                }

                MachinePlayer machinePlayer = gameController.getMachinePlayer(); // Asumiendo getter

                // Verificar si la máquina aún debe declarar UNO (no fue atrapada y sigue con 1 carta)
                if (machinePlayer.isUnoCandidate() && !machinePlayer.hasDeclaredUnoThisTurn()) {
                    gameController.getGameState().playerDeclaresUno(machinePlayer);
                    gameController.getGameView().displayMessage("¡Máquina dice UNO!"); // Asumiendo getter
                }
                // Siempre se procede a cerrar la ventana de oportunidad de UNO/castigo,
                this.gameController.proceedWithGameAfterMachineUnoWindow();
            });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("MachineDeclareUnoRunnable interrumpido.");
        }
    }
}
