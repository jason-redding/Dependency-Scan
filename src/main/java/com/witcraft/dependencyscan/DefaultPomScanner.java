package com.witcraft.dependencyscan;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathNodes;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.function.Predicate.not;

@Log4j2
public final class DefaultPomScanner implements PomScanner {

    public static final Pattern PATTERN_VARIABLE = Pattern.compile("\\$\\{([^}]*)}");

    @Getter
    private final Path file;

    private final Document document;

    private final XPathExecutor xPathExecutor;

    private final Map<String, String> properties;

    private DefaultPomScanner(Path file) throws IOException, SAXException {
        this.file = Objects.requireNonNull(file);
        this.document = XmlUtil.loadXmlDocument(file);
        this.xPathExecutor = XPathExecutor.newInstance();
        this.properties = getProperties();
    }

    @Override
    public Map<String, DependencyInfo> scanDependencyInfo() {
        final XPathNodes managedDependencyNodes = xPathExecutor.xPathNodes(document, "/project/dependencyManagement/dependencies/dependency");
        final Map<String, DependencyInfo> managedDependencies = buildDependencyInfo(managedDependencyNodes);

        final XPathNodes dependencyNodes = xPathExecutor.xPathNodes(document, "/project/dependencies/dependency");
        final Map<String, DependencyInfo> directDependencies = buildDependencyInfo(dependencyNodes, (info) -> {
            final String version = info.getVersion();
            if (version == null || version.isBlank()) {
                final DependencyInfo managedDependencyInfo = managedDependencies.get(info.getArtifactPath());
                if (managedDependencyInfo != null) {
                    info.withVersion(managedDependencyInfo.getVersion());
                }
            }
        });

        return directDependencies;
    }

    private Map<String, DependencyInfo> buildDependencyInfo(XPathNodes dependencyNodes) {
        return buildDependencyInfo(dependencyNodes, new HashMap<>());
    }

    private Map<String, DependencyInfo> buildDependencyInfo(XPathNodes dependencyNodes, Consumer<DependencyInfo.Builder> consumer) {
        return buildDependencyInfo(dependencyNodes, new HashMap<>(), consumer);
    }

    private Map<String, DependencyInfo> buildDependencyInfo(XPathNodes dependencyNodes, Map<String, DependencyInfo> dependencies) {
        return buildDependencyInfo(dependencyNodes, dependencies, null);
    }

    private Map<String, DependencyInfo> buildDependencyInfo(XPathNodes dependencyNodes, Map<String, DependencyInfo> dependencies, Consumer<DependencyInfo.Builder> consumer) {
        if (dependencyNodes != null) {
            for (Node dependencyNode : dependencyNodes) {
                if (dependencyNode instanceof Element element) {
                    final String groupId = resolveVariables(properties, xPathExecutor.xPathText(element, "groupId"));
                    final String artifactId = resolveVariables(properties, xPathExecutor.xPathText(element, "artifactId"));
                    final String version = resolveVariables(properties, xPathExecutor.xPathText(element, "version"));
                    final String scope = Optional.ofNullable(resolveVariables(properties, xPathExecutor.xPathText(element, "scope")))
                        .filter(not(String::isBlank))
                        .orElse("compile");
                    final String artifactPath = "%s:%s".formatted(groupId, artifactId);
                    final DependencyInfo.Builder dependencyBuilder = DependencyInfo.builder()
                        .withElement(element)
                        .withGroupId(groupId)
                        .withArtifactId(artifactId)
                        .withVersion(version)
                        .withScope(scope);
                    if (consumer != null) {
                        consumer.accept(dependencyBuilder);
                    }
                    dependencies.put(artifactPath, dependencyBuilder.build());
                }
            }
        }
        return dependencies;
    }

    private Map<String, String> getProperties() {
        final Map<String, String> properties = new HashMap<>();
        final XPathNodes propertyNodes = xPathExecutor.xPathNodes(document, "/project/properties/*");
        for (Node propertyNode : propertyNodes) {
            final String propertyName = propertyNode.getNodeName();
            final String propertyValue = Optional.ofNullable(propertyNode.getTextContent())
                .map(String::trim)
                .orElse("");

            properties.put(propertyName, propertyValue);
        }
        return properties;
    }

    private String resolveVariables(Map<String, String> properties, String value) {
        if (value == null) {
            return null;
        }

        final Matcher matcher = PATTERN_VARIABLE.matcher(value);
        final StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            matcher.appendReplacement(result, properties.getOrDefault(matcher.group(1), ""));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public static Builder builder(Path file) {
        return new Builder(file);
    }

    public final static class Builder {
        private final Path pomFile;

        private Builder(Path file) {
            this.pomFile = file;
        }

        public PomScanner build() throws IOException, SAXException {
            return new DefaultPomScanner(pomFile);
        }
    }
}
