package com.oopsappgroup.praktika.Fragments;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.oopsappgroup.praktika.Dialogs.NewLazyCatDialog;
import com.oopsappgroup.praktika.Model.Cat;
import com.oopsappgroup.praktika.R;
import com.oopsappgroup.praktika.RecyclerView.CatHolder;

import java.util.HashMap;
import java.util.Map;

public class LazyCatsDataBaseFragment extends Fragment {

    //fab
    FloatingActionButton fab;

    //Search field
    EditText searchField;
    ImageButton searchButton;

    //Recycler view
    private FirebaseRecyclerAdapter<Cat, CatHolder> mAdapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    //Database
    private DatabaseReference mDatabase;
    private FirebaseUser user;

    public LazyCatsDataBaseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(
                R.layout.fragment_base,container,
                false);

        //finding elements
        fab = (FloatingActionButton)rootView.findViewById(R.id.fabLCDB);
        recyclerView = (RecyclerView)rootView.findViewById(R.id.rvLCDB);
        searchField = (EditText)rootView.findViewById(R.id.etSearchField);
        searchButton = (ImageButton)rootView.findViewById(R.id.ibSearchButton);

        //Initialize database ref
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        //Setting recycler view's layout manager
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        setRecyclerView(null);

        //Set click listeners
        fab.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 NewLazyCatDialog dialog = new NewLazyCatDialog();
                 dialog.newInstance(LazyCatsDataBaseFragment.this, null, null, 0);
                 dialog.show(getActivity().getFragmentManager(), "NewLazyCat");
                 dialog.setTargetFragment(dialog, 0);
             }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!searchField.getText().toString().equals("")) {
                    String searchText = searchField.getText().toString();
                    setRecyclerView(searchText);
                }else {
                    setRecyclerView(null);
                }
            }
        });



        return rootView;
    }

    public void setRecyclerView(String catName){

        Query catsQuery;

        if (catName == null)
            catsQuery = getQuery(mDatabase);
        else
            catsQuery = mDatabase.child("cats").orderByChild("name").equalTo(catName);

        mAdapter = new FirebaseRecyclerAdapter<Cat, CatHolder>(Cat.class, R.layout.database_item,
                CatHolder.class, catsQuery) {
            @Override
            protected void populateViewHolder(final CatHolder viewHolder, final Cat model, final int position) {
                final DatabaseReference postRef = getRef(position);

                // Set click listener for the whole post view
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //For list click
                    }
                });

                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToCat(LazyCatsDataBaseFragment.this, model, new View.OnClickListener() {
                    @Override
                    public void onClick(View menuView) {
                        // Need to write to both places the post is stored
                        DatabaseReference globalPostRef = mDatabase.child("posts").child(postRef.getKey());
                        DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());

                        // Run two transactions
                        onMenuClicked(globalPostRef);
                        onMenuClicked(userPostRef);

                        ImageButton menu = viewHolder.menu;
                        final String catName = viewHolder.name.getText().toString();
                        final String photo = model.photo;

                        PopupMenu popupMenu = new PopupMenu(menuView.getContext(), menu);

                        getActivity().getMenuInflater().inflate(R.menu.recycler_view_context_menu, popupMenu.getMenu());

                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()){

                                    case R.id.context_item_edit:

                                        NewLazyCatDialog dialog = new NewLazyCatDialog();
                                        dialog.newInstance(LazyCatsDataBaseFragment.this, catName, photo, position);
                                        dialog.show(getActivity().getFragmentManager(), "NewLazyCat");
                                        dialog.setTargetFragment(dialog, 0);

                                        return true;
                                    case R.id.context_item_delete:

                                        removeItem(catName);

                                        return true;
                                    default:
                                        return false;

                                }
                            }
                        });

                        popupMenu.show();

                    }
                });
            }
        };

        recyclerView.setAdapter(mAdapter);

    }

    private void onMenuClicked(DatabaseReference postRef){
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed

            }
        });
    }

    public void addItem(final String catName, final String photo){
        final String uid = user.getUid();

        mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String key = mDatabase.child("cats").push().getKey();
                Cat cat = new Cat(uid, catName, photo);

                Map<String, String> catValues = cat.toMap();

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/cats/" + key, catValues);

                mDatabase.updateChildren(childUpdates);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), R.string.something_went_wrong, Toast.LENGTH_LONG).show();
                FirebaseCrash.log("Creating recyclerView error");
            }
        });
    }

    public void removeItem(String catName){

        Query query = mDatabase.child("cats").orderByChild("name").equalTo(catName);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void updateItem(int adapterPosition, final String newName, String newPhoto){

        mAdapter.getRef(adapterPosition).child("name").setValue(newName);

    }

    private Query getQuery(DatabaseReference database){
        return database.child("cats");
    }

}
