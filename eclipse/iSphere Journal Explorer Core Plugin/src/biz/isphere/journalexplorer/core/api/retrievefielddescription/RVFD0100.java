/*******************************************************************************
 * Copyright (c) 2012-2017 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.journalexplorer.core.api.retrievefielddescription;

import java.io.UnsupportedEncodingException;

import biz.isphere.core.internal.api.APIFormat;
import biz.isphere.journalexplorer.core.model.MetaColumn;

import com.ibm.as400.access.AS400;

/**
 * Format RVFD0100 of the iSphere Retrieve File Field Description (QDBTRVFD)
 * API.
 * 
 * @author Thomas Raddatz
 */
public class RVFD0100 extends APIFormat {

    private static final String FIELD_NAME = "name"; //$NON-NLS-1$
    private static final String TYPE = "type"; //$NON-NLS-1$
    private static final String USE = "use"; //$NON-NLS-1$
    private static final String LENGTH = "length"; //$NON-NLS-1$
    private static final String DECIMAL_POSITIONS = "decPos"; //$NON-NLS-1$
    private static final String BUFFER_LENGTH = "buffLen"; //$NON-NLS-1$
    private static final String ALLOCATED_LENGTH = "alcLen"; //$NON-NLS-1$
    private static final String CCSID = "ccsid"; //$NON-NLS-1$
    private static final String IS_VARYING_FIELD = "isVarying"; //$NON-NLS-1$
    private static final String IS_BINARY_FIELD = "isBinary"; //$NON-NLS-1$
    private static final String IS_NULLABLE_FIELD = "isNullable"; //$NON-NLS-1$
    private static final String RESERVED_1 = "reserved_1"; //$NON-NLS-1$
    private static final String DATE_TIME_FORMAT = "dateTimeFormat"; //$NON-NLS-1$
    private static final String DATE_TIME_SEPARATOR = "dateTimeSeparator"; //$NON-NLS-1$
    private static final String TEXT = "text"; //$NON-NLS-1$

    /**
     * Constructs a RVFD0100 object.
     * 
     * @param system - System that calls the API
     * @param bytes - buffer that contains the retrieved field descriptions
     * @throws UnsupportedEncodingException
     */
    public RVFD0100(AS400 system, byte[] bytes) throws UnsupportedEncodingException {
        super(system, "RVFD0100"); //$NON-NLS-1$

        createStructure();

        setBytes(bytes);
    }

    protected RVFD0100(AS400 system, byte[] bytes, String name) throws UnsupportedEncodingException {
        super(system, name);

        createStructure();

        setBytes(bytes);

    }

    /**
     * Sets the offset to a particular field description.
     */
    public void setOffset(int offset) {
        super.setOffset(offset);
    }

    /**
     * Returns the field name.
     * 
     * @return field name
     */
    public String getFieldName() throws UnsupportedEncodingException {
        return getCharValue(FIELD_NAME).trim();
    }

    /**
     * Returns the data type of the field.
     * 
     * @return data type
     * @throws UnsupportedEncodingException
     */
    public String getType() throws UnsupportedEncodingException {
        return getCharValue(TYPE).trim();
    }

