package bubtjobs.com.audioplayer;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SessionManager {
	SharedPreferences perf;
	Editor editor;
	Context _context;
	int PRIVATE_MODE=0;
	private static final String PREF_NAME="audio";
	public static final String ID="id";
	public static final String SHUFFLE="shuffle";
	public static final String REPEAT="repeat";
	public SessionManager(Context _context) {
		this._context = _context;
		perf=_context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor=perf.edit();
	}

	public void setId(String id,boolean shuffle,boolean repeat){
		editor.putString(ID,id);
		editor.putString(SHUFFLE,String.valueOf(shuffle));
		editor.putString(REPEAT,String.valueOf(repeat));
		editor.commit();
		
	}
	public String getId(){
		return perf.getString(ID, null);
	}
	public boolean getShuffle(){
		if(perf.getString(SHUFFLE,"").equals("false"))
			return false;
		else
			return true;
	}
	public Boolean getRepeat(){
		if(perf.getString(REPEAT,"").equals("false"))
			return false;
		else
			return true;
	}

    
	

}
