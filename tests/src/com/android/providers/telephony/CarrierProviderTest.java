/*
 * Copyright (C) 2017 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.providers.telephony;

import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.telephony.TelephonyManager;
import android.test.mock.MockContentResolver;
import android.test.mock.MockContext;
import android.text.TextUtils;
import android.util.Log;

import androidx.test.filters.SmallTest;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Tests for testing CRUD operations of CarrierProvider.
 * Uses TelephonyProviderTestable to set up in-memory database
 *
 * Build, install and run the tests by running the commands below:
 *     runtest --path <dir or file>
 *     runtest --path <dir or file> --test-method <testMethodName>
 *     e.g.)
 *         runtest --path tests/src/com/android/providers/telephony/CarrierProviderTest.java \
 *                 --test-method testInsertCarriers
 */
public class CarrierProviderTest extends TestCase {

    private static final String TAG = "CarrierProviderTest";

    private MockContextWithProvider mContext;
    private MockContentResolver mContentResolver;
    private CarrierProviderTestable mCarrierProviderTestable;

    public static final int test_type = 1;
    public static final String test_mnc = "MNC001";
    public static final String test_mnc2 = "MNC002";
    public static final String test_mcc = "MCC005";
    public static final String test_key1 = "PUBKEY1";
    public static final String test_key2 = "PUBKEY2";
    public static final String  test_key_identifier_data = "key_identifier1";
    public static final long  test_key_expiration = 1496795015L;
    public static final int TEST_CARRIER_ID_1 = 1;
    public static final int TEST_CARRIER_ID_2 = 2;

    /**
     * This is used to give the CarrierProviderTest a mocked context which takes a
     * CarrierProvider and attaches it to the ContentResolver.
     */
    private class MockContextWithProvider extends MockContext {
        private final MockContentResolver mResolver;

        public MockContextWithProvider(CarrierProvider carrierProvider) {
            mResolver = new MockContentResolver();

            ProviderInfo providerInfo = new ProviderInfo();
            providerInfo.authority = CarrierProvider.PROVIDER_NAME;

            // Add context to given telephonyProvider
            carrierProvider.attachInfoForTesting(this, providerInfo);
            Log.d(TAG, "MockContextWithProvider: carrierProvider.getContext(): "
                    + carrierProvider.getContext());

            // Add given telephonyProvider to mResolver, so that mResolver can send queries
            // to the provider.
            mResolver.addProvider(CarrierProvider.PROVIDER_NAME, carrierProvider);
            Log.d(TAG, "MockContextWithProvider: Add carrierProvider to mResolver");
        }

        @Override
        public Object getSystemService(String name) {
            Log.d(TAG, "getSystemService: returning null");
            return null;
        }

        @Override
        public Resources getResources() {
            Log.d(TAG, "getResources: returning null");
            return null;
        }

        @Override
        public MockContentResolver getContentResolver() {
            return mResolver;
        }

        // Gives permission to write to the APN table within the MockContext
        @Override
        public int checkCallingOrSelfPermission(String permission) {
            if (TextUtils.equals(permission, "android.permission.WRITE_APN_SETTINGS")) {
                Log.d(TAG, "checkCallingOrSelfPermission: permission=" + permission
                        + ", returning PackageManager.PERMISSION_GRANTED");
                return PackageManager.PERMISSION_GRANTED;
            } else {
                Log.d(TAG, "checkCallingOrSelfPermission: permission=" + permission
                        + ", returning PackageManager.PERMISSION_DENIED");
                return PackageManager.PERMISSION_DENIED;
            }
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mCarrierProviderTestable = new CarrierProviderTestable();
        mContext = new MockContextWithProvider(mCarrierProviderTestable);
        mContentResolver = (MockContentResolver) mContext.getContentResolver();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mCarrierProviderTestable.closeDatabase();
    }

