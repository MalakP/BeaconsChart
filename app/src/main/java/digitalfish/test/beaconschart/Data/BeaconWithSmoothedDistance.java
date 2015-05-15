package digitalfish.test.beaconschart.Data;

import org.altbeacon.beacon.Beacon;

/**
 * Created by Piotr Malak on 2015-05-14.
 */
public class BeaconWithSmoothedDistance {
    private Double mDistance;
    Beacon mBeacon;

    public BeaconWithSmoothedDistance(Beacon pBeacon, Double pDistance) {
        mDistance = pDistance;
        mBeacon = pBeacon;
    }

    public Double getDistance() {
        return mDistance;
    }

    public Beacon getBeacon() {
        return mBeacon;
    }
}
