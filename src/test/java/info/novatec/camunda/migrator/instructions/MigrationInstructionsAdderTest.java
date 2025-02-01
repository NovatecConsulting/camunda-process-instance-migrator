package info.novatec.camunda.migrator.instructions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import info.novatec.camunda.migrator.migration.CustomMigrationInstruction;
import info.novatec.camunda.migrator.migration.CustomMigrationPlan;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MigrationInstructionsAdderTest {

    private static final String ACTIVITY_1 = "ServiceTask1";
    private static final String ACTIVITY_2 = "ServiceTask2";
    private static final String ACTIVITY_3 = "UserTask1";
    private static final String ACTIVITY_4 = "UserTask2";
    private static final String ACTIVITY_5 = "ReceiveTask1";
    private static final String ACTIVITY_6 = "ReceiveTask2";

    @Mock
    CustomMigrationPlan migrationPlan;

    @Spy
    List<CustomMigrationInstruction> migrationPlanInstructionList = new ArrayList<>();

    List<CustomMigrationInstruction> instructionList;

    @Mock
    CustomMigrationInstruction migrationInstruction1;
    @Mock
    CustomMigrationInstruction migrationInstruction2;
    @Mock
    CustomMigrationInstruction migrationInstruction3;
    @Mock
    CustomMigrationInstruction migrationInstruction4;

    @Captor
    private ArgumentCaptor<List<CustomMigrationInstruction>> migrationInstructionCaptor;

    @BeforeEach()
    void setUp() {
        migrationPlanInstructionList.add(migrationInstruction1);
        migrationPlanInstructionList.add(migrationInstruction2);

        instructionList = new ArrayList<>();
        instructionList.add(migrationInstruction3);
        instructionList.add(migrationInstruction4);

        when(migrationPlan.getInstructions()).thenReturn(migrationPlanInstructionList);
    }

    @Test
    void addInstructions_should_overwrite_instructions_with_existing_source() {
        when(migrationInstruction1.getSourceActivityId()).thenReturn(ACTIVITY_1);
        when(migrationInstruction1.getTargetActivityId()).thenReturn(ACTIVITY_1);

        when(migrationInstruction2.getSourceActivityId()).thenReturn(ACTIVITY_2);
        when(migrationInstruction2.getTargetActivityId()).thenReturn(ACTIVITY_2);

        when(migrationInstruction3.getSourceActivityId()).thenReturn(ACTIVITY_1);
        when(migrationInstruction3.getTargetActivityId()).thenReturn(ACTIVITY_3);

        when(migrationInstruction4.getSourceActivityId()).thenReturn(ACTIVITY_2);
        when(migrationInstruction4.getTargetActivityId()).thenReturn(ACTIVITY_4);

        MigrationInstructionsAdder.addInstructions(migrationPlan, instructionList);

        verify(migrationPlanInstructionList).clear();
        verify(migrationPlanInstructionList).addAll(migrationInstructionCaptor.capture());
        List<CustomMigrationInstruction> newInstructions = migrationInstructionCaptor.getValue();

        assertThat(newInstructions).hasSize(2);
        assertThat(newInstructions).anyMatch(instruction -> instruction.getSourceActivityId() == ACTIVITY_1
                && instruction.getTargetActivityId() == ACTIVITY_3);
        assertThat(newInstructions).anyMatch(instruction -> instruction.getSourceActivityId() == ACTIVITY_2
                && instruction.getTargetActivityId() == ACTIVITY_4);
    }

    @Test
    void addInstructions_should_add_instructions_with_not_yet_existing_source() {
        when(migrationInstruction1.getSourceActivityId()).thenReturn(ACTIVITY_1);
        when(migrationInstruction1.getTargetActivityId()).thenReturn(ACTIVITY_1);

        when(migrationInstruction2.getSourceActivityId()).thenReturn(ACTIVITY_2);
        when(migrationInstruction2.getTargetActivityId()).thenReturn(ACTIVITY_2);

        when(migrationInstruction3.getSourceActivityId()).thenReturn(ACTIVITY_3);
        when(migrationInstruction3.getTargetActivityId()).thenReturn(ACTIVITY_4);

        when(migrationInstruction4.getSourceActivityId()).thenReturn(ACTIVITY_5);
        when(migrationInstruction4.getTargetActivityId()).thenReturn(ACTIVITY_6);

        MigrationInstructionsAdder.addInstructions(migrationPlan, instructionList);

        verify(migrationPlanInstructionList).clear();
        verify(migrationPlanInstructionList).addAll(migrationInstructionCaptor.capture());
        List<CustomMigrationInstruction> newInstructions = migrationInstructionCaptor.getValue();

        assertThat(newInstructions).hasSize(4);
        assertThat(newInstructions).anyMatch(instruction -> instruction.getSourceActivityId() == ACTIVITY_1
                && instruction.getTargetActivityId() == ACTIVITY_1);
        assertThat(newInstructions).anyMatch(instruction -> instruction.getSourceActivityId() == ACTIVITY_2
                && instruction.getTargetActivityId() == ACTIVITY_2);
        assertThat(newInstructions).anyMatch(instruction -> instruction.getSourceActivityId() == ACTIVITY_3
                && instruction.getTargetActivityId() == ACTIVITY_4);
        assertThat(newInstructions).anyMatch(instruction -> instruction.getSourceActivityId() == ACTIVITY_5
                && instruction.getTargetActivityId() == ACTIVITY_6);
    }

}
