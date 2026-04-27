/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.worxlandroid.internal.model;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MowerModelResolver {

    private static final Map<String, MowerType> MODEL_TYPES = loadModels();

    private MowerModelResolver() {
        // utility class
    }

    public static MowerType resolve(String id) {
        return MODEL_TYPES.getOrDefault(id, MowerType.WIRED);
    }

    private static Map<String, MowerType> loadModels() {
        Map<String, MowerType> map = new HashMap<>();

        try (InputStream is = MowerModelResolver.class.getResourceAsStream("/mower-models.xml")) {

            if (is == null) {
                throw new RuntimeException("mower-models.xml not found in resources");
            }

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(is);

            NodeList models = doc.getElementsByTagName("model");

            for (int i = 0; i < models.getLength(); i++) {
                Element element = (Element) models.item(i);

                String id = element.getElementsByTagName("id").item(0).getTextContent();

                String type = element.getElementsByTagName("type").item(0).getTextContent();

                map.put(id, MowerType.valueOf(type));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error loading mower models", e);
        }

        return map;
    }
}
