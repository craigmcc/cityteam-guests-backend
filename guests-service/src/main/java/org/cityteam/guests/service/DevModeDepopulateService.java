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

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.logging.Logger;

import static org.cityteam.guests.model.Constants.BAN_NAME;
import static org.cityteam.guests.model.Constants.FACILITY_NAME;
import static org.cityteam.guests.model.Constants.GUEST_NAME;
import static org.cityteam.guests.model.Constants.REGISTRATION_NAME;
import static org.cityteam.guests.model.Constants.TEMPLATE_NAME;

/**
 * <p>Split out from {@link DevModeStartupService} so that service and
 * integration tests can call it separately if needed.</p>
 */
@LocalBean
@Singleton
public class DevModeDepopulateService {

    // Instance Variables ----------------------------------------------------

    @PersistenceContext
    private EntityManager entityManager;

    private static final Logger LOG =
            Logger.getLogger(DevModeDepopulateService.class.getSimpleName());

    // Public Methods --------------------------------------------------------

    public void depopulate() {
        LOG.info("----- Depopulate Development Test Data Begin -----");
        // depopulate data in order respecting dependencies
        depopulateRegistrations();
        depopulateBans();
        depopulateGuests();
        depopulateTemplates();
        depopulateFacilities();
        // Restart the sequence generator since we are reloading data
        // from scratch
        resetSequence();
        LOG.info("------ Depopulate Development Test Data End ------");
    }

    // Private Methods -------------------------------------------------------

    private void resetSequence() {
        entityManager.createNativeQuery
                ("ALTER SEQUENCE hibernate_sequence RESTART WITH 1")
                .executeUpdate();
    }

    private void depopulateBans() {
        int deletedCount = entityManager
                .createQuery("DELETE FROM " + BAN_NAME)
                .executeUpdate();
        LOG.info(String.format("Deleted %d bans", deletedCount));
    }

    private void depopulateFacilities() {
        int deletedCount = entityManager
                .createQuery("DELETE FROM " + FACILITY_NAME)
                .executeUpdate();
        LOG.info(String.format("Deleted %d facilities", deletedCount));
    }

    private void depopulateGuests() {
        int deletedCount = entityManager
                .createQuery("DELETE FROM " + GUEST_NAME)
                .executeUpdate();
        LOG.info(String.format("Deleted %d guests", deletedCount));
    }

    private void depopulateRegistrations() {
        int deletedCount = entityManager
                .createQuery("DELETE FROM " + REGISTRATION_NAME)
                .executeUpdate();
        LOG.info(String.format("Deleted %d registrations", deletedCount));
    }

    private void depopulateTemplates() {
        int deletedCount = entityManager
                .createQuery("DELETE FROM " + TEMPLATE_NAME)
                .executeUpdate();
        LOG.info(String.format("Deleted %d templates", deletedCount));
    }

}
