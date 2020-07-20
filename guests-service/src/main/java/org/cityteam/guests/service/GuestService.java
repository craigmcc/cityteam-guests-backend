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

import org.cityteam.guests.model.Guest;
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
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static org.cityteam.guests.model.Constants.FACILITY_ID_COLUMN;
import static org.cityteam.guests.model.Constants.FIRST_NAME_COLUMN;
import static org.cityteam.guests.model.Constants.GUEST_NAME;
import static org.cityteam.guests.model.Constants.LAST_NAME_COLUMN;
import static org.cityteam.guests.model.Constants.NAME_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@LocalBean
@Stateless
public class GuestService extends ModelService<Guest> {

    // Instance Variables ----------------------------------------------------

    @PersistenceContext
    protected EntityManager entityManager;

    // Static Variables ------------------------------------------------------

    private static final Logger LOG =
            Logger.getLogger(GuestService.class.getName());

    // Public Methods --------------------------------------------------------

    @Override
    public Guest delete(@NotNull Long guestId)
            throws InternalServerError, NotFound {

        try {

            Guest deleted = entityManager.find(Guest.class, guestId);
            if (deleted != null) {
                entityManager.remove(deleted);
                deleted.setUpdated(LocalDateTime.now());
                return deleted;
            }

        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("delete(%d): %s",
                            guestId, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

        throw new NotFound(
                String.format("guestId: Missing guest %d", guestId)
        );

    }

    @Override
    public Guest find(@NotNull Long guestId)
            throws InternalServerError, NotFound {

        try {

            TypedQuery<Guest> query = entityManager.createNamedQuery
                    (GUEST_NAME + ".findById", Guest.class)
                    .setParameter(ID_COLUMN, guestId);
            return query.getSingleResult();

        } catch (NoResultException e) {
            throw new NotFound(
                    String.format("guestId: Missing guest %d", guestId)
            );
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("find(%d): %s",
                            guestId, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public @NotNull List<Guest> findAll()
            throws InternalServerError {

        try {

            TypedQuery<Guest> query = entityManager.createNamedQuery
                    (GUEST_NAME + ".findAll", Guest.class);
            return query.getResultList();

        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("findAll(): %s", e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);

        }

    }

    public @NotNull List<Guest> findByFacilityId(@NotNull Long facilityId)
        throws InternalServerError {

        try {

            TypedQuery<Guest> query = entityManager.createNamedQuery
                    (GUEST_NAME + ".findByFacilityId", Guest.class)
                    .setParameter(FACILITY_ID_COLUMN, facilityId);
            return query.getResultList();

        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("findByFacilityId(%d): %s",
                            facilityId, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    public @NotNull List<Guest> findByName
            (@NotNull Long facilityId, @NotNull String name)
            throws InternalServerError {

        try {

            TypedQuery<Guest> query = entityManager.createNamedQuery
                    (GUEST_NAME + ".findByName", Guest.class)
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

    public @NotNull Guest findByNameExact
            (@NotNull Long facilityId,
             @NotNull String firstName,
             @NotNull String lastName)
            throws InternalServerError, NotFound {

        try {

            TypedQuery<Guest> query = entityManager.createNamedQuery
                    (GUEST_NAME + ".findByNameExact", Guest.class)
                    .setParameter(FACILITY_ID_COLUMN, facilityId)
                    .setParameter(FIRST_NAME_COLUMN, firstName)
                    .setParameter(LAST_NAME_COLUMN, lastName);
            return query.getSingleResult();

        } catch (NoResultException e) {
            throw new NotFound(String.format
                    ("firstName/lastName: Missing guest %s %s",
                            firstName, lastName));
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("findByNameExact(%d,%s,%s): %s",
                            facilityId, firstName, lastName,
                            e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public Guest insert(@NotNull Guest guest)
            throws BadRequest, InternalServerError, NotUnique {

        try {

            // Check uniqueness constraint
            try {
                findByNameExact(guest.getFacilityId(),
                        guest.getFirstName(), guest.getLastName());
                throw new NotUnique(String.format
                        ("name: Name '%s %s' is already in use within this facility",
                                guest.getFirstName(), guest.getLastName()));
            } catch (NotFound e) {
                // Expected result if unique
            }

            // Perform the requested insert
            guest.setId(null); // Ignore any specified primary key
            guest.setPublished(LocalDateTime.now());
            guest.setUpdated(guest.getPublished());
            entityManager.persist(guest);
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
                            guest, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

        return guest;

    }

    @Override
    public Guest update(@NotNull Long guestId, @NotNull Guest guest)
            throws BadRequest, InternalServerError, NotFound, NotUnique {

        Guest original = null;

        try {

            // Check uniqueness constraint
            try {
                Guest duplicate = findByNameExact(guest.getFacilityId(),
                        guest.getFirstName(), guest.getLastName());
                if (!guest.getId().equals(duplicate.getId())) {
                    throw new NotUnique(String.format
                            ("name: Name '%s %s' is already in use " +
                                    "within this facility",
                                    guest.getFirstName(), guest.getLastName()));
                }
                // Otherwise, updating something else on the current row
            } catch (NotFound e) {
                // Expected result if unique
            }

            // Perform requested update
            original = find(guestId);
            original.copy(guest);
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
                            guestId, guest, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

        return original;

    }
}
