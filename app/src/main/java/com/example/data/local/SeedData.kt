package com.example.data.local

import com.example.data.model.AccessLevel
import com.example.data.model.AuthorityType
import com.example.data.model.BriefEntity
import com.example.data.model.BriefType
import com.example.data.model.CaseAccessEntity
import com.example.data.model.CaseEntity
import com.example.data.model.CaseStageEntity
import com.example.data.model.CaseStatus
import com.example.data.model.CaseType
import com.example.data.model.LawCategory
import com.example.data.model.LawEntity
import com.example.data.model.RulingAuthority
import com.example.data.model.RulingEntity
import com.example.data.model.StageStatus
import com.example.data.model.StageType
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.util.SecurityUtils

object SeedData {

    fun initialUsers(): List<UserEntity> {
        val leadSalt = SecurityUtils.generateSalt()
        val leadAnsSalt = SecurityUtils.generateSalt()
        val assocSalt = SecurityUtils.generateSalt()
        val assocAnsSalt = SecurityUtils.generateSalt()

        return listOf(
            UserEntity(
                fullName = "استاد دکتر امیرحسین رادمنش",
                identifier = "09120000001",
                passwordHash = SecurityUtils.hashSecret("123456", leadSalt),
                passwordSalt = leadSalt,
                role = UserRole.LEAD_LAWYER,
                isActive = true,
                twoFactorEnabled = false,
                securityQuestion = "نام شهر محل تولد شما چیست؟",
                securityAnswerHash = SecurityUtils.hashSecret("تهران", leadAnsSalt),
                securityAnswerSalt = leadAnsSalt,
                barLicenseNumber = "پروانه وکالت پایه یک شماره ۲۴۸۱۰"
            ),
            UserEntity(
                fullName = "سارا فرهادی (وکیل همکار)",
                identifier = "09120000002",
                passwordHash = SecurityUtils.hashSecret("123456", assocSalt),
                passwordSalt = assocSalt,
                role = UserRole.ASSOCIATE,
                isActive = true,
                twoFactorEnabled = false,
                securityQuestion = "نام شهر محل تولد شما چیست؟",
                securityAnswerHash = SecurityUtils.hashSecret("شیراز", assocAnsSalt),
                securityAnswerSalt = assocAnsSalt,
                barLicenseNumber = "پروانه کارآموزی / وکالت ۳۱۵۰۲"
            )
        )
    }

