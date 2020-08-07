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

import org.cityteam.guests.model.Registration;
import org.cityteam.guests.model.Template;
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

public class TemplateClient extends AbstractServiceClient<Template> {

    // Instance Variables ----------------------------------------------------

    private final WebTarget templateTarget = getBaseTarget()
            .path(TEMPLATE_PATH);

    // Public Methods --------------------------------------------------------

    @Override
    public @NotNull Template delete(@NotNull Long templateId)
            throws InternalServerError, NotFound {

        Response response = templateTarget
                .path(templateId.toString())
                .request(MediaType.APPLICATION_JSON)
                .delete();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Template.class);
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public @NotNull Template find(@NotNull Long templateId)
            throws InternalServerError, NotFound {

        Response response = templateTarget
                .path(templateId.toString())
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Template.class);
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public @NotNull List<Template> findAll()
            throws InternalServerError {

        Response response = templateTarget
                .request(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(new GenericType<List<Template>>() {});
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    /**
     * <p>For the given templateId and registrationDate, create and return
     * a list of unassigned {@link Registration} objects, in preparation
     * for checking in nightly guests.</p>
     *
     * @param templateId ID of the template used as the basis for
     *                   generating {@link Registration} objects
     * @param registrationDate Date for which to generate
     *                         {@link Registration} objects
     *
     * @return List of generated {@link Registration} objects
     *
     * @throws BadRequest If one or more registrations already exist for
     *                    the specified registration date and corresponding
     *                    facility
     * @throws InternalServerError If a server side processing error occurs
     * @throws NotFound If no template with the specified ID can be found
     * @throws NotUnique If attempting to add the same mat number twice
     */
    public @NotNull List<Registration> generate
            (@NotNull Long templateId, @NotNull LocalDate registrationDate)
        throws BadRequest, InternalServerError, NotFound, NotUnique {

        Response response = templateTarget
                .path("/" + templateId)
                .path("/registrations")
                .path("/" + registrationDate.toString())
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(null));
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(new GenericType<>() {});
        } else if (response.getStatus() == RESPONSE_BAD_REQUEST) {
            throw new BadRequest(response.readEntity(String.class));
        } else if (response.getStatus() == RESPONSE_NOT_FOUND) {
            throw new NotFound(response.readEntity(String.class));
        } else if (response.getStatus() == RESPONSE_CONFLICT) {
            throw new NotUnique(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }
    }

    @Override
    public @NotNull Template insert(@NotNull Template template)
            throws BadRequest, InternalServerError, NotUnique {

        Response response = templateTarget
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(template, MediaType.APPLICATION_JSON));
        if (response.getStatus() == RESPONSE_CREATED) {
            return response.readEntity(Template.class);
        } else if (response.getStatus() == RESPONSE_BAD_REQUEST) {
            throw new BadRequest(response.readEntity(String.class));
        } else if (response.getStatus() == RESPONSE_CONFLICT) {
            throw new NotUnique(response.readEntity(String.class));
        } else {
            throw new InternalServerError(response.readEntity(String.class));
        }

    }

    @Override
    public @NotNull Template update(@NotNull Long templateId,
                                    @NotNull Template template)
            throws BadRequest, InternalServerError, NotFound, NotUnique {

        Response response = templateTarget
                .path(templateId.toString())
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.entity(template, MediaType.APPLICATION_JSON));
        if (response.getStatus() == RESPONSE_OK) {
            return response.readEntity(Template.class);
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
