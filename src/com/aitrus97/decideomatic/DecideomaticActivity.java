package com.aitrus97.decideomatic;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DecideomaticActivity extends Activity implements OnClickListener  {
	
	ListView lstvwOptions;
	Button btnAdd;
	Button btnDecide;
	Button btnClearAll;
	ChoiceAdapter optionAdapter;
	SensorManager sensorManager;
	private ShakeEventListener sensorListener;
	
	Random randomGenerator = new Random();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
            
        btnAdd = (Button) findViewById(R.id.btnAddOption);
        btnDecide = (Button) findViewById(R.id.btnDecide);
        btnClearAll = (Button) findViewById(R.id.btnClearAll);
        lstvwOptions = (ListView) findViewById(R.id.option_ListView);
        
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        
        
    	if (sensorListener == null) {
    		sensorListener = new ShakeEventListener();
    	}
    	       
        sensorListener.setOnShakeListener(new ShakeEventListener.OnShakeListener() {

            public void onShake() {
            	if (isShakeOn()) {
                	//Context context = getApplicationContext();
                	//Toast.makeText(context, "Shake!", Toast.LENGTH_SHORT).show();
                	eliminateAnOption();
            	}
            }
          });
        
        setDecideButton();
        
        Context context = getApplicationContext();
        
        optionAdapter = new ChoiceAdapter(context,  R.layout.item);
        
        lstvwOptions.setAdapter(optionAdapter);
        
        btnAdd.setOnClickListener(this);
        btnDecide.setOnClickListener(this);
        btnClearAll.setOnClickListener(this);
    }
    
    
	@Override
	protected void onPause() {
		
		String FILENAME = "save_file";
		
		ArrayList<Choice> tempList = new ArrayList<Choice>();
		
		for (int i = 0; i < optionAdapter.getCount(); i++) {
			tempList.add(optionAdapter.getItem(i));
		}
		
		FileOutputStream fos;
		ObjectOutputStream out = null;
		try {
			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
			out = new ObjectOutputStream(fos);
			out.writeObject(tempList);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		if (sensorManager != null) {
			sensorManager.unregisterListener(sensorListener);
		}
		
		super.onPause();		
	}


	@SuppressWarnings("unchecked")
	@Override
	protected void onResume() {		
		super.onResume();
		
		String FILENAME = "save_file";
		
		ArrayList<Choice> tempList = new ArrayList<Choice>();
		
		FileInputStream fis;
		ObjectInputStream in = null;
		try {
			fis = openFileInput(FILENAME);
			in = new ObjectInputStream(fis);
			tempList = (ArrayList<Choice>) in.readObject();
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		optionAdapter.clear();
		for (Choice item : tempList) {
			optionAdapter.add(item);
		}
        
        setDecideButton();
	}


	/**
	 * 
	 */
	private void setDecideButton() {
		if (isShakeOn()) {
        	btnDecide.setText(R.string.btnShake);
        	btnDecide.setEnabled(false);
        	
        	sensorManager.registerListener(sensorListener,
        	        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        	        SensorManager.SENSOR_DELAY_UI);
        } else {
        	btnDecide.setText(R.string.btnDecide);
        	btnDecide.setEnabled(true);
        }
	}


	/**
	 * @return true if the Use Shake to decide is enabled in the options
	 */
	private boolean isShakeOn() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean("shake", false);
	}


	public void onClick(View arg0) {
		Button tmpButton = (Button) arg0;
		
		if (tmpButton.getId() == R.id.btnAddOption) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
	
			alert.setTitle("Add Option");
			alert.setMessage("Enter a new option for me to consider.");
	
			// Set an EditText view to get user input 
			final EditText input = new EditText(this);
			alert.setView(input);
	
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				  String value = input.getText().toString();
				  // Do something with value!			  
				  if (optionAdapter.getPosition(getString(R.string.emptyList)) >= 0) {
					  optionAdapter.remove(getString(R.string.emptyList));
				  }
				  optionAdapter.add(value);
			  
				  if (optionAdapter.getCount() > 1 && !isShakeOn()) {
					  btnDecide.setEnabled(true);
				  }
			  }
			});
	
			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			    // Canceled.
			  }
			});
	
			alert.show();
		}
		else if (tmpButton.getId() == R.id.btnDecide) {

			eliminateAnOption();
			
		} else if (tmpButton.getId() == R.id.btnClearAll) {
			optionAdapter.clear();
			setDecideButton();
			btnAdd.setEnabled(true);
			randomGenerator = new Random();
		}
	}


	/**
	 * eliminate an option from the ChoiceAdapter
	 */
	private void eliminateAnOption() {
				
		Context context = getApplicationContext();
		if (!optionAdapter.hasAvailableChoices()) {
			showMessage(context, "Enter options before asking me to make a decision.");	
			return;
		}

		int numberOfOptions = optionAdapter.getRemainingChoiceCount();
		
		if (numberOfOptions == 1) {
			showMessage(context, "You need to enter more than one choice before asking me to make a decision.");
			return;				
		}
		
		btnAdd.setEnabled(false);
		
		int randomNum  = randomGenerator.nextInt();
		if (randomNum < 0 ) {
			randomNum = randomNum * -1;
		}
		int elimNumber = randomNum % numberOfOptions;
		
		Choice elimatedChoice = optionAdapter.eliminateChoice(elimNumber);									
		lstvwOptions.invalidateViews();
		
		if (optionAdapter.getRemainingChoiceCount() > 1) {
			showMessage(context, elimatedChoice.getValue() + " has been eliminated!");
		}
		else {
			btnDecide.setEnabled(false);
			showMessage(context, "WE HAVE A WINNER!");
		}
		
		return;
	}


	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    switch (item.getItemId()) 
	    {
	        case R.id.settings_title:
	                startActivity(new Intent(this, Prefs.class));
	                return true;
	        case R.id.exit_title:
	                finish();
	                return true;
	    }
	  return false;
	}	
	/**
	 * @param context
	 */
	private void showMessage(Context context, String message) {
		
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast_layout,
		                               (ViewGroup) findViewById(R.id.toast_layout_root));

		ImageView image = (ImageView) layout.findViewById(R.id.image);
		image.setImageResource(R.drawable.gear);
		TextView text = (TextView) layout.findViewById(R.id.text);
		text.setText(message);
		
		Toast toast = new Toast(context);
		toast.setGravity(Gravity.TOP, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}

}