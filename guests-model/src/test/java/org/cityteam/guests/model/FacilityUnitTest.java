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

import static org.cityteam.guests.model.Facility.NameComparator;
import static org.craigmcc.library.model.Constants.PUBLISHED_COLUMN;
import static org.craigmcc.library.model.Constants.UPDATED_COLUMN;
import static org.craigmcc.library.model.Constants.VERSION_COLUMN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;

@Category(UnitTests.class)
public class FacilityUnitTest {

    private Facility facility = new Facility();

    @Test
    public void equalsVerifier() {

        EqualsVerifier.forClass(Facility.class)
                .usingGetClass()
                .withIgnoredFields(PUBLISHED_COLUMN, UPDATED_COLUMN, VERSION_COLUMN)
                .withRedefinedSuperclass()
                .verify();

    }

    @Test
    public void matchNameNegative() {

        facility.setName("Foo Bar");

        assertThat(facility.matchName("Baz"), not(true));
        assertThat(facility.matchName("baz"), not(true));
        assertThat(facility.matchName("BAZ"), not(true));
        assertThat(facility.matchName(" "), not(true));
        assertThat(facility.matchName(""), not(true));
        assertThat(facility.matchName(null), not(true));

    }

    @Test
    public void matchNamePositive() {

        facility.setName("Foo Bar");

        // Full string
        assertThat(facility.matchName("Foo Bar"), is(true));
        assertThat(facility.matchName("Foo bar"), is(true));
        assertThat(facility.matchName("foo Bar"), is(true));

        // Prefix
        assertThat(facility.matchName("Foo"), is(true));
        assertThat(facility.matchName("FOo"), is(true));
        assertThat(facility.matchName("foo"), is(true));
        assertThat(facility.matchName("FOO"), is(true));

        // Suffix
        assertThat(facility.matchName("Bar"), is(true));
        assertThat(facility.matchName("BAr"), is(true));
        assertThat(facility.matchName("bar"), is(true));
        assertThat(facility.matchName("BAR"), is(true));

        // Middle
        assertThat(facility.matchName("o B"), is(true));
        assertThat(facility.matchName("o b"), is(true));
        assertThat(facility.matchName("O b"), is(true));
        assertThat(facility.matchName("O B"), is(true));

    }

    @Test
    public void NameComparator() {

        Facility first = new Facility();
        first.setName("Bar");
        Facility second = new Facility();
        second.setName("Foo");

        assertThat(NameComparator.compare(first, second), lessThan(0));
        assertThat(NameComparator.compare(first, first), comparesEqualTo(0));
        assertThat(NameComparator.compare(second, second), comparesEqualTo(0));
        assertThat(NameComparator.compare(second, first), greaterThan(0));
        assertThat(NameComparator.compare(first, second),
                comparesEqualTo(-NameComparator.compare(second, first)));

    }

}
