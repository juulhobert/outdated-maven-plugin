package com.giovds.dto;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PomResponseTest {
    @Test
    public void should_deserialize_example_pom_xml() throws IOException {
        final XmlMapper xmlMapper = new XmlMapper();

        var mavenApiPom = getClass().getResourceAsStream("/example-pom.xml");
        var pomResponse = xmlMapper.readValue(mavenApiPom, PomResponse.class);

        assertEquals("https://github.com/giovds/outdated-maven-plugin/", pomResponse.url());
    }
}