    public MetaColumn.DataType getSqlDataType() throws Exception {

        String type = getType();

        if (IQDBRTVFD.TYPE_API_BINARY.equals(type)) {
            int bufferLength = getBufferLength();
            switch (bufferLength) {
            case 2:
                return MetaColumn.DataType.SMALLINT;
            case 4:
                return MetaColumn.DataType.INTEGER;
            case 8:
                return MetaColumn.DataType.BIGINT;
            default:
                throw new RuntimeException(getName() + " (" + getFieldName() + "): Illegal field length: " + bufferLength); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else if (IQDBRTVFD.TYPE_API_ZONED.equals(type)) {
            return MetaColumn.DataType.NUMERIC;
        } else if (IQDBRTVFD.TYPE_API_PACKED.equals(type)) {
            return MetaColumn.DataType.DECIMAL;
        } else if (IQDBRTVFD.TYPE_API_CHAR.equals(type)) {
            if (getCcsid() == 65535) {
                if (isVaryingLength()) {
                    return MetaColumn.DataType.VARBINARY;
                } else {
                    return MetaColumn.DataType.BINARY;
                }
            } else {
                if (isVaryingLength()) {
                    return MetaColumn.DataType.VARCHAR;
                } else {
                    return MetaColumn.DataType.CHAR;
                }
            }
        } else if (IQDBRTVFD.TYPE_API_DATE.equals(type)) {
            return MetaColumn.DataType.DATE;
        } else if (IQDBRTVFD.TYPE_API_TIME.equals(type)) {
            return MetaColumn.DataType.TIME;
        } else if (IQDBRTVFD.TYPE_API_TIMESTAMP.equals(type)) {
            return MetaColumn.DataType.TIMESTAMP;
        } else if (IQDBRTVFD.TYPE_API_GRAPHIC.equals(type)) {
            if (isVaryingLength()) {
                return MetaColumn.DataType.VARGRAPHIC;
            } else {
                return MetaColumn.DataType.GRAPHIC;
            }
        } else if (IQDBRTVFD.TYPE_API_FLOAT.equals(type)) {
            int bufferLength = getBufferLength();
            switch (bufferLength) {
            case 4:
                return MetaColumn.DataType.REAL;
            case 8:
                return MetaColumn.DataType.DOUBLE;
            default:
                throw new RuntimeException(getName() + " (" + getFieldName() + "): Illegal field length: " + bufferLength); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else if (IQDBRTVFD.TYPE_API_LOB.equals(type)) {
            // TODO: fix it, return BLOB or CLOB
            return MetaColumn.DataType.LOB;
        } else if (IQDBRTVFD.TYPE_API_DATALINK.equals(type)) {
            return MetaColumn.DataType.DATALINK;
        } else {
            return MetaColumn.DataType.UNKNOWN;
        }
    }

    /**
     * Returns the use type of the field.
     * 
     * @return usage
     * @throws UnsupportedEncodingException
     */
    public String getUse() throws UnsupportedEncodingException {
        return getCharValue(USE).trim();
    }

    /**
     * Returns the length of the field.
     * 
     * @return length
     */
    public int getFieldLength() {
        return getInt4Value(LENGTH);
    }

    /**
     * Returns the number of decimal positions of a numeric field.
     * 
     * @return decimal positions
     */
    public int getDecimalPositions() {
        return getInt4Value(DECIMAL_POSITIONS);
    }

    /**
     * Returns the description of the field.
     * 
     * @return description
     * @throws UnsupportedEncodingException
     */
    public String getText() throws UnsupportedEncodingException {
        return getCharValue(TEXT);
    }

    /**
     * Returns the length of the field in the record format buffer.
     * 
     * @return buffer length
     */
    public int getBufferLength() {
        return getInt4Value(BUFFER_LENGTH);
    }

    /**
     * Returns the allocated length of a varying length field.
     * 
     * @return allocated length
     */
    public int getAllocatedLength() {
        return getInt4Value(ALLOCATED_LENGTH);
    }

    /**
     * Returns <code>true</code> for a varying length field, else
     * <code>false</code>.
     * 
     * @return <code>true</code> for varying fields
     */
    public boolean isVaryingLength() throws UnsupportedEncodingException {

        String isVarying = getCharValue(IS_VARYING_FIELD);
        if ("1".equals(isVarying)) { //$NON-NLS-1$
            return true;
        }

        return false;
    }

    /**
     * Returns <code>true</code> for a binary field, else <code>false</code>.
     * 
     * @return <code>true</code> for binary fields
     */
    public boolean isNullable() throws UnsupportedEncodingException {

        String isNullable = getCharValue(IS_NULLABLE_FIELD);
        if ("1".equals(isNullable)) { //$NON-NLS-1$
            return true;
        }

        return false;
    }

    /**
     * Returns <code>true</code> if the NULL value is allowed, else
     * <code>false</code>.
     * 
     * @return <code>true</code> for binary fields
     */
    public boolean isBinary() throws UnsupportedEncodingException {

        String isBinary = getCharValue(IS_BINARY_FIELD);
        if ("1".equals(isBinary)) { //$NON-NLS-1$
            return true;
        }

        return false;
    }

    /**
     * Returns <code>true</code> for a numeric field, else <code>false</code>.
     * 
     * @return <code>true</code> for numeric fields
     */
    private boolean isNumeric() throws Exception {

        if (IQDBRTVFD.TYPE_API_BINARY.equals(getType()) || IQDBRTVFD.TYPE_API_FLOAT.equals(getType()) || IQDBRTVFD.TYPE_API_PACKED.equals(getType())
            || IQDBRTVFD.TYPE_API_ZONED.equals(getType())) {
            return true;
        }

        return false;
    }

    /**
     * Returns <code>true</code> for a date, time or timestamp field, else
     * <code>false</code>.
     * 
     * @return <code>true</code> for date/time/timestamp fields
     */
    private boolean isDateTime() throws Exception {

        if (IQDBRTVFD.TYPE_API_DATE.equals(getType()) || IQDBRTVFD.TYPE_API_TIME.equals(getType()) || IQDBRTVFD.TYPE_API_TIMESTAMP.equals(getType())) {
            return true;
        }

        return false;
    }

    /**
     * Returns the CCSID of the field.
     * 
     * @return ccsid
     */
    public int getCcsid() {

        return getInt4Value(CCSID);
    }

    /**
     * Returns the date/time format of the field.
     * 
     * @return date or time format
     * @throws UnsupportedEncodingException
     */
    public String getDateTimeFormat() throws UnsupportedEncodingException {

        return getCharValue(DATE_TIME_FORMAT).trim();
    }

    /**
     * Returns the date/time separator of the field.
     * 
     * @return date or time format
     * @throws UnsupportedEncodingException
     */
    public String getDateTimeSeparator() throws UnsupportedEncodingException {

        return getCharValue(DATE_TIME_SEPARATOR).trim();
    }

    public MetaColumn createMetaColumn(int index) throws Exception {

        MetaColumn metaColumn = new MetaColumn(index);

        metaColumn.setFieldName(getFieldName());
        metaColumn.setType(getSqlDataType());
        metaColumn.setLength(getFieldLength());
        metaColumn.setDecimalPositions(getDecimalPositions());
        metaColumn.setBufferLength(getBufferLength());
        metaColumn.setCcsid(getCcsid());
        metaColumn.setVaryingLength(isVaryingLength());
        metaColumn.setBinary(isBinary());
        metaColumn.setNullable(isNullable());
        metaColumn.setDateTimeFormat(removeLeadingAsterisk(getDateTimeFormat()));
        metaColumn.setDateTimeSeparator(getDateTimeSeparator());
        metaColumn.setText(getText());

        metaColumn.setNumeric(isNumeric()); // transient
        metaColumn.setDateTime(isDateTime()); // transient

        return metaColumn;
    }

    private String removeLeadingAsterisk(String format) {

        if (format.startsWith("*")) { //$NON-NLS-1$
            return format.substring(1);
        }

        return format;
    }

    /**
     * Creates the RVFD0100 structure.
     */
    protected void createStructure() {

        addCharField(FIELD_NAME, 0, 10);
        addCharField(TYPE, 10, 1);
        addCharField(USE, 11, 1);
        addInt4Field(LENGTH, 12);
        addInt4Field(DECIMAL_POSITIONS, 16);
        addInt4Field(BUFFER_LENGTH, 20);
        addInt4Field(ALLOCATED_LENGTH, 24);
        addInt4Field(CCSID, 28);
        addCharField(IS_VARYING_FIELD, 32, 1);
        addCharField(IS_BINARY_FIELD, 33, 1);
        addCharField(IS_NULLABLE_FIELD, 34, 1);
        addCharField(RESERVED_1, 35, 1);
        addCharField(DATE_TIME_FORMAT, 36, 10);
        addCharField(DATE_TIME_SEPARATOR, 46, 10);
        addCharField(TEXT, 56, 50);
    }

    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder();

        try {

            buffer.append(getFieldName());
            buffer.append(": "); //$NON-NLS-1$
            buffer.append(getType());
            buffer.append("(");//$NON-NLS-1$
            buffer.append(getFieldLength());
            if (isNumeric()) {
                buffer.append(", ");//$NON-NLS-1$
                buffer.append(getDecimalPositions());
            } else if (isDateTime()) {
                buffer.append(", ");//$NON-NLS-1$
                buffer.append(getDateTimeFormat());
                buffer.append(", '");//$NON-NLS-1$
                buffer.append(getDateTimeSeparator());
                buffer.append("'");//$NON-NLS-1$
            }
            buffer.append(")");//$NON-NLS-1$

            return buffer.toString();

        } catch (Exception e) {
            return super.toString();
        }
    }
}
