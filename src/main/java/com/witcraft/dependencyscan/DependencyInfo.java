package com.witcraft.dependencyscan;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.w3c.dom.Element;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static lombok.AccessLevel.NONE;

@Data
public final class DependencyInfo {

    private static final Duration ONE_DAY = Duration.ofDays(1);

    private static final Pattern PATTERN_PERIOD = Pattern.compile("\\.");

    private static final Pattern PATTERN_REVISION = Pattern.compile("^(?<revision>\\d+)(?<suffix>.*)$");

    private static final ZoneId SYSTEM_DEFAULT_ZONE_ID = ZoneId.systemDefault();

    private final String groupId;

    private final String artifactId;

    private final String version;

    private final Instant publishDate;

    private final String scope;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final Element element;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final String majorVersion;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final String minorVersion;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final String revisionVersion;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final String versionSuffix;

    private DependencyInfo(String groupId, String artifactId, String version, Instant publishDate, String scope, Element element) {
        this.groupId = Optional.ofNullable(groupId).map(String::trim).orElseThrow(() -> new NullPointerException("groupId"));
        this.artifactId = Optional.ofNullable(artifactId).map(String::trim).orElseThrow(() -> new NullPointerException("artifactId"));
        this.version = Optional.ofNullable(version).map(String::trim).orElseThrow(() -> new NullPointerException("version"));
        this.publishDate = publishDate;
        this.scope = Optional.ofNullable(scope).map(String::trim).orElse("compile");
        this.element = element;

        final String[] versionParts = PATTERN_PERIOD.split(this.version);

        if (versionParts.length > 2) {
            this.majorVersion = versionParts[0];
            this.minorVersion = versionParts[1];
            final Matcher revisionMatcher = PATTERN_REVISION.matcher(versionParts[2]);
            if (revisionMatcher.find()) {
                this.revisionVersion = revisionMatcher.group("revision");
                this.versionSuffix = revisionMatcher.group("suffix");
            } else {
                this.revisionVersion = versionParts[2];
                this.versionSuffix = null;
            }
        } else if (versionParts.length > 1) {
            this.majorVersion = versionParts[0];
            this.minorVersion = versionParts[1];
            this.revisionVersion = null;
            this.versionSuffix = null;
        } else if (versionParts.length > 0) {
            this.majorVersion = versionParts[0];
            this.minorVersion = null;
            this.revisionVersion = null;
            this.versionSuffix = null;
        } else {
            this.majorVersion = null;
            this.minorVersion = null;
            this.revisionVersion = null;
            this.versionSuffix = null;
        }
    }

    public String getAge() {
        return getAgeFrom(Instant.now());
    }

    public String getAgeFrom(Instant time) {
        if (this.publishDate == null) {
            return null;
        }
        final Instant publishDate = this.publishDate;
        final Duration dependencyAge = Duration.between(publishDate, time).abs();
        final long hours = dependencyAge.toHours();
        final long days = dependencyAge.toDays();
        return (dependencyAge.compareTo(ONE_DAY) < 0 ? "%d Hour%s".formatted(hours, (hours == 1 ? "" : "s")) : "%d Day%s".formatted(days, (days == 1 ? "" : "s")));
    }

    public String getGroupArtifact() {
        return "%s:%s".formatted(groupId, artifactId);
    }

    public String getGroupArtifactVersion() {
        return "%s:%s:%s".formatted(groupId, artifactId, version);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(DependencyInfo dependency) {
        return builder()
            .withGroupId(dependency.getGroupId())
            .withArtifactId(dependency.getArtifactId())
            .withVersion(dependency.getVersion())
            .withScope(dependency.getScope())
            .withElement(dependency.getElement())
            .withPublishDate(dependency.getPublishDate());
    }

    @Override
    public String toString() {
        final StringJoiner joiner = new StringJoiner(", ", "Dependency(", ")");
        joiner.add("version=\"%s\"".formatted(version));
        joiner.add("published=\"%s ago\"".formatted(getAge().toLowerCase()));
        joiner.add("publishDate=\"%s\"".formatted(publishDate.atOffset(SYSTEM_DEFAULT_ZONE_ID.getRules().getStandardOffset(publishDate))));
        return joiner.toString();
    }

    @Data
    public static class Builder {
        @Setter(NONE)
        private String groupId;

        @Setter(NONE)
        private String artifactId;

        @Setter(NONE)
        private String version;

        @Setter(NONE)
        private String scope;

        @Setter(NONE)
        private Element element;

        @Setter(NONE)
        private Instant publishDate;

        private Builder() {
        }

        public String getArtifactPath() {
            return "%s:%s".formatted(groupId, artifactId);
        }

        public DependencyInfo build() {
            return new DependencyInfo(this.groupId, this.artifactId, this.version, this.publishDate, this.scope, this.element);
        }

        public Builder withArtifactId(String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        public Builder withElement(Element element) {
            this.element = element;
            return this;
        }

        public Builder withGroupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder withScope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder withVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder withPublishDate(Instant publishDate) {
            this.publishDate = publishDate;
            return this;
        }
    }
}
