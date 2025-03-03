/*
 * BIT Lalpur App
 *
 * Created by Ayaan on 4/14/22, 2:16 PM
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 4/13/22, 8:55 PM
 */



package com.atech.core.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import androidx.navigation.Navigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.atech.core.R
import com.atech.core.data.room.attendance.AttendanceModel
import com.atech.core.data.room.attendance.IsPresent
import com.atech.core.data.ui.notice.Notice3
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit


inline fun NavController.onDestinationChange(crossinline des: ((NavDestination) -> Unit)) =
    this.addOnDestinationChangedListener { _, destination, _ ->
        des.invoke(destination)
    }

/**
 * Open Link
 * @param activity Current Activity
 * @author Ayaan
 * @since 4.0.2
// */
fun String.openLinks(activity: Activity, @StringRes string: Int) {
    try {
        activity.startActivity(Intent(Intent.ACTION_VIEW).also {
            it.data = Uri.parse(this)
        })
    } catch (e: Exception) {
        Toast.makeText(
            activity, activity.resources.getString(string), Toast.LENGTH_SHORT
        ).show()
    }
}

/**
 * Load Image in ImageView in
 * @param parentView Parent View or Context
 * @param view ImageView
 * @param progressBar ProgressBar
 * @author Ayaan
 * @since 4.0.2
 */
fun String.loadImageCircular(
    parentView: View, view: ImageView, progressBar: ProgressBar, @DrawableRes errorImage: Int
) = Glide.with(parentView).load(this).centerCrop().apply(RequestOptions().circleCrop())
    .error(errorImage).transition(DrawableTransitionOptions.withCrossFade())
    .listener(object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: com.bumptech.glide.request.target.Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            progressBar.visibility = View.GONE
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: com.bumptech.glide.request.target.Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            progressBar.visibility = View.GONE
            return false
        }

    }).timeout(10000).into(view)


fun String.loadImageCircular(
    imageView: ImageView
) = imageView.apply {
    Glide.with(context).load(this@loadImageCircular).apply(RequestOptions.circleCropTransform())
        .into(imageView)
}

/**
 * Load image in ImageView
 * @param parentView Parent View or Context
 * @param view ImageView
 * @param progressBar ProgressBar (Nullable)
 * @author Ayaan
 * @since 4.0.2
 */
fun String.loadImage(
    parentView: View,
    view: ImageView,
    progressBar: ProgressBar?,
    cornerRadius: Int = DEFAULT_CORNER_RADIUS,
    @DrawableRes errorImage: Int
) = Glide.with(parentView).load(this).fitCenter().error(errorImage)
    .listener(object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: com.bumptech.glide.request.target.Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            progressBar?.visibility = View.GONE
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: com.bumptech.glide.request.target.Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            progressBar?.visibility = View.GONE
            return false
        }

    }).apply(RequestOptions.bitmapTransform(RoundedCorners(cornerRadius))).timeout(10000)
    .transition(DrawableTransitionOptions.withCrossFade()).into(view)


fun String.loadImageDefault(
    parentView: View, view: ImageView, progressBar: ProgressBar?, @DrawableRes errorImage: Int
) = this.apply {
    Glide.with(parentView).load(this).error(errorImage)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                progressBar?.visibility = View.GONE
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                progressBar?.visibility = View.GONE

                return false
            }

        }).timeout(6000).transition(DrawableTransitionOptions.withCrossFade()).into(view)
}

fun String.loadImageBitMap(
    parentView: View,
    @DrawableRes errorImage: Int,
    customAction: ((Bitmap?) -> Unit)? = null,
) {
    Glide.with(parentView).asBitmap().load(this@loadImageBitMap).error(errorImage)
        .listener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Bitmap>?,
                isFirstResource: Boolean
            ): Boolean = false


            override fun onResourceReady(
                resource: Bitmap?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Bitmap>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                customAction?.invoke(resource)
                return false
            }

        }).submit(100, 100)
}
//

/**
 * Replace all \n or \r to br
 * @return
 * @since 2.0
 */
fun String.replaceNewLineWithBreak() = this.replace("\n|\r\n".toRegex(), "<br>")


/**
 *Covert valid link into Bitmap in background thread
 * @return Notification Builder
 * @throws IOException unused
 * @author Ayaan
 * @since 4.0.3
 */
