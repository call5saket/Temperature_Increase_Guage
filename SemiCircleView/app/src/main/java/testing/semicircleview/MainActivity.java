package testing.semicircleview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SemiCircle.OnSwagPointsChangeListener {

    private SemiCircle semiCrcl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        semiCrcl =(SemiCircle)findViewById(R.id.semi);

        String temp = String .valueOf(semiCrcl.getPoints());

        semiCrcl.setOnSwagPointsChangeListener(this);


    }

    @Override
    public void onPointsChanged(SemiCircle swagPoints, int points, boolean fromUser) {
       // Toast.makeText(this, "temp-----"+points+"", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStartTrackingTouch(SemiCircle swagPoints) {

    }

    @Override
    public void onStopTrackingTouch(SemiCircle swagPoints) {

    }
}
