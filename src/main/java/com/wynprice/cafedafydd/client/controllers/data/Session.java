package com.wynprice.cafedafydd.client.controllers.data;


import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.common.utils.DateUtils;
import lombok.Data;

import java.util.Date;

import static com.wynprice.cafedafydd.common.DatabaseStrings.Sessions;

@Data
public class Session {
    private final int fieldID;
    private final int computerID;
    private final Date startDate;
    private final Date endDate;
    private final float price;
    private final boolean hasPaid;

    public static Session fromRecord(DatabaseRecord record) {
        return new Session(
            record.getPrimaryField(),
            record.getField(Sessions.COMPUTER_ID).getAsInt(),
            record.getField(Sessions.ISO8601_START).getAsDate(),
            record.getField(Sessions.ISO8601_END).getAsDate(),
            record.getField(Sessions.CALCULATED_PRICE).getAsFloat(),
            record.getField(Sessions.PAID).getAsBoolean()
        );
    }

    @Override
    public String toString() {
        if(this.endDate == DateUtils.EMPTY_DATE) {
            return "ID: " + this.fieldID + " Started at " + this.startDate;
        }
        return
            "ID: " + this.fieldID +
            " Took " + DateUtils.getStringDifference(this.endDate, this.startDate) +
            " and cost £" + this.price +
            " - " + (this.hasPaid ? "paid" : "not paid");
    }
}