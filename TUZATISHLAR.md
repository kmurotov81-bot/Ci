# Tuzatishlar ro'yxati

## Build'ni buzadigan xatolar
1. **`com.google.mlkit:text-recognition-cyrillic` mavjud emas** (ML Kit v2 faqat Latin/Chinese/Devanagari/Japanese/Korean beradi) → Gradle "Could not find" xatosi. Dependency va `CyrillicTextRecognizerOptions` olib tashlandi; kirill matn uchun `OcrFunctions` Gemini Vision'ga o'tadi.
2. Workflow: JDK 17, Android SDK + litsenziyalar (`setup-android`), `GEMINI_API_KEY` xavfsiz yozish, `gradlew` + `gradle-wrapper.jar` repo'ga qo'shildi, `blank.yml` (bo'sh shablon) o'chirildi.
3. `.gitignore`: `app/build/` ham ignore qilinadi.

## Ishlash xatolari
4. **Wake-word**: audio `/32768` bilan normallashtirilardi (model int16 masshtabini kutadi) va 480 namunalik kontekst yo'q edi → "Hey Travis" ham "Hey Jarvis"dan yuqori skor berardi. Tuzatildi, chegara 0.25 → 0.5.
5. `WakeWordDetector`: AudioRecord ochilmasa servis qulardi — endi qayta uriniladi; tred mahalliy `record` bilan ishlaydi.
6. `MainActivity`: bitta ixtiyoriy ruxsat (SMS, joylashuv...) rad etilsa servis umuman ishga tushmasdi.
7. Android 14+: `mediaProjection` FGS turi ruxsatsiz boshlanib, ilova qulardi.
8. `FileSearchFunctions`: Android 11+ da `ORDER BY ... LIMIT` MediaStore'da xato berardi.
9. `ScreenshotManager`: Android 14+ uchun callback, try/catch, ikki marta natija qaytishi.
10. TTS watchdog 6 soniya qat'iy edi — uzun javobda Jarvis o'z ovozini eshitardi.
11. Xotira qidiruvi butun gapni kalit so'z qilardi (hech qachon topmasdi) — endi so'zlar bo'yicha.
12. Suhbat tarixi cheksiz o'sardi — oxirgi 20 xabar saqlanadi. `trace_log.txt` 512 KB bilan cheklandi.
