package com.proj.avatar.utils;

import android.opengl.GLES20;
import android.opengl.Matrix;


/**
 * 把纹理渲染到屏幕上
 */
public class TextureBgRender {


    private static String TAG = ":TextureBgRender";

    private final FullFrameRect preFrameRect;
    private int inputTextureID;
    private int outputTextureID = -1;
    private int outWidth, outHeight;
    private boolean mDestroyed = false;

    private float[] mBgColor = new float[4];
    private int muColor = -1;
    private boolean mUseFBO = false;
    private int mFBO = -1;

    public TextureBgRender(int inputTextureID, boolean useFbo, int outWidth, int outHeight, Texture2dProgram.ProgramType type) {
        this.inputTextureID = inputTextureID;
        this.outWidth = outWidth;
        this.outHeight = outHeight;
        mUseFBO = useFbo;
        if(mUseFBO) {
            this.outputTextureID = GlUtil.createImageTexture(null, outWidth, outHeight, GLES20.GL_RGBA);
            mFBO = GlUtil.createFrameBuffer();
        }

        Texture2dProgram preProgram = new Texture2dProgram(type);
        preFrameRect = new FullFrameRect(preProgram);
        GLES20.glUseProgram(preProgram.getProgramHandle());
        muColor = GLES20.glGetUniformLocation(preProgram.getProgramHandle(), "uColor");
        GlUtil.checkLocation(muColor, "uColor");
    }

    public int getOutputTextureID(){
        return outputTextureID;
    }

    public void setBgColor(float r, float g, float b, float a){
        mBgColor[0] = r;
        mBgColor[1] = g;
        mBgColor[2] = b;
        mBgColor[3] = a;
    }

    public void destroy() {
        mDestroyed = true;
        preFrameRect.release(true);
        if(mUseFBO){
            mUseFBO = false;
            GlUtil.deleteTexture(outputTextureID);
            GlUtil.deleteFrameBuffer(mFBO);
        }
    }

    public void draw(boolean revertY) {
        if(mDestroyed){
            return;
        }

        if(mUseFBO)
            GlUtil.bindFramebuffer(mFBO, outputTextureID);
        GLES20.glUseProgram(preFrameRect.getProgram().getProgramHandle());
        GLES20.glUniform4fv(muColor, 1, mBgColor, 0);
        if(revertY){
            GLES20.glViewport(0, 0, outWidth, outHeight);
            float[] matrix = new float[]{
                    1.0f,
                    0.0f,
                    0.0f,
                    0.0f,
                    0.0f,
                    -1.0f,
                    0.0f,
                    0.0f,
                    0.0f,
                    0.0f,
                    1.0f,
                    0.0f,
                    0.0f,
                    0.0f,
                    0.0f,
                    1.0f,
            };
            preFrameRect.drawFrame(this.inputTextureID, matrix, GlUtil.IDENTITY_MATRIX);
        }else{
            GLES20.glViewport(0, 0, outWidth, outHeight);
            preFrameRect.drawFrame(this.inputTextureID);
        }

        if(mUseFBO)
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

    }

    public void draw() {
        if(mDestroyed){
            return;
        }

        if(mUseFBO)
            GlUtil.bindFramebuffer(mFBO, outputTextureID);

        GLES20.glViewport(0, 0, outWidth, outHeight);
        GLES20.glUseProgram(preFrameRect.getProgram().getProgramHandle());
        GLES20.glUniform4fv(muColor, 1, mBgColor, 0);
        preFrameRect.drawFrame(this.inputTextureID);

        if(mUseFBO)
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public void draw(int width, int height, boolean reverseY) {
        if(mDestroyed){
            return;
        }

        if(mUseFBO)
            GlUtil.bindFramebuffer(mFBO, outputTextureID);

        float scale = Math.min(outWidth * 1.0f / width, outHeight * 1.0f / height);
        GLES20.glViewport((int) ((outWidth - width * scale) / 2), (int) ((outHeight - height * scale) / 2), (int) (width * scale), (int) (height * scale));
//        GLES20.glViewport(0, 0, outWidth, outHeight);
        float[] matrix = new float[16];
        Matrix.setIdentityM(matrix,0);
        GLES20.glUniform4fv(muColor, 1, mBgColor, 0);
        preFrameRect.drawFrame(this.inputTextureID, matrix);

        if(mUseFBO)
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public void setInputTexture(int inputTextureID) {
        this.inputTextureID = inputTextureID;
    }

    public int getInputTexture() {
        return inputTextureID;
    }
}
