package digitalfish.test.beaconschart;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import digitalfish.test.beaconschart.Data.BeaconWithSmoothedDistance;
import digitalfish.test.beaconschart.Interfaces.BeaconDataSmoother;

/**
 * Created by Piotr Malak on 2015-05-13.
 */
public class BeaconListAdjuster implements BeaconDataSmoother {

    List<Collection<Beacon>> mListOfCollections = new ArrayList<>();
    Collection<BeaconWithSmoothedDistance> lResultList = new ArrayList<>();
    Map<Beacon, Integer> lBeaconMap = new HashMap<>();
    HashSet<Beacon> lBeaconsOnRecentListsSet = new HashSet<>();
    Map<Beacon, List<Double>> mDistancesMap = new HashMap<>();
    private int bufforSize = 4;
    final static int SAMPLE_SIZE = 5;

    public Collection<BeaconWithSmoothedDistance> getSmoothBeaconList(Collection<Beacon> pBeaconList){

            mListOfCollections.add(pBeaconList);
            if(mListOfCollections == null || mListOfCollections.size()<1 || mListOfCollections.size()>SAMPLE_SIZE){
                mListOfCollections.remove(0);
            }

            lResultList.clear();
            lBeaconsOnRecentListsSet.clear();
            int pos = 0;
            for(Collection<Beacon> pList: mListOfCollections){
                pos++;
                for(Beacon b: pList){
                    int lCount = 0;
                    if(lBeaconMap.containsKey(b)){
                        lCount = lBeaconMap.get(b)+1;
                    }
                    lBeaconMap.put(b,lCount);
                    lBeaconsOnRecentListsSet.add(b);
                    if(pos>=mListOfCollections.size()){
                        //Last list, update distances
                        List<Double> lDistances;
                        if(mDistancesMap.containsKey(b)){
                            lDistances = mDistancesMap.get(b);
                        }else{
                            lDistances = new ArrayList<>();
                        }
                        lDistances.add(b.getDistance());
                        if (lDistances.size() == bufforSize) {
                            lDistances.remove(0);
                        }
                        mDistancesMap.put(b,lDistances);
                    }
                }
            }

            Iterator it = lBeaconMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                //System.out.println(pair.getKey() + " = " + pair.getValue());
                if (lBeaconsOnRecentListsSet.contains(pair.getKey())) {
                    if ((Integer) pair.getValue() > mListOfCollections.size() / 2) {
                        lResultList.add(new BeaconWithSmoothedDistance((Beacon) pair.getKey(), median(mDistancesMap.get(pair.getKey()))));
                    }
                }else{
                    it.remove();
                }
            }

        return lResultList;
    }

    private double median(List<Double> pMedianBuffor){
        List<Double>a = new ArrayList<>(pMedianBuffor);
        Collections.sort(a);
        int middle = a.size()/2;
        if (a.size() % 2 == 1) {
            return a.get(middle);
        } else {
            return (a.get(middle-1) + a.get(middle)) / 2.0;
        }
    }
}
