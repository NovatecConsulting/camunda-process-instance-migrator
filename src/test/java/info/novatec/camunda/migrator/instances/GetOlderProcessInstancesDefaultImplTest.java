package info.novatec.camunda.migrator.instances;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.extension.mockito.QueryMocks.mockProcessDefinitionQuery;
import static org.camunda.bpm.extension.mockito.QueryMocks.mockProcessInstanceQuery;
import static org.mockito.Mockito.*;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import info.novatec.camunda.migrator.ProcessVersion;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GetOlderProcessInstancesDefaultImplTest {

    private static final String PROCESS_DEFINITION_KEY = "myKey";
    private static final String OLDER_VERSION_PROCESS_DEFINITION_ID = "12345";
    private static final ProcessVersion NEWEST_VERSION = ProcessVersion.fromString("1.5.9").get();
    private static final ProcessVersion OLDER_VERSION = ProcessVersion.fromString("1.3.4").get();
    private static final ProcessVersion TOO_OLD_VERSION = ProcessVersion.fromString("0.2.3").get();
    private static final ProcessVersion TOO_NEW_VERSION = ProcessVersion.fromString("2.5.10").get();
    private static final String PROCESS_INSTANCE_1_BUSINESS_KEY = "0815";
    private static final String PROCESS_INSTANCE_2_BUSINESS_KEY = "0816";
    private static final String PROCESS_INSTANCE_1_ID = "4711";
    private static final String PROCESS_INSTANCE_2_ID = "4712";

    @Mock
    ProcessEngine processEngine;
    @InjectMocks
    GetOlderProcessInstancesDefaultImpl getOlderProcessInstancesDefaultImpl;

    @Mock
    RepositoryService repositoryService;
    @Mock
    RuntimeService runtimeService;

    @Mock
    ProcessDefinition processDefinitionResult1;
    @Mock
    ProcessDefinition processDefinitionResult2;
    @Mock
    ProcessDefinition processDefinitionResult3;
    @Mock
    ProcessInstance processInstanceResult1;
    @Mock
    ProcessInstance processInstanceResult2;

    @BeforeEach
    void setup() {
        when(processEngine.getRepositoryService()).thenReturn(repositoryService);
        when(processEngine.getRuntimeService()).thenReturn(runtimeService);

        mockProcessDefinitionQuery(repositoryService)
            .list(asList(new ProcessDefinition[] {processDefinitionResult1, processDefinitionResult2,
                processDefinitionResult3}));
        mockProcessInstanceQuery(runtimeService)
            .list(asList(new ProcessInstance[] {processInstanceResult1, processInstanceResult2}));

        when(processInstanceResult1.getBusinessKey()).thenReturn(PROCESS_INSTANCE_1_BUSINESS_KEY);
        when(processInstanceResult2.getBusinessKey()).thenReturn(PROCESS_INSTANCE_2_BUSINESS_KEY);
        when(processInstanceResult1.getId()).thenReturn(PROCESS_INSTANCE_1_ID);
        when(processInstanceResult2.getId()).thenReturn(PROCESS_INSTANCE_2_ID);

    }

    @Test
    void getOlderProcessInstances_should_get_older_process_instances() {
        when(processDefinitionResult1.getVersionTag()).thenReturn(OLDER_VERSION.toVersionTag());
        when(processDefinitionResult1.getId()).thenReturn(OLDER_VERSION_PROCESS_DEFINITION_ID);

        List<VersionedProcessInstance> result = getOlderProcessInstancesDefaultImpl
            .getOlderProcessInstances(PROCESS_DEFINITION_KEY, NEWEST_VERSION);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(instance ->
            instance.getProcessDefinitionId().equals(OLDER_VERSION_PROCESS_DEFINITION_ID) &&
            instance.getProcessVersion().equals(OLDER_VERSION));
        assertThat(result).anyMatch(instance ->
            instance.getBusinessKey().equals(PROCESS_INSTANCE_1_BUSINESS_KEY) &&
            instance.getProcessInstanceId().equals(PROCESS_INSTANCE_1_ID));
        assertThat(result).anyMatch(instance ->
            instance.getBusinessKey().equals(PROCESS_INSTANCE_2_BUSINESS_KEY) &&
            instance.getProcessInstanceId().equals(PROCESS_INSTANCE_2_ID));
    }

    @Test
    void getOlderProcessInstances_should_filter_older_newer_and_null_version_tags() {
        when(processDefinitionResult1.getVersionTag()).thenReturn(TOO_OLD_VERSION.toVersionTag());
        when(processDefinitionResult2.getVersionTag()).thenReturn(null);
        when(processDefinitionResult3.getVersionTag()).thenReturn(TOO_NEW_VERSION.toVersionTag());

        List<VersionedProcessInstance> result = getOlderProcessInstancesDefaultImpl
            .getOlderProcessInstances(PROCESS_DEFINITION_KEY, NEWEST_VERSION);

        assertThat(result).isEmpty();
    }
}