    fun initialCases(leadUserId: Long): List<CaseEntity> {
        val now = System.currentTimeMillis()
        val day = 24L * 60L * 60L * 1000L
        return listOf(
            CaseEntity(
                officeCaseNumber = "۱۴۰۵/ح/۱۰۱",
                courtCaseNumber = "۰۵۰۹۹۸۳۴۱۲۷۰۰۰۱۴",
                caseType = CaseType.CIVIL,
                authorityType = AuthorityType.COURT,
                authorityBranch = "شعبه ۱۲ دادگاه عمومی حقوقی تهران",
                plaintiffName = "شرکت ساختمانی افق طلایی",
                defendantName = "آقای بهرام کریمی",
                subject = "الزام به تنظیم سند رسمی و مطالبه وجه التزام قراردادی",
                filingDateMillis = now - 45 * day,
                nextHearingMillis = now + 4 * day,
                opposingCounsel = "آقای مسعود ناصری",
                status = CaseStatus.IN_COURT,
                summary = "خواهان به استناد مبایعه‌نامه عادی مورخ ۱۴۰۳/۰۸/۱۲ تقاضای الزام خوانده به تنظیم سند رسمی انتقال و مطالبه خسارت تأخیر روزانه را مطرح نموده است.",
                encryptedLawyerNotes = SecurityUtils.encryptText("استعلام ثبتی وضعیت پلاک ثبتی ۲۴۱۵ فرعی از ۷۸ اصلی بررسی شود. فیش واریزی ثمن در جلسه آتی ارائه گردد."),
                importantDatesNote = "مهلت ارائه لایحه تکمیلی تا ۲ روز پیش از جلسه",
                reminderEnabled = true,
                reminderMillis = now + 3 * day,
                hasPendingAction = true,
                ownerUserId = leadUserId
            ),
            CaseEntity(
                officeCaseNumber = "۱۴۰۵/ک/۱۰۲",
                courtCaseNumber = "۰۵۰۱۱۲۰۰۴۸۷۰۰۰۸۹",
                caseType = CaseType.CRIMINAL,
                authorityType = AuthorityType.PROSECUTOR,
                authorityBranch = "شعبه ۵ بازپرسی دادسرای عمومی و انقلاب ناحیه ۳",
                plaintiffName = "خانم مریم کاظمی",
                defendantName = "آقای سهراب جمشیدی",
                subject = "تحصیل مال از طریق نامشروع و خیانت در امانت",
                filingDateMillis = now - 20 * day,
                nextHearingMillis = now + 11 * day,
                opposingCounsel = "ندارد",
                status = CaseStatus.IN_PROSECUTOR,
                summary = "شاکی بابت استرداد وجوه امانی و اسناد تجاری تحویلی طرح شکایت نموده و پرونده در مرحله اخذ توضیح از مطلعین است.",
                encryptedLawyerNotes = SecurityUtils.encryptText("صورتجلسه تحویل اسناد امانی و شهادت دو نفر شاهد برای ارائه به بازپرس محترم آماده شده است."),
                importantDatesNote = "پیگیری قرار تأمین و استعلام بانکی",
                reminderEnabled = false,
                reminderMillis = null,
                hasPendingAction = true,
                ownerUserId = leadUserId
            )
        )
    }

    fun initialAccess(caseId: Long, associateId: Long): CaseAccessEntity {
        return CaseAccessEntity(
            caseId = caseId,
            userId = associateId,
            accessLevel = AccessLevel.REGISTER_STAGES,
            isEnabled = true
        )
    }

    fun initialStages(case1Id: Long, case2Id: Long, leadName: String): List<CaseStageEntity> {
        val now = System.currentTimeMillis()
        val day = 24L * 60L * 60L * 1000L
        return listOf(
            CaseStageEntity(
                caseId = case1Id,
                stageType = StageType.FILING,
                title = "ثبت دادخواست بدوی در دفتر خدمات قضایی",
                dateMillis = now - 45 * day,
                description = "دادخواست به همراه تصویر مصدق مبایعه‌نامه و گواهی عدم حضور دفترخانه ثبت شد.",
                responsibleName = leadName,
                status = StageStatus.COMPLETED
            ),
            CaseStageEntity(
                caseId = case1Id,
                stageType = StageType.HEARING_SCHEDULED,
                title = "تعیین وقت رسیدگی و ابلاغ به طرفین",
                dateMillis = now - 15 * day,
                description = "وقت رسیدگی نخستین تعیین و اخطاریه از طریق ثنا ابلاغ گردید.",
                responsibleName = leadName,
                status = StageStatus.COMPLETED
            ),
            CaseStageEntity(
                caseId = case1Id,
                stageType = StageType.BRIEF_FILED,
                title = "تهیه و ثبت لایحه دفاعیه پیش از جلسه",
                dateMillis = now + 2 * day,
                description = "تنظیم لایحه مستدل بر مبنای مواد ۲۲۰ و ۲۳۰ قانون مدنی و ضمیمه‌کردن استعلام ثبتی.",
                responsibleName = leadName,
                status = StageStatus.PENDING_ACTION
            ),
            CaseStageEntity(
                caseId = case2Id,
                stageType = StageType.REFERRAL,
                title = "ارجاع شکواییه به شعبه ۵ بازپرسی",
                dateMillis = now - 18 * day,
                description = "دستور اخذ اظهارات شاکی و استعلام حساب‌های بانکی صادر شد.",
                responsibleName = leadName,
                status = StageStatus.IN_PROGRESS
            )
        )
    }

