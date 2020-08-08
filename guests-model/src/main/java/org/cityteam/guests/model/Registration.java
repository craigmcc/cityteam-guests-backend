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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cityteam.guests.action.Assign;
import org.cityteam.guests.model.types.FeatureType;
import org.cityteam.guests.model.types.PaymentType;
import org.craigmcc.library.model.Model;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.cityteam.guests.model.Constants.FACILITY_ID_COLUMN;
import static org.cityteam.guests.model.Constants.GUEST_ID_COLUMN;
import static org.cityteam.guests.model.Constants.MAT_NUMBER_COLUMN;
import static org.cityteam.guests.model.Constants.REGISTRATION_DATE_COLUMN;
import static org.cityteam.guests.model.Constants.REGISTRATION_NAME;
import static org.cityteam.guests.model.Constants.REGISTRATION_TABLE;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

// Persistence Configuration -------------------------------------------------

@Entity(name = REGISTRATION_NAME)
@Table(
        indexes = {
                @Index(
                        columnList = FACILITY_ID_COLUMN + " ASC, " +
                                     REGISTRATION_DATE_COLUMN + " ASC, " +
                                     MAT_NUMBER_COLUMN + " ASC",
                        name = "IX_" + REGISTRATION_TABLE + "_" +
                               REGISTRATION_DATE_COLUMN + "_" +
                               MAT_NUMBER_COLUMN,
                        unique = true
                )
        },
        name = REGISTRATION_TABLE
)
@Access(AccessType.FIELD)

@NamedQueries({
        @NamedQuery(
                name = REGISTRATION_NAME + ".deleteByFacilityAndDate",
                query = "DELETE FROM " + REGISTRATION_NAME + " r " +
                        "WHERE r." + FACILITY_ID_COLUMN + " = :" + FACILITY_ID_COLUMN +
                        " AND r." + REGISTRATION_DATE_COLUMN + " = :" + REGISTRATION_DATE_COLUMN
        ),
        @NamedQuery(
                name = REGISTRATION_NAME + ".findAll",
                query = "SELECT r FROM " + REGISTRATION_NAME + " r " +
                        "ORDER BY r." + FACILITY_ID_COLUMN +
                               ", r." + REGISTRATION_DATE_COLUMN +
                               ", r." + MAT_NUMBER_COLUMN
        ),
        @NamedQuery(
                name = REGISTRATION_NAME + ".findByFacilityAndDate",
                query = "SELECT r FROM " + REGISTRATION_NAME + " r " +
                        "WHERE r." + FACILITY_ID_COLUMN + " = :" + FACILITY_ID_COLUMN +
                         " AND r." + REGISTRATION_DATE_COLUMN + " = :" + REGISTRATION_DATE_COLUMN +
                        " ORDER BY r." + FACILITY_ID_COLUMN +
                        ", r." + REGISTRATION_DATE_COLUMN +
                        ", r." + MAT_NUMBER_COLUMN
        ),
        @NamedQuery(
                name = REGISTRATION_NAME + ".findByFacilityAndDateAndMat",
                query = "SELECT r FROM " + REGISTRATION_NAME + " r " +
                        "WHERE r." + FACILITY_ID_COLUMN + " = :" + FACILITY_ID_COLUMN +
                        " AND r." + REGISTRATION_DATE_COLUMN + " = :" + REGISTRATION_DATE_COLUMN +
                        " AND r." + MAT_NUMBER_COLUMN + " = :" + MAT_NUMBER_COLUMN +
                        " ORDER BY r." + FACILITY_ID_COLUMN +
                        ", r." + REGISTRATION_DATE_COLUMN +
                        ", r." + MAT_NUMBER_COLUMN
        ),
        @NamedQuery(
                name = REGISTRATION_NAME + ".findByGuestId",
                query = "SELECT r FROM " + REGISTRATION_NAME + " r " +
                        "WHERE r." + GUEST_ID_COLUMN + " =:" + GUEST_ID_COLUMN +
                        " ORDER BY r." + REGISTRATION_DATE_COLUMN
        ),
        @NamedQuery(
                name = REGISTRATION_NAME + ".findById",
                query = "SELECT r FROM " + REGISTRATION_NAME + " r " +
                        "WHERE r." + ID_COLUMN + " = :" + ID_COLUMN
        )
})

