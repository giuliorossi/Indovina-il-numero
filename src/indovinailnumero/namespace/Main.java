package indovinailnumero.namespace;


import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.packet.Message.Body;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity implements MesssageReceiver
{
	
	enum Stato
	{
		WAIT_FOR_START,WAIT_FOR_START_ACK,USER_SELECTING,WAIT_FOR_NUMBER_SELECTION,WAIT_FOR_BET,USER_BETTING
	}
	
	private static final int SHOW_TOAST = 0;
	
	TextView et1;
	Body body;
	ConnectionManager connection;

	
	
	private Stato statoCorrente;
	String TAG = "tag";
	Timer timer = new Timer();
	TimerTask sendStart= new TimerTask() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (statoCorrente==Stato.WAIT_FOR_START_ACK){
				connection.send("START");}
			else
			{
				Log.d(TAG,"Sending START but the state is "+statoCorrente);
			}
		}
	};
	
	final Handler handler= new Handler(){
		@Override
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.what)
			{
			case Main.SHOW_TOAST:
				Toast.makeText(Main.this, msg.getData().getString("toast"),Toast.LENGTH_LONG).show();
				break;
				default:
					super.handleMessage(msg);
			}
		}
	};

	private String selectedNumber;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		et1=(TextView)findViewById(R.id.textview);
		String utente1= getIntent().getExtras().getString("utente1");
		String utente2= getIntent().getExtras().getString("utente2");
		et1.setText(utente1+"   "+utente2);
		Button btn1=(Button)findViewById(R.id.button1);
		Button btn2=(Button)findViewById(R.id.button2);
		Button btn3=(Button)findViewById(R.id.button3);
		btn1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				numberSelected(v);
			}
		});
		btn2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				numberSelected(v);
			}
		});
		btn3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				numberSelected(v);
			}
		});
		connection = new ConnectionManager(utente1, utente2, this);
		if(utente2.hashCode()<utente1.hashCode())
		{
			//inizio io
			timer.schedule(sendStart, 1000, 5000);
			statoCorrente=Stato.WAIT_FOR_START_ACK;
		}
		else
		{
			//inizia lui, io aspetto il pacchetto
			statoCorrente=Stato.WAIT_FOR_START;
		}
		
		
	}
	@Override
	public void receiveMessage(String body) {
		// TODO Auto-generated method stub
		if (body.equals("START")){
			if (statoCorrente== Stato.WAIT_FOR_START)
			{
				//mando l'ack indietro
				connection.send("STARTACK");
				Message osmsg =handler.obtainMessage(Main.SHOW_TOAST);
				Bundle b = new Bundle();
				b.putString("toast","Scegli un numero");
				osmsg.setData(b);
				handler.sendMessage(osmsg);
				statoCorrente=Stato.USER_SELECTING;
			}
			else if(body.equals("STARTACK"))
			{
				if (statoCorrente==Stato.WAIT_FOR_START_ACK)
				{
					statoCorrente=Stato.WAIT_FOR_NUMBER_SELECTION;
				}
				else
					Log.e(TAG, "Ricevuto START ma lo stato è "+statoCorrente);
			}
			else if(body.startsWith("SELECTED"))
			{
				if(statoCorrente==Stato.WAIT_FOR_NUMBER_SELECTION)
				{
					selectedNumber=body.split(":")[1];
					Message osmsg = handler.obtainMessage(Main.SHOW_TOAST);
					Bundle b = new Bundle();
					b.putString("toast","Indovina il numero");
					osmsg.setData(b);
					handler.sendMessage(osmsg);
					statoCorrente=Stato.USER_SELECTING;
				}
				else
					Log.e(TAG, "Ricevuto SELECTED ma lo stato è "+statoCorrente);
			}
			else if(body.startsWith("BET"))
			{
				if(statoCorrente==Stato.WAIT_FOR_BET)
				{
					String result = body.split(":")[1];
					Message osmsg = handler.obtainMessage(Main.SHOW_TOAST);
					Bundle b = new Bundle();
					if (result.equals("Y"))
						b.putString("toast", "Hai perso, il tuo avversario ha indovinato");
					else
						b.putString("toast", "Hai vinto, il tuo avversario ha sbagliato");
					osmsg.setData(b);
					handler.sendMessage(osmsg);
					statoCorrente=Stato.WAIT_FOR_NUMBER_SELECTION;
					
				}
				else
					Log.e(TAG, "Ricevuto SELECTED ma lo stato è "+statoCorrente);
			}
			else
				Log.e(TAG, "Ricevuto START ma lo stato è "+statoCorrente);
		}
		
	}
	public void numberSelected (View v)
	{
		Button b = (Button)v;
		String B = b.getText().toString();
		if (statoCorrente==Stato.USER_SELECTING)
		{
			connection.send("SELECTED:"+B);
			statoCorrente=Stato.WAIT_FOR_BET;
		}
		else if(statoCorrente==Stato.USER_BETTING)
		{
			if(b.getText().toString().equals(selectedNumber))
			{
				connection.send("BET:Y");
				Toast.makeText(Main.this, "Bravo hai indovinato, ora tocca a te", Toast.LENGTH_LONG).show();
			}
			else
			{
				connection.send("BET:N");
				Toast.makeText(Main.this, "Peccato non hai indovinato, ora tocca a te",Toast.LENGTH_LONG).show();
			}
			statoCorrente=Stato.USER_SELECTING;
		}
	}
	
}
