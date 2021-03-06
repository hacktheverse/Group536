package example.com.pebblemusicswitcher;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


// playAudio(findSong("name"))
public class MainActivity extends AppCompatActivity {

    private TextView t2;
    private FetchBPMTask task;

    MediaPlayer singer;
    private int currentSong;
    private int[] songs;
    private String[] songsList;
    private List<Song> tmpSongList;


    TableLayout lip;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TableLayout lip;

        songsList = listSongs();
        for(int i=0; i<songsList.length; i++){
            if(songsList[i].endsWith(".mp3")) {
                Button b = new Button(this);
                b.setId(i);
                b.setText(songsList[i]);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playAudio(v);
                    }
                });
                lip = (TableLayout) findViewById(R.id.biggy);
                lip.addView(b);
            }
        }

        currentSong = 0;
        singer = mkReadyToPlay(songsList[currentSong]);

        //readSongsFromSdCard();
        //System.out.println(songsList.get(0));

        task = new FetchBPMTask();
        task.execute();
    }

    public void playAudio(View view) {
        int id = view.getId();
        try {
            if (id != currentSong) {
                singer.reset();
                singer.release();
                singer = mkReadyToPlay(songsList[id]);
                currentSong = id;
            }
            if (singer.isPlaying()) {
                singer.pause();
            } else {
                singer.start();
            }
        } catch (Exception e) {
            System.out.println(id + ", " + songsList[currentSong]);
        }
    }

    public void playAudio(int id){
        try {
            singer.reset();
            singer.release();
            singer = mkReadyToPlay(songsList[id]);
            currentSong = id;
            singer.start();
        } catch (Exception e){
            //Bad Song Id
        }
    }



    /*public void clicky(View view) {
        EditText etxt = (EditText) findViewById(R.id.test);
        String s = String.valueOf(etxt.getText());

        int id = findSong(s);
        if(id!=-1){
            playAudio(id);
        } else etxt.setText("WEIRD");

        //etxt.setText("");
    }*/

    private String[] listSongs(){
        String[] list = null;

        try {
            list = getAssets().list("");
        } catch(IOException e){
        }

        return list;
    }

    private FileInputStream getSongFile(String song) {
        FileInputStream x = null;

        AssetManager am  = this.getAssets();
        try {
            AssetFileDescriptor filedesc = am.openFd(song);
            x = filedesc.createInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return x;
    }

    private MediaPlayer mkReadyToPlay(String song){
        MediaPlayer mp = new MediaPlayer();

        AssetManager am  = this.getAssets();
        AssetFileDescriptor filedesc;
        try {
            filedesc = am.openFd(song);
            mp.setDataSource(filedesc.getFileDescriptor(), filedesc.getStartOffset(), filedesc.getLength());
            filedesc.close();
            mp.prepare();
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }

        return mp;
    }

    public void clicky(View view) {
        TextView txt = (TextView) findViewById(R.id.texty);

        EditText etxt = (EditText) findViewById(R.id.test);
        String s = String.valueOf(etxt.getText());

        try {
            FileInputStream temp = getSongFile(s);
            txt.setText(temp.toString());
        } catch (Exception e){
            e.printStackTrace();
            txt.setText("Oops");
        }
    }

    public int findSong(String song){
        for(int i=0; i<songsList.length; i++){
            if(song.equals(songsList[i]))
                return i;
        } return -1;
    }

    // ---------------------------------------------------------------
    public double getBPM(String songName) {
        double bpm = 0.00;
        HTTPJson httpJson = HTTPJson.getInstance();
        String jsonString = "";
        FileInputStream fis = getSongFile(songName);

        if (fis != null) {
            jsonString = httpJson.connectHTTP(fis);
        } else {
            Log.e("getBPM", "Null");
        }

        try {
            bpm = httpJson.readBPM(jsonString);
            if(bpm==0.00) {
                Log.e("getBPM", "BPM==0");
            }
        } catch(Exception e) {
            Log.d("getBPM", e + "meh");
            e.printStackTrace();
        }
        return bpm;
    }

//    public List<Song> getSongList() {
//        getBPM();
//    }


    private class FetchBPMTask extends AsyncTask<String, Void, Void> {

        String bpmStr = "dqwdqrgqefqed";
        double bpm = 999.99999999;
        String jsonString ="dqeduqeh";

        @Override
        protected Void doInBackground(String... songName) {
            // Retrieve JSON String with the help of the HTTPJson class

            HTTPJson httpJson = HTTPJson.getInstance();

            FileInputStream fis = getSongFile("cry_me_a_river.mp3");
            if (fis!=null){
                jsonString = httpJson.connectHTTP(fis);
            } else {
                Log.e("eqfuihdqwdq", "qegofegdoqihpd");
            }

            // Parse JSON String, store bpm data
            try {
                bpm = httpJson.readBPM(jsonString);


                bpmStr = Double.toString(bpm);

            } catch(Exception e) {
                Log.d("udgaogfqoevq", e + "");
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            t2 = (TextView) findViewById(R.id.text2);
            t2.setText(bpmStr);
            System.out.println(jsonString);
        }
    }


}