// API Documentation ---------------------------------------------------------

@Schema(
        description = "Availability of an overnight registration of a " +
                "particular matNumber, on a particular registrationDate, " +
                "within a particular facility.",
        name = REGISTRATION_NAME
)

public class Registration extends Model<Registration> implements Constants {

    // Instance Variables ----------------------------------------------------

    @Column(
            name = COMMENTS_COLUMN,
            nullable = true
    )
    @Schema(description = "Optional comments about this registration.")
    private String comments;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            foreignKey = @ForeignKey(
                    name = "fk_" + REGISTRATION_TABLE + "_" + FACILITY_TABLE,
                    value = ConstraintMode.CONSTRAINT
            ),
            insertable = false,
            name = FACILITY_ID_COLUMN,
            referencedColumnName = ID_COLUMN,
            updatable = false
    )
    @Schema(hidden = true)
    private Facility facility;

    @Column(
            name = FACILITY_ID_COLUMN,
            nullable = false
    )
    @NotNull(message = FACILITY_ID_VALIDATION_MESSAGE)
    @Schema(description = "ID of the facility to which this " +
                "registration belongs.")
    private Long facilityId;

    @Column(
            name = FEATURES_COLUMN,
            nullable = true
    )
    @Schema(description = "Feature identifiers for this matNumber.")
    // JPA does not know how to handle List<FeatureType> well, so fake
    // it in the getter and setter methods
    private String features;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = true
    )
    @JoinColumn(
            foreignKey = @ForeignKey(
                    name = "fk_" + REGISTRATION_TABLE + "_" + GUEST_TABLE,
                    value = ConstraintMode.CONSTRAINT
            ),
            insertable = false,
            name = GUEST_ID_COLUMN,
            nullable = true,
            referencedColumnName = ID_COLUMN,
            updatable = false
    )
    @Schema(hidden = true)
    private Guest guest;

    @Column(
            name = GUEST_ID_COLUMN,
            nullable = true
    )
    @Schema(description = "ID of the guest assigned in this registration " +
            "(if this mat has been assigned).")
    private Long guestId;

    @Column(
            name = MAT_NUMBER_COLUMN,
            nullable = false
    )
    @Schema(
            description = "Unique (per registrationDate) mat number " +
                          "for this registration.",
            required = true
    )
    private Integer matNumber;

    @Column(
            name = PAYMENT_AMOUNT_COLUMN,
            nullable = true
    )
    @Schema(description = "Payment amount for this registration.  Only " +
                          "required for payment type $$ (cash).")
    private BigDecimal paymentAmount;

    @Column(
            name = PAYMENT_TYPE_COLUMN,
            nullable = true
    )
    @Enumerated(EnumType.STRING)
    @Schema(description = "Type of payment for this registration. " +
                "This is required when a registration is assigned.")
    private PaymentType paymentType;

    @Column(
            name = REGISTRATION_DATE_COLUMN,
            nullable = false
    )
    @NotNull(message = REGISTRATION_DATE_VALIDATION_MESSAGE)
    @Schema(
            description = "Date on which this mat may be registered " +
                "to a particular guest.  The combination of facilityId, " +
                "registrationDate, and matNumber must be unique.",
            required = true)
    private LocalDate registrationDate;

    @Column(
            name = SHOWER_TIME_COLUMN,
            nullable = true
    )
    @Schema(description = "Time this guest wishes to be awoken " +
                          "for a shower.")
    private LocalTime showerTime;

    @Column(
            name = WAKEUP_TIME_COLUMN,
            nullable = true
    )
    @Schema(description = "Time this guest wishes to be awoken.")
    private LocalTime wakeupTime;

    // Constructors ----------------------------------------------------------

    // Completely empty
    public Registration() { }

    // Unassigned
    public Registration(
            Long facilityId,
            List<FeatureType> features,
            Integer matNumber,
            LocalDate registrationDate
    ) {
        this.facilityId = facilityId;
        setFeatures(features);
        this.matNumber = matNumber;
        this.registrationDate = registrationDate;
    }

    // Assigned
    public Registration(
            String comments,
            Long facilityId,
            List<FeatureType> features,
            Long guestId,
            Integer matNumber,
            BigDecimal paymentAmount,
            PaymentType paymentType,
            LocalDate registrationDate,
            LocalTime showerTime,
            LocalTime wakeupTime
    ) {
        this.comments = comments;
        this.facilityId = facilityId;
        setFeatures(features);
        this.guestId = guestId;
        this.matNumber = matNumber;
        this.paymentAmount = paymentAmount;
        this.paymentType = paymentType;
        this.registrationDate = registrationDate;
        this.showerTime = showerTime;
        this.wakeupTime = wakeupTime;
    }

    // Property Methods ------------------------------------------------------

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

