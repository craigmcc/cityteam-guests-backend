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

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.HashMap;
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
    private Map<String, Facility> facilities = new HashMap<>();

    // Static Variables ------------------------------------------------------

    private static final Logger LOG =
            Logger.getLogger(DevModePopulateService.class.getSimpleName());

    // Public Methods --------------------------------------------------------

    public void populate() {
        LOG.info("----- Populate Development Test Data Begin -----");
        // Populate data in order respecting dependencies
        populateFacilities();
        // Clean up our temporary data maps
        cleanTemporaryMaps();
        LOG.info("------ Populate Development Test Data End ------");
    }

    // Private Methods -------------------------------------------------------

    private void cleanTemporaryMaps() {
        facilities.clear();
    }

    private Long lookupFaclity(String name) {
        Facility facility = facilities.get(name);
        if (facility == null) {
            throw new IllegalArgumentException
                    ("Cannot find facility for '" + name + "'");
        } else if (facility.getId() == null) {
            throw new IllegalArgumentException
                    ("No ID for facility '" + name + "'");
        }
        return facility.getId();
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

}
