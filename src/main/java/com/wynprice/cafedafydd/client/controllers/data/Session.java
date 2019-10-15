package com.wynprice.cafedafydd.client.controllers.data;


import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.common.utils.DateUtils;
import lombok.Data;

import java.util.Date;

import static com.wynprice.cafedafydd.common.DatabaseStrings.Sessions;

/**
 * Sessions holds data about a current session. Used by {@link com.wynprice.cafedafydd.client.controllers.SearchSessionsPage} 
 * and {@link com.wynprice.cafedafydd.client.controllers.UserLoginPage}. <br>
 * This is mainly used to delegate the {@link #toString} method, to allow for a customized visulization on the listview. 
 * It also caches the fields from the record. 
 */
@Data
public class Session {
    /**
     * The primary field id 
     */
    private final int fieldID;
    
    /**
     * The id of the computer used in this session 
     */
    private final int computerID;
    
    /**
     * The datetime of when this session was started. 
     */
    private final Date startDate;
    
    /**
     * The datetime of when this session ended, or Unix epoch if the session has not ended.
     */
    private final Date endDate;
    
    /**
     * The calculated price for this database, or 0 if the session has not ended.
     */
    private final float price;
    
    /**
     * The datetime of when this session ended, or Unix epoch if the session has not ended.
     */
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
