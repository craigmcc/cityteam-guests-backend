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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.Comparator;
import java.util.List;

import static org.cityteam.guests.model.Constants.FACILITY_NAME;
import static org.cityteam.guests.model.Constants.FACILITY_TABLE;
import static org.cityteam.guests.model.Constants.NAME_COLUMN;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

// Persistence Configuration -------------------------------------------------

@Entity(name = FACILITY_NAME)
@Table(
        indexes = {
                @Index(
                        columnList = NAME_COLUMN + " ASC",
                        name = "IX_" + FACILITY_TABLE + "_" + NAME_COLUMN,
                        unique = true
                )
        },
        name = FACILITY_TABLE
)
@Access(AccessType.FIELD)

// Named Queries -------------------------------------------------------------

@NamedQueries({
        @NamedQuery(
                name = FACILITY_NAME + ".findAll",
                query = "SELECT f FROM " + FACILITY_NAME + " f " +
                        "ORDER BY f." + NAME_COLUMN + " ASC"
        ),
        @NamedQuery(
                name = FACILITY_NAME + ".findById",
                query = "SELECT f FROM " + FACILITY_NAME + " f " +
                        "WHERE f." + ID_COLUMN + " = :" + ID_COLUMN
        ),
        @NamedQuery(
                name = FACILITY_NAME + ".findByName",
                query = "SELECT f FROM " + FACILITY_NAME + " f " +
                        "WHERE LOWER(f." + NAME_COLUMN +
                        ") LIKE LOWER(CONCAT('%',:" + NAME_COLUMN + ",'%')) " +
                        "ORDER BY f." + NAME_COLUMN + " ASC"
        ),
        @NamedQuery(
                name = FACILITY_NAME + ".findByNameExact",
                query = "SELECT f FROM " + FACILITY_NAME + " f " +
                        "WHERE f." + NAME_COLUMN + " = :" + NAME_COLUMN
        )
})

// API Documentation ---------------------------------------------------------

@Schema(
        description = "An individual CityTeam facility that schedules " +
                "its own overnight guests.",
        name = FACILITY_NAME
)

public class Facility extends Model<Facility> implements Constants {

    // Instance Variables ----------------------------------------------------

    @Column(
            name = ADDRESS1_COLUMN,
            nullable = true
    )
    @Schema(description = "First line of the address for this facility.")
    private String address1;

    @Column(
            name = ADDRESS2_COLUMN,
            nullable = true
    )
    @Schema(description = "Second line of the address for this facility.")
    private String address2;

    @Column(
            name = CITY_COLUMN,
            nullable = true
    )
    @Schema(description = "City name of the address for this facility.")
    private String city;

    @Column(
            name = EMAIL_COLUMN,
            nullable = true
    )
    @Schema(description = "Email address of this facility.")
    private String email;

    @OneToMany(
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY,
            mappedBy = FACILITY_ID_COLUMN,
            orphanRemoval = true
    )
    @OrderBy(FACILITY_ID_COLUMN + ", " + LAST_NAME_COLUMN +
            ", " + FIRST_NAME_COLUMN)
    @Schema(hidden = true)
    private List<Guest> guests;

    @Column(
            name = NAME_COLUMN,
            nullable = false
    )
    @Schema(
            description = "Unique name of this facility.",
            required = true
    )
    private String name;

    @Column(
            name = PHONE_COLUMN,
            nullable = true
    )
    @Schema(description = "Phone number of this facility.")
    private String phone;

    @OneToMany(
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY,
            mappedBy = FACILITY_ID_COLUMN,
            orphanRemoval = true

    )
    @Schema(hidden = true)
    private List<Registration> registrations;

    @Column(
            name = STATE_COLUMN,
            nullable = true
    )
    @Schema(description = "State abbreviation of the address for this facility.")
    private String state;

    @OneToMany(
            cascade = CascadeType.REMOVE,
            fetch = FetchType.LAZY,
            mappedBy = FACILITY_ID_COLUMN,
            orphanRemoval = true

    )
    @Schema(hidden = true)
    private List<Template> templates;

    @Column(
            name = ZIPCODE_COLUMN,
            nullable = true
    )
    @Schema(description = "Zip code of the address for this facility.")
    private String zipCode;

    // Static Classes ------------------------------------------------------

    public static final Comparator<Facility> NameComparator =
            (f1, f2) -> {
                return f1.getName().compareTo(f2.getName());
            };

    // Constructors ----------------------------------------------------------

    public Facility() { }

    public Facility(
            String address1,
            String address2,
            String city,
            String email,
            String name,
            String phone,
            String state,
            String zipCode
    ) {
        this.address1 = address1;
        this.address2 = address2;
        this.city = city;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.state = state;
        this.zipCode = zipCode;
    }

    // Property Methods ------------------------------------------------------

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

/*
    public List<Guest> getGuests() {
        return guests;
    }

    public void setGuests(List<Guest> guests) {
        this.guests = guests;
    }
*/

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

/*
    public List<Registration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<Registration> registrations) {
        this.registrations = registrations;
    }
*/

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

/*
    public List<Template> getTemplates() {
        return templates;
    }

    public void setTemplates(List<Template> templates) {
        this.templates = templates;
    }
*/

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    // Public Methods --------------------------------------------------------

    @Override
    public void copy(Facility that) {
        this.address1 = that.address1;
        this.address2 = that.address2;
        this.city = that.city;
        this.email = that.email;
        this.name = that.name;
        this.phone = that.phone;
        this.state = that.state;
        this.zipCode = that.zipCode;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Facility)) {
            return false;
        }
        Facility that = (Facility) object;
        return new EqualsBuilder()
                .appendSuper(super.equals(that))
                .append(this.address1, that.address1)
                .append(this.address2, that.address2)
                .append(this.city, that.city)
                .append(this.email, that.email)
                .append(this.name, that.name)
                .append(this.phone, that.phone)
                .append(this.state, that.state)
                .append(this.zipCode, that.zipCode)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.address1)
                .append(this.address2)
                .append(this.city)
                .append(this.email)
                .append(this.name)
                .append(this.phone)
                .append(this.state)
                .append(this.zipCode)
                .toHashCode();
    }

    public boolean matchName(String name) {
        if ((name == null) || name.isBlank()) {
            return false;
        }
        if (!this.getName().toLowerCase().contains
                (name.trim().toLowerCase())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(ADDRESS1_COLUMN, this.address1)
                .append(ADDRESS2_COLUMN, this.address2)
                .append(CITY_COLUMN, this.city)
                .append(EMAIL_COLUMN, this.email)
                .append(NAME_COLUMN, this.name)
                .append(PHONE_COLUMN, this.phone)
                .append(STATE_COLUMN, this.state)
                .append(ZIPCODE_COLUMN, this.zipCode)
                .toString();
    }

}
