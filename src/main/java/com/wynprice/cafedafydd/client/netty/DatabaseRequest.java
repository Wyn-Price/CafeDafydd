package com.wynprice.cafedafydd.client.netty;

import com.wynprice.cafedafydd.client.CafeDafyddMain;
import com.wynprice.cafedafydd.common.BackupHeader;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketGetDatabaseEntries;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketHasDatabaseEntry;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketRequestBackupEntry;
import com.wynprice.cafedafydd.common.netty.packets.serverbound.PacketRequestBackupHeaders;
import com.wynprice.cafedafydd.common.search.SearchRequirement;
import com.wynprice.cafedafydd.common.utils.DatabaseRecord;
import com.wynprice.cafedafydd.common.utils.FormBuilder;
import com.wynprice.cafedafydd.common.utils.RequestType;
import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * {@link DatabaseRequest} is used to send requests to the server-side databases. These are currently in the form:
 * <lu>
 *     <li>Has Entry - Checks to see if the database contains an entry. See: {@link DatabaseRequest#HAS_ENTRY}</li>
 *
 *     <li>Get Entry - Returns a list of database entries matching the given form. See {@link DatabaseRequest#GET_ENTRIES}</li>
 *
 *     <li>Search Entry - Searches for a list of database entries matching the form. This search is done by
 *     {@code s1.contains(s1) || s2.contains(s1)}. See {@link DatabaseRequest#SEARCH_ENTRIES}</li>
 * </lu>
 */
@Log4j2
public class DatabaseRequest {

    /**
     * The form to see if a database has an entry matching the given form
     */
    public static final NamedRecordForm<Boolean> HAS_ENTRY = new NamedRecordForm<>(PacketHasDatabaseEntry::new);

    /**
     * The form to get a list of entries matching the given form.
     */
    public static final NamedRecordForm<List<DatabaseRecord>> GET_ENTRIES = new NamedRecordForm<>((r, d, f) -> new PacketGetDatabaseEntries(RequestType.GET, r, d, f));

    /**
     * The form to search a list of entries matching the given form. Matches are done with {@code s1.contains(s1) || s2.contains(s1)}
     */
    public static final NamedRecordForm<List<DatabaseRecord>> SEARCH_ENTRIES = new NamedRecordForm<>((r, d, f) -> new PacketGetDatabaseEntries(RequestType.SEARCH, r, d, f));

    /**
     * The request to get the list of backup headers for a database.
     */
    public static final RequestForm<List<BackupHeader>, Void> BACKUP_HEADERS = new RequestForm<>((r, d, f) -> new PacketRequestBackupHeaders(r, d));

    /**
     * The request to get the list of backup entries for a database.
     */
    public static final RequestForm<List<String>, Integer> BACKUP_ENTRIES = new RequestForm<>(PacketRequestBackupEntry::new);


    /**
     * The form used to keep track of which requests have been sent, and to handle inbound requests
     * @param <D>
     * @param <T> the type
     */
    //TODO: make the requests "timeout" after a specified time so the data isn't held for ages and fill up the memory
    @RequiredArgsConstructor
    public static class RequestForm<D, T> {

        /**
         * The interface used to create the packets that are sent to the server.
         */
        private final PacketCreation<?, T> creation;

        /**
         * The current integer requests ID.
         */
        private int requests = 0;

        /**
         * This map stores the request id -> the handler for that id
         */
        private final Map<Integer, Consumer<D>> storage = new HashMap<>();

        /**
         * Send the request to the server
         * @param databaseFile the database file to send to the request to
         * @param receiver the handler to use when the request is complete
         * @param data the data to additionally send
         */
        public void sendRequest(String databaseFile, Consumer<D> receiver, T data) {
            int id = this.requests++;
            this.storage.put(id, receiver);
            CafeDafyddMain.getClient().getHandler().sendPacket(this.creation.createPacket(id, databaseFile, data));
        }


        /**
         * Receives the request
         * @param requestID the request ID assigned to the received request
         * @param value the value received.
         */
        void receive(int requestID, D value) {
            //Get and remove the handler. If it exists, invoke it otherwise log an error.
            Consumer<D> removed = this.storage.remove(requestID);
            if(removed != null) {
                Platform.runLater(() -> removed.accept(value));
            } else {
                log.error(new IllegalArgumentException("Could not find request with id " + requestID));
            }
        }
    }

    public static class NamedRecordForm<D> extends RequestForm<D, SearchRequirement[]> {

        public NamedRecordForm(PacketCreation<?, SearchRequirement[]> creation) {
            super(creation);
        }

        /**
         * Send the request to the server. Delegates to {@link #sendRequest(String, Consumer, SearchRequirement[])},
         * with {@code form} going to {@link FormBuilder#getForm()}
         *
         * @param databaseFile the database file to send to the request to
         * @param receiver the handler to use when the request is complete
         * @param form the FormBuilder to create the form from
         * @see #sendRequest(String, Consumer, SearchRequirement[])
         */
        public void sendRequest(String databaseFile, Consumer<D> receiver, FormBuilder form) {
            this.sendRequest(databaseFile, receiver, form.getForm());
        }

        //Allow for varargs
        @Override
        public void sendRequest(String databaseFile, Consumer<D> receiver, SearchRequirement... data) {
            super.sendRequest(databaseFile, receiver, data);
        }
    }

    /**
     * The interface used to create packets from the request id, database name and form.
     * @param <P> The packet class of which to construct a packet.
     */
    private interface PacketCreation<P, T> {
        P createPacket(int requestID, String databaseFile, T data);
    }
}
