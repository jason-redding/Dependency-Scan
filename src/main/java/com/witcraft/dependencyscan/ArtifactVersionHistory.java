package com.witcraft.dependencyscan;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static lombok.AccessLevel.NONE;

@Data
public final class ArtifactVersionHistory {
    @Setter(NONE)
    @Getter(NONE)
    @ToString.Exclude
    private final AtomicReference<DependencyInfo> dependency;

    private final Map<String, List<DependencyInfo>> versions;

    private ArtifactVersionHistory(DependencyInfo dependency) {
        this.dependency = new AtomicReference<>(Objects.requireNonNull(dependency));
        this.versions = new LinkedHashMap<>();
    }

    private ArtifactVersionHistory(String groupId, String artifactId, String version) {
        this(
            DependencyInfo.builder()
                .withGroupId(groupId)
                .withArtifactId(artifactId)
                .withVersion(version)
                .build()
        );
    }

    public static ArtifactVersionHistory of(DependencyInfo dependency) {
        return of(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
    }

    public static ArtifactVersionHistory of(String groupId, String artifactId, String version) {
        return new ArtifactVersionHistory(groupId, artifactId, version);
    }

    public ArtifactVersionHistory addVersion(DependencyInfo version) {
        final DependencyInfo dependencyInfo = dependency.get();
        final String currentVersion = dependencyInfo.getVersion();
        if (Objects.equals(version.getVersion(), currentVersion)) {
            final DependencyInfo newInfo = DependencyInfo.builder(dependencyInfo)
                .withPublishDate(version.getPublishDate())
                .build();
            dependency.set(newInfo);
        } else {
            versions.computeIfAbsent(version.getMajorVersion(), majorVersion -> new ArrayList<>())
                .add(version);
        }
        return this;
    }

    public DependencyInfo getDependency() {
        return dependency.get();
    }

    public List<DependencyInfo> getUpgradeVersions() {
        final List<DependencyInfo> versions = this.versions.get(dependency.get().getMajorVersion());
        if (versions != null) {
            return versions;
        }
        return Collections.emptyList();
    }

    public List<DependencyInfo> getOtherVersions() {
        final String majorVersion = dependency.get().getMajorVersion();
        final List<DependencyInfo> result = new ArrayList<>();
        for (Map.Entry<String, List<DependencyInfo>> entry : versions.entrySet()) {
            if (!Objects.equals(entry.getKey(), majorVersion)) {
                result.addAll(entry.getValue());
            }
        }
        return result;
    }
}
