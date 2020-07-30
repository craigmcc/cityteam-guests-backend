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

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Represents a list of mat numbers, made up of ranges and individual
 * numbers (for example: <code>1-3,5,9-12</code>).  Mat numbers in a list
 * must be in ascending order, may not be negative, and cannot contain
 * duplicates.</p>
 *
 * <p>In a database, a <code>MatsList</code> is represented by a String.
 * This class contains methods to parse and validate such lists.</p>
 *
 * <p>If any problems occur during parsing,
 * <code>IllegalArgumentException</code> will be thrown.</p>
 */
public class MatsList {

    // Constructors ---------------------------------------------------------

    public MatsList(@NotNull String list) throws IllegalArgumentException {
        explode(list);
    }

    // Instance Variables ----------------------------------------------------

    private List<Integer> exploded = new ArrayList<>();
    private int highest = 0;

    // Public Methods --------------------------------------------------------

    /**
     * <p>Return the exploded set of mat numbers.</p>
     */
    public List<Integer> exploded() {
        return exploded;
    }

    /**
     * <p>Is the specified mat number included in this list?</p>
     */
    public boolean isMemberOf(int matNumber) {
        for (Integer item : exploded) {
            if (item == matNumber) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>Is this mats list a subset (or exact match) of the
     * specified mats list?</p>
     */
    public boolean isSubsetOf(MatsList that) {
        for (Integer item : exploded) {
            if (!that.isMemberOf(item)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int element : exploded) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append("" + element);
        }
        return sb.toString();
    }

    // Private Methods -------------------------------------------------------

    /**
     * <p>Parse a list of individual mat number represented
     * in this String into <code>exploded</code> and
     * <code>highest</code>.</p>
     *
     * @param list String containing list of mat numbers and ranges
     */
    private List<Integer> explode(String list)
            throws IllegalArgumentException {

        String items[] = list.split(",");
        if (items.length < 1) {
            throw new IllegalArgumentException
                    ("List '" + list + "' must have at least one item");
        }

        for (String item : items) {
            if (item.contains("-")) {
                String subitems[] = item.split("-");
                if (subitems.length != 2) {
                    throw new IllegalArgumentException
                            ("List item '" + item + "' must not contain" +
                                    " more than one dash");
                }
                int from = validated(subitems[0]);
                int to = validated(subitems[1]);
                if (from > to) {
                    throw new IllegalArgumentException
                            ("List item '" + item + "' must have" +
                                    " lower number first");
                } else if (from <= highest) {
                    throw new IllegalArgumentException
                            ("List item '" + item + "' is out of" +
                                    " ascending order");
                }
                for (int i = from; i <= to; i++) {
                    exploded.add(i);
                }
                highest = to;
            } else {
                int only = validated(item);
                if (only <= highest) {
                    throw new IllegalArgumentException
                            ("List item '" + only + "' is out of" +
                                    " ascending order");
                }
                exploded.add(only);
                highest = only;
            }
        }

        return null; // TODO - explode()

    }

    /**
     * <p>Return the integer value of the specified string, or throw
     * an exception if this item cannot be converted.</p>
     *
     * @param item String item to be converted and validated
     */
    private int validated(String item) {
        String trimmed = item.trim();
        if (trimmed.length() < 1) {
            throw new IllegalArgumentException
                    ("Item '" + item + "' cannot be blank");
        }
        try {
            int result = Integer.valueOf(trimmed);
            if (result < 1) {
                throw new IllegalArgumentException
                        ("Item '" + item + "' must be positive");
            }
            return result;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException
                    ("Item '" + item + "' is not a number");
        }
    }

}
