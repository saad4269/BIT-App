package com.atech.bit.ui.fragments.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.room.withTransaction
import com.atech.bit.NavGraphDirections
import com.atech.bit.R
import com.atech.bit.databinding.FragmentHomeBinding
import com.atech.bit.ui.activity.main_activity.viewmodels.CommunicatorViewModel
import com.atech.bit.ui.activity.main_activity.viewmodels.PreferenceManagerViewModel
import com.atech.bit.ui.activity.main_activity.viewmodels.UserDataViewModel
import com.atech.bit.ui.custom_views.DividerItemDecorationNoLast
import com.atech.bit.ui.fragments.course.CourseFragment
import com.atech.bit.ui.fragments.course.sem_choose.adapters.SyllabusOnlineAdapter
import com.atech.bit.ui.fragments.event.EventsAdapter
import com.atech.bit.ui.fragments.home.adapter.AttendanceHomeAdapter
import com.atech.bit.ui.fragments.home.adapter.HolidayHomeAdapter
import com.atech.bit.ui.fragments.home.adapter.HomeLibraryAdapter
import com.atech.bit.ui.fragments.home.adapter.SyllabusHomeAdapter
import com.atech.bit.ui.fragments.universal_dialog.UniversalDialogFragment
import com.atech.bit.utils.CardViewHighlightContent
import com.atech.bit.utils.Encryption.decryptText
import com.atech.bit.utils.Encryption.getCryptore
import com.atech.bit.utils.SyllabusEnableModel
import com.atech.bit.utils.addMenuHost
import com.atech.bit.utils.bindData
import com.atech.bit.utils.compareToCourseSem
import com.atech.bit.utils.getUid
import com.atech.bit.utils.launchWhenStarted
import com.atech.bit.utils.loadAdds
import com.atech.bit.utils.openBugLink
import com.atech.bit.utils.sortBySno
import com.atech.core.api.syllabus.Semester
import com.atech.core.api.syllabus.SubjectModel
import com.atech.core.data.network.user.UserModel
import com.atech.core.data.preferences.Cgpa
import com.atech.core.data.room.BitDatabase
import com.atech.core.data.room.library.LibraryModel
import com.atech.core.data.room.syllabus.SyllabusList
import com.atech.core.data.room.syllabus.SyllabusModel
import com.atech.core.data.ui.events.Events
import com.atech.core.utils.ANN_LINK
import com.atech.core.utils.ANN_MESSAGE
import com.atech.core.utils.ANN_NEG_BUTTON
import com.atech.core.utils.ANN_POS_BUTTON
import com.atech.core.utils.ANN_TITLE
import com.atech.core.utils.ANN_VERSION
import com.atech.core.utils.CalendarReminder
import com.atech.core.utils.DataState
import com.atech.core.utils.GITHUB_LINK
import com.atech.core.utils.KEY_COURSE_OPEN_FIRST_TIME
import com.atech.core.utils.KEY_CURRENT_SHOW_TIME
import com.atech.core.utils.KEY_DO_NOT_SHOW_AGAIN
import com.atech.core.utils.KEY_HOME_NOTICE_ANNOUNCEMENT_CARD_VIEW
import com.atech.core.utils.KEY_IS_USER_LOG_IN
import com.atech.core.utils.KEY_REACH_TO_HOME
import com.atech.core.utils.KEY_SHOW_TIMES
import com.atech.core.utils.KEY_TOGGLE_SYLLABUS_SOURCE_ARRAY
import com.atech.core.utils.KEY_USER_HAS_DATA_IN_DB
import com.atech.core.utils.MAX_TIME_TO_SHOW_CARD
import com.atech.core.utils.REQUEST_EVENT_FROM_HOME
import com.atech.core.utils.REQUEST_LOGIN_FROM_HOME
import com.atech.core.utils.REQUEST_UPDATE_SEM
import com.atech.core.utils.RemoteConfigUtil
import com.atech.core.utils.TAG
import com.atech.core.utils.TAG_REMOTE
import com.atech.core.utils.calculatedDays
import com.atech.core.utils.changeStatusBarToolbarColor
import com.atech.core.utils.checkForAPI33
import com.atech.core.utils.compareDifferenceInDays
import com.atech.core.utils.findPercentage
import com.atech.core.utils.loadImageCircular
import com.atech.core.utils.navigateToDestination
import com.atech.core.utils.onScrollColorChange
import com.atech.core.utils.openCustomChromeTab
import com.atech.core.utils.showSnackBar
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.HttpException
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Date
import javax.inject.Inject
import kotlin.math.ceil

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private val binding: FragmentHomeBinding by viewBinding()
    private val viewModel: HomeViewModel by viewModels()
    private val communicatorViewModel: CommunicatorViewModel by activityViewModels()
    private val preferencesManagerViewModel: PreferenceManagerViewModel by activityViewModels()
    private val userDataViewModel by activityViewModels<UserDataViewModel>()
    private var defPercentage = 75
    private lateinit var syllabusPeAdapter: SyllabusHomeAdapter
    private lateinit var syllabusTheoryAdapter: SyllabusHomeAdapter
    private lateinit var syllabusLabAdapter: SyllabusHomeAdapter
    private lateinit var holidayAdapter: HolidayHomeAdapter
    private lateinit var onlineTheoryAdapter: SyllabusOnlineAdapter
    private lateinit var onlineLabAdapter: SyllabusOnlineAdapter
    private lateinit var onlinePEAdapter: SyllabusOnlineAdapter
    private var userModel: UserModel? = null

    @Inject
    lateinit var db: FirebaseFirestore

    @Inject
    lateinit var pref: SharedPreferences

    @Inject
    lateinit var auth: FirebaseAuth


    @Inject
    lateinit var bitDatabase: BitDatabase


    @Inject
    lateinit var remoteConfigUtil: RemoteConfigUtil

    private var courseSem: String = ""

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        if (myScrollViewerInstanceState != null) {
            binding.scrollViewHome.onRestoreInstanceState(CourseFragment.myScrollViewerInstanceState)
        }

        holidayAdapter = HolidayHomeAdapter()
        binding.apply {
            lineChartCgpa.setNoDataText(resources.getString(R.string.loading))
            fragmentHomeExt.apply {
                setting.setOnClickListener {
                    navigateToWelcomeScreen()
                }
                edit.setOnClickListener {
                    navigateToEdit()
                }
            }

            attendanceClick.root.setOnClickListener {
                navigateToAttendance()
            }


            textShowAllHoliday.setOnClickListener {
                navigateToHoliday()
            }
            textViewEdit.setOnClickListener {
                navigateToCGPA()
            }
        }


