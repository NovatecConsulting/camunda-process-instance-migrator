package info.novatec.bpm.camunda.migrator;

import static info.novatec.bpm.camunda.migrator.assertions.ProcessInstanceListAsserter.assertThat;
import static info.novatec.bpm.camunda.migrator.assertions.TaskListAsserter.assertThat;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ProcessInstanceMigratorTest {

    @ClassRule
    public static ProcessEngineRule rule = new ProcessEngineRule();

    protected static final String MIGRATEABLE_PROCESS_MODEL_PATH = "test-processmodels/migrateable_processmodel_1_0_0.bpmn";
    private static final String UPDATED_PROCESS_MODEL_PATH = "test-processmodels/migrateable_processmodel_1_0_1_with_formkeys.bpmn";
    private static final String MINOR_INCREASED_PROCESS_MODEL_PATH = "test-processmodels/migrateable_processmodel_1_5_0.bpmn";
    private static final String MAJOR_INCREASED_PROCESS_MODEL_PATH = "test-processmodels/migrateable_processmodel_2_0_0.bpmn";

    private static final String PROCESS_DEFINITION_KEY = "MigrateableProcess";

    private ProcessInstanceMigrator processInstanceMigrator = new ProcessInstanceMigrator(rule.getProcessEngine());

    private ProcessDefinition newestProcessDefinition;
    private ProcessInstance processInstance1;
    private ProcessInstance processInstance2;

    @Before
    public void setUp() {
        deployBPMNFromClasspathResource(MIGRATEABLE_PROCESS_MODEL_PATH);
        //this will refer to the initial process Model
        newestProcessDefinition = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(newestProcessDefinition.getVersionTag()).isEqualTo("1.0.0");

        processInstance1 = startProcessInstance(PROCESS_DEFINITION_KEY);
        processInstance2 = startProcessInstance(PROCESS_DEFINITION_KEY);
    }

    @After
    public void cleanUp() {
        rule.getRuntimeService().deleteProcessInstance(processInstance1.getId(), "noReason");
        rule.getRuntimeService().deleteProcessInstance(processInstance2.getId(), "noReason");

        rule.getRepositoryService().createDeploymentQuery().list().forEach(
                deployment -> rule.getRepositoryService().deleteDeployment(deployment.getId()));
    }

    @Test
    public void processInstanceMigrator_should_migrate_all_process_instances_to_higher_patch() {
        deployBPMNFromClasspathResource(UPDATED_PROCESS_MODEL_PATH);
        ProcessDefinition newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.0.1");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(newestProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(newestProcessDefinition.getId())
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

        deployBPMNFromClasspathResource(UPDATED_PROCESS_MODEL_PATH);
        ProcessDefinition newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.0.1");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(newestProcessDefinition.getId())
            .allProcessInstancesAreSuspended();

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(newestProcessDefinition.getId())
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
        suspendProcessDefinition(newestProcessDefinition);

        deployBPMNFromClasspathResource(UPDATED_PROCESS_MODEL_PATH);
        ProcessDefinition newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.0.1");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
        .numberOfProcessInstancesIs(2)
        .allProcessInstancesHaveDefinitionId(newestProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(newestProcessDefinition.getId())
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
        deployBPMNFromClasspathResource(UPDATED_PROCESS_MODEL_PATH);
        ProcessDefinition newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.0.1");

        suspendProcessDefinition(newestProcessDefinitionAfterRedeployment);

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(newestProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(newestProcessDefinition.getId())
            .allTasksHaveFormkey(null);

        processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(newestProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(newestProcessDefinition.getId())
            .allTasksHaveFormkey(null);
    }

    @Test
    public void processInstanceMigrator_should_not_migrate_to_higher_minor_version() {
        deployBPMNFromClasspathResource(MINOR_INCREASED_PROCESS_MODEL_PATH);
        ProcessDefinition newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("1.5.0");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(newestProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(newestProcessDefinition.getId())
            .allTasksHaveFormkey(null);

        processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(newestProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(newestProcessDefinition.getId())
            .allTasksHaveFormkey(null);
    }

    @Test
    public void processInstanceMigrator_should_not_migrate_to_higher_major_version() {
        deployBPMNFromClasspathResource(MAJOR_INCREASED_PROCESS_MODEL_PATH);
        ProcessDefinition newestProcessDefinitionAfterRedeployment = getNewestDeployedProcessDefinitionId(PROCESS_DEFINITION_KEY);
        assertThat(newestProcessDefinitionAfterRedeployment.getVersionTag()).isEqualTo("2.0.0");

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(newestProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(newestProcessDefinition.getId())
            .allTasksHaveFormkey(null);

        processInstanceMigrator.migrateProcessInstances(PROCESS_DEFINITION_KEY);

        assertThat(getRunningProcessInstances(PROCESS_DEFINITION_KEY))
            .numberOfProcessInstancesIs(2)
            .allProcessInstancesHaveDefinitionId(newestProcessDefinition.getId());

        assertThat(getCurrentTasks(PROCESS_DEFINITION_KEY))
            .numberOfTasksIs(2)
            .allTasksHaveDefinitionId(newestProcessDefinition.getId())
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

    private ProcessInstance startProcessInstance(String processDefinitionKey) {
        return rule.getRuntimeService()
            .startProcessInstanceByKey(processDefinitionKey);
    }
}
