// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0
package se.janderssonse.sariftool.mapper;

import se.janderssonse.sariftool.SarifParser;

public final class MapperFactory {

    private MapperFactory() {
    }

    public static Mapper getMapper(final Mapper.MapperType type, final SarifParser parser) {
        return switch (type) {
            case SONAR -> new SonarMapper(parser);
        };
    }

}
