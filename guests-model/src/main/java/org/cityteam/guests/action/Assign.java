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
import org.cityteam.guests.model.Facility;
import org.cityteam.guests.model.Guest;
import org.cityteam.guests.model.Registration;
import org.cityteam.guests.model.types.PaymentType;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * <p>Values needed to assign a particular {@link Guest} to a particular
 * {@link Registration}.  The only required value is <code>guestId</code>,
 * which must identify a {@link Guest} associated with the same
 * {@link Facility} as the {@link Registration} that is being updated.
 */
public class Assign implements Constants {

    // Instance Variables ----------------------------------------------------

    private String comments;
    private Long guestId;
    private BigDecimal paymentAmount;
    private PaymentType paymentType;
    private LocalTime showerTime;
    private LocalTime wakeupTime;

    // Constructors ----------------------------------------------------------

    public Assign() { }

    public Assign(
            String comments,
            Long guestId,
            BigDecimal paymentAmount,
            PaymentType paymentType,
            LocalTime showerTime,
            LocalTime wakeupTime
    ) {
        this.comments = comments;
        this.guestId = guestId;
        this.paymentAmount = paymentAmount;
        this.paymentType = paymentType;
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

    public Long getGuestId() {
        return guestId;
    }

    public void setGuestId(Long guestId) {
        this.guestId = guestId;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(COMMENTS_COLUMN, this.comments)
                .append(GUEST_ID_COLUMN, this.guestId)
                .append(PAYMENT_AMOUNT_COLUMN, this.paymentAmount)
                .append(PAYMENT_TYPE_COLUMN, this.paymentType)
                .append(SHOWER_TIME_COLUMN, this.showerTime)
                .append(WAKEUP_TIME_COLUMN, this.wakeupTime)
                .toString();
    }

}
