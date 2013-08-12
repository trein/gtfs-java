package com.trein.gtfs.vo;

import com.googlecode.jcsv.annotations.MapToNamedColumn;

/**
 * Fare information for a transit organization's routes.
 * 
 * @author trein
 */
public class GtfsFareAttribute {
    
    @MapToNamedColumn(column = "fare_id")
    private String fareId;
    
    @MapToNamedColumn(column = "price")
    private Double price;
    
    @MapToNamedColumn(column = "currency_type")
    private String currencyType;
    
    @MapToNamedColumn(column = "payment_method")
    private Integer paymentType;
    
    @MapToNamedColumn(column = "transfers")
    private Integer transferType;
    
    @MapToNamedColumn(column = "transfer_duration", optional = true)
    private Double transferDuration;
    
    /**
     * price Required The price field contains the fare price, in the unit specified by
     * currency_type.
     */
    public String getFareId() {
	return this.fareId;
    }
    
    /**
     * fare_id Required The fare_id field contains an ID that uniquely identifies a fare class. The
     * fare_id is dataset unique.
     */
    public Double getPrice() {
	return this.price;
    }
    
    /**
     * currency_type Required The currency_type field defines the currency used to pay the fare.
     * Please use the ISO 4217 alphabetical currency codes which can be found at the following URL:
     * http://www.iso.org/iso/home/standards/iso4217.htm.
     */
    public String getCurrencyType() {
	return this.currencyType;
    }
    
    /**
     * payment_method Required The payment_method field indicates when the fare must be paid. Valid
     * values for this field are:
     * 
     * <pre>
     * 0 - Fare is paid on board.
     * 1 - Fare must be paid before boarding.
     * </pre>
     */
    public Integer getPaymentType() {
	return this.paymentType;
    }
    
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
    public Integer getTransferType() {
	return this.transferType;
    }
    
    /**
     * transfer_duration Optional The transfer_duration field specifies the length of time in
     * seconds before a transfer expires. When used with a transfers value of 0, the
     * transfer_duration field indicates how long a ticket is valid for a fare where no transfers
     * are allowed. Unless you intend to use this field to indicate ticket validity,
     * transfer_duration should be omitted or empty when transfers is set to 0.
     */
    public Double getTransferDuration() {
	return this.transferDuration;
    }
    
}
