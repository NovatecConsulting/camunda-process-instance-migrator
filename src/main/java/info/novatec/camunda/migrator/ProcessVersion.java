package info.novatec.camunda.migrator;

import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProcessVersion {

    private final int majorVersion;
    private final int minorVersion;
    private final int patchVersion;

    public static Optional<ProcessVersion> fromString(String versionString) {
        if (versionString == null || !versionString.matches("\\d+\\.\\d+\\.\\d+")) {
           return Optional.empty();
        }

        String[] stringArray = versionString.split("\\.");
        int majorVersion = Integer.parseInt(stringArray[0]);
        int minorVersion = Integer.parseInt(stringArray[1]);
        int patchVersion = Integer.parseInt(stringArray[2]);
        return Optional.of(new ProcessVersion(majorVersion, minorVersion, patchVersion));
    }

    public boolean isOlderVersionThan(ProcessVersion processVersionToCompare) {
        return this.majorVersion < processVersionToCompare.getMajorVersion()
            || (this.majorVersion == processVersionToCompare.getMajorVersion()
                && this.minorVersion < processVersionToCompare.getMinorVersion())
            || (this.majorVersion == processVersionToCompare.getMajorVersion()
                && this.minorVersion == processVersionToCompare.getMinorVersion()
                && this.patchVersion < processVersionToCompare.getPatchVersion());
    }

    public boolean isOlderPatchThan(ProcessVersion processVersionToCompare) {
        return this.majorVersion == processVersionToCompare.getMajorVersion()
            && this.minorVersion == processVersionToCompare.getMinorVersion()
            && this.patchVersion < processVersionToCompare.getPatchVersion();
    }

    public boolean isOlderMinorThan(ProcessVersion processVersionToCompare) {
        return this.majorVersion == processVersionToCompare.getMajorVersion()
            && this.minorVersion < processVersionToCompare.getMinorVersion();
    }

    public boolean isOlderMajorThan(ProcessVersion processVersionToCompare) {
        return this.majorVersion < processVersionToCompare.getMajorVersion();
    }

    public String toVersionTag() {
        return majorVersion + "." + minorVersion + "." + patchVersion;
    }

    public boolean equals(ProcessVersion other) {
        return this.majorVersion == other.majorVersion &&
            this.minorVersion == other.minorVersion &&
            this.patchVersion == other.patchVersion;
    }
}
