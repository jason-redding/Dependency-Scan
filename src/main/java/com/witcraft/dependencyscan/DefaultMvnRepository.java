package com.witcraft.dependencyscan;

import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathNodes;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.witcraft.dependencyscan.MvnRepository.getArtifactVersionsUrl;

@Log4j2
public final class DefaultMvnRepository implements MvnRepository {
    public static final ZoneId SYSTEM_DEFAULT_ZONE_ID = ZoneId.systemDefault();

    private final XPathExecutor xPathExecutor;

    private DefaultMvnRepository() {
        this.xPathExecutor = XPathExecutor.newInstance();
    }

    public static DefaultMvnRepository newInstance() {
        return new DefaultMvnRepository();
    }

    @Override
    public ArtifactVersionHistory lookupArtifactVersions(DependencyInfo dependency) {
        return lookupArtifactVersions(dependency, 1);
    }

    @Override
    public ArtifactVersionHistory lookupArtifactVersions(DependencyInfo dependency, int pageSize) {
        return lookupArtifactVersions(dependency, pageSize, 0);
    }

    @Override
    public ArtifactVersionHistory lookupArtifactVersions(DependencyInfo dependency, int pageSize, int start) {
        final ArtifactVersionHistory versionHistory = ArtifactVersionHistory.of(dependency);

        final AtomicInteger pageStart = new AtomicInteger(start);
        final AtomicInteger resultCount = new AtomicInteger(-1);
        final AtomicInteger searchCount = new AtomicInteger();

        searchForCurrent:
        do {
            final Optional<Document> doc = Optional.ofNullable(getArtifactVersionsUrl(dependency, pageSize, pageStart.get()))
                .map(DefaultMvnRepository::loadDocument);
            searchCount.incrementAndGet();

            if (doc.isPresent()) {
                final Document document = doc.get();
                final Node resultNode = xPathExecutor.xPathNode(document, "/response/result[@name = 'response']");
                final Integer numFound = xPathExecutor.xPathInteger(resultNode, "@numFound");
                // final Integer startOffset = xPathExecutor.xPathInteger(resultNode, "@start");
                final XPathNodes items = xPathExecutor.xPathNodes(resultNode, "doc");

                resultCount.compareAndSet(-1, numFound);

                for (Node item : items) {
                    // final String groupId = xPathExecutor.xPathText(item, "str[name = 'g']");
                    // final String artifactId = xPathExecutor.xPathText(item, "str[name = 'a']");
                    final String version = xPathExecutor.xPathText(item, "str[@name = 'v']");
                    final Instant publishDate = Instant.ofEpochMilli(xPathExecutor.xPathLong(item, "long[@name = 'timestamp']"));

                    final DependencyInfo.Builder dependencyBuilder = DependencyInfo.builder(dependency)
                        .withVersion(version)
                        .withPublishDate(publishDate);
                    Optional.of(item)
                        .filter(Element.class::isInstance)
                        .map(Element.class::cast)
                        .ifPresent(dependencyBuilder::withElement);

                    versionHistory.addVersion(dependencyBuilder.build());

                    if (Objects.equals(dependency.getVersion(), version)) {
                        break searchForCurrent;
                    }
                }

                pageStart.addAndGet(pageSize);

                if (pageSize == 1) {
                    pageSize += 4;
                }
            } else {
                log.atError().log("Failed to fetch artifact information");
                break;
            }
        } while (pageStart.get() <= resultCount.get());

        return versionHistory;
    }

    private static Document loadDocument(String url) {
        final HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(url))
            .GET()
            .build();
        final HttpClient httpClient = HttpClient.newHttpClient();
        try {
            final HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            final String responseBody = httpResponse.body();
            return XmlUtil.loadXmlDocument(responseBody);
        } catch (IOException | InterruptedException ex) {
            log.atError().withThrowable(ex).log();
        }
        return null;
    }
}
