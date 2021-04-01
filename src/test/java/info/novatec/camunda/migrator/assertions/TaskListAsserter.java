package info.novatec.camunda.migrator.assertions;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.task.Task;
import java.util.List;

@RequiredArgsConstructor
public class TaskListAsserter {

    private final List<Task> tasks;

    public static TaskListAsserter assertThat(List<Task> Tasks) {
        return new TaskListAsserter(Tasks);
    }

    public TaskListAsserter allTasksHaveDefinitionId(String processDefinitionId) {
        Assertions.assertThat(tasks.stream().allMatch(
                task -> processDefinitionId.equals(task.getProcessDefinitionId()))).isTrue();
        return this;
    }

    public TaskListAsserter numberOfTasksIs(int numberOfTasks) {
        Assertions.assertThat(tasks.size()).isEqualTo(numberOfTasks);
        return this;
    }

    public TaskListAsserter allTasksHaveFormkey(String formKey) {
        Assertions.assertThat(tasks.stream().allMatch(
                task -> formKey == task.getFormKey()));
        return this;
    }
    
    public TaskListAsserter allTasksHaveName(String name) {
        Assertions.assertThat(tasks.stream().allMatch(
                task -> name == task.getName()));
        return this;
    }
}
