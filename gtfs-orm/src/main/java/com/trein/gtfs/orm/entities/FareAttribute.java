package com.trein.gtfs.orm.entities;

import javax.persistence.Id;

/**
 * Fare information for a transit organization's routes.
 * 
 * @author trein
 */
// @Entity(name = "fare_attributes")
public class FareAttribute {
    
    @Id
    private long id;
    
    /**
     * fare_id Required The fare_id field contains an ID that uniquely identifies a fare class. The
     * fare_id is dataset unique.
     */
    private Fare fare;
    
    /**
     * price Required The price field contains the fare price, in the unit specified by
     * currency_type.
     */
    private double price;
    
    /**
     * currency_type Required The currency_type field defines the currency used to pay the fare.
     * Please use the ISO 4217 alphabetical currency codes which can be found at the following URL:
     * http://www.iso.org/iso/home/standards/iso4217.htm.
     */
    private CurrencyType currencyType;
    
    /**
     * payment_method Required The payment_method field indicates when the fare must be paid. Valid
     * values for this field are:
     * 
     * <pre>
     * 0 - Fare is paid on board.
     * 1 - Fare must be paid before boarding.
     * </pre>
     */
    private PaymentType paymentType;
    
    /**
     * transfers Required The transfers field specifies the number of transfers permitted on this
     * fare. Valid values for this field are:
     * 
     * <pre>
     * 0 - No transfers permitted on this fare.
     * 1 - Passenger may transfer once.
     * 2 - Passenger may transfer twice.
     * (empty) - If this field is empty, unlimited transfers are permitted.
     * </pre>
     */
    private TransferType transferType;
    
    /**
     * transfer_duration Optional The transfer_duration field specifies the length of time in
     * seconds before a transfer expires. When used with a transfers value of 0, the
     * transfer_duration field indicates how long a ticket is valid for a fare where no transfers
     * are allowed. Unless you intend to use this field to indicate ticket validity,
     * transfer_duration should be omitted or empty when transfers is set to 0.
     */
    private double transferDuration;
}
