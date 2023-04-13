/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.proj.avatar.utils;


import java.nio.FloatBuffer;
import java.util.Arrays;

/**
 * Base class for stuff we like to draw.
 */
public class Drawable2d {
    private static final int SIZEOF_FLOAT = 4;

    /**
     * Simple equilateral triangle (1.0 per side).  Centered on (0,0).
     */
    private static final float TRIANGLE_COORDS[] = {
         0.0f,  0.577350269f,   // 0 top
        -0.5f, -0.288675135f,   // 1 bottom left
         0.5f, -0.288675135f    // 2 bottom right
    };
    private static final float TRIANGLE_TEX_COORDS[] = {
        0.5f, 0.0f,     // 0 top center
        0.0f, 1.0f,     // 1 bottom left
        1.0f, 1.0f,     // 2 bottom right
    };
    private static final FloatBuffer TRIANGLE_BUF =
            GlUtil.createFloatBuffer(TRIANGLE_COORDS);
    private static final FloatBuffer TRIANGLE_TEX_BUF =
            GlUtil.createFloatBuffer(TRIANGLE_TEX_COORDS);

    /**
     * Simple square, specified as a triangle strip.  The square is centered on (0,0) and has
     * a size of 1x1.
     * <p>
     * Triangles are 0-1-2 and 2-1-3 (counter-clockwise winding).
     */
    private static final float RECTANGLE_COORDS[] = {
        -0.5f, -0.5f,   // 0 bottom left
         0.5f, -0.5f,   // 1 bottom right
        -0.5f,  0.5f,   // 2 top left
         0.5f,  0.5f,   // 3 top right
    };
    private static final float RECTANGLE_TEX_COORDS[] = {
        0.0f, 1.0f,     // 0 bottom left
        1.0f, 1.0f,     // 1 bottom right
        0.0f, 0.0f,     // 2 top left
        1.0f, 0.0f      // 3 top right
    };
    private static final FloatBuffer RECTANGLE_BUF =
            GlUtil.createFloatBuffer(RECTANGLE_COORDS);
    private static final FloatBuffer RECTANGLE_TEX_BUF =
            GlUtil.createFloatBuffer(RECTANGLE_TEX_COORDS);

    /**
     * A "full" square, extending from -1 to +1 in both dimensions.  When the model/view/projection
     * matrix is identity, this will exactly cover the viewport.
     * <p>
     * The texture coordinates are Y-inverted relative to RECTANGLE.  (This seems to work out
     * right with external textures from SurfaceTexture.)
     */
    private static final float FULL_RECTANGLE_COORDS[] = {
        -1.0f, -1.0f,   // 0 bottom left
         1.0f, -1.0f,   // 1 bottom right
        -1.0f,  1.0f,   // 2 top left
         1.0f,  1.0f,   // 3 top right
    };
    private static final float FULL_RECTANGLE_TEX_COORDS[] = {
            0.0f, 1.0f,     // 0 bottom left
            1.0f, 1.0f,     // 1 bottom right
            0.0f, 0.0f,     // 2 top left
            1.0f, 0.0f      // 3 top right
    };
    private static final FloatBuffer FULL_RECTANGLE_BUF =
            GlUtil.createFloatBuffer(FULL_RECTANGLE_COORDS);
    private static final FloatBuffer FULL_RECTANGLE_TEX_BUF =
            GlUtil.createFloatBuffer(FULL_RECTANGLE_TEX_COORDS);


    private float[] vertices;
    private FloatBuffer mVertexArray;
    private float[] textureCoords;
    private FloatBuffer mTexCoordArray;
    private int mVertexCount;
    private int mCoordsPerVertex;
    private int mVertexStride;
    private int mTexCoordStride;
    private Prefab mPrefab;

    /**
     * Enum values for constructor.
     */
    public enum Prefab {
        TRIANGLE, RECTANGLE, FULL_RECTANGLE
    }

