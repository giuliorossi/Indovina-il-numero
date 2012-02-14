package indovinailnumero.namespace;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class StartActivity extends Activity {
    /** Called when the activity is first created. */
	EditText et1, et2;
	String usr1, usr2;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        et1=(EditText)findViewById(R.id.editText1);
        et2=(EditText)findViewById(R.id.editText2);
        
        Button btn1 = (Button)findViewById(R.id.button1);
        
        btn1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(StartActivity.this, Main.class);
				usr1 = et1.getText().toString();
		        usr2 = et2.getText().toString();
				intent.putExtra("utente1", usr1);
				intent.putExtra("utente2", usr2);
				startActivity(intent);
			}
		});
    }
}