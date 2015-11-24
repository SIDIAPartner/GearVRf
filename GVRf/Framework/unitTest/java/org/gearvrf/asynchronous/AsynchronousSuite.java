package org.gearvrf.asynchronous;

import org.gearvrf.animation.GVRAccelerateDecelerateInterpolatorTest;
import org.gearvrf.animation.GVRAnimationTest;
import org.gearvrf.animation.GVRColorAnimationTest;
import org.gearvrf.animation.GVROpacityAnimationTest;
import org.gearvrf.animation.GVRPositionAnimationTest;
import org.gearvrf.animation.GVRPostEffectAnimationTest;
import org.gearvrf.animation.GVRRelativeMotionAnimationTest;
import org.gearvrf.animation.GVRRotationByAxisAnimationTest;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimationTest;
import org.gearvrf.animation.GVRScaleAnimationTest;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by darlison.osorio on 13/11/2015.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        AdaptiveScalableTextureCompressionTest.class,
        AsyncBitmapTextureTest.class,
        AsyncCompressedCubemapTextureTest.class,
        AsyncCubemapTextureTest.class,
        AsyncMeshTest.class,
        CompressedTextureTest.class,
})
public class AsynchronousSuite {

}
