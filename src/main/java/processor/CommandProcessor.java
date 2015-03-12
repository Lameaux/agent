package processor;

import java.util.ArrayList;
import java.util.List;

public class CommandProcessor {

	List<Command> commands = new ArrayList<Command>();

	public CommandProcessor() {
		commands.add(new HelpCommand(commands));
		commands.add(new TimeCommand());
		commands.add(new UptimeCommand());
		commands.add(new ShellCommand());
		commands.add(new VersionCommand());
		commands.add(new ConfigCommand());		
		commands.add(new SysInfoCommand());		
		commands.add(new HostnameCommand());
		commands.add(new ServiceCommand());		
	}

	public String process(String request) {
		for (Command command : commands) {
			if (command.match(request)) {
				return command.execute(request);
			}
		}
		return "Invalid command";
	}

}
