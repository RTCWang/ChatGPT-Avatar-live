
package com.proj.avatar.zego.avatar;
import androidx.annotation.NonNull;

import com.zego.avatar.OnAvatarMaskCaptureCallback;
import com.zego.avatar.bean.AvatarCaptureConfig;
import com.zego.avatar.OnAvatarCaptureCallback;
import com.zego.avatar.ZegoAvatarView;
import com.zego.avatar.bean.ZegoAvatarViewState;
import com.zego.avatar.bean.ZegoExpression;
import com.zego.avatar.bean.ZegoFaceFeature;
import com.zego.avatar.bean.ZegoPose;
import com.zego.avatar.ICharacterCallback;
import com.zego.avatar.ZegoCharacter;

import java.util.List;

public class ZegoCharacterHelper {
    public static final String MODEL_ID_MALE = "male";
    public static final String MODEL_ID_FEMALE = "female";
    //****************************** 捏脸维度的 key 值 ******************************/
    public static final String FACESHAPE_BROW_SIZE_Y = "faceshape_brow_size_y";// 眉毛厚度, 取值范围0.0-1.0，默认值0.5
    public static final String FACESHAPE_BROW_SIZE_X = "faceshape_brow_size_x";// 眉毛长度, 取值范围0.0-1.0，默认值0.5
    public static final String FACESHAPE_BROW_ALL_Y = "faceshape_brow_all_y";// 眉毛高度, 取值范围0.0-1.0，默认值0.5
    public static final String FACESHAPE_BROW_ALL_ROLL_Z = "faceshape_brow_all_roll_z";// 眉毛旋转, 取值范围0.0-1.0，默认值0.5
    public static final String FACESHAPE_EYE_SIZE = "faceshape_eye_size"; // 眼睛大小, 取值范围0.0-1.0，默认值0.5
    public static final String FACESHAPE_EYE_SIZE_Y = "faceshape_eye_size_y";
    public static final String FACESHAPE_EYE_ROLL_Y = "faceshape_eye_roll_y";// 眼睛高度, 取值范围0.0-1.0，默认值0.5
    public static final String FACESHAPE_EYE_ROLL_Z = "faceshape_eye_roll_z";// 眼睛旋转, 取值范围0.0-1.0，默认值0.5
    public static final String FACESHAPE_EYE_X = "faceshape_eye_x";// 双眼眼距, 取值范围0.0-1.0，默认值0.5
    public static final String FACESHAPE_NOSE_ALL_X = "faceshape_nose_all_x";// 鼻子宽度, 取值范围0.0-1.0，默认值0.5
    public static final String FACESHAPE_NOSE_ALL_Y = "faceshape_nose_all_y";// 鼻子高度, 取值范围0.0-1.0，默认值0.5
    public static final String FACESHAPE_NOSE_SIZE_Z = "faceshape_nose_size_z";
    public static final String FACESHAPE_NOSE_ALL_ROLL_Y = "faceshape_nose_all_roll_y";// 鼻头旋转, 取值范围0.0-1.0，默认值0.5
    public static final String FACESHAPE_NOSTRIL_ROLL_Y = "faceshape_nostril_roll_y";// 鼻翼旋转, 取值范围0.0-1.0，默认值0.5
    public static final String FACESHAPE_NOSTRIL_X = "faceshape_nostril_x";
    public static final String FACESHAPE_MOUTH_ALL_Y = "faceshape_mouth_all_y";// 嘴巴上下, 取值范围0.0-1.0，默认值0.5
    public static final String FACESHAPE_LIP_ALL_SIZE_Y = "faceshape_lip_all_size_y";// 嘴唇厚度, 取值范围0.0-1.0，默认值0.5
    public static final String FACESHAPE_LIPCORNER_Y = "faceshape_lipcorner_y";// 嘴角旋转, 取值范围0.0-1.0，默认值0.5
    public static final String FACESHAPE_LIP_UPPER_SIZE_X = "faceshape_lip_upper_size_x"; // 上唇宽度, 取值范围0.0-1.0，默认值0.5
    public static final String FACESHAPE_LIP_LOWER_SIZE_X = "faceshape_lip_lower_size_x"; // 下唇宽度, 取值范围0.0-1.0，默认值0.5
    public static final String FACESHAPE_JAW_ALL_SIZE_X = "faceshape_jaw_all_size_x";// 下巴宽度, 取值范围0.0-1.0，默认值0.5
    public static final String FACESHAPE_JAW_Y = "faceshape_jaw_y";// 下巴高度, 取值范围0.0-1.0，默认值0.5
    public static final String FACESHAPE_CHEEK_ALL_SIZE_X = "faceshape_cheek_all_size_x";// 脸颊宽度, 取值范围0.0-1.0，默认值0.5

    //****************************** end of 捏脸维度的 key 值 ******************************/

    private final ZegoCharacterHelperImpl impl;

