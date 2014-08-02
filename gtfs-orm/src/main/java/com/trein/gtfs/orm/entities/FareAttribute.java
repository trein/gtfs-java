package com.trein.gtfs.orm.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Fare information for a transit organization's routes.
 *
 * @author trein
 */
@Entity(name = "fare_attributes")
@Cache(region = "entity", usage = CacheConcurrencyStrategy.READ_WRITE)
public class FareAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    @JoinColumn(name = "fare", nullable = false)
    private Fare fare;

    @Column(name = "price", nullable = false)
    private double price;

    @Column(name = "currency_type")
    private CurrencyType currencyType;

    @Column(name = "payment_type")
    private PaymentType paymentType;

    @Column(name = "transfer_type")
    private TransferType transferType;

    @Column(name = "transfer_duration")
    private double transferDuration;
    
    FareAttribute() {
    }
    
    public FareAttribute(Fare fare, double price, CurrencyType currencyType, PaymentType paymentType, TransferType transferType,
            double duration) {
        this.fare = fare;
        this.price = price;
        this.currencyType = currencyType;
        this.paymentType = paymentType;
        this.transferType = transferType;
        this.transferDuration = duration;
    }

    public long getId() {
        return this.id;
    }

    /**
     * fare_id Required The fare_id field contains an ID that uniquely identifies a fare class. The
     * fare_id is dataset unique.
     */
    public Fare getFare() {
        return this.fare;
    }

    /**
     * price Required The price field contains the fare price, in the unit specified by
     * currency_type.
     */
    public double getPrice() {
        return this.price;
    }

    /**
     * currency_type Required The currency_type field defines the currency used to pay the fare.
     * Please use the ISO 4217 alphabetical currency codes which can be found at the following URL:
     * http://www.iso.org/iso/home/standards/iso4217.htm.
     */
    public CurrencyType getCurrencyType() {
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
    public PaymentType getPaymentType() {
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
    public TransferType getTransferType() {
        return this.transferType;
    }

    /**
     * transfer_duration Optional The transfer_duration field specifies the length of time in
     * seconds before a transfer expires. When used with a transfers value of 0, the
     * transfer_duration field indicates how long a ticket is valid for a fare where no transfers
     * are allowed. Unless you intend to use this field to indicate ticket validity,
     * transfer_duration should be omitted or empty when transfers is set to 0.
     */
    public double getTransferDuration() {
        return this.transferDuration;
    }
    
}
