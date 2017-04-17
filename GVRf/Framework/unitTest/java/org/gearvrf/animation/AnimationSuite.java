package org.gearvrf.animation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by darlison.osorio on 13/11/2015.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        GVRAccelerateDecelerateInterpolatorTest.class,
        GVRAnimationTest.class,
        GVRColorAnimationTest.class,
        GVRMaterialAnimationTest.class,
        GVROpacityAnimationTest.class,
        GVRPositionAnimationTest.class,
        GVRPostEffectAnimationTest.class,
        GVRRelativeMotionAnimationTest.class,
        GVRRotationByAxisAnimationTest.class,
        GVRRotationByAxisWithPivotAnimationTest.class,
        GVRScaleAnimationTest.class,
        GVRTransformAnimationTest.class,
})
public class AnimationSuite {
}
