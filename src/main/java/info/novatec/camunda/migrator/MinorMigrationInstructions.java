package info.novatec.camunda.migrator;

import java.util.List;


import org.camunda.bpm.engine.migration.MigrationInstruction;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Data-Class for migration instructions linked to a single minor migration of a
 * process instance. Contains the migrations source minor version, its target
 * minor version, a list of migration instructions and the major version of
 * source and target version
 */
@Getter
@RequiredArgsConstructor
@Builder
public class MinorMigrationInstructions {
    private final int sourceMinorVersion;
    private final int targetMinorVersion;
    @NonNull
    private final List<MigrationInstruction> migrationInstructions;
    private final int majorVersion;
}
