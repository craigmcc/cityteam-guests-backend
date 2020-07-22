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
import org.craigmcc.library.model.ModelService;
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.InternalServerError;
import org.craigmcc.library.shared.exception.NotFound;
import org.craigmcc.library.shared.exception.NotUnique;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static org.cityteam.guests.model.Constants.FACILITY_NAME;
import static org.cityteam.guests.model.Constants.NAME_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@LocalBean
@Stateless
public class FacilityService extends ModelService<Facility> {

    // Instance Variables ----------------------------------------------------
    
    @PersistenceContext
    private EntityManager entityManager;

    // Static Variables ------------------------------------------------------

    private static final Logger LOG =
            Logger.getLogger(FacilityService.class.getName());

    // Public Methods --------------------------------------------------------

    @Override
    public @NotNull Facility delete(@NotNull Long facilityId)
            throws InternalServerError, NotFound {

        try {

            Facility deleted = entityManager.find(Facility.class, facilityId);
            if (deleted != null) {
                entityManager.remove(deleted);
                deleted.setUpdated(LocalDateTime.now());
                return deleted;
            }

        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("delete(%d): %s",
                            facilityId, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

        throw new NotFound(
                String.format("facilityId: Missing facility %d", facilityId)
        );

    }

    @Override
    public @NotNull Facility find(@NotNull Long facilityId)
            throws InternalServerError, NotFound {

        try {

            TypedQuery<Facility> query = entityManager.createNamedQuery
                    (FACILITY_NAME + ".findById", Facility.class)
                    .setParameter(ID_COLUMN, facilityId);
            return query.getSingleResult();

        } catch (NoResultException e) {
            throw new NotFound(
                    String.format("facilityId: Missing facility %d", facilityId)
            );
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("find(%d): %s", facilityId, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public @NotNull List<Facility> findAll()
            throws InternalServerError {

        try {

            TypedQuery<Facility> query = entityManager.createNamedQuery
                    (FACILITY_NAME + ".findAll", Facility.class);
            return query.getResultList();

        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("findAll(): %s", e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    public @NotNull List<Facility> findByName(@NotNull String name)
            throws InternalServerError {

        try {

            TypedQuery<Facility> query = entityManager.createNamedQuery
                    (FACILITY_NAME + ".findByName", Facility.class)
                    .setParameter(NAME_COLUMN, name);
            return query.getResultList();

        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("findByName(%s): %s",
                            name, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    public @NotNull Facility findByNameExact(@NotNull String name)
            throws InternalServerError, NotFound {

        try {

            TypedQuery<Facility> query = entityManager.createNamedQuery
                    (FACILITY_NAME + ".findByNameExact", Facility.class)
                    .setParameter(NAME_COLUMN, name);
            return query.getSingleResult();

        } catch (NoResultException e) {
            throw new NotFound(String.format
                    ("name: Missing facility '%s'", name));
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("findByNameExact(%s): %s",
                            name, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public @NotNull Facility insert(@NotNull Facility facility)
            throws BadRequest, InternalServerError, NotUnique {

        try {

            // Check uniqueness constraint
            try {
                findByNameExact(facility.getName());
                throw new NotUnique(String.format
                        ("name: Name '%s' is already in use",
                                facility.getName()));
            } catch (NotFound e) {
                // Expected result if unique
            }

            // Perform the requested insert
            facility.setId(null); // Ignore any specified primary key
            facility.setPublished(LocalDateTime.now());
            facility.setUpdated(facility.getPublished());
            entityManager.persist(facility);
            entityManager.flush();

        } catch (ConstraintViolationException e) {
            throw new BadRequest(formatMessage(e));
        } catch (NotUnique e) {
            throw e;
        } catch (PersistenceException e) {
            handlePersistenceException(e);
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("insert(%s): %s",
                            facility, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

        return facility;

    }

    @Override
    public @NotNull Facility update(@NotNull Long facilityId,
                                    @NotNull Facility facility)
            throws BadRequest, InternalServerError, NotFound, NotUnique {

        Facility original = null;

        try {

            // Check uniqueness constraint
            try {
                Facility duplicate = findByNameExact(facility.getName());
                if (!facility.getId().equals(duplicate.getId())) {
                    throw new NotUnique(String.format
                            ("name: Name '%s' is already in use",
                                    facility.getName()));
                }
                // Otherwise, updating something else on the current row
            } catch (NotFound e) {
                // Expected result if unique
            }

            // Perform requested update
            original = find(facilityId);
            original.copy(facility);
            original.setUpdated(LocalDateTime.now());
            entityManager.merge(original);
            entityManager.flush();

        } catch (ConstraintViolationException e) {
            throw new BadRequest(formatMessage(e));
        } catch (InternalServerError|NotFound|NotUnique e) {
            throw e;
        } catch (PersistenceException e) {
            handlePersistenceException(e);
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("update(%d, %s): %s",
                            facilityId, facility, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

        return original;

    }

}
