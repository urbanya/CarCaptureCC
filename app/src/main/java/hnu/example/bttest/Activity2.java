package hnu.example.bttest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.NumberFormat;
import static hnu.example.bttest.Activity3.AvgRPMTextViewAnzeige;
import static hnu.example.bttest.Activity3.AvgVTextViewAnzeige;

public class Activity2 extends AppCompatActivity implements IBTMsgClient {
    //Deklarieren Komponenten in Activity 2
    private TextView RPMTextView;
    private TextView VTextView;
    private TextView tTextView;

    //Deklarieren des Buttons 'Report anzeigen"
    private Button buttonReport;

    //Deklarieren der ImageViews Pfeile 'Hochschalten'/'Runterschalten'
    private ImageView up;
    private ImageView down;

    //Deklarieren der Variablen
    public static String VMSG;                      //Geschwindigkeit
    public static String RPMMSG;                    //Drehzahl
    public static String TMSG;                      //Laufzeit

    public static double VSum =0;                   //Summer aller empfangener Geschwindigkeiten
    public static double RPMSum=0;                  //Summer aller empfangener Drehzahlen
    public static double VCount=0;                  //Geschwindigkeit Counter
    public static double RPMCount=0;                //Drehzahl Counter

    public static double VAVG;                      //Durhschnitt der empfangenen Geschwindigkeiten
    public static double RPMAVG;                    //Durchschnitt der empfangenen Drehzahlen

    public static double FMin;                      //Laufzeit in Minuten






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2);

        //Initialisieren der Komponenten aus dem Layout Activity2.xml
        RPMTextView = (TextView) findViewById(R.id.RPMTextViewAnzeige);
        VTextView = (TextView) findViewById(R.id.VTextViewAnzeige);
        tTextView = (TextView) findViewById(R.id.tTextViewAnzeige);
        buttonReport = (Button) findViewById(R.id.buttonReport);

        down = (ImageView) findViewById(R.id.down);
        up = (ImageView) findViewById(R.id.up);

        //ImageViews 'up' und 'down' werden auf dem Layout als unsichtbar gesetzt
        down.setVisibility(View.INVISIBLE);
        up.setVisibility(View.INVISIBLE);



        BTManager.addBTMsgClient(this);

        //OnCLickListener für den Button 'Report anzeigen': Wenn Button geklickt, dann mach[ ]
        buttonReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                VAVG = VSum / VCount;           //Berechnung der Durchschnittsgeschwindigkeit
                RPMAVG = RPMSum / RPMCount;     //Berechnung der Durchschnittsdrehzahl

                startActivity(3);           //Methode um Activity zu wechseln
            }
        });
    }


    //Methode zum wechseln in Activty3, wenn int nr=3
    public void startActivity(int nr) {
        if (nr == 3) {
            Intent launchMain3Activity = new Intent(this, Activity3.class);
            startActivity(launchMain3Activity);
        }
    }


    //Methode zum verwerten der empfangenen Werte
    @Override
    public void receiveMessage(final String msg) {

        //Substring entfernt Präfix und Postfix der empfangenen String Daten und wandelt in Double um. Macht Berechnung möglich
        int len = msg.length();
        String RN = msg.substring(1,len-1);
        double zahl = Double.parseDouble(RN);

        //Geschwindigkeit, Drehzahl, Laufzeit als String
        VMSG = String.valueOf(zahl);
        RPMMSG = String.valueOf(zahl);
        TMSG = String.valueOf(zahl);



        //Ausführen wenn Präfix mit 'R' startet. Ausgabe und Berechnung
        if (msg.startsWith("R")) {
            RPMTextView.setText(RPMMSG+" RPM");        //Ausgabe von Drehzahl im TextView 'Drehzahl'

            RPMSum = RPMSum +zahl;                     //RPM Summe berechnen
            RPMCount = RPMCount + 1;                   //RPM Counter


            //ImageView für 'down' wird sichtbar, 'up' wird unsichtbar wenn Zahl <= 1500
            if (zahl<=1500){

                down.setVisibility(View.VISIBLE);
                up.setVisibility(View.INVISIBLE);

                //ImageView für 'up' wird sichtbar, 'down' wird unsichtbar wenn Zahl >2500
            } else if(zahl > 2500){

                up.setVisibility(View.VISIBLE);
                down.setVisibility(View.INVISIBLE);

                //beide ImageViews werden unsichtbar
            }else{
                up.setVisibility(View.INVISIBLE);
                down.setVisibility(View.INVISIBLE);

            }

            //Ausführen wenn Präfix mit 'V' startet. Ausgabe und Berechnung
        }else if (msg.startsWith("V")){
            VTextView.setText(VMSG+" km/h");           //Ausgabe von Geschwindigkeit im TextView 'Geschwindigkeit'


            VSum = VSum + zahl;                        //V Summe berechnen
            VCount = VCount + 1;                       //V Counter

            //Ausführen wenn Präfix mit 'T' startet. Ausgabe und Berechnung
        }else if (msg.startsWith("T")) {
            tTextView.setText(TMSG + " s");            //Ausgabe von Laufzeit im TextView 'Laufzeit'

            FMin = zahl / 60;                          //Laufzeit in Minuten umwandeln

        }
    }
    @Override
    public void receiveConnectStatus(boolean isConnected) {
    }

    @Override
    public void handleException(Exception e) {
    }
}
