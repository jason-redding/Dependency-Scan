package com.witcraft.dependencyscan;

import lombok.extern.log4j.Log4j2;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public final class Main {
    public static void main(String[] args) throws IOException, SAXException {
        final Path path;
        if (args.length > 0) {
            path = Path.of(args[0]);
        } else {
            path = Path.of("pom.xml");
        }
        final PomScanner pomScanner = DefaultPomScanner.builder(path).build();
        final Map<String, DependencyInfo> dependencyMap = pomScanner.scanDependencyInfo();
        final MvnRepository mvnRepository = DefaultMvnRepository.newInstance();
        final AtomicInteger newerDependencyCount = new AtomicInteger();

        for (Map.Entry<String, DependencyInfo> entry : dependencyMap.entrySet()) {
            final DependencyInfo dependency = entry.getValue();
            final String dependencyGroupArtifact = dependency.getGroupArtifact();
            final String dependencyGroupArtifactVersion = dependency.getGroupArtifactVersion();
            final ArtifactVersionHistory versionHistory = mvnRepository.lookupArtifactVersions(dependency);
            final List<DependencyVersion> dependenciesAtSameMajorVersion = versionHistory.getUpgradeVersions();
            final List<DependencyVersion> remainingVersions = versionHistory.getOtherVersions();
            final int upgradeCount = dependenciesAtSameMajorVersion.size();
            final int otherVersionCount = remainingVersions.size();
            final DependencyVersion versionInfo = dependency.getVersionInfo();
            final String majorVersion = versionInfo.getMajorVersion();

            if (upgradeCount > 0) {
                newerDependencyCount.incrementAndGet();
                final DependencyVersion latestVersion = dependenciesAtSameMajorVersion.get(0);
                log.info(
                    "[{}] Newer version is available: \"{}\" (published {})",
                    dependencyGroupArtifact,
                    latestVersion.getVersion(),
                    latestVersion.getPublishDate()
                );
            } else {
                if (otherVersionCount > 0) {
                    final boolean isSingular = (otherVersionCount == 1);
                    final DependencyVersion latestOtherVersion = remainingVersions.get(0);
                    log.debug(
                        "[{}] already at latest {}.x version ({}), but there {} {} newer {}{}",
                        dependencyGroupArtifact,
                        majorVersion,
                        dependency.getVersion(),
                        (isSingular ? "is" : "are"),
                        (isSingular ? "a" : otherVersionCount),
                        (isSingular ? "version" : "versions"),
                        (isSingular ? ": %s" : ". The latest is: %s (published %s)").formatted(latestOtherVersion.getVersion(), latestOtherVersion.getPublishDate())
                    );
                } else {
                    log.debug("[{}] already at latest version: {}", dependencyGroupArtifact, dependency.getVersion());
                }
            }

            if (upgradeCount > 1) {
                log.debug("[{}] There are {} newer {}.x {}: {}", dependencyGroupArtifact, upgradeCount, majorVersion, "versions", dependenciesAtSameMajorVersion);
            }
            if (otherVersionCount > 0) {
                log.debug("[{}] There {} {} other {}: {}", dependencyGroupArtifact, (otherVersionCount == 1 ? "is" : "are"), otherVersionCount, (otherVersionCount == 1 ? "version" : "versions"), remainingVersions);
            }
        }

        if (newerDependencyCount.get() == 0) {
            log.info("No newer dependency versions found.");
        }
    }
}
