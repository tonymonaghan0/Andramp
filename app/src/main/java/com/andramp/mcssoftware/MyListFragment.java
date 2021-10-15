package com.andramp.mcssoftware;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;


public class MyListFragment extends ListFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String[] values = new String[] { "List", "iPhone", "WindowsMobile",
                "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
                "Linux", "OS/2" };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(),
                android.R.layout.simple_list_item_1, values);

        setListAdapter(adapter);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {

        Toast.makeText(getActivity(), "ID " + id + " Position " + position + " "+ l.getItemAtPosition(position) + " Clicked!"
                        , Toast.LENGTH_SHORT).show();


/*        //Shows the setting fragment
        Fragment settingsFragment = new SettingsFragment();
        FragmentManager manager = getParentFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.flFragment, settingsFragment);
        ft.addToBackStack(null);
        ft.commit();*/


    }
}