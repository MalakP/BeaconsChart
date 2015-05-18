package digitalfish.test.beaconschart.Interfaces;

import org.altbeacon.beacon.Beacon;

import java.util.Collection;

import digitalfish.test.beaconschart.Data.BeaconWithSmoothedDistance;

/**
 * Interface for class implemented raw beacon list smoother.
 *
 * Created by Piotr Malak on 2015-05-13.
 *
 */
public interface BeaconDataSmoother {
    Collection<BeaconWithSmoothedDistance> getSmoothBeaconList(Collection<Beacon> pBeaconList);
}
