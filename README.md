# Jarvis — Shaxsiy AI Yordamchi (Android)

"Hey Jarvis" deb chaqirilganda uyg'onadigan, ovozli buyruqlarni Gemini AI
orqali tushunadigan va telefonni boshqaradigan shaxsiy Android ilova.

⚠️ **MUHIM — qurilma cheklovi:** Bu build faqat **arm64-v8a** va
**armeabi-v7a** (ARM, 64-bit va 32-bit) arxitekturali qurilmalarda
ishlaydi (masalan Samsung Galaxy J6, SM-J600F/DS, Exynos 7870 — bu
protsessor 64-bit bo'lsa ham, ba'zi qurilmalar tizimni 32-bit
rejimida ishga tushiradi, shuning uchun ikkalasi ham qo'shilgan).
APK hajmini kamaytirish uchun `app/build.gradle.kts` da
`ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a") }` qasddan
qo'shilgan — x86/x86_64 (emulyatorlar, Intel-asosli qurilmalar)
uchun bu APK **o'rnatilmaydi**. Agar shular ham kerak bo'lsa, ro'yxatga
`"x86", "x86_64"` qo'shing (APK hajmi mos ravishda ortadi).

> **Joriy chaqiruv iborasi: "Hey Jarvis"** (sof "Jarvis" emas) —
> rasmiy, oldindan o'qitilgan model ishlatiladi.

## ⚠️ MUHIM: wake-word arxitekturasi butunlay o'zgardi (2026-08-22)

**Qisqacha tarix (uch bosqich):**

1. **TFLite orqali `melspectrogram.tflite`** — bu ba'zi qurilmalarda
   (jumladan Samsung Galaxy J6, Exynos 7870 — sinovdan o'tkazilgan)
   quyidagi native xato bilan MUQARRAR qulardi:
   ```
   java.lang.IllegalStateException: Internal error: Unexpected failure
   when preparing tensor allocations: tensorflow/lite/util.cc
   BytesRequired number of elements overflowed.
   Node number 3 (CONV_2D) failed to prepare.
   ```
   Bu — loyihaga xos xato emas, balki openWakeWord'ning o'zida rasmiy
   hujjatlashtirilgan, ochiq masala:
   **https://github.com/dscripka/openWakeWord/issues/223**

2. **Sof-Kotlin qo'lda yozilgan STFT + mel-filterbank**, keyin
   **ONNX Runtime + rasmiy `.onnx` modellar** — bularning ikkalasi ham
   native crash'ni chetlab o'tdi, lekin wake-word aniqlash skori
   haqiqiy nutqda ham deyarli hech qachon ~0.0000-0.0155 dan yuqoriga
   chiqmadi. Bir nechta gipoteza (log-shkala formulasi, mel-filterbank
   normalizatsiyasi, hatto o'zimiz o'qitgan klassifikator modelining
   sifati) birma-bir sinovdan o'tkazildi, lekin hech biri hal qiluvchi
   bo'lmadi.

3. **YAKUNIY YECHIM — tayyor, sinovdan o'tgan kutubxona:** xuddi shu
   vazifani (openWakeWord modellarini Android'da to'g'ri ishga
   tushirish) boshqa dasturchilar ALLAQACHON hal qilib, Maven
   Central'da nashr etishgan:
   **`Re-MENTIA/openwakeword-android-kt`**
   (https://github.com/Re-MENTIA/openwakeword-android-kt,
   `xyz.rementia:openwakeword`). Endi bizning o'z pipeline
   kodimiz (mel-spektrogramma, embedding formatlash, klassifikatsiya)
   BUTUNLAY OLIB TASHLANDI — ular kutubxona ichida, sinovdan o'tgan
   holda bajariladi. `WakeWordDetector.kt` endi atigi ~150 qatorlik,
   shu kutubxonaning `WakeWordEngine`sini eski
   (`loadModel()`/`startListening()`/`stopListening()`/`close()`)
   interfeys bilan moslashtiruvchi ingichka o'ram (thin wrapper), xolos.

## Kerakli assets (barchasi `.onnx`, TFLite EMAS)

- `melspectrogram.onnx` — rasmiy openWakeWord audio old-qayta ishlash modeli
- `embedding_model.onnx` — rasmiy openWakeWord/Google nutq embedding modeli
- `hey_jarvis.onnx` — rasmiy, oldindan o'qitilgan "Hey Jarvis" klassifikatori
  (eslatma: bizning avval o'zimiz o'qitgan `jarvis_wakeword.tflite`
  o'rniga ATAYLAB rasmiy modelga qaytildi — sabab yuqoridagi 2-bosqichda
  tushuntirilgan)

Barchasi: `github.com/dscripka/openWakeWord/releases/download/v0.5.1/<fayl_nomi>`

## APK hajmi

Eng katta xato: `tensorflow-lite-select-tf-ops` ("Flex delegate")
kutubxonasi APK hajmini ~40MB'dan ~306MB'ga oshirgan edi, lekin xatoni
TUZATMAGAN edi. Bu, va TFLite'ning o'zi (endi umuman kerak emas —
barcha 3 model ONNX formatida) butunlay olib tashlandi. ABI filtrlash
(`arm64-v8a` + `armeabi-v7a` bilan cheklangan) ham saqlanib qoldi.

## Gradle wrapper haqida muhim eslatma

Repo'da `gradle/wrapper/gradle-wrapper.properties` bor, lekin
`gradlew`, `gradlew.bat` va `gradle/wrapper/gradle-wrapper.jar`
fayllari **YO'Q** — ularni generatsiya qilish uchun internetga ulangan
haqiqiy Gradle kerak, bu esa shu loyiha tayyorlangan muhitda mavjud
emas edi. Ikki yo'l bilan hal qilinadi:

- **CI'da (GitHub Actions):** `.github/workflows/build.yml` endi
  `gradle-version: '8.10'` bilan Gradle'ni o'zi o'rnatadi va wrapper
  fayllari yo'q bo'lsa, build boshlanishidan oldin avtomatik
  `gradle wrapper --gradle-version 8.10` orqali yaratib beradi — hech
  narsa qo'lda qilish shart emas.
- **Mahalliy (local) ishlash uchun:** wrapper fayllarini BIR MARTA
  o'zingiz generatsiya qilib, natijasini repo'ga commit qiling:
  ```
  gradle wrapper --gradle-version 8.10 --distribution-type bin
  git add gradlew gradlew.bat gradle/wrapper/gradle-wrapper.jar
  git commit -m "Add Gradle wrapper"
  ```
  (Buning uchun kompyuteringizda Gradle o'rnatilgan bo'lishi kerak —
  yoki Android Studio'ni ochib "Sync Project with Gradle Files"ni
  bosing, u wrapper fayllarini avtomatik yaratib beradi.)
  Shundan keyin `.gitignore` ularni ignore qilmaydi (buni maxsus
  tekshirib qo'yilgan), shuning uchun commit muvaffaqiyatli bo'ladi.

## Loyihani ochish

1. Android Studio (eng so'nggi barqaror versiya) bilan oching — u
   ochilganda wrapper fayllari yo'qligini payqab, ularni odatda o'zi
   avtomatik yaratadi ("Gradle Sync" jarayonida).
2. Agar Android Studio ishlatmasangiz, yuqoridagi "Gradle wrapper
   haqida muhim eslatma" bo'limidagi buyruqni qo'lda bajaring.
3. `local.properties.example` faylini `local.properties` deb nusxalang
   va o'zingizning Gemini API kalitingizni kiriting:
   ```
   GEMINI_API_KEY=sizning_kalitingiz
   ```
4. Wake-word uchun barcha 3 ta `.tflite` fayli allaqachon
   `app/src/main/assets/` papkasida qo'shilgan — qo'shimcha ish
   talab qilinmaydi. Tafsilot uchun pastdagi "Wake-word modelini
   qo'shish" bo'limiga qarang.
5. Run qiling — birinchi ishga tushirishda barcha kerakli ruxsatlar
   so'raladi.

## Wake-word modelini qo'shish

`WakeWordDetector` — endi **o'zining pipeline kodi yo'q**, faqat
`xyz.rementia:openwakeword` kutubxonasi (`WakeWordEngine`) atrofidagi
ingichka o'ram. Haqiqiy uch bosqichli konveyer (mel-spektrogramma ->
embedding -> klassifikatsiya) kutubxonaning ichida bajariladi:

```
xom audio -> melspectrogram.onnx -> embedding_model.onnx -> hey_jarvis.onnx -> ehtimollik
```

`app/src/main/assets/` papkasida UCHALA fayl ham bor (barchasi
`.onnx`, TFLite EMAS, jami ~3.6 MB):

| Fayl | Holati | Manbasi |
|---|---|---|
| `melspectrogram.onnx` | ✅ QO'SHILGAN | openWakeWord GitHub repozitoriyasi (v0.5.1 reliz) — barcha so'zlar uchun umumiy, tayyor holda beriladi |
| `embedding_model.onnx` | ✅ QO'SHILGAN | Xuddi shu reliz — bu ham umumiy, tayyor model |
| `hey_jarvis.onnx` | ✅ QO'SHILGAN | openWakeWord'ning rasmiy "Hey Jarvis" relizi — [`hey_jarvis_v0.1.onnx`](https://github.com/dscripka/openWakeWord/releases/download/v0.5.1/hey_jarvis_v0.1.onnx), shu nom bilan qayta nomlanib qo'shilgan |

Uchalasi ham litsenziya sababli asl `.onnx` fayl sifatida,
o'zgartirilmasdan qo'shilgan (openWakeWord v0.5.1 relizidan).

### Chaqiruv iborasi haqida — "Hey Jarvis", sof "Jarvis" emas

Hozir ilova ichidagi `hey_jarvis.onnx` — openWakeWord'ning tayyor
"Hey Jarvis" klassifikatori, sizning shaxsiy o'qitgan modelingiz
emas (avval o'zimiz o'qitgan `jarvis_wakeword.tflite` sifatli
bo'lmasligi ehtimoli aniqlangani uchun ATAYLAB rasmiy modelga
qaytildi). Demak, ilova **faqat "Hey Jarvis" iborasiga** javob
beradi, yolg'iz "Jarvis" so'ziga emas.

Kelajakda sof "Jarvis" (yoki istalgan boshqa so'z/ibora) uchun
o'zingiz modelni o'qitib, uni almashtirishingiz mumkin:
1. openWakeWord'ning o'qitish qo'llanmasi (`train.py` yoki repo'dagi
   Colab notebook) orqali o'zingiz tanlagan so'z uchun `.onnx`
   klassifikator o'qiting (kutubxona `.onnx` formatini kutadi,
   `.tflite` emas).
2. Yangi faylni xuddi shu nom — `hey_jarvis.onnx` — bilan
   `app/src/main/assets/` ichidagi eskisining o'rniga qo'ying (yoki
   `WakeWordDetector.kt` dagi `MODEL_ASSET`/`WAKE_WORD_NAME`
   konstantalarini yangi fayl nomiga moslab o'zgartiring).
3. Foydalanuvchi interfeysi va bildirishnoma matnlaridagi
   "Hey Jarvis" so'zini o'zingizning yangi chaqiruv iborangizga
   moslab yangilashni unutmang (`ui/JarvisScreen.kt`,
   `service/JarvisForegroundService.kt`, `res/values/strings.xml`).


Agar bu uchta faylning biri (yoki barchasi) hali qo'shilmagan bo'lsa:
ilova QULAMAYDI. `JarvisForegroundService` xatoni ushlab, aynan qaysi
fayl yetishmayotganini bildirishnomada ko'rsatadi va servis xavfsiz
to'xtaydi (`WakeWordDetector.kt` dagi `WakeWordLoadException` va uning
uch pastki turi — `WakeWordAssetMissing`, `WakeWordAssetCorrupted`,
`WakeWordModelLoadFailed` — orqali).

### Wake-word yuklashda xato bo'lsa — diagnostika

`WakeWordDetector.loadModel()` uch modelni ketma-ket, har birini
alohida `Log.d("WakeWordDetector", ...)` bilan belgilab yuklaydi.
Muammo bo'lsa, `adb logcat -s WakeWordDetector` orqali quyidagilarni
ko'rasiz:
- Qaysi model ([1/3], [2/3], [3/3]) yuklanayotganda muammo chiqqani.
- Har bir asset faylning haqiqiy bayt hajmi (agar u kutilgan
  minimal hajmdan — 50 KB dan — kichik bo'lsa, fayl buzilgan/to'liq
  emas deb topilib, aniq shu haqda xato beriladi).
- Muvaffaqiyatli yuklangan modellarning input/output tensor
  shape'lari.

TensorFlow Lite Interpreter atayin CPU-only, bitta oqimli sozlamalar
bilan yaratiladi (`setNumThreads(1)`, `setUseNNAPI(false)`,
`setUseXNNPACK(false)`) — chunki ba'zi qurilmalarning NNAPI/XNNPACK
tezlashtirish drayverlari tensor tayyorlash bosqichida (masalan
CONV_2D qatlamida) "number of elements overflowed" kabi native
xatolar berishi ma'lum holat. Bu sozlamalar ishlash tezligini sal
pasaytiradi, lekin wake-word kabi kichik modellar uchun bu farq
sezilmaydi va barqarorlik ancha oshadi.

## Arxitektura — kelajakka chidamlilik

| Tamoyil | Qayerda amalga oshirilgan |
|---|---|
| Versiyalar markazlashgan | `gradle/libs.versions.toml` |
| Ruxsatlar versiyaga qarab dinamik | `permissions/PermissionManager.kt` |
| Foreground service eng qattiq (Android 14+) talabga mos | `service/JarvisForegroundService.kt`, `util/SdkCompat.kt` |
| AI-provayderdan mustaqil arxitektura | `ai/AIProvider.kt` (interfeys) + `ai/GeminiProvider.kt` (implementatsiya) |
| Graceful degradation (funksiya mavjud bo'lmasa, crash o'rniga xabar) | `FunctionResult` klassi — `permissions/PermissionManager.kt` fayli ichida (alohida fayl emas), har bir `functions/*.kt` |
| Wake-word tensor shape'lari qattiq yozilmagan, modeldan dinamik o'qiladi | `wakeword/WakeWordDetector.kt` |
| `.tflite` assetlari APK'da siqilmasdan saqlanadi (memory-map uchun shart) | `app/build.gradle.kts` → `androidResources.noCompress` |
| Jarvis xarakteri (system prompt) yagona joyda, providerdan mustaqil | `ai/JarvisPersona.kt` (`GeminiProvider` uni `systemInstruction`ga uzatadi) |
| Vaqtinchalik crash-diagnostika (fayl + Toast) | `util/CrashLogger.kt`, `JarvisApplication.installGlobalCrashHandler()`, `MainActivity.onCreate()` |
| Vaqtinchalik bosqichma-bosqich trace (native qulashlar uchun) | `util/TraceLogger.kt`, `service/JarvisForegroundService.kt`, `wakeword/WakeWordDetector.kt` |
| targetSdk har doim eng so'nggi | `libs.versions.toml` → `targetSdk` |

## Jarvis xarakteri (system prompt)

Jarvis'ning muloqot uslubi — o'zbekona hazil, "siz" bilan murojaat,
xotirjam va aniq javoblar — `ai/JarvisPersona.kt` faylida yagona joyda
saqlanadi. Bu matn `AIMessage.Role.SYSTEM` xabari sifatida
`JarvisForegroundService`dagi suhbat tarixining boshiga qo'yiladi, va
`GeminiProvider` uni Gemini API'ning maxsus `systemInstruction`
maydoniga (oddiy suhbat xabari sifatida emas) uzatadi — shu tufayli u
token sarfini oshirmaydi va har bir javobga barqaror ta'sir qiladi.

Xarakterni o'zgartirish uchun faqat `JarvisPersona.kt`dagi
`SYSTEM_PROMPT` matnini tahrirlash kifoya — `GeminiProvider` va
`JarvisForegroundService`ga tegishning hojati yo'q.

## AI provayderni almashtirish

`JarvisApplication.kt` faylida bitta qatorni almashtirish kifoya:

```kotlin
aiProvider = GeminiProvider(apiKey = BuildConfig.GEMINI_API_KEY)
// -> masalan:
aiProvider = ClaudeProvider(apiKey = BuildConfig.CLAUDE_API_KEY)
```

`ClaudeProvider` yozilganda faqat `AIProvider` interfeysini implement
qilish kifoya — Service, FunctionRegistry va UI o'zgarmaydi.

## Yangi funksiya qo'shish

1. `functions/` papkasida yangi klass yozing (yoki mavjudiga metod
   qo'shing), natijani `FunctionResult` sifatida qaytaring.
2. `ai/FunctionRegistry.kt` da `declarations` ro'yxatiga
   `AIFunctionDeclaration` qo'shing.
3. `execute()` ichidagi `when` blokiga bitta `case` qo'shing.

AI (Gemini) yangi funksiyani avtomatik "ko'radi" va kerak bo'lganda
chaqiradi — boshqa hech narsani o'zgartirish shart emas.

## Vaqtinchalik diagnostika: CrashLogger

Ilova ishga tushganda kompyuter/logcat ulanmagan holatda ham nima
uchun qulayotganini bilish uchun, loyihaga **vaqtinchalik**
diagnostika vositasi qo'shilgan (`util/CrashLogger.kt`):

- `JarvisApplication.installGlobalCrashHandler()` —
  `Thread.setDefaultUncaughtExceptionHandler` orqali har qanday
  tutilmagan (uncaught) xatoni ilova to'liq qulashidan OLDIN ushlab
  qoladi.
- `MainActivity.onCreate()` — `setContent { ... }` chaqiruvi
  `try/catch` bilan o'ralgan, shuning uchun global handler
  o'rnatilishidan oldin (yoki u ishlamay qolgan holatda) yuzaga
  keladigan xatolar ham qo'lga olinadi.
- Ikkalasi ham xatoni `CrashLogger.logCrash()` orqali:
  1. **Faylga** — `getExternalFilesDir(null)/crash_log.txt` (odatda
     `/storage/emulated/0/Android/data/com.user.jarvis/files/crash_log.txt`).
     Bu maxsus ruxsat talab qilmaydi, lekin Android 10+ da "scoped
     storage" tufayli ba'zi uchinchi tomon fayl menejerlari boshqa
     ilovaning shu papkasini ko'rsatmasligi mumkin — eng ishonchli
     yo'l: qurilmani kompyuterga ulab, `adb pull
     /sdcard/Android/data/com.user.jarvis/files/crash_log.txt` orqali
     olish, yoki qurilmaning tizim darajasidagi standart "Fayllar"
     ilovasidan foydalanish.
  2. **Toast** orqali — xato turi va xabarining qisqa ko'rinishi
     bir necha soniya ekranda ko'rinadi, so'ng ilova (yoki ekran)
     yopiladi.

**Bu ODDIY, VAQTINCHALIK yechim** — production darajasida buning
o'rniga Firebase Crashlytics yoki shunga o'xshash haqiqiy
crash-reporting SDK ishlatilishi tavsiya etiladi (avtomatik yuklash,
qurilma/versiya konteksti, tendentsiya tahlili va h.k. beradi, buni
esa lokal matn fayli bermaydi). Diagnostika tugagach, xohlasangiz
`installGlobalCrashHandler()` chaqiruvini `JarvisApplication.onCreate()`
dan olib tashlashingiz kifoya — qolgan kod ishlashda davom etadi.

## Vaqtinchalik diagnostika: TraceLogger (native qulashlar uchun)

`CrashLogger` faqat Kotlin/JVM darajasidagi (Throwable sifatida
ushlanadigan) xatolarni yozadi. Agar servis **native** (masalan
TensorFlow Lite'ning C++ interpreter qatlami) darajasida jim qulasa —
`CrashLogger` HECH QACHON chaqirilmaydi, chunki bunday qulash Kotlin
`try/catch` orqali umuman ushlanmaydi va butun jarayon hech qanday
stack trace qoldirmasdan yo'q bo'ladi.

Shu holat uchun `util/TraceLogger.kt` qo'shilgan — u `CrashLogger`dan
farqli o'laroq, xatoni emas, balki **bosqichlarni** yozadi: servis
ishga tushishining har bir muhim nuqtasidan OLDIN va KEYIN
`getExternalFilesDir(null)/trace_log.txt` fayliga (append rejimida,
har chaqiruvda darhol diskka) bitta qator qo'shiladi.

Agar servis jim to'xtasa, faylni oching (`crash_log.txt` bilan bir xil
papkada) va **eng oxirgi qatorga** qarang:
- Agar oxirgi qator `"<fayl_nomi>.tflite: Interpreter(buffer, options)
  dan OLDIN"` bo'lib, undan keyingi `"... dan KEYIN"` qatori yo'q
  bo'lsa — bu aynan o'sha `.tflite` faylni yuklashda **native**
  (Kotlin `try/catch` ushlay olmaydigan) qulash sodir bo'lganini
  bildiradi.
- Agar fayl umuman yaratilmagan yoki `"onCreate boshlandi"` qatoridan
  ham nariga o'tmagan bo'lsa — muammo servisning o'zida emas, balki
  uni hech kim chaqirmaganida (`MainActivity`dagi diagnostikaga qarang).

Bu ham **vaqtinchalik** vosita — muammo topilgach, `TraceLogger.log()`
chaqiruvlarini kodda qoldirish shart emas (yoki debug-build'ga
cheklash mumkin).

## Chiqarish

Bu loyiha Play Store'ga emas, faqat shaxsiy `.apk` sifatida
tarqatish uchun mo'ljallangan. `.github/workflows/build.yml` har bir
push'da debug `.apk` yasab, GitHub Actions artifact sifatida yuklaydi.

## Ma'lum cheklovlar

Loyiha hozircha **build qilinadigan skeleton** holatida — quyidagi
narsalar sizga tegishli va ular qo'shilmaguncha ilova to'liq
ishlamaydi (ammo qulamaydi — har biri graceful degradation bilan
qoplangan):

- **Chaqiruv iborasi hozircha "Hey Jarvis"**, sof "Jarvis" emas —
  chunki joriy `hey_jarvis.onnx` openWakeWord'ning tayyor
  "Hey Jarvis" relizi. Sof "Jarvis" (yoki boshqa so'z) uchun o'zingiz
  model o'qitib almashtirishingiz mumkin — yuqoridagi "Chaqiruv
  iborasi haqida" bo'limiga qarang.
- **`GEMINI_API_KEY`** — `local.properties` (lokal) yoki GitHub
  Secrets (CI) orqali o'zingiz kiritishingiz kerak; bo'sh bo'lsa
  Gemini so'rovlari xato qaytaradi.
- **Gradle wrapper fayllari** (`gradlew`, `gradlew.bat`,
  `gradle-wrapper.jar`) repo'da yo'q — yuqoridagi "Gradle wrapper
  haqida muhim eslatma" bo'limiga qarang.
- **App ikonkasi vaqtinchalik** — neon-ko'k doira + "J" harfi
  (`res/mipmap-*`). O'z brend ikonkangiz bilan almashtirishingiz mumkin.