    /**
     * base.bundle 对应的路径，可以放在asset中，也可以是动态下载的sd卡路径，或者assets拷贝后的路径
     *
     * @param baseResPath 对应resource/android/base.bundle
     */
    public ZegoCharacterHelper(String baseResPath) {
        impl = new ZegoCharacterHelperImpl(baseResPath);
    }

    /**
     * 此接口用于预加载资源，避免初始化Helper花费过长时间
     *
     * @param baseResPath 对应resource/android/base.bundle
     */
    public static void preload(String baseResPath) {
        ZegoCharacterHelperImpl.preload(baseResPath);
    }

    /**
     * Packages所有文件可以动态下载，这里动态下载的目录
     * 如果是内置在app中的，则需要将该目录拷贝到sd卡中（应用私有目录，不能是公开目录），该路径对应的是拷贝后的绝对路径
     *
     * @param path 对应resource/android/Packages
     */
    public void setExtendPackagePath(String path) {
        impl.setExtendPackagePath(path);
    }

    /**
     * @param modeID 男性：MODEL_ID_MALE or 女性：MODEL_ID_FEMALE
     */
    public void setDefaultAvatar(String modeID) {
        impl.setDefaultAvatar(modeID);
    }

    /**
     * 当前设置的获取人模id
     * @return
     */
    public String getModelID() {
        return impl.getModelID();
    }

    /**
     * 获取当前 avatar 的装扮信息, 包装成 json 字符串, 可以保存到后台
     *
     * @return
     */
    public String getAvatarJson() {
        return impl.getAvatarJson();
    }

    /**
     * 把 getAvatarJson 返回的字符串, 重新设置 avatar 的形象
     *
     * @param json
     */
    public void setAvatarJson(String json) {
        impl.setAvatarJson(json);
    }

    /**
     * 设置妆容和服装的资源, 需要确保资源已经在 setExtendPackagePath 指定的路径中
     *
     * @param packageID 资源ID
     */
    public void setPackage(String packageID) {
        impl.setPackage(packageID);
    }

    /**
     * 设置头发颜色, 第一个参数是发根颜色,第二个参数时发梢颜色,可以设置成同一颜色
     *
     * @param rootColor 发根颜色
     * @param mainColor 发梢颜色
     */
    public void setHairColor(ZegoColor rootColor, ZegoColor mainColor) {
        impl.setHairColor(rootColor, mainColor);
    }

    /**
     * 获取当前设置的头发颜色
     *
     * @return
     */
    public ZegoColor[] getHairColor() {
        return impl.getHairColor();
    }

    /**
     * 设置肤色在取色盘中的坐标. （0,0）对应白种人，(0.3, 0.3)对应亚洲人，（1.0,1.0）对应黑人，坐标越大，颜色越深。
     *
     * @param point 坐标值
     */
    public void setSkinColorCoordinates(ZegoPoint point) {
        impl.setSkinColorCoordinates(point);
    }

    /**
     * 获取肤色的坐标, 可以用来在色盘上显示标记
     *
     * @return
     */
    public ZegoPoint getSkinColorCoordinates() {
        return impl.getSkinColorCoordinates();
    }

    /**
     * @param propertyID 捏脸维度的 key 值, 取值范围为ZegoCharacterHelper中定义的FACESHAPE_开头的常量
     * @param floatValue 浮点数, 范围为 0.0 ~ 1.0, 值越大形状变化越大
     */
    public void setFaceShape(String propertyID, Float floatValue) {
        impl.setFaceShape(propertyID, floatValue);
    }

    /**
     * 设置 avatar 表情
     *
     * @param expression 表情数据, 通过 AI detectExpression 返回的或者其他地方事先保存的数据
     * @return
     */
    public boolean setExpression(ZegoExpression expression) {
        return impl.setExpression(expression);
    }

    /**
     * 设置 avatar 姿态
     *
     * @param pose 表情数据, 通过 AI detectPose 返回的或者其他地方事先保存的数据
     * @return
     */
    public boolean setPose(long pose) {
        return impl.setPose(pose);
    }

    /**
     * 设置虚拟形象的大小, 值越大，虚拟形象显示得越小
     *
     * @param pos 范围20-100, 默认60。
     */
    public void setAvatarSize(int pos) {
        impl.setAvatarSize(pos);
    }

    /**
     * 设置虚拟形象模式 0：全身人，1：半身人，2：头像
     * @param viewport
     */
    public void setViewport(ZegoAvatarViewState viewport) {
        impl.setViewport(viewport);
    }

    /**
     * 播放动作，必须在人模动画开关开启时才有效, 可选参数有："relax"、"walk"、"run"、"think"
     * @param animationName
     */
    public void playAnimation(String animationName) {
        impl.playAnimation(animationName);
    }

    /**
     * 播放动作开关
     * @param enableAnimation
     */
    public void enableAnimation(boolean enableAnimation) {
        impl.enableAnimation(enableAnimation);
    }


