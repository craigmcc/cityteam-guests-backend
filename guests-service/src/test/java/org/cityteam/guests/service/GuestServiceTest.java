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
package org.cityteam.guests.service;

import org.cityteam.guests.model.Facility;
import org.cityteam.guests.model.Guest;
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.NotFound;
import org.craigmcc.library.shared.exception.NotUnique;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

import static org.cityteam.guests.model.Constants.FACILITY_ID_COLUMN;
import static org.cityteam.guests.model.Constants.FACILITY_NAME;
import static org.cityteam.guests.model.Constants.FIRST_NAME_COLUMN;
import static org.cityteam.guests.model.Constants.GUEST_NAME;
import static org.cityteam.guests.model.Constants.LAST_NAME_COLUMN;
import static org.cityteam.guests.model.Constants.NAME_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThrows;

@Category(ServiceTests.class)
@RunWith(Arquillian.class)
public class GuestServiceTest extends AbstractServiceTest {

    // Configuration and Injections ------------------------------------------

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive archive = ShrinkWrap.create
                (JavaArchive.class, "testGuest.jar")
                .addClass(GuestService.class);
        addServiceFixtures(archive, false);
        System.out.println("GuestServiceTest: Assembled Archive:");
        System.out.println(archive.toString(true));
        return archive;
    }

    @Inject
    DevModeDepopulateService devModeDepopulateService;

    @Inject
    DevModePopulateService devModePopulateService;

    @PersistenceContext
    EntityManager entityManager;

    @Inject
    GuestService guestService;

    // Lifecycle Methods -----------------------------------------------------

    @After
    public void after() {
        devModeDepopulateService.depopulate();
    }

    @Before
    public void before() {
        devModePopulateService.populate();
    }

    // Test Methods ----------------------------------------------------------

    // delete() tests

    @Test
    public void deleteHappy() throws Exception {

        List<Guest> guests = findGuestsAll();
        assertThat(guests.size(), is(greaterThan(0)));

        for (Guest guest : guests) {

            // Delete and verify we can no longer retrieve it
            guestService.delete(guest.getId());
            assertThat(findGuestById(guest.getId()).isPresent(),
                    is(false));

            // TODO - Delete should have cascaded to related models
/*
            assertThat(findBansByGuestId(guest.getId()).size(), is(0));
            assertThat(findRegistrationsByGuestId(guest.getId()).size(), is(0));
*/

        }

        assertThat(guestService.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {

        assertThrows(NotFound.class,
                () -> guestService.delete(Long.MAX_VALUE));

    }

    // find() tests

    @Test
    public void findHappy() throws Exception {

        List<Guest> guests = findGuestsAll();
        assertThat(guests.size(), is(greaterThan(0)));

        for (Guest guest : guests) {
            Guest found = guestService.find(guest.getId());
            assertThat(found.equals(guest), is(true));
        }

    }

    @Test
    public void findNotFound() throws Exception {

        assertThrows(NotFound.class,
                () -> guestService.find(Long.MAX_VALUE));

    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        List<Guest> guests = guestService.findAll();
        assertThat(guests.size(), is(greaterThan(0)));

        String previousName = null;
        for (Guest guest : guests) {
            String thisName = guest.getFacilityId() +
                    "|" +guest.getLastName() + "|" + guest.getFirstName();
            if (previousName != null) {
                assertThat(thisName, is(greaterThan(previousName)));
            }
            previousName = thisName;
        }

    }

    // findByFacilityId() tests

    @Test
    public void findByFacilityIdHappy() throws Exception {

        String facilityName = "Chester";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        List<Guest> guests = guestService.findByFacilityId
                (facility.get().getId());
        assertThat(guests.size(), is(greaterThan(0)));

        String previousName = null;
        for (Guest guest : guests) {
            assertThat(guest.getComments(), startsWith(facilityName));
            assertThat(guest.getFacilityId(), is(equalTo(facility.get().getId())));
            String thisName = guest.getLastName() + "|" + guest.getFirstName();
            if (previousName != null) {
                assertThat(thisName, is(greaterThan(previousName)));
            }
            previousName = thisName;
        }

    }

    @Test
    public void findByFacilityIdNoneFound() throws Exception {

        // Invalid facilityId
        List<Guest> guests1 = guestService.findByFacilityId(Long.MAX_VALUE);
        assertThat(guests1.size(), is(equalTo(0)));

        // No guests for facilityId
        Optional<Facility> facility = findFacilityByNameExact("Oakland");
        assertThat(facility.isPresent(), is(true));
        List<Guest> guests2 = guestService.findByFacilityId(facility.get().getId());
        assertThat(guests2.size(), is(equalTo(0)));

    }

    // findByName() tests

    @Test
    public void findByNameHappy() throws Exception {

        String facilityName = "Portland";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        List<Guest> guests = guestService.findByName
                (facility.get().getId(), "ubbl");
        assertThat(guests.size(), is(greaterThan(0)));

        String previousName = null;
        for (Guest guest : guests) {
            assertThat(guest.getComments(), startsWith(facilityName));
            assertThat(guest.getFacilityId(), is(equalTo(facility.get().getId())));
            String thisName = guest.getLastName() + "|" + guest.getFirstName();
            if (previousName != null) {
                assertThat(thisName, is(greaterThan(previousName)));
            }
            previousName = thisName;
        }

    }

    @Test
    public void findByNameNoMatch() throws Exception {

        String facilityName = "San Jose";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        List<Guest> guests = guestService.findByName
                (facility.get().getId(), "unmatched");
        assertThat(guests.size(), is(equalTo(0)));

    }

    // findByNameExact() tests

    @Test
    public void findByNameExactHappy() throws Exception {

        String facilityName = "San Francisco";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        List<Guest> guests = findGuestsByFacilityId(facility.get().getId());
        assertThat(guests.size(), is(greaterThan(0)));

        for (Guest guest : guests) {
            Guest found = guestService.findByNameExact(
                    guest.getFacilityId(),
                    guest.getFirstName(),
                    guest.getLastName()
            );
            assertThat(found.getId(), is(equalTo(guest.getId())));
        }

    }

    @Test
    public void findByNameExactNotFound() throws Exception {

        // Invalid facilityId
        assertThrows(NotFound.class,
                () -> guestService.findByNameExact(
                        Long.MAX_VALUE,
                        "Fred",
                        "Flintstone")
        );

        // Mismatched firstName
        String facilityName1 = "Chester";
        Optional<Facility> facility1 = findFacilityByNameExact(facilityName1);
        assertThat(facility1.isPresent(), is(true));
        assertThrows(NotFound.class,
                () -> guestService.findByNameExact(
                        facility1.get().getId(),
                        "Wrong First Name",
                        "Rubble")
        );

        // Mismatched lastName
        String facilityName2 = "Portland";
        Optional<Facility> facility2 = findFacilityByNameExact(facilityName2);
        assertThat(facility2.isPresent(), is(true));
        assertThrows(NotFound.class,
                () -> guestService.findByNameExact(
                        facility2.get().getId(),
                        "Bam Bam",
                        "Wrong Last Name")
        );

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {

        String facilityName = "Chester";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        Guest guest = newGuest(facility.get().getId());
        Guest inserted = guestService.insert(guest);

        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getPublished(), is(notNullValue()));
        assertThat(inserted.getUpdated(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        assertThat(findGuestById(inserted.getId()).isPresent(), is(true));

    }

    @Test
    public void insertBadRequest() throws Exception {

        String facilityName = "Chester";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        // Completely empty instance
        final Guest guest0 = new Guest();
        assertThrows(BadRequest.class,
                () -> guestService.insert(guest0));

        // Missing facilityId field
        final Guest guest1 = newGuest(facility.get().getId());
        guest1.setFacilityId(null);
        assertThrows(BadRequest.class,
                () -> guestService.insert(guest1));

        // Invalid facilityId field
        final Guest guest2 = newGuest(facility.get().getId());
        guest2.setFacilityId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> guestService.insert(guest2));

        // Missing firstName field
        final Guest guest3 = newGuest(facility.get().getId());
        guest3.setFirstName(null);
        assertThrows(BadRequest.class,
                () -> guestService.insert(guest3));

        // Missing lastName field
        final Guest guest4 = newGuest(facility.get().getId());
        guest4.setLastName(null);
        assertThrows(BadRequest.class,
                () -> guestService.insert(guest4));

    }

    @Test
    public void insertNotUnique() throws Exception {

        String facilityName = "Chester";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        Guest guest = newGuest(facility.get().getId());
        Guest inserted = guestService.insert(guest);
        assertThrows(NotUnique.class,
                () -> guestService.insert(inserted));

    }

    // update() tests

    @Test
    public void updateHappy() throws Exception {

        String facilityName = "Portland";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        // Change something but keep firstName/lastName
        Guest guest1 = findGuestByNameExact(
                facility.get().getId(),
                "Fred",
                "Flintstone").get();
        guest1.setComments(guest1.getComments() + " Updated");
        guestService.update(guest1.getId(), guest1);

        // Change firstName to something unique
        Guest guest2 = findGuestByNameExact(
                facility.get().getId(),
                "Fred",
                "Flintstone").get();
        guest2.setFirstName(guest2.getFirstName() + " Updated");
        guestService.update(guest2.getId(), guest2);

        // Change lastName to something unique
        Guest guest3 = findGuestByNameExact(
                facility.get().getId(),
                "Barney",
                "Rubble").get();
        guest2.setLastName(guest3.getLastName() + " Updated");
        guestService.update(guest3.getId(), guest3);

    }

    @Test
    public void updateBadRequest() throws Exception {

        String facilityName = "San Francisco";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));
        String firstName = "Fred";
        String lastName = "Flintstone";
        Optional<Guest> original = findGuestByNameExact(
                facility.get().getId(),
                firstName,
                lastName
        );

        // Completely empty instance
        final Guest guest0 = new Guest();
        assertThrows(BadRequest.class,
                () -> guestService.update(original.get().getId(), guest0));

        // Missing facilityId field
        final Guest guest1 = findGuestByNameExact(
                facility.get().getId(),
                firstName,
                lastName
        ).get();
        guest1.setFacilityId(null);
        assertThrows(BadRequest.class,
                () -> guestService.update(original.get().getId(), guest1));

        // Invalid facilityId field
        final Guest guest2 = findGuestByNameExact(
                facility.get().getId(),
                firstName,
                lastName
        ).get();
        guest2.setFacilityId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> guestService.update(original.get().getId(), guest2));

        // Missing firstName field
        final Guest guest3 = findGuestByNameExact(
                facility.get().getId(),
                firstName,
                lastName
        ).get();
        guest3.setFirstName(null);
        assertThrows(BadRequest.class,
                () -> guestService.update(original.get().getId(), guest3));

        // Missing lastName field
        final Guest guest4 = findGuestByNameExact(
                facility.get().getId(),
                firstName,
                lastName
        ).get();
        guest4.setLastName(null);
        assertThrows(BadRequest.class,
                () -> guestService.update(original.get().getId(), guest4));

    }

    @Test
    public void updateNotUnique() throws Exception {

        String facilityName = "San Francisco";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));
        String firstName = "Fred";
        String lastName = "Flintstone";
        Optional<Guest> original = findGuestByNameExact(
                facility.get().getId(),
                firstName,
                lastName
        );

        // Violate name uniqueness in same facility
        final Guest guest5 = findGuestByNameExact(
                facility.get().getId(),
                firstName,
                lastName
        ).get();
        guest5.setFirstName("Barney");
        guest5.setLastName("Rubble");
        assertThrows(NotUnique.class,
                () -> guestService.update(original.get().getId(), guest5));

        // Violate name uniqueness in different facility
        final Guest guest6 = findGuestByNameExact(
                facility.get().getId(),
                firstName,
                lastName
        ).get();
        Optional<Facility> facility2 = findFacilityByNameExact("San Jose");
        assertThat(facility2.isPresent(), is(true));
        assertThat(facility2.get().getId(), is(not(equalTo(facility.get().getId()))));
        guest6.setFacilityId(facility2.get().getId());
        assertThrows(NotUnique.class,
                () -> guestService.update(original.get().getId(), guest6));

    }

    // Support Methods -------------------------------------------------------

    private Optional<Facility> findFacilityByNameExact(String name) {
        TypedQuery<Facility> query = entityManager.createNamedQuery
                (FACILITY_NAME + ".findByNameExact", Facility.class)
                .setParameter(NAME_COLUMN, name);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    private Optional<Guest> findGuestById(Long guestId) {
        TypedQuery<Guest> query = entityManager.createNamedQuery
                (GUEST_NAME + ".findById", Guest.class)
                .setParameter(ID_COLUMN, guestId);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    private Optional<Guest> findGuestByNameExact
            (Long facilityId, String firstName, String lastName) {
        TypedQuery<Guest> query = entityManager.createNamedQuery
                (GUEST_NAME + ".findByNameExact", Guest.class)
                .setParameter(FACILITY_ID_COLUMN, facilityId)
                .setParameter(FIRST_NAME_COLUMN, firstName)
                .setParameter(LAST_NAME_COLUMN, lastName);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    private List<Guest> findGuestsAll() {
        return entityManager.createNamedQuery
                (GUEST_NAME + ".findAll", Guest.class)
                .getResultList();
    }

    private List<Guest> findGuestsByFacilityId(Long facilityId) {
        return entityManager.createNamedQuery
                (GUEST_NAME + ".findByFacilityId", Guest.class)
                .setParameter(FACILITY_ID_COLUMN, facilityId)
                .getResultList();
    }

    private Guest newGuest(Long facilityId) {
        return new Guest(
                "George Comment",
                facilityId,
                "George",
                "Jetson"
        );
    }

}
