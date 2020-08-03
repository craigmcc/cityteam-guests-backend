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
import org.cityteam.guests.model.Registration;
import org.cityteam.guests.model.Template;
import org.cityteam.guests.model.types.FeatureType;
import org.cityteam.guests.model.types.PaymentType;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * <p>Split out from {@link DevModeStartupService} so that service and
 * integration tests can call it separately if needed.</p>
 */
@LocalBean
@Singleton
public class DevModePopulateService {

    // Instance Variables ----------------------------------------------------

    @PersistenceContext
    private EntityManager entityManager;

    // Key is "name"
    private final Map<String, Facility> facilities = new HashMap<>();

    // Key is "facilityId|lastName|firstName"
    private final Map<String, Guest> guests = new HashMap<>();

    // Static Variables ------------------------------------------------------

    private static final Logger LOG =
            Logger.getLogger(DevModePopulateService.class.getSimpleName());

    // Public Methods --------------------------------------------------------

    public void populate() {
        LOG.info("----- Populate Development Test Data Begin -----");
        // Populate data in order respecting dependencies
        populateFacilities();
        populateTemplates();
        populateGuests();
        populateBans();
        populateRegistrations();
        // Clean up our temporary data maps
        cleanTemporaryMaps();
        LOG.info("------ Populate Development Test Data End ------");
    }

    // Private Methods -------------------------------------------------------

    private void cleanTemporaryMaps() {
        facilities.clear();
    }

    private Facility lookupFacility(String name) {
        Facility facility = facilities.get(name);
        if (facility == null) {
            throw new IllegalArgumentException
                    ("Cannot find facility for name '" + name + "'");
        } else if (facility.getId() == null) {
            throw new IllegalArgumentException
                    ("No facility ID for name '" + name + "'");
        }
        return facility;
    }

    private Guest lookupGuest(Long facilityId, String firstName, String lastName) {
        Guest guest = guests.get(facilityId + "|" + lastName + "|" + firstName);
        if (guest == null) {
            throw new IllegalArgumentException
                    ("Cannot find guest for facilityId " + facilityId +
                            "and name " + firstName + " " + lastName);
        } else if (guest.getId() == null) {
            throw new IllegalArgumentException
                    ("No guest ID for facilityId " + facilityId +
                            "and name " + firstName + " " + lastName);

        }
        return guest;
    }

    private void populateBan(
            Boolean active,
            LocalDate banFrom,
            LocalDate banTo,
            String comments,
            Long guestId,
            String staff
    ) {
        Ban ban = new Ban(
                active,
                banFrom,
                banTo,
                comments,
                guestId,
                staff
        );
        ban.setPublished(LocalDateTime.now());
        ban.setUpdated(ban.getPublished());
        entityManager.persist(ban);
    }

    private void populateBans() {

        // NOTE: Do not populate guests for facility "Portland"
        LOG.info("Populating bans begin");

        // Populate bans for facility "San Francisco"
        Long facilityId3 = lookupFacility("San Francisco").getId();

        Long guestId3_Fred = lookupGuest(facilityId3,
                "Fred", "Flintstone").getId();
        populateBan(true,
                LocalDate.parse("2020-08-01"),
                LocalDate.parse("2020-08-31"),
                "San Francisco Fred August Ban",
                guestId3_Fred,
                "Manager"
        );
        populateBan(false,
                LocalDate.parse("2020-10-01"),
                LocalDate.parse("2020-10-31"),
                "San Francisco Fred October Ban",
                guestId3_Fred,
                "Manager"
        );

        Long guestId3_Barney = lookupGuest(facilityId3,
                "Barney", "Rubble").getId();
        populateBan(true,
                LocalDate.parse("2020-09-01"),
                LocalDate.parse("2020-09-30"),
                "San Francisco Barney September Ban",
                guestId3_Barney,
                "Manager"
        );
        populateBan(false,
                LocalDate.parse("2020-11-01"),
                LocalDate.parse("2020-11-30"),
                "San Francisco Barney November Ban",
                guestId3_Barney,
                "Manager"
        );

        LOG.info("Populating bans end");

    }

