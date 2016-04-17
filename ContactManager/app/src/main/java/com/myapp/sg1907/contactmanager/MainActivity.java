package com.myapp.sg1907.contactmanager;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    private Button backup, recover;
    private TextView contactNumber;
    private ListView contactList;
    private RadioGroup operators;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backup = (Button) findViewById(R.id.BackUp);
        backup.getBackground().setAlpha(192);
        recover = (Button) findViewById(R.id.recover);
        recover.getBackground().setAlpha(192);
        contactList = (ListView) findViewById(R.id.contactList);
        contactList.getBackground().setAlpha(192);
        contactNumber = (TextView) findViewById(R.id.contactNumber);
        operators = (RadioGroup) findViewById(R.id.operators);
        operators.getBackground().setAlpha(192);
        contactList.setOnItemClickListener(this);
    }

    public void classifyNumbers(View view) {
        ArrayList<Contact> aList = new ArrayList<>();
        ArrayList<Contact> operatorList = new ArrayList<>();
        int id = view.getId();
        aList = this.getContacts();

        char[] numberChar;

        switch (id) {
            // for Türkcell number prefixes have been assumed as 532, 533, 534, 535, 536, 537, 538, 539
            case R.id.turkcell:
                for (Contact contact : aList) {
                    numberChar = contact.getPhoneNumber().toCharArray();

                    if (numberChar[2] == '3') {
                        if(numberChar[3] == '0' || numberChar[3] == '1');
                        else{
                            operatorList.add(contact);
                        }
                    }
                }
                break;

            // for Vodafone number prefixes have been assumed as 542, 543, 544, 545
            case R.id.vodafone:
                for (Contact contact : aList) {
                    numberChar = contact.getPhoneNumber().toCharArray();
                    if (numberChar[2] == '4') {
                        if (numberChar[3] == '2' || numberChar[3] == '3'
                                || numberChar[3] == '4' || numberChar[3] == '5') {
                            operatorList.add(contact);
                        }
                    }
                }
                break;

            // for Türktelekom number prefixes have been assumed as 552, 553, 554, 555:
            case R.id.turktekekom:
                for (Contact contact : aList) {
                    numberChar = contact.getPhoneNumber().toCharArray();
                    if (numberChar[2] == '5') {
                        if (numberChar[3] == '2' || numberChar[3] == '3'
                                || numberChar[3] == '4' || numberChar[3] == '5') {
                            operatorList.add(contact);
                        }
                    }
                }
                break;
            case R.id.all:
                operatorList = aList;
        }

        contactNumber.setText(Integer.toString(operatorList.size()));

        MyAdapter<Contact> myAdapter = new MyAdapter<>(this, R.layout.list_layout, R.id.contactName, R.id.contactPhoneNumber, R.id.contactPhoto, operatorList);

        contactList.setAdapter(myAdapter);
    }

    public void backUpContacts(View view) throws IOException {
        ArrayList<Contact> aList;
        aList = this.getContacts();
        FileOutputStream fileos = null;
        int contactCounter = 0;

        try{
            fileos = openFileOutput("backup.xml", this.MODE_PRIVATE);

        }catch(FileNotFoundException e)
        {
            Log.e("FileNotFoundException", e.toString());
        }

        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(fileos, "UTF-8");
        serializer.startDocument(null, Boolean.valueOf(true));
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        serializer.startTag(null, "ContactBackUp");

        for (Contact contact: aList){
            serializer.startTag(null, "contact");
            serializer.attribute(null, "id", Integer.toString(contactCounter));
            serializer.startTag(null, "name");
            serializer.text(contact.getName());
            serializer.endTag(null, "name");
            serializer.startTag(null, "phonenumber");
            serializer.text(contact.getPhoneNumber());
            serializer.endTag(null, "phonenumber");
            serializer.startTag(null, "photobytes");
            if(contact.getPhoto() == null){ serializer.text(""); }
            else{ serializer.text(Base64.encodeToString(contact.getPhoto(), Base64.DEFAULT)); }
            serializer.endTag(null, "photobytes");
            serializer.endTag(null,"contact");
            contactCounter++;
        }

        serializer.endTag(null, "ContactBackUp");
        serializer.endDocument();
        serializer.flush();
        fileos.close();
        Toast.makeText(this, "Contact Backup Operation completed...", Toast.LENGTH_LONG).show();
    }

    public void recoverContacts(View view){
        String content = this.getContent("backup.xml");
        if(content == null){
            Toast.makeText(this, "You should take a back-up", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, "Contact Recover Operation started!\nPlease wait...", Toast.LENGTH_LONG).show();
            ArrayList<Contact> contacts = this.parseXMLContent(content);
            deleteContact();
            buildContact(contacts);
            Toast.makeText(this, "Contact Recover Operation completed!", Toast.LENGTH_LONG).show();
            contactNumber.setText(Integer.toString(contacts.size()));
            MyAdapter<Contact> myAdapter = new MyAdapter<>(this, R.layout.list_layout, R.id.contactName, R.id.contactPhoneNumber, R.id.contactPhoto, contacts);
            contactList.setAdapter(myAdapter);
        }
    }

    public ArrayList<Contact> getContacts(){
        ArrayList<Contact> contactData = new ArrayList<>();
        Contact contact;
        byte[] photo = null;

        Cursor contacts = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while ( contacts.moveToNext() )
        {
            String name = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String photoURI = contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

            // It gets the byte array of image which has specific uri.
            InputStream inputStream = null;
            try {
                if(photoURI != null) {
                    inputStream= getContentResolver().openInputStream(Uri.parse(photoURI));
                    photo = getBytes(inputStream);
                }else{
                    photo = null;
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            contact = new Contact(name, phoneNumber, photo);
            contactData.add(contact);
        }

        contacts.close();
        return contactData;
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int length = 0;

        while ( (length = inputStream.read(buffer)) != -1 ) {
            baoStream.write(buffer, 0, length);
        }

        return baoStream.toByteArray();
    }

    public void deleteContact(){
        Cursor contact = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        while(contact.moveToNext()){
            String lookupkey = contact.getString(contact.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupkey);
            getContentResolver().delete(uri, null, null);
        }

    }

    public void buildContact(ArrayList<Contact> contactList){
        String displayName = null, mobileNumber = null;
        byte[] photo = null;

        for(Contact contact : contactList){
            displayName = contact.getName();
            mobileNumber = contact.getPhoneNumber();
            photo = contact.getPhoto();

            ArrayList< ContentProviderOperation > cpo = new ArrayList < ContentProviderOperation > ();

            cpo.add(ContentProviderOperation
                    .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .build());

            if (displayName != null) {
                cpo.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                        .build());
            }

            if (mobileNumber != null) {
                cpo.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobileNumber)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build());
            }

            if(photo != null) {
                cpo.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Photo.DATA15, photo)
                        .build());
            }

            try { getContentResolver().applyBatch(ContactsContract.AUTHORITY, cpo); }
            catch (Exception e) { e.printStackTrace(); }
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Contact contact = (Contact) contactList.getItemAtPosition(position);
        Uri number = Uri.parse("tel:" + contact.getPhoneNumber());
        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
        startActivity(callIntent);
    }

    public String getContent(String fileName){
        FileInputStream fis = null;
        InputStreamReader isr = null;
        String content = null;

        try {
            fis = openFileInput(fileName);
            isr = new InputStreamReader(fis);
            char[] inputBuffer = new char[fis.available()];
            isr.read(inputBuffer);
            content = new String(inputBuffer);
            isr.close();
            fis.close();
        }catch (IOException e){
            Log.v("serdar", e.getMessage());
        }

        return content;
    }

    // converting the String data to XML format
    // so that the DOM parser understand it as an XML input.
    public ArrayList<Contact> parseXMLContent(String content) {
        NodeList itemNames = null;
        NodeList itemPhoneNumbers = null;
        NodeList itemPhoto = null;
        InputStream is = null;
        Contact contact;

        try{
            is = new ByteArrayInputStream(content.getBytes("UTF-8"));
        }catch (UnsupportedEncodingException e){
            Log.v("serdar", e.getMessage());
        }

        DocumentBuilderFactory dbf;
        DocumentBuilder db;
        Document dom;

        try{
            dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
            dom = db.parse(is);
            dom.getDocumentElement().normalize();

            itemNames = dom.getElementsByTagName("name");
            itemPhoneNumbers = dom.getElementsByTagName("phonenumber");
            itemPhoto = dom.getElementsByTagName("photobytes");

        }catch (ParserConfigurationException e){
            Log.v("serdar", e.getMessage());
        }catch (SAXException e){
            Log.v("serdar", e.getMessage());
        } catch (IOException e) {
            Log.v("serdar", e.getMessage());
        }

        ArrayList<Contact> itemList = new ArrayList<>();

        for (int i = 0; i < itemNames.getLength(); i++) {
            contact = new Contact("", "", null);
            contact.setName(itemNames.item(i).getTextContent());
            contact.setPhoneNumber(itemPhoneNumbers.item(i).getTextContent());

            if (itemPhoto.item(i).getTextContent() != "") {
                contact.setPhoto(Base64.decode(itemPhoto.item(i).getTextContent(), Base64.DEFAULT));
            }
            itemList.add(contact);
        }
        return itemList;
    }
}
