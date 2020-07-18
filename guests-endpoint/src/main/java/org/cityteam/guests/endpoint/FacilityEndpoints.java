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
import org.cityteam.guests.service.FacilityService;
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

@ApplicationScoped
@Path("/facilities")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Facility Endpoints")
public class FacilityEndpoints {

    // Instance Variables ----------------------------------------------------
    
    @Inject
    private FacilityService facilityService;

    // Endpoint Methods ------------------------------------------------------

    @DELETE
    @Path("/{facilityId}")
    @Operation(description = "Delete facility by ID.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Facility.class)
                    ),
                    description = "The deleted facility.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing facility message.",
                    responseCode = "404"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    public Response delete(
            @Parameter(description = "ID of facility to delete.")
            @PathParam("facilityId") Long facilityId
    ) {
        try {
            Facility facility = facilityService.delete(facilityId);
            return Response.ok(facility).build();
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
    @Path("/{facilityId}")
    @Operation(description = "Find facility by ID.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Facility.class)
                    ),
                    description = "The found facility.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing facility message.",
                    responseCode = "404"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    public Response find(
            @Parameter(description = "ID of facility to find.")
            @PathParam("facilityId") Long facilityId
    ) {
        try {
            Facility facility = facilityService.find(facilityId);
            return Response.ok(facility).build();
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
    @Operation(description = "Find all facilities, ordered by name.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Facility.class)
                    ),
                    description = "The found facilities.",
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
            return Response.ok(facilityService.findAll()).build();
        } catch (InternalServerError e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET
    @Path("/name/{name}")
    @Operation(description = "Find facilities matching name segment.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Facility.class)
                    ),
                    description = "The found facilities.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    public Response findByName(
            @Parameter(description = "Name matching segment of facilities to find.")
            @PathParam("name") String name
    ) {
        try {
            return Response.ok(facilityService.findByName(name)).build();
        } catch (InternalServerError e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET
    @Path("/nameExact/{name}")
    @Operation(description = "Find facilities matching an exact name.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Facility.class)
                    ),
                    description = "The found facilities.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing facility message.",
                    responseCode = "404"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    public Response findByNameExact(
            @Parameter(description = "Name matching facility to find.")
            @PathParam("name") String name
    ) {
        try {
            return Response.ok(facilityService.findByNameExact(name)).build();
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

    @POST
    @Operation(description = "Insert a new facility.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Facility.class)
                    ),
                    description = "The inserted facility.",
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
                    description = "facility to be inserted.",
                    name = "facility",
                    schema = @Schema(implementation = Facility.class)
            )
            Facility facility
    ) {
        try {
            facility = facilityService.insert(facility);
            URI uri = UriBuilder.fromResource(FacilityEndpoints.class)
                    .path(facility.getId().toString())
                    .build();
            return Response.created(uri)
                    .entity(facility)
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
    @Path("/{facilityId}")
    @Operation(description = "Update an existing facility.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Facility.class)
                    ),
                    description = "The updated facility.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Bad request message.",
                    responseCode = "400"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing facility message.",
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
            @Parameter(description = "ID of the facility to be updated.")
            @PathParam("facilityId") Long facilityId,
            @Parameter(
                    description = "Facility to be updated.",
                    name = "facility",
                    schema = @Schema(implementation = Facility.class)
            )
            Facility facility
    ) {
        try {
            facility = facilityService.update(facilityId, facility);
            return Response.ok(facility).build();
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
