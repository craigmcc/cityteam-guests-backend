/*
 * Copyright 2020 CityTeam, craigmcc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cityteam.guests.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.cityteam.guests.model.Constants.FACILITY_COLUMN;
import static org.craigmcc.library.model.Constants.PUBLISHED_COLUMN;
import static org.craigmcc.library.model.Constants.UPDATED_COLUMN;
import static org.craigmcc.library.model.Constants.VERSION_COLUMN;

@Category(UnitTests.class)
public class TemplateUnitTest {

    private Template template = new Template();

    @Test
    public void equalsVerifier() {

        EqualsVerifier.forClass(Template.class)
                .usingGetClass()
                .withIgnoredFields(PUBLISHED_COLUMN, UPDATED_COLUMN,
                        VERSION_COLUMN, FACILITY_COLUMN)
                .withPrefabValues(Facility.class,
                        new Facility(null, null, null, null, "First",
                                null, null, null),
                        new Facility(null, null, null, null, "Second",
                                null, null, null)
                )
                .withRedefinedSuperclass()
                .verify();

    }

}
