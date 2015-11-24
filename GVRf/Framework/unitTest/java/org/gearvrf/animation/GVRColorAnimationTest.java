package org.gearvrf.animation;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.SparseArray;

import org.apache.commons.math3.analysis.function.Power;
import org.gearvrf.GVRContext;
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
import org.mockito.verification.VerificationMode;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by darlison.osorio on 12/11/2015.
 *
 * Test cases of GVRColorAnimationTest class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SparseArray.class, GVRMaterialShaderId.class, TextUtils.class, Color.class})
@SuppressStaticInitializationFor({
        NativeClassUtils.NATIVE_MATERIAL,
        NativeClassUtils.NATIVE_SCENE_OBJECT,
        NativeClassUtils.NATIVE_TRANSFORM,
        NativeClassUtils.NATIVE_RENDER_DATA,
        NativeClassUtils.NATIVE_RENDER_PASS})
//@PowerMockIgnore({GVRColorAnimationTest.NATIVE_MATERIAL})
public class GVRColorAnimationTest {

    private GVRColorAnimation colorAnimation;
    private GVRMaterial material;
    private GVRMaterial mockMaterial;
    private GVRContext context;


    private final float duration = 3f;

    private final float[] white = new float[] {255.0f,255.0f,255.0f,255.0f};
    final int red = 255;
    final int green = 0;
    final int blue = 255;

    @Before
    public void initGVRColorAnimationTest() throws Exception {

        context = Mockito.mock(GVRContext.class);
        PowerMockito.mockStatic(SparseArray.class);
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.mockStatic(Color.class);

        PowerMockito.mockStatic(Class.forName(NativeClassUtils.NATIVE_MATERIAL));
        PowerMockito.mockStatic(Class.forName(NativeClassUtils.NATIVE_SCENE_OBJECT));
        PowerMockito.mockStatic(Class.forName(NativeClassUtils.NATIVE_TRANSFORM));
        PowerMockito.mockStatic(Class.forName(NativeClassUtils.NATIVE_RENDER_DATA));
        PowerMockito.mockStatic(Class.forName(NativeClassUtils.NATIVE_RENDER_PASS));

        final SparseArray<GVRMaterialShaderId> array = PowerMockito.mock(SparseArray.class);

        PowerMockito.whenNew(SparseArray.class).withAnyArguments().thenReturn(array);
        PowerMockito.when(TextUtils.isEmpty(Mockito.anyString())).thenReturn(false);
        PowerMockito.when(Color.red(Mockito.anyInt())).thenReturn(red);
        PowerMockito.when(Color.green(Mockito.anyInt())).thenReturn(green);
        PowerMockito.when(Color.blue(Mockito.anyInt())).thenReturn(blue);
        PowerMockito.when(Color.rgb(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(16711680);

        material = new GVRMaterial(context);
        mockMaterial = PowerMockito.spy(material);
        PowerMockito.when(mockMaterial.getVec3("color")).thenReturn(white);

    }


    /** tests the animate method */
    @Test
    public void animateTest() throws Exception {
        colorAnimation = new GVRColorAnimation(material, duration, 16711680);
        colorAnimation.animate(material, 0.5f);

        //verifies if native method has been called
        PowerMockito.verifyPrivate(Class.forName(NativeClassUtils.NATIVE_MATERIAL),
                VerificationModeFactory.atLeastOnce()).invoke("setVec3", Mockito.anyLong(), Mockito.anyString(),
                Mockito.anyFloat(), Mockito.anyFloat(), Mockito.anyFloat());
    }

    /** tests the animate method with constructor using GVRSceneObject */
    @Test
    public void newConstructorWithAnimateTest() throws Exception {
        final float ratio = 2.0f;

        final GVRSceneObject object = new GVRSceneObject(Mockito.mock(GVRContext.class));
        GVRRenderData renderData = new GVRRenderData(context);
        renderData.setMaterial(material);
        object.attachRenderData(renderData);

        colorAnimation = new GVRColorAnimation(object, duration, 16711680);
        colorAnimation.animate(object, ratio);

        PowerMockito.verifyPrivate(Class.forName(NativeClassUtils.NATIVE_MATERIAL),
                VerificationModeFactory.atLeastOnce()).invoke("setVec3", Mockito.anyLong(), Mockito.anyString(),
                Mockito.anyFloat(), Mockito.anyFloat(), Mockito.anyFloat());

    }





}
