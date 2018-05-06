package com.example.arunkodnani.touchdown;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/* Fragment used as page 1 */
public class MatchFacts extends Fragment {
    public static String id;
    static String charset = java.nio.charset.StandardCharsets.UTF_8.name();
    static List<String> al = new ArrayList<String>();
    XmlPullParserFactory xmlFactoryObject;
    XmlPullParser myparser;
    ListView lv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_match_facts, container, false);
        al.clear();
        lv = (ListView)rootView.findViewById(R.id.matchfacts);

        // Instanciating an array list (you don't need to do this,
        // you already have yours).


        String id=TeamDetails.id;
        Toast.makeText(getActivity(),"In MatchFacts",Toast.LENGTH_SHORT).show();
//        Toast.makeText(getActivity(),TeamDetails.id,Toast.LENGTH_SHORT).show();
        String url = "https://api.sportradar.us/nfl-ot2/games/"+id+"/boxscore.xml";
        String api_key ="rr7maet8u7uataccpcqt5e5j";
        String query="";
        try {
            query = String.format("api_key=%s", URLEncoder.encode(api_key,charset));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        MatchFactsCall callAPI = new MatchFactsCall();
        Object response = null;

        response = callAPI.execute(new Object[]{url,query,this});
        al.add("Match Facts mofo");


        // This is the array adapter, it takes the context of the activity as a
        // first parameter, the type of list view as a second parameter and your
        // array as a third parameter.
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, al);

        lv.setAdapter(arrayAdapter);
        return rootView;
    }

    public void updateDisplayList(Object result){

        final HashMap<String,String> gameStore = new HashMap<>();
        try {
            xmlFactoryObject = XmlPullParserFactory.newInstance();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        try {
            myparser = xmlFactoryObject.newPullParser();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        InputStream stream = new ByteArrayInputStream(result.toString().getBytes(StandardCharsets.UTF_8));
        try {
            myparser.setInput(stream, null);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        int event = 0;
        String gameID="";
        String hometeam="",awayteam="";
        try {
            event = myparser.getEventType();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        while (event != XmlPullParser.END_DOCUMENT)  {
            String name=myparser.getName();
            switch (event){
                case XmlPullParser.START_TAG:
                    if(name.equals("home")&&al.size()<2){
                        hometeam = myparser.getAttributeValue(null,"name");
                        al.add("Home Team  "+hometeam);
                    }
                    else if (name.equals("away")&&al.size()<3){
                        awayteam = myparser.getAttributeValue(null,"name");
                        al.add("Away Team  "+awayteam);
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if(name.equals("quarter")&&al.size()<7){
                        String quarternumber = myparser.getAttributeValue(null,"number");
                        String homescore = myparser.getAttributeValue(null,"home_points");
                        String awayscore = myparser.getAttributeValue(null,"away_points");
                        al.add("Quarter: "+quarternumber+"  Home: "+homescore+"  Away:  "+awayscore);
                    }
                    break;
            }
            try {
                event = myparser.next();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //al.add(result.toString());
        System.out.println("Debug: Display List Updated");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_list_item_1,
                al);

        lv.setAdapter(arrayAdapter);

//
        //lv.setTextColor(Color.WHITE);
//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
//                String valueToSend = gameStore.get(al.get(position));
////                Toast.makeText(getApplicationContext(),valueToSend,Toast.LENGTH_LONG).show();
//                Intent intent = new Intent(MainActivity.this, TeamDetails.class);
//                intent.putExtra("id",valueToSend);
//                startActivity(intent);
//            }
//        });
        return;
    }
}
