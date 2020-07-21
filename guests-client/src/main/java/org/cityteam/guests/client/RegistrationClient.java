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

import org.cityteam.guests.action.Assign;
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
import java.util.List;

public class RegistrationClient extends AbstractServiceClient<Registration> {

    // Instance Variables ----------------------------------------------------

    private final WebTarget registrationTarget = getBaseTarget()
            .path(REGISTRATION_PATH);

    // Public Methods --------------------------------------------------------

    public @NotNull Registration assign(@NotNull Long registrationId,
                                        @NotNull Assign assign)
        throws BadRequest, InternalServerError, NotFound {

        Response response = registrationTarget
                .path(registrationId.toString())
                .path("/assign")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(assign, MediaType.APPLICATION_JSON));
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Registration.class);
        } else if (response.getStatus() == RESPONSE_BAD_REQUEST) {
            throw new BadRequest(response.readEntity(String.class));
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    public @NotNull Registration deassign(@NotNull Long registrationId)
        throws BadRequest, InternalServerError, NotFound {

        Response response = registrationTarget
                .path(registrationId.toString())
                .path("/deassign")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(""));
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Registration.class);
        } else if (response.getStatus() == RESPONSE_BAD_REQUEST) {
            throw new BadRequest(response.readEntity(String.class));
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public @NotNull Registration delete(@NotNull Long registrationId)
            throws InternalServerError, NotFound {

        Response response = registrationTarget
                .path(registrationId.toString())
                .request(MediaType.APPLICATION_JSON)
                .delete();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Registration.class);
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public @NotNull Registration find(@NotNull Long registrationId)
            throws InternalServerError, NotFound {

        Response response = registrationTarget
                .path(registrationId.toString())
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Registration.class);
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public @NotNull List<Registration> findAll()
            throws InternalServerError {

        Response response = registrationTarget
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(new GenericType<List<Registration>>() {});
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public @NotNull Registration insert(@NotNull Registration registration)
            throws BadRequest, InternalServerError, NotUnique {

        Response response = registrationTarget
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(registration, MediaType.APPLICATION_JSON));
        if (response.getStatus() == RESPONSE_CREATED) {
            return response.readEntity(Registration.class);
        } else if (response.getStatus() == RESPONSE_BAD_REQUEST) {
            throw new BadRequest(response.readEntity(String.class));
        } else if (response.getStatus() == RESPONSE_CONFLICT) {
            throw new NotUnique(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public @NotNull Registration update(@NotNull Long registrationId,
                                    @NotNull Registration registration)
            throws BadRequest, InternalServerError, NotFound, NotUnique {

        Response response = registrationTarget
                .path(registrationId.toString())
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(registration, MediaType.APPLICATION_JSON));
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Registration.class);
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
