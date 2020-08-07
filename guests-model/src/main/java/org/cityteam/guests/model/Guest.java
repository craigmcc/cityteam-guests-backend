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
import javax.persistence.CascadeType;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;

import static org.cityteam.guests.model.Constants.FACILITY_ID_COLUMN;
import static org.cityteam.guests.model.Constants.FIRST_NAME_COLUMN;
import static org.cityteam.guests.model.Constants.GUEST_NAME;
import static org.cityteam.guests.model.Constants.GUEST_TABLE;
import static org.cityteam.guests.model.Constants.LAST_NAME_COLUMN;
import static org.cityteam.guests.model.Constants.NAME_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

// Persistence Configuration -------------------------------------------------

@Entity(name = GUEST_NAME)
@Table(
        indexes = {
                @Index(
                        columnList = FACILITY_ID_COLUMN + " ASC, " +
                                     LAST_NAME_COLUMN + " ASC, " +
                                     FIRST_NAME_COLUMN + " ASC",
                        name = "IX_" + GUEST_TABLE + "_" + LAST_NAME_COLUMN +
                               "_" + FIRST_NAME_COLUMN,
                        unique = true
                )
        },
        name = GUEST_TABLE
)
@Access(AccessType.FIELD)

@NamedQueries({
        @NamedQuery(
                name = GUEST_NAME + ".findAll",
                query = "SELECT g FROM " + GUEST_NAME + " g " +
                        "ORDER BY g." + FACILITY_ID_COLUMN + ", g." +
                        LAST_NAME_COLUMN + ", g." + FIRST_NAME_COLUMN
        ),
        @NamedQuery(
                name = GUEST_NAME + ".findByFacilityId",
                query = "SELECT g FROM " + GUEST_NAME + " g " +
                        "WHERE g." + FACILITY_ID_COLUMN + " = :" +
                        FACILITY_ID_COLUMN + " " +
                        "ORDER BY g." + FACILITY_ID_COLUMN + ", g." +
                        LAST_NAME_COLUMN + ", g." + FIRST_NAME_COLUMN
        ),
        @NamedQuery(
                name = GUEST_NAME + ".findById",
                query = "SELECT g FROM " + GUEST_NAME + " g " +
                        "WHERE g." + ID_COLUMN + " = :" + ID_COLUMN
        ),
        @NamedQuery(
                name = GUEST_NAME + ".findByName",
                query = "SELECT g FROM " + GUEST_NAME + " g " +
                        "WHERE g." + FACILITY_ID_COLUMN + " = :" +
                        FACILITY_ID_COLUMN + " AND (" +
                        "(g." + FIRST_NAME_COLUMN +
                        " LIKE LOWER(CONCAT('%',:" +
                        NAME_COLUMN + ",'%'))) OR " +
                        "(g." + LAST_NAME_COLUMN +
                        " LIKE LOWER(CONCAT('%',:" +
                        NAME_COLUMN + ",'%')))) " +
                        "ORDER BY g." + FACILITY_ID_COLUMN + ", g." +
                        LAST_NAME_COLUMN + ", g." + FIRST_NAME_COLUMN
        ),
        @NamedQuery(
                name = GUEST_NAME + ".findByNameExact",
                query = "SELECT g FROM " + GUEST_NAME + " g " +
                        "WHERE g." + FACILITY_ID_COLUMN + " =:" +
                        FACILITY_ID_COLUMN + " AND " +
                        "g." + LAST_NAME_COLUMN + " = :" + LAST_NAME_COLUMN +
                        " AND g." + FIRST_NAME_COLUMN + " = :" +
                        FIRST_NAME_COLUMN
        )
})

// API Documentation ---------------------------------------------------------

@Schema(
        description = "An individual overnight guest at a particular facility.",
        name = GUEST_NAME
)

public class Guest extends Model<Guest> implements Constants {

    // Instance Variables ----------------------------------------------------

    @OneToMany(
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY,
            mappedBy = GUEST_ID_COLUMN,
            orphanRemoval = true
    )
    @Schema(hidden = true)
    private List<Ban> bans;

