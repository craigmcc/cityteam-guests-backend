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
import org.cityteam.guests.action.ImportRequest;
import org.cityteam.guests.action.ImportProblem;
import org.cityteam.guests.action.ImportResults;
import org.cityteam.guests.model.Facility;
import org.cityteam.guests.model.Guest;
import org.cityteam.guests.model.Registration;
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
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static org.cityteam.guests.model.Constants.FACILITY_ID_COLUMN;
import static org.cityteam.guests.model.Constants.FACILITY_NAME;
import static org.cityteam.guests.model.Constants.GUEST_ID_COLUMN;
import static org.cityteam.guests.model.Constants.GUEST_NAME;
import static org.cityteam.guests.model.Constants.MAT_NUMBER_COLUMN;
import static org.cityteam.guests.model.Constants.REGISTRATION_DATE_COLUMN;
import static org.cityteam.guests.model.Constants.REGISTRATION_NAME;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@LocalBean
@Stateless
public class RegistrationService extends ModelService<Registration> {

    // Instance Variables ----------------------------------------------------

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private FacilityService facilityService;

    @Inject
    private GuestService guestService;

    // Static Variables ------------------------------------------------------

    private static final Logger LOG =
            Logger.getLogger(RegistrationService.class.getName());

    // Public Methods --------------------------------------------------------

