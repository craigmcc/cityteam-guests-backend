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
package org.cityteam.guests.action;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cityteam.guests.model.Constants;
import org.cityteam.guests.model.types.FeatureType;
import org.cityteam.guests.model.types.PaymentType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.cityteam.guests.model.Constants.IMPORT_NAME;

// API Documentation ---------------------------------------------------------

@Schema(
        description = "Properties passed to import historical registration " +
                      "information.  When processed, a Guest will be created " +
                      "automatically (if not already present).  Note that " +
                      "facilityId and registrationDate will be specified by " +
                      "path parameters on the import request, so are not " +
                      "included here.",
        name = IMPORT_NAME
)

public class ImportRequest implements Constants {

    // Instance Variables ----------------------------------------------------

    @Schema(description = "Comments about this registration (if assigned).")
    private String comments;

    @Schema(description = "Feature identifiers for this matNumber.")
    private List<FeatureType> features;

    @Schema(description = "First name of the assigned guest (if any).")
    private String firstName;

    @Schema (description = "Last name of the assigned guest (if any).")
    private String lastName;

    @Schema(description = "Unique (per registrationDate) mat number " +
                          "for this registration.")
    private Integer matNumber;

    @Schema(description = "Payment amount (if guest is assigned).  Only " +
                          "required for payment type $$ (cash).")
    private BigDecimal paymentAmount;

    @Schema(description = "Payment type (if guest is assigned).")
    private PaymentType paymentType;

    @Schema(description = "Requested shower time (if guest is assigned).")
    private LocalTime showerTime;

    @Schema(description = "Requested wakeup time (if guest is assigned)")
    private LocalTime wakeupTime;

    // Constructors ----------------------------------------------------------

    public ImportRequest() { }

    // Unassigned
    public ImportRequest(
            List<FeatureType> features,
            Integer matNumber
    ) {
        this.features = features;
        this.matNumber = matNumber;
    }

    // Assigned
    public ImportRequest(
            String comments,
            List<FeatureType> features,
            String firstName,
            String lastName,
            Integer matNumber,
            BigDecimal paymentAmount,
            PaymentType paymentType,
            LocalTime showerTime,
            LocalTime wakeupTime
    ) {
        this.comments = comments;
        this.features = features;
        this.firstName = firstName;
        this.lastName = lastName;
        this.matNumber = matNumber;
        this.paymentAmount = paymentAmount;
        this.paymentType = paymentType;
        this.showerTime = showerTime;
        this.wakeupTime = wakeupTime;
    }

    // Property Methods ------------------------------------------------------

    public String getComments() {
        return comments;
    }

    public List<FeatureType> getFeatures() {
        return features;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getMatNumber() {
        return matNumber;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public LocalTime getShowerTime() {
        return showerTime;
    }

    public LocalTime getWakeupTime() {
        return wakeupTime;
    }

    // Public Methods --------------------------------------------------------

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(COMMENTS_COLUMN, this.comments)
                .append(FEATURES_COLUMN, this.features)
                .append(FIRST_NAME_COLUMN, this.firstName)
                .append(LAST_NAME_COLUMN, this.lastName)
                .append(MAT_NUMBER_COLUMN, this.matNumber)
                .append(PAYMENT_AMOUNT_COLUMN, this.paymentAmount)
                .append(PAYMENT_TYPE_COLUMN, this.paymentType)
                .append(SHOWER_TIME_COLUMN, this.showerTime)
                .append(WAKEUP_TIME_COLUMN, this.wakeupTime)
                .toString();
    }

}