    @Column(
            name = COMMENTS_COLUMN,
            nullable = true
    )
    @Schema(description = "Optional comments about this guest.")
    private String comments;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            foreignKey = @ForeignKey(
                    name = "fk_" + GUEST_TABLE + "_" + FACILITY_TABLE,
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
    @Schema(
            description = "ID of the facility to which this guest belongs.",
            required = true
    )
    private Long facilityId;

    @Column(
            name = FIRST_NAME_COLUMN,
            nullable = false
    )
    @NotEmpty(message = FIRST_NAME_VALIDATION_MESSAGE)
    @Schema(
            description = "First name of this guest. " +
                "First name and last name must be unique within a facility.",
            required = true
    )
    private String firstName;

    @Column(
            name = LAST_NAME_COLUMN,
            nullable = false
    )
    @NotEmpty(message = LAST_NAME_VALIDATION_MESSAGE)
    @Schema(
            description = "Last name of this guest. " +
                "First name and last name must be unique within a facility.",
            required = true
    )
    private String lastName;

    @OneToMany(
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY,
            mappedBy = GUEST_ID_COLUMN,
            orphanRemoval = true

    )
    @Schema(hidden = true)
    private List<Registration> registrations;

    // Static Classes --------------------------------------------------------

    public static final Comparator<Guest> NameComparator =
            (g1, g2) -> {
                int lastNameComparision =
                        g1.getLastName().compareTo(g2.getLastName());
                if (lastNameComparision != 0) {
                    return lastNameComparision;
                } else {
                    return
                        g1.getFirstName().compareTo(g2.getFirstName());
                }
            };

    // Constructors ----------------------------------------------------------

    public Guest() { }

    public Guest(
            String comments,
            Long facilityId,
            String firstName,
            String lastName
    ) {
        this.comments = comments;
        this.facilityId = facilityId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Property Methods ------------------------------------------------------

/*
    public List<Ban> getBans() {
        return bans;
    }

    public void setBans(List<Ban> bans) {
        this.bans = bans;
    }
*/

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

/*
    public List<Registration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<Registration> registrations) {
        this.registrations = registrations;
    }
*/

// Public Methods --------------------------------------------------------

    @Override
    public void copy(Guest that) {
        this.comments = that.comments;
        this.facilityId = that.facilityId;
        this.firstName = that.firstName;
        this.lastName = that.lastName;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Guest)) {
            return false;
        }
        Guest that = (Guest) object;
        return new EqualsBuilder()
                .appendSuper(super.equals(that))
                .append(this.comments, that.comments)
                .append(this.facilityId, that.facilityId)
                .append(this.firstName, that.firstName)
                .append(this.lastName, that.lastName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.comments)
                .append(this.facilityId)
                .append(this.firstName)
                .append(this.lastName)
                .toHashCode();
    }

    /**
     * <p>If <code>names</code> has a space, take the prefix part as the
     * <code>lastName</code> matcher and the suffix part as the <code>firstName</code>
     * matcher.  Otherwise, use the entire string as the matcher for both with an
     * <strong>OR</strong> condition.</p>
     *
     * @param name Matching pattern, optionally with a space to separate last from first
     */
    public boolean matchNames(@NotBlank String name) {
        if ((name == null) || (name.isBlank())) {
            return false;
        }
        String firstName = name.trim();
        String lastName = name.trim();
        int index = name.indexOf(" ");
        if ((index > 0) && (index < name.length() - 1)) {
            firstName = name.substring(0, index).trim();
            lastName = name.substring(index + 1).trim();
        }
        if ((this.firstName.toLowerCase().contains(firstName.toLowerCase())) ||
                (this.lastName.toLowerCase().contains(lastName.toLowerCase()))) {
            return true;
        } else {
            return false;
        }
    }


    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(COMMENTS_COLUMN, this.comments)
                .append(FACILITY_ID_COLUMN, this.facilityId)
                .append(FIRST_NAME_COLUMN, this.firstName)
                .append(LAST_NAME_COLUMN, this.lastName)
                .toString();
    }

}
