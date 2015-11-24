package org.gearvrf.animation;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.SparseArray;

import junit.framework.Assert;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRMaterialShaderId;
import org.gearvrf.GVRPostEffect;
import org.gearvrf.GVRPostEffectShaderId;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.testUtils.NativeClassUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

/**
 * Created by darlison.osorio on 13/11/2015.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SparseArray.class, TextUtils.class, Color.class})
@SuppressStaticInitializationFor({
        NativeClassUtils.NATIVE_POST_EFFECT_DATA,
})
public class GVRPostEffectAnimationTest {


    //custom class for test
    private static final class TestPostEffectAnimation extends GVRPostEffectAnimation {
        //constructor
        private TestPostEffectAnimation(GVRPostEffect target, float duration) { super(target, duration); }
        @Override
        protected void animate(GVRHybridObject target, float ratio) {}
    }

    private final float duration = 10.0f;

    private final float[] white = new float[] {255.0f,255.0f,255.0f,255.0f};

    private GVRPostEffect postEffect;
    private GVRContext context;

    @Before
    public void initGVRMaterialAnimationTest() throws Exception {

        context = Mockito.mock(GVRContext.class);
        PowerMockito.mockStatic(SparseArray.class);
        PowerMockito.mockStatic(TextUtils.class);
        PowerMockito.mockStatic(Color.class);

        PowerMockito.mockStatic(Class.forName(NativeClassUtils.NATIVE_POST_EFFECT_DATA));

        SparseArray array = Mockito.mock(SparseArray.class);

        PowerMockito.whenNew(SparseArray.class).withAnyArguments().thenReturn(array);
        PowerMockito.when(TextUtils.isEmpty(Mockito.anyString())).thenReturn(false);

        postEffect = new GVRPostEffect(context, Mockito.mock(GVRPostEffectShaderId.class));
    }

    /** Tests a custom MaterialAnimation class with constructor using GVRSceneObject */
    @Test
    public void newMaterialAnimationClassWithGVRSceneObjectTest() throws Exception {

        final TestPostEffectAnimation animation = new TestPostEffectAnimation(postEffect, duration);
        Assert.assertEquals(postEffect, Whitebox.getInternalState(animation, "mPostEffectData"));
        Assert.assertEquals(duration, animation.getDuration());
    }


}
