package com.codebrew.whrzat.ui.settings

import android.content.ContentResolver
import android.content.Context
import android.os.AsyncTask
import android.provider.ContactsContract
import android.text.TextUtils
import android.util.Log
import com.codebrew.whrzat.util.Constants
import com.codebrew.whrzat.util.Prefs

import java.util.ArrayList


class FetchContacts(var mContext: Context, private val resolver: ContentResolver, private val listener: FetchContactsListener?)
    : AsyncTask<Void, Void, ArrayList<String>>() {

    private val TAG = "FetchContacts"

    override fun doInBackground(vararg params: Void): ArrayList<String>? {
        try {
            /*val contacts = ArrayList<Contact>()*/
            val contacts = ArrayList<String>()

            val cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, PROJECTION, FILTER, null, ORDER)
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Get the contact's information
                    val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                    val name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME))
                    val hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                    // Get the user's phone number
                    var phone: String = ""
                    if (hasPhone > 0) {
                        val cp = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id), null)
                        if (cp != null && cp.moveToFirst()) {
                            phone = cp.getString(cp.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            cp.close()
                        }
                    }

                    val code = Prefs.with(mContext).getString(Constants.CODE, "")
                    // If the user user has a phone then add it to contacts
                    if (!TextUtils.isEmpty(phone)) {
                        //val contact = Contact(phone)
                        phone = phone.replace(" ", "").replace(code, "").replace("(", "").replace(")", "").replace("-", "")
                        contacts.add(phone)
                    }

                } while (cursor.moveToNext())

                // Clean up cursor
                cursor.close()
            }

            return contacts
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }

    override fun onPostExecute(contacts: ArrayList<String>?) {
        if (listener != null) {
            if (contacts != null) {
                for(i in 0..contacts.size-1) {
                    Log.d(TAG, "onPostExecute: contacts list:-  ${contacts[i]}")
                }
                listener.onContactsReceived(contacts)
            } else {
                listener.onErrorFetchingContacts()
            }
        }
    }

    interface FetchContactsListener {
        fun onContactsReceived(contacts: List<String>)

        fun onErrorFetchingContacts()
    }

    companion object {
        private val DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        private val FILTER = DISPLAY_NAME + " NOT LIKE '%@%'"
        private val ORDER = String.format("%1\$s COLLATE NOCASE", DISPLAY_NAME)
        private val PROJECTION = arrayOf(ContactsContract.Contacts._ID, DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER)
    }
}