    /**
     * Prepares a drawable from a "pre-fabricated" shape definition.
     * <p>
     * Does no EGL/GL operations, so this can be done at any time.
     */
    public Drawable2d(Prefab shape) {
        switch (shape) {
            case TRIANGLE:
                mVertexArray = TRIANGLE_BUF;
                mTexCoordArray = TRIANGLE_TEX_BUF;
                mCoordsPerVertex = 2;
                mVertexStride = mCoordsPerVertex * SIZEOF_FLOAT;
                mVertexCount = TRIANGLE_COORDS.length / mCoordsPerVertex;
                break;
            case RECTANGLE:
                mVertexArray = RECTANGLE_BUF;
                mTexCoordArray = RECTANGLE_TEX_BUF;
                mCoordsPerVertex = 2;
                mVertexStride = mCoordsPerVertex * SIZEOF_FLOAT;
                mVertexCount = RECTANGLE_COORDS.length / mCoordsPerVertex;
                break;
            case FULL_RECTANGLE:
                mVertexArray = FULL_RECTANGLE_BUF;
                mTexCoordArray = FULL_RECTANGLE_TEX_BUF;
                mCoordsPerVertex = 2;
                mVertexStride = mCoordsPerVertex * SIZEOF_FLOAT;
                mVertexCount = FULL_RECTANGLE_COORDS.length / mCoordsPerVertex;
                break;
            default:
                throw new RuntimeException("Unknown shape " + shape);
        }
        mTexCoordStride = 2 * SIZEOF_FLOAT;
        mPrefab = shape;
    }

    public void setTransformation(final Transformation transformation) {
        if (mPrefab != Prefab.FULL_RECTANGLE) {
            return;
        }

        vertices = Arrays.copyOf(FULL_RECTANGLE_COORDS, FULL_RECTANGLE_COORDS.length);
        textureCoords = new float[8];

        if (transformation.cropRect != null) {
            resolveCrop(transformation.cropRect.x, transformation.cropRect.y,
                    transformation.cropRect.width, transformation.cropRect.height);
        } else {
            resolveCrop(Transformation.FULL_RECT.x, Transformation.FULL_RECT.y,
                    Transformation.FULL_RECT.width, Transformation.FULL_RECT.height);
        }
        resolveFlip(transformation.flip);
        resolveRotate(transformation.rotation);
        if (transformation.inputSize != null && transformation.outputSize != null) {
            resolveScale(transformation.inputSize.width, transformation.inputSize.height,
                    transformation.outputSize.width, transformation.outputSize.height,
                    transformation.scaleType);
        }

        mVertexArray = GlUtil.createFloatBuffer(vertices);
        mTexCoordArray = GlUtil.createFloatBuffer(textureCoords);
    }

    private void resolveCrop(float x, float y, float width, float height) {
        float minX = x;
        float minY = y;
        float maxX = minX + width;
        float maxY = minY + height;

        // left bottom
        textureCoords[0] = minX;
        textureCoords[1] = minY;
        // right bottom
        textureCoords[2] = maxX;
        textureCoords[3] = minY;
        // left top
        textureCoords[4] = minX;
        textureCoords[5] = maxY;
        // right top
        textureCoords[6] = maxX;
        textureCoords[7] = maxY;
    }

    private void resolveFlip(int flip) {
        switch (flip) {
            case Transformation.FLIP_HORIZONTAL:
                swap(textureCoords, 0, 2);
                swap(textureCoords, 4, 6);
                break;
            case Transformation.FLIP_VERTICAL:
                swap(textureCoords, 1, 5);
                swap(textureCoords, 3, 7);
                break;
            case Transformation.FLIP_HORIZONTAL_VERTICAL:
                swap(textureCoords, 0, 2);
                swap(textureCoords, 4, 6);

                swap(textureCoords, 1, 5);
                swap(textureCoords, 3, 7);
                break;
            case Transformation.FLIP_NONE:
            default:
                break;
        }
    }

