package com.example.admin.hackuapp;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.admin.hackuapp.dummy.DummyContent;

/**
 * A fragment representing a single BusinessCards detail screen.
 * This fragment is either contained in a {@link BusinessCardsListActivity}
 * in two-pane mode (on tablets) or a {@link BusinessCardsDetailActivity}
 * on handsets.
 */
public class BusinessCardsDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private PhoneBookContent.PhoneBookItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BusinessCardsDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = PhoneBookContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.name);
            }

            FloatingActionButton fab = (FloatingActionButton) activity.findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setClassName("com.example.admin.hackuapp", "com.example.admin.hackuapp.EditCard");
                    intent.putExtra("oldName",mItem.name);
                    intent.putExtra("oldPhone",mItem.phoneNumber);
                    intent.putExtra("oldEmail",mItem.email);
                    intent.putExtra("oldComp",mItem.company);
                    intent.putExtra("oldDep",mItem.depart);
                    intent.putExtra("oldPos",mItem.posit);

                    startActivity(intent);

                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.businesscards_detail, container, false);
        String text = "";

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            if(mItem.company != null) {
                text += "会社名: "+ mItem.company+"\n\n";

            }
            if(mItem.company != null) {
                text += "部署: "+ mItem.depart+"\n\n";

            }
            if(mItem.company != null) {
                text += "役職: "+ mItem.posit+"\n\n";

            }
            if(mItem.phoneNumber != null) {
                text += "電話番号: "+ mItem.phoneNumber+"\n\n";
            }
            if(mItem.email != null) {
                text += "メール: "+ mItem.email+"\n\n";
            }
            ((TextView) rootView.findViewById(R.id.businesscards_detail)).setText(text);
        }
        return rootView;
    }
}
