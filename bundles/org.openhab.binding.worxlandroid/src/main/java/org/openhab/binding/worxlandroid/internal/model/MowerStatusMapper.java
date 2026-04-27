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

import org.openhab.binding.worxlandroid.internal.api.dto.AbstractProductItemStatus;

public class MowerStatusMapper {

    public static MowerStatus map(AbstractProductItemStatus dto) {

        MowerType type = MowerModelResolver.resolve(dto.id);

        return new MowerStatus(dto.id, dto.name, dto.serialNumber, dto.online, type);
    }
}