    /**
     * <p>Cause the specified {@link Registration} to be assigned to the
     * {@link Guest} specified by <code>guestId</code>, and any other
     * fields in the {@link Assign} object to be copied to the
     * {@link Registration} being updated.</p>
     *
     * <p>It is legal to reassign a registration to the same guest,
     * to allow updating of the other fields.</p>
     *
     * @param registrationId ID of the registration to be assigned
     * @param assign Object containing assignment details
     *
     * @return The updated {@link Registration}
     *
     * @throws BadRequest Specified guest is specified or is not associated
     *                    with the same facility as this registration, or
     *                    the specified registration is already assigned
     * @throws NotFound Specified guest or registration cannot be found
     * @throws NotUnique If this guest is already assigned to a different
     *                   mat on this registration date
     * @throws InternalServerError A server side error has occurred
     */
    public Registration assign(@NotNull Long registrationId,
                               @NotNull Assign assign)
        throws BadRequest, InternalServerError, NotFound, NotUnique {

        try {

            // Look up the specified registration and verify unassigned
            // or already assigned to this guest
            TypedQuery<Registration> query = entityManager.createNamedQuery
                    (REGISTRATION_NAME + ".findById", Registration.class)
                    .setParameter(ID_COLUMN, registrationId);
            Registration registration = query.getSingleResult();

            // Verify that this registration is unassigned, or is already
            // assigned to the specified guest (to allow info updates)
            if ((registration.getGuestId() != null) &&
                    (registration.getGuestId() != assign.getGuestId())) {
                throw new BadRequest(String.format
                        ("registrationId: Registration %d is assigned to someone else",
                                registrationId));
            }

            // If unassigned, verify that the specified guest and
            // specified registration belong to the same facility
            if (registration.getGuestId() == null) {
                TypedQuery<Guest> query2 = entityManager.createNamedQuery
                        (GUEST_NAME + ".findById", Guest.class)
                        .setParameter(ID_COLUMN, assign.getGuestId());
                Guest guest = query2.getSingleResult();
                if (guest.getFacilityId().longValue() != registration.getFacilityId().longValue()) {
                    throw new BadRequest(String.format
                            ("guestId: Guest %d does not belong to facility %d",
                                    guest.getId(), registration.getId()));
                }
            }

            // Check for another assignment for this guest on this date
            List<Registration> existings =
                    findByFacilityAndDate(registration.getFacilityId(),
                            registration.getRegistrationDate());
            for (Registration existing : existings) {
                if ((existing.getGuestId() == assign.getGuestId()) &&
                        (existing.getId() != registration.getId())) {
                    throw new NotUnique(String.format
                            ("guestId: Guest %d is already assigned to mat %d",
                                    existing.getGuestId(), existing.getMatNumber()));
                }
            }

            // Update the assignment information and persist
            registration.setComments(assign.getComments());
            registration.setGuestId(assign.getGuestId());
            registration.setPaymentAmount(assign.getPaymentAmount());
            registration.setPaymentType(assign.getPaymentType());
            registration.setShowerTime(assign.getShowerTime());
            registration.setWakeupTime(assign.getWakeupTime());
            registration.setUpdated(LocalDateTime.now());
            entityManager.merge(registration);
            entityManager.flush();
            return registration;

        } catch (BadRequest e) {
            throw e;
        } catch (NotUnique e) {
            throw e;
        } catch (NoResultException e) {
            throw new NotFound(String.format
                    ("registrationId: Missing registration %d",
                            registrationId));
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("assign(%d,%s): %s,",
                            registrationId, assign, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    /**
     * <p>Cause the specified {@link Registration} to be deassigned from
     * any {@link Guest} to whom it is currently assigned.</p>
     *
     * @param registrationId ID of the registration to be deassigned
     *
     * @return The updated {@link Registration}
     *
     * @throws BadRequest If the specified registration is not
     *                    currently assigned to anyone
     * @throws InternalServerError A server side error has occurred
     * @throws NotFound Specified registration cannot be found
     */
    public Registration deassign(@NotNull Long registrationId)
        throws BadRequest, InternalServerError, NotFound {

        try {

            // Look up the specified registration and verify assigned
            TypedQuery<Registration> query = entityManager.createNamedQuery
                    (REGISTRATION_NAME + ".findById", Registration.class)
                    .setParameter(ID_COLUMN, registrationId);
            Registration registration = query.getSingleResult();
            if (registration.getGuestId() == null) {
                throw new BadRequest(String.format
                        ("registrationId: Registration %d is not currently assigned",
                                registrationId));
            }

            // Erase the assignment information and persist
            registration.setComments(null);
            registration.setGuestId(null);
            registration.setPaymentAmount(null);
            registration.setPaymentType(null);
            registration.setShowerTime(null);
            registration.setWakeupTime(null);
            registration.setUpdated(LocalDateTime.now());
            entityManager.merge(registration);
            entityManager.flush();
            return registration;

        } catch (BadRequest e) {
            throw e;
        } catch (NoResultException e) {
            throw new NotFound(String.format
                    ("registrationId: Missing registration %d",
                            registrationId));
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("deassign(%d): %s,",
                            registrationId, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public Registration delete(@NotNull Long registrationId)
            throws InternalServerError, NotFound {

        try {

            Registration deleted =
                    entityManager.find(Registration.class, registrationId);
            if (deleted != null) {
                entityManager.remove(deleted);
                deleted.setUpdated(LocalDateTime.now());
                return deleted;
            }

        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("delete(%d): %s",
                            registrationId, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

        throw new NotFound(
                String.format("registrationId: Missing registration %d",
                        registrationId));

    }

    /**
     * <p>Delete all {@link Registration} objects for the specified
     * facilityId and registrationDate, but only if none of the affected
     * registrations have had guests assigned to them.</p>
     *
     * @param facilityId ID of facility for which to delete registrations
     * @param registrationDate Registration date for which to delete
     *                         registrations
     *
     * @return The deleted registrations
     *
     * @throws BadRequest At least one registration that would be deleted
     *                    has been assigned to a guest already
     * @throws InternalServerError An internal server error has occurred
     * @throws NotFound No registrations exist for the specified facilityId
     *                  and registrationDate
     */
    public @NotNull List<Registration> deleteByFacilityAndDate(
            @NotNull Long facilityId, @NotNull LocalDate registrationDate)
            throws BadRequest, InternalServerError, NotFound {

        try {

            // Get a list of the registrations to be deleted
            TypedQuery<Registration> query = entityManager.createNamedQuery
                    (REGISTRATION_NAME + ".findByFacilityAndDate",
                            Registration.class)
                    .setParameter(FACILITY_ID_COLUMN, facilityId)
                    .setParameter(REGISTRATION_DATE_COLUMN,
                            registrationDate);
            List<Registration> registrations = query.getResultList();

            // Validate the list contents
            if (registrations.size() < 1) {
                throw new NotFound("registrationDate: No registrations " +
                        "exist for the specified facility and date");
            }
            LocalDateTime updated = LocalDateTime.now();
            for (Registration registration : registrations) {
                if (registration.getGuestId() != null) {
                    throw new BadRequest("registrationDate: At least one " +
                            "registration has already been assigned");
                }
                registration.setUpdated(updated);
            }

            // Delete the entire set of registrations
            // and return the original list
            Query query2 = entityManager.createNamedQuery
                    (REGISTRATION_NAME + ".deleteByFacilityAndDate")
                    .setParameter(FACILITY_ID_COLUMN, facilityId)
                    .setParameter(REGISTRATION_DATE_COLUMN, registrationDate);
            int deletedCount = query2.executeUpdate();
            if (deletedCount != registrations.size()) {
                throw new InternalServerError("delete: Found " +
                        registrations.size() +
                        " registrations but only deleted " + deletedCount);
            }
            return registrations;

        } catch (BadRequest|InternalServerError|NotFound e) {
            throw e;
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("deleteByFacilityAndDate(%d, %s)",
                            facilityId, registrationDate.toString()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public Registration find(@NotNull Long registrationId)
            throws InternalServerError, NotFound {

        try {

            TypedQuery<Registration> query = entityManager.createNamedQuery
                    (REGISTRATION_NAME + ".findById",
                            Registration.class)
                    .setParameter(ID_COLUMN, registrationId);
            return query.getSingleResult();

        } catch (NoResultException e) {
            throw new NotFound(
                    String.format("registrationId: Missing registration %d",
                            registrationId)
            );
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("find(%d): %s,",
                            registrationId, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public @NotNull List<Registration> findAll() throws InternalServerError {

        try {

            TypedQuery<Registration> query = entityManager.createNamedQuery
                    (REGISTRATION_NAME + ".findAll",
                            Registration.class);
            return query.getResultList();

        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("findAll(): %s", e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);

        }

    }

    public @NotNull List<Registration> findByFacilityAndDate(
            @NotNull Long facilityId, @NotNull LocalDate registrationDate)
        throws InternalServerError {

        try {

            TypedQuery<Registration> query = entityManager.createNamedQuery
                    (REGISTRATION_NAME + ".findByFacilityAndDate",
                            Registration.class)
                    .setParameter(FACILITY_ID_COLUMN, facilityId)
                    .setParameter(REGISTRATION_DATE_COLUMN,
                            registrationDate);
            return query.getResultList();

        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("findByFacilityAndDate(%d, %s)",
                            facilityId, registrationDate.toString()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    public @NotNull List<Registration> findByGuestId(
            @NotNull Long guestId)
        throws InternalServerError {

        try {

            TypedQuery<Registration> query = entityManager.createNamedQuery
                    (REGISTRATION_NAME + ".findByGuestId",
                            Registration.class)
                    .setParameter(GUEST_ID_COLUMN, guestId);
            return query.getResultList();

        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("findByGuestId(%d)", guestId), e);
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    public @NotNull ImportResults importByFacilityAndDate(
            @NotNull Long facilityId,
            @NotNull LocalDate registrationDate,
            List<ImportRequest> importRequests
    ) throws BadRequest, InternalServerError, NotFound, NotUnique {

        try {

            facilityService.find(facilityId);
            List<ImportProblem> problems = new ArrayList<>();
            List<Registration> registrations = new ArrayList<>();

            for (ImportRequest importRequest : importRequests) {

                // Create an unassigned registration
                Registration registration = new Registration(
                        facilityId,
                        importRequest.getFeatures(),
                        importRequest.getMatNumber(),
                        registrationDate
                );
                registration = insert(registration);

                // If this mat is already assigned, deal with it
                if (importRequest.getFirstName() != null) {

                    // Look up existing guest (if any)
                    Guest guest = null;
                    try {
                        guest = guestService.findByNameExact(
                                facilityId,
                                importRequest.getFirstName(),
                                importRequest.getLastName()
                        );
                    } catch (NotFound e) {
                        ; // We will create one below
                    }

                    // Create a new guest if necessary
                    if (guest == null) {
                        guest = new Guest(
                                null,
                                facilityId,
                                importRequest.getFirstName(),
                                importRequest.getLastName()
                        );
                        guest = guestService.insert(guest);
                    }

                    // Assign this guest to this registration
                    Assign assign = new Assign(
                            importRequest.getComments(),
                            guest.getId(),
                            importRequest.getPaymentAmount(),
                            importRequest.getPaymentType(),
                            importRequest.getShowerTime(),
                            importRequest.getWakeupTime()
                    );
                    try {
                        registration = assign(registration.getId(), assign);
                    } catch (NotUnique e) {
                        problems.add(new ImportProblem(
                                "NotUnique: " + e.getMessage(),
                                importRequest,
                                "Left unassigned"
                        ));
                    }

                }

                // Add this registration to our results
                registrations.add(registration);

            }

            return new ImportResults(problems, registrations);

        } catch (BadRequest e) {
            throw e;
        } catch (ConstraintViolationException e) {
            throw new BadRequest(formatMessage(e));
        } catch (NotFound e) {
            throw e;
        } catch (NotUnique e) {
            throw e;
        } catch (PersistenceException e) {
            handlePersistenceException(e);
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("importByFacilityAndDate(%d, %s)",
                            facilityId, registrationDate.toString()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

        return null;

    }

    @Override
    public Registration insert(@NotNull Registration registration)
            throws BadRequest, InternalServerError, NotUnique {

        Registration inserted = null;

        try {

            // We can only insert unassigned registrations
            if (registration.getGuestId() != null) {
                throw new BadRequest
                        ("guestId: Can only insert unassigned registrations");
            }

            // Check for required fields TODO - why is this necessary here?
            if (registration.getFacilityId() == null) {
                throw new BadRequest
                        ("facilityId: Cannot be null");
            } else if (registration.getRegistrationDate() == null) {
                throw new BadRequest
                        ("registrationDate: Cannot be null");
            }

            // Check foreign key validity TODO - why is this necessary here?
            TypedQuery<Facility> query1 = entityManager.createNamedQuery
                    (FACILITY_NAME + ".findById", Facility.class)
                    .setParameter(ID_COLUMN, registration.getFacilityId());
            try {
                query1.getSingleResult();
            } catch (NoResultException e) {
                throw new BadRequest
                        ("facilityId: Must specify valid facility");
            }

            // We will only be inserting the fields for an unassigned
            // registration, not all possible fields
            inserted = new Registration(
                    registration.getFacilityId(),
                    registration.getFeatures(),
                    registration.getMatNumber(),
                    registration.getRegistrationDate()
            );
            inserted.setId(null);
            inserted.setPublished(LocalDateTime.now());
            inserted.setUpdated(inserted.getPublished());

            // Check uniqueness constraint
            TypedQuery<Registration> query = entityManager.createNamedQuery
                    (REGISTRATION_NAME + ".findByFacilityAndDateAndMat",
                            Registration.class)
                    .setParameter(FACILITY_ID_COLUMN,
                            inserted.getFacilityId())
                    .setParameter(REGISTRATION_DATE_COLUMN,
                            inserted.getRegistrationDate())
                    .setParameter(MAT_NUMBER_COLUMN,
                            inserted.getMatNumber());
            try {
                query.getSingleResult();
                throw new NotUnique
                        ("facilityId/registrationDate/matNumber: " +
                                "Registration already exists for this combo");
            } catch (NoResultException e) {
                // Expected result for a new unique row
            }

            // Perform the requested insert
            entityManager.persist(inserted);

        } catch (BadRequest e) {
            throw e;
        } catch (ConstraintViolationException e) {
            throw new BadRequest(formatMessage(e));
        } catch (NotUnique e) {
            throw e;
        } catch (PersistenceException e) {
            handlePersistenceException(e);
        } catch (Exception e) {
            LOG.log(SEVERE,
                    String.format("insert(%s): %s",
                            inserted, e.getMessage()), e);
            throw new InternalServerError(e.getMessage(), e);
        }

        return inserted;

    }

    @Override
    public Registration update(@NotNull Long id, @NotNull Registration model)
            throws BadRequest, InternalServerError, NotFound, NotUnique {

        throw new InternalServerError("Cannot update() " +
                "Registrations directly after insertion - use " +
                "assign() or deassign().");

    }

}
