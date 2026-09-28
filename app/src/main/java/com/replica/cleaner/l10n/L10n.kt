package com.replica.cleaner.l10n

/**
 * In-app string catalog. UI reads via [LocalL10n]; English is the fallback for any missing key.
 */
class L10n internal constructor(
    val language: AppLanguage,
    internal val values: Map<String, String>
) {
    operator fun get(key: String): String = values[key] ?: englishValues[key] ?: key

    val home get() = this["home"]
    val tools get() = this["tools"]
    val storage get() = this["storage"]
    val account get() = this["account"]
    val upgrade get() = this["upgrade"]
    val settings get() = this["settings"]
    val languageLabel get() = this["language"]
    val languageSubtitle get() = this["language_subtitle"]
    val themes get() = this["themes"]
    val about get() = this["about"]
    val quickClean get() = this["quick_clean"]
    val freeSpace get() = this["free_space"]
    val unneededFiles get() = this["unneeded_files"]
    val hiddenCaches get() = this["hidden_caches"]
    val filesToReview get() = this["files_to_review"]
    val removeJunk get() = this["remove_junk"]
    val removeJunkBody get() = this["remove_junk_body"]
    val makeMoreRoom get() = this["make_more_room"]
    val makeMoreRoomBody get() = this["make_more_room_body"]
    val sleepMode get() = this["sleep_mode"]
    val tips get() = this["tips"]
    val media get() = this["media"]
    val apps get() = this["apps"]
    val photos get() = this["photos"]
    val audio get() = this["audio"]
    val video get() = this["video"]
    val others get() = this["others"]
    val seeTips get() = this["see_tips"]
    val signIn get() = this["sign_in"]
    val redeem get() = this["redeem"]
    val exploreFeatures get() = this["explore_features"]
    val analysisPrefs get() = this["analysis_prefs"]
    val notifications get() = this["notifications"]
    val realtime get() = this["realtime"]
    val cloudServices get() = this["cloud_services"]
    val personalPrivacy get() = this["personal_privacy"]
    val deepClean get() = this["deep_clean"]
    val browserCleaner get() = this["browser_cleaner"]
    val autoCleaning get() = this["auto_cleaning"]
    val photoOptimizer get() = this["photo_optimizer"]
    val videoOptimizer get() = this["video_optimizer"]
    val cloudTransfers get() = this["cloud_transfers"]
    val systemInfo get() = this["system_info"]
    val continueLabel get() = this["continue"]
    val cancel get() = this["cancel"]
    val back get() = this["back"]
    val scanning get() = this["scanning"]
    val customize get() = this["customize"]
    val freeUpTo get() = this["free_up_to"]
    val withPremium get() = this["with_premium"]
    val signOut get() = this["sign_out"]
    val licenses get() = this["licenses"]
    val selected get() = this["selected"]
    val done get() = this["done"]
    val allow get() = this["allow"]
    val notNow get() = this["not_now"]
    val continueWithAds get() = this["continue_with_ads"]
    val privacyPolicy get() = this["privacy_policy"]
    val spaceFreed get() = this["space_freed"]

    fun freeUpTo(bytesLabel: String): String =
        freeUpTo.replace("%s", bytesLabel)

    companion object {
        val english: L10n = L10n(AppLanguage.DEFAULT, englishValues + phraseIdentity)

        fun forDisplayName(displayName: String): L10n =
            forLanguage(AppLanguage.fromDisplayName(displayName))

        fun forLanguage(lang: AppLanguage): L10n {
            // Do NOT merge phraseIdentity into non-English catalogs: identity entries
            // echo English and would short-circuit [tr] before slug/keyed translations apply.
            val catalog = if (lang.tag == "en" || lang == AppLanguage.DEFAULT) {
                englishValues + phraseIdentity
            } else {
                englishValues + lookupTable(lang.tag) + phrasesFor(lang.tag)
            }
            return L10n(lang, catalog)
        }
    }
}

internal val englishValues: Map<String, String> = mapOf(
    "home" to "Home",
    "tools" to "Tools",
    "storage" to "Storage",
    "account" to "Account",
    "upgrade" to "UPGRADE",
    "settings" to "Settings",
    "language" to "Language",
    "language_subtitle" to "Some languages require additional download. It may take some time.",
    "themes" to "Themes",
    "about" to "About",
    "quick_clean" to "Quick Clean",
    "free_space" to "Free space",
    "unneeded_files" to "Unneeded files",
    "hidden_caches" to "Hidden caches",
    "files_to_review" to "Files to review",
    "remove_junk" to "Remove Junk",
    "remove_junk_body" to "Clean up unnecessary files to free up space instantly.",
    "make_more_room" to "Make More Room",
    "make_more_room_body" to "Free up storage by optimizing images or moving files to the cloud - without deleting what matters.",
    "sleep_mode" to "Sleep Mode",
    "tips" to "Tips",
    "media" to "Media",
    "apps" to "Apps",
    "photos" to "Photos",
    "audio" to "Audio",
    "video" to "Video",
    "others" to "Others",
    "see_tips" to "SEE TIPS",
    "sign_in" to "SIGN IN",
    "redeem" to "Redeem subscription",
    "explore_features" to "Explore features",
    "analysis_prefs" to "Analysis preferences",
    "notifications" to "Notifications",
    "realtime" to "Real-time Detection",
    "cloud_services" to "Cloud services",
    "personal_privacy" to "Personal privacy",
    "deep_clean" to "Deep Clean",
    "browser_cleaner" to "Browser Cleaner",
    "auto_cleaning" to "Automatic Cleaning",
    "photo_optimizer" to "Photo Optimizer",
    "video_optimizer" to "Video Optimizer",
    "cloud_transfers" to "Cloud Transfers",
    "system_info" to "System Info",
    "continue" to "CONTINUE",
    "cancel" to "Cancel",
    "back" to "Back",
    "scanning" to "Scanning…",
    "customize" to "Customize",
    "free_up_to" to "Free up to %s",
    "with_premium" to "WITH PREMIUM YOU'LL HAVE",
    "sign_out" to "Sign out",
    "licenses" to "Open source licenses",
    "selected" to "Selected",
    "done" to "Done",
    "allow" to "ALLOW",
    "not_now" to "NOT NOW",
    "continue_with_ads" to "CONTINUE WITH ADS",
    "privacy_policy" to "Privacy Policy",
    "space_freed" to "Space freed",
    // Phrase-as-key entries (identity) live in [phraseIdentity]; language packs override via [phrasesFor].
)

