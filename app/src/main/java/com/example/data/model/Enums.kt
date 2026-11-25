package com.example.data.model

enum class UserRole(val titleFa: String) {
    LEAD_LAWYER("وکیل اصلی (مدیر دفتر)"),
    ASSOCIATE("وکیل همکار / کارمند دفتر")
}

enum class AccessLevel(val titleFa: String, val descriptionFa: String) {
    VIEW_ONLY("فقط مشاهده", "مشاهده اطلاعات، مدارک و مراحل پرونده بدون امکان تغییر"),
    VIEW_AND_UPLOAD("مشاهده و بارگذاری مدرک", "مشاهده پرونده و افزودن مدارک جدید"),
    REGISTER_STAGES("ثبت مراحل پرونده", "مشاهده، بارگذاری مدرک و ثبت/ویرایش مراحل رسیدگی"),
    FULL_CASE_ACCESS("دسترسی کامل به پرونده", "ویرایش اطلاعات پرونده، مدارک و مراحل رسیدگی")
}

enum class CaseType(val titleFa: String) {
    CIVIL("حقوقی"),
    CRIMINAL("کیفری"),
    FAMILY("خانواده"),
    ENFORCEMENT("اجرای احکام"),
    ADMINISTRATIVE("اداری"),
    REGISTRATION("ثبتی"),
    CIVIL_STATUS("ثبت احوال"),
    MUNICIPAL("شهرداری"),
    ADMIN_JUSTICE_COURT("دیوان عدالت اداری"),
    OTHER("سایر")
}

enum class AuthorityType(val titleFa: String) {
    COURT("دادگاه"),
    PROSECUTOR("دادسرا"),
    DISPUTE_COUNCIL("شورای حل اختلاف"),
    ADMIN_JUSTICE_COURT("دیوان عدالت اداری"),
    DEED_REGISTRY("اداره ثبت اسناد"),
    CIVIL_REGISTRY("ثبت احوال"),
    MUNICIPALITY("شهرداری"),
    ADMIN_COMMISSION("کمیسیون‌های اداری"),
    OTHER("سایر")
}

enum class CaseStatus(val titleFa: String) {
    UNDER_REVIEW("در حال بررسی"),
    IN_PROSECUTOR("در دادسرا"),
    IN_COURT("در دادگاه"),
    APPEAL("در مرحله تجدیدنظر"),
    IN_ENFORCEMENT("در اجرای احکام"),
    CLOSED("بسته‌شده"),
    DISMISSED("منتفی"),
    OTHER("سایر")
}

enum class DocumentCategory(val titleFa: String) {
    PETITION("دادخواست / شکواییه"),
    BRIEF("لایحه"),
    CONTRACT("قرارداد / سند"),
    SUMMONS("ابلاغیه / اخطاریه"),
    RULING("رأی / قرار"),
    IDENTITY("مدارک هویتی"),
    FINANCIAL("مالی / کارشناسی"),
    OTHER("سایر")
}

enum class DocumentFileType(val titleFa: String) {
    PDF("PDF"),
    IMAGE("تصویر"),
    WORD("Word"),
    EXCEL("Excel"),
    OTHER("سایر")
}

enum class StageType(val titleFa: String) {
    FILING("ثبت دادخواست یا شکواییه"),
    REFERRAL("ارجاع به دادسرا یا دادگاه"),
    HEARING_SCHEDULED("تعیین وقت رسیدگی"),
    SUMMONS_SENT("ارسال ابلاغیه"),
    BRIEF_FILED("ثبت لایحه"),
    HEARING_HELD("جلسه رسیدگی"),
    ORDER_OR_VERDICT("صدور قرار یا رأی"),
    APPEAL("تجدیدنظرخواهی"),
    ENFORCEMENT("اجرای حکم"),
    CLOSED("بسته‌شدن پرونده")
}

enum class StageStatus(val titleFa: String) {
    COMPLETED("انجام‌شده"),
    IN_PROGRESS("در حال پیگیری"),
    PENDING_ACTION("در انتظار اقدام"),
    CANCELLED("لغو شده")
}

enum class BriefType(val titleFa: String) {
    CIVIL_PETITION("دادخواست"),
    DEFENSE_BRIEF("لایحه دفاعیه"),
    CRIMINAL_COMPLAINT("شکواییه"),
    REQUEST("درخواست"),
    OFFICIAL_NOTICE("اظهارنامه"),
    POWER_OF_ATTORNEY("وکالتنامه و حق‌الوکاله"),
    ADMIN_LETTER("مکاتبات اداری")
}

enum class LawCategory(val titleFa: String) {
    CIVIL("قانون مدنی"),
    CIVIL_PROCEDURE("آیین دادرسی مدنی"),
    PENAL("مجازات اسلامی"),
    CRIMINAL_PROCEDURE("آیین دادرسی کیفری"),
    COMMERCIAL("قانون تجارت"),
    FAMILY("حمایت خانواده"),
    LABOR("کار و تأمین اجتماعی"),
    REGISTRATION("ثبت اسناد و املاک"),
    ADMIN_JUSTICE("دیوان عدالت اداری"),
    OTHER("سایر مقررات")
}

enum class RulingAuthority(val titleFa: String) {
    SUPREME_COURT_UNITY("رأی وحدت رویه دیوان عالی کشور"),
    SUPREME_COURT("دیوان عالی کشور"),
    ADMIN_JUSTICE_UNITY("هیأت عمومی دیوان عدالت اداری"),
    APPEAL_COURT("دادگاه تجدیدنظر"),
    FIRST_INSTANCE("دادگاه بدوی / سایر مراجع")
}

enum class LegalRefType {
    LAW,
    RULING
}
