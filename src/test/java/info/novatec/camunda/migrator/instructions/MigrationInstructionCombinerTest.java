package info.novatec.camunda.migrator.instructions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import info.novatec.camunda.migrator.migration.CustomMigrationInstruction;

@ExtendWith(MockitoExtension.class)
public class MigrationInstructionCombinerTest {

    private static final int MAJOR_VERSION = 1;

    private static final String ACTIVITY_1 = "ServiceTask1";
    private static final String ACTIVITY_2 = "ServiceTask2";
    private static final String ACTIVITY_3 = "UserTask1";
    private static final String ACTIVITY_4 = "UserTask2";
    private static final String ACTIVITY_5 = "ReceiveTask1";
    private static final String ACTIVITY_6 = "ReceiveTask2";
    private static final String ACTIVITY_7 = "CallActivity1";
    private static final String ACTIVITY_8 = "CallActivity2";

    @Mock
    MigrationInstruction migrationInstruction1;
    @Mock
    MigrationInstruction migrationInstruction2;
    @Mock
    MigrationInstruction migrationInstruction3;
    @Mock
    MigrationInstruction migrationInstruction4;

    MinorMigrationInstructions migrationInstructions1To2;

    MinorMigrationInstructions migrationInstructions2To3;

    @BeforeEach()
    void setUp() {
        migrationInstructions1To2 = MinorMigrationInstructions.builder()
            .majorVersion(MAJOR_VERSION)
            .sourceMinorVersion(1)
            .targetMinorVersion(2)
            .migrationInstructions(Arrays.asList(migrationInstruction1, migrationInstruction2))
            .build();

        migrationInstructions2To3 = MinorMigrationInstructions.builder()
            .majorVersion(MAJOR_VERSION)
            .sourceMinorVersion(2)
            .targetMinorVersion(3)
            .migrationInstructions(Arrays.asList(migrationInstruction3, migrationInstruction4))
            .build();
    }

    @Test
    void combineMigrationInstruction_should_combine_instructions_where_source_matches_target() {
        when(migrationInstruction1.getSourceActivityId()).thenReturn(ACTIVITY_1);
        when(migrationInstruction1.getTargetActivityId()).thenReturn(ACTIVITY_2);

        when(migrationInstruction2.getSourceActivityId()).thenReturn(ACTIVITY_3);
        when(migrationInstruction2.getTargetActivityId()).thenReturn(ACTIVITY_4);

        when(migrationInstruction3.getSourceActivityId()).thenReturn(ACTIVITY_2);
        when(migrationInstruction3.getTargetActivityId()).thenReturn(ACTIVITY_5);

        when(migrationInstruction4.getSourceActivityId()).thenReturn(ACTIVITY_4);
        when(migrationInstruction4.getTargetActivityId()).thenReturn(ACTIVITY_6);

        List<CustomMigrationInstruction> result = MigrationInstructionCombiner.combineMigrationInstructions(
            Arrays.asList(new MinorMigrationInstructions[] {migrationInstructions1To2, migrationInstructions2To3}));

        assertThat(result).hasSize(2);
        assertThat(result)
            .anyMatch(migrationInstruction -> migrationInstruction.getSourceActivityId() == ACTIVITY_1
                && migrationInstruction.getTargetActivityId() == ACTIVITY_5);
        assertThat(result)
            .anyMatch(migrationInstruction -> migrationInstruction.getSourceActivityId() == ACTIVITY_3
                && migrationInstruction.getTargetActivityId() == ACTIVITY_6);
    }

    @Test
    void combineMigrationInstruction_should_not_combine_instructions_where_source_does_not_match_target() {
        when(migrationInstruction1.getSourceActivityId()).thenReturn(ACTIVITY_1);
        when(migrationInstruction1.getTargetActivityId()).thenReturn(ACTIVITY_2);

        when(migrationInstruction2.getSourceActivityId()).thenReturn(ACTIVITY_3);
        when(migrationInstruction2.getTargetActivityId()).thenReturn(ACTIVITY_4);

        when(migrationInstruction3.getSourceActivityId()).thenReturn(ACTIVITY_5);
        when(migrationInstruction3.getTargetActivityId()).thenReturn(ACTIVITY_6);

        when(migrationInstruction4.getSourceActivityId()).thenReturn(ACTIVITY_7);
        when(migrationInstruction4.getTargetActivityId()).thenReturn(ACTIVITY_8);

        List<CustomMigrationInstruction> result = MigrationInstructionCombiner.combineMigrationInstructions(
            Arrays.asList(new MinorMigrationInstructions[] {migrationInstructions1To2, migrationInstructions2To3}));

        assertThat(result).hasSize(4);
        assertThat(result)
            .anyMatch(migrationInstruction -> migrationInstruction.getSourceActivityId() == ACTIVITY_1
                && migrationInstruction.getTargetActivityId() == ACTIVITY_2);
        assertThat(result)
            .anyMatch(migrationInstruction -> migrationInstruction.getSourceActivityId() == ACTIVITY_3
                && migrationInstruction.getTargetActivityId() == ACTIVITY_4);
        assertThat(result)
            .anyMatch(migrationInstruction -> migrationInstruction.getSourceActivityId() == ACTIVITY_5
                && migrationInstruction.getTargetActivityId() == ACTIVITY_6);
        assertThat(result)
            .anyMatch(migrationInstruction -> migrationInstruction.getSourceActivityId() == ACTIVITY_7
                && migrationInstruction.getTargetActivityId() == ACTIVITY_8);
    }
}
