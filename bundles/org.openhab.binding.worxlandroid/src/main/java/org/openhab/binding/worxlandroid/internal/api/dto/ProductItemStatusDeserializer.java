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
package org.openhab.binding.worxlandroid.internal.api.dto;

import java.io.IOException;
import java.util.Map;

import org.openhab.binding.worxlandroid.internal.model.MowerModelResolver;
import org.openhab.binding.worxlandroid.internal.model.MowerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ProductItemStatusDeserializer extends JsonDeserializer<AbstractProductItemStatus> {

    private static final Map<MowerType, Class<? extends AbstractProductItemStatus>> TYPE_CLASS_MAP = Map
            .of(MowerType.WIRED, ProductItemStatus.class, MowerType.VISION, ProductItemStatusVision.class);

    private final Logger logger = LoggerFactory.getLogger(ProductItemStatusDeserializer.class);

    @Override
    public AbstractProductItemStatus deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {

        logger.debug("ProductItemStatusDeserializer - 1");
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        logger.debug("ProductItemStatusDeserializer - 2");
        ObjectNode node = mapper.readTree(parser);
        logger.debug("ProductItemStatusDeserializer - 3");
        String id = node.get("product_id").asText();
        logger.debug("ProductItemStatusDeserializer - 4 === ID = {}", id);

        MowerType type = MowerModelResolver.resolve(id);
        logger.debug("ProductItemStatusDeserializer - 5 ==== type = {}", type);

        Class<? extends AbstractProductItemStatus> clazz = TYPE_CLASS_MAP.get(type);
        logger.debug("ProductItemStatusDeserializer - 6");

        return mapper.convertValue(node, clazz);
    }
}
