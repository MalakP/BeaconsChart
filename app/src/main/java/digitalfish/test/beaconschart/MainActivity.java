package digitalfish.test.beaconschart;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import digitalfish.test.beaconschart.Data.BeaconWithSmoothedDistance;
import digitalfish.test.beaconschart.Interfaces.BeaconDataSmoother;


public class MainActivity extends Activity implements BeaconConsumer {
    private int HISTORY_SIZE = 100; // How many samples is shown on the chart
    @InjectView(R.id.chart)
    LineChart mChart;
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
    BeaconDataSmoother mBeaconDataSmoother;

    String ESTIMOTE_IDENTIFIER = "B9407F30-F5F8-466E-AFF9-25556B57FE6D"; // default estimote identifier
    boolean mIsKnownOnly = true;
    boolean mKeepScreenOn = false;
    private final static Map<Integer, String> FRIENDLY_NAMES;
    static {// list containing majors numbers of beacons of interest
        FRIENDLY_NAMES = new HashMap<>();
        FRIENDLY_NAMES.put(61566, "b1");
        FRIENDLY_NAMES.put(788, "b2");
        FRIENDLY_NAMES.put(26070, "b3");
        FRIENDLY_NAMES.put(32823, "b4");
        FRIENDLY_NAMES.put(32823, "b4");
        FRIENDLY_NAMES.put(41369, "b5");
        FRIENDLY_NAMES.put(32554, "b6");
    }

    int mDataCount = 0;
    Map<Beacon, List<Double>> mValuesMap = new HashMap<>();
    ArrayList<String> mXVals = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        mBeaconDataSmoother = new BeaconListAdjuster();

    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        beaconManager.unbind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_reset:
                mValuesMap.clear();
                mDataCount = 0;
                mXVals.clear();
                return true;

            case R.id.known_only:
                mIsKnownOnly  = !mIsKnownOnly;
                item.setChecked(mIsKnownOnly);
                mValuesMap.clear();
                mDataCount = 0;
                mXVals.clear();
                return true;

            case R.id.stay_awake:
                mKeepScreenOn = !mKeepScreenOn;
                item.setChecked(mKeepScreenOn);
                setKeepScreenOn(mKeepScreenOn);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setKeepScreenOn(boolean pKeepScreenOn) {
        if(pKeepScreenOn)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(final Collection<Beacon> beacons, Region region) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        PrepareChartData(mBeaconDataSmoother.getSmoothBeaconList(beacons));
                    }
                });
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", Identifier.parse(ESTIMOTE_IDENTIFIER), null, null));
        } catch (RemoteException e) {
            Log.e("BC", "Problem starting ranging beacons in range: "+e.getMessage());
        }
    }


    synchronized private void PrepareChartData(Collection<BeaconWithSmoothedDistance> pSmoothBeaconList) {

        if(mDataCount <HISTORY_SIZE) {
            mDataCount++;
            mXVals.add(String.valueOf(mDataCount));
        }
        for(BeaconWithSmoothedDistance bwsd: pSmoothBeaconList){


            if(mIsKnownOnly &&!FRIENDLY_NAMES.containsKey(bwsd.getBeacon().getId2().toInt()))
                continue;
            List<Double> tempListOfDistances;
            if(mValuesMap.containsKey(bwsd.getBeacon())){
                tempListOfDistances = mValuesMap.get(bwsd.getBeacon());
            }else
                tempListOfDistances = new ArrayList<>();

            tempListOfDistances.add(bwsd.getDistance());
            if(tempListOfDistances.size()>HISTORY_SIZE){
                tempListOfDistances.remove(0);
                mDataCount = HISTORY_SIZE;
            }

            if(tempListOfDistances.size()< mDataCount)
                tempListOfDistances.add(tempListOfDistances.get(tempListOfDistances.size()-1));

            mValuesMap.put(bwsd.getBeacon(), tempListOfDistances);
            

        }

        //Create Lists of entries and fill it with values
        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        Iterator it = mValuesMap.entrySet().iterator();
        int j=0;
        while (it.hasNext()) {

            Map.Entry pair = (Map.Entry) it.next();

            ArrayList<Entry> tempValues = new ArrayList<>();
            int i=0;
            for(Double td: (List<Double>)pair.getValue()){
                tempValues.add(new Entry((float)td.doubleValue(),i++ ));
            }

            String bName = ((Beacon)pair.getKey()).getId2().toString();
            if(FRIENDLY_NAMES.containsKey(((Beacon)pair.getKey()).getId2().toInt())){
                bName = FRIENDLY_NAMES.get(((Beacon)pair.getKey()).getId2().toInt());
            }

            LineDataSet currentBeaconDataSet = new LineDataSet(tempValues, bName);
            currentBeaconDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            currentBeaconDataSet.setDrawCircles(false);
            currentBeaconDataSet.setDrawCircleHole(false);
            currentBeaconDataSet.setDrawValues(false);

            j++;
            switch (j){
                case 1:
                    currentBeaconDataSet.setColor(getResources().getColor(android.R.color.holo_blue_bright));
                    break;
                case 2:
                    currentBeaconDataSet.setColor(getResources().getColor(android.R.color.black));
                    break;
                case 3:
                    currentBeaconDataSet.setColor(getResources().getColor(android.R.color.holo_red_dark));
                    break;
                case 4:
                    currentBeaconDataSet.setColor(getResources().getColor(android.R.color.darker_gray));
                    break;
                case 5:
                    currentBeaconDataSet.setColor(getResources().getColor(android.R.color.holo_orange_dark));
                    break;
                case 6:
                    currentBeaconDataSet.setColor(getResources().getColor(android.R.color.holo_green_dark));
                    break;
                case 7:
                    currentBeaconDataSet.setColor(getResources().getColor(android.R.color.holo_purple));
                    break;
                case 8:
                    currentBeaconDataSet.setColor(getResources().getColor(android.R.color.holo_red_light));
                    break;
                case 9:
                    currentBeaconDataSet.setColor(getResources().getColor(android.R.color.holo_green_light));
                    break;
                case 10:
                    currentBeaconDataSet.setColor(getResources().getColor(android.R.color.holo_blue_dark));
                    break;

                default:
                    currentBeaconDataSet.setColor(getResources().getColor(android.R.color.holo_orange_light));

            }
            dataSets.add(currentBeaconDataSet);
        }

        if(mXVals !=null ) {
            try {
                LineData data = new LineData(mXVals, dataSets);
                mChart.setData(data);
                mChart.invalidate();
            }catch (Exception e){
                Log.e("BC", "problem updating chart "+e.getMessage());
                mXVals.add("0");
            }
        }
    }
}
