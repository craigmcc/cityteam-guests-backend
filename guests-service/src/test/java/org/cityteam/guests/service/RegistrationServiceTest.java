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

import org.cityteam.guests.action.Assign;
import org.cityteam.guests.action.Import;
import org.cityteam.guests.model.Facility;
import org.cityteam.guests.model.Guest;
import org.cityteam.guests.model.Registration;
import org.cityteam.guests.model.types.FeatureType;
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.InternalServerError;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.cityteam.guests.model.Constants.FACILITY_ID_COLUMN;
import static org.cityteam.guests.model.Constants.FACILITY_NAME;
import static org.cityteam.guests.model.Constants.GUEST_NAME;
import static org.cityteam.guests.model.Constants.NAME_COLUMN;
import static org.cityteam.guests.model.Constants.REGISTRATION_DATE_COLUMN;
import static org.cityteam.guests.model.Constants.REGISTRATION_NAME;
import static org.cityteam.guests.model.types.PaymentType.$$;
import static org.cityteam.guests.model.types.PaymentType.AG;
import static org.cityteam.guests.model.types.PaymentType.CT;
import static org.cityteam.guests.model.types.PaymentType.MM;
import static org.craigmcc.library.model.Constants.ID_COLUMN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

@Category(ServiceTests.class)
@RunWith(Arquillian.class)
public class RegistrationServiceTest extends AbstractServiceTest {

    // Configuration and Injections ------------------------------------------

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive archive = ShrinkWrap.create
                (JavaArchive.class, "testRegistration.jar")
                .addClass(FacilityService.class)
                .addClass(GuestService.class)
                .addClass(RegistrationService.class);
        addServiceFixtures(archive, false);
        System.out.println("RegistrationServiceTest: Assembled Archive:");
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
    RegistrationService registrationService;

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

    // assign() tests