    private void populateFacility(
            String address1,
            String address2,
            String city,
            String email,
            String name,
            String phone,
            String state,
            String zipCode
    ) {
        Facility facility = new Facility(
                address1,
                address2,
                city,
                email,
                name,
                phone,
                state,
                zipCode
        );
        facility.setPublished(LocalDateTime.now());
        facility.setUpdated(facility.getPublished());
        entityManager.persist(facility);
        facilities.put(name, facility);
    }

    private void populateFacilities() {
        LOG.info("Populating facilities begin");
        populateFacility(
                "634 Sproul Street",
                null,
                "Chester",
                "chester@cityteam.org",
                "Chester",
                "610-872-6865",
                "PA",
                "19013"
        );
        populateFacility(
                "722 Washington St.",
                null,
                "Oakland",
                "oakland@cityteam.org",
                "Oakland",
                "510-452-3758",
                "CA",
                "94607"
        );
        populateFacility(
                "526 SE Grand Ave.",
                null,
                "Portland",
                "portland@cityteam.org",
                "Portland",
                "503-231-9334",
                "OR",
                "97214"
        );
        populateFacility(
                "164 6th Street",
                null,
                "San Francisco",
                "sanfrancisco@cityteam.org",
                "San Francisco",
                "415-861-8688",
                "CA",
                "94103"
        );
        populateFacility(
                "2306 Zanker Road",
                null,
                "San Jose",
                "sanjose@cityteam.org",
                "San Jose",
                "408-232-5600",
                "CA",
                "95131"
        );
        LOG.info("Populating facilities end");
    }

    private void populateGuest(
            String comment,
            Long facilityId,
            String firstName,
            String lastName
    ) {
        Guest guest = new Guest(
                comment,
                facilityId,
                firstName,
                lastName
        );
        guest.setPublished(LocalDateTime.now());
        guest.setUpdated(guest.getPublished());
        entityManager.persist(guest);
        guests.put(facilityId + "|" + lastName + "|" + firstName, guest);
    }

    private void populateGuests() {

        // NOTE: Do not populate guests for facility "Portland"
        LOG.info("Populating guests begin");

        // Populate guests for facility "Chester"
        Long facilityId1 = lookupFacility("Chester").getId();
        populateGuest(
                "Chester Fred Comment",
                facilityId1,
                "Fred",
                "Flintstone"
        );
        populateGuest(
                "Chester Barney Comment",
                facilityId1,
                "Barney",
                "Rubble"
        );
        populateGuest(
                "Chester Bam Bam Comment",
                facilityId1,
                "Bam Bam",
                "Rubble"
        );

        // Populate guests for facility "Oakland"
        Long facilityId2 = lookupFacility("Oakland").getId();
        populateGuest(
                "Oakland Fred Comment",
                facilityId2,
                "Fred",
                "Flintstone"
        );
        populateGuest(
                "Oakland Barney Comment",
                facilityId2,
                "Barney",
                "Rubble"
        );
        populateGuest(
                "Oakland Bam Bam Comment",
                facilityId2,
                "Bam Bam",
                "Rubble"
        );

        // Populate guests for facility "San Francisco"
        Long facilityId3 = lookupFacility("San Francisco").getId();
        populateGuest(
                "San Francisco Fred Comment",
                facilityId3,
                "Fred",
                "Flintstone"
        );
        populateGuest(
                "San Francisco Barney Comment",
                facilityId3,
                "Barney",
                "Rubble"
        );
        populateGuest(
                "San Francisco Bam Bam Comment",
                facilityId3,
                "Bam Bam",
                "Rubble"
        );

        // Populate guests for facility "San Jose"
        Long facilityId4 = lookupFacility("San Jose").getId();
        populateGuest(
                "San Jose Fred Comment",
                facilityId4,
                "Fred",
                "Flintstone"
        );
        populateGuest(
                "San Jose Barney Comment",
                facilityId4,
                "Barney",
                "Rubble"
        );
        populateGuest(
                "San Jose Bam Bam Comment",
                facilityId4,
                "Bam Bam",
                "Rubble"
        );

        LOG.info("Populating guests end");
    }

