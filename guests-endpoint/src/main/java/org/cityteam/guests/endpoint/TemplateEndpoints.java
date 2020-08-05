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
package org.cityteam.guests.endpoint;

import org.cityteam.guests.model.Facility;
import org.cityteam.guests.model.Template;
import org.cityteam.guests.service.RegistrationService;
import org.cityteam.guests.service.TemplateService;
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.InternalServerError;
import org.craigmcc.library.shared.exception.NotFound;
import org.craigmcc.library.shared.exception.NotUnique;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.time.LocalDate;

@ApplicationScoped
@Path("/templates")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TemplateEndpoints {

    // Instance Variables ----------------------------------------------------

    @Inject
    private TemplateService templateService;

    @Inject
    private RegistrationService registrationService;

    // Endpoint Methods ------------------------------------------------------

    @DELETE
    @Path("/{templateId}")
    @Operation(description = "Delete template by ID.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Template.class)
                    ),
                    description = "The deleted template.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing template message.",
                    responseCode = "404"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    public Response delete(
            @Parameter(description = "ID of template to delete.")
            @PathParam("templateId") Long templateId
    ) {
        try {
            Template guest = templateService.delete(templateId);
            return Response.ok(guest).build();
        } catch (InternalServerError e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (NotFound e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET
    @Path("/{templateId}")
    @Operation(description = "Find template by ID.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Template.class)
                    ),
                    description = "The found template.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing template message.",
                    responseCode = "404"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    public Response find(
            @Parameter(description = "ID of template to find.")
            @PathParam("templateId") Long templateId
    ) {
        try {
            Template guest = templateService.find(templateId);
            return Response.ok(guest).build();
        } catch (InternalServerError e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (NotFound e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET
    @Operation(description = "Find all templates, ordered by " +
            "facilityId/name")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Template.class)
                    ),
                    description = "The found templates.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    public Response findAll() {
        try {
            return Response.ok(templateService.findAll()).build();
        } catch (InternalServerError e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @POST
    @Operation(description = "Insert a new template.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Template.class)
                    ),
                    description = "The inserted template.",
                    responseCode = "201"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Bad request message.",
                    responseCode = "400"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Uniqueness conflict message.",
                    responseCode = "409"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    public Response insert(
            @Parameter(
                    description = "Template to be inserted.",
                    name = "template",
                    schema = @Schema(implementation = Template.class)
            )
                    Template template
    ) {
        try {
            template = templateService.insert(template);
            URI uri = UriBuilder.fromResource(TemplateEndpoints.class)
                    .path(template.getId().toString())
                    .build();
            return Response.created(uri)
                    .entity(template)
                    .build();
        } catch (BadRequest e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (InternalServerError e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (NotUnique e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @PUT
    @Path("/{templateId}")
    @Operation(description = "Update an existing template.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Template.class)
                    ),
                    description = "The updated template.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Bad request message.",
                    responseCode = "400"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing template message.",
                    responseCode = "404"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Uniqueness conflict message.",
                    responseCode = "409"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    public Response update(
            @Parameter(description = "ID of the template to be updated.")
            @PathParam("templateId") Long templateId,
            @Parameter(
                    description = "Template to be updated.",
                    name = "template",
                    schema = @Schema(implementation = Template.class)
            )
                    Template template
    ) {
        try {
            template = templateService.update(templateId, template);
            return Response.ok(template).build();
        } catch (BadRequest e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (InternalServerError e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (NotFound e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (NotUnique e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

    }

}
