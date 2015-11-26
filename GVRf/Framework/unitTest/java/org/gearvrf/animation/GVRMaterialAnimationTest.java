package org.gearvrf.animation;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.SparseArray;

import junit.framework.Assert;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterialShaderId;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRStockMaterialShaderId;
import org.gearvrf.testUtils.NativeClassUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({SparseArray.class, TextUtils.class, GVRMaterialShaderId.class, GVRMaterialAnimation.class,  Color.class})
public class GVRMaterialAnimationTest {

    //custom class for test
    private static final class TestMaterialAnimation extends GVRMaterialAnimation {
        //constructor
        private TestMaterialAnimation(GVRSceneObject target, float duration) { super((GVRSceneObject)target, duration); }
        @Override
        protected void animate(GVRHybridObject target, float ratio) {
            //nothing to implement
        }
    }

    private final float duration = 10.0f;

    private final float[] white = new float[] {255.0f,255.0f,255.0f,255.0f};

    private GVRMaterial material;
    private GVRContext context;

    @Before
    public void initGVRMaterialAnimationTest() throws Exception {

        context = Mockito.mock(GVRContext.class);

        material = PowerMockito.mock(GVRMaterial.class);
    }

    /** Tests a custom MaterialAnimation class with constructor using GVRSceneObject */
    @Test
    public void newMaterialAnimationClassWithGVRSceneObjectTest() throws Exception {

        final GVRRenderData renderData = Mockito.mock(GVRRenderData.class);
        Mockito.when(renderData.getMaterial()).thenReturn(material);
        final GVRSceneObject object = Mockito.mock(GVRSceneObject.class);
        Mockito.when(object.getRenderData()).thenReturn(renderData);

        final TestMaterialAnimation animation = new TestMaterialAnimation(object, duration);
        animation.animate(material, 2.0f);

        Assert.assertEquals(material, Whitebox.getInternalState(animation, "mMaterial"));

    }


}
