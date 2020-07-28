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

import org.cityteam.guests.model.Ban;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.cityteam.guests.model.Constants.BAN_NAME;
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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThrows;

@Category(ServiceTests.class)
@RunWith(Arquillian.class)
public class BanServiceTest extends AbstractServiceTest {

    // Configuration and Injections ------------------------------------------

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive archive = ShrinkWrap.create
                (JavaArchive.class, "testBan.jar")
                .addClasses(BanService.class, GuestService.class);
        addServiceFixtures(archive, false);
        System.out.println("BanServiceTest: Assembled Archive:");
        System.out.println(archive.toString(true));
        return archive;
    }

    @Inject
    BanService banService;

    @Inject
    DevModeDepopulateService devModeDepopulateService;

    @Inject
    DevModePopulateService devModePopulateService;

    @PersistenceContext
    EntityManager entityManager;

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

        System.out.println("TRYING: deleteHappy()");
        List<Ban> bans = findBansAll();
        assertThat(bans.size(), is(greaterThan(0)));

        for (Ban ban : bans) {

            // Delete and verify we can no longer retrieve it
            banService.delete(ban.getId());
            assertThat(findBanById(ban.getId()).isPresent(),
                    is(false));

        }

        assertThat(banService.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {

        assertThrows(NotFound.class,
                () -> banService.delete(Long.MAX_VALUE));

    }

    // find() tests

    @Test
    public void findHappy() throws Exception {

        List<Ban> bans = findBansAll();
        assertThat(bans.size(), is(greaterThan(0)));

        for (Ban ban : bans) {
            Ban found = banService.find(ban.getId());
            assertThat(found.equals(ban), is(true));
        }

    }

    @Test
    public void findNotFound() throws Exception {

        assertThrows(NotFound.class,
                () -> banService.find(Long.MAX_VALUE));

    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        List<Ban> bans = banService.findAll();
        assertThat(bans.size(), is(greaterThan(0)));

        String previousKey = null;
        for (Ban ban : bans) {
            String thisKey = "" + ban.getGuestId() + "|" +ban.getBanFrom();
            if (previousKey != null) {
                assertThat(thisKey, is(greaterThan(previousKey)));
            }
            previousKey = thisKey;
        }

    }

    // findByGuestId() tests

    @Test
    public void findByGuestIdHappy() throws Exception {

        String facilityName = "San Francisco";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        List<Guest> guests = findGuestsByFacilityId
                (facility.get().getId());
        assertThat(guests.size(), is(greaterThan(0)));

        for (Guest guest : guests) {
            List<Ban> bans = banService.findByGuestId(guest.getId());
            String previousKey = null;
            for (Ban ban : bans) {
                assertThat(ban.getComments(), startsWith(facilityName));
                String thisKey = ban.getBanFrom().toString();
                if (previousKey != null) {
                    assertThat(thisKey, is(greaterThan(previousKey)));
                }
                previousKey = thisKey;
            }
        }

    }

    @Test
    public void findByGuestIdNoneFound() throws Exception {

        String facilityName = "San Francisco";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        // No bans for guestId
        Optional<Guest> guest = findGuestByNameExact(facility.get().getId(),
            "Bam Bam", "Rubble");
        assertThat(guest.isPresent(), is(true));
        List<Ban> bans1 = banService.findByGuestId(guest.get().getId());
        assertThat(bans1.size(), is(0));

        // Invalid guestId
        List<Ban> bans2 = banService.findByGuestId(Long.MAX_VALUE);
        assertThat(bans2.size(), is(0));

    }

    // findByGuestIdAndRegistrationDate() tests

    @Test
    public void findByGuestIdAndRegistrationDateHappy() throws Exception {

        String facilityName = "San Francisco";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        Optional<Guest> guest = findGuestByNameExact(facility.get().getId(),
                "Fred", "Flintstone");
        assertThat(guest.isPresent(), is(true));

        banService.findByGuestIdAndRegistrationDate
                (guest.get().getId(), LocalDate.parse("2020-08-01"));
        banService.findByGuestIdAndRegistrationDate
                (guest.get().getId(), LocalDate.parse("2020-08-15"));
        banService.findByGuestIdAndRegistrationDate
                (guest.get().getId(), LocalDate.parse("2020-08-31"));
        banService.findByGuestIdAndRegistrationDate
                (guest.get().getId(), LocalDate.parse("2020-10-01"));
        banService.findByGuestIdAndRegistrationDate
                (guest.get().getId(), LocalDate.parse("2020-10-15"));
        banService.findByGuestIdAndRegistrationDate
                (guest.get().getId(), LocalDate.parse("2020-10-31"));

    }

    @Test
    public void findByGuestIdAndRegistrationDateNotFound() throws Exception {

        String facilityName = "San Francisco";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        Optional<Guest> guest = findGuestByNameExact(facility.get().getId(),
                "Barney", "Rubble");
        assertThat(guest.isPresent(), is(true));

        assertThrows(NotFound.class,
                () -> banService.findByGuestIdAndRegistrationDate
                        (guest.get().getId(),
                                LocalDate.parse("2020-08-15")));
        assertThrows(NotFound.class,
                () -> banService.findByGuestIdAndRegistrationDate
                        (guest.get().getId(),
                                LocalDate.parse("2020-10-15")));
        assertThrows(NotFound.class,
                () -> banService.findByGuestIdAndRegistrationDate
                        (guest.get().getId(),
                                LocalDate.parse("2020-12-15")));

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {

        String facilityName = "San Francisco";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        Optional<Guest> guest = findGuestByNameExact(facility.get().getId(),
                "Bam Bam", "Rubble");
        assertThat(guest.isPresent(), is(true));

        Ban ban = newBan(guest.get().getId());
        Ban inserted = banService.insert(ban);
        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getPublished(), is(notNullValue()));
        assertThat(inserted.getUpdated(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));

        Optional<Ban> found = findBanById(inserted.getId());
        assertThat(found.isPresent(), is(true));
        assertThat(found.get(), is(equalTo(inserted)));

    }

    @Test
    public void insertBadRequest() throws Exception {

        String facilityName = "Oakland";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        Optional<Guest> guest = findGuestByNameExact(facility.get().getId(),
                "Bam Bam", "Rubble");
        assertThat(guest.isPresent(), is(true));

        // Missing guestId field
        final Ban ban1 = newBan(guest.get().getId());
        ban1.setGuestId(null);
        assertThrows(BadRequest.class,
                () -> banService.insert(ban1));

        // Invalid guestId field
        final Ban ban2 = newBan(guest.get().getId());
        ban2.setGuestId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> banService.insert(ban2));

        // Missing banFrom field
        final Ban ban3 = newBan(guest.get().getId());
        ban3.setBanFrom(null);
        assertThrows(BadRequest.class,
                () -> banService.insert(ban3));

        // Missing banTo field
        final Ban ban4 = newBan(guest.get().getId());
        ban4.setBanTo(null);
        assertThrows(BadRequest.class,
                () -> banService.insert(ban4));

        // Out of order ban dates
        final Ban ban5 = newBan(guest.get().getId());
        ban5.setBanFrom(LocalDate.parse("2012-07-15"));
        ban5.setBanTo(LocalDate.parse("2012-07-10"));
        assertThrows(BadRequest.class,
                () -> banService.insert(ban5));

    }

    @Test
    public void insertNotUnique() throws Exception {

        String facilityName = "San Francisco";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        Optional<Guest> guest = findGuestByNameExact(facility.get().getId(),
                "Barney", "Rubble");
        assertThat(guest.isPresent(), is(true));

        // Proposed from date matches existing to date
        Ban ban1 = newBan(guest.get().getId());
        ban1.setBanFrom(LocalDate.parse("2020-09-30"));
        ban1.setBanTo(LocalDate.parse("2020-10-01"));
        assertThrows(NotUnique.class,
                () -> banService.insert(ban1));

        // Proposed from date inside existing ban
        Ban ban2 = newBan(guest.get().getId());
        ban2.setBanFrom(LocalDate.parse("2020-09-29"));
        ban2.setBanTo(LocalDate.parse("2020-10-01"));
        assertThrows(NotUnique.class,
                () -> banService.insert(ban2));

        // Proposed to date matches existing from date
        Ban ban3 = newBan(guest.get().getId());
        ban3.setBanFrom(LocalDate.parse("2020-08-05"));
        ban3.setBanTo(LocalDate.parse("2020-09-01"));
        assertThrows(NotUnique.class,
                () -> banService.insert(ban3));

        // Proposed to date inside existing ban
        Ban ban4 = newBan(guest.get().getId());
        ban4.setBanFrom(LocalDate.parse("2020-08-05"));
        ban4.setBanTo(LocalDate.parse("2020-09-03"));
        assertThrows(NotUnique.class,
                () -> banService.insert(ban4));

        // Proposed ban inside existing ban
        Ban ban5 = newBan(guest.get().getId());
        ban5.setBanFrom(LocalDate.parse("2020-09-05"));
        ban5.setBanTo(LocalDate.parse("2020-09-10"));
        assertThrows(NotUnique.class,
                () -> banService.insert(ban5));

        // Proposed ban matches existing ban
        Ban ban6 = newBan(guest.get().getId());
        ban6.setBanFrom(LocalDate.parse("2020-09-01"));
        ban6.setBanTo(LocalDate.parse("2020-09-30"));
        assertThrows(NotUnique.class,
                () -> banService.insert(ban6));

        // Proposed ban subsumes existing ban
        Ban ban7 = newBan(guest.get().getId());
        ban7.setBanFrom(LocalDate.parse("2020-08-31"));
        ban7.setBanTo(LocalDate.parse("2020-10-01"));
        assertThrows(NotUnique.class,
                () -> banService.insert(ban7));

    }


    // update() tests

