package com.witcraft.dependencyscan;

import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathNodes;
import java.util.NoSuchElementException;
import java.util.Optional;

@Log4j2
public final class DefaultXPathExecutor implements XPathExecutor {

    private final XPath xPath;

    DefaultXPathExecutor() {
        final XPathFactory xPathFactory = XPathFactory.newInstance();
        xPath = xPathFactory.newXPath();
    }

    @Override
    public Node xPathNode(Node context, String xPath) {
        return xPath(context, xPath, Node.class).orElse(null);
    }

    @Override
    public Node xPathNodeRequired(Node context, String xPath, String message) {
        return xPathRequired(context, xPath, Node.class, message);
    }

    @Override
    public XPathNodes xPathNodes(Node context, String xPath) {
        return xPath(context, xPath, XPathNodes.class).orElse(null);
    }

    @Override
    public XPathNodes xPathNodesRequired(Node context, String xPath, String message) {
        return xPathRequired(context, xPath, XPathNodes.class, message);
    }

    @Override
    public Boolean xPathBoolean(Node context, String xPath) {
        return xPath(context, xPath, Boolean.class).orElse(null);
    }

    @Override
    public Boolean xPathBooleanRequired(Node context, String xPath, String message) {
        return xPathRequired(context, xPath, Boolean.class, message);
    }

    @Override
    public Double xPathDouble(Node context, String xPath) {
        return xPath(context, xPath, Double.class).orElse(null);
    }

    @Override
    public Integer xPathInteger(Node context, String xPath) {
        return xPath(context, xPath, Integer.class).orElse(null);
    }

    @Override
    public Long xPathLong(Node context, String xPath) {
        return xPath(context, xPath, Long.class).orElse(null);
    }

    @Override
    public Double xPathDoubleRequired(Node context, String xPath, String message) {
        return xPathRequired(context, xPath, Double.class, message);
    }

    @Override
    public Integer xPathIntegerRequired(Node context, String xPath, String message) {
        return xPathRequired(context, xPath, Integer.class, message);
    }

    @Override
    public Long xPathLongRequired(Node context, String xPath, String message) {
        return xPathRequired(context, xPath, Long.class, message);
    }

    @Override
    public String xPathText(Node context, String xPath) {
        return xPath(context, xPath, String.class).map(String::trim).orElse(null);
    }

    @Override
    public String xPathTextRequired(Node context, String xPath, String message) {
        return xPathRequired(context, xPath, String.class, message);
    }

    @Override
    public <T> Optional<T> xPath(Node context, String xPath, Class<T> type) {
        try {
            return Optional.ofNullable(this.xPath.evaluateExpression(xPath, context, type));
        } catch (XPathExpressionException ex) {
            log.atError().withThrowable(ex).log();
        }
        return Optional.empty();
    }

    @Override
    public <T> T xPathRequired(Node context, String xPath, Class<T> type, String message) {
        return xPath(context, xPath, type)
            .filter(DefaultXPathExecutor::isResultNotEmpty)
            .orElseThrow(() -> new NoSuchElementException(message));
    }

    private static boolean isResultNotEmpty(Object result) {
        return !(result == null || (result instanceof String text && text.isBlank()) || (result instanceof XPathNodes nodes && nodes.size() == 0));
    }
}
