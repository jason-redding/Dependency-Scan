package com.witcraft.dependencyscan;

import org.w3c.dom.Node;

import javax.xml.xpath.XPathNodes;
import java.util.Optional;

public interface XPathExecutor {
    static XPathExecutor newInstance() {
        return new DefaultXPathExecutor();
    }

    Node xPathNode(Node context, String xPath);

    Node xPathNodeRequired(Node context, String xPath, String message);

    XPathNodes xPathNodes(Node context, String xPath);

    XPathNodes xPathNodesRequired(Node context, String xPath, String message);

    Boolean xPathBoolean(Node context, String xPath);

    Boolean xPathBooleanRequired(Node context, String xPath, String message);

    Double xPathDouble(Node context, String xPath);

    Integer xPathInteger(Node context, String xPath);

    Long xPathLong(Node context, String xPath);

    Integer xPathIntegerRequired(Node context, String xPath, String message);

    Number xPathDoubleRequired(Node context, String xPath, String message);

    Long xPathLongRequired(Node context, String xPath, String message);

    String xPathText(Node context, String xPath);

    String xPathTextRequired(Node context, String xPath, String message);

    <T> Optional<T> xPath(Node context, String xPath, Class<T> type);

    <T> T xPathRequired(Node context, String xPath, Class<T> type, String message);
}
