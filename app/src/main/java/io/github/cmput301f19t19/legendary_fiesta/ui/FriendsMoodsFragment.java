package io.github.cmput301f19t19.legendary_fiesta.ui;

/*
 * FriendsMoodsFragment deals with displaying the user's following's MoodEvents in a interactive list,
 * launching a map to visualize the locations of the user's following's MoodEvents and allowing the
 * user to filter MoodEvents his/her following's MoodEvents.
 * FriendsMoodsFragment also allows the user to click a MoodEvent to see their following's
 * MoodEvent in detail.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import io.github.cmput301f19t19.legendary_fiesta.FirebaseHelper;
import io.github.cmput301f19t19.legendary_fiesta.MapActivity;
import io.github.cmput301f19t19.legendary_fiesta.Mood;
import io.github.cmput301f19t19.legendary_fiesta.MoodEvent;
import io.github.cmput301f19t19.legendary_fiesta.R;
import io.github.cmput301f19t19.legendary_fiesta.User;
import io.github.cmput301f19t19.legendary_fiesta.ui.CustomAdapter.MoodEventFriendsAdapter;
import io.github.cmput301f19t19.legendary_fiesta.ui.CustomAdapter.SpinnerArrayAdapter;
import io.github.cmput301f19t19.legendary_fiesta.ui.UIEventHandlers.FilterEventHandlers;

public class FriendsMoodsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    public static final String MOOD_EVENT_TAG = "MOOD_EVENT";
    public static final String FRIENDS_MOOD_UI_TEST_TAG = "FROM_UI_TESTS";
    private static final int MARKER_MODE = 72;
    private static final FirebaseHelper firebaseHelper = new FirebaseHelper(FirebaseApp.getInstance());

    // View elements
    private Activity mActivity;
    private View mView;
    private ListView moodList;
    private ArrayList<MoodEvent> moodDataList;
    private MoodEventFriendsAdapter moodArrayAdapter;

    private User user;
    private HashMap<String, String> friendUsernames;
    private Button mapButton;
    private SwipeRefreshLayout swipeRefreshLayout;

    //Filter spinner related variables
    private Spinner moodFilter;
    private Spinner filterSpinner;
    private @Mood.MoodType
    Integer chosenMoodType;
    private ArrayList<MoodEvent> filteredMoodList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_friends_moods, container, false);

        setUpFilterSpinner();

        user = requireActivity().getIntent().getParcelableExtra("USER_PROFILE");
        if (user == null) {
            Bundle receiveBundle = this.getArguments();
            assert receiveBundle != null;
            user = receiveBundle.getParcelable("USER_PROFILE");
        }

        friendUsernames = new HashMap<>();
        moodDataList = new ArrayList<>();

        if (getTag() != FRIENDS_MOOD_UI_TEST_TAG) {
            loadData();
        }

        moodList = mView.findViewById(R.id.mood_list_friends);

        moodFilter = mView.findViewById(R.id.filter_spinner_friends);

        // When an item in the spinner is selected
        moodFilter.setOnItemSelectedListener(this);

        moodList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Fragment replacement = new ViewPostFragment();
                Bundle args = new Bundle();
                args.putParcelable(MOOD_EVENT_TAG, moodDataList.get(i));
                replacement.setArguments(args);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                transaction.replace(R.id.nav_host_fragment, replacement);
                transaction.commit();
            }
        });

        // Map Button
        mapButton = mView.findViewById(R.id.show_on_map_button_friends);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapActivity.class);
                intent.putExtra("FRIEND_MODE", MARKER_MODE);

                if (moodFilter.getSelectedItemPosition() == Mood.MoodTypes.size()) {
                    // No filter selected
                    intent.putParcelableArrayListExtra("EVENTS", moodDataList);
                    intent.putStringArrayListExtra("FRIENDS", createNamesList(moodDataList));
                } else {
                    intent.putParcelableArrayListExtra("EVENTS", filteredMoodList);
                    intent.putStringArrayListExtra("FRIENDS", createNamesList(filteredMoodList));
                }
                startActivity(intent);
            }
        });

        swipeRefreshLayout = mView.findViewById(R.id.friends_moods_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return mView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Get reference to associated activity
        mActivity = (Activity) context;
    }

    public void loadData() {
        firebaseHelper.getAllUsers(new FirebaseHelper.FirebaseCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documents) {
                for (QueryDocumentSnapshot document : documents) {
                    friendUsernames.put(document.getId(), document.getString("username"));
                }
                moodArrayAdapter = new MoodEventFriendsAdapter(mActivity, moodDataList, friendUsernames);
                moodList.setAdapter(moodArrayAdapter);
                loadMoodData();
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("FeelsLog", "onFailure: ");
            }
        });
    }

    public void loadMoodData() {
        if (user.getFollowing().size() == 0) {
            return;
        }
        moodDataList.clear();
        firebaseHelper.getFriendsMoodEvents(user.getFollowing(), new FirebaseHelper.FirebaseCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                for (QueryDocumentSnapshot document : documentSnapshots) {
                    moodDataList.add(document.toObject(MoodEvent.class));
                }
                moodDataList.sort(Comparator.comparing(MoodEvent::getDate, Collections.reverseOrder()));
                moodArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("FeelsLog", "onFailure: ");
            }
        });
    }

    /*
     * This function is used in tests to add a mood event into the data list
     */
    public void AddMoodEvent(MoodEvent newMoodEvent) {
        moodDataList.add(newMoodEvent);
        moodArrayAdapter.notifyDataSetChanged();
    }

    /*
     * Used to set up the spinner, populate it with a string array from resource.xml
     */
    private void setUpFilterSpinner() {
        filterSpinner = mView.findViewById(R.id.filter_spinner_friends);

        /*
         * filterArray will contain a list of mood from Mood.moodtype enum
         */
        ArrayList<String> filterArray = new ArrayList<>();
        Mood.MoodTypes.forEach((k, v) -> filterArray.add(k));
        filterArray.add(getResources().getString(R.string.spinner_empty)); //filter_empty is "None"

        /*
         * convert ArrayList to array, so that it can be passed to SpinnerArrayAdapter
         */
        String[] filterObject = new String[filterArray.size()];
        filterObject = filterArray.toArray(filterObject);

        /*
        Create string ArrayAdapter that will be used for filterSpinner
         */
        ArrayAdapter<String> spinnerAdapter = new SpinnerArrayAdapter(mActivity, R.layout.spinner_item, filterObject);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        filterSpinner.setAdapter(spinnerAdapter);

        //set default selection to None
        int defaultIndex = filterArray.indexOf(getResources().getString(R.string.spinner_empty));
        filterSpinner.setSelection(defaultIndex);

        //assign filter selected listener
        filterSpinner.setOnItemSelectedListener(new FilterEventHandlers());
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        filteredMoodList = new ArrayList<>();
        chosenMoodType = -1;

        // Assign the correct value to chosenMoodType depending on what the user has selected
        switch (i) {
            case 0: //Scared
                chosenMoodType = Mood.SCARED;
                break;
            case 1: //Happy
                chosenMoodType = Mood.HAPPY;
                break;
            case 2: //Surprised
                chosenMoodType = Mood.SURPRISED;
                break;
            case 3: //Sad
                chosenMoodType = Mood.SAD;
                break;
            case 4: //Angry
                chosenMoodType = Mood.ANGRY;
                break;
            case 5: //Disgusted
                chosenMoodType = Mood.DISGUSTED;
                break;
            case 6: //Neutral
                chosenMoodType = Mood.NEUTRAL;
                break;
            default:   //None
                break;
        }

        // If chosenMoodType is a number between 0-6, filter!
        if (chosenMoodType != -1) {
            for (MoodEvent mood : moodDataList) {
                if (mood.getMoodType() == chosenMoodType) {
                    filteredMoodList.add(mood);
                }
            }
            // Update adapter and listview
            moodArrayAdapter = new MoodEventFriendsAdapter(mActivity, filteredMoodList, friendUsernames);
            moodList.setAdapter(moodArrayAdapter);
        } else {
            moodArrayAdapter = new MoodEventFriendsAdapter(mActivity, moodDataList, friendUsernames);
            moodList.setAdapter(moodArrayAdapter);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        moodArrayAdapter = new MoodEventFriendsAdapter(mActivity, moodDataList, friendUsernames);
        moodList.setAdapter(moodArrayAdapter);
    }

    /**
     * Returns an ArrayList of names that corresponds to the MoodEvents
     *
     * @param dataList ArrayList of MoodEvents
     * @return Returns an ArrayList of names
     */
    private ArrayList<String> createNamesList(ArrayList<MoodEvent> dataList) {
        ArrayList<String> names = new ArrayList<>();
        for (MoodEvent moodEvent : dataList) {
            names.add(friendUsernames.get(moodEvent.getUser()));
        }
        return names;
    }
}