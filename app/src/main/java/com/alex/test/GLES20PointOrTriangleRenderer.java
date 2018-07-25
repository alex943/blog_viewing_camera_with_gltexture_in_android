package com.alex.test;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

// 显示四个点或者显示一个三角形
public class GLES20PointOrTriangleRenderer extends BaseGLRender implements GLSurfaceView.Renderer {

    private final String mVertexShader =
        "attribute vec4 a_Position;" +
            "void main() {" +
            "gl_Position = a_Position;" +
            "gl_PointSize = 20.0;" +
            "}";

    private final String mFragmentShader =
        "void main() {" +
            "gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);" +
        "}";

    // X, Y, Z 对应GL坐标系的位置
    private static final float[] mTriangleVerticesData = {
        // X, Y, Z,
        0.2f, 0.2f, 0.0f,
        -0.2f, -0.2f, 0.0f,
        -0.2f, 0.2f, 0.0f,
        0.2f, -0.2f, 0.0f
    };

    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = mTriangleVerticesData.length;

    private int mProgram;
    private int maPositionHandle;
    private FloatBuffer mTriangleVertices;

    public GLES20PointOrTriangleRenderer(Context context) {
        mTriangleVertices = ByteBuffer.allocateDirect(mTriangleVerticesData.length
            * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1);
        mProgram = createProgram(mVertexShader, mFragmentShader);
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glClear( GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);

        // 向shader中传递3个点的坐标
        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        GLES20.glEnableVertexAttribArray(0);

        // 画一个三角，或者画四个点
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        //GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 4);
    }
}
