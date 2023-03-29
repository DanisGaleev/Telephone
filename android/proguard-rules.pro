# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#-dontoptimize
#-dontobfuscate
-verbose
-printusage error/usage.txt
-dontwarn com.badlogic.gdx.backends.android.AndroidFragmentApplication
-dontwarn com.badlogic.gdx.utils.GdxBuild
-dontwarn com.badlogic.gdx.physics.box2d.utils.Box2DBuild
-dontwarn com.badlogic.gdx.jnigen.BuildTarget*
-dontwarn com.badlogic.gdx.graphics.g2d.freetype.FreetypeBuild

# Required if using Gdx-Controllers extension
-keep class com.badlogic.gdx.controllers.android.AndroidControllers
#-keep class com.badlogic.gdx.** { *; }
-keep class * implements com.esotericsoftware.kryo.factories.ReflectionSerializerFactory{*;}
-keep class com.esotericsoftware.kryo.serializers.FieldSerializer{*;}
-keep public interface com.esotericsoftware.kryo.factories.SerializerFactory{*;}
-keep public class com.esotericsoftware.reflectasm.**{*;}
-keep public class com.badlogic.gdx.scenes.scene2d.ui.**{*;}
-keep public class com.badlogic.gdx.graphics.g2d.BitmapFont{*;}
-keep public class com.badlogic.gdx.graphics.Color{*;}
-keep public class com.badlogic.gdx.scenes.scene2d.ui.Cell{*;}
# Required if using Box2D extension
-keepclassmembers class com.badlogic.gdx.physics.box2d.World {
   boolean contactFilter(long, long);
   void    beginContact(long);
   void    endContact(long);
   void    preSolve(long, long);
   void    postSolve(long, long);
   boolean reportFixture(long);
   float   reportRayFixture(long, float, float, float, float, float);
}
