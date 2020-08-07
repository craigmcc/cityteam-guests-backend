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

import org.cityteam.guests.model.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

@Category(UnitTests.class)
public class MatsListUnitTest {

    // Instance Variables ----------------------------------------------------

    // Single Number Lists

    // "-" means we are looking for a range, so not interpreted as negative
    private String cannotBeBlankSingleNumberLists[] = {
            "-1",
            "1,-2",
    };

    private String cannotHaveDuplicateSingleNumberLists[] = {
            "2,2"
    };

    private String mustNotContainAnEmptyItemSingleNumberLists[] = {
            "",
            "3,,5",
            ",4"
    };

    private String mustBePositiveSingleNumberLists[] = {
            "0",
            "2,0",
            "3,4,0",
    };

    private String notANumberSingleNumberLists[] = {
            "a",
            "1,b,3",
            "1,2,c",
    };

    private String outOfOrderSingleNumberLists[] = {
            "1,1",
            "2,1",
            "1,2,2",
            "1,3,2",
    };

    private String validSingleNumberLists[] = {
            "1",
            "1,2",
            "2,3,4",
            " 1 ",
            " 1 , 2 ",
            " 2 , 3, 4 ",
    };

    // Range Lists

    private String backwardsRangeLists[] = {
            "6-1",
            "1-3,5-4",
            "1,2,4-3",
    };

    private String outOfOrderRangeLists[] = {
            "1-3,2-4",
            "1-3,2,4-5",
            "1-3,3-5",
    };

    private String validRangeLists[] = {
            "1-6",
            "1-3, 4-6",
            " 1 - 4 ",
            "1-2, 4 - 6 ",
            "1,2,4,5-6",
            "1,2-2,3",
            "3,4,7-9",
    };

    private String validRangeListsMatches[] = {
            "1,2,3,4,5,6",
            "1,2,3,4,5,6",
            "1,2,3,4",
            "1,2,4,5,6",
            "1,2,4,5,6",
            "1,2,3",
            "3,4,7,8,9",
    };

    // Subset Lists

    private String validSubsetParents[] = {
            "1-3, 5, 7-8",
            "1,5-6",
            "1-7",
    };

    private String validSubsetChilds[] = {
            "1,3,5,7-8",
            "1,5-6",
            "2-4,6",
    };

    private String invalidSubsetParents[] = {
            "1-3, 5, 7-8",
            "1,5-6",
            "1-7",
    };

    private String invalidSubsetChilds[] = {
            "4,6",
            "3,4",
            "8",
    };

    // Test Methods ----------------------------------------------------------

    // Range List Tests

    @Test
    public void backwardRangeLists() {
        for (String list : backwardsRangeLists) {
            try {
                new MatsList(list);
                fail("Should have failed for '" + list + "'");
            } catch (IllegalArgumentException e) {
                assertThat(e.getMessage(),
                        endsWith("must have lower number first"));
            }
        }
    }

    @Test
    public void outOfOrderRangeLists() {
        for (String list : outOfOrderRangeLists) {
            try {
                new MatsList(list);
                fail("Should have failed for '" + list + "'");
            } catch (IllegalArgumentException e) {
                assertThat(e.getMessage(),
                        endsWith("is out of ascending order"));
            }
        }
    }

    @Test
    public void validRangeLists() {
        for (int i = 0; i < validRangeLists.length; i++) {
            MatsList matsList = new MatsList(validRangeLists[i]);
            assertThat(matsList.toString(),
                    is(equalTo(validRangeListsMatches[i])));
        }
    }

    // Single Number Lists Tests

    @Test
    public void cannotBeBlankSingleNumberLists() {
        for (String list : cannotBeBlankSingleNumberLists) {
            try {
                new MatsList(list);
                fail("Should have failed for '" + list + "'");
            } catch (IllegalArgumentException e) {
                assertThat(e.getMessage(),
                        endsWith("cannot be blank"));
            }
        }
    }

    @Test
    public void setHaveDuplicateSingleNumberLists() {
        for (String list : cannotHaveDuplicateSingleNumberLists) {
            try {
                new MatsList(list);
                fail("Should have failed for '" + list + "'");
            } catch (IllegalArgumentException e) {
                assertThat(e.getMessage(),
                        endsWith("is out of ascending order"));
            }
        }
    }

    @Test
    public void mustBePositiveSingleNumberLists() {
        for (String list : mustBePositiveSingleNumberLists) {
            try {
                new MatsList(list);
                fail("Should have failed for '" + list + "'");
            } catch (IllegalArgumentException e) {
                assertThat(e.getMessage(),
                        endsWith("must be positive"));
            }
        }
    }

    @Test
    public void mustNotContainAnEmptyItemSingleNumberLists() {
        for (String list : mustNotContainAnEmptyItemSingleNumberLists) {
            try {
                new MatsList(list);
                fail("Should have failed for '" + list + "'");
            } catch (IllegalArgumentException e) {
                ; // Expected result
            }
        }
    }

    @Test
    public void notANumberSingleNumberLists() {
        for (String list : notANumberSingleNumberLists) {
            try {
                new MatsList(list);
                fail("Should have failed for '" + list + "'");
            } catch (IllegalArgumentException e) {
                assertThat(e.getMessage(),
                        endsWith("is not a number"));
            }
        }
    }

    @Test
    public void outOfOrderSingleNumberLists() {
        for (String list : outOfOrderSingleNumberLists) {
            try {
                new MatsList(list);
                fail("Should have failed for '" + list + "'");
            } catch (IllegalArgumentException e) {
                assertThat(e.getMessage(),
                        endsWith("is out of ascending order"));
            }
        }
    }

    @Test
    public void validSingleNumberLists() {
        for (String list : validSingleNumberLists) {
            try {
                new MatsList(list);
            } catch (IllegalArgumentException e) {
                fail("Failed for '" + list + "' with error: " + e.getMessage());
            }
        }
    }

    // Subset Lists Tests

    @Test
    public void invalidSubsets() {
        for (int i = 0; i < invalidSubsetParents.length; i++) {
            MatsList parent = new MatsList(invalidSubsetParents[i]);
            MatsList child = new MatsList(invalidSubsetChilds[i]);
            assertThat(child.isSubsetOf(parent), is(false));
        }
    }

    @Test
    public void validSubsets() {
        for (int i = 0; i < validSubsetParents.length; i++) {
            MatsList parent = new MatsList(validSubsetParents[i]);
            MatsList child = new MatsList(validSubsetChilds[i]);
            assertThat(child.isSubsetOf(parent), is(true));
        }
    }

}