fun String.applyImageUrl(builder: NotificationCompat.Builder) = runBlocking(Dispatchers.IO) {
    val url = URL(this@applyImageUrl)
    withContext(Dispatchers.IO) {
        try {
            val input = url.openStream()
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            null
        }
    }?.let { bitmap ->
        builder.setLargeIcon(bitmap)
        builder.setStyle(
            NotificationCompat.BigPictureStyle().bigPicture(bitmap).bigLargeIcon(null)
        )
    }
}

/**
 * @since 4.0.3
 * @author Ayaan
 */
@SuppressLint("SimpleDateFormat")
fun Date.convertDateToTime(): String = SimpleDateFormat("dd/MM/yyyy").format(this)

/**
 * @since 4.0.3
 * @author Ayaan
 */
@SuppressLint("SimpleDateFormat")
fun Long.convertLongToTime(pattern: String): String = SimpleDateFormat(pattern).run {
    val date = Date(this@convertLongToTime)
    this.format(date)
}


fun Date.compareDifferenceInDays(date: Date): Int {
    val diff = this.time - date.time
    return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
}

/**
 * @since 4.0.3
 * @author Ayaan
 */
@SuppressLint("SimpleDateFormat")
fun String.convertStringToLongMillis(): Long? = SimpleDateFormat("dd/MM/yyyy").run {
    try {
        val d = this.parse(this@convertStringToLongMillis)
        d?.time
    } catch (e: ParseException) {
        e.printStackTrace()
        null
    }
}

/**
 * @since 4.0.3
 * @author Ayaan
 */
inline fun AttendanceModel.showUndoMessage(
    parentView: View, crossinline action: (AttendanceModel) -> Unit
) = Snackbar.make(
    parentView, "Deleted ${this.subject}", Snackbar.LENGTH_SHORT
).setAction("Undo") {
    action.invoke(this)
}.apply {
    this.setBackgroundTint(
        MaterialColors.getColor(
            parentView.context, com.google.android.material.R.attr.colorSurface, Color.WHITE
        )
    )
    this.setActionTextColor(ContextCompat.getColor(parentView.context, R.color.red))
    this.setTextColor(ContextCompat.getColor(parentView.context, R.color.textColor))
}.show()

@Deprecated("THIS IS DEPRECATED")
inline fun MutableList<AttendanceModel>.showUndoMessage(
    parentView: View, crossinline action: (MutableList<AttendanceModel>) -> Unit
) = Snackbar.make(
    parentView, "Deleted ${this.size}", Snackbar.LENGTH_SHORT
).setAction("Undo") {
    action.invoke(this)
}.apply {
    this.setBackgroundTint(
        MaterialColors.getColor(
            parentView.context, com.google.android.material.R.attr.colorSurface, Color.WHITE
        )
    )
    this.setActionTextColor(ContextCompat.getColor(parentView.context, R.color.red))
    this.setTextColor(ContextCompat.getColor(parentView.context, R.color.textColor))
}.show()


/**
 * @since 4.0.3
 * @author Ayaan
 */
fun View.showSnackBar(message: String, duration: Int) = Snackbar.make(
    this, message, duration
).apply {
    this.setBackgroundTint(
        MaterialColors.getColor(
            this.context, com.google.android.material.R.attr.colorSurface, Color.WHITE
        )
    )
    this.setTextColor(ContextCompat.getColor(this@showSnackBar.context, R.color.textColor))
}.show()


/**
 * @since 4.0.3
 * @author Ayaan
 */
fun View.showSnackBar(
    message: String, duration: Int, actionName: String?, action: (() -> Unit)?
) = Snackbar.make(
    this, message, duration
).apply {
    this.setBackgroundTint(
        MaterialColors.getColor(
            this.context, com.google.android.material.R.attr.colorSurface, Color.WHITE
        )
    )
    this.setTextColor(ContextCompat.getColor(this@showSnackBar.context, R.color.textColor))
    action?.let { action ->
        this.setActionTextColor(ContextCompat.getColor(this@showSnackBar.context, R.color.red))
        setAction(actionName) {
            action.invoke()
        }
    }
}.show()


/**
 * @since 4.0.3
 * @author Ayaan
 */
fun ArrayList<IsPresent>.countTotalClass(size: Int, isPresent: Boolean): Int {
    var days = 1
    val removeIndex = arrayListOf<Int>()
    for ((index, i) in this.withIndex()) {
        if (this.last().day.convertLongToTime("dd/mm/yyyy") == i.day.convertLongToTime("dd/mm/yyyy") && i.isPresent == isPresent) {
            days++
            if (size - 1 != index) {
                removeIndex.add(index)
            }
        }
    }
    for (r in removeIndex.reversed()) {
        this.removeAt(r)
    }
    return days
}


