#!/usr/bin/env python3
"""Add missing phrase keys to phraseIdentity and all language packs in L10nPhrases.kt."""
from __future__ import annotations

import re
from pathlib import Path

PATH = Path(__file__).resolve().parents[1] / "app/src/main/java/com/replica/cleaner/l10n/L10nPhrases.kt"

# English key -> per-lang translations. Languages match pack suffixes.
NEW = {
    "items": {
        "Es": "elementos", "De": "Elemente", "Fr": "éléments", "It": "elementi",
        "Pt": "itens", "Ru": "элементов", "Ar": "عناصر", "Hi": "आइटम",
        "Zh": "项", "Ja": "項目", "Ko": "항목", "Tr": "öğe", "Vi": "mục",
        "Id": "item", "Nl": "items", "Pl": "elementów", "Uk": "елементів",
        "Th": "รายการ", "El": "στοιχεία",
    },
    "%d space saving tips": {
        "Es": "%d consejos para ahorrar espacio", "De": "%d Tipps zum Speicher sparen",
        "Fr": "%d astuces pour libérer de l'espace", "It": "%d consigli per risparmiare spazio",
        "Pt": "%d dicas para liberar espaço", "Ru": "%d советов по экономии места",
        "Ar": "%d نصائح لتوفير المساحة", "Hi": "%d जगह बचाने के टिप्स",
        "Zh": "%d 个节省空间技巧", "Ja": "容量節約のヒント %d 件", "Ko": "공간 절약 팁 %d개",
        "Tr": "%d yer tasarrufu ipucu", "Vi": "%d mẹo tiết kiệm dung lượng",
        "Id": "%d tips hemat ruang", "Nl": "%d tips om ruimte te besparen",
        "Pl": "%d wskazówek oszczędzania miejsca", "Uk": "%d порад для економії місця",
        "Th": "เคล็ดลับประหยัดพื้นที่ %d ข้อ", "El": "%d συμβουλές εξοικονόμησης χώρου",
    },
    "Tip %d": {
        "Es": "Consejo %d", "De": "Tipp %d", "Fr": "Astuce %d", "It": "Suggerimento %d",
        "Pt": "Dica %d", "Ru": "Совет %d", "Ar": "نصيحة %d", "Hi": "टिप %d",
        "Zh": "提示 %d", "Ja": "ヒント %d", "Ko": "팁 %d", "Tr": "İpucu %d",
        "Vi": "Mẹo %d", "Id": "Tips %d", "Nl": "Tip %d", "Pl": "Wskazówka %d",
        "Uk": "Порада %d", "Th": "เคล็ดลับ %d", "El": "Συμβουλή %d",
    },
    "TRANSFER %d FILES": {
        "Es": "TRANSFERIR %d ARCHIVOS", "De": "%d DATEIEN ÜBERTRAGEN",
        "Fr": "TRANSFÉRER %d FICHIERS", "It": "TRASFERISCI %d FILE",
        "Pt": "TRANSFERIR %d ARQUIVOS", "Ru": "ПЕРЕНЕСТИ %d ФАЙЛОВ",
        "Ar": "نقل %d ملفات", "Hi": "%d फ़ाइलें ट्रांसफ़र करें",
        "Zh": "传输 %d 个文件", "Ja": "%d 件のファイルを転送", "Ko": "파일 %d개 전송",
        "Tr": "%d DOSYA AKTAR", "Vi": "CHUYỂN %d TỆP", "Id": "TRANSFER %d FILE",
        "Nl": "%d BESTANDEN OVERZETTEN", "Pl": "PRZENIEŚ %d PLIKÓW",
        "Uk": "ПЕРЕНЕСТИ %d ФАЙЛІВ", "Th": "โอน %d ไฟล์", "El": "ΜΕΤΑΦΟΡΑ %d ΑΡΧΕΙΩΝ",
    },
    "PUT %d APPS TO SLEEP": {
        "Es": "PONER %d APPS EN SUSPENSIÓN", "De": "%d APPS IN DEN SCHLAFMODUS",
        "Fr": "METTRE %d APPLIS EN VEILLE", "It": "METTI IN SOSPENSIONE %d APP",
        "Pt": "COLOCAR %d APPS EM DESCANSO", "Ru": "УСЫПИТЬ %d ПРИЛОЖЕНИЙ",
        "Ar": "وضع %d تطبيقات في وضع السكون", "Hi": "%d ऐप्स को स्लीप करें",
        "Zh": "让 %d 个应用进入睡眠", "Ja": "%d 個のアプリをスリープ", "Ko": "앱 %d개 절전",
        "Tr": "%d UYGULAMAYI UYKUYA AL", "Vi": "ĐƯA %d ỨNG DỤNG VÀO NGỦ",
        "Id": "TIDURKAN %d APLIKASI", "Nl": "%d APPS LATEN SLAPEN",
        "Pl": "UŚPIJ %d APLIKACJI", "Uk": "Приспати %d програм",
        "Th": "พัก %d แอป", "El": "ΘΕΣΕ %d ΕΦΑΡΜΟΓΕΣ ΣΕ ΑΝΑΣΤΟΛΗ",
    },
    "On": {
        "Es": "Activado", "De": "Ein", "Fr": "Activé", "It": "Attivo", "Pt": "Ligado",
        "Ru": "Вкл.", "Ar": "تشغيل", "Hi": "चालू", "Zh": "开", "Ja": "オン",
        "Ko": "켜짐", "Tr": "Açık", "Vi": "Bật", "Id": "Aktif", "Nl": "Aan",
        "Pl": "Wł.", "Uk": "Увімк.", "Th": "เปิด", "El": "Ενεργό",
    },
    "Off": {
        "Es": "Desactivado", "De": "Aus", "Fr": "Désactivé", "It": "Disattivo", "Pt": "Desligado",
        "Ru": "Выкл.", "Ar": "إيقاف", "Hi": "बंद", "Zh": "关", "Ja": "オフ",
        "Ko": "꺼짐", "Tr": "Kapalı", "Vi": "Tắt", "Id": "Nonaktif", "Nl": "Uit",
        "Pl": "Wył.", "Uk": "Вимк.", "Th": "ปิด", "El": "Ανενεργό",
    },
    "Wi-fi": {
        "Es": "Wi-Fi", "De": "WLAN", "Fr": "Wi-Fi", "It": "Wi-Fi", "Pt": "Wi-Fi",
        "Ru": "Wi-Fi", "Ar": "Wi-Fi", "Hi": "Wi-Fi", "Zh": "Wi-Fi", "Ja": "Wi-Fi",
        "Ko": "Wi-Fi", "Tr": "Wi-Fi", "Vi": "Wi-Fi", "Id": "Wi-Fi", "Nl": "Wi-Fi",
        "Pl": "Wi-Fi", "Uk": "Wi-Fi", "Th": "Wi-Fi", "El": "Wi-Fi",
    },
    "Up-time": {
        "Es": "Tiempo activo", "De": "Betriebszeit", "Fr": "Temps d'activité",
        "It": "Tempo di attività", "Pt": "Tempo ativo", "Ru": "Время работы",
        "Ar": "مدة التشغيل", "Hi": "अपटाइम", "Zh": "运行时间", "Ja": "稼働時間",
        "Ko": "가동 시간", "Tr": "Çalışma süresi", "Vi": "Thời gian hoạt động",
        "Id": "Waktu aktif", "Nl": "Uptime", "Pl": "Czas pracy", "Uk": "Час роботи",
        "Th": "เวลาทำงาน", "El": "Χρόνος λειτουργίας",
    },
    "Android": {
        "Es": "Android", "De": "Android", "Fr": "Android", "It": "Android", "Pt": "Android",
        "Ru": "Android", "Ar": "Android", "Hi": "Android", "Zh": "Android", "Ja": "Android",
        "Ko": "Android", "Tr": "Android", "Vi": "Android", "Id": "Android", "Nl": "Android",
        "Pl": "Android", "Uk": "Android", "Th": "Android", "El": "Android",
    },
    "Unnecessary data": {
        "Es": "Datos innecesarios", "De": "Unnötige Daten", "Fr": "Données inutiles",
        "It": "Dati non necessari", "Pt": "Dados desnecessários", "Ru": "Ненужные данные",
        "Ar": "بيانات غير ضرورية", "Hi": "अनावश्यक डेटा", "Zh": "不必要的数据",
        "Ja": "不要なデータ", "Ko": "불필요한 데이터", "Tr": "Gereksiz veriler",
        "Vi": "Dữ liệu không cần thiết", "Id": "Data tidak diperlukan", "Nl": "Onnodige gegevens",
        "Pl": "Niepotrzebne dane", "Uk": "Непотрібні дані", "Th": "ข้อมูลที่ไม่จำเป็น",
        "El": "Μη απαραίτητα δεδομένα",
    },
    "Folders with nothing inside": {
        "Es": "Carpetas vacías", "De": "Leere Ordner", "Fr": "Dossiers vides",
        "It": "Cartelle vuote", "Pt": "Pastas vazias", "Ru": "Пустые папки",
        "Ar": "مجلدات فارغة", "Hi": "खाली फ़ोल्डर", "Zh": "空文件夹",
        "Ja": "中身のないフォルダ", "Ko": "빈 폴더", "Tr": "İçi boş klasörler",
        "Vi": "Thư mục trống", "Id": "Folder kosong", "Nl": "Lege mappen",
        "Pl": "Puste foldery", "Uk": "Порожні папки", "Th": "โฟลเดอร์ว่าง",
        "El": "Κενά φάκελοι",
    },
    "Your app diary": {
        "Es": "Tu diario de apps", "De": "Dein App-Tagebuch", "Fr": "Votre journal d'applis",
        "It": "Il diario delle app", "Pt": "Seu diário de apps", "Ru": "Дневник приложений",
        "Ar": "سجل تطبيقاتك", "Hi": "आपकी ऐप डायरी", "Zh": "应用使用日记",
        "Ja": "アプリ日記", "Ko": "앱 사용 기록", "Tr": "Uygulama günlüğün",
        "Vi": "Nhật ký ứng dụng", "Id": "Buku harian aplikasi", "Nl": "Je app-dagboek",
        "Pl": "Dziennik aplikacji", "Uk": "Щоденник додатків", "Th": "บันทึกแอปของคุณ",
        "El": "Το ημερολόγιο εφαρμογών σας",
    },
    "How much time you spent in each app?": {
        "Es": "¿Cuánto tiempo pasaste en cada app?", "De": "Wie viel Zeit hast du in jeder App verbracht?",
        "Fr": "Combien de temps avez-vous passé dans chaque appli ?",
        "It": "Quanto tempo hai trascorso in ogni app?", "Pt": "Quanto tempo você passou em cada app?",
        "Ru": "Сколько времени вы провели в каждом приложении?",
        "Ar": "كم من الوقت قضيت في كل تطبيق؟", "Hi": "प्रत्येक ऐप में आपने कितना समय बिताया?",
        "Zh": "你在每个应用中花了多少时间？", "Ja": "各アプリでどれくらい時間を使いましたか？",
        "Ko": "각 앱에서 얼마나 시간을 보냈나요?", "Tr": "Her uygulamada ne kadar zaman geçirdin?",
        "Vi": "Bạn đã dành bao nhiêu thời gian cho mỗi ứng dụng?",
        "Id": "Berapa lama Anda menghabiskan waktu di setiap aplikasi?",
        "Nl": "Hoeveel tijd heb je in elke app doorgebracht?",
        "Pl": "Ile czasu spędziłeś w każdej aplikacji?",
        "Uk": "Скільки часу ви провели в кожній програмі?",
        "Th": "คุณใช้เวลาในแต่ละแอปนานเท่าใด", "El": "Πόσο χρόνο περάσατε σε κάθε εφαρμογή;",
    },
    "Rarely used apps": {
        "Es": "Apps poco usadas", "De": "Selten genutzte Apps", "Fr": "Applis peu utilisées",
        "It": "App usate di rado", "Pt": "Apps pouco usadas", "Ru": "Редко используемые",
        "Ar": "تطبيقات نادرة الاستخدام", "Hi": "कम इस्तेमाल वाले ऐप्स", "Zh": "很少使用的应用",
        "Ja": "ほとんど使わないアプリ", "Ko": "거의 사용하지 않는 앱", "Tr": "Nadir kullanılan uygulamalar",
        "Vi": "Ứng dụng ít dùng", "Id": "Aplikasi jarang dipakai", "Nl": "Zelden gebruikte apps",
        "Pl": "Rzadko używane aplikacje", "Uk": "Рідко використовувані", "Th": "แอปที่ใช้น้อย",
        "El": "Εφαρμογές που χρησιμοποιούνται σπάνια",
    },
    "Not used": {
        "Es": "Sin usar", "De": "Nicht genutzt", "Fr": "Non utilisées", "It": "Non usate",
        "Pt": "Não usadas", "Ru": "Не используются", "Ar": "غير مستخدمة", "Hi": "उपयोग नहीं",
        "Zh": "未使用", "Ja": "未使用", "Ko": "미사용", "Tr": "Kullanılmayan",
        "Vi": "Không dùng", "Id": "Tidak dipakai", "Nl": "Niet gebruikt", "Pl": "Nieużywane",
        "Uk": "Не використовуються", "Th": "ไม่ได้ใช้", "El": "Χωρίς χρήση",
    },
    "Keep the best, drop the rest": {
        "Es": "Quédate con las mejores y elimina el resto", "De": "Behalte die besten, lösche den Rest",
        "Fr": "Gardez les meilleures, supprimez le reste", "It": "Tieni le migliori, elimina il resto",
        "Pt": "Fique com as melhores e remova o resto", "Ru": "Оставьте лучшие, удалите остальные",
        "Ar": "احتفظ بالأفضل واحذف الباقي", "Hi": "बेहतरीन रखें, बाकी हटाएँ",
        "Zh": "保留最好的，删除其余的", "Ja": "最良を残して他は削除", "Ko": "최고만 남기고 나머지는 삭제",
        "Tr": "En iyileri tut, gerisini sil", "Vi": "Giữ ảnh đẹp nhất, xóa phần còn lại",
        "Id": "Simpan yang terbaik, hapus sisanya", "Nl": "Bewaar de beste, verwijder de rest",
        "Pl": "Zachowaj najlepsze, usuń resztę", "Uk": "Залиште найкращі, видаліть решту",
        "Th": "เก็บรูปที่ดีที่สุด ลบที่เหลือ", "El": "Κρατήστε τα καλύτερα, διαγράψτε τα υπόλοιπα",
    },
    "Added more than a year ago": {
        "Es": "Añadidas hace más de un año", "De": "Vor mehr als einem Jahr hinzugefügt",
        "Fr": "Ajoutées il y a plus d'un an", "It": "Aggiunte più di un anno fa",
        "Pt": "Adicionadas há mais de um ano", "Ru": "Добавлены более года назад",
        "Ar": "أُضيفت منذ أكثر من عام", "Hi": "एक साल से ज़्यादा पहले जोड़ी गईं",
        "Zh": "添加于一年多以前", "Ja": "1年以上前に追加", "Ko": "1년 이상 전에 추가됨",
        "Tr": "Bir yıldan uzun süre önce eklendi", "Vi": "Đã thêm hơn một năm trước",
        "Id": "Ditambahkan lebih dari setahun lalu", "Nl": "Meer dan een jaar geleden toegevoegd",
        "Pl": "Dodane ponad rok temu", "Uk": "Додано понад рік тому",
        "Th": "เพิ่มเมื่อกว่าหนึ่งปีที่แล้ว", "El": "Προστέθηκαν πριν από περισσότερο από ένα χρόνο",
    },
    "Large videos": {
        "Es": "Vídeos grandes", "De": "Große Videos", "Fr": "Grosses vidéos",
        "It": "Video grandi", "Pt": "Vídeos grandes", "Ru": "Большие видео",
        "Ar": "مقاطع فيديو كبيرة", "Hi": "बड़े वीडियो", "Zh": "大型视频",
        "Ja": "大きな動画", "Ko": "큰 동영상", "Tr": "Büyük videolar",
        "Vi": "Video lớn", "Id": "Video besar", "Nl": "Grote video's",
        "Pl": "Duże wideo", "Uk": "Великі відео", "Th": "วิดีโอขนาดใหญ่",
        "El": "Μεγάλα βίντεο",
    },
    "Big files": {
        "Es": "Archivos grandes", "De": "Große Dateien", "Fr": "Gros fichiers",
        "It": "File grandi", "Pt": "Arquivos grandes", "Ru": "Большие файлы",
        "Ar": "ملفات كبيرة", "Hi": "बड़ी फ़ाइलें", "Zh": "大文件",
        "Ja": "大きなファイル", "Ko": "큰 파일", "Tr": "Büyük dosyalar",
        "Vi": "Tệp lớn", "Id": "File besar", "Nl": "Grote bestanden",
        "Pl": "Duże pliki", "Uk": "Великі файли", "Th": "ไฟล์ขนาดใหญ่",
        "El": "Μεγάλα αρχεία",
    },
    "Found a few big items. Take a look.": {
        "Es": "Hay algunos elementos grandes. Échales un vistazo.",
        "De": "Einige große Dateien gefunden. Schau sie dir an.",
        "Fr": "Quelques gros éléments trouvés. Jetez-y un œil.",
        "It": "Trovati alcuni elementi grandi. Dai un'occhiata.",
        "Pt": "Alguns itens grandes encontrados. Dê uma olhada.",
        "Ru": "Найдены крупные файлы. Взгляните.",
        "Ar": "عثرنا على بعض العناصر الكبيرة. ألقِ نظرة.",
        "Hi": "कुछ बड़ी चीज़ें मिलीं। एक नज़र डालें।",
        "Zh": "发现一些大文件。去看看吧。",
        "Ja": "大きな項目がいくつか見つかりました。確認してみてください。",
        "Ko": "큰 항목이 몇 개 있습니다. 확인해 보세요.",
        "Tr": "Birkaç büyük öğe bulundu. Bir göz at.",
        "Vi": "Tìm thấy vài mục lớn. Hãy xem thử.",
        "Id": "Beberapa item besar ditemukan. Lihat sebentar.",
        "Nl": "Een paar grote items gevonden. Neem een kijkje.",
        "Pl": "Znaleziono kilka dużych elementów. Zerknij.",
        "Uk": "Знайдено кілька великих елементів. Погляньте.",
        "Th": "พบรายการขนาดใหญ่บางรายการ ลองดูสิ",
        "El": "Βρέθηκαν μερικά μεγάλα στοιχεία. Ρίξτε μια ματιά.",
    },
    "%d empty folders": {
        "Es": "%d carpetas vacías", "De": "%d leere Ordner", "Fr": "%d dossiers vides",
        "It": "%d cartelle vuote", "Pt": "%d pastas vazias", "Ru": "%d пустых папок",
        "Ar": "%d مجلدات فارغة", "Hi": "%d खाली फ़ोल्डर", "Zh": "%d 个空文件夹",
        "Ja": "空のフォルダ %d 件", "Ko": "빈 폴더 %d개", "Tr": "%d boş klasör",
        "Vi": "%d thư mục trống", "Id": "%d folder kosong", "Nl": "%d lege mappen",
        "Pl": "%d pustych folderów", "Uk": "%d порожніх папок", "Th": "โฟลเดอร์ว่าง %d รายการ",
        "El": "%d κενοί φάκελοι",
    },
    "%d files in Downloads": {
        "Es": "%d archivos en Descargas", "De": "%d Dateien in Downloads",
        "Fr": "%d fichiers dans Téléchargements", "It": "%d file in Download",
        "Pt": "%d arquivos em Downloads", "Ru": "%d файлов в Загрузках",
        "Ar": "%d ملفات في التنزيلات", "Hi": "डाउनलोड में %d फ़ाइलें", "Zh": "下载中有 %d 个文件",
        "Ja": "ダウンロードに %d 件", "Ko": "다운로드에 파일 %d개", "Tr": "İndirilenlerde %d dosya",
        "Vi": "%d tệp trong Tải xuống", "Id": "%d file di Unduhan", "Nl": "%d bestanden in Downloads",
        "Pl": "%d plików w Pobranych", "Uk": "%d файлів у Завантаженнях",
        "Th": "%d ไฟล์ในดาวน์โหลด", "El": "%d αρχεία στις Λήψεις",
    },
    "%s can be reviewed": {
        "Es": "%s se pueden revisar", "De": "%s können geprüft werden",
        "Fr": "%s peuvent être examinés", "It": "%s da rivedere",
        "Pt": "%s podem ser revisados", "Ru": "%s можно проверить",
        "Ar": "يمكن مراجعة %s", "Hi": "%s की समीक्षा की जा सकती है", "Zh": "可审查 %s",
        "Ja": "%s を確認できます", "Ko": "%s 검토 가능", "Tr": "%s incelenebilir",
        "Vi": "Có thể xem lại %s", "Id": "%s dapat ditinjau", "Nl": "%s kan worden bekeken",
        "Pl": "%s do sprawdzenia", "Uk": "%s можна перевірити", "Th": "ตรวจทานได้ %s",
        "El": "%s μπορούν να ελεγχθούν",
    },
    "Free up to %s": {
        "Es": "Libera hasta %s", "De": "Bis zu %s freigeben", "Fr": "Libérez jusqu'à %s",
        "It": "Libera fino a %s", "Pt": "Liberar até %s", "Ru": "Освободить до %s",
        "Ar": "حرر حتى %s", "Hi": "%s तक खाली करें", "Zh": "最多可释放 %s",
        "Ja": "最大 %s を解放", "Ko": "최대 %s 확보", "Tr": "%s'ye kadar boşalt",
        "Vi": "Giải phóng tới %s", "Id": "Kosongkan hingga %s", "Nl": "Maak tot %s vrij",
        "Pl": "Zwolnij do %s", "Uk": "Звільнити до %s", "Th": "เพิ่มพื้นที่ได้สูงสุด %s",
        "El": "Ελευθερώστε έως %s",
    },
    "You have spent least time using %s": {
        "Es": "Has pasado menos tiempo usando %s", "De": "Am wenigsten genutzt: %s",
        "Fr": "Vous avez passé le moins de temps sur %s", "It": "Hai passato meno tempo su %s",
        "Pt": "Você passou menos tempo usando %s", "Ru": "Меньше всего времени: %s",
        "Ar": "قضيت أقل وقت في استخدام %s", "Hi": "आपने %s पर सबसे कम समय बिताया",
        "Zh": "你使用 %s 的时间最少", "Ja": "%s の使用時間が最も少ないです",
        "Ko": "%s 사용 시간이 가장 적습니다", "Tr": "En az zaman geçirdiğin uygulama: %s",
        "Vi": "Bạn dùng %s ít nhất", "Id": "Paling sedikit digunakan: %s",
        "Nl": "Minst gebruikt: %s", "Pl": "Najmniej czasu: %s",
        "Uk": "Найменше часу: %s", "Th": "ใช้เวลาน้อยที่สุดกับ %s",
        "El": "Λιγότερος χρόνος σε %s",
    },
    "%d bad photos found": {
        "Es": "%d fotos malas encontradas", "De": "%d schlechte Fotos gefunden",
        "Fr": "%d mauvaises photos trouvées", "It": "%d foto scadenti trovate",
        "Pt": "%d fotos ruins encontradas", "Ru": "Найдено плохих фото: %d",
        "Ar": "تم العثور على %d صور سيئة", "Hi": "%d खराब फ़ोटो मिलीं", "Zh": "发现 %d 张差照片",
        "Ja": "不良な写真 %d 枚", "Ko": "나쁜 사진 %d장 발견", "Tr": "%d kötü fotoğraf bulundu",
        "Vi": "Tìm thấy %d ảnh kém", "Id": "%d foto buruk ditemukan", "Nl": "%d slechte foto's gevonden",
        "Pl": "Znaleziono %d złych zdjęć", "Uk": "Знайдено поганих фото: %d",
        "Th": "พบรูปคุณภาพต่ำ %d รูป", "El": "Βρέθηκαν %d κακές φωτογραφίες",
    },
    "%s can be cleaned": {
        "Es": "%s se pueden limpiar", "De": "%s können bereinigt werden",
        "Fr": "%s peuvent être nettoyés", "It": "%s possono essere puliti",
        "Pt": "%s podem ser limpos", "Ru": "%s можно очистить",
        "Ar": "يمكن تنظيف %s", "Hi": "%s साफ़ किए जा सकते हैं", "Zh": "可清理 %s",
        "Ja": "%s をクリーンできます", "Ko": "%s 정리 가능", "Tr": "%s temizlenebilir",
        "Vi": "Có thể dọn %s", "Id": "%s dapat dibersihkan", "Nl": "%s kan worden opgeschoond",
        "Pl": "%s można wyczyścić", "Uk": "%s можна очистити", "Th": "ล้างได้ %s",
        "El": "%s μπορούν να καθαριστούν",
    },
    "%d screenshots found": {
        "Es": "%d capturas encontradas", "De": "%d Screenshots gefunden",
        "Fr": "%d captures d'écran trouvées", "It": "%d screenshot trovati",
        "Pt": "%d capturas encontradas", "Ru": "Найдено скриншотов: %d",
        "Ar": "تم العثور على %d لقطات شاشة", "Hi": "%d स्क्रीनशॉट मिले", "Zh": "发现 %d 张截图",
        "Ja": "スクリーンショット %d 枚", "Ko": "스크린샷 %d장 발견", "Tr": "%d ekran görüntüsü bulundu",
        "Vi": "Tìm thấy %d ảnh chụp màn hình", "Id": "%d tangkapan layar ditemukan",
        "Nl": "%d screenshots gevonden", "Pl": "Znaleziono %d zrzutów",
        "Uk": "Знайдено знімків: %d", "Th": "พบภาพหน้าจอ %d รูป",
        "El": "Βρέθηκαν %d στιγμιότυπα",
    },
    "%d similar photos": {
        "Es": "%d fotos similares", "De": "%d ähnliche Fotos", "Fr": "%d photos similaires",
        "It": "%d foto simili", "Pt": "%d fotos semelhantes", "Ru": "%d похожих фото",
        "Ar": "%d صور متشابهة", "Hi": "%d समान फ़ोटो", "Zh": "%d 张相似照片",
        "Ja": "類似写真 %d 枚", "Ko": "비슷한 사진 %d장", "Tr": "%d benzer fotoğraf",
        "Vi": "%d ảnh tương tự", "Id": "%d foto serupa", "Nl": "%d vergelijkbare foto's",
        "Pl": "%d podobnych zdjęć", "Uk": "%d схожих фото", "Th": "รูปคล้ายกัน %d รูป",
        "El": "%d παρόμοιες φωτογραφίες",
    },
    "%d old photos": {
        "Es": "%d fotos antiguas", "De": "%d alte Fotos", "Fr": "%d anciennes photos",
        "It": "%d foto vecchie", "Pt": "%d fotos antigas", "Ru": "%d старых фото",
        "Ar": "%d صور قديمة", "Hi": "%d पुरानी फ़ोटो", "Zh": "%d 张旧照片",
        "Ja": "古い写真 %d 枚", "Ko": "오래된 사진 %d장", "Tr": "%d eski fotoğraf",
        "Vi": "%d ảnh cũ", "Id": "%d foto lama", "Nl": "%d oude foto's",
        "Pl": "%d starych zdjęć", "Uk": "%d старих фото", "Th": "รูปเก่า %d รูป",
        "El": "%d παλιές φωτογραφίες",
    },
    "%d optimizable images": {
        "Es": "%d imágenes optimizables", "De": "%d optimierbare Bilder",
        "Fr": "%d images optimisables", "It": "%d immagini ottimizzabili",
        "Pt": "%d imagens otimizáveis", "Ru": "%d изображений для оптимизации",
        "Ar": "%d صور قابلة للتحسين", "Hi": "%d अनुकूलन योग्य छवियाँ", "Zh": "%d 张可优化图片",
        "Ja": "最適化できる画像 %d 枚", "Ko": "최적화 가능 이미지 %d장", "Tr": "%d optimize edilebilir görsel",
        "Vi": "%d ảnh có thể tối ưu", "Id": "%d gambar dapat dioptimalkan",
        "Nl": "%d optimaliseerbare afbeeldingen", "Pl": "%d obrazów do optymalizacji",
        "Uk": "%d зображень для оптимізації", "Th": "รูปที่ปรับได้ %d รูป",
        "El": "%d εικόνες προς βελτιστοποίηση",
    },
    "Get %s more space": {
        "Es": "Gana %s más de espacio", "De": "Gewinne %s mehr Speicher",
        "Fr": "Gagnez %s d'espace en plus", "It": "Ottieni %s di spazio in più",
        "Pt": "Ganhe mais %s de espaço", "Ru": "Получите ещё %s",
        "Ar": "احصل على مساحة إضافية %s", "Hi": "%s और जगह पाएँ", "Zh": "再腾出 %s 空间",
        "Ja": "さらに %s の空き容量", "Ko": "%s 더 확보", "Tr": "%s daha fazla yer aç",
        "Vi": "Thêm %s dung lượng", "Id": "Dapatkan %s ruang lagi",
        "Nl": "Krijg %s meer ruimte", "Pl": "Zyskaj %s więcej miejsca",
        "Uk": "Отримайте ще %s", "Th": "ได้พื้นที่เพิ่ม %s", "El": "Κερδίστε %s περισσότερο χώρο",
    },
    "%d videos using %s": {
        "Es": "%d vídeos usan %s", "De": "%d Videos belegen %s",
        "Fr": "%d vidéos utilisent %s", "It": "%d video usano %s",
        "Pt": "%d vídeos usam %s", "Ru": "%d видео занимают %s",
        "Ar": "%d مقاطع تستخدم %s", "Hi": "%d वीडियो %s इस्तेमाल कर रहे हैं",
        "Zh": "%d 个视频占用 %s", "Ja": "%d 本の動画が %s 使用", "Ko": "동영상 %d개가 %s 사용",
        "Tr": "%d video %s kullanıyor", "Vi": "%d video đang dùng %s",
        "Id": "%d video memakai %s", "Nl": "%d video's gebruiken %s",
        "Pl": "%d filmów zajmuje %s", "Uk": "%d відео займають %s",
        "Th": "วิดีโอ %d รายการใช้ %s", "El": "%d βίντεο χρησιμοποιούν %s",
    },
    "Shrink your photos and free up storage space while keeping the memories.": {
        "Es": "Reduce tus fotos y libera espacio sin perder recuerdos.",
        "De": "Verkleinern Sie Fotos und schaffen Sie Speicherplatz, ohne Erinnerungen zu verlieren.",
        "Fr": "Réduisez vos photos et libérez de l'espace tout en gardant vos souvenirs.",
        "It": "Riduci le foto e libera spazio senza perdere i ricordi.",
        "Pt": "Reduza suas fotos e libere espaço mantendo as memórias.",
        "Ru": "Сжимайте фото и освобождайте место, сохраняя воспоминания.",
        "Ar": "صغّر صورك وحرّر مساحة مع الاحتفاظ بالذكريات.",
        "Hi": "फ़ोटो छोटा करें और यादें रखते हुए जगह खाली करें।",
        "Zh": "压缩照片以释放空间，同时保留回忆。",
        "Ja": "写真を縮小して容量を空け、思い出はそのまま残します。",
        "Ko": "추억은 유지하면서 사진을 줄여 공간을 확보하세요.",
        "Tr": "Anıları koruyarak fotoğrafları küçültüp yer açın.",
        "Vi": "Thu nhỏ ảnh và giải phóng dung lượng mà vẫn giữ kỷ niệm.",
        "Id": "Perkecil foto dan bebaskan ruang tanpa kehilangan kenangan.",
        "Nl": "Verklein foto's en maak ruimte vrij zonder herinneringen te verliezen.",
        "Pl": "Zmniejsz zdjęcia i zwolnij miejsce, zachowując wspomnienia.",
        "Uk": "Стискайте фото й звільняйте місце, зберігаючи спогади.",
        "Th": "ย่อรูปและเพิ่มพื้นที่โดยไม่เสียความทรงจำ",
        "El": "Συμπιέστε φωτογραφίες και ελευθερώστε χώρο χωρίς να χάσετε αναμνήσεις.",
    },
    "Lighter videos, same moments. Free space by reviewing oversized clips.": {
        "Es": "Vídeos más ligeros, mismos momentos. Libera espacio revisando clips grandes.",
        "De": "Leichtere Videos, gleiche Momente. Prüfen Sie große Clips und schaffen Sie Platz.",
        "Fr": "Vidéos plus légères, mêmes moments. Libérez de l'espace en examinant les gros clips.",
        "It": "Video più leggeri, stessi momenti. Libera spazio rivedendo i clip grandi.",
        "Pt": "Vídeos mais leves, mesmos momentos. Liberte espaço revisando clipes grandes.",
        "Ru": "Более лёгкие видео, те же моменты. Освободите место, проверив крупные ролики.",
        "Ar": "مقاطع أخف بنفس اللحظات. حرّر مساحة بمراجعة المقاطع الكبيرة.",
        "Hi": "हल्के वीडियो, वही पल। बड़े क्लिप देखकर जगह खाली करें।",
        "Zh": "更轻的视频，同样的瞬间。检查过大片段以释放空间。",
        "Ja": "軽い動画で同じ思い出。大きなクリップを見直して容量を空けます。",
        "Ko": "가벼운 동영상, 같은 순간. 큰 클립을 검토해 공간을 확보하세요.",
        "Tr": "Daha hafif videolar, aynı anılar. Büyük klipleri inceleyerek yer açın.",
        "Vi": "Video nhẹ hơn, cùng khoảnh khắc. Giải phóng chỗ bằng cách xem lại clip lớn.",
        "Id": "Video lebih ringan, momen sama. Bebaskan ruang dengan meninjau klip besar.",
        "Nl": "Lichtere video's, dezelfde momenten. Maak ruimte vrij door grote clips te bekijken.",
        "Pl": "Lżejsze wideo, te same chwile. Zwolnij miejsce, przeglądając duże klipy.",
        "Uk": "Легші відео, ті самі моменти. Звільніть місце, переглянувши великі кліпи.",
        "Th": "วิดีโอบางลง โมเมนต์เดิม ตรวจคลิปใหญ่เพื่อเพิ่มพื้นที่",
        "El": "Ελαφρύτερα βίντεο, ίδιες στιγμές. Ελευθερώστε χώρο ελέγχοντας μεγάλα κλιπ.",
    },
    "Large videos use the most space. Select clips to remove local copies after you've backed them up.": {
        "Es": "Los vídeos grandes ocupan más espacio. Selecciona clips para borrar copias locales tras respaldarlos.",
        "De": "Große Videos belegen am meisten Speicher. Wähle Clips zum Löschen lokaler Kopien nach dem Backup.",
        "Fr": "Les grosses vidéos prennent le plus de place. Sélectionnez des clips à supprimer localement après sauvegarde.",
        "It": "I video grandi usano più spazio. Seleziona clip da rimuovere in locale dopo il backup.",
        "Pt": "Vídeos grandes usam mais espaço. Selecione clipes para remover cópias locais após o backup.",
        "Ru": "Большие видео занимают больше всего места. Выберите ролики для удаления локальных копий после резервного копирования.",
        "Ar": "مقاطع الفيديو الكبيرة تستهلك أكبر مساحة. اختر المقاطع لحذف النسخ المحلية بعد النسخ الاحتياطي.",
        "Hi": "बड़े वीडियो सबसे ज़्यादा जगह लेते हैं। बैकअप के बाद लोकल कॉपी हटाने के लिए क्लिप चुनें।",
        "Zh": "大型视频占用最多空间。备份后选择片段删除本地副本。",
        "Ja": "大きな動画が最も容量を使います。バックアップ後にローカルコピーを削除するクリップを選択してください。",
        "Ko": "큰 동영상이 공간을 가장 많이 씁니다. 백업 후 로컬 복사본을 삭제할 클립을 선택하세요.",
        "Tr": "Büyük videolar en çok yer kaplar. Yedekledikten sonra yerel kopyaları silmek için klipleri seçin.",
        "Vi": "Video lớn chiếm nhiều dung lượng nhất. Chọn clip để xóa bản cục bộ sau khi sao lưu.",
        "Id": "Video besar memakai ruang terbanyak. Pilih klip untuk menghapus salinan lokal setelah dicadangkan.",
        "Nl": "Grote video's gebruiken de meeste ruimte. Selecteer clips om lokale kopieën te verwijderen na een back-up.",
        "Pl": "Duże wideo zajmują najwięcej miejsca. Wybierz klipy do usunięcia lokalnych kopii po kopii zapasowej.",
        "Uk": "Великі відео займають найбільше місця. Виберіть кліпи, щоб видалити локальні копії після резервного копіювання.",
        "Th": "วิดีโอใหญ่ใช้พื้นที่มากที่สุด เลือกคลิปเพื่อลบสำเนาในเครื่องหลังสำรองแล้ว",
        "El": "Τα μεγάλα βίντεο καταλαμβάνουν τον περισσότερο χώρο. Επιλέξτε κλιπ για διαγραφή τοπικών αντιγράφων μετά το αντίγραφο ασφαλείας.",
    },
    "Pick apps to force-stop. Android will confirm each one in system settings.": {
        "Es": "Elige apps para forzar detención. Android confirmará cada una en ajustes del sistema.",
        "De": "Apps zum Beenden wählen. Android bestätigt jede in den Systemeinstellungen.",
        "Fr": "Choisissez des applis à forcer l'arrêt. Android confirmera chacune dans les paramètres.",
        "It": "Scegli le app da forzare l'arresto. Android conferma ciascuna nelle impostazioni di sistema.",
        "Pt": "Escolha apps para forçar parada. O Android confirmará cada uma nas configurações.",
        "Ru": "Выберите приложения для принудительной остановки. Android подтвердит каждое в настройках.",
        "Ar": "اختر تطبيقات لإيقافها إجباريًا. سيؤكد Android كلًا منها في إعدادات النظام.",
        "Hi": "फोर्स-स्टॉप के लिए ऐप्स चुनें। Android सिस्टम सेटिंग्स में प्रत्येक की पुष्टि करेगा।",
        "Zh": "选择要强制停止的应用。Android 会在系统设置中逐个确认。",
        "Ja": "強制停止するアプリを選びます。Android がシステム設定でそれぞれ確認します。",
        "Ko": "강제 중지할 앱을 고르세요. Android가 시스템 설정에서 각각 확인합니다.",
        "Tr": "Zorla durdurulacak uygulamaları seçin. Android her birini sistem ayarlarında onaylar.",
        "Vi": "Chọn ứng dụng để buộc dừng. Android sẽ xác nhận từng cái trong cài đặt hệ thống.",
        "Id": "Pilih aplikasi untuk dipaksa berhenti. Android akan mengonfirmasi masing-masing di pengaturan sistem.",
        "Nl": "Kies apps om geforceerd te stoppen. Android bevestigt elke in de systeeminstellingen.",
        "Pl": "Wybierz aplikacje do wymuszonego zatrzymania. Android potwierdzi każdą w ustawieniach systemu.",
        "Uk": "Виберіть програми для примусової зупинки. Android підтвердить кожну в системних налаштуваннях.",
        "Th": "เลือกแอปที่จะบังคับหยุด Android จะยืนยันทีละตัวในการตั้งค่าระบบ",
        "El": "Επιλέξτε εφαρμογές για αναγκαστική διακοπή. Το Android θα επιβεβαιώσει καθεμία στις ρυθμίσεις συστήματος.",
    },
    "Deep Clean walks hidden caches and browser leftovers. Start Quick Clean to review and remove what Android allows.": {
        "Es": "Limpieza profunda revisa cachés ocultas y restos del navegador. Abre Limpieza rápida para revisar y eliminar lo que Android permita.",
        "De": "Tiefenreinigung prüft versteckte Caches und Browserreste. Starte Schnellreinigung, um Freigaben von Android zu prüfen und zu entfernen.",
        "Fr": "Le nettoyage approfondi parcourt caches cachés et restes de navigateur. Lancez le nettoyage rapide pour examiner et supprimer ce qu'Android autorise.",
        "It": "Pulizia approfondita esplora cache nascoste e residui del browser. Avvia Pulizia rapida per rivedere e rimuovere ciò che Android consente.",
        "Pt": "A limpeza profunda analisa caches ocultos e restos do navegador. Inicie a limpeza rápida para revisar e remover o que o Android permitir.",
        "Ru": "Глубокая очистка проверяет скрытый кэш и остатки браузера. Запустите быструю очистку, чтобы удалить то, что разрешает Android.",
        "Ar": "التنظيف العميق يفحص الذاكرة المؤقتة المخفية وبقايا المتصفح. ابدأ التنظيف السريع لمراجعة وحذف ما يسمح به Android.",
        "Hi": "डीप क्लीन छिपा कैश और ब्राउज़र अवशेष देखता है। Android जो अनुमति दे उसे हटाने के लिए क्विक क्लीन शुरू करें।",
        "Zh": "深度清理会检查隐藏缓存和浏览器残留。启动快速清理以审查并删除 Android 允许的内容。",
        "Ja": "ディープクリーンは非表示キャッシュとブラウザの残りを確認します。Android が許可するものを確認・削除するにはクイッククリーンを開始してください。",
        "Ko": "딥 클린은 숨겨진 캐시와 브라우저 잔여물을 확인합니다. Android가 허용하는 항목을 검토·삭제하려면 빠른 정리를 시작하세요.",
        "Tr": "Derin Temizlik gizli önbellekleri ve tarayıcı artıkları tarar. Android'in izin verdiğini incelemek ve silmek için Hızlı Temizlik'i başlatın.",
        "Vi": "Dọn sâu kiểm tra bộ nhớ đệm ẩn và dư thừa trình duyệt. Mở Dọn nhanh để xem và xóa những gì Android cho phép.",
        "Id": "Pembersihan Mendalam menelusuri cache tersembunyi dan sisa browser. Mulai Pembersihan Cepat untuk meninjau dan menghapus yang diizinkan Android.",
        "Nl": "Diepe reiniging doorzoekt verborgen caches en browserresten. Start snelle opschoning om te bekijken en te verwijderen wat Android toestaat.",
        "Pl": "Głębokie czyszczenie sprawdza ukryte pamięci i resztki przeglądarki. Uruchom szybkie czyszczenie, aby przejrzeć i usunąć to, na co pozwala Android.",
        "Uk": "Глибоке очищення перевіряє прихований кеш і залишки браузера. Запустіть швидке очищення, щоб переглянути й видалити те, що дозволяє Android.",
        "Th": "ทำความสะอาดเชิงลึกตรวจแคชที่ซ่อนและเศษเบราว์เซอร์ เริ่มทำความสะอาดด่วนเพื่อตรวจและลบสิ่งที่ Android อนุญาต",
        "El": "Ο βαθύς καθαρισμός ελέγχει κρυφές προσωρινές μνήμες και υπολείμματα προγράμματος περιήγησης. Ξεκινήστε γρήγορο καθαρισμό για έλεγχο και διαγραφή όσων επιτρέπει το Android.",
    },
    "Browser data sizes are measured in Quick Clean. Open the locked Browser data row to jump into each browser's storage screen.": {
        "Es": "Los tamaños de datos del navegador se miden en Limpieza rápida. Abre la fila bloqueada de Datos del navegador para ir a la pantalla de almacenamiento de cada navegador.",
        "De": "Browserdatengrößen werden in der Schnellreinigung gemessen. Öffne die gesperrte Zeile Browserdaten, um den Speicherbildschirm jedes Browsers zu öffnen.",
        "Fr": "Les tailles des données de navigateur sont mesurées dans le nettoyage rapide. Ouvrez la ligne verrouillée Données du navigateur pour accéder à l'écran de stockage de chaque navigateur.",
        "It": "Le dimensioni dei dati del browser sono misurate in Pulizia rapida. Apri la riga bloccata Dati del browser per aprire la schermata di archiviazione di ciascun browser.",
        "Pt": "Os tamanhos dos dados do navegador são medidos na limpeza rápida. Abra a linha bloqueada Dados do navegador para ir à tela de armazenamento de cada navegador.",
        "Ru": "Размеры данных браузера измеряются в быстрой очистке. Откройте заблокированную строку «Данные браузера», чтобы перейти к экрану хранилища каждого браузера.",
        "Ar": "تُقاس أحجام بيانات المتصفح في التنظيف السريع. افتح صف بيانات المتصفح المقفل للانتقال إلى شاشة تخزين كل متصفح.",
        "Hi": "ब्राउज़र डेटा का आकार क्विक क्लीन में मापा जाता है। प्रत्येक ब्राउज़र की स्टोरेज स्क्रीन खोलने के लिए लॉक की गई पंक्ति खोलें।",
        "Zh": "浏览器数据大小在快速清理中测量。打开锁定的“浏览器数据”行以进入各浏览器的存储界面。",
        "Ja": "ブラウザデータのサイズはクイッククリーンで計測されます。ロックされた「ブラウザデータ」行を開いて各ブラウザのストレージ画面へ移動します。",
        "Ko": "브라우저 데이터 크기는 빠른 정리에서 측정됩니다. 잠긴 브라우저 데이터 행을 열어 각 브라우저 저장공간 화면으로 이동하세요.",
        "Tr": "Tarayıcı verisi boyutları Hızlı Temizlik'te ölçülür. Her tarayıcının depolama ekranına gitmek için kilitli Tarayıcı verileri satırını açın.",
        "Vi": "Kích thước dữ liệu trình duyệt được đo trong Dọn nhanh. Mở hàng Dữ liệu trình duyệt bị khóa để vào màn hình lưu trữ của từng trình duyệt.",
        "Id": "Ukuran data browser diukur di Pembersihan Cepat. Buka baris Data browser yang terkunci untuk masuk ke layar penyimpanan tiap browser.",
        "Nl": "Browsergegevensgrootten worden gemeten in snelle opschoning. Open de vergrendelde rij Browsergegevens om naar het opslagscherm van elke browser te gaan.",
        "Pl": "Rozmiary danych przeglądarki są mierzone w szybkim czyszczeniu. Otwórz zablokowany wiersz Dane przeglądarki, aby przejść do ekranu pamięci każdej przeglądarki.",
        "Uk": "Розміри даних браузера вимірюються в швидкому очищенні. Відкрийте заблокований рядок «Дані браузера», щоб перейти до екрана сховища кожного браузера.",
        "Th": "ขนาดข้อมูลเบราว์เซอร์วัดในทำความสะอาดด่วน เปิดแถวข้อมูลเบราว์เซอร์ที่ล็อกเพื่อไปหน้าจอที่เก็บของแต่ละเบราว์เซอร์",
        "El": "Τα μεγέθη δεδομένων προγράμματος περιήγησης μετρώνται στον γρήγορο καθαρισμό. Ανοίξτε την κλειδωμένη γραμμή Δεδομένα προγράμματος περιήγησης για την οθόνη αποθήκευσης κάθε προγράμματος.",
    },
}