    // Unassigned registration
    private void populateRegistration(
            Long facilityId,
            List<FeatureType> features,
            Integer matNumber,
            LocalDate registrationDate
    ) {
        Registration registration = new Registration(
                facilityId,
                features,
                matNumber,
                registrationDate
        );
        registration.setPublished(LocalDateTime.now());
        registration.setUpdated(registration.getPublished());
        entityManager.persist(registration);
    }

    // Assigned registration
    private void populateRegistration(
            String comments,
            Long facilityId,
            List<FeatureType> features,
            Long guestId,
            Integer matNumber,
            BigDecimal paymentAmount,
            PaymentType paymentType,
            LocalDate registrationDate,
            LocalTime showerTime,
            LocalTime wakeupTime
    ) {
        Registration registration = new Registration(
                comments,
                facilityId,
                features,
                guestId,
                matNumber,
                paymentAmount,
                paymentType,
                registrationDate,
                showerTime,
                wakeupTime
        );
        registration.setPublished(LocalDateTime.now());
        registration.setUpdated(registration.getPublished());
        entityManager.persist(registration);
    }

    private void populateRegistrations() {

        // WARNING: populateRegistration() bypasses the restrictions
        // in RegistrationService, so be sure everything you add is valid

        // NOTE: Do not populate registrations for facility "Portland"

        LOG.info("Populating registrations begin");

        // Date and time values of interest
        LocalDate registrationDate = LocalDate.parse("2020-07-04");
        LocalTime showerTime = LocalTime.parse("03:30");
        LocalTime wakeupTime = LocalTime.parse("04:00");

        // Various combinations of features
        List<FeatureType> features1 =
                List.of(FeatureType.H);
        List<FeatureType> features2 =
                List.of(FeatureType.S);
        List<FeatureType> features3 =
                List.of(FeatureType.H, FeatureType.S);

        // Acquire Chester cross reference ids
        Long chesterFacilityId = lookupFacility("Chester").getId();
        Long chesterGuestBamBamId =
                lookupGuest(chesterFacilityId, "Bam Bam", "Rubble").getId();
        Long chesterGuestBarneyId =
                lookupGuest(chesterFacilityId, "Barney", "Rubble").getId();
        Long chesterGuestFredId =
                lookupGuest(chesterFacilityId, "Fred", "Flintstone").getId();

        // Add unassigned registrations for Chester
        populateRegistration(
                chesterFacilityId,
                features1,
                1,
                registrationDate
        );
        populateRegistration(
                chesterFacilityId,
                features2,
                2,
                registrationDate
        );
        populateRegistration(
                chesterFacilityId,
                features3,
                3,
                registrationDate
        );
        populateRegistration(
                chesterFacilityId,
                null,
                4,
                registrationDate
        );

        // Add assigned registrations for Chester
        populateRegistration(
                "Bam Bam in Chester",
                chesterFacilityId,
                features1,
                chesterGuestBamBamId,
                5,
                null,
                PaymentType.AG,
                registrationDate,
                showerTime,
                wakeupTime
        );
        populateRegistration(
                "Barney in Chester",
                chesterFacilityId,
                features1,
                chesterGuestBarneyId,
                6,
                null,
                PaymentType.SW,
                registrationDate,
                showerTime,
                null
        );
        populateRegistration(
                "Fred in Chester",
                chesterFacilityId,
                features1,
                chesterGuestFredId,
                7,
                new BigDecimal("5.00"),
                PaymentType.$$,
                registrationDate,
                null,
                wakeupTime
        );

        // Acquire Oakland cross reference ids
        Long oaklandFacilityId = lookupFacility("Oakland").getId();
        Long oaklandGuestBamBamId =
                lookupGuest(oaklandFacilityId, "Bam Bam", "Rubble").getId();
        Long oaklandGuestBarneyId =
                lookupGuest(oaklandFacilityId, "Barney", "Rubble").getId();
        Long oaklandGuestFredId =
                lookupGuest(oaklandFacilityId, "Fred", "Flintstone").getId();

        // Add unassigned registrations for Oakland
        populateRegistration(
                oaklandFacilityId,
                features1,
                1,
                registrationDate
        );
        populateRegistration(
                oaklandFacilityId,
                features2,
                2,
                registrationDate
        );
        populateRegistration(
                oaklandFacilityId,
                features3,
                3,
                registrationDate
        );
        populateRegistration(
                oaklandFacilityId,
                null,
                4,
                registrationDate
        );

        // Add assigned registrations for Oakland
        populateRegistration(
                "Bam Bam in Oakland",
                oaklandFacilityId,
                features1,
                oaklandGuestBamBamId,
                5,
                null,
                PaymentType.MM,
                registrationDate,
                showerTime,
                null
        );
        populateRegistration(
                "Barney in Oakland",
                oaklandFacilityId,
                features1,
                oaklandGuestBarneyId,
                6,
                null,
                PaymentType.CT,
                registrationDate,
                showerTime,
                null
        );
        populateRegistration(
                "Fred in Oakland",
                oaklandFacilityId,
                features1,
                oaklandGuestFredId,
                7,
                new BigDecimal("4.00"),
                PaymentType.$$,
                registrationDate,
                showerTime,
                null
        );

        LOG.info("Populating registrations end");

    }

