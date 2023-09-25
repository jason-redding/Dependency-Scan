package com.witcraft.dependencyscan;

import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;

@Log4j2
public final class XmlUtil {

    private static final DocumentBuilder DOCUMENT_BUILDER = initDocumentBuilder();

    private static DocumentBuilder initDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            log.atError().withThrowable(ex).log();
        }
        return null;
    }

    private XmlUtil() {
    }

    public static Document loadXmlDocument(Path path) {
        if (DOCUMENT_BUILDER != null) {
            try {
                return DOCUMENT_BUILDER.parse(path.toFile());
            } catch (SAXException | IOException ex) {
                log.atError().withThrowable(ex).log();
            }
        }
        return null;
    }

    public static Document loadXmlDocument(String content) {
        if (DOCUMENT_BUILDER != null) {
            try {
                return DOCUMENT_BUILDER.parse(new InputSource(new StringReader(content)));
            } catch (SAXException | IOException ex) {
                log.atError().withThrowable(ex).log();
            }
        }
        return null;
    }
}
