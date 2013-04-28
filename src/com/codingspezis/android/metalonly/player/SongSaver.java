package com.codingspezis.android.metalonly.player;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

/**
 * 
 * <b>SongSaver</b><br/>
 * 
 * this class stores a list of songs in internal storage via JSON files
 * 
 * @author codingspezis.com
 * @version Apr 28, 2013
 *
 */
public class SongSaver {

	/**
	 * JSON keys for accessing songs 
	 */
	public static final String JSON_ARRAY_SONGS      = "songs";
	public static final String JSON_STRING_INTERPRET = "interpret";
	public static final String JSON_STRING_TITLE     = "title";
	public static final String JSON_STRING_THUMB     = "thumb";
	public static final String JSON_LONG_DATE        = "date";
	
	
	/**
	 * Song
	 * @version 03.02.2013
	 *
	 * class for holding a song
	 */
	public static class Song{
		public String interpret = "";
		public String title = "";
		public String thumb = "";
		public long date = 0;
		public boolean isValid(){
			return interpret.length()!=0 && title.length()!=0;
		}
	}
	
	
	/**
	 * variables and objects for saving songs
	 */
	private Context context;
	private String fileName;
	private int limit;
	private LinkedList<Song> songList; // main data
	private boolean changes; // is there something to save?
	
	
	/**
	 * constructor
	 * 
	 * @param context context of calling object
	 * @param fileName name of file where to save songs
	 * @param limit maximum of songs (FIFO) - if value <= 0 songs are unlimited
	 */
	public SongSaver(Context context, String fileName, int limit){
		this.context=context;
		this.fileName=fileName;
		if(limit<=0)
			this.limit=Integer.MAX_VALUE;
		else
			this.limit=limit;
		songList = new LinkedList<SongSaver.Song>();
		readSongsFromStorage();
		changes = false;
	}
	
	
	/**
	 * reads songs from file
	 */
	private synchronized void readSongsFromStorage(){
		
		InputStreamReader isr;
		try {
			isr = new InputStreamReader(context.openFileInput(fileName));
			String s = "";
			int read = -1;
			final int BUFF_SIZE = 256;
			char buffer[] = new char[BUFF_SIZE];
			do {
				read = isr.read(buffer, 0, BUFF_SIZE);
				if(read > 0) s += String.valueOf(buffer, 0, read);
			} while (read == BUFF_SIZE);
			isr.close();
			JSONObject jObj = new JSONObject(s);
			JSONArray jSongs = jObj.getJSONArray(JSON_ARRAY_SONGS);
			songList.clear();
			for(int i=0; i<jSongs.length(); i++){
				JSONObject jSong = jSongs.getJSONObject(i);
				Song song = new Song();
				song.interpret = jSong.getString(JSON_STRING_INTERPRET);
				song.title     = jSong.getString(JSON_STRING_TITLE);
				song.thumb     = jSong.getString(JSON_STRING_THUMB);
				song.date      = jSong.getLong(JSON_LONG_DATE);
				queeIn(song);
			}
		} catch (FileNotFoundException e) {
			// everything is fine - just nothing saved
		} catch (IOException e) {
			// TODO: error handling (but this should be dead code)
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO: error handling (but this should be dead code)
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * saves songs to file
	 */
	public synchronized void saveSongsToStorage(){  
		if(somethingChanged()){
			try {
				JSONArray jSongs = new JSONArray();
				for(int i=0; i<songList.size(); i++){
					Song song = songList.get(i);
					JSONObject jSong = new JSONObject();
					jSong.put(JSON_STRING_INTERPRET, song.interpret);
					jSong.put(JSON_STRING_TITLE, song.title);
					jSong.put(JSON_STRING_THUMB, song.thumb);
					jSong.put(JSON_LONG_DATE, song.date);
					jSongs.put(jSong);
				}
				JSONObject jObj = new  JSONObject();
				jObj.put(JSON_ARRAY_SONGS, jSongs);
				
				context.deleteFile(fileName);
				OutputStreamWriter osr = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));
				osr.write(jObj.toString());
				osr.close();
			} catch (JSONException e) {
				// TODO: error handling (but this should be dead code)
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO: error handling (but this should be dead code)
				e.printStackTrace();
			} catch (IOException e) {
				// TODO: error handling (but this should be dead code)
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * adds song to song list
	 * @param song song to add
	 * @return true if adding was successful - false otherwise
	 */
	public boolean addSong(Song song){
		if(song.isValid() &&
		   isAlreadyIn(song)==-1 &&
		   limit > songList.size()){
			songList.add(song);
			changes = true;
			return true;
		}
		return false;
	}
	
	
	/**
	 * adds song to song list - if limit is reached first in will be deleted
	 * @param song song to add
	 */
	public boolean queeIn(Song song){
		while(limit <= songList.size()){
			songList.remove();
		}
		return addSong(song);
	}
	
	
	/**
	 * checks if song is already in list of songs
	 * @param song song to check
	 * @return index of last entry of song if it is in the list - -1 otherwise
	 */
	public int isAlreadyIn(Song song){
		for(int i=songList.size()-1; i>=0; i--){
			if(songList.get(i).interpret.equals(song.interpret) &&
			   songList.get(i).title.equals(song.title))
				return i;
		}
		return -1;
	}
	
	
	/**
	 * removes song at position i
	 * @param i position of song to delete
	 */
	public void removeAt(int i){
		changes = true;
		songList.remove(i);
	}
	
	
	/**
	 * removes every song of the list
	 */
	public void clear(){
		while(!songList.isEmpty()){
			changes = true;
			songList.remove();
		}
	}
	
	
	/**
	 * getter for i-th song of list of songs
	 * @param i index of requested song
	 * @return i-th song of list of songs
	 */
	public Song get(int i){
		return songList.get(i);
	}
	
	
	/**
	 * @return true if there were changes on list of songs since they have been read - false otherwise
	 */
	public boolean somethingChanged(){
		return changes;
	}
	
	
	/**
	 * @return returns the size of list of songs
	 */
	public int size(){
		return songList.size();
	}
	
	
	/**
	 * reloads song list
	 */
	public void reload(){
		readSongsFromStorage();
	}
	
	
}
