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

import java.time.LocalDate;

import static org.cityteam.guests.model.Constants.FACILITY_COLUMN;
import static org.cityteam.guests.model.Constants.REGISTRATIONS_COLUMN;
import static org.cityteam.guests.model.Guest.NameComparator;
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
public class GuestUnitTest {

    private Guest guest = new Guest();

    @Test
    public void equalsVerifier() {

        EqualsVerifier.forClass(Guest.class)
                .usingGetClass()
                .withIgnoredFields(PUBLISHED_COLUMN, UPDATED_COLUMN,
                        VERSION_COLUMN, FACILITY_COLUMN, REGISTRATIONS_COLUMN)
                .withPrefabValues(Facility.class,
                        new Facility(null, null, null, null, "First",
                                null, null, null),
                        new Facility(null, null, null, null, "Second",
                                null, null, null)
                        )
                .withPrefabValues(Registration.class,
                        new Registration(1L, null, 1, LocalDate.parse("2020-07-04")),
                        new Registration(2L, null, 2, LocalDate.parse("2020-07-04")))
                .withRedefinedSuperclass()
                .verify();

    }

    @Test
    public void matchNameNegative() {

        guest.setFirstName("Foo");
        guest.setLastName("Bar");

        assertThat(guest.matchNames("Baz"), not(true));
        assertThat(guest.matchNames("baz"), not(true));
        assertThat(guest.matchNames("BAZ"), not(true));
        assertThat(guest.matchNames(" "), not(true));
        assertThat(guest.matchNames(""), not(true));
        assertThat(guest.matchNames(null), not(true));

    }

    @Test
    public void matchNamePositive() {

        guest.setFirstName("Foo");
        guest.setLastName("Bar");

        // Full string
        assertThat(guest.matchNames("Foo Bar"), is(true));
        assertThat(guest.matchNames("Foo bar"), is(true));
        assertThat(guest.matchNames("foo Bar"), is(true));

        // Prefix
        assertThat(guest.matchNames("Foo"), is(true));
        assertThat(guest.matchNames("FOo"), is(true));
        assertThat(guest.matchNames("foo"), is(true));
        assertThat(guest.matchNames("FOO"), is(true));

        // Suffix
        assertThat(guest.matchNames("Bar"), is(true));
        assertThat(guest.matchNames("BAr"), is(true));
        assertThat(guest.matchNames("bar"), is(true));
        assertThat(guest.matchNames("BAR"), is(true));

        // Middle
        assertThat(guest.matchNames("o B"), is(true));
        assertThat(guest.matchNames("o b"), is(true));
        assertThat(guest.matchNames("O b"), is(true));
        assertThat(guest.matchNames("O B"), is(true));

    }

    @Test
    public void NameComparator() {

        Guest first = new Guest();
        first.setFirstName("Foo");
        first.setLastName("Bar");
        Guest second = new Guest();
        second.setFirstName("Baz");
        second.setLastName("Bop");


        assertThat(NameComparator.compare(first, second), lessThan(0));
        assertThat(NameComparator.compare(first, first), comparesEqualTo(0));
        assertThat(NameComparator.compare(second, second), comparesEqualTo(0));
        assertThat(NameComparator.compare(second, first), greaterThan(0));
        assertThat(NameComparator.compare(first, second),
                comparesEqualTo(-NameComparator.compare(second, first)));

    }

}
