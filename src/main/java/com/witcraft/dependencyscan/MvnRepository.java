package com.witcraft.dependencyscan;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public interface MvnRepository {
    static String getArtifactVersionsUrl(DependencyInfo dependency, int pageSize, int start) {
        final String escapedGroupId = URLEncoder.encode(dependency.getGroupId(), StandardCharsets.UTF_8);
        final String escapedArtifactId = URLEncoder.encode(dependency.getArtifactId(), StandardCharsets.UTF_8);
        final String startCondition = (start > 0 ? "&start=%d".formatted(start) : "");

        return "https://search.maven.org/solrsearch/select?q=g:%s+AND+a:%s&core=gav&rows=%d%s&wt=xml".formatted(escapedGroupId, escapedArtifactId, pageSize, startCondition);
    }

    ArtifactVersionHistory lookupArtifactVersions(DependencyInfo dependency);

    ArtifactVersionHistory lookupArtifactVersions(DependencyInfo dependency, int pageSize);

    ArtifactVersionHistory lookupArtifactVersions(DependencyInfo dependency, int pageSize, int start);
}
