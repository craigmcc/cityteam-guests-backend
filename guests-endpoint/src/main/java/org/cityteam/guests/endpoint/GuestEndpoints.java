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

import org.cityteam.guests.model.Ban;
import org.cityteam.guests.model.Guest;
import org.cityteam.guests.model.Registration;
import org.cityteam.guests.service.BanService;
import org.cityteam.guests.service.GuestService;
import org.cityteam.guests.service.RegistrationService;
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.InternalServerError;
import org.craigmcc.library.shared.exception.NotFound;
import org.craigmcc.library.shared.exception.NotUnique;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

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
@Path("/guests")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(
        description = "CRUD operations for managing overnight guests " +
                "of a particular CityTeam facility.  Guests within a " +
                "facility must have a unique firstName/lastName combination.",
        name = "Guest Endpoints"
)
public class GuestEndpoints {

    // Instance Variables ----------------------------------------------------
    
    @Inject
    private BanService banService;

    @Inject
    private GuestService guestService;
    
    @Inject
    private RegistrationService registrationService;

    // Endpoint Methods ------------------------------------------------------

    @DELETE
    @Path("/{guestId}")
    @Operation(description = "Delete a guest by ID.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Guest.class)
                    ),
                    description = "The deleted guest.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing guest message.",
                    responseCode = "404"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    public Response delete(
            @Parameter(description = "ID of the guest to delete.")
            @PathParam("guestId") Long guestId
    ) {
        try {
            Guest guest = guestService.delete(guestId);
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
    @Path("/{guestId}")
    @Operation(description = "Find a guest by ID.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Guest.class)
                    ),
                    description = "The found guest.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing guest message.",
                    responseCode = "404"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    public Response find(
            @Parameter(description = "ID of the guest to find.")
            @PathParam("guestId") Long guestId
    ) {
        try {
            Guest guest = guestService.find(guestId);
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
    @Operation(description = "Find all guests, ordered by " +
                             "facilityId/lastName/firstName")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Guest.class,
                            type = SchemaType.ARRAY)
                    ),
                    description = "The found guests.",
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
            return Response.ok(guestService.findAll()).build();
        } catch (InternalServerError e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET
    @Path("/{guestId}/bans")
    @Operation(description = "Find bans by guest ID, ordered by fromDate.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Ban.class,
                            type = SchemaType.ARRAY)
                    ),
                    description = "The found bans.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    public Response findBansByGuestId(
            @Parameter(description = "ID of the guest for which to find bans.")
            @PathParam("guestId") Long guestId
    ) {
        try {
            return Response.ok(banService.findByGuestId(guestId)).build();
        } catch (InternalServerError e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET
    @Path("/{guestId}/bans/{registrationDate}")
    @Operation(description = "Find any ban by guest ID and registrationDate.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Ban.class)
                    ),
                    description = "The found ban.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Not found message.",
                    responseCode = "404"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    public Response findBansByGuestIdAndRegistrationDate(
            @Parameter(description = "ID of the guest for which to find ban.")
            @PathParam("guestId") Long guestId,
            @Parameter(description = "Registration date for which to find ban.")
            @PathParam("registrationDate") String registrationDate
    ) {
        try {
            return Response.ok(banService.findByGuestIdAndRegistrationDate
                    (guestId, LocalDate.parse(registrationDate))).build();
        } catch (NotFound e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        } catch (InternalServerError e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET
    @Path("/{guestId}/registrations")
    @Operation(description = "Find all registrations by guest ID, ordered " +
            "by registrationDate.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Registration.class,
                            type = SchemaType.ARRAY)
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
    public Response findRegistrationsByGuestId(
            @Parameter(description = "ID of the guest for which to find registrations.")
            @PathParam("guestId") Long guestId
    ) {
        try {
            return Response.ok(registrationService.findByGuestId(guestId)).build();
        } catch (InternalServerError e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @POST
    @Operation(description = "Insert a new guest.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Guest.class)
                    ),
                    description = "The inserted guest.",
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
                    description = "Guest to be inserted.",
                    name = "guest",
                    schema = @Schema(implementation = Guest.class)
            )
                    Guest guest
    ) {
        try {
            guest = guestService.insert(guest);
            URI uri = UriBuilder.fromResource(GuestEndpoints.class)
                    .path(guest.getId().toString())
                    .build();
            return Response.created(uri)
                    .entity(guest)
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
    @Path("/{guestId}")
    @Operation(description = "Update an existing guest.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Guest.class)
                    ),
                    description = "The updated guest.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Bad request message.",
                    responseCode = "400"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing guest message.",
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
            @Parameter(description = "ID of the guest to be updated.")
            @PathParam("guestId") Long guestId,
            @Parameter(
                    description = "Guest to be updated.",
                    name = "guest",
                    schema = @Schema(implementation = Guest.class)
            )
                    Guest guest
    ) {
        try {
            guest = guestService.update(guestId, guest);
            return Response.ok(guest).build();
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
