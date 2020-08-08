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

import org.cityteam.guests.action.ImportRequest;
import org.cityteam.guests.action.ImportResults;
import org.cityteam.guests.model.Facility;
import org.cityteam.guests.model.Guest;
import org.cityteam.guests.model.Registration;
import org.cityteam.guests.model.Template;
import org.cityteam.guests.service.FacilityService;
import org.cityteam.guests.service.GuestService;
import org.cityteam.guests.service.RegistrationService;
import org.cityteam.guests.service.TemplateService;
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
import java.util.List;

@ApplicationScoped
@Path("/facilities")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(
        description = "CRUD operations for managing separate CityTeam " +
                "facilities, that each have their own guests, " +
                "registrations, and templates.",
        name = "Facility Endpoints"
)
public class FacilityEndpoints {

    // Instance Variables ----------------------------------------------------
    
    @Inject
    private FacilityService facilityService;

    @Inject
    private GuestService guestService;

    @Inject
    private RegistrationService registrationService;

    @Inject
    private TemplateService templateService;

    // Endpoint Methods ------------------------------------------------------

    @DELETE
    @Path("/{facilityId}")
    @Operation(description = "Delete a facility by ID.")
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
            @Parameter(description = "ID of the facility to delete.")
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

    @DELETE
    @Path("/{facilityId}/registrations/{registrationDate}")
    @Operation(description = "Delete registrations for a facility and " +
            "specific registration date, but only if none are assigned.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Registration.class,
                            type = SchemaType.ARRAY)
                    ),
                    description = "The deleted registrations.",
                    responseCode = "200"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "At least one assigned registration message.",
                    responseCode = "400"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "No registrations found message.",
                    responseCode = "404"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Internal server error message.",
                    responseCode = "500"
            )
    })
    public Response deleteRegistrationsByFacilityAndDate(
            @Parameter(description = "Facility ID for which to delete " +
                    "registrations.")
            @PathParam("facilityId") Long facilityId,
            @Parameter(description = "Registration date for which to " +
                    "delete registrations.")
            @PathParam("registrationDate") String registrationDate
    ) {
        try {
            return Response.ok(registrationService.deleteByFacilityAndDate
                    (facilityId, LocalDate.parse(registrationDate))).build();
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

    @GET
    @Path("/{facilityId}")
    @Operation(description = "Find a facility by ID.")
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
            @Parameter(description = "ID of the facility to find.")
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
                            implementation = Facility.class,
                            type = SchemaType.ARRAY)
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
    @Operation(description = "Find all facilities matching name segment, " +
            "ordered by name.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Facility.class,
                            type = SchemaType.ARRAY)
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
    @Operation(description = "Find the facility matching an exact name.")
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
    public Response findByNameExact(
            @Parameter(description = "Name matching the facility to find.")
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

    @GET()
    @Path("/{facilityId}/guests")
    @Operation(description = "Find guests for this facility " +
            "ordered by lastName, firstName.")
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
    public Response findGuestsByFacilityId(
            @Parameter(description = "Facility ID for which to find guests.")
            @PathParam("facilityId") Long facilityId
    ) {
        try {
            return Response.ok(guestService.findByFacilityId(facilityId))
                    .build();
        } catch (InternalServerError e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET()
    @Path("/{facilityId}/guests/name/{name}")
    @Operation(description = "Find guests for this facility " +
            "matching name segment, " +
            "ordered by lastName, firstName.")
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
    public Response findGuestsByName(
            @Parameter(description = "Facility ID for which to find guests.")
            @PathParam("facilityId") Long facilityId,
            @Parameter(description = "Name segment match for guests to find.")
            @PathParam("name") String name
    ) {
        try {
            return Response.ok(guestService.findByName
                    (facilityId, name)).build();
        } catch (InternalServerError e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET()
    @Path("/{facilityId}/guests/nameExact/{firstName}/{lastName}")
    @Operation(description = "Find the guest for this facility " +
            "matching firstName and lastName exactly.")
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
    public Response findGuestsByNameExact(
            @Parameter(description = "Facility ID for which to find the guest.")
            @PathParam("facilityId") Long facilityId,
            @Parameter(description = "First name of the guest to find.")
            @PathParam("firstName") String firstName,
            @Parameter(description = "Last name of the guest to find.")
            @PathParam("lastName") String lastName
    ) {
        try {
            return Response.ok(guestService.findByNameExact
                    (facilityId, firstName, lastName)).build();
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
    @Path("/{facilityId}/registrations/{registrationDate}")
    @Operation(description = "Find registrations for a facility and " +
            "specific registration date, ordered by matNumber.")
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
    public Response findRegistrationsByFacilityAndDate(
            @Parameter(description = "Facility ID for which to find " +
                    "registrations.")
            @PathParam("facilityId") Long facilityId,
            @Parameter(description = "Registration date for which to " +
                    "find registrations.")
            @PathParam("registrationDate") String registrationDate
    ) {
        try {
            return Response.ok(registrationService.findByFacilityAndDate
                    (facilityId, LocalDate.parse(registrationDate))).build();
        } catch (InternalServerError e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @POST
    @Path("/{facilityId}/registrations/{registrationDate}")
    @Operation(description = "Import registration information by " +
            "facility and registration date.  Guests " +
            "will be created as necessary.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = ImportResults.class)
                    ),
                    description = "Result object containing the list " +
                            "of inserted registrations " +
                            "and the list of any associated problems.",
                    responseCode = "201"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Bad request message.",
                    responseCode = "400"
            ),
            @APIResponse(
                    content = @Content(mediaType = MediaType.TEXT_PLAIN),
                    description = "Missing facility or guest message.",
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
    public Response importRegistrationsByFacilityAndDate(
            @Parameter(description = "Facility ID for which to import " +
                    "registrations.")
            @PathParam("facilityId") Long facilityId,
            @Parameter(description = "Registration date for which to " +
                    "import registrations.")
            @PathParam("registrationDate") String registrationDate,
            @Parameter(description = "List of imports to process.")
            @Parameter List<ImportRequest> importRequests
    ) {
        try {
            ImportResults importResults =
                    registrationService.importByFacilityAndDate(
                            facilityId,
                            LocalDate.parse(registrationDate),
                            importRequests
                    );
            URI uri = UriBuilder.fromResource(FacilityEndpoints.class)
                    .path(facilityId.toString())
                    .path("/registrations")
                    .path(registrationDate)
                    .build();
            return Response.created(uri)
                    .entity(importResults)
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

    @GET()
    @Path("/{facilityId}/templates")
    @Operation(description = "Find templates for this facility, " +
            "ordered by name.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Template.class,
                            type = SchemaType.ARRAY)
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
    public Response findTemplatesByFacilityId(
            @Parameter(description =
                    "Facility ID for which to find templates.")
            @PathParam("facilityId") Long facilityId
    ) {
        try {
            return Response.ok(templateService.findByFacilityId(facilityId))
                    .build();
        } catch (InternalServerError e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET()
    @Path("/{facilityId}/templates/name/{name}")
    @Operation(description = "Find templates for this facility " +
            "matching name segment, " +
            "ordered by name.")
    @APIResponses(value = {
            @APIResponse(
                    content = @Content(schema = @Schema(
                            implementation = Template.class,
                            type = SchemaType.ARRAY)
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
    public Response findTemplatesByName(
            @Parameter(description =
                    "Facility ID for which to find templates.")
            @PathParam("facilityId") Long facilityId,
            @Parameter(description =
                    "Name segment match for templates to find.")
            @PathParam("name") String name
    ) {
        try {
            return Response.ok(templateService.findByName
                    (facilityId, name)).build();
        } catch (InternalServerError e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }
    }

    @GET()
    @Path("/{facilityId}/templates/nameExact/{name}")
    @Operation(description = "Find the template for this facility " +
            "matching the specified name exactly.")
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
    public Response findTemplatesByNameExact(
            @Parameter(description =
                    "Facility ID for which to find template.")
            @PathParam("facilityId") Long facilityId,
            @Parameter(description = "Name of template to find.")
            @PathParam("name") String name
    ) {
        try {
            return Response.ok(templateService.findByNameExact
                    (facilityId, name)).build();
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