    @Test
    public void assignHappy() throws Exception {

        // Seed unassigned registrations
        String facilityName = "San Jose";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));
        LocalDate registrationDate = LocalDate.parse("2020-07-04");
        List<Registration> registrations = seedUnassignedRegistrations
                (facility.get().getId(), registrationDate);
        assertThat(registrations.size(), is(equalTo(4)));

        // Assign one of them
        List<Guest> guests = findGuestsByFacilityId(facility.get().getId());
        Assign assign = new Assign(
                "Happy assignment",
                guests.get(0).getId(),
                new BigDecimal("5.00"),
                $$,
                null,
                null
        );
        Registration registration =
            registrationService.assign(registrations.get(0).getId(), assign);
        assertThat(registration.getGuestId(),
                is(equalTo(guests.get(0).getId())));

        // Can reassign to the same guest
        registrationService.assign(registrations.get(0).getId(), assign);

    }

    @Test
    public void assignBadRequestAlreadyAssigned() throws Exception {

        // Seed unassigned registrations and look up required information
        String facilityName = "San Jose";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));
        LocalDate registrationDate = LocalDate.parse("2020-07-04");
        List<Registration> registrations = seedUnassignedRegistrations
                (facility.get().getId(), registrationDate);
        assertThat(registrations.size(), is(equalTo(4)));
        List<Guest> guests = findGuestsByFacilityId(facility.get().getId());

        // Assign one of them
        Assign assign = new Assign(
                "Happy assignment",
                guests.get(0).getId(),
                new BigDecimal("5.00"),
                $$,
                null,
                null
        );
        Registration registration =
                registrationService.assign(registrations.get(0).getId(), assign);
        assertThat(registration.getGuestId(),
                is(equalTo(guests.get(0).getId())));

        // Try to assign same registration to someone else
        assign.setGuestId(guests.get(1).getId());
        assertThrows(BadRequest.class,
                () -> registrationService.assign
                        (registrations.get(0).getId(), assign));

    }

    @Test
    public void assignBadRequestIncorrrectGuest() throws Exception {

        // Seed unassigned registrations and look up required information
        String facilityName1 = "San Jose";
        Optional<Facility> facility1 = findFacilityByNameExact(facilityName1);
        assertThat(facility1.isPresent(), is(true));
        LocalDate registrationDate = LocalDate.parse("2020-07-04");
        List<Registration> registrations = seedUnassignedRegistrations
                (facility1.get().getId(), registrationDate);
        assertThat(registrations.size(), is(equalTo(4)));
        List<Guest> guests1 = findGuestsByFacilityId(facility1.get().getId());
        String facilityName2 = "San Francisco";
        Optional<Facility> facility2 = findFacilityByNameExact(facilityName2);
        assertThat(facility2.isPresent(), is(true));
        List<Guest> guests2 = findGuestsByFacilityId(facility2.get().getId());

        // Assign guest from facility1 to one of them
        Assign assign1 = new Assign(
                "Happy assignment",
                guests1.get(0).getId(),
                new BigDecimal("5.00"),
                $$,
                null,
                null
        );
        Registration registration =
                registrationService.assign
                        (registrations.get(0).getId(), assign1);
        assertThat(registration.getGuestId(),
                is(equalTo(guests1.get(0).getId())));

        // Try to assign guest from facility2 to one of them
        Assign assign2 = new Assign(
                "Happy assignment",
                guests2.get(0).getId(),
                new BigDecimal("5.00"),
                $$,
                null,
                null
        );
        assertThrows(BadRequest.class,
                () -> registrationService.assign
                        (registrations.get(1).getId(), assign2));

    }

    @Test
    public void assignBadRequestTwoAssignments() throws Exception {

        // Seed unassigned registrations and look up required information
        String facilityName = "San Jose";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));
        LocalDate registrationDate = LocalDate.parse("2020-07-04");
        List<Registration> registrations = seedUnassignedRegistrations
                (facility.get().getId(), registrationDate);
        assertThat(registrations.size(), is(equalTo(4)));
        List<Guest> guests = findGuestsByFacilityId(facility.get().getId());

        // Assign one of them
        Assign assign = new Assign(
                "Happy assignment",
                guests.get(0).getId(),
                new BigDecimal("5.00"),
                $$,
                null,
                null
        );
        Registration registration =
                registrationService.assign(registrations.get(0).getId(), assign);
        assertThat(registration.getGuestId(),
                is(equalTo(guests.get(0).getId())));

        // Try to assign same guest to a different registration
        assertThrows(BadRequest.class,
                () -> registrationService.assign
                        (registrations.get(1).getId(), assign));

    }

    @Test
    public void assignNotFound() throws Exception {

        // Seed unassigned registrations and look up required information
        String facilityName = "San Jose";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));
        LocalDate registrationDate = LocalDate.parse("2020-07-04");
        List<Registration> registrations = seedUnassignedRegistrations
                (facility.get().getId(), registrationDate);
        assertThat(registrations.size(), is(equalTo(4)));
        List<Guest> guests = findGuestsByFacilityId(facility.get().getId());

        // Invalid registrationId
        Assign assign = new Assign(
                "Happy assignment",
                guests.get(0).getId(),
                new BigDecimal("5.00"),
                $$,
                null,
                null
        );
        assertThrows(NotFound.class,
                () -> registrationService.assign(Long.MAX_VALUE, assign));

    }

    // desassign() tests

    @Test
    public void deassignHappy() throws Exception {

        // Seed unassigned registrations
        String facilityName = "San Jose";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));
        LocalDate registrationDate = LocalDate.parse("2020-07-04");
        List<Registration> registrations = seedUnassignedRegistrations
                (facility.get().getId(), registrationDate);
        assertThat(registrations.size(), is(equalTo(4)));

        // Assign one of them
        List<Guest> guests = findGuestsByFacilityId(facility.get().getId());
        Assign assign = new Assign(
                "Happy assignment",
                guests.get(0).getId(),
                new BigDecimal("5.00"),
                $$,
                null,
                null
        );
        Registration registration =
                registrationService.assign(registrations.get(0).getId(), assign);
        assertThat(registration.getGuestId(),
                is(equalTo(guests.get(0).getId())));

        // Now deassign that registration
        registration =
                registrationService.deassign(registration.getId());
        assertThat(registration.getGuestId(), is(equalTo(null)));

    }

    @Test
    public void deassignBadRequest() throws Exception {

        // Seed unassigned registrations
        String facilityName = "San Jose";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));
        LocalDate registrationDate = LocalDate.parse("2020-07-04");
        List<Registration> registrations = seedUnassignedRegistrations
                (facility.get().getId(), registrationDate);
        assertThat(registrations.size(), is(equalTo(4)));

        // Attempt to deassign an unassigned registration
        assertThrows(BadRequest.class,
                () -> registrationService.deassign
                        (registrations.get(0).getId()));

    }

    @Test
    public void deassignNotFound() throws Exception {

        assertThrows(NotFound.class,
                () -> registrationService.deassign(Long.MAX_VALUE));

    }

    // delete() tests

    @Test
    public void deleteHappy() throws Exception {

        List<Registration> registrations = findRegistrationsAll();
        assertThat(registrations.size(), is(greaterThan(0)));

        for (Registration registration : registrations) {

            // Delete and verify we can no longer retrieve it
            registrationService.delete(registration.getId());
            assertThat(findRegistrationById(registration.getId()).isPresent(),
                    is(false));

        }

        assertThat(registrationService.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {

        assertThrows(NotFound.class,
                () -> registrationService.delete(Long.MAX_VALUE));

    }

    // find() tests

    @Test
    public void findHappy() throws Exception {

        List<Registration> registrations = findRegistrationsAll();
        assertThat(registrations.size(), is(greaterThan(0)));

        for (Registration registration : registrations) {
            Registration found =
                    registrationService.find(registration.getId());
            assertThat(found.equals(registration), is(true));
        }

    }

    @Test
    public void findNotFound() throws Exception {

        assertThrows(NotFound.class,
                () -> registrationService.find(Long.MAX_VALUE));

    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        List<Registration> registrations = registrationService.findAll();
        assertThat(registrations.size(), is(greaterThan(0)));

        String previousKey = null;
        for (Registration registration : registrations) {
            String thisKey = registration.getFacilityId() +
                    "|" + registration.getRegistrationDate() + "|" +
                    registration.getMatNumber();
            if (previousKey != null) {
                assertThat(thisKey, is(greaterThan(previousKey)));
            }
            previousKey = thisKey;
        }

    }

    // findByFacilityAndDate() tests

    @Test
    public void findByFacilityAndDateHappy() throws Exception {

        String facilityName = "Chester";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));
        LocalDate registrationDate = LocalDate.parse("2020-07-04");

        List<Registration> registrations =
                registrationService.findByFacilityAndDate
                (facility.get().getId(), registrationDate);
        assertThat(registrations.size(), is(greaterThan(0)));

        String previousKey = null;
        for (Registration registration : registrations) {
            String thisKey = registration.getFacilityId() +
                    "|" + registration.getRegistrationDate() + "|" +
                    registration.getMatNumber();
            if (previousKey != null) {
                assertThat(thisKey, is(greaterThan(previousKey)));
            }
            previousKey = thisKey;
        }

    }

    @Test
    public void findByFacilityAndDateNoMatch() throws Exception {

        String facilityName = "Oakland";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));
        LocalDate registrationDate1 = LocalDate.parse("2020-07-04");
        LocalDate registrationDate2 = LocalDate.parse("2020-07-05");

        // Invalid facilityId
        List<Registration> registrations1 =
                registrationService.findByFacilityAndDate
                        (Long.MAX_VALUE, registrationDate1);
        assertThat(registrations1.size(), is(equalTo(0)));

        // Invalid registrationDate
        List<Registration> registrations2 =
                registrationService.findByFacilityAndDate
                        (facility.get().getId(), registrationDate2);
        assertThat(registrations2.size(), is(equalTo(0)));

        // Invalid both
        List<Registration> registrations3 =
                registrationService.findByFacilityAndDate
                        (Long.MAX_VALUE, registrationDate2);
        assertThat(registrations3.size(), is(equalTo(0)));

    }

    // importByFacilityAndDate() tests

    @Test
    public void importByFaclityAndDate() throws Exception {

        // Accumulate information we need to perform this test

        String facilityName = "San Francisco";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        LocalDate registrationDate = LocalDate.parse("2020-07-05");
        LocalTime showerTime = LocalTime.parse("04:00");
        LocalTime wakeupTime = LocalTime.parse("03:30");

        List<FeatureType> features1 =
                List.of(FeatureType.H);
        List<FeatureType> features2 =
                List.of(FeatureType.S);
        List<FeatureType> features3 =
                List.of(FeatureType.H, FeatureType.S);
        List<Import> imports = new ArrayList<>();

        // Add some unassigned mats
        imports.add(new Import(features1, 1));
        imports.add(new Import(features2, 2));
        imports.add(new Import(features3, 3));

        // Add some assigned mats (existing people)
        imports.add(new Import(
                "Fred on Mat 4",
                features1,
                "Fred",
                "Flintstone",
                4,
                null,
                AG,
                showerTime,
                null
        ));
        imports.add(new Import(
                "Bam Bam on Mat 5",
                features2,
                "Bam Bam",
                "Rubble",
                5,
                null,
                $$,
                null,
                wakeupTime
        ));
        imports.add(new Import(
                "Barney on Mat 6",
                features3,
                "Barney",
                "Rubble",
                6,
                null,
                MM,
                showerTime,
                wakeupTime
        ));

        // Add a new guest
        imports.add(new Import(
                "New Person on Mat 7",
                null,
                "New",
                "Person",
                7,
                null,
                CT,
                null,
                null
        ));

        // Import these and verify the results
        List<Registration> registrations =
                registrationService.importByFacilityAndDate(
                        facility.get().getId(),
                        registrationDate,
                        imports
                );
        assertThat(registrations.size(), is(equalTo(imports.size())));
        System.out.println("IMPORT: RESULTS: " + registrations);

        // Retrieve them again and match them up
        List<Registration> retrieves =
                registrationService.findByFacilityAndDate(
                        facility.get().getId(),
                        registrationDate
                );
        assertThat(retrieves.size(), is(equalTo(registrations.size())));
        for (int i = 0; i < retrieves.size(); i++) {
            assertThat(retrieves.get(i), is(equalTo(registrations.get(i))));
        }

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {

        // Unassigned registrations for new facility on same date

        String facilityName = "San Jose";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));
        LocalDate registrationDate = LocalDate.parse("2020-07-04");

        List<Registration> registrations = seedUnassignedRegistrations
                (facility.get().getId(), registrationDate);
        assertThat(registrations.size(), is(equalTo(4)));

    }

    @Test
    public void insertBadRequest() throws Exception {

        String facilityName = "San Francisco";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));
        LocalDate registrationDate = LocalDate.parse("2020-07-04");

        // Missing facilityId
        Registration registration1 = newRegistration(
                null,
                91,
                registrationDate
        );
        assertThrows(BadRequest.class,
                () -> registrationService.insert(registration1));

        // Invalid facilityId
        Registration registration2 = newRegistration(
                Long.MAX_VALUE,
                92,
                registrationDate
        );
        assertThrows(BadRequest.class,
                () -> registrationService.insert(registration2));

        // Missing registrationDate
        Registration registration3 = newRegistration(
                facility.get().getId(),
                93,
                null
        );
        assertThrows(BadRequest.class,
                () -> registrationService.insert(registration3));

        // Attempt to insert with a guestId
        List<Guest> guests = findGuestsByFacilityId(facility.get().getId());
        assertThat(guests.size(), is(greaterThan(0)));
        Registration registration4 = newRegistration(
                facility.get().getId(),
                94,
                registrationDate
        );
        registration4.setGuestId(guests.get(0).getId());
        assertThrows(BadRequest.class,
                () -> registrationService.insert(registration4));

    }

    @Test
    public void insertNotUnique() throws Exception {

        String facilityName = "Chester";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));
        LocalDate registrationDate = LocalDate.parse("2020-07-04");
        List<Registration> registrations = findRegistrationsByFacilityAndDate
                (facility.get().getId(), registrationDate);
        assertThat(registrations.size(), is(greaterThan(0)));
        assertThrows(NotUnique.class,
                () -> registrationService.insert(registrations.get(0)));

    }

    // update() tests

    @Test
    public void update() throws Exception {

        List<Registration> registrations = findRegistrationsAll();
        assertThat(registrations.size(), is(greaterThan(0)));

        for (Registration registration : registrations) {
            assertThrows(InternalServerError.class,
                    () -> registrationService.update
                            (registration.getId(), registration));
        }

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

    private List<Guest> findGuestsByFacilityId(Long facilityId) {
        TypedQuery<Guest> query = entityManager.createNamedQuery
                (GUEST_NAME + ".findByFacilityId", Guest.class)
                .setParameter(FACILITY_ID_COLUMN, facilityId);
        return query.getResultList();
    }

    private Optional<Registration> findRegistrationById(Long registrationId) {
        TypedQuery<Registration> query = entityManager.createNamedQuery
                (REGISTRATION_NAME + ".findById", Registration.class)
                .setParameter(ID_COLUMN, registrationId);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    private List<Registration> findRegistrationsAll() {
        return entityManager.createNamedQuery
                (REGISTRATION_NAME + ".findAll", Registration.class)
                .getResultList();
    }

    private List<Registration> findRegistrationsByFacilityAndDate
            (Long facilityId, LocalDate registrationDate) {
        return entityManager.createNamedQuery
                (REGISTRATION_NAME + ".findByFacilityAndDate", Registration.class)
                .setParameter(FACILITY_ID_COLUMN, facilityId)
                .setParameter(REGISTRATION_DATE_COLUMN, registrationDate)
                .getResultList();
    }

    private Registration newRegistration
            (Long facilityId, Integer matNumber, LocalDate registrationDate) {
        return new Registration(
                facilityId,
                null,
                matNumber,
                registrationDate
        );
    }

    private List<Registration> seedUnassignedRegistrations
            (Long facilityId, LocalDate registrationDate)
            throws Exception {
        List<Registration> registrations = new ArrayList<>();
        for (int matNumber = 1; matNumber < 5; matNumber++) {
            Registration registration =
                    newRegistration(facilityId, matNumber, registrationDate);
            registrations.add(registrationService.insert(registration));
        }
        return registrations;
    }

}