    fun legalTemplates(): List<BriefEntity> = listOf(
        BriefEntity(
            title = "الگوی دادخواست نخستین حقوقی",
            briefType = BriefType.CIVIL_PETITION,
            isTemplate = true,
            content = """ریاست محترم دادگاه عمومی حقوقی [مرجع رسیدگی]

با سلام و احترام؛
اینجانب به وکالت از خواهان محترم، [نام خواهان]، به استحضار می‌رساند:

۱. حسب مدارک و مستندات پیوست، خوانده محترم [نام خوانده] متعهد به انجام موضوع قرارداد گردیده اما تاکنون از ایفای تعهدات خویش استنکاف نموده است.
۲. علی‌رغم ارسال اظهارنامه رسمی و پیگیری‌های مکرر، اقدام مؤثری از سوی خوانده صورت نگرفته است.

لذا مستنداً به مواد ۱۰، ۲۱۹، ۲۲۰ و ۲۲۱ قانون مدنی و مواد ۱۹۸ و ۵۱۹ قانون آیین دادرسی مدنی، صدور حکم شایسته مبنی بر محکومیت خوانده در خصوص [موضوع] به انضمام کلیه خسارات دادرسی و حق‌الوکاله وکیل مورد استدعاست.

با تجدید احترام
وکیل خواهان"""
        ),
        BriefEntity(
            title = "الگوی لایحه دفاعیه",
            briefType = BriefType.DEFENSE_BRIEF,
            isTemplate = true,
            content = """ریاست محترم [مرجع رسیدگی]
موضوع: لایحه دفاعیه در پرونده کلاسه [شماره پرونده]

با سلام و احترام؛
در خصوص دعوای مطروحه به طرفیت موکل [نام خوانده] از سوی [نام خواهان] به خواسته [موضوع]، دفاعیات ذیل به استحضار آن مقام محترم قضایی می‌رسد:

اولاً: ادعای مطرح‌شده فاقد ادله اثباتی قانونی وفق ماده ۱۲۵۷ قانون مدنی بوده و مستندات ابرازی دلالتی بر استحقاق طرف مقابل ندارد.
ثانیاً: ایرادات شکلی و ماهوی مشروح ذیل بر دادخواست/شکواییه وارد است:
[شرح دفاعیات مستند و مواد قانونی]

بنا به مراتب فوق و نظر به فقدان ارکان قانونی دعوا، رد دعوای خواهان و صدور حکم بر بی‌حقی ایشان مورد تقاضاست.

با احترام
وکیل پرونده"""
        ),
        BriefEntity(
            title = "الگوی شکواییه کیفری",
            briefType = BriefType.CRIMINAL_COMPLAINT,
            isTemplate = true,
            content = """دادستان محترم عمومی و انقلاب [مرجع رسیدگی]

با سلام و احترام؛
اینجانب به وکالت از شاکی، [نام خواهان]، علیه مشتکی‌عنه [نام خوانده] به اتهام [موضوع] شکایت خویش را به شرح ذیل تقدیم می‌دارم:

شرح ماوقع:
در تاریخ ... مشتکی‌عنه با سوءنیت و بر خلاف موازین قانونی اقدام به ... نموده که موجب ورود خسارت مادی و معنوی به موکل گردیده است.

دلایل و مستندات:
۱. تصویر مصدق قرارداد / رسید
۲. استماع شهادت شهود تعرفه‌شده
۳. استعلامات مربوطه

لذا مستنداً به مواد قانون مجازات اسلامی و قانون آیین دادرسی کیفری، تقاضای تعقیب، تحقیق و صدور قرار مجرمیت و کیفرخواست مقتضی را دارم.

با احترام
وکیل شاکی"""
        ),
        BriefEntity(
            title = "الگوی درخواست قضایی (استمهال / مطالعه پرونده)",
            briefType = BriefType.REQUEST,
            isTemplate = true,
            content = """ریاست محترم [مرجع رسیدگی]
عطف به پرونده کلاسه: [شماره پرونده]

با سلام و احترام؛
اینجانب وکیل [نام خواهان] در پرونده موصوف، احتراماً با توجه به [دلیل درخواست: لزوم ارائه مستندات تکمیلی / ضرورت مطالعه اوراق پرونده]، استدعا دارد دستور فرمایید نسبت به [موضوع درخواست] مساعدت و اقدام مقتضی معمول گردد.

پیشاپیش از بذل عنایت آن مقام محترم سپاسگزارم.

با احترام"""
        ),
        BriefEntity(
            title = "الگوی اظهارنامه رسمی",
            briefType = BriefType.OFFICIAL_NOTICE,
            isTemplate = true,
            content = """مخاطب محترم: [نام خوانده]
موضوع: اخطار رسمی جهت ایفای تعهدات قانونی و قراردادی

با سلام؛
نظر به تعهدات ناشی از قرارداد / رابطه حقوقی فی‌مابین در خصوص [موضوع]، بدین‌وسیله به شما ابلاغ می‌گردد ظرف مهلت ۱۰ روز از تاریخ ابلاغ این اظهارنامه، نسبت به انجام کامل تعهدات و رفع نقض اقدام نمایید.

بدیهی است در صورت عدم اقدام در مهلت مقرر، موکل [نام خواهان] ناگزیر به طرح دعوای حقوقی و مطالبه کلیه خسارات قانونی و هزینه دادرسی خواهد بود."""
        ),
        BriefEntity(
            title = "الگوی قرارداد حق‌الوکاله و وکالتنامه داخلی",
            briefType = BriefType.POWER_OF_ATTORNEY,
            isTemplate = true,
            content = """قرارداد حق‌الوکاله و مشاوره حقوقی

موکل: [نام خواهان]
وکیل: دفتر وکالت وکیل‌یار
موضوع وکالت: پیگیری پرونده [موضوع] در [مرجع رسیدگی] (کلاسه داخلی: [شماره پرونده])

ماده ۱- حدود اختیارات وکیل مطابق مفاد وکالتنامه رسمی صادره در مراحل بدوی / تجدیدنظر / فرجام و اجرای احکام است.
ماده ۲- میزان حق‌الوکاله و نحوه پرداخت: ... ریال / تومان طی ... قسط.
ماده ۳- پرداخت هزینه‌های دادرسی، کارشناسی و تمبر مالیاتی بر عهده موکل محترم خواهد بود.

امضای موکل                    امضای وکیل"""
        ),
        BriefEntity(
            title = "الگوی مکاتبات اداری و استعلامات",
            briefType = BriefType.ADMIN_LETTER,
            isTemplate = true,
            content = """بسمه تعالی
به: مدیریت محترم [مرجع رسیدگی]
موضوع: درخواست رسیدگی / ارائه گواهی در خصوص پرونده [شماره پرونده]

با سلام و احترام؛
عطف به پرونده مطروحه به نام [نام خواهان] به موضوع [موضوع]، خواهشمند است دستور فرمایید نتیجه استعلام / گواهی مربوطه جهت ارائه به مرجع محترم قضایی صادر و تحویل اینجانب گردد.

با تشکر و احترام
وکیل دادگستری"""
        )
    )

