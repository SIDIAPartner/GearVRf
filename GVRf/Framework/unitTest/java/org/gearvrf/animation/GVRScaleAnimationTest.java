package org.gearvrf.animation;

import android.text.TextUtils;
import android.util.SparseArray;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterialShaderId;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
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
 * Created by darlison.osorio on 16/11/2015.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SparseArray.class, GVRMaterialShaderId.class,  TextUtils.class})
@SuppressStaticInitializationFor({
        NativeClassUtils.NATIVE_SCENE_OBJECT,
        NativeClassUtils.NATIVE_TRANSFORM,
})
public class GVRScaleAnimationTest {

    GVRScaleAnimation animation;
    GVRSceneObject sceneObject;

    final float duration = 3;
    final float scale = 0;

    @Before
    public void initGVRPositionAnimationTest() throws Exception {

        PowerMockito.mockStatic(SparseArray.class);
        PowerMockito.mockStatic(TextUtils.class);

        PowerMockito.mockStatic(Class.forName(NativeClassUtils.NATIVE_SCENE_OBJECT));
        PowerMockito.mockStatic(Class.forName(NativeClassUtils.NATIVE_TRANSFORM));

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

        animation = new GVRScaleAnimation(sceneObject, duration, scale);

        animation.animate(sceneObject, 10.0f);

        //verify if native method is called

        PowerMockito.verifyPrivate(Class.forName(NativeClassUtils.NATIVE_TRANSFORM),
                VerificationModeFactory.atLeastOnce()).invoke("setScale", Mockito.anyLong(),
                Mockito.anyFloat(), Mockito.anyFloat(), Mockito.anyFloat());

    }

    /**
     * Tests the animation method passing a GVRTransform as parameter
     * @throws Exception
     */
    @Test
    public void animateByCreatingFromGVRTransformTest() throws Exception {

        final GVRTransform transform = sceneObject.getTransform();

        animation = new GVRScaleAnimation(transform, duration, scale);

        animation.animate(transform, 10.0f);

        //verify if native method is called

        PowerMockito.verifyPrivate(Class.forName(NativeClassUtils.NATIVE_TRANSFORM),
                VerificationModeFactory.atLeastOnce()).invoke("setScale", Mockito.anyLong(),
                Mockito.anyFloat(), Mockito.anyFloat(), Mockito.anyFloat());
    }


}
