package cloud.timo.TimoCloud.core.commands;

import org.jline.builtins.Builtins;
import org.jline.builtins.CommandRegistry;
import org.jline.builtins.Completers;
import org.jline.builtins.Widgets;
import org.jline.utils.AttributedString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TimoCloudCommandRegistry implements CommandRegistry {
    private final Map<String, Builtins.CommandMethods> commands;
    private final Map<String, List<String>> commandInfos;
    private final Map<String, String> aliases;

    public TimoCloudCommandRegistry() {
        commands = new ConcurrentHashMap<>();
        commandInfos = new ConcurrentHashMap<>();
        aliases = new ConcurrentHashMap<>();
    }

    public void addCommand(Command command) {
        commands.put(command.getName(), new Builtins.CommandMethods(command::prepareCommand, command::complete));

        commandInfos.put(command.getName(), command.getDescription());
        command.getAliases().forEach(s -> aliases.put(s, command.getName()));
    }

    @Override
    public Set<String> commandNames() {
        return commands.keySet();
    }

    @Override
    public Map<String, String> commandAliases() {
        return aliases;
    }

    @Override
    public List<String> commandInfo(String s) {
        return commandInfos.get(s);
    }

    @Override
    public boolean hasCommand(String command) {
        return (commands.containsKey(command) || aliases.containsKey(command));
    }

    public String getCommandName(String alias) {
        if (commands.containsKey(alias)) return alias;
        if (aliases.containsKey(alias)) return aliases.get(alias);

        return null;
    }

    @Override
    public Completers.SystemCompleter compileCompleters() {
        Completers.SystemCompleter out = new Completers.SystemCompleter();
        for (Map.Entry<String, Builtins.CommandMethods> entry : commands.entrySet()) {
            out.add(entry.getKey(), entry.getValue().compileCompleter().apply(entry.getKey()));
        }
        return out;
    }

    public void execute(String command, String[] args) {
        commands.get(command).execute().accept(new Builtins.CommandInput(args));
    }

    @Override
    public Widgets.CmdDesc commandDescription(String command) {
        List<AttributedString> mainDesc = new ArrayList<>();
        commandInfos.get(command).forEach(string -> mainDesc.add(new AttributedString(string)));
        return new Widgets.CmdDesc().mainDesc(mainDesc);
    }
}
