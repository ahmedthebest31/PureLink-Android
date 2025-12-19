Write-Host ">>> بدء خطة الملف الوهمي..." -ForegroundColor Cyan

# 1. إخفاء الملفات الأصلية (عشان ميعترضش على الكود القديم)
if (Test-Path "settings.gradle") { Move-Item "settings.gradle" "settings.gradle.bak" -Force }
if (Test-Path "build.gradle") { Move-Item "build.gradle" "build.gradle.bak" -Force }

# 2. إنشاء ملف وهمي فاضي (عشان يرضى يشتغل)
New-Item -Path "build.gradle" -ItemType File -Value "// Temporary file" -Force | Out-Null
Write-Host ">>> تم إنشاء ملف وهمي لخداع Gradle." -ForegroundColor Yellow

# 3. تشغيل أمر الرابر (دلوقتي هيلاقي ملف وهيشوفه سليم)
try {
    Write-Host ">>> جاري إنشاء Wrapper 8.5..." -ForegroundColor Cyan
    call gradle wrapper --gradle-version 8.5 --distribution-type bin
} catch {
    Write-Host "[تنبيه] حصلت مشكلة بسيطة، هنكمل استرجاع الملفات." -ForegroundColor Yellow
}

# 4. حذف الملف الوهمي واسترجاع الأصلي
Remove-Item "build.gradle" -Force
if (Test-Path "settings.gradle.bak") { Move-Item "settings.gradle.bak" "settings.gradle" -Force }
if (Test-Path "build.gradle.bak") { Move-Item "build.gradle.bak" "build.gradle" -Force }

Write-Host ">>> تم استرجاع ملفات المشروع الأصلية." -ForegroundColor Cyan

# 5. التحقق النهائي
if (Test-Path "gradlew.bat") {
    Write-Host "`n========================================"
    Write-Host "الله عليك يا فنان! ملف gradlew.bat وصل بالسلامة." -ForegroundColor Green
    Write-Host "========================================"
} else {
    Write-Host "لسه الملف ما جاش." -ForegroundColor Red
}