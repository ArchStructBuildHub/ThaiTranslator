# Thai Translator (Minecraft 1.21.11 - NeoForge 21.11.48)
**Author:** NCchaNon

Mod แปลภาษาภายในเกมแบบสดๆ พร้อมฟอนต์ไทยแก้สระลอยในตัว

## วิธีแปลงเป็นไฟล์ `.jar` (เลือกได้ 2 วิธี)

### วิธีที่ 1: Build อัตโนมัติบน GitHub (ไม่ต้องลงโปรแกรมในเครื่อง)
1. สร้าง Repository ใหม่บน GitHub แล้วอัปโหลดไฟล์ทั้งหมดในโฟลเดอร์นี้ขึ้นไป
2. ไปที่แท็บ **Actions** -> เลือก **Build Thai Translator 1.21.11 Mod** -> กด **Run workflow**
3. รอประมาณ 2 นาที แล้วดาวน์โหลดไฟล์ `ThaiTranslator-1.21.11-1.0.0.jar` จากช่อง **Artifacts**

### วิธีที่ 2: Build บนเครื่องคอมพิวเตอร์ (Windows/Mac/Linux)
1. ติดตั้ง **Java 21 (JDK 21)** และ **Gradle**
2. ดับเบิลคลิกไฟล์ `BUILD_WINDOWS.bat` (หรือรันคำสั่ง `gradle build` ใน Terminal)
3. นำไฟล์ `build/libs/ThaiTranslator-1.21.11-1.0.0.jar` ไปวางในโฟลเดอร์ `.minecraft/mods/`
