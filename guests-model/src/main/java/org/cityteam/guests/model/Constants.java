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
package org.cityteam.guests.model;

/**
 * <p>Manifest constants for columns and tables.</p>
 */
public interface Constants {

    // Per-Column Constants --------------------------------------------------

    String ADDRESS1_COLUMN = "address1";

    String ADDRESS2_COLUMN = "address2";

    String BAN_ID_COLUMN = "banId";

    String CITY_COLUMN = "city";

    String EMAIL_COLUMN = "email";

    String FACILITY_ID_COLUMN = "facilityId";

    String GUEST_ID_COLUMN = "guestId";

    String NAME_COLUMN = "name";
    String NAME_VALIDATION_MESSAGE =
            "name: Required and must not be blank";

    String PHONE_COLUMN = "phone";

    String REGISTRATION_ID_COLUMN = "registrationId";

    String STATE_COLUMN = "state";

    String TEMPLATE_ID_COLUMN = "templateId";

    String ZIPCODE_COLUMN = "zipCode";

    // Per-Table Constants ---------------------------------------------------

    String BAN_NAME = "Ban";
    String BAN_TABLE = "bans";

    String FACILITY_NAME = "Facility";
    String FACILITY_TABLE = "facilities";

    String GUEST_NAME = "Guest";
    String GUEST_TABLE = "guests";

    String REGISTRATION_NAME = "Registration";
    String REGISTRATION_TABLE = "registrations";

    String TEMPLATE_NAME = "Template";
    String TEMPLATE_TABLE = "templates";

}
