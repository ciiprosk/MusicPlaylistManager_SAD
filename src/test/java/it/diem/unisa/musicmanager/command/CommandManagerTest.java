package it.diem.unisa.musicmanager.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class CommandManagerTest {

    private CommandManager commandManager;

    @BeforeEach
    void setUp() {
        commandManager = new CommandManager();
    }

    @Test
    void testExecuteSuccessfulCommandShouldPushToStack() {
        Command successCmd = new Command() {
            @Override
            public Optional<String> execute() { return Optional.empty(); }
            @Override
            public void undo() {}
            @Override
            public String getDescription() { return "Success Command"; }
        };

        Optional<String> result = commandManager.executeCommand(successCmd);
        
        assertTrue(result.isEmpty());
        assertTrue(commandManager.canUndo());
        assertEquals("Success Command", commandManager.peekUndoDescription().orElse(""));
    }

    @Test
    void testExecuteFailedCommandShouldNotPushToStack() {
        Command failCmd = new Command() {
            @Override
            public Optional<String> execute() { return Optional.of("Error occurred"); }
            @Override
            public void undo() {}
            @Override
            public String getDescription() { return "Fail Command"; }
        };

        Optional<String> result = commandManager.executeCommand(failCmd);
        
        assertTrue(result.isPresent());
        assertEquals("Error occurred", result.get());
        assertFalse(commandManager.canUndo());
    }

    @Test
    void testUndoShouldInvokeCommandUndoAndPop() {
        final boolean[] undoCalled = {false};
        Command cmd = new Command() {
            @Override
            public Optional<String> execute() { return Optional.empty(); }
            @Override
            public void undo() { undoCalled[0] = true; }
            @Override
            public String getDescription() { return "Undo Command"; }
        };

        commandManager.executeCommand(cmd);
        commandManager.undo();

        assertTrue(undoCalled[0]);
        assertFalse(commandManager.canUndo());
    }
}
