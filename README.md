# MJIsGood
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
![Android Build](https://github.com/mym0404/MJIsGood/workflows/Android%20Build/badge.svg)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

## Preview

<img src="1.gif" width="200px" /> <img src="2.gif" width="200px" /> <img src="3.gif" width="200px" /> <img src="4.gif" width="200px" />
## Contents

- [Library](#library)
- [Feature](#feature)
  * [week #1](#assignment-1) 
  * [week #2](#assignment-2)
  * [week #3](#assignment-3)
  * [week #6](#assignment-6)
- [What to learn](#what-to-learn)
  * [week #1](#assignment-1-1)
  * [week #2](#assignment-2-1)
  * [week #3](#assignment-3-1)
  * [week #6](#assignment-6-1)
- [Checkout date](#checkout-date)
- [Contributors](#contributors-)

## Library
- Kotlin standard library
- Glide
- Dagger, Hilt
- LoremIpsum
- Leakcanary
- Kotlin Coroutine
- Retrofit
- FlowBinding

#### Jetpack
- DataBinding, ViewBinding
- Core
- Activity
- Fragment
- ConstraintLayout
- SwipeRefreshLayout
- Material design components
- Security-crypto
- DataStore
- Biometric
- Lifecycle
- Navigation
- Hilt

## Feature

#### Assignment #1
- Sign-up
- Sign-in
- Auto sign-in
- Form validation
- Switch dark theme

#### Assignment #2
- Show items in list
- Drag items in list
- Swipe items in list
- Delete items in list

#### Assignment #3
- ViewPager2 (setPageTransform)

#### Assignment #6
- Retrofit with Coroutine
- FlowBinding
- SimpleDateFormat
- OkHttpClient Interceptor

## What to learn

- Kotlin Gradle script(custom task)
```kotlin
tasks.register("lintAppModule") {
    dependsOn(":app:lint")

    doLast {
        println("Lint check success ✅")
    }
}
```

- Github action (CI android debug build)
```yml
name: Android Build
on: [push]
defaults:
  run:
    shell: bash
    working-directory: .

jobs:
  build:
    runs-on: ubuntu-latest
    name: build debug
    if: "!contains(toJSON(github.event.commits.*.message), '[skip action]') && !startsWith(github.ref, 'refs/tags/')"
    steps:
      - name: Checkout repository
        uses: actions/checkout@v2
      - name: Gradle cache
        uses: actions/cache@v2
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
          restore-keys: |
            ${{ runner.os }}-gradle-
      - name: Lint
        run: ./gradlew lintAppModule
      - name: Build debug
        run: ./gradlew assembleDebug
      - name: Archive artifacts
        uses: actions/upload-artifact@v2
        with:
          path: app/build/outputs

```

#### Assignment #1
- AAC Lifecycle(LiveData, ViewModel)

*SignInViewModel.kt*
```kotlin
typealias AutoSignIn = Boolean

class SignInViewModel @ViewModelInject constructor(
    @Named(AUTHENTICATOR_TYPE) private val authenticator: Authenticator, /*@Assisted private val savedStateHandle: SavedStateHandle*/
) : ViewModel() {
    // StateFlow data binding support is coming in AGP 4.3
    // https://twitter.com/manuelvicnt/status/1314621067831521282
    val id = MutableLiveData("")
    val pw = MutableLiveData("")

    private var isAutoSignInTried = false

    val onSignInSuccess = EventLiveData<AutoSignIn>()
    val onSignInFail = EventLiveData<AutoSignIn>()

    suspend fun canAutoSignIn(): Boolean {
        if (isAutoSignInTried) return false
        isAutoSignInTried = true

        return authenticator.canAutoSignIn()
    }

    fun tryManualSignIn() = viewModelScope.launch {
        if (matchWithLastSignInInfo()) {
            onSignInSuccess.emit(false)
        } else {
            onSignInFail.emit(false)
        }
    }

    private suspend fun matchWithLastSignInInfo() = authenticator.signInWithId(id.value!!, pw.value!!)
}
```

- AAC DataBinding, ViewBinding
- Kotlin Coroutine(Flow, StateFlow, SharedFlow, ...)

```kotlin
lifecycleScope.launch {
    settingManager.updateLastSignInInfo(id.value!!, pw.value!!)
    findNavController().popBackStack()
}
```

- AAC Navigation

```kotlin
findNavController().navigate(
    SignInFragmentDirections.actionSignInFragmentToSignUpFragment(
        viewModel.id.value!!, viewModel.pw.value!!
    )
)
```

- Dagger, Hilt

*AppModule.kt*
```kotlin
@InstallIn(ApplicationComponent::class)
@Module
object AppModule {
    @Provides
    @Singleton
    fun provideContext(app: Application): Context = app

    @Provides
    @Singleton
    fun provideDisplayMetrics(context: Context): DisplayMetrics = context.resources.displayMetrics

    @Provides
    @Singleton
    fun providePixelRatio(displayMetrics: DisplayMetrics) = PixelRatio(displayMetrics)
}
```

*AuthenticatorModule.kt*
```kotlin
@InstallIn(ApplicationComponent::class)
@Module
abstract class AuthenticatorModule {
    @Binds
    @Singleton
    @Named("SharedPreferences")
    abstract fun bindSharedPreferencesAuthenticator(authenticator: SharedPreferencesAuthenticator): Authenticator

    @Binds
    @Singleton
    @Named("EncryptedSharedPreferences")
    abstract fun bindEncryptedSharedPreferencesAuthenticator(authenticator: EncryptedSharedPreferencesAuthenticator): Authenticator

    @Binds
    @Singleton
    @Named("DataStorePreferences")
    abstract fun bindDataStorePreferencesAuthenticator(authenticator: DataStorePreferencesAuthenticator): Authenticator

    @Binds
    @Singleton
    @Named("EncryptedFileAuthenticator")
    abstract fun bindEncryptedFileAuthenticator(authenticator: EncryptedFileAuthenticator): Authenticator

    companion object {
        const val AUTHENTICATOR_TYPE = "EncryptedSharedPreferences"
    }
}
```

### Authentication abstraction

*Authenticator.kt*
```kotlin
interface Authenticator {
    suspend fun canAutoSignIn(): Boolean
    suspend fun signUpWithId(id: String, password: String)
    suspend fun signInWithId(id: String, password: String): Boolean
    suspend fun signOut()
}
```

*SharedPreferencesAuthenticator.kt*
```kotlin
class SharedPreferencesAuthenticator @Inject constructor(
    context: Context, private val validator: IdValidator
) : Authenticator {
    private var sharedPreferences = context.getSharedPreferences("sharedPreferences", 0)

    fun replaceSharedPreferences(sharedPreferences: SharedPreferences) {
        logE(sharedPreferences)
        this.sharedPreferences = sharedPreferences
    }

    override suspend fun canAutoSignIn() = sharedPreferences.getBoolean(AUTO_SIGNIN_KEY, false)

    override suspend fun signUpWithId(id: String, password: String) = sharedPreferences.edit(true) {
        putString(ID_KEY, id)
        putString(PW_KEY, password)
    }

    override suspend fun signInWithId(id: String, password: String) = validator.validateIdAndPwWithOthers(
        id, password, sharedPreferences.getString(ID_KEY, ""), sharedPreferences.getString(PW_KEY, "")
    ).also {
        if (it) {
            sharedPreferences.edit(true) {
                putBoolean(AUTO_SIGNIN_KEY, true)
            }
        }
    }

    override suspend fun signOut() {
        sharedPreferences.edit(true) {
            remove(AUTO_SIGNIN_KEY)
        }
    }

    companion object {
        private const val ID_KEY = "ID"
        private const val PW_KEY = "PW"
        private const val AUTO_SIGNIN_KEY = "AUTO_SIGNIN"
    }
}
```

- DataStore

*DataStorePreferencesAuthenticator.kt*
```kotlin
class DataStorePreferencesAuthenticator @Inject constructor(context: Context, private val validator: IdValidator) :
    Authenticator {
    private val dataStore = context.createDataStore("DataStorePreferencesAuthenticator")

    override suspend fun canAutoSignIn() = runCatching {
        val pref = dataStore.data.first()
        pref[AUTO_SIGNIN_KEY] == true
    }.getOrDefault(false)

    override suspend fun signUpWithId(id: String, password: String) {
        logE("$id $password")
        dataStore.edit { pref ->
            pref[ID_KEY] = id
            pref[PW_KEY] = password
        }
    }

    override suspend fun signInWithId(id: String, password: String): Boolean {
        return runCatching {
            val pref = dataStore.data.first()
            validator.validateIdAndPwWithOthers(id, password, pref[ID_KEY], pref[PW_KEY]).also {
                if (it) {
                    dataStore.edit { pref ->
                        pref[AUTO_SIGNIN_KEY] = true
                    }
                }
            }
        }.getOrDefault(false)
    }

    override suspend fun signOut() {
        dataStore.edit { pref ->
            pref.remove(AUTO_SIGNIN_KEY)
        }
    }

    companion object {
        private val ID_KEY = preferencesKey<String>("id")
        private val PW_KEY = preferencesKey<String>("pw")
        private val AUTO_SIGNIN_KEY = preferencesKey<Boolean>("autoSignIn")
    }
}
```

#### Jetpack Security
- EncryptedSharedPreferences

*EncryptedSharedPreferencesAuthenticator.kt*
```kotlin
class EncryptedSharedPreferencesAuthenticator
@Inject constructor(context: Context, @Named("SharedPreferences") authenticator: Authenticator) :
    Authenticator by ((authenticator as? SharedPreferencesAuthenticator
        ?: throw RuntimeException("Fix your type casting")).apply {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val encryptedSharedPreferences =
            EncryptedSharedPreferences.create("EncryptedSharedPreferences", masterKey, context, AES256_SIV, AES256_GCM)

        replaceSharedPreferences(encryptedSharedPreferences)
    })
```

*EncryptedFile*
```kotlin
class EncryptedFileAuthenticator @Inject constructor(context: Context, private val validator: IdValidator) :
    Authenticator {
    private val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
    private val file = File(context.getExternalFilesDir(null), "data.txt")
    private val encryptedFile = EncryptedFile.Builder(
        context, file, masterKey, AES256_GCM_HKDF_4KB
    ).build()

    override suspend fun canAutoSignIn(): Boolean = withContext(Dispatchers.IO) {
        file.exists()
    }

    override suspend fun signUpWithId(id: String, password: String) {
        encryptedFile.openFileOutput().use {
            it.write("$id\n$password\n".toByteArray())
        }
    }

    override suspend fun signInWithId(id: String, password: String): Boolean = withContext(Dispatchers.IO) {
        createFileIfNotExist()
        val content = encryptedFile.openFileInput().bufferedReader().useLines {
            it.fold("") { acc, line -> acc + "$line\n" }
        }
        val lastId = content.split("\n").firstOrNull()
        val lastPw = content.split("\n")[1]

        validator.validateIdAndPwWithOthers(id, password, lastId, lastPw)
    }

    private fun createFileIfNotExist() {
        if (!file.exists()) file.createNewFile()
    }

    override suspend fun signOut() {
        deleteFile()
    }

    private suspend fun deleteFile() = withContext(Dispatchers.IO) {
        file.delete()
    }
}
```

- AndroidX Biometric

*BioAuth.kt*
```kotlin
@Singleton
class BioAuth @Inject constructor(private val context: Context) {
    private val promptInfo = BiometricPrompt.PromptInfo.Builder().apply {
        this.setTitle("Title")
        this.setDescription("Description")
        setNegativeButtonText("Cancel")
        setAllowedAuthenticators(AUTHENTICATORS)
    }.build()

    val biometricEnabled: Boolean
        get() = BiometricManager.from(context).canAuthenticate(AUTHENTICATORS) == BIOMETRIC_SUCCESS

    suspend fun authenticate(fragment: Fragment) = suspendCancellableCoroutine<Boolean> { continuation ->
        BiometricPrompt(fragment, ContextCompat.getMainExecutor(context), object : AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                continuation.resume(false)
            }

            override fun onAuthenticationSucceeded(result: AuthenticationResult) {
                continuation.resume(true)
            }

            override fun onAuthenticationFailed() {
                continuation.resume(false)
            }
        }).authenticate(promptInfo)
    }

    companion object {
        private const val AUTHENTICATORS = BIOMETRIC_WEAK
    }
}
```

- Kotlin gradle script
- Kotlin stdlib
- ConstraintLayout
- MDC

#### Assignment #2
- RecyclerView
- ItemTouchHelper
- SwipeMenuTouchListener

*SwipeMenuTouchListener.kt*
```kotlin
class SwipeMenuTouchListener(
    private val menuWidth: Float, private val callback: Callback
) : OnTouchListener {
    private var dx = 0f

    override fun onTouch(view: View, e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                callback.onContentXChanged(e.rawX + dx)
            }
            MotionEvent.ACTION_DOWN -> {
                dx = view.x - e.rawX
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (e.rawX + dx < -menuWidth) {
                    callback.onContentXAnimated(-menuWidth)
                    callback.onMenuOpened()
                } else {
                    callback.onContentXAnimated(0f)
                    callback.onMenuClosed()
                }
            }
        }

        return false
    }

    interface Callback {
        fun onContentXChanged(x: Float)
        fun onContentXAnimated(x: Float)
        fun onMenuOpened()
        fun onMenuClosed()
    }
}
```

- OnDebounceClickListener

*OnDebounceClickListener.kt*
```kotlin
class OnDebounceClickListener(private val listener: OnClickListener) : View.OnClickListener {
    override fun onClick(v: View?) {
        val now = System.currentTimeMillis()
        if (now < lastTime + INTERVAL) return
        lastTime = now
        v?.run(listener)
    }

    companion object {
        private const val INTERVAL: Long = 300L
        private var lastTime: Long = 0
    }
}


infix fun View.onDebounceClick(listener: OnClickListener) {
    this.setOnClickListener(OnDebounceClickListener {
        it.run(listener)
    })
}
```

- EventLiveData

*EventLiveData.kt*
```kotlin
class EventLiveData<T> : MutableLiveData<T>() {
    private val pending = AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner) {
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(it)
            }
        }
    }

    @MainThread
    fun emit(value: T) {
        pending.set(true)
        setValue(value)
    }
}
```

- PixelRatio

*PixelRatio.kt*
```kotlin
@Singleton
class PixelRatio @Inject constructor(private val displayMetrics: DisplayMetrics) {
    val screenWidth: Int
        get() = displayMetrics.widthPixels

    val screenHeight: Int
        get() = displayMetrics.heightPixels

    @Px
    fun toPixel(dp: Int) = (dp * displayMetrics.density).roundToInt()

    fun toDP(@Px pixel: Int) = (pixel / displayMetrics.density).roundToInt()
}
```

We can test `PixelRatio` with mocking Android instance(`DisplayMetrics`) with **Mockito**.
```kotlin
class PixelRatioTest {
    private lateinit var pixelRatio: PixelRatio

    private val mockWidth = 1000
    private val mockHeight = 2000

    @Before
    fun setup() {
        val mockDisplayMetrics = mock(DisplayMetrics::class.java).apply {
            widthPixels = mockWidth
            heightPixels = mockHeight
            density = 3f
        }

        pixelRatio = PixelRatio(mockDisplayMetrics)
    }

    @Test
    fun `screenWidth, screenHeight should be same with real screen size`() {
        Truth.assertThat(pixelRatio.screenWidth).isEqualTo(mockWidth)
        Truth.assertThat(pixelRatio.screenHeight).isEqualTo(mockHeight)
    }

    @Test
    fun `toDP, toPixel`() {
        Truth.assertThat(pixelRatio.toDP(3)).isEqualTo(1)
        Truth.assertThat(pixelRatio.toPixel(1)).isEqualTo(3)
    }
}
```

- ViewModel
- Suspend function
- OnBackPressedCallback

*MainFragment.kt*
```kotlin
private val backPressedCallback = object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
        isEnabled = false
        if (isCardShowing) {
            hideCard()
        } else {
            onBackPressed()
        }
    }
}

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    requireActivity().onBackPressedDispatcher.addCallback(backPressedCallback)

    ...
}

override fun onDestroyView() {
    super.onDestroyView()
    backPressedCallback.remove()
}
```

- MaterialContainerTransform
- LeakCanary and Android Studio memory profiler
- ShapeableImageView, ShapeAppearanceModel

```kotlin
@BindingAdapter("app:useCircleOutlineWithRadius")
fun ShapeableImageView.useCircleOutlineWithRadius(radius: Float) {
    shapeAppearanceModel = ShapeAppearanceModel().withCornerSize(radius)
}
```

- Glide

```kotlin
@BindingAdapter("app:url", requireAll = false)
fun ImageView.loadUrlAsync(url: String?) {
    val anim = CircularProgressDrawable(context).apply {
        strokeWidth = 4f
        setColorSchemeColors(
            *listOf(
                R.color.colorPrimary, R.color.colorSecondary, R.color.colorError70
            ).map { context.getColor(it) }.toIntArray()
        )
        start()
    }

    if (url == null) {
        Glide.with(this).load(anim).into(this)
    } else {
        Glide.with(this).load(url)
            .transition(withCrossFade(DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()))
            .placeholder(anim).into(this)
    }
}
```

- FragmentFactory

*MainFragmentFactory.kt*
```kotlin
class MainFragmentFactory(activity: Activity) : FragmentFactory() {

    @EntryPoint
    @InstallIn(ActivityComponent::class)
    interface MainFragmentFactoryEntryPoint {
        fun pixelRatio(): PixelRatio
        fun loremIpsum(): LoremIpsum

        @Named(AUTHENTICATOR_TYPE)
        fun authenticator(): Authenticator

        fun bioAuth(): BioAuth
    }

    private val entryPoint = EntryPointAccessors.fromActivity(activity, MainFragmentFactoryEntryPoint::class.java)

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (loadFragmentClass(classLoader, className)) {
            MainFragment::class.java -> MainFragment(
                entryPoint.pixelRatio(), entryPoint.loremIpsum(), entryPoint.authenticator()
            )
            SignInFragment::class.java -> SignInFragment(entryPoint.bioAuth())
            SignUpFragment::class.java -> SignUpFragment(entryPoint.authenticator())
            else -> super.instantiate(classLoader, className)
        }
    }

    companion object {
        fun getInstance(activity: Activity): MainFragmentFactory {
            return MainFragmentFactory(activity)
        }
    }
}
```

#### Assignment #3

- FragmentStateAdapter

*FrameAdapter.kt*
```kotlin
class FrameAdapter(private val fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        val classLoader = fragment.requireActivity().classLoader
        val factory = MainFragmentFactory.getInstance(fragment.requireActivity())
        return when (position) {
            0 -> factory.instantiate(classLoader, ProfileFragment::class.java.name)
            1 -> factory.instantiate(classLoader, MainFragment::class.java.name)
            2 -> factory.instantiate(classLoader, SettingsFragment::class.java.name)
            else -> throw RuntimeException("What the...")
        }
    }
}
```

- ViewPager2

*FrameFragment.kt*
```kotlin
private fun configurePager() = mBinding.pager.run {
    offscreenPageLimit = 3
    adapter = FrameAdapter(this@FrameFragment)
    registerOnPageChangeCallback(object : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            viewModel.onPageSelected(position)
        }
    })
    setPageTransformer { page, position ->
        page.pivotX = if (position < 0) page.width.toFloat() else 0f
        page.pivotY = page.height * 0.5f
        page.rotationY = 50f * position
    }
}
```

#### Assignment #6

- Retrofit

*RetrofitModule.kt*
```kotlin
@Module
@InstallIn(ApplicationComponent::class)
class RetrofitModule {
    private val loggingInterceptor = HttpLoggingInterceptor(Logger.DEFAULT).apply {
        this.level = HttpLoggingInterceptor.Level.BODY
    }

    private val baseClient = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

    @Provides
    @Singleton
    @Named(REQRES_QUALIFIER)
    fun provideReqresRetrofit(nativeLib: NativeLib) =
        Retrofit.Builder().baseUrl(nativeLib.reqresBaseUrl).client(baseClient)
            .addConverterFactory(GsonConverterFactory.create()).build()

    @Provides
    @Singleton
    @Named(KAKAO_QUALIFIER)
    fun provideKakaoRetrofit(nativeLib: NativeLib): Retrofit {
        val kakaoNetworkInterceptor = object : Interceptor {
            override fun intercept(chain: Chain): Response {
                logE(nativeLib.kakaoApiKey)
                val req =
                    chain.request().newBuilder().addHeader("Authorization", "KakaoAK " + nativeLib.kakaoApiKey).build()
                return chain.proceed(req)
            }
        }

        val kakaoClient = baseClient.newBuilder().addNetworkInterceptor(kakaoNetworkInterceptor).build()

        return Retrofit.Builder().baseUrl(nativeLib.kakaoBaseUrl).client(kakaoClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    companion object {
        const val REQRES_QUALIFIER = "Reqres"
        const val KAKAO_QUALIFIER = "Kakao"
    }
}
```

- SimpleDateFormat

*KakaoSearchAdapter.kt*
```kotlin
object KakaoSearchAdapter : ModelAdapter<KakaoSearchDTO, KakaoSearchEntity> {
    private val timeZone = TimeZone.getTimeZone("Asia/Seoul")

    private val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.KOREA).apply {
        timeZone = timeZone
    }

    override fun toEntity(source: KakaoSearchDTO): KakaoSearchEntity {

        val cal = Calendar.getInstance(timeZone).apply {
            time = formatter.parse(source.datetime) ?: Date()
        }

        return KakaoSearchEntity(source.contents, cal, source.title, source.url)
    }

    override fun toDTO(source: KakaoSearchEntity): KakaoSearchDTO {
        return KakaoSearchDTO(source.contents, formatter.format(source.datetime.time), source.title, source.url)
    }
}
```

- FlowBinding

*SearchFragment.kt*
```kotlin
private fun configureEditText() = mBinding.editText.run {
    textChanges().debounce(1500L).onEach {
        if (it.isNotEmpty()) viewModel.search(it.toString())
    }.launchIn(lifecycleScope)
}
```

## Checkout date

- assignment #1 2020.10.15
- assignment #2 2020.10.19
- assignment #3 2020.11.20
- assignment #6 2020.11.29

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://www.mjstudio.net/"><img src="https://avatars0.githubusercontent.com/u/33388801?v=4" width="100px;" alt=""/><br /><sub><b>MJ Studio</b></sub></a><br /><a href="https://github.com/mym0404/MJIsGood/commits?author=mym0404" title="Code">💻</a></td>
  </tr>
</table>

<!-- markdownlint-enable -->
<!-- prettier-ignore-end -->
<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
