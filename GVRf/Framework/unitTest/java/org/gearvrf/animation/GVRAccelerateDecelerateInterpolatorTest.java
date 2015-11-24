package org.gearvrf.animation;

import android.text.TextUtils;
import android.util.SparseArray;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.gearvrf.GVRMaterialShaderId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by darlison.osorio on 12/11/2015.
 *
 * Test cases for GVRAccelerateDEcelerateInterpolatorTest class
 *
 */
@RunWith(PowerMockRunner.class)
public class GVRAccelerateDecelerateInterpolatorTest {

    /** Map Ratio method test */
    @Test
    public void mapRadioTest() {

       final float mapRatio = 5.0f;

       final GVRAccelerateDecelerateInterpolator interpolator = GVRAccelerateDecelerateInterpolator.getInstance();

       final float result = interpolator.mapRatio(mapRatio);

       Assert.assertEquals(mapRatio, result);

   }

}