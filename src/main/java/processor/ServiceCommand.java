package processor;

import java.util.Map;

import service.ServiceManager;
import service.ServiceState;
import utils.StringUtils;
import agent.Agent;

public class ServiceCommand extends CommandBase implements Command {

	
	public ServiceCommand() {
		this.serviceManager = Agent.get().getServiceManager();
	}

	private final ServiceManager serviceManager;

	@Override
	public String execute(String request) {

		String[] params = parameters(request);
		
		// get all states
		if (params.length == 0) {
			Map<String, ServiceState> allStates = serviceManager.getAllStates();
			StringBuffer sb = new StringBuffer();
			for (String serviceName : allStates.keySet()) {
				ServiceState serviceState = allStates.get(serviceName);
				sb.append(serviceName).append(": ").append(serviceState.toString()).append("\r\n");
			}
			sb.append("\r\nHelp: ").append(help());
			return sb.toString();
		}
		
		if (params.length < 2 || StringUtils.nullOrEmpty(params[0]) || StringUtils.nullOrEmpty(params[1])) {
			return syntaxError();
		}

		String serviceName = params[0].toLowerCase();
		String action = params[1].toLowerCase();

		if (!serviceManager.isAvailable(serviceName)) {
			return "Service " + serviceName + " is not available";
		}

		if (!serviceManager.isAllowedAction(serviceName, action)) {
			return "Unable to " + action + " " + serviceName;
		}

		ServiceState status = serviceManager.executeAction(serviceName, action);

		return serviceName + ": " + status.toString();

	}

	@Override
	public String help() {
		return "service name status|start|stop|restart";
	}

	@Override
	public String name() {
		return "service";
	}

}