/*
    @Test
    public void updateHappy() throws Exception {

        String facilityName = "Oakland";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        // Change something but keep firstName/lastName
        Ban guest1 = findBanByNameExact(
                facility.get().getId(),
                "Fred",
                "Flintstone").get();
        guest1.setComments(guest1.getComments() + " Updated");
        banService.update(guest1.getId(), guest1);

        // Change firstName to something unique
        Ban guest2 = findBanByNameExact(
                facility.get().getId(),
                "Fred",
                "Flintstone").get();
        guest2.setFirstName(guest2.getFirstName() + " Updated");
        banService.update(guest2.getId(), guest2);

        // Change lastName to something unique
        Ban guest3 = findBanByNameExact(
                facility.get().getId(),
                "Barney",
                "Rubble").get();
        guest2.setLastName(guest3.getLastName() + " Updated");
        banService.update(guest3.getId(), guest3);

    }
*/

/*
    @Test
    public void updateBadRequest() throws Exception {

        String facilityName = "San Francisco";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));
        String firstName = "Fred";
        String lastName = "Flintstone";
        Optional<Ban> original = findBanByNameExact(
                facility.get().getId(),
                firstName,
                lastName
        );

        // Completely empty instance
        final Ban guest0 = new Ban();
        assertThrows(BadRequest.class,
                () -> banService.update(original.get().getId(), guest0));

        // Missing facilityId field
        final Ban guest1 = findBanByNameExact(
                facility.get().getId(),
                firstName,
                lastName
        ).get();
        guest1.setFacilityId(null);
        assertThrows(BadRequest.class,
                () -> banService.update(original.get().getId(), guest1));

        // Invalid facilityId field
        final Ban guest2 = findBanByNameExact(
                facility.get().getId(),
                firstName,
                lastName
        ).get();
        guest2.setFacilityId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> banService.update(original.get().getId(), guest2));

        // Missing firstName field
        final Ban guest3 = findBanByNameExact(
                facility.get().getId(),
                firstName,
                lastName
        ).get();
        guest3.setFirstName(null);
        assertThrows(BadRequest.class,
                () -> banService.update(original.get().getId(), guest3));

        // Missing lastName field
        final Ban guest4 = findBanByNameExact(
                facility.get().getId(),
                firstName,
                lastName
        ).get();
        guest4.setLastName(null);
        assertThrows(BadRequest.class,
                () -> banService.update(original.get().getId(), guest4));

    }
*/