    /**
     * Test inserting values in carrier key table.
     */
    @Test
    @SmallTest
    public void testInsertCertificates() {
        int count = -1;
        ContentValues contentValues = new ContentValues();
        contentValues.put(CarrierDatabaseHelper.KEY_TYPE, test_type);
        contentValues.put(CarrierDatabaseHelper.MCC, test_mcc);
        contentValues.put(CarrierDatabaseHelper.MNC, test_mnc);
        contentValues.put(CarrierDatabaseHelper.CARRIER_ID, TEST_CARRIER_ID_1);
        contentValues.put(CarrierDatabaseHelper.KEY_IDENTIFIER, test_key_identifier_data);
        contentValues.put(CarrierDatabaseHelper.PUBLIC_KEY, test_key1.getBytes());
        contentValues.put(CarrierDatabaseHelper.EXPIRATION_TIME, test_key_expiration);

        try {
            mContentResolver.insert(CarrierProvider.CONTENT_URI, contentValues);
        } catch (Exception e) {
            Log.d(TAG, "Error inserting certificates:" + e);
        }
        try {
            Cursor countCursor = mContentResolver.query(CarrierProvider.CONTENT_URI,
                    new String[]{"count(*) AS count"},
                    null,
                    null,
                    null);
            countCursor.moveToFirst();
            count = countCursor.getInt(0);
        } catch (Exception e) {
            Log.d(TAG, "Exception in getting count:" + e);
        }
        assertEquals(1, count);
    }

    /**
     * Test update & query.
     */
    @Test
    @SmallTest
    public void testUpdateCertificates() {
        String key = null;
        ContentValues contentValues = new ContentValues();
        contentValues.put(CarrierDatabaseHelper.KEY_TYPE, test_type);
        contentValues.put(CarrierDatabaseHelper.MCC, test_mcc);
        contentValues.put(CarrierDatabaseHelper.MNC, test_mnc);
        contentValues.put(CarrierDatabaseHelper.CARRIER_ID, TEST_CARRIER_ID_1);
        contentValues.put(CarrierDatabaseHelper.KEY_IDENTIFIER, test_key_identifier_data);
        contentValues.put(CarrierDatabaseHelper.PUBLIC_KEY, test_key1.getBytes());
        contentValues.put(CarrierDatabaseHelper.EXPIRATION_TIME, test_key_expiration);

        try {
            mContentResolver.insert(CarrierProvider.CONTENT_URI, contentValues);
        } catch (Exception e) {
            Log.d(TAG, "Error inserting certificates:" + e);
        }

        try {
            ContentValues updatedValues = new ContentValues();
            updatedValues.put(CarrierDatabaseHelper.PUBLIC_KEY, test_key2);
            mContentResolver.update(CarrierProvider.CONTENT_URI, updatedValues,
                    "mcc=? and mnc=? and carrier_id=? and key_type=?",
                    new String[]{test_mcc, test_mnc, Integer.toString(TEST_CARRIER_ID_1),
                            String.valueOf(test_type)});
        } catch (Exception e) {
            Log.d(TAG, "Error updating values:" + e);
        }

        try {
            String[] columns ={CarrierDatabaseHelper.PUBLIC_KEY};
            Cursor findEntry = mContentResolver.query(CarrierProvider.CONTENT_URI, columns,
                    "mcc=? and mnc=? and carrier_id=? and key_type=?",
                    new String[]{test_mcc, test_mnc, Integer.toString(TEST_CARRIER_ID_1),
                            String.valueOf(test_type)}, null);
            findEntry.moveToFirst();
            key = findEntry.getString(0);
        } catch (Exception e) {
            Log.d(TAG, "Query failed:" + e);
        }
        assertEquals(key, test_key2);
    }

