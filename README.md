# WACS - WhatsApp Contact Saver

تطبيق Android يكتشف الأرقام غير المحفوظة من قائمة محادثات واتساب ويحفظها تلقائياً في جهات الاتصال (ومنها تنسخن مع Google Contacts).

## 🎯 الفكرة

- تفتح التطبيق وتفعّل خدمة إمكانية الوصول (Accessibility Service)
- تفتح واتساب وتتصفح قائمة المحادثات عادي
- التطبيق يقرأ الأرقام غير المحفوظة تلقائياً في الخلفية
- ترجع للتطبيق وتشوف قائمة بالأرقام المكتشفة
- تضغط "حفظ" على أي رقم وبيتحفظ في جهات اتصال الموبايل فوراً

## ✨ المميزات

- يعمل مع WhatsApp العادي و WhatsApp Business
- يوحّد صيغة الأرقام (E.164) باستخدام Google libphonenumber
- يدعم الأرقام التركية والسورية والسعودية والإماراتية والأردنية واللبنانية والمصرية والعراقية
- واجهة عربية بالكامل مع دعم RTL
- جميع البيانات تبقى محلية على جهازك (ما في سيرفر خارجي)
- مجاني ومفتوح المصدر

## 🚀 طريقة البناء

### الخيار 1: GitHub Actions (الأسهل - بدون تنزيل شي على جهازك)

1. أنشئ repo جديد على GitHub
2. ارفع محتوى المشروع:
   ```bash
   cd wacs
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin git@github.com:YOUR_USERNAME/wacs.git
   git push -u origin main
   ```
3. روح لـ **Actions tab** على GitHub وراقب البناء
4. بعد انتهاء البناء (~5 دقائق)، حمّل `wacs-debug-apk` من Artifacts
5. نزّل ملف APK على موبايلك وثبّته

### الخيار 2: البناء المحلي بـ Command Line Tools

#### المتطلبات الأساسية:
- Java JDK 17+
- Android SDK Command Line Tools
- Gradle 8.4+

#### خطوات البناء:

**1. تنزيل Command Line Tools:**
```bash
# لـ Linux/Mac
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-*.zip -d ~/android-sdk/cmdline-tools
mv ~/android-sdk/cmdline-tools/cmdline-tools ~/android-sdk/cmdline-tools/latest

# لـ Windows
# نزّل من: https://developer.android.com/studio#command-line-tools-only
# فك الضغط في C:\android-sdk\cmdline-tools\latest\
```

**2. إعداد متغيرات البيئة:**
```bash
# Linux/Mac - أضف في ~/.bashrc أو ~/.zshrc
export ANDROID_HOME=$HOME/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools

# Windows (PowerShell)
$env:ANDROID_HOME = "C:\android-sdk"
$env:Path += ";C:\android-sdk\cmdline-tools\latest\bin"
$env:Path += ";C:\android-sdk\platform-tools"
```

