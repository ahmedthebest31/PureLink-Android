import os
import shutil
import xml.etree.ElementTree as ET
from svglib.svglib import svg2rlg
from reportlab.graphics import renderPM

# --- إعدادات المسارات ---
ANDROID_XML_PATH = r"app/src/main/res/drawable/ic_logo.xml"
FASTLANE_DIR = r"fastlane/metadata/android/en-US/images"
FASTLANE_PNG_PATH = os.path.join(FASTLANE_DIR, "icon.png")
TEMP_SVG = "temp_icon.svg"
TEMP_PNG = "preview_icon.png"

def android_vector_to_svg(xml_path, svg_path):
    """
    يقوم بقراءة ملف Vector XML الخاص بالأندرويد
    ويعيد كتابته كملف SVG قياسي يفهمه الكمبيوتر.
    """
    ET.register_namespace('android', 'http://schemas.android.com/apk/res/android')
    tree = ET.parse(xml_path)
    root = tree.getroot()
    
    # استخراج مساحة الرسم (Viewport)
    ns = {'android': 'http://schemas.android.com/apk/res/android'}
    viewport_w = root.get('{' + ns['android'] + '}viewportWidth', '24')
    viewport_h = root.get('{' + ns['android'] + '}viewportHeight', '24')
    
    # كتابة ملف SVG
    with open(svg_path, 'w') as f:
        # رأس الملف: نحدد المقاس 512x512 ونظبط الـ ViewBox على مقاس الفيكتور الأصلي
        f.write(f'<svg width="512" height="512" viewBox="0 0 {viewport_w} {viewport_h}" xmlns="http://www.w3.org/2000/svg">\n')
        
        # تحويل المسارات (Paths)
        for path in root.findall('path'):
            fill_color = path.get('{' + ns['android'] + '}fillColor', '#000000')
            path_data = path.get('{' + ns['android'] + '}pathData', '')
            stroke_color = path.get('{' + ns['android'] + '}strokeColor')
            stroke_width = path.get('{' + ns['android'] + '}strokeWidth', '0')
            
            # تجهيز خصائص المسار
            attrs = f'd="{path_data}" fill="{fill_color}"'
            if stroke_color:
                attrs += f' stroke="{stroke_color}" stroke-width="{stroke_width}"'
            
            f.write(f'  <path {attrs} />\n')
        
        f.write('</svg>')
    print(f"✅ تم تحويل XML إلى SVG بنجاح: {svg_path}")

def convert_svg_to_png(svg_path, png_path):
    """
    يستخدم مكتبة ReportLab لتحويل SVG إلى PNG عالي الجودة
    """
    drawing = svg2rlg(svg_path)
    renderPM.drawToFile(drawing, png_path, fmt="PNG")
    print(f"✅ تم توليد صورة PNG بمقاس 512x512: {png_path}")

def main():
    print(">>> بدء معالجة أيقونة التطبيق...")
    
    # 1. التحويل
    if not os.path.exists(ANDROID_XML_PATH):
        print(f"❌ خطأ: لم أجد ملف الأيقونة في المسار: {ANDROID_XML_PATH}")
        return

    try:
        android_vector_to_svg(ANDROID_XML_PATH, TEMP_SVG)
        convert_svg_to_png(TEMP_SVG, TEMP_PNG)
        
        print("\n" + "="*40)
        print(f"تم إنشاء الصورة بنجاح: {TEMP_PNG}")
        print("يمكنك فتح هذا الملف الآن للتأكد من الشكل.")
        print("="*40 + "\n")
        
        # 2. السؤال للموافقة
        confirm = input("هل تريد اعتماد هذه الصورة ونسخها لمجلد Fastlane؟ (y/n): ").lower()
        
        if confirm == 'y':
            # التأكد من وجود المجلد
            if not os.path.exists(FASTLANE_DIR):
                os.makedirs(FASTLANE_DIR)
                
            shutil.copy(TEMP_PNG, FASTLANE_PNG_PATH)
            print(f"🚀 تم النسخ بنجاح إلى: {FASTLANE_PNG_PATH}")
            print("الآن أنت جاهز لرفع التحديث!")
            
            # تنظيف الملفات المؤقتة
            os.remove(TEMP_SVG)
            os.remove(TEMP_PNG)
            print("🧹 تم حذف الملفات المؤقتة.")
            
        else:
            print("تم الإلغاء. لم يتم تغيير ملفات Fastlane.")
            
    except Exception as e:
        print(f"❌ حدث خطأ أثناء التحويل: {e}")
