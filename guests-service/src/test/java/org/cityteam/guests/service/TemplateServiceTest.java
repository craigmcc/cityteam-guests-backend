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
import org.cityteam.guests.model.Registration;
import org.cityteam.guests.model.Template;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.cityteam.guests.model.Constants.FACILITY_ID_COLUMN;
import static org.cityteam.guests.model.Constants.FACILITY_NAME;
import static org.cityteam.guests.model.Constants.NAME_COLUMN;
import static org.cityteam.guests.model.Constants.TEMPLATE_NAME;
import static org.cityteam.guests.model.types.FeatureType.H;
import static org.cityteam.guests.model.types.FeatureType.S;
import static org.craigmcc.library.model.Constants.ID_COLUMN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThrows;

@Category(ServiceTests.class)
@RunWith(Arquillian.class)
public class TemplateServiceTest extends AbstractServiceTest {

    // Configuration and Injections ------------------------------------------

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive archive = ShrinkWrap.create
                (JavaArchive.class, "testTemplate.jar")
                .addClass(FacilityService.class)
                .addClass(GuestService.class)
                .addClass(RegistrationService.class)
                .addClass(TemplateService.class);
        addServiceFixtures(archive, false);
        System.out.println("TemplateServiceTest: Assembled Archive:");
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
    FacilityService facilityService;

    @Inject
    RegistrationService registrationService;

    @Inject
    TemplateService templateService;

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

        List<Template> templates = findTemplatesAll();
        assertThat(templates.size(), is(greaterThan(0)));

        for (Template template : templates) {

            // Delete and verify we can no longer retrieve it
            templateService.delete(template.getId());
            assertThat(findTemplateById(template.getId()).isPresent(),
                    is(false));

        }

        assertThat(findTemplatesAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {

        assertThrows(NotFound.class,
                () -> templateService.delete(Long.MAX_VALUE));

    }

    // find() tests

    @Test
    public void findHappy() throws Exception {

        List<Template> templates = findTemplatesAll();
        assertThat(templates.size(), is(greaterThan(0)));

        for (Template template : templates) {
            Template found = templateService.find(template.getId());
            assertThat(found.equals(template), is(true));
        }

    }

    @Test
    public void findNotFound() throws Exception {

        assertThrows(NotFound.class,
                () -> templateService.find(Long.MAX_VALUE));

    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        List<Template> templates = templateService.findAll();
        assertThat(templates.size(), is(greaterThan(0)));

        String previousName = null;
        for (Template template : templates) {
            String thisName = template.getFacilityId() + "|" +
                    template.getName();
            if (previousName != null) {
                assertThat(thisName, is(greaterThan(previousName)));
            }
            previousName = thisName;
        }

    }

    // findByFacilityId() tests

    @Test
    public void findByFacilityIdHappy() throws Exception {

        String facilityNames[] = {
                "Chester",
                "Oakland",
                "Portland",
                "San Francisco",
                "San Jose"
        };

        for (String facilityName : facilityNames) {

            Optional<Facility> facility = findFacilityByNameExact(facilityName);
            assertThat(facility.isPresent(), is(true));

            List<Template> templates = templateService.findByFacilityId
                    (facility.get().getId());
            if (!"Portland".equals(facilityName)) {
                assertThat(templates.size(), is(greaterThan(0)));
            }

            String previousName = null;
            for (Template guest : templates) {
                assertThat(guest.getComments(), startsWith(facilityName));
                assertThat(guest.getFacilityId(),
                        is(equalTo(facility.get().getId())));
                String thisName = guest.getName();
                if (previousName != null) {
                    assertThat(thisName, is(greaterThan(previousName)));
                }
                previousName = thisName;
            }

        }

    }

    @Test
    public void findByFacilityIdNoneFound() throws Exception {

        // Invalid facilityId
        List<Template> guests1 =
                templateService.findByFacilityId(Long.MAX_VALUE);
        assertThat(guests1.size(), is(equalTo(0)));

        // No guests for facilityId
        String name = "Facility " + LocalDateTime.now().toString();
        facilityService.insert(newFacility(name));
        Optional<Facility> facility = findFacilityByNameExact(name);
        assertThat(facility.isPresent(), is(true));
        List<Template> guests2 =
                templateService.findByFacilityId(facility.get().getId());
        assertThat(guests2.size(), is(0));

    }

    // findByName() tests

    @Test
    public void findByNameHappy() throws Exception {

        String facilityName = "Oakland";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        List<Template> guests = templateService.findByName
                (facility.get().getId(), "land");
        assertThat(guests.size(), is(greaterThan(0)));

        String previousName = null;
        for (Template guest : guests) {
            assertThat(guest.getComments(), startsWith(facilityName));
            assertThat(guest.getFacilityId(),
                    is(equalTo(facility.get().getId())));
            String thisName = guest.getName();
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

        List<Template> templates = templateService.findByName
                (facility.get().getId(), "unmatched");
        assertThat(templates.size(), is(equalTo(0)));

    }

    // findByNameExact() tests

    @Test
    public void findByNameExactHappy() throws Exception {

        String facilityName = "San Francisco";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        List<Template> templates =
                findTemplatesByFacilityId(facility.get().getId());
        assertThat(templates.size(), is(greaterThan(0)));

        for (Template template : templates) {
            Template found = templateService.findByNameExact(
                    template.getFacilityId(),
                    template.getName()
            );
            assertThat(found.getId(), is(equalTo(template.getId())));
        }

    }

    @Test
    public void findByNameExactNotFound() throws Exception {

        // Invalid facilityId
        assertThrows(NotFound.class,
                () -> templateService.findByNameExact(
                        Long.MAX_VALUE,
                        "Chester Standard")
        );

        // Mismatched name
        String facilityName = "Chester";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));
        assertThrows(NotFound.class,
                () -> templateService.findByNameExact(
                        facility.get().getId(),
                        "Oakland Standard")
        );

    }

