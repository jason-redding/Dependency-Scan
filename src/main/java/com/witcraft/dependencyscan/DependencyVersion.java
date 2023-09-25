package com.witcraft.dependencyscan;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.regex.Pattern;

import static lombok.AccessLevel.NONE;

@Data
public final class DependencyVersion {
    private static final Pattern PATTERN_PERIOD = Pattern.compile("\\.");

    private final String version;

    private final LocalDate publishDate;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final String majorVersion;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final String minorVersion;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final String revisionVersion;

    private DependencyVersion(String version, LocalDate publishDate) {
        this.version = version;
        this.publishDate = publishDate;

        final String[] versionParts = PATTERN_PERIOD.split(this.version);

        if (versionParts.length > 2) {
            this.majorVersion = versionParts[0];
            this.minorVersion = versionParts[1];
            this.revisionVersion = versionParts[2];
        } else if (versionParts.length > 1) {
            this.majorVersion = versionParts[0];
            this.minorVersion = versionParts[1];
            this.revisionVersion = null;
        } else if (versionParts.length > 0) {
            this.majorVersion = versionParts[0];
            this.minorVersion = null;
            this.revisionVersion = null;
        } else {
            this.majorVersion = null;
            this.minorVersion = null;
            this.revisionVersion = null;
        }
    }

    public static DependencyVersion of(DependencyInfo dependency) {
        return of(dependency.getVersionInfo().getVersion());
    }

    public static DependencyVersion of(String version) {
        return new DependencyVersion(version, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Data
    public static class Builder {
        @Setter(NONE)
        private String version;

        @Setter(NONE)
        private LocalDate publishDate;

        private Builder() {
        }

        public DependencyVersion build() {
            return new DependencyVersion(this.version, this.publishDate);
        }

        public Builder withPublishDate(LocalDate publishDate) {
            this.publishDate = publishDate;
            return this;
        }

        public Builder withVersion(String version) {
            this.version = version;
            return this;
        }
    }
}
