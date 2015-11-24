package org.gearvrf.animation;

import android.text.TextUtils;
import android.util.SparseArray;

import junit.framework.Assert;

import org.apache.commons.math3.analysis.function.Power;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterialShaderId;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRStockMaterialShaderId;
import org.gearvrf.GVRTransform;
import org.gearvrf.testUtils.NativeClassUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SparseArray.class, GVRMaterialShaderId.class, GVRTransformAnimation.class, TextUtils.class})
@SuppressStaticInitializationFor({
        NativeClassUtils.NATIVE_SCENE_OBJECT,
        NativeClassUtils.NATIVE_TRANSFORM,
})
public class GVRTransformAnimationTest {

    /**
     * Custom class used to test GVRTransformAnimation methods
     */
    protected static final class GVRCustomTransformAnimation extends GVRTransformAnimation {

         protected GVRCustomTransformAnimation(GVRTransform transform, float duration) {
             super(transform, duration);
         }

        @Override
        protected void animate(GVRHybridObject target, float ratio) {
            //unnecessary
        }
    };

    /**
     * Custom class used to test GVRTransformAnimation methods with GVRSceneObject
     */
    protected static final class GVRCustom2TransformAnimation extends GVRTransformAnimation {

        protected GVRCustom2TransformAnimation(GVRSceneObject sceneObject, float duration) {
            super(sceneObject, duration);
        }

        @Override
        protected void animate(GVRHybridObject target, float ratio) {
            //unnecessary
        }
    };

    GVRSceneObject sceneObject;

    final float duration = 3;

    @Before
    public void initGVRPositionAnimationTest() throws Exception {

        PowerMockito.mockStatic(SparseArray.class);
        PowerMockito.mockStatic(TextUtils.class);

        PowerMockito.mockStatic(Class.forName(NativeClassUtils.NATIVE_SCENE_OBJECT));
        PowerMockito.mockStatic(Class.forName(NativeClassUtils.NATIVE_TRANSFORM));


        PowerMockito.mockStatic( GVRMaterialShaderId.class );
        PowerMockito.whenNew(GVRMaterialShaderId.class).withAnyArguments().thenReturn(Mockito.mock(GVRMaterialShaderId.class));

        final SparseArray<GVRMaterialShaderId> array = PowerMockito.mock(SparseArray.class);
        PowerMockito.whenNew(SparseArray.class).withAnyArguments().thenReturn(array);

        PowerMockito.when(TextUtils.isEmpty(Mockito.anyString())).thenReturn(false);

        sceneObject = new GVRSceneObject(Mockito.mock(GVRContext.class));

    }

    /**
     * Tests the contructor method passing a GVRTransform as parameter
     * @throws Exception
     */
    @Test
    public void animateByCreatingFromGVRTransformTest() throws Exception {

        final GVRTransform transform = sceneObject.getTransform();

//        PowerMockito.spy(GVRTransformAnimation.class);
//        PowerMockito.when(GVRTransformAnimation.getTransform(Mockito.any(GVRSceneObject.class))).thenReturn(transform);


        final GVRCustomTransformAnimation animation = new GVRCustomTransformAnimation(transform, duration);

        Assert.assertEquals(transform, Whitebox.getInternalState(animation, "mTransform"));
    }

    /**
     * Tests the contructor method passing a GVRSceneObject as parameter
     *
     * FIXME: only works by setting  testOptions {  unitTests.returnDefaultValues = true}
     * @throws Exception
     */
    @Test
    public void animateByCreatingFromGVRSceneObjectTest() throws Exception {

        final GVRTransform transform = sceneObject.getTransform();

        GVRSceneObject object = Mockito.spy(sceneObject);
        Mockito.when(object.getTransform()).thenReturn(transform);

        final GVRCustom2TransformAnimation animation = new GVRCustom2TransformAnimation(object, duration);

        Assert.assertEquals(transform, Whitebox.getInternalState(animation, "mTransform"));

    }
}
