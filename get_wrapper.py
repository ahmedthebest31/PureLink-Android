import os
import urllib.request

# مسار المشروع الحالي
PROJECT_ROOT = os.getcwd()
WRAPPER_DIR = os.path.join(PROJECT_ROOT, "gradle", "wrapper")

# الروابط المباشرة للملفات القياسية (Gradle 8.5)
FILES = {
    "gradlew": "https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradlew",
    "gradlew.bat": "https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradlew.bat",
    "gradle/wrapper/gradle-wrapper.jar": "https://github.com/gradle/gradle/raw/v8.5.0/gradle/wrapper/gradle-wrapper.jar"
}

def download_file(url, dest_path):
    print(f"جاري تحميل: {os.path.basename(dest_path)} ...")
    try:
        urllib.request.urlretrieve(url, dest_path)
        print("✅ تم التحميل.")
    except Exception as e:
        print(f"❌ فشل التحميل: {e}")

def main():
    print(">>> بدء عملية استعادة ملفات Gradle Wrapper يدوياً...")

    # 1. التأكد من وجود مجلد gradle/wrapper
    if not os.path.exists(WRAPPER_DIR):
        os.makedirs(WRAPPER_DIR)
        print(f"تم إنشاء المجلد: {WRAPPER_DIR}")

    # 2. تحميل الملفات
    for name, url in FILES.items():
        # تحديد المسار النهائي للملف
        if "gradle-wrapper.jar" in name:
            dest = os.path.join(WRAPPER_DIR, "gradle-wrapper.jar")
        else:
            dest = os.path.join(PROJECT_ROOT, name)
        
        download_file(url, dest)

    print("\n========================================")
    print("مبروك! الملفات بقت موجودة.")
    print("دلوقتي جرب تشغل الأمر: .\\gradlew assembleDebug")
    print("========================================")

if __name__ == "__main__":
    main()