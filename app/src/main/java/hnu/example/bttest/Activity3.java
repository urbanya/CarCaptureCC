package hnu.example.bttest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;



public class Activity3 extends AppCompatActivity {

    //Deklarieren der Komponenten in Activity3
    private Button UploadButton;
    private Button DeleteButton;
    private Button RestartButton;
    private Button DiscButton;
    public static TextView AvgVTextViewAnzeige, AvgRPMTextViewAnzeige, FMinAnzeige;

    //Deklarieren der URL als String
    private String url = "";


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);

        //Geschwindigkeit-Feld initialisieren
        AvgVTextViewAnzeige = (TextView) findViewById(R.id.AvgVTextViewAnzeige);
        AvgVTextViewAnzeige.setText(String.valueOf(Activity2.VAVG));

        //Drehzahl-Feld initialisieren
        AvgRPMTextViewAnzeige = (TextView) findViewById(R.id.AvgRPMTextViewAnzeige);
        AvgRPMTextViewAnzeige.setText(String.valueOf(Activity2.RPMAVG));

        //Laufzeit-Feld initialisieren
        FMinAnzeige = (TextView) findViewById(R.id.FMinAnzeige);
        FMinAnzeige.setText(String.valueOf(Activity2.FMin));

        //Buttons initialisieren
        UploadButton = (Button) findViewById(R.id.UploadButton);
        DeleteButton = (Button) findViewById(R.id.DeleteButton);
        RestartButton = (Button) findViewById(R.id.RestartButton);
        DiscButton = (Button) findViewById(R.id.DiscButton);






        //OnCLickListener für den Button 'Report anzeigen': Wenn Button geklickt, dann mach[ ]
        UploadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                processREST_GET();                   //Methode um URL zu bilden
                sendRequestAndPrintResponse();       //Methode um Request zu senden
                emptyFields();                       //Methode um Felder zu leeren
            }
        });

        //OnCLickListener für den Button 'Delete': Wenn Button geklickt, dann mach[ ]
       DeleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                emptyFields();                       //Methode um Felder zu leeren
            }
        });

        //OnCLickListener für den Button 'Restart': Wenn Button geklickt, dann mach[ ]
       RestartButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                emptyFields();                       //Methode um Felder zu leeren
                startActivity(2);                //Methode um Activity zu wechseln
            }
        });

        //OnCLickListener für den Button 'Disconnect': Wenn Button geklickt, dann mach[ ]
       DiscButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                emptyFields();                       //Methode um Felder zu leeren
                startActivity(1);                //Methode um Activity zu wechseln
                BTManager.cancel();                  //Methode um Bluetooth verbindung zu trennen
            }
        });
    }


    //Methode zum wechseln in Activty2, wenn int nr=2 oder in MainActivity wenn nr=1
    public void startActivity(int nr) {
        if (nr == 2) {
            Intent launchactivity2 = new Intent(this, Activity2.class);
            startActivity(launchactivity2);
        }else if(nr==1){
            Intent launchMain = new Intent(this, MainActivity.class);
            startActivity(launchMain);
        }
    }


    //Methode bildet URL für Thingspeak server
    private void processREST_GET() {

        //Durchschnittsgeschwindigkeit und Durchschnittsdrehzahl werden aus den TextViews rausgezogen und in Strings gespeichert
        String strGeschwindigkeit = AvgVTextViewAnzeige.getText().toString();
        String strDrehzahl = AvgRPMTextViewAnzeige.getText().toString();

        String api_key = "QYDW98AVOJ7GT8GM";                   //API Key um Werte in einen Channel bei Thingspeak zu übertragen ("Write a Channel Feed")

        //URL um Durchschnittsgeschwindigkeit und Durchschnittsdrehzahl in die zugeordneten Graphen in Thingspeak zu übertragen
        this.url = "https://api.thingspeak.com/update?api_key=" + api_key + "&field1=" + strGeschwindigkeit + "&field2=" + strDrehzahl;
    }


    //Methode sendet spezifische URL an Thingspeak und gibt Antwort/Fehlermeldung
    private void sendRequestAndPrintResponse() {

        StringRequest stringRequest = new StringRequest(Request.Method.GET, this.url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Result handling
                        System.out.println("Response erhalten.");

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Error handling
                System.out.println("Something went wrong!");
                error.printStackTrace();
            }
        });

        //Methode um Anfrage in die Warteschlange zu setzen
        Volley.newRequestQueue(this).add(stringRequest);

    }

    //Methode um alle Werte auf 0 bzw. ""(empty) zu setzen
    private void emptyFields(){
        Activity2.VSum=0;
        Activity2.RPMSum=0;
        Activity2.VCount=0;
        Activity2.RPMCount=0;
        Activity2.FMin =0;
        this.AvgRPMTextViewAnzeige.setText("");
        this.AvgVTextViewAnzeige.setText("");
        this.FMinAnzeige.setText("");
    }
}