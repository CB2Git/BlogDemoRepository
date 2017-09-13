
#### Android矢量动画

引入时间	Android 5.0+ API 21

#### 向下兼容

Android Studio自动兼容

其实就是在编译apk的时候将SVG格式的图片转换为png了而已

如果只想使用SVG图片，那么需要修改 build.gradle 文件以及使用com.android.support:appcompat-v7:23.2.0 +

修改方法

		// Gradle Plugin 2.0+  
		android {  
		defaultConfig {  
			 vectorDrawables.useSupportLibrary = true  
			}  
		}  
		//或更高
		dependencies {
		  compile 'com.android.support:appcompat-v7:23.2.0'
		}

如果使用Gradle版本较低

	 // Gradle Plugin 1.5  
	 android {  
	   defaultConfig {  
	     generatedDensities = []  
	  }  
	
	  // This is handled for you by the 2.0+ Gradle Plugin  
	  aaptOptions {  
	    additionalParameters "--no-version-vectors"  
	  }  
	 }  

在布局文件中使用

	<ImageView
		xmlns:app="http://schemas.android.com/apk/res-auto"
	    android:id="@+id/main_svg_img"
	    android:layout_width="50dp"
	    android:layout_height="50dp"
	    app:srcCompat="@drawable/ic_transfer_within_a_station_black_24dp"/>


需要使用 `app:srcCompat` 并且需要Activity继承自 `AppCompatActivity` ,如果不使用app:srcCompat会直接崩溃，如果不继承AppCompatActivity，那么图片就没了，运行时改变可以直接调用`mSvgImg.setImageResource`方法

[https://android-developers.googleblog.com/2016/02/android-support-library-232.html](https://android-developers.googleblog.com/2016/02/android-support-library-232.html "官方文档")


对于Button来说，使用 `app:srcCompat` 没有啥效果，不过我们可以取巧，为Button设置一个`android:background`为selector，然后在selector中使用SVG图像即可，不过，会崩溃！！！

解决方法，在Activity中加入如下代码

	static {
	    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
	}

**不过设置的selector无效，只能显示默认的那个SVG图片**

[http://www.jianshu.com/p/e3614e7abc03](http://www.jianshu.com/p/e3614e7abc03 "参考链接")


#### 让SVG图片动起来

首先还是使用`vector`标签定义一个图形,对需要使用动画的部分使用`android:name=""`命名，然后使用`animated-vector`标签将`vector`与动画联合起来。

大致格式如下

	<animated-vector
	    xmlns:android="http://schemas.android.com/apk/res/android"
	    android:drawable="@drawable/vector修饰的SVG图片">
	
	    <target
	        android:name="vector中定义的name"
	        android:animation="属性动画"/>
	
	    <target
	        android:name="vector中定义的name"
	        android:animation="属性动画"/>
	
	</animated-vector>


Demo

**SVG图形 searchbar.xml**

	<vector xmlns:android="http://schemas.android.com/apk/res/android"
	        android:width="150dp"
	        android:height="24dp"
	        android:viewportHeight="24"
	        android:viewportWidth="150">
	
	    <path
	        android:name="search"
	        android:pathData="M141,17 A9,9 0 1,1 142,16 L149,23"
	        android:strokeAlpha="0.8"
	        android:strokeColor="#000000"
	        android:strokeLineCap="round"
	        android:strokeWidth="2"/>
	    <path
	        android:name="bar"
	        android:pathData="M0,23 L149,23"
	        android:strokeAlpha="0.8"
	        android:strokeColor="#000000"
	        android:strokeLineCap="square"
	        android:strokeWidth="2"/>
	</vector>


**animated-vector标签**

	<animated-vector
	    xmlns:android="http://schemas.android.com/apk/res/android"
	    android:drawable="@drawable/searchbar">
	
	    <target
	        android:name="search"
	        android:animation="@animator/anim_searchbar_in"/>
	
	    <target
	        android:name="bar"
	        android:animation="@animator/anim_searchbar_out"/>
	
	</animated-vector>

**动画**

可以看到使用的属性动画，修饰的trimPathStart，这是path标签自带的属性，查看全部支持的属性，可以查看[https://developer.android.com/reference/android/graphics/drawable/AnimatedVectorDrawable.html?hl=zh-cn](https://developer.android.com/reference/android/graphics/drawable/AnimatedVectorDrawable.html?hl=zh-cn "官方文档")

	<objectAnimator
	    xmlns:android="http://schemas.android.com/apk/res/android"
	    android:duration="1000"
	    android:propertyName="trimPathStart"
	    android:repeatCount="infinite"
	    android:repeatMode="reverse"
	    android:valueFrom="1"
	    android:valueTo="0"
	    android:valueType="floatType"/>

然后启动动画，需要手动启动

	Drawable drawable = imageView.getDrawable();
	//开始动画，此方法兼容5.0之前
	if (drawable instanceof Animatable) {
	    ((Animatable) drawable).start();
	}