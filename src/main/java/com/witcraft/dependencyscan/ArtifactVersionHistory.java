package com.witcraft.dependencyscan;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Data
public final class ArtifactVersionHistory {
    private final AtomicReference<DependencyInfo> dependency;

    private final Map<String, List<DependencyVersion>> versions;

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
        return of(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersionInfo().getVersion());
    }

    public static ArtifactVersionHistory of(String groupId, String artifactId, String version) {
        return new ArtifactVersionHistory(groupId, artifactId, version);
    }

    public ArtifactVersionHistory addVersion(DependencyVersion version) {
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

    public List<DependencyVersion> getUpgradeVersions() {
        final List<DependencyVersion> versions = this.versions.get(dependency.get().getVersionInfo().getMajorVersion());
        if (versions != null) {
            return versions;
        }
        return Collections.emptyList();
    }

    public List<DependencyVersion> getOtherVersions() {
        final String majorVersion = dependency.get().getVersionInfo().getMajorVersion();
        final List<DependencyVersion> result = new ArrayList<>();
        for (Map.Entry<String, List<DependencyVersion>> entry : versions.entrySet()) {
            if (!Objects.equals(entry.getKey(), majorVersion)) {
                result.addAll(entry.getValue());
            }
        }
        return result;
    }
}
