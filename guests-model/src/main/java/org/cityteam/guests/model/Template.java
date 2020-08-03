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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static org.cityteam.guests.model.Constants.FACILITY_ID_COLUMN;
import static org.cityteam.guests.model.Constants.NAME_COLUMN;
import static org.cityteam.guests.model.Constants.TEMPLATE_NAME;
import static org.cityteam.guests.model.Constants.TEMPLATE_TABLE;
import static org.craigmcc.library.model.Constants.ID_COLUMN;

// Persistence Configuration -------------------------------------------------

@Entity(name = TEMPLATE_NAME)
@Table(
        indexes = {
                @Index(
                        columnList = FACILITY_ID_COLUMN + " ASC, " +
                                     NAME_COLUMN + " ASC",
                        name = "IX_" + TEMPLATE_TABLE + "_" + NAME_COLUMN,
                        unique = true
                )
        },
        name = TEMPLATE_TABLE
)
@Access(AccessType.FIELD)

@NamedQueries({
        @NamedQuery(
                name = TEMPLATE_NAME + ".findAll",
                query = "SELECT t FROM " + TEMPLATE_NAME + " t " +
                        "ORDER BY t." + FACILITY_ID_COLUMN + ", t." +
                        NAME_COLUMN
        ),
        @NamedQuery(
                name = TEMPLATE_NAME + ".findByFacilityId",
                query = "SELECT t FROM " + TEMPLATE_NAME + " t " +
                        "WHERE t." + FACILITY_ID_COLUMN + " =: " +
                        FACILITY_ID_COLUMN + " " +
                        "ORDER BY t." + FACILITY_ID_COLUMN + ", t." +
                        NAME_COLUMN
        ),
        @NamedQuery(
                name = TEMPLATE_NAME + ".findById",
                query = "SELECT t FROM " + TEMPLATE_NAME + " t " +
                        "WHERE t." + ID_COLUMN + " = :" + ID_COLUMN
        ),
        @NamedQuery(
                name = TEMPLATE_NAME + ".findByName",
                query = "SELECT t FROM " + TEMPLATE_NAME + " t " +
                        "WHERE t." + FACILITY_ID_COLUMN + " = :" +
                        FACILITY_ID_COLUMN + " AND " +
                        "t." + NAME_COLUMN + " LIKE " +
                        "LOWER(CONCAT('%',:" + NAME_COLUMN + ",'%')) " +
                        "ORDER BY t." + FACILITY_ID_COLUMN + ", t." +
                        NAME_COLUMN
        ),
        @NamedQuery(
                name = TEMPLATE_NAME + ".findByNameExact",
                query = "SELECT t FROM " + TEMPLATE_NAME + " t " +
                        "WHERE t." + FACILITY_ID_COLUMN + " = :" +
                        FACILITY_ID_COLUMN + " AND t." +
                        NAME_COLUMN + " = :" + NAME_COLUMN + " " +
                        "ORDER BY t." + FACILITY_ID_COLUMN + ", t." +
                        NAME_COLUMN
        )
})

// API Documentation ---------------------------------------------------------

@Schema(
        description = "A template for a set of mats to be generated for " +
                "a specific registration date.",
        name = TEMPLATE_NAME
)

public class Template extends Model<Template> implements Constants {

    // Instance Variables ----------------------------------------------------

    @Column(
            name = ALL_MATS_COLUMN,
            nullable = false
    )
    @Schema(description = "List of all mat numbers to be generated " +
            "from this template")
    @NotEmpty(message = ALL_MATS_VALIDATION_MESSAGE)
    private String allMats;

    @Column(
            name = COMMENTS_COLUMN,
            nullable = true
    )
    @Schema(description = "Optional comments about this template.")
    private String comments;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            foreignKey = @ForeignKey(
                    name = "fk_" + TEMPLATE_TABLE + "_" + FACILITY_TABLE,
                    value = ConstraintMode.CONSTRAINT
            ),
            insertable = false,
            name = FACILITY_ID_COLUMN,
            referencedColumnName = ID_COLUMN,
            updatable = false
    )
    @Schema(description = "Details of the facility this template " +
            "   is associated with.")
    private Facility facility;

    @Column(
            name = FACILITY_ID_COLUMN,
            nullable = false
    )
    @NotNull(message = FACILITY_ID_VALIDATION_MESSAGE)
    @Schema(description = "ID of the facility to which this template belongs.")
    private Long facilityId;

    @Column(
            name = HANDICAP_MATS_COLUMN,
            nullable = true
    )
    @Schema(description = "List of mat numbers suitable for " +
            "handicap use. Must be a subset of all mats.")
    private String handicapMats;

    @Column(
            name = NAME_COLUMN,
            nullable = false
    )
    @NotEmpty(message = NAME_VALIDATION_MESSAGE)
    @Schema(description = "Name of this template. " +
            "Must be unique within a facility.")
    private String name;

    @Column(
            name = SOCKET_MATS_COLUMN,
            nullable = true
    )
    @Schema(description = "List of mat numbers with an electric " +
            "socket nearby. Must be a subset of all mats.")
    private String socketMats;

    // Constructors ----------------------------------------------------------

    public Template() { }

    public Template(
            String allMats,
            String comments,
            Long facilityId,
            String handicapMats,
            String name,
            String socketMats
    ) {
        this.allMats = allMats;
        this.comments = comments;
        this.facilityId = facilityId;
        this.handicapMats = handicapMats;
        this.name = name;
        this.socketMats = socketMats;
    }

    // Property Methods ------------------------------------------------------

    public String getAllMats() {
        return allMats;
    }

    public void setAllMats(String allMats) {
        this.allMats = allMats;
    }

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

    public String getHandicapMats() {
        return handicapMats;
    }

    public void setHandicapMats(String handicapMats) {
        this.handicapMats = handicapMats;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSocketMats() {
        return socketMats;
    }

    public void setSocketMats(String socketMats) {
        this.socketMats = socketMats;
    }

    // Public Methods --------------------------------------------------------

    @Override
    public void copy(Template that) {
        this.allMats = that.allMats;
        this.comments = that.comments;
        this.facilityId = that.facilityId;
        this.handicapMats = that.handicapMats;
        this.name = that.name;
        this.socketMats = that.socketMats;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Template)) {
            return false;
        }
        Template that = (Template) object;
        return new EqualsBuilder()
                .appendSuper(super.equals(that))
                .append(this.allMats, that.allMats)
                .append(this.comments, that.comments)
                .append(this.facilityId, that.facilityId)
                .append(this.handicapMats, that.handicapMats)
                .append(this.name, that.name)
                .append(this.socketMats, that.socketMats)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(this.allMats)
                .append(this.comments)
                .append(this.facilityId)
                .append(this.handicapMats)
                .append(this.name)
                .append(this.socketMats)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .appendSuper(super.toString())
                .append(ALL_MATS_COLUMN, this.allMats)
                .append(COMMENTS_COLUMN, this.comments)
                .append(FACILITY_ID_COLUMN, this.facilityId)
                .append(HANDICAP_MATS_COLUMN, this.handicapMats)
                .append(NAME_COLUMN, this.name)
                .append(SOCKET_MATS_COLUMN, this.socketMats)
                .toString();
    }

}