private val es = mapOf(
    "home" to "Inicio", "tools" to "Herramientas", "storage" to "Almacenamiento",
    "account" to "Cuenta", "upgrade" to "MEJORAR", "settings" to "Ajustes",
    "language" to "Idioma",
    "language_subtitle" to "Algunos idiomas requieren descarga adicional. Puede tardar un poco.",
    "themes" to "Temas", "about" to "Acerca de", "quick_clean" to "Limpieza rápida",
    "free_space" to "Espacio libre", "unneeded_files" to "Archivos innecesarios",
    "hidden_caches" to "Cachés ocultas", "files_to_review" to "Archivos a revisar",
    "remove_junk" to "Eliminar basura",
    "remove_junk_body" to "Limpia archivos innecesarios para liberar espacio al instante.",
    "make_more_room" to "Liberar más espacio",
    "make_more_room_body" to "Libera almacenamiento optimizando imágenes o moviendo archivos a la nube.",
    "sleep_mode" to "Modo suspensión", "tips" to "Consejos", "media" to "Multimedia",
    "apps" to "Apps", "photos" to "Fotos", "audio" to "Audio", "video" to "Vídeo",
    "others" to "Otros", "see_tips" to "VER CONSEJOS", "sign_in" to "INICIAR SESIÓN",
    "redeem" to "Canjear suscripción", "explore_features" to "Explorar funciones",
    "analysis_prefs" to "Preferencias de análisis", "notifications" to "Notificaciones",
    "realtime" to "Detección en tiempo real", "cloud_services" to "Servicios en la nube",
    "personal_privacy" to "Privacidad personal", "deep_clean" to "Limpieza profunda",
    "browser_cleaner" to "Limpiador de navegador", "auto_cleaning" to "Limpieza automática",
    "photo_optimizer" to "Optimizador de fotos", "video_optimizer" to "Optimizador de vídeo",
    "cloud_transfers" to "Transferencias a la nube", "system_info" to "Info del sistema",
    "continue" to "CONTINUAR", "cancel" to "Cancelar", "back" to "Atrás",
    "scanning" to "Analizando…", "customize" to "Personalizar",
    "free_up_to" to "Libera hasta %s", "with_premium" to "CON PREMIUM TENDRÁS",
    "sign_out" to "Cerrar sesión", "licenses" to "Licencias de código abierto",
    "selected" to "Seleccionado", "done" to "Listo", "allow" to "PERMITIR",
    "not_now" to "AHORA NO", "continue_with_ads" to "CONTINUAR CON ANUNCIOS",
    "privacy_policy" to "Política de privacidad", "space_freed" to "Espacio liberado"
)

private val de = mapOf(
    "home" to "Start", "tools" to "Tools", "storage" to "Speicher",
    "account" to "Konto", "upgrade" to "UPGRADE", "settings" to "Einstellungen",
    "language" to "Sprache",
    "language_subtitle" to "Einige Sprachen erfordern einen zusätzlichen Download.",
    "themes" to "Designs", "about" to "Info", "quick_clean" to "Schnellreinigung",
    "free_space" to "Freier Speicher", "unneeded_files" to "Unnötige Dateien",
    "hidden_caches" to "Versteckte Caches", "files_to_review" to "Zu prüfende Dateien",
    "remove_junk" to "Müll entfernen",
    "remove_junk_body" to "Entfernen Sie unnötige Dateien und schaffen Sie sofort Platz.",
    "make_more_room" to "Mehr Platz schaffen",
    "make_more_room_body" to "Speicher freigeben durch Optimieren von Bildern oder Cloud-Upload.",
    "sleep_mode" to "Schlafmodus", "tips" to "Tipps", "media" to "Medien",
    "apps" to "Apps", "photos" to "Fotos", "audio" to "Audio", "video" to "Video",
    "others" to "Sonstiges", "see_tips" to "TIPPS ANZEIGEN", "sign_in" to "ANMELDEN",
    "redeem" to "Abo einlösen", "explore_features" to "Funktionen entdecken",
    "analysis_prefs" to "Analyse-Einstellungen", "notifications" to "Benachrichtigungen",
    "realtime" to "Echtzeit-Erkennung", "cloud_services" to "Cloud-Dienste",
    "personal_privacy" to "Datenschutz", "deep_clean" to "Tiefenreinigung",
    "browser_cleaner" to "Browser-Reiniger", "auto_cleaning" to "Automatische Reinigung",
    "photo_optimizer" to "Foto-Optimierer", "video_optimizer" to "Video-Optimierer",
    "cloud_transfers" to "Cloud-Transfers", "system_info" to "Systeminfo",
    "continue" to "WEITER", "cancel" to "Abbrechen", "back" to "Zurück",
    "scanning" to "Scannen…", "customize" to "Anpassen",
    "free_up_to" to "Bis zu %s freigeben", "with_premium" to "MIT PREMIUM ERHALTEN SIE",
    "sign_out" to "Abmelden", "licenses" to "Open-Source-Lizenzen",
    "selected" to "Ausgewählt", "done" to "Fertig", "allow" to "ERLAUBEN",
    "not_now" to "NICHT JETZT", "continue_with_ads" to "MIT WERBUNG FORTFAHREN",
    "privacy_policy" to "Datenschutzerklärung", "space_freed" to "Freigegebener Speicher"
)

private val fr = mapOf(
    "home" to "Accueil", "tools" to "Outils", "storage" to "Stockage",
    "account" to "Compte", "upgrade" to "PASSER À PREMIUM", "settings" to "Paramètres",
    "language" to "Langue",
    "language_subtitle" to "Certaines langues nécessitent un téléchargement. Cela peut prendre du temps.",
    "themes" to "Thèmes", "about" to "À propos", "quick_clean" to "Nettoyage rapide",
    "free_space" to "Espace libre", "unneeded_files" to "Fichiers inutiles",
    "hidden_caches" to "Caches cachés", "files_to_review" to "Fichiers à revoir",
    "remove_junk" to "Supprimer les fichiers indésirables",
    "remove_junk_body" to "Nettoyez les fichiers inutiles pour libérer de l'espace instantanément.",
    "make_more_room" to "Faire de la place",
    "make_more_room_body" to "Libérez de l'espace en optimisant les images ou en les envoyant dans le cloud.",
    "sleep_mode" to "Mode veille", "tips" to "Astuces", "media" to "Médias",
    "apps" to "Applis", "photos" to "Photos", "audio" to "Audio", "video" to "Vidéo",
    "others" to "Autres", "see_tips" to "VOIR LES ASTUCES", "sign_in" to "CONNEXION",
    "redeem" to "Utiliser un abonnement", "explore_features" to "Explorer les fonctions",
    "analysis_prefs" to "Préférences d'analyse", "notifications" to "Notifications",
    "realtime" to "Détection en temps réel", "cloud_services" to "Services cloud",
    "personal_privacy" to "Confidentialité", "deep_clean" to "Nettoyage approfondi",
    "browser_cleaner" to "Nettoyeur de navigateur", "auto_cleaning" to "Nettoyage auto",
    "photo_optimizer" to "Optimiseur de photos", "video_optimizer" to "Optimiseur vidéo",
    "cloud_transfers" to "Transferts cloud", "system_info" to "Infos système",
    "continue" to "CONTINUER", "cancel" to "Annuler", "back" to "Retour",
    "scanning" to "Analyse…", "customize" to "Personnaliser",
    "free_up_to" to "Libérez jusqu'à %s", "with_premium" to "AVEC PREMIUM VOUS AUREZ",
    "sign_out" to "Déconnexion", "licenses" to "Licences open source",
    "selected" to "Sélectionné", "done" to "Terminé", "allow" to "AUTORISER",
    "not_now" to "PAS MAINTENANT", "continue_with_ads" to "CONTINUER AVEC PUBS",
    "privacy_policy" to "Politique de confidentialité", "space_freed" to "Espace libéré"
)

