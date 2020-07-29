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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static org.cityteam.guests.model.Constants.BAN_NAME;
import static org.cityteam.guests.model.Constants.GUEST_ID_COLUMN;
import static org.cityteam.guests.model.Constants.REGISTRATION_DATE_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@LocalBean
@Stateless
public class BanService extends ModelService<Ban> {

    // Instance Variables ----------------------------------------------------

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private GuestService guestService;

    // Static Variables ------------------------------------------------------

    private static final Logger LOG =
            Logger.getLogger(BanService.class.getName());

    // Public Methods --------------------------------------------------------

    @Override
    public Ban delete(@NotNull Long banId)
            throws InternalServerError, NotFound {

        try {

            Ban deleted = entityManager.find(Ban.class, banId);
            if (deleted != null) {
                entityManager.remove(deleted);
                deleted.setUpdated(LocalDateTime.now());
                return deleted;
            }

        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("delete(%d): %s",
                            banId, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

        throw new NotFound(
                String.format("banId: Missing ban %d", banId)
        );

    }

    @Override
    public Ban find(@NotNull Long banId)
            throws InternalServerError, NotFound {

        try {

            TypedQuery<Ban> query = entityManager.createNamedQuery
                    (BAN_NAME + ".findById", Ban.class)
                    .setParameter(ID_COLUMN, banId);
            return query.getSingleResult();

        } catch (NoResultException e) {
            throw new NotFound(
                    String.format("banId: Missing ban %d", banId)
            );
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("find(%d): %s", banId, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public @NotNull List<Ban> findAll()
            throws InternalServerError {

        try {

            TypedQuery<Ban> query = entityManager.createNamedQuery
                    (BAN_NAME + ".findAll", Ban.class);
            return query.getResultList();

        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("findAll(): %s", e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    public @NotNull List<Ban> findByGuestId(@NotNull Long guestId)
        throws InternalServerError {

        try {

            TypedQuery<Ban> query = entityManager.createNamedQuery
                    (BAN_NAME + ".findByGuestId", Ban.class)
                    .setParameter(GUEST_ID_COLUMN, guestId);
            return query.getResultList();

        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("findByGuestId(%d): %s",
                            guestId, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    public @NotNull Ban findByGuestIdAndRegistrationDate(
            @NotNull Long guestId,
            @NotNull LocalDate registrationDate)
            throws InternalServerError, NotFound {

        try {

            TypedQuery<Ban> query = entityManager.createNamedQuery
                    (BAN_NAME + ".findByGuestIdAndRegistrationDate",
                            Ban.class)
                    .setParameter(GUEST_ID_COLUMN, guestId)
                    .setParameter(REGISTRATION_DATE_COLUMN, registrationDate);
            return query.getSingleResult();

        } catch (NoResultException e) {
            throw new NotFound("guestId/registrationDate: Missing ban for " +
                    "guestId " + guestId + " and date " + registrationDate);
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format(
                            "findByGuestIdAndRegistrationDate(%d, %s): %s",
                            guestId, registrationDate, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public Ban insert(@NotNull Ban ban)
            throws BadRequest, InternalServerError, NotUnique {

        try {

            // Check from/to ordering
            if (ban.getBanFrom() == null) {
                throw new BadRequest("banFrom: Cannot be null");
            }
            if (ban.getBanTo() == null) {
                throw new BadRequest("banTo: Cannot be null");
            }
            if (ban.getBanFrom().compareTo(ban.getBanTo()) > 0) {
                throw new BadRequest("banFrom: Cannot be greater than banTo");
            }

            // Check valid guest
            try {
                if (ban.getGuestId() ==  null) {
                    throw new BadRequest("guestId: Cannot be null");
                }
                guestService.find(ban.getGuestId());
            } catch (NotFound e) {
                throw new BadRequest(String.format("guestId: Missing guest %d",
                        ban.getGuestId()));
            }

            // Check overlap with existing bans
            List<Ban> existingBans = findByGuestId(ban.getGuestId());
            for (Ban existingBan : existingBans) {
                if ((ban.getBanFrom().compareTo(existingBan.getBanFrom()) >= 0) &&
                    (ban.getBanFrom().compareTo(existingBan.getBanTo()) <= 0)) {
                    throw new NotUnique("banFrom: Overlaps existing ban");
                }
                if ((ban.getBanTo().compareTo(existingBan.getBanFrom()) >= 0) &&
                    (ban.getBanTo().compareTo(existingBan.getBanTo()) <= 0)) {
                    throw new NotUnique("banTo: Overlaps existing ban");
                }
                if ((ban.getBanFrom().compareTo(existingBan.getBanFrom()) <= 0) &&
                    (ban.getBanTo().compareTo(existingBan.getBanTo()) >= 0)) {
                    throw new NotUnique("banFrom/banTo: Overlaps existing ban");
                }
            }

            // Perform the requested insert
            ban.setId(null); // Ignore any specified primary key
            ban.setPublished(LocalDateTime.now());
            ban.setUpdated(ban.getPublished());
            entityManager.persist(ban);
            entityManager.flush();

            // TODO - pass through InternalServerError - see update() handling
        } catch (BadRequest|InternalServerError|NotUnique e) {
            throw e;
        } catch (ConstraintViolationException e) {
            throw new BadRequest(formatMessage(e));
        } catch (PersistenceException e) {
            handlePersistenceException(e);
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("insert(%s): %s",
                            ban, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

        return ban;

    }

    @Override
    public Ban update(@NotNull Long banId, @NotNull Ban ban)
            throws BadRequest, InternalServerError, NotFound, NotUnique {

        // NOTE:  Only the active, comments, and staff values
        // can be updated.  Any other type of change requries
        // inserting a new Ban object.

        Ban original = null;

        try {

            // Look up the original ban
            original = find(banId);

            // Verify that only valid columns can be changed
            if ((ban.getBanFrom() == null) ||
                    !ban.getBanFrom().equals(original.getBanFrom())) {
                throw new BadRequest("banFrom: Cannot be changed on an update");
            }
            if ((ban.getBanTo() == null) ||
                    !ban.getBanTo().equals(original.getBanTo())) {
                throw new BadRequest("banTo: Cannot be changed on an update");
            }
            if ((ban.getGuestId() == null) ||
                    !ban.getGuestId().equals(original.getGuestId())) {
                throw new BadRequest("guestId: Cannot be changed on an update");
            }

            // Perform requested update
            original.setUpdated(LocalDateTime.now());
            original.setActive(ban.getActive());
            original.setComments(ban.getComments());
            original.setStaff(ban.getStaff());
            entityManager.merge(original);
            entityManager.flush();

        } catch (BadRequest|InternalServerError|NotFound/* |NotUnique */ e) {
            throw e;
        } catch (ConstraintViolationException e) {
            throw new BadRequest(formatMessage(e));
        } catch (PersistenceException e) {
            handlePersistenceException(e);
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("update(%d, %s): %s",
                            banId, ban, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

        return original;

    }

}
