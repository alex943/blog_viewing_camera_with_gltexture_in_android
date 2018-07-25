package com.alex.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

// 使用Gl Texture显示一张图
public class GLES20TextureRenderer extends BaseGLRender implements GLSurfaceView.Renderer {

    private final String mVertexShader =
        "attribute vec4 a_Position;\n" +
        "attribute vec2 a_TextureCoord;\n" +
        "varying vec2 vTextureCoord;\n" +
        "void main() {\n" +
            "gl_Position = a_Position;\n" +
            "vTextureCoord = a_TextureCoord;\n" +
        "}\n";

    // 传vTextureCoord是因为texture坐标系与GL不同，
    // 由于jpg格式采样坐标与gl texture在Y轴上正好相反，所以在映射坐标的时候原y坐标乘以-1。
    private final String mFragmentShader =
        "precision mediump float;\n" +
        "varying vec2 vTextureCoord;\n" +
        "uniform sampler2D sTexture;\n" +
            "void main() {\n" +
            "gl_FragColor = texture2D(sTexture, vTextureCoord * vec2(1.0, -1.0));\n" +
            "}\n";

    // T, U 分别对应的是纹理坐标系的位置
    private static final float[] mPosVerticesData = {
        // X, Y, Z, T, U
        0.8f, -0.8f, 0.0f, 1, 0,
        -0.8f, -0.8f, 0.0f, 0, 0,
        0.8f, 0.8f, 0.0f, 1,1,
        -0.8f, 0.8f, 0.0f, 0,1
    };

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;

    private int mProgram;
    private int mTextureID;
    private int maPositionHandle, maTextureHandle;
    private FloatBuffer mPosVertices;

    private Context mContext;

    public GLES20TextureRenderer(Context context) {
        Log.d(TAG, "GLES20TextureRenderer constructor");
        mContext = context;
        // always throw java.nio.BufferOverflowException without scaling FLOAT_SIZE_BYTES
        mPosVertices = ByteBuffer.allocateDirect(mPosVerticesData.length * FLOAT_SIZE_BYTES).
            order(ByteOrder.nativeOrder()).asFloatBuffer();
        mPosVertices.put(mPosVerticesData).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1);
        mProgram = createProgram(mVertexShader, mFragmentShader);

        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }

        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "a_TextureCoord");
        checkGlError("glGetUniformLocation a_TextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for maTextureHandle");
        }

        /*
         * Create our texture. This has to be done each time the
         * surface is created.
         */
        // 声明，并生成一个texture
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        // 绑定 texture
        mTextureID = textures[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);
        // 最邻近方式取texture像素
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR);
        // 纹理坐标超出范围时候的处理方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_REPEAT);

        InputStream is = mContext.getResources()
            .openRawResource(R.raw.city);
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch(IOException e) {
                // Ignore.
            }
        }

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged");
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d(TAG, "onDrawFrame");
        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID);

        mPosVertices.position(0);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
            VERTICES_DATA_STRIDE_BYTES, mPosVertices);
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glVertexAttribPointer maPosition");

        mPosVertices.position(3);
        GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
            VERTICES_DATA_STRIDE_BYTES, mPosVertices);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");
    }
}