/*
    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }
*/

    public Long getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Long facilityId) {
        this.facilityId = facilityId;
    }

    public List<FeatureType> getFeatures() {
        if ((this.features == null) || (this.features.length() == 0)) {
            return null;
        }
        List<FeatureType> featureTypes = new ArrayList<>();
        for (char c : this.features.toUpperCase().toCharArray()) {
            featureTypes.add(FeatureType.valueOf("" + c));
        }
        return featureTypes;
    }

    public void setFeatures(List<FeatureType> features) {
        if ((features == null) || (features.size() == 0)) {
            this.features = null;
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (FeatureType feature : features) {
            sb.append(feature.name());
        }
        this.features = sb.toString();
    }


/*
    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }
*/

    public Long getGuestId() {
        return guestId;
    }

    public void setGuestId(Long guestId) {
        this.guestId = guestId;
    }

    public Integer getMatNumber() {
        return matNumber;
    }

    public void setMatNumber(Integer matNumber) {
        this.matNumber = matNumber;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalTime getShowerTime() {
        return showerTime;
    }

    public void setShowerTime(LocalTime showerTime) {
        this.showerTime = showerTime;
    }

    public LocalTime getWakeupTime() {
        return wakeupTime;
    }

    public void setWakeupTime(LocalTime wakeupTime) {
        this.wakeupTime = wakeupTime;
    }

    // Public Methods --------------------------------------------------------

    // Copy just the fields for an assignment
    public void copy(Assign that) {
        this.comments = that.getComments();
        this.guestId = that.getGuestId();
        this.paymentAmount = that.getPaymentAmount();
        this.paymentType = that.getPaymentType();
        this.showerTime = that.getShowerTime();
        this.wakeupTime = that.getWakeupTime();
    }

    @Override
    public void copy(Registration that) {
        this.comments = that.comments;
        this.facilityId = that.facilityId;
        this.features = that.features;
        this.guestId = that.guestId;
        this.matNumber = that.matNumber;
        this.paymentAmount = that.paymentAmount;
        this.paymentType = that.paymentType;
        this.registrationDate = that.registrationDate;
        this.showerTime = that.showerTime;
        this.wakeupTime = that.wakeupTime;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Registration)) {
            return false;
        }
        Registration that = (Registration) object;
        return new EqualsBuilder()
                .appendSuper(super.equals(that))
                .append(this.comments, that.comments)
                .append(this.facilityId, that.facilityId)
                .append(this.features, that.features)
                .append(this.guestId, that.guestId)
                .append(this.matNumber, that.matNumber)
                .append(this.paymentAmount, that.paymentAmount)
                .append(this.paymentType, that.paymentType)
                .append(this.registrationDate, that.registrationDate)
                .append(this.showerTime, that.showerTime)
                .append(this.wakeupTime, that.wakeupTime)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.comments)
                .append(this.facilityId)
                .append(this.features)
                .append(this.guestId)
                .append(this.matNumber)
                .append(this.paymentAmount)
                .append(this.paymentType)
                .append(this.registrationDate)
                .append(this.showerTime)
                .append(this.wakeupTime)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(COMMENTS_COLUMN, this.comments)
                .append(FACILITY_ID_COLUMN, this.facilityId)
                .append(FEATURES_COLUMN, this.features)
                .append(GUEST_ID_COLUMN, this.guestId)
                .append(MAT_NUMBER_COLUMN, this.matNumber)
                .append(PAYMENT_AMOUNT_COLUMN, this.paymentAmount)
                .append(PAYMENT_TYPE_COLUMN, this.paymentType)
                .append(REGISTRATION_DATE_COLUMN, this.registrationDate)
                .append(SHOWER_TIME_COLUMN, this.showerTime)
                .append(WAKEUP_TIME_COLUMN, this.wakeupTime)
                .toString();
    }

}
