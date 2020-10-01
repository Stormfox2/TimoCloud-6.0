package cloud.timo.TimoCloud.core.managers;

import cloud.timo.TimoCloud.core.TimoCloudCore;
import cloud.timo.TimoCloud.core.commands.Command;
import cloud.timo.TimoCloud.core.commands.TimoCloudCommandRegistry;
import cloud.timo.TimoCloud.core.commands.defaults.AddBaseCommand;
import org.jline.builtins.Builtins;
import org.jline.builtins.Completers;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CommandManager {
    private final Map<String, Command> commandList;

    private final TimoCloudCommandRegistry commandRegistry;
    private final ScheduledThreadPoolExecutor scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    private org.jline.reader.LineReader lineReader;
    private Terminal terminal = null;
    private Completers.Completer completer;

    public CommandManager() {
        commandList = new HashMap<>();
        commandRegistry = new TimoCloudCommandRegistry();
    }

    public void load() {
        addBasicCommands();
        startThread();
    }

    public void addCommand(Command command) {
        commandList.put(command.getName(), command);
        commandRegistry.addCommand(command);
    }

    public boolean reload() {
        Builtins builtins = new Builtins(Paths.get(""), null, null);
        builtins.rename(Builtins.Command.TTOP, "top");

        AggregateCompleter systemCompleter = new AggregateCompleter(CommandRegistry.compileCompleters(/*builtins,*/ commandRegistry)
                , completer != null ? completer : NullCompleter.INSTANCE);

        TerminalBuilder builder = TerminalBuilder.builder();
        try {
            terminal = builder.build();
        } catch (IOException e) {
            TimoCloudCore.getInstance().getLogger().severe(e.getMessage());
            TimoCloudCore.getInstance().getLogger().severe(e.getCause().toString());
            return false;
        }

        Parser mainParser = new DefaultParser();


        lineReader = LineReaderBuilder.builder().terminal(terminal)
                .completer(systemCompleter)
                .parser(mainParser)
                .variable(org.jline.reader.LineReader.SECONDARY_PROMPT_PATTERN, "%M%P > ")
                .variable(org.jline.reader.LineReader.INDENTATION, 2)
                .option(org.jline.reader.LineReader.Option.INSERT_BRACKET, true)
                .option(org.jline.reader.LineReader.Option.EMPTY_WORD_OPTIONS, false)
                .build();
        return true;
    }

    public CommandManager removeCommand(Command command) {
        commandList.remove(command.getName());
        return this;
    }

    private void addBasicCommands() {
        addCommand(new AddBaseCommand());
    }

    private void startThread() {
        String prompt = "> ";
        reload();

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            String line = lineReader.readLine(prompt);
            line = line.trim();

            ParsedLine pl = lineReader.getParser().parse(line, 0);
            String[] argv = pl.words().subList(1, pl.words().size()).toArray(new String[0]);
            String cmd = Parser.getCommand(pl.word());

            if (commandRegistry.hasCommand(cmd))
                commandRegistry.execute(cmd, argv);
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void printLog(String string) {
        if (lineReader != null && lineReader.isReading()) {
            //Redraw prompt
            lineReader.callWidget(org.jline.reader.LineReader.CLEAR);
            terminal.writer().print(string);
            lineReader.callWidget(org.jline.reader.LineReader.REDRAW_LINE);
            lineReader.callWidget(LineReader.REDISPLAY);
            lineReader.getTerminal().flush();
        } else {
            // No need to redraw prompt
            terminal.writer().println(string);
        }

        terminal.writer().flush();
        terminal.flush();


    }

    public TimoCloudCommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public Command getCommand(String name) {
        return commandList.get(name);
    }

    public Map<String, Command> getCommands() {
        return commandList;
    }

    public void stop() {
        scheduledExecutorService.shutdownNow();
    }


}
