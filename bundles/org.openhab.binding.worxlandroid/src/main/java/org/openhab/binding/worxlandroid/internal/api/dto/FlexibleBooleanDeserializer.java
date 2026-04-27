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

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class FlexibleBooleanDeserializer implements JsonDeserializer<Boolean> {

    @Override
    public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }

        if (json.isJsonPrimitive()) {
            var prim = json.getAsJsonPrimitive();

            if (prim.isBoolean()) {
                return prim.getAsBoolean();
            }

            if (prim.isNumber()) {
                return prim.getAsInt() != 0;
            }

            if (prim.isString()) {
                String val = prim.getAsString();
                return "1".equals(val) || "true".equalsIgnoreCase(val);
            }
        }

        return false;
    }
}
