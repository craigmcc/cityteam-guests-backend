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
import org.craigmcc.library.model.Model;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import java.time.LocalDate;

import static org.cityteam.guests.model.Constants.BAN_FROM_COLUMN;
import static org.cityteam.guests.model.Constants.BAN_NAME;
import static org.cityteam.guests.model.Constants.BAN_TABLE;
import static org.cityteam.guests.model.Constants.BAN_TO_COLUMN;
import static org.cityteam.guests.model.Constants.GUEST_ID_COLUMN;
import static org.cityteam.guests.model.Constants.REGISTRATION_DATE_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

// Persistence Configuration -------------------------------------------------

@Entity(name = BAN_NAME)
@Table(
        indexes = {
                @Index(
                        columnList = GUEST_ID_COLUMN + " ASC, " +
                                BAN_FROM_COLUMN + " ASC",
                        name = "IX_" + BAN_TABLE + "_" +
                                GUEST_ID_COLUMN + "_" + BAN_FROM_COLUMN,
                        unique = true
                )
        },
        name = BAN_TABLE
)
@Access(AccessType.FIELD)

@NamedQueries({
        @NamedQuery(
                name = BAN_NAME + ".findAll",
                query = "SELECT b FROM " + BAN_NAME + " b " +
                "ORDER BY b." + GUEST_ID_COLUMN + ", b." + BAN_FROM_COLUMN
        ),
        @NamedQuery(
                name = BAN_NAME + ".findById",
                query = "SELECT b FROM " + BAN_NAME + " b " +
                        "WHERE b." + ID_COLUMN + " = :" + ID_COLUMN
        ),
        @NamedQuery(
                name = BAN_NAME + ".findByGuestId",
                query = "SELECT b FROM " + BAN_NAME + " b " +
                        "WHERE b." + GUEST_ID_COLUMN + " = :" +
                        GUEST_ID_COLUMN + " " +
                        "ORDER BY b." + BAN_FROM_COLUMN + " ASC"
        ),
        @NamedQuery(
                name = BAN_NAME + ".findByGuestIdAndRegistrationDate",
                query = "SELECT b FROM " + BAN_NAME + " b " +
                        "WHERE b." + BAN_FROM_COLUMN + " <= :" +
                        REGISTRATION_DATE_COLUMN + " AND b." +
                        BAN_TO_COLUMN + " >= :" +
                        REGISTRATION_DATE_COLUMN + " AND b." +
                        GUEST_ID_COLUMN + " = :" + GUEST_ID_COLUMN + " " +
                        "ORDER BY b." + BAN_FROM_COLUMN + " ASC"
        ),
        @NamedQuery(
                name = BAN_NAME + ".findByRegistrationDate",
                query = "SELECT b FROM " + BAN_NAME + " b " +
                        "WHERE b." + BAN_FROM_COLUMN + " <= :" +
                        REGISTRATION_DATE_COLUMN + " AND b." +
                        BAN_TO_COLUMN + " >= :" + REGISTRATION_DATE_COLUMN
        )
})

// API Documentation ---------------------------------------------------------

@Schema(
        description = "A date range for which a particular guest is banned. " +
            "Bans for a particular guest cannot have overlapping date ranges.",
        name = BAN_NAME
)

public class Ban extends Model<Ban> implements Constants{

    // Instance Variables ----------------------------------------------------

    @Column(
            name = ACTIVE_COLUMN,
            nullable = false
    )
    @Schema(
            description = "Flag indicating this ban is active and should " +
                "be enforced.",
            required = true
    )
    private Boolean active;

    @Column(
            name = BAN_FROM_COLUMN,
            nullable = false
    )
    @Schema(
            description = "First date (inclusive) of this ban.",
            required = true
    )
    private LocalDate banFrom;

    @Column(
            name = BAN_TO_COLUMN,
            nullable = false
    )
    @Schema(
            description = "Last date (inclusive) of this ban.  Must be " +
                "greater than or equal to banFrom date.",
            required = true
    )
    private LocalDate banTo;

    @Column(
            name = COMMENTS_COLUMN,
            nullable = true
    )
    @Schema(description = "Optional comments about this ban.")
    private String comments;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            foreignKey = @ForeignKey(
                    name = "fk_" + BAN_TABLE + "_" + GUEST_TABLE,
                    value = ConstraintMode.CONSTRAINT
            ),
            insertable = false,
            name = GUEST_ID_COLUMN,
            referencedColumnName = ID_COLUMN,
            updatable = false
    )
    @Schema(hidden = true)
    private Guest guest;

    @Column(
            name = GUEST_ID_COLUMN,
            nullable = false
    )
    @Schema(
            description = "ID of the guest this ban is associated with.",
            required = true
    )
    private Long guestId;

    @Column(
            name = STAFF_COLUMN,
            nullable = false
    )
    @Schema(description = "Name or description of the staff member " +
            "who initiated this ban.")
    private String staff;

    // Constructors ----------------------------------------------------------

    public Ban() { }

    public Ban(
            Boolean active,
            LocalDate banFrom,
            LocalDate banTo,
            String comments,
            Long guestId,
            String staff
    ) {
        this.active = active;
        this.banFrom = banFrom;
        this.banTo = banTo;
        this.comments = comments;
        this.guestId = guestId;
        this.staff = staff;
    }

    // Property Methods ------------------------------------------------------


    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDate getBanFrom() {
        return banFrom;
    }

    public void setBanFrom(LocalDate banFrom) {
        this.banFrom = banFrom;
    }

    public LocalDate getBanTo() {
        return banTo;
    }

    public void setBanTo(LocalDate banTo) {
        this.banTo = banTo;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
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

    public String getStaff() {
        return staff;
    }

    public void setStaff(String staff) {
        this.staff = staff;
    }

    // Public Methods --------------------------------------------------------

    @Override
    public void copy(Ban that) {
        this.active = that.active;
        this.banFrom = that.banFrom;
        this.banTo = that.banTo;
        this.comments = that.comments;
        this.guestId = that.guestId;
        this.staff = that.staff;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Ban)) {
            return false;
        }
        Ban that = (Ban) object;
        return new EqualsBuilder()
                .appendSuper(super.equals(that))
                .append(this.active, that.active)
                .append(this.banFrom, that.banFrom)
                .append(this.banTo, that.banTo)
                .append(this.comments, that.comments)
                .append(this.guestId, that.guestId)
                .append(this.staff, that.staff)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.active)
                .append(this.banFrom)
                .append(this.banTo)
                .append(this.comments)
                .append(this.guestId)
                .append(this.staff)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(ACTIVE_COLUMN, this.active)
                .append(BAN_FROM_COLUMN, this.banFrom)
                .append(BAN_TO_COLUMN, this.banTo)
                .append(COMMENTS_COLUMN, this.comments)
                .append(GUEST_ID_COLUMN, this.guestId)
                .append(STAFF_COLUMN, this.staff)
                .toString();
    }

}