//        SetUpSyllabus
        settingUpSyllabus()

//        Preference Manager
        preferenceManager()

//        set Progress
        setProgressBar()


//        setUp Event
        getEvent()

//        Handle on Destroy
        detectScroll()

        setDefaultValueForSwitch()

//        setTimeTable()
        getData()
        getOnlineSyllabus()
        setHoliday()

        createMenu()



        setUpLinkClick()

        checkHasData()
        setPref()
        restoreScroll()
        setAds()
        getOldAppWarningDialog()
        clearAndAddSyllabusDatabase()

        checkPermission()
        getHoliday()
        setOnlineSyllabusView()
        switchClick()
        setLibraryWarningScreen()
        setShortcuts()
        setAnnouncement()
        showAnnouncementDialog()
    }

    private fun setAnnouncement() = binding.cardViewAnnouncement.apply {
        val times = pref.getInt(KEY_HOME_NOTICE_ANNOUNCEMENT_CARD_VIEW, 1)
        root.isVisible = times < MAX_TIME_TO_SHOW_CARD
        if (times < MAX_TIME_TO_SHOW_CARD) {
            CardViewHighlightContent(
                "Notice Section",
                "Notice Section is now on top of the appbar",
                R.drawable.ic_notice
            ).also {
                bindData(it)
            }
        }
    }

    private fun setLibraryWarningScreen() = binding.layoutHomeLibrary.apply {
        val homeLibraryAdapter = HomeLibraryAdapter(
            onDeleteClick = {
                deleteBook(it)
            },
            onMarkAsReturnClick = {
                markAsReturn(it)
            }
        )
        recyclerViewShowBooks.apply {
            adapter = homeLibraryAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(false)
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(this)
            libraryIndicator.attachToRecyclerView(this, snapHelper)
            homeLibraryAdapter.registerAdapterDataObserver(libraryIndicator.adapterDataObserver)
        }
        viewModel.getLibrary().observe(viewLifecycleOwner) {
            val v = it.filter { book ->
                val diff = Date(book.returnDate).compareDifferenceInDays(
                    Date(System.currentTimeMillis())
                )
                diff in 0..3 && !book.markAsReturn
            }
            root.isVisible = v.isNotEmpty()
            homeLibraryAdapter.submitList(v)
        }
    }

    private fun deleteBook(it: LibraryModel) {
        if (it.eventId != -1L) {
            CalendarReminder.deleteEvent(requireContext(), it.eventId)
        }
        viewModel.deleteBook(it)
    }

    private fun markAsReturn(it: LibraryModel) {
        val markAsReturn = it.markAsReturn
        if (it.eventId != -1L) {
            CalendarReminder.deleteEvent(requireContext(), it.eventId)
        }
        viewModel.updateBook(
            it.copy(
                eventId = -1L, alertDate = 0L, markAsReturn = !markAsReturn
            )
        )
    }


    private fun setSource(courseSem: String) {
        val source = viewModel.syllabusEnableModel.compareToCourseSem(courseSem)
        binding.fragmentHomeExt.switchOldNew.isChecked = source
        setText(source)
        layoutChanges(source)
    }

    private fun setOnlineSyllabusView() {
        onlineTheoryAdapter = SyllabusOnlineAdapter(true) { pos ->
            navigateToViewOnlineSyllabus(pos)
        }
        onlineLabAdapter = SyllabusOnlineAdapter(true) {
            navigateToViewOnlineSyllabus(it)
        }.also { it.setType("Lab") }
        onlinePEAdapter = SyllabusOnlineAdapter(true) {
            navigateToViewOnlineSyllabus(it)
        }.also { it.setType("Pe") }

        binding.fragmentHomeExt.semChoseOnlineExt
            .constraintLayoutOnline.setPadding(0, 0, 0, 0)
        binding.fragmentHomeExt.semChoseOnlineExt.recyclerViewOnlineSyllabus.apply {
            adapter = ConcatAdapter(onlineTheoryAdapter, onlineLabAdapter, onlinePEAdapter)
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecorationNoLast(
                requireContext(), LinearLayoutManager.VERTICAL
            ).apply {
                setDrawable(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.divider
                    )
                )
            })
        }
    }

    private fun navigateToViewOnlineSyllabus(model: SubjectModel) {
        Log.d(TAG, "navigateToViewOnlineSyllabus: $courseSem")
        val action =
            HomeFragmentDirections.actionHomeFragmentToViewSyllabusFragment(
                model.subjectName,
                courseSem.lowercase()
            )
        navigateToDestination(this, action)
    }

    private fun switchClick() = binding.fragmentHomeExt.switchOldNew.apply {
        setOnCheckedChangeListener { _, isChecked ->
            setText(isChecked)
            layoutChanges(isChecked)
            binding.fragmentHomeExt.edit.isVisible = !isChecked
        }
    }

    private fun setText(isEnable: Boolean) {
        binding.fragmentHomeExt.switchOldNew.text =
            if (isEnable) resources.getString(R.string.online)
            else resources.getString(R.string.offline)
    }

    private fun layoutChanges(isEnable: Boolean) = binding.fragmentHomeExt.apply {
        semChoseOnlineExt.root.isVisible = isEnable
        materialCardViewSyllabusRecyclerView.isVisible = !isEnable
    }

    private fun checkPermission() {
        if (checkForAPI33()) {
            checkNotificationPermission()
            requestPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkNotificationPermission() {
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    Log.d(TAG, "checkNotificationPermission: Granted")
                } else {
                    Snackbar.make(
                        binding.root,
                        "Please grant Notification permission from App Settings",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "requestPermission: Granted")
        } else {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun clearAndAddSyllabusDatabase() {
        val isSyllabusDataRefresh = pref.getBoolean(KEY_COURSE_OPEN_FIRST_TIME, true)
        if (isSyllabusDataRefresh) launchWhenStarted {
            bitDatabase.withTransaction {
                bitDatabase.syllabusDao().deleteAll()
                SyllabusList.syllabus.also {
                    bitDatabase.syllabusDao().insertAll(it)
                }
                pref.edit().putBoolean(KEY_COURSE_OPEN_FIRST_TIME, false).apply()
            }
        }
    }

    private fun setAds() {
        requireContext().loadAdds(binding.adView)
    }

    private fun setPref() {
        pref.edit().putBoolean(
            KEY_REACH_TO_HOME, true
        ).apply()
    }

    private fun checkHasData() {
        val isLogin = pref.getBoolean(KEY_IS_USER_LOG_IN, false)
        if (auth.currentUser != null && isLogin) userDataViewModel.checkUserData(getUid(auth)!!, {
            if (!it) {
                preferencesManagerViewModel.preferencesFlow.observe(viewLifecycleOwner) { dataStore ->
                    userDataViewModel.addCourseSem(getUid(auth)!!,
                        dataStore.course,
                        dataStore.sem,
                        {
                            pref.edit().putBoolean(KEY_USER_HAS_DATA_IN_DB, true).apply()
                        }) {
                        Toast.makeText(
                            requireContext(), "Data upload failed", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }) {}
    }

    private fun setUpLinkClick() {
        binding.layoutNoteDev.tagInfo.setOnClickListener {
            remoteConfigUtil.fetchData({
                Log.e(TAG, "setUpLinkClick: ${it.message}")
            }) {
                try {
                    val link = remoteConfigUtil.getString(GITHUB_LINK).trim()
                    requireActivity().startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                } catch (e: Exception) {
                    Log.e(TAG, "setUpLinkClick: ${e.message}")
                }
            }
        }
    }

    private fun getOldAppWarningDialog() {
        val u = pref.getBoolean(KEY_DO_NOT_SHOW_AGAIN, false)
        if (isOldAppInstalled() && !communicatorViewModel.uninstallDialogSeen && !u) navigateToUninstallOldAppDialog()
    }

    private fun navigateToUninstallOldAppDialog() {
        val action = NavGraphDirections.actionGlobalUninstallOldAppDialog()
        navigateToDestination(this, action)
    }

    @Suppress("deprecation")
    private fun isOldAppInstalled(): Boolean {
        var available = true
        try {
            // check if available
            requireContext().packageManager.getPackageInfo("com.aatec.bit", 0)
        } catch (e: PackageManager.NameNotFoundException) {
            // if not available set
            // available as false
            available = false
        }
        return available
    }

    private fun navigateToCGPA() {
        val action = HomeFragmentDirections.actionHomeFragmentToCgpaCalculatorFragment()
        navigateToDestination(this, action, transition = {
            exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true).apply {
                duration = resources.getInteger(R.integer.duration_medium).toLong()
            }
            reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false).apply {
                duration = resources.getInteger(R.integer.duration_medium).toLong()
            }
        })
    }


    private fun createMenu() {
        addMenuHost(R.menu.menu_toolbar, {
            Handler(Looper.getMainLooper()).post {
                val menuItem = it.findItem(R.id.menu_profile)
                val view = menuItem.actionView as FrameLayout
                val imageView = view.findViewById<ImageView>(R.id.toolbar_profile_image)
                imageView?.let {
                    setProfileImageView(it)
                }
            }
        }) {
            when (it.itemId) {
                R.id.menu_notice -> {
                    navigateToNotice()
                    true
                }

                else -> false
            }
        }
    }

    private fun navigateToNotice() {
        val action = HomeFragmentDirections.actionHomeFragmentToNoticeFragment()
        navigateToDestination(this, action, transition = {
            exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true).apply {
                duration = resources.getInteger(R.integer.duration_medium).toLong()
            }
            reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false).apply {
                duration = resources.getInteger(R.integer.duration_medium).toLong()
            }
        })
    }

    private fun setProfileImageView(imageView: ImageView) {
        imageView.apply {
            if (auth.currentUser != null) {
                auth.currentUser?.photoUrl.toString().loadImageCircular(this)
                getDataOFUser()
                setOnClickListener {
                    userModel?.let {
                        navigateToProfile(getUid(auth)!!, it)
                    }
                }
            } else {
                imageView.setImageResource(
                    R.drawable.ic_account
                )
                setOnClickListener {
                    navigateToLogin()
                }
            }
        }
    }

    private fun navigateToLogin() {
        val action =
            HomeFragmentDirections.actionHomeFragmentToLogInFragment(request = REQUEST_LOGIN_FROM_HOME)
        navigateToDestination(this, action)
    }

    private fun getDataOFUser() = launchWhenStarted {
        val uid = getUid(auth)!!
        userDataViewModel.getUser(uid, {
            userModel = convertEncryptedData(uid, it)
        }, {
            Toast.makeText(requireContext(), "Something went wrong !!", Toast.LENGTH_SHORT).show()
        })
    }

    private fun convertEncryptedData(uid: String, user: UserModel): UserModel? {
        try {
            val cryptore = context?.getCryptore(uid)
            val email = cryptore?.decryptText(user.email)
            val name = cryptore?.decryptText(user.name)
            val profilePic = cryptore?.decryptText(user.profilePic)
            return UserModel(
                email = email,
                name = name,
                profilePic = profilePic,
                uid = user.uid,
                syncTime = user.syncTime
            )

        } catch (e: Exception) {
            Log.e(TAG, "convertEncryptedData: $e")
            return null
        }
    }

    private fun navigateToProfile(uid: String, userDecrypt: UserModel) {
        val action = HomeFragmentDirections.actionHomeFragmentToProfileFragment(uid, userDecrypt)
        navigateToDestination(this, action)
    }

    private fun setHoliday() = binding.apply {
        showHoliday.apply {
            adapter = holidayAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
            addItemDecoration(
                DividerItemDecorationNoLast(
                    requireContext(),
                    LinearLayoutManager.VERTICAL
                ).apply {
                    setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider))
                })
        }
        holidayAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }


    /**
     * @author Ayaan
     * @since 4.0.3
     */
    private fun getEvent() {
        val eventAdapter = EventsAdapter(
            db,
            {
                navigateToImageView(it)
            },
        ) { event, rootView ->
            navigateToEventDetail(event, rootView)
        }
        binding.apply {
            showEvent.apply {
                adapter = eventAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                val snapHelper = PagerSnapHelper()
                snapHelper.attachToRecyclerView(this)
                eventIndicator.attachToRecyclerView(this, snapHelper)
                eventAdapter.registerAdapterDataObserver(eventIndicator.adapterDataObserver)
            }
            eventAdapter.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            textShowAllEvent.setOnClickListener {
                navigateToEvent()
            }
        }
        viewModel.getEvent(
            communicatorViewModel.instanceBefore14Days!!,
            communicatorViewModel.instanceAfter15Days!!
        ).observe(viewLifecycleOwner) {
            it?.let {
                eventAdapter.submitList(it)
                binding.materialCardViewEventRecyclerView.isVisible = it.isNotEmpty()
                binding.textEvent.isVisible = it.isNotEmpty()
                binding.textShowAllEvent.isVisible = it.isNotEmpty()
                binding.eventIndicator.isVisible = it.isNotEmpty()
            }
        }

    }

    private fun navigateToImageView(link: String) {
        val action = NavGraphDirections.actionGlobalViewImageFragment(link)
        navigateToDestination(this, action)
    }

    /**
     * @author Ayaan
     * @since 4.0.3
     */
    private fun navigateToEvent() {
        val action = HomeFragmentDirections.actionHomeFragmentToEventFragment()
        navigateToDestination(this, action, transition = {
            exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true).apply {
                duration = resources.getInteger(R.integer.duration_medium).toLong()
            }
            reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false).apply {
                duration = resources.getInteger(R.integer.duration_medium).toLong()
            }
        })
    }

    /**
     * @author Ayaan
     * @since 4.0.3
     */
    private fun navigateToEventDetail(event: Events, view: View) {
        val extras = FragmentNavigatorExtras(view to event.path)
        val action = NavGraphDirections.actionGlobalEventDetailFragment(
            path = event.path, request = REQUEST_EVENT_FROM_HOME
        )
        navigateToDestination(this, action, extras = extras, transition = {
            enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
            returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        })
    }


    private fun navigateToEdit() {
        val directions = NavGraphDirections.actionGlobalEditSubjectBottomSheet()
        navigateToDestination(this, directions)
    }


    private fun navigateToAttendance() {
        val action = HomeFragmentDirections.actionHomeFragmentToAttendanceFragment()
        navigateToDestination(this, action, transition = {
            enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
            returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        })
    }

    private fun setProgressBar() {
        val attendanceHomeAdapter = AttendanceHomeAdapter(defPercentage)
        binding.attendanceClick.recyclerViewShowAttendance.apply {
            adapter = attendanceHomeAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(false)
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(this)
            binding.attendanceClick.attendanceIndicator.attachToRecyclerView(this, snapHelper)
            attendanceHomeAdapter.registerAdapterDataObserver(binding.attendanceClick.attendanceIndicator.adapterDataObserver)
        }
        viewModel.attAttendance.observe(viewLifecycleOwner) {
            attendanceHomeAdapter.submitList(it)
            var sumPresent = 0
            var sumTotal = 0
            it.forEach { at ->
                sumPresent += at.present
                sumTotal += at.total
            }
            val finalPercentage =
                findPercentage(sumPresent.toFloat(), sumTotal.toFloat()) { present, total ->
                    when (total) {
                        0.0F -> 0.0F
                        else -> ((present / total) * 100)
                    }
                }
            setCondition(finalPercentage.toInt(), sumPresent, sumTotal)
            binding.attendanceClick.apply {
                textViewTotal.text = sumTotal.toString()
                textViewPresent.text = sumPresent.toString()
                val df = DecimalFormat("#.#")
                df.roundingMode = RoundingMode.FLOOR
                textViewOverAllAttendance.text =
                    resources.getString(R.string.overallAttendance, df.format(finalPercentage))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setCondition(per: Int, present: Int, total: Int) {
        binding.cardViewAttendance.isVisible = per != 0
        when {
            per >= defPercentage -> {
                val day = calculatedDays(present, total) { p, t ->
                    (((100 * p) - (defPercentage * t)) / defPercentage)
                }.toInt()
                binding.attendanceClick.textViewStats.text = when {
                    per == defPercentage || day == 0 -> "On track don't miss next class"

                    day > 0 -> "You can leave $day class"
                    else -> "Error !!"
                }
            }

            else -> {
                val day = calculatedDays(present, total) { p, t ->
                    (((defPercentage * t) - (100 * p)) / (100 - defPercentage))
                }
                binding.attendanceClick.textViewStats.text =
                    "Attend Next ${(ceil(day)).toInt()} Class"
            }
        }
    }


    private fun navigateToWelcomeScreen() {
        val action = NavGraphDirections.actionGlobalChooseSemBottomSheet(REQUEST_UPDATE_SEM)
        navigateToDestination(this, action)
    }

    private fun preferenceManager() {
        preferencesManagerViewModel.preferencesFlow.observe(viewLifecycleOwner) {
            courseSem = "${it.course}${it.sem}"
            setSource(courseSem.lowercase())
            viewModel.syllabusQuery.value = courseSem
            binding.materialCardViewCgpaGraph.isVisible = !it.cgpa.isAllZero
            binding.textViewCgpa.isVisible = !it.cgpa.isAllZero
            binding.textViewEdit.isVisible = !it.cgpa.isAllZero
            if (!it.cgpa.isAllZero) setUpChart(it.cgpa)
        }
    }

    private fun setUpChart(cgpa: Cgpa) = launchWhenStarted {
        binding.lineChartCgpa.apply {
            val list = mutableListOf(
                Entry(0f, 0f)
            )
            addData(list, cgpa)
            val barDataSet = LineDataSet(list, "SGPA")
            barDataSet.apply {
                color = MaterialColors.getColor(
                    binding.root, androidx.appcompat.R.attr.colorPrimary, Color.WHITE
                )
                valueTextSize = 10f
                valueTextColor =
                    MaterialColors.getColor(binding.root, R.attr.textColor, Color.WHITE)
            }
            val barData = LineData(barDataSet)
            xAxis.setLabelCount(list.size, /*force: */true)
            legend.textColor = MaterialColors.getColor(binding.root, R.attr.textColor, Color.WHITE)
            xAxis.textColor = MaterialColors.getColor(binding.root, R.attr.textColor, Color.WHITE)
            axisLeft.textColor =
                MaterialColors.getColor(binding.root, R.attr.textColor, Color.WHITE)
            axisRight.textColor =
                MaterialColors.getColor(binding.root, R.attr.textColor, Color.WHITE)
            description.text = "Average CGPA :- ${DecimalFormat("#0.00").format(cgpa.cgpa)}"
            description.textColor =
                MaterialColors.getColor(binding.root, R.attr.textColor, Color.WHITE)
            setPinchZoom(false)
            setScaleEnabled(false)
            this.data = barData
        }
    }

    private fun addData(list: MutableList<Entry>, cgpa: Cgpa) {
        if (cgpa.sem1 != 0.0) list.add(Entry(1f, cgpa.sem1.toFloat()))
        if (cgpa.sem2 != 0.0) list.add(Entry(2f, cgpa.sem2.toFloat()))
        if (cgpa.sem3 != 0.0) list.add(Entry(3f, cgpa.sem3.toFloat()))
        if (cgpa.sem4 != 0.0) list.add(Entry(4f, cgpa.sem4.toFloat()))
        if (cgpa.sem5 != 0.0) list.add(Entry(5f, cgpa.sem5.toFloat()))
        if (cgpa.sem6 != 0.0) list.add(Entry(6f, cgpa.sem6.toFloat()))
    }

    private fun settingUpSyllabus() {
        syllabusTheoryAdapter = SyllabusHomeAdapter(listener = { s, v ->
            setOnSyllabusClickListener(s, v)
        })
        syllabusLabAdapter = SyllabusHomeAdapter(listener = { s, v ->
            setOnSyllabusClickListener(s, v)
        })
        syllabusPeAdapter = SyllabusHomeAdapter(listener = { s, v ->
            setOnSyllabusClickListener(s, v)
        })
        binding.fragmentHomeExt.apply {

            showTheory.apply {
                adapter = syllabusTheoryAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            showLab.apply {
                adapter = syllabusLabAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            showPe.apply {
                adapter = syllabusPeAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

        }
        syllabusLabAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        syllabusLabAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        syllabusPeAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

    }

    private fun getOnlineSyllabus() {
        viewModel.getOnlineSyllabus().observe(viewLifecycleOwner) { dataState ->
            when (dataState) {
                DataState.Empty -> {}
                is DataState.Error -> {
                    if (dataState.exception is HttpException) {
                        binding.root.showSnackBar(
                            "${dataState.exception.message}", Snackbar.LENGTH_SHORT, "Report"
                        ) {
//                            setViewOfOnlineSyllabusExt(false)
                            requireActivity().openBugLink(
                                com.atech.core.R.string.bug_repost,
                                "${this.javaClass.simpleName}.class",
                                dataState.exception.message
                            )
                        }
                    }

                }

                DataState.Loading -> {
                    binding.fragmentHomeExt.semChoseOnlineExt.apply {
                        progressBarLoading.isVisible = true
                        noData.isVisible = false
                        noDataText.isVisible = false
                    }
                }

                is DataState.Success -> {
                    dataState.data.semester?.let { syllabus ->
                        setOnLineData(syllabus) //
                    }
                    setViewOfOnlineSyllabusExt(dataState.data.semester != null)
                }
            }
        }
    }

    private fun setOnLineData(data: Semester) {
        onlineTheoryAdapter.submitList(data.subjects.theory)
        onlineLabAdapter.setStartPos(data.subjects.theory.size)
        onlineLabAdapter.submitList(data.subjects.lab)
        onlinePEAdapter.setStartPos(data.subjects.theory.size + data.subjects.lab.size)

        onlinePEAdapter.submitList(data.subjects.pe)
    }


    private fun setViewOfOnlineSyllabusExt(isVisible: Boolean) =
        binding.fragmentHomeExt.semChoseOnlineExt.apply {
            progressBarLoading.isVisible = false
            noData.isVisible = !isVisible
            noDataText.isVisible = !isVisible
            recyclerViewOnlineSyllabus.isVisible = isVisible
            textView6.isVisible = false
        }

    private fun getData() {
        viewModel.theory.observe(viewLifecycleOwner) {
            binding.fragmentHomeExt.showTheory.isVisible = it.isNotEmpty()
            binding.fragmentHomeExt.textView6.isVisible = it.isNotEmpty()
            setOfflineNoData(
                binding.fragmentHomeExt.showTheory.isVisible,
                binding.fragmentHomeExt.showPe.isVisible,
                binding.fragmentHomeExt.showLab.isVisible
            )
            syllabusTheoryAdapter.submitList(it)
        }

        viewModel.lab.observe(viewLifecycleOwner) {
            binding.fragmentHomeExt.showLab.isVisible = it.isNotEmpty()
            binding.fragmentHomeExt.textView7.isVisible = it.isNotEmpty()
            binding.fragmentHomeExt.dividerTheory.isVisible = it.isNotEmpty()
            setOfflineNoData(
                binding.fragmentHomeExt.showTheory.isVisible,
                binding.fragmentHomeExt.showPe.isVisible,
                binding.fragmentHomeExt.showLab.isVisible
            )
            syllabusLabAdapter.submitList(it)
        }
        viewModel.pe.observe(viewLifecycleOwner) {
            binding.fragmentHomeExt.showPe.isVisible = it.isNotEmpty()
            binding.fragmentHomeExt.textView8.isVisible = it.isNotEmpty()
            binding.fragmentHomeExt.dividerLab.isVisible = it.isNotEmpty()
            setOfflineNoData(
                binding.fragmentHomeExt.showTheory.isVisible,
                binding.fragmentHomeExt.showPe.isVisible,
                binding.fragmentHomeExt.showLab.isVisible
            )
            syllabusPeAdapter.submitList(it)
        }

    }

    private fun setOfflineNoData(theory: Boolean, lab: Boolean, pe: Boolean) =
        binding.fragmentHomeExt.apply {
            lvNoData.isVisible = !(theory || lab || pe)
            lvContent.isVisible = theory || lab || pe
        }

    private fun getHoliday() {
        viewModel.getHoliday().observe(viewLifecycleOwner) { dateState ->
            when (dateState) {
                is DataState.Success -> {
                    binding.apply {
                        textHoliday.isVisible = true
                        materialCardViewHolidayRecyclerView.isVisible = true
                        textShowAllHoliday.isVisible = true
                    }
                    holidayAdapter.submitList(dateState.data.holidays.sortBySno())
                }

                DataState.Empty -> binding.apply {
                    textHoliday.isVisible = false
                    materialCardViewHolidayRecyclerView.isVisible = false
                    textShowAllHoliday.isVisible = false
                }

                is DataState.Error -> binding.root.showSnackBar(
                    "${dateState.exception.message}", -1
                )

                DataState.Loading -> {
                }
            }
        }
    }

    private fun navigateToHoliday() {
        val action = HomeFragmentDirections.actionHomeFragmentToHolidayFragment()
        navigateToDestination(this, action, transition = {
            enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
            returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true)
        })
    }

    //        Syllabus
    private fun setOnSyllabusClickListener(syllabusModel: SyllabusModel, view: View) {
        val extras = FragmentNavigatorExtras(view to syllabusModel.openCode)
        val action = NavGraphDirections.actionGlobalSubjectHandlerFragment(syllabusModel)
        navigateToDestination(this, action, extras, transition = {
            exitTransition = MaterialElevationScale(false).apply {
                duration = resources.getInteger(R.integer.duration_medium).toLong()
            }
            reenterTransition = MaterialElevationScale(true).apply {
                duration = resources.getInteger(R.integer.duration_small).toLong()
            }
        })
    }

    /**
     * @since 4.0.4
     * @author Ayaan
     */
    private fun detectScroll() {
        activity?.onScrollColorChange(binding.scrollViewHome, {
            activity?.changeStatusBarToolbarColor(
                R.id.toolbar, com.google.android.material.R.attr.colorSurface
            )
        }, {
            activity?.changeStatusBarToolbarColor(
                R.id.toolbar, R.attr.bottomBar
            )
        })

    }

    /**
     * @author Ayaan
     * @since 4.0.3
     */
    private fun restoreScroll() {
        try {
            if (communicatorViewModel.homeNestedViewPosition != null) {
                binding.scrollViewHome.scrollTo(
                    0, communicatorViewModel.homeNestedViewPosition!!
                )
            }
        } catch (e: Exception) {
            Log.e("Error", e.message!!)
        }
    }

    private fun setDefaultValueForSwitch() {
        remoteConfigUtil.fetchData({
            Log.e(TAG_REMOTE, "setDefaultValueForSwitch: $it")
        }) {
            val switchState = remoteConfigUtil.getString(KEY_TOGGLE_SYLLABUS_SOURCE_ARRAY)
            Log.d(TAG_REMOTE, "setDefaultValueForSwitch: $switchState")
            pref.edit()
                .putString(KEY_TOGGLE_SYLLABUS_SOURCE_ARRAY, switchState)
                .apply()
        }
        setSyllabusEnableModel()
    }

    private fun setSyllabusEnableModel() {
        val source = pref.getString(
            KEY_TOGGLE_SYLLABUS_SOURCE_ARRAY,
            resources.getString(R.string.def_value_online_syllabus)
        )
        viewModel.syllabusEnableModel =
            Gson().fromJson(source, SyllabusEnableModel::class.java)
    }

    private fun setShortcuts() = binding.fragmentShortcutsExt.apply {
        buttonCgpa.setOnClickListener { navigateToCGPA() }
        buttonLibraryManager.setOnClickListener { navigateToLibraryManager() }
        buttonSociety.setOnClickListener { navigateToSociety() }
        buttonIssue.setOnClickListener { activity?.openCustomChromeTab(resources.getString(R.string.issue_link)) }
    }



    private fun navigateToLibraryManager() {
        val action = HomeFragmentDirections.actionHomeFragmentToLibraryFragment()
        navigateToDestination(this, action, transition = {
            exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true).apply {
                duration = resources.getInteger(R.integer.duration_medium).toLong()
            }
            reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false).apply {
                duration = resources.getInteger(R.integer.duration_medium).toLong()
            }
        })
    }

    private fun navigateToSociety() {
        val action = HomeFragmentDirections.actionHomeFragmentToSocietyFragment()
        navigateToDestination(this, action, transition = {
            exitTransition = MaterialSharedAxis(MaterialSharedAxis.Y, true).apply {
                duration = resources.getInteger(R.integer.duration_medium).toLong()
            }
            reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false).apply {
                duration = resources.getInteger(R.integer.duration_medium).toLong()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        myScrollViewerInstanceState = binding.scrollViewHome.onSaveInstanceState()
    }

    companion object {
        var myScrollViewerInstanceState: Parcelable? = null
    }

    private fun showAnnouncementDialog() {
        if (viewModel.isAnnouncementDialogShown) {
            viewModel.isAnnouncementDialogShown = false
            val currentShowTime = pref.getInt(KEY_CURRENT_SHOW_TIME, 1)
            val showTimes = pref.getInt(KEY_SHOW_TIMES, MAX_TIME_TO_SHOW_CARD)
            val annVersion = remoteConfigUtil.getLong(ANN_VERSION).toInt()
            if (currentShowTime <= showTimes && annVersion != 1) {
                remoteConfigUtil.fetchData({}) {
                    val annTitle = remoteConfigUtil.getString(ANN_TITLE)
                    val annMessage = remoteConfigUtil.getString(ANN_MESSAGE)
                    val annLink = remoteConfigUtil.getString(ANN_LINK)
                    val annPosButton = remoteConfigUtil.getString(ANN_POS_BUTTON)
                    val annNegButton = remoteConfigUtil.getString(ANN_NEG_BUTTON)
                    UniversalDialogFragment.UniversalDialogData(
                        annTitle,
                        annMessage,
                        annPosButton,
                        annNegButton,
                        annLink
                    ).also {
                        navigateToUniversalDialog(it)
                    }
                }
                pref.edit().putInt(KEY_CURRENT_SHOW_TIME, currentShowTime + 1).apply()
            }
        }
    }

    private fun navigateToUniversalDialog(data: UniversalDialogFragment.UniversalDialogData) {
        val action = NavGraphDirections.actionGlobalUniversalDialogFragment(data)
        navigateToDestination(this, action)
    }


}