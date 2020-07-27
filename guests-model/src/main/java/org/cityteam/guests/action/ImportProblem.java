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
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import static org.cityteam.guests.model.Constants.IMPORT_PROBLEM_NAME;

// API Documentation ---------------------------------------------------------

@Schema(
        description = "Problem report on a particular import.",
        name = IMPORT_PROBLEM_NAME
)

public class ImportProblem {

    // Instance Variables ----------------------------------------------------

    @Schema(description = "The error message about this import.")
    private String message;

    @Schema(description = "The import about which this problem is reported.")
    private ImportRequest problem;

    @Schema(description = "Description of the resolution for this problem.")
    private String resolution;

    // Constructors ----------------------------------------------------------

    public ImportProblem() { }

    public ImportProblem(String message, ImportRequest problem) {
        this.message = message;
        this.problem = problem;
    }

    public ImportProblem(String message, ImportRequest problem, String resolution) {
        this.message = message;
        this.problem = problem;
        this.resolution = resolution;
    }

    // Property Methods ------------------------------------------------------

    public String getMessage() {
        return message;
    }

    public ImportRequest getProblem() {
        return problem;
    }

    public String getResolution() {
        return resolution;
    }

    // Public Methods --------------------------------------------------------

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("message", this.message)
                .append("problem", this.problem)
                .append("resolution", this.resolution)
                .toString();
    }

}