**3. تثبيت SDK components:**
```bash
# قبول التراخيص
yes | sdkmanager --licenses

# تثبيت المكونات المطلوبة
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

**4. إنشاء Gradle wrapper:**
```bash
cd wacs
gradle wrapper --gradle-version 8.4
```

**5. البناء:**
```bash
./gradlew assembleDebug
# الـ APK رح يطلع في: app/build/outputs/apk/debug/app-debug.apk
```

## 📱 خطوات التثبيت والاستخدام

### 1. تثبيت التطبيق

**عبر ADB (الأسرع):**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

**يدوياً:**
- انقل ملف APK للموبايل
- فعّل "Unknown Sources" في إعدادات الأمان
- اضغط على الملف لتثبيته

### 2. إعطاء الصلاحيات

1. افتح تطبيق **حافظ أرقام واتساب**
2. اضغط **تفعيل الخدمة**
3. سيفتح لك إعدادات **Accessibility**
4. ابحث عن التطبيق وفعّل الخدمة
5. اقبل تنبيه Android (هذا طبيعي لأي خدمة Accessibility)
6. ارجع للتطبيق - سترى "الخدمة تعمل ✓"
7. امنح صلاحية **جهات الاتصال** عند السؤال

### 3. الاستخدام اليومي

1. افتح **واتساب**
2. تصفح قائمة المحادثات (اعمل scroll)
3. ارجع لتطبيق WACS
4. ستجد الأرقام الجديدة في القائمة
5. اضغط **حفظ** على أي رقم وأدخل الاسم
6. الرقم يُحفظ في جهات الاتصال مباشرة

### 4. المزامنة مع Google Contacts

التطبيق يحفظ الأرقام في جهات الاتصال المحلية. إذا كان حساب Google متزامن:
- روح لـ: **Settings > Accounts > Google > Contacts**
- تأكد إن المزامنة مفعّلة
- الأرقام الجديدة ستظهر في Google Contacts خلال دقائق

## 🔍 استكشاف الأخطاء

### التطبيق لا يكتشف أي أرقام

**تأكد من:**
1. خدمة Accessibility مفعّلة (أعد الفتح من الإعدادات إذا لزم)
2. أن WhatsApp مثبّت وفُتح مرة واحدة على الأقل
3. أن قائمة المحادثات تحتوي فعلاً أرقاماً غير محفوظة

**راقب الـ Logs عبر ADB:**
```bash
adb logcat -s WACS_Service WACS_Contacts
```

ستظهر رسائل مثل:
```
I WACS_Service: Service connected and running
D WACS_Service: Collected 15 potential titles
I WACS_Service: Detected new phone: +905551234567
```

### لا يحفظ في جهات الاتصال

- تأكد من منح صلاحيات `READ_CONTACTS` و `WRITE_CONTACTS`
- إذا رفضت من قبل، روح لـ: **Settings > Apps > WACS > Permissions**

### الخدمة تتوقف بعد فترة

بعض شركات الموبايل (Xiaomi, Huawei, Oppo) توقف خدمات الخلفية تلقائياً. الحل:
- **Xiaomi:** Security > Permissions > Autostart > فعّل WACS
- **Huawei:** Battery > App launch > WACS > Manage manually > فعّل كل الخيارات
- **Samsung:** Settings > Battery > Background usage limits > استثني WACS

## 🔒 الخصوصية والأمان

- **لا يتصل التطبيق بأي سيرفر خارجي** على الإطلاق
- البيانات كلها في قاعدة بيانات Room محلية
- الكود مفتوح المصدر - راجعه بنفسك
- الصلاحيات الوحيدة: Accessibility + Contacts

## ⚠️ ملاحظات مهمة

1. هذا التطبيق **ما بيرسل** رسائل، بس بيقرأ. يعني ما بيخالف شروط واتساب.
2. يعتمد على بنية واجهة واتساب - إذا واتساب عمل تحديث كبير، يمكن يحتاج تعديل في الكود.
3. دقة الكشف ~95% للأرقام الواضحة. الأرقام بصيغة غريبة يمكن يتجاهلها.

## 📂 بنية المشروع

```
wacs/
├── app/
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/midani/wacs/
│       │   ├── service/
│       │   │   └── WhatsAppAccessibilityService.kt  ← المراقبة
│       │   ├── data/
│       │   │   ├── DetectedNumber.kt
│       │   │   ├── DetectedNumberDao.kt
│       │   │   └── WacsDatabase.kt
│       │   ├── utils/
│       │   │   ├── PhoneNumberDetector.kt           ← تمييز الأرقام
│       │   │   └── ContactsHelper.kt                ← الحفظ
│       │   └── ui/
│       │       ├── MainActivity.kt
│       │       ├── MainViewModel.kt
│       │       └── DetectedNumbersAdapter.kt
│       └── res/
│           ├── layout/
│           ├── values/
│           ├── values-ar/
│           ├── xml/
│           └── mipmap-*/
├── gradle/wrapper/
├── .github/workflows/build.yml
├── build.gradle
├── settings.gradle
├── gradle.properties
└── gradlew
```

## 🛠️ تخصيصات مستقبلية

أفكار تقدر تضيفها بسهولة:

1. **حفظ دفعة واحدة** - زر لحفظ كل الأرقام باسم افتراضي
2. **ربط بـ Google Contacts API مباشرة** - بدل الاعتماد على المزامنة
3. **تصدير CSV** - لتخزين الأرقام خارج التطبيق
4. **فلترة حسب الدولة** - اختيار أرقام سوريا فقط مثلاً
5. **تكامل مع Talabify** - إرسال الأرقام كـ leads لمنصتك

## 📝 الترخيص

مفتوح المصدر للاستخدام الشخصي. يمكنك التعديل والتوزيع بحرية.
