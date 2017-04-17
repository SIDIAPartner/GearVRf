package org.gearvrf.animation;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.SparseArray;

import junit.framework.Assert;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterialShaderId;
import org.gearvrf.testUtils.NativeClassUtils;
import org.gearvrf.utility.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by darlison.osorio on 12/11/2015.
 *
 * Test cases for test on GVRAnimation class
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SparseArray.class, GVRMaterialShaderId.class, TextUtils.class, Log.class})
@SuppressStaticInitializationFor({ NativeClassUtils.NATIVE_MATERIAL })
public class GVRAnimationTest {

    /**
     * GVRAnimation is abstract, so, we instantiate its first non-abstract child(GVRColorAnimation),
     *   but only primitive methods.
     */
    GVRColorAnimation animation;
    private GVRMaterial material;
    private GVRMaterial mockMaterial;
    private GVRContext context;

    private final float[] white = new float[] {255.0f,255.0f,255.0f,255.0f};
    private final float duration = 10f;

    // steps to create the animation
    @Before
    public void initGVRAnimationTest() throws Exception {
        context = Mockito.mock(GVRContext.class);

        PowerMockito.mockStatic(SparseArray.class);
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.mockStatic(Log.class);
        PowerMockito.mockStatic(Class.forName(NativeClassUtils.NATIVE_MATERIAL));

        final SparseArray<GVRMaterialShaderId> array = PowerMockito.mock(SparseArray.class);

        PowerMockito.whenNew(SparseArray.class).withAnyArguments().thenReturn(array);
        PowerMockito.when(array.clone()).thenReturn(array);
        PowerMockito.when(array.size()).thenReturn(0);
        PowerMockito.when(TextUtils.isEmpty(Mockito.anyString())).thenReturn(false);

        material = new GVRMaterial(context);
        mockMaterial = PowerMockito.spy(material);
        PowerMockito.when(mockMaterial.getVec3("color")).thenReturn(white);
        animation = new GVRColorAnimation(material, duration, white);
    }

    /**
     * setInterpolator method test
     */
    @Test
    public void setInterpolatorTest() {
        final GVRInterpolator interpolator = new GVRInterpolator() {
            @Override
            public float mapRatio(float ratio) {   return 2.0f;  }
        };
        animation.setInterpolator(interpolator);
        Assert.assertEquals(interpolator, Whitebox.getInternalState(animation, "mInterpolator"));
    }

    /**
     * setRepeatMode test
     */
    @Test
    public void setRepeatModeTest() {

        //Once state
        int repeatMode = GVRRepeatMode.ONCE;
        animation.setRepeatMode(repeatMode);
        Assert.assertEquals(repeatMode, Whitebox.getInternalState(animation, "mRepeatMode"));

        //pingpong state
        repeatMode = GVRRepeatMode.PINGPONG;
        animation.setRepeatMode(repeatMode);
        Assert.assertEquals(repeatMode, Whitebox.getInternalState(animation, "mRepeatMode"));

        //repeated state
        repeatMode = GVRRepeatMode.REPEATED;
        animation.setRepeatMode(repeatMode);
        Assert.assertEquals(repeatMode, Whitebox.getInternalState(animation, "mRepeatMode"));
    }

    /**
     * setRepeatMode test expecting repeat "repeated" state
     */
    @Test(expected = IllegalArgumentException.class)
    public void setRepeatModeInvalidTest() {
        final int repeatMode = 3;
        animation.setRepeatMode(repeatMode);
    }

    /**
     * setRepeatCount and getRepeatCount methods test.
     */
    @Test
    public void setRepeatCountTest() {
        final int repeatCount = 8;
        animation.setRepeatCount(repeatCount);
        Assert.assertEquals(repeatCount, animation.getRepeatCount());
    }

    /**
     * setOnfinish method test
     */
    @Test
    public void setOnFinishTest() {

        //GVROnFinish Test
        final GVROnFinish onFinishCallback = new GVROnFinish() {
            @Override
            public void finished(GVRAnimation animation) { /* nothing to implement */ }
        };
        animation.setOnFinish(onFinishCallback);
        Assert.assertEquals(onFinishCallback, Whitebox.getInternalState(animation, "mOnFinish"));
        Assert.assertEquals(null, Whitebox.getInternalState(animation, "mOnRepeat"));
        Assert.assertEquals(2, Whitebox.getInternalState(animation, "mRepeatCount"));


        //GVROnRepeat Test
        final GVROnRepeat onRepeatCallback = new GVROnRepeat() {
            @Override
            public boolean iteration(GVRAnimation animation, int count) { return false; }
            @Override
            public void finished(GVRAnimation animation) { /** Nothing to implement */}
        };
        animation.setOnFinish(onRepeatCallback);
        Assert.assertEquals(onRepeatCallback, Whitebox.getInternalState(animation, "mOnFinish"));
        Assert.assertEquals(onRepeatCallback, Whitebox.getInternalState(animation, "mOnRepeat"));
        Assert.assertEquals(-1, Whitebox.getInternalState(animation, "mRepeatCount"));


        //Null Test
        animation.setOnFinish(null);
        Assert.assertEquals(null, Whitebox.getInternalState(animation, "mOnFinish"));

    }