    // generate() tests

    @Test
    public void generateHappy() throws Exception {

        String facilityName = "San Francisco";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));
        String templateName = "San Francisco COVID";
        Optional<Template> template = findTemplateByNameExact
                (facility.get().getId(), templateName);
        assertThat(template.isPresent(), is(true));

        LocalDate registrationDate = LocalDate.parse("2020-07-05");
        List<Registration> registrations = templateService.generate
                (template.get().getId(), registrationDate);
        assertThat(registrations.size(), is(equalTo(12)));

        for (Registration registration : registrations) {
            assertThat(registration.getComments(), is(nullValue()));
            assertThat(registration.getFacilityId(),
                    is(equalTo(facility.get().getId())));
            if (registration.getMatNumber() == 1) {
                assertThat(registration.getFeatures().contains(H),
                        is(true));
            } else if (registration.getMatNumber() == 3) {
                assertThat(registration.getFeatures().contains(H),
                        is(true));
                assertThat(registration.getFeatures().contains(S),
                        is(true));
            } else if (registration.getMatNumber() == 5) {
                assertThat(registration.getFeatures().contains(S),
                        is(true));
            } else {
                assertThat(registration.getFeatures(), is(nullValue()));
            }
            assertThat(registration.getGuestId(), is(nullValue()));
            assertThat(registration.getMatNumber(), is(notNullValue()));
            assertThat(registration.getPaymentAmount(), is(nullValue()));
            assertThat(registration.getPaymentType(), is(nullValue()));
            assertThat(registration.getShowerTime(), is(nullValue()));
            assertThat(registration.getWakeupTime(), is(nullValue()));
            assertThat(registration.getRegistrationDate(),
                    is(equalTo(registrationDate)));
        }

    }

    @Test
    public void generateBadRequest() throws Exception {

        String facilityName = "San Francisco";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));
        String templateName = "San Francisco COVID";
        Optional<Template> template = findTemplateByNameExact
                (facility.get().getId(), templateName);
        assertThat(template.isPresent(), is(true));

        // Force at least one mat for this registrationDate
        LocalDate registrationDate = LocalDate.parse("2020-07-06");
        Registration registration = new Registration(
                facility.get().getId(),
                null,
                1,
                registrationDate
        );
        registrationService.insert(registration);

        assertThrows(BadRequest.class,
                () -> templateService.generate
                        (template.get().getId(), registrationDate));

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {

        String facilityName = "Chester";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        Template template = newTemplate(facility.get().getId());
        Template inserted = templateService.insert(template);

        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getPublished(), is(notNullValue()));
        assertThat(inserted.getUpdated(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        assertThat(findTemplateById(inserted.getId()).isPresent(),
                is(true));

    }

    @Test
    public void insertBadRequest() throws Exception {

        String facilityName = "Chester";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        // Missing facilityId field
        final Template template1 = newTemplate(facility.get().getId());
        template1.setFacilityId(null);
        assertThrows(BadRequest.class,
                () -> templateService.insert(template1));

        // Invalid facilityId field
        final Template template2 = newTemplate(facility.get().getId());
        template2.setFacilityId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> templateService.insert(template2));

        // Missing name field
        final Template template3 = newTemplate(facility.get().getId());
        template3.setName(null);
        assertThrows(BadRequest.class,
                () -> templateService.insert(template3));

        // Missing allMats field
        final Template template4 = newTemplate(facility.get().getId());
        template4.setAllMats(null);
        assertThrows(BadRequest.class,
                () -> templateService.insert(template4));

        // Invalid allMats field
        final Template template5 = newTemplate(facility.get().getId());
        template5.setAllMats("142,b,144");
        assertThrows(BadRequest.class,
                () -> templateService.insert(template5));

        // Invalid handicapMats field
        final Template template6 = newTemplate(facility.get().getId());
        template6.setHandicapMats("142,b,144");
        assertThrows(BadRequest.class,
                () -> templateService.insert(template6));

        // Invalid handicapMats subset
        final Template template7 = newTemplate(facility.get().getId());
        template7.setHandicapMats("142,144");
        assertThrows(BadRequest.class,
                () -> templateService.insert(template7));

        // Invalid socketMats field
        final Template template8 = newTemplate(facility.get().getId());
        template8.setSocketMats("142,b,144");
        assertThrows(BadRequest.class,
                () -> templateService.insert(template8));

        // Invalid socketMats subset
        final Template template9 = newTemplate(facility.get().getId());
        template9.setSocketMats("142,144");
        assertThrows(BadRequest.class,
                () -> templateService.insert(template9));

    }

    @Test
    public void insertNotUnique() throws Exception {

        String facilityName = "Chester";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        Template template = newTemplate(facility.get().getId());
        template.setName("Chester COVID");
        assertThrows(NotUnique.class,
                () -> templateService.insert(template));

    }

    // update() tests

    @Test
    public void updateHappy() throws Exception {

        String facilityName = "Oakland";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));

        // Change something but keep name
        Template template1 = findTemplateByNameExact(
                facility.get().getId(), "Oakland Standard").get();
        template1.setComments(template1.getComments() + " Updated");
        templateService.update(template1.getId(), template1);

        // Change name to something unique
        Template template2 = findTemplateByNameExact
                (facility.get().getId(), "Oakland COVID").get();
        template2.setName(template2.getName() + " Updated");
        templateService.update(template2.getId(), template2);

        // OK to use same name in different facilities
        Template template3 = findTemplateByNameExact
                (facility.get().getId(), "Oakland Standard").get();
        template3.setName("San Jose Standard");
        templateService.update(template3.getId(), template3);

    }

    @Test
    public void updateHappyNoChange() throws Exception {

        List<Template> templates = findTemplatesAll();
        for (Template template : templates) {
            Template updated =
                    templateService.update(template.getId(), template);
            assertThat(updated, is(equalTo(template)));
        }

    }

    @Test
    public void updateBadRequest() throws Exception {

        String facilityName = "San Francisco";
        Optional<Facility> facility = findFacilityByNameExact(facilityName);
        assertThat(facility.isPresent(), is(true));
        String name = "San Francisco Standard";
        Optional<Template> original = findTemplateByNameExact
                (facility.get().getId(), name);

        // Missing facilityId field
        final Template template1 = findTemplateByNameExact
                (facility.get().getId(), "San Francisco COVID").get();
        template1.setFacilityId(null);
        assertThrows(BadRequest.class,
                () -> templateService.update(original.get().getId(),
                        template1));

        // Invalid facilityId field
        final Template template2 = findTemplateByNameExact
                (facility.get().getId(), "San Francisco Standard").get();
        template2.setFacilityId(Long.MAX_VALUE);
        assertThrows(BadRequest.class,
                () -> templateService.update(original.get().getId(),
                        template2));

        // Missing firstName field
        final Template template3 = findTemplateByNameExact
                (facility.get().getId(), "San Francisco Standard").get();
        template3.setName(null);
        assertThrows(BadRequest.class,
                () -> templateService.update(original.get().getId(),
                        template3));

        // MatsList tests were done for insertHappy()

    }

    @Test
    public void updateNotUnique() throws Exception {

        String facilityName1 = "San Francisco";
        Optional<Facility> facility1 = findFacilityByNameExact(facilityName1);
        assertThat(facility1.isPresent(), is(true));
        String name = "San Francisco Standard";
        Optional<Template> original = findTemplateByNameExact
                (facility1.get().getId(), name);
        assertThat(original.isPresent(), is(true));

        // Violate name uniqueness in same facility
        Template template1 = new Template();
        template1.copy(original.get());
        template1.setName("San Francisco COVID");
        assertThrows(NotUnique.class,
                () -> templateService.update
                        (original.get().getId(), template1));

        // Name uniqueness in different facility
        String facilityName2 = "San Jose";
        Optional<Facility> facility2 = findFacilityByNameExact(facilityName2);
        assertThat(facility2.isPresent(), is(true));
        Template template2 = new Template();
        template2.copy(original.get());
        template2.setFacilityId(facility2.get().getId());
        template2.setName("San Jose Standard");
        assertThrows(NotUnique.class,
                () -> templateService.update
                        (original.get().getId(), template2));

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

    private Optional<Template> findTemplateById(Long templateId) {
        TypedQuery<Template> query = entityManager.createNamedQuery
                (TEMPLATE_NAME + ".findById", Template.class)
                .setParameter(ID_COLUMN, templateId);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    private Optional<Template> findTemplateByNameExact
            (Long facilityId, String name) {
        TypedQuery<Template> query = entityManager.createNamedQuery
                (TEMPLATE_NAME + ".findByNameExact", Template.class)
                .setParameter(FACILITY_ID_COLUMN, facilityId)
                .setParameter(NAME_COLUMN, name);
        try {
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    private List<Template> findTemplatesAll() {
        return entityManager.createNamedQuery
                (TEMPLATE_NAME + ".findAll", Template.class)
                .getResultList();
    }

    private List<Template> findTemplatesByFacilityId(Long facilityId) {
        return entityManager.createNamedQuery
                (TEMPLATE_NAME + ".findByFacilityId", Template.class)
                .setParameter(FACILITY_ID_COLUMN, facilityId)
                .getResultList();
    }

    private Facility newFacility(String name) {
        return new Facility(
                true,
                null,
                null,
                null,
                null,
                name,
                null,
                null,
                null
        );
    }

    private Template newTemplate(Long facilityId) {
        return new Template(
                "1-12",
                "New Template Comment",
                facilityId,
                null,
                "New Template",
                null
        );
    }

}
