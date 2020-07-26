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
package org.cityteam.guests.model.types;

public enum PaymentType {

    $$("Paid Cash"),
    AG("Agency Voucher"),
    CT("CityTeam Decision"),
    FM("Free Mat"),
    MM("Medical Mat"),
    SW("Severe Weather"),
    UK("Unknown");

    private String description;

    PaymentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

}