    /**
     * Checks the animation flow with OnRepeatCallback
     * @throws Exception
     */
    @Test
    public void startAnimateEndWithOnRepeatCallbackTest() throws Exception {

        final GVRAnimationEngine engine = GVRAnimationEngine.getInstance(context);

        final GVROnRepeat onRepeatCallback = Mockito.mock(GVROnRepeat.class);

        //start animation
        animation.start(engine);
        List animations = (List)Whitebox.getInternalState(engine, "mAnimations");
        Assert.assertEquals(1, animations.size());

        //first drawFrame
        final GVRDrawFrameListener frameListener = (GVRDrawFrameListener) Whitebox.getInternalState(engine, "mOnDrawFrame");
        animation.setRepeatMode(GVRRepeatMode.ONCE);
        frameListener.onDrawFrame(5.0f);
        PowerMockito.verifyPrivate(Class.forName(NativeClassUtils.NATIVE_MATERIAL),
                VerificationModeFactory.atLeastOnce()).invoke("setVec3", Mockito.anyLong(), Mockito.anyString(),
                Mockito.anyFloat(), Mockito.anyFloat(), Mockito.anyFloat());

        //last drawFrame, finishCallback.finished method must be called
        animation.setRepeatMode(GVRRepeatMode.REPEATED);
        animation.setOnFinish(onRepeatCallback);
        frameListener.onDrawFrame(5.0f);
        PowerMockito.verifyPrivate(Class.forName(NativeClassUtils.NATIVE_MATERIAL),
                VerificationModeFactory.atLeastOnce()).invoke("setVec3", Mockito.anyLong(), Mockito.anyString(),
                Mockito.anyFloat(), Mockito.anyFloat(), Mockito.anyFloat());

        Mockito.verify(onRepeatCallback, Mockito.times(1)).iteration(Mockito.any(GVRAnimation.class), Mockito.anyInt());
        Mockito.verify(onRepeatCallback,Mockito.times(1)).finished(Mockito.any(GVRAnimation.class));

        //check elapsedTime = duration
        Assert.assertEquals(animation.getDuration(), animation.getElapsedTime());

        //check if is finished;
        Assert.assertTrue(animation.isFinished());


        //stop animation
        engine.stop(animation);
        animations = (List)Whitebox.getInternalState(engine, "mAnimations");

        //verify animations size
        Assert.assertEquals(0, animations.size() );
    }

    /**
     * Tests the OnfinishCallback during animation
     * @throws Exception
     */
    @Test
    public void startAnimateEndWithOnFinishCallbackTest() throws Exception {

        final GVRAnimationEngine engine = GVRAnimationEngine.getInstance(context);

        final GVROnFinish onFinishCallback = Mockito.mock(GVROnFinish.class);

        //start animation
        animation.start(engine);
        List animations = (List)Whitebox.getInternalState(engine, "mAnimations");
        Assert.assertEquals(1, animations.size());

        //first drawFrame
        final GVRDrawFrameListener frameListener = (GVRDrawFrameListener) Whitebox.getInternalState(engine, "mOnDrawFrame");
        animation.setRepeatMode(GVRRepeatMode.ONCE);
        animation.setRepeatCount(2);
        frameListener.onDrawFrame(5.0f);
        PowerMockito.verifyPrivate(Class.forName(NativeClassUtils.NATIVE_MATERIAL),
                VerificationModeFactory.atLeastOnce()).invoke("setVec3", Mockito.anyLong(), Mockito.anyString(),
                Mockito.anyFloat(), Mockito.anyFloat(), Mockito.anyFloat());

        //second drawFrame
        animation.setRepeatMode(GVRRepeatMode.REPEATED);
        animation.setOnFinish(onFinishCallback);
        animation.setRepeatCount(0);
        frameListener.onDrawFrame(5.0f);
        PowerMockito.verifyPrivate(Class.forName(NativeClassUtils.NATIVE_MATERIAL),
                VerificationModeFactory.atLeastOnce()).invoke("setVec3", Mockito.anyLong(), Mockito.anyString(),
                Mockito.anyFloat(), Mockito.anyFloat(), Mockito.anyFloat());
        Assert.assertEquals(10.0f, animation.getElapsedTime());

        Mockito.verify(onFinishCallback,Mockito.times(2)).finished(Mockito.any(GVRAnimation.class));
        Assert.assertEquals(10.0f, animation.getElapsedTime());

        //stop animation
        engine.stop(animation);
        animations = (List) Whitebox.getInternalState(engine, "mAnimations");
        Assert.assertEquals(0, animations.size());


        Method method = GVRContext.class.getDeclaredMethod("resetOnRestart");
        method.setAccessible(true);
        method.invoke(null);

        //verify instance of GVRAnimationEngine
        Assert.assertNotSame(engine, GVRAnimationEngine.getInstance(context));

    }

}