private val ru = mapOf(
    "home" to "Главная", "tools" to "Инструменты", "storage" to "Память",
    "account" to "Аккаунт", "upgrade" to "ПРЕМИУМ", "settings" to "Настройки",
    "language" to "Язык",
    "language_subtitle" to "Для некоторых языков требуется дополнительная загрузка.",
    "themes" to "Темы", "about" to "О приложении", "quick_clean" to "Быстрая очистка",
    "free_space" to "Свободно", "unneeded_files" to "Ненужные файлы",
    "hidden_caches" to "Скрытый кэш", "files_to_review" to "Файлы на проверку",
    "remove_junk" to "Удалить мусор",
    "remove_junk_body" to "Очистите ненужные файлы и сразу освободите место.",
    "make_more_room" to "Освободить место",
    "make_more_room_body" to "Освободите память, оптимизируя фото или перенося файлы в облако.",
    "sleep_mode" to "Режим сна", "tips" to "Советы", "media" to "Медиа",
    "apps" to "Приложения", "photos" to "Фото", "audio" to "Аудио", "video" to "Видео",
    "others" to "Прочее", "see_tips" to "СОВЕТЫ", "sign_in" to "ВОЙТИ",
    "redeem" to "Активировать подписку", "explore_features" to "Обзор функций",
    "analysis_prefs" to "Настройки анализа", "notifications" to "Уведомления",
    "realtime" to "Обнаружение в реальном времени", "cloud_services" to "Облачные сервисы",
    "personal_privacy" to "Конфиденциальность", "deep_clean" to "Глубокая очистка",
    "browser_cleaner" to "Очистка браузера", "auto_cleaning" to "Автоочистка",
    "photo_optimizer" to "Оптимизация фото", "video_optimizer" to "Оптимизация видео",
    "cloud_transfers" to "Облачные переносы", "system_info" to "О системе",
    "continue" to "ПРОДОЛЖИТЬ", "cancel" to "Отмена", "back" to "Назад",
    "scanning" to "Сканирование…", "customize" to "Настроить",
    "free_up_to" to "Освободить до %s", "with_premium" to "С PREMIUM ВЫ ПОЛУЧИТЕ",
    "sign_out" to "Выйти", "licenses" to "Лицензии открытого ПО",
    "selected" to "Выбрано", "done" to "Готово", "allow" to "РАЗРЕШИТЬ",
    "not_now" to "НЕ СЕЙЧАС", "continue_with_ads" to "ПРОДОЛЖИТЬ С РЕКЛАМОЙ",
    "privacy_policy" to "Политика конфиденциальности", "space_freed" to "Освобождено"
)