/**
 * This is only use in FragmentAddEdit.kt
 * @param type condition of attendance
 * @since 4.0.3
 * @author Ayaan
 */
fun String.getAndSetHint(type: String): Int = when (this) {
    type -> 0
    else -> this.toInt()
}

/**
 * Detect current theme
 * @since 4.0.3
 * @author Ayaan
 *
 */
fun Context.isDark() =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES


/**
 * Return color in hex for webView
 * @param context Current Context
 * @author Ayaan
 * @since 4.0.3
 */
fun getColorForBackground(context: Context) = context.run {
    if (this.isDark()) "rgb(23, 26, 31)"
    else "rgb(255,255,255)"
}

/**
 * Return color in hex for webView
 * @param context Current Context
 * @author Ayaan
 * @since 4.0.3
 */
fun getColorForText(context: Context) = context.run {
    if (this.isDark()) "rgb(255,255,255)"
    else "rgb(0,0,0)"
}


///**
// * Custom Back Pressed
// * @since 4.0.3
// * @author Ayaan
// */
//fun Fragment.handleCustomBackPressed(
//    onBackPressed: OnBackPressedCallback.() -> Unit
//) {
//    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
//        onBackPressed()
//    }
//}

/**
 * Recycler View
 * @since 4.0.4
 * @author Ayaan
 */
inline fun RecyclerView.onScrollChange(
    crossinline lambda1: (RecyclerView) -> Unit, crossinline lambda2: (RecyclerView) -> Unit
) = this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (dy > 0) lambda2.invoke(recyclerView)
        else if (dy < 0) lambda1.invoke(recyclerView)
    }
})


/**
 * Change Color
 * @since 4.0.4
 * @author Ayaan
// */
fun Activity.changeStatusBarToolbarColor(@IdRes id: Int, @AttrRes colorCode: Int) = this.apply {
    try {
        val window = window
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window?.statusBarColor = MaterialColors.getColor(
            this, colorCode, Color.WHITE
        )
        this.findViewById<Toolbar>(id).setBackgroundColor(
            MaterialColors.getColor(
                this, colorCode, Color.WHITE
            )
        )
    } catch (e: Exception) {
        Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Change Color of ImageView
 * @since 4.0.5
 * @author Ayaan
 */
fun Activity.changeStatusBarToolbarColorImageView(@ColorInt colorCode: Int) = this.apply {
    try {
        val window = window
        window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window?.statusBarColor = colorCode
    } catch (e: Exception) {
        Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Change Status Bar Color
 * @since 4.0.4
 * @author Ayaan
 */
@Suppress("DEPRECATION")
fun setStatusBarUiTheme(activity: Activity?, isLight: Boolean) {
    activity?.window?.decorView?.let {
        it.systemUiVisibility =
            if (isLight) it.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // dark icons
            else it.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() // light icons
    }
}

/**
 * Provide android pending intent
 * @since 4.0.4
 * @author Ayaan
 */
fun getPendingIntentFlag() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_ONE_SHOT


@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
fun checkForAPI33() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

/**
 * Open Custom Tab
 * @since 4.0.4
 * @author Ayaan
 */
fun Context.openCustomChromeTab(link: String) = this.run {
    val defaultColors = CustomTabColorSchemeParams.Builder().setToolbarColor(
        MaterialColors.getColor(
            this, androidx.appcompat.R.attr.colorAccent, Color.RED
        )
    ).build()
    try {
        val customTabIntent =
            CustomTabsIntent.Builder().setDefaultColorSchemeParams(defaultColors).build()
        customTabIntent.intent.`package` = "com.android.chrome"
        customTabIntent.launchUrl(this, Uri.parse(link))
    } catch (e: Exception) {
        Toast.makeText(this, "Invalid Link", Toast.LENGTH_SHORT).show()
    }
}

/**
 * BottomNav Change color
 * @since 4.0.4
 * @author Ayaan
 */
fun Activity.changeBottomNav(@AttrRes color: Int) = this.apply {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) window.navigationBarColor =
        MaterialColors.getColor(
            this, color, Color.RED
        )
}


/**
 * BottomNav Change color
 * @since 4.0.5
 * @author Ayaan
 */
fun Activity.changeBottomNavImageView(@ColorInt color: Int) = this.apply {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) window.navigationBarColor = color
}


/**
 * Change Theme
 * @since 4.0.4
 * @author Ayaan
 */
fun setAppTheme(type: Int) {
    AppCompatDelegate.setDefaultNightMode(type)
}

/**
 * Change Theme
 * @since 4.0.5
 * @author Ayaan
 */
fun isColorDark(@ColorInt color: Int): Boolean {
    val darkness: Double =
        1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
    return darkness >= 0.5
}


/**
 * Get Link for Notification type
 * @author Ayaan
 * @since 4.0.5
 */
fun Notice3.getImageLinkNotification(): String = when (this.sender) {
    "App Notice" -> "https://firebasestorage.googleapis.com/v0/b/theaiyubit.appspot.com/o/Utils%2Fapp_notification.png?alt=media&token=0a7babfe-bf59-4d19-8fc0-98d7fde151a6"
    else -> "https://firebasestorage.googleapis.com/v0/b/theaiyubit.appspot.com/o/Utils%2Fcollege_notifications.png?alt=media&token=c5bbfda0-c73d-4af1-9c3c-cb29a99d126b"
}


/**
 * Open Play Store
 * @author Ayaan
 * @since 4.0.5
 */
fun Activity.openPlayStore(name: String) {
    startActivity(Intent(
        Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$name")
    ).also {
        it.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    })
}

inline fun Activity.onScrollColorChange(
    scrollView: NestedScrollView, crossinline to: (() -> Unit), crossinline from: (() -> Unit)
) = scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
    when (scrollY) {
        0 -> {
            to.invoke()
        }

        else -> {
            from.invoke()
        }
    }
}

fun <T1, T2, T3, T4, T5, T6, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    transform: suspend (T1, T2, T3, T4, T5, T6) -> R
): Flow<R> = combine(
    combine(flow, flow2, flow3, ::Triple), combine(flow4, flow5, flow6, ::Triple)
) { t1, t2 ->
    transform(
        t1.first, t1.second, t1.third, t2.first, t2.second, t2.third
    )
}

