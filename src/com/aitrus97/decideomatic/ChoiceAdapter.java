package com.aitrus97.decideomatic;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChoiceAdapter extends ArrayAdapter<Choice> {

	private Context _context;
	
	public ChoiceAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		_context = context;		
	}
	
	public ChoiceAdapter(Context context, int textViewResourceId, List<Choice> objects) {
		super(context, textViewResourceId, objects);
		_context = context;		
	}
	
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {     
      View row = convertView;

      if (row == null) {
        // This gives us a View object back which, in reality, is our LinearLayout with 
        // an ImageView and a TextView, just as R.layout.row specifies.
    	  
    	  LayoutInflater inflater = (LayoutInflater) _context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    	  row = inflater.inflate(R.layout.item, parent, false);
      }
      
      Choice currentRowChoice = (Choice)this.getItem(position);
      
      TextView label = (TextView) row.findViewById(R.id.itemName);
      label.setText(currentRowChoice.getValue());
      ImageView icon = (ImageView)row.findViewById(R.id.icon);

      if (currentRowChoice.isElminated()) {
        icon.setImageResource(R.drawable.block);
        row.setBackgroundColor(Color.RED);
        label.setTextColor(Color.BLACK);
      } else {
        icon.setImageResource(R.drawable.tick);
        row.setBackgroundColor(Color.TRANSPARENT);
        label.setTextColor(Color.GREEN);
      }

      return row;       
    }

	/**
	 * @param choiceValue
	 */
	public void add(String choiceValue) {
		Choice newChoice = new Choice();
		newChoice.setValue(choiceValue);
		this.add(newChoice);		
	}

	public int getPosition(String testString) {
		for (int i = 0; i < this.getCount(); i++) {
			if (this.getItem(i).getValue() == testString) {
				return i;
			}
		}
		return -1;
	}

	public void remove(String testString) {
		for (int i = 0; i < this.getCount(); i++) {
			if (this.getItem(i).getValue() == testString) {
				this.remove(this.getItem(i));
			}
		}
	}	

	public int getRemainingChoiceCount() {
		int count = 0;
		for (int i = 0; i < this.getCount(); i++) {
			if (!this.getItem(i).isElminated()) {
				count++;
			}
		}
		return count;
	}	

	public Choice eliminateChoice(int position) {
		int count = 0;
		for (int i = 0; i < this.getCount(); i++) {
			if (!this.getItem(i).isElminated()) {
				if (count == position) {
					this.getItem(i).setElminated(true);
					return this.getItem(i);
				}
				count++;
			}
		}
		return null;
	}

	public boolean hasAvailableChoices() {
		for (int i = 0; i < this.getCount(); i++) {
			if (!this.getItem(i).isElminated()) {
				return true;
			}
		}
		return false;

	}		
}