    fun initialLaws(): List<LawEntity> = listOf(
        LawEntity(
            lawName = "قانون مدنی",
            category = LawCategory.CIVIL,
            articleNumber = "ماده ۱۰",
            tabsera = "ندارد",
            fullText = "قراردادهای خصوصی نسبت به کسانی که آن را منعقد نموده‌اند، در صورتی که مخالف صریح قانون نباشد نافذ است.",
            personalNote = "مبنای اصلی اصل حاکمیت اراده و اعتبار مبایعه‌نامه‌ها و شروط قراردادی."
        ),
        LawEntity(
            lawName = "قانون مدنی",
            category = LawCategory.CIVIL,
            articleNumber = "ماده ۲۱۹ و ۲۲۰",
            tabsera = "ندارد",
            fullText = "عقودی که بر طبق قانون واقع شده باشد بین متعاملین و قائم‌مقام آن‌ها لازم‌الاتباع است (ماده ۲۱۹). عقود نه فقط متعاملین را به اجرای چیزی که در آن تصریح شده است ملزم می‌نماید، بلکه متعاملین به کلیه نتایجی هم که به موجب عرف و عادت یا به موجب قانون از عقد حاصل می‌شود ملزم می‌باشند (ماده ۲۲۰).",
            personalNote = "استناد کلیدی در دعاوی الزام به تنظیم سند رسمی و خسارات قراردادی."
        ),
        LawEntity(
            lawName = "قانون آیین دادرسی مدنی",
            category = LawCategory.CIVIL_PROCEDURE,
            articleNumber = "ماده ۵۱۹",
            tabsera = "تبصره ۱ و ۲",
            fullText = "خسارت دادرسی عبارت است از هزینه دادرسی و حق‌الوکاله وکیل و هزینه‌های دیگری که به طور مستقیم مربوط به دادرسی و برای اثبات دعوا یا دفاع لازم بوده است از قبیل حق‌الزحمه کارشناسی و هزینه تحقیقات محلی.",
            personalNote = "همیشه در ستون خواسته به صراحت مطالبه شود."
        ),
        LawEntity(
            lawName = "قانون آیین دادرسی مدنی",
            category = LawCategory.CIVIL_PROCEDURE,
            articleNumber = "ماده ۵۲۲",
            tabsera = "ندارد",
            fullText = "در دعاویی که موضوع آن دِین و از نوع وجه رایج بوده و با مطالبه داین و تمکن مدیون، مدیون امتناع از پرداخت نموده، در صورت تغییر فاحش شاخص قیمت سالانه از زمان سررسید تا هنگام پرداخت و پس از مطالبه طلبکار، دادگاه با رعایت تناسب تغییر شاخص سالانه که توسط بانک مرکزی جمهوری اسلامی ایران تعیین می‌گردد محاسبه و مورد حکم قرار خواهد داد مگر اینکه طرفین به نحو دیگری مصالحه نمایند.",
            personalNote = "تاریخ سررسید و اثبات مطالبه (اظهارنامه یا دادخواست) بسیار تعیین‌کننده است."
        ),
        LawEntity(
            lawName = "قانون آیین دادرسی کیفری",
            category = LawCategory.CRIMINAL_PROCEDURE,
            articleNumber = "ماده ۴۸",
            tabsera = "تبصره",
            fullText = "با شروع تحت نظر قرار گرفتن، متهم می‌تواند تقاضای حضور وکیل نماید. وکیل باید با رعایت و توجه به محرمانه بودن تحقیقات و مذاکرات، با شخص تحت نظر ملاقات نماید و وکیل می‌تواند در پایان ملاقات با متهم که نباید بیش از یک ساعت باشد ملاحظات کتبی خود را برای درج در پرونده ارائه دهد.",
            personalNote = "تضمین حق دفاع متهم در مرحله تحقیقات مقدماتی."
        ),
        LawEntity(
            lawName = "قانون ثبت اسناد و املاک",
            category = LawCategory.REGISTRATION,
            articleNumber = "ماده ۲۲",
            tabsera = "ندارد",
            fullText = "همین که ملکی مطابق قانون در دفتر املاک به ثبت رسید دولت فقط کسی را که ملک به اسم او ثبت شده و یا کسی که ملک مزبور به او منتقل گردیده و این انتقال نیز در دفتر املاک به ثبت رسیده یا اینکه ملک مزبور از مالک رسمی ارثاً به او رسیده باشد مالک خواهد شناخت.",
            personalNote = "در تعارض اسناد عادی و رسمی و با رعایت قانون الزام به ثبت رسمی معاملات اموال غیرمنقول بررسی شود."
        )
    )

