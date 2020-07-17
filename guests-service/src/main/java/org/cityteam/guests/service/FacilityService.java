package org.cityteam.guests.service;

import org.cityteam.guests.model.Facility;
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

import static org.cityteam.guests.model.Constants.FACILITY_NAME;
import static org.cityteam.guests.model.Constants.NAME_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

@LocalBean
@Stateless
public class FacilityService extends ModelService<Facility> {

    // Instance Variables ----------------------------------------------------
    
    @PersistenceContext
    protected EntityManager entityManager;

    // Public Methods --------------------------------------------------------

    @Override
    public @NotNull Facility delete(@NotNull Long id)
            throws InternalServerError, NotFound {

        try {

            Facility deleted = entityManager.find(Facility.class, id);
            if (deleted != null) {
                entityManager.remove(deleted);
                deleted.setUpdated(LocalDateTime.now());
                return deleted;
            }

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

        throw new NotFound(String.format("id: Missing book %d", id));

    }

    @Override
    public @NotNull Facility find(@NotNull Long id)
            throws InternalServerError, NotFound {

        try {

            TypedQuery<Facility> query = entityManager.createNamedQuery
                    (FACILITY_NAME + ".findById", Facility.class)
                    .setParameter(ID_COLUMN, id);
            Facility result = query.getSingleResult();
            return result;

        } catch (NoResultException e) {
            throw new NotFound("id: Missing facility " + id);
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    @Override
    public @NotNull List<Facility> findAll() throws InternalServerError {

        try {

            TypedQuery<Facility> query = entityManager.createNamedQuery
                    (FACILITY_NAME + ".findAll", Facility.class);
            List<Facility> results = query.getResultList();
            return results;

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    public @NotNull List<Facility> findByName(@NotNull String name)
            throws InternalServerError {

        try {

            TypedQuery<Facility> query = entityManager.createNamedQuery
                    (FACILITY_NAME + ".findByName", Facility.class)
                    .setParameter(NAME_COLUMN, name);
            List<Facility> result = query.getResultList();
            return result;

        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

    }

    public @NotNull Facility findByNameExact(@NotNull String name)
            throws InternalServerError, NotFound {

        try {

            TypedQuery<Facility> query = entityManager.createNamedQuery
                    (FACILITY_NAME + ".findByNameExact", Facility.class)
                    .setParameter(NAME_COLUMN, name);
            Facility result = query.getSingleResult();
            return result;

        } catch (NoResultException e) {
            throw new NotFound("name: Missing facility '" + name + "'");
        } catch (Exception e) {
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
                throw new NotUnique("name: Name '" + facility.getName() +
                        "' is already in use");
            } catch (InternalServerError e) {
                throw e;
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
            throw new InternalServerError(e.getMessage(), e);
        }

        return facility;

    }

    @Override
    public @NotNull Facility update(@NotNull Long facilityId, @NotNull Facility facility)
            throws BadRequest, InternalServerError, NotFound, NotUnique {

        Facility original = null;

        try {

            // Check uniqueness constraint
            try {
                Facility duplicate = findByNameExact(facility.getName());
                if (facility.getId() != duplicate.getId()) {
                    throw new NotUnique("name: Name '" + facility.getName() +
                            "' is already in use");
                }
                // Otherwise, updating something else on the current row
            } catch (NotFound e) {
                // Expected result if unique
            }

            // Perform requested update
            original = find(facility.getId());
            original.copy(facility);
            original.setUpdated(LocalDateTime.now());
            entityManager.merge(original);
            entityManager.flush();

        } catch (ConstraintViolationException e) {
            throw new BadRequest(formatMessage(e));
        } catch (InternalServerError e) {
            throw e;
        } catch (NotFound e) {
            throw e;
        } catch (NotUnique e) {
            throw e;
        } catch (PersistenceException e) {
            handlePersistenceException(e);
        } catch (Exception e) {
            throw new InternalServerError(e.getMessage(), e);
        }

        return original;

    }

}