fun <T1, T2, T3, T4, R> combineFourFlows(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    transform: suspend (T1, T2, T3, T4) -> R
): Flow<R> = combine(
    combine(flow, flow2, ::Pair), combine(flow3, flow4, ::Pair)
) { t1, t2 ->
    transform(
        t1.first,
        t1.second,
        t2.first,
        t2.second,
    )
}

@ColorInt
fun Context.getColorFromAttr(
    @AttrRes attrColor: Int
): Int {
    val typedArray = theme.obtainStyledAttributes(intArrayOf(attrColor))
    val textColor = typedArray.getColor(0, 0)
    typedArray.recycle()
    return textColor
}

fun getRgbFromHex(hex: String): String {
    val initColor = Color.parseColor(hex)
    val r = Color.red(initColor)
    val g = Color.green(initColor)
    val b = Color.blue(initColor)
    return "rgb($r,$g,$b)"
}

@Suppress("DEPRECATION")
fun hasNetwork(context: Context): Boolean? {
    var isConnected: Boolean? = false // Initial Value
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
    if (activeNetwork != null && activeNetwork.isConnected)
        isConnected = true
    return isConnected
}

// fun to open app settings page
fun Context.openAppSettings() = this.apply {
    val intent = Intent()
    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    val uri = Uri.fromParts("package", this.packageName, null)
    intent.data = uri
    this.startActivity(intent)
}

fun ConstraintLayout.setHorizontalBias(
    @IdRes targetViewId: Int,
    verticalBias: Float,
    horizontalBias: Float = 0.0f

) {
    val constraintSet = ConstraintSet()
    constraintSet.clone(this)
    constraintSet.setHorizontalBias(targetViewId, horizontalBias)
    constraintSet.setVerticalBias(targetViewId, verticalBias)
    constraintSet.applyTo(this)
}

fun <T> mergeList(list1: List<T>, list2: List<T>, list3: List<T>): List<T> {
    val list = mutableListOf<T>()
    list.addAll(list1)
    list.addAll(list2)
    list.addAll(list3)
    return list
}

inline fun <F : Fragment> navigateToDestination(
    fragment: F,
    direction: NavDirections,
    extras: Navigator.Extras? = null,
    crossinline transition: (F) -> Unit = {
        it.apply {
            exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
            reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        }
    },
    crossinline onError: (Exception) -> Unit = {}
) = fragment.apply {
    try {
        transition.invoke(this)
        if (extras != null) {
            findNavController().navigate(direction, extras)
        } else {
            findNavController().navigate(direction)
        }
    } catch (e: Exception) {
        onError.invoke(e)
    }
}