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
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.NotFound;
import org.craigmcc.library.shared.exception.NotUnique;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Assert;
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

import static org.cityteam.guests.model.Constants.FACILITY_NAME;
import static org.cityteam.guests.model.Constants.NAME_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;

@Category(ServiceTests.class)
@RunWith(Arquillian.class)
public class FacilityServiceTest extends AbstractServiceTest {

    // Configuration and Injections ------------------------------------------

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive archive = ShrinkWrap.create
                (JavaArchive.class, "testFacility.jar")
                .addClass(FacilityService.class);
        addServiceFixtures(archive, false);
        System.out.println("FacilityServiceTest: Assembled Archive:");
        System.out.println(archive.toString(true));
        return archive;
    }

    @Inject
    FacilityService facilityService;

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

        List<Facility> facilities = findFacilitiesAll();
        assertThat(facilities.size(), is(greaterThan(0)));

        for (Facility facility : facilities) {

            // Delete and verify we can no longer retrieve it
            facilityService.delete(facility.getId());
            assertThat(findFacilityById(facility.getId()).isPresent(),
                    is(false));

            // TODO - Delete should have cascaded to related models
/*
            assertThat(findGuestsByFacilityId(facility.getId()).size(), is(0));
            assertThat(findRegistrationsByFacilityId(facility.getId()).size(), is(0));
            assertThat(findTemplatesByFacilityId(facility.getId()).size(), is(0));
*/

        }

        assertThat(facilityService.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {

        assertThrows(NotFound.class,
                () -> facilityService.delete(Long.MAX_VALUE));

    }

    // find() tests

    @Test
    public void findHappy() throws Exception {

        List<Facility> facilities = findFacilitiesAll();
        assertThat(facilities.size(), is(greaterThan(0)));

        for (Facility facility : facilities) {
            Facility found = facilityService.find(facility.getId());
            assertThat(found.equals(facility), is(true));
        }

    }

    @Test
    public void findNotFound() throws Exception {

        assertThrows(NotFound.class,
                () -> facilityService.find(Long.MAX_VALUE));

    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        List<Facility> facilities = facilityService.findAll();
        assertThat(facilities.size(), is(greaterThan(0)));

        String previousName = null;
        for (Facility facility : facilities) {
            String thisName = facility.getName();
            if (previousName != null) {
                assertThat(thisName, is(greaterThan(previousName)));
            }
            previousName = thisName;
        }

    }

    // findByName() tests

    @Test
    public void findByNameHappy() throws Exception {

        List<Facility> facilities = facilityService.findByName("san");
        assertThat(facilities.size(), is(greaterThan(0)));

        String previousName = null;
        for (Facility facility : facilities) {
            String thisName = facility.getName();
            if (previousName != null) {
                assertThat(thisName, is(greaterThan(previousName)));
            }
            previousName = thisName;
        }
    }

    @Test
    public void findByNameNoMatch() throws Exception {

        List<Facility> facilities = facilityService.findByName("unmatched");
        assertThat(facilities.size(), is(equalTo(0)));

    }

    // findByNameExact() tests

    @Test
    public void findByNameExactHappy() throws Exception {

        List<Facility> facilities = findFacilitiesAll();
        assertThat(facilities.size(), is(greaterThan(0)));

        for (Facility facility : facilities) {
            Facility found = facilityService.findByNameExact(facility.getName());
            assertThat(found.getId(), is(equalTo(facility.getId())));
            assertThat(found.getName(), is(equalTo(facility.getName())));
        }

    }

    @Test
    public void findByNameExactNotFound() throws Exception {

        assertThrows(NotFound.class,
                () -> facilityService.findByNameExact("This Is Not A Match"));

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {

        Facility facility = newFacility();
        Facility inserted = facilityService.insert(facility);

        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getPublished(), is(notNullValue()));
        assertThat(inserted.getUpdated(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        assertThat(findFacilityById(inserted.getId()).isPresent(), is(true));

    }

    @Test
    public void insertBadRequest() throws Exception {

        // Completely empty instance
        final Facility facility0 = new Facility();
        assertThrows(BadRequest.class,
                () -> facilityService.insert(facility0));

        // Missing name field
        final Facility facility1 = newFacility();
        facility1.setName(null);
        assertThrows(BadRequest.class,
                () -> facilityService.insert(facility0));

    }

    @Test
    public void insertNotUnique() throws Exception {

        List<Facility> facilities = findFacilitiesAll();
        assertThat(facilities.size(), is(greaterThan(0)));
        assertThrows(NotUnique.class,
                () -> facilityService.insert(facilities.get(0)));

    }

    // update() tests

    @Test
    public void updateHappy() throws Exception {

        // Change something but keep name
        Facility facility1 = findFacilityByNameExact("Portland").get();
        facility1.setCity(facility1.getCity() + " Updated");
        facilityService.update(facility1.getId(), facility1);

        // Change name to something unique
        Facility facility2 = findFacilityByNameExact("Chester").get();
        facility2.setName(facility2.getName() + " Updated");
        facilityService.update(facility2.getId(), facility2);

    }

    @Test
    public void updateUnhappy() throws Exception {

        // Required field
        Facility facility1 = findFacilityByNameExact("San Francisco").get();
        facility1.setName(null);
        try {
            facilityService.update(facility1.getId(), facility1);
            Assert.fail("Should have thrown BadRequest");
        } catch (BadRequest e) {
            // Expected result
        }

        // Violate uniqueness
        Facility facility2 = findFacilityByNameExact("Oakland").get();
        facility2.setName("San Jose");
        try {
            facilityService.update(facility2.getId(), facility2);
            Assert.fail("Should have thrown NotUnique");
        } catch (NotUnique e) {
            // Expected result
        }

    }

    // Support Methods -------------------------------------------------------

    private List<Facility> findFacilitiesAll() {
        return entityManager.createNamedQuery
                (FACILITY_NAME + ".findAll", Facility.class)
                .getResultList();
    }

    private Optional<Facility> findFacilityById(Long facilityId) {
        TypedQuery<Facility> query = entityManager.createNamedQuery
                (FACILITY_NAME + ".findById", Facility.class)
                .setParameter(ID_COLUMN, facilityId);
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

    private Facility newFacility() {
        return new Facility(
                "123 New Street",
                null,
                "New City",
                "newcity@cityteam.org",
                "New City",
                "999-555-1212",
                "US",
                "99999"
        );
    }

}