    private void populateTemplate(Template template) {
        template.setPublished(LocalDateTime.now());
        template.setUpdated(template.getPublished());
        entityManager.persist(template);
    }

    private void populateTemplates() {

        // Do not populate templates for facility "Portland"
        LOG.info("Populating templates begin");

        // Populate templates for facility "Chester"
        Long facilityId1 = lookupFacility("Chester").getId();
        populateTemplate(new Template(
                "1-6",
                "Chester COVID Template",
                facilityId1,
                "1,3",
                "Chester COVID",
                "3,5"
        ));
        populateTemplate(new Template(
                "1-58",
                "Chester Standard Template",
                facilityId1,
                "1,3",
                "Chester Standard",
                "3,5"
        ));

        // Populate templates for facility "Oakland"
        Long facilityId2 = lookupFacility("Oakland").getId();
        populateTemplate(new Template(
                "1-3,4-6",
                "Oakland COVID Template",
                facilityId2,
                "1-3",
                "Oakland COVID",
                "3-5"
        ));
        populateTemplate(new Template(
                "1-58",
                "Oakland Standard Template",
                facilityId2,
                "1-10,12",
                "Oakland Standard",
                "6-15"
        ));

        // Populate templates for facility "San Francisco"
        Long facilityId3 = lookupFacility("San Francisco").getId();
        populateTemplate(new Template(
                "1-12",
                "San Francisco COVID Template",
                facilityId3,
                "1,3",
                "San Francisco COVID",
                "3,5"
        ));
        populateTemplate(new Template(
                "1-58",
                "San Francisco Standard Template",
                facilityId3,
                "1,3",
                "San Francisco Standard",
                "3,5"
        ));

        // Populate templates for facility "San Jose"
        Long facilityId4 = lookupFacility("San Jose").getId();
        populateTemplate(new Template(
                "1-24",
                "San Jose COVID Template",
                facilityId4,
                "1,9-10,21",
                "San Jose COVID",
                "17-18,22-23"
        ));
        populateTemplate(new Template(
                "1-58",
                "San Jose Standard Template",
                facilityId4,
                "1,9-10,21,30-31,34-35,43,54-55,58",
                "San Jose Standard",
                "17-18,22-23,30-31,36-37,42,53-54,57-58"
        ));

        LOG.info("Populating templates end");

    }

}
