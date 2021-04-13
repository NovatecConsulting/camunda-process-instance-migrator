package info.novatec.camunda.migrator;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;

import static info.novatec.camunda.migrator.assertions.TaskListAsserter.assertThat;
import static info.novatec.camunda.migrator.assertions.ProcessInstanceListAsserter.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ProcessInstanceMigratorTest {

    @ClassRule
    public static ProcessEngineRule rule = new ProcessEngineRule();

    private static final String NON_MIGRATEABLE_PROCESS_MODEL_WITHOUT_VERSION = "test-processmodels/migrateable_processmodel_without_version.bpmn";
    private static final String MIGRATEABLE_PROCESS_MODEL_PATH = "test-processmodels/migrateable_processmodel_1_0_0.bpmn";
    private static final String UPDATED_PROCESS_MODEL_PATH = "test-processmodels/migrateable_processmodel_1_0_1_with_formkeys.bpmn";
    private static final String UPDATED_PROCESS_MODEL_PATH_WITH_SUBPROCESSES = "test-processmodels/migrateable_processmodel_1_0_2_with_subprocesses.bpmn";
    private static final String MINOR_INCREASED_PROCESS_MODEL_PATH = "test-processmodels/migrateable_processmodel_1_5_0.bpmn";
    private static final String MAJOR_INCREASED_PROCESS_MODEL_PATH = "test-processmodels/migrateable_processmodel_2_0_0.bpmn";

    private static final String PROCESS_DEFINITION_KEY = "MigrateableProcess";

    private ProcessInstanceMigrator processInstanceMigrator = new ProcessInstanceMigrator(rule.getProcessEngine());

    private ProcessDefinition initialProcessDefinition;
    private ProcessDefinition newestProcessDefinitionAfterRedeployment;
    private ProcessInstance processInstance1;
    private ProcessInstance processInstance2;

    @After
    public void cleanUp() {
        rule.getRepositoryService().createDeploymentQuery().list().forEach(
                deployment -> rule.getRepositoryService().deleteDeployment(deployment.getId(), true));
    }

    @Test
    public void processInstanceMigrator_should_migrate_all_process_instances_sitting_at_user_tasks_to_higher_patch() {
    	deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
    	deployNewProcessModel(UPDATED_PROCESS_MODEL_PATH, "1.0.1");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveName("Do something")
            .allTasksHaveFormkey(null);

        processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId())
            .allTasksHaveName("Do something")
            .allTasksHaveFormkey("Formkey1");
    }
    
    @Test
    public void processInstanceMigrator_should_migrate_all_process_instances_sitting_at_receive_tasks_to_higher_patch() {
    	deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
    	deployNewProcessModel(UPDATED_PROCESS_MODEL_PATH, "1.0.1");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());
        
        complete(task(processInstance1));
        complete(task(processInstance2));
        
        assertThat(processInstance1).isWaitingAtExactly("ReceiveTask1");
        assertThat(processInstance2).isWaitingAtExactly("ReceiveTask1");

        processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(newestProcessDefinitionAfterRedeployment.getId());

        assertThat(processInstance1).isWaitingAtExactly("ReceiveTask1");
        assertThat(processInstance2).isWaitingAtExactly("ReceiveTask1");
    }
    
    @Test
    public void processInstanceMigrator_should_not_migrate_process_instances_sitting_at_user_tasks_to_higher_patch_if_target_is_in_subprocess() {
    	deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
    	deployNewProcessModel(UPDATED_PROCESS_MODEL_PATH_WITH_SUBPROCESSES, "1.0.2");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveName("Do something")
            .allTasksHaveFormkey(null);

        processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(initialProcessDefinition.getId())
            .allTasksHaveName("Do something")
            .allTasksHaveFormkey(null);
    }
    
    @Test
    public void processInstanceMigrator_should_not_migrate_process_instances_sitting_at_receive_tasks_to_higher_patch_if_target_is_in_subprocess() {
    	deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
    	deployNewProcessModel(UPDATED_PROCESS_MODEL_PATH_WITH_SUBPROCESSES, "1.0.2");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());
        
        complete(task(processInstance1));
        complete(task(processInstance2));
        
        assertThat(processInstance1).isWaitingAtExactly("ReceiveTask1");
        assertThat(processInstance2).isWaitingAtExactly("ReceiveTask1");

        processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(initialProcessDefinition.getId());

        assertThat(processInstance1).isWaitingAtExactly("ReceiveTask1");
        assertThat(processInstance2).isWaitingAtExactly("ReceiveTask1");
    }

    @Test
    public void processInstanceMigrator_should_migrate_suspended_process_instances() {
    	deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
        suspendProcessInstance(processInstance1);
        suspendProcessInstance(processInstance2);

        deployNewProcessModel(UPDATED_PROCESS_MODEL_PATH, "1.0.1");

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
    	deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
        suspendProcessDefinition(initialProcessDefinition);

        deployNewProcessModel(UPDATED_PROCESS_MODEL_PATH, "1.0.1");

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
    	deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
    	deployNewProcessModel(UPDATED_PROCESS_MODEL_PATH, "1.0.1");

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
    public void processInstanceMigrator_should_not_migrate_to_higher_minor_version() {    	
    	deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
    	deployNewProcessModel(MINOR_INCREASED_PROCESS_MODEL_PATH, "1.5.0");

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
    public void processInstanceMigrator_should_not_migrate_to_higher_major_version() {
    	deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
    	deployNewProcessModel(MAJOR_INCREASED_PROCESS_MODEL_PATH, "2.0.0");

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
    public void processInstanceMigrator_should_not_migrate_process_instances_to_models_without_version_tag() {
    	deployInitialProcessModelAndStartProcessInstances(MIGRATEABLE_PROCESS_MODEL_PATH, "1.0.0");
    	deployNewProcessModel(NON_MIGRATEABLE_PROCESS_MODEL_WITHOUT_VERSION, null);

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
    public void processInstanceMigrator_should_not_fail_if_only_process_models_without_version_tag_exist() {
    	deployInitialProcessModelAndStartProcessInstances(NON_MIGRATEABLE_PROCESS_MODEL_WITHOUT_VERSION, null);
    	
    	assertDoesNotThrow(() -> processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY));
    }
    
    @Test
    public void processInstanceMigrator_should_not_migrate_instances_from_process_models_without_version_tag() {
    	deployInitialProcessModelAndStartProcessInstances(NON_MIGRATEABLE_PROCESS_MODEL_WITHOUT_VERSION, null);        
    	deployNewProcessModel(UPDATED_PROCESS_MODEL_PATH, "1.0.1");
        
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

    private void suspendProcessInstance(ProcessInstance processInstance) {
       rule.getRuntimeService().suspendProcessInstanceById(processInstance.getId());
    }

    private void suspendProcessDefinition(ProcessDefinition processDefinition) {
        rule.getRepositoryService().suspendProcessDefinitionById(processDefinition.getId());
    }

    private ProcessDefinition getNewestDeployedProcessDefinitionId(String processDefinitionKey) {
        return rule.getRepositoryService().createProcessDefinitionQuery()
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

    private List<Task> getCurrentTasks(String processDefinitionKey){
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
    
    private void deployInitialProcessModelAndStartProcessInstances(String processModelPath, String expectedVersionTag) {
        deployBPMNFromClasspathResource(processModelPath);
        initialProcessDefinition = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(initialProcessDefinition.getVersionTag()).isEqualTo(expectedVersionTag);

        processInstance1 = startProcessInstance(PROCESS_DEFINITION_KEY);
        processInstance2 = startProcessInstance(PROCESS_DEFINITION_KEY);
    }
    
    private void deployNewProcessModel(String processModelPath, String expectedVersionTag) {
    	deployBPMNFromClasspathResource(processModelPath);
    	newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo(expectedVersionTag);
    }

    private ProcessInstance startProcessInstance(String processDefinitionKey) {
        return rule.getRuntimeService()
            .startProcessInstanceByKey(processDefinitionKey);
    }
}