    /**
     * 相对旋转
     * @param value , 当前帧与前一帧的差值，正负代表方向
     */
    public void rotateCharacter(float value) {
        impl.rotateCharacter(value);
    }

    /**
     * 重置模型旋转角度
     */
    public void resetCharacter(){
        impl.resetCharacter();
    }


    /**
     * 播放舌头动作开关
     * @param enableAnimation
     */
    public void enableTongueAnimation(boolean enableAnimation) {
        impl.enableTongueAnimation(enableAnimation);
    }

    /**
     * 根据人脸特征，设置人物外形和妆容数据
     *
     * @param faceFeature 人脸特征, 通过 AI detectFaceFeature 返回的特征数据
     */
    public void applyFaceFeature(ZegoFaceFeature faceFeature) {
        impl.applyFaceFeature(faceFeature);
    }

    /**
     * 开始采集获取Avatar内容
     * @param config 采集配置参数
     * @param callback 获取内容回调，包含texture
     */
    public void startCaptureAvatar(AvatarCaptureConfig config, OnAvatarCaptureCallback callback){
        impl.startCaptureAvatar(config, callback);
    }

    /**
     * 停止采集获取Avatar内容
     */
    public void stopCaptureAvatar(){
        impl.stopCaptureAvatar();
    }


    /**
     * 开始采集获取Avatar内容
     * @param config 采集配置参数
     * @param callback 获取内容回调，包含texture
     */
    public void startCaptureFaceMaskAvatar(AvatarCaptureConfig config, OnAvatarMaskCaptureCallback callback){
        impl.startCaptureFaceMaskAvatar(config, callback);
    }

    /**
     * 停止采集获取Avatar内容
     */
    public void stopCaptureFaceMaskAvatar(){
        impl.stopCaptureFaceMaskAvatar();
    }

    /**
     * 设置要上屏的view, 必须在 UI线程调用
     *
     * @param view
     */
    public boolean setCharacterView(ZegoAvatarView view) {
        return impl.setCharacterView(view);
    }

    /**
     * 设置要上屏的view, 必须在 UI线程调用
     *
     * @param view
     * @param callback avatar 显示第一帧后回调
     * @return
     */
    public boolean setCharacterView(ZegoAvatarView view, ICharacterCallback callback) {
        return impl.setCharacterView(view, callback);
    }

    /**
     * 判断指定 packageID 的资源在本地是否存在
     * @param packageID
     * @return packageID 代表的资源是否存在
     */
    public boolean hasPackage(String packageID){
        return impl.hasPackage(packageID);
    }

    /**
     * 判断是否有当前package type
     * @param type
     * @return
     */
    public boolean hasPackageType(String type) {
        return impl.hasPackageType(type);
    }


    /**
     * 设置默认表情动画数据
     *
     * @param path 默认表情动画文件路径
     */
    public void setDefaultExpression(String path) {
        impl.setDefaultExpression(path);
    }

    /**
     * 是否开启默认动画
     * @param enable
     */
    public void enableDefaultExpression(boolean enable) {
        impl.enableDefaultExpression(enable);
    }

    /**
     * 检查指定的 avatarJson 中的 pacakge 是否都在本地存在, 不存在的packageID组装成数组返回, 如果都存在, 返回 null
     * @param avatarJson
     * @return avatarJson 中本地不存在的资源ID 列表, 如果都存在, 返回 null
     */
    public List<String> checkAvatarPackage(String avatarJson){
        return impl.checkAvatarPackage(avatarJson);
    }


    /**
     * 颜色结构
     */
    public static class ZegoColor {
        public int r = 0;
        public int g = 0;
        public int b = 0;
        public int a = 0;

        public ZegoColor(int r, int g, int b, int a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }

        @NonNull
        @Override
        public String toString() {
            return a + "," + r + "," + g + "," + b;
        }

        public static ZegoColor fromString(String str) {
            if (null == str) {
                return null;
            }
            String[] split = str.split(",");
            if (split.length != 4) {
                return null;
            }
            return new ZegoColor(Integer.parseInt(split[1]),
                    Integer.parseInt(split[2]),
                    Integer.parseInt(split[3]),
                    Integer.parseInt(split[0]));
        }
    }

    /**
     * 色盘坐标结构
     */
    public static class ZegoPoint {
        public float x = 0;
        public float y = 0;

        public ZegoPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @NonNull
        @Override
        public String toString() {
            return x + "," + y;
        }

        public static ZegoPoint fromString(String str) {
            if (null == str) {
                return null;
            }
            String[] split = str.split(",");
            if (split.length != 2) {
                return null;
            }
            return new ZegoPoint(Integer.parseInt(split[0]),
                    Integer.parseInt(split[1]));
        }
    }

    public ZegoCharacter getCharacter() {
        return impl.getCharacter();
    }
}
