package info.novatec.bpm.camunda.migrator;

import static info.novatec.bpm.camunda.migrator.assertions.ProcessInstanceListAsserter.assertThat;
import static info.novatec.bpm.camunda.migrator.assertions.TaskListAsserter.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.impl.migration.MigrationInstructionImpl;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class ProcessInstanceMigratorTest {

    @ClassRule
    public static ProcessEngineRule rule = new ProcessEngineRule();

    protected static final String MIGRATEABLE_PROCESS_MODEL_PATH = "test-processmodels/migrateable_processmodel_1_0_0.bpmn";
    private static final String PATCHED_PROCESS_MODEL_PATH = "test-processmodels/migrateable_processmodel_1_0_1_with_formkeys.bpmn";
    private static final String MINOR_INCREASED_PROCESS_MODEL_PATH = "test-processmodels/migrateable_processmodel_1_5_0.bpmn";
    private static final String MINOR_INCREASED_AND_PATCHED_PROCESS_MODEL_PATH = "test-processmodels/migrateable_processmodel_1_5_1.bpmn";
    private static final String MAJOR_INCREASED_PROCESS_MODEL_PATH = "test-processmodels/migrateable_processmodel_2_0_0.bpmn";

    private static final String PROCESS_DEFINITION_KEY = "MigrateableProcess";

    private ProcessInstanceMigrator processInstanceMigrator = new ProcessInstanceMigrator(rule.getProcessEngine(), new MigrationInstructions.Builder().build());

    private ProcessDefinition initialProcessDefinition;
    private ProcessDefinition newestProcessDefinitionAfterRedeployment;
    private ProcessInstance processInstance1;
    private ProcessInstance processInstance2;

    @Before
    public void setUp() {
        deployBPMNFromClasspathResource(MIGRATEABLE_PROCESS_MODEL_PATH);
        // this will refer to the initial process Model
        initialProcessDefinition = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(initialProcessDefinition.getVersionTag()).isEqualTo("1.0.0");

        processInstance1 = startProcessInstance(PROCESS_DEFINITION_KEY);
        processInstance2 = startProcessInstance(PROCESS_DEFINITION_KEY);
    }

    @After
    public void cleanUp() {
        rule.getRuntimeService().deleteProcessInstance(processInstance1.getId(), "noReason");
        rule.getRuntimeService().deleteProcessInstance(processInstance2.getId(), "noReason");

        rule.getRepositoryService()
            .createDeploymentQuery()
            .list()
            .forEach(
                deployment -> rule.getRepositoryService().deleteDeployment(deployment.getId()));
    }

    @Test
    public void processInstanceMigrator_should_migrate_all_process_instances_to_higher_patch() {
        deployBPMNFromClasspathResource(PATCHED_PROCESS_MODEL_PATH);
        newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.0.1");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveFormkey(null);

        processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
            .allTasksHaveFormkey("Formkey1");
    }

    @Test
    public void processInstanceMigrator_should_migrate_suspended_process_instances() {
        suspendProcessInstance(processInstance1);
        suspendProcessInstance(processInstance2);

        deployBPMNFromClasspathResource(PATCHED_PROCESS_MODEL_PATH);
        newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.0.1");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId())
            .allProcessInstancesAreSuspended();

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveFormkey(null);

        processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
            .allProcessInstancesAreSuspended();

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
            .allTasksHaveFormkey("Formkey1");
    }

    @Test
    public void processInstanceMigrator_should_migrate_from_suspended_process_definitions() {
        suspendProcessDefinition(initialProcessDefinition);

        deployBPMNFromClasspathResource(PATCHED_PROCESS_MODEL_PATH);
        newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.0.1");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveFormkey(null);

        processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
            .allTasksHaveFormkey("Formkey1");
    }

    @Test
    public void processInstanceMigrator_should_not_migrate_to_suspended_process_definitions() {
        deployBPMNFromClasspathResource(PATCHED_PROCESS_MODEL_PATH);
        newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.0.1");

        suspendProcessDefinition(newestProcessDefinitionAfterRedeployment);

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveFormkey(null);

        processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveFormkey(null);
    }

    @Test
    public void processInstanceMigrator_should_not_migrate_to_higher_minor_version_if_no_migration_plan_was_provided() {
        deployBPMNFromClasspathResource(MINOR_INCREASED_PROCESS_MODEL_PATH);
        newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.5.0");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveFormkey(null);

        processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveFormkey(null);
    }

    @Test
    public void processInstanceMigrator_should_migrate_to_higher_minor_version_if_migration_plan_was_provided() {
        deployBPMNFromClasspathResource(MINOR_INCREASED_PROCESS_MODEL_PATH);
        newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.5.0");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveKey("UserTask1")
            .allTasksHaveFormkey(null);

		processInstanceMigrator = new ProcessInstanceMigrator(rule.getProcessEngine(),
				new MigrationInstructions.Builder()
						.putInstructions(PROCESS_DEFINITION_KEY, generateMigrationInstructionsFor100To150())
						.build());
        processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
            .allTasksHaveKey("UserTask2")
            .allTasksHaveFormkey("Formkey2");
    }
    
    @Test
    public void processInstanceMigrator_should_not_migrate_if_migration_to_higher_minor_version_has_faulty_migration_instructions() {
        deployBPMNFromClasspathResource(MINOR_INCREASED_PROCESS_MODEL_PATH);
        newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.5.0");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveKey("UserTask1")
            .allTasksHaveFormkey(null);

		processInstanceMigrator = new ProcessInstanceMigrator(rule.getProcessEngine(),
				new MigrationInstructions.Builder()
						.putInstructions(PROCESS_DEFINITION_KEY, generateFaultyMigrationInstructionsFor100To150())
						.build());
        processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
	        .numberOfProcessInstancesIs(2)
	        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
	        .numberOfTasksIs(2)
	        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
	        .allTasksHaveKey("UserTask1")
	        .allTasksHaveFormkey(null);
    }

    @Test
    public void processInstanceMigrator_should_migrate_to_higher_minor_by_adding_up_migration_instructions() {
        deployBPMNFromClasspathResource(MINOR_INCREASED_PROCESS_MODEL_PATH);
        newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.5.0");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveKey("UserTask1")
            .allTasksHaveFormkey(null);

        processInstanceMigrator = new ProcessInstanceMigrator(rule.getProcessEngine(),
				new MigrationInstructions.Builder()
						.putInstructions(PROCESS_DEFINITION_KEY, generateMigrationInstructionFor100To130())
						.putInstructions(PROCESS_DEFINITION_KEY, generateMigrationInstructionFor130To150())
						.build());
        processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
            .allTasksHaveKey("UserTask2")
            .allTasksHaveFormkey("Formkey2");
    }
    
    @Test
    public void processInstanceMigrator_should_migrate_to_higher_minor_and_patch_version_using_only_minor_migration_plan() {
    	deployBPMNFromClasspathResource(MINOR_INCREASED_AND_PATCHED_PROCESS_MODEL_PATH);
    	newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.5.1");
        
        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
	        .numberOfProcessInstancesIs(2)
	        .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());
        
        complete(task(processInstance1));
	
	    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
	        .numberOfTasksIs(2)
	        .allTasksHaveDefinitionId(initialProcessDefinition.getId())
	        .oneTaskHasKey("UserTask1")
	        .oneTaskHasKey("UserTask2")
	        .allTasksHaveFormkey(null);
	
		processInstanceMigrator = new ProcessInstanceMigrator(rule.getProcessEngine(),
				new MigrationInstructions.Builder()
						.putInstructions(PROCESS_DEFINITION_KEY, generateMigrationInstructionsFor100To150())
						.build());
	    processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);
	
	    assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
	        .numberOfProcessInstancesIs(2)
	        .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());
	
	    assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
	        .numberOfTasksIs(2)
	        .allTasksHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
	        .allTasksHaveKey("UserTask2")
	        .allTasksHaveFormkey("Formkey2");
    }

    @Test
    public void processInstanceMigrator_should_not_migrate_to_higher_major_version() {
        deployBPMNFromClasspathResource(MAJOR_INCREASED_PROCESS_MODEL_PATH);
        newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("2.0.0");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveFormkey(null);

        processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveFormkey(null);
    }

    private List<MinorMigrationInstructions> generateMigrationInstructionsFor100To150() {
        return Arrays.asList(MinorMigrationInstructions.builder()
        		.sourceMinorVersion(0)
        		.targetMinorVersion(5)
        		.migrationInstructions(Arrays.asList(new MigrationInstructionImpl("UserTask1", "UserTask2")))
        		.majorVersion(1)
        		.build());
    }
    
    private List<MinorMigrationInstructions> generateFaultyMigrationInstructionsFor100To150() {
        return Arrays.asList(MinorMigrationInstructions.builder()
        		.sourceMinorVersion(0)
        		.targetMinorVersion(5)
        		.migrationInstructions(Arrays.asList(new MigrationInstructionImpl("UserTask1", "UserTask6")))
        		.majorVersion(1)
        		.build());
    }

    private List<MinorMigrationInstructions> generateMigrationInstructionFor100To130() {
        return Arrays.asList(MinorMigrationInstructions.builder()
        		.sourceMinorVersion(0)
        		.targetMinorVersion(3)
        		.migrationInstructions(Arrays.asList(new MigrationInstructionImpl("UserTask1", "UserTask3")))
        		.majorVersion(1)
        		.build());
    }

    private List<MinorMigrationInstructions> generateMigrationInstructionFor130To150() {
        return Arrays.asList(MinorMigrationInstructions.builder()
        		.sourceMinorVersion(3)
        		.targetMinorVersion(5)
        		.migrationInstructions(Arrays.asList(new MigrationInstructionImpl("UserTask3", "UserTask2")))
        		.majorVersion(1)
        		.build());
    }

    private void suspendProcessInstance(ProcessInstance processInstance) {
        rule.getRuntimeService().suspendProcessInstanceById(processInstance.getId());
    }

    private void suspendProcessDefinition(ProcessDefinition processDefinition) {
        rule.getRepositoryService().suspendProcessDefinitionById(processDefinition.getId());
    }

    private ProcessDefinition getNewestDeployedProcessDefinitionId(String processDefinitionKey) {
        return rule.getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionKey(processDefinitionKey)
            .latestVersion()
            .singleResult();
    }

    private List<ProcessInstance> getRunningProcessInstances(String processDefinitionKey) {
        return rule.getRuntimeService()
            .createProcessInstanceQuery()
            .processDefinitionKey(processDefinitionKey)
            .list();
    }

    private List<Task> getCurrentTasks(String processDefinitionKey) {
        return rule.getTaskService()
            .createTaskQuery()
            .processDefinitionKey(processDefinitionKey)
            .initializeFormKeys()
            .list();
    }

    private void deployBPMNFromClasspathResource(String path) {
        rule.getRepositoryService()
            .createDeployment()
            .addClasspathResource(path)
            .deploy();
    }

    private ProcessInstance startProcessInstance(String processDefinitionKey) {
        return rule.getRuntimeService()
            .startProcessInstanceByKey(processDefinitionKey);
    }
}
