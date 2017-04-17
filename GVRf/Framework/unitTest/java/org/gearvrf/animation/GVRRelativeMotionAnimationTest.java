package org.gearvrf.animation;

import android.text.TextUtils;
import android.util.SparseArray;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterialShaderId;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.testUtils.NativeClassUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by darlison.osorio on 13/11/2015.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SparseArray.class,GVRMaterialShaderId.class,  TextUtils.class})
@SuppressStaticInitializationFor({
        NativeClassUtils.NATIVE_SCENE_OBJECT,
        NativeClassUtils.NATIVE_TRANSFORM,
        NativeClassUtils.NATIVE_RENDER_DATA,
        NativeClassUtils.NATIVE_RENDER_PASS})
public class GVRRelativeMotionAnimationTest {

    GVRRelativeMotionAnimation animation;
    GVRSceneObject sceneObject;

    final float positionX = 1.0f;
    final float positionY = 2.0f;
    final float positionZ = 3.0f;
    final float duration = 3;

    @Before
    public void initGVRPositionAnimationTest() throws Exception {

        PowerMockito.mockStatic(SparseArray.class);
        PowerMockito.mockStatic(TextUtils.class);

        PowerMockito.mockStatic(Class.forName(NativeClassUtils.NATIVE_SCENE_OBJECT));
        PowerMockito.mockStatic(Class.forName(NativeClassUtils.NATIVE_TRANSFORM));
        PowerMockito.mockStatic(Class.forName(NativeClassUtils.NATIVE_RENDER_DATA));
        PowerMockito.mockStatic(Class.forName(NativeClassUtils.NATIVE_RENDER_PASS));

        final SparseArray<GVRMaterialShaderId> array = PowerMockito.mock(SparseArray.class);

        PowerMockito.whenNew(SparseArray.class).withAnyArguments().thenReturn(array);
        PowerMockito.when(TextUtils.isEmpty(Mockito.anyString())).thenReturn(false);

        sceneObject = new GVRSceneObject(Mockito.mock(GVRContext.class));

    }

    /**
     * Tests the animation method
     * @throws Exception
     */
    @Test
    public void animateTest() throws Exception {

        animation = new GVRRelativeMotionAnimation(sceneObject, duration, positionX, positionY, positionZ);

        animation.animate(sceneObject, 10.0f);

        PowerMockito.verifyPrivate(Class.forName(NativeClassUtils.NATIVE_TRANSFORM),
                VerificationModeFactory.atLeastOnce()).invoke("setPosition", Mockito.anyLong(),
                Mockito.anyFloat(), Mockito.anyFloat(), Mockito.anyFloat());

    }
}