/*
    @Test
    public void updateNotUnique() throws Exception {

        String facilityName = "San Francisco";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));
        String firstName = "Fred";
        String lastName = "Flintstone";
        Optional<Ban> original = findBanByNameExact(
                facility.get().getId(),
                firstName,
                lastName
        );

        // Violate name uniqueness in same facility
        final Ban guest5 = findBanByNameExact(
                facility.get().getId(),
                firstName,
                lastName
        ).get();
        guest5.setFirstName("Barney");
        guest5.setLastName("Rubble");
        assertThrows(NotUnique.class,
                () -> banService.update(original.get().getId(), guest5));

        // Violate name uniqueness in different facility
        final Ban guest6 = findBanByNameExact(
                facility.get().getId(),
                firstName,
                lastName
        ).get();
        Optional<Facility> facility2 = findFacilityByNameExact("San Jose");
        assertThat(facility2.isPresent(), is(true));
        assertThat(facility2.get().getId(), is(not(equalTo(facility.get().getId()))));
        guest6.setFacilityId(facility2.get().getId());
        assertThrows(NotUnique.class,
                () -> banService.update(original.get().getId(), guest6));

    }
*/

    // Support Methods -------------------------------------------------------

    private List<Ban> findBansAll() {
        return entityManager.createNamedQuery
                (BAN_NAME + ".findAll", Ban.class)
                .getResultList();
    }

    private Optional<Ban> findBanById(Long banId) {
        TypedQuery<Ban> query = entityManager.createNamedQuery
                (BAN_NAME + ".findById", Ban.class)
                .setParameter(ID_COLUMN, banId);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

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

    private Optional<Guest> findGuestByNameExact(Long facilityId,
                                                 String firstName,
                                                 String lastName) {
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

    private List<Guest> findGuestsByFacilityId(Long facilityId) {
        return entityManager.createNamedQuery
                (GUEST_NAME + ".findByFacilityId", Guest.class)
                .setParameter(FACILITY_ID_COLUMN, facilityId)
                .getResultList();
    }

    private Ban newBan(Long guestId) {
        return new Ban(
                true,
                LocalDate.parse("2020-05-05"),
                LocalDate.parse("2020-05-05"),
                "Guest " + guestId + " Ban",
                guestId,
                "Fearless Leader"
        );
    }

}
