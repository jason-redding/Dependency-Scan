package com.witcraft.dependencyscan;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.w3c.dom.Element;

import java.time.LocalDate;
import java.util.Optional;

import static lombok.AccessLevel.NONE;

@Data
public final class DependencyInfo {
    private final String groupId;

    private final String artifactId;

    private final DependencyVersion versionInfo;

    private final String scope;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final Element element;

    private DependencyInfo(String groupId, String artifactId, DependencyVersion version, String scope, Element element) {
        this.groupId = Optional.ofNullable(groupId).map(String::trim).orElseThrow(() -> new NullPointerException("groupId"));
        this.artifactId = Optional.ofNullable(artifactId).map(String::trim).orElseThrow(() -> new NullPointerException("artifactId"));
        this.versionInfo = Optional.ofNullable(version).orElseThrow(() -> new NullPointerException("version"));
        this.scope = Optional.ofNullable(scope).map(String::trim).orElse("compile");
        this.element = element;
    }

    public String getVersion() {
        return versionInfo.getVersion();
    }

    public String getGroupArtifact() {
        return "%s:%s".formatted(groupId, artifactId);
    }

    public String getGroupArtifactVersion() {
        return "%s:%s:%s".formatted(groupId, artifactId, versionInfo.getVersion());
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
            .withPublishDate(dependency.getVersionInfo().getPublishDate());
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
        private LocalDate publishDate;

        private Builder() {
        }

        public String getArtifactPath() {
            return "%s:%s".formatted(groupId, artifactId);
        }

        public DependencyInfo build() {
            final DependencyVersion versionInfo = DependencyVersion.builder()
                .withVersion(this.version)
                .withPublishDate(this.publishDate)
                .build();

            return new DependencyInfo(this.groupId, this.artifactId, versionInfo, this.scope, this.element);
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

        public Builder withPublishDate(LocalDate publishDate) {
            this.publishDate = publishDate;
            return this;
        }
    }
}
