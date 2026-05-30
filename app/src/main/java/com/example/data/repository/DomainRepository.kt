package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.DomainDao
import com.example.data.local.AppraisalDao
import com.example.data.model.Domain
import com.example.data.model.Appraisal
import com.example.data.remote.GeminiRetrofitClient
import com.example.data.remote.GenerateContentRequest
import com.example.data.remote.Content
import com.example.data.remote.Part
import com.example.data.remote.GenerationConfig
import com.example.data.remote.DomainAppraisalResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import android.util.Log

class DomainRepository(
    private val domainDao: DomainDao,
    private val appraisalDao: AppraisalDao
) {
    val allDomains: Flow<List<Domain>> = domainDao.getAllDomains()
    val favoriteDomains: Flow<List<Domain>> = domainDao.getFavoriteDomains()
    val appraisalHistory: Flow<List<Appraisal>> = appraisalDao.getAllAppraisals()

    fun getDomainsByCategory(category: String): Flow<List<Domain>> {
        return if (category == "الكل" || category == "All") {
            domainDao.getAllDomains()
        } else {
            domainDao.getDomainsByCategory(category)
        }
    }

    suspend fun toggleFavorite(domain: Domain) = withContext(Dispatchers.IO) {
        domainDao.updateFavorite(domain.id, !domain.isFavorite)
    }

    suspend fun insertDomain(domain: Domain) = withContext(Dispatchers.IO) {
        domainDao.insertDomain(domain)
    }

    suspend fun deleteDomain(domain: Domain) = withContext(Dispatchers.IO) {
        domainDao.deleteDomain(domain)
    }

    suspend fun clearAppraisalHistory() = withContext(Dispatchers.IO) {
        appraisalDao.clearHistory()
    }

    suspend fun deleteAppraisal(appraisal: Appraisal) = withContext(Dispatchers.IO) {
        appraisalDao.deleteAppraisal(appraisal)
    }

    suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val count = domainDao.getCount()
        if (count == 0) {
            val seedList = listOf(
                // Category: AI & Tech
                Domain(
                    name = "SaudiAI.com",
                    price = 8500.0,
                    category = "ذكاء اصطناعي",
                    description = "اسم نطاق متميز يربط مشاريع الذكاء الاصطناعي في المملكة العربية السعودية بملكية استثنائية.",
                    isPremium = true
                ),
                Domain(
                    name = "DeepArab.ai",
                    price = 4900.0,
                    category = "ذكاء اصطناعي",
                    description = "مثالي لشركات التكنولوجيا العميقة ونماذج اللغة الكبيرة الموجهة للسوق العربي.",
                    isPremium = true
                ),
                Domain(
                    name = "GulfDev.com",
                    price = 3200.0,
                    category = "ذكاء اصطناعي",
                    description = "دومين ممتاز لشركات البرمجة وتطوير الأنظمة الداعمة للرؤية الخليجية.",
                    isPremium = false
                ),

                // Category: Finance & Money
                Domain(
                    name = "Dirham.app",
                    price = 9800.0,
                    category = "مالية واستثمار",
                    description = "علامة مالية رقمية للمحافظ والاتحاد المالي وتطبيقات الدفع المبتكرة بدبي والخليج.",
                    isPremium = true
                ),
                Domain(
                    name = "Mal.net",
                    price = 12500.0,
                    category = "مالية واستثمار",
                    description = "اسم نطاق مميز من ثلاثة حروف يعني المال، قوة تسويقية لا تضاهى لقطاع البنوك والاستثمار.",
                    isPremium = true
                ),
                Domain(
                    name = "Sahm.finance",
                    price = 3800.0,
                    category = "مالية واستثمار",
                    description = "دومين مثالي لمنصات تداول الأسهم، الاستشارات، والصناديق الاستثمارية في المنطقة.",
                    isPremium = false
                ),

                // Category: Arabic Brands
                Domain(
                    name = "Matjar.shop",
                    price = 2400.0,
                    category = "أسماء عربية",
                    description = "دومين يعبر مباشرة عن المتجر الإلكتروني بلاحقة تسوق حديثة؛ جاذبية تسويقية فورية.",
                    isPremium = true
                ),
                Domain(
                    name = "Raqami.net",
                    price = 1950.0,
                    category = "أسماء عربية",
                    description = "علامة تجارية معبرة تشير إلى الرقمنة والتحول الرقمي للشركات الناشئة.",
                    isPremium = false
                ),
                Domain(
                    name = "Tasweeq.ly",
                    price = 2700.0,
                    category = "أسماء عربية",
                    description = "الخيار الأمثل للوكالات الإعلانية وشركات التسويق الطموحة بليبيا.",
                    isPremium = true
                ),

                // Category: SaaS & Apps
                Domain(
                    name = "SaudiSaaS.com",
                    price = 5600.0,
                    category = "تطبيقات وبرمجيات",
                    description = "الاسم الرائد لمنصات البرمجيات كخدمة (SaaS) المستهدفة لرواد الأعمال بالمملكة العربية السعودية.",
                    isPremium = true
                ),
                Domain(
                    name = "Sari.app",
                    price = 6200.0,
                    category = "تطبيقات وبرمجيات",
                    description = "اسم مكون من 4 حروف فقط يعبر عن السرعة وسريان الخدمات اللوجستية وتطبيقات التوصيل.",
                    isPremium = true
                ),
                Domain(
                    name = "Fatora.io",
                    price = 2900.0,
                    category = "تطبيقات وبرمجيات",
                    description = "اسم مميز وجذاب لتطبيقات الفوترة الرقمية وحلول نقاط البيع السحابية.",
                    isPremium = false
                ),

                // Category: Short Domains
                Domain(
                    name = "Aqat.com",
                    price = 14000.0,
                    category = "مواقع وعقارات",
                    description = "نطاق رباعي نادر (عقارات) فائق القوة، مناسب لبوابة عقارية فاخرة أو سوق تداول عقاري.",
                    isPremium = true
                ),
                Domain(
                    name = "Riyadh.app",
                    price = 18500.0,
                    category = "مواقع وعقارات",
                    description = "الاسم الرسمي للعاصمة الرياض برمز التطبيقات؛ فرصة نادرة لأدلة السياحة والخدمات البلدية والذكية.",
                    isPremium = true
                ),
                Domain(
                    name = "Rizq.org",
                    price = 3300.0,
                    category = "مواقع وعقارات",
                    description = "دومين يحمل معنى الرزق والعمل الخيري، ملائم للمنصات التنموية أو التمويل الجماعي.",
                    isPremium = false
                )
            )
            domainDao.insertAll(seedList)
        }
    }

    suspend fun evaluateDomainWithGemini(domainName: String): Result<Appraisal> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("يرجى تكوين مفتاح Gemini API في لوحة الأسرار (Secrets Panel) لتشغيل تقييم الدومينات"))
        }

        val cleanedName = domainName.trim().lowercase()
        val prompt = """
            Evaluate the domain name: "$cleanedName" from the perspective of an expert domain appraiser and valuation expert (like GoDaddy, Atom, or Squadhelp).
            Analyze length, TLD, keywords, pronounceability, suitability for startups, and general commercial value.
            Return a JSON object in ARABIC containing the evaluation. Keep the JSON keys strictly in English as defined below. Do not use markdown backticks or enclose the JSON with ```json, output only the raw JSON.
            
            JSON format:
            {
              "domainName": "$cleanedName",
              "estimatedValueRange": "[Write estimated range here, e.g. '$1,500 - $2,800 USD' based on real analysis]",
              "score": [Write score between 1 and 100 based on value],
              "positives": ["[Bullet point 1 in Arabic]", "[Bullet point 2 in Arabic]", ...],
              "negatives": ["[Risk or negative 1 in Arabic]", "[Risk or negative 2 in Arabic]", ...],
              "marketTrends": "[Explain market trends for this sector in Arabic]",
              "suggestedUses": ["[Business use 1 in Arabic]", "[Business use 2 in Arabic]", ...]
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.5f
            )
        )

        try {
            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(Exception("Gemini returned an empty response"))

            Log.d("DomainRepository", "Gemini response: $jsonText")

            // Parse jsonText to DomainAppraisalResult
            val adapter = GeminiRetrofitClient.responseMoshi.adapter(DomainAppraisalResult::class.java)
            val result = adapter.fromJson(jsonText)
                ?: return@withContext Result.failure(Exception("Failed to parse Gemini valuation findings"))

            val positivesSerialized = result.positives.joinToString("\n")
            val negativesSerialized = result.negatives.joinToString("\n")
            val suggestedUsesSerialized = result.suggestedUses.joinToString("\n")

            val appraisal = Appraisal(
                domainName = result.domainName,
                estimatedValueRange = result.estimatedValueRange,
                score = result.score,
                positives = positivesSerialized,
                negatives = negativesSerialized,
                marketTrends = result.marketTrends,
                suggestedUses = suggestedUsesSerialized
            )

            // Save inside local database
            appraisalDao.insertAppraisal(appraisal)

            Result.success(appraisal)
        } catch (e: Exception) {
            Log.e("DomainRepository", "Error during Gemini call", e)
            Result.failure(e)
        }
    }
}
