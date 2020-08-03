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

import org.cityteam.guests.model.Template;
import org.cityteam.guests.model.types.MatsList;
import org.craigmcc.library.model.ModelService;
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.InternalServerError;
import org.craigmcc.library.shared.exception.NotFound;
import org.craigmcc.library.shared.exception.NotUnique;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static org.cityteam.guests.model.Constants.FACILITY_ID_COLUMN;
import static org.cityteam.guests.model.Constants.NAME_COLUMN;
import static org.cityteam.guests.model.Constants.TEMPLATE_NAME;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

// TODO - Generate and Ungenerate for templateId/registrationDate

@LocalBean
@Stateless
public class TemplateService extends ModelService<Template> {

    // Instance Variables ----------------------------------------------------

    @PersistenceContext
    private EntityManager entityManager;

    // Static Variables ------------------------------------------------------

    private static final Logger LOG =
            Logger.getLogger(TemplateService.class.getName());

    // Public Methods --------------------------------------------------------

    @Override
    public Template delete(@NotNull Long templateId)
            throws InternalServerError, NotFound {

        try {

            Template deleted = entityManager.find(Template.class, templateId);
            if (deleted != null) {
                entityManager.remove(deleted);
                deleted.setUpdated(LocalDateTime.now());
                return deleted;
            }

        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("delete(%d): %s",
                            templateId, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

        throw new NotFound(
                String.format("templateId: Missing template %d", templateId)
        );

    }

    @Override
    public Template find(@NotNull Long templateId)
            throws InternalServerError, NotFound {

        try {

            TypedQuery<Template> query = entityManager.createNamedQuery
                    (TEMPLATE_NAME + ".findById", Template.class)
                    .setParameter(ID_COLUMN, templateId);
            return query.getSingleResult();

        } catch (NoResultException e) {
            throw new NotFound(
                    String.format("templateId: Missing template %d", templateId)
            );
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("find(%d): %s",
                            templateId, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public @NotNull List<Template> findAll()
            throws InternalServerError {

        try {

            TypedQuery<Template> query = entityManager.createNamedQuery
                    (TEMPLATE_NAME + ".findAll", Template.class);
            return query.getResultList();

        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("findAll(): %s", e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);

        }

    }

    public @NotNull List<Template> findByFacilityId(@NotNull Long facilityId)
            throws InternalServerError {

        try {

            TypedQuery<Template> query = entityManager.createNamedQuery
                    (TEMPLATE_NAME + ".findByFacilityId", Template.class)
                    .setParameter(FACILITY_ID_COLUMN, facilityId);
            return query.getResultList();

        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("findByFacilityId(%d): %s",
                            facilityId, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    public @NotNull List<Template> findByName
            (@NotNull Long facilityId, @NotNull String name)
            throws InternalServerError {

        try {

            TypedQuery<Template> query = entityManager.createNamedQuery
                    (TEMPLATE_NAME + ".findByName", Template.class)
                    .setParameter(FACILITY_ID_COLUMN, facilityId)
                    .setParameter(NAME_COLUMN, name);
            return query.getResultList();

        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("findByName(%d,%s): %s",
                            facilityId, name, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    public @NotNull Template findByNameExact
            (@NotNull Long facilityId,
             @NotNull String name)
            throws InternalServerError, NotFound {

        try {

            TypedQuery<Template> query = entityManager.createNamedQuery
                    (TEMPLATE_NAME + ".findByNameExact", Template.class)
                    .setParameter(FACILITY_ID_COLUMN, facilityId)
                    .setParameter(NAME_COLUMN, name);
            return query.getSingleResult();

        } catch (NoResultException e) {
            throw new NotFound(String.format
                    ("name: Missing guest '%s'", name));
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("findByNameExact(%d,%s): %s",
                            facilityId, name,
                            e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public Template insert(@NotNull Template template)
            throws BadRequest, InternalServerError, NotUnique {

        try {

            // Check uniqueness constraint
            try {
                findByNameExact
                        (template.getFacilityId(), template.getName());
                throw new NotUnique(String.format
                        ("name: Name '%s' is already in use " +
                         "within this facility", template.getName()));
            } catch (NotFound e) {
                // Expected result if unique
            }

            // Check valid mat lists
            checkMatLists(template);

            // Perform the requested insert
            template.setId(null); // Ignore any specified primary key
            template.setPublished(LocalDateTime.now());
            template.setUpdated(template.getPublished());
            entityManager.persist(template);
            entityManager.flush();

        } catch (BadRequest|InternalServerError|NotUnique e) {
            throw e;
        } catch (ConstraintViolationException e) {
            throw new BadRequest(formatMessage(e));
        } catch (PersistenceException e) {
            handlePersistenceException(e);
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("insert(%s): %s",
                            template, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

        return template;

    }

    @Override
    public Template update(@NotNull Long templateId, @NotNull Template template)
            throws BadRequest, InternalServerError, NotFound, NotUnique {

        Template original = null;

        try {

            // Check uniqueness constraint
            try {
                Template duplicate = findByNameExact
                        (template.getFacilityId(), template.getName());
                if ((duplicate != null) &&
                        !templateId.equals(duplicate.getId())) {
                    throw new NotUnique(String.format
                            ("name: Name '%s' is already in use " +
                             "within this facility", template.getName()));
                }
                // Otherwise, updating something else on the current row
            } catch (NotFound e) {
                // Expected result if unique
            }

            // Check valid mat lists
            checkMatLists(template);

            // Perform requested update
            original = find(templateId);
            original.copy(template);
            original.setUpdated(LocalDateTime.now());
            entityManager.merge(original);
            entityManager.flush();

        } catch (BadRequest|InternalServerError|NotFound|NotUnique e) {
            throw e;
        } catch (ConstraintViolationException e) {
            throw new BadRequest(formatMessage(e));
        } catch (PersistenceException e) {
            handlePersistenceException(e);
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("update(%d, %s): %s",
                            templateId, template, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

        return original;

    }

    // Private Methods -------------------------------------------------------

    private void checkMatLists(Template template) throws BadRequest {

        MatsList allMats = null;
        List<String> messages = new ArrayList<>();

        try {
            if (template.getAllMats() == null) {
                messages.add("allMats: Cannot be null");
            } else {
                allMats = new MatsList(template.getAllMats());
            }
        } catch (IllegalArgumentException e) {
            messages.add("allMats: Invalid mats list syntax");
        }

        if ((allMats != null) && (template.getHandicapMats() != null)) {
            try {
                MatsList handicapMats =
                        new MatsList(template.getHandicapMats());
                if (!handicapMats.isSubsetOf(allMats)) {
                    messages.add("handicapMats: contains at least one " +
                            "mat number that is not part of allMats");
                }
            } catch (IllegalArgumentException e) {
                messages.add("handicapMats: Invalid mats list syntax");
            }
        }

        if ((allMats != null) && (template.getSocketMats() != null)) {
            try {
                MatsList socketMats =
                        new MatsList(template.getSocketMats());
                if (!socketMats.isSubsetOf(allMats)) {
                    messages.add("socketMats: Contains at least one " +
                            "mat number that is not part of allMats");
                }
            } catch (IllegalArgumentException e) {
                messages.add("socketMats: Invalid mats list syntax");
            }
        }

        if (messages.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String message : messages) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(message);
            }
            throw new BadRequest(sb.toString());
        }

    }

}
