package com.wynprice.cafedafydd.client.controllers.data;


import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.common.utils.DateUtils;
import lombok.Data;

import java.util.Date;

import static com.wynprice.cafedafydd.common.FieldDefinitions.Sessions;

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
            record.get(Sessions.COMPUTER_ID),
            record.get(Sessions.ISO8601_START),
            record.get(Sessions.ISO8601_END),
            record.get(Sessions.CALCULATED_PRICE),
            record.get(Sessions.PAID)
        );
    }

    @Override
    public String toString() {
        if(DateUtils.EMPTY_DATE.equals(this.endDate)) {
            return "ID: " + this.fieldID + " Started at " + this.startDate;
        }
        return
            "ID: " + this.fieldID +
            " Took " + DateUtils.getStringDifference(this.endDate, this.startDate) +
            " and cost £" + this.price +
            " - " + (this.hasPaid ? "paid" : "not paid");
    }
}