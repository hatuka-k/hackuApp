package com.example.admin.hackuapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/08/04.
 */
public class PhoneBookContent {
    public static List<PhoneBookItem> ITEMS ;

    public static Map<String, PhoneBookItem> ITEM_MAP;

    private Activity action;


    public PhoneBookContent(Activity action){
        this.action = action;
        ITEMS = new ArrayList<PhoneBookItem>();
        ITEM_MAP = new HashMap<String, PhoneBookItem>();
        reset();
    }

    public String getDisplayName(String id){
        String name = "";
        Cursor c = action.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[]{ContactsContract.Contacts.DISPLAY_NAME},
                ContactsContract.Contacts._ID + "=" + id,
                null,
                null
        );
        if(c != null && c.getCount() > 0){
            try {
                c.moveToFirst();
                do {
                    name += c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)) + " ";
                } while (c.moveToNext());
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                c.close();
            }
            c.close();
        }

        return name;
    }

    public String getPhoneNumber(String id){
        String phones = "";
        Cursor c = action.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id,
                null,
                null
        );
        if(c != null && c.getCount() > 0){
            try {
                c.moveToFirst();
                do {
                    phones += c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)) + " ";
                } while (c.moveToNext());
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                c.close();
            }
            c.close();
        }

        return phones;
    }

    public String getEmailAddress(String id){
        String addresses = "";
        Cursor c = action.getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Email.ADDRESS},
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=" + id,
                null,
                null
        );
        if (c != null && c.getCount() > 0) {
            try {
                c.moveToFirst();
                do {
                    addresses += c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)) + " ";
                } while (c.moveToNext());
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                c.close();
            }
        }
        return addresses;
    }

    public String getCompany(String id){
        String result = null;

        String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] orgWhereParams = new String[]{id,
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};
        Cursor orgCur = action.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                null, orgWhere, orgWhereParams, null);
        if(orgCur.moveToFirst()) {
            result = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
            orgCur.close();
        }

        return result;
    }

    public String getDepart(String id){
        String result = null;

        String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] orgWhereParams = new String[]{id,
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};
        Cursor orgCur = action.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                null, orgWhere, orgWhereParams, null);
        if(orgCur.moveToFirst()) {
            result = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DEPARTMENT));
            orgCur.close();
        }

        return result;
    }

    public String getPosit(String id){
        String result = null;

        String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] orgWhereParams = new String[]{id,
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};
        Cursor orgCur = action.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                null, orgWhere, orgWhereParams, null);
        if(orgCur.moveToFirst()) {
            result = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
            orgCur.close();
        }

        return result;
    }

    public List<PhoneBookItem> getItems(){
        return ITEMS;
    }


    public Map<String, PhoneBookItem> ITEM_MAP(){
        return ITEM_MAP;
    }

    public static void addItem(PhoneBookItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static void reset(){
        ITEMS.clear();
        ITEM_MAP.clear();
    }

    public static class PhoneBookItem{
        public  String id;
        public  String name;
        public  String phoneNumber;
        public  String email;
        public  String company;
        public  String memo;

        public PhoneBookItem(String id, String name, String phoneNumber, String email, String company, String memo) {
            this.id = id;
            this.name = name;
            this.phoneNumber = phoneNumber;
            this.email = email;
            this.company = company;
            this.memo = memo;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