    private void resolveRotate(int rotation) {
        float x, y;
        switch (rotation) {
            case Transformation.ROTATION_90:
                x = textureCoords[0];
                y = textureCoords[1];
                textureCoords[0] = textureCoords[4];
                textureCoords[1] = textureCoords[5];
                textureCoords[4] = textureCoords[6];
                textureCoords[5] = textureCoords[7];
                textureCoords[6] = textureCoords[2];
                textureCoords[7] = textureCoords[3];
                textureCoords[2] = x;
                textureCoords[3] = y;
                break;
            case Transformation.ROTATION_180:
                swap(textureCoords, 0, 6);
                swap(textureCoords, 1, 7);
                swap(textureCoords, 2, 4);
                swap(textureCoords, 3, 5);
                break;
            case Transformation.ROTATION_270:
                x = textureCoords[0];
                y = textureCoords[1];
                textureCoords[0] = textureCoords[2];
                textureCoords[1] = textureCoords[3];
                textureCoords[2] = textureCoords[6];
                textureCoords[3] = textureCoords[7];
                textureCoords[6] = textureCoords[4];
                textureCoords[7] = textureCoords[5];
                textureCoords[4] = x;
                textureCoords[5] = y;
                break;
            case Transformation.ROTATION_0:
            default:
                break;
        }
    }

    private void resolveScale(int inputWidth, int inputHeight, int outputWidth, int outputHeight,
            int scaleType) {
        if (scaleType == Transformation.SCALE_TYPE_FIT_XY) {
            // The default is FIT_XY
            return;
        }

        // Note: scale type need to be implemented by adjusting
        // the vertices (not textureCoords).
        if (inputWidth * outputHeight == inputHeight * outputWidth) {
            // Optional optimization: If input w/h aspect is the same as output's,
            // there is no need to adjust vertices at all.
            return;
        }

        float inputAspect = inputWidth / (float) inputHeight;
        float outputAspect = outputWidth / (float) outputHeight;

        if (scaleType == Transformation.SCALE_TYPE_CENTER_CROP) {
            if (inputAspect < outputAspect) {
                float heightRatio = outputAspect / inputAspect;
                vertices[1] *= heightRatio;
                vertices[3] *= heightRatio;
                vertices[5] *= heightRatio;
                vertices[7] *= heightRatio;
            } else {
                float widthRatio = inputAspect / outputAspect;
                vertices[0] *= widthRatio;
                vertices[2] *= widthRatio;
                vertices[4] *= widthRatio;
                vertices[6] *= widthRatio;
            }
        } else if (scaleType == Transformation.SCALE_TYPE_CENTER_INSIDE) {
            if (inputAspect < outputAspect) {
                float widthRatio = inputAspect / outputAspect;
                vertices[0] *= widthRatio;
                vertices[2] *= widthRatio;
                vertices[4] *= widthRatio;
                vertices[6] *= widthRatio;
            } else {
                float heightRatio = outputAspect / inputAspect;
                vertices[1] *= heightRatio;
                vertices[3] *= heightRatio;
                vertices[5] *= heightRatio;
                vertices[7] *= heightRatio;
            }
        }
    }

    private void swap(float[] arr, int index1, int index2) {
        float temp = arr[index1];
        arr[index1] = arr[index2];
        arr[index2] = temp;
    }

    /**
     * Returns the array of vertices.
     * <p>
     * To avoid allocations, this returns internal state.  The caller must not modify it.
     */
    public FloatBuffer getVertexArray() {
        return mVertexArray;
    }

    /**
     * Returns the array of texture coordinates.
     * <p>
     * To avoid allocations, this returns internal state.  The caller must not modify it.
     */
    public FloatBuffer getTexCoordArray() {
        return mTexCoordArray;
    }

    /**
     * Returns the number of vertices stored in the vertex array.
     */
    public int getVertexCount() {
        return mVertexCount;
    }

    /**
     * Returns the width, in bytes, of the data for each vertex.
     */
    public int getVertexStride() {
        return mVertexStride;
    }

    /**
     * Returns the width, in bytes, of the data for each texture coordinate.
     */
    public int getTexCoordStride() {
        return mTexCoordStride;
    }

    /**
     * Returns the number of position coordinates per vertex.  This will be 2 or 3.
     */
    public int getCoordsPerVertex() {
        return mCoordsPerVertex;
    }

    @Override
    public String toString() {
        if (mPrefab != null) {
            return "[Drawable2d: " + mPrefab + "]";
        } else {
            return "[Drawable2d: ...]";
        }
    }
}
