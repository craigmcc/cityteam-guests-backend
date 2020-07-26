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

import org.cityteam.guests.action.Assign;
import org.cityteam.guests.model.Registration;
import org.cityteam.guests.service.RegistrationService;
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

@ApplicationScoped
@Path("/registrations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RegistrationEndpoints {

    // Instance Variables ----------------------------------------------------

    @Inject
    private RegistrationService registrationService;

    // Endpoint Methods ------------------------------------------------------

    @POST
    @Path("/{registrationId}/assign")
    @Operation(description = "Assign a guest to the specified registration.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Registration.class)
                    ),
                    description = "The updated registration.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Bad request message.",
                    responseCode = "400"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing registration message.",
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
    public Response assign(
            @Parameter(description = "ID of the registration to be assigned.")
            @PathParam("registrationId") Long registrationId,
            @Parameter(
                    description = "Properties for this assignment. " +
                                  "Only guestId is required.",
                    name = "assign",
                    schema = @Schema(implementation = Assign.class)
            )
            Assign assign
    ) {

        try {
            Registration registration = registrationService.assign(
                    registrationId,
                    assign
            );
            return Response.ok(registration).build();
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

    @POST
    @Path("/{registrationId}/deassign")
    @Operation(description = "Deassign a guest to the specified registration.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Registration.class)
                    ),
                    description = "The updated registration.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Bad request message.",
                    responseCode = "400"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing registration message.",
                    responseCode = "404"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    public Response deassign(
            @Parameter(description = "ID of the registration to be deassigned.")
            @PathParam("registrationId") Long registrationId
    ) {

        try {
            Registration registration = registrationService.deassign(
                    registrationId
            );
            return Response.ok(registration).build();
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
        }

    }

    @DELETE
    @Path("/{registrationId}")
    @Operation(description = "Delete registration by ID.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Registration.class)
                    ),
                    description = "The deleted registration.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing registration message.",
                    responseCode = "404"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    public Response delete(
            @Parameter(description = "ID of registration to delete.")
            @PathParam("registrationId") Long registrationId
    ) {
        try {
            Registration registration = registrationService.delete(registrationId);
            return Response.ok(registration).build();
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
    @Path("/{registrationId}")
    @Operation(description = "Find registration by ID.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Registration.class)
                    ),
                    description = "The found registration.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing registration message.",
                    responseCode = "404"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    public Response find(
            @Parameter(description = "ID of registration to find.")
            @PathParam("registrationId") Long registrationId
    ) {
        try {
            Registration registration = registrationService.find(registrationId);
            return Response.ok(registration).build();
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
    @Operation(description = "Find all registrations, ordered by " +
            "facilityId/registrationDate/matNumber")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Registration.class)
                    ),
                    description = "The found registrations.",
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
            return Response.ok(registrationService.findAll()).build();
        } catch (InternalServerError e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @POST
    @Operation(description = "Insert a new registration.  Only unassigned " +
        "registrations can be created in this manner.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Registration.class)
                    ),
                    description = "The inserted registration.",
                    responseCode = "201"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Bad registration message.",
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
                    description = "Registration to be inserted.",
                    name = "registration",
                    schema = @Schema(implementation = Registration.class)
            )
                    Registration registration
    ) {
        try {
            registration = registrationService.insert(registration);
            URI uri = UriBuilder.fromResource(RegistrationEndpoints.class)
                    .path(registration.getId().toString())
                    .build();
            return Response.created(uri)
                    .entity(registration)
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
    @Path("/{registrationId}")
    @Operation(description = "Update an existing registration.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "This operation is not supported.",
                    responseCode = "500"
            )
    })
    public Response update(
            @Parameter(description = "ID of the registration to be updated.")
            @PathParam("registrationId") Long registrationId,
            @Parameter(
                    description = "Registration to be updated.",
                    name = "registration",
                    schema = @Schema(implementation = Registration.class)
            )
                    Registration registration
    ) {

        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("registrationId: This operation is not supported")
                .type(MediaType.TEXT_PLAIN)
                .build();

    }

}
