/*
 * Copyright 2020 craigmcc.
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
package org.cityteam.guests.client;

import org.cityteam.guests.action.ImportRequest;
import org.cityteam.guests.action.ImportResults;
import org.cityteam.guests.model.Facility;
import org.cityteam.guests.model.Guest;
import org.cityteam.guests.model.Registration;
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.InternalServerError;
import org.craigmcc.library.shared.exception.NotFound;
import org.craigmcc.library.shared.exception.NotUnique;

import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;

public class FacilityClient extends AbstractServiceClient<Facility> {

    // Instance Variables ----------------------------------------------------
    
    private final WebTarget facilityTarget = getBaseTarget()
            .path(FACILITY_PATH);

    // Public Methods --------------------------------------------------------

    @Override
    public @NotNull Facility delete(@NotNull Long facilityId)
            throws InternalServerError, NotFound {

        Response response = facilityTarget
                .path(facilityId.toString())
                .request(MediaType.APPLICATION_JSON)
                .delete();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Facility.class);
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public @NotNull Facility find(@NotNull Long facilityId)
            throws InternalServerError, NotFound {

        Response response = facilityTarget
                .path(facilityId.toString())
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Facility.class);
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public @NotNull List<Facility> findAll()
            throws InternalServerError {

        Response response = facilityTarget
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(new GenericType<List<Facility>>() {});
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    /**
     * <p>Return a list of {@link Facility} objects matching the specified
     * name segment, ordered by name.</p>
     *
     * @param name The name segment to be matched
     */
    public @NotNull List<Facility> findByName(@NotNull String name)
            throws InternalServerError {

        Response response = facilityTarget
                .path("/name")
                .path(name)
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(
                        new GenericType<List<Facility>>() {}
                    );
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    public @NotNull Facility findByNameExact(@NotNull String name)
            throws InternalServerError, NotFound {

        Response response = facilityTarget
                .path("/nameExact")
                .path(name)
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Facility.class);
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    /**
     * <p>Return a list of {@link Guest} objects for the specified
     * facility, ordered by lastName/firstName.</p>
     *
     * @param facilityId ID of the facility for which to retrieve guests
     *
     * @throws InternalServerError If an internal server error has occurred
     * @throws NotFound If specified facilityId is not found
     */
    public @NotNull List<Guest> findGuestsByFacilityId
        (@NotNull Long facilityId)
            throws InternalServerError, NotFound {

        Response response = facilityTarget
                .path(facilityId.toString())
                .path("/guests")
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(
                    new GenericType<List<Guest>>() {
                    }
            );
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    /**
     * <p>Return a list of {@link Guest} objects for the specified
     * facility, matching the specified name segment,
     * ordered by lastName/firstName.</p>
     *
     * @param facilityId ID of the facility for which to retrieve guests
     * @param name Name segment for which to retrieve guests
     *
     * @throws InternalServerError If an internal server error has occurred
     * @throws NotFound If specified facilityId is not found
     */
    public @NotNull List<Guest> findGuestsByName(
        @NotNull Long facilityId,
        @NotNull String name)
            throws InternalServerError, NotFound {

        Response response = facilityTarget
                .path(facilityId.toString())
                .path("/guests")
                .path("/name")
                .path(name)
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(
                    new GenericType<List<Guest>>() {
                    }
            );
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    /**
     * <p>Return a {@link Guest} objects for the specified
     * facility, matching the specified firstName and lastName.
     *
     * @param facilityId ID of the facility for which to retrieve guest
     * @param firstName First name for which to retrieve guest
     * @param lastName Last name for which to retrieve guest
     *
     * @throws InternalServerError If an internal server error has occurred
     * @throws NotFound If specified facilityId is not found
     */
    public @NotNull Guest findGuestsByNameExact(
            @NotNull Long facilityId,
            @NotNull String firstName,
            @NotNull String lastName)
                throws InternalServerError, NotFound {

        Response response = facilityTarget
                .path(facilityId.toString())
                .path("/guests")
                .path("/nameExact")
                .path(firstName)
                .path(lastName)
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Guest.class);
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    public @NotNull List<Registration> findRegistrationsByFacilityAndDate
            (@NotNull Long facilityId, @NotNull LocalDate registrationDate)
        throws InternalServerError {

        Response response = facilityTarget
                .path(facilityId.toString())
                .path("/registrations")
                .path(registrationDate.toString())
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity
                    (new GenericType<List<Registration>>() {});
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    public @NotNull ImportResults importRegistrationsByFacilityAndDate(
            @NotNull Long facilityId,
            @NotNull LocalDate registrationDate,
            @NotNull List<ImportRequest> importRequests
    ) throws BadRequest, InternalServerError, NotFound, NotUnique {

        Response response = facilityTarget
                .path(facilityId.toString())
                .path("/registrations")
                .path(registrationDate.toString())
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(importRequests, MediaType.APPLICATION_JSON));
        if (response.getStatus() == RESPONSE_CREATED) {
            return response.readEntity(ImportResults.class);
        } else if (response.getStatus() == RESPONSE_BAD_REQUEST) {
            throw new BadRequest(response.readEntity(String.class));
        } else if (response.getStatus() == RESPONSE_CONFLICT) {
            throw new NotUnique(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public @NotNull Facility insert(@NotNull Facility facility)
            throws BadRequest, InternalServerError, NotUnique {

        Response response = facilityTarget
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(facility, MediaType.APPLICATION_JSON));
        if (response.getStatus() == RESPONSE_CREATED) {
            return response.readEntity(Facility.class);
        } else if (response.getStatus() == RESPONSE_BAD_REQUEST) {
            throw new BadRequest(response.readEntity(String.class));
        } else if (response.getStatus() == RESPONSE_CONFLICT) {
            throw new NotUnique(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public @NotNull Facility update(@NotNull Long facilityId,
                                    @NotNull Facility facility)
            throws BadRequest, InternalServerError, NotFound, NotUnique {

        Response response = facilityTarget
                .path(facilityId.toString())
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(facility, MediaType.APPLICATION_JSON));
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Facility.class);
        } else if (response.getStatus() == RESPONSE_BAD_REQUEST) {
            throw new BadRequest(response.readEntity(String.class));
        } else if (response.getStatus() == RESPONSE_CONFLICT) {
            throw new NotUnique(response.readEntity(String.class));
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

}