private val tables: Map<String, Map<String, String>> = mapOf(
    "es" to es,
    "de" to de,
    "fr" to fr,
    "ru" to ru,
    "it" to mapOf(
        "home" to "Home", "tools" to "Strumenti", "storage" to "Memoria",
        "account" to "Account", "upgrade" to "UPGRADE", "settings" to "Impostazioni",
        "language" to "Lingua",
        "language_subtitle" to "Alcune lingue richiedono un download aggiuntivo.",
        "themes" to "Temi", "about" to "Info", "quick_clean" to "Pulizia rapida",
        "free_space" to "Spazio libero", "unneeded_files" to "File non necessari",
        "hidden_caches" to "Cache nascoste", "files_to_review" to "File da rivedere",
        "remove_junk" to "Rimuovi spazzatura",
        "remove_junk_body" to "Pulisci i file non necessari per liberare spazio subito.",
        "make_more_room" to "Crea più spazio",
        "make_more_room_body" to "Libera spazio ottimizzando le immagini o spostandole sul cloud.",
        "sleep_mode" to "Modalità sonno", "tips" to "Suggerimenti", "media" to "Media",
        "apps" to "App", "photos" to "Foto", "audio" to "Audio", "video" to "Video",
        "others" to "Altro", "see_tips" to "VEDI SUGGERIMENTI", "sign_in" to "ACCEDI",
        "redeem" to "Riscatta abbonamento", "explore_features" to "Esplora funzioni",
        "analysis_prefs" to "Preferenze analisi", "notifications" to "Notifiche",
        "realtime" to "Rilevamento in tempo reale", "cloud_services" to "Servizi cloud",
        "personal_privacy" to "Privacy", "deep_clean" to "Pulizia approfondita",
        "browser_cleaner" to "Pulizia browser", "auto_cleaning" to "Pulizia automatica",
        "photo_optimizer" to "Ottimizzatore foto", "video_optimizer" to "Ottimizzatore video",
        "cloud_transfers" to "Trasferimenti cloud", "system_info" to "Info di sistema",
        "continue" to "CONTINUA", "cancel" to "Annulla", "back" to "Indietro",
        "scanning" to "Scansione…", "customize" to "Personalizza",
        "free_up_to" to "Libera fino a %s", "with_premium" to "CON PREMIUM AVRAI",
        "sign_out" to "Esci", "licenses" to "Licenze open source",
        "selected" to "Selezionato", "done" to "Fatto", "allow" to "CONSENTI",
        "not_now" to "NON ORA", "continue_with_ads" to "CONTINUA CON ANNUNCI",
        "privacy_policy" to "Informativa sulla privacy", "space_freed" to "Spazio liberato"
    ),
    "pt-BR" to mapOf(
        "home" to "Início", "tools" to "Ferramentas", "storage" to "Armazenamento",
        "account" to "Conta", "upgrade" to "UPGRADE", "settings" to "Configurações",
        "language" to "Idioma",
        "language_subtitle" to "Alguns idiomas exigem download adicional. Pode demorar um pouco.",
        "themes" to "Temas", "about" to "Sobre", "quick_clean" to "Limpeza rápida",
        "free_space" to "Espaço livre", "unneeded_files" to "Arquivos desnecessários",
        "hidden_caches" to "Caches ocultos", "files_to_review" to "Arquivos para revisar",
        "remove_junk" to "Remover lixo",
        "remove_junk_body" to "Limpe arquivos desnecessários para liberar espaço imediatamente.",
        "make_more_room" to "Liberar mais espaço",
        "make_more_room_body" to "Libere armazenamento otimizando imagens ou enviando para a nuvem.",
        "sleep_mode" to "Modo soneca", "tips" to "Dicas", "media" to "Mídia",
        "apps" to "Apps", "photos" to "Fotos", "audio" to "Áudio", "video" to "Vídeo",
        "others" to "Outros", "see_tips" to "VER DICAS", "sign_in" to "ENTRAR",
        "redeem" to "Resgatar assinatura", "explore_features" to "Explorar recursos",
        "analysis_prefs" to "Preferências de análise", "notifications" to "Notificações",
        "realtime" to "Detecção em tempo real", "cloud_services" to "Serviços na nuvem",
        "personal_privacy" to "Privacidade", "deep_clean" to "Limpeza profunda",
        "browser_cleaner" to "Limpador de navegador", "auto_cleaning" to "Limpeza automática",
        "photo_optimizer" to "Otimizador de fotos", "video_optimizer" to "Otimizador de vídeo",
        "cloud_transfers" to "Transferências na nuvem", "system_info" to "Info do sistema",
        "continue" to "CONTINUAR", "cancel" to "Cancelar", "back" to "Voltar",
        "scanning" to "Analisando…", "customize" to "Personalizar",
        "free_up_to" to "Liberar até %s", "with_premium" to "COM O PREMIUM VOCÊ TERÁ",
        "sign_out" to "Sair", "licenses" to "Licenças de código aberto",
        "selected" to "Selecionado", "done" to "Concluído", "allow" to "PERMITIR",
        "not_now" to "AGORA NÃO", "continue_with_ads" to "CONTINUAR COM ANÚNCIOS",
        "privacy_policy" to "Política de privacidade", "space_freed" to "Espaço liberado"
    ),
    "ar" to mapOf(
        "home" to "الرئيسية", "tools" to "الأدوات", "storage" to "التخزين",
        "account" to "الحساب", "upgrade" to "ترقية", "settings" to "الإعدادات",
        "language" to "اللغة",
        "language_subtitle" to "بعض اللغات تتطلب تنزيلاً إضافياً. قد يستغرق ذلك بعض الوقت.",
        "themes" to "السمات", "about" to "حول", "quick_clean" to "تنظيف سريع",
        "free_space" to "المساحة الحرة", "unneeded_files" to "ملفات غير ضرورية",
        "hidden_caches" to "ذاكرة مؤقتة مخفية", "files_to_review" to "ملفات للمراجعة",
        "remove_junk" to "إزالة المهملات",
        "remove_junk_body" to "نظّف الملفات غير الضرورية لتحرير مساحة فوراً.",
        "make_more_room" to "توفير مساحة أكبر",
        "make_more_room_body" to "حرّر التخزين بتحسين الصور أو نقل الملفات إلى السحابة.",
        "sleep_mode" to "وضع السكون", "tips" to "نصائح", "media" to "الوسائط",
        "apps" to "التطبيقات", "photos" to "الصور", "audio" to "الصوت", "video" to "الفيديو",
        "others" to "أخرى", "see_tips" to "عرض النصائح", "sign_in" to "تسجيل الدخول",
        "redeem" to "استرداد الاشتراك", "explore_features" to "استكشاف الميزات",
        "analysis_prefs" to "تفضيلات التحليل", "notifications" to "الإشعارات",
        "realtime" to "الكشف في الوقت الفعلي", "cloud_services" to "خدمات السحابة",
        "personal_privacy" to "الخصوصية", "deep_clean" to "تنظيف عميق",
        "browser_cleaner" to "منظف المتصفح", "auto_cleaning" to "تنظيف تلقائي",
        "photo_optimizer" to "محسّن الصور", "video_optimizer" to "محسّن الفيديو",
        "cloud_transfers" to "نقل إلى السحابة", "system_info" to "معلومات النظام",
        "continue" to "متابعة", "cancel" to "إلغاء", "back" to "رجوع",
        "scanning" to "جارٍ الفحص…", "customize" to "تخصيص",
        "free_up_to" to "حرر حتى %s", "with_premium" to "مع بريميوم ستحصل على",
        "sign_out" to "تسجيل الخروج", "licenses" to "تراخيص مفتوحة المصدر",
        "selected" to "محدد", "done" to "تم", "allow" to "سماح",
        "not_now" to "ليس الآن", "continue_with_ads" to "المتابعة مع الإعلانات",
        "privacy_policy" to "سياسة الخصوصية", "space_freed" to "المساحة المحررة"
    ),
    "hi" to mapOf(
        "home" to "होम", "tools" to "टूल्स", "storage" to "स्टोरेज",
        "account" to "अकाउंट", "upgrade" to "अपग्रेड", "settings" to "सेटिंग्स",
        "language" to "भाषा",
        "language_subtitle" to "कुछ भाषाओं के लिए अतिरिक्त डाउनलोड की ज़रूरत हो सकती है।",
        "themes" to "थीम्स", "about" to "परिचय", "quick_clean" to "क्विक क्लीन",
        "free_space" to "खाली जगह", "unneeded_files" to "अनावश्यक फ़ाइलें",
        "hidden_caches" to "छिपा कैश", "files_to_review" to "समीक्षा के लिए फ़ाइलें",
        "remove_junk" to "जंक हटाएँ",
        "remove_junk_body" to "अनावश्यक फ़ाइलें साफ़ करके तुरंत जगह बनाएँ।",
        "make_more_room" to "और जगह बनाएँ",
        "make_more_room_body" to "फ़ोटो अनुकूलित करके या क्लाउड में भेजकर स्टोरेज खाली करें।",
        "sleep_mode" to "स्लीप मोड", "tips" to "टिप्स", "media" to "मीडिया",
        "apps" to "ऐप्स", "photos" to "फ़ोटो", "audio" to "ऑडियो", "video" to "वीडियो",
        "others" to "अन्य", "see_tips" to "टिप्स देखें", "sign_in" to "साइन इन",
        "redeem" to "सदस्यता रिडीम करें", "explore_features" to "फ़ीचर्स देखें",
        "analysis_prefs" to "विश्लेषण प्राथमिकताएँ", "notifications" to "सूचनाएँ",
        "realtime" to "रियल-टाइम पहचान", "cloud_services" to "क्लाउड सेवाएँ",
        "personal_privacy" to "गोपनीयता", "deep_clean" to "डीप क्लीन",
        "browser_cleaner" to "ब्राउज़र क्लीनर", "auto_cleaning" to "ऑटो क्लीनिंग",
        "photo_optimizer" to "फ़ोटो ऑप्टिमाइज़र", "video_optimizer" to "वीडियो ऑप्टिमाइज़र",
        "cloud_transfers" to "क्लाउड ट्रांसफ़र", "system_info" to "सिस्टम जानकारी",
        "continue" to "जारी रखें", "cancel" to "रद्द करें", "back" to "वापस",
        "scanning" to "स्कैन हो रहा है…", "customize" to "कस्टमाइज़",
        "free_up_to" to "%s तक खाली करें", "with_premium" to "प्रीमियम के साथ आपको मिलेगा",
        "sign_out" to "साइन आउट", "licenses" to "ओपन सोर्स लाइसेंस",
        "selected" to "चयनित", "done" to "हो गया", "allow" to "अनुमति दें",
        "not_now" to "अभी नहीं", "continue_with_ads" to "विज्ञापनों के साथ जारी रखें",
        "privacy_policy" to "गोपनीयता नीति", "space_freed" to "खाली की गई जगह"
    ),
    "zh-CN" to mapOf(
        "home" to "首页", "tools" to "工具", "storage" to "存储",
        "account" to "账户", "upgrade" to "升级", "settings" to "设置",
        "language" to "语言",
        "language_subtitle" to "某些语言需要额外下载，可能需要一些时间。",
        "themes" to "主题", "about" to "关于", "quick_clean" to "快速清理",
        "free_space" to "可用空间", "unneeded_files" to "不需要的文件",
        "hidden_caches" to "隐藏缓存", "files_to_review" to "待查文件",
        "remove_junk" to "清理垃圾",
        "remove_junk_body" to "清理不需要的文件，立即释放空间。",
        "make_more_room" to "腾出更多空间",
        "make_more_room_body" to "通过优化图片或上传到云端来释放存储空间。",
        "sleep_mode" to "睡眠模式", "tips" to "提示", "media" to "媒体",
        "apps" to "应用", "photos" to "照片", "audio" to "音频", "video" to "视频",
        "others" to "其他", "see_tips" to "查看提示", "sign_in" to "登录",
        "redeem" to "兑换订阅", "explore_features" to "探索功能",
        "analysis_prefs" to "分析偏好", "notifications" to "通知",
        "realtime" to "实时检测", "cloud_services" to "云服务",
        "personal_privacy" to "个人隐私", "deep_clean" to "深度清理",
        "browser_cleaner" to "浏览器清理", "auto_cleaning" to "自动清理",
        "photo_optimizer" to "照片优化", "video_optimizer" to "视频优化",
        "cloud_transfers" to "云传输", "system_info" to "系统信息",
        "continue" to "继续", "cancel" to "取消", "back" to "返回",
        "scanning" to "正在扫描…", "customize" to "自定义",
        "free_up_to" to "最多可释放 %s", "with_premium" to "升级高级版后您将拥有",
        "sign_out" to "退出登录", "licenses" to "开源许可",
        "selected" to "已选择", "done" to "完成", "allow" to "允许",
        "not_now" to "暂不", "continue_with_ads" to "继续使用广告版",
        "privacy_policy" to "隐私政策", "space_freed" to "已释放空间"
    ),
    "ja" to mapOf(
        "home" to "ホーム", "tools" to "ツール", "storage" to "ストレージ",
        "account" to "アカウント", "upgrade" to "アップグレード", "settings" to "設定",
        "language" to "言語",
        "language_subtitle" to "一部の言語は追加ダウンロードが必要です。時間がかかる場合があります。",
        "themes" to "テーマ", "about" to "情報", "quick_clean" to "クイッククリーン",
        "free_space" to "空き容量", "unneeded_files" to "不要なファイル",
        "hidden_caches" to "非表示のキャッシュ", "files_to_review" to "確認するファイル",
        "remove_junk" to "ジャンクを削除",
        "remove_junk_body" to "不要なファイルを削除してすぐに空き容量を確保します。",
        "make_more_room" to "さらに容量を確保",
        "make_more_room_body" to "画像の最適化やクラウドへの移動でストレージを空けます。",
        "sleep_mode" to "スリープモード", "tips" to "ヒント", "media" to "メディア",
        "apps" to "アプリ", "photos" to "写真", "audio" to "音声", "video" to "動画",
        "others" to "その他", "see_tips" to "ヒントを見る", "sign_in" to "サインイン",
        "redeem" to "サブスクリプションを利用", "explore_features" to "機能を見る",
        "analysis_prefs" to "分析設定", "notifications" to "通知",
        "realtime" to "リアルタイム検出", "cloud_services" to "クラウドサービス",
        "personal_privacy" to "プライバシー", "deep_clean" to "ディープクリーン",
        "browser_cleaner" to "ブラウザクリーナー", "auto_cleaning" to "自動クリーニング",
        "photo_optimizer" to "写真オプティマイザー", "video_optimizer" to "動画オプティマイザー",
        "cloud_transfers" to "クラウド転送", "system_info" to "システム情報",
        "continue" to "続行", "cancel" to "キャンセル", "back" to "戻る",
        "scanning" to "スキャン中…", "customize" to "カスタマイズ",
        "free_up_to" to "最大 %s を解放", "with_premium" to "プレミアムで利用可能",
        "sign_out" to "サインアウト", "licenses" to "オープンソースライセンス",
        "selected" to "選択中", "done" to "完了", "allow" to "許可",
        "not_now" to "後で", "continue_with_ads" to "広告付きで続行",
        "privacy_policy" to "プライバシーポリシー", "space_freed" to "解放した容量"
    ),
    "ko" to mapOf(
        "home" to "홈", "tools" to "도구", "storage" to "저장공간",
        "account" to "계정", "upgrade" to "업그레이드", "settings" to "설정",
        "language" to "언어",
        "language_subtitle" to "일부 언어는 추가 다운로드가 필요할 수 있습니다.",
        "themes" to "테마", "about" to "정보", "quick_clean" to "빠른 정리",
        "free_space" to "남은 공간", "unneeded_files" to "불필요한 파일",
        "hidden_caches" to "숨겨진 캐시", "files_to_review" to "검토할 파일",
        "remove_junk" to "정크 삭제",
        "remove_junk_body" to "불필요한 파일을 정리해 바로 공간을 확보하세요.",
        "make_more_room" to "공간 더 확보",
        "make_more_room_body" to "사진 최적화 또는 클라우드 이동으로 저장공간을 확보하세요.",
        "sleep_mode" to "절전 모드", "tips" to "팁", "media" to "미디어",
        "apps" to "앱", "photos" to "사진", "audio" to "오디오", "video" to "동영상",
        "others" to "기타", "see_tips" to "팁 보기", "sign_in" to "로그인",
        "redeem" to "구독 사용", "explore_features" to "기능 살펴보기",
        "analysis_prefs" to "분석 환경설정", "notifications" to "알림",
        "realtime" to "실시간 감지", "cloud_services" to "클라우드 서비스",
        "personal_privacy" to "개인정보", "deep_clean" to "딥 클린",
        "browser_cleaner" to "브라우저 정리", "auto_cleaning" to "자동 정리",
        "photo_optimizer" to "사진 최적화", "video_optimizer" to "동영상 최적화",
        "cloud_transfers" to "클라우드 전송", "system_info" to "시스템 정보",
        "continue" to "계속", "cancel" to "취소", "back" to "뒤로",
        "scanning" to "검사 중…", "customize" to "사용자 지정",
        "free_up_to" to "최대 %s 확보", "with_premium" to "프리미엄 이용 시",
        "sign_out" to "로그아웃", "licenses" to "오픈소스 라이선스",
        "selected" to "선택됨", "done" to "완료", "allow" to "허용",
        "not_now" to "나중에", "continue_with_ads" to "광고와 함께 계속",
        "privacy_policy" to "개인정보처리방침", "space_freed" to "확보한 공간"
    ),
    "tr" to mapOf(
        "home" to "Ana sayfa", "tools" to "Araçlar", "storage" to "Depolama",
        "account" to "Hesap", "upgrade" to "YÜKSELT", "settings" to "Ayarlar",
        "language" to "Dil",
        "language_subtitle" to "Bazı diller ek indirme gerektirebilir.",
        "themes" to "Temalar", "about" to "Hakkında", "quick_clean" to "Hızlı Temizlik",
        "free_space" to "Boş alan", "unneeded_files" to "Gereksiz dosyalar",
        "hidden_caches" to "Gizli önbellekler", "files_to_review" to "İncelenecek dosyalar",
        "remove_junk" to "Gereksizleri sil",
        "remove_junk_body" to "Gereksiz dosyaları temizleyerek hemen yer açın.",
        "make_more_room" to "Daha fazla yer aç",
        "make_more_room_body" to "Görselleri optimize ederek veya buluta taşıyarak yer açın.",
        "sleep_mode" to "Uyku Modu", "tips" to "İpuçları", "media" to "Medya",
        "apps" to "Uygulamalar", "photos" to "Fotoğraflar", "audio" to "Ses", "video" to "Video",
        "others" to "Diğer", "see_tips" to "İPUÇLARINI GÖR", "sign_in" to "GİRİŞ YAP",
        "redeem" to "Aboneliği kullan", "explore_features" to "Özellikleri keşfet",
        "analysis_prefs" to "Analiz tercihleri", "notifications" to "Bildirimler",
        "realtime" to "Gerçek zamanlı algılama", "cloud_services" to "Bulut hizmetleri",
        "personal_privacy" to "Gizlilik", "deep_clean" to "Derin Temizlik",
        "browser_cleaner" to "Tarayıcı Temizleyici", "auto_cleaning" to "Otomatik Temizlik",
        "photo_optimizer" to "Fotoğraf Optimizasyonu", "video_optimizer" to "Video Optimizasyonu",
        "cloud_transfers" to "Bulut aktarımları", "system_info" to "Sistem bilgisi",
        "continue" to "DEVAM", "cancel" to "İptal", "back" to "Geri",
        "scanning" to "Taranıyor…", "customize" to "Özelleştir",
        "free_up_to" to "%s'ye kadar boşalt", "with_premium" to "PREMIUM İLE",
        "sign_out" to "Çıkış yap", "licenses" to "Açık kaynak lisansları",
        "selected" to "Seçili", "done" to "Tamam", "allow" to "İZİN VER",
        "not_now" to "ŞİMDİ DEĞİL", "continue_with_ads" to "REKLAMLARLA DEVAM",
        "privacy_policy" to "Gizlilik politikası", "space_freed" to "Boşaltılan alan"
    ),
    "vi" to mapOf(
        "home" to "Trang chủ", "tools" to "Công cụ", "storage" to "Bộ nhớ",
        "account" to "Tài khoản", "upgrade" to "NÂNG CẤP", "settings" to "Cài đặt",
        "language" to "Ngôn ngữ",
        "language_subtitle" to "Một số ngôn ngữ cần tải thêm. Có thể mất chút thời gian.",
        "themes" to "Chủ đề", "about" to "Giới thiệu", "quick_clean" to "Dọn nhanh",
        "free_space" to "Dung lượng trống", "unneeded_files" to "Tệp không cần thiết",
        "hidden_caches" to "Bộ nhớ đệm ẩn", "files_to_review" to "Tệp cần xem lại",
        "remove_junk" to "Xóa rác",
        "remove_junk_body" to "Dọn tệp không cần thiết để giải phóng dung lượng ngay.",
        "make_more_room" to "Tạo thêm chỗ trống",
        "make_more_room_body" to "Giải phóng bộ nhớ bằng cách tối ưu ảnh hoặc đưa lên đám mây.",
        "sleep_mode" to "Chế độ ngủ", "tips" to "Mẹo", "media" to "Đa phương tiện",
        "apps" to "Ứng dụng", "photos" to "Ảnh", "audio" to "Âm thanh", "video" to "Video",
        "others" to "Khác", "see_tips" to "XEM MẸO", "sign_in" to "ĐĂNG NHẬP",
        "redeem" to "Đổi thuê bao", "explore_features" to "Khám phá tính năng",
        "analysis_prefs" to "Tùy chọn phân tích", "notifications" to "Thông báo",
        "realtime" to "Phát hiện thời gian thực", "cloud_services" to "Dịch vụ đám mây",
        "personal_privacy" to "Quyền riêng tư", "deep_clean" to "Dọn sâu",
        "browser_cleaner" to "Dọn trình duyệt", "auto_cleaning" to "Tự động dọn",
        "photo_optimizer" to "Tối ưu ảnh", "video_optimizer" to "Tối ưu video",
        "cloud_transfers" to "Chuyển lên đám mây", "system_info" to "Thông tin hệ thống",
        "continue" to "TIẾP TỤC", "cancel" to "Hủy", "back" to "Quay lại",
        "scanning" to "Đang quét…", "customize" to "Tùy chỉnh",
        "free_up_to" to "Giải phóng tới %s", "with_premium" to "VỚI PREMIUM BẠN SẼ CÓ",
        "sign_out" to "Đăng xuất", "licenses" to "Giấy phép mã nguồn mở",
        "selected" to "Đã chọn", "done" to "Xong", "allow" to "CHO PHÉP",
        "not_now" to "ĐỂ SAU", "continue_with_ads" to "TIẾP TỤC VỚI QUẢNG CÁO",
        "privacy_policy" to "Chính sách quyền riêng tư", "space_freed" to "Dung lượng đã giải phóng"
    ),
    "id" to mapOf(
        "home" to "Beranda", "tools" to "Alat", "storage" to "Penyimpanan",
        "account" to "Akun", "upgrade" to "UPGRADE", "settings" to "Pengaturan",
        "language" to "Bahasa",
        "language_subtitle" to "Beberapa bahasa memerlukan unduhan tambahan.",
        "themes" to "Tema", "about" to "Tentang", "quick_clean" to "Pembersihan Cepat",
        "free_space" to "Ruang kosong", "unneeded_files" to "File tidak diperlukan",
        "hidden_caches" to "Cache tersembunyi", "files_to_review" to "File untuk ditinjau",
        "remove_junk" to "Hapus sampah",
        "remove_junk_body" to "Bersihkan file yang tidak diperlukan untuk segera mengosongkan ruang.",
        "make_more_room" to "Buat lebih banyak ruang",
        "make_more_room_body" to "Kosongkan penyimpanan dengan mengoptimalkan gambar atau mengunggah ke cloud.",
        "sleep_mode" to "Mode tidur", "tips" to "Tips", "media" to "Media",
        "apps" to "Aplikasi", "photos" to "Foto", "audio" to "Audio", "video" to "Video",
        "others" to "Lainnya", "see_tips" to "LIHAT TIPS", "sign_in" to "MASUK",
        "redeem" to "Tukarkan langganan", "explore_features" to "Jelajahi fitur",
        "analysis_prefs" to "Preferensi analisis", "notifications" to "Notifikasi",
        "realtime" to "Deteksi real-time", "cloud_services" to "Layanan cloud",
        "personal_privacy" to "Privasi", "deep_clean" to "Pembersihan Mendalam",
        "browser_cleaner" to "Pembersih Browser", "auto_cleaning" to "Pembersihan Otomatis",
        "photo_optimizer" to "Pengoptimal Foto", "video_optimizer" to "Pengoptimal Video",
        "cloud_transfers" to "Transfer cloud", "system_info" to "Info sistem",
        "continue" to "LANJUT", "cancel" to "Batal", "back" to "Kembali",
        "scanning" to "Memindai…", "customize" to "Sesuaikan",
        "free_up_to" to "Kosongkan hingga %s", "with_premium" to "DENGAN PREMIUM ANDA MENDAPAT",
        "sign_out" to "Keluar", "licenses" to "Lisensi open source",
        "selected" to "Dipilih", "done" to "Selesai", "allow" to "IZINKAN",
        "not_now" to "NANTI SAJA", "continue_with_ads" to "LANJUT DENGAN IKLAN",
        "privacy_policy" to "Kebijakan privasi", "space_freed" to "Ruang dibebaskan"
    ),
    "nl" to mapOf(
        "home" to "Home", "tools" to "Tools", "storage" to "Opslag",
        "account" to "Account", "upgrade" to "UPGRADEN", "settings" to "Instellingen",
        "language" to "Taal",
        "language_subtitle" to "Sommige talen vereisen een extra download.",
        "themes" to "Thema's", "about" to "Over", "quick_clean" to "Snelle opschoning",
        "free_space" to "Vrije ruimte", "unneeded_files" to "Onnodige bestanden",
        "hidden_caches" to "Verborgen caches", "files_to_review" to "Te controleren bestanden",
        "remove_junk" to "Rommel verwijderen",
        "remove_junk_body" to "Verwijder onnodige bestanden en maak meteen ruimte vrij.",
        "make_more_room" to "Meer ruimte maken",
        "make_more_room_body" to "Maak ruimte vrij door afbeeldingen te optimaliseren of naar de cloud te verplaatsen.",
        "sleep_mode" to "Slaapstand", "tips" to "Tips", "media" to "Media",
        "apps" to "Apps", "photos" to "Foto's", "audio" to "Audio", "video" to "Video",
        "others" to "Overig", "see_tips" to "TIPS BEKIJKEN", "sign_in" to "INLOGGEN",
        "redeem" to "Abonnement inwisselen", "explore_features" to "Functies verkennen",
        "analysis_prefs" to "Analysevoorkeuren", "notifications" to "Meldingen",
        "realtime" to "Realtime detectie", "cloud_services" to "Cloudservices",
        "personal_privacy" to "Privacy", "deep_clean" to "Diepe reiniging",
        "browser_cleaner" to "Browseropschoner", "auto_cleaning" to "Automatisch opschonen",
        "photo_optimizer" to "Foto-optimalisatie", "video_optimizer" to "Video-optimalisatie",
        "cloud_transfers" to "Cloudtransfers", "system_info" to "Systeeminformatie",
        "continue" to "DOORGAAN", "cancel" to "Annuleren", "back" to "Terug",
        "scanning" to "Scannen…", "customize" to "Aanpassen",
        "free_up_to" to "Maak tot %s vrij", "with_premium" to "MET PREMIUM KRIJG JE",
        "sign_out" to "Uitloggen", "licenses" to "Open-sourcelicenties",
        "selected" to "Geselecteerd", "done" to "Klaar", "allow" to "TOESTAAN",
        "not_now" to "NU NIET", "continue_with_ads" to "DOORGAAN MET ADS",
        "privacy_policy" to "Privacybeleid", "space_freed" to "Vrijgemaakte ruimte"
    ),
    "pl" to mapOf(
        "home" to "Start", "tools" to "Narzędzia", "storage" to "Pamięć",
        "account" to "Konto", "upgrade" to "ULEPSZ", "settings" to "Ustawienia",
        "language" to "Język",
        "language_subtitle" to "Niektóre języki wymagają dodatkowego pobrania.",
        "themes" to "Motywy", "about" to "Informacje", "quick_clean" to "Szybkie czyszczenie",
        "free_space" to "Wolne miejsce", "unneeded_files" to "Niepotrzebne pliki",
        "hidden_caches" to "Ukryte pamięci podręczne", "files_to_review" to "Pliki do sprawdzenia",
        "remove_junk" to "Usuń śmieci",
        "remove_junk_body" to "Usuń niepotrzebne pliki i od razu zwolnij miejsce.",
        "make_more_room" to "Zwolnij więcej miejsca",
        "make_more_room_body" to "Zwolnij pamięć, optymalizując zdjęcia lub przenosząc je do chmury.",
        "sleep_mode" to "Tryb uśpienia", "tips" to "Wskazówki", "media" to "Multimedia",
        "apps" to "Aplikacje", "photos" to "Zdjęcia", "audio" to "Audio", "video" to "Wideo",
        "others" to "Inne", "see_tips" to "ZOBACZ WSKAZÓWKI", "sign_in" to "ZALOGUJ",
        "redeem" to "Zrealizuj subskrypcję", "explore_features" to "Poznaj funkcje",
        "analysis_prefs" to "Preferencje analizy", "notifications" to "Powiadomienia",
        "realtime" to "Wykrywanie w czasie rzeczywistym", "cloud_services" to "Usługi w chmurze",
        "personal_privacy" to "Prywatność", "deep_clean" to "Głębokie czyszczenie",
        "browser_cleaner" to "Czyszczenie przeglądarki", "auto_cleaning" to "Automatyczne czyszczenie",
        "photo_optimizer" to "Optymalizacja zdjęć", "video_optimizer" to "Optymalizacja wideo",
        "cloud_transfers" to "Transfery do chmury", "system_info" to "Informacje o systemie",
        "continue" to "KONTYNUUJ", "cancel" to "Anuluj", "back" to "Wstecz",
        "scanning" to "Skanowanie…", "customize" to "Dostosuj",
        "free_up_to" to "Zwolnij do %s", "with_premium" to "Z PREMIUM OTRZYMASZ",
        "sign_out" to "Wyloguj", "licenses" to "Licencje open source",
        "selected" to "Wybrane", "done" to "Gotowe", "allow" to "ZEZWÓL",
        "not_now" to "NIE TERAZ", "continue_with_ads" to "KONTYNUUJ Z REKLAMAMI",
        "privacy_policy" to "Polityka prywatności", "space_freed" to "Zwolnione miejsce"
    ),
    "uk" to mapOf(
        "home" to "Головна", "tools" to "Інструменти", "storage" to "Сховище",
        "account" to "Обліковий запис", "upgrade" to "ПРЕМІУМ", "settings" to "Налаштування",
        "language" to "Мова",
        "language_subtitle" to "Деякі мови потребують додаткового завантаження.",
        "themes" to "Теми", "about" to "Про програму", "quick_clean" to "Швидке очищення",
        "free_space" to "Вільне місце", "unneeded_files" to "Непотрібні файли",
        "hidden_caches" to "Прихований кеш", "files_to_review" to "Файли для перевірки",
        "remove_junk" to "Видалити сміття",
        "remove_junk_body" to "Очистіть непотрібні файли й одразу звільніть місце.",
        "make_more_room" to "Звільнити більше місця",
        "make_more_room_body" to "Звільніть пам’ять, оптимізуючи фото або переносячи їх у хмару.",
        "sleep_mode" to "Сплячий режим", "tips" to "Поради", "media" to "Медіа",
        "apps" to "Програми", "photos" to "Фото", "audio" to "Аудіо", "video" to "Відео",
        "others" to "Інше", "see_tips" to "ПОРАДИ", "sign_in" to "УВІЙТИ",
        "redeem" to "Активувати підписку", "explore_features" to "Огляд функцій",
        "analysis_prefs" to "Параметри аналізу", "notifications" to "Сповіщення",
        "realtime" to "Виявлення в реальному часі", "cloud_services" to "Хмарні служби",
        "personal_privacy" to "Конфіденційність", "deep_clean" to "Глибоке очищення",
        "browser_cleaner" to "Очищення браузера", "auto_cleaning" to "Автоочищення",
        "photo_optimizer" to "Оптимізація фото", "video_optimizer" to "Оптимізація відео",
        "cloud_transfers" to "Хмарні перенесення", "system_info" to "Про систему",
        "continue" to "ПРОДОВЖИТИ", "cancel" to "Скасувати", "back" to "Назад",
        "scanning" to "Сканування…", "customize" to "Налаштувати",
        "free_up_to" to "Звільнити до %s", "with_premium" to "З PREMIUM ВИ ОТРИМАЄТЕ",
        "sign_out" to "Вийти", "licenses" to "Ліцензії з відкритим кодом",
        "selected" to "Вибрано", "done" to "Готово", "allow" to "ДОЗВОЛИТИ",
        "not_now" to "НЕ ЗАРАЗ", "continue_with_ads" to "ПРОДОВЖИТИ З РЕКЛАМОЮ",
        "privacy_policy" to "Політика конфіденційності", "space_freed" to "Звільнено"
    ),
    "th" to mapOf(
        "home" to "หน้าแรก", "tools" to "เครื่องมือ", "storage" to "ที่เก็บข้อมูล",
        "account" to "บัญชี", "upgrade" to "อัปเกรด", "settings" to "การตั้งค่า",
        "language" to "ภาษา",
        "language_subtitle" to "บางภาษาต้องดาวน์โหลดเพิ่ม อาจใช้เวลาสักครู่",
        "themes" to "ธีม", "about" to "เกี่ยวกับ", "quick_clean" to "ทำความสะอาดด่วน",
        "free_space" to "พื้นที่ว่าง", "unneeded_files" to "ไฟล์ที่ไม่จำเป็น",
        "hidden_caches" to "แคชที่ซ่อนอยู่", "files_to_review" to "ไฟล์ที่ต้องตรวจสอบ",
        "remove_junk" to "ลบขยะ",
        "remove_junk_body" to "ล้างไฟล์ที่ไม่จำเป็นเพื่อเพิ่มพื้นที่ทันที",
        "make_more_room" to "เพิ่มพื้นที่ว่าง",
        "make_more_room_body" to "เพิ่มพื้นที่ด้วยการปรับภาพหรือย้ายไปยังคลาวด์",
        "sleep_mode" to "โหมดพักเครื่อง", "tips" to "เคล็ดลับ", "media" to "สื่อ",
        "apps" to "แอป", "photos" to "รูปภาพ", "audio" to "เสียง", "video" to "วิดีโอ",
        "others" to "อื่นๆ", "see_tips" to "ดูเคล็ดลับ", "sign_in" to "ลงชื่อเข้าใช้",
        "redeem" to "แลกสมัครสมาชิก", "explore_features" to "สำรวจฟีเจอร์",
        "analysis_prefs" to "การตั้งค่าการวิเคราะห์", "notifications" to "การแจ้งเตือน",
        "realtime" to "ตรวจจับแบบเรียลไทม์", "cloud_services" to "บริการคลาวด์",
        "personal_privacy" to "ความเป็นส่วนตัว", "deep_clean" to "ทำความสะอาดเชิงลึก",
        "browser_cleaner" to "ทำความสะอาดเบราว์เซอร์", "auto_cleaning" to "ทำความสะอาดอัตโนมัติ",
        "photo_optimizer" to "ปรับแต่งรูปภาพ", "video_optimizer" to "ปรับแต่งวิดีโอ",
        "cloud_transfers" to "โอนไปคลาวด์", "system_info" to "ข้อมูลระบบ",
        "continue" to "ดำเนินการต่อ", "cancel" to "ยกเลิก", "back" to "กลับ",
        "scanning" to "กำลังสแกน…", "customize" to "ปรับแต่ง",
        "free_up_to" to "เพิ่มพื้นที่ได้สูงสุด %s", "with_premium" to "ด้วยพรีเมียมคุณจะได้",
        "sign_out" to "ออกจากระบบ", "licenses" to "สัญญาอนุญาตโอเพนซอร์ส",
        "selected" to "เลือกแล้ว", "done" to "เสร็จสิ้น", "allow" to "อนุญาต",
        "not_now" to "ไว้ทีหลัง", "continue_with_ads" to "ดำเนินการต่อพร้อมโฆษณา",
        "privacy_policy" to "นโยบายความเป็นส่วนตัว", "space_freed" to "พื้นที่ที่เพิ่มได้"
    )
)