PACKS = [
    ("phrasesEs", "Es"), ("phrasesDe", "De"), ("phrasesFr", "Fr"), ("phrasesIt", "It"),
    ("phrasesPt", "Pt"), ("phrasesRu", "Ru"), ("phrasesAr", "Ar"), ("phrasesHi", "Hi"),
    ("phrasesZh", "Zh"), ("phrasesJa", "Ja"), ("phrasesKo", "Ko"), ("phrasesTr", "Tr"),
    ("phrasesVi", "Vi"), ("phrasesId", "Id"), ("phrasesNl", "Nl"), ("phrasesPl", "Pl"),
    ("phrasesUk", "Uk"), ("phrasesTh", "Th"), ("phrasesEl", "El"),
]


def escape_kt(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n")


def insert_entries(block: str, entries: dict[str, str]) -> str:
    """Insert missing key->value pairs before the closing of a mapOf block."""
    existing = set(re.findall(r'"((?:\\.|[^"\\])*)"\s+to\s+', block))
    to_add = []
    for k, v in entries.items():
        if k not in existing:
            to_add.append(f'    "{escape_kt(k)}" to "{escape_kt(v)}"')
    if not to_add:
        return block
    # Insert before final closing paren of mapOf — last line is typically ")"
    # Find last non-empty content line
    lines = block.rstrip().split("\n")
    # Ensure previous line ends with comma
    for i in range(len(lines) - 1, -1, -1):
        if lines[i].strip() and not lines[i].strip().startswith("//"):
            if not lines[i].rstrip().endswith(",") and lines[i].strip() != ")":
                # last entry might lack trailing comma before )
                pass
            break
    # Rebuild: everything until final ")"
    if lines[-1].strip() != ")":
        raise SystemExit(f"Unexpected block ending: {lines[-1]!r}")
    body = lines[:-1]
    # Add comma to last entry if needed
    for i in range(len(body) - 1, -1, -1):
        s = body[i].rstrip()
        if s and not s.startswith("//"):
            if not s.endswith(","):
                body[i] = s + ","
            break
    body.extend(to_add)
    body.append(")")
    return "\n".join(body)


def main() -> None:
    text = PATH.read_text(encoding="utf-8")

    # phraseIdentity
    m = re.search(
        r"(internal val phraseIdentity: Map<String, String> = mapOf\()(.*?)(\n\))",
        text,
        re.S,
    )
    if not m:
        raise SystemExit("phraseIdentity not found")
    ident_entries = {k: k for k in NEW}
    new_ident = insert_entries(m.group(1) + m.group(2) + m.group(3), ident_entries)
    # insert_entries expects full mapOf(...) — fix
    ident_full = m.group(0)
    new_ident = insert_entries(ident_full, ident_entries)
    text = text[: m.start()] + new_ident + text[m.end() :]

    for pack_name, lang in PACKS:
        pattern = rf"(private val {pack_name}: Map<String, String> = mapOf\()(.*?)(\n\))"
        m = re.search(pattern, text, re.S)
        if not m:
            print(f"WARN: {pack_name} not found")
            continue
        entries = {k: NEW[k][lang] for k in NEW}
        full = m.group(0)
        updated = insert_entries(full, entries)
        text = text[: m.start()] + updated + text[m.end() :]
        print(f"updated {pack_name}")

    PATH.write_text(text, encoding="utf-8")
    print("done", PATH)


if __name__ == "__main__":
    main()
