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
import org.cityteam.guests.model.Registration;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

import static org.cityteam.guests.model.Constants.IMPORT_RESULTS_NAME;

// API Documentation ---------------------------------------------------------

@Schema(
        description = "Results of processing a list of imports.",
        name = IMPORT_RESULTS_NAME
)

public class ImportResults {

    // Instance Variables ----------------------------------------------------

    @Schema(description = "Problem reports on this set of imports.")
    private List<ImportProblem> problems;

    @Schema(description = "Registrations resulting from this set of imports.")
    private List<Registration> registrations;

    // Constructors ----------------------------------------------------------

    public ImportResults() { }

    public ImportResults(
            List<ImportProblem> problems,
            List<Registration> registrations
    ) {
        this.problems = problems;
        this.registrations = registrations;
    }

    // Property Methods ------------------------------------------------------

    public List<ImportProblem> getProblems() {
        return problems;
    }

    public List<Registration> getRegistrations() {
        return registrations;
    }

    // Public Methods --------------------------------------------------------

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("problems", this.problems)
                .append("registrations", this.registrations)
                .toString();
    }

}
