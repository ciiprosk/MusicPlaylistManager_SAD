package it.diem.unisa.musicmanager.command;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Gestisce l'esecuzione dei comandi e la loro storicizzazione
 * per supportare funzionalità di undo.
 * Il CommandManager funge da "invoker" nel Command Pattern:
 * esegue i comandi, ne controlla il risultato e li memorizza
 * in una pila (stack) per poter annullare le operazioni.
 */
public class CommandManager {
    /**
     * Stack dei comandi eseguiti con successo.
     * Viene utilizzato per supportare l'operazione di undo.
     * L'ultimo comando eseguito è il primo ad essere annullato (LIFO).
     */
    private final Deque<Command> undoStack = new ArrayDeque<>();


    /**
     * Esegue un comando e, se l'esecuzione ha successo,
     * lo memorizza nello stack per il possibile undo.
     * Un comando viene considerato riuscito se execute()
     * restituisce Optional.empty()}.
     * In caso contrario, il comando NON viene salvato nello stack.
     * @param command il comando da eseguire
     * @return Optional vuoto se l'esecuzione ha successo,
     * oppure un Optional contenente il messaggio di errore
     */
    public Optional<String> executeCommand(Command command) {
        Optional<String> error = command.execute();
        if (error.isEmpty()) {       // metto sullo stack SOLO se è andata a buon fine
            undoStack.push(command);
        }
        return error;
    }

    /**
     * Annulla l'ultimo comando eseguito con successo.
     * Se non ci sono comandi nello stack, il metodo non fa nulla.
     * L'undo viene eseguito secondo logica LIFO (ultimo entrato, primo uscito).
     */
    public void undo() {
        if (!undoStack.isEmpty()) {
            undoStack.pop().undo();
        }
    }

    /**
     * Verifica se è possibile effettuare un'operazione di undo.
     * @return true se ci sono comandi da annullare,
     * false altrimenti
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public Optional<String> peekUndoDescription() {
        return undoStack.isEmpty()
                ? Optional.empty()
                : Optional.of(undoStack.peek().getDescription());
    }
}
