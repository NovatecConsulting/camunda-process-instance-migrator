package info.novatec.camunda.migrator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProcessVersion {
    private final int majorVersion;
    private final int minorVersion;
    private final int patchVersion;

    public static ProcessVersion fromString(String versionString) {
        String[] stringArray = versionString.split("\\.");
        int majorVersion = Integer.valueOf(stringArray[0]);
        int minorVersion = Integer.valueOf(stringArray[1]);
        int patchVersion = Integer.valueOf(stringArray[2]);
        return new ProcessVersion(majorVersion, minorVersion, patchVersion);
    }

    public boolean isOlderVersionThan(ProcessVersion processVersionToCompare) {
        return this.majorVersion < processVersionToCompare.getMajorVersion()
               || (this.majorVersion == processVersionToCompare.getMajorVersion() && this.minorVersion < processVersionToCompare.getMinorVersion())
               || (this.majorVersion == processVersionToCompare.getMajorVersion() && this.minorVersion == processVersionToCompare.getMinorVersion()
                   && this.patchVersion < processVersionToCompare.getPatchVersion());
    }

    public boolean isOlderPatchThan(ProcessVersion processVersionToCompare) {
        return this.majorVersion == processVersionToCompare.getMajorVersion()
                && this.minorVersion == processVersionToCompare.getMinorVersion()
                && this.patchVersion < processVersionToCompare.getPatchVersion();
    }

    public boolean isOlderMinorThan(ProcessVersion processVersionToCompare) {
        return this.majorVersion == processVersionToCompare.getMajorVersion()
                && this.minorVersion <  processVersionToCompare.getMinorVersion();
    }

    public boolean isOlderMajorThan(ProcessVersion processVersionToCompare) {
        return this.majorVersion < processVersionToCompare.getMajorVersion();
    }

    public String toVersionTag() {
        return majorVersion + "." + minorVersion + "." + patchVersion;
    }
}
