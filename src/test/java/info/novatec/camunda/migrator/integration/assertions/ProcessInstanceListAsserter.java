package info.novatec.camunda.migrator.integration.assertions;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import java.util.List;

@RequiredArgsConstructor
public class ProcessInstanceListAsserter {

    private final List<ProcessInstance> processInstances;

    public static ProcessInstanceListAsserter assertThat(List<ProcessInstance> processInstances) {
        return new ProcessInstanceListAsserter(processInstances);
    }

    public ProcessInstanceListAsserter allProcessInstancesHaveDefinitionId(String processDefinitionId) {
        Assertions.assertThat(processInstances.stream().allMatch(
                processInstance -> processDefinitionId.equals(processInstance.getProcessDefinitionId()))).isTrue();
        return this;
    }

    public ProcessInstanceListAsserter numberOfProcessInstancesIs(int numberOfProcessInstances) {
        Assertions.assertThat(processInstances.size()).isEqualTo(numberOfProcessInstances);
        return this;
    }

    public ProcessInstanceListAsserter allProcessInstancesAreSuspended() {
        Assertions.assertThat(processInstances.stream().allMatch(
                processInstance -> processInstance.isSuspended())).isTrue();
        return this;
    }

}