private fun lookupTable(tag: String): Map<String, String> {
    tables[tag]?.let { return it }
    return when (tag) {
        "pt-PT" -> tables.getValue("pt-BR") + mapOf(
            "settings" to "Definições",
            "about" to "Acerca de",
            "unneeded_files" to "Ficheiros desnecessários",
            "media" to "Multimédia",
            "sign_in" to "INICIAR SESSÃO"
        )
        "ca" -> es + mapOf("home" to "Inici", "settings" to "Configuració", "language" to "Idioma")
        "da" -> de + mapOf(
            "home" to "Hjem", "settings" to "Indstillinger", "language" to "Sprog",
            "tools" to "Værktøjer", "storage" to "Lager", "account" to "Konto",
            "quick_clean" to "Hurtig rengøring", "upgrade" to "OPGRADER"
        )
        "nb" -> de + mapOf(
            "home" to "Hjem",
            "settings" to "Innstillinger",
            "language" to "Språk",
            "tools" to "Verktøy",
            "storage" to "Lagring",
            "account" to "Konto",
            "quick_clean" to "Hurtigrengjøring",
            "upgrade" to "OPPGRADER"
        )
        "sv" -> de + mapOf(
            "home" to "Hem",
            "settings" to "Inställningar",
            "language" to "Språk",
            "tools" to "Verktyg",
            "storage" to "Lagring",
            "account" to "Konto",
            "quick_clean" to "Snabbstädning",
            "upgrade" to "UPPGRADERA"
        )
        "fi" -> mapOf(
            "home" to "Koti", "settings" to "Asetukset", "language" to "Kieli",
            "tools" to "Työkalut", "storage" to "Tallennustila", "account" to "Tili",
            "quick_clean" to "Pikapuhdistus", "upgrade" to "PÄIVITÄ",
            "free_space" to "Vapaa tila", "apps" to "Sovellukset", "photos" to "Kuvat",
            "themes" to "Teemat", "about" to "Tietoja", "tips" to "Vinkit"
        )
        "hu" -> de + mapOf(
            "home" to "Kezdőlap", "settings" to "Beállítások", "language" to "Nyelv",
            "tools" to "Eszközök", "storage" to "Tárhely", "account" to "Fiók",
            "quick_clean" to "Gyors tisztítás", "upgrade" to "FRISSÍTÉS"
        )
        "ro" -> es + mapOf(
            "home" to "Acasă", "settings" to "Setări", "language" to "Limbă",
            "tools" to "Instrumente", "storage" to "Stocare", "account" to "Cont",
            "quick_clean" to "Curățare rapidă", "upgrade" to "ACTUALIZEAZĂ"
        )
        "sk" -> tables.getValue("pl") + mapOf(
            "home" to "Domov",
            "settings" to "Nastavenia",
            "language" to "Jazyk",
            "tools" to "Nástroje",
            "storage" to "Úložisko",
            "account" to "Účet",
            "quick_clean" to "Rýchle čistenie"
        )
        "cs" -> tables.getValue("pl") + mapOf(
            "home" to "Domů",
            "settings" to "Nastavení",
            "language" to "Jazyk",
            "tools" to "Nástroje",
            "storage" to "Úložiště",
            "account" to "Účet",
            "quick_clean" to "Rychlé čištění"
        )
        "el" -> mapOf(
            "home" to "Αρχική", "settings" to "Ρυθμίσεις", "language" to "Γλώσσα",
            "tools" to "Εργαλεία", "storage" to "Αποθήκευση", "account" to "Λογαριασμός",
            "quick_clean" to "Γρήγορος καθαρισμός", "upgrade" to "ΑΝΑΒΑΘΜΙΣΗ",
            "free_space" to "Ελεύθερος χώρος", "apps" to "Εφαρμογές", "photos" to "Φωτογραφίες",
            "themes" to "Θέματα", "about" to "Πληροφορίες", "tips" to "Συμβουλές"
        )
        "bg" -> ru + mapOf(
            "home" to "Начало", "settings" to "Настройки", "language" to "Език",
            "tools" to "Инструменти", "storage" to "Хранилище", "account" to "Акаунт",
            "quick_clean" to "Бързо почистване"
        )
        else -> emptyMap()
    }
}
