package com.example.admin.hackuapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AndroidException;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class EditCard extends AppCompatActivity {

    private EditText eName;
    private EditText ePhone;
    private EditText eEmail;
    private EditText eCompany;
    private EditText eDepart;
    private EditText ePosit;

    private DBAccesser dba;

    private String profileId;

    static final int PICK_CONTACT_REQUEST = 1;

    private String ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ID = "";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_card);

        requestDBPermission();

        dba = new DBAccesser(this);

        eName = (EditText)findViewById(R.id.editName);
        ePhone = (EditText)findViewById(R.id.editPhone);
        eEmail = (EditText)findViewById(R.id.editEmail);
        eCompany = (EditText)findViewById(R.id.editCompany);
        eDepart = (EditText)findViewById(R.id.editDepart);
        ePosit = (EditText)findViewById(R.id.editPosit);

        //値の受け取り
        Intent intent = getIntent();
        String oldName = intent.getStringExtra( "oldName" );
        String oldPhone = intent.getStringExtra( "oldPhone" );
        String oldEmail = intent.getStringExtra( "oldEmail" );
        String oldComp = intent.getStringExtra( "oldComp" );
        String oldDep = intent.getStringExtra( "oldDep" );
        String oldPos = intent.getStringExtra( "oldPos" );

        profileId = intent.getStringExtra( "profileId" );

        EditText input = (EditText)this.findViewById(R.id.editName);
        input.setText(oldName);
        input = (EditText)this.findViewById(R.id.editPhone);
        input.setText(oldPhone);
        input = (EditText)this.findViewById(R.id.editEmail);
        input.setText(oldEmail);
        input = (EditText)this.findViewById(R.id.editCompany);
        input.setText(oldComp);
        input = (EditText)this.findViewById(R.id.editDepart);
        input.setText(oldDep);
        input = (EditText)this.findViewById(R.id.editPosit);
        input.setText(oldPos);
    }

    public void bookRegistration(View view) throws AndroidException, OperationApplicationException {
        if (ID.length() == 0) {
            ID = addContact();
        }

        dba.put(new DBLine(profileId, ID, ""));

        System.out.println("INTENT_TO_LIST");
        Intent intent = new Intent();
        intent.setClassName("com.example.admin.hackuapp", "com.example.admin.hackuapp.BusinessCardsListActivity");
        startActivity(intent);

    }

    private String addContact() throws AndroidException, OperationApplicationException {
        ContentValues values = new ContentValues();
        Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);

        long rawContactId = ContentUris.parseId(rawContactUri);

        values.clear();
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, eName.getText().toString());
        getContentResolver().insert(ContactsContract.Data.CONTENT_URI, values);

        // 携帯の電話番号を登録
        Uri mobileUri = Uri.withAppendedPath(rawContactUri, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
        values.clear();
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        values.put(ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY, 1);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, ePhone.getText().toString());
        getContentResolver().insert(mobileUri, values);

        // 携帯のメアドを登録
        Uri emailUri = Uri.withAppendedPath(rawContactUri, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
        values.clear();
        values.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_MOBILE);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Email.DATA1, eEmail.getText().toString());
        getContentResolver().insert(emailUri, values);

        // 会社名を登録
        Uri companyUri = Uri.withAppendedPath(rawContactUri, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
        values.clear();
        values.put(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Organization.DATA, eCompany.getText().toString());
        getContentResolver().insert(companyUri, values);

        // 部署を登録
        Uri departUri = Uri.withAppendedPath(rawContactUri, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
        values.clear();
        values.put(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Organization.DEPARTMENT, eDepart.getText().toString());
        getContentResolver().insert(departUri, values);

        // 役職を登録
        Uri positUri = Uri.withAppendedPath(rawContactUri, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
        values.clear();
        values.put(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Organization.TITLE, ePosit.getText().toString());
        getContentResolver().insert(positUri, values);

        return String.valueOf(rawContactId);
    }

    private void requestDBPermission(){
        // Here, thisActivity is the current activity
        int MY_PERMISSIONS_REQUEST_WRITE_CONTACTS = 1;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_CONTACTS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_CONTACTS},
                        MY_PERMISSIONS_REQUEST_WRITE_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @SuppressLint("ShowToast")
    public void pickContact(View view) throws AndroidException, OperationApplicationException {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, PICK_CONTACT_REQUEST);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == PICK_CONTACT_REQUEST){
            if(resultCode == RESULT_OK){
                Uri contactUri = data.getData();
                toastInfo(contactUri);
            }
        }
    }

    private void toastInfo(Uri uri) {
        String[] projection = {ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            int idCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
            String id = cursor.getString(idCol);
            int dispNameCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            String dispName = cursor.getString(dispNameCol);
            int numberCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            String number = cursor.getString(numberCol);

            Toast.makeText(this, "ID:" + id + "\nDISPLAY_NAME:" + dispName + "\nNUMBER:" + number, Toast.LENGTH_LONG).show();
            this.ID = id;
            EditText input = (EditText)this.findViewById(R.id.editName);
            input.setText(getDisplayName(id));
            input = (EditText)this.findViewById(R.id.editPhone);
            input.setText(getPhoneNumber(id));
            input = (EditText)this.findViewById(R.id.editEmail);
            input.setText(getEmailAddress(id));
            input = (EditText)this.findViewById(R.id.editCompany);
            input.setText(getCompany(id));
            input = (EditText)this.findViewById(R.id.editDepart);
            input.setText(getDepart(id));
            input = (EditText)this.findViewById(R.id.editPosit);
            input.setText(getPosit(id));

            cursor.close();
        }
    }

    private String getDisplayName(String id){
        String name = "";
        Cursor c = getContentResolver().query(
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

    private String getPhoneNumber(String id){
        String phones = "";
        Cursor c = getContentResolver().query(
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

    private String getEmailAddress(String id){
        String addresses = "";
        Cursor c = getContentResolver().query(
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

    private String getCompany(String id){
        String result = null;

        String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] orgWhereParams = new String[]{id,
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};
        Cursor orgCur = getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                null, orgWhere, orgWhereParams, null);
        if(orgCur.moveToFirst()) {
            result = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
            orgCur.close();
        }

        return result;
    }

    private String getDepart(String id){
        String result = null;

        String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] orgWhereParams = new String[]{id,
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};
        Cursor orgCur = getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                null, orgWhere, orgWhereParams, null);
        if(orgCur.moveToFirst()) {
            result = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DEPARTMENT));
            orgCur.close();
        }

        return result;
    }

    private String getPosit(String id){
        String result = null;

        String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] orgWhereParams = new String[]{id,
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};
        Cursor orgCur = getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                null, orgWhere, orgWhereParams, null);
        if(orgCur.moveToFirst()) {
            result = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
            orgCur.close();
        }

        return result;
    }
}