    /**
     * Test inserting multiple certs
     */
    @Test
    @SmallTest
    public void testMultipleCertificates() {
        int count = -1;
        ContentValues contentValues = new ContentValues();
        contentValues.put(CarrierDatabaseHelper.KEY_TYPE, test_type);
        contentValues.put(CarrierDatabaseHelper.MCC, test_mcc);
        contentValues.put(CarrierDatabaseHelper.MNC, test_mnc);
        contentValues.put(CarrierDatabaseHelper.CARRIER_ID, TEST_CARRIER_ID_1);
        contentValues.put(CarrierDatabaseHelper.KEY_IDENTIFIER, test_key_identifier_data);
        contentValues.put(CarrierDatabaseHelper.PUBLIC_KEY, test_key1.getBytes());

        ContentValues contentValuesNew = new ContentValues();
        contentValuesNew.put(CarrierDatabaseHelper.KEY_TYPE, test_type);
        contentValuesNew.put(CarrierDatabaseHelper.MCC, test_mcc);
        contentValuesNew.put(CarrierDatabaseHelper.MNC, test_mnc2);
        contentValues.put(CarrierDatabaseHelper.CARRIER_ID, TEST_CARRIER_ID_1);
        contentValues.put(CarrierDatabaseHelper.KEY_IDENTIFIER, test_key_identifier_data);
        contentValuesNew.put(CarrierDatabaseHelper.PUBLIC_KEY, test_key2.getBytes());

        try {
            mContentResolver.insert(CarrierProvider.CONTENT_URI, contentValues);
            mContentResolver.insert(CarrierProvider.CONTENT_URI, contentValuesNew);
        } catch (Exception e) {
            System.out.println("Error inserting certificates:: " + e);
        }

        try {
            Cursor countCursor = mContentResolver.query(CarrierProvider.CONTENT_URI,
                    new String[]{"count(*) AS count"},
                    null,
                    null,
                    null);
            countCursor.moveToFirst();
            count = countCursor.getInt(0);
        } catch (Exception e) {
            Log.d(TAG, "Exception in getting count:" + e);
        }
        assertEquals(2, count);
    }

    /**
     * Test inserting cert with same MCC and MNC but with diff carrier ID
     */
    @Test
    @SmallTest
    public void testMnoandMvnoCertificates() {
        int count = -1;
        ContentValues contentValues = new ContentValues();
        contentValues.put(CarrierDatabaseHelper.KEY_TYPE, test_type);
        contentValues.put(CarrierDatabaseHelper.MCC, test_mcc);
        contentValues.put(CarrierDatabaseHelper.MNC, test_mnc);
        contentValues.put(CarrierDatabaseHelper.CARRIER_ID, TEST_CARRIER_ID_1);
        contentValues.put(CarrierDatabaseHelper.KEY_IDENTIFIER, test_key_identifier_data);
        contentValues.put(CarrierDatabaseHelper.PUBLIC_KEY, test_key1.getBytes());

        ContentValues contentValuesNew = new ContentValues();
        contentValuesNew.put(CarrierDatabaseHelper.KEY_TYPE, test_type);
        contentValuesNew.put(CarrierDatabaseHelper.MCC, test_mcc);
        contentValuesNew.put(CarrierDatabaseHelper.MNC, test_mnc);
        contentValuesNew.put(CarrierDatabaseHelper.CARRIER_ID, TEST_CARRIER_ID_2);
        contentValuesNew.put(CarrierDatabaseHelper.KEY_IDENTIFIER, test_key_identifier_data);
        contentValuesNew.put(CarrierDatabaseHelper.PUBLIC_KEY, test_key1.getBytes());

        try {
            mContentResolver.insert(CarrierProvider.CONTENT_URI, contentValues);
            mContentResolver.insert(CarrierProvider.CONTENT_URI, contentValuesNew);
        } catch (Exception e) {
            System.out.println("Error inserting certificates:: " + e);
        }

        try {
            Cursor countCursor = mContentResolver.query(CarrierProvider.CONTENT_URI,
                    new String[]{"count(*) AS count"},
                    null,
                    null,
                    null);
            countCursor.moveToFirst();
            count = countCursor.getInt(0);
        } catch (Exception e) {
            Log.d(TAG, "Exception in getting count:" + e);
        }
        assertEquals(2, count);
    }

