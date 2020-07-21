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

    String COMMENTS_COLUMN = "comments";

    String EMAIL_COLUMN = "email";

    String FACILITY_COLUMN = "facility";

    String FACILITY_ID_COLUMN = "facilityId";
    String FACILITY_ID_VALIDATION_MESSAGE =
            "facilityId: Required and must identify a valid facility";

    String FEATURES_COLUMN = "features";

    String FIRST_NAME_COLUMN = "firstName";
    String FIRST_NAME_VALIDATION_MESSAGE =
            "firstName: Required and must not be blank";

    String GUEST_COLUMN = "guest";

    String GUEST_ID_COLUMN = "guestId";
    String GUEST_ID_VALIDATION_MESSAGE =
            "guestId: If present must identify a valid guest for this facility";

    String GUESTS_COLUMN = "guests";

    String LAST_NAME_COLUMN = "lastName";
    String LAST_NAME_VALIDATION_MESSAGE =
            "lastName: Required and must not be blank";

    String MAT_NUMBER_COLUMN = "matNumber";
    String MAT_NUMBER_VALIDATION_MESSAGE =
            "matNumber: Required and must not be blank";

    String NAME_COLUMN = "name";
    String NAME_VALIDATION_MESSAGE =
            "name: Required and must not be blank";

    String PAYMENT_AMOUNT_COLUMN = "paymentAmount";

    String PAYMENT_TYPE_COLUMN = "paymentType";

    String PHONE_COLUMN = "phone";

    String REGISTRATION_DATE_COLUMN = "registrationDate";
    String REGISTRATION_DATE_VALIDATION_MESSAGE =
            "registrationDate: Cannot be null";

    String REGISTRATION_ID_COLUMN = "registrationId";

    String REGISTRATIONS_COLUMN = "registrations";

    String SHOWER_TIME_COLUMN = "showerTime";

    String WAKEUP_TIME_COLUMN = "wakeupTime";

    String STATE_COLUMN = "state";

    String TEMPLATE_ID_COLUMN = "templateId";

    String ZIPCODE_COLUMN = "zipCode";

    // Per-Table Constants ---------------------------------------------------

    String ASSIGN_NAME = "Assign"; // Not really a table, but documented like one

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
