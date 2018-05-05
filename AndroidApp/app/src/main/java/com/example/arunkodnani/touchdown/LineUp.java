package com.example.arunkodnani.touchdown;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

/* Fragment used as page 1 */
public class LineUp extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_line_up, container, false);

        ListView lv = (ListView)rootView.findViewById(R.id.lineup);

        // Instanciating an array list (you don't need to do this,
        // you already have yours).



        List<String> al = new ArrayList<>();
        al.add("Marcel vs Winslow");
        al.add("Karan vs Akhil");
        al.add("Marcel vs Winslow");
        al.add("Karan vs Akhil");

        // This is the array adapter, it takes the context of the activity as a
        // first parameter, the type of list view as a second parameter and your
        // array as a third parameter.
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, al);

        lv.setAdapter(arrayAdapter);
        return rootView;
    }
}