    fun initialRulings(): List<RulingEntity> = listOf(
        RulingEntity(
            title = "محاسبه خسارت تأخیر تأدیه وجه چک",
            rulingNumber = "رأی وحدت رویه شماره ۸۱۲",
            authority = RulingAuthority.SUPREME_COURT_UNITY,
            persianYear = "۱۴۰۰",
            topicTag = "اسناد تجاری / خسارت تأخیر تأدیه",
            keywords = "چک، تأخیر تأدیه، گواهی عدم پرداخت، بانک مرکزی",
            fullText = "مبدأ محاسبه خسارت تأخیر تأدیه در مورد چک، تاریخ چک است نه تاریخ گواهی عدم پرداخت یا مطالبه؛ مشروط بر اینکه دارنده با رعایت مقررات قانونی اقدام کرده باشد.",
            personalNote = "رأی بسیار کاربردی برای محاسبه خسارت چک از تاریخ سررسید مندرج در برگه چک."
        ),
        RulingEntity(
            title = "جبران خسارت کاهش ارزش پول در ثمن معامله باطل (مبیع مستحق‌للغیر)",
            rulingNumber = "رأی وحدت رویه شماره ۷۳۳",
            authority = RulingAuthority.SUPREME_COURT_UNITY,
            persianYear = "۱۳۹۳",
            topicTag = "بیع فاسد / مستحق‌للغیر",
            keywords = "معامله فضولی، ثمن، غرامت، کارشناسی روز",
            fullText = "چنانچه مبیع مستحق‌للغیر درآید و باطل بودن بیع به سبب جهل خریدار به فساد باشد، بایع باید علاوه بر رد ثمن، غرامت وارده بر خریدار ناشی از کاهش ارزش ثمن (بر اساس قیمت روز مبیع یا شاخص تورم حسب رأی تکمیلی ۸۱۱) را جبران نماید.",
            personalNote = "در کنار رأی وحدت رویه ۸۱۱ (ملاک قیمت روز اموال مشابه) در لوایح استفاده شود."
        ),
        RulingEntity(
            title = "نحوه احتساب غرامت مشتری جاهل به فساد بیع (اموال مشابه)",
            rulingNumber = "رأی وحدت رویه شماره ۸۱۱",
            authority = RulingAuthority.SUPREME_COURT_UNITY,
            persianYear = "۱۴۰۰",
            topicTag = "بیع مستحق‌للغیر / کارشناسی",
            keywords = "غرامت، ثمن، ارزش روز، رأی ۷۳۳",
            fullText = "در مواردی که مبیع مستحق‌للغیر درآید و مشتری جاهل به فساد معامله باشد، میزان غرامت بر مبنای قیمت روز مبیع مشابه و هم‌طراز توسط کارشناس رسمی دادگستری تعیین می‌شود.",
            personalNote = "تکمیل‌کننده رأی ۷۳۳ جهت مطالبه قیمت روز ملک."
        ),
        RulingEntity(
            title = "تعیین صلاحیت دادگاه محل وقوع مال غیرمنقول",
            rulingNumber = "رأی وحدت رویه شماره ۶۷۱",
            authority = RulingAuthority.SUPREME_COURT_UNITY,
            persianYear = "۱۳۸۳",
            topicTag = "صلاحیت محلی / اموال غیرمنقول",
            keywords = "صلاحیت، غیرمنقول، الزام به تنظیم سند، ماده ۱۲",
            fullText = "دعاوی راجع به اموال غیرمنقول اعم از دعوای مالکیت و سایر حقوق راجع به آن (از جمله الزام به تنظیم سند رسمی) باید در دادگاهی اقامه شود که مال غیرمنقول در حوزه آن واقع است، اگرچه خوانده در آن حوزه مقیم نباشد.",
            personalNote = "برای پاسخ به ایراد عدم صلاحیت محلی و تنظیم صحیح دادخواست بدوی."
        )
    )
}
