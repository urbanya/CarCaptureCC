package hnu.example.bttest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    //Deklarieren des Buttons 'Start'
    private Button startbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //Initialisieren der Komponenten aus dem Layout StartActivity
        startbutton = (Button) findViewById(R.id.startbutton);

        //OnCLickListener für den Button 'Start': Wenn Button geklickt, dann mach[ ]
        startbutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            startActivity(1);                 //Methode um Activity zu wechseln
            }
        });


    }
    //Methode zum wechseln in MainActivity, wenn int nr=1
    public void startActivity (int nr) {
        if (nr == 1) {
            Intent launchMainActivity = new Intent(this, MainActivity.class);
            startActivity(launchMainActivity);
        }
    }
}
