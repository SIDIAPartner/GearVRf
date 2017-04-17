package org.gearvrf.animation;

import android.text.TextUtils;
import android.util.SparseArray;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterialShaderId;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.testUtils.NativeClassUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by darlison.osorio on 12/11/2015.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SparseArray.class, GVRMaterialShaderId.class, TextUtils.class})
@SuppressStaticInitializationFor(NativeClassUtils.NATIVE_MATERIAL)
@PowerMockIgnore({NativeClassUtils.NATIVE_MATERIAL})
public class GVROpacityAnimationTest {

    GVROpacityAnimation opacityAnimation;
    GVRMaterial material;
    final float[] white = new float[]{255.0f, 255.0f, 255.0f};
    final float opacity = 0.3f;
    final float density = 1.0f;
    final float ratio = 2.5f;

    @Before
    public void initGVROpacityAnimationTest() throws Exception {
        PowerMockito.mockStatic(SparseArray.class);
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.mockStatic(Class.forName(NativeClassUtils.NATIVE_MATERIAL));
        final SparseArray<GVRMaterialShaderId> array = PowerMockito.mock(SparseArray.class);

        PowerMockito.whenNew(SparseArray.class).withAnyArguments().thenReturn(array);
        PowerMockito.when(TextUtils.isEmpty(Mockito.anyString())).thenReturn(false);


        material = new GVRMaterial(Mockito.mock(GVRContext.class));
        PowerMockito.spy(material);
        //PowerMockito.when(material.getVec3("color")).thenReturn(white);

    }

    /**
     * first constructor animate method test
     */
    @Test
    public void animateWithGVRMaterialTest() throws Exception {

        opacityAnimation = new GVROpacityAnimation(material, density, opacity);
        opacityAnimation.animate(material, ratio);

        PowerMockito.verifyPrivate(Class.forName(NativeClassUtils.NATIVE_MATERIAL),
                VerificationModeFactory.atLeastOnce()).invoke("setFloat", Mockito.anyLong(),
                Mockito.anyString(), Mockito.anyFloat());
    }

    /**
     * second constructor and animate methods test
     */
    @Test
    public void animateWithGVRHybridObject() throws Exception {

        GVRSceneObject object = Mockito.mock(GVRSceneObject.class);
        GVRRenderData renderData = Mockito.mock(GVRRenderData.class);
        Mockito.when(object.getRenderData()).thenReturn(renderData);
        Mockito.when(renderData.getMaterial()).thenReturn(material);

        opacityAnimation = new GVROpacityAnimation(object, density, opacity);
        opacityAnimation.animate(material, ratio);

        PowerMockito.verifyPrivate(Class.forName(NativeClassUtils.NATIVE_MATERIAL),
                VerificationModeFactory.atLeastOnce()).invoke("setFloat", Mockito.anyLong(),
                Mockito.anyString(), Mockito.anyFloat());


    }

}