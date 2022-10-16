// SPDX-FileCopyrightText: 2022 Josef Andersson
//
// SPDX-License-Identifier: Apache-2.0
package se.janderssonse.sariftool.mapper;

public final class MapperFactory {

    private MapperFactory() {
    }

    public static Mapper getMapper(final Mapper.MapperType type) {
        return switch (type) {
            case SONAR -> new SonarMapper();
            case LOG -> new LogMapper();
        };
    }

}
