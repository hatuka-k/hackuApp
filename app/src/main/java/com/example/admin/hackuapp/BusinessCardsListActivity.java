package com.example.admin.hackuapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.example.admin.hackuapp.dummy.DummyContent;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of BusinessCards. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link BusinessCardsDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class BusinessCardsListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    public static final int NEW_ITEM = 0;
    private PhoneBookContent pbContent;

    private int MY_PERMISSIONS_REQUEST = 1;

    private DBAccesser dba;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_businesscards_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getTitle());
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClassName("com.example.admin.hackuapp", "com.example.admin.hackuapp.Recording");
                    startActivity(intent);
                }
            });
        }

        checkDBPermission();

        if (findViewById(R.id.businesscards_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // メニューの要素を追加して取得
        MenuItem actionItem = menu.add(0, NEW_ITEM, 0, "Action Button Input Icon");
        // アイコンを設定
        actionItem.setIcon(android.R.drawable.ic_input_add);

        // SHOW_AS_ACTION_ALWAYS:常に表示
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        intent.setClassName("com.example.admin.hackuapp", "com.example.admin.hackuapp.Registation");
        startActivity(intent);
        return true;
    }
    */

    private void checkDBPermission(){
        // Here, thisActivity is the current activity
        boolean success = true;

        List<String> permissions = new ArrayList<String>();
        int i = 0;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
            } else {
                permissions.add(Manifest.permission.READ_CONTACTS);
                success = false;
            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else{
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                success = false;
            }
        }

        if(success) {
            dba = new DBAccesser(this);
            pbContent = new PhoneBookContent(this);

            View recyclerView = findViewById(R.id.businesscards_list);
            assert recyclerView != null;

            setupRecyclerView((RecyclerView) recyclerView);
        } else {
            ActivityCompat.requestPermissions(this,
                    (String[])permissions.toArray(new String[0]),
                    MY_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if(requestCode != MY_PERMISSIONS_REQUEST){
            return;
        }

        boolean success = true;

        for(int i = 0; i < permissions.length; i++){
            if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
            }else{
                success = false;
            }
        }
        if(success) {
            pbContent = new PhoneBookContent(this);
            dba = new DBAccesser(this);

            View recyclerView = findViewById(R.id.businesscards_list);
            assert recyclerView != null;

            setupRecyclerView((RecyclerView) recyclerView);
        }
    }


    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        DBLine[] dbLines = dba.getAll();
        if(dbLines != null) {
            for(DBLine row : dba.getAll()) {
                pbContent.addItem(new PhoneBookContent.PhoneBookItem(
                        row.BOOK_ID,
                        pbContent.getDisplayName(row.BOOK_ID),
                        pbContent.getPhoneNumber(row.BOOK_ID),
                        pbContent.getEmailAddress(row.BOOK_ID),
                        pbContent.getCompany(row.BOOK_ID),
                        row.MEMO

                ));
            }
        }
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(pbContent.getItems()));
    }



    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<PhoneBookContent.PhoneBookItem> mValues;

        public SimpleItemRecyclerViewAdapter(List<PhoneBookContent.PhoneBookItem> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.businesscards_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position).name);
            holder.mContentView.setText(mValues.get(position).company);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(BusinessCardsDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        BusinessCardsDetailFragment fragment = new BusinessCardsDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.businesscards_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, BusinessCardsDetailActivity.class);
                        intent.putExtra(BusinessCardsDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        System.out.print(BusinessCardsDetailFragment.ARG_ITEM_ID + " " + holder.mItem.id);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public PhoneBookContent.PhoneBookItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.name);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
