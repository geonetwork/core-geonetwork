package jeeves.server.context;

public class ServiceExecutionFailedException extends Exception {
    private static final long serialVersionUID = -2539405391224145743L;

	public ServiceExecutionFailedException(String service, Exception e) {
	    super("Failed to execute "+service+" correctly",e);
    }
}
