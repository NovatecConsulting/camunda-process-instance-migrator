package info.novatec.camunda.migrator;


public class ProcessInstanceMigratorConfigurationException extends RuntimeException {

    private static final long serialVersionUID = 3010064810292611192L;
    private static final String message = "Process Engine needs to be configured for ProcessInstanceMigrator";

    public ProcessInstanceMigratorConfigurationException() {
        super(message);
    }

}
