/*
 * Class:     RecordLedgerFormat
 * Version:   $Revision$
 * Date:      $Date$
 * Copyright: Copyright © 2002-2007 Ian Phillips and Örjan Lundberg.
 * License:   Apache Software License (Version 2.0)
 */
package org.rvsnoop;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import rvsnoop.Record;
import rvsnoop.RecordTypes;
import rvsnoop.RvConnection;

import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

/**
 * A format that describes the contents of a record ledger.
 *
 * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
 * @version $Revision$, $Date$
 */
public final class RecordLedgerFormat implements AdvancedTableFormat {

    private static final class ComparableComparator implements Comparator {
        ComparableComparator() {
            super();
        }
        public int compare(Object o1, Object o2) {
            return ((Comparable) o1).compareTo(o2);
        }
    }

    /**
     * Format information for a single column.
     *
     * @author <a href="mailto:ianp@ianp.org">Ian Phillips</a>
     * @version $Revision$, $Date$
     */
    public static abstract class ColumnFormat {
        private final Class clazz;
        private final Comparator comparator;
        private final String identifier;
        private final String name;
        ColumnFormat(String id, String name, Class clazz) {
            this(id, name, clazz, Collator.getInstance());
        }
        ColumnFormat(String id, String name, Class clazz, Comparator comparator) {
            this.clazz = clazz;
            this.comparator = comparator;
            this.identifier = id;
            this.name = name;
        }
        public Class getClazz() {
            return clazz;
        }
        public Comparator getComparator() {
            return comparator;
        }
        public final String getIdentifier() {
            return identifier;
        }
        public final String getName() {
            return name;
        }
        public abstract Object getValue(Record record);
    }

    public static ColumnFormat CONNECTION = new ColumnFormat("connection", "Connection", String.class, new ComparableComparator()) {
        private static final long serialVersionUID = -4938884664732119140L;
        public Object getValue(Record record) {
            final RvConnection connection = record.getConnection();
            return connection != null ? connection.getDescription() : "";
        }
    };

    public static final ColumnFormat MESSAGE = new ColumnFormat("message", "Message", Object.class) {
        private static final long serialVersionUID = 3526646591996520301L;
        public Object getValue(Record record) {
            return record.getMessage();
        }
    };

    public static final ColumnFormat SEQUENCE_NO = new ColumnFormat("sequence", "Seq. No.", Long.class, new ComparableComparator()) {
        private static final long serialVersionUID = -5762735421370128862L;
        public Object getValue(Record record) {
            return Long.toString(record.getSequenceNumber());
        }
    };

    public static final ColumnFormat SIZE_IN_BYTES = new ColumnFormat("size", "Size (bytes)", Integer.class, new ComparableComparator()) {
        private static final long serialVersionUID = 2274660797219318776L;
        public Object getValue(Record record) {
            return new Integer(record.getSizeInBytes());
        }
    };

    public static final ColumnFormat SUBJECT = new ColumnFormat("subject", "Subject", String.class, new ComparableComparator()) {
        private static final long serialVersionUID = 7415054603888398693L;
        public Object getValue(Record record) {
            return record.getSendSubject();
        }
    };

    public static final ColumnFormat TIMESTAMP = new ColumnFormat("timestamp", "Timestamp", Date.class, new ComparableComparator()) {
        private static final long serialVersionUID = -3858078006527711756L;
        public Object getValue(Record record) {
            return new Date(record.getTimestamp());
        }
    };

    public static final ColumnFormat TRACKING_ID = new ColumnFormat("tracking", "Tracking ID", String.class, new ComparableComparator()) {
        private static final long serialVersionUID = -3033175036104293820L;
        public Object getValue(Record record) {
            return record.getTrackingId();
        }
    };

    public static final ColumnFormat TYPE = new ColumnFormat("type", "Type", String.class, new ComparableComparator()) {
        private static final long serialVersionUID = 6353909616946303068L;
        final RecordTypes types = RecordTypes.getInstance();
        public Object getValue(Record record) {
            return types.getFirstMatchingType(record).getName();
        }
    };

    public static final List ALL_COLUMNS = Collections.unmodifiableList(
        Arrays.asList(new ColumnFormat[] {
            CONNECTION, TIMESTAMP, SEQUENCE_NO, TYPE,
            SUBJECT, SIZE_IN_BYTES, TRACKING_ID, MESSAGE
    }));

    /**
     * Gets a column format by it's ID.
     *
     * @param id The ID of the column.
     * @return The column, or <code>null</code> if none match the supplied ID.
     */
    public static ColumnFormat getColumn(String id) {
        for (int i = 0, imax = ALL_COLUMNS.size(); i < imax; ++i) {
            final ColumnFormat column = (ColumnFormat) ALL_COLUMNS.get(i);
            if (column.getIdentifier().equals(id)) { return column; }
        }
        return null;
    }

    private final List columns = new ArrayList(ALL_COLUMNS);

    private EventTableModel model;

    /**
     * Create a new <code>RecordLedgerFormat</code>.
     */
    public RecordLedgerFormat() {
        super();
        // Do not display the message by default.
        columns.remove(MESSAGE);
    }

    /**
     * Add a column to this format.
     *
     * @param column The column to add.
     */
    public void add(ColumnFormat column) {
        if (!columns.contains(column) && columns.add(column) && model != null) {
            model.setTableFormat(this);
        }
    }

    /* (non-Javadoc)
     * @see ca.odell.glazedlists.gui.AdvancedTableFormat#getColumnClass(int)
     */
    public Class getColumnClass(int column) {
        return ((ColumnFormat) columns.get(column)).getClazz();
    }

    /* (non-Javadoc)
     * @see ca.odell.glazedlists.gui.AdvancedTableFormat#getColumnComparator(int)
     */
    public Comparator getColumnComparator(int column) {
        return ((ColumnFormat) columns.get(column)).getComparator();
    }

    /* (non-Javadoc)
     * @see ca.odell.glazedlists.gui.TableFormat#getColumnCount()
     */
    public int getColumnCount() {
        return columns.size();
    }

    /* (non-Javadoc)
     * @see ca.odell.glazedlists.gui.TableFormat#getColumnName(int)
     */
    public String getColumnName(int index) {
        return ((ColumnFormat) columns.get(index)).getName();
    }

    /**
     * Get a read only list of the columns currently displayed by this format.
     *
     * @return A <em>read-only</em> list containing the columns.
     */
    public List getColumns() {
        return Collections.unmodifiableList(columns);
    }

    /* (non-Javadoc)
     * @see ca.odell.glazedlists.gui.TableFormat#getColumnValue(java.lang.Object, int)
     */
    public Object getColumnValue(Object record, int index) {
        return ((ColumnFormat) columns.get(index)).getValue((Record) record);
    }

    /**
     * Remove a column from this format.
     *
     * @param column The column to remove.
     */
    public void remove(ColumnFormat column) {
        if (columns.remove(column) && model != null) {
            model.setTableFormat(this);
        }
    }

    /**
     * Set the columns included in this format.
     *
     * @param columns The columns to set.
     */
    public void setColumns(List columns) {
        columns.clear();
        columns.addAll(columns);
        if (model != null) { model.setTableFormat(this); }
    }

    /**
     * Set the model that will display this format.
     * <p>
     * If a model has been set it will be automatically notified whenever a
     * column is added to or removed from this format.
     *
     * @param model
     */
    public void setModel(EventTableModel model) {
        // TODO: reduce visibility.
        this.model = model;
    }

}