    /**
     * once upgrade to version 3, carrierId = -1
     * After upgrade, it triggers for new download with correct carrierId
     * This test case will test writing the new entry, even old entry for same
     * operator is already existed
     */
    @Test
    @SmallTest
    public void testOldAndNewDBEntries() {
        int count = -1;
        ContentValues contentValues = new ContentValues();
        contentValues.put(CarrierDatabaseHelper.KEY_TYPE, test_type);
        contentValues.put(CarrierDatabaseHelper.MCC, test_mcc);
        contentValues.put(CarrierDatabaseHelper.MNC, test_mnc);
        contentValues.put(CarrierDatabaseHelper.CARRIER_ID, TelephonyManager.UNKNOWN_CARRIER_ID);
        contentValues.put(CarrierDatabaseHelper.KEY_IDENTIFIER, test_key_identifier_data);
        contentValues.put(CarrierDatabaseHelper.PUBLIC_KEY, test_key1.getBytes());

        ContentValues contentValuesNew = new ContentValues();
        contentValuesNew.put(CarrierDatabaseHelper.KEY_TYPE, test_type);
        contentValuesNew.put(CarrierDatabaseHelper.MCC, test_mcc);
        contentValuesNew.put(CarrierDatabaseHelper.MNC, test_mnc);
        contentValuesNew.put(CarrierDatabaseHelper.CARRIER_ID, TEST_CARRIER_ID_2);
        contentValuesNew.put(CarrierDatabaseHelper.KEY_IDENTIFIER, test_key_identifier_data);
        contentValuesNew.put(CarrierDatabaseHelper.PUBLIC_KEY, test_key1.getBytes());

        try {
            mContentResolver.insert(CarrierProvider.CONTENT_URI, contentValues);
            mContentResolver.insert(CarrierProvider.CONTENT_URI, contentValuesNew);
        } catch (Exception e) {
            System.out.println("Error inserting certificates:: " + e);
        }

        try {
            Cursor countCursor = mContentResolver.query(CarrierProvider.CONTENT_URI,
                    new String[]{"count(*) AS count"},
                    null,
                    null,
                    null);
            countCursor.moveToFirst();
            count = countCursor.getInt(0);
        } catch (Exception e) {
            Log.d(TAG, "Exception in getting count:" + e);
        }
        assertEquals(2, count);
    }

    /**
     * Test inserting duplicate values in carrier key table. Ensure that a SQLException is thrown.
     */
    @Test(expected = SQLException.class)
    public void testDuplicateFailure() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CarrierDatabaseHelper.KEY_TYPE, test_type);
        contentValues.put(CarrierDatabaseHelper.MCC, test_mcc);
        contentValues.put(CarrierDatabaseHelper.MNC, test_mnc);
        contentValues.put(CarrierDatabaseHelper.CARRIER_ID, TEST_CARRIER_ID_1);
        contentValues.put(CarrierDatabaseHelper.PUBLIC_KEY, test_key1.getBytes());

        try {
            mContentResolver.insert(CarrierProvider.CONTENT_URI, contentValues);
        } catch (Exception e) {
            Log.d(TAG, "Error inserting certificates:: " + e);
        }
        try {
            mContentResolver.insert(CarrierProvider.CONTENT_URI, contentValues);
        } catch (Exception e) {
            Log.d(TAG, "Error inserting certificates:: " + e);
        }
    }

    /**
     * Test delete.
     */
    @Test
    @SmallTest
    public void testDelete() {
        int numRowsDeleted = -1;
        ContentValues contentValues = new ContentValues();
        contentValues.put(CarrierDatabaseHelper.KEY_TYPE, test_type);
        contentValues.put(CarrierDatabaseHelper.MCC, test_mcc);
        contentValues.put(CarrierDatabaseHelper.MNC, test_mnc);
        contentValues.put(CarrierDatabaseHelper.CARRIER_ID, TEST_CARRIER_ID_1);
        contentValues.put(CarrierDatabaseHelper.KEY_IDENTIFIER, test_key_identifier_data);
        contentValues.put(CarrierDatabaseHelper.PUBLIC_KEY, test_key1.getBytes());
        contentValues.put(CarrierDatabaseHelper.EXPIRATION_TIME, test_key_expiration);

        try {
            mContentResolver.insert(CarrierProvider.CONTENT_URI, contentValues);
        } catch (Exception e) {
            Log.d(TAG, "Error inserting certificates:" + e);
        }

        try {
            String whereClause = "mcc=? and mnc=? and carrier_id=?";
            String[] whereArgs = new String[]{test_mcc, test_mnc, Integer.toString(
                    TEST_CARRIER_ID_1)};
            numRowsDeleted = mContentResolver.delete(CarrierProvider.CONTENT_URI, whereClause,
                    whereArgs);
        } catch (Exception e) {
            Log.d(TAG, "Error updating values:" + e);
        }
        assertEquals(numRowsDeleted, 1);
    }

}
