package info.novatec.camunda.migrator.integration.assertions;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.task.Task;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskListAsserter {

    private final List<Task> tasks;

    public static TaskListAsserter assertThat(List<Task> Tasks) {
        return new TaskListAsserter(Tasks);
    }

    public TaskListAsserter allTasksHaveDefinitionId(String processDefinitionId) {
        Assertions.assertThat(tasks.stream()
            .allMatch(
                task -> processDefinitionId.equals(task.getProcessDefinitionId())))
            .isTrue();
        return this;
    }

    public TaskListAsserter numberOfTasksIs(int numberOfTasks) {
        Assertions.assertThat(tasks.size()).isEqualTo(numberOfTasks);
        return this;
    }

    public TaskListAsserter allTasksHaveFormkey(String formKey) {
        tasks.stream()
            .forEach(
                task -> Assertions.assertThat(formKey).isEqualTo(task.getFormKey()));
        return this;
    }

    public TaskListAsserter allTasksHaveKey(String key) {
        tasks.stream()
            .forEach(
                task -> Assertions.assertThat(key).isEqualTo(task.getTaskDefinitionKey()));
        return this;
    }
    
    public TaskListAsserter allTasksHaveName(String name) {
        Assertions.assertThat(tasks.stream().allMatch(
                task -> name == task.getName()));
        return this;
    }

    public TaskListAsserter oneTaskHasKey(String key) {
    	Assertions.assertThat(tasks.stream()
    		.anyMatch(task -> key.equals(task.getTaskDefinitionKey())));
    	return this;
    }
